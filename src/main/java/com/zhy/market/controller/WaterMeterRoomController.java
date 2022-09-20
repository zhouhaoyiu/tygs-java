package com.zhy.market.controller;

import com.zhy.market.domain.RepairInfo;
import com.zhy.market.domain.WaterMeterInfo;
import com.zhy.market.mapper.WaterMeterInfoMapper;
import com.zhy.market.mapper.WaterMeterRoomMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import static com.zhy.market.controller.utils.getJsonRes;

@RestController
@RequestMapping("WaterMeterRoom")
public class WaterMeterRoomController {

    @Autowired
    private WaterMeterInfoMapper waterMeterInfoMapper;
    @Autowired
    private WaterMeterRoomMapper waterMeterRoomMapper;

    @GetMapping("getAllWaterMeterRoomInfo")
    public Object getAllWaterMeterRoomInfo() {
        return waterMeterRoomMapper.getAllWaterMeterRoomInfo();
    }

    @PostMapping("updateWaterMeterRoomRepairInfoWithId")
    public Object updateWaterMeterRoomRepairInfoWithId(@RequestBody RepairInfo repairInfo) {

        String repairInfoText = repairInfo.repairInfo;
        Integer id = repairInfo.id;

        Integer res = waterMeterRoomMapper.updateWaterMeterRoomRepairInfoWithId(repairInfoText, id);
        if (res == 1) {
            return getJsonRes(0, "更新维修记录成功");
        } else {
            return getJsonRes(1, "更新维修记录失败");
        }
    }

    @GetMapping("getWaterMeterRoomRepairInfoById")
    public Object getWaterMeterRoomRepairInfoById(HttpServletRequest request) {
        Integer id = Integer.valueOf(request.getParameter("id"));
        System.out.println("getRepairInfoById WaterMeterRoom" + id);
        return getJsonRes(0, "获得维修记录成功", waterMeterRoomMapper.getWaterMeterRoomRepairInfoById(id));
    }

    @PostMapping("insertWaterMeterRoomWaterMeterInfo")
    public Object insertWaterMeterRoomWaterMeterInfo(@RequestBody WaterMeterInfo waterMeterInfo) {
        System.out.println("WaterMeterRoomWaterMeterId " + waterMeterInfo.waterMeterId);
        Integer res = waterMeterInfoMapper.insertWaterMeterRoomInfoWaterMeterInfo(waterMeterInfo.waterMeterId, waterMeterInfo.wallId, waterMeterInfo.paymentNumber,
                waterMeterInfo.accountNumber, waterMeterInfo.accountName, waterMeterInfo.status, waterMeterInfo.updateTime);

        return getJsonRes(0, "test", res.toString());
    }

    @GetMapping("getWaterMeterRoomWaterMeterInfoByWallId")
    public Object getWaterMeterRoomWaterMeterInfoById(HttpServletRequest request) {
        Integer wallId = Integer.valueOf(request.getParameter("wallId"));
        System.out.println("getWaterMeterByWallId" + wallId);
//        System.out.println(waterMeterInfoMapper.getWatermeterRoomWaterMeterInfoByWallId(wallId));
        return getJsonRes(0, "获得水表信息成功", waterMeterInfoMapper.getWatermeterRoomWaterMeterInfoByWallId(wallId));
    }
}
