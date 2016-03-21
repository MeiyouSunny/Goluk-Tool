package cn.com.mobnote.golukmobile.usercenter;

import java.util.HashMap;

import cn.com.mobnote.golukmobile.http.IRequestResultListener;
import cn.com.mobnote.golukmobile.http.request.GolukFastjsonRequest;
import cn.com.mobnote.golukmobile.usercenter.bean.HomeJson;

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
	
	public void get(String xieyi, String otheruid, String operation, String commuid, String index) {
		HashMap<String, String> headers = (HashMap<String, String>) getHeader();
		headers.put("xieyi", xieyi);
		headers.put("otheruid", otheruid);
		headers.put("operation", operation);
		headers.put("commuid", commuid);
		headers.put("index", index);
		get();
	}

}
