package com.push6.zkclient.command.path;

import com.push6.zkclient.command.Command;
import com.push6.zkclient.support.ZkClientException;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.utils.EnsurePath;
import org.apache.curator.utils.ZKPaths;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

/**
 * 创建path，注意临时节点不可以有子节点
 * Created by serv on 2015/3/9.
 */
public class CreatePathCommand implements Command {

    protected String path;
    protected byte[] data;
    protected CreateMode mode = CreateMode.PERSISTENT;

    /**
     * 创建path
     * @param path
     */
    public CreatePathCommand(String path) {
        this.path = path;
    }

    /**
     * 创建path，并设置data
     * @param path
     * @param data
     */
    public CreatePathCommand(String path, byte[] data) {
        this.path = path;
        this.data = data;
    }

    /**
     * 创建path，并设置类型和data
     * @param path
     * @param data
     * @param mode
     */
    public CreatePathCommand(String path, byte[] data, CreateMode mode) {
        this.path = path;
        this.data = data;
        this.mode = mode;
    }

    @Override
    public void command(CuratorFramework client) throws Exception {
        Stat stat = client.checkExists().forPath(path);
        if(stat!=null){
            throw new ZkClientException("节点已存在:"+path);
        }
        ensureParent(client);
        if(data!=null){
            client.create().withMode(mode).forPath(path,data);
        }else{
            client.create().withMode(mode).forPath(path);
        }
    }

    /**
     * 保证父节点路径必须已经创建，并且都是持久化节点
     * @param client
     * @throws Exception
     */
    private void ensureParent(CuratorFramework client) throws Exception {
        String parentPath = ZKPaths.getPathAndNode(path).getPath();
        EnsurePath parent = new EnsurePath(parentPath);
        parent.ensure(client.getZookeeperClient());
    }


}
