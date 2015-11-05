package cn.com.mobnote.golukmobile.promotion;

import java.util.HashMap;

import android.text.TextUtils;
import cn.com.mobnote.golukmobile.http.IRequestResultListener;
import cn.com.mobnote.golukmobile.http.request.GolukFastjasonRequest;

public class PromotionStatusRequest extends GolukFastjasonRequest<PromotionStatusModel> {

	public PromotionStatusRequest(int requestType, IRequestResultListener listener) {
		super(requestType, PromotionStatusModel.class, listener);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected String getPath() {
		// TODO Auto-generated method stub
		return "/navidog4MeetTrans/activity.htm";
	}

	@Override
	protected String getMethod() {
		// TODO Auto-generated method stub
		return "newstatus";
	}

	public void get(String location) {
		HashMap<String, String> headers = (HashMap<String, String>) getHeader();
		if (!TextUtils.isEmpty(location)) {
			headers.put("location", location);
		}
		get();
	}
}
