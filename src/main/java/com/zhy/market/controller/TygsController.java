package com.zhy.market.controller;

import com.zhy.market.mapper.FireHydrantMapper;
import com.zhy.market.mapper.ValueWellMapper;
import com.zhy.market.mapper.WaterMeterRoomMapper;
import com.zhy.market.mapper.WaterMeterWellMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

/**
 * @author Administrator
 */
@RequestMapping("Tygs")
@RestController
public class TygsController {
    @Resource
    private WaterMeterRoomMapper waterMeterRoomMapper;

    @Resource
    private WaterMeterWellMapper waterMeterWellMapper;

    @Resource
    private ValueWellMapper valueWellMapper;

    @Resource
    private FireHydrantMapper fireHydrantMapper;

    // 获得每种水表的数量
    @GetMapping("getWaterMeterCount")
    public Object getWaterMeterRoomCount() {
        java.util.Map<String, Object> json = new java.util.HashMap<>();
        json.put("code", 0);
        json.put("waterMeterRoomCount", waterMeterRoomMapper.getAllWaterMeterRoomInfoCount());
        json.put("waterMeterWellCount", waterMeterWellMapper.getAllWaterMeterWellInfoCount());
        json.put("valueWellCount", valueWellMapper.getAllValueWellInfoCount());
        json.put("fireHydrant", fireHydrantMapper.getAllFireHydrantInfoCount());

        return json;
    }

}
