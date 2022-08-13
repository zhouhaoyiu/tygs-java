package com.zhy.market.domain;

import lombok.Data;

@Data
public class AdminLog {
    public Integer adminLogid;

    public Integer getAdminLogid() {
        return adminLogid;
    }

    public void setAdminLogid(Integer adminLogid) {
        this.adminLogid = adminLogid;
    }

    public String getAdminLogUUid() {
        return adminLogUUid;
    }

    public void setAdminLogUUid(String adminLogUUid) {
        this.adminLogUUid = adminLogUUid;
    }

    public String getAdminUUid() {
        return adminUUid;
    }

    public void setAdminUUid(String adminUUid) {
        this.adminUUid = adminUUid;
    }

    public String getLogTime() {
        return logTime;
    }

    public void setLogTime(String logTime) {
        this.logTime = logTime;
    }

    public String adminLogUUid;
    public String adminUUid;
    public String logTime;

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String info;

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String remark;
}
