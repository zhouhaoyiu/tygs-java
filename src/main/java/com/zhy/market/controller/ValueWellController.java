package com.zhy.market.controller;

import static com.zhy.market.controller.LegacyResponses.response;

import com.zhy.market.domain.RepairInfo;
import com.zhy.market.domain.WaterMeterInfo;
import com.zhy.market.mapper.ValueWellMapper;
import com.zhy.market.mapper.WaterMeterInfoMapper;
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

/**
 * @author Administrator
 */
@RestController
@RequestMapping("ValueWell")
public class ValueWellController {
    private final MeterActions meterActions;
    private final RepairActions repairActions;
    private final ValueWellMapper valueWellMapper;
    private final WaterMeterInfoMapper waterMeterInfoMapper;

    public ValueWellController(
            MeterActions meterActions,
            RepairActions repairActions,
            ValueWellMapper valueWellMapper,
            WaterMeterInfoMapper waterMeterInfoMapper) {
        this.meterActions = meterActions;
        this.repairActions = repairActions;
        this.valueWellMapper = valueWellMapper;
        this.waterMeterInfoMapper = waterMeterInfoMapper;
    }

    @RequestMapping("getAllValueWellInfo")
    public Object getAllValueWellInfo() {
        return valueWellMapper.getAllValueWellInfo();
    }

    @RequestMapping("getValueWellInfoById")
    public Object getValueWellInfoById(HttpServletRequest request) {
        int id = Integer.parseInt(request.getParameter("id"));
        return response(0, "获得表井信息成功", valueWellMapper.getValueWellRepairInfoById(id));
    }

    @PostMapping("updateValueWellRepairInfoWithId")
    public Object updateValueWellRepairInfoWithId(
            @RequestBody RepairInfo input, HttpServletRequest request) {
        repairActions.append(input, "valuewell", Sessions.user(request));
        return response(0, "维修记录已保存");
    }

    @RequestMapping("getValueWellRepairInfoById")
    public Object getValueWellRepairInfoById(HttpServletRequest request) {
        int id = Integer.parseInt(request.getParameter("id"));
        return response(0, "获得维修记录成功", valueWellMapper.getValueWellRepairInfoById(id));
    }

    @PostMapping("insertValueWellWaterMeterInfo")
    public Object insertValueWellWaterMeterInfo(@RequestBody WaterMeterInfo waterMeterInfo) {
        Integer res =
                waterMeterInfoMapper.insertValueWellWaterMeterInfo(
                        waterMeterInfo.waterMeterId,
                        waterMeterInfo.wallId,
                        waterMeterInfo.paymentNumber,
                        waterMeterInfo.accountNumber,
                        waterMeterInfo.accountName,
                        waterMeterInfo.nature,
                        waterMeterInfo.updateTime);

        return response(0, "test", res.toString());
    }

    @GetMapping("getValueWellWaterMeterInfoByWallId")
    public Object geValueWellWaterMeterInfoByWallId(HttpServletRequest request) {
        int wallId = Integer.parseInt(request.getParameter("wallId"));
        return response(
                0, "获得水表信息成功", waterMeterInfoMapper.getValueWellWaterMeterInfoByWallId(wallId));
    }

    @PostMapping("deleteWaterMeterInfoById")
    public Object deleteWaterMeterInfoById(@RequestBody Retire input, HttpServletRequest request) {
        meterActions.retire(input, "valuewell", Sessions.user(request));
        return response(0, "水表已停用");
    }
}
