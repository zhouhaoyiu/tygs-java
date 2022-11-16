package com.zhy.market.domain;

import lombok.Data;

/**
 * @author Administrator
 */
@Data
public class AdminLog {
    public Integer adminLogid;
    public String adminLogUUid;
    public String adminUUid;
    public String logTime;
    public String info;
    public String remark;
}