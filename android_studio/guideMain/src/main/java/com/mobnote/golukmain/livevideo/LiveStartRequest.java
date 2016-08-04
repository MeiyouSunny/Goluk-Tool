package com.mobnote.golukmain.livevideo;

import android.text.TextUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;

import org.json.JSONObject;

import cn.com.tiros.api.Tapi;

import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.http.request.GolukFastjsonRequest;
import com.mobnote.golukmain.live.LiveDataInfo;
import com.mobnote.golukmain.live.LiveSettingBean;
import com.mobnote.golukmain.live.UserInfo;
import com.mobnote.map.LngLat;

public class LiveStartRequest extends GolukFastjsonRequest<LiveDataInfo> {

    public LiveStartRequest(int requestType, IRequestResultListener listener) {
        super(requestType, LiveDataInfo.class, listener);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected String getPath() {
        return "/navidog4MeetTrans/activePlay.htm";
    }

    @Override
    protected String getMethod() {
        return null;
    }

    public boolean get(String vid ,LiveSettingBean bean){
        if(bean == null){
            return false;
        }
        String talk = "0";
        String desc = null;
        String flux = "";
        String vTypeStr = "";
        String shortLocation = "";
        if (bean != null) {
            talk = bean.isCanTalk ? "1" : "0";
                try {
                    if(!TextUtils.isEmpty(bean.shortLocation)){
                        shortLocation =  URLEncoder.encode(bean.shortLocation, "utf-8");
                    }
                    if(!TextUtils.isEmpty(bean.desc)) {
                        desc = URLEncoder.encode(bean.desc, "utf-8");
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            flux = null != bean.netCountStr ? "" + bean.netCountStr : "";
            vTypeStr = null != bean ? "" + bean.vtype : "";
        }

        httpAddHeader("xieyi", "200");
        httpAddHeader("active", "1");
        httpAddHeader("talk", talk);
        httpAddHeader("tag", "android");
        httpAddHeader("part", "phone");
        httpAddHeader("vid", vid);
        httpAddHeader("desc", desc);
        httpAddHeader("restime", bean.duration + "");
        httpAddHeader("vtype", vTypeStr);
        httpAddHeader("flux",flux);
        httpAddHeader("voice",bean.isEnableVoice ? "1" : "0");

        UserInfo userInfo = GolukApplication.getInstance().getMyInfo();
        httpAddHeader("mid", Tapi.getMobileId());
        httpAddHeader("commuid", userInfo.uid);
        httpAddHeader("aid", userInfo.aid);

        httpAddHeader("location", shortLocation);
        httpAddHeader("commlon", String.valueOf(bean.lon));
        httpAddHeader("commlat",  String.valueOf(bean.lat));
        httpAddHeader("speed", "" + 10);
        httpAddHeader("devicetag",GolukApplication.getInstance().mIPCControlManager.mProduceName);
        super.post();
        return true;
    }
    public boolean get(String json) {
        try {
            JSONObject jsonObj = new JSONObject(json);

            String active = jsonObj.optString("active");
            String talk = jsonObj.optString("talk");
            String tag = jsonObj.optString("tag");
            String vid = jsonObj.optString("vid");
            String desc = jsonObj.optString("desc");
            String restime = jsonObj.optString("restime");
            String vtype = jsonObj.optString("vtype");
            String flux = jsonObj.optString("flux");
            String voice = jsonObj.optString("voice");

            httpAddHeader("xieyi", "200");
            httpAddHeader("active", active);
            httpAddHeader("talk", talk);
            httpAddHeader("tag", tag);
            httpAddHeader("part", "phone");
            httpAddHeader("vid", vid);

            httpAddHeader("desc", desc);
            httpAddHeader("restime", restime);
            httpAddHeader("vtype", vtype);
            httpAddHeader("flux", flux);
            httpAddHeader("voice", voice);

            UserInfo userInfo = GolukApplication.getInstance().getMyInfo();
            httpAddHeader("mid", Tapi.getMobileId());
            httpAddHeader("commuid", userInfo.uid);
            httpAddHeader("aid", userInfo.aid);

            httpAddHeader("lon", "" + LngLat.lng);
            httpAddHeader("lat", "" + LngLat.lat);
            httpAddHeader("speed", "" + 10);

        } catch (Exception e) {

        }
        super.post();
        return true;
    }

    private void httpAddHeader(String key, String value) {
        HashMap<String, String> paramters = (HashMap<String, String>) this.getHeader();
        paramters.put(key, value);
    }

}
