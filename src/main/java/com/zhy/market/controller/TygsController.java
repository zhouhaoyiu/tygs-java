package com.zhy.market.controller;

import com.zhy.market.mapper.FireHydrantMapper;
import com.zhy.market.mapper.ValueWellMapper;
import com.zhy.market.mapper.WaterMeterWellMapper;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zhy.market.mapper.WaterMeterRoomMapper;

@RequestMapping("tygs")
@RestController
public class TygsController {
    @Autowired
    private WaterMeterRoomMapper waterMeterRoomMapper;

    @Autowired
    private WaterMeterWellMapper waterMeterWellMapper;

    @Autowired
    private ValueWellMapper valueWellMapper;

    @Autowired
    private FireHydrantMapper fireHydrantMapper;

    // 获得每种水表的数量
    @GetMapping("getWaterMeterCount")
    public Object getWaterMeterRoomCount() {
        JSONObject json = new JSONObject();
        json.put("code", 0);
        json.put("waterMeterRoomCount", waterMeterRoomMapper.getAllWaterMeterRoomInfoCount());
        json.put("waterMeterWellCount", waterMeterWellMapper.getAllWatermeterwellInfoCount());
        json.put("valueWellCount", valueWellMapper.getAllValueWellInfoCount());
        json.put("fireHydrant", fireHydrantMapper.getAllFireHydrantInfoCount());

        return json;
    }

}
