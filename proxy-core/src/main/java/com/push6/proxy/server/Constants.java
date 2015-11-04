package com.push6.proxy.server;

/**
 * Created by Administrator on 2014/12/11.
 */
public interface Constants {

    /**
     * redis 黑名单中的ip不允许连接
     */
    String IP_BLACKLIST = "O2O:IP_BLACKLIST";
    /**
     * redis 黑名单中的command 不记录日志到mongodb中
     */
    String COMMAND_RECORD_BLACKLIST = "O2O:COMMAND_RECORD_BLACKLIST";
    /**
     * redis 中保存的partner 和 partnerKey
     */
    String SOCKET_PARTNER = "O2O:SOCKET_PARTNERS";

    /**
     * 保存的workkey前缀,用来验证是否是有效请求. 有效时长为5分钟
     */
    String WORKKEY_PREFIX = "O2O:WORKKEY:";

    /**
     * 命令白名单，在白名单之内的命令不需要验证workKey
     */
    String COMMAND_WHITE_LIST = "O2O:COMMAND_WHITE_LIST";

    /**
     * tcp 保存在redis中的 channelUUID key前缀
     */
    String TCP_CHANNELID_PREFIX = "O2O:TCP_CHANNELID:";

    /**
     * udp 保存在redis中的 channelUUID key前缀
     */
    String UDP_UUID_PREFIX = "O2O:UDP_UUID:";

    /**
     * tcp redis的channelUUID超时时间 单位为分钟
     */
    int TCP_CHANNEL_UUID_TIMEOUT = 10;
    /**
     * udp redis的channelUUID超时时间 单位为分钟
     */
    int UDP_CHANNEL_UUID_TIMEOUT = 5;

    /**
     * workkey超时时间(单位分钟)
     */
    long WORKKEY_TIMEOUT = 5;

    /**
     * 请求restfulUrl的headtype
     */
    String CONTENT_TYPE_JSON = "application/json";

    /**
     * channel中源请求的partnerKey
     */
    String CHANNEL_ATTRIBUTEKEY_PARTNERKEY = "partnerKey";

    /**
     * zookeeper 终端代理服务根节点
     */
    String ROOT_PATH = "/offline";

    /**
     * 请求业务restful地址的超时时间
     */
    int REQUEST_RESTFUL_TIMEOUT  = 5;


    /**
     * 记录在mongodb中的 collection名称
     */
    String RECORD_COLLECTION_NAME = "data_record";


}
