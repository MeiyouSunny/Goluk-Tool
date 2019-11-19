package com.mobnote.golukmain.startshare;

import java.util.HashMap;

import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.http.request.GolukFastjsonRequest;
import com.mobnote.golukmain.startshare.bean.VideoSaveRetBean;
import com.mobnote.map.LngLat;

import android.text.TextUtils;

public class GetVideoSaveAddressRequest extends GolukFastjsonRequest<VideoSaveRetBean> {

    public GetVideoSaveAddressRequest(int requestType, IRequestResultListener listener) {
        super(requestType, VideoSaveRetBean.class, listener);
    }

    @Override
    protected String getPath() {
        return "/navidog4MeetTrans/video.htm";
    }

    @Override
    protected String getMethod() {
        return null;
    }

    public void get(String videoid, String type, String describe, String attribute,
                    String issquare, String creattime, String signtime, String activityid,
                    String location, String devicetag, String tagId, String gps,
                    String resolution, String checkCode,String photoUrl,String videoUrl) {
        HashMap<String, String> params = (HashMap<String, String>) getParam();
        params.put("xieyi", "" + 100);
        params.put("videoid", videoid);
        params.put("photourl", photoUrl);
        params.put("videourl", videoUrl);
        params.put("type", type);
        params.put("describe", describe);
        params.put("attribute", attribute);
        params.put("issquare", issquare);
        params.put("creattime", creattime);
        params.put("signtime", signtime);
        params.put("lat", "" + LngLat.lat);
        params.put("lon", "" + LngLat.lng);
        String uid = GolukApplication.getInstance().mCurrentUId;
        if (TextUtils.isEmpty(uid)) {
            params.put("uid", "");
        } else {
            params.put("uid", uid);
        }

        if (!TextUtils.isEmpty(location)) {
            params.put("location", location);
        } else {
            params.put("location", "");
        }

        if (!TextUtils.isEmpty(activityid)) {
            params.put("activityid", activityid);
        }

        if (devicetag != null) {
            params.put("devicetag", devicetag);
        }

        if(!TextUtils.isEmpty(resolution)) {
            params.put("resolution", resolution);
        }

        if(!TextUtils.isEmpty(gps)) {
            params.put("gps", gps);
        }

        if(!TextUtils.isEmpty(checkCode)) {
            params.put("checkcode", checkCode);
        }

        if(!TextUtils.isEmpty(tagId)) {
            params.put("tagid", tagId);
        }

        super.post();
    }
}
