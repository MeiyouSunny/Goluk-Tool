package com.mobnote.golukmobile;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.mobnote.application.GolukApplication;
import com.mobnote.eventbus.EventStartApp;
import com.mobnote.golukmain.MainActivity;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.UserStartActivity;
import com.mobnote.golukmain.xdpush.GolukNotification;
import com.mobnote.golukmain.xdpush.StartAppBean;
import com.mobnote.guide.GolukGuideManage;
import com.mobnote.permission.GolukPermissionUtils;

import java.util.List;

import cn.com.tiros.baidu.BaiduLocation;
import cn.com.tiros.debug.GolukDebugUtils;
import de.greenrobot.event.EventBus;
import pub.devrel.easypermissions.EasyPermissions;

/**
 *
 * @ 功能描述:Goluk引导页
 *
 * @author 陈宣宇
 *
 */
@SuppressLint("HandlerLeak")
public class GuideActivity extends FragmentActivity implements EasyPermissions.PermissionCallbacks {

	public static final String KEY_WEB_START = "web_start_app";

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
		mApp = GolukApplication.getInstance();
		mPreExist = mApp.isExit();
		super.onCreate(savedInstanceState);
		setContentView(R.layout.guide);
		if (shouldRequestUserPermission()){
			requestUserPermission();
			return;
		}

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
		if (mApp == null) return;
		mApp.initializeSDK();
		mApp.setContext(this, "GuideActivity");
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
		mApp.initLogic();
		// 注册信鸽的推送
		GolukNotification.getInstance().createXG();
		mApp.startUpgrade();
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
		this.mGolukGuideManage = new GolukGuideManage(this);
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
			if (null != mApp) {
				mApp.setExit(true);
				mApp.destroyLogic();
				mApp.appFree();
			}
			finish();
		}
		return false;
	}

	private boolean shouldRequestUserPermission() {
		return !GolukPermissionUtils.hasIndispensablePermission(this);
	}

	private void requestUserPermission() {
		GolukPermissionUtils.requestPermissions(this, new String[]{
				Manifest.permission.WRITE_EXTERNAL_STORAGE,
				Manifest.permission.ACCESS_COARSE_LOCATION,
				Manifest.permission.ACCESS_FINE_LOCATION,
				Manifest.permission.READ_PHONE_STATE,
		});
	}

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode,permissions,grantResults,this);
    }

	@Override
	public void onPermissionsGranted(int requestCode, List<String> perms) {
		if (requestCode == GolukPermissionUtils.CODE_REQUEST_PERMISSION && !shouldRequestUserPermission()) {
			init();
		}
	}

	@Override
	public void onPermissionsDenied(int requestCode, List<String> perms) {
		GolukPermissionUtils.handlePermissionPermanentlyDenied(this, perms);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == GolukPermissionUtils.CODE_REQUEST_PERMISSION) {
			if (resultCode == Activity.RESULT_CANCELED) {
				finish();
			} else if (resultCode ==Activity.RESULT_OK){
				if (shouldRequestUserPermission()) {
					finish();
				} else {
					init();
				}
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
}
