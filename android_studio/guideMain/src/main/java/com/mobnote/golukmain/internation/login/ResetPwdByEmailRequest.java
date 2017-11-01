package com.mobnote.golukmain.internation.login;

import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.http.request.GolukFastjsonRequest;
import com.mobnote.golukmain.internation.bean.ResetPwdByEmailRetBean;

import java.util.HashMap;

/**
 * Created by leege100 on 2017/1/17.
 */

public class ResetPwdByEmailRequest extends GolukFastjsonRequest<ResetPwdByEmailRetBean> {

    public ResetPwdByEmailRequest(int requestType, IRequestResultListener listener) {
        super(requestType, ResetPwdByEmailRetBean.class, listener);
    }

    @Override
    protected String getPath() {
        return "/cdcRegister/resetPwdByEmail.htm";
    }

    @Override
    protected String getMethod() {
        return null;
    }

    public boolean send(String email, String pwd, String vcode) {
        HashMap<String, String> headers = (HashMap<String, String>) getHeader();
        headers.put("xieyi", "200");
        headers.put("email", email);
        headers.put("pwd", pwd);
        headers.put("vcode", vcode);
        get();
        return true;
    }
}
