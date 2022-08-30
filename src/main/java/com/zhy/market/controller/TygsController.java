package com.zhy.market.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zhy.market.mapper.WaterMeterRoomMapper;
import com.zhy.market.mapper.WatermeterwellMapper;

@RequestMapping("tygs")
@RestController
public class TygsController {
  @Autowired
  private WaterMeterRoomMapper waterMeterRoomMapper;

  @Autowired
  private WatermeterwellMapper watermeterwellMapper;

  // 获得每种水表的数量
  @GetMapping("getWaterMeterCount")
  public Object getWaterMeterRoomCount() {
    List<Object> list = new ArrayList<>();
    list.add(waterMeterRoomMapper.getAllWaterMeterRoomInfoCount());
    list.add(watermeterwellMapper.getAllWatermeterwellInfoCount());
    return list;
  }

}
