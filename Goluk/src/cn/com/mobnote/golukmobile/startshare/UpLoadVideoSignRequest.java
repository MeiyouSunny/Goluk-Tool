package cn.com.mobnote.golukmobile.startshare;

import java.util.HashMap;

import android.text.TextUtils;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.helper.QCloudHelper;
import cn.com.mobnote.golukmobile.http.IRequestResultListener;
import cn.com.mobnote.golukmobile.http.request.GolukFastjsonRequest;
import cn.com.mobnote.golukmobile.startshare.bean.SignBean;

public class UpLoadVideoSignRequest extends GolukFastjsonRequest<SignBean> {

	public UpLoadVideoSignRequest(int requestType, IRequestResultListener listener) {
		super(requestType, SignBean.class, listener);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected String getPath() {
		// TODO Auto-generated method stub
		return "/navidog4MeetTrans/videosign.htm";
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
