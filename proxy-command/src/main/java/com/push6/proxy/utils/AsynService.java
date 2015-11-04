package com.push6.proxy.utils;

import com.push6.proxy.service.OfflineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Created by serv on 2015/4/24.
 */
@Service
public class AsynService {

    @Autowired
    OfflineService offlineService;


    @Async
    public void sendUdp(String channelId){
        int i = 0;
        while (i<5){
            try {
                System.out.println("send "+i);
                offlineService.notifyDevice(channelId, "asynCommand", "中文"+UUID.randomUUID().toString()+" --- "+i);
//                Thread.sleep(3000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            i++;
        }
    }
}
