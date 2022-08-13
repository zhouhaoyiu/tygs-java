package com.zhy.market.controller;

import com.zhy.market.domain.Goods;
import com.zhy.market.mapper.GoodsMapper;
import net.sf.json.JSONObject;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.List;
import java.util.UUID;

import static com.zhy.market.controller.utils.getJsonRes;

@RequestMapping("goods")
@RestController
public class GoodsController {

    @Resource
    private GoodsMapper goodsMapper;

    @PostMapping("upload/image")
    public Object uploadImage(@RequestParam("picture") MultipartFile picture, HttpServletRequest request) {
        // return null;
        String path = "E://market//goods//images";
        File filePath = new File(path);
        if (!filePath.exists()) {
            filePath.mkdirs();
        }

        File file = new File(path, String.valueOf(UUID.randomUUID()) + ".jpg");
        String fileName = file.getName();
        JSONObject json = new JSONObject();
        try {
            picture.transferTo(file);
            json.put("code", 0);
            json.put("msg", "上传成功");
            json.put("data", fileName);

        } catch (Exception e) {
            e.printStackTrace();
            json.put("code", 1);
            json.put("msg", "上传失败");
        }
        return json;
    }

    @PostMapping("addGoods")
    public Object addGoods(@RequestBody Goods good) {
        Integer result = goodsMapper.addGood(good.getGoodName(), good.getGoodClassification(), good.getGoodPrice(), good.getGoodCount(), good.getGoodImages(), good.getGoodDescribeImages());
        JSONObject json = new JSONObject();
        if (result > 0) {
            json.put("code", 0);
            json.put("msg", "添加成功");
        } else {
            json.put("code", 1);
            json.put("msg", "添加失败");
        }
        return json;
    }

    @GetMapping("getAllGoods")
    public Object getAllGoods() {
        List<Goods> list = goodsMapper.getAllGoods();
        return getJsonRes(1,"获得商品成功",list);
    }

    @GetMapping("deleteGoodsById")
    public Object deleteGoods(@Param("id") Integer goodId) {
        Integer result = goodsMapper.deleteGood(goodId);
        JSONObject json = new JSONObject();
        if (result > 0) {
            json.put("code", 0);
            json.put("msg", "删除成功");
        } else {
            json.put("code", 1);
            json.put("msg", "删除失败");
        }
        return null;
    }

    @GetMapping("getGoodsImages")
    public File goodsImages(HttpServletRequest request) {
        return null;
    }

    @GetMapping("addGoodVisitCount")
    public Object addGoodVisitCount(@RequestParam("goodId") Integer goodId) {
        Integer res = goodsMapper.addGoodVisible(goodId);
        if (res > 0) {
            return getJsonRes(1, "", (List) null);
        } else return getJsonRes(0, "", (List) null);
    }
}
