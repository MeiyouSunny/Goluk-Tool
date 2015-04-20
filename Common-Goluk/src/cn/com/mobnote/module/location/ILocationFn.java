package cn.com.mobnote.module.location;

import cn.com.mobnote.logic.IGolukCommFn;

public interface ILocationFn extends IGolukCommFn {
	/** 　同步获取命令 */
	public static final int LOCATION_CMD_GET_POSITION = 0;

	public void LocationCallBack(String gpsJson);
}
