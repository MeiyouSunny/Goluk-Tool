package com.mobnote.golukmain.helper;

import java.util.HashMap;

import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.helper.bean.SignBean;
import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.http.request.GolukFastjsonRequest;

import android.text.TextUtils;

public class UpLoadVideoSignRequest extends GolukFastjsonRequest<SignBean> {

	public UpLoadVideoSignRequest(int requestType, IRequestResultListener listener) {
		super(requestType, SignBean.class, listener);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected String getPath() {
		// TODO Auto-generated method stub
		return "/navidog4MeetTrans/videosignv2.htm";
	}

	@Override
	protected String getMethod() {
		// TODO Auto-generated method stub
		return null;
	}

	public void get() {
		HashMap<String, String> headers = (HashMap<String, String>) getHeader();
		headers.put("appid", QCloudHelper.APPID);
		headers.put("xieyi", ""+100);
		String uid = GolukApplication.getInstance().mCurrentUId;
		if (TextUtils.isEmpty(uid)) {
			headers.put("uid", "");
		} else {
			headers.put("uid", uid);
		}
		super.get();
	}
}
