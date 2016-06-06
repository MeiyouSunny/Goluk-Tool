package com.mobnote.golukmain.msg;

import java.util.HashMap;

import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.http.request.GolukFastjsonRequest;
import com.mobnote.golukmain.msg.bean.MessageBean;

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
		HashMap<String, String> headers = (HashMap<String, String>) getParam();
		headers.put("xieyi", xieyi);
		headers.put("uid", uid);
		headers.put("types", types);
		headers.put("operation", operation);
		headers.put("timestamp", timestamp);
		get();
	}
}
