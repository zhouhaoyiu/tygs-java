package com.zhy.market.controller;

public final class LegacyResponses {
    private LegacyResponses() {}

    public record Status(Integer code, String msg) {}

    public record Payload<T>(Integer code, String msg, T data) {}

    public static Status response(Integer code, String msg) {
        return new Status(code, msg);
    }

    public static <T> Payload<T> response(Integer code, String msg, T data) {
        return new Payload<>(code, msg, data);
    }
}
