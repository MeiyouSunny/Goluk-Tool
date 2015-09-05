package cn.com.mobnote.golukmobile;

import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.guide.GolukGuideManage;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.application.SysApplication;
import cn.com.tiros.debug.GolukDebugUtils;

// OneAPM, Micle
import com.blueware.agent.android.BlueWare;

/**
 * <pre>
 * 1.类命名首字母大写
 * 2.公共函数驼峰式命名
 * 3.属性函数驼峰式命名
 * 4.变量/参数驼峰式命名
 * 5.操作符之间必须加空格
 * 6.注释都在行首写.(枚举除外)
 * 7.编辑器必须显示空白处
 * 8.所有代码必须使用TAB键缩进
 * 9.函数使用块注释,代码逻辑使用行注释
 * 10.文件头部必须写功能说明
 * 11.后续人员开发保证代码格式一致
 * </pre>
 * 
 * @ 功能描述:Goluk引导页
 * 
 * @author 陈宣宇
 * 
 */
@SuppressLint("HandlerLeak")
public class GuideActivity extends BaseActivity {
	/** 上下文 */
	private Context mContext = null;
	/** 引导页管理类 */
	private GolukGuideManage mGolukGuideManage = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.guide);
		mContext = this;
		GolukApplication.getInstance().setContext(this, "GuideActivity");
		GolukApplication.getInstance().initSharedPreUtil(this);
		((GolukApplication) this.getApplication()).initLogic();
		
		((GolukApplication) this.getApplication()).startUpgrade();
		
		// 初始化
		init();
		
		// OneAPM
		BlueWare.withApplicationToken("E18AA269D23C98A1521C6454D1836CE006").start(this.getApplication());
		
		SysApplication.getInstance().addActivity(this);
	}

	private boolean isFirstStart() {
		// 读取SharedPreFerences中需要的数据,使用SharedPreFerences来记录程序启动的使用次数
		SharedPreferences preferences = getSharedPreferences("golukmark", MODE_PRIVATE);
		// 取得相应的值,如果没有该值,说明还未写入,用true作为默认值
		return preferences.getBoolean("isfirst", true);
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
				Intent it = new Intent(this, MainActivity.class);
				GolukDebugUtils.i("lily", "======MainActivity==GuideActivity====");
				startActivity(it);
			} else {
				// 是第一次登录(没有登录过)
				Intent intent = new Intent(this, UserStartActivity.class);
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
			// 退出对话框
			int PID = android.os.Process.myPid();
			android.os.Process.killProcess(PID);
			android.os.Process.sendSignal(PID, 9);
			finish();
		}
		return false;
	}
}
