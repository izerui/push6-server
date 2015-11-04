package com.push6.proxy.server.handler;

import com.push6.proxy.message.Protocol;
import com.push6.proxy.server.SocketContext;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.net.InetSocketAddress;

/**
 * Created by serv on 2015/4/23.
 */
public class UdpServerHandler extends SimpleChannelInboundHandler<DatagramPacket>{

    private static final Logger logger = LoggerFactory.getLogger(UdpServerHandler.class);

    private SocketContext socketContext;

    private Protocol.Message request;
    private InetSocketAddress sender;

    public UdpServerHandler(SocketContext socketContext) {
        this.socketContext = socketContext;
    }

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
        ByteBuf buf = msg.copy().content();
        byte[] req = new byte[buf.readableBytes()];
        buf.readBytes(req);

        sender = msg.sender();
        request = Protocol.Message.parseDelimitedFrom(new ByteArrayInputStream(req));

        socketContext.doDatagramBehavior(ctx.channel(),request,sender);
    }


    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }


    /**
     *  {@link io.netty.channel.ChannelPipeline}</li>
     */
    @Override
    public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        super.close(ctx, promise);
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error(cause.getMessage());
        socketContext.datagramExceptionCaught(ctx.channel(), request, sender, cause);
        // We don't close the channel because we can keep serving requests.
    }
}
