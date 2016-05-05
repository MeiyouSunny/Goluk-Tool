package com.mobnote.golukmain.newest;

import com.mobnote.golukmain.videosuqare.VideoSquareInfo;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;

public class ClickFunctionListener implements OnClickListener {
	private VideoSquareInfo mVideoSquareInfo;
	private Context mContext;
	private boolean mIsDel = false;
	private IDialogDealFn mListener = null;
	/** 是否可以举报 */
	private boolean isCanConfirm = true;

	public ClickFunctionListener(Context context, VideoSquareInfo info, boolean isDel, IDialogDealFn fn) {
		this.mVideoSquareInfo = info;
		this.mContext = context;
		mListener = fn;
		mIsDel = isDel;
	}

	/**
	 * 设置是否可以举报
	 * 
	 * @author jyf
	 */
	public ClickFunctionListener setConfirm(boolean isConfirm) {
		isCanConfirm = isConfirm;
		return this;
	}

	@Override
	public void onClick(View v) {
		new FunctionDialog(mContext, mVideoSquareInfo.mVideoEntity.videoid, mIsDel, mListener).setConfirm(isCanConfirm)
				.show();
	}

}
