package com.mobnote.golukmain.livevideo;

import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.follow.bean.FollowRetBean;
import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.http.request.GolukFastjsonRequest;

import java.util.HashMap;

/**
 * Created by leege100 on 2016/8/6.
 */
public class IsLiveRequest extends GolukFastjsonRequest<IsLiveRetBean> {
    public IsLiveRequest(int requestType, IRequestResultListener listener) {
        super(requestType, IsLiveRetBean.class, listener);
    }

    @Override
    protected String getPath() {
        return "/navidog4MeetTrans/isAlive.htm";
    }

    @Override
    protected String getMethod() {
        return null;
    }

    public void request(){
        if(!GolukApplication.getInstance().isUserLoginToServerSuccess()) {
            return;
        }
        if(GolukApplication.getInstance().getMyInfo() == null) {
            return;
        }
        HashMap<String, String> headers = (HashMap<String, String>) getHeader();
        headers.put("xieyi", "100");
        headers.put("uid", GolukApplication.getInstance().getMyInfo().uid);
        headers.put("commuid", GolukApplication.getInstance().getMyInfo().uid);
        get();
        return;
    }
}
