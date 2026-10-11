package com.zhy.market.controller;

import com.zhy.market.domain.RepairInfo;
import com.zhy.market.domain.WaterMeterInfo;
import com.zhy.market.domain.Watermeterwell;
import com.zhy.market.mapper.WaterMeterInfoMapper;
import com.zhy.market.mapper.WaterMeterWellMapper;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

import static com.zhy.market.controller.utils.getJsonRes;

/**
 * @author Administrator
 */
@RestController
@RequestMapping("WaterMeterWell")
public class WaterMeterWellController {
    @Resource private com.zhy.market.security.MeterActions meterActions;
    @Resource private com.zhy.market.security.RepairActions repairActions;

    @Resource
    private WaterMeterInfoMapper waterMeterInfoMapper;
    @Resource
    private WaterMeterWellMapper watermeterwellMapper;

    @GetMapping("getAllWaterMeterWellInfo")
    public Object getAllInfo() {
        return watermeterwellMapper.getAllWaterMeterWellInfo();
    }

    //    filledBy: "",
    //    accountName: "",
    //    accountNumber: "",
    //    address: "",
    //    coordinates: "",
    //    caliber: "",
    //    operatingStatus: "",
    //    waterNature: "",
    //    wellDepth: "",
    //    includedFacilities: "",
    //    waterMeterManufacturer: "",
    //    accountIdentifier: "",
    @PostMapping("addWaterMeterWell")
    public Object addWaterMeterWell(@RequestBody Watermeterwell watermeterwell, HttpServletRequest request) {
        int res = watermeterwellMapper.addWaterMeterWell(watermeterwell.filledBy, watermeterwell.accountName, watermeterwell.accountNumber, watermeterwell.address,
                watermeterwell.coordinates, watermeterwell.caliber, watermeterwell.operatingStatus, watermeterwell.waterNature, watermeterwell.wellDepth,
                watermeterwell.includedFacilities, watermeterwell.waterMeterManufacturer, watermeterwell.accountIdentifier,watermeterwell.writtingTime);
        if (res == 1) {
            return getJsonRes(0, "添加成功");
        } else {
            return getJsonRes(1, "添加失败");
        }
    }

    @PostMapping("updateWaterMeterWellRepairInfoWithId")
    public Object updateWaterMeterWellRepairInfoWithId(@RequestBody RepairInfo input, HttpServletRequest request) {
        repairActions.append(input,"watermeterwell",com.zhy.market.security.Sessions.user(request));
        return getJsonRes(0,"维修记录已保存");
    }

    @GetMapping("getWaterMeterWellRepairInfoById")
    public Object getWaterMeterWellRepairInfoById(HttpServletRequest request) {
        int id = Integer.parseInt(request.getParameter("id"));

        return getJsonRes(0, "获得维修记录成功", watermeterwellMapper.getWaterMeterWellRepairInfoById(id));
    }

    @PostMapping("insertWaterMeterWellWaterMeterInfo")
    public Object insertWaterMeterWellWaterMeterInfo(@RequestBody WaterMeterInfo waterMeterInfo) {
        Integer res = waterMeterInfoMapper.insertWaterMeterWellWaterMeterInfo(waterMeterInfo.waterMeterId,
                waterMeterInfo.wallId, waterMeterInfo.paymentNumber,
                waterMeterInfo.accountNumber, waterMeterInfo.accountName, waterMeterInfo.nature,
                waterMeterInfo.updateTime);

        return getJsonRes(0, "test", res.toString());
    }

    @GetMapping("getWaterMeterWellWaterMeterInfoByWallId")
    public Object getWaterMeterWellWaterMeterInfoByWallId(HttpServletRequest request) {
        int wallId = Integer.parseInt(request.getParameter("wallId"));
        return getJsonRes(0, "获得水表信息成功", waterMeterInfoMapper.getWaterMeterWellWaterMeterInfoByWallId(wallId));
    }

    @PostMapping("deleteWaterMeterInfoById")
    public Object deleteWaterMeterInfoById(@RequestBody com.zhy.market.security.MeterActions.Retire input, HttpServletRequest request) {
        meterActions.retire(input, "watermeterwell", com.zhy.market.security.Sessions.user(request));
        return getJsonRes(0, "水表已停用");
    }
}
