package com.push6.proxy.server.service;

import com.push6.proxy.server.Constants;
import com.push6.proxy.server.listener.ZookeeperNotifyListener;
import com.push6.proxy.service.NettyException;
import com.push6.proxy.service.OfflineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.apache.commons.lang3.StringUtils.*;

/**
 * Created by serv on 2015/1/21.
 */
@Service
public class OfflineServiceImpl implements OfflineService{

    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private ZookeeperNotifyListener zookeeperNotifyListener;

    @Override
    public String getDeviceInfoByWorkKey(String workKey) {
        String key = Constants.WORKKEY_PREFIX+workKey;
        if(!redisTemplate.hasKey(key)){
            return null;
        }
        return redisTemplate.boundValueOps(key).get();
    }

    @Override
    public String getWorkKeyByDeviceInfo(String deviceInfo) {
        return createWorkKeyByDeviceInfo(deviceInfo);
    }

    @Override
    public String createWorkKeyByDeviceInfo(String deviceInfo) {
        String workKey = replace(UUID.randomUUID().toString(), "-", "");
        //workkey 放入redis 5分钟有效
        redisTemplate.boundValueOps(Constants.WORKKEY_PREFIX + workKey).set(deviceInfo, Constants.WORKKEY_TIMEOUT, TimeUnit.MINUTES);
        return workKey;
    }

    @Override
    public Boolean isValid(String workKey) {
        String key = Constants.WORKKEY_PREFIX+workKey;
        return redisTemplate.hasKey(key)&& isNotEmpty(redisTemplate.boundValueOps(key).get());
    }

    @Override
    public void notifyDevice(String channelUUID ,String command, String message) {
        if(isEmpty(channelUUID)){
            throw new NettyException(342,"channelUUID 不能为空");
        }
        if(isEmpty(command)){
            throw new NettyException(343,"command不能为空");
        }
        if(isEmpty(message)){
            throw new NettyException(344,"消息不能为空");
        }
        zookeeperNotifyListener.broadcastNode(channelUUID,command,message);
    }

    @Deprecated
    @Override
    public void notifyDevice(String channelUUID , String command , String message , String partner) {
        notifyDevice(channelUUID, command, message);
    }

    public String getPartnerKey(String partner){
        //根据request的 partner 从redis中获取对应的 partnerKey 来签名验证
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(Constants.SOCKET_PARTNER);
        if(!hashOps.hasKey(Constants.SOCKET_PARTNER)){
            hashOps.put("demoPartner","1234567890");
        }
        String partnerKey = hashOps.get(partner);
        if(isEmpty(partnerKey)){
            throw new NettyException(756,"没有找到partner: "+partner+" 对应的partnerKey");
        }
        return partnerKey;
    }

    @Override
    public boolean isInWhiteList(String command) {
        BoundSetOperations<String, String> whiteListOps = redisTemplate.boundSetOps(Constants.COMMAND_WHITE_LIST);
        boolean pass = command.equals("workKeyCommand")||whiteListOps.isMember(command);
        return pass;
    }

    @Override
    public Set<String> getCommandWhiteList() throws NettyException {
        BoundSetOperations<String, String> whiteListOps = redisTemplate.boundSetOps(Constants.COMMAND_WHITE_LIST);
        return whiteListOps.members();
    }

    @Override
    public Set<String> addCommandWhiteList(String command) throws NettyException {
        BoundSetOperations<String, String> whiteListOps = redisTemplate.boundSetOps(Constants.COMMAND_WHITE_LIST);
        whiteListOps.add(command);
        return whiteListOps.members();
    }

    @Override
    public Set<String> removeCommandFromWhiteList(String command) throws NettyException {
        BoundSetOperations<String, String> whiteListOps = redisTemplate.boundSetOps(Constants.COMMAND_WHITE_LIST);
        whiteListOps.remove(command);
        return whiteListOps.members();
    }

    @Override
    public void expireWorkKey(String workKey,int minutes) {
        redisTemplate.boundValueOps(Constants.WORKKEY_PREFIX + workKey).expire(5, TimeUnit.MINUTES);
    }
}
