package com.zhy.market;

import static org.junit.jupiter.api.Assertions.*;

import com.zhy.market.ledger.LedgerImport;
import com.zhy.market.security.Sessions;
import java.net.URI;
import java.net.http.*;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

@ActiveProfiles("local")
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
            "spring.datasource.url=jdbc:h2:mem:ledger-tests;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
            "spring.datasource.username=sa",
            "spring.datasource.password=",
            "LOCAL_DB_PASSWORD=test-only",
            "LOCAL_ADMIN_PASSWORD=local-test-password",
            "server.ssl.enabled=false"
        })
class LedgerTests {
    @Autowired LedgerImport importer;
    @Autowired JdbcTemplate db;
    @Autowired Sessions sessions;
    @Autowired ObjectMapper json;
    @LocalServerPort int port;

    @BeforeEach
    void clear() {
        db.update("DELETE FROM ledger_record");
        db.update("DELETE FROM ledger_source");
        for (String table : List.of("watermeterwell", "watermeterroom", "valuewell", "firehydrant"))
            db.update("DELETE FROM " + table);
    }

    private Map<String, String> meter(String number) {
        var values = new LinkedHashMap<String, String>();
        values.put("填写人（自动）", "原填报人");
        values.put("所在部门（自动）", "原部门");
        values.put("填写时间（自动）", "2023-05-01 09:30:00");
        values.put("户名（必填）", "用户甲");
        values.put("户号（必填）", number);
        values.put("编号", "");
        values.put("井深（必填）", "0");
        values.put("水表厂家（必填）", "厂商");
        values.put("口径（必填）", "DN50");
        values.put("坐标（必填）", "原始位置文字");
        values.put("井外拍照（必填）", "https://drive.weixin.qq.com/s?k=test");
        values.put("修复时间（已删除）", "旧字段仍保存");
        return values;
    }

    private LedgerImport.Batch batch(String hash, String kind, Map<String, String> values) {
        return new LedgerImport.Batch(
                hash.repeat(64),
                "fixture.xlsx",
                kind,
                1,
                List.of(
                        new LedgerImport.SourceRow(
                                "台账",
                                2,
                                values,
                                Map.of("A2", Map.of("value", "原填报人", "type", "s")))));
    }

    @Test
    void importsAllMappedFieldsAndPreservesExtraFieldsAndTextIdentifiers() {
        var input = batch("a", "meters", meter("001234"));
        assertEquals(1, importer.importBatch(input).get("inserted"));
        var row = db.queryForMap("SELECT * FROM watermeterwell");
        assertEquals("001234", row.get("accountNumber"));
        assertEquals("原部门", row.get("department"));
        assertEquals("原始位置文字", row.get("coordinates"));
        assertEquals("0", row.get("wellDepth"));
        assertEquals("https://drive.weixin.qq.com/s?k=test", row.get("wellOutside"));
        assertEquals("2023-05-01 09:30:00", row.get("writtingTime"));
        var raw =
                json.readTree(db.queryForObject("SELECT rawJson FROM ledger_record", String.class));
        assertEquals("旧字段仍保存", raw.path("values").path("修复时间（已删除）").asText());
        assertEquals("原填报人", raw.path("cells").path("A2").path("value").asText());
        assertEquals(2, raw.path("rowNumber").asInt());
    }

    @Test
    void sameSourceIsIdempotentButDifferentSourceNeverMergesByIdentifier() {
        var input = batch("a", "meters", meter("001234"));
        importer.importBatch(input);
        assertEquals(1, importer.importBatch(input).get("skipped"));
        importer.importBatch(batch("b", "meters", meter("001234")));
        assertEquals(2, db.queryForObject("SELECT COUNT(*) FROM watermeterwell", Integer.class));
        assertEquals(2, db.queryForObject("SELECT COUNT(*) FROM ledger_record", Integer.class));
    }

    @Test
    void changedSourceRowCannotOverwriteExistingRecord() {
        importer.importBatch(batch("a", "meters", meter("001234")));
        assertThrows(
                ResponseStatusException.class,
                () -> importer.importBatch(batch("a", "meters", meter("999999"))));
        assertEquals(
                "001234",
                db.queryForObject("SELECT accountNumber FROM watermeterwell", String.class));
    }

    @Test
    void invalidLaterRowRollsBackEntireBatch() {
        var invalid =
                new LedgerImport.Batch(
                        "c".repeat(64),
                        "fixture.xlsx",
                        "meters",
                        2,
                        List.of(
                                new LedgerImport.SourceRow("台账", 2, meter("001234"), Map.of()),
                                new LedgerImport.SourceRow(
                                        "台账", 3, Map.of("错误表头", "value"), Map.of())));
        assertThrows(ResponseStatusException.class, () -> importer.importBatch(invalid));
        assertEquals(0, db.queryForObject("SELECT COUNT(*) FROM ledger_record", Integer.class));
        assertEquals(0, db.queryForObject("SELECT COUNT(*) FROM watermeterwell", Integer.class));
        assertEquals(0, db.queryForObject("SELECT COUNT(*) FROM ledger_source", Integer.class));
    }

    @Test
    void historicalEventsRemainUnlinkedAndMixedChambersUnclassified() {
        var maintenance =
                Map.of(
                        "本次维护、检修内容（必填）",
                        "维修",
                        "检修时间（必填）",
                        "2023-01-01",
                        "检修人员（必填）",
                        "原检修人",
                        "编号",
                        "1");
        importer.importBatch(batch("a", "maintenance", maintenance));
        var recheck =
                Map.of("户号（必填）", "001234", "井内其余水表口径及用途（必填）", "原用途", "地址（必填）", "地址", "楼栋数量", "7");
        importer.importBatch(batch("b", "recheck", recheck));
        var chamber =
                new LinkedHashMap<>(
                        Map.of("井室类型（必填）", "主线阀门,消火栓", "街道名称（必填）", "街道", "井深（米）（必填）", "2"));
        importer.importBatch(batch("c", "chambers", chamber));
        chamber.put("井室类型（必填）", "消火栓");
        importer.importBatch(batch("d", "chambers", chamber));
        assertEquals(
                2,
                db.queryForObject(
                        "SELECT COUNT(*) FROM ledger_record WHERE linkStatus='unlinked' AND targetId IS NULL",
                        Integer.class));
        assertEquals(
                1,
                db.queryForObject(
                        "SELECT COUNT(*) FROM ledger_record WHERE linkStatus='classification_pending' AND targetId IS NULL",
                        Integer.class));
        assertEquals(1, db.queryForObject("SELECT COUNT(*) FROM firehydrant", Integer.class));
        assertEquals(0, db.queryForObject("SELECT COUNT(*) FROM valuewell", Integer.class));
        assertEquals(0, db.queryForObject("SELECT COUNT(*) FROM watermeterwell", Integer.class));
    }

    @Test
    void apiRequiresAuthenticationAndRestrictsImportsToAdministrators() throws Exception {
        var client = HttpClient.newHttpClient();
        var uri = URI.create("http://127.0.0.1:" + port + "/ledger/summary");
        assertEquals(
                401,
                client.send(
                                HttpRequest.newBuilder(uri).GET().build(),
                                HttpResponse.BodyHandlers.ofString())
                        .statusCode());
        String staff = sessions.create(new Sessions.User("fixture-staff", "fixture", "staff"));
        var request =
                HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/ledger/import"))
                        .header("Authorization", "Bearer " + staff)
                        .header("Content-Type", "application/json")
                        .POST(
                                HttpRequest.BodyPublishers.ofString(
                                        json.writeValueAsString(
                                                batch("a", "meters", meter("001234")))))
                        .build();
        assertEquals(403, client.send(request, HttpResponse.BodyHandlers.ofString()).statusCode());
    }

    @Test
    void paginationDetailAndCandidateCountsUseStoredRecords() throws Exception {
        importer.importBatch(batch("a", "meters", meter("001234")));
        var recheck =
                new LinkedHashMap<>(
                        Map.of(
                                "户号（必填）",
                                "001234",
                                "井内其余水表口径及用途（必填）",
                                "原用途",
                                "地址（必填）",
                                "地址",
                                "户名",
                                "用户甲"));
        importer.importBatch(batch("b", "recheck", recheck));
        String token = sessions.create(new Sessions.User("fixture-admin", "fixture", "admin"));
        var client = HttpClient.newHttpClient();
        var response =
                client.send(
                        HttpRequest.newBuilder(
                                        URI.create(
                                                "http://127.0.0.1:"
                                                        + port
                                                        + "/ledger/records?kind=recheck"))
                                .header("Authorization", "Bearer " + token)
                                .GET()
                                .build(),
                        HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        var records = json.readTree(response.body());
        assertEquals(1, records.path("total").asInt());
        var detail =
                client.send(
                        HttpRequest.newBuilder(
                                        URI.create(
                                                "http://127.0.0.1:"
                                                        + port
                                                        + "/ledger/records/"
                                                        + records.path("rows")
                                                                .get(0)
                                                                .path("id")
                                                                .asLong()))
                                .header("Authorization", "Bearer " + token)
                                .GET()
                                .build(),
                        HttpResponse.BodyHandlers.ofString());
        assertEquals(200, detail.statusCode());
        assertEquals(1, json.readTree(detail.body()).path("candidates").size());
        var relations =
                client.send(
                        HttpRequest.newBuilder(
                                        URI.create(
                                                "http://127.0.0.1:" + port + "/ledger/relations"))
                                .header("Authorization", "Bearer " + token)
                                .GET()
                                .build(),
                        HttpResponse.BodyHandlers.ofString());
        assertEquals(200, relations.statusCode());
        assertEquals(1, json.readTree(relations.body()).get(2).path("uniqueCandidates").asInt());
    }

    @Test
    void addressAndPositionMatchesCountDistinctTargetRows() throws Exception {
        var target = meter("001234");
        target.put("地址（必填）", "相同地址");
        target.put("坐标（必填）", "相同地址");
        importer.importBatch(batch("a", "meters", target));
        var source =
                Map.of("户号（必填）", "001234", "户名", "用户甲", "井内其余水表口径及用途（必填）", "原用途", "地址（必填）", "相同地址");
        importer.importBatch(batch("b", "recheck", source));
        String token = sessions.create(new Sessions.User("fixture-admin", "fixture", "admin"));
        var request =
                HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/ledger/relations"))
                        .header("Authorization", "Bearer " + token)
                        .GET()
                        .build();
        var client = HttpClient.newHttpClient();
        var first =
                json.readTree(client.send(request, HttpResponse.BodyHandlers.ofString()).body());
        assertEquals(1, first.get(6).path("uniqueCandidates").asInt());
        assertEquals(0, first.get(6).path("ambiguous").asInt());
        assertEquals(1, first.get(7).path("uniqueCandidates").asInt());
        importer.importBatch(batch("c", "meters", target));
        var second =
                json.readTree(client.send(request, HttpResponse.BodyHandlers.ofString()).body());
        assertEquals(0, second.get(6).path("uniqueCandidates").asInt());
        assertEquals(1, second.get(6).path("ambiguous").asInt());
        assertEquals(1, second.get(7).path("ambiguous").asInt());
        assertEquals(
                1,
                db.queryForObject(
                        "SELECT COUNT(*) FROM ledger_record WHERE linkStatus='unlinked'",
                        Integer.class));
    }

    @Test
    void manualMeterEndpointWritesEverySubmittedFieldAndRejectsMissingAccount() throws Exception {
        var values = new LinkedHashMap<String, String>();
        for (String field :
                List.of(
                        "filledBy",
                        "department",
                        "writtingTime",
                        "userType",
                        "accountIdentifier",
                        "accountName",
                        "accountNumber",
                        "address",
                        "coordinates",
                        "caliber",
                        "wellDepth",
                        "includedFacilities",
                        "waterMeterManufacturer",
                        "operatingStatus",
                        "wellOutside",
                        "wellInside",
                        "waterNature",
                        "operatingUsersNumber",
                        "operatingUserDetailsAndDetailedAddress",
                        "specialBankUsersNumber",
                        "specialBankUserDetailsAndDetailedAddress")) {
            values.put(field, "fixture-" + field);
        }
        values.put("accountNumber", "001234");
        values.put("wellDepth", "0");
        String token = sessions.create(new Sessions.User("fixture-admin", "fixture", "admin"));
        var client = HttpClient.newHttpClient();
        var endpoint = URI.create("http://127.0.0.1:" + port + "/WaterMeterWell/addWaterMeterWell");
        var request =
                HttpRequest.newBuilder(endpoint)
                        .header("Authorization", "Bearer " + token)
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(values)))
                        .build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertEquals(0, json.readTree(response.body()).path("code").asInt());
        var saved = db.queryForMap("SELECT * FROM watermeterwell");
        values.forEach((field, value) -> assertEquals(value, saved.get(field), field));
        assertEquals("[]", saved.get("repairInfo"));
        values.put("accountNumber", " ");
        var invalid =
                HttpRequest.newBuilder(endpoint)
                        .header("Authorization", "Bearer " + token)
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(values)))
                        .build();
        assertEquals(400, client.send(invalid, HttpResponse.BodyHandlers.ofString()).statusCode());
        assertEquals(1, db.queryForObject("SELECT COUNT(*) FROM watermeterwell", Integer.class));
    }
}
