package com.mobnote.golukmain.following;

import java.util.HashMap;

import com.mobnote.golukmain.followed.bean.FollowedRetBean;
import com.mobnote.golukmain.following.bean.FollowingRetBean;
import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.http.request.GolukFastjsonRequest;

public class FollowingListRequest extends GolukFastjsonRequest<FollowingRetBean> {
	public FollowingListRequest(int requestType, IRequestResultListener listener) {
		super(requestType, FollowingRetBean.class, listener);
	}

	@Override
	protected String getPath() {
		return "/cdcGraph/link.htm";
	}

	@Override
	protected String getMethod() {
		return "following";
	}
	
	public void get (String xieyi,String linkuid,String operation,String index,String pagesize,String commuid){
		HashMap<String, String> headers = (HashMap<String, String>) getHeader();
		headers.put("xieyi", xieyi);
		headers.put("linkuid", linkuid);
		headers.put("operation", operation);
		headers.put("index", index);
		headers.put("pagesize", pagesize);
		headers.put("commuid", commuid);
		get();
	}
}
