package com.mobnote.golukmain.profit;

import java.util.HashMap;

import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.http.request.GolukFastjsonRequest;

public class ProfitDetailRequest extends GolukFastjsonRequest<ProfitDetailInfo> {

	public ProfitDetailRequest(int requestType, IRequestResultListener listener) {
		super(requestType, ProfitDetailInfo.class, listener);
	}

	@Override
	protected String getPath() {
		
		return "/navidog4MeetTrans/myHomePage.htm";
	}

	@Override
	protected String getMethod() {
		
		return "myincomeview";
	}
	
	public void get(String uid,String operation,String timestamp,String pagesize,String xieyi) {
		HashMap<String, String> headers = (HashMap<String, String>) getHeader();
		headers.put("uid", uid);
		headers.put("operation", operation);
		headers.put("timestamp", timestamp);
		headers.put("pagesize", pagesize);
		headers.put("xieyi", xieyi);
		get();
	}

}
