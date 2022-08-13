package com.zhy.market.controller;

import net.sf.json.JSONObject;

import java.util.List;




public class utils {
    public static Object getJsonRes(Integer code, String msg,List data) {
        JSONObject json = new JSONObject();
        json.put("code", code);
        json.put("msg", msg);
        json.put("data", data);
        return json;
    }
    public static Object getJsonRes(Integer code, String msg, String data) {
        JSONObject json = new JSONObject();
        json.put("code", code);
        json.put("msg", msg);
        json.put("data", data);
        return json;
    }
}