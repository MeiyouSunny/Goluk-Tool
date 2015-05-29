package cn.com.mobnote.golukmobile.videosuqare;

import java.util.HashMap;
import java.util.List;
import com.bokecc.sdk.mobile.play.DWMediaPlayer;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.view.View;
import android.widget.ImageView;
import cn.com.mobnote.golukmobile.carrecorder.util.LogUtils;
import cn.com.mobnote.golukmobile.videosuqare.VideoSquareListViewAdapter.ViewHolder;

public class VideoOnBufferingUpdateListener implements OnBufferingUpdateListener{
	private HashMap<String, DWMediaPlayer> mDWMediaPlayerList=null;
	private VideoSquareInfo mVideoSquareInfo=null;
	private ViewHolder mViewHolder=null;
	private ImageView mPreLoading=null;
	private RingView mRingView = null;
	private List<VideoSquareInfo> mVideoSquareListData=null;
	
	public VideoOnBufferingUpdateListener(List<VideoSquareInfo> _mVideoSquareListData, HashMap<String, DWMediaPlayer> _mDWMediaPlayerList, ViewHolder _mViewHolder, VideoSquareInfo _mVideoSquareInfo){
		this.mDWMediaPlayerList = _mDWMediaPlayerList;
		this.mViewHolder = _mViewHolder;
		this.mRingView = mViewHolder.mRingView;
		this.mPreLoading = mViewHolder.mPreLoading;
		this.mVideoSquareInfo=_mVideoSquareInfo;
		this.mVideoSquareListData = _mVideoSquareListData;
	}

	@Override
	public void onBufferingUpdate(MediaPlayer arg0, int arg1) {
		mRingView.setProcess(arg1);
		if(arg1 >= 100){
			mRingView.setVisibility(View.GONE);
			DWMediaPlayer player = mDWMediaPlayerList.get(mVideoSquareInfo.id);
			if(null !=player){
				if(!player.isPlaying()){
					updatePlayerState(PlayerState.bufferend);
				}else{
					mPreLoading.setVisibility(View.GONE);
				}
			}
		}else{
			mRingView.setVisibility(View.VISIBLE);
			updatePlayerState(PlayerState.buffing);
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
