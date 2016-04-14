package com.mobnote.golukmain.praised;

import java.util.HashMap;

import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.http.request.GolukFastjsonRequest;
import com.mobnote.golukmain.praised.bean.MyPraisedListBean;

import android.text.TextUtils;

public class MyPraisedListRequest extends GolukFastjsonRequest<MyPraisedListBean> {

	public MyPraisedListRequest(int requestType, IRequestResultListener listener) {
		super(requestType, MyPraisedListBean.class, listener);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected String getPath() {
		// TODO Auto-generated method stub
		return "/navidog4MeetTrans/shareVideo.htm";
	}

	@Override
	protected String getMethod() {
		// TODO Auto-generated method stub
		return "praisedVideoList";
	}

	public boolean get(String uid, String operation, String pageSize, String timeStamp) {
		if (TextUtils.isEmpty(uid)) {
			return false;
		}
		HashMap<String, String> paramters = (HashMap<String, String>) getParam();
		paramters.put("xieyi", "100");
		paramters.put("commuid", uid);
		paramters.put("operation", operation);
		paramters.put("pagesize", pageSize);
		paramters.put("timestamp", timeStamp);
		super.get();
		return true;
	}
}
