package com.zhy.market.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerMapping;

@RestControllerAdvice
public class ApiErrors {
    private static final Logger LOG = LoggerFactory.getLogger(ApiErrors.class);

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<?> status(ResponseStatusException e) {
        return ResponseEntity.status(e.getStatusCode())
                .body(Map.of("message", e.getReason() == null ? "请求未完成" : e.getReason()));
    }

    @ExceptionHandler({
        NumberFormatException.class,
        HttpMessageNotReadableException.class,
        MethodArgumentTypeMismatchException.class,
        MissingServletRequestParameterException.class
    })
    public ResponseEntity<?> input(Exception e) {
        return ResponseEntity.badRequest().body(Map.of("message", "参数格式错误"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> failure(Exception e, HttpServletRequest request) {
        Object route = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        // Exception messages and request URLs can contain SQL values, addresses or credentials.
        var causes = new ArrayList<String>();
        Throwable cause = e;
        for (int depth = 0; cause != null && depth < 8; depth++) {
            causes.add(cause.getClass().getName());
            cause = cause.getCause();
        }
        var frames =
                Arrays.stream(e.getStackTrace())
                        .limit(12)
                        .map(StackTraceElement::toString)
                        .toList();
        LOG.error(
                "Unhandled API failure route={} types={} frames={}",
                route == null ? "<unmapped>" : route,
                causes,
                frames);
        return ResponseEntity.internalServerError().body(Map.of("message", "服务暂时不可用"));
    }
}
