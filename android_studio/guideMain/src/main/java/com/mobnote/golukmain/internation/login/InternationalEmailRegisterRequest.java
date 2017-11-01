package com.mobnote.golukmain.internation.login;

import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.http.request.GolukFastjsonRequest;
import com.mobnote.golukmain.internation.bean.RegistBean;

import java.util.HashMap;

/**
 * Created by leege100 on 2017/1/16.
 */

public class InternationalEmailRegisterRequest extends GolukFastjsonRequest<RegistBean> {

    public InternationalEmailRegisterRequest(int requestType, IRequestResultListener listener) {
        super(requestType, RegistBean.class, listener);
    }

    @Override
    protected String getPath() {
        return "/cdcRegister/registerByEmail.htm";
    }

    @Override
    protected String getMethod() {
        return null;
    }

    public boolean get(String email, String pwd) {
        HashMap<String, String> headers = (HashMap<String, String>) getHeader();
        headers.put("xieyi", "200");
        headers.put("email", email);
        headers.put("pwd", pwd);
        get();
        return true;
    }
}