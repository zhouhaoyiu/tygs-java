package com.zhy.market.domain;

import lombok.Data;

/**
 * @author zhy
 * */
@Data
public class UserLog {
    /**
     * @Param logid 日志id
     *
     */
    Integer logid;
    String loguuid;

    public String getUseruuid() {
        return useruuid;
    }

    public void setUseruuid(String useruuid) {
        this.useruuid = useruuid;
    }

    String useruuid;

    public Integer getLogid() {
        return logid;
    }

    public void setLogid(Integer logid) {
        this.logid = logid;
    }

    public String getLoguuid() {
        return loguuid;
    }

    public void setLoguuid(String loguuid) {
        this.loguuid = loguuid;
    }



    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getGid() {
        return gid;
    }

    public void setGid(Integer gid) {
        this.gid = gid;
    }

    public String getLogtime() {
        return logtime;
    }

    public void setLogtime(String logtime) {
        this.logtime = logtime;
    }

    String type;
    Integer gid;
    String logtime;

}
