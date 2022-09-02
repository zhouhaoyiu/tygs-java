package com.zhy.market.controller;

import com.zhy.market.domain.RepairInfo;
import com.zhy.market.domain.WaterMeterInfo;
import com.zhy.market.mapper.FireHydrantMapper;
import com.zhy.market.mapper.WaterMeterInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import static com.zhy.market.controller.utils.getJsonRes;

@RestController
@RequestMapping("FireHydrant")
public class FireHydrantController {

    @Autowired
    private FireHydrantMapper fireHydrantMapper;
    @Autowired
    private WaterMeterInfoMapper waterMeterInfoMapper;

    @GetMapping("getAllFireHydrantInfo")
    public Object getAllFireHydrantInfo() {
        return fireHydrantMapper.getAllFireHydrantInfo();
    }

    @GetMapping("getFireHydrantInfoById")
    public Object getFireHydrantInfoById(HttpServletRequest request) {
        Integer id = Integer.valueOf(request.getParameter("id"));
        System.out.println("getFireHydrantInfoById FireHydrant" + id);
        return getJsonRes(0, "获得消防栓信息成功", fireHydrantMapper.getFireHydrantRepairInfoById(id));
    }

    @GetMapping("updateFireHydrantRepairInfoWithId")
    public Object updateFireHydrantRepairInfoWithId(@RequestBody RepairInfo repairInfo) {
        String repairInfoText = repairInfo.repairInfo;
        Integer id = repairInfo.id;

        Integer res = fireHydrantMapper.updateFireHydrantRepairInfoWithId(repairInfoText, id);
        if (res == 1) {
            return getJsonRes(0, "更新维修记录成功");
        } else {
            return getJsonRes(1, "更新维修记录失败");
        }
    }

    @GetMapping("getFireHydrantRepairInfoById")
    public Object getFireHydrantRepairInfoById(HttpServletRequest request) {
        Integer id = Integer.valueOf(request.getParameter("id"));
        System.out.println("getFireHydrantRepairInfoById FireHydrant" + id);
        return getJsonRes(0, "获得消防栓维修信息成功", fireHydrantMapper.getFireHydrantRepairInfoById(id));
    }

    @PostMapping("insertFireHydrantWaterMeterInfo")
    public Object insertFireHydrantWaterMeterInfo(@RequestBody WaterMeterInfo waterMeterInfo) {

        System.out.println("insertFireHydrantWaterMeterInfo");

        Integer res = waterMeterInfoMapper.insertFireHydrantWaterMeterInfo(waterMeterInfo.waterMeterId, waterMeterInfo.wallId, waterMeterInfo.paymentNumber,
                waterMeterInfo.accountNumber, waterMeterInfo.accountName, waterMeterInfo.status, waterMeterInfo.updateTime);

        return getJsonRes(0, "test", res.toString());
    }

    @GetMapping("getFireHydrantWaterMeterInfoByWallId")
    public Object getWaterMeterInfoById(HttpServletRequest request) {
        Integer wallId = Integer.valueOf(request.getParameter("wallId"));
        System.out.println("getFireHydrantWaterMeterInfoByWallId" + wallId);
//        System.out.println(waterMeterInfoMapper.getWatermeterwellWaterMeterInfoByWallId(wallId));
        return getJsonRes(0, "获得水表信息成功", waterMeterInfoMapper.getFireHydrantWaterMeterInfoByWallId(wallId));
    }
}
