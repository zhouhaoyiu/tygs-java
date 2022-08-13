package com.zhy.market.controller;


import com.zhy.market.domain.MarketInfo;
import com.zhy.market.mapper.MarketInfoMapper;
import net.sf.json.JSONObject;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

import static com.zhy.market.controller.utils.getJsonRes;

@RestController
@RequestMapping("marketInfo")
public class MarketInfoController {

    @Resource
    private MarketInfoMapper marketInfoMapper;

    @GetMapping("getMarketInfo")
    public Object getMarketInfo() {

        List<MarketInfo> info = marketInfoMapper.getMarketInfo();

        JSONObject json = new JSONObject();
        json.put("data", info);
        return json;
    }

    @PostMapping("setMarketInfo")
    public Object setMarketInfo(@RequestBody MarketInfo marketInfo) {
        String marketName = marketInfo.getMarketName();
        String marketRecommend = marketInfo.getMarketRecommend();
        String marketMeta = marketInfo.getMarketMeta();
        String marketImages = marketInfo.getMarketImages();

        Integer res = marketInfoMapper.setMarketInfo(marketName, marketRecommend, marketMeta, marketImages);
        if (res >= 0) {
            return getJsonRes(1, "修改商城信息成功", (List) null);
        }
        return getJsonRes(0, "修改商城信息失败", (List) null);
    }

}
