package com.hamstercoders.netty.server;


import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.traffic.AbstractTrafficShapingHandler;

public class ServerInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline p = socketChannel.pipeline();
        p.addLast("codec", new HttpServerCodec());
        p.addLast("traffic", new ServerChannelTrafficShapingHandler(
                AbstractTrafficShapingHandler.DEFAULT_CHECK_INTERVAL
        ));
        p.addLast("handler", new ServerHandler());
    }
}
