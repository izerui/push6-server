//package com.push6.proxy.command;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.push6.data.rbac.service.DeviceInfoService;
//import com.push6.proxy.service.OfflineService;
//import com.push6.proxy.utils.MD5;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.util.StringUtils;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.HashMap;
//import java.util.Map;
//import java.util.UUID;
//
///**
// * Created by serv on 2015/4/27.
// */
//@RestController
//public class WorkKeyCommand {
//
//
//    @Autowired
//    DeviceInfoService deviceInfoService;
//
//    @Autowired
//    OfflineService offlineService;
//
//    @RequestMapping("/workKeyCommand")
//    public Map<String,String> workKeyCommand(@RequestBody JsonNode jsonNode){
//        Map<String,String> resultMap = new HashMap<String,String>();
//        try{
//            String deviceInfo = jsonNode.path("deviceInfo").asText();
//            String password = jsonNode.path("password").asText();
//            resultMap.put("nonceStr",StringUtils.replace(UUID.randomUUID().toString(), "-", ""));
//            //验证设备的账号密码 , 该接口提供者需要做相应的缓存
//            if(deviceInfoService.checkDeviceMD5Password(deviceInfo, MD5.md5(password))){
//                String workKey = offlineService.createWorkKeyByDeviceInfo(deviceInfo);
//                resultMap.put("workKey", workKey);
//            }else{
//                resultMap.put("errorCode","500");
//                resultMap.put("errorMsg","获取工作key失败");
//            }
//            return resultMap;
//        }catch (Exception e){
//            resultMap.put("errorCode","error workKey");
//            resultMap.put("errorMsg",e.getMessage());
//            return resultMap;
//        }
//
//    }
//}
