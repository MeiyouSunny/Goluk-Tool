package com.mobnote.eventbus;

import com.mobnote.golukmain.adas.AdasConfigParamterBean;

public class EventAdasConfigStatus {
	private int mOpCode;
	private AdasConfigParamterBean mAdasConfigParamterBean;

	public EventAdasConfigStatus(int code) {
		mOpCode = code;
	}

	public int getOpCode() {
		return mOpCode;
	}
	
	public void setData(AdasConfigParamterBean adasConfigParamterBean) {
		mAdasConfigParamterBean = adasConfigParamterBean;
	}
	
	public AdasConfigParamterBean getData() {
		return mAdasConfigParamterBean;
	}
}
