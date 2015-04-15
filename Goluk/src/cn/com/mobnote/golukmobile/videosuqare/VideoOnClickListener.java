package cn.com.mobnote.golukmobile.videosuqare;

import java.util.HashMap;
import com.bokecc.sdk.mobile.play.DWMediaPlayer;
import android.view.View;
import android.view.View.OnClickListener;

public class VideoOnClickListener implements OnClickListener{
	private VideoSquareInfo mVideoSquareInfo=null;
	private HashMap<String, DWMediaPlayer> mDWMediaPlayerList=null;
	
	public VideoOnClickListener(HashMap<String, DWMediaPlayer> _mDWMediaPlayerList, VideoSquareInfo _mVideoSquareInfo){
		this.mVideoSquareInfo=_mVideoSquareInfo;
		this.mDWMediaPlayerList = _mDWMediaPlayerList;
	}
	
	@Override
	public void onClick(View arg0) {
		DWMediaPlayer player = mDWMediaPlayerList.get(mVideoSquareInfo.mVideoEntity.videoid);
		if(null !=player){
			if(player.isPlaying()){
				player.pause();
			}else{
				player.start();
			}
		}
	}
}
