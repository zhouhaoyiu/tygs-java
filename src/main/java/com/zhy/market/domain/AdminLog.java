package com.zhy.market.domain;

import lombok.Data;

@Data
public class AdminLog {
    public Integer adminLogid;
    public String adminLogUUid;
    public String adminUUid;
    public String logTime;
    public String info;
    public String remark;
}