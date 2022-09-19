package com.zhy.market.controller;

import com.zhy.market.mapper.Sheet1Mapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RequestMapping("sheet1")
@RestController

public class Sheet1Controller {

    @Resource
    private Sheet1Mapper sheet1Mapper;

    @GetMapping("getInfoByPage")
    public Object getAllInfo(HttpServletRequest request) {
        Integer page = Integer.parseInt(request.getParameter("page"));
        Integer size = 30;
        Integer start = (page - 1) * size;
        return sheet1Mapper.selectInfoByPage(start, size);
    }

    @GetMapping("getInfoByPageCount")
    public Integer getAllInfoCount() {
        return sheet1Mapper.selectInfoCount();
    }

    @GetMapping("getInfoByPageAndName")
    public Object getInfoByPageAndName(HttpServletRequest request) {
        Integer page = Integer.parseInt(request.getParameter("page"));
        String name = request.getParameter("mc");
        Integer size = 30;
        Integer start = (page - 1) * size;
        return sheet1Mapper.selectInfoByPageAndName(start, size, name);
    }

    @GetMapping("getInfoByPageAndNameCount")
    public Integer getInfoByPageAndNameCount(HttpServletRequest request) {
        String name = request.getParameter("mc");
        return sheet1Mapper.selectInfoByPageAndNameCount(name);
    }

    @GetMapping("getInfoByPageAndJfh")
    public Object getInfoByPageAndJfh(HttpServletRequest request) {
        Integer page = Integer.parseInt(request.getParameter("page"));
        String jfh = request.getParameter("jfh");
        Integer size = 30;
        Integer start = (page - 1) * size;
        return sheet1Mapper.selectInfoByPageAndJfh(start, size, jfh);
    }

    @GetMapping("getInfoByPageAndJfhCount")
    public Integer getInfoByPageAndJfhCount(HttpServletRequest request) {
        String jfh = request.getParameter("jfh");
        return sheet1Mapper.selectInfoByPageAndJfhCount(jfh);
    }

    @GetMapping("getInfoByPageAndDz")
    public Object getInfoByPageAndDz(HttpServletRequest request) {
        Integer page = Integer.parseInt(request.getParameter("page"));
        String dz = request.getParameter("dz");
        Integer size = 30;
        Integer start = (page - 1) * size;
        return sheet1Mapper.selectInfoByPageAndDz(start, size, dz);
    }

    @GetMapping("getInfoByPageAndDzCount")
    public Integer getInfoByPageAndDzCount(HttpServletRequest request) {
        String dz = request.getParameter("dz");
        return sheet1Mapper.selectInfoByPageAndDzCount(dz);
    }



}
