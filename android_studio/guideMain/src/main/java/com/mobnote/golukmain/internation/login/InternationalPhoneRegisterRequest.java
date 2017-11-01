package com.mobnote.golukmain.internation.login;

import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.http.request.GolukFastjsonRequest;
import com.mobnote.golukmain.internation.bean.RegistBean;

import java.util.HashMap;

/**
 * Created by lily on 16-6-27.
 */
public class InternationalPhoneRegisterRequest extends GolukFastjsonRequest<RegistBean> {

    public InternationalPhoneRegisterRequest(int requestType, IRequestResultListener listener) {
        super(requestType, RegistBean.class, listener);
    }

    @Override
    protected String getPath() {
        return "/cdcRegister/getPhoneRegister.htm";
    }

    @Override
    protected String getMethod() {
        return null;
    }

    public boolean get(String phone, String pwd, String vcode, String dialingcode, String step2Code) {
        HashMap<String, String> headers = (HashMap<String, String>) getHeader();
        headers.put("xieyi", "200");
        headers.put("phone", phone);
        headers.put("pwd", pwd);
        headers.put("step2code", step2Code);
        headers.put("dialingcode", dialingcode);
        get();
        return true;
    }
}
