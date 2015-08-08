package cn.com.mobnote.golukmobile.newest;

import cn.com.mobnote.golukmobile.videosuqare.VideoSquareInfo;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;

public class ClickFunctionListener implements OnClickListener{
	private VideoSquareInfo mVideoSquareInfo;
	private Context mContext;
	
	public ClickFunctionListener(Context context, VideoSquareInfo info) {
		this.mVideoSquareInfo = info;
		this.mContext = context;
	}

	@Override
	public void onClick(View arg0) {
		new FunctionDialog(mContext, mVideoSquareInfo).show();
	}
	
}

