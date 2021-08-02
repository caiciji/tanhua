package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.UserLocation;

public interface UserLocationApi {
    /**
     * 上传地理位置信息
     * @param userLocation
     * @param longitude
     * @param latitude
     */
    void save(UserLocation userLocation, Double longitude, Double latitude);
}
