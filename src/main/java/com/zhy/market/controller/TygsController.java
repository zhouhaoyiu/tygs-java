package com.zhy.market.controller;

import com.zhy.market.mapper.FireHydrantMapper;
import com.zhy.market.mapper.ValueWellMapper;
import com.zhy.market.mapper.WaterMeterRoomMapper;
import com.zhy.market.mapper.WaterMeterWellMapper;
import java.util.HashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Administrator
 */
@RequestMapping("Tygs")
@RestController
public class TygsController {
    private final WaterMeterRoomMapper waterMeterRoomMapper;

    private final WaterMeterWellMapper waterMeterWellMapper;

    private final ValueWellMapper valueWellMapper;

    private final FireHydrantMapper fireHydrantMapper;

    public TygsController(
            WaterMeterRoomMapper waterMeterRoomMapper,
            WaterMeterWellMapper waterMeterWellMapper,
            ValueWellMapper valueWellMapper,
            FireHydrantMapper fireHydrantMapper) {
        this.waterMeterRoomMapper = waterMeterRoomMapper;
        this.waterMeterWellMapper = waterMeterWellMapper;
        this.valueWellMapper = valueWellMapper;
        this.fireHydrantMapper = fireHydrantMapper;
    }

    // 获得每种水表的数量
    @GetMapping("getWaterMeterCount")
    public Object getWaterMeterRoomCount() {
        Map<String, Object> json = new HashMap<>();
        json.put("code", 0);
        json.put("waterMeterRoomCount", waterMeterRoomMapper.getAllWaterMeterRoomInfoCount());
        json.put("waterMeterWellCount", waterMeterWellMapper.getAllWaterMeterWellInfoCount());
        json.put("valueWellCount", valueWellMapper.getAllValueWellInfoCount());
        json.put("fireHydrant", fireHydrantMapper.getAllFireHydrantInfoCount());

        return json;
    }
}
