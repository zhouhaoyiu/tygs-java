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
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import static com.zhy.market.controller.utils.getJsonRes;

@RestController
@RequestMapping("WaterMeterWell")
public class WaterMeterWellController {

    @Resource
    private WaterMeterInfoMapper waterMeterInfoMapper;
    @Resource
    private WaterMeterWellMapper watermeterwellMapper;

    @GetMapping("getAllWaterMeterWellInfo")
    public Object getAllInfo() {
        return watermeterwellMapper.getAllWaterMeterWellInfo();
    }

    @PostMapping("updateWaterMeterWellRepairInfoWithId")
    public Object updateWaterMeterWellRepairInfoWithId(@RequestBody RepairInfo repairInfo) {
        String repairInfoText = repairInfo.repairInfo;
        Integer id = repairInfo.id;

        Integer res = watermeterwellMapper.updateWaterMeterWellRepairInfoWithId(repairInfoText, id);
        if (res == 1) {
            return getJsonRes(0, "更新维修记录成功");
        } else {
            return getJsonRes(1, "更新维修记录失败");
        }
    }

    @GetMapping("getWaterMeterWellRepairInfoById")
    public Object getWaterMeterWellRepairInfoById(HttpServletRequest request) {
        Integer id = Integer.valueOf(request.getParameter("id"));

        return getJsonRes(0, "获得维修记录成功", watermeterwellMapper.getWaterMeterWellRepairInfoById(id));
    }

    @PostMapping("insertWaterMeterWellWaterMeterInfo")
    public Object insertWaterMeterWellWaterMeterInfo(@RequestBody WaterMeterInfo waterMeterInfo) {
        Integer res = waterMeterInfoMapper.insertWaterMeterWellWaterMeterInfo(waterMeterInfo.waterMeterId,
                waterMeterInfo.wallId, waterMeterInfo.paymentNumber,
                waterMeterInfo.accountNumber, waterMeterInfo.accountName, waterMeterInfo.nature,
                waterMeterInfo.updateTime);

        return getJsonRes(0, "test", res.toString());
    }

    @GetMapping("getWaterMeterWellWaterMeterInfoByWallId")
    public Object getWaterMeterWellWaterMeterInfoByWallId(HttpServletRequest request) {
        Integer wallId = Integer.valueOf(request.getParameter("wallId"));
        return getJsonRes(0, "获得水表信息成功", waterMeterInfoMapper.getWaterMeterWellWaterMeterInfoByWallId(wallId));
    }

    @GetMapping("deleteWaterMeterInfoById")
    public Object deleteWaterMeterInfoById(HttpServletRequest request) {
        String waterMeterId = request.getParameter("waterMeterId");
        Integer res = waterMeterInfoMapper.updateWaterMeterInfoStatus(waterMeterId, "1");
        if (res == 1) {
            return getJsonRes(0, "删除水表信息成功");
        } else {
            return getJsonRes(1, "删除水表信息失败");
        }
    }

}
