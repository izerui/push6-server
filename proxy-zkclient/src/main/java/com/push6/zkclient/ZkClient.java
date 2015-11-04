package com.push6.zkclient;

import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.framework.recipes.locks.*;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

import java.util.List;

/**
 * Created by serv on 2015/3/10.
 */
public interface ZkClient {
    void connect();
    void close();
    NodeCache nodeListener(String path, NodeCacheListener listener);
    PathChildrenCache pathListener(String path, PathChildrenCacheListener listener);
    void createPath(String path, byte[] data, CreateMode mode);
    void createPath(String path, byte[] data);
    void createPath(String path,CreateMode mode);
    void createPath(String path);
    void deletePath(String path);
    List<String> getChildren(String path);
    Stat setData(String path, byte[] data);
    boolean existsPath(String path);
    InterProcessMutex getMutex(String path);
    InterProcessReadWriteLock getReadWriteLock(String path,byte[] data);
    InterProcessSemaphoreMutex getSemaphoreMutex(String path);
    InterProcessMultiLock getMultiLock(List<InterProcessLock> locks);
}
