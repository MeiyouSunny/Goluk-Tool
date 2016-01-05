package cn.com.mobnote.eventbus;

import cn.com.mobnote.golukmobile.xdpush.StartAppBean;

public class EventStartApp {

	public int mCode;
	public StartAppBean mBean;

	public EventStartApp(int code, StartAppBean bean) {
		mCode = code;
		mBean = bean;
	}

}
