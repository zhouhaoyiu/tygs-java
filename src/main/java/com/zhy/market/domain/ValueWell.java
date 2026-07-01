package com.zhy.market.domain;

import lombok.Data;

@Data
public class ValueWell {
  public Integer id; // id
  public String filledBy; // 填报人
  public String department; // 部门
  public String fillingTime; // 填报时间
  public String customerType; // 客户类型
  public String accountIdentifier; // 编号
  public String streetName; // 街道名称
  public String wellChamberType; // 井室类型
  public String caliber; // 口径
  public String runningState; // 运行状态
  public String quantity; // 数量
  public String wellDepth; // 井深 
  public String positioningCoordinates; // 坐标
  public String photosInTheWell; // 井内照片
  public String manufactor; // 制造商
  public String personLiable; // 负责人
  public String troubleshootingTime; // 故障时间
  public String runTime; // 运行时间
  public String repairTime; // 维修时间
  public String photosOutsideTheWell; // 井外照片
  public String repairInfo;
  public String status; // 状态
}
