package com.mobnote.user.bindphone;

import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.http.request.GolukFastjsonRequest;
import com.mobnote.user.bindphone.bean.BindPhoneRetBean;

import java.util.HashMap;

public class BindPhoneRequest extends GolukFastjsonRequest<BindPhoneRetBean> {
    public BindPhoneRequest(int requestType, IRequestResultListener listener) {
        super(requestType, BindPhoneRetBean.class, listener);
    }

    @Override
    protected String getPath() {
        return "/cdcRegister/bindinfo.htm";
    }

    @Override
    protected String getMethod() {
        return "bindPhone";
    }

    public void get(String xieyi, String uid, String phone, String vcode) {
        HashMap<String, String> headers = (HashMap<String, String>) getHeader();
        headers.put("xieyi", xieyi);
        headers.put("uid", uid);
        headers.put("phone", phone);
        headers.put("vcode", vcode);
        get();
    }
}