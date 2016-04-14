package com.mobnote.golukmain.usercenter;

import java.util.HashMap;

import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.http.request.GolukFastjsonRequest;
import com.mobnote.golukmain.usercenter.bean.AttentionJson;

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
