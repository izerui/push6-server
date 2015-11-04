package com.push6.proxy.server;

import com.push6.proxy.message.Protocol;
import com.push6.proxy.server.handler.TcpServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * Created by rocky on 14-9-9.
 */
public class TcpServer implements InitializingBean, DisposableBean,Runnable{

    private static final Logger logger = LoggerFactory.getLogger(TcpServer.class);

    private int socketPort;

    private SocketContext socketContext;

    private ChannelFuture channelFuture = null;

    public TcpServer(int socketPort,SocketContext socketContext) {
        this.socketPort = socketPort;
        this.socketContext = socketContext;
    }

    public void run() {
        try{
            EventLoopGroup bossGroup = new NioEventLoopGroup();
            EventLoopGroup workerGroup = new NioEventLoopGroup();

            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new ProtobufVarint32FrameDecoder());
                            socketChannel.pipeline().addLast(new ProtobufDecoder(Protocol.Message.getDefaultInstance()));
                            socketChannel.pipeline().addLast(new ProtobufVarint32LengthFieldPrepender());
                            socketChannel.pipeline().addLast(new ProtobufEncoder());
                            socketChannel.pipeline().addLast(new TcpServerHandler(socketContext));
                        }
                    })
                    //最大客户端等待队列
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    //禁用纳格算法，将数据立即发送出去
                    .option(ChannelOption.TCP_NODELAY,true);

            logger.info(":::开始监听 [TCP]端口: {}", socketPort);
            channelFuture = bootstrap.bind(socketPort).sync();
            channelFuture.channel().closeFuture().sync();
        }catch (Exception e){
            logger.error(e.getMessage(), e);
            destroy();
        }
    }

    public void destroy(){
        channelFuture.channel().close();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        new Thread(this).start();
    }


}
