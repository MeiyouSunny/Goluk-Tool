package cn.com.mobnote.eventbus;

import cn.com.tiros.baidu.BaiduLocationInfo;

public class EventPostLocation {

	private BaiduLocationInfo info;
	private int opCode;

	public EventPostLocation() {

	}

	public EventPostLocation(BaiduLocationInfo info, int opCode) {
		super();
		this.info = info;
		this.opCode = opCode;
	}

	public int getOpCode() {
		return opCode;
	}

	public BaiduLocationInfo getInfo() {
		return info;
	}

}
