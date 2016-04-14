package com.mobnote.golukmain.cluster;

import java.util.HashMap;

import com.mobnote.golukmain.cluster.bean.ClusterVoteShareBean;
import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.http.request.GolukFastjsonRequest;

public class ClusterVoteShareRequest extends GolukFastjsonRequest<ClusterVoteShareBean> {

	public ClusterVoteShareRequest(int requestType, IRequestResultListener listener) {
		super(requestType, ClusterVoteShareBean.class, listener);
	}

	@Override
	protected String getPath() {
		return "/cdcGeneralize/withdraw.htm";
	}

	@Override
	protected String getMethod() {
		return "voteshare";
	}

	public void get(String voteId) {
		HashMap<String, String> headers = (HashMap<String, String>) getHeader();
		headers.put("voteid", voteId);
		get();
	}
}
