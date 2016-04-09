package com.mobnote.eventbus;

import com.mobnote.golukmain.wifidatacenter.WifiBindHistoryBean;

public class EventWifiInfo {
	
	public int opCode;
	
	public WifiBindHistoryBean mBean;
	
	public EventWifiInfo(int code, WifiBindHistoryBean bean) {
		opCode = code;
		mBean = bean ;
	}

}
