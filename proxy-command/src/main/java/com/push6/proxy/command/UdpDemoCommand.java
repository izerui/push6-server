package com.push6.proxy.command;

import com.fasterxml.jackson.databind.JsonNode;
import com.push6.proxy.service.OfflineService;
import com.push6.proxy.utils.AsynService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by serv on 2015/4/23.
 */
@Controller
public class UdpDemoCommand {

    @Autowired
    OfflineService offlineService;

    @Autowired
    AsynService asynService;

    @RequestMapping("/udpDemoCommand")
    @ResponseBody
    public String udpDemoCommand(@RequestBody JsonNode jsonNode){
        System.out.println(jsonNode.toString());
        asynService.sendUdp(jsonNode.path("channelId").asText());
        return "这是一个测试";
    }
}
