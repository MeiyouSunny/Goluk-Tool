package cn.com.mobnote.golukmobile.special;

import cn.com.mobnote.golukmobile.MainActivity;
import cn.com.mobnote.golukmobile.comment.CommentActivity;
import cn.com.mobnote.golukmobile.player.VideoPlayerView;
import cn.com.mobnote.golukmobile.videosuqare.VideoSquareInfo;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;

public class SpecialCommentListener implements OnClickListener{
	private Context mContext;
	
	String imagepath;
	String videopath;
	String from;
	String type;
	
	public SpecialCommentListener(Context context, String ipath,String vpath,String f,String t) {
		this.mContext = context;
		imagepath = ipath;
		videopath = vpath;
		from = f;
		type = t;
	}
	
	@Override
	public void onClick(View arg0) {
		if("2".equals(type)){
//			MainActivity a = (MainActivity)mContext;
			Intent intent = new Intent(mContext, VideoPlayerView.class);
			intent.putExtra("from", from);
			intent.putExtra("image", imagepath);
			intent.putExtra("playUrl", videopath);
			//uploadPlayer(mVideoSquareInfo.mVideoEntity.videoid, "1","1");//上报播放次数
			mContext.startActivity(intent);
		}
		
	}
	
}
