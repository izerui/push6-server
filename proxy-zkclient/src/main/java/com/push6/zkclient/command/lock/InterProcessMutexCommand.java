package com.push6.zkclient.command.lock;

import com.push6.zkclient.command.Command;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.framework.recipes.locks.LockInternalsDriver;

/**
 * 可重入锁
 * Created by serv on 2015/4/26.
 */
public class InterProcessMutexCommand implements Command {

    private String path;
    private LockInternalsDriver lockInternalsDriver;
    private InterProcessMutex mutex;

    public InterProcessMutexCommand(String path) {
        this.path = path;
    }

    public InterProcessMutexCommand(String path, LockInternalsDriver lockInternalsDriver) {
        this.path = path;
        this.lockInternalsDriver = lockInternalsDriver;
    }

    @Override
    public void command(CuratorFramework client) throws Exception {
        if(lockInternalsDriver==null){
            mutex = new InterProcessMutex(client,path);
        }else{
            mutex = new InterProcessMutex(client,path,lockInternalsDriver);
        }
    }

    public InterProcessMutex getMutex() {
        return mutex;
    }
}
