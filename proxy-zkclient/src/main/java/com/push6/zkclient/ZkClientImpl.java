package com.push6.zkclient;

import com.push6.zkclient.command.Command;
import com.push6.zkclient.command.connect.CloseCommand;
import com.push6.zkclient.command.connect.StateCommand;
import com.push6.zkclient.command.listener.NodeCacheListenerCommand;
import com.push6.zkclient.command.listener.PathChildrenCacheListenerCommand;
import com.push6.zkclient.command.lock.InterProcessMultiLockCommand;
import com.push6.zkclient.command.lock.InterProcessMutexCommand;
import com.push6.zkclient.command.lock.InterProcessReadWriteLockCommand;
import com.push6.zkclient.command.lock.InterProcessSemaphoreMutexCommand;
import com.push6.zkclient.command.path.*;
import com.push6.zkclient.support.ZkClientException;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.framework.recipes.locks.*;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by serv on 2015/3/10.
 */
public class ZkClientImpl implements ZkClient{

    private Logger logger = LoggerFactory.getLogger(ZkClientImpl.class);

    private CuratorFramework client;
    private ZkConfig config;
    private ConnectionStateListener connectionStateListener;


    public CuratorFramework getClient() {
        return client;
    }

    public ZkClientImpl(String connectionString) {
        this.config = new ZkConfig();
        this.config.setConnectionString(connectionString);
    }

    public ZkClientImpl(ZkConfig config) {
        this.config = config;
    }
    public ZkClientImpl(String connectionString,ConnectionStateListener connectionStateListener) {
        this.config = new ZkConfig();
        this.config.setConnectionString(connectionString);
        this.connectionStateListener = connectionStateListener;
    }

    public ZkClientImpl(ZkConfig config,ConnectionStateListener connectionStateListener) {
        this.config = config;
        this.connectionStateListener = connectionStateListener;
    }

    protected  <T extends Command> T execute(T command){
        try{
            if(client==null){
                throw new ZkClientException("未建立连接");
            }
            command.command(client);
            return command;
        }catch (Exception e){
            if(e instanceof ZkClientException){
                throw (ZkClientException)e;
            }else{
                throw new ZkClientException(e);
            }
        }
    }


    public void connect(){
        try {
            ExponentialBackoffRetry retryPolicy = new ExponentialBackoffRetry(config.getBaseSleepTimeMs(), config.getMaxRetries());
            this.client = CuratorFrameworkFactory.newClient(config.getConnectionString(), retryPolicy);

            if(connectionStateListener!=null){
                client.getConnectionStateListenable().addListener(connectionStateListener);
            }

            client.start();
            if(config.isBlockUntilConnectedOrTimedOut()){
                client.getZookeeperClient().blockUntilConnectedOrTimedOut();
            }
            logger.info("连接{}成功", config.getConnectionString());
        } catch (InterruptedException e) {
            throw new ZkClientException("zookeeper服务器连接失败!",e);
        }
    }


    @Override
    public void close() {
        execute(new CloseCommand());
    }

    @Override
    public NodeCache nodeListener(String path, NodeCacheListener listener) {
        return execute(new NodeCacheListenerCommand(path,listener)).getCache();
    }

    @Override
    public PathChildrenCache pathListener(String path, PathChildrenCacheListener listener) {
        return execute(new PathChildrenCacheListenerCommand(path,listener)).getCache();
    }

    @Override
    public void createPath(String path, byte[] data, CreateMode mode) {
        execute(new CreatePathCommand(path,data,mode));
    }

    @Override
    public void createPath(String path, byte[] data) {
        execute(new CreatePathCommand(path,data));
    }

    @Override
    public void createPath(String path,CreateMode mode){
        execute(new CreatePathCommand(path,null,mode));
    }

    @Override
    public void createPath(String path) {
        execute(new CreatePathCommand(path));
    }

    @Override
    public void deletePath(String path) {
        execute(new DeletePathCommand(path));
    }

    @Override
    public List<String> getChildren(String path) {
        return execute(new GetChildrenCommand(path)).getChildren();
    }

    @Override
    public Stat setData(String path, byte[] data) {
        return execute(new SetDataCommand(path,data)).getStat();
    }

    @Override
    public boolean existsPath(String path) {
        return execute(new ExistsPathCommand(path)).isExists();
    }

    @Override
    public InterProcessReadWriteLock getReadWriteLock(String path,byte[] data) {
        return execute(new InterProcessReadWriteLockCommand(path,data)).getLock();
    }

    @Override
    public InterProcessSemaphoreMutex getSemaphoreMutex(String path) {
        return execute(new InterProcessSemaphoreMutexCommand(path)).getMutex();
    }

    @Override
    public InterProcessMutex getMutex(String path) {
        return execute(new InterProcessMutexCommand(path)).getMutex();
    }

    @Override
    public InterProcessMultiLock getMultiLock(List<InterProcessLock> locks) {
        return execute(new InterProcessMultiLockCommand(locks)).getMultiLock();
    }
}
