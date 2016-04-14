package com.mobnote.eventbus;

public class EventMessageUpdate {
	int opCode;

	public EventMessageUpdate(int code) {
		opCode = code;
	}

	public int getOpCode() {
		return opCode;
	}

	public void setOpCode(int opCode) {
		this.opCode = opCode;
	}
}
