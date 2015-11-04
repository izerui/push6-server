package com.push6.restful.registry;

import java.util.List;

/**
 * Created by serv on 2015/3/10.
 */
public interface Restful {

    /**
     * 注册到zookeeper服务器restful服务
     */
    void start();

    /**
     * 通过一个业务主键获取下面所有的临时resturl，没有则返回null
     * @param businessKey
     * @return
     */
    List<String> getRestUrls(String businessKey);

    /**
     * 通过负载均衡算法获取一个resturl，没有返回null
     * @param businessKey
     * @return
     */
    String getRestUrl(String businessKey);

    /**
     * 监听一个业务主键下的resturl事件
     * @param businessKey
     * @param listener
     */
    void listener(String businessKey, RestfulListener listener);


}
