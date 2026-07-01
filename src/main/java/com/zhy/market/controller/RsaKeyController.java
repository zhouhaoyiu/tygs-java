package com.zhy.market.controller;

import com.zhy.market.mapper.RsaKeyMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @author Administrator
 */
@RequestMapping("rsa")
@RestController
public class RsaKeyController {
    @Resource
    private RsaKeyMapper rsaKeyMapper;

    @GetMapping("pubKey")
    public String getPubKey() {
        return rsaKeyMapper.getPublicKey();
    }
}
