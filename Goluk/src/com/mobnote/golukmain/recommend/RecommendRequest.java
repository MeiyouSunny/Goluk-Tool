package com.mobnote.golukmain.recommend;

import java.util.HashMap;

import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.http.request.GolukFastjsonRequest;
import com.mobnote.golukmain.recommend.bean.RecommendRetBean;

public class RecommendRequest extends GolukFastjsonRequest<RecommendRetBean> {
	public RecommendRequest(int requestType, IRequestResultListener listener) {
		super(requestType, RecommendRetBean.class, listener);
	}

	@Override
	protected String getPath() {
		return "/cdcSearch/search.htm";
	}

	@Override
	protected String getMethod() {
		return "recommendUser";
	}
	
	public void get (String xieyi,String commuid){
		HashMap<String, String> headers = (HashMap<String, String>) getHeader();
		headers.put("xieyi", xieyi);
		headers.put("commuid", commuid);
		get();
	}
}
