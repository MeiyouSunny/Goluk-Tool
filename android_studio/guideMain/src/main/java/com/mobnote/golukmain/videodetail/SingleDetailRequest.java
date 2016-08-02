package com.mobnote.golukmain.videodetail;

import java.util.HashMap;




import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.http.request.GolukFastjsonRequest;

import android.text.TextUtils;

public class SingleDetailRequest extends GolukFastjsonRequest<VideoDetailRetBean> {

	public SingleDetailRequest(int requestType, IRequestResultListener listener) {
		super(requestType, VideoDetailRetBean.class, listener);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected String getPath() {
		// TODO Auto-generated method stub
		return "/navidog4MeetTrans/myHomePage.htm";
	}

	@Override
	protected String getMethod() {
		// TODO Auto-generated method stub
		return "singleDetails";
	}

	public boolean get(String videoid) {
		if (TextUtils.isEmpty(videoid)) {
			return false;
		}
		HashMap<String, String> headers = (HashMap<String, String>) getHeader();
		headers.put("xieyi", ""+100);
		headers.put("videoid", videoid);
		super.get();
		return true;
	}
}
