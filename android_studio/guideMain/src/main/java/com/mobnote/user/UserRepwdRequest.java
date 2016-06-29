package com.mobnote.user;

import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.http.request.GolukFastjsonRequest;
import com.mobnote.user.bindphone.bean.BindPhoneRetBean;

import java.util.HashMap;

/**
 * Created by hanzheng on 2016/6/29.
 */
public class UserRepwdRequest extends GolukFastjsonRequest<UserRepwdBean>{

    public UserRepwdRequest(int requestType, IRequestResultListener listener) {
        super(requestType, UserRepwdBean.class, listener);
    }
    @Override
    protected String getPath() {
        return "/cdcRegister/getPwdReset.htm";
    }

    @Override
    protected String getMethod() {
        return "";
    }

    public void get(String phone, String pwd, String vcode, String dialingcode) {
        HashMap<String, String> headers = (HashMap<String, String>) getParam();
        headers.put("phone", phone);
        headers.put("pwd", pwd);
        headers.put("vcode", vcode);
        headers.put("dialingcode", dialingcode);
        get();
    }
}
