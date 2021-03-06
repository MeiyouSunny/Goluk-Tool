package com.mobnote.golukmain.live;

import java.io.Serializable;

public class LiveSettingBean implements Serializable {
	/** */
	private static final long serialVersionUID = 1L;
	/** 视频类型 */
	public int vtype;
	/** 视频描述 */
	public String desc;
	/** 直播时长 */
	public int duration;
	/** 是否支持对讲 */
	public boolean isCanTalk;
	/** 是否支持视频声音 */
	public boolean isCanVoice;
	public String netCountStr;
}
