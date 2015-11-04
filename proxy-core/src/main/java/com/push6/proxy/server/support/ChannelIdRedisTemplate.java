package com.push6.proxy.server.support;

import io.netty.channel.ChannelId;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * 保存tcp请求的channelId对应
 * Created by serv on 2015/2/2.
 */
public class ChannelIdRedisTemplate extends RedisTemplate<String, ChannelId> {

    public ChannelIdRedisTemplate() {
        RedisSerializer<String> string = new StringRedisSerializer();
        JdkSerializationRedisSerializer jdk = new JdkSerializationRedisSerializer();
        setKeySerializer(string);
        setValueSerializer(jdk);
        setHashKeySerializer(string);
        setHashValueSerializer(jdk);
    }

    public ChannelIdRedisTemplate(RedisConnectionFactory connectionFactory) {
        this();
        setConnectionFactory(connectionFactory);
        afterPropertiesSet();
    }


}
