package com.zhy.market.controller;

import static com.zhy.market.controller.LegacyResponses.response;

import com.zhy.market.domain.RepairInfo;
import com.zhy.market.domain.WaterMeterInfo;
import com.zhy.market.mapper.WaterMeterInfoMapper;
import com.zhy.market.mapper.WaterMeterRoomMapper;
import com.zhy.market.security.MeterActions;
import com.zhy.market.security.MeterActions.Retire;
import com.zhy.market.security.RepairActions;
import com.zhy.market.security.Sessions;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("WaterMeterRoom")
public class WaterMeterRoomController {
    private final MeterActions meterActions;
    private final RepairActions repairActions;

    private final WaterMeterInfoMapper waterMeterInfoMapper;
    private final WaterMeterRoomMapper waterMeterRoomMapper;

    public WaterMeterRoomController(
            MeterActions meterActions,
            RepairActions repairActions,
            WaterMeterInfoMapper waterMeterInfoMapper,
            WaterMeterRoomMapper waterMeterRoomMapper) {
        this.meterActions = meterActions;
        this.repairActions = repairActions;
        this.waterMeterInfoMapper = waterMeterInfoMapper;
        this.waterMeterRoomMapper = waterMeterRoomMapper;
    }

    @GetMapping("getAllWaterMeterRoomInfo")
    public Object getAllWaterMeterRoomInfo() {
        return waterMeterRoomMapper.getAllWaterMeterRoomInfo();
    }

    @PostMapping("updateWaterMeterRoomRepairInfoWithId")
    public Object updateWaterMeterRoomRepairInfoWithId(
            @RequestBody RepairInfo input, HttpServletRequest request) {
        repairActions.append(input, "watermeterroom", Sessions.user(request));
        return response(0, "维修记录已保存");
    }

    @GetMapping("getWaterMeterRoomRepairInfoById")
    public Object getWaterMeterRoomRepairInfoById(HttpServletRequest request) {
        int id = Integer.parseInt(request.getParameter("id"));
        return response(0, "获得维修记录成功", waterMeterRoomMapper.getWaterMeterRoomRepairInfoById(id));
    }

    @PostMapping("insertWaterMeterRoomWaterMeterInfo")
    public Object insertWaterMeterRoomWaterMeterInfo(@RequestBody WaterMeterInfo waterMeterInfo) {
        Integer res =
                waterMeterInfoMapper.insertWaterMeterRoomInfoWaterMeterInfo(
                        waterMeterInfo.waterMeterId,
                        waterMeterInfo.wallId,
                        waterMeterInfo.paymentNumber,
                        waterMeterInfo.accountNumber,
                        waterMeterInfo.accountName,
                        waterMeterInfo.nature,
                        waterMeterInfo.updateTime);

        return response(0, "test", res.toString());
    }

    @GetMapping("getWaterMeterRoomWaterMeterInfoByWallId")
    public Object getWaterMeterRoomWaterMeterInfoById(HttpServletRequest request) {
        Integer wallId = Integer.valueOf(request.getParameter("wallId"));
        return response(
                0,
                "获得水表信息成功",
                waterMeterInfoMapper.getWatermeterRoomWaterMeterInfoByWallId(wallId));
    }

    @PostMapping("deleteWaterMeterInfoById")
    public Object deleteWaterMeterInfoById(@RequestBody Retire input, HttpServletRequest request) {
        meterActions.retire(input, "watermeterroom", Sessions.user(request));
        return response(0, "水表已停用");
    }
}
