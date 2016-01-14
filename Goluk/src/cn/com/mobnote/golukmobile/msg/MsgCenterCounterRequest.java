package cn.com.mobnote.golukmobile.msg;

import java.util.HashMap;

import cn.com.mobnote.golukmobile.http.IRequestResultListener;
import cn.com.mobnote.golukmobile.http.request.GolukFastjsonRequest;
import cn.com.mobnote.golukmobile.msg.bean.MessageCounterBean;

public class MsgCenterCounterRequest extends GolukFastjsonRequest<MessageCounterBean> {
	public MsgCenterCounterRequest(int requestType, IRequestResultListener listener) {
		super(requestType, MessageCounterBean.class, listener);
	}

	@Override
	protected String getPath() {
		return "/cdcMessage/message.htm";
	}

	@Override
	protected String getMethod() {
		return "count";
	}

	public void get(String protocol, String uid, String lon, String lat, String location) {
		HashMap<String, String> headers = (HashMap<String, String>) getHeader();
		headers.put("xieyi", protocol);
		headers.put("uid", uid);
		headers.put("lon", lon);
		headers.put("lat", lat);
		headers.put("location", location);
		get();
	}

}
