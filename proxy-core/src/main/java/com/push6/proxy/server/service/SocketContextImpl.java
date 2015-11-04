package com.push6.proxy.server.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.push6.proxy.message.Protocol;
import com.push6.proxy.server.Constants;
import com.push6.proxy.server.SocketContext;
import com.push6.proxy.server.support.ChannelIdRedisTemplate;
import com.push6.proxy.server.support.TcpUdpEnum;
import com.push6.proxy.server.support.UdpAddress;
import com.push6.proxy.server.support.UdpAddressRedisTemplate;
import com.push6.proxy.server.utils.JsonUtils;
import com.push6.proxy.server.utils.MD5;
import com.push6.proxy.server.utils.NetUtils;
import com.push6.proxy.service.NettyException;
import com.push6.proxy.service.OfflineService;
import com.push6.restful.registry.Restful;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by serv on 2015/5/2.
 */
@Service
public class SocketContextImpl implements SocketContext , Constants{

    private final static Logger logger = LoggerFactory.getLogger(SocketContextImpl.class);

    private static final ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    @Autowired
    private OfflineService offlineService;
    @Autowired
    private ChannelIdRedisTemplate channelIdRedisTemplate;
    @Autowired
    private UdpAddressRedisTemplate udpAddressRedisTemplate;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private Restful restful;
    @Autowired
    private RecordServiceImpl recordService;


    @Async
    public void writeAndFlush(String channelUUID, String command,String message) throws Exception{
        //保存在redis中的channelUUID过期后不再通知
        if(!channelIdRedisTemplate.hasKey(channelUUID)){
            return;
        }

        Protocol.Message.Builder builder = Protocol.Message.newBuilder()
                .setCommand(command)
                .setMessage(message);

        if(TcpUdpEnum.isUDP(channelUUID)){//udp 推送
            UdpAddress udpAddress = udpAddressRedisTemplate.boundValueOps(channelUUID).get();
            Channel channel = channelGroup.find(udpAddress.getChannelId());
            if(channel==null){
                return;
            }
            Protocol.Message response = builder.setSign(MD5.sign(builder.getMessage(),udpAddress.getPartnerKey(), "utf-8")).build();
            //protobuf生成有分隔标志的字节数组
            ByteArrayOutputStream output = new ByteArrayOutputStream(1024);
            response.writeDelimitedTo(output);

            channel.writeAndFlush(new DatagramPacket(
                    Unpooled.wrappedBuffer(output.toByteArray()), udpAddress.getSender()));
            logger.info("↑↑↑ UDP notify: \n command: {} \n sign: {} \n message: {}\n",
                    response.getCommand(),
                    response.getSign(),
                    response.getMessage());
        }else{//tcp 推送
            ChannelId channelId = channelIdRedisTemplate.boundValueOps(channelUUID).get();
            Channel channel = channelGroup.find(channelId);
            if(channel==null){
                return;
            }
            String partnerKey = (String) channel.attr(AttributeKey.valueOf(Constants.CHANNEL_ATTRIBUTEKEY_PARTNERKEY)).get();
            Protocol.Message response = builder.setSign(MD5.sign(builder.getMessage(), partnerKey, "utf-8")).build();
            channel.writeAndFlush(response);
            logger.info("↑↑↑ TCP notify: \n command: {} \n sign: {} \n message: {}\n",
                    response.getCommand(),
                    response.getSign(),
                    response.getMessage());

        }
    }




    @Override
    public void doChannelBehavior(Channel channel, Protocol.Message request) throws Exception{
        logger.info("↓↓↓ TCP request: \n IP: {} \n partner: {} \n command: {} \n sign: {} \n message: {} \n",
                channel.remoteAddress(),
                request.getPartner(),
                request.getCommand(),
                request.getSign(),
                request.getMessage());
        long startTime = System.currentTimeMillis();

        ChannelRequestHolder holder = new ChannelRequestHolder(channel,request);

        Protocol.Message response = holder.executeRequest();

        if (response!=null) {
            channel.writeAndFlush(response);
            logger.info("↑↑↑ TCP response: \n time: {} \n command: {} \n sign: {} \n message: {}\n",
                    System.currentTimeMillis()-startTime,
                    response.getCommand(),
                    response.getSign(),
                    response.getMessage());
        }

    }


    @Override
    public void doDatagramBehavior(Channel channel, Protocol.Message request,InetSocketAddress sender) throws Exception {
        logger.info("↓↓↓ UDP request: \n IP: {} \n partner: {} \n command: {} \n sign: {} \n message: {} \n",
                sender.getAddress(),
                request.getPartner(),
                request.getCommand(),
                request.getSign(),
                request.getMessage());
        long startTime = System.currentTimeMillis();

        ChannelRequestHolder holder = new ChannelRequestHolder(channel,request,sender);

        //请求业务
        Protocol.Message response = holder.executeRequest();

        if(response!=null){
            if(response.toByteArray().length>1024){
                throw new NettyException(777,"返回数据超过大小限制");
            }
            //protobuf生成有分隔标志的字节数组
            ByteArrayOutputStream output = new ByteArrayOutputStream(1024);
            response.writeDelimitedTo(output);
            channel.writeAndFlush(new DatagramPacket(
                    Unpooled.wrappedBuffer(output.toByteArray()), sender));
            logger.info("↑↑↑ UDP response: \n time: {} \n command: {} \n sign: {} \n message: {}\n",
                    System.currentTimeMillis()-startTime,
                    response.getCommand(),
                    response.getSign(),
                    response.getMessage());
        }
    }


    @Override
    public void channelInactive(Channel channel) {
        String channelUUID = TcpUdpEnum.TCP.getChannelUUID(channel.id().asLongText());
        channelIdRedisTemplate.delete(channelUUID);
        channelGroup.remove(channel);
    }

    @Override
    public void channelExceptionCaught(Channel channel, Protocol.Message request, Throwable cause) {
        if(request==null){
            return;
        }
        String message = JsonUtils.writeValue(new ErrorMessage(cause));
        String partnerKey = offlineService.getPartnerKey(request.getPartner());
        Protocol.Message response = Protocol.Message.newBuilder()
                .setCommand(request.getCommand())
                .setMessage(message)
                .setSign(MD5.sign(message, partnerKey , "utf-8"))
                .build();
        channel.writeAndFlush(response);
    }


    @Override
    public void datagramExceptionCaught(Channel channel, Protocol.Message request, InetSocketAddress sender, Throwable cause) {
        try{
            if(request==null){
                return;
            }
            String message = JsonUtils.writeValue(new ErrorMessage(cause));
            String partnerKey = offlineService.getPartnerKey(request.getPartner());
            Protocol.Message response = Protocol.Message.newBuilder()
                    .setCommand(request.getCommand())
                    .setMessage(message)
                    .setSign(MD5.sign(message, partnerKey, "utf-8"))
                    .build();
            //protobuf生成有分隔标志的字节数组
            ByteArrayOutputStream output = new ByteArrayOutputStream(1024);
            response.writeDelimitedTo(output);
            channel.writeAndFlush(new DatagramPacket(
                    Unpooled.wrappedBuffer(output.toByteArray()), sender));
        }catch (Exception e){
            logger.error(e.getMessage(),e);
        }
    }


    @Override
    public int getTcpChannelSize() {
        return channelGroup.size();
    }

    public List<String> listTcpRemoteAddress(){
        List<String> remoteAddress = new ArrayList<String>();
        Set<String> keys = channelIdRedisTemplate.keys(TcpUdpEnum.TCP_CHANNELID_PREFIX + "*");
        Iterator<String> iterator = keys.iterator();
        while (iterator.hasNext()){
            String channelIdRedisKey = iterator.next();
            ChannelId channelId = channelIdRedisTemplate.boundValueOps(channelIdRedisKey).get();
            Channel channel = channelGroup.find(channelId);
            if(channel!=null){
                remoteAddress.add(channel.remoteAddress().toString());
            }
        }
        return remoteAddress;
    }

    @Override
    public void closeTcpConnection(String ip) {
        Set<String> keys = channelIdRedisTemplate.keys(TcpUdpEnum.TCP_CHANNELID_PREFIX + "*");
        Iterator<String> iterator = keys.iterator();
        while (iterator.hasNext()){
            String channelIdRedisKey = iterator.next();
            ChannelId channelId = channelIdRedisTemplate.boundValueOps(channelIdRedisKey).get();
            Channel channel = channelGroup.find(channelId);
            if(channel!=null&&channel.remoteAddress().toString().contains(ip)){
                try{
                    channel.close();
                    channelIdRedisTemplate.delete(channelIdRedisKey);
                }catch (Exception e){
                    logger.error(e.getMessage());
                }
            }
        }
    }

    private class ChannelRequestHolder {

        private Channel channel;
        private Protocol.Message request;
        //udp
        private InetSocketAddress sender;



        //解析后的message
        private JsonNode jsonNode;
        private String partnerKey;
        private String channelUUID;

        //tcp
        private ChannelRequestHolder(Channel channel, Protocol.Message request) {
            this.channel = channel;
            this.request = request;
            validateRequest();
            saveChannelId();
        }

        //udp
        private ChannelRequestHolder(Channel channel, Protocol.Message request, InetSocketAddress sender) {
            this.channel = channel;
            this.request = request;
            this.sender = sender;
            validateRequest();
            saveUdpAddress();
        }

        //验证请求格式、黑名单
        private void validateRequest(){
            BoundSetOperations<String, String> blackSet = redisTemplate.boundSetOps(IP_BLACKLIST);
            if(blackSet!=null){
                String remoteIp;
                if(sender!=null){
                    remoteIp = sender.getAddress().getHostAddress();
                }else{
                    remoteIp = ((InetSocketAddress)channel.remoteAddress()).getAddress().getHostAddress();
                }
                if(blackSet.isMember(remoteIp)){
                    throw new NettyException(444,"remoteIp 连接受限，无效请求!");
                }
            }
            jsonNode = JsonUtils.readValue(request.getMessage());
            partnerKey = offlineService.getPartnerKey(request.getPartner());

            if(!jsonNode.hasNonNull("nonceStr")){
                throw new NettyException(333,"缺少随机串 nonceStr");
            }
            if(!MD5.verify(request.getMessage(), request.getSign(), partnerKey, "utf-8")){
                throw new NettyException(500,"签名错误 command:"+request.getCommand());
            }
            boolean pass = offlineService.isInWhiteList(request.getCommand());
            if(!pass){
                //验证工作秘钥
                if(!jsonNode.has("workKey")){
                    throw new NettyException(232,"没有传入工作key,没有权限操作该接口");
                }
                //workkey 是否在redis的临时keys中存在
                if(!offlineService.isValid(jsonNode.path("workKey").asText())){
                    throw new NettyException(301,"验证工作Key出错");
                }
            }
        }

        //tcp 保存channelId 并返回channelUUID
        private void saveChannelId(){

            channelGroup.add(channel);

            channel.attr(AttributeKey.valueOf(CHANNEL_ATTRIBUTEKEY_PARTNERKEY)).set(partnerKey);

            channelUUID = TcpUdpEnum.TCP.getChannelUUID(channel.id().asLongText());
            channelIdRedisTemplate.boundValueOps(channelUUID)
                    .set(channel.id(), TcpUdpEnum.TCP.getExpire(), TimeUnit.MINUTES);


        }

        //保存UdpAddress 并返回channelUUID
        private void saveUdpAddress(){
            channelGroup.add(channel);

            UdpAddress udpAddress = new UdpAddress();
            udpAddress.setPartnerKey(partnerKey);
            udpAddress.setSender(sender);
            udpAddress.setChannelId(channel.id());

            channelUUID = TcpUdpEnum.UDP.getChannelUUID(UUID.randomUUID().toString());
            udpAddressRedisTemplate.boundValueOps(channelUUID).set(udpAddress, TcpUdpEnum.UDP.getExpire(), TimeUnit.MINUTES);
        }

        private Protocol.Message executeRequest() throws Exception{
            //业务地址
            String restUrl = restful.getRestUrl(request.getCommand());
            if (StringUtils.isEmpty(restUrl)) {
                throw new NettyException(667, "没有找到" + request.getCommand() + "对应的处理者");
            }

            //附加channelId
            HashMap map = JsonUtils.readValue(request.getMessage(), HashMap.class);
            map.put("channelId", channelUUID);
            //后期要删除掉。partner已经没有用了。当所有应用都升级版本后,只是为了防止restful调用失败
            map.put("partner", request.getPartner());
            logger.info("restUrl: {} \n data: {} \n", restUrl, map.toString());

            ByteArrayEntity se = new ByteArrayEntity(JsonUtils.writeValueAsBytes(map));
            se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, Constants.CONTENT_TYPE_JSON));

            //请求业务,并把redis key通知给业务。方便业务推送消息
            String responseMessage = NetUtils.post(restUrl, se);

            Protocol.Message response = null;
            if (StringUtils.isNotEmpty(responseMessage)) {
                response = Protocol.Message.newBuilder()
                        .setCommand(request.getCommand())
                        .setMessage(responseMessage)
                        .setSign(MD5.sign(responseMessage, partnerKey, "utf-8"))
                        .build();
            }
            recordService.record(channel, sender, request, response);
            return response;
        }

    }


}
