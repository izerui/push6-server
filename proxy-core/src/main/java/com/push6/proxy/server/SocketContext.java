package com.push6.proxy.server;

import com.push6.proxy.message.Protocol;
import io.netty.channel.Channel;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * Created by serv on 2015/5/2.
 */
public interface SocketContext {


    /**
     * 根据redis中的channelUUID 找到对应的chanenl并发送消息
     * @param channelUUID 保存在redis中的唯一串 tcp保存的是channelId udp保存的是UdpAddress
     * @param command 命令
     * @param message 内容
     */
    public void writeAndFlush(String channelUUID,String command,String message) throws Exception;

    /**
     * tcp channel连接失效时候触发
     * @param channel 通道
     */
    public void channelInactive(Channel channel);

    /**
     * tcp捕获到异常时候触发
     * @param channel 通道
     * @param request 请求对象
     * @param cause 异常对象
     */
    public void channelExceptionCaught(Channel channel,Protocol.Message request,Throwable cause);


    /**
     * tcp 执行业务
     * @param channel 通道
     * @param request 请求对象
     */
    public void doChannelBehavior(Channel channel,Protocol.Message request) throws Exception;


    /**
     * udp 执行业务
     * @param channel 通道 默认udp通道不会变的
     * @param request 请求对象
     * @param sender 源udp报文地址
     */
    public void doDatagramBehavior(Channel channel,Protocol.Message request,InetSocketAddress sender) throws Exception;


    /**
     * udp 捕获到异常触发
     * @param channel 通道 通道 默认udp通道不会变的
     * @param request 请求对象
     * @param cause 异常对象
     */
    public void datagramExceptionCaught(Channel channel,Protocol.Message request,InetSocketAddress sender,Throwable cause);

    /**
     * 获取tcp长连接的连接数
     * @return
     */
    public int getTcpChannelSize();

    /**
     * 获取tcp的连接列表
     * @return
     */
    public List<String> listTcpRemoteAddress();

    /**
     * 通过ip关闭该ip下的所有连接
     * @param ip
     */
    public void closeTcpConnection(String ip);
}
