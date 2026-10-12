package com.zhy.market.controller;

import static com.zhy.market.controller.LegacyResponses.response;

import com.zhy.market.domain.RepairInfo;
import com.zhy.market.domain.WaterMeterInfo;
import com.zhy.market.domain.Watermeterwell;
import com.zhy.market.mapper.WaterMeterInfoMapper;
import com.zhy.market.mapper.WaterMeterWellMapper;
import com.zhy.market.security.MeterActions;
import com.zhy.market.security.MeterActions.Retire;
import com.zhy.market.security.RepairActions;
import com.zhy.market.security.Sessions;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * @author Administrator
 */
@RestController
@RequestMapping("WaterMeterWell")
public class WaterMeterWellController {
    private final MeterActions meterActions;
    private final RepairActions repairActions;

    private final WaterMeterInfoMapper waterMeterInfoMapper;
    private final WaterMeterWellMapper watermeterwellMapper;

    public WaterMeterWellController(
            MeterActions meterActions,
            RepairActions repairActions,
            WaterMeterInfoMapper waterMeterInfoMapper,
            WaterMeterWellMapper watermeterwellMapper) {
        this.meterActions = meterActions;
        this.repairActions = repairActions;
        this.waterMeterInfoMapper = waterMeterInfoMapper;
        this.watermeterwellMapper = watermeterwellMapper;
    }

    @GetMapping("getAllWaterMeterWellInfo")
    public Object getAllInfo() {
        return watermeterwellMapper.getAllWaterMeterWellInfo();
    }

    @PostMapping("addWaterMeterWell")
    public Object addWaterMeterWell(
            @RequestBody Watermeterwell watermeterwell, HttpServletRequest request) {
        if (watermeterwell.accountName == null
                || watermeterwell.accountName.isBlank()
                || watermeterwell.accountNumber == null
                || watermeterwell.accountNumber.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "请填写户名和户号");
        }
        int res = watermeterwellMapper.addWaterMeterWell(watermeterwell);
        if (res == 1) {
            return response(0, "添加成功");
        } else {
            return response(1, "添加失败");
        }
    }

    @PostMapping("updateWaterMeterWellRepairInfoWithId")
    public Object updateWaterMeterWellRepairInfoWithId(
            @RequestBody RepairInfo input, HttpServletRequest request) {
        repairActions.append(input, "watermeterwell", Sessions.user(request));
        return response(0, "维修记录已保存");
    }

    @GetMapping("getWaterMeterWellRepairInfoById")
    public Object getWaterMeterWellRepairInfoById(HttpServletRequest request) {
        int id = Integer.parseInt(request.getParameter("id"));

        return response(0, "获得维修记录成功", watermeterwellMapper.getWaterMeterWellRepairInfoById(id));
    }

    @PostMapping("insertWaterMeterWellWaterMeterInfo")
    public Object insertWaterMeterWellWaterMeterInfo(@RequestBody WaterMeterInfo waterMeterInfo) {
        Integer res =
                waterMeterInfoMapper.insertWaterMeterWellWaterMeterInfo(
                        waterMeterInfo.waterMeterId,
                        waterMeterInfo.wallId,
                        waterMeterInfo.paymentNumber,
                        waterMeterInfo.accountNumber,
                        waterMeterInfo.accountName,
                        waterMeterInfo.nature,
                        waterMeterInfo.updateTime);

        return response(0, "test", res.toString());
    }

    @GetMapping("getWaterMeterWellWaterMeterInfoByWallId")
    public Object getWaterMeterWellWaterMeterInfoByWallId(HttpServletRequest request) {
        int wallId = Integer.parseInt(request.getParameter("wallId"));
        return response(
                0,
                "获得水表信息成功",
                waterMeterInfoMapper.getWaterMeterWellWaterMeterInfoByWallId(wallId));
    }

    @PostMapping("deleteWaterMeterInfoById")
    public Object deleteWaterMeterInfoById(@RequestBody Retire input, HttpServletRequest request) {
        meterActions.retire(input, "watermeterwell", Sessions.user(request));
        return response(0, "水表已停用");
    }
}
