package com.zhy.market;

import static org.junit.jupiter.api.Assertions.*;

import com.zhy.market.workspace.GeocodeRules;
import java.util.List;
import org.junit.jupiter.api.Test;

class GeocodeRulesTests {
    private GeocodeRules.Candidate candidate(String address, String district, String level) {
        return new GeocodeRules.Candidate(
                address, "山西省", "太原市", district, "140106", "测试路", "12号", level, 112.5, 37.8);
    }

    private GeocodeRules.ProviderResult result(GeocodeRules.Candidate... candidates) {
        return new GeocodeRules.ProviderResult(
                "complete", "OK", candidates.length, List.of(candidates), null);
    }

    @Test
    void exactNumberedAddressIsOnlyAnAddressReferenceNotAFacilitySurvey() {
        var decision =
                GeocodeRules.assess("测试路12号", result(candidate("山西省太原市迎泽区测试路12号", "迎泽区", "门牌号")));
        assertEquals("address_matched", decision.status());
        assertTrue(decision.reason().contains("非设施实测点"));
        assertEquals(112.5, decision.longitude());
    }

    @Test
    void coarseMultipleAndUnmatchedAddressesNeverReceivePlotCoordinates() {
        var exact = candidate("山西省太原市迎泽区测试路12号", "迎泽区", "门牌号");
        for (var input :
                List.of(
                        result(candidate("测试小区", "迎泽区", "小区")),
                        result(exact, exact),
                        result(candidate("测试路13号", "迎泽区", "门牌号")))) {
            var decision = GeocodeRules.assess("测试路12号", input);
            assertEquals("review", decision.status());
            assertNull(decision.longitude());
        }
        for (String source : List.of("测试路12号2栋1单元", "测试路12号东侧", "杏花岭区测试路12号", "测试路")) {
            assertEquals("review", GeocodeRules.assess(source, result(exact)).status());
        }
    }

    @Test
    void servicesErrorsWrongCitiesAndInvalidCoordinatesRemainUnplotted() {
        assertEquals(
                "unavailable",
                GeocodeRules.assess(
                                "测试路12号",
                                new GeocodeRules.ProviderResult(
                                        "error", "INVALID_USER_KEY", 0, List.of(), null))
                        .status());
        var otherCity =
                new GeocodeRules.Candidate(
                        "测试路12号", "河北省", "石家庄市", "测试区", "130101", "测试路", "12号", "门牌号", 114.5, 38.0);
        assertNull(GeocodeRules.assess("测试路12号", result(otherCity)).longitude());
        var badPoint =
                new GeocodeRules.Candidate(
                        "测试路12号", "山西省", "太原市", "迎泽区", "140106", "测试路", "12号", "门牌号", 200.0, 37.8);
        assertNull(GeocodeRules.assess("测试路12号", result(badPoint)).longitude());
    }

    @Test
    void queryPreservesAddressContentAndBlocksUnexpectedSensitiveFields() {
        assertEquals("山西省太原市迎泽区测试路12-1号", GeocodeRules.query("山西省/太原市/迎泽区/测试路12-1号"));
        for (String value : List.of("", "https://example.com", "测试路13800000000", "a@example.com"))
            assertThrows(Exception.class, () -> GeocodeRules.query(value));
    }
}
