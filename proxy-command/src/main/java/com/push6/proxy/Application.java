package com.push6.proxy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.scheduling.annotation.EnableAsync;

import java.nio.charset.Charset;

/**
 * Created by serv on 2015/4/23.
 */
@SpringBootApplication
@EnableAsync
@ImportResource({"classpath:dubbo.xml","classpath:restful.xml"})
public class Application {

    @Bean
    public StringHttpMessageConverter stringHttpMessageConverter(){
        return new StringHttpMessageConverter(Charset.forName("utf-8"));
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class,args);
    }
}
