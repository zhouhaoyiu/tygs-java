package com.zhy.market.controller;

import com.zhy.market.mapper.ValueWallMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("ValueWall")
public class ValueWallController {

    @Autowired
    private ValueWallMapper valueWallMapper;

    @GetMapping("getAllValueWallInfo")
    public Object getAllValueWallInfo(){
        return valueWallMapper.getAllValueWallInfo();
    }
}
