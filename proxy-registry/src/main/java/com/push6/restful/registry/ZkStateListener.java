package com.push6.restful.registry;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;

/**
 * Created by serv on 2015/4/24.
 */
public class ZkStateListener implements ConnectionStateListener{

    private Restful restfulBean;

    public void setRestfulBean(Restful restfulBean) {
        this.restfulBean = restfulBean;
    }

    @Override
    public void stateChanged(CuratorFramework client, ConnectionState newState) {
        if (newState == ConnectionState.RECONNECTED) {
            restfulBean.start();
        }
    }
}
