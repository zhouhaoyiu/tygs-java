package com.zhy.market.controller;

import com.zhy.market.mapper.Sheet1Mapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author Administrator
 */
@RequestMapping("sheet1")
@RestController

public class Sheet1Controller {
    @Resource
    private Sheet1Mapper sheet1Mapper;

    @GetMapping("getInfoByPage")
    public Object getAllInfo(HttpServletRequest request) {
        int page = Integer.parseInt(request.getParameter("page"));
        int size = 30;
        int start = (page - 1) * size;
        return sheet1Mapper.selectInfoByPage(start, size);
    }

    @GetMapping("getInfoByPageCount")
    public int getAllInfoCount() {
        return sheet1Mapper.selectInfoCount();
    }

    @GetMapping("getInfoByPageAndName")
    public Object getInfoByPageAndName(HttpServletRequest request) {
        int page = Integer.parseInt(request.getParameter("page"));
        String name = request.getParameter("mc");
        int size = 30;
        int start = (page - 1) * size;
        return sheet1Mapper.selectInfoByPageAndName(start, size, name);
    }

    @GetMapping("getInfoByPageAndNameCount")
    public Integer getInfoByPageAndNameCount(HttpServletRequest request) {
        String name = request.getParameter("mc");
        return sheet1Mapper.selectInfoByPageAndNameCount(name);
    }

    @GetMapping("getInfoByPageAndJfh")
    public Object getInfoByPageAndJfh(HttpServletRequest request) {
        int page = Integer.parseInt(request.getParameter("page"));
        String jfh = request.getParameter("jfh");
        int size = 30;
        int start = (page - 1) * size;
        return sheet1Mapper.selectInfoByPageAndJfh(start, size, jfh);
    }

    @GetMapping("getInfoByPageAndJfhCount")
    public int getInfoByPageAndJfhCount(HttpServletRequest request) {
        String jfh = request.getParameter("jfh");
        return sheet1Mapper.selectInfoByPageAndJfhCount(jfh);
    }

    @GetMapping("getInfoByPageAndHh")
    public Object getInfoByPageAndHh(HttpServletRequest request) {
        int page = Integer.parseInt(request.getParameter("page"));
        String hh = request.getParameter("hh");
        int size = 30;
        int start = (page - 1) * size;
        return sheet1Mapper.selectInfoByPageAndHh(start, size, hh);
    }

    @GetMapping("getInfoByPageAndHhCount")
    public Integer getInfoByPageAndHhCount(HttpServletRequest request) {
        String hh = request.getParameter("hh");
        return sheet1Mapper.selectInfoByPageAndHhCount(hh);
    }

    @GetMapping("getInfoByPageAndXxdz")
    public Object getInfoByPageAndXxdz(HttpServletRequest request) {
        int page = Integer.parseInt(request.getParameter("page"));
        String xxdz = request.getParameter("xxdz");
        int size = 30;
        int start = (page - 1) * size;
        return sheet1Mapper.selectInfoByPageAndXxdz(start, size, xxdz);
    }

    @GetMapping("getInfoByPageAndXxdzCount")
    public int getInfoByPageAndXxdzCount(HttpServletRequest request) {
        String xxdz = request.getParameter("xxdz");
        return sheet1Mapper.selectInfoByPageAndXxdzCount(xxdz);
    }
}
