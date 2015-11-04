package com.push6.proxy.server.listener;

import com.push6.proxy.server.Constants;
import com.push6.proxy.server.SocketContext;
import com.push6.proxy.server.utils.JsonUtils;
import com.push6.proxy.service.NettyException;
import com.push6.zkclient.ZkClient;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.utils.ZKPaths;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 激活代理服务，监听消息节点，发送消息
 * Created by serv on 2015/2/1.
 */
@Service
public class ZookeeperNotifyListener implements Constants {

    private static final Logger logger = LoggerFactory.getLogger(ZookeeperNotifyListener.class);

    //服务激活标识位节点,临时节点在表示服务器运行正常，当active节点消失则服务器不再提供服务
    private static final String PROXY_SERVER_ACTIVE_NODE_NAME = "active";
    //监听节点，收发消息
    private static final String MESSAGE_LISTENER_NODE_NAME = "messages";

    private String currentServerActivePath;
    private String currentServerMessagesPath;

    @Autowired
    private ZkClient zkClient;

    @Autowired
    private SocketContext socketContext;

    private PathChildrenCache childrenCache;


    @Autowired
    public ZookeeperNotifyListener(@Value("${socket.port}") String port) throws UnknownHostException {
        //监听节点名称
        String nodeName = URLEncoder.encode(InetAddress.getLocalHost().toString()) + "-" + port;
        currentServerActivePath = ZKPaths.makePath(ROOT_PATH, nodeName, PROXY_SERVER_ACTIVE_NODE_NAME);
        currentServerMessagesPath = ZKPaths.makePath(ROOT_PATH, nodeName, MESSAGE_LISTENER_NODE_NAME);
    }


    @PostConstruct
    public void init() throws Exception {
        try {
            deletePath(currentServerActivePath);
            deletePath(currentServerMessagesPath);

            zkClient.createPath(currentServerActivePath, CreateMode.EPHEMERAL);
            zkClient.createPath(currentServerMessagesPath);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        beginListener();
    }

    private void deletePath(String path) {
        try {
            if (zkClient.existsPath(path)) {
                zkClient.deletePath(path);
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    //开始监听zookeeper的当前机器节点变化，当有childrenNode 则读取出来通知设备，并删除
    public void beginListener() throws Exception {
        logger.info("开始监听消息在 {}", currentServerMessagesPath);
        if(childrenCache!=null){
            childrenCache.close();
        }
        childrenCache = zkClient.pathListener(currentServerMessagesPath, new PathChildrenCacheListener() {
            @Override
            public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                switch (event.getType()) {
                    case CHILD_ADDED:
                        try {
                            //要推送的消息内容
                            Map<String, String> map = JsonUtils.readValue(event.getData().getData(), HashMap.class);
                            socketContext.writeAndFlush(map.get("channelUUID"), map.get("command"), map.get("message"));
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                        }
                        //inBackground is requeried
                        client.delete().inBackground().forPath(event.getData().getPath());
                        break;
                }
            }
        });

    }


    /**
     * 将消息写入到多台属于active状态的proxy_server的节点下，该谁处理的谁处理，否则则丢弃
     */
    public void broadcastNode(String channelUUID, String command, String message) {
        Map<String, String> map = new HashMap<String, String>();
        map.put("command", command);
        map.put("channelUUID", channelUUID);
        map.put("message", message);
        byte[] data = JsonUtils.writeValueAsBytes(map);
        List<String> proxyServers = zkClient.getChildren(ROOT_PATH);
        for (String proxyServer : proxyServers) {
            boolean active = zkClient.existsPath(ZKPaths.makePath(ROOT_PATH, proxyServer, PROXY_SERVER_ACTIVE_NODE_NAME));
            if (active) {
                //将 channelId 写入到zookeeper的 对应的 proxyHost 节点下
                zkClient.createPath(ZKPaths.makePath(ROOT_PATH, proxyServer, MESSAGE_LISTENER_NODE_NAME) + "/" +
                        UUID.randomUUID().toString(), data, CreateMode.EPHEMERAL);
            }
        }

    }


}
