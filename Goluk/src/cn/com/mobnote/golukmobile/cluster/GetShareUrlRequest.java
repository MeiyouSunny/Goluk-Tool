package cn.com.mobnote.golukmobile.cluster;

import java.util.HashMap;

import cn.com.mobnote.golukmobile.cluster.bean.ActivityJsonData;
import cn.com.mobnote.golukmobile.cluster.bean.GetClusterShareUrlData;
import cn.com.mobnote.golukmobile.cluster.bean.JsonData;
import cn.com.mobnote.golukmobile.http.IRequestResultListener;
import cn.com.mobnote.golukmobile.http.request.GolukFastjsonRequest;

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
