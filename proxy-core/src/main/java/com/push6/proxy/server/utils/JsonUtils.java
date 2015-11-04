package com.push6.proxy.server.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.push6.proxy.service.NettyException;

import java.io.IOException;

/**
 * Created by serv on 2014/12/12.
 */
public abstract class JsonUtils {

    private final static ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper();
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        objectMapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true) ;
        objectMapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
    }

    public static JsonNode readValue(String message){
        try {
            return objectMapper.readValue(message, JsonNode.class);
        } catch (IOException e) {
            throw new NettyException(989,"请求格式不对:"+e.getMessage());
        }
    }
    public static <T> T readValue(String message,Class<T> tClass){
        try {
            return objectMapper.readValue(message, tClass);
        } catch (IOException e) {
            throw new NettyException(989,"请求格式不对:"+e.getMessage());
        }
    }
    public static <T> T readValue(byte[] message,Class<T> tClass){
        try {
            return objectMapper.readValue(message, tClass);
        } catch (IOException e) {
            throw new NettyException(989,"请求格式不对:"+e.getMessage());
        }
    }

    public static String writeValue(Object object){
        try {
            return objectMapper.writeValueAsString(object);
        } catch (IOException e) {
            throw new NettyException(989,"请求格式不对:"+e.getMessage());
        }
    }
    public static byte[] writeValueAsBytes(Object object){
        try {
            return objectMapper.writeValueAsBytes(object);
        } catch (IOException e) {
            throw new NettyException(989,"请求格式不对:"+e.getMessage());
        }
    }
}
