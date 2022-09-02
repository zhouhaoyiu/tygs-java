/*
 * Copyright (c) 2022. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.zhy.market.controller;

import com.zhy.market.domain.RepairInfo;
import com.zhy.market.domain.WaterMeterInfo;
import com.zhy.market.mapper.WaterMeterInfoMapper;
import com.zhy.market.mapper.WaterMeterWellMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import static com.zhy.market.controller.utils.getJsonRes;

@RestController
@RequestMapping("Watermeterwell")
public class WaterMeterWellController {

    @Autowired
    private WaterMeterInfoMapper waterMeterInfoMapper;
    @Autowired
    private WaterMeterWellMapper watermeterwellMapper;

    @GetMapping("getAllWatermeterwellInfo")
    public Object getAllInfo() {
        return watermeterwellMapper.getAllWatermeterwellInfo();
    }

    @PostMapping("updateWatermeterwellRepairInfoWithId")
    public Object updateWatermeterwellRepairInfoWithId(@RequestBody RepairInfo repairInfo) {
        System.out.println(repairInfo);

        String repairInfoText = repairInfo.repairInfo;
        Integer id = repairInfo.id;

        System.out.println(repairInfoText);
        System.out.println(id);

        Integer res = watermeterwellMapper.updateWatermeterwellRepairInfoWithId(repairInfoText, id);
        if (res == 1) {
            return getJsonRes(1, "更新维修记录成功");
        } else {
            return getJsonRes(0, "更新维修记录失败");
        }
    }

    @GetMapping("getWatermeterwellRepairInfoById")
    public Object getWatermeterwellRepairInfoById(HttpServletRequest request) {
        Integer id = Integer.valueOf(request.getParameter("id"));
        System.out.println("getRepairInfoById watermeterwell" + id);
        return getJsonRes(0, "获得维修记录成功", watermeterwellMapper.getWatermeterwellRepairInfoById(id));
    }

    @PostMapping("insertWaterMeterWellWaterMeterInfo")
    public Object insertWaterMeterWellWaterMeterInfo(@RequestBody WaterMeterInfo waterMeterInfo) {

        System.out.println("WaterMeterWellwaterMeterId " + waterMeterInfo.waterMeterId);
        Integer res = waterMeterInfoMapper.insertWaterMeterWellWaterMeterInfo(waterMeterInfo.waterMeterId, waterMeterInfo.wallId, waterMeterInfo.paymentNumber,
                waterMeterInfo.accountNumber, waterMeterInfo.accountName, waterMeterInfo.status, waterMeterInfo.updateTime);

        return getJsonRes(0, "test", res.toString());
    }

    @GetMapping("getWatermeterwellWaterMeterInfoByWallId")
    public Object getWaterMeterInfoById(HttpServletRequest request) {
        Integer wallId = Integer.valueOf(request.getParameter("wallId"));
        System.out.println("getWaterMeterByWallId" + wallId);
//        System.out.println(waterMeterInfoMapper.getWatermeterwellWaterMeterInfoByWallId(wallId));
        return getJsonRes(0, "获得水表信息成功", waterMeterInfoMapper.getWatermeterwellWaterMeterInfoByWallId(wallId));
    }

}
