package com.zhy.market.controller;


import com.zhy.market.domain.MarketOrder;
import com.zhy.market.mapper.GoodsMapper;
import com.zhy.market.mapper.MarketOrderMapper;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;

import static com.zhy.market.controller.utils.getJsonRes;

@RestController
@RequestMapping("order")
public class MarketOrderController {

    @Resource
    private MarketOrderMapper marketOrderMapper;
    @Resource
    private GoodsMapper goodsMapper;


    @GetMapping("getAllOrder")
    public Object getAllOrder() {

        List<MarketOrder> orderData = marketOrderMapper.getAllOrder();
        return getJsonRes(1, "获取订单信息成功", orderData);
    }

    @PostMapping("createOrder")
    public Object createOrder(@RequestBody MarketOrder order) {
        String orderdate = order.getOrderdate();
        String goods = order.getGoods();
        Integer orderStatus = order.getOrderStatus();
        String useruuid = order.getUseruuid();
        String username = order.getUsername();
        String orderaddress = order.getOrderaddress();
        UUID orderuuid = UUID.randomUUID();
        Integer res = marketOrderMapper.createOrder(String.valueOf(orderuuid), orderdate, orderaddress, goods, orderStatus, useruuid, username);
        if (res > 0) {
            return getJsonRes(1, "创建订息成功", (List) null);
        }
        return getJsonRes(0, "创建订息失败", (List) null);
    }

    @GetMapping("setOrderStatus")
    public Object setOrderStatus(HttpServletRequest request) {
        int orderStatus = Integer.parseInt(request.getParameter("orderStatus"));
        String orderuuid = request.getParameter("orderuuid");
        boolean flag = false;

        if (orderStatus == 1) {
            String refuseReason = request.getParameter("refuseReason");
            marketOrderMapper.refuseOrder(orderStatus, refuseReason, orderuuid);
            flag = true;
        }

        if (orderStatus == 2) {
            String remarks = request.getParameter("remarks");
            String orderGoodsStr = request.getParameter("orderGoods");
            JSONArray orderGoodJson = JSONArray.fromObject(orderGoodsStr);

            for (int i = 0; i < orderGoodJson.size(); i++) {
                JSONObject json = (JSONObject) orderGoodJson.get(i);
                Integer orderGoodsNumber = (Integer) json.get("orderGoodsNumber");
                Integer goodId = (Integer) json.get("goodId");
                if ((goodsMapper.getGoodCountById(goodId) - orderGoodsNumber) > 0) {
                    Integer minusGoodCount = goodsMapper.minusGoodCount(goodId, orderGoodsNumber);
                    Integer updateSellCount = goodsMapper.addGoodSellCount(goodId, orderGoodsNumber);
                    if (minusGoodCount > 0 && updateSellCount > 0) {
                        flag = true;
                    }
                }
            }
            Integer res = marketOrderMapper.acceptOrder(orderStatus, remarks, orderuuid);
            System.out.println(res);
        }

        if (flag) {
            return getJsonRes(1, "修改状态成功", (List) null);
        } else return getJsonRes(0, "修改状态失败", (List) null);
    }

    @GetMapping("getOrderByUserName")
    public Object getOrderByUserName(@RequestParam String useruuid) {
        List<MarketOrder> marketOrderByUserName = marketOrderMapper.getOrderByUserName(useruuid);
        if (marketOrderByUserName != null) {
            return getJsonRes(1, "获取成功", marketOrderByUserName);
        }
        return getJsonRes(0, "获取失败", (List) null);
    }
}
