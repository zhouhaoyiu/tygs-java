package com.zhy.market.controller;

import net.sf.json.JSONObject;

import java.util.List;

/**
 * @author Administrator
 */

public class utils {
    static String CODE = "code";
    static String MSG = "msg";
    static String DATE = "data";

    public static <E> Object getJsonRes(Integer code, String msg, List<E> data) {
        JSONObject json = new JSONObject();
        json.put(CODE, code);
        json.put(MSG, msg);
        json.put(DATE, data);
        return json;
    }

    public static Object getJsonRes(Integer code, String msg, String data) {
        JSONObject json = new JSONObject();
        json.put(CODE, code);
        json.put(MSG, msg);
        json.put(DATE, data);
        return json;
    }

    public static Object getJsonRes(Integer code, String msg) {
        JSONObject json = new JSONObject();
        json.put(CODE, code);
        json.put(MSG, msg);
        return json;
    }
}