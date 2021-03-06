package com.push6.proxy.server;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Created by serv on 2015/6/15.
 */
@SpringBootApplication
@EnableAsync
@ImportResource({"classpath:dubbo.xml","classpath:restful.xml","classpath:zookeeper.xml"})
public class Application {

    @Bean
    UdpServer udpServer(@Value("${socket.port}") int udpServerPort,SocketContext socketContext) throws Exception {
        return new UdpServer(udpServerPort,socketContext);
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
