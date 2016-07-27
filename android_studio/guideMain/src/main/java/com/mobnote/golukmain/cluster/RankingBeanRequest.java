package com.mobnote.golukmain.cluster;

import android.text.TextUtils;

import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.cluster.bean.RankingListBean;
import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.http.request.GolukFastjsonRequest;

import java.util.HashMap;

import cn.com.tiros.api.Tapi;

public class RankingBeanRequest extends GolukFastjsonRequest<RankingListBean> {

	public RankingBeanRequest(int requestType, IRequestResultListener listener) {
		super(requestType, RankingListBean.class, listener);
	}

	@Override
	protected String getPath() {
		return "/navidog4MeetTrans/activity.htm";
	}

	@Override
	protected String getMethod() {
		return "rankVideo";
	}

	public void get(String activityId,String operation,String timestamp,String pagesize) {
		HashMap<String, String> headers = (HashMap<String, String>) getHeader();
		headers.put("activityid", activityId);
		if(GolukApplication.getInstance().getMyInfo() != null){
			String uid = GolukApplication.getInstance().getMyInfo().uid;
			if (TextUtils.isEmpty(uid)) {
				headers.put("uid", "");
			} else {
				headers.put("uid", uid);
			}
		}
		headers.put("mobileid", "" + Tapi.getMobileId());
		headers.put("operation",operation);
		headers.put("timestamp",timestamp);
		headers.put("pagesize",pagesize);
		get();
	}
}
