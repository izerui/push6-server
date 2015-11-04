package com.push6.zkclient;

import java.io.Serializable;

/**
 * Created by serv on 2015/3/11.
 */
public class ZkConfig implements Serializable {

    private String connectionString;
    private int baseSleepTimeMs = 1000;
    private int maxRetries = Integer.MAX_VALUE;
    private boolean blockUntilConnectedOrTimedOut = true;

    public String getConnectionString() {
        return connectionString;
    }

    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }

    public int getBaseSleepTimeMs() {
        return baseSleepTimeMs;
    }

    public void setBaseSleepTimeMs(int baseSleepTimeMs) {
        this.baseSleepTimeMs = baseSleepTimeMs;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public boolean isBlockUntilConnectedOrTimedOut() {
        return blockUntilConnectedOrTimedOut;
    }

    public void setBlockUntilConnectedOrTimedOut(boolean blockUntilConnectedOrTimedOut) {
        this.blockUntilConnectedOrTimedOut = blockUntilConnectedOrTimedOut;
    }
}
