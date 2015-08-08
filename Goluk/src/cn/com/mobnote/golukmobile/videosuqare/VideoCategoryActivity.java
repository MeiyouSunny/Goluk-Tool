package cn.com.mobnote.golukmobile.videosuqare;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.BaseActivity;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.thirdshare.SharePlatformUtil;
import cn.com.mobnote.module.videosquare.VideoSuqareManagerFn;
import cn.com.mobnote.util.GolukUtils;

public class VideoCategoryActivity extends BaseActivity implements OnClickListener, VideoSuqareManagerFn {
	/** application */
	public GolukApplication mApp = null;
	private static final String TAG = "VideoCategoryActivity";

	private ImageButton mBackBtn = null;
	private TextView mTitleTv;
	public String shareVideoId;
	/** 视频广场类型 */
	private String mType;
	// 点播分类
	private String attribute = "1";

	private SharePlatformUtil sharePlatform;
	private FrameLayout mSwitchLayout = null;
	/** 列表状态 */
	private final int TYPE_LIST = 0;
	/** 当前显示地图 */
	private final int TYPE_MAP = 1;
	private int mCurrentType = TYPE_LIST;

	private ImageButton mMapBtn = null;
	private CategoryListView mCategoryLayout = null;
	private BaiduMapView mMapView = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.video_square_play);

		mApp = (GolukApplication) getApplication();
		mApp.setContext(this, TAG);
		getIntentData();
		initView();
		initViewData();

		sharePlatform = new SharePlatformUtil(this);
		sharePlatform.configPlatforms();// 设置分享平台的参数

		switchLayout(TYPE_LIST);
	}

	private boolean isLive() {
		return "1".equals(mType);
	}

	private void initViewData() {
		if ("1".equals(attribute)) {
			mTitleTv.setText("曝光台");
		} else if ("2".equals(attribute)) {
			mTitleTv.setText("事故大爆料");
		} else if ("3".equals(attribute)) {
			mTitleTv.setText("美丽风景");
		} else if ("4".equals(attribute)) {
			mTitleTv.setText("随手拍");
		} else {
			mTitleTv.setText("直播列表");
		}

		FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
				FrameLayout.LayoutParams.MATCH_PARENT);
		mMapBtn.setVisibility(View.GONE);
		if (isLive()) {
			mMapView = new BaiduMapView(this);
			mSwitchLayout.addView(mMapView.getView(), lp);
			mMapBtn.setVisibility(View.VISIBLE);
		}

		mSwitchLayout.addView(mCategoryLayout.getView(), lp);
	}

	private void getIntentData() {
		Intent intent = getIntent();
		mType = intent.getStringExtra("type");// 视频广场类型
		attribute = intent.getStringExtra("attribute");// 点播类型
	}

	private void initView() {
		mTitleTv = (TextView) findViewById(R.id.title);
		mSwitchLayout = (FrameLayout) findViewById(R.id.category_switchlayout);
		/** 返回按钮 */
		mBackBtn = (ImageButton) findViewById(R.id.back_btn);

		mMapBtn = (ImageButton) findViewById(R.id.category_map);
		mCategoryLayout = new CategoryListView(this, mType, attribute);

		mMapBtn.setOnClickListener(this);
		mBackBtn.setOnClickListener(this);
	}

	private void switchLayout(int type) {
		if (TYPE_LIST == type) {
			mCurrentType = type;
			// 切换到列表页
			mCategoryLayout.getView().setVisibility(View.VISIBLE);
			if (isLive()) {
				mMapView.getView().setVisibility(View.INVISIBLE);
				mMapBtn.setBackgroundResource(R.drawable.btn_live_switch_map);
			}

		} else if (TYPE_MAP == type && isLive()) {
			// 只有直播界面才有地图
			mCurrentType = type;
			// 切换到地图
			mCategoryLayout.getView().setVisibility(View.INVISIBLE);
			mMapView.getView().setVisibility(View.VISIBLE);
			mMapBtn.setBackgroundResource(R.drawable.btn_live_switch_list);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (null != mCategoryLayout) {
			mCategoryLayout.onActivityResult(requestCode, resultCode, data);
		}
	}

	private void back() {
		if (null != mCategoryLayout) {
			mCategoryLayout.onDestroy();
		}
		this.finish();
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.back_btn:
			back();
			break;
		case R.id.category_map:
			if (isLive()) {
				int temp = mCurrentType == TYPE_LIST ? TYPE_MAP : TYPE_LIST;
				this.switchLayout(temp);
			}
			break;
		default:
			break;
		}
	}

	// 分享成功后需要调用的接口
	public void shareSucessDeal(boolean isSucess, String channel) {
		if (!isSucess) {
			GolukUtils.showToast(VideoCategoryActivity.this, "第三方分享失败");
			return;
		}
		GolukApplication.getInstance().getVideoSquareManager().shareVideoUp(channel, shareVideoId);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (KeyEvent.KEYCODE_BACK == keyCode) {
			back();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		if (null != mCategoryLayout) {
			mCategoryLayout.onBackPressed();
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (null != mCategoryLayout) {
			mCategoryLayout.onStop();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (null != mCategoryLayout) {
			mCategoryLayout.onResume();
		}
	}

	@Override
	public void VideoSuqare_CallBack(int event, int msg, int param1, Object param2) {

	}
}
