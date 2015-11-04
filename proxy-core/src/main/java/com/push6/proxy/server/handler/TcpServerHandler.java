package com.push6.proxy.server.handler;

import com.push6.proxy.message.Protocol;
import com.push6.proxy.server.Constants;
import com.push6.proxy.server.SocketContext;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by serv on 2015/4/26.
 */
public class TcpServerHandler extends ChannelHandlerAdapter implements Constants{

    private static final Logger logger = LoggerFactory.getLogger(TcpServerHandler.class);

    private SocketContext socketContext;

    private Protocol.Message request;

    public TcpServerHandler(SocketContext socketContext) {
        this.socketContext = socketContext;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        request = (Protocol.Message) msg;

        socketContext.doChannelBehavior(ctx.channel(), request);

        super.channelRead(ctx, msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error(cause.getMessage());
        socketContext.channelExceptionCaught(ctx.channel(),request,cause);
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        socketContext.channelInactive(ctx.channel());
        ctx.close();
    }

    /**
     *  {@link io.netty.channel.ChannelPipeline}</li>
     */
    @Override
    public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        socketContext.channelInactive(ctx.channel());
        super.close(ctx, promise);
    }




}
