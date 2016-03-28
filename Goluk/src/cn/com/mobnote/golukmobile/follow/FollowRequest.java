package cn.com.mobnote.golukmobile.follow;

import java.util.HashMap;

import cn.com.mobnote.golukmobile.follow.bean.FollowRetBean;
import cn.com.mobnote.golukmobile.http.IRequestResultListener;
import cn.com.mobnote.golukmobile.http.request.GolukFastjsonRequest;

public class FollowRequest extends GolukFastjsonRequest<FollowRetBean> {
	public FollowRequest(int requestType, IRequestResultListener listener) {
		super(requestType, FollowRetBean.class, listener);
	}

	@Override
	protected String getPath() {
		return "/cdcGraph/link.htm";
	}

	@Override
	protected String getMethod() {
		return "follow";
	}

	public void get(String xieyi, String linkuid, String type, String commuid) {
		HashMap<String, String> headers = (HashMap<String, String>) getHeader();
		headers.put("xieyi", xieyi);
		headers.put("commuid", commuid);
		headers.put("linkuid", linkuid);
		headers.put("type", type);
		get();
	}
}