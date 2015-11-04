package com.push6.proxy.server;

import org.fusesource.mqtt.client.*;

import java.net.URISyntaxException;

/**
 * Created by serv on 2015/7/17.
 */
public class MqttClient {

    public static void main(String[] args) throws Exception {
        MQTT mqtt = new MQTT();
        mqtt.setHost("115.28.168.237", 1883);
//        mqtt.setHost("tcp://localhost:1883");
        BlockingConnection connection = mqtt.blockingConnection();
        connection.connect();

        connection.publish("foo", "Hello".getBytes(), QoS.AT_LEAST_ONCE, false);


//        Topic[] topics = {new Topic("foo", QoS.AT_LEAST_ONCE)};
//        byte[] qoses = connection.subscribe(topics);

        Message message = connection.receive();
        System.out.println(message.getTopic());
        byte[] payload = message.getPayload();
        System.out.println(new String(payload,"utf-8"));
// process the message then:
        message.ack();



    }
}
