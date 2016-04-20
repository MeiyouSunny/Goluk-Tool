package com.mobnote.golukmain.userlogin;

import java.util.HashMap;

import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.cluster.bean.JsonData;
import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.http.request.GolukFastjsonRequest;

import android.text.TextUtils;
import cn.com.tiros.api.Tapi;

public class UserloginBeanRequest extends GolukFastjsonRequest<UserResult> {

	public UserloginBeanRequest(int requestType, IRequestResultListener listener) {
		super(requestType, UserResult.class, listener);
	}

	@Override
	protected String getPath() {
		return "/cdcRegister/getLogin.htm";
	}

	@Override
	protected String getMethod() {
		return "getLogin";
	}

	public void get(String phone,String pwd) {
		HashMap<String, String> headers = (HashMap<String, String>) getHeader();
		headers.put("phone", phone);
		headers.put("pwd", pwd);
		headers.put("tag", "android");
		headers.put("mid", "" + Tapi.getMobileId());
		get();
	}
}
