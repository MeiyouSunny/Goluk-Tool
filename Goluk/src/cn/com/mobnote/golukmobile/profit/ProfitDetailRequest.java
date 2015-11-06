package cn.com.mobnote.golukmobile.profit;

import java.util.HashMap;

import cn.com.mobnote.golukmobile.http.IRequestResultListener;
import cn.com.mobnote.golukmobile.http.request.GolukFastjasonRequest;

public class ProfitDetailRequest extends GolukFastjasonRequest<ProfitDetailInfo> {

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
