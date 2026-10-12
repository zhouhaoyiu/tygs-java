package com.zhy.market;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.zhy.market.controller.LegacyResponses;
import java.util.List;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

class LegacyResponsesTests {
    private final ObjectMapper json = new ObjectMapper();

    @Test
    void statusResponseOmitsDataAndKeepsOriginalCodeAndMessage() {
        var result = json.valueToTree(LegacyResponses.response(0, "水表已停用"));
        assertEquals(json.readTree("{\"code\":0,\"msg\":\"水表已停用\"}"), result);
        assertFalse(result.has("data"));
    }

    @Test
    void payloadResponseKeepsExplicitNullStringAndListData() {
        var empty = json.valueToTree(LegacyResponses.response(1, "新增管理员日志成功", null));
        assertEquals(json.readTree("{\"code\":1,\"msg\":\"新增管理员日志成功\",\"data\":null}"), empty);
        assertTrue(empty.has("data"));
        assertTrue(empty.path("data").isNull());
        assertEquals(
                json.readTree("{\"code\":0,\"msg\":\"test\",\"data\":\"1\"}"),
                json.valueToTree(LegacyResponses.response(0, "test", "1")));
        assertEquals(
                json.readTree("{\"code\":1,\"msg\":\"ok\",\"data\":[1,2]}"),
                json.valueToTree(LegacyResponses.response(1, "ok", List.of(1, 2))));
    }
}
