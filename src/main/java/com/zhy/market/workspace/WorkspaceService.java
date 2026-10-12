package com.zhy.market.workspace;

import static com.zhy.market.workspace.WorkspaceErrors.bad;
import static com.zhy.market.workspace.WorkspaceErrors.conflict;

import com.zhy.market.ledger.LedgerImport;
import com.zhy.market.ledger.LedgerImport.SourceRow;
import com.zhy.market.workspace.WorkspaceResponses.Change;
import com.zhy.market.workspace.WorkspaceResponses.ImportResult;
import com.zhy.market.workspace.WorkspaceResponses.RecordDetail;
import com.zhy.market.workspace.WorkspaceResponses.RecordPage;
import com.zhy.market.workspace.WorkspaceResponses.RecordView;
import com.zhy.market.workspace.WorkspaceResponses.Source;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

@Service
@Profile("local")
public class WorkspaceService {
    public record SaveRequest(String requestId, String revision, Map<String, String> values) {}

    public record ArchiveRequest(String revision, boolean archived) {}

    public record ImportRow(SourceRow raw, Map<String, String> values) {}

    public record ImportRequest(String sha256, String fileName, List<ImportRow> rows) {}

    private final JdbcTemplate db;
    private final ObjectMapper json;
    private final ModuleCatalog catalog;

    public WorkspaceService(JdbcTemplate db, ObjectMapper json, ModuleCatalog catalog) {
        this.db = db;
        this.json = json;
        this.catalog = catalog;
    }

    private static String quoted(String value) {
        return "`" + value + "`";
    }

    private static void requireManagedModule(ModuleCatalog.Module module) {
        if (!module.managed()) {
            throw bad("此模块的修改请使用设施业务页面");
        }
    }

    private Map<String, String> validateValues(
            ModuleCatalog.Module module, Map<String, String> input) {
        if (input == null) {
            throw bad("缺少记录内容");
        }
        Set<String> allowed = new HashSet<>();
        var values = new LinkedHashMap<String, String>();
        for (var field : module.fields()) {
            allowed.add(field.key());
            String value = Objects.requireNonNullElse(input.get(field.key()), "");
            if (value.length() > 20000) {
                throw bad(field.label() + "过长");
            }
            if (field.required() && value.isBlank()) {
                throw bad("请填写" + field.label());
            }
            values.put(field.key(), value);
        }
        if (!allowed.containsAll(input.keySet())) {
            throw bad("包含未定义字段");
        }
        return values;
    }

    private String hash(Object value) {
        try {
            return HexFormat.of()
                    .formatHex(
                            MessageDigest.getInstance("SHA-256")
                                    .digest(
                                            json.writeValueAsString(value)
                                                    .getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private Map<String, String> mapSource(ModuleCatalog.Module module, Map<String, String> raw) {
        var values = new LinkedHashMap<String, String>();
        for (var field : module.fields()) {
            String matched = null;
            for (var cell : raw.entrySet()) {
                String header = LedgerImport.header(cell.getKey());
                if (header.equals(LedgerImport.header(field.label()))
                        || header.equals(field.key())) {
                    if (matched != null) throw bad("重复映射字段：" + field.label());
                    matched = cell.getValue();
                }
            }
            values.put(field.key(), Objects.requireNonNullElse(matched, ""));
        }
        return validateValues(module, values);
    }

    private RecordView mapRecord(ModuleCatalog.Module module, ResultSet rs) throws SQLException {
        var values = new LinkedHashMap<String, String>();
        for (var field : module.fields()) {
            values.put(field.key(), Objects.requireNonNullElse(rs.getString(field.key()), ""));
        }
        long id = rs.getLong(module.idColumn());
        boolean archived = rs.getBoolean("_archived");
        return new RecordView(id, values, archived, hash(List.of(id, values, archived)));
    }

    private String from(ModuleCatalog.Module module) {
        return " FROM "
                + quoted(module.table())
                + " t LEFT JOIN workspace_state s ON s.module=? AND s.recordId=t."
                + quoted(module.idColumn());
    }

    private String select() {
        return "SELECT t.*,COALESCE(s.archived,FALSE) _archived";
    }

    private RecordView load(ModuleCatalog.Module module, long id, boolean lock) {
        if (lock) {
            var found =
                    db.queryForList(
                            "SELECT "
                                    + quoted(module.idColumn())
                                    + " FROM "
                                    + quoted(module.table())
                                    + " WHERE "
                                    + quoted(module.idColumn())
                                    + "=? FOR UPDATE",
                            id);
            if (found.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "记录不存在");
        }
        var result =
                db.query(
                        select() + from(module) + " WHERE t." + quoted(module.idColumn()) + "=?",
                        (rs, n) -> mapRecord(module, rs),
                        module.key(),
                        id);
        if (result.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "记录不存在");
        return result.get(0);
    }

    public RecordPage list(String key, String q, String field, String state, int page, int size) {
        var module = catalog.get(key);
        if (page < 1
                || page > 100000
                || size < 1
                || size > 500
                || q.length() > 200
                || !Set.of("active", "archived", "all").contains(state)) throw bad("查询参数无效");
        var fields = module.fields().stream().map(ModuleCatalog.Field::key).toList();
        if (!field.isEmpty() && !fields.contains(field)) throw bad("查询字段无效");
        var args = new ArrayList<Object>();
        args.add(key);
        String where = " WHERE 1=1";
        if (!state.equals("all")) {
            where += " AND COALESCE(s.archived,FALSE)=?";
            args.add(state.equals("archived"));
        }
        if (!q.isBlank()) {
            var searchFields = field.isEmpty() ? fields : List.of(field);
            where +=
                    " AND ("
                            + String.join(
                                    " OR ",
                                    searchFields.stream()
                                            .map(f -> "t." + quoted(f) + " LIKE ? ESCAPE '!'")
                                            .toList())
                            + ")";
            String term =
                    "%" + q.strip().replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%";
            for (int i = 0; i < searchFields.size(); i++) args.add(term);
        }
        long total =
                db.queryForObject(
                        "SELECT COUNT(*)" + from(module) + where, Long.class, args.toArray());
        args.add(size);
        args.add((page - 1) * size);
        var rows =
                db.query(
                        select()
                                + from(module)
                                + where
                                + " ORDER BY t."
                                + quoted(module.idColumn())
                                + " DESC LIMIT ? OFFSET ?",
                        (rs, n) -> mapRecord(module, rs),
                        args.toArray());
        return new RecordPage(rows, total, page, size);
    }

    public RecordDetail detail(String key, long id) {
        var module = catalog.get(key);
        var result = load(module, id, false);
        var history =
                db.query(
                        "SELECT action,actor,beforeJson,afterJson,createdAt FROM workspace_change WHERE module=? AND recordId=? ORDER BY id DESC LIMIT 100",
                        (rs, n) ->
                                new Change(
                                        rs.getString("action"),
                                        rs.getString("actor"),
                                        json.readTree(rs.getString("beforeJson")),
                                        json.readTree(rs.getString("afterJson")),
                                        rs.getString("createdAt")),
                        key,
                        id);
        var sources =
                db.query(
                        "SELECT sourceSha,fileName,sheetName,rowNumber,rawJson FROM workspace_import WHERE module=? AND recordId=?",
                        (rs, n) ->
                                new Source(
                                        rs.getString("sourceSha"),
                                        rs.getString("fileName"),
                                        rs.getString("sheetName"),
                                        rs.getInt("rowNumber"),
                                        json.readTree(rs.getString("rawJson"))),
                        key,
                        id);
        return new RecordDetail(
                result.id(),
                result.values(),
                result.archived(),
                result.revision(),
                history,
                sources);
    }

    private long insert(ModuleCatalog.Module module, Map<String, String> values) {
        var keys = new ArrayList<>(values.keySet());
        String sql =
                "INSERT INTO "
                        + quoted(module.table())
                        + "("
                        + String.join(",", keys.stream().map(WorkspaceService::quoted).toList())
                        + ") VALUES("
                        + String.join(",", Collections.nCopies(keys.size(), "?"))
                        + ")";
        var generated = new GeneratedKeyHolder();
        db.update(
                connection -> {
                    var statement =
                            connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                    for (int i = 0; i < keys.size(); i++)
                        statement.setString(i + 1, values.get(keys.get(i)));
                    return statement;
                },
                generated);
        return Objects.requireNonNull(generated.getKey()).longValue();
    }

    private void audit(
            String key, long id, String action, String actor, Object before, Object after) {
        db.update(
                "INSERT INTO workspace_change(module,recordId,action,actor,beforeJson,afterJson,createdAt) VALUES(?,?,?,?,?,?,?)",
                key,
                id,
                action,
                actor,
                json.writeValueAsString(before),
                json.writeValueAsString(after),
                Instant.now().toString());
    }

    @Transactional
    public RecordView create(String key, SaveRequest request, String actor) {
        var module = catalog.get(key);
        var values = validateValues(module, request.values());
        if (request.requestId() == null || !request.requestId().matches("[a-zA-Z0-9-]{16,80}"))
            throw bad("缺少有效提交标识");
        String fingerprint = hash(values);
        var previous =
                db.queryForList(
                        "SELECT inputHash,recordId FROM workspace_request WHERE module=? AND requestId=?",
                        key,
                        request.requestId());
        if (!previous.isEmpty()) {
            if (!previous.get(0).get("inputHash").equals(fingerprint))
                throw conflict("同一提交标识的内容发生变化，请重新提交");
            return load(module, ((Number) previous.get(0).get("recordId")).longValue(), false);
        }
        long id = insert(module, values);
        db.update(
                "INSERT INTO workspace_request(module,requestId,inputHash,recordId) VALUES(?,?,?,?)",
                key,
                request.requestId(),
                fingerprint,
                id);
        var saved = load(module, id, false);
        audit(key, id, "create", actor, Map.of(), saved);
        return saved;
    }

    private void checkRevision(RecordView previous, String revision) {
        if (!Objects.equals(previous.revision(), revision)) throw conflict("记录已变化，请重新打开后核对");
    }

    @Transactional
    public RecordView update(String key, long id, SaveRequest request, String actor) {
        var module = catalog.get(key);
        requireManagedModule(module);
        var values = validateValues(module, request.values());
        var before = load(module, id, true);
        checkRevision(before, request.revision());
        if (before.archived()) throw conflict("请先恢复归档记录");
        var args = new ArrayList<Object>(values.values());
        args.add(id);
        db.update(
                "UPDATE "
                        + quoted(module.table())
                        + " SET "
                        + String.join(
                                ",", values.keySet().stream().map(k -> quoted(k) + "=?").toList())
                        + " WHERE "
                        + quoted(module.idColumn())
                        + "=?",
                args.toArray());
        var saved = load(module, id, false);
        audit(key, id, "update", actor, before, saved);
        return saved;
    }

    @Transactional
    public RecordView archive(String key, long id, ArchiveRequest request, String actor) {
        var module = catalog.get(key);
        requireManagedModule(module);
        var before = load(module, id, true);
        checkRevision(before, request.revision());
        db.update(
                "MERGE INTO workspace_state(module,recordId,archived) KEY(module,recordId) VALUES(?,?,?)",
                key,
                id,
                request.archived());
        var saved = load(module, id, false);
        audit(key, id, request.archived() ? "archive" : "restore", actor, before, saved);
        return saved;
    }

    @Transactional
    public ImportResult importRows(String key, ImportRequest request, String actor) {
        var module = catalog.get(key);
        requireManagedModule(module);
        validateImportRequest(request);
        int inserted = 0;
        int skipped = 0;
        var batchPositions = new HashSet<String>();
        for (var input : request.rows()) {
            var raw = input.raw();
            validateSourcePosition(raw);
            if (!batchPositions.add(raw.sheet() + "\n" + raw.rowNumber())) {
                throw bad("批次包含重复来源位置");
            }
            var values = validateValues(module, input.values());
            if (!values.equals(mapSource(module, raw.values()))) {
                throw bad("映射内容与原始字段不一致");
            }
            String rawJson = json.writeValueAsString(raw);
            String mappedJson = json.writeValueAsString(values);
            if (rawJson.length() > 500000) {
                throw bad("原始记录过长");
            }
            if (alreadyImported(key, request.sha256(), raw, rawJson, mappedJson)) {
                skipped++;
                continue;
            }
            long id = insert(module, values);
            db.update(
                    "INSERT INTO workspace_import(module,sourceSha,fileName,sheetName,rowNumber,recordId,rawJson,mappedJson,importedAt) VALUES(?,?,?,?,?,?,?,?,?)",
                    key,
                    request.sha256(),
                    request.fileName(),
                    raw.sheet(),
                    raw.rowNumber(),
                    id,
                    rawJson,
                    mappedJson,
                    Instant.now().toString());
            audit(key, id, "import", actor, Map.of(), load(module, id, false));
            inserted++;
        }
        return new ImportResult(inserted, skipped);
    }

    private void validateImportRequest(ImportRequest request) {
        if (request.sha256() == null
                || !request.sha256().matches("[a-f0-9]{64}")
                || request.fileName() == null
                || request.fileName().isBlank()
                || request.fileName().length() > 500
                || request.rows() == null
                || request.rows().isEmpty()
                || request.rows().size() > 100) {
            throw bad("导入批次无效，每批最多100条");
        }
    }

    private void validateSourcePosition(SourceRow raw) {
        if (raw == null
                || raw.sheet() == null
                || raw.sheet().isBlank()
                || raw.sheet().length() > 500
                || raw.rowNumber() < 1
                || raw.rowNumber() > 100000
                || raw.cells() == null
                || raw.values() == null) {
            throw bad("缺少原始记录位置");
        }
    }

    private boolean alreadyImported(
            String key, String sha256, SourceRow raw, String rawJson, String mappedJson) {
        var previous =
                db.queryForList(
                        "SELECT rawJson,mappedJson FROM workspace_import WHERE module=? AND sourceSha=? AND sheetName=? AND rowNumber=?",
                        key,
                        sha256,
                        raw.sheet(),
                        raw.rowNumber());
        if (previous.isEmpty()) {
            return false;
        }
        if (!json.readTree(previous.get(0).get("rawJson").toString()).equals(json.readTree(rawJson))
                || !json.readTree(previous.get(0).get("mappedJson").toString())
                        .equals(json.readTree(mappedJson))) {
            throw conflict("同一来源位置内容冲突，未覆盖已有数据");
        }
        return true;
    }
}
