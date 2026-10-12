package com.zhy.market.controller;

import com.zhy.market.mapper.WeihuzongguanMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("Weihuzongguan")
public class WeihuzongguanController {
    private final WeihuzongguanMapper weihuzongguanMapper;

    public WeihuzongguanController(WeihuzongguanMapper weihuzongguanMapper) {
        this.weihuzongguanMapper = weihuzongguanMapper;
    }

    @GetMapping("getAllWeihuzongguan")
    public Object getAllWeihuzongguan() {
        return weihuzongguanMapper.getAllWeihuzongguan();
    }
}
