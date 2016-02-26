package cn.com.mobnote.golukmobile.praised;

import java.util.HashMap;

import android.text.TextUtils;
import cn.com.mobnote.golukmobile.http.IRequestResultListener;
import cn.com.mobnote.golukmobile.http.request.GolukFastjsonRequest;
import cn.com.mobnote.golukmobile.praised.bean.MyPraisedListBean;

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
