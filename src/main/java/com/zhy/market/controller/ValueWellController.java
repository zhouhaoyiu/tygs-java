package com.zhy.market.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zhy.market.domain.RepairInfo;
import com.zhy.market.domain.WaterMeterInfo;
import com.zhy.market.mapper.ValueWellMapper;
import com.zhy.market.mapper.WaterMeterInfoMapper;

import static com.zhy.market.controller.utils.getJsonRes;

@RestController
@RequestMapping("ValueWell")
public class ValueWellController {
  @Autowired
  private ValueWellMapper valueWellMapper;
  @Autowired
  private WaterMeterInfoMapper waterMeterInfoMapper;

  @RequestMapping("getAllValueWellInfo")
  public Object getAllValueWellInfo() {
    return valueWellMapper.getAllValueWellInfo();
  }

  @RequestMapping("getValueWellInfoById")
  public Object getValueWellInfoById(HttpServletRequest request) {
    Integer id = Integer.valueOf(request.getParameter("id"));
    System.out.println("getValueWellInfoById ValueWell" + id);
    return getJsonRes(0, "获得值井信息成功", valueWellMapper.getValueWellRepairInfoById(id));
  }

  @RequestMapping("updateValueWellRepairInfoWithId")
  public Object updateValueWellRepairInfoWithId(@RequestBody RepairInfo repairInfo) {
    System.out.println(repairInfo);

    String repairInfoText = repairInfo.repairInfo;
    Integer id = repairInfo.id;

    System.out.println(repairInfoText);
    System.out.println(id);

    Integer res = valueWellMapper.updateValueWellRepairInfoWithId(repairInfoText, id);
    if (res == 1) {
      return getJsonRes(1, "更新维修记录成功");
    } else {
      return getJsonRes(0, "更新维修记录失败");
    }
  }

  @RequestMapping("getValueWellRepairInfoById")
  public Object getValueWellRepairInfoById(HttpServletRequest request) {
    Integer id = Integer.valueOf(request.getParameter("id"));
    System.out.println("getValueWellRepairInfoById ValueWell" + id);
    return getJsonRes(0, "获得维修记录成功", valueWellMapper.getValueWellRepairInfoById(id));
  }

  @PostMapping("insertValueWellWaterMeterInfo")
  public Object insertValueWellWaterMeterInfo(@RequestBody WaterMeterInfo waterMeterInfo) {
    String waterMeterId = waterMeterInfo.waterMeterId;
    Integer wallId = waterMeterInfo.wallId;
    String paymentNumber = waterMeterInfo.paymentNumber;
    String accountNumber = waterMeterInfo.accountNumber;
    String accountName = waterMeterInfo.accountName;
    String status = waterMeterInfo.status;
    String updateTime = waterMeterInfo.updateTime;
    System.out.println("waterMeterId" + waterMeterId);
    Integer res = waterMeterInfoMapper.insertValueWellWaterMeterInfo(waterMeterId, wallId, paymentNumber,
        accountNumber, accountName, status, updateTime);

    return getJsonRes(0, "test", res.toString());
  }

  @GetMapping("getValueWellWaterMeterInfoByWallId")
  public Object geValueWellWaterMeterInfoByWallId(HttpServletRequest request) {
    Integer wallId = Integer.valueOf(request.getParameter("wallId"));
    System.out.println("getWaterMeterByWallId" + wallId);
    System.out.println(waterMeterInfoMapper.getValueWellWaterMeterInfoByWallId(wallId));
    return getJsonRes(0, "获得水表信息成功", waterMeterInfoMapper.getValueWellWaterMeterInfoByWallId(wallId));
  }
}