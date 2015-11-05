package cn.com.mobnote.golukmobile.promotion;

import java.util.HashMap;
import java.util.Map;

import android.text.TextUtils;
import cn.com.mobnote.golukmobile.http.IRequestResultListener;
import cn.com.mobnote.golukmobile.http.request.GolukFastjasonRequest;

public class PromotionListRequest extends GolukFastjasonRequest<PromotionModel> {

	public PromotionListRequest(int requestType,
			IRequestResultListener listener) {
		super(requestType, PromotionModel.class, listener);
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
		return "list";
	}

	public void get(String location) {
		HashMap<String, String> headers = (HashMap<String, String>) getHeader();
		if (!TextUtils.isEmpty(location)) {
			headers.put("location", location);
		}
		get();
	}
}
