package cn.com.mobnote.golukmobile.videosuqare;

import java.util.HashMap;

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
	
	public VideoOnBufferingUpdateListener(HashMap<String, DWMediaPlayer> _mDWMediaPlayerList, ViewHolder _mViewHolder, VideoSquareInfo _mVideoSquareInfo){
		this.mDWMediaPlayerList = _mDWMediaPlayerList;
		this.mViewHolder = _mViewHolder;
		this.mRingView = mViewHolder.mRingView;
		this.mPreLoading = mViewHolder.mPreLoading;
		this.mVideoSquareInfo=_mVideoSquareInfo;
	}

	@Override
	public void onBufferingUpdate(MediaPlayer arg0, int arg1) {
		LogUtils.d("SSS============onBufferingUpdate=========arg1="+arg1);
		mRingView.setProcess(arg1);
		if(arg1 >= 100){
			mRingView.setVisibility(View.GONE);
//			mPreLoading.setVisibility(View.GONE);
			DWMediaPlayer player = mDWMediaPlayerList.get(mVideoSquareInfo.mVideoEntity.videoid);
			if(null !=player){
//				LogUtils.d("SSS============player.seekTo(1);=========View.GONE=");
			}
			LogUtils.d("SSS============onBufferingUpdate=========View.GONE=");
		}else{
			mRingView.setVisibility(View.VISIBLE);
		}
	}
}
