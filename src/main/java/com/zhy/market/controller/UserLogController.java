package com.zhy.market.controller;

import com.zhy.market.domain.UserLog;
import com.zhy.market.mapper.GoodsMapper;
import com.zhy.market.mapper.UserLogMapper;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;

import static com.zhy.market.controller.utils.getJsonRes;

@RequestMapping("userLog")
@RestController
public class UserLogController {
    @Resource
    private UserLogMapper userLogMapper;
    @Resource
    private GoodsMapper goodsMapper;

    @PostMapping("addUserLog")
    public Object addUserLog(@RequestBody UserLog userLog) {
        String loguuid = String.valueOf(UUID.randomUUID());
        String useruuid = userLog.getUseruuid();
        String type = userLog.getType();
        Integer gid = userLog.getGid();
        String logtime = userLog.getLogtime();

        Integer res = userLogMapper.addUserLog(loguuid, useruuid, type, gid, logtime);
        if (res > 0) {
            goodsMapper.addGoodVisible(gid);
            return getJsonRes(0, "添加访问日志成功", (List) null);
        }
        return getJsonRes(1, "添加访问日志失败", (List) null);
    }


    @GetMapping("getAllUserLog")
    public Object getAllUserLog() {
        List<UserLog> data = userLogMapper.getAllUserLog();
        return getJsonRes(1, "获取成功", data);
    }

}
