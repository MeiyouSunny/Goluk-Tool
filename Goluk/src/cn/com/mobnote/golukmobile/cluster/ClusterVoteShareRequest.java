package cn.com.mobnote.golukmobile.cluster;

import java.util.HashMap;

import cn.com.mobnote.golukmobile.cluster.bean.ClusterVoteShareBean;
import cn.com.mobnote.golukmobile.http.IRequestResultListener;
import cn.com.mobnote.golukmobile.http.request.GolukFastjsonRequest;

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
