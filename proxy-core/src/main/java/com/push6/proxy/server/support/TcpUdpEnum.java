package com.push6.proxy.server.support;

import com.push6.proxy.server.Constants;

/**
 * Created by serv on 2015/5/1.
 */
public enum TcpUdpEnum implements Constants{
    TCP(TCP_CHANNELID_PREFIX,TCP_CHANNEL_UUID_TIMEOUT),UDP(UDP_UUID_PREFIX,UDP_CHANNEL_UUID_TIMEOUT);

    //redis中的channelUUID超时时间 单位为分钟
    private int expire;
    //redis中的channelUUID 前缀
    private String prefix;

    TcpUdpEnum(String prefix, int expire) {
        this.prefix = prefix;
        this.expire = expire;
    }

    public int getExpire() {
        return expire;
    }

    public String getChannelUUID(String uuid){
        return prefix+uuid;
    }

    public static boolean isUDP(String channelUUID){
        return channelUUID.startsWith(UDP.prefix);
    }
}
