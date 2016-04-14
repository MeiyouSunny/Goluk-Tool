package com.mobnote.golukmain.usercenter;

import java.util.HashMap;

import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.http.request.GolukFastjsonRequest;
import com.mobnote.golukmain.usercenter.bean.HomeJson;

public class UserInfoRequest extends GolukFastjsonRequest<HomeJson> {

	public UserInfoRequest(int requestType, IRequestResultListener listener) {
		super(requestType, HomeJson.class, listener);
	}

	@Override
	protected String getPath() {
		
		return "/navidog4MeetTrans/myHomePage.htm";
	}

	@Override
	protected String getMethod() {

		return "userHome";
	}
	
	public void get(String otheruid, String operation, String commuid, String index) {
		HashMap<String, String> headers = (HashMap<String, String>) getHeader();
		headers.put("xieyi", "200");
		headers.put("otheruid", otheruid);
		headers.put("operation", operation);
		headers.put("commuid", commuid);
		headers.put("index", index);
		get();
	}

}
