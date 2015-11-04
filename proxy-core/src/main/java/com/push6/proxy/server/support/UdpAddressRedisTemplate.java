package com.push6.proxy.server.support;

import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * 保存udp请求的InetSocketAddress对应
 * Created by serv on 2015/5/1.
 */
public class UdpAddressRedisTemplate extends RedisTemplate<String, UdpAddress> {

    public UdpAddressRedisTemplate() {
        RedisSerializer<String> string = new StringRedisSerializer();
        JdkSerializationRedisSerializer jdk = new JdkSerializationRedisSerializer();
        setKeySerializer(string);
        setValueSerializer(jdk);
        setHashKeySerializer(string);
        setHashValueSerializer(jdk);
    }

    public UdpAddressRedisTemplate(RedisConnectionFactory connectionFactory) {
        this();
        setConnectionFactory(connectionFactory);
        afterPropertiesSet();
    }

}
