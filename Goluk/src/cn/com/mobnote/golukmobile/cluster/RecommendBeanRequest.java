package cn.com.mobnote.golukmobile.cluster;

import java.util.HashMap;

import cn.com.mobnote.golukmobile.cluster.bean.ActivityJsonData;
import cn.com.mobnote.golukmobile.cluster.bean.JsonData;
import cn.com.mobnote.golukmobile.http.IRequestResultListener;
import cn.com.mobnote.golukmobile.http.request.GolukFastjasonRequest;

public class RecommendBeanRequest extends GolukFastjasonRequest<ActivityJsonData> {

	public RecommendBeanRequest(int requestType, IRequestResultListener listener) {
		super(requestType, ActivityJsonData.class, listener);
	}

	@Override
	protected String getPath() {
		return "/navidog4MeetTrans/activity.htm";
	}

	@Override
	protected String getMethod() {
		return "recommendVideo";
	}

	public void get(String activityId,String operation,String timestamp,String pagesize) {
		HashMap<String, String> headers = (HashMap<String, String>) getHeader();
		headers.put("activityid", activityId);
		headers.put("operation", operation);
		headers.put("timestamp", timestamp);
		headers.put("pagesize", pagesize);
		get();
	}
}
