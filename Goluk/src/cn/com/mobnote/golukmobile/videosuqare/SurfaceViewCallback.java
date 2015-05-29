package cn.com.mobnote.golukmobile.videosuqare;

import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.media.AudioManager;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import cn.com.mobnote.golukmobile.carrecorder.util.LogUtils;
import cn.com.tiros.debug.GolukDebugUtils;

import com.bokecc.sdk.mobile.play.DWMediaPlayer;

public class SurfaceViewCallback implements Callback{
		private VideoSquareInfo mVideoSquareInfo=null;
		private HashMap<String, DWMediaPlayer> mDWMediaPlayerList=null;
		private List<VideoSquareInfo> mVideoSquareListData=null;
		private int position;
		private HashMap<String, SurfaceHolder> mHolderList=null;
		private Context mContext = null;
		private VideoSquareListViewAdapter mVideoSquareListViewAdapter=null;
		
		public SurfaceViewCallback(VideoSquareListViewAdapter _mVideoSquareListViewAdapter, int _position, HashMap<String, SurfaceHolder> _mHolderList, List<VideoSquareInfo> _mVideoSquareListData, HashMap<String, DWMediaPlayer> _mDWMediaPlayerList, VideoSquareInfo _mVideoSquareInfo){
			this.position=_position;
			this.mHolderList=_mHolderList;
			this.mVideoSquareInfo=_mVideoSquareInfo;
			this.mDWMediaPlayerList = _mDWMediaPlayerList;
			this.mVideoSquareListData = _mVideoSquareListData;
			this.mVideoSquareListViewAdapter = _mVideoSquareListViewAdapter;
		}
		
		@Override
		public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		}

		@Override
		public void surfaceCreated(SurfaceHolder arg0) {
			GolukDebugUtils.d("","SSS============surfaceCreated=====111====position="+position);
			if(!mHolderList.containsKey(""+position)){
				mHolderList.put(""+position, arg0);
				GolukDebugUtils.d("","SSS============mHolderList=====position="+position+"===11111===SurfaceHolder===="+arg0);
			}
			
			if(0 == position){
				mVideoSquareListViewAdapter.updatePlayerState(0);
			}
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder arg0) {
			if(mHolderList.containsKey(""+position)){
				mHolderList.remove(""+position);
			}
			
			GolukDebugUtils.d("","SSS============surfaceDestroyed=========position="+position);
		}
}
