package com.mobnote.golukmain.userlogin;

import java.util.HashMap;

import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.http.request.GolukFastjsonRequest;

import android.text.TextUtils;

import cn.com.tiros.api.Tapi;

public class UserloginBeanRequest extends GolukFastjsonRequest<UserResult> {

    public UserloginBeanRequest(int requestType, IRequestResultListener listener) {
        super(requestType, UserResult.class, listener);
    }

    @Override
    protected String getPath() {
        return "/cdcRegister/getLogin.htm";
    }

    @Override
    protected String getMethod() {
        return "getLogin";
    }

    public void loginByPhone(String phone, String pwd, String uid) {
        HashMap<String, String> headers = (HashMap<String, String>) getHeader();
        headers.put("phone", phone);
        headers.put("pwd", pwd);
        headers.put("commuid", uid);
        headers.put("tag", "android");
        headers.put("mid", "" + Tapi.getMobileId());
        get();
    }

    public void loginByEmail(String email, String pwd, String uid) {
        HashMap<String, String> headers = (HashMap<String, String>) getHeader();
        headers.put("email", email);
        headers.put("pwd", pwd);
        headers.put("commuid", uid);
        headers.put("tag", "android");
        headers.put("mid", "" + Tapi.getMobileId());
        get();
    }

}
