package com.mobnote.golukmain.livevideo;

import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.http.request.GolukFastjsonRequest;

import java.util.HashMap;

/**
 * Created by leege100 on 2016/8/6.
 */
public class StopPlayRequest extends GolukFastjsonRequest<IsLiveRetBean> {
    public StopPlayRequest(IRequestResultListener listener) {
        super(0, IsLiveRetBean.class, listener);
    }

    @Override
    protected String getPath() {
        return "/navidog4MeetTrans/stopPlay.htm";
    }

    @Override
    protected String getMethod() {
        return "";
    }

    protected void getStopLive(String uid) {
        HashMap<String, String> headers = (HashMap<String, String>) getHeader();
        headers.put("xieyi", "100");
        headers.put("uid", uid);
        get();
    }
}
