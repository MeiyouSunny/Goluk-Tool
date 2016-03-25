package cn.com.mobnote.golukmobile.followed;

import java.util.HashMap;

import cn.com.mobnote.golukmobile.followed.bean.FollowRetBean;
import cn.com.mobnote.golukmobile.http.IRequestResultListener;
import cn.com.mobnote.golukmobile.http.request.GolukFastjsonRequest;

public class FollowedListRequest extends GolukFastjsonRequest<FollowRetBean> {
	public FollowedListRequest(int requestType, IRequestResultListener listener) {
		super(requestType, FollowRetBean.class, listener);
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
