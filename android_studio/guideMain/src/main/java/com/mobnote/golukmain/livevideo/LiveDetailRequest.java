package com.mobnote.golukmain.livevideo;

import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.http.request.GolukFastjsonRequest;
import com.mobnote.golukmain.live.LiveDataInfo;

import java.util.HashMap;

/**
 * Created by leege100 on 16/7/13.
 */
public class LiveDetailRequest extends GolukFastjsonRequest<LiveDataInfo> {

    public LiveDetailRequest(int requestType, IRequestResultListener listener) {
        super(requestType, LiveDataInfo.class, listener);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected String getPath() {
        return "/navidog4MeetTrans/reqPlay.htm";
    }

    @Override
    protected String getMethod() {
        return null;
    }

    public void get (String zuid,String zaid){
        HashMap<String, String> headers = (HashMap<String, String>) getHeader();
        headers.put("xieyi", "100");
        headers.put("part", "phone");
        headers.put("zaid", zaid);
        headers.put("zuid", zuid);
        if(GolukApplication.getInstance().isUserLoginToServerSuccess() && GolukApplication.getInstance().getMyInfo() != null){
            headers.put("uid", GolukApplication.getInstance().getMyInfo().uid);
            headers.put("aid", GolukApplication.getInstance().getMyInfo().aid);
        }
        get();
    }
}
