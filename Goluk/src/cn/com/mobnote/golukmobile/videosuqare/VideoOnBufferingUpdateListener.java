package cn.com.mobnote.golukmobile.videosuqare;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.view.View;
import android.widget.ImageView;
import cn.com.mobnote.golukmobile.carrecorder.util.LogUtils;

public class VideoOnBufferingUpdateListener implements OnBufferingUpdateListener{
	private VideoSquareInfo mVideoSquareInfo=null;
	private ImageView mPreLoading=null;
	
	public VideoOnBufferingUpdateListener(ImageView _mPreLoading, VideoSquareInfo _mVideoSquareInfo){
		this.mPreLoading = _mPreLoading;
		this.mVideoSquareInfo=_mVideoSquareInfo;
	}

	@Override
	public void onBufferingUpdate(MediaPlayer arg0, int arg1) {
		LogUtils.d("SSS============onBufferingUpdate=========arg1="+arg1);
		if(arg1 >= 100){
			LogUtils.d("SSS============onBufferingUpdate=========View.GONE=");
			mPreLoading.setVisibility(View.GONE);
		}else{
			
		}
	}
}
