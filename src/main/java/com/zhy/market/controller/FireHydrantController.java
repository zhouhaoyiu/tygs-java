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
    @Resource private com.zhy.market.security.MeterActions meterActions;
    @Resource private com.zhy.market.security.RepairActions repairActions;

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

    @PostMapping("updateFireHydrantRepairInfoWithId")
    public Object updateFireHydrantRepairInfoWithId(@RequestBody RepairInfo input, HttpServletRequest request) {
        repairActions.append(input,"firehydrant",com.zhy.market.security.Sessions.user(request));
        return getJsonRes(0,"维修记录已保存");
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

    @PostMapping("deleteWaterMeterInfoById")
    public Object deleteWaterMeterInfoById(@RequestBody com.zhy.market.security.MeterActions.Retire input, HttpServletRequest request) {
        meterActions.retire(input, "firehydrant", com.zhy.market.security.Sessions.user(request));
        return getJsonRes(0, "水表已停用");
    }
}
