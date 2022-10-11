package com.zhy.market.controller;

import com.zhy.market.domain.RepairInfo;
import com.zhy.market.domain.WaterMeterInfo;
import com.zhy.market.mapper.ValueWellMapper;
import com.zhy.market.mapper.WaterMeterInfoMapper;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import static com.zhy.market.controller.utils.getJsonRes;

@RestController
@RequestMapping("ValueWell")
public class ValueWellController {
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
        Integer id = Integer.valueOf(request.getParameter("id"));
        return getJsonRes(0, "获得表井信息成功", valueWellMapper.getValueWellRepairInfoById(id));
    }

    @RequestMapping("updateValueWellRepairInfoWithId")
    public Object updateValueWellRepairInfoWithId(@RequestBody RepairInfo repairInfo) {
        String repairInfoText = repairInfo.repairInfo;
        Integer id = repairInfo.id;

        Integer res = valueWellMapper.updateValueWellRepairInfoWithId(repairInfoText, id);
        if (res == 1) {
            return getJsonRes(0, "更新维修记录成功");
        } else {
            return getJsonRes(1, "更新维修记录失败");
        }
    }

    @RequestMapping("getValueWellRepairInfoById")
    public Object getValueWellRepairInfoById(HttpServletRequest request) {
        Integer id = Integer.valueOf(request.getParameter("id"));
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
        Integer wallId = Integer.valueOf(request.getParameter("wallId"));
        return getJsonRes(0, "获得水表信息成功", waterMeterInfoMapper.getValueWellWaterMeterInfoByWallId(wallId));
    }

    @GetMapping("deleteWaterMeterInfoById")
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