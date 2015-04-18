package cn.com.mobnote.golukmobile.videosuqare;

import java.util.HashMap;
import java.util.List;

import android.media.AudioManager;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import cn.com.mobnote.golukmobile.carrecorder.util.LogUtils;

import com.bokecc.sdk.mobile.play.DWMediaPlayer;

public class SurfaceViewCallback implements Callback{
		private VideoSquareInfo mVideoSquareInfo=null;
		private HashMap<String, DWMediaPlayer> mDWMediaPlayerList=null;
		private List<VideoSquareInfo> mVideoSquareListData=null;
		
		public SurfaceViewCallback(List<VideoSquareInfo> _mVideoSquareListData, HashMap<String, DWMediaPlayer> _mDWMediaPlayerList, VideoSquareInfo _mVideoSquareInfo){
			this.mVideoSquareInfo=_mVideoSquareInfo;
			this.mDWMediaPlayerList = _mDWMediaPlayerList;
			this.mVideoSquareListData = _mVideoSquareListData;
		}
		
		@Override
		public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
			LogUtils.d("SSS====surfaceChanged====arg1="+arg1+"===arg2="+arg2+"===arg3="+arg3);
		}

		@Override
		public void surfaceCreated(SurfaceHolder arg0) {
			DWMediaPlayer player = mDWMediaPlayerList.get(mVideoSquareInfo.id);
			if(null !=player){
				LogUtils.d("SSS============surfaceCreated=====111====id="+mVideoSquareInfo.id);
				updatePlayerState(PlayerState.allowbuffer);
				player.setDisplay(arg0);
				player.setAudioStreamType(AudioManager.STREAM_MUSIC);
			}
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder arg0) {
			LogUtils.d("SSS============surfaceDestroyed=========id="+mVideoSquareInfo.id);
			DWMediaPlayer player = mDWMediaPlayerList.get(mVideoSquareInfo.id);
			if(null !=player){
				if(player.isPlaying()){
					player.pause();
					updatePlayerState(PlayerState.pause);
				}
			}
		}
		
		private void updatePlayerState(PlayerState mPlayerState){
			mVideoSquareInfo.mPlayerState=mPlayerState;
			for(int i=0; i<mVideoSquareListData.size(); i++){
				String id = mVideoSquareListData.get(i).id;
				if(id.equals(mVideoSquareInfo.id)){
					mVideoSquareListData.get(i).mPlayerState = mPlayerState;
					break;
				}
			}
		}
		
}
