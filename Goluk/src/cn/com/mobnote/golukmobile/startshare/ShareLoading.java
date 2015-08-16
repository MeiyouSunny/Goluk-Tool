package cn.com.mobnote.golukmobile.startshare;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewParent;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.videosuqare.RingView;
import cn.com.mobnote.util.GolukUtils;

public class ShareLoading implements OnClickListener, OnTouchListener {

	private final String STR_STATE_CREATE_VIDEO = "视频生成中";
	private final String STR_STATE_UPLOAD = "视频上传中";
	private final String STR_STATE_GET_SHARE = "获取分享连接...";
	private final String STR_STATE_SHARING = "正在分享...";

	public static final int STATE_NONE = 0;
	public static final int STATE_CREATE_VIDEO = 1;
	public static final int STATE_UPLOAD = 2;
	public static final int STATE_GET_SHARE = 3;
	public static final int STATE_SHAREING = 4;

	private Context mContext = null;
	private RelativeLayout mRootLayout = null;
	private RelativeLayout mLoadingLayout = null;
	private LayoutInflater mLayoutFlater = null;
	private RingView mRingView = null;
	private TextView mLoadingTv = null;
	private TextView mLoadingProress = null;
	private LinearLayout mTopLayout = null;

	private int mCurrentState = STATE_NONE;

	public ShareLoading(Context context, RelativeLayout root) {
		this.mContext = context;
		mRootLayout = root;
		mLayoutFlater = LayoutInflater.from(mContext);
		initView();
	}

	private void initView() {
		mLoadingLayout = (RelativeLayout) mLayoutFlater.inflate(R.layout.shareloading, null);
		mRingView = (RingView) mLoadingLayout.findViewById(R.id.share_loading_RingView);
		mLoadingTv = (TextView) mLoadingLayout.findViewById(R.id.share_loading_txt);
		mLoadingProress = (TextView) mLoadingLayout.findViewById(R.id.share_loading_process);
		mTopLayout = (LinearLayout) mLoadingLayout.findViewById(R.id.share_loading_top);
		mTopLayout.setOnClickListener(this);
	}

	public int getCurrentState() {
		return mCurrentState;
	}

	public void switchState(int state) {
		if (mIsExit) {
			return;
		}
		mCurrentState = state;
		switch (mCurrentState) {
		case STATE_CREATE_VIDEO:
			mLoadingTv.setText(STR_STATE_CREATE_VIDEO);
			mLoadingProress.setVisibility(View.VISIBLE);
			mLoadingProress.setText("0%");
			setProcess(0);
			break;
		case STATE_UPLOAD:
			mLoadingTv.setText(STR_STATE_UPLOAD);
			mLoadingProress.setVisibility(View.VISIBLE);
			mLoadingProress.setText("0%");
			setProcess(0);
			break;
		case STATE_GET_SHARE:
			mLoadingTv.setText(STR_STATE_GET_SHARE);
			mLoadingProress.setVisibility(View.GONE);
			setProcess(0);
			break;
		case STATE_SHAREING:
			mLoadingTv.setText(STR_STATE_SHARING);
			mLoadingProress.setVisibility(View.GONE);
			setProcess(0);
			break;
		default:
			break;
		}
	}

	// 更新内容
	public void updateLoadingTxt(String content) {
		if (mIsExit) {
			return;
		}
		mLoadingTv.setText(content);
	}

	public void setProcess(int process) {
		if (mIsExit) {
			return;
		}
		mRingView.setProcess(process);
		mLoadingProress.setText(process + "%");
	}

	public void showLoadingLayout() {
		if (mIsExit) {
			return;
		}
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
				RelativeLayout.LayoutParams.MATCH_PARENT);
		removeIt();
		mRootLayout.addView(mLoadingLayout, lp);
		mLoadingLayout.setOnTouchListener(this);
	}

	private void removeIt() {
		if (null != mLoadingLayout) {
			ViewParent vp = mLoadingLayout.getParent();
			if (null != vp && vp instanceof RelativeLayout) {
				((RelativeLayout) vp).removeView(mLoadingLayout);
			}
		}
	}

	public void hide() {
		mRootLayout.removeView(mLoadingLayout);
	}

	@Override
	public void onClick(View v) {
		if (mIsExit) {
			return;
		}
		if (v.getId() == R.id.share_loading_top) {
			if (null != mContext && mContext instanceof VideoEditActivity) {
				((VideoEditActivity) mContext).CallBack_Comm(VideoEditActivity.EVENT_COMM_EXIT, null);
			}
		}

	}

	private boolean mIsExit = false;

	public void setExit() {
		mIsExit = true;
	}

	@Override
	public boolean onTouch(View v, MotionEvent arg1) {
		if (mLoadingLayout == v) {
			return true;
		}
		return false;
	}

}
