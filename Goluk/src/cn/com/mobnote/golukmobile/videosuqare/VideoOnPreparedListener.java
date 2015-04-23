package cn.com.mobnote.golukmobile.videosuqare;

import java.util.HashMap;
import java.util.List;
import cn.com.mobnote.golukmobile.carrecorder.util.LogUtils;
import com.bokecc.sdk.mobile.play.DWMediaPlayer;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;

public class VideoOnPreparedListener implements OnPreparedListener{
	private VideoSquareInfo mVideoSquareInfo=null;
	private HashMap<String, DWMediaPlayer> mDWMediaPlayerList=null;
	private List<VideoSquareInfo> mVideoSquareListData=null;
	
	public VideoOnPreparedListener(List<VideoSquareInfo> _mVideoSquareListData, HashMap<String, DWMediaPlayer> _mDWMediaPlayerList, VideoSquareInfo _mVideoSquareInfo){
		this.mVideoSquareInfo=_mVideoSquareInfo;
		this.mDWMediaPlayerList = _mDWMediaPlayerList;
		this.mVideoSquareListData = _mVideoSquareListData;
	}

	@Override
	public void onPrepared(MediaPlayer arg0) {
		DWMediaPlayer player = mDWMediaPlayerList.get(mVideoSquareInfo.id);
		if(null !=player){
			LogUtils.d("SSS==============player.start=========id="+mVideoSquareInfo.id);
			player.start();
			updatePlayerState(PlayerState.playing);
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
