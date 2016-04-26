package com.mobnote.golukmain.userlogin;

import java.util.HashMap;

import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.http.request.GolukFastjsonRequest;
import cn.com.tiros.api.Tapi;

public class UpdUserHeadBeanRequest extends GolukFastjsonRequest<UpHeadResult> {

	public UpdUserHeadBeanRequest(int requestType, IRequestResultListener listener) {
		super(requestType, UpHeadResult.class, listener);
	}

	@Override
	protected String getPath() {
		return "/cdcRegister/modifyUserInfo.htm";
	}

	@Override
	protected String getMethod() {
		return "modifyHead";
	}

	public void get(String uid,String phone,String channel,String urlhead,String head) {
		HashMap<String, String> headers = (HashMap<String, String>) getHeader();
		headers.put("commuid", uid);
		headers.put("tag", "android");
		headers.put("mid", "" + Tapi.getMobileId());
		headers.put("method", "modifyHead");
		headers.put("channel", channel);
		headers.put("urlhead", urlhead);
		headers.put("head", head);
		headers.put("phone", phone);
		get();
	}
	
}
