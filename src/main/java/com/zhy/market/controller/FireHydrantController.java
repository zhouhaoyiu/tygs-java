package com.zhy.market.controller;

import static com.zhy.market.controller.LegacyResponses.response;

import com.zhy.market.domain.RepairInfo;
import com.zhy.market.domain.WaterMeterInfo;
import com.zhy.market.mapper.FireHydrantMapper;
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
@RequestMapping("FireHydrant")
public class FireHydrantController {
    private final MeterActions meterActions;
    private final RepairActions repairActions;

    private final FireHydrantMapper fireHydrantMapper;
    private final WaterMeterInfoMapper waterMeterInfoMapper;

    public FireHydrantController(
            MeterActions meterActions,
            RepairActions repairActions,
            FireHydrantMapper fireHydrantMapper,
            WaterMeterInfoMapper waterMeterInfoMapper) {
        this.meterActions = meterActions;
        this.repairActions = repairActions;
        this.fireHydrantMapper = fireHydrantMapper;
        this.waterMeterInfoMapper = waterMeterInfoMapper;
    }

    @GetMapping("getAllFireHydrantInfo")
    public Object getAllFireHydrantInfo() {
        return fireHydrantMapper.getAllFireHydrantInfo();
    }

    @GetMapping("getFireHydrantInfoById")
    public Object getFireHydrantInfoById(HttpServletRequest request) {
        int id = Integer.parseInt(request.getParameter("id"));
        return response(0, "获得消防栓信息成功", fireHydrantMapper.getFireHydrantRepairInfoById(id));
    }

    @PostMapping("updateFireHydrantRepairInfoWithId")
    public Object updateFireHydrantRepairInfoWithId(
            @RequestBody RepairInfo input, HttpServletRequest request) {
        repairActions.append(input, "firehydrant", Sessions.user(request));
        return response(0, "维修记录已保存");
    }

    @GetMapping("getFireHydrantRepairInfoById")
    public Object getFireHydrantRepairInfoById(HttpServletRequest request) {
        Integer id = Integer.valueOf(request.getParameter("id"));
        return response(0, "获得消防栓维修信息成功", fireHydrantMapper.getFireHydrantRepairInfoById(id));
    }

    @PostMapping("insertFireHydrantWaterMeterInfo")
    public Object insertFireHydrantWaterMeterInfo(@RequestBody WaterMeterInfo waterMeterInfo) {
        Integer res =
                waterMeterInfoMapper.insertFireHydrantWaterMeterInfo(
                        waterMeterInfo.waterMeterId,
                        waterMeterInfo.wallId,
                        waterMeterInfo.paymentNumber,
                        waterMeterInfo.accountNumber,
                        waterMeterInfo.accountName,
                        waterMeterInfo.nature,
                        waterMeterInfo.updateTime);

        return response(0, "test", res.toString());
    }

    @GetMapping("getFireHydrantWaterMeterInfoByWallId")
    public Object getWaterMeterInfoById(HttpServletRequest request) {
        Integer wallId = Integer.valueOf(request.getParameter("wallId"));
        return response(
                0, "获得水表信息成功", waterMeterInfoMapper.getFireHydrantWaterMeterInfoByWallId(wallId));
    }

    @PostMapping("deleteWaterMeterInfoById")
    public Object deleteWaterMeterInfoById(@RequestBody Retire input, HttpServletRequest request) {
        meterActions.retire(input, "firehydrant", Sessions.user(request));
        return response(0, "水表已停用");
    }
}
