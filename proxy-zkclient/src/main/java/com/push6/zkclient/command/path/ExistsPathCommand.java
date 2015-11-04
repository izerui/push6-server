package com.push6.zkclient.command.path;

import com.push6.zkclient.command.Command;
import org.apache.curator.framework.CuratorFramework;

/**
 * Created by serv on 2015/4/26.
 */
public class ExistsPathCommand implements Command {

    private String path;
    private boolean isExists;

    public ExistsPathCommand(String path) {
        this.path = path;
    }

    @Override
    public void command(CuratorFramework client) throws Exception {
        isExists = client.checkExists().forPath(path)!=null;
    }

    public boolean isExists() {
        return isExists;
    }
}
