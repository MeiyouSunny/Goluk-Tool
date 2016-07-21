package com.mobnote.golukmain.livevideo;

import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.http.request.GolukFastjsonRequest;
import com.mobnote.golukmain.livevideo.bean.LiveSignRetBean;

import java.util.HashMap;

/**
 * Created by leege100 on 2016/7/21.
 */
public class LiveSignRequest extends GolukFastjsonRequest<LiveSignRetBean> {
    public LiveSignRequest(int requestType, IRequestResultListener listener) {
        super(requestType, LiveSignRetBean.class, listener);
    }

    @Override
    protected String getPath() {
        return "/navidog4MeetTrans/livesign.htm";
    }

    @Override
    protected String getMethod() {
        return "";
    }

    public void get (String commuid , String commlon,String commlat){
        HashMap<String, String> headers = (HashMap<String, String>) getHeader();
        headers.put("xieyi", "200");
        headers.put("commuid",commuid);
        headers.put("commlon",commlon);
        headers.put("commlat",commlat);
        get();
    }
}
