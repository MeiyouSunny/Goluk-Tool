package com.mobnote.golukmain.userlogin;

import java.util.HashMap;

import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.cluster.bean.JsonData;
import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.http.request.GolukFastjsonRequest;

import android.text.TextUtils;
import cn.com.tiros.api.Tapi;

public class UpdUserNameBeanRequest extends GolukFastjsonRequest<UpNameResult> {

	public UpdUserNameBeanRequest(int requestType, IRequestResultListener listener) {
		super(requestType, UpNameResult.class, listener);
	}

	@Override
	protected String getPath() {
		return "/cdcRegister/modifyUserInfo.htm";
	}

	@Override
	protected String getMethod() {
		return "modifyNickName";
	}

	public void get(String uid,String phone,String nickname) {
		HashMap<String, String> headers = (HashMap<String, String>) getHeader();
		headers.put("commuid", uid);
		headers.put("tag", "android");
		headers.put("mid", "" + Tapi.getMobileId());
		headers.put("method", "userCancel");
		headers.put("nickname", nickname);
		headers.put("phone", phone);
		get();
	}
	
}
