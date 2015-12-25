package cn.com.mobnote.eventbus;

import cn.com.mobnote.golukmobile.wifidatacenter.WifiBindHistoryBean;

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
