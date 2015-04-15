package cn.com.mobnote.golukmobile.videosuqare;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import cn.com.mobnote.golukmobile.carrecorder.util.LogUtils;

public class VideoOnErrorListener implements OnErrorListener{
private VideoSquareInfo mVideoSquareInfo=null;
	
	public VideoOnErrorListener(VideoSquareInfo _mVideoSquareInfo){
		this.mVideoSquareInfo=_mVideoSquareInfo;
	}

	@Override
	public boolean onError(MediaPlayer arg0, int arg1, int arg2) {
		LogUtils.d("SSS============onError=========arg1="+arg1+"==arg2="+arg2);
		return false;
	}
	
}