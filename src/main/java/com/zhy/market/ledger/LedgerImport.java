package com.zhy.market.ledger;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.text.Normalizer;
import java.time.Instant;
import java.util.*;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
@Profile("local")
public class LedgerImport {
    public record SourceRow(
            String sheet, int rowNumber, Map<String, String> values, Map<String, Object> cells) {}

    public record Batch(
            String sha256, String fileName, String kind, int expectedRows, List<SourceRow> rows) {}

    public static final Set<String> KINDS =
            Set.of("meters", "rooms", "chambers", "recheck", "maintenance");
    private static final Set<String> VALVE_TYPES =
            Set.of("用户阀门", "主线阀门", "排气", "泄水", "减压阀", "调流调压阀", "测压阀门");
    private final JdbcTemplate db;
    private final ObjectMapper json;
    private final JsonNode mapping;

    public LedgerImport(JdbcTemplate db, ObjectMapper json) throws IOException {
        this.db = db;
        this.json = json;
        try (var stream = new ClassPathResource("ledger-mapping.json").getInputStream()) {
            mapping = json.readTree(stream);
        }
    }

    public static String header(String value) {
        return Normalizer.normalize(value, Normalizer.Form.NFKC)
                .replaceAll("\\((必填|自动)\\)", "")
                .replaceAll("[\\s:]+", "");
    }

    public static String key(String value) {
        return Normalizer.normalize(Objects.toString(value, ""), Normalizer.Form.NFKC)
                .toUpperCase(Locale.ROOT)
                .replaceAll("[—–－−﹣]", "-")
                .replaceAll("\\s+", "");
    }

    public Map<String, String> map(String kind, Map<String, String> values) {
        Map<String, String> result = new LinkedHashMap<>();
        var fields = mapping.path(kind);
        for (var cell : values.entrySet()) {
            String column = fields.path(header(cell.getKey())).asText("");
            if (!column.isEmpty()) {
                String value = cell.getValue();
                if (result.containsKey(column) && !Objects.equals(result.get(column), value)) {
                    throw bad("重复表头映射到同一字段，且内容不一致：" + cell.getKey());
                }
                result.put(column, value);
            }
        }
        return result;
    }

    private void validateKind(String kind, Map<String, String> values) {
        Set<String> headers = new HashSet<>();
        values.keySet().forEach(h -> headers.add(header(h)));
        Set<String> required =
                switch (kind) {
                    case "rooms" -> Set.of("小区名称", "总户数", "水表位置");
                    case "meters" -> Set.of("户号", "水表厂家", "井深");
                    case "chambers" -> Set.of("井室类型", "街道名称", "井深(米)");
                    case "recheck" -> Set.of("户号", "井内其余水表口径及用途", "地址");
                    case "maintenance" -> Set.of("本次维护、检修内容", "检修时间", "检修人员");
                    default -> throw bad("未知台账类型");
                };
        if (!headers.containsAll(required)
                || (!kind.equals("maintenance") && headers.contains("本次维护、检修内容"))
                || (!kind.equals("recheck") && headers.contains("井内其余水表口径及用途"))) {
            throw bad("文件表头与所选台账类型不符");
        }
    }

    public static String target(String kind, Map<String, String> values) {
        return switch (kind) {
            case "meters" -> "watermeterwell";
            case "rooms" -> "watermeterroom";
            case "chambers" -> {
                String type = Objects.toString(values.get("wellChamberType"), "").trim();
                yield type.equals("消火栓")
                        ? "firehydrant"
                        : VALVE_TYPES.contains(type) ? "valuewell" : null;
            }
            default -> null;
        };
    }

    @Transactional
    public synchronized Map<String, Object> importBatch(Batch batch) {
        if (batch == null
                || batch.sha256() == null
                || !batch.sha256().matches("[a-f0-9]{64}")
                || batch.fileName() == null
                || batch.fileName().isBlank()
                || batch.fileName().length() > 255
                || !KINDS.contains(Objects.toString(batch.kind(), ""))
                || batch.expectedRows() < 1
                || batch.expectedRows() > 100000
                || batch.rows() == null
                || batch.rows().isEmpty()
                || batch.rows().size() > 250) {
            throw bad("导入参数无效；每批最多 250 行");
        }
        var existingSource =
                db.queryForList(
                        "SELECT kind, expectedRows FROM ledger_source WHERE sha256=?",
                        batch.sha256());
        if (existingSource.isEmpty()) {
            db.update(
                    "INSERT INTO ledger_source(sha256,kind,fileName,expectedRows,importedAt) VALUES(?,?,?,?,?)",
                    batch.sha256(),
                    batch.kind(),
                    batch.fileName(),
                    batch.expectedRows(),
                    Instant.now().toString());
        } else if (!existingSource.get(0).get("kind").equals(batch.kind())
                || ((Number) existingSource.get(0).get("expectedRows")).intValue()
                        != batch.expectedRows()) {
            throw conflict("相同文件的类型或记录数与已保存信息不一致");
        }
        int inserted = 0;
        int skipped = 0;
        for (SourceRow row : batch.rows()) {
            if (row == null
                    || row.sheet() == null
                    || row.sheet().isBlank()
                    || row.sheet().length() > 255
                    || row.rowNumber() < 2
                    || row.rowNumber() > 1048576
                    || row.values() == null
                    || row.values().isEmpty()
                    || row.values().size() > 100
                    || row.cells() == null) throw bad("来源行信息无效");
            for (var cell : row.values().entrySet()) {
                if (cell.getKey() == null
                        || cell.getKey().length() > 255
                        || cell.getValue() == null
                        || cell.getValue().length() > 100000) {
                    throw bad("单元格长度或内容无效");
                }
            }
            validateKind(batch.kind(), row.values());
            String raw = json.writeValueAsString(row);
            if (raw.length() > 1000000) throw bad("单行数据过大");
            var existing =
                    db.queryForList(
                            "SELECT rawJson FROM ledger_record WHERE sourceSha=? AND sheetName=? AND rowNumber=?",
                            batch.sha256(),
                            row.sheet(),
                            row.rowNumber());
            if (!existing.isEmpty()) {
                if (!json.readTree((String) existing.get(0).get("rawJson"))
                        .equals(json.readTree(raw))) {
                    throw conflict("同一来源行内容变化，未覆盖已保存记录");
                }
                skipped++;
                continue;
            }
            Map<String, String> fields = map(batch.kind(), row.values());
            String target = target(batch.kind(), fields);
            Integer targetId = target == null ? null : insertFacility(target, fields);
            String identifier = fields.getOrDefault("accountIdentifier", "");
            String account = fields.getOrDefault("accountNumber", "");
            String name = first(fields, "accountName", "communityName", "streetName");
            String street = fields.getOrDefault("streetName", "");
            db.update(
                    "INSERT INTO ledger_record(sourceSha,kind,sheetName,rowNumber,rawJson,mappedJson,targetTable,targetId,linkStatus,identifier,accountNumber,name,address,street,facilityType,collectedAt,identifierKey,accountKey,nameKey,streetKey,addressKey,positionKey) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                    batch.sha256(),
                    batch.kind(),
                    row.sheet(),
                    row.rowNumber(),
                    raw,
                    json.writeValueAsString(fields),
                    target,
                    targetId,
                    target == null
                            ? batch.kind().equals("chambers")
                                    ? "classification_pending"
                                    : "unlinked"
                            : "source_record",
                    identifier,
                    account,
                    name,
                    first(
                            fields,
                            "address",
                            "detailedAddress",
                            "coordinates",
                            "positioningCoordinates",
                            "location"),
                    street,
                    fields.getOrDefault("wellChamberType", ""),
                    first(fields, "writtingTime", "fillingTime"),
                    boundedKey(identifier, 512),
                    boundedKey(account, 512),
                    boundedKey(name, 1024),
                    boundedKey(street, 512),
                    boundedKey(first(fields, "address", "detailedAddress"), 2048),
                    boundedKey(
                            first(fields, "coordinates", "positioningCoordinates", "location"),
                            2048));
            inserted++;
        }
        int count =
                db.queryForObject(
                        "SELECT COUNT(*) FROM ledger_record WHERE sourceSha=?",
                        Integer.class,
                        batch.sha256());
        if (count > batch.expectedRows()) throw conflict("实际行数超过文件声明，整批未写入");
        return Map.of(
                "inserted",
                inserted,
                "skipped",
                skipped,
                "importedRows",
                count,
                "expectedRows",
                batch.expectedRows(),
                "complete",
                count == batch.expectedRows());
    }

    public static String boundedKey(String value, int length) {
        String normalized = key(value);
        return normalized.length() <= length ? normalized : "";
    }

    private Integer insertFacility(String table, Map<String, String> fields) {
        var keys = new ArrayList<>(fields.keySet());
        if (keys.isEmpty()) throw bad("没有可用的业务字段");
        String columns = String.join(",", keys.stream().map(k -> "`" + k + "`").toList());
        String placeholders = String.join(",", Collections.nCopies(keys.size(), "?"));
        var holder = new GeneratedKeyHolder();
        db.update(
                connection -> {
                    PreparedStatement ps =
                            connection.prepareStatement(
                                    "INSERT INTO "
                                            + table
                                            + " ("
                                            + columns
                                            + ") VALUES ("
                                            + placeholders
                                            + ")",
                                    Statement.RETURN_GENERATED_KEYS);
                    for (int i = 0; i < keys.size(); i++)
                        ps.setString(i + 1, fields.get(keys.get(i)));
                    return ps;
                },
                holder);
        return Objects.requireNonNull(holder.getKey()).intValue();
    }

    private static String first(Map<String, String> fields, String... names) {
        for (String name : names)
            if (fields.containsKey(name) && !fields.get(name).isBlank()) return fields.get(name);
        return "";
    }

    static ResponseStatusException bad(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    static ResponseStatusException conflict(String message) {
        return new ResponseStatusException(HttpStatus.CONFLICT, message);
    }
}
