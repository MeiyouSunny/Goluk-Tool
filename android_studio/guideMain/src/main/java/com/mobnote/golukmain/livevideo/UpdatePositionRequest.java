package com.mobnote.golukmain.livevideo;

import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.http.request.GolukFastjsonRequest;
import com.mobnote.golukmain.livevideo.bean.PositionRetBean;

import java.util.HashMap;

/**
 * 上报位置信息
 * Created by leege100 on 2016/8/4.
 */
public class UpdatePositionRequest extends GolukFastjsonRequest<PositionRetBean> {
    public UpdatePositionRequest(int requestType, IRequestResultListener listener) {
        super(requestType, PositionRetBean.class, listener);
    }

    @Override
    protected String getPath() {
        return "/navidog4MeetTrans/sendPosit.htm";
    }

    @Override
    protected String getMethod() {
        return null;
    }

    public void get(String commlon, String commlat) {
        if(GolukApplication.getInstance().isUserLoginToServerSuccess() && GolukApplication.getInstance().getMyInfo() != null){
            HashMap<String, String> headers = (HashMap<String, String>) getHeader();
            headers.put("xieyi", "200");
            headers.put("commuid", GolukApplication.getInstance().getMyInfo().uid);
            headers.put("commlon", commlon);
            headers.put("commlat", commlat);
            headers.put("active","1");
            headers.put("vs","2");
            headers.put("tag","android");
            headers.put("speed","0");
        }
        get();
    }
}

