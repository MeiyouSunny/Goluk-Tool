package com.mobnote.golukmain.photoalbum;

import java.util.List;

import com.mobnote.golukmain.carrecorder.entity.VideoInfo;

public interface DataCallBack {
	public void onSuccess(int type, List<VideoInfo> mLocalListData, List<String> mGroupListName);
}
