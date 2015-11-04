package com.push6.proxy.server.support;

import io.netty.channel.ChannelId;

import java.io.Serializable;
import java.net.InetSocketAddress;

/**
 * Created by serv on 2015/5/1.
 */
public class UdpAddress implements Serializable {

    //原地址
    private InetSocketAddress sender;
    private ChannelId channelId;
    //原请求的partnerKey 用于签名
    private String partnerKey;

    public InetSocketAddress getSender() {
        return sender;
    }

    public void setSender(InetSocketAddress sender) {
        this.sender = sender;
    }

    public ChannelId getChannelId() {
        return channelId;
    }

    public void setChannelId(ChannelId channelId) {
        this.channelId = channelId;
    }

    public String getPartnerKey() {
        return partnerKey;
    }

    public void setPartnerKey(String partnerKey) {
        this.partnerKey = partnerKey;
    }
}
