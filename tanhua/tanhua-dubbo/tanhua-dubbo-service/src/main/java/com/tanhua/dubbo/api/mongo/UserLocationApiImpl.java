package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.UserLocation;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;


@Service
public class UserLocationApiImpl implements UserLocationApi {

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 上传地理位置信息
     * @param userLocation
     * @param longitude
     * @param latitude
     */
    @Override
    public void save(UserLocation userLocation, Double longitude, Double latitude) {
        //1.构建登录的条件
        Query query=new Query(Criteria.where("userId").is(userLocation.getUserId()));

        //构建坐标 GeoJsonPoint 反序列化失败
        GeoJsonPoint point=new GeoJsonPoint(longitude,latitude);
        long timeMillis = System.currentTimeMillis();
        //2.判断是否有记录
        if(!mongoTemplate.exists(query,UserLocation.class)){
            //3.如果没有记录，就添加记录
            userLocation.setLocation(point);
            userLocation.setCreated(timeMillis);
            userLocation.setUpdated(timeMillis);
            mongoTemplate.insert(userLocation);
        }else{
            //4.如果有记录，则更新数据
            Update update=new Update();
            update.set("location",point);
            update.set("address",userLocation.getAddress());
            update.set("updated",timeMillis);
            mongoTemplate.updateFirst(query,update,UserLocation.class);
        }
    }
}
