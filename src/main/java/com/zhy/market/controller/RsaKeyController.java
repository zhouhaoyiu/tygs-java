package com.zhy.market.controller;

import com.zhy.market.mapper.RsaKeyMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RequestMapping("rsa")
@RestController
public class RsaKeyController {
    @Autowired
    private RsaKeyMapper rsaKeyMapper;

    @GetMapping("pubKey")
    public String getPubKey(HttpServletRequest request, HttpServletResponse response) {
        String publicKey = rsaKeyMapper.getPublicKey();
        return publicKey;
    }
}
