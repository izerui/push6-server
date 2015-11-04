package com.push6.proxy.server.listener;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by serv on 2015/4/24.
 */
@Service
public class ZkStateListener implements ConnectionStateListener{

    @Autowired
    private ZookeeperNotifyListener listener;

    @Override
    public void stateChanged(CuratorFramework client, ConnectionState newState) {
        if (newState == ConnectionState.RECONNECTED) {
            try {
                listener.init();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
