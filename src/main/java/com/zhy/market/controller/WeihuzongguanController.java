package com.zhy.market.controller;

import com.zhy.market.mapper.HubiaozongguanMapper;
import com.zhy.market.mapper.WeihuzongguanMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

@RestController
@RequestMapping("Weihuzongguan")
public class WeihuzongguanController {
    @Resource
    private WeihuzongguanMapper weihuzongguanMapper;

    @GetMapping("getAllWeihuzongguan")
    public Object getAllWeihuzongguan() {
        return weihuzongguanMapper.getAllWeihuzongguan();
    }
}
