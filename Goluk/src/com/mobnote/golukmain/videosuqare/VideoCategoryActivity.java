package com.mobnote.golukmain.videosuqare;

import com.mobnote.application.GolukApplication;
import com.mobnote.eventbus.EventConfig;
import com.mobnote.eventbus.EventDeleteVideo;
import com.mobnote.eventbus.EventPraiseStatusChanged;
import com.mobnote.golukmain.BaseActivity;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.newest.NewestListView;
import com.mobnote.util.GolukUtils;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import cn.com.tiros.debug.GolukDebugUtils;
import de.greenrobot.event.EventBus;

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

		EventBus.getDefault().register(this);
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

	/** 防止重复点击　 */
	private boolean isPressBack = false;

	private void back() {
		if (isPressBack) {
			return;
		}
		isPressBack = true;
		GolukDebugUtils.e("", "jyf----VideoCategoryActivity------back ----111");
		if (null != mCategoryLayout) {
			mCategoryLayout.onDestroy();
		}

		GolukDebugUtils.e("", "jyf----VideoCategoryActivity------back ----2222");
		if (null != mMapView) {
			mMapView.onDestroy();
		}

		GolukDebugUtils.e("", "jyf----VideoCategoryActivity------back ----3333");
		// mSwitchLayout.removeAllViews();

		GolukDebugUtils.e("", "jyf----VideoCategoryActivity------back ----4444");
		this.finish();

		GolukDebugUtils.e("", "jyf----VideoCategoryActivity------back ----5555");
	}

	@Override
	public void onClick(View view) {
		int id = view.getId();
		if (id == R.id.back_btn) {
			back();
		} else if (id == R.id.category_map) {
			if (isLive()) {
				int temp = mCurrentType == TYPE_LIST ? TYPE_MAP : TYPE_LIST;
				this.switchLayout(temp);
			}
		} else {
		}
	}

	public void onEventMainThread(EventPraiseStatusChanged event) {
		if (null == event) {
			return;
		}

		switch (event.getOpCode()) {
		case EventConfig.PRAISE_STATUS_CHANGE:
			mCategoryLayout.changePraiseStatus(event.isStatus(), event.getVideoId());
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
			GolukUtils.showToast(VideoCategoryActivity.this, this.getString(R.string.str_third_share_fail));
			return;
		}
		GolukApplication.getInstance().getVideoSquareManager().shareVideoUp(channel, shareVideoId);
	}

	// 删除视频
	public void onEventMainThread(EventDeleteVideo event) {
		if (EventConfig.VIDEO_DELETE == event.getOpCode()) {
			final String delVid = event.getVid(); // 已经删除的id
			mCategoryLayout.deleteVideo(delVid);
		}
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

	@Override
	protected void onPause() {
		super.onPause();
		if (null != mCategoryLayout) {
			mCategoryLayout.onPause();
		}
		if (null != mMapView) {
			// mMapView.onPause();
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
	protected void onDestroy() {
		super.onDestroy();
		EventBus.getDefault().unregister(this);
	}

}
