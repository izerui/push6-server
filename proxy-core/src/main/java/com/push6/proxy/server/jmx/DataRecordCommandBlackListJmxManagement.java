package com.push6.proxy.server.jmx;

import com.push6.proxy.server.service.RecordServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.jmx.export.annotation.*;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * Created by serv on 2015/6/15.
 */
@Service
@ManagedResource(currencyTimeLimit = 20)
@Lazy
public class DataRecordCommandBlackListJmxManagement {

    @Autowired
    RecordServiceImpl recordService;

    @ManagedAttribute(description = "不记录日志的command列表")
    public Set<String> getCommandWhiteList() {
        return recordService.getDataRecordBlackList();
    }

    @ManagedOperation(description = "添加不记录日志的command")
    @ManagedOperationParameters(
            @ManagedOperationParameter(name = "command",description = "command命令")
    )
    public Set<String> updateDataRecordBlackList(String command) {
        return recordService.updateDataRecordBlackList(command);
    }

    @ManagedOperation(description = "将command命令从日志记录黑名单中移除")
    @ManagedOperationParameters(
            @ManagedOperationParameter(name = "command",description = "command命令")
    )
    public Set<String> removeDataRecordBlackList(String command) {
        return recordService.removeDataRecordBlackList(command);
    }

}
