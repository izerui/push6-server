package com.push6.proxy.service;

import java.util.Set;

/**
 * Created by serv on 2015/1/21.
 */
public interface OfflineService {

    /**
     * 根据workKey获取设备号
     * @param workKey
     * @return
     */
    public String getDeviceInfoByWorkKey(String workKey) throws NettyException;

    /**
     * 根据设备号生成一个新的workkey
     * 建议使用{@link #createWorkKeyByDeviceInfo(String)}
     * @param deviceInfo
     * @return
     */
    @Deprecated
    public String getWorkKeyByDeviceInfo(String deviceInfo) throws NettyException;

    
    /**
     * 根据设备号生成一个新的workkey
     * @param deviceInfo
     * @return
     */
    public String createWorkKeyByDeviceInfo(String deviceInfo) throws NettyException;

    /**
     * 验证workKey是否有效
     * @param workKey
     * @return
     */
    public Boolean isValid(String workKey) throws NettyException;

    /**
     * 发送通知结果给终端
     * @param channelUUID 通知给业务的 channel唯一串
     * @param command 通知给终端的command命令
     * @param message 通知的内容 (必须是json类型,格式要求参看终端通信文档)
     * @param partner partner
     */
    @Deprecated
    public void notifyDevice(String channelUUID , String command , String message , String partner) throws NettyException;

    /**
     * 发送通知结果给终端
     * @param channelUUID 通知给业务的 channel唯一串
     * @param command 通知给终端的command命令
     * @param message 通知的内容 (必须是json类型,格式要求参看终端通信文档)
     */
    public void notifyDevice(String channelUUID,  String command , String message) throws NettyException;

    /**
     * 根据partner 获取partnerKey
     * @param partner
     * @return
     */
    public String getPartnerKey(String partner) throws NettyException;

    /**
     * 终端命令是否在白名单中，白名单中的命令不验证workkey
     * @param command
     * @return
     */
    public boolean isInWhiteList(String command) throws NettyException;


    /**
     * 获取command的白名单列表
     * @return
     * @throws NettyException
     */
    public Set<String> getCommandWhiteList() throws NettyException;

    /**
     * 设置command到白名单
     * @param command
     * @return
     * @throws NettyException
     */
    public Set<String> addCommandWhiteList(String command) throws NettyException;

    /**
     * 从白名单中移除掉
     * @param command
     * @return
     * @throws NettyException
     */
    public Set<String> removeCommandFromWhiteList(String command) throws NettyException;

    /**
     * 延长workkey的有效期
     * @param workKey
     */
    public void expireWorkKey(String workKey,int minutes) throws NettyException;

}
