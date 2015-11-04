package com.push6.zkclient.command.connect;

import com.push6.zkclient.command.Command;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.imps.CuratorFrameworkState;

/**
 * Created by serv on 2015/3/10.
 */
public class StateCommand implements Command {

    private CuratorFrameworkState state;

    public StateCommand() {
    }

    @Override
    public void command(CuratorFramework client) throws Exception {
        state = client.getState();
    }

    public CuratorFrameworkState getState() {
        return state;
    }
}
