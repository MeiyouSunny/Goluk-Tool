package com.mobnote.golukmain.videodetail;

import java.util.HashMap;

import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.http.request.GolukFastjsonRequest;

import android.text.TextUtils;

public class SingleVideoRequest extends GolukFastjsonRequest<VideoJson> {

	public SingleVideoRequest(int requestType, IRequestResultListener listener) {
		super(requestType, VideoJson.class, listener);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected String getPath() {
		// TODO Auto-generated method stub
		return "/navidog4MeetTrans/boutique.htm";
	}

	@Override
	protected String getMethod() {
		// TODO Auto-generated method stub
		return "singleVideo";
	}

	public boolean get(String ztid) {
		if (TextUtils.isEmpty(ztid)) {
			return false;
		}
		HashMap<String, String> headers = (HashMap<String, String>) getHeader();
		headers.put("xieyi", ""+100);
		headers.put("ztid", ztid);
		super.get();
		return true;
	}
}
