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
        if(!GolukApplication.getInstance().isUserLoginToServerSuccess()) {
            return null;
        }
        if(GolukApplication.getInstance().getMyInfo() == null) {
            return null;
        }
        HashMap<String, String> headers = (HashMap<String, String>) getHeader();
        headers.put("xieyi", "200");
        headers.put("commuid", GolukApplication.getInstance().getMyInfo().uid);
        get();
        return null;
    }
}
