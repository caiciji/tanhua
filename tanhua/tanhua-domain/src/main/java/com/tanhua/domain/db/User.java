package com.tanhua.domain.db;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;

@Data
public class User extends BasePojo{
    private Long id;
    private String mobile; //手机号
    @JSONField(serialize = false)
    private String password; //密码，json序列化时忽略
}