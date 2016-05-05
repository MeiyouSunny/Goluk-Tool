package com.mobnote.eventbus;

/**
 * 删除视频
 * 
 * @author jyf
 */
public class EventDeleteVideo {
	private int mOpCode;
	private String mVid;

	public EventDeleteVideo(int code, String vid) {
		mOpCode = code;
		mVid = vid;
	}

	public String getVid() {
		return mVid;
	}

	public int getOpCode() {
		return mOpCode;
	}

}
