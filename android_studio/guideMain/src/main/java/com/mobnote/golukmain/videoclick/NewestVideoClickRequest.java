package com.mobnote.golukmain.videoclick;

import java.util.HashMap;

import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.http.request.GolukFastjsonRequest;

import android.text.TextUtils;
import cn.com.tiros.api.Tapi;

public class NewestVideoClickRequest extends GolukFastjsonRequest<VideoClickInfo> {

	public NewestVideoClickRequest(int requestType, IRequestResultListener listener) {
		super(requestType, VideoClickInfo.class, listener);
	}

	@Override
	protected String getPath() {

		return "/navidog4MeetTrans/shareVideo.htm";
	}

	@Override
	protected String getMethod() {

		return "shareVideoClick";
	}

	public void get(String xieyi, String channel, String videolist) {
		HashMap<String, String> headers = (HashMap<String, String>) getHeader();
		headers.put("xieyi", xieyi);
		headers.put("channel", channel);
		
		String uid = GolukApplication.getInstance().mCurrentUId;
		if (TextUtils.isEmpty(uid)) {
			headers.put("uid", "");
		} else {
			headers.put("uid", uid);
		}
		headers.put("mobileid",  Tapi.getMobileId());
		headers.put("videolist", videolist);
		get();
	}

}
