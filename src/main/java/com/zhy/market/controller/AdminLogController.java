package com.zhy.market.controller;

import com.zhy.market.domain.AdminLog;
import com.zhy.market.mapper.AdminLogMapper;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;

import static com.zhy.market.controller.utils.getJsonRes;

/**
 * @author Administrator
 */
@RestController
@RequestMapping("adminLog")
public class AdminLogController {

    @Resource
    private AdminLogMapper adminLogMapper;

    @PostMapping("addAdminLog")
    public Object addAdminLog(@RequestBody @NotNull AdminLog adminLog) {
        String adminLogUUid = String.valueOf(UUID.randomUUID());
        String adminUUid = adminLog.getAdminUUid();
        String logTime = adminLog.getLogTime();
        String info = adminLog.getInfo();
        String remark = adminLog.getRemark();

        Integer addRes = adminLogMapper.addAdminLog(adminLogUUid, adminUUid, logTime, info, remark);
        if (addRes > 0) {
            return getJsonRes(1, "新增管理员日志成功", (List) null);
        }

        return getJsonRes(0, "新增管理员日志失败", (List) null);
    }

    @GetMapping("getAllAdminLog")
    public Object getAllAdminLog() {
        List<AdminLog> adminLogs = adminLogMapper.getAllAdminLog();
        return getJsonRes(1, "获得管理员日志成功", adminLogs);
    }

    @GetMapping("getAdminLogByAdminUUid")
    public Object getAdminLogByAdminUUid(@RequestParam String adminUUid) {
        List<AdminLog> adminLogs = adminLogMapper.getAdminLogByAdminUUid(adminUUid);
        return getJsonRes(1, "获得管理员日志成功", adminLogs);
    }
}