package com.mobnote.golukmain.comment;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;

import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.comment.bean.CommentAddResultBean;
import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.http.request.GolukFastjsonRequest;

import android.text.TextUtils;

public class CommentAddRequest extends GolukFastjsonRequest<CommentAddResultBean> {

	public CommentAddRequest(int requestType, IRequestResultListener listener) {
		super(requestType, CommentAddResultBean.class, listener);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected String getPath() {
		// TODO Auto-generated method stub
		return "/activePlay.htm";
	}

	@Override
	protected String getMethod() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean get(String topicid, String topictype, String content, String replyId, String replyName, String ztid) {
		String uid = GolukApplication.getInstance().mCurrentUId;
		if (TextUtils.isEmpty(topicid) || TextUtils.isEmpty(uid) || TextUtils.isEmpty(content)) {
			return false;
		}
		HashMap<String, String> paramters = (HashMap<String, String>) getParam();
		paramters.put("xieyi", "100");
		paramters.put("topicid", topicid);
		paramters.put("topictype", topictype);
		paramters.put("authorid", uid);

		paramters.put("text", content);
		if (TextUtils.isEmpty(replyName)) {
			paramters.put("replyname", "");
		} else {
			paramters.put("replyname", replyName);
		}
		if (TextUtils.isEmpty(replyId)) {
			paramters.put("replyid", "");
		} else {
			paramters.put("replyid", replyId);
		}

		if (TextUtils.isEmpty(ztid)) {
			paramters.put("ztid", "");
		} else {
			paramters.put("ztid", ztid);
		}
		super.post();
		return true;
	}
}
