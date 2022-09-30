/*
 * Copyright (c) 2022. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.zhy.market.domain;

import lombok.Data;

@Data
public class Watermeterwell {
    public Integer id;
    public String filledBy;
    public String department;
    public String writtingTime;
    public String userType;
    public String accountIdentifier;
    public String accountName;
    public String accountNumber;
    public String address;
    public String coordinates;
    public String caliber;
    public String wellDepth;
    public String includedFacilities;
    public String waterMeterManufacturer;
    public String operatingStatus;
    public String wellOutside;
    public String wellInside;
    public String waterNature;
    public String operatingUsersNumber;
    public String operatingUserDetailsAndDetailedAddress;
    public String specialBankUsersNumber;
    public String specialBankUserDetailsAndDetailedAddress;
    public String repairInfo;
    public String status; // 状态
}
