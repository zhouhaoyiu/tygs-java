package com.zhy.market.controller;

import com.zhy.market.domain.RepairInfo;
import com.zhy.market.domain.WaterMeterInfo;
import com.zhy.market.mapper.WaterMeterInfoMapper;
import com.zhy.market.mapper.WaterMeterRoomMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import static com.zhy.market.controller.utils.getJsonRes;

import javax.servlet.http.HttpServletRequest;

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
    public Object updateWatermeterwellRepairInfoWithId(@RequestBody RepairInfo repairInfo) {
        System.out.println(repairInfo);

        String repairInfoText = repairInfo.repairInfo;
        Integer id = repairInfo.id;

        System.out.println(repairInfoText);
        System.out.println(id);

        Integer res = waterMeterRoomMapper.updateWaterMeterRoomRepairInfoWithId(repairInfoText, id);
        if (res == 1) {
            return getJsonRes(1, "更新维修记录成功");
        } else {
            return getJsonRes(0, "更新维修记录失败");
        }
    }

    @GetMapping("getWaterMeterRoomRepairInfoById")
    public Object getWatermeterwellRepairInfoById(HttpServletRequest request) {
        Integer id = Integer.valueOf(request.getParameter("id"));
        System.out.println("getRepairInfoById WaterMeterRoom" + id);
        return getJsonRes(0, "获得维修记录成功", waterMeterRoomMapper.getWaterMeterRoomRepairInfoById(id));
    }

    @PostMapping("insertWaterMeterRoomWaterMeterInfo")
    public Object insertWaterMeterInfo(@RequestBody WaterMeterInfo waterMeterInfo) {
        String waterMeterId = waterMeterInfo.waterMeterId;
        Integer wallId = waterMeterInfo.wallId;
        String paymentNumber = waterMeterInfo.paymentNumber;
        String accountNumber = waterMeterInfo.accountNumber;
        String accountName = waterMeterInfo.accountName;
        String status = waterMeterInfo.status;
        String updateTime = waterMeterInfo.updateTime;
        System.out.println("waterMeterId" + waterMeterId);
        Integer res = waterMeterInfoMapper.insertWaterMeterRoomInfoWaterMeterInfo(waterMeterId, wallId, paymentNumber,
                accountNumber, accountName, status, updateTime);

        return getJsonRes(0, "test", res.toString());
    }

    @GetMapping("getWatermeterRoomWaterMeterInfoByWallId")
    public Object getWaterMeterInfoById(HttpServletRequest request) {
        Integer wallId = Integer.valueOf(request.getParameter("wallId"));
        System.out.println("getWaterMeterByWallId" + wallId);
        System.out.println(waterMeterInfoMapper.getWatermeterRoomWaterMeterInfoByWallId(wallId));
        return getJsonRes(0, "获得水表信息成功", waterMeterInfoMapper.getWatermeterRoomWaterMeterInfoByWallId(wallId));
    }
}
