package com.mobnote.golukmain.livevideo;

import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.http.request.GolukFastjsonRequest;
import com.mobnote.golukmain.livevideo.bean.GetPositionRetBean;

import java.util.HashMap;

/**
 * Created by leege100 on 2016/8/12.
 */
public class GetPositionRequest extends GolukFastjsonRequest<GetPositionRetBean> {
    public GetPositionRequest(int requestType, IRequestResultListener listener) {
        super(requestType, GetPositionRetBean.class, listener);
    }

    @Override
    protected String getPath() {
        return "/navidog4MeetTrans/getNils.htm";
    }

    @Override
    protected String getMethod() {
        return null;
    }

    public void request(){

        HashMap<String, String> headers = (HashMap<String, String>) getHeader();
//        headers.put("xieyi", "100");
//        headers.put("uid", GolukApplication.getInstance().getMyInfo().uid);
//        headers.put("commuid", GolukApplication.getInstance().getMyInfo().uid);
        get();
        return;
    }
}
