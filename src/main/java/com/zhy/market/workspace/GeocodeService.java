package com.zhy.market.workspace;

import static com.zhy.market.workspace.WorkspaceErrors.bad;
import static com.zhy.market.workspace.WorkspaceErrors.conflict;

import com.zhy.market.workspace.LocationModels.GeocodeDetails;
import com.zhy.market.workspace.LocationModels.GeocodeHistory;
import com.zhy.market.workspace.LocationModels.GeocodeInput;
import java.time.Instant;
import java.util.Objects;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

@Service
@Profile("local")
public class GeocodeService {
    private final JdbcTemplate db;
    private final ObjectMapper json;

    public GeocodeService(JdbcTemplate db, ObjectMapper json) {
        this.db = db;
        this.json = json;
    }

    private String loadAddress(String table, long id, boolean lock) {
        FacilityTables.validate(table);
        if (lock) {
            db.queryForList("SELECT id FROM " + table + " WHERE id=? FOR UPDATE", id);
        }
        var records =
                db.queryForList(
                        "SELECT address FROM "
                                + FacilityTables.INVENTORY
                                + " r WHERE targetTable=? AND targetId=?",
                        table,
                        id);
        if (records.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "设施记录不存在");
        }
        return Objects.toString(records.get(0).get("address"), "");
    }

    public GeocodeDetails detail(String table, long id) {
        String source = loadAddress(table, id, false);
        String query = "";
        String blocked = "";
        try {
            query = GeocodeRules.query(source);
        } catch (ResponseStatusException e) {
            blocked = e.getReason();
        }
        return new GeocodeDetails(
                source,
                query,
                "太原市",
                blocked,
                db.query(
                        "SELECT * FROM facility_geocode WHERE targetTable=? AND targetId=? ORDER BY id DESC LIMIT 20",
                        (rs, n) ->
                                new GeocodeHistory(
                                        rs.getString("sourceAddress"),
                                        rs.getString("queryAddress"),
                                        rs.getString("decision"),
                                        rs.getString("reason"),
                                        rs.getString("actor"),
                                        rs.getString("createdAt"),
                                        rs.getLong("id"),
                                        source.equals(rs.getString("sourceAddress")),
                                        json.readTree(rs.getString("resultJson"))),
                        table,
                        id));
    }

    @Transactional
    public GeocodeDetails save(String table, long id, GeocodeInput input, String actor) {
        validateInput(input);
        String source = loadAddress(table, id, true);
        if (!source.equals(input.sourceAddress())) {
            throw conflict("原始地址已变化，匹配结果未应用");
        }
        String query = GeocodeRules.query(source);
        String resultJson = json.writeValueAsString(input.result());
        if (resultJson.length() > 200000) {
            throw bad("地址匹配结果过长");
        }
        var existing =
                db.queryForList(
                        "SELECT targetTable,targetId,sourceAddress,resultJson FROM facility_geocode WHERE requestId=?",
                        input.requestId());
        if (!existing.isEmpty()) {
            var old = existing.get(0);
            if (!table.equals(old.get("targetTable"))
                    || id != ((Number) old.get("targetId")).longValue()
                    || !source.equals(old.get("sourceAddress"))
                    || !json.readTree(resultJson)
                            .equals(json.readTree(old.get("resultJson").toString()))) {
                throw conflict("提交标识与已有匹配记录不一致");
            }
            return detail(table, id);
        }
        var decision = GeocodeRules.assess(source, input.result());
        db.update(
                "INSERT INTO facility_geocode(requestId,targetTable,targetId,sourceAddress,queryAddress,decision,reason,longitude,latitude,resultJson,actor,createdAt) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)",
                input.requestId(),
                table,
                id,
                source,
                query,
                decision.status(),
                decision.reason(),
                decision.longitude(),
                decision.latitude(),
                resultJson,
                actor,
                Instant.now().toString());
        return detail(table, id);
    }

    private void validateInput(GeocodeInput input) {
        if (input.requestId() == null
                || !input.requestId().matches("[a-zA-Z0-9-]{16,80}")
                || input.result() == null
                || input.result().candidates() == null
                || input.result().candidates().size() > 20
                || input.result().resultCount() < 0
                || input.result().resultCount() > 1000) {
            throw bad("地址匹配结果无效");
        }
    }
}
