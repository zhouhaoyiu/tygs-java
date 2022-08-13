package com.zhy.market.controller;

import com.zhy.market.domain.GoodsClassification;
import com.zhy.market.mapper.GoodsClassificationMapper;
import net.sf.json.JSONObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("goodsClassification")
public class GoodsClassificationController {

    @Resource
    private GoodsClassificationMapper goodsClassificationMapper;

    @GetMapping("addClassification")
    public Object addClassification(HttpServletRequest request) {
        String classificationName = request.getParameter("classificationName");
        Integer parentId = Integer.valueOf(request.getParameter("parentId"));
        Integer check = goodsClassificationMapper.checkIdExist(parentId);
        JSONObject json = new JSONObject();
        if (check == 0) {
            json.put("code", 1);
            json.put("msg", "当前父分类不存在");
        }
        else {
            Integer result = goodsClassificationMapper.addClassification(classificationName, parentId);

            if (result == 1) {
                json.put("code", 0);
                json.put("msg", "添加商品分类成功");
            } else {
                json.put("code", 1);
                json.put("msg", "添加商品分类失败");
            }
        }
        return json;
    }

    @GetMapping("getAllClassfication")
    public Object getAllClassfication() {
        List<GoodsClassification> result = goodsClassificationMapper.getAllGoodsClassification();
        JSONObject json = new JSONObject();
        json.put("code", 0);
        json.put("data", result);
        return json;
    }
}
