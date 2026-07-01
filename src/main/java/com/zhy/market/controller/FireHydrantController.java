package com.zhy.market.controller;

import com.zhy.market.domain.RepairInfo;
import com.zhy.market.domain.WaterMeterInfo;
import com.zhy.market.mapper.FireHydrantMapper;
import com.zhy.market.mapper.WaterMeterInfoMapper;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

import static com.zhy.market.controller.utils.getJsonRes;

/**
 * @author Administrator
 */
@RestController
@RequestMapping("FireHydrant")
public class FireHydrantController {

    @Resource
    private FireHydrantMapper fireHydrantMapper;
    @Resource
    private WaterMeterInfoMapper waterMeterInfoMapper;

    @GetMapping("getAllFireHydrantInfo")
    public Object getAllFireHydrantInfo() {
        return fireHydrantMapper.getAllFireHydrantInfo();
    }

    @GetMapping("getFireHydrantInfoById")
    public Object getFireHydrantInfoById(HttpServletRequest request) {
        int id = Integer.parseInt(request.getParameter("id"));
        return getJsonRes(0, "获得消防栓信息成功", fireHydrantMapper.getFireHydrantRepairInfoById(id));
    }

    @GetMapping("updateFireHydrantRepairInfoWithId")
    public Object updateFireHydrantRepairInfoWithId(@RequestBody RepairInfo repairInfo) {
        String repairInfoText = repairInfo.repairInfo;
        int id = repairInfo.id;

        int res = fireHydrantMapper.updateFireHydrantRepairInfoWithId(repairInfoText, id);
        if (res == 1) {
            return getJsonRes(0, "更新维修记录成功");
        } else {
            return getJsonRes(1, "更新维修记录失败");
        }
    }

    @GetMapping("getFireHydrantRepairInfoById")
    public Object getFireHydrantRepairInfoById(HttpServletRequest request) {
        Integer id = Integer.valueOf(request.getParameter("id"));
        return getJsonRes(0, "获得消防栓维修信息成功", fireHydrantMapper.getFireHydrantRepairInfoById(id));
    }

    @PostMapping("insertFireHydrantWaterMeterInfo")
    public Object insertFireHydrantWaterMeterInfo(@RequestBody WaterMeterInfo waterMeterInfo) {
        Integer res = waterMeterInfoMapper.insertFireHydrantWaterMeterInfo(waterMeterInfo.waterMeterId, waterMeterInfo.wallId, waterMeterInfo.paymentNumber,
                waterMeterInfo.accountNumber, waterMeterInfo.accountName, waterMeterInfo.nature, waterMeterInfo.updateTime);

        return getJsonRes(0, "test", res.toString());
    }

    @GetMapping("getFireHydrantWaterMeterInfoByWallId")
    public Object getWaterMeterInfoById(HttpServletRequest request) {
        Integer wallId = Integer.valueOf(request.getParameter("wallId"));
        return getJsonRes(0, "获得水表信息成功", waterMeterInfoMapper.getFireHydrantWaterMeterInfoByWallId(wallId));
    }

    @GetMapping("del" +
            "eteWaterMeterInfoById")
    public Object deleteWaterMeterInfoById(HttpServletRequest request) {
        String waterMeterId = request.getParameter("waterMeterId");
        Integer res = waterMeterInfoMapper.updateWaterMeterInfoStatus(waterMeterId, "2");
        if (res == 1) {
            return getJsonRes(0, "删除水表信息成功");
        } else {
            return getJsonRes(1, "删除水表信息失败");
        }
    }
}
