package com.push6.proxy.server;

import org.fusesource.mqtt.client.BlockingConnection;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.QoS;

/**
 * Created by serv on 2015/7/17.
 */
public class Publish {
    public static void main(String[] args) throws Exception {
        MQTT mqtt = new MQTT();
        mqtt.setHost("115.28.168.237", 1883);
//        mqtt.setHost("tcp://localhost:1883");
        BlockingConnection connection = mqtt.blockingConnection();
        connection.connect();

        connection.publish("foo", "Hello".getBytes(), QoS.AT_LEAST_ONCE, false);

    }
}
