package cn.com.mobnote.golukmobile.videosuqare;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.util.SoundUtils;
import cn.com.tiros.debug.GolukDebugUtils;

@SuppressLint("Instantiatable")
public class VideoSquareActivity implements OnClickListener {
	public VideoSquareAdapter mVideoSquareAdapter = null;
	private ViewPager mViewPager = null;
	private ImageView hot = null;
	private TextView hotTitle = null;
	private TextView squareTitle = null;
	public String shareVideoId;

	RelativeLayout mRootLayout = null;
	Context mContext = null;

	private float density;

	RelativeLayout.LayoutParams lineParams = null;
	private int lineTop = 0;
	private int textColorSelect = 0;
	private int textcolorQx = 0;

	public VideoSquareActivity(RelativeLayout rootlayout, Context context) {
		mRootLayout = rootlayout;
		mContext = context;
		density = SoundUtils.getInstance().getDisplayMetrics().density;
		lineParams = new RelativeLayout.LayoutParams((int) (50 * density), (int) (2 * density));
		lineTop = (int) (5 * density);
		textColorSelect = mContext.getResources().getColor(R.color.textcolor_select);
		textcolorQx = mContext.getResources().getColor(R.color.textcolor_qx);
		init();

	}

	public VideoSquareAdapter getVideoSquareAdapter() {
		return mVideoSquareAdapter;
	}

	public void init() {
		mViewPager = (ViewPager) mRootLayout.findViewById(R.id.mViewpager);
		mViewPager.setOffscreenPageLimit(3);
		mVideoSquareAdapter = new VideoSquareAdapter(mContext);
		mViewPager.setAdapter(mVideoSquareAdapter);
		mViewPager.setOnPageChangeListener(opcl);
		hot = (ImageView) mRootLayout.findViewById(R.id.line_hot);
		hotTitle = (TextView) mRootLayout.findViewById(R.id.hot_title);
		squareTitle = (TextView) mRootLayout.findViewById(R.id.square_title);
		setListener();
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		mVideoSquareAdapter.onActivityResult(requestCode, resultCode, data);
	}

	private void setListener() {
		hotTitle.setOnClickListener(this);
		squareTitle.setOnClickListener(this);
	}

	// 分享成功后需要调用的接口
	public void shareSucessDeal(boolean isSucess, String channel) {
		if (!isSucess) {
			return;
		}
		GolukApplication.getInstance().getVideoSquareManager().shareVideoUp(channel, shareVideoId);
	}

	private OnPageChangeListener opcl = new OnPageChangeListener() {

		@Override
		public void onPageSelected(int page) {
			GolukDebugUtils.e("", "VideoSquareActivity------AA------------onPageSelected:" + page);
			updateState(page);
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {

			// arg0 :当前页面，及你点击滑动的页面
			// arg1:当前页面偏移的百分比
			// arg2:当前页面偏移的像素位置

			GolukDebugUtils.e("", "VideoSquareActivity------AA------------onPageScrolled: arg0: " + arg0 + "   arg1:"
					+ arg1 + "  arg2:" + arg2);
			if (0 == arg2) {
				return;
			}

			float process = arg1 * 100;
			if (process < 0) {
				process = 0;
			}

			if (process > 99) {
				process = 100;
			}

			updateLine((int) process);
		}

		@Override
		public void onPageScrollStateChanged(int state) {
			GolukDebugUtils.e("", "VideoSquareActivity------AA------------onPageScrollStateChanged: arg0: " + state);

			// 其中state这个参数有三种状态（0，1，2）
			// state ==1的时辰默示正在滑动，
			// state==2的时辰默示滑动完毕了
			// state==0的时辰默示什么都没做。
			// 当页面开始滑动的时候，三种状态的变化顺序为（1，2，0）
		}
	};

	private void updateLine(int process) {
		final int leftMargin = (int) (process * density);
		GolukDebugUtils.e("", "VideoSquareActivity------AA------------updateLine: : " + leftMargin);
		lineParams.addRule(RelativeLayout.BELOW, R.id.hot_title);
		lineParams.setMargins(leftMargin, lineTop, 0, 0);
		hot.setLayoutParams(lineParams);
	}

	private void updateState(int type) {
		if (0 == type) {
			hotTitle.setTextColor(textColorSelect);
			squareTitle.setTextColor(textcolorQx);
		} else if (1 == type) {
			hotTitle.setTextColor(textcolorQx);
			squareTitle.setTextColor(textColorSelect);
		}
	}

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.hot_title:
			mViewPager.setCurrentItem(0);
			this.updateState(0);
			updateLine(0);
			break;
		case R.id.square_title:
			mViewPager.setCurrentItem(1);
			this.updateState(1);
			updateLine(100);
			break;
		case R.id.back_btn:
			exit();
			break;
		default:
			break;
		}
	}

	public void onResume() {
		if (null != mVideoSquareAdapter) {
			mVideoSquareAdapter.onResume();
		}
	}

	public void onPause() {
		if (null != mVideoSquareAdapter) {
			mVideoSquareAdapter.onPause();
		}
	}

	public void onStop() {
		if (null != mVideoSquareAdapter) {
			mVideoSquareAdapter.onStop();
		}
	}

	public void onDestroy() {
		if (null != mVideoSquareAdapter) {
			mVideoSquareAdapter.onDestroy();
		}
	}

	public void exit() {
		if (null != mVideoSquareAdapter) {
			mVideoSquareAdapter.onDestroy();
		}
	}

}
