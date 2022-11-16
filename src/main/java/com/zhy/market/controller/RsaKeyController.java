package com.zhy.market.controller;

import com.zhy.market.mapper.RsaKeyMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
