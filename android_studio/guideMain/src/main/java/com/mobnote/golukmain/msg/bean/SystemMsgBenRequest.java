package com.mobnote.golukmain.msg.bean;

import java.util.HashMap;

import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.http.request.GolukFastjsonRequest;

public class SystemMsgBenRequest extends GolukFastjsonRequest<MessageBean>{

	public SystemMsgBenRequest(int requestType, IRequestResultListener listener) {
		super(requestType,MessageBean.class,listener);
	}

	@Override
	protected String getPath() {
		return "/cdcMessage/message.htm";
	}

	@Override
	protected String getMethod() {
		return "list";
	}

	public void get(String uid ,String types ,String operation ,String timestamp) {
		HashMap<String, String> headers = (HashMap<String, String>) getHeader();
		headers.put("uid", uid);
		headers.put("types", types);
		headers.put("timestamp", timestamp);
		headers.put("xieyi","100");
		headers.put("operation", operation);
		
		get();
	}

}
