package com.tanhua.domain.vo;

import com.tanhua.domain.db.BasePojo;
import lombok.Data;

@Data
public class SettingsVo extends BasePojo {
    private Long id;
    private String strangerQuestion;
    private String phone;
    private boolean likeNotification=true;//默认开启通用设置
    private boolean pinglunNotification=true;
    private boolean gonggaoNotification=true;
}