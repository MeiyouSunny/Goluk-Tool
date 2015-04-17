package cn.com.mobnote.golukmobile.videosuqare;

import java.util.HashMap;

import cn.com.mobnote.golukmobile.carrecorder.util.LogUtils;
import cn.com.mobnote.golukmobile.videosuqare.VideoSquareListViewAdapter.ViewHolder;

import com.bokecc.sdk.mobile.play.DWMediaPlayer;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

public class VideoOnClickListener implements OnClickListener{
	private VideoSquareInfo mVideoSquareInfo=null;
	private HashMap<String, DWMediaPlayer> mDWMediaPlayerList=null;
	private ViewHolder mViewHolder=null;
	private ImageView mPreLoading=null;
	
	public VideoOnClickListener(ViewHolder _mViewHolder, HashMap<String, DWMediaPlayer> _mDWMediaPlayerList, VideoSquareInfo _mVideoSquareInfo){
		this.mViewHolder = _mViewHolder;
		this.mVideoSquareInfo=_mVideoSquareInfo;
		this.mDWMediaPlayerList = _mDWMediaPlayerList;
		this.mPreLoading = mViewHolder.mPreLoading;
	}
	
	@Override
	public void onClick(View arg0) {
		if("2".equals(mVideoSquareInfo.mVideoEntity.type)){
			DWMediaPlayer player = mDWMediaPlayerList.get(mVideoSquareInfo.mVideoEntity.videoid);
			if(null !=player){
				if(player.isPlaying()){
					player.pause();
				}else{
					mPreLoading.setVisibility(View.GONE);
					LogUtils.d("SSS=========player.prepareAsync==========");
//					player.prepareAsync();
					player.start();
				}
			}
		}else{
			//直播点击跳转
			
		}
	}
}
