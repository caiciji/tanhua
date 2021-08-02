package com.tanhua.server.service;

import com.tanhua.domain.mongo.UserLocation;
import com.tanhua.dubbo.api.mongo.UserLocationApi;
import com.tanhua.server.interceptor.UserHolder;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class LocationService {

    @Reference
    private UserLocationApi userLocationApi;


    /**
     * 上报地理信息
     * @param paramMap
     */
    public void addLocation(Map<String, Object> paramMap) {
        //1.获取纬度
        Double latitude = (Double) paramMap.get("latitude");
        //2.获取经度
        Double longitude = (Double) paramMap.get("longitude");
        //3.获取地理描述
        String addrStr = (String) paramMap.get("addrStr");
        //构建用户的地理信息
        UserLocation userLocation=new UserLocation();
        userLocation.setUserId(UserHolder.getUserId());
        userLocation.setAddress(addrStr);
        //调用api，保存
        userLocationApi.save(userLocation,longitude,latitude);
    }
}
