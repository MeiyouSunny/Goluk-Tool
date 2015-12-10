package cn.com.mobnote.golukmobile.comment;

import java.util.HashMap;

import android.text.TextUtils;
import cn.com.mobnote.golukmobile.comment.bean.CommentResultBean;
import cn.com.mobnote.golukmobile.http.IRequestResultListener;
import cn.com.mobnote.golukmobile.http.request.GolukFastjsonRequest;

public class CommentListRequest extends GolukFastjsonRequest<CommentResultBean> {
	public static final int PAGE_SIZE = 20;
	public CommentListRequest(int requestType, IRequestResultListener listener) {
		super(requestType, CommentResultBean.class, listener);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected String getPath() {
		// TODO Auto-generated method stub
		return "/cdcComment/comment.htm";
	}

	@Override
	protected String getMethod() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean get(String topicid, String topictype, int operation, String timestamp) {
		if (TextUtils.isEmpty(topicid)) {
			return false;
		}
		HashMap<String, String> paramters = (HashMap<String, String>) getParam();
		paramters.put("xieyi", "100");
		paramters.put("topicid", topicid);
		paramters.put("topictype", topictype);
		paramters.put("operation", "" + operation);
		paramters.put("timestamp", timestamp);
		paramters.put("pagesize", "" + PAGE_SIZE);
		super.get();
		return true;
	}
}
