package com.zhy.market.controller;
import java.util.Map;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
@RestControllerAdvice
public class ApiErrors {
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<?> status(ResponseStatusException e) { return ResponseEntity.status(e.getStatusCode()).body(Map.of("message",e.getReason()==null?"请求未完成":e.getReason())); }
    @ExceptionHandler({NumberFormatException.class,org.springframework.http.converter.HttpMessageNotReadableException.class})
    public ResponseEntity<?> input(Exception e) { return ResponseEntity.badRequest().body(Map.of("message","参数格式错误")); }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> failure(Exception e) { return ResponseEntity.internalServerError().body(Map.of("message","服务暂时不可用")); }
}
