package com.zhy.market;

import static org.junit.jupiter.api.Assertions.*;

import com.zhy.market.ledger.LedgerImport.SourceRow;
import com.zhy.market.security.Sessions;
import com.zhy.market.workspace.*;
import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.*;
import java.util.HexFormat;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@ActiveProfiles("local")
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
            "spring.datasource.url=jdbc:h2:mem:workspace-tests;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
            "spring.datasource.username=sa",
            "spring.datasource.password=",
            "LOCAL_DB_PASSWORD=test-only",
            "LOCAL_ADMIN_PASSWORD=local-test-password",
            "server.ssl.enabled=false"
        })
class WorkspaceTests {
    @Autowired WorkspaceService service;
    @Autowired ModuleCatalog catalog;
    @Autowired JdbcTemplate db;
    @Autowired Sessions sessions;
    @Autowired ObjectMapper json;
    @LocalServerPort int port;
    String admin, staff;

    @BeforeEach
    void clear() {
        for (String table :
                List.of(
                        "workspace_change",
                        "workspace_request",
                        "workspace_import",
                        "workspace_state",
                        "facility_location",
                        "facility_geocode",
                        "ledger_record",
                        "ledger_source",
                        "watermeterwell")) db.update("DELETE FROM " + table);
        for (var module : catalog.all()) db.update("DELETE FROM `" + module.table() + "`");
        admin = sessions.create(new Sessions.User("qa-admin", "qa-admin", "admin"));
        staff = sessions.create(new Sessions.User("qa-staff", "qa-staff", "staff"));
    }

    private Map<String, String> values(String key) {
        Map<String, String> result = new LinkedHashMap<>();
        for (var field : catalog.get(key).fields())
            result.put(field.key(), field.key().equals("jfh") ? "000123" : "测试-" + field.label());
        return result;
    }

    private WorkspaceService.SaveRequest save(String key) {
        return new WorkspaceService.SaveRequest(UUID.randomUUID().toString(), null, values(key));
    }

    private long id(WorkspaceResponses.RecordView record) {
        return record.id();
    }

    private HttpResponse<String> http(String method, String path, Object body, String token)
            throws Exception {
        var request =
                HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path))
                        .header("Content-Type", "application/json");
        if (token != null) request.header("Authorization", "Bearer " + token);
        request.method(
                method,
                body == null
                        ? HttpRequest.BodyPublishers.noBody()
                        : HttpRequest.BodyPublishers.ofString(json.writeValueAsString(body)));
        return HttpClient.newHttpClient()
                .send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    private List<HttpResponse<String>> concurrentRequests(
            String method, String path, Object body, String token) throws Exception {
        var request =
                HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path))
                        .timeout(Duration.ofSeconds(10))
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + token)
                        .method(
                                method,
                                HttpRequest.BodyPublishers.ofString(json.writeValueAsString(body)))
                        .build();
        var client = HttpClient.newHttpClient();
        var first = client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        var second = client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        return List.of(first.get(10, TimeUnit.SECONDS), second.get(10, TimeUnit.SECONDS));
    }

    private WorkspaceService.ImportRow raw(String key, int row) {
        var mapped = values(key);
        Map<String, String> original = new LinkedHashMap<>();
        for (var field : catalog.get(key).fields())
            original.put(field.label(), mapped.get(field.key()));
        original.put("原件附加列", "仍保留");
        return new WorkspaceService.ImportRow(
                new SourceRow(
                        "原表",
                        row,
                        original,
                        Map.of("A" + row, Map.of("value", "000123", "type", "s"))),
                mapped);
    }

    private void keys(JsonNode value, String... names) {
        assertTrue(value.isObject());
        assertEquals(Set.of(names), json.convertValue(value, Map.class).keySet());
    }

    @Test
    void workspaceHttpResponseShapesAndAuditPayloadRemainStable() throws Exception {
        var input = save("cards");
        var created = json.readTree(http("POST", "/workspace/cards/records", input, staff).body());
        keys(created, "id", "values", "archived", "revision");
        assertEquals(json.valueToTree(input.values()), created.path("values"));
        assertFalse(created.path("archived").asBoolean());
        String revision =
                HexFormat.of()
                        .formatHex(
                                MessageDigest.getInstance("SHA-256")
                                        .digest(
                                                json.writeValueAsString(
                                                                List.of(
                                                                        created.path("id").asLong(),
                                                                        input.values(),
                                                                        false))
                                                        .getBytes(StandardCharsets.UTF_8)));
        assertEquals(revision, created.path("revision").asText());
        String route = "/workspace/cards/records/" + created.path("id").asLong();
        var detail = json.readTree(http("GET", route, null, staff).body());
        keys(detail, "id", "values", "archived", "revision", "history", "sources");
        keys(detail.path("history").get(0), "action", "actor", "before", "after", "createdAt");
        assertEquals(created, detail.path("history").get(0).path("after"));
        assertEquals(json.readTree("{}"), detail.path("history").get(0).path("before"));
        assertTrue(detail.path("sources").isArray());
        var page = json.readTree(http("GET", "/workspace/cards/records", null, staff).body());
        keys(page, "rows", "total", "page", "size");
        assertEquals(created, page.path("rows").get(0));
        var imported =
                json.readTree(
                        http(
                                        "POST",
                                        "/workspace/supply/import",
                                        new WorkspaceService.ImportRequest(
                                                "c".repeat(64),
                                                "contract.xlsx",
                                                List.of(raw("supply", 2))),
                                        admin)
                                .body());
        assertEquals(json.readTree("{\"inserted\":1,\"skipped\":0}"), imported);
        var supply =
                json.readTree(http("GET", "/workspace/supply/records", null, admin).body())
                        .path("rows")
                        .get(0);
        var source =
                json.readTree(
                                http(
                                                "GET",
                                                "/workspace/supply/records/"
                                                        + supply.path("id").asLong(),
                                                null,
                                                admin)
                                        .body())
                        .path("sources")
                        .get(0);
        keys(source, "sha256", "fileName", "sheetName", "rowNumber", "raw");
    }

    @Test
    void locationResponsesPreserveNullsHistoryAndErrorContracts() throws Exception {
        var facility = service.create("rooms", save("rooms"), "qa");
        String route = "/locations/watermeterroom/" + id(facility);
        var page = json.readTree(http("GET", "/locations/records", null, staff).body());
        keys(page, "rows", "total");
        var row = page.path("rows").get(0);
        keys(
                row,
                "id",
                "name",
                "identifier",
                "address",
                "targetTable",
                "targetId",
                "location",
                "geocode");
        assertTrue(row.path("location").isNull());
        assertTrue(row.path("geocode").isNull());
        var summary = json.readTree(http("GET", "/locations/summary", null, staff).body());
        assertEquals(
                json.readTree(
                        "{\"total\":1,\"located\":0,\"addressMatched\":0,\"review\":0,\"pending\":1}"),
                summary);
        var saved =
                json.readTree(
                        http(
                                        "PUT",
                                        route,
                                        new LocationModels.LocationInput(
                                                112.5, 37.8, "WGS84", "contract fixture", 0),
                                        staff)
                                .body());
        assertEquals(
                json.readTree(
                        "{\"longitude\":112.5,\"latitude\":37.8,\"crs\":\"WGS84\",\"evidence\":\"contract fixture\",\"revision\":1}"),
                saved);
        var located =
                json.readTree(http("GET", "/locations/records", null, staff).body())
                        .path("rows")
                        .get(0)
                        .path("location");
        keys(located, "longitude", "latitude", "crs", "evidence", "actor", "updatedAt", "revision");
        var history = json.readTree(http("GET", route + "/history", null, staff).body()).get(0);
        keys(history, "actor", "before", "after", "createdAt");
        assertEquals(saved, history.path("after"));
        var error = http("GET", "/locations/records?page=0", null, staff);
        assertEquals(400, error.statusCode());
        assertEquals(json.readTree("{\"message\":\"查询参数无效\"}"), json.readTree(error.body()));
        var geocode = json.readTree(http("GET", route + "/geocode", null, staff).body());
        keys(geocode, "sourceAddress", "queryAddress", "city", "blocked", "history");
    }

    @Test
    void locationWriteAndAuditRollBackTogetherWhenAuditFails() throws Exception {
        var facility = service.create("rooms", save("rooms"), "qa");
        db.execute(
                "ALTER TABLE workspace_change ADD CONSTRAINT contract_reject_location CHECK (module NOT LIKE 'locations:%')");
        try {
            var response =
                    http(
                            "PUT",
                            "/locations/watermeterroom/" + id(facility),
                            new LocationModels.LocationInput(
                                    112.5, 37.8, "WGS84", "rollback fixture", 0),
                            staff);
            assertEquals(500, response.statusCode());
            assertEquals(
                    json.readTree("{\"message\":\"服务暂时不可用\"}"), json.readTree(response.body()));
            assertEquals(
                    0, db.queryForObject("SELECT COUNT(*) FROM facility_location", Integer.class));
            assertEquals(
                    0,
                    db.queryForObject(
                            "SELECT COUNT(*) FROM workspace_change WHERE module LIKE 'locations:%'",
                            Integer.class));
        } finally {
            db.execute("ALTER TABLE workspace_change DROP CONSTRAINT contract_reject_location");
        }
    }

    @Test
    void concurrentLocationSavesKeepOneRevisionAndOneAuditRecord() throws Exception {
        var facility = service.create("rooms", save("rooms"), "qa");
        var replies =
                concurrentRequests(
                        "PUT",
                        "/locations/watermeterroom/" + id(facility),
                        new LocationModels.LocationInput(
                                112.5, 37.8, "WGS84", "concurrent fixture", 0),
                        staff);
        assertEquals(
                List.of(200, 409),
                replies.stream().map(HttpResponse::statusCode).sorted().toList());
        assertEquals(1L, db.queryForObject("SELECT revision FROM facility_location", Long.class));
        assertEquals(
                1,
                db.queryForObject(
                        "SELECT COUNT(*) FROM workspace_change WHERE module LIKE 'locations:%'",
                        Integer.class));
    }

    @Test
    void facilityPagesPreserveLegacyFieldsAndBoundResults() throws Exception {
        for (var spec :
                List.of(
                        List.of(
                                "rooms",
                                "watermeterroom",
                                "WaterMeterRoom/getAllWaterMeterRoomInfo"),
                        List.of(
                                "meters",
                                "watermeterwell",
                                "WaterMeterWell/getAllWaterMeterWellInfo"),
                        List.of("hydrants", "firehydrant", "FireHydrant/getAllFireHydrantInfo"),
                        List.of("valves", "valuewell", "ValueWell/getAllValueWellInfo"))) {
            for (int i = 0; i < 65; i++)
                db.update(
                        "INSERT INTO " + spec.get(1) + "(accountIdentifier,filledBy) VALUES(?,?)",
                        "QA-" + i,
                        "合成测试");
            var old = json.readTree(http("GET", "/" + spec.get(2), null, admin).body());
            var first =
                    json.readTree(
                            http("GET", "/facilities/" + spec.get(0) + "/records", null, admin)
                                    .body());
            assertEquals(65, first.path("total").asInt());
            assertEquals(30, first.path("rows").size());
            assertEquals(old.get(0), first.path("rows").get(0));
            var last =
                    json.readTree(
                            http(
                                            "GET",
                                            "/facilities/" + spec.get(0) + "/records?page=3",
                                            null,
                                            admin)
                                    .body());
            assertEquals(5, last.path("rows").size());
            assertEquals(old.get(60), last.path("rows").get(0));
        }
    }

    @Test
    void facilityFiltersSearchWholeDatasetWithLiteralWildcards() throws Exception {
        for (int i = 0; i < 65; i++)
            db.update(
                    "INSERT INTO watermeterroom(accountIdentifier,communityName) VALUES(?,?)",
                    "QA-" + i,
                    i == 64 ? "Needle_100%!" : "普通测试");
        var query =
                json.readTree(
                        http(
                                        "GET",
                                        "/facilities/rooms/records?field=communityName&q=needle_100%25!",
                                        null,
                                        admin)
                                .body());
        assertEquals(1, query.path("total").asInt());
        assertEquals("QA-64", query.path("rows").get(0).path("accountIdentifier").asText());
        db.update(
                "INSERT INTO valuewell(accountIdentifier,caliber,wellChamberType,streetName) VALUES('Q1','50','A','TEST street'),('Q2','100','A','TEST street'),('Q3','50','B','TEST street')");
        var filtered =
                json.readTree(
                        http(
                                        "GET",
                                        "/facilities/valves/records?caliber=50&caliber=100&wellChamberType=A&streetName=TEST",
                                        null,
                                        admin)
                                .body());
        assertEquals(2, filtered.path("total").asInt());
        var wrongCase =
                json.readTree(
                        http("GET", "/facilities/valves/records?streetName=test", null, admin)
                                .body());
        assertEquals(0, wrongCase.path("total").asInt());
    }

    @Test
    void facilityPaginationRejectsInvalidParametersAndRequiresLogin() throws Exception {
        assertEquals(401, http("GET", "/facilities/rooms/records", null, null).statusCode());
        assertEquals(404, http("GET", "/facilities/admin/records", null, admin).statusCode());
        for (String suffix :
                List.of(
                        "page=0",
                        "size=501",
                        "page=abc",
                        "field=id",
                        "field=communityName%20OR%201=1",
                        "q=x",
                        "caliber=50",
                        "wellChamberType=A"))
            assertEquals(
                    400,
                    http("GET", "/facilities/rooms/records?" + suffix, null, admin).statusCode(),
                    suffix);
        var summary = json.readTree(http("GET", "/locations/summary", null, admin).body());
        for (String field : List.of("total", "located", "addressMatched", "review", "pending"))
            assertEquals(0, summary.path(field).asInt());
    }

    @Test
    void everyModulePersistsAllFieldsWithoutCreatingLedgerLinks() {
        assertEquals(9, catalog.all().size());
        for (var module : catalog.all()) {
            var input = save(module.key());
            var result = service.create(module.key(), input, "qa");
            assertEquals(input.values(), result.values());
            var detail = service.detail(module.key(), id(result));
            assertEquals(1, detail.history().size());
            assertEquals(
                    1,
                    db.queryForObject(
                            "SELECT COUNT(*) FROM `" + module.table() + "`", Integer.class));
            if (!module.managed())
                assertEquals(
                        "[]",
                        db.queryForObject(
                                "SELECT repairInfo FROM `" + module.table() + "`", String.class));
        }
        assertEquals(0, db.queryForObject("SELECT COUNT(*) FROM ledger_record", Integer.class));
    }

    @Test
    void createIsIdempotentAndDifferentSubmissionsNeverMerge() {
        var input = save("supply");
        var first = service.create("supply", input, "qa");
        assertEquals(first, service.create("supply", input, "qa"));
        var changed = new LinkedHashMap<>(input.values());
        changed.put("mc", "改名");
        var conflict =
                assertThrows(
                        ResponseStatusException.class,
                        () ->
                                service.create(
                                        "supply",
                                        new WorkspaceService.SaveRequest(
                                                input.requestId(), null, changed),
                                        "qa"));
        assertEquals(409, conflict.getStatusCode().value());
        service.create("supply", save("supply"), "qa");
        assertEquals(2, db.queryForObject("SELECT COUNT(*) FROM ThreeSupply", Integer.class));
    }

    @Test
    void updateChecksRevisionAndArchiveRemainsRecoverable() {
        var first = service.create("cards", save("cards"), "qa");
        var values = values("cards");
        values.put("js", "0");
        values.put("jsbh", "000007");
        var updated =
                service.update(
                        "cards",
                        id(first),
                        new WorkspaceService.SaveRequest(null, first.revision(), values),
                        "qa");
        assertEquals(values, updated.values());
        assertThrows(
                ResponseStatusException.class,
                () ->
                        service.update(
                                "cards",
                                id(first),
                                new WorkspaceService.SaveRequest(null, first.revision(), values),
                                "qa"));
        var archived =
                service.archive(
                        "cards",
                        id(first),
                        new WorkspaceService.ArchiveRequest(updated.revision(), true),
                        "qa");
        assertEquals(0L, service.list("cards", "", "", "active", 1, 30).total());
        assertEquals(1L, service.list("cards", "", "", "archived", 1, 30).total());
        service.archive(
                "cards",
                id(first),
                new WorkspaceService.ArchiveRequest(archived.revision(), false),
                "qa");
        assertEquals(1L, service.list("cards", "", "", "active", 1, 30).total());
        assertEquals(4, service.detail("cards", id(first)).history().size());
        assertEquals("000007", db.queryForObject("SELECT jsbh FROM card", String.class));
    }

    @Test
    void importRetainsRawSourceAndDoesNotOverwriteEditsOnRetry() {
        var input =
                new WorkspaceService.ImportRequest(
                        "a".repeat(64), "原件.xlsx", List.of(raw("supply", 2)));
        assertEquals(1, service.importRows("supply", input, "qa").inserted());
        long record = db.queryForObject("SELECT infoId FROM ThreeSupply", Long.class);
        var first = service.detail("supply", record);
        var edited = values("supply");
        edited.put("mc", "人工核对名称");
        service.update(
                "supply",
                record,
                new WorkspaceService.SaveRequest(null, first.revision(), edited),
                "qa");
        assertEquals(1, service.importRows("supply", input, "qa").skipped());
        assertEquals("人工核对名称", db.queryForObject("SELECT mc FROM ThreeSupply", String.class));
        var raw =
                json.readTree(
                        db.queryForObject("SELECT rawJson FROM workspace_import", String.class));
        assertEquals("仍保留", raw.path("values").path("原件附加列").asText());
        assertEquals("000123", raw.path("cells").path("A2").path("value").asText());
        service.importRows(
                "supply",
                new WorkspaceService.ImportRequest("b".repeat(64), "其他来源.xlsx", input.rows()),
                "qa");
        assertEquals(2, db.queryForObject("SELECT COUNT(*) FROM ThreeSupply", Integer.class));
    }

    @Test
    void failedLaterImportRowRollsBackDataAndAudit() {
        var first = raw("supply", 2);
        var brokenValues = new LinkedHashMap<>(values("supply"));
        brokenValues.put("mc", "");
        var invalid = new WorkspaceService.ImportRow(first.raw(), brokenValues);
        assertThrows(
                ResponseStatusException.class,
                () ->
                        service.importRows(
                                "supply",
                                new WorkspaceService.ImportRequest(
                                        "a".repeat(64), "原表.xlsx", List.of(first, invalid)),
                                "qa"));
        for (String table : List.of("ThreeSupply", "workspace_import", "workspace_change"))
            assertEquals(0, db.queryForObject("SELECT COUNT(*) FROM " + table, Integer.class));
        var tampered = new LinkedHashMap<>(first.values());
        tampered.put("mc", "没有来源支持的值");
        assertThrows(
                ResponseStatusException.class,
                () ->
                        service.importRows(
                                "supply",
                                new WorkspaceService.ImportRequest(
                                        "a".repeat(64),
                                        "原表.xlsx",
                                        List.of(
                                                new WorkspaceService.ImportRow(
                                                        first.raw(), tampered))),
                                "qa"));
    }

    @Test
    void queryEscapesWildcardsAndRejectsUnknownFields() {
        var value = values("supply");
        value.put("mc", "A%_B");
        service.create(
                "supply",
                new WorkspaceService.SaveRequest(UUID.randomUUID().toString(), null, value),
                "qa");
        service.create("supply", save("supply"), "qa");
        assertEquals(1L, service.list("supply", "%_", "mc", "active", 1, 30).total());
        assertEquals(1, service.list("supply", "", "", "all", 2, 1).rows().size());
        assertThrows(
                ResponseStatusException.class,
                () -> service.list("supply", "", "mc OR 1=1", "all", 1, 30));
        var extra = values("supply");
        extra.put("adminRole", "0");
        assertThrows(
                ResponseStatusException.class,
                () ->
                        service.create(
                                "supply",
                                new WorkspaceService.SaveRequest(
                                        UUID.randomUUID().toString(), null, extra),
                                "qa"));
    }

    @Test
    void httpAuthenticationAndAdministratorBoundaries() throws Exception {
        assertEquals(401, http("GET", "/workspace/modules", null, null).statusCode());
        assertEquals(200, http("GET", "/workspace/modules", null, staff).statusCode());
        assertEquals(404, http("GET", "/workspace/admin/schema", null, admin).statusCode());
        var created = http("POST", "/workspace/supply/records", save("supply"), staff);
        assertEquals(200, created.statusCode());
        var row = json.readTree(created.body());
        assertEquals(
                403,
                http(
                                "POST",
                                "/workspace/supply/records/" + row.path("id").asLong() + "/archive",
                                Map.of("revision", row.path("revision").asText(), "archived", true),
                                staff)
                        .statusCode());
        assertEquals(
                403,
                http(
                                "POST",
                                "/workspace/supply/import",
                                new WorkspaceService.ImportRequest(
                                        "a".repeat(64), "原表.xlsx", List.of(raw("supply", 2))),
                                staff)
                        .statusCode());
        assertEquals(
                200,
                http("GET", "/workspace/supply/records?field=jfh&q=000123", null, admin)
                        .statusCode());
    }

    @Test
    void locationWritesRequireCoordinatesCrsEvidenceAndCurrentVersion() throws Exception {
        var facility = service.create("hydrants", save("hydrants"), "qa");
        String route = "/locations/firehydrant/" + id(facility);
        var original =
                db.queryForObject("SELECT positioningCoordinates FROM firehydrant", String.class);
        assertEquals(401, http("GET", "/locations/summary", null, null).statusCode());
        assertEquals(
                400,
                http(
                                "PUT",
                                route,
                                new LocationModels.LocationInput(200.0, 37.8, "WGS84", "GPS", 0),
                                admin)
                        .statusCode());
        assertEquals(
                400,
                http(
                                "PUT",
                                route,
                                new LocationModels.LocationInput(112.5, 37.8, "UNKNOWN", "GPS", 0),
                                admin)
                        .statusCode());
        assertEquals(
                400,
                http(
                                "PUT",
                                route,
                                new LocationModels.LocationInput(112.5, 37.8, "WGS84", "", 0),
                                admin)
                        .statusCode());
        var input = new LocationModels.LocationInput(112.5, 37.8, "WGS84", "测试环境合成点，不是实测", 0);
        assertEquals(200, http("PUT", route, input, staff).statusCode());
        assertEquals(409, http("PUT", route, input, admin).statusCode());
        assertEquals(404, http("PUT", "/locations/admin/1", input, admin).statusCode());
        var records =
                json.readTree(http("GET", "/locations/records?state=located", null, admin).body());
        assertEquals(1, records.path("total").asInt());
        assertEquals(
                112.5, records.path("rows").get(0).path("location").path("longitude").asDouble());
        assertEquals(1, json.readTree(http("GET", route + "/history", null, admin).body()).size());
        assertEquals(
                original,
                db.queryForObject("SELECT positioningCoordinates FROM firehydrant", String.class));
        assertEquals(0, db.queryForObject("SELECT COUNT(*) FROM ledger_record", Integer.class));
    }

    @Test
    void geocodeAuditIsIdempotentAndNeverReplacesConfirmedCoordinatesOrSource() throws Exception {
        var value = values("rooms");
        value.put("detailedAddress", "测试路12号");
        var facility =
                service.create(
                        "rooms",
                        new WorkspaceService.SaveRequest(UUID.randomUUID().toString(), null, value),
                        "qa");
        String route = "/locations/watermeterroom/" + id(facility);
        var candidate =
                new GeocodeRules.Candidate(
                        "山西省太原市迎泽区测试路12号",
                        "山西省",
                        "太原市",
                        "迎泽区",
                        "140106",
                        "测试路",
                        "12号",
                        "门牌号",
                        112.5,
                        37.8);
        var result =
                new GeocodeRules.ProviderResult(
                        "complete", "OK", 1, List.of(candidate), Map.of("testOnly", true));
        var input = new LocationModels.GeocodeInput(UUID.randomUUID().toString(), "测试路12号", result);
        assertEquals(401, http("GET", route + "/geocode", null, null).statusCode());
        assertEquals(403, http("POST", route + "/geocode", input, staff).statusCode());
        var replies = concurrentRequests("POST", route + "/geocode", input, admin);
        assertTrue(replies.stream().allMatch(reply -> reply.statusCode() == 200));
        var details = json.readTree(replies.get(0).body());
        keys(details, "sourceAddress", "queryAddress", "city", "blocked", "history");
        var match = details.path("history").get(0);
        keys(
                match,
                "sourceAddress",
                "queryAddress",
                "decision",
                "reason",
                "actor",
                "createdAt",
                "id",
                "current",
                "result");
        assertEquals(json.valueToTree(result), match.path("result"));
        assertTrue(match.path("current").asBoolean());
        assertEquals(details, json.readTree(http("POST", route + "/geocode", input, admin).body()));
        assertEquals(1, db.queryForObject("SELECT COUNT(*) FROM facility_geocode", Integer.class));
        assertEquals(0, db.queryForObject("SELECT COUNT(*) FROM facility_location", Integer.class));
        assertEquals(
                "测试路12号",
                db.queryForObject("SELECT detailedAddress FROM watermeterroom", String.class));
        var listed =
                json.readTree(http("GET", "/locations/records?state=matched", null, admin).body());
        assertEquals(1, listed.path("total").asInt());
        assertEquals(
                "address_matched",
                listed.path("rows").get(0).path("geocode").path("decision").asText());
        assertTrue(listed.path("rows").get(0).path("location").isNull());
        keys(
                listed.path("rows").get(0).path("geocode"),
                "id",
                "decision",
                "reason",
                "longitude",
                "latitude",
                "createdAt");
        assertEquals(
                200,
                http(
                                "PUT",
                                route,
                                new LocationModels.LocationInput(
                                        112.6, 37.9, "WGS84", "独立人工依据，测试合成点", 0),
                                staff)
                        .statusCode());
        assertEquals(
                0,
                json.readTree(http("GET", "/locations/records?state=matched", null, admin).body())
                        .path("total")
                        .asInt());
        assertEquals(
                112.6, db.queryForObject("SELECT longitude FROM facility_location", Double.class));
        db.update("UPDATE watermeterroom SET detailedAddress='测试路13号'");
        assertEquals(409, http("POST", route + "/geocode", input, admin).statusCode());
        var history =
                json.readTree(http("GET", route + "/geocode", null, admin).body()).path("history");
        assertFalse(history.get(0).path("current").asBoolean());
    }

    @Test
    void coarseGeocodeIsRetainedForReviewAndSourceChangeInvalidatesTheMatch() throws Exception {
        var value = values("hydrants");
        value.put("positioningCoordinates", "测试路");
        var facility =
                service.create(
                        "hydrants",
                        new WorkspaceService.SaveRequest(UUID.randomUUID().toString(), null, value),
                        "qa");
        String route = "/locations/firehydrant/" + id(facility) + "/geocode";
        var candidate =
                new GeocodeRules.Candidate(
                        "测试路", "山西省", "太原市", "迎泽区", "140106", "测试路", "", "道路", 112.5, 37.8);
        assertEquals(
                200,
                http(
                                "POST",
                                route,
                                new LocationModels.GeocodeInput(
                                        UUID.randomUUID().toString(),
                                        "测试路",
                                        new GeocodeRules.ProviderResult(
                                                "complete", "OK", 1, List.of(candidate), null)),
                                admin)
                        .statusCode());
        var listed =
                json.readTree(http("GET", "/locations/records?state=review", null, admin).body());
        assertEquals(1, listed.path("total").asInt());
        assertTrue(listed.path("rows").get(0).path("geocode").path("longitude").isNull());
        assertEquals(
                1,
                json.readTree(http("GET", "/locations/summary", null, admin).body())
                        .path("pending")
                        .asInt());
        db.update("UPDATE firehydrant SET positioningCoordinates='另一条路'");
        assertEquals(
                0,
                json.readTree(http("GET", "/locations/records?state=review", null, admin).body())
                        .path("total")
                        .asInt());
        assertTrue(
                json.readTree(http("GET", "/locations/records", null, admin).body())
                        .path("rows")
                        .get(0)
                        .path("geocode")
                        .isNull());
    }
}
