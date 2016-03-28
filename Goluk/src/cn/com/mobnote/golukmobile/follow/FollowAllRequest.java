package cn.com.mobnote.golukmobile.follow;

import java.util.HashMap;

import cn.com.mobnote.golukmobile.follow.bean.FollowAllRetBean;
import cn.com.mobnote.golukmobile.http.IRequestResultListener;
import cn.com.mobnote.golukmobile.http.request.GolukFastjsonRequest;

public class FollowAllRequest extends GolukFastjsonRequest<FollowAllRetBean> {
	public FollowAllRequest(int requestType, IRequestResultListener listener) {
		super(requestType, FollowAllRetBean.class, listener);
	}

	@Override
	protected String getPath() {
		return "/cdcGraph/link.htm";
	}

	@Override
	protected String getMethod() {
		return "followAll";
	}

	public void get(String xieyi, String linkuid, String commuid) {
		HashMap<String, String> headers = (HashMap<String, String>) getHeader();
		headers.put("xieyi", xieyi);
		headers.put("commuid", commuid);
		headers.put("linkuid", linkuid);
		get();
	}
}
