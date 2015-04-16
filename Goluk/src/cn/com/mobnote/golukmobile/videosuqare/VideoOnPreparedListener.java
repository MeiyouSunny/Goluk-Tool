package cn.com.mobnote.golukmobile.videosuqare;

import java.util.HashMap;

import cn.com.mobnote.golukmobile.carrecorder.util.LogUtils;

import com.bokecc.sdk.mobile.play.DWMediaPlayer;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;

public class VideoOnPreparedListener implements OnPreparedListener{
	private VideoSquareInfo mVideoSquareInfo=null;
	private HashMap<String, DWMediaPlayer> mDWMediaPlayerList=null;
	
	public VideoOnPreparedListener(HashMap<String, DWMediaPlayer> _mDWMediaPlayerList, VideoSquareInfo _mVideoSquareInfo){
		this.mVideoSquareInfo=_mVideoSquareInfo;
		this.mDWMediaPlayerList = _mDWMediaPlayerList;
	}

	@Override
	public void onPrepared(MediaPlayer arg0) {
		DWMediaPlayer player = mDWMediaPlayerList.get(mVideoSquareInfo.mVideoEntity.videoid);
		if(null !=player){
			LogUtils.d("SSS==============player.start=========videoid="+mVideoSquareInfo.mVideoEntity.videoid);
//			player.start();
		}
	}
}
