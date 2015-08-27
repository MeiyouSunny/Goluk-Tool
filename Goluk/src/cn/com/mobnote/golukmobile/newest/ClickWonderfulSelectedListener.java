package cn.com.mobnote.golukmobile.newest;

import cn.com.mobnote.golukmobile.BaseActivity;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.UserOpenUrlActivity;
import cn.com.mobnote.golukmobile.VideoSquareDeatilActivity;
import cn.com.mobnote.golukmobile.special.ClusterListActivity;
import cn.com.mobnote.golukmobile.special.SpecialListActivity;
import cn.com.tiros.debug.GolukDebugUtils;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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
		Intent intent = null;
		if ("1".equals(mJXListItemDataInfo.ztype)) {// 专题
			intent = new Intent(mContext, SpecialListActivity.class);
			intent.putExtra("ztid", mJXListItemDataInfo.ztid);
			intent.putExtra("title", mJXListItemDataInfo.ztitle);
		} else if ("2".equals(mJXListItemDataInfo.ztype)) {// tag
			intent = new Intent(mContext, ClusterListActivity.class);
			intent.putExtra("ztid", mJXListItemDataInfo.ztid);
			intent.putExtra("title", mJXListItemDataInfo.ztitle);
		} else if ("3".equals(mJXListItemDataInfo.ztype)) {// 单视频
			intent = new Intent(mContext, VideoSquareDeatilActivity.class);
			intent.putExtra("ztid", mJXListItemDataInfo.ztid);
			intent.putExtra("imageurl", mJXListItemDataInfo.jximg);
			intent.putExtra("title", mJXListItemDataInfo.ztitle);
		} else if ("4".equals(mJXListItemDataInfo.ztype)) {// url
			String url = mJXListItemDataInfo.adverturl;
			intent = new Intent(mContext, UserOpenUrlActivity.class);
			intent.putExtra("url", url);
		}
		if (null == intent) {
			return;
		}
		// 防止重复点击
		if (null != mContext && mContext instanceof BaseActivity) {
			if (!((BaseActivity) mContext).isAllowedClicked()) {
				return;
			}
			((BaseActivity) mContext).setJumpToNext();
		}
		mContext.startActivity(intent);
	}

	private void showAnimation(final View view) {
		final ImageView mengban = (ImageView) view.findViewById(R.id.mengban);
		final TextView mTitleName = (TextView) view.findViewById(R.id.mTitleName);
		final LinearLayout mVideoLayout = (LinearLayout) view.findViewById(R.id.mVideoLayout);
		final LinearLayout mLookLayout = (LinearLayout) view.findViewById(R.id.mLookLayout);

		AlphaAnimation show = new AlphaAnimation(0f, 1.0f);
		show.setDuration(300);
		show.setAnimationListener(new AnimationListener() {
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
				mLookLayout.clearAnimation();

				mengban.setVisibility(View.VISIBLE);
				mTitleName.setVisibility(View.VISIBLE);
				GolukDebugUtils.e("", "BBBBBBB====videonumber=" + mJXListItemDataInfo.videonumber + "====="
						+ mJXListItemDataInfo.ztitle);
				if ("-1".equals(mJXListItemDataInfo.clicknumber)) {
					mVideoLayout.setVisibility(View.GONE);
				} else {
					mVideoLayout.setVisibility(View.VISIBLE);
				}

				if ("-1".equals(mJXListItemDataInfo.videonumber)) {
					mLookLayout.setVisibility(View.GONE);
				} else {
					mLookLayout.setVisibility(View.VISIBLE);
				}
			}
		});

		mengban.startAnimation(show);
		mTitleName.startAnimation(show);
		mVideoLayout.startAnimation(show);
		mLookLayout.startAnimation(show);
	}

	private void hideAnimation(final View view) {
		final ImageView mengban = (ImageView) view.findViewById(R.id.mengban);
		final TextView mTitleName = (TextView) view.findViewById(R.id.mTitleName);
		final LinearLayout mVideoLayout = (LinearLayout) view.findViewById(R.id.mVideoLayout);
		final LinearLayout mLookLayout = (LinearLayout) view.findViewById(R.id.mLookLayout);

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
				mLookLayout.clearAnimation();

				mengban.setVisibility(View.GONE);
				mTitleName.setVisibility(View.GONE);
				mVideoLayout.setVisibility(View.GONE);
				mLookLayout.setVisibility(View.GONE);
			}
		});

		mengban.startAnimation(hide);
		mTitleName.startAnimation(hide);
		mVideoLayout.startAnimation(hide);
		mLookLayout.startAnimation(hide);
	}

}
