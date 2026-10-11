package com.zhy.market.controller;

import com.zhy.market.domain.RepairInfo;
import com.zhy.market.domain.WaterMeterInfo;
import com.zhy.market.mapper.ValueWellMapper;
import com.zhy.market.mapper.WaterMeterInfoMapper;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

import static com.zhy.market.controller.utils.getJsonRes;

/**
 * @author Administrator
 */
@RestController
@RequestMapping("ValueWell")
public class ValueWellController {
    @Resource private com.zhy.market.security.MeterActions meterActions;
    @Resource private com.zhy.market.security.RepairActions repairActions;
    @Resource
    private ValueWellMapper valueWellMapper;
    @Resource
    private WaterMeterInfoMapper waterMeterInfoMapper;

    @RequestMapping("getAllValueWellInfo")
    public Object getAllValueWellInfo() {
        return valueWellMapper.getAllValueWellInfo();
    }

    @RequestMapping("getValueWellInfoById")
    public Object getValueWellInfoById(HttpServletRequest request) {
        int id = Integer.parseInt(request.getParameter("id"));
        return getJsonRes(0, "获得表井信息成功", valueWellMapper.getValueWellRepairInfoById(id));
    }

    @PostMapping("updateValueWellRepairInfoWithId")
    public Object updateValueWellRepairInfoWithId(@RequestBody RepairInfo input, HttpServletRequest request) {
        repairActions.append(input,"valuewell",com.zhy.market.security.Sessions.user(request));
        return getJsonRes(0,"维修记录已保存");
    }

    @RequestMapping("getValueWellRepairInfoById")
    public Object getValueWellRepairInfoById(HttpServletRequest request) {
        int id = Integer.parseInt(request.getParameter("id"));
        return getJsonRes(0, "获得维修记录成功", valueWellMapper.getValueWellRepairInfoById(id));
    }

    @PostMapping("insertValueWellWaterMeterInfo")
    public Object insertValueWellWaterMeterInfo(@RequestBody WaterMeterInfo waterMeterInfo) {
        Integer res = waterMeterInfoMapper.insertValueWellWaterMeterInfo(waterMeterInfo.waterMeterId, waterMeterInfo.wallId, waterMeterInfo.paymentNumber,
                waterMeterInfo.accountNumber, waterMeterInfo.accountName, waterMeterInfo.nature, waterMeterInfo.updateTime);

        return getJsonRes(0, "test", res.toString());
    }

    @GetMapping("getValueWellWaterMeterInfoByWallId")
    public Object geValueWellWaterMeterInfoByWallId(HttpServletRequest request) {
        int wallId = Integer.parseInt(request.getParameter("wallId"));
        return getJsonRes(0, "获得水表信息成功", waterMeterInfoMapper.getValueWellWaterMeterInfoByWallId(wallId));
    }

    @PostMapping("deleteWaterMeterInfoById")
    public Object deleteWaterMeterInfoById(@RequestBody com.zhy.market.security.MeterActions.Retire input, HttpServletRequest request) {
        meterActions.retire(input, "valuewell", com.zhy.market.security.Sessions.user(request));
        return getJsonRes(0, "水表已停用");
    }
}
