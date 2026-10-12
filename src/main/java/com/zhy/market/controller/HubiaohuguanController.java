package com.zhy.market.controller;

import com.zhy.market.mapper.HubiaohuguanMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("Hubiaohuguan")
public class HubiaohuguanController {
    private final HubiaohuguanMapper hubiaohuguanMapper;

    public HubiaohuguanController(HubiaohuguanMapper hubiaohuguanMapper) {
        this.hubiaohuguanMapper = hubiaohuguanMapper;
    }

    @GetMapping("getAllHubiaohuguan")
    public Object getAllHubiaohuguan() {
        return hubiaohuguanMapper.getAllHubiaohuguan();
    }
}
