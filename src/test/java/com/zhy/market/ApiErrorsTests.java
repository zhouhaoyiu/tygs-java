package com.zhy.market;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.zhy.market.controller.ApiErrors;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.HandlerMapping;

class ApiErrorsTests {
    @Test
    void unexpectedErrorsLogDiagnosticsWithoutSensitiveValues() {
        var logger = (Logger) LoggerFactory.getLogger(ApiErrors.class);
        var events = new ListAppender<ILoggingEvent>();
        events.start();
        logger.addAppender(events);
        try {
            var request = new MockHttpServletRequest("PUT", "/locations/private-address/123");
            request.setQueryString("token=private-query-token");
            request.addHeader("Authorization", "Bearer private-auth-token");
            request.setContent("private-request-body".getBytes(StandardCharsets.UTF_8));
            request.setAttribute(
                    HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE, "/locations/{table}/{id}");
            var error =
                    new IllegalStateException(
                            "private-error-message", new SQLException("private-sql-value"));

            var response = new ApiErrors().failure(error, request);

            assertEquals(500, response.getStatusCode().value());
            assertEquals(Map.of("message", "服务暂时不可用"), response.getBody());
            assertEquals(1, events.list.size());
            var event = events.list.get(0);
            String logged = event.getFormattedMessage();
            assertTrue(logged.contains("/locations/{table}/{id}"));
            assertTrue(logged.contains("java.lang.IllegalStateException"));
            assertTrue(logged.contains("java.sql.SQLException"));
            assertTrue(logged.contains("ApiErrorsTests.java:"));
            assertFalse(logged.contains("private-"));
            assertNull(event.getThrowableProxy());
        } finally {
            logger.detachAppender(events);
            events.stop();
        }
    }

    @Test
    void unmappedErrorsDoNotLogTheRequestUri() {
        var request = new MockHttpServletRequest("GET", "/private-address");
        assertEquals(
                500,
                new ApiErrors()
                        .failure(new RuntimeException("private-value"), request)
                        .getStatusCode()
                        .value());
    }
}
