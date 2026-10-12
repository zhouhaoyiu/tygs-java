package com.zhy.market.controller;

import static com.zhy.market.controller.LegacyResponses.response;

import com.zhy.market.domain.AdminLog;
import com.zhy.market.mapper.AdminLogMapper;
import com.zhy.market.security.Sessions;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.bind.annotation.*;

/**
 * @author Administrator
 */
@RestController
@RequestMapping("adminLog")
public class AdminLogController {

    private final AdminLogMapper adminLogMapper;

    public AdminLogController(AdminLogMapper adminLogMapper) {
        this.adminLogMapper = adminLogMapper;
    }

    @PostMapping("addAdminLog")
    public Object addAdminLog(@RequestBody @NotNull AdminLog adminLog, HttpServletRequest request) {
        String adminLogUUid = String.valueOf(UUID.randomUUID());
        String adminUUid = Sessions.user(request).id();
        String logTime = Instant.now().toString();
        String info = adminLog.getInfo();
        String remark = adminLog.getRemark();

        int addRes = adminLogMapper.addAdminLog(adminLogUUid, adminUUid, logTime, info, remark);
        if (addRes > 0) {
            return response(1, "新增管理员日志成功", null);
        }

        return response(0, "新增管理员日志失败", null);
    }

    @GetMapping("getAllAdminLog")
    public Object getAllAdminLog() {
        List<AdminLog> adminLogs = adminLogMapper.getAllAdminLog();
        return response(1, "获得管理员日志成功", adminLogs);
    }

    @GetMapping("getAdminLogByAdminUUid")
    public Object getAdminLogByAdminUUid(
            @RequestParam String adminUUid, HttpServletRequest request) {
        Sessions.owner(request, adminUUid);
        List<AdminLog> adminLogs = adminLogMapper.getAdminLogByAdminUUid(adminUUid);
        return response(1, "获得管理员日志成功", adminLogs);
    }
}
