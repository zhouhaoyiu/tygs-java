/*
 * Copyright (c) 2022. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.zhy.market.controller;

import com.zhy.market.domain.WaterMeterInfo;
import com.zhy.market.mapper.TygsMapper;
import com.zhy.market.mapper.WaterMeterInfoMapper;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static com.zhy.market.controller.utils.getJsonRes;

@RestController
@RequestMapping("Tygs")
public class TygsController {

    @Autowired
    private WaterMeterInfoMapper waterMeterInfoMapper;
    @Autowired
    private TygsMapper tygsMapper;

    @GetMapping("getAllInfo")
    public Object getAllInfo() {
        return tygsMapper.getAllInfo();
    }

    @PostMapping("updateRepairInfo")
    public Object updateRepairInfo(@RequestBody RepairInfo repairInfo) {
        System.out.println(repairInfo);

        String repairInfoText = repairInfo.repairInfo;
        Integer id = repairInfo.id;

        System.out.println(repairInfoText);
        System.out.println(id);

        Integer res = tygsMapper.updateRepairInfoWithId(repairInfoText, id);
        // Todo 判断res
        return getJsonRes(1, "更新维修记录成功", (List) null);
//        return  null;
    }

    @GetMapping("getRepairInfoById")
    public Object getRepairInfoById(HttpServletRequest request) {
        Integer id = Integer.valueOf(request.getParameter("id"));
        System.out.println("getRepairInfoById" + id);
        return getJsonRes(0, "获得维修记录成功", tygsMapper.getRepairInfoById(id));
    }

    @PostMapping("insertWaterMeterInfo")
    public Object insertWaterMeterInfo(@RequestBody WaterMeterInfo waterMeterInfo) {
        String waterMeterId = waterMeterInfo.waterMeterId;
        Integer wallId = waterMeterInfo.wallId;
        String paymentNumber = waterMeterInfo.paymentNumber;
        String accountNumber = waterMeterInfo.accountNumber;
        String accountName = waterMeterInfo.accountName;
        String status = waterMeterInfo.status;
        String updateTime = waterMeterInfo.updateTime;
        System.out.println("waterMeterId" + waterMeterId);
        Integer res = waterMeterInfoMapper.insertWaterMeterInfo(waterMeterId, wallId, paymentNumber, accountNumber, accountName, status, updateTime);

        return getJsonRes(0, "test", res.toString());
    }

    @GetMapping("getWaterMeterByWallId")
    public Object getWaterMeterInfoById(HttpServletRequest request) {
        Integer wallId = Integer.valueOf(request.getParameter("wallId"));
        System.out.println("getWaterMeterByWallId" + wallId);
        System.out.println(waterMeterInfoMapper.getWaterMeterInfoByWallId(wallId));
        return getJsonRes(0, "获得水表信息成功", waterMeterInfoMapper.getWaterMeterInfoByWallId(wallId));
    }

}

@Data
class RepairInfo {
    public String repairInfo;
    public Integer id;
}
