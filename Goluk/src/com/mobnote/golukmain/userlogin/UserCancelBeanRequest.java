package com.mobnote.golukmain.userlogin;

import java.util.HashMap;

import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.cluster.bean.JsonData;
import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.http.request.GolukFastjsonRequest;

import android.text.TextUtils;
import cn.com.tiros.api.Tapi;

public class UserCancelBeanRequest extends GolukFastjsonRequest<CancelResult> {

	public UserCancelBeanRequest(int requestType, IRequestResultListener listener) {
		super(requestType, CancelResult.class, listener);
	}

	@Override
	protected String getPath() {
		return "/cdcRegister/pushService.htm";
	}

	@Override
	protected String getMethod() {
		return "userCancel";
	}

	public void get(String uid) {
		HashMap<String, String> headers = (HashMap<String, String>) getHeader();
		headers.put("uid", uid);
		headers.put("tag", "android");
		headers.put("mid", "" + Tapi.getMobileId());
		headers.put("method", "userCancel");
		get();
	}
	
}
