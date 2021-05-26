package com.mobnote.golukmain.newest;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mobnote.golukmain.R;

import cn.com.tiros.debug.GolukDebugUtils;

@SuppressLint("ClickableViewAccessibility")
public class ClickWonderfulSelectedListener implements OnTouchListener {
	private JXListItemDataInfo mJXListItemDataInfo;
	private Context mContext;
	private WonderfulSelectedAdapter mWonderfulSelectedAdapter;

	public ClickWonderfulSelectedListener(Context context, JXListItemDataInfo info, WonderfulSelectedAdapter adapter) {
		this.mJXListItemDataInfo = info;
		this.mContext = context;
		this.mWonderfulSelectedAdapter = adapter;
	}

	@Override
	public boolean onTouch(View arg0, MotionEvent arg1) {
		switch (arg1.getAction()) {
		case MotionEvent.ACTION_DOWN:
			hideAnimation(arg0);
			GolukDebugUtils.e("", "BBBBBBB======ACTION_DOWN=======");
			break;
		case MotionEvent.ACTION_UP:
			GolukDebugUtils.e("", "BBBBBBB======ACTION_UP=======");
			showAnimation(arg0);
			jump();
			break;
		case MotionEvent.ACTION_CANCEL:
			GolukDebugUtils.e("", "BBBBBBB======ACTION_CANCEL=======");
			showAnimation(arg0);
			break;

		default:
			break;
		}
		return true;
	}

	private void jump() {
	}

	private void showAnimation(final View view) {
		final RelativeLayout mengban = (RelativeLayout) view.findViewById(R.id.mengban);
		final TextView mTitleName = (TextView) view.findViewById(R.id.mTitleName);
//		final LinearLayout mVideoLayout = (LinearLayout) view.findViewById(R.id.mVideoLayout);
//		final LinearLayout mLookLayout = (LinearLayout) view.findViewById(R.id.mLookLayout);
		final TextView mVideoLayout = (TextView) view.findViewById(R.id.mVideoNum);

		AlphaAnimation show = new AlphaAnimation(0f, 1.0f);
		show.setDuration(300);
		show.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation arg0) {
				if ("-1".equals(mJXListItemDataInfo.clicknumber)) {
					mVideoLayout.setVisibility(View.GONE);
				} else {
					mVideoLayout.setVisibility(View.VISIBLE);
				}
			}

			@Override
			public void onAnimationRepeat(Animation arg0) {
			}

			@Override
			public void onAnimationEnd(Animation arg0) {
				mengban.clearAnimation();
				mTitleName.clearAnimation();
				mVideoLayout.clearAnimation();
//				mLookLayout.clearAnimation();

				mengban.setVisibility(View.VISIBLE);
				mTitleName.setVisibility(View.VISIBLE);
				GolukDebugUtils.e("", "BBBBBBB====videonumber=" + mJXListItemDataInfo.videonumber + "====="
						+ mJXListItemDataInfo.ztitle);
				if ("-1".equals(mJXListItemDataInfo.clicknumber)) {
					mVideoLayout.setVisibility(View.GONE);
				} else {
					mVideoLayout.setVisibility(View.VISIBLE);
				}

//				if ("-1".equals(mJXListItemDataInfo.videonumber)) {
//					mLookLayout.setVisibility(View.GONE);
//				} else {
//					mLookLayout.setVisibility(View.VISIBLE);
//				}
			}
		});

		mengban.startAnimation(show);
		mTitleName.startAnimation(show);
		mVideoLayout.startAnimation(show);
//		mLookLayout.startAnimation(show);
	}

	private void hideAnimation(final View view) {
		final RelativeLayout mengban = (RelativeLayout) view.findViewById(R.id.mengban);
		final TextView mTitleName = (TextView) view.findViewById(R.id.mTitleName);
//		final LinearLayout mVideoLayout = (LinearLayout) view.findViewById(R.id.mVideoLayout);
//		final LinearLayout mLookLayout = (LinearLayout) view.findViewById(R.id.mLookLayout);
		final TextView mVideoLayout = (TextView) view.findViewById(R.id.mVideoNum);

		AlphaAnimation hide = new AlphaAnimation(1.0f, 0f);
		hide.setDuration(300);
		hide.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation arg0) {
			}

			@Override
			public void onAnimationRepeat(Animation arg0) {
			}

			@Override
			public void onAnimationEnd(Animation arg0) {
				mengban.clearAnimation();
				mTitleName.clearAnimation();
				mVideoLayout.clearAnimation();
//				mLookLayout.clearAnimation();

				mengban.setVisibility(View.GONE);
				mTitleName.setVisibility(View.GONE);
				mVideoLayout.setVisibility(View.GONE);
//				mLookLayout.setVisibility(View.GONE);
			}
		});

		mengban.startAnimation(hide);
		mTitleName.startAnimation(hide);
		mVideoLayout.startAnimation(hide);
//		mLookLayout.startAnimation(hide);
	}

}
