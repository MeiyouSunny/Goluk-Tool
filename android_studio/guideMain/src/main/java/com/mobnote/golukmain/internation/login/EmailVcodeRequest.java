package com.mobnote.golukmain.internation.login;

import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.http.request.GolukFastjsonRequest;
import com.mobnote.golukmain.internation.bean.EmailVcodeRetBean;

import java.util.HashMap;

/**
 * Created by leege100 on 2017/1/17.
 * 获取邮箱验证码
 */

public class EmailVcodeRequest extends GolukFastjsonRequest<EmailVcodeRetBean>{

    public EmailVcodeRequest(int requestType, IRequestResultListener listener) {
        super(requestType, EmailVcodeRetBean.class, listener);
    }

    @Override
    protected String getPath() {
        return "/cdcRegister/getCodeByEmail.htm";
    }

    @Override
    protected String getMethod() {
        return null;
    }

    public boolean send(String email, String type) {
        HashMap<String, String> headers = (HashMap<String, String>) getHeader();
        headers.put("xieyi", "200");
        headers.put("email", email);
        headers.put("type", type);
        get();
        return true;
    }
}
