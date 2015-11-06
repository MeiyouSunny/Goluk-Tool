package cn.com.mobnote.golukmobile.newest;

import java.util.HashMap;

import android.text.TextUtils;
import cn.com.mobnote.golukmobile.http.IRequestResultListener;
import cn.com.mobnote.golukmobile.http.request.GolukFastjasonRequest;


public class BannerListRequest extends GolukFastjasonRequest<BannerModel> {

	public BannerListRequest(int requestType, IRequestResultListener listener) {
		super(requestType, BannerModel.class, listener);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected String getPath() {
		// TODO Auto-generated method stub
		return "/navidog4MeetTrans/boutique.htm";
	}

	@Override
	protected String getMethod() {
		// TODO Auto-generated method stub
		return "slideList";
	}

	public void get(String location) {
		HashMap<String, String> headers = (HashMap<String, String>) getHeader();
		if (!TextUtils.isEmpty(location)) {
			headers.put("location", location);
		}
		get();
	}
}
