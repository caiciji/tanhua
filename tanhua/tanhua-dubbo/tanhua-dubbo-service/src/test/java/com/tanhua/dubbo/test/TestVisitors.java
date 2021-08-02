package com.tanhua.dubbo.test;

import com.tanhua.domain.mongo.Visitor;
import com.tanhua.dubbo.api.mongo.VisitorApi;
import org.apache.dubbo.config.annotation.Reference;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class TestVisitors {

    @Reference
    private VisitorApi visitorApi;

    @Test
    public void testSave(){
        // 添加1到10号的账号为当前登陆用户的访客
        for (long i = 1; i <= 10; i++) {
            Visitor visitor = new Visitor();
            visitor.setFrom("首页");
            visitor.setUserId(10010l);//用户id
            visitor.setVisitorUserId(i);
            visitor.setDate(System.currentTimeMillis());
            this.visitorApi.add(visitor);
        }
        System.out.println("ok");
    }
}