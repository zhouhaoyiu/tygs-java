package com.zhy.market.controller;

import com.zhy.market.mapper.HubiaohuguanMapper;
import com.zhy.market.mapper.HubiaozongguanMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

@RestController
@RequestMapping("Hubiaohuguan")
public class HubiaohuguanController {
    @Resource
    private HubiaohuguanMapper hubiaohuguanMapper;

    @GetMapping("getAllHubiaohuguan")
    public Object getAllHubiaohuguan() {
        return hubiaohuguanMapper.getAllHubiaohuguan();
    }
}
