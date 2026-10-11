package com.zhy.market;
import com.zhy.market.security.*;
import com.zhy.market.domain.RepairInfo;
import com.zhy.market.mapper.AdminLogMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RepairTests {
    private final JdbcTemplate db=mock(JdbcTemplate.class);
    private final AdminLogMapper logs=mock(AdminLogMapper.class);
    private final Sessions.User user=new Sessions.User("u","test","staff");
    private final RepairActions actions=new RepairActions(db,new ObjectMapper(),logs);
    private RepairInfo input() {
        var input=new RepairInfo();input.id=1;input.requestId="repair-request-123456";
        input.expectedInfo="[]";input.repairInfo="[{\"text\":\"更换阀门\",\"repairTime\":\"2026-10-12\",\"remarks\":\"\"}]";
        return input;
    }
    @Test void appendsWithAuditAndServerActor() {
        when(db.queryForList("SELECT repairInfo FROM watermeterwell WHERE id=? FOR UPDATE",1)).thenReturn(List.of(Map.of("repairInfo","[]")));
        when(db.update(eq("UPDATE watermeterwell SET repairInfo=? WHERE id=?"),anyString(),eq(1))).thenReturn(1);
        when(logs.addAdminLog(anyString(),anyString(),anyString(),anyString(),anyString())).thenReturn(1);
        actions.append(input(),"watermeterwell",user);
        verify(db).update(eq("UPDATE watermeterwell SET repairInfo=? WHERE id=?"),contains("\"actor\":\"u\""),eq(1));
    }
    @Test void concurrentHistoryCannotBeOverwritten() {
        when(db.queryForList("SELECT repairInfo FROM watermeterwell WHERE id=? FOR UPDATE",1)).thenReturn(List.of(Map.of("repairInfo","[{\"text\":\"另一条记录\"}]")));
        assertThrows(ResponseStatusException.class,()->actions.append(input(),"watermeterwell",user));
        verify(db,never()).update(anyString(),anyString(),anyInt());
    }
    @Test void duplicateRequestDoesNotAppendTwice() {
        when(db.queryForList("SELECT repairInfo FROM watermeterwell WHERE id=? FOR UPDATE",1)).thenReturn(List.of(Map.of("repairInfo","[{\"requestId\":\"repair-request-123456\",\"text\":\"更换阀门\",\"repairTime\":\"2026-10-12\",\"remarks\":\"\"}]")));
        actions.append(input(),"watermeterwell",user);
        verify(db,never()).update(anyString(),anyString(),anyInt());
        verifyNoInteractions(logs);
    }
}
