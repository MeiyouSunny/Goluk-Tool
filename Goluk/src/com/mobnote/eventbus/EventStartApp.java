package com.mobnote.eventbus;

import com.mobnote.golukmain.xdpush.StartAppBean;

public class EventStartApp {

	public int mCode;
	public StartAppBean mBean;

	public EventStartApp(int code, StartAppBean bean) {
		mCode = code;
		mBean = bean;
	}

}
