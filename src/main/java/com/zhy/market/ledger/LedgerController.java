package com.zhy.market.ledger;

import com.zhy.market.security.Sessions;
import jakarta.servlet.http.HttpServletRequest;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/ledger")
@Profile("local")
public class LedgerController {
    private final LedgerImport importer;
    private final JdbcTemplate db;
    private final ObjectMapper json;
    private static final String FROM =
            " FROM ledger_record r JOIN ledger_source s ON s.sha256=r.sourceSha ";
    private static final String SELECT =
            "SELECT r.id,r.kind,r.sourceSha,r.sheetName,r.rowNumber,r.identifier,r.accountNumber,r.name,r.address,r.street,r.facilityType,r.collectedAt,r.linkStatus,r.targetTable,r.targetId,s.fileName";
    private static final String DETAIL_SELECT = SELECT + ",r.rawJson,r.mappedJson";

    private static String addressMatch(String left, String right) {
        return "("
                + left
                + ".addressKey<>'' AND ("
                + left
                + ".addressKey="
                + right
                + ".addressKey OR "
                + left
                + ".addressKey="
                + right
                + ".positionKey)) OR ("
                + left
                + ".positionKey<>'' AND ("
                + left
                + ".positionKey="
                + right
                + ".addressKey OR "
                + left
                + ".positionKey="
                + right
                + ".positionKey))";
    }

    public LedgerController(LedgerImport importer, JdbcTemplate db, ObjectMapper json) {
        this.importer = importer;
        this.db = db;
        this.json = json;
    }

    @PostMapping("/import")
    public Object importRows(@RequestBody LedgerImport.Batch input, HttpServletRequest request) {
        if (!Sessions.user(request).manages())
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        return importer.importBatch(input);
    }

    @GetMapping("/summary")
    public Object summary() {
        var sources =
                db.query(
                        "SELECT s.*,COUNT(r.id) importedRows FROM ledger_source s LEFT JOIN ledger_record r ON r.sourceSha=s.sha256 GROUP BY s.sha256,s.kind,s.fileName,s.expectedRows,s.importedAt ORDER BY s.importedAt",
                        (rs, n) ->
                                Map.of(
                                        "sha256",
                                        rs.getString("sha256"),
                                        "kind",
                                        rs.getString("kind"),
                                        "fileName",
                                        rs.getString("fileName"),
                                        "expectedRows",
                                        rs.getInt("expectedRows"),
                                        "importedRows",
                                        rs.getInt("importedRows"),
                                        "importedAt",
                                        rs.getString("importedAt")));
        var kinds =
                db.query(
                        "SELECT kind, COUNT(*) total, SUM(CASE WHEN linkStatus='unlinked' THEN 1 ELSE 0 END) unlinked, SUM(CASE WHEN linkStatus='classification_pending' THEN 1 ELSE 0 END) pending FROM ledger_record GROUP BY kind ORDER BY kind",
                        (rs, n) ->
                                Map.of(
                                        "kind",
                                        rs.getString("kind"),
                                        "total",
                                        rs.getInt("total"),
                                        "unlinked",
                                        rs.getInt("unlinked"),
                                        "pending",
                                        rs.getInt("pending")));
        long total =
                kinds.stream().mapToLong(kind -> ((Number) kind.get("total")).longValue()).sum();
        return Map.of("sources", sources, "kinds", kinds, "total", total);
    }

    @GetMapping("/records")
    public Object records(
            @RequestParam(defaultValue = "") String kind,
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "") String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "30") int size) {
        if ((!kind.isEmpty() && !LedgerImport.KINDS.contains(kind))
                || page < 1
                || page > 100000
                || size < 1
                || size > 100
                || q.length() > 200
                || !Set.of("", "unlinked", "classification_pending", "source_record")
                        .contains(status)) throw LedgerImport.bad("查询参数无效");
        var args = new ArrayList<Object>();
        String where = " WHERE 1=1";
        if (!kind.isEmpty()) {
            where += " AND r.kind=?";
            args.add(kind);
        }
        if (!status.isEmpty()) {
            where += " AND r.linkStatus=?";
            args.add(status);
        }
        if (!q.isBlank()) {
            where +=
                    " AND (r.identifier LIKE ? ESCAPE '!' OR r.accountNumber LIKE ? ESCAPE '!' OR r.name LIKE ? ESCAPE '!' OR r.address LIKE ? ESCAPE '!')";
            String escaped =
                    "%" + q.trim().replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%";
            for (int i = 0; i < 4; i++) args.add(escaped);
        }
        long total =
                db.queryForObject(
                        "SELECT COUNT(*) FROM ledger_record r" + where, Long.class, args.toArray());
        args.add(size);
        args.add((page - 1) * size);
        var rows =
                db.query(
                        SELECT + FROM + where + " ORDER BY r.id LIMIT ? OFFSET ?",
                        (rs, n) -> row(rs, false),
                        args.toArray());
        return Map.of("rows", rows, "total", total, "page", page, "size", size);
    }

    @GetMapping("/records/{id}")
    public Object detail(@PathVariable long id) {
        var rows = db.query(DETAIL_SELECT + FROM + " WHERE r.id=?", (rs, n) -> row(rs, true), id);
        if (rows.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        var result = rows.get(0);
        result.put("candidates", candidates(id, (String) result.get("kind")));
        return result;
    }

    @GetMapping("/sources/{sha}/rows")
    public Object sourceRows(
            @PathVariable String sha,
            @RequestParam(defaultValue = "0") long after,
            @RequestParam(defaultValue = "250") int size,
            HttpServletRequest request) {
        if (!Sessions.user(request).manages())
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        if (!sha.matches("[a-f0-9]{64}") || after < 0 || size < 1 || size > 250)
            throw LedgerImport.bad("查询参数无效");
        return db.query(
                DETAIL_SELECT + FROM + " WHERE r.sourceSha=? AND r.id>? ORDER BY r.id LIMIT ?",
                (rs, n) -> row(rs, true),
                sha,
                after,
                size);
    }

    private List<Map<String, Object>> candidates(long id, String kind) {
        List<String> targetKinds =
                switch (kind) {
                    case "maintenance" -> List.of("chambers");
                    case "chambers" -> List.of("maintenance");
                    case "recheck" -> List.of("meters", "rooms");
                    case "rooms" -> List.of("meters", "recheck");
                    case "meters" -> List.of("recheck", "rooms");
                    default -> List.of();
                };
        boolean street = kind.equals("maintenance") || kind.equals("chambers");
        List<Map<String, Object>> results = new ArrayList<>();
        for (String targetKind : targetKinds) {
            boolean site = kind.equals("rooms") || targetKind.equals("rooms");
            String condition =
                    site
                            ? "(a.nameKey<>'' AND a.nameKey=r.nameKey) OR ("
                                    + addressMatch("a", "r")
                                    + ")"
                            : street
                                    ? "a.identifierKey<>'' AND a.identifierKey=r.identifierKey"
                                    : "(a.accountKey<>'' AND a.accountKey=r.accountKey) OR (a.nameKey<>'' AND a.nameKey=r.nameKey) OR (a.identifierKey<>'' AND a.identifierKey=r.identifierKey) OR ("
                                            + addressMatch("a", "r")
                                            + ")";
            results.addAll(
                    db.query(
                            SELECT
                                    + ",r.identifierKey,r.accountKey,r.nameKey,r.streetKey,r.addressKey,r.positionKey,a.identifierKey leftIdentifier,a.accountKey leftAccount,a.nameKey leftName,a.streetKey leftStreet,a.addressKey leftAddress,a.positionKey leftPosition"
                                    + FROM
                                    + " JOIN ledger_record a ON a.id=? WHERE r.kind=? AND ("
                                    + condition
                                    + ") ORDER BY r.id LIMIT 100",
                            (rs, n) -> {
                                var result = row(rs, false);
                                List<String> reasons = new ArrayList<>();
                                if (!rs.getString("accountKey").isEmpty()
                                        && rs.getString("accountKey")
                                                .equals(rs.getString("leftAccount")))
                                    reasons.add("户号一致");
                                if (!rs.getString("nameKey").isEmpty()
                                        && rs.getString("nameKey").equals(rs.getString("leftName")))
                                    reasons.add(site ? "名称一致，可能同小区" : "名称一致");
                                if (!rs.getString("identifierKey").isEmpty()
                                        && rs.getString("identifierKey")
                                                .equals(rs.getString("leftIdentifier")))
                                    reasons.add("编号一致");
                                if (street)
                                    reasons.add(
                                            !rs.getString("streetKey").isEmpty()
                                                            && rs.getString("streetKey")
                                                                    .equals(
                                                                            rs.getString(
                                                                                    "leftStreet"))
                                                    ? "街道一致"
                                                    : "街道不同或缺失");
                                if (!street) {
                                    var addresses =
                                            new HashSet<>(
                                                    List.of(
                                                            rs.getString("addressKey"),
                                                            rs.getString("positionKey")));
                                    addresses.remove("");
                                    if (addresses.contains(rs.getString("leftAddress"))
                                            || addresses.contains(rs.getString("leftPosition")))
                                        reasons.add("地址或位置原文一致");
                                    if (!site
                                            && !rs.getString("accountKey").isEmpty()
                                            && !rs.getString("leftAccount").isEmpty()
                                            && !rs.getString("accountKey")
                                                    .equals(rs.getString("leftAccount")))
                                        reasons.add("户号不同");
                                }
                                result.put("reasons", reasons);
                                result.put("confirmed", false);
                                return result;
                            },
                            id,
                            targetKind));
        }
        return results;
    }

    @GetMapping("/relations")
    public Object relations() {
        var records =
                db.query(
                        "SELECT id,kind,identifierKey,accountKey,nameKey,streetKey,addressKey,positionKey FROM ledger_record",
                        (rs, n) ->
                                new JoinRow(
                                        rs.getLong("id"),
                                        rs.getString("kind"),
                                        rs.getString("identifierKey"),
                                        rs.getString("accountKey"),
                                        rs.getString("nameKey"),
                                        rs.getString("streetKey"),
                                        rs.getString("addressKey"),
                                        rs.getString("positionKey")));
        Map<String, List<JoinRow>> byKind = new HashMap<>();
        for (var record : records)
            byKind.computeIfAbsent(record.kind(), k -> new ArrayList<>()).add(record);
        List<Map<String, Object>> result = new ArrayList<>();
        for (var rule :
                List.of(
                        new JoinRule(
                                "maintenance",
                                "chambers",
                                "街道和编号",
                                r -> List.of(List.of(r.identifier(), r.street()))),
                        new JoinRule(
                                "maintenance",
                                "chambers",
                                "仅编号",
                                r -> List.of(List.of(r.identifier()))),
                        new JoinRule("recheck", "meters", "户号", r -> List.of(List.of(r.account()))),
                        new JoinRule("recheck", "meters", "户名", r -> List.of(List.of(r.name()))),
                        new JoinRule(
                                "recheck", "meters", "编号", r -> List.of(List.of(r.identifier()))),
                        new JoinRule("meters", "rooms", "户名与小区名称", r -> List.of(List.of(r.name()))),
                        new JoinRule(
                                "recheck",
                                "meters",
                                "地址或位置原文",
                                r -> List.of(List.of(r.address()), List.of(r.position()))),
                        new JoinRule(
                                "recheck",
                                "meters",
                                "户名及地址/位置",
                                r ->
                                        List.of(
                                                List.of(r.name(), r.address()),
                                                List.of(r.name(), r.position()))),
                        new JoinRule(
                                "recheck", "rooms", "户名与小区名称", r -> List.of(List.of(r.name()))),
                        new JoinRule(
                                "recheck",
                                "rooms",
                                "地址或位置原文",
                                r -> List.of(List.of(r.address()), List.of(r.position()))))) {
            // Multiple matching fields can point to the same row; count that row once.
            Map<List<String>, Set<Long>> index = new HashMap<>();
            for (var row : byKind.getOrDefault(rule.to(), List.of()))
                for (var key : rule.keys().apply(row)) {
                    if (key.stream().anyMatch(String::isEmpty)) continue;
                    index.computeIfAbsent(key, k -> new HashSet<>()).add(row.id());
                }
            var source = byKind.getOrDefault(rule.from(), List.of());
            int unmatched = 0, unique = 0, ambiguous = 0;
            for (var row : source) {
                Set<Long> matches = new HashSet<>();
                for (var key : rule.keys().apply(row))
                    matches.addAll(index.getOrDefault(key, Set.of()));
                int n = matches.size();
                if (n == 0) unmatched++;
                else if (n == 1) unique++;
                else ambiguous++;
            }
            result.add(
                    Map.of(
                            "from",
                            rule.from(),
                            "to",
                            rule.to(),
                            "rule",
                            rule.label(),
                            "total",
                            source.size(),
                            "unmatched",
                            unmatched,
                            "uniqueCandidates",
                            unique,
                            "ambiguous",
                            ambiguous,
                            "confirmed",
                            0));
        }
        return result;
    }

    private record JoinRow(
            long id,
            String kind,
            String identifier,
            String account,
            String name,
            String street,
            String address,
            String position) {}

    private record JoinRule(
            String from, String to, String label, Function<JoinRow, List<List<String>>> keys) {}

    private Map<String, Object> row(ResultSet rs, boolean detail) throws SQLException {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", rs.getLong("id"));
        for (String field :
                List.of(
                        "kind",
                        "sourceSha",
                        "sheetName",
                        "fileName",
                        "identifier",
                        "accountNumber",
                        "name",
                        "address",
                        "street",
                        "facilityType",
                        "collectedAt",
                        "linkStatus",
                        "targetTable")) result.put(field, rs.getString(field));
        result.put("rowNumber", rs.getInt("rowNumber"));
        result.put("targetId", rs.getObject("targetId"));
        if (detail) {
            result.put("raw", json.readTree(rs.getString("rawJson")));
            result.put("mapped", json.readTree(rs.getString("mappedJson")));
        }
        return result;
    }
}
