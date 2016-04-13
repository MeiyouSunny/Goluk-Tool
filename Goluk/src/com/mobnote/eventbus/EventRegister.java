package com.mobnote.eventbus;

public class EventRegister {
	private int opCode;

	private int mEvent;
	private int mResult;
	private Object mData;

	public int getOpCode() {
		return opCode;
	}

	public int getmEvent() {
		return mEvent;
	}

	public int getmResult() {
		return mResult;
	}

	public Object getmData() {
		return mData;
	}

	public EventRegister(int code, int event, int result, Object data) {
		opCode = code;
		mEvent = event;
		mResult = result;
		mData = data;
	}

}
