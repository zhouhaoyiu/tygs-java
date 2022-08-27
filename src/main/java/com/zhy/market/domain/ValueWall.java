package com.zhy.market.domain;

import lombok.Data;

@Data
public class ValueWall {
    public Integer id;
    public String filledBy; // 填写人
    public String department; // 所在部门
    public String writtingTime; // 填写时间
    public String userType; // 用户类型
    public String accountIdentifier;
    public String communityName;
    public String detailedAddress;
    public String location;
    public String watermeterLocation;
    public String watermeterType;
    public String totalHouseholds;
    public String numberHouseholds;
    public String numberNoHouseholds;
    public String numberHouseholdsWithSubitems;
    public String noHouseholdsDetailsAndDetailNumber;
    public String numberSpecialHouseHolds;
    public String specialHouseHoldsDetailsAndDetailedHouseNumber;
    public String pipe;
    public String conditionOfWaterSeparator;
    public String Troubleshooting;
    public String valueWallOutSide;
    public String valueWallInside;
    public String personLiable;

}
