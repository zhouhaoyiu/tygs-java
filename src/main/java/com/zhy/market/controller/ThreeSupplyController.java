package com.zhy.market.controller;

import com.zhy.market.mapper.ThreeSupplyMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

/**
 * @author Administrator
 */
@RequestMapping("threeSupply")
@RestController
public class ThreeSupplyController {

    @Resource
    private ThreeSupplyMapper threeSupplyMapper;

    @GetMapping("getAllThreeSupply")
    public Object getAllThreeSupply(){
        return threeSupplyMapper.getAllThreeSupplyInfo();
    }

}
