package com.mobnote.golukmain.followed;

import java.util.HashMap;

import com.mobnote.golukmain.followed.bean.FollowedRetBean;
import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.http.request.GolukFastjsonRequest;

public class FollowedListRequest extends GolukFastjsonRequest<FollowedRetBean> {
	public FollowedListRequest(int requestType, IRequestResultListener listener) {
		super(requestType, FollowedRetBean.class, listener);
	}

	@Override
	protected String getPath() {
		return "/navidog4MeetTrans/shareVideo.htm";
	}

	@Override
	protected String getMethod() {
		return "followingUserVideo";
	}

	public void get(String xieyi, String uid, String pageSize, String operation, String timestamp) {
		HashMap<String, String> headers = (HashMap<String, String>) getHeader();
		headers.put("xieyi", xieyi);
		headers.put("commuid", uid);
		headers.put("pagesize", pageSize);
		headers.put("operation", operation);
		headers.put("timestamp", timestamp);
		get();
	}
}
