package com.mobnote.golukmain.promotion;

import java.util.HashMap;
import java.util.Map;

import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.http.request.GolukFastjsonRequest;
import com.mobnote.map.LngLat;
import com.mobnote.util.SharedPrefUtil;

import android.text.TextUtils;

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
