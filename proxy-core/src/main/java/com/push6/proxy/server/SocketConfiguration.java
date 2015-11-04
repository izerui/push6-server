package com.push6.proxy.server;

import com.push6.proxy.server.support.ChannelIdRedisTemplate;
import com.push6.proxy.server.support.UdpAddressRedisTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;

/**
 * Created by serv on 2015/6/15.
 */
@Configuration
public class SocketConfiguration {

    @Bean
    ChannelIdRedisTemplate channelIdHostRedisTemplate(RedisConnectionFactory redisConnectionFactory){
        return new ChannelIdRedisTemplate(redisConnectionFactory);
    }

    @Bean
    UdpAddressRedisTemplate udpSenderRedisTemplate(RedisConnectionFactory redisConnectionFactory){
        return new UdpAddressRedisTemplate(redisConnectionFactory);
    }


}
