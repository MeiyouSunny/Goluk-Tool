package cn.com.mobnote.golukmobile.fan;
import java.util.HashMap;

import cn.com.mobnote.golukmobile.followed.bean.FollowedRetBean;
import cn.com.mobnote.golukmobile.following.bean.FollowingRetBean;
import cn.com.mobnote.golukmobile.http.IRequestResultListener;
import cn.com.mobnote.golukmobile.http.request.GolukFastjsonRequest;

public class FansListRequest extends GolukFastjsonRequest<FollowingRetBean> {
	public FansListRequest(int requestType, IRequestResultListener listener) {
		super(requestType, FollowingRetBean.class, listener);
	}

	@Override
	protected String getPath() {
		return "/cdcGraph/link.htm";
	}

	@Override
	protected String getMethod() {
		return "fans";
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