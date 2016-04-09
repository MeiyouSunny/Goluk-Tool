package com.mobnote.golukmain.followed;

import com.mobnote.golukmain.followed.bean.FollowedVideoObjectBean;
import com.mobnote.golukmain.newest.FunctionDialog;
import com.mobnote.golukmain.newest.IDialogDealFn;
import com.mobnote.golukmain.videosuqare.VideoSquareInfo;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;

public class VideoItemFunctionClickListener implements OnClickListener {
	private FollowedVideoObjectBean mVideoSquareInfo;
	private Context mContext;
	private boolean mIsDel = false;
	private IDialogDealFn mListener = null;
	/** 是否可以举报 */
	private boolean isCanConfirm = true;

	public VideoItemFunctionClickListener(Context context, FollowedVideoObjectBean info, boolean isDel, IDialogDealFn fn) {
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
	public VideoItemFunctionClickListener setConfirm(boolean isConfirm) {
		isCanConfirm = isConfirm;
		return this;
	}

	@Override
	public void onClick(View v) {
		new FunctionDialog(mContext, mVideoSquareInfo.video.videoid, mIsDel, mListener).setConfirm(isCanConfirm)
				.show();
	}

}
