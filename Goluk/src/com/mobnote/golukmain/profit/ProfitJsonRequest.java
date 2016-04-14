package com.mobnote.golukmain.profit;

import java.util.HashMap;

import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.http.request.GolukFastjsonRequest;

public class ProfitJsonRequest extends GolukFastjsonRequest<ProfitInfo> {

	public ProfitJsonRequest(int requestType, IRequestResultListener listener) {
		super(requestType, ProfitInfo.class, listener);
	}

	@Override
	protected String getPath() {
		
		return "/navidog4MeetTrans/myHomePage.htm";
	}

	@Override
	protected String getMethod() {
		
		return "myincome";
	}
	
	public void get(String uid,String xieyi) {
		HashMap<String, String> headers = (HashMap<String, String>) getHeader();
		headers.put("uid", uid);
		headers.put("xieyi", xieyi);
		get();
	}

}
