package com.zhy.market.security;
import com.zhy.market.domain.RepairInfo;
import com.zhy.market.mapper.AdminLogMapper;
import tools.jackson.databind.*;
import tools.jackson.databind.node.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;
import java.util.*;

@Service
public class RepairActions {
    private final JdbcTemplate db; private final ObjectMapper json; private final AdminLogMapper logs;
    public RepairActions(JdbcTemplate db,ObjectMapper json,AdminLogMapper logs) {this.db=db;this.json=json;this.logs=logs;}
    private ArrayNode parse(String text) {
        if(text==null||text.isBlank()||text.equals("null")) return json.createArrayNode();
        try {var value=json.readTree(text);if(value.isArray()) return (ArrayNode)value;} catch(Exception ignored) {}
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"维修记录格式错误");
    }
    @Transactional
    public void append(RepairInfo input,String table,Sessions.User user) {
        if(!Set.of("watermeterwell","watermeterroom","valuewell","firehydrant").contains(table)) throw new IllegalArgumentException();
        if(input.id==null||input.id<=0||input.expectedInfo==null||input.requestId==null||!input.requestId.matches("[A-Za-z0-9_-]{16,64}")||input.repairInfo==null||input.repairInfo.length()>1000000) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        var rows=db.queryForList("SELECT repairInfo FROM "+table+" WHERE id=? FOR UPDATE",input.id);
        if(rows.size()!=1) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        var previous=parse((String)rows.get(0).get("repairInfo"));
        var expected=parse(input.expectedInfo);var incoming=parse(input.repairInfo);
        if(incoming.size()!=expected.size()+1||!incoming.get(incoming.size()-1).isObject()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        for(int i=0;i<expected.size();i++) if(!expected.get(i).equals(incoming.get(i))) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"不能改写历史维修记录");
        var entry=(ObjectNode)incoming.get(incoming.size()-1);
        if(entry.path("text").asText().isBlank()||entry.path("repairTime").asText().isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"请填写维修内容与日期");
        for(var existing:previous) if(input.requestId.equals(existing.path("requestId").asText())) {
            for(String key:List.of("text","repairTime","remarks")) if(!existing.path(key).equals(entry.path(key))) throw new ResponseStatusException(HttpStatus.CONFLICT);
            return;
        }
        if(!previous.equals(expected)) throw new ResponseStatusException(HttpStatus.CONFLICT,"维修记录已更新，请重新读取后核对");
        entry.put("requestId",input.requestId);entry.put("actor",user.id());entry.put("time",Instant.now().toString());
        if(db.update("UPDATE "+table+" SET repairInfo=? WHERE id=?",incoming.toString(),input.id)!=1) throw new ResponseStatusException(HttpStatus.CONFLICT);
        if(logs.addAdminLog(UUID.randomUUID().toString(),user.id(),Instant.now().toString(),"追加维修记录 "+table+"/"+input.id,entry.path("text").asText())!=1) throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
