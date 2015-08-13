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
//	private ImageView square = null;
//	private LinearLayout mVideoList = null;
//	private LinearLayout mTypeList = null;
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
//		square = (ImageView) mRootLayout.findViewById(R.id.line_square);

//		mVideoList = (LinearLayout) mRootLayout.findViewById(R.id.mVideoList);
//		mTypeList = (LinearLayout) mRootLayout.findViewById(R.id.mTypeList);

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
		public void onPageSelected(int arg0) {
			updateState(arg0);
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
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
			
			updateLine((int)process);
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {
		}
	};
	
	private void updateLine(int process) {
		RelativeLayout.LayoutParams lineParams = new RelativeLayout.LayoutParams(50*3, 2*3);
		lineParams.addRule(RelativeLayout.BELOW, R.id.hot_title);
		lineParams.setMargins(process*3, 5*3, 0, 0);
		hot.setLayoutParams(lineParams);
	}

	private void updateState(int type) {
		if (0 == type) {
//			updateLine(0);
//			hot.setVisibility(View.VISIBLE);
//			square.setVisibility(View.INVISIBLE);

			hotTitle.setTextColor(mContext.getResources().getColor(R.color.textcolor_select));
			squareTitle.setTextColor(mContext.getResources().getColor(R.color.textcolor_qx));
		} else if (1 == type) {
//			updateLine(100);
//			square.setVisibility(View.VISIBLE);
//			hot.setVisibility(View.INVISIBLE);

			hotTitle.setTextColor(mContext.getResources().getColor(R.color.textcolor_qx));
			squareTitle.setTextColor(mContext.getResources().getColor(R.color.textcolor_select));

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
