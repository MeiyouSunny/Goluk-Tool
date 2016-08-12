package com.mobnote.golukmain.newest;

import com.mobnote.golukmain.BaseActivity;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.UserOpenUrlActivity;
import com.mobnote.golukmain.cluster.ClusterActivity;
import com.mobnote.golukmain.special.ClusterListActivity;
import com.mobnote.golukmain.special.SpecialListActivity;
import com.mobnote.golukmain.videodetail.VideoDetailActivity;
import com.mobnote.util.GolukConfig;
import com.mobnote.util.ZhugeUtils;

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
import android.widget.RelativeLayout;
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
			intent = new Intent(mContext, ClusterActivity.class);
			intent.putExtra(ClusterActivity.CLUSTER_KEY_ACTIVITYID,
					mJXListItemDataInfo.tagid);
			String topName = "#" + mJXListItemDataInfo.ztitle;
			intent.putExtra(ClusterActivity.CLUSTER_KEY_TITLE, topName);
		} else if ("3".equals(mJXListItemDataInfo.ztype)) {// 单视频
			//视频详情页访问
			ZhugeUtils.eventVideoDetail(mContext, mContext.getString(R.string.str_zhuge_wonderful_event));
			intent = new Intent(mContext, VideoDetailActivity.class);
			intent.putExtra(VideoDetailActivity.TYPE, "Wonderful");
			intent.putExtra("ztid", mJXListItemDataInfo.ztid);
			intent.putExtra("imageurl", mJXListItemDataInfo.jximg);
			intent.putExtra("title", mJXListItemDataInfo.ztitle);
		} else if ("4".equals(mJXListItemDataInfo.ztype)) {// url
			String url = mJXListItemDataInfo.adverturl;
			intent = new Intent(mContext, UserOpenUrlActivity.class);
			intent.putExtra(GolukConfig.NEED_H5_TITLE, mJXListItemDataInfo.ztitle);
			intent.putExtra(GolukConfig.WEB_TYPE, GolukConfig.NEED_SHARE);
			intent.putExtra(GolukConfig.H5_URL, url);
			intent.putExtra(GolukConfig.URL_OPEN_PATH, "wonderful_list");
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
