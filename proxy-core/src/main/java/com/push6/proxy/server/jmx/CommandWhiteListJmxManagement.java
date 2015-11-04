package com.push6.proxy.server.jmx;

import com.push6.proxy.service.OfflineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.jmx.export.annotation.*;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * Created by serv on 2015/6/13.
 */
@Service
@ManagedResource(currencyTimeLimit = 20)
@Lazy
public class CommandWhiteListJmxManagement {

    @Autowired
    OfflineService offlineService;


    @ManagedAttribute(description = "command白名单集合")
    public Set<String> getCommandWhiteList() {
        return offlineService.getCommandWhiteList();
    }

    @ManagedOperation(description = "设置到command白名单")
    @ManagedOperationParameters(
            @ManagedOperationParameter(name = "command", description = "command命令")
    )
    public Set<String> addCommandWhiteList(String command) {
        return offlineService.addCommandWhiteList(command);
    }

    @ManagedOperation(description = "将command命令从白名单中移除")
    @ManagedOperationParameters(
            @ManagedOperationParameter(name = "command", description = "command命令")
    )
    public Set<String> removeCommandFromWhiteList(String command){
        return offlineService.removeCommandFromWhiteList(command);
    }

}
