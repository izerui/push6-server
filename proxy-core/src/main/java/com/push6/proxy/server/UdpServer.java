package com.push6.proxy.server;

import com.push6.proxy.server.handler.UdpServerHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;


/**
 * Created by serv on 2015/4/23.
 */
public class UdpServer implements InitializingBean, DisposableBean, Runnable {
    private static final Logger logger = LoggerFactory.getLogger(UdpServer.class);

    private int udpServerPort;
    private SocketContext socketContext;

    private ChannelFuture channelFuture;

    public UdpServer(int udpServerPort,SocketContext socketContext) {
        this.udpServerPort = udpServerPort;
        this.socketContext = socketContext;
    }

    @Override
    public void run() {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioDatagramChannel.class)
                    .option(ChannelOption.SO_BROADCAST, true)
                    .handler(new UdpServerHandler(socketContext));
            channelFuture = b.bind(udpServerPort).sync().channel().closeFuture();
            logger.info(":::开始监听 [UDP]端口: {}", udpServerPort);
            channelFuture.await();
        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            group.shutdownGracefully();
        }
    }


    public void destroy() {
        channelFuture.channel().close();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        new Thread(this).start();
    }

}
