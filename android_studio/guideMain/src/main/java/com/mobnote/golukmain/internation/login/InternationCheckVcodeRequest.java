package com.mobnote.golukmain.internation.login;

import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.http.request.GolukFastjsonRequest;
import com.mobnote.golukmain.internation.bean.CheckVcodeBean;
import com.mobnote.util.GolukUtils;

import java.util.HashMap;

import cn.com.tiros.api.Tapi;

/**
 * Created by lily on 16-6-24.
 */
public class InternationCheckVcodeRequest extends GolukFastjsonRequest<CheckVcodeBean> {

    public InternationCheckVcodeRequest(int requestType, IRequestResultListener listener) {
        super(requestType, CheckVcodeBean.class, listener);
    }

    @Override
    protected String getPath() {
        return "/cdcRegister/checkVcode.htm";
    }

    @Override
    protected String getMethod() {
        return null;
    }

    public boolean get(String phone, String vcode, String dialingcode) {
        HashMap<String, String> headers = (HashMap<String, String>) getHeader();
        headers.put("phone", phone);
        headers.put("vcode", vcode);
        headers.put("dialingcode", dialingcode);
        headers.put("commostag", "android");
        headers.put("commversion", GolukUtils.getCommversion());
        headers.put("xieyi", "200");
        get();
        return true;
    }

}
