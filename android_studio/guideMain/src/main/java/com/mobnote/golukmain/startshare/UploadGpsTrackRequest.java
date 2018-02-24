package com.mobnote.golukmain.startshare;

import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.http.request.GolukFastjsonRequest;
import com.mobnote.golukmain.startshare.bean.GpsTrackParamBean;
import com.mobnote.golukmain.startshare.bean.UploadGpsTrackRetBean;

import java.util.HashMap;

import likly.dollar.$;

public class UploadGpsTrackRequest extends GolukFastjsonRequest<UploadGpsTrackRetBean> {

    public UploadGpsTrackRequest(int requestType, IRequestResultListener listener) {
        super(requestType, UploadGpsTrackRetBean.class, listener);
    }

    @Override
    protected String getPath() {
        return "/navidog4MeetTrans/videogps.htm";
    }

    @Override
    protected String getMethod() {
        return null;
    }

    public void request(GpsTrackParamBean param) {
        if (param == null)
            return;
        HashMap<String, String> params = (HashMap<String, String>) getParam();
        params.put("xieyi", "" + 100);

        // 请求参数整体转为Json放入body
        String uid = GolukApplication.getInstance().mCurrentUId;
        param.uid = uid;
        String requestBody = $.json().toJson(param);
        setRequestBody(requestBody);

        getHeader().put("Content-Type", "application/json;charset=UTF-8");

        super.post();
    }

}
