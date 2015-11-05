package cn.com.mobnote.golukmobile.cluster;

import java.util.HashMap;

import android.text.TextUtils;
import cn.com.mobnote.golukmobile.cluster.bean.JsonData;
import cn.com.mobnote.golukmobile.http.IRequestResultListener;
import cn.com.mobnote.golukmobile.http.request.GolukFastjasonRequest;

public class ClusterBeanRequest extends GolukFastjasonRequest<JsonData> {

	public ClusterBeanRequest(int requestType, IRequestResultListener listener) {
		super(requestType, JsonData.class, listener);
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
		return "activityInfo";
	}

	public void get(String activityId) {
		HashMap<String, String> headers = (HashMap<String, String>) getHeader();
		headers.put("activityid", activityId);
		get();
	}
}
