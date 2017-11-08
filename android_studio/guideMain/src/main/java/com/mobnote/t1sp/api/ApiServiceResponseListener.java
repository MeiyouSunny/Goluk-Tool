package com.mobnote.t1sp.api;

import org.json.JSONException;

import likly.dollar.$;
import likly.reverse.JsonParseException;
import likly.reverse.OnCallExecuteListener;
import likly.reverse.Response;

/**
 * 网络请求返回数据处理
 * 对返回数据做解析,解密等处理
 */
public class ApiServiceResponseListener implements OnCallExecuteListener {
    @Override
    public void onStart() {

    }

    @Override
    public void onResponse(Response response) {

//        String JSESSIONID = response.header("Set-Cookie");
//        if (!TextUtils.isEmpty(JSESSIONID) && JSESSIONID.contains("JSESSIONID")) {
//
//            String cookie = response.header("Set-Cookie");
//            $.debug().e(cookie);
//            String jsessionId = cookie.substring(0, cookie.indexOf(";"));
//            $.debug().e(jsessionId);
//        }
    }

    @Override
    public void onParseResponseStart() {

    }

    @Override
    public String onParseJson(String s) throws JsonParseException {
        try {
            return parseJsonData(s);
        } catch (JSONException e) {
            $.debug().tag("ERROR:").e(e.toString());
            throw new JsonParseException(e);
        }
    }

    @Override
    public void onResponseResult(Object o) {
        //$.debug().e($.json().toJson(o));
    }

    @Override
    public void onParseResponseFinish() {

    }

    @Override
    public void onFinish() {

    }

    @Override
    public void onCancel() {

    }

    private String parseJsonData(String json) throws JSONException {
//        $.debug().e("RESPONSE: " + json);
//        JSONObject jsonObject = new JSONObject(json);
//        final int code = jsonObject.optInt("code");
//        if (code != 0) {
//            final String msg = jsonObject.optString("msg");
//            String data = jsonObject.optString("data");
//            data = parseData(data);
//            $.debug().tag("DATA:").e(data);
//            throw new ServiceException(code, msg);
//        }
//        String data = jsonObject.optString("data");
//        data = parseData(data);
//        try {
//            $.debug().tag("DATA:").e(data);
//            $.debug().tag("DATA:").json(data, 4);
//        } catch (Exception ignore) {
//        }

        return json;
    }

    private String parseData(String data) {
        return data;
    }
}
