package cn.com.mobnote.golukmobile.videosuqare;

import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.MainActivity;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomLoadingDialog;
import cn.com.mobnote.golukmobile.thirdshare.SharePlatformUtil;
import cn.com.tiros.debug.GolukDebugUtils;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

@SuppressLint("Instantiatable")
public class VideoSquareActivity implements OnClickListener {
	public VideoSquareAdapter mVideoSquareAdapter = null;
	private ViewPager mViewPager = null;
	private ImageView hot = null;
	private ImageView square = null;
	private LinearLayout mVideoList = null;
	private LinearLayout mTypeList = null;
	private TextView hotTitle = null;
	private TextView squareTitle = null;
	public CustomLoadingDialog mCustomProgressDialog;
	public String shareVideoId;

	RelativeLayout mRootLayout = null;
	Context mContext = null;
	MainActivity ma = null;

	public VideoSquareActivity(RelativeLayout rootlayout, Context context) {
		mRootLayout = rootlayout;
		mContext = context;
		init();
	}

	public void init() {
		ma = (MainActivity) mContext;
		mViewPager = (ViewPager) mRootLayout.findViewById(R.id.mViewpager);
		mViewPager.setOffscreenPageLimit(3);
		mVideoSquareAdapter = new VideoSquareAdapter(mContext, null);
		mViewPager.setAdapter(mVideoSquareAdapter);
		mViewPager.setOnPageChangeListener(opcl);

		hot = (ImageView) mRootLayout.findViewById(R.id.line_hot);
		square = (ImageView) mRootLayout.findViewById(R.id.line_square);

		mVideoList = (LinearLayout) mRootLayout.findViewById(R.id.mVideoList);
		mTypeList = (LinearLayout) mRootLayout.findViewById(R.id.mTypeList);

		hotTitle = (TextView) mRootLayout.findViewById(R.id.hot_title);
		squareTitle = (TextView) mRootLayout.findViewById(R.id.square_title);

		// squareImage = (ImageView)
		// mRootLayout.findViewById(R.id.square_image);
		// hotImage = (ImageView) mRootLayout.findViewById(R.id.hot_image);
		setListener();
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		mVideoSquareAdapter.onActivityResult( requestCode,  resultCode,  data);
	}

	private void setListener() {
		mVideoList.setOnClickListener(this);
		mTypeList.setOnClickListener(this);
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
		public void onPageSelected(int arg0) {
			updateState(arg0);
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
			GolukDebugUtils.e("", "BBBBBB===onPageScrolled==arg0="+arg0+"===arg1="+arg1+"==arg2="+arg2);
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {
		}
	};

	private void updateState(int type) {
		if (0 == type) {
			hot.setVisibility(View.VISIBLE);
			square.setVisibility(View.INVISIBLE);

			hotTitle.setTextColor(mContext.getResources().getColor(R.color.textcolor_select));
			squareTitle.setTextColor(mContext.getResources().getColor(R.color.textcolor_qx));
		} else if (1 == type) {

			square.setVisibility(View.VISIBLE);
			hot.setVisibility(View.INVISIBLE);

			hotTitle.setTextColor(mContext.getResources().getColor(R.color.textcolor_qx));
			squareTitle.setTextColor(mContext.getResources().getColor(R.color.textcolor_select));

		} 
	}

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.mVideoList:
			mViewPager.setCurrentItem(0);
			this.updateState(0);
			break;
		case R.id.mTypeList:
			mViewPager.setCurrentItem(1);
			this.updateState(1);
			break;
		case R.id.back_btn:
			exit();
			break;
		default:
			break;
		}
	}

	public void onBackPressed() {
		
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
