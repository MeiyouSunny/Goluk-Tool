package cn.com.mobnote.golukmobile.videosuqare;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.BaseActivity;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.util.GolukUtils;

public class VideoCategoryActivity extends BaseActivity implements OnClickListener {
	/** application */
	public GolukApplication mApp = null;
	private static final String TAG = "VideoCategoryActivity";

	/** 类型表示 1表示直播 2 表示直播 */
	public static final String KEY_VIDEO_CATEGORY_TYPE = "key_video_category_type";
	/** 视频分类 例如 1表示曝光台, 2表示随手拍 */
	public static final String KEY_VIDEO_CATEGORY_ATTRIBUTE = "key_video_category_attribute";
	/** 视频分类名称 (例如，曝光台，随手拍) */
	public static final String KEY_VIDEO_CATEGORY_TITLE = "key_video_category_title";

	public static final String CATEGORY_TYPE_LIVE = "1";
	public static final String CATEGORY_TYPE_DB = "2";
	/** 直播类型　默认 attribute属性值 */
	public static final String LIVE_ATTRIBUTE_VALUE = "0";

	private ImageButton mBackBtn = null;
	private TextView mTitleTv;
	public String shareVideoId;
	/** 视频广场类型 */
	private String mType;
	// 点播分类
	private String attribute = "1";
	/** 名称显示 */
	private String mTitle = "";

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

		switchLayout(TYPE_LIST);
	}

	private boolean isLive() {
		return "1".equals(mType);
	}

	private void initViewData() {
		mTitleTv.setText(mTitle);
		FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
				FrameLayout.LayoutParams.MATCH_PARENT);
		mMapBtn.setVisibility(View.GONE);
		if (isLive()) {
			mMapView = new BaiduMapView(this, mApp);
			mSwitchLayout.addView(mMapView.getView(), lp);
			mMapBtn.setVisibility(View.VISIBLE);
		}

		mSwitchLayout.addView(mCategoryLayout.getView(), lp);
	}

	private void getIntentData() {
		Intent intent = getIntent();
		mType = intent.getStringExtra(KEY_VIDEO_CATEGORY_TYPE);// 视频广场类型
		attribute = intent.getStringExtra(KEY_VIDEO_CATEGORY_ATTRIBUTE);// 点播类型
		mTitle = intent.getStringExtra(KEY_VIDEO_CATEGORY_TITLE);
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
		if (null != mMapView) {
			mMapView.onDestroy();
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
		case R.id.square_type_default:
			if (null != mCategoryLayout) {
				mCategoryLayout.firstRequest();
			}
			break;
		default:
			break;
		}
	}

	public void pointDataCallback(int success, Object obj) {
		if (!this.isLive()) {
			// 当前不是直播界面，不需更新数据
			return;
		}
		this.mMapView.pointDataCallback(success, obj);
	}

	public void downloadBubbleImageCallBack(int success, Object obj) {
		if (this.isLive() && null != mMapView) {
			mMapView.downloadBubbleImageCallBack(success, obj);
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
	protected void onPause() {
		super.onPause();

		if (null != mMapView) {
			mMapView.onPause();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		mApp.setContext(this, TAG);
		if (null != mCategoryLayout) {
			mCategoryLayout.onResume();
		}
		if (null != mMapView) {
			mMapView.onResume();
		}
	}
}
