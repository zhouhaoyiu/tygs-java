package com.zhy.market.controller;

import com.zhy.market.mapper.HubiaozongguanMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("Hubiaozongguan")
public class HubiaozongguanController {
    private final HubiaozongguanMapper hubiaozongguanMapper;

    public HubiaozongguanController(HubiaozongguanMapper hubiaozongguanMapper) {
        this.hubiaozongguanMapper = hubiaozongguanMapper;
    }

    @GetMapping("getAllHubiaozongguan")
    public Object getAllHubiaozongguan() {
        return hubiaozongguanMapper.getAllHubiaozongguan();
    }
}
