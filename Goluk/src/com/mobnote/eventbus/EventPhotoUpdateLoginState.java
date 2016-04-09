package com.mobnote.eventbus;

public class EventPhotoUpdateLoginState {
	int opCode;

	public EventPhotoUpdateLoginState(int code) {
		opCode = code;
	}

	public int getOpCode() {
		return opCode;
	}

	public void setOpCode(int opCode) {
		this.opCode = opCode;
	}
}
