package com.push6.proxy.command;

import com.fasterxml.jackson.databind.JsonNode;
import com.push6.proxy.service.OfflineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by serv on 2015/4/27.
 */
@RestController
public class HeartBeatCommand {

    @Autowired
    OfflineService offlineService;

    @RequestMapping("/heartBeatCommand")
    public String heartBeatCommand(@RequestBody JsonNode jsonNode){
        try{
            String workKey = jsonNode.path("workKey").asText();
            if(!offlineService.isValid(workKey)){
                throw new RuntimeException("无效的workKey");
            }
            offlineService.expireWorkKey(workKey,5);
        }catch (Exception e){
            return "{\"errorCode\":\"error workKey\",\"errorMsg\":\""+e.getMessage()+"\"}";
        }
        return "{\"status\":\"SUCCESS\"}";
    }
}
