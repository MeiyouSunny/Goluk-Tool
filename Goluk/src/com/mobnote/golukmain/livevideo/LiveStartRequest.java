package com.mobnote.golukmain.livevideo;

import java.util.HashMap;

import org.json.JSONObject;

import cn.com.tiros.api.Tapi;

import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.http.request.GolukFastjsonRequest;
import com.mobnote.golukmain.live.LiveDataInfo;
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
			httpAddHeader("uid", userInfo.uid);
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
