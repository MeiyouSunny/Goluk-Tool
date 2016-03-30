package cn.com.mobnote.golukmobile.usercenter;

import java.util.HashMap;

import cn.com.mobnote.golukmobile.http.IRequestResultListener;
import cn.com.mobnote.golukmobile.http.request.GolukFastjsonRequest;
import cn.com.mobnote.golukmobile.usercenter.bean.VideoJson;

public class UserVideoListRequest extends GolukFastjsonRequest<VideoJson> {

	public UserVideoListRequest(int requestType, IRequestResultListener listener) {
		super(requestType, VideoJson.class, listener);
	}

	@Override
	protected String getPath() {

		return "/navidog4MeetTrans/myHomePage.htm";
	}

	@Override
	protected String getMethod() {

		return "userVideo";
	}

	public void get(String xieyi, String otheruid, String collection, String operation, String commuid, String index) {
		HashMap<String, String> headers = (HashMap<String, String>) getHeader();
		headers.put("xieyi", xieyi);
		headers.put("otheruid", otheruid);
		headers.put("collection", collection);
		headers.put("operation", operation);
		headers.put("commuid", commuid);
		headers.put("index", index);
		get();
	}

}
