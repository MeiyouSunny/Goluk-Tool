package com.mobnote.golukmain.cluster;

import java.util.HashMap;

import com.mobnote.golukmain.cluster.bean.GetClusterShareUrlData;
import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.http.request.GolukFastjsonRequest;

public class GetShareUrlRequest extends GolukFastjsonRequest<GetClusterShareUrlData> {

	public GetShareUrlRequest(int requestType, IRequestResultListener listener) {
		super(requestType, GetClusterShareUrlData.class, listener);
	}

	@Override
	protected String getPath() {
		return "/navidog4MeetTrans/activity.htm";
	}

	@Override
	protected String getMethod() {
		return "share";
	}

	public void get(String activityId) {
		HashMap<String, String> headers = (HashMap<String, String>) getHeader();
		headers.put("activityid", activityId);
		get();
	}
}
