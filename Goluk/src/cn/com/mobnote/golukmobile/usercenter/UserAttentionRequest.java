package cn.com.mobnote.golukmobile.usercenter;

import java.util.HashMap;

import cn.com.mobnote.golukmobile.http.IRequestResultListener;
import cn.com.mobnote.golukmobile.http.request.GolukFastjsonRequest;
import cn.com.mobnote.golukmobile.usercenter.bean.AttentionJson;

public class UserAttentionRequest extends GolukFastjsonRequest<AttentionJson> {

	public UserAttentionRequest(int requestType, IRequestResultListener listener) {
		super(requestType, AttentionJson.class, listener);
	}

	@Override
	protected String getPath() {

		return "/cdcGraph/link.htm";
	}

	@Override
	protected String getMethod() {

		return "follow";
	}

	public void get(String linkuid, String type, String commuid) {
		HashMap<String, String> headers = (HashMap<String, String>) getHeader();
		headers.put("xieyi", "200");
		headers.put("linkuid", linkuid);
		headers.put("type", type);
		headers.put("commuid", commuid);
		get();
	}

}
