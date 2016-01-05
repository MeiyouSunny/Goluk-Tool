package cn.com.mobnote.golukmobile;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.eventbus.EventStartApp;
import cn.com.mobnote.golukmobile.wifidatacenter.JsonWifiBindManager;
import cn.com.mobnote.golukmobile.wifidatacenter.WifiBindDataCenter;
import cn.com.mobnote.golukmobile.xdpush.GolukNotification;
import cn.com.mobnote.golukmobile.xdpush.StartAppBean;
import cn.com.mobnote.guide.GolukGuideManage;
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		mApp = (GolukApplication) getApplication();
		mPreExist = mApp.isExit();
		super.onCreate(savedInstanceState);

		setContentView(R.layout.guide);
		mContext = this;
		GolukApplication.getInstance().setContext(this, "GuideActivity");
		getIntentData();
		boolean isExit = getWebStartData();
		if (isExit) {
			return;
		}
		((GolukApplication) this.getApplication()).initLogic();
		// 注册信鸽的推送
		GolukNotification.getInstance().createXG();
		((GolukApplication) this.getApplication()).startUpgrade();
		// 初始化
		init();
		// 初始化绑定信息的数据保存
		WifiBindDataCenter.getInstance().setAdatper(new JsonWifiBindManager());
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
		final String scheme = intent.getScheme(); // golukapp
		GolukDebugUtils.e("", "start App: scheme:" + scheme);
		final Uri uri = intent.getData();
		final String dataStr = intent.getDataString(); // 获取整个字符串
		if (null != scheme && "golukapp".equals(scheme) && null != uri) {
			String host = uri.getHost(); // goluk.app
			String path = uri.getPath();

			String vid = uri.getQueryParameter("id");
			String title = uri.getQueryParameter("title");
			String type = uri.getQueryParameter("type");
			mStartAppBean = new StartAppBean();
			mStartAppBean.uri = uri.toString();
			mStartAppBean.dataStr = dataStr;
			mStartAppBean.host = host;
			mStartAppBean.path = path;

			mStartAppBean.type = type;
			mStartAppBean.id = vid;
			mStartAppBean.title = title;

			GolukDebugUtils.e("", "start App: host:" + host + "  path:" + path + "   dataStr:" + dataStr + "   vid:"
					+ vid + "  mPreExist:" + mPreExist);
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

	private void addWebStartData(Intent intent) {
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
