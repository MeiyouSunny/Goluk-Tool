package com.mobnote.golukmain.bean;

import java.util.HashMap;

import cn.com.tiros.api.Tapi;

import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.http.request.GolukFastjsonRequest;

public class GetPushSettingRequest extends GolukFastjsonRequest<PushMsgSettingBean> {

	public GetPushSettingRequest(int requestType, IRequestResultListener listener) {
		super(requestType, PushMsgSettingBean.class, listener);
	}

	@Override
	protected String getPath() {
		return "/cdcRegister/pushService.htm";
	}

	@Override
	protected String getMethod() {
		return "getPushConfig";
	}
	
	public void get(String uid) {
		HashMap<String, String> headers = (HashMap<String, String>) getHeader();
		headers.put("uid", uid);
		headers.put("tag", "android");
		headers.put("mid", "" + Tapi.getMobileId());
		headers.put("method", "getPushConfig");
		get();
	}

}
