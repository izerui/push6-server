package com.push6.zkclient.command.path;

import com.push6.zkclient.command.Command;
import org.apache.curator.framework.CuratorFramework;

import java.util.List;

/**
 * 获取path的子目录
 * Created by serv on 2015/3/9.
 */
public class GetChildrenCommand implements Command {

    protected String path;
    private List<String> children;

    public GetChildrenCommand(String path) {
        this.path = path;
    }

    @Override
    public void command(CuratorFramework client) throws Exception {
        children = client.getChildren().forPath(path);
    }

    public List<String> getChildren() {
        return children;
    }
}
