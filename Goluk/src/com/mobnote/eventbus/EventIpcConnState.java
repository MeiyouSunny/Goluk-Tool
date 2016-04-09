package com.mobnote.eventbus;

public class EventIpcConnState {

	private int mOpCode;

	public EventIpcConnState(int code) {
		mOpCode = code;
	}

	public int getmOpCode() {
		return mOpCode;
	}

}
