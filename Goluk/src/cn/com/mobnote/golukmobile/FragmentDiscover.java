package cn.com.mobnote.golukmobile;

import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.eventbus.EventConfig;
import cn.com.mobnote.eventbus.EventLocationFinish;
import cn.com.mobnote.golukmobile.carrecorder.util.SoundUtils;
import cn.com.mobnote.golukmobile.newest.WonderfulSelectedListView;
import cn.com.mobnote.golukmobile.videosuqare.VideoSquareAdapter;
import cn.com.mobnote.util.SharedPrefUtil;
import cn.com.tiros.debug.GolukDebugUtils;
import de.greenrobot.event.EventBus;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class FragmentDiscover extends Fragment implements OnClickListener {
	private static final String TAG = "FragmentDiscover";

	public FragmentDiscover() {

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		EventBus.getDefault().register(this);
	}

	View mSquareRootView;
	private String mCityCode;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View rootView = LayoutInflater.from(getActivity()).inflate(R.layout.video_square_main,
				null);
		mSquareRootView = rootView;
		density = SoundUtils.getInstance().getDisplayMetrics().density;
		lineParams = new RelativeLayout.LayoutParams((int) (50 * density), (int) (2 * density));
		lineTop = (int) (5 * density);
		textColorSelect = getActivity().getResources().getColor(R.color.textcolor_select);
		textcolorQx = getActivity().getResources().getColor(R.color.textcolor_qx);
		init();
		mBannerLoaded = false;
		mCityCode = SharedPrefUtil.getCityIDString();
		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
	}

	public VideoSquareAdapter mVideoSquareAdapter = null;
	private ViewPager mViewPager = null;
	private ImageView hot = null;
	private TextView hotTitle = null;
	private TextView squareTitle = null;
	public String shareVideoId;

//	LinearLayout mRootLayout = null;
///	Context mContext = null;

	private float density;

	RelativeLayout.LayoutParams lineParams = null;
	private int lineTop = 0;
	private int textColorSelect = 0;
	private int textcolorQx = 0;

//	public VideoSquareActivity(LinearLayout rootlayout, Context context) {
//		mRootLayout = rootlayout;
//		mContext = context;
//		density = SoundUtils.getInstance().getDisplayMetrics().density;
//		lineParams = new RelativeLayout.LayoutParams((int) (50 * density), (int) (2 * density));
//		lineTop = (int) (5 * density);
//		textColorSelect = mContext.getResources().getColor(R.color.textcolor_select);
//		textcolorQx = mContext.getResources().getColor(R.color.textcolor_qx);
//		init();
//
//	}

	public VideoSquareAdapter getVideoSquareAdapter() {
		return mVideoSquareAdapter;
	}

	public void init() {
		mViewPager = (ViewPager) mSquareRootView.findViewById(R.id.mViewpager);
		mViewPager.setOffscreenPageLimit(3);
		mVideoSquareAdapter = new VideoSquareAdapter(getActivity());
		mViewPager.setAdapter(mVideoSquareAdapter);
		mViewPager.setOnPageChangeListener(opcl);
		hot = (ImageView) mSquareRootView.findViewById(R.id.line_hot);
		hotTitle = (TextView) mSquareRootView.findViewById(R.id.hot_title);
		squareTitle = (TextView) mSquareRootView.findViewById(R.id.square_title);
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

	private boolean mBannerLoaded;

	public void onEventMainThread(EventLocationFinish event) {
		if (null == event) {
			return;
		}

		switch (event.getOpCode()) {
		case EventConfig.LOCATION_FINISH:
			Log.d(TAG, "Location Finished: " + event.getCityCode());
			// Start load banner
//			VideoSquareAdapter videoSquareAdapter = mVideoSquareActivity.getVideoSquareAdapter();
//			FragmentDiscover fragmentDiscover = (FragmentDiscover)getSupportFragmentManager().findFragmentByTag("Discover");
//			FragmentDiscover fragmentDiscover = (FragmentDiscover)getSupportFragmentManager()
//					.findFragmentByTag("tabsfragment")
//					.getChildFragmentManager().findFragmentByTag("Discover");
			VideoSquareAdapter videoSquareAdapter = getVideoSquareAdapter();
			if (null == videoSquareAdapter) {
				return;
			}
			WonderfulSelectedListView listView = videoSquareAdapter.getWonderfulSelectedListView();

			if (null == listView) {
				return;
			}

			if (!mBannerLoaded) {
				Log.d(TAG, "Activity first start, fill everything anyway");
				if (event.getCityCode().equals("-1")) {
					if (null == mCityCode || mCityCode.trim().equals("")) {
						mCityCode = event.getCityCode();
						SharedPrefUtil.setCityIDString(mCityCode);
						listView.loadBannerData(mCityCode);
					} else {
						listView.loadBannerData(mCityCode);
					}
				} else {
					mCityCode = event.getCityCode();
					SharedPrefUtil.setCityIDString(mCityCode);
					listView.loadBannerData(mCityCode);
				}
				mBannerLoaded = true;
			}

			if (null == mCityCode || mCityCode.trim().equals("")) {
				Log.d(TAG, "First located, fill everything anyway");
				mCityCode = event.getCityCode();
				SharedPrefUtil.setCityIDString(mCityCode);
				listView.loadBannerData(mCityCode);
			} else {
				// In whole nation
				if ("-1".equals(mCityCode)) {
					if (event.getCityCode().equals("-1")) {
						// do nothing
					} else {
						mCityCode = event.getCityCode();
						SharedPrefUtil.setCityIDString(mCityCode);
						listView.loadBannerData(mCityCode);
					}
				} else { // In city
					if (event.getCityCode().equals("-1")) {
						// do nothing
					} else {
						if (!mCityCode.equals(event.getCityCode())) {
							mCityCode = event.getCityCode();
							SharedPrefUtil.setCityIDString(mCityCode);
							listView.loadBannerData(mCityCode);
						}
					}
				}
			}

			break;
		default:
			break;
		}
	}

//	@Override
//	public void onResume() {
//		if (null != mVideoSquareAdapter) {
//			mVideoSquareAdapter.onResume();
//		}
//	}
//
//	public void onPause() {
//		if (null != mVideoSquareAdapter) {
//			mVideoSquareAdapter.onPause();
//		}
//	}
//
//	public void onStop() {
//		if (null != mVideoSquareAdapter) {
//			mVideoSquareAdapter.onStop();
//		}
//	}

	@Override
	public void onDestroy() {
		super.onDestroy();
//		if (null != mVideoSquareAdapter) {
//			mVideoSquareAdapter.onDestroy();
//		}
		mBannerLoaded = false;
		EventBus.getDefault().register(this);
	}

	public void exit() {
		if (null != mVideoSquareAdapter) {
			mVideoSquareAdapter.onDestroy();
		}
	}


}
