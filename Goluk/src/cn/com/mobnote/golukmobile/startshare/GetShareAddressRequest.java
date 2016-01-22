package cn.com.mobnote.golukmobile.startshare;

import java.util.HashMap;

import android.text.TextUtils;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.http.IRequestResultListener;
import cn.com.mobnote.golukmobile.http.request.GolukFastjsonRequest;
import cn.com.mobnote.golukmobile.startshare.bean.ShareDataFullBean;
import cn.com.mobnote.map.LngLat;

public class GetShareAddressRequest extends GolukFastjsonRequest<ShareDataFullBean> {

	public GetShareAddressRequest(int requestType, IRequestResultListener listener) {
		super(requestType, ShareDataFullBean.class, listener);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected String getPath() {
		// TODO Auto-generated method stub
		return "/navidog4MeetTrans/video.htm";
	}

	@Override
	protected String getMethod() {
		// TODO Auto-generated method stub

		return null;
	}

	public void get(String videoid, String type, String describe, String attribute, String issquare, String creattime,
			String signtime, String channelid, String activityid, String activityname, String location, String devicetag) {
		HashMap<String, String> params = (HashMap<String, String>) getParam();
		params.put("xieyi", "" + 100);
		params.put("videoid", videoid);
		params.put("type", type);
		params.put("describe", describe);
		params.put("attribute", attribute);
		params.put("issquare", issquare);
		params.put("tagid", "goluk");
		params.put("creattime", creattime);
		params.put("signtime", signtime);
		params.put("lat", "" + LngLat.lat);
		params.put("lon", "" + LngLat.lng);
		String uid = GolukApplication.getInstance().mCurrentUId;
		if (TextUtils.isEmpty(uid)) {
			params.put("uid", "");
		} else {
			params.put("uid", uid);
		}

		if (!TextUtils.isEmpty(location)) {
			params.put("location", location);
		} else {
			params.put("location", "");
		}

		if (!TextUtils.isEmpty(channelid)) {
			params.put("channelid", channelid);
		}
		if (!TextUtils.isEmpty(activityid)) {
			params.put("activityid", activityid);
		}
		if (!TextUtils.isEmpty(activityname)) {
			params.put("activityname", activityname);
		}
		if (devicetag != null) {
			params.put("devicetag", devicetag);
		} else {
			params.put("devicetag", "");
		}
		super.post();
	}
}
