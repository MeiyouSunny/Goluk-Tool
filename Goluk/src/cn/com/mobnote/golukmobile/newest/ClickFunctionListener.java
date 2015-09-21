package cn.com.mobnote.golukmobile.newest;

import cn.com.mobnote.golukmobile.videosuqare.VideoSquareInfo;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;

public class ClickFunctionListener implements OnClickListener {
	private VideoSquareInfo mVideoSquareInfo;
	private Context mContext;
	private boolean mIsDel = false;
	private IDialogDealFn mListener = null;

	public ClickFunctionListener(Context context, VideoSquareInfo info, boolean isDel, IDialogDealFn fn) {
		this.mVideoSquareInfo = info;
		this.mContext = context;
		mListener = fn;
		mIsDel = isDel;
	}

	@Override
	public void onClick(View v) {
		new FunctionDialog(mContext, mVideoSquareInfo.mVideoEntity.videoid, mIsDel, mListener).show();
	}

}
