package cn.com.mobnote.golukmobile.comment;

import java.util.HashMap;

import android.text.TextUtils;
import cn.com.mobnote.golukmobile.comment.bean.CommentDelResultBean;
import cn.com.mobnote.golukmobile.http.IRequestResultListener;
import cn.com.mobnote.golukmobile.http.request.GolukFastjsonRequest;

public class CommentDeleteRequest extends GolukFastjsonRequest<CommentDelResultBean> {

	public CommentDeleteRequest(int requestType, IRequestResultListener listener) {
		super(requestType, CommentDelResultBean.class, listener);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected String getPath() {
		// TODO Auto-generated method stub
		return "/cdcComment/comment/delete.htm";
	}

	@Override
	protected String getMethod() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean get(String id) {
		if (TextUtils.isEmpty(id)) {
			return false;
		}
		HashMap<String, String> paramters = (HashMap<String, String>) getParam();
		paramters.put("xieyi", "100");
		paramters.put("comment_id", id);
		super.get();
		return true;
	}
}
