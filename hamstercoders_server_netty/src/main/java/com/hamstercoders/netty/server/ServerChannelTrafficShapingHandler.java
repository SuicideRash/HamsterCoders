package com.hamstercoders.netty.server;

import com.hamstercoders.netty.status.ServerConnectionStatus;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.traffic.ChannelTrafficShapingHandler;
import io.netty.handler.traffic.TrafficCounter;

import java.net.InetSocketAddress;
import java.sql.Timestamp;
import java.util.*;

public class ServerChannelTrafficShapingHandler extends ChannelTrafficShapingHandler {

    private static List<ServerConnectionStatus> serverConnectionList = Collections.synchronizedList(new LinkedList<ServerConnectionStatus>());

    private ServerConnectionStatus serverConnectionStatus = new ServerConnectionStatus();

    public ServerChannelTrafficShapingHandler(long checkInterval) {
        super(checkInterval);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        serverConnectionStatus.setSrcIp(((InetSocketAddress) ctx.channel().remoteAddress()).getAddress().getHostAddress());
        serverConnectionStatus.setTimestamp(new Timestamp(Calendar.getInstance().getTimeInMillis()));
        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpRequest) {
            String uri = ((HttpRequest) msg).getUri();
            serverConnectionStatus.setUri(uri);
            trafficAccounting();
        }
        super.channelRead(ctx, msg);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        trafficAccounting();
        if (serverConnectionStatus.getUri() == null) {
            serverConnectionStatus.setUri("Undefined URI.");
        }
        super.channelInactive(ctx);
    }

    private void trafficAccounting() {
        TrafficCounter tc = trafficCounter();
        serverConnectionStatus.setReceivedBytes(tc.cumulativeReadBytes());
        serverConnectionStatus.setSentBytes(tc.cumulativeWrittenBytes());
        serverConnectionStatus.setThroughput(tc.lastWriteThroughput() * 1000 / tc.checkInterval());
        serverConnectionStatus.setTimestamp(new Timestamp(Calendar.getInstance().getTimeInMillis()));

        if (serverConnectionList.contains(serverConnectionStatus)) {
            serverConnectionList.remove(serverConnectionStatus);
            addToConnectionList();
        } else {
            addToConnectionList();
        }
    }

    public static ListIterator<ServerConnectionStatus> getServerConnectionListIterator() {
        List<ServerConnectionStatus> list = new LinkedList<>(serverConnectionList);
        return list.listIterator(list.size());
    }

    private void addToConnectionList() {
        serverConnectionList.add(serverConnectionStatus);
        if (serverConnectionList.size() > 16) {
            serverConnectionList.remove(0);
        }
    }

}
