package cn.com.mobnote.golukmobile.profit;

import java.util.HashMap;

import cn.com.mobnote.golukmobile.http.IRequestResultListener;
import cn.com.mobnote.golukmobile.http.request.GolukFastjasonRequest;

public class ProfitJsonRequest extends GolukFastjasonRequest<ProfitInfo> {

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
