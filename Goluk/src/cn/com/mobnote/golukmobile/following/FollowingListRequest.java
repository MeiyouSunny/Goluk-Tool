package cn.com.mobnote.golukmobile.following;

import java.util.HashMap;

import cn.com.mobnote.golukmobile.followed.bean.FollowedRetBean;
import cn.com.mobnote.golukmobile.http.IRequestResultListener;
import cn.com.mobnote.golukmobile.http.request.GolukFastjsonRequest;

public class FollowingListRequest extends GolukFastjsonRequest<FollowedRetBean> {
	public FollowingListRequest(int requestType, IRequestResultListener listener) {
		super(requestType, FollowedRetBean.class, listener);
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
