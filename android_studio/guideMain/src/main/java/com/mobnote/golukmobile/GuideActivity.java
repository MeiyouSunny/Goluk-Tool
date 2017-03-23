package com.mobnote.golukmobile;

import com.elvishew.xlog.XLog;
import com.mobnote.application.GolukApplication;
import com.mobnote.eventbus.EventStartApp;
import com.mobnote.golukmain.BaseActivity;
import com.mobnote.golukmain.MainActivity;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.UserStartActivity;
import com.mobnote.golukmain.xdpush.GolukNotification;
import com.mobnote.golukmain.xdpush.StartAppBean;
import com.mobnote.guide.GolukGuideManage;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import cn.com.tiros.baidu.BaiduLocation;
import cn.com.tiros.debug.GolukDebugUtils;
import de.greenrobot.event.EventBus;

/**
 * 
 * @ 功能描述:Goluk引导页
 * 
 * @author 陈宣宇
 * 
 */
@SuppressLint("HandlerLeak")
public class GuideActivity extends BaseActivity {
	public static final String KEY_WEB_START = "web_start_app";
	/** 上下文 */
	private Context mContext = null;
	/** 引导页管理类 */
	private GolukGuideManage mGolukGuideManage = null;

	private String mPushFrom = null;
	private String mPushJson = "";
	private StartAppBean mStartAppBean = null;
	private GolukApplication mApp = null;
	/** 为了判断网页启动App，当前程序是否启动 */
	private boolean mPreExist = true;
	private RelativeLayout mBackground;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		GolukDebugUtils.e("", "start App: GuideActivity:------------: taskid: " + this.getTaskId());
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		mApp = (GolukApplication) getApplication();
		mPreExist = mApp.isExit();
		super.onCreate(savedInstanceState);
		setContentView(R.layout.guide);
		XLog.i("guide on create");
		mContext = this;
		GolukApplication.getInstance().setContext(this, "GuideActivity");
		mBackground = (RelativeLayout) findViewById(R.id.ry_guide_background_layout);
		getIntentData();
		boolean isExit = getWebStartData();
		if (isExit) {
			return;
		}
//		BaiduLocation.mServerFlag = GolukApplication.getInstance().isMainland();
		GolukDebugUtils.e("", "-------------GuideActivity-------------isMainland: "
				+ GolukApplication.getInstance().isMainland() + "--------------BaiduLocation.mServerFlag: "
				+ BaiduLocation.mServerFlag);
		((GolukApplication) this.getApplication()).initLogic();
		// 注册信鸽的推送
		GolukNotification.getInstance().createXG();
		((GolukApplication) this.getApplication()).startUpgrade();
		// 初始化
		init();
	}

	@Override
	protected void onResume() {
		super.onResume();
		GolukApplication.getInstance().setContext(this, "GuideActivity");
	}

	private void getIntentData() {
		Intent intent = getIntent();
		mPushFrom = intent.getStringExtra(GolukNotification.NOTIFICATION_KEY_FROM);
		if (null != mPushFrom && !"".equals(mPushFrom) && mPushFrom.equals("notication")) {
			mPushJson = intent.getStringExtra(GolukNotification.NOTIFICATION_KEY_JSON);
		}
		GolukDebugUtils.e("", "jyf----GuideActivity-----from: " + mPushFrom + "  json:" + mPushJson);
	}

	private boolean getWebStartData() {
		Intent intent = getIntent();
		mStartAppBean = (StartAppBean) intent.getSerializableExtra(GuideActivity.KEY_WEB_START);
		if (null != mStartAppBean) {
			if (!mPreExist) {
				EventBus.getDefault().post(new EventStartApp(100, mStartAppBean));
				startMain();
				finish();
				return true;
			}
		}
		return false;
	}

	private void startMain() {
		GolukDebugUtils.e("", "start App---GuideActivity------startMain----");
		Intent intent = new Intent(this, MainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		addWebStartData(intent);
		this.startActivity(intent);
	}

	private boolean isFirstStart() {
		// 读取SharedPreFerences中需要的数据,使用SharedPreFerences来记录程序启动的使用次数
		SharedPreferences preferences = getSharedPreferences("golukmark", MODE_PRIVATE);
		// 取得相应的值,如果没有该值,说明还未写入,用true作为默认值
		return preferences.getBoolean("isfirst", true);
	}

	/**
	 * 当启动主界面的时候，添加推送标志，用于在主界面执行推送动作
	 * 
	 * @param intent
	 *            启动主界面的动作
	 * @author jyf
	 */
	private void addPushData(Intent intent) {
		if (null == mPushFrom) {
			return;
		}
		GolukDebugUtils.e("", "jyf----GuideActivity--addPushData---from: " + mPushFrom + "  json:" + mPushJson);
		intent.putExtra(GolukNotification.NOTIFICATION_KEY_FROM, mPushFrom);
		intent.putExtra(GolukNotification.NOTIFICATION_KEY_JSON, mPushJson);
	}

	public void addWebStartData(Intent intent) {
		if (null != this.mStartAppBean) {
			intent.putExtra(KEY_WEB_START, mStartAppBean);
		}
	}

	/**
	 * 页面初始化,获取页面元素,注册事件
	 */
	private void init() {
		// 判断程序是否第一次启动
		if (!isFirstStart()) {// 启动过
			// 读取SharedPreference中用户的信息
			SharedPreferences mPreferences = getSharedPreferences("firstLogin", MODE_PRIVATE);
			boolean isFirstLogin = mPreferences.getBoolean("FirstLogin", true);
			// 判断是否是第一次登录
			if (!isFirstLogin) {
				// 登录过，跳转到地图首页进行自动登录
				GolukDebugUtils.e("", "start App ------ GuideActivity-----init:");
				Intent it = new Intent(this, MainActivity.class);
				addPushData(it);
				addWebStartData(it);
				startActivity(it);
			} else {
				// 是第一次登录(没有登录过)
				Intent intent = new Intent(this, UserStartActivity.class);
				addWebStartData(intent);
				startActivity(intent);
			}
			this.finish();
		} else {// 没有启动过
			initViewPager();
		}
	}

	/**
	 * 初始化ViewPager
	 */
	public void initViewPager() {
		this.mGolukGuideManage = new GolukGuideManage(mContext);
		this.mGolukGuideManage.initGolukGuide();
		mBackground.setBackgroundColor(Color.WHITE);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (null != mGolukGuideManage) {
			mGolukGuideManage.destoryImage();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (null != mBaseApp) {
				mBaseApp.setExit(true);
				mBaseApp.destroyLogic();
				mBaseApp.appFree();
			}
			finish();
		}
		return false;
	}

}
