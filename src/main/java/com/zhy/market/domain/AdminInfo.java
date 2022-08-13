package com.zhy.market.domain;

import lombok.Data;

@Data
public class AdminInfo {
    public String userName;
    public String adminUUid;
    public String phoneNumber;
    public String emailAddress;
    public Integer adminRole;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getAdminUUid() {
        return adminUUid;
    }

    public void setAdminUUid(String adminUUid) {
        this.adminUUid = adminUUid;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public Integer getAdminRole() {
        return adminRole;
    }

    public void setAdminRole(Integer adminRole) {
        this.adminRole = adminRole;
    }


}
