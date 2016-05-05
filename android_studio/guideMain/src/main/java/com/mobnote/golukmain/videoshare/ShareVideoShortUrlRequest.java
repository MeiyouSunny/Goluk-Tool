package com.mobnote.golukmain.videoshare;

import java.util.HashMap;

import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.http.request.GolukFastjsonRequest;
import com.mobnote.golukmain.videoshare.bean.VideoShareRetBean;

import android.text.TextUtils;
import cn.com.tiros.api.Tapi;

public class ShareVideoShortUrlRequest extends GolukFastjsonRequest<VideoShareRetBean> {

	public ShareVideoShortUrlRequest(int requestType, IRequestResultListener listener) {
		super(requestType, VideoShareRetBean.class, listener);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected String getPath() {
		// TODO Auto-generated method stub
		return "/navidog4MeetTrans/shareVideo.htm";
	}

	@Override
	protected String getMethod() {
		// TODO Auto-generated method stub
		return "shareVideoShortUrl";
	}

	public boolean get(String videoId, String type) {
		if (TextUtils.isEmpty(videoId) || TextUtils.isEmpty(type)) {
			return false;
		}
		HashMap<String, String> paramters = (HashMap<String, String>) getParam();
		paramters.put("xieyi", "100");
		paramters.put("videoid", videoId);
		paramters.put("type", type);
		String uid = GolukApplication.getInstance().mCurrentUId;
		if (TextUtils.isEmpty(uid)) {
			paramters.put("uid", "");
		} else {
			paramters.put("uid", uid);
		}
		paramters.put("mobileid",  Tapi.getMobileId());
		super.get();
		return true;
	}
}
