package cn.com.mobnote.golukmobile.promotion;

import java.util.HashMap;
import java.util.Map;

import android.text.TextUtils;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.http.IRequestResultListener;
import cn.com.mobnote.golukmobile.http.request.GolukFastjsonRequest;
import cn.com.mobnote.map.LngLat;
import cn.com.mobnote.util.SharedPrefUtil;

public class PromotionListRequest extends GolukFastjsonRequest<PromotionModel> {

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

	public void get() {
		HashMap<String, String> headers = (HashMap<String, String>) getHeader();
		headers.put("lat", "" + LngLat.lat);
		headers.put("lon", "" + LngLat.lng);
		String uid = GolukApplication.getInstance().mCurrentUId;
		if (TextUtils.isEmpty(uid)) {
			headers.put("uid", "");
		} else {
			headers.put("uid", uid);
		}
		String cityCode = SharedPrefUtil.getCityIDString();
		if (!TextUtils.isEmpty(cityCode)) {
			headers.put("location", cityCode);
		}
		super.get();
	}
}
