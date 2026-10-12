package com.zhy.market.security;

import com.zhy.market.mapper.AdminLogMapper;
import com.zhy.market.mapper.WaterMeterInfoMapper;
import java.time.Instant;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class MeterActions {
    private final WaterMeterInfoMapper meters;
    private final AdminLogMapper logs;

    public record Retire(String waterMeterId, Integer wallId, String reason) {}

    public MeterActions(WaterMeterInfoMapper meters, AdminLogMapper logs) {
        this.meters = meters;
        this.logs = logs;
    }

    @Transactional
    public void retire(Retire input, String source, Sessions.User user) {
        if (input.waterMeterId() == null
                || input.waterMeterId().isBlank()
                || input.wallId() == null
                || input.wallId() <= 0
                || input.reason() == null
                || input.reason().isBlank()
                || input.reason().length() > 1000)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "请填写水表所属设施和停用原因");
        int count = meters.retire(input.waterMeterId(), input.wallId(), source);
        if (count != 1) throw new ResponseStatusException(HttpStatus.CONFLICT, "水表不存在、已停用或所属设施已变化");
        if (logs.addAdminLog(
                        UUID.randomUUID().toString(),
                        user.id(),
                        Instant.now().toString(),
                        "停用水表 " + source + "/" + input.wallId() + "/" + input.waterMeterId(),
                        input.reason())
                != 1) throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
