package com.mobnote.eventbus;

/**
 * 下载(ipc)远端视频event
 * @author uestc
 *
 */
public class EventDownloadIpcVid {

	private String mVidPath;
	private int mType;
	
	public EventDownloadIpcVid(String path,int type) {
		mVidPath = path;
		mType = type;
	}

	public String getVidPath() {
		return mVidPath;
	}

	public int getType() {
		return mType;
	}
}
