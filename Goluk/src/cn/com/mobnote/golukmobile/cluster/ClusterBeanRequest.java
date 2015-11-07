package cn.com.mobnote.golukmobile.cluster;

import java.util.HashMap;
import cn.com.mobnote.golukmobile.cluster.bean.JsonData;
import cn.com.mobnote.golukmobile.http.IRequestResultListener;
import cn.com.mobnote.golukmobile.http.request.GolukFastjsonRequest;

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
		get();
	}
}
