package com.mobnote.golukmain.userlogin;

import java.util.HashMap;

import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.http.request.GolukFastjsonRequest;
import cn.com.tiros.api.Tapi;

public class OtherUserloginBeanRequest extends GolukFastjsonRequest<UserResult> {

	public OtherUserloginBeanRequest(int requestType, IRequestResultListener listener) {
		super(requestType, UserResult.class, listener);
	}

	@Override
	protected String getPath() {
		return "/cdcRegister/oauthLogin.htm";
	}

	@Override
	protected String getMethod() {
		return "oauthLogin";
	}

	
	public void get(HashMap<String, String> other) {
		HashMap<String, String> headers = (HashMap<String, String>) getHeader();
		headers.putAll(other);
		headers.put("mid", Tapi.getMobileId());
		headers.put("xieyi", "100");
		super.post();
	}
}
