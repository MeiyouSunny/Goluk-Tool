package com.mobnote.eventbus;

import com.mobnote.golukmain.wifidatacenter.WifiBindHistoryBean;

// Event bind finished
public class EventBindFinish {
	int opCode;
	public WifiBindHistoryBean bean;

	public EventBindFinish(int code) {
		opCode = code;
	}

	public int getOpCode() {
		return opCode;
	}

	public void setOpCode(int opCode) {
		this.opCode = opCode;
	}

}
