package cn.com.mobnote.golukmobile.videosuqare;

import java.util.HashMap;
import android.media.AudioManager;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import cn.com.mobnote.golukmobile.carrecorder.util.LogUtils;
import com.bokecc.sdk.mobile.play.DWMediaPlayer;

public class SurfaceViewCallback implements Callback{
		private VideoSquareInfo mVideoSquareInfo=null;
		private HashMap<String, DWMediaPlayer> mDWMediaPlayerList=null;
		
		public SurfaceViewCallback(HashMap<String, DWMediaPlayer> _mDWMediaPlayerList, VideoSquareInfo _mVideoSquareInfo){
			this.mVideoSquareInfo=_mVideoSquareInfo;
			this.mDWMediaPlayerList = _mDWMediaPlayerList;
		}
		
		@Override
		public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		}

		@Override
		public void surfaceCreated(SurfaceHolder arg0) {
			LogUtils.d("SSS============surfaceCreated=========="+mVideoSquareInfo.mUserEntity.nickname);
			
			DWMediaPlayer player = mDWMediaPlayerList.get(mVideoSquareInfo.mVideoEntity.videoid);
			if(null !=player){
				player.setDisplay(arg0);
				player.setAudioStreamType(AudioManager.STREAM_MUSIC);
//				player.prepareAsync();
//				LogUtils.d("SSS============player.prepareAsync=========videoid="+mVideoSquareInfo.mVideoEntity.videoid);
			}
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder arg0) {
			LogUtils.d("SSS============surfaceDestroyed==========");
			DWMediaPlayer player = mDWMediaPlayerList.get(mVideoSquareInfo.mVideoEntity.videoid);
			if(null !=player){
				if(player.isPlaying()){
					player.pause();
				}
			}
		}
		
}
