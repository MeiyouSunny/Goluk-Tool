package com.mobnote.golukmain.cluster;

import java.util.HashMap;

import com.mobnote.golukmain.cluster.bean.ActivityJsonData;
import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.http.request.GolukFastjsonRequest;

public class RecommendBeanRequest extends GolukFastjsonRequest<ActivityJsonData> {

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
