package com.push6.restful.registry;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.push6.zkclient.ZkClient;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.utils.ZKPaths;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by serv on 2015/3/10.
 */
public class RestfulBean implements Restful {

    private Logger logger = LoggerFactory.getLogger(RestfulBean.class);

    private String root;
    private Map<String,String> businessMap;
    protected ZkClient zkClient;

    public String getRoot() {
        return root;
    }

    public void setRoot(String root) {
        this.root = root;
    }

    public Map<String, String> getBusinessMap() {
        return businessMap;
    }

    public void setBusinessMap(Map<String, String> businessMap) {
        this.businessMap = businessMap;
    }

    public ZkClient getZkClient() {
        return zkClient;
    }

    public void setZkClient(ZkClient zkClient) {
        this.zkClient = zkClient;
    }

    public void start() {
        if(root==null||"".equals(root)){
            throw new RuntimeException("没有定义根节点");
        }
        if(businessMap==null){
            throw new RuntimeException("没有定义restful服务");
        }

        try {
            for (String businessKey : businessMap.keySet()){
                String url = businessMap.get(businessKey);
                url = url.replace("{server}", NetUtils.getLocalHost());
                url = URLEncoder.encode(url, "UTF-8");
                String path = ZKPaths.makePath(root, businessKey, url);
                //不管有没有，先删除掉再开始发布
                try{
                    zkClient.deletePath(path);
                }catch (Exception e){
                    ;
                }
                zkClient.createPath(path, CreateMode.EPHEMERAL);
                logger.info("发布reftful服务[{}]: {} 成功",businessKey,url);
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(),e);
        }
    }

    @Override
    public List<String> getRestUrls(String businessKey) {
        try{
            List<String> children = zkClient.getChildren(ZKPaths.makePath(root, businessKey));
            return Lists.transform(children, new Function<String, String>() {
                @Override
                public String apply(String s) {
                    try {
                        return URLDecoder.decode(s, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        throw new RuntimeException(e.getMessage(), e);
                    }
                }
            });
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            return null;
        }
    }

    @Override
    public String getRestUrl(String businessKey) {
        try{
            List<String> restUrls = getRestUrls(businessKey);
            if (restUrls != null&&restUrls.size()>0) {
                return restUrls.get(new Random().nextInt(restUrls.size()));
            }
        }catch (Exception e){
            logger.error(e.getMessage(),e);
        }
        return null;
    }

    @Override
    public void listener(final String businessKey, final RestfulListener listener) {
        try{
            zkClient.pathListener(ZKPaths.makePath(root, businessKey), new PathChildrenCacheListener() {
                @Override
                public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                    listener.childChange(businessKey);
                }
            });
        }catch (Exception e){
            logger.error(e.getMessage(),e);
        }
    }

}
