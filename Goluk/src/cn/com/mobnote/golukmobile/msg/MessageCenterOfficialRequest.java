package cn.com.mobnote.golukmobile.msg;

import java.util.HashMap;

import cn.com.mobnote.golukmobile.http.IRequestResultListener;
import cn.com.mobnote.golukmobile.http.request.GolukFastjsonRequest;
import cn.com.mobnote.golukmobile.msg.bean.MessageBean;

public class MessageCenterOfficialRequest extends GolukFastjsonRequest<MessageBean> {
	public MessageCenterOfficialRequest(int requestType, IRequestResultListener listener) {
		super(requestType, MessageBean.class, listener);
	}

	@Override
	protected String getPath() {
		return "/cdcMessage/message.htm";
	}

	@Override
	protected String getMethod() {
		return "list";
	}

	public void get(String xieyi, String uid, String types, String operation, String timestamp) {
		HashMap<String, String> headers = (HashMap<String, String>) getHeader();
		headers.put("xieyi", xieyi);
		headers.put("uid", uid);
		headers.put("types", types);
		headers.put("operation", operation);
		headers.put("timestamp", timestamp);
		get();
	}
}
