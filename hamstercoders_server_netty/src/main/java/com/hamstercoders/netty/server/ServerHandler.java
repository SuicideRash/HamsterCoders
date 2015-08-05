package com.hamstercoders.netty.server;

import com.hamstercoders.netty.status.Dao;
import com.hamstercoders.netty.status.RedirectRequestStatus;
import com.hamstercoders.netty.status.ServerConnectionStatus;
import com.hamstercoders.netty.status.ServerRequestStatus;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.*;
import io.netty.util.*;
import io.netty.util.concurrent.ImmediateEventExecutor;

import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpHeaders.is100ContinueExpected;
import static io.netty.handler.codec.http.HttpHeaders.*;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.*;

public class ServerHandler extends ChannelInboundHandlerAdapter {

    private static Timer timer = new HashedWheelTimer();
    private static ByteBuf content = null;

    private static DefaultChannelGroup allChannels = new DefaultChannelGroup("netty-receiver", ImmediateEventExecutor.INSTANCE);
    private static final String HELLO = "/hello";
    private static final String STATUS = "/status";
    private static final String REDIRECT = "/redirect?url=";
    private Dao dao = new Dao();

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        allChannels.add(ctx.channel());
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        FullHttpResponse response = null;
        if (msg instanceof HttpRequest) {
            HttpRequest req = (HttpRequest) msg;
            String uri = req.getUri();
            boolean keepAlive = HttpHeaders.isKeepAlive(req);

            if (is100ContinueExpected(req)) {
                ctx.write(new DefaultFullHttpResponse(HTTP_1_1, CONTINUE));
            }

            if (HELLO.equals(uri)) {
                ServerRequestStatus sReqRecord = new ServerRequestStatus(
                        ((InetSocketAddress) ctx.channel().remoteAddress())
                                .getHostString());
                dao.mergeServerRequestStatus(sReqRecord);

                StringBuilder buf = new StringBuilder()
                        .append("<!DOCTYPE html>\n")
                        .append("<html><head><meta charset=\"utf-8\"><title>\n")
                        .append("Hello page\n")
                        .append("</title></head><body>\n Hello World <br>\n")
                        .append("</ul></body></html>\n");
                ByteBuf buffer = Unpooled.copiedBuffer(buf, CharsetUtil.UTF_8);

                response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer(buffer));
                response.headers().set(CONTENT_TYPE, "text/html; charset=UTF-8");
                response.headers().set(CONTENT_LENGTH, response.content().readableBytes());

                timer.newTimeout(new HelloWorldTimerTask(ctx, response, keepAlive), 10, TimeUnit.SECONDS);
            } else if (STATUS.equals(uri)) {
                ServerRequestStatus sReqRecord = new ServerRequestStatus(
                        ((InetSocketAddress) ctx.channel().remoteAddress())
                                .getHostString());
                dao.mergeServerRequestStatus(sReqRecord);

                content = Unpooled.unreleasableBuffer(Unpooled.copiedBuffer(
                        generateStatus(), CharsetUtil.UTF_8));
                response = new DefaultFullHttpResponse(HTTP_1_1, OK, content.duplicate());
                writeToResponse(ctx, response, keepAlive);
            } else if (uri.length() > 13 && REDIRECT.equals(uri.substring(0, 14))) {
                dao.mergeRedirectRequestStatus(new RedirectRequestStatus(uri.substring(14),
                        ((InetSocketAddress) ctx.channel().remoteAddress()).getHostString()));

                response = new DefaultFullHttpResponse(HTTP_1_1, MOVED_PERMANENTLY);
                response.headers().set(LOCATION, "https://www." + uri.substring(14));
                response.headers().set(CONTENT_LENGTH, response.content().readableBytes());
                writeToResponse(ctx, response, keepAlive);
            } else {
                ServerRequestStatus sReqRecord = new ServerRequestStatus(
                        ((InetSocketAddress) ctx.channel().remoteAddress())
                                .getHostString());
                dao.mergeServerRequestStatus(sReqRecord);

                StringBuilder buf = new StringBuilder()
                        .append("<!DOCTYPE html>\n")
                        .append("<html><head><meta charset=\"utf-8\"><title>\n")
                        .append("Default page\n")
                        .append("</title></head><body>\n Default page <br>\n")
                        .append("</ul></body></html>\n");
                ByteBuf buffer = Unpooled.copiedBuffer(buf, CharsetUtil.UTF_8);

                response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer(buffer));
                response.headers().set(CONTENT_TYPE, "text/html; charset=UTF-8");
                response.headers().set(CONTENT_LENGTH, response.content().readableBytes());
                writeToResponse(ctx, response, keepAlive);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    private String generateStatus() throws SQLException {
        List<ServerRequestStatus> serverRequestList = dao.getServerRequest();
        Map<String, Long> redirectRequestMap = dao.getRedirectRequest();
        StringBuilder sb = new StringBuilder();
        sb.append(
                "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\"><head><meta charset=\"utf-8\">")
                .append(

                        "<head><style>")
                .append("#table_srequests{float:left;}#table_rrequests{float:left;}")
                .append("#table_sconnections{width: 60%;}")
                .append("table,td{border: 1px solid black;}")
                .append("tbody {height: 300px; overflow: auto;}")
                .append("th{border: 0px; width: 183px;}")
                .append("td {width: 150px; padding: 3px 10px; height:40px}")
                .append("caption{font-size: 16pt; font: bold;}")
                .append("thead > tr, tbody{ display:block;}}</style></head>")

                .append("Server request count: ")
                .append(dao.getServerRequestCount())
                .append("<br>Server unique requests count: ")
                .append(dao.getServerRequestUniqueCount())

                .append("<br><table id=\"table_srequests\"><caption>Server requests")
                .append("</caption>")
                .append("<thead><tr> <th>IP</th><th>Count</th>")
                .append("<th>Last Request</th> </tr></thead><tbody> ");
        for (ServerRequestStatus record : serverRequestList) {
            sb.append("<tr><td>")
                    .append(record.getSrcIp())
                    .append("</td><td>")
                    .append(record.getRequestCount())
                    .append("</td><td>")
                    .append(DateFormat.getDateTimeInstance().format(
                            record.getLastRequest())).append("</td></tr>");
        }
        sb.append("</tbody></table>");

        sb.append(
                "<table id=\"table_rrequests\"><caption>Redirect requests")
                .append("</caption>")
                .append("<thead><tr><th>URL</th>")
                .append("<th>Count</th> </tr></thead><tbody> ");
        for (String record : redirectRequestMap.keySet()) {
            sb.append("<tr><td>").append(record)
                    .append("</td><td>").append(redirectRequestMap.get(record))
                    .append("</td>");
        }
        sb.append("</tbody></table>");

        sb.append(
                "<table id=\"table_sconnections\"><caption>Last 16 connections")
                .append("</caption>")
                .append("<tr> <th>IP</th><th>URI</th>")
                .append("<th>TimeStamp</th><th>Sent</th><th>Recieved</th><th>Speed</th> </tr> ");
        ListIterator<ServerConnectionStatus> iterator = ServerChannelTrafficShapingHandler
                .getServerConnectionListIterator();
        synchronized (iterator) {
            while (iterator.hasPrevious()) {
                ServerConnectionStatus item = iterator.previous();
                sb.append("<tr><td>")
                        .append(item.getSrcIp())
                        .append("</td><td>")
                        .append(item.getUri())
                        .append("</td><td>")
                        .append(DateFormat.getDateTimeInstance().format(
                                item.getTimestamp())).append("</td><td>")
                        .append(item.getSentBytes()).append("</td><td>")
                        .append(item.getReceivedBytes()).append("</td><td>")
                        .append(item.getThroughput()).append("</td></tr>");
            }
        }
        sb.append("</table>");
        sb.append("<br>Open connections: ").append(allChannels.size());
        return sb.toString();
    }

    private void writeToResponse(ChannelHandlerContext ctx, FullHttpResponse response, boolean keepAlive) {
        response.headers().set(CONTENT_TYPE, "text/html");
        response.headers().set(CONTENT_LENGTH, response.content().readableBytes());

        if (!keepAlive) {
            ctx.write(response).addListener(ChannelFutureListener.CLOSE);
        } else {
            response.headers().set(CONNECTION, Values.KEEP_ALIVE);
            ctx.write(response);
        }
    }

    private class HelloWorldTimerTask implements TimerTask {
        private ChannelHandlerContext ctx;
        private FullHttpResponse response;
        private boolean keepAlive;

        public HelloWorldTimerTask(ChannelHandlerContext ctx,
                                   FullHttpResponse response, boolean keepAlive) {
            setCtx(ctx);
            setResponse(response);
            setKeepAlive(keepAlive);
        }

        @Override
        public void run(Timeout timeout) throws Exception {
            writeToResponse(ctx, response, keepAlive);
            ctx.flush();
        }

        public void setCtx(ChannelHandlerContext ctx) {
            this.ctx = ctx;
        }

        public void setResponse(FullHttpResponse response) {
            this.response = response;
        }

        public void setKeepAlive(boolean keepAlive) {
            this.keepAlive = keepAlive;
        }
    }
}
