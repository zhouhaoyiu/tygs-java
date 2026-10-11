package com.zhy.market.controller;

import com.zhy.market.domain.RepairInfo;
import com.zhy.market.domain.WaterMeterInfo;
import com.zhy.market.mapper.WaterMeterInfoMapper;
import com.zhy.market.mapper.WaterMeterRoomMapper;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

import static com.zhy.market.controller.utils.getJsonRes;

@RestController
@RequestMapping("WaterMeterRoom")
public class WaterMeterRoomController {
    @Resource private com.zhy.market.security.MeterActions meterActions;
    @Resource private com.zhy.market.security.RepairActions repairActions;

    @Resource
    private WaterMeterInfoMapper waterMeterInfoMapper;
    @Resource
    private WaterMeterRoomMapper waterMeterRoomMapper;

    @GetMapping("getAllWaterMeterRoomInfo")
    public Object getAllWaterMeterRoomInfo() {
        return waterMeterRoomMapper.getAllWaterMeterRoomInfo();
    }

    @PostMapping("updateWaterMeterRoomRepairInfoWithId")
    public Object updateWaterMeterRoomRepairInfoWithId(@RequestBody RepairInfo input, HttpServletRequest request) {
        repairActions.append(input,"watermeterroom",com.zhy.market.security.Sessions.user(request));
        return getJsonRes(0,"维修记录已保存");
    }

    @GetMapping("getWaterMeterRoomRepairInfoById")
    public Object getWaterMeterRoomRepairInfoById(HttpServletRequest request) {
        int id = Integer.parseInt(request.getParameter("id"));
        return getJsonRes(0, "获得维修记录成功", waterMeterRoomMapper.getWaterMeterRoomRepairInfoById(id));
    }

    @PostMapping("insertWaterMeterRoomWaterMeterInfo")
    public Object insertWaterMeterRoomWaterMeterInfo(@RequestBody WaterMeterInfo waterMeterInfo) {
        Integer res = waterMeterInfoMapper.insertWaterMeterRoomInfoWaterMeterInfo(waterMeterInfo.waterMeterId, waterMeterInfo.wallId, waterMeterInfo.paymentNumber,
                waterMeterInfo.accountNumber, waterMeterInfo.accountName, waterMeterInfo.nature, waterMeterInfo.updateTime);

        return getJsonRes(0, "test", res.toString());
    }

    @GetMapping("getWaterMeterRoomWaterMeterInfoByWallId")
    public Object getWaterMeterRoomWaterMeterInfoById(HttpServletRequest request) {
        Integer wallId = Integer.valueOf(request.getParameter("wallId"));
        return getJsonRes(0, "获得水表信息成功", waterMeterInfoMapper.getWatermeterRoomWaterMeterInfoByWallId(wallId));
    }

    @PostMapping("deleteWaterMeterInfoById")
    public Object deleteWaterMeterInfoById(@RequestBody com.zhy.market.security.MeterActions.Retire input, HttpServletRequest request) {
        meterActions.retire(input, "watermeterroom", com.zhy.market.security.Sessions.user(request));
        return getJsonRes(0, "水表已停用");
    }
}
