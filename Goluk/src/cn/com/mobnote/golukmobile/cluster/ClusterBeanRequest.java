package cn.com.mobnote.golukmobile.cluster;

import java.util.HashMap;

import android.text.TextUtils;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.cluster.bean.JsonData;
import cn.com.mobnote.golukmobile.http.IRequestResultListener;
import cn.com.mobnote.golukmobile.http.request.GolukFastjsonRequest;
import cn.com.tiros.api.Tapi;

public class ClusterBeanRequest extends GolukFastjsonRequest<JsonData> {

	public ClusterBeanRequest(int requestType, IRequestResultListener listener) {
		super(requestType, JsonData.class, listener);
	}

	@Override
	protected String getPath() {
		return "/navidog4MeetTrans/activity.htm";
	}

	@Override
	protected String getMethod() {
		return "activityInfo";
	}

	public void get(String activityId) {
		HashMap<String, String> headers = (HashMap<String, String>) getHeader();
		headers.put("activityid", activityId);
		String uid = GolukApplication.getInstance().mCurrentUId;
		if (TextUtils.isEmpty(uid)) {
			headers.put("uid", "");
		} else {
			headers.put("uid", uid);
		}
		headers.put("mobileid", "" + Tapi.getMobileId());
		get();
	}
}
