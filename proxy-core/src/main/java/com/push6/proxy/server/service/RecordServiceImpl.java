package com.push6.proxy.server.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.push6.proxy.message.Protocol;
import com.push6.proxy.server.Constants;
import com.push6.proxy.server.utils.JsonUtils;
import com.push6.proxy.service.OfflineService;
import io.netty.channel.Channel;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * Created by serv on 2015/6/2.
 */
@Service
public class RecordServiceImpl implements Constants {

    private final static Logger logger = LoggerFactory.getLogger(RecordServiceImpl.class);

    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private OfflineService offlineService;

    @Async
    public void record(Channel channel, InetSocketAddress sender, Protocol.Message request, Protocol.Message response) {
        try {
//            //名单中的command 不记录历史
            BoundSetOperations<String, String> ops = redisTemplate.boundSetOps(COMMAND_RECORD_BLACKLIST);
            if (ops.isMember(request.getCommand())) {
                return;
            }
            boolean isUDP = sender != null;
            Map map = new HashMap();

            JsonNode jsonNode = JsonUtils.readValue(request.getMessage(), JsonNode.class);
            if(jsonNode.has("workKey")){
                String deviceInfo = offlineService.getDeviceInfoByWorkKey(jsonNode.path("workKey").asText());
                map.put("device_info",deviceInfo);
            }
            map.put("remote_address", isUDP ? sender.toString() : channel.remoteAddress().toString());
            map.put("server_address", InetAddress.getLocalHost().toString());
            map.put("channel_id", channel.id().asLongText());
            map.put("request_partner", request.getPartner());
            map.put("request_command", request.getCommand());
            map.put("request_sign", request.getSign());
            map.put("request_message", request.getMessage());
            if (response != null) {
                map.put("response_command", response.getCommand());
                map.put("response_sign", response.getSign());
                map.put("response_message", response.getMessage());
            }
            DateTime now = DateTime.now();
            map.put("time_stamp", now.toString("yyyy-MM-dd HH:mm:ss"));
            map.put("time_millis", now.getMillis());
            map.put("type", isUDP ? "UDP" : "TCP");
            mongoTemplate.save(map, RECORD_COLLECTION_NAME);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }


    /**
     * 根据客户端Ip查询其最近的请求记录
     *
     * @return
     */
    public List<Map> findRecord(String partner, String requestCommand, String responseCommand, String remoteIp) {
        Criteria where = new Criteria();
        if (isNotEmpty(partner)) {
            where.and("request_partner").regex(partner);
        }
        if (isNotEmpty(requestCommand)) {
            where.and("request_command").regex(requestCommand);
        }
        if (isNotEmpty(responseCommand)) {
            where.and("response_command").regex(responseCommand);
        }
        if (isNotEmpty(remoteIp)) {
            where.and("remote_address").regex(remoteIp);
        }

        Query query = Query.query(where);
        query.with(new Sort(Sort.Direction.DESC, "timeMillis"));
        query.limit(100);
        return mongoTemplate.find(query, Map.class, RECORD_COLLECTION_NAME);
    }


    public Set<String> getDataRecordBlackList(){
        return redisTemplate.boundSetOps(COMMAND_RECORD_BLACKLIST).members();
    }


    public Set<String> updateDataRecordBlackList(String command){
        BoundSetOperations<String, String> ops = redisTemplate.boundSetOps(COMMAND_RECORD_BLACKLIST);
        ops.add(command);
        return ops.members();
    }

    public Set<String> removeDataRecordBlackList(String command){
        BoundSetOperations<String, String> ops = redisTemplate.boundSetOps(COMMAND_RECORD_BLACKLIST);
        ops.remove(command);
        return ops.members();
    }



}
