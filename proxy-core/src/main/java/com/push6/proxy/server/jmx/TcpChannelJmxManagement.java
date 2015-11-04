package com.push6.proxy.server.jmx;

import com.push6.proxy.server.SocketContext;
import com.push6.proxy.server.service.RecordServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.jmx.export.annotation.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Created by serv on 2015/6/12.
 */
@Service
@ManagedResource(currencyTimeLimit = 20)
@Lazy
public class TcpChannelJmxManagement {

    @Autowired
    SocketContext socketContext;


    @Autowired
    RecordServiceImpl recordService;

    @ManagedAttribute(description = "当前TCP连接数")
    public int getTcpChannelSize() {
        return socketContext.getTcpChannelSize();
    }


    @ManagedOperation(description = "获取tcp连接列表")
    public List<String> listTcpRemoteAddress() {
        return socketContext.listTcpRemoteAddress();
    }

    @ManagedOperation(description = "关闭对应IP的所有连接")
    @ManagedOperationParameters(
            @ManagedOperationParameter(name = "ip", description = "终端IP")
    )
    public void closeTcpConnection(String ip) {
        socketContext.closeTcpConnection(ip);
    }

    @ManagedOperation(description = "查询最近100条记录")
    @ManagedOperationParameters(
            {
                    @ManagedOperationParameter(name = "partner", description = "合作者身份"),
                    @ManagedOperationParameter(name = "requestCommand", description = "请求的command命令"),
                    @ManagedOperationParameter(name = "responseCommand", description = "返回的command命令"),
                    @ManagedOperationParameter(name = "remoteIp", description = "客户端ip")
            }
    )
    public List<Map> findRecord(String partner, String requestCommand, String responseCommand, String remoteIp) {
        return recordService.findRecord(partner, requestCommand, responseCommand, remoteIp);
    }
}
