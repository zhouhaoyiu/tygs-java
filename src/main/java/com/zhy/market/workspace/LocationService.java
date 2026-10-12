package com.zhy.market.workspace;

import static com.zhy.market.workspace.WorkspaceErrors.bad;
import static com.zhy.market.workspace.WorkspaceErrors.conflict;

import com.zhy.market.workspace.LocationModels.Change;
import com.zhy.market.workspace.LocationModels.Coordinates;
import com.zhy.market.workspace.LocationModels.Facility;
import com.zhy.market.workspace.LocationModels.GeocodeReference;
import com.zhy.market.workspace.LocationModels.LocationInput;
import com.zhy.market.workspace.LocationModels.Records;
import com.zhy.market.workspace.LocationModels.SavedLocation;
import com.zhy.market.workspace.LocationModels.Summary;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

@Service
@Profile("local")
public class LocationService {
    private final JdbcTemplate db;
    private final ObjectMapper json;
    private static final String FROM =
            " FROM "
                    + FacilityTables.INVENTORY
                    + " r LEFT JOIN facility_location l ON l.targetTable=r.targetTable AND l.targetId=r.targetId"
                    + " LEFT JOIN facility_geocode g ON g.targetTable=r.targetTable AND g.targetId=r.targetId AND g.sourceAddress=r.address"
                    + " AND g.id=(SELECT MAX(g2.id) FROM facility_geocode g2 WHERE g2.targetTable=r.targetTable AND g2.targetId=r.targetId)";

    public LocationService(JdbcTemplate db, ObjectMapper json) {
        this.db = db;
        this.json = json;
    }

    public Summary summary() {
        return db.queryForObject(
                "SELECT COUNT(*) total,"
                        + "COALESCE(SUM(CASE WHEN l.targetId IS NOT NULL THEN 1 ELSE 0 END),0) located,"
                        + "COALESCE(SUM(CASE WHEN l.targetId IS NULL AND g.decision='address_matched' THEN 1 ELSE 0 END),0) matched,"
                        + "COALESCE(SUM(CASE WHEN l.targetId IS NULL AND g.id IS NOT NULL AND g.decision<>'address_matched' THEN 1 ELSE 0 END),0) review"
                        + FROM,
                (rs, n) -> {
                    long total = rs.getLong("total");
                    long located = rs.getLong("located");
                    long matched = rs.getLong("matched");
                    return new Summary(
                            total,
                            located,
                            matched,
                            rs.getLong("review"),
                            total - located - matched);
                });
    }

    public Records records(String q, String state, int page, int size) {
        if (page < 1
                || page > 100000
                || size < 1
                || size > 500
                || q.length() > 200
                || !Set.of("all", "pending", "located", "matched", "review").contains(state)) {
            throw bad("查询参数无效");
        }
        var args = new ArrayList<Object>();
        String where = " WHERE 1=1";
        if (state.equals("located")) {
            where += " AND l.targetId IS NOT NULL";
        }
        if (state.equals("matched")) {
            where += " AND l.targetId IS NULL AND g.decision='address_matched'";
        }
        if (state.equals("review")) {
            where +=
                    " AND l.targetId IS NULL AND g.id IS NOT NULL AND g.decision<>'address_matched'";
        }
        if (state.equals("pending")) {
            where += " AND l.targetId IS NULL AND (g.id IS NULL OR g.decision<>'address_matched')";
        }
        if (!q.isBlank()) {
            where +=
                    " AND (r.name LIKE ? ESCAPE '!' OR r.identifier LIKE ? ESCAPE '!' OR r.address LIKE ? ESCAPE '!')";
            String term =
                    "%" + q.strip().replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%";
            args.addAll(List.of(term, term, term));
        }
        long total =
                db.queryForObject("SELECT COUNT(*)" + FROM + where, Long.class, args.toArray());
        args.add(size);
        args.add((page - 1) * size);
        var rows =
                db.query(
                        "SELECT r.name,r.identifier,r.address,r.targetTable,r.targetId,l.longitude,l.latitude,l.crs,l.evidence,l.actor,l.updatedAt,l.revision,"
                                + "g.id geocodeId,g.decision,g.reason,g.longitude geocodeLng,g.latitude geocodeLat,g.createdAt geocodeTime"
                                + FROM
                                + where
                                + " ORDER BY r.targetTable,r.targetId LIMIT ? OFFSET ?",
                        (rs, n) -> mapFacility(rs),
                        args.toArray());
        return new Records(rows, total);
    }

    private Facility mapFacility(ResultSet rs) throws SQLException {
        String table = rs.getString("targetTable");
        long id = rs.getLong("targetId");
        Coordinates location =
                rs.getObject("longitude") == null
                        ? null
                        : new Coordinates(
                                rs.getDouble("longitude"),
                                rs.getDouble("latitude"),
                                rs.getString("crs"),
                                rs.getString("evidence"),
                                rs.getString("actor"),
                                rs.getString("updatedAt"),
                                rs.getLong("revision"));
        GeocodeReference geocode =
                rs.getObject("geocodeId") == null
                        ? null
                        : new GeocodeReference(
                                rs.getLong("geocodeId"),
                                rs.getString("decision"),
                                rs.getString("reason"),
                                rs.getObject("geocodeLng", Double.class),
                                rs.getObject("geocodeLat", Double.class),
                                rs.getString("geocodeTime"));
        return new Facility(
                table + "/" + id,
                rs.getString("name"),
                rs.getString("identifier"),
                rs.getString("address"),
                table,
                id,
                location,
                geocode);
    }

    public List<Change> history(String table, long id) {
        FacilityTables.validate(table);
        return db.query(
                "SELECT actor,beforeJson,afterJson,createdAt FROM workspace_change WHERE module=? AND recordId=? ORDER BY id DESC LIMIT 100",
                (rs, n) ->
                        new Change(
                                rs.getString("actor"),
                                json.readTree(rs.getString("beforeJson")),
                                json.readTree(rs.getString("afterJson")),
                                rs.getString("createdAt")),
                "locations:" + table,
                id);
    }

    @Transactional
    public SavedLocation save(String table, long id, LocationInput input, String actor) {
        FacilityTables.validate(table);
        validateInput(input);
        if (db.queryForList("SELECT id FROM " + table + " WHERE id=? FOR UPDATE", id).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "设施记录不存在");
        }
        var current =
                db.queryForList(
                        "SELECT longitude,latitude,crs,evidence,revision FROM facility_location WHERE targetTable=? AND targetId=?",
                        table,
                        id);
        long revision =
                current.isEmpty() ? 0 : ((Number) current.get(0).get("revision")).longValue();
        if (revision != input.revision()) {
            throw conflict("坐标记录已变化，请重新打开后核对");
        }
        String now = Instant.now().toString();
        var after =
                new SavedLocation(
                        input.longitude(),
                        input.latitude(),
                        input.crs(),
                        input.evidence(),
                        revision + 1);
        db.update(
                "MERGE INTO facility_location(targetTable,targetId,longitude,latitude,crs,evidence,actor,updatedAt,revision) KEY(targetTable,targetId) VALUES(?,?,?,?,?,?,?,?,?)",
                table,
                id,
                input.longitude(),
                input.latitude(),
                input.crs(),
                input.evidence(),
                actor,
                now,
                revision + 1);
        db.update(
                "INSERT INTO workspace_change(module,recordId,action,actor,beforeJson,afterJson,createdAt) VALUES(?,?,'update',?,?,?,?)",
                "locations:" + table,
                id,
                actor,
                json.writeValueAsString(current.isEmpty() ? Map.of() : current.get(0)),
                json.writeValueAsString(after),
                now);
        return after;
    }

    private void validateInput(LocationInput input) {
        if (input.longitude() == null
                || input.latitude() == null
                || !Double.isFinite(input.longitude())
                || !Double.isFinite(input.latitude())
                || Math.abs(input.longitude()) > 180
                || Math.abs(input.latitude()) > 90
                || !Set.of("WGS84", "GCJ02").contains(Objects.toString(input.crs(), ""))
                || input.evidence() == null
                || input.evidence().isBlank()
                || input.evidence().length() > 2000
                || input.revision() < 0) {
            throw bad("请核对经纬度范围、坐标系及定位依据");
        }
    }
}
