package com.zhy.market.controller;

import com.zhy.market.mapper.HubiaozongguanMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

@RestController
@RequestMapping("Hubiaozongguan")
public class HubiaozongguanController {
    @Resource
    private HubiaozongguanMapper hubiaozongguanMapper;

    @GetMapping("getAllHubiaozongguan")
    public Object getAllHubiaozongguan() {
        return hubiaozongguanMapper.getAllHubiaozongguan();
    }
}
