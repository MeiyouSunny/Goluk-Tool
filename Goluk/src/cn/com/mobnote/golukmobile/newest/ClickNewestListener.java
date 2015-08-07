package cn.com.mobnote.golukmobile.newest;

import java.util.ArrayList;
import java.util.List;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.player.VideoPlayerView;
import cn.com.mobnote.golukmobile.videosuqare.VideoEntity;
import cn.com.mobnote.golukmobile.videosuqare.VideoSquareInfo;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;

public class ClickNewestListener implements OnClickListener{
	private VideoSquareInfo mVideoSquareInfo;
	private Context mContext;
	
	public ClickNewestListener(Context context, VideoSquareInfo info) {
		this.mVideoSquareInfo = info;
		this.mContext = context;
	}

	@Override
	public void onClick(View arg0) {
		Intent intent = new Intent(mContext, VideoPlayerView.class);
		intent.putExtra("from", "suqare");
		intent.putExtra("image", mVideoSquareInfo.mVideoEntity.picture);
		intent.putExtra("playUrl", mVideoSquareInfo.mVideoEntity.ondemandwebaddress);
		uploadPlayer(mVideoSquareInfo.mVideoEntity.videoid, "1","1");//上报播放次数
		mContext.startActivity(intent);
	}
	
	private void uploadPlayer(String videoid,String channel,String clicknumber){
		VideoSquareInfo vsi = new VideoSquareInfo();
		VideoEntity ve = new VideoEntity();
		ve.videoid = videoid;
		ve.clicknumber = clicknumber;
		vsi.mVideoEntity = ve;
		List<VideoSquareInfo> list = new ArrayList<VideoSquareInfo>() ;
		list.add(vsi);
		GolukApplication.getInstance().getVideoSquareManager().clickNumberUpload(channel, list);
	}
	
	
}
