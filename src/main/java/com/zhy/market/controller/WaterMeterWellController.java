package com.zhy.market.controller;

import static com.zhy.market.controller.utils.getJsonRes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zhy.market.domain.RepairInfo;
import com.zhy.market.domain.WaterMeterInfo;
import com.zhy.market.domain.Watermeterwell;
import com.zhy.market.mapper.WaterMeterInfoMapper;
import com.zhy.market.mapper.WaterMeterWellMapper;

@RestController
@RequestMapping("WaterMeterWell")
public class WaterMeterWellController {

    @Resource
    private WaterMeterInfoMapper waterMeterInfoMapper;
    @Resource
    private WaterMeterWellMapper watermeterwellMapper;

    @GetMapping("getAllWaterMeterWellInfo")
    public Object getAllInfo() {
        return watermeterwellMapper.getAllWaterMeterWellInfo();
    }

    @PostMapping("addWaterMeterWell")
    public Object addWaterMeterWell(@RequestBody Watermeterwell watermeterwell, HttpServletRequest request) {
        Integer res = watermeterwellMapper.addWaterMeterWell(watermeterwell.filledBy, watermeterwell.department,
                watermeterwell.writtingTime, watermeterwell.userType, watermeterwell.accountIdentifier,
                watermeterwell.accountName, watermeterwell.accountNumber, watermeterwell.address,
                watermeterwell.coordinates, watermeterwell.caliber, watermeterwell.wellDepth,
                watermeterwell.includedFacilities, watermeterwell.waterMeterManufacturer,
                watermeterwell.operatingStatus, watermeterwell.wellOutside, watermeterwell.wellInside,
                watermeterwell.waterNature, watermeterwell.operatingUsersNumber,
                watermeterwell.operatingUserDetailsAndDetailedAddress, watermeterwell.specialBankUsersNumber,
                watermeterwell.specialBankUserDetailsAndDetailedAddress);
        if (res == 1) {
            return getJsonRes(0, "添加成功");
        } else {
            return getJsonRes(1, "添加失败");
        }
    }

    @PostMapping("updateWaterMeterWellRepairInfoWithId")
    public Object updateWaterMeterWellRepairInfoWithId(@RequestBody RepairInfo repairInfo) {
        String repairInfoText = repairInfo.repairInfo;
        Integer id = repairInfo.id;

        Integer res = watermeterwellMapper.updateWaterMeterWellRepairInfoWithId(repairInfoText, id);
        if (res == 1) {
            return getJsonRes(0, "更新维修记录成功");
        } else {
            return getJsonRes(1, "更新维修记录失败");
        }
    }

    @GetMapping("getWaterMeterWellRepairInfoById")
    public Object getWaterMeterWellRepairInfoById(HttpServletRequest request) {
        Integer id = Integer.valueOf(request.getParameter("id"));

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
        Integer wallId = Integer.valueOf(request.getParameter("wallId"));
        return getJsonRes(0, "获得水表信息成功", waterMeterInfoMapper.getWaterMeterWellWaterMeterInfoByWallId(wallId));
    }

    @GetMapping("deleteWaterMeterInfoById")
    public Object deleteWaterMeterInfoById(HttpServletRequest request) {
        String waterMeterId = request.getParameter("waterMeterId");
        Integer res = waterMeterInfoMapper.updateWaterMeterInfoStatus(waterMeterId, "1");
        if (res == 1) {
            return getJsonRes(0, "删除水表信息成功");
        } else {
            return getJsonRes(1, "删除水表信息失败");
        }
    }

}
