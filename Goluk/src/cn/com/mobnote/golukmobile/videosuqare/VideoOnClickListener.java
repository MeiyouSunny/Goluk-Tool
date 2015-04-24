package cn.com.mobnote.golukmobile.videosuqare;

import java.util.HashMap;
import java.util.List;

import cn.com.mobnote.golukmobile.carrecorder.util.LogUtils;
import cn.com.mobnote.golukmobile.live.LiveActivity;
import cn.com.mobnote.golukmobile.videosuqare.VideoSquareListViewAdapter.ViewHolder;

import com.bokecc.sdk.mobile.play.DWMediaPlayer;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

public class VideoOnClickListener implements OnClickListener{
	private VideoSquareInfo mVideoSquareInfo=null;
	private HashMap<String, DWMediaPlayer> mDWMediaPlayerList=null;
	private ViewHolder mViewHolder=null;
	private ImageView mPreLoading=null;
	private List<VideoSquareInfo> mVideoSquareListData=null;
	private Context mContext = null;
	private int form ;
	
	public VideoOnClickListener(List<VideoSquareInfo> _mVideoSquareListData, ViewHolder _mViewHolder, HashMap<String, DWMediaPlayer> _mDWMediaPlayerList, VideoSquareInfo _mVideoSquareInfo,Context context,int platform){
		this.mViewHolder = _mViewHolder;
		this.mVideoSquareInfo=_mVideoSquareInfo;
		this.mDWMediaPlayerList = _mDWMediaPlayerList;
		this.mPreLoading = mViewHolder.mPreLoading;
		this.mVideoSquareListData = _mVideoSquareListData;
		this.mContext = context;
		this.form = platform;
	}
	
	@Override
	public void onClick(View arg0) {
		if("2".equals(mVideoSquareInfo.mVideoEntity.type)){
			PlayerState mPlayerState = mVideoSquareInfo.mPlayerState;
			DWMediaPlayer player = mDWMediaPlayerList.get(mVideoSquareInfo.id);
			if(null !=player){
				if(PlayerState.allowbuffer == mPlayerState){
					player.prepareAsync();
					updatePlayerState(PlayerState.buffing);
					LogUtils.d("SSS=========buffing====1111====");
				}else if(PlayerState.buffing == mPlayerState){
					LogUtils.d("SSS=========buffing===2222=====");
				}else if(PlayerState.bufferend == mPlayerState){
					player.start();
					updatePlayerState(PlayerState.playing);
					LogUtils.d("SSS=========playing========");
				}else if(PlayerState.playing == mPlayerState){
					if(player.isPlaying()){
						player.pause();
						updatePlayerState(PlayerState.pause);
						LogUtils.d("SSS=========pause========");
					}
				}else if(PlayerState.pause == mPlayerState){
					player.start();
					updatePlayerState(PlayerState.playing);
					LogUtils.d("SSS=========playing========");
				}else {
					LogUtils.d("SSS=========noallow========");
				}
			}
		}else{
			//直播点击跳转
			 // 开启直播
	        Intent intent = new Intent(mContext, LiveActivity.class);
	        intent.putExtra(LiveActivity.KEY_IS_LIVE, false);
	        intent.putExtra(LiveActivity.KEY_GROUPID, "");
	        intent.putExtra(LiveActivity.KEY_PLAY_URL, "");
	        intent.putExtra(LiveActivity.KEY_JOIN_GROUP, "");
	        //intent.putExtra(LiveActivity.KEY_USERINFO, userInfo);
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
