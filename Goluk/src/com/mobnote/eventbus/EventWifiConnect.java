package com.mobnote.eventbus;

public class EventWifiConnect {
	int opCode;

	public EventWifiConnect(int code) {
		opCode = code;
	}

	public int getOpCode() {
		return opCode;
	}

	public void setOpCode(int opCode) {
		this.opCode = opCode;
	}
}
