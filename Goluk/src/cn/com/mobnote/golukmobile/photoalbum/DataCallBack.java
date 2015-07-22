package cn.com.mobnote.golukmobile.photoalbum;

import java.util.List;
import cn.com.mobnote.golukmobile.carrecorder.entity.VideoInfo;

public interface DataCallBack {
	public void onSuccess(int type, List<VideoInfo> mLocalListData, List<String> mGroupListName);
}
