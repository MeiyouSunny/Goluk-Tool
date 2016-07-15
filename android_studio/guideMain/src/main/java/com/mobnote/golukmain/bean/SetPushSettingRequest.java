package com.mobnote.golukmain.bean;

import java.util.HashMap;

import cn.com.tiros.api.Tapi;

import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.http.request.GolukFastjsonRequest;

public class SetPushSettingRequest extends GolukFastjsonRequest<SetPushMsgSettingBean> {

    public SetPushSettingRequest(int requestType, IRequestResultListener listener) {
        super(requestType, SetPushMsgSettingBean.class, listener);
    }

    @Override
    protected String getPath() {
        return "/cdcRegister/pushService.htm";
    }

    @Override
    protected String getMethod() {
        return "setPushConfig";
    }

    public void get(String uid, String iscomment, String ispraise, String isfollow,String isfriend) {
        HashMap<String, String> headers = (HashMap<String, String>) getHeader();
        headers.put("uid", uid);
        headers.put("tag", "android");
        headers.put("iscomment", iscomment);
        headers.put("ispraise", ispraise);
        headers.put("isfollow", isfollow);
        headers.put("isfriend",isfriend);
        headers.put("mid", "" + Tapi.getMobileId());
        headers.put("method", "setPushConfig");
        get();
    }

}
