package com.mobnote.golukmain.userlogin;

import java.util.HashMap;

import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.cluster.bean.JsonData;
import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.http.request.GolukFastjsonRequest;

import android.text.TextUtils;
import cn.com.tiros.api.Tapi;

public class UpdUserDescBeanRequest extends GolukFastjsonRequest<UpDescResult> {

	public UpdUserDescBeanRequest(int requestType, IRequestResultListener listener) {
		super(requestType, UpDescResult.class, listener);
	}

	@Override
	protected String getPath() {
		return "/cdcRegister/modifyUserInfo.htm";
	}

	@Override
	protected String getMethod() {
		return "modifyDesc";
	}

	public void get(String uid,String phone,String desc) {
		HashMap<String, String> headers = (HashMap<String, String>) getHeader();
		headers.put("commuid", uid);
		headers.put("tag", "android");
		headers.put("mid", "" + Tapi.getMobileId());
		headers.put("method", "userCancel");
		headers.put("desc", desc);
		headers.put("phone", phone);
		get();
	}
	
}
