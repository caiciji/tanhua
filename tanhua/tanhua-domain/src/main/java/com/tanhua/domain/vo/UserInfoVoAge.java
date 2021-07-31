package com.tanhua.domain.vo;

import com.tanhua.domain.db.BasePojo;
import lombok.Data;

@Data
public class UserInfoVoAge extends BasePojo {
    private Long id; //用户id
    private String nickname; //昵称
    private String avatar; //用户头像
    private String birthday; //生日
    private String gender; //性别
    private Integer age; //年龄
    private String city; //城市
    private String income; //收入
    private String education; //学历
    private String profession; //行业
    private Integer marriage; //婚姻状态
}