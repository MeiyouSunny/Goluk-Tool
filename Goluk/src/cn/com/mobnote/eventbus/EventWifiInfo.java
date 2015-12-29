package cn.com.mobnote.eventbus;

import cn.com.mobnote.golukmobile.wifidatacenter.WifiBindHistoryBean;

public class EventWifiInfo {
	
	public int opCode;
	
	public WifiBindHistoryBean mBean;
	
	public EventWifiInfo(int code, WifiBindHistoryBean bean) {
		opCode = code;
		mBean = bean ;
	}

}
