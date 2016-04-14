package com.mobnote.eventbus;

/**
 * 绑定成功的结果
 * */
public class EventBindResult {

	private int mOpCode;

	public EventBindResult(int code) {
		mOpCode = code;
	}

	public int getOpCode() {
		return mOpCode;
	}

}
