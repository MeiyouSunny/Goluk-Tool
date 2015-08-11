package cn.com.mobnote.golukmobile;

import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import cn.com.mobnote.application.GlobalWindow;
import cn.com.mobnote.application.GolukApplication;

import com.umeng.analytics.MobclickAgent;

/**
 * 1.编辑器必须显示空白处
 *
 * 2.所有代码必须使用TAB键缩进
 *
 * 3.类首字母大写,函数、变量使用驼峰式命名,常量所有字母大写
 *
 * 4.注释必须在行首写.(枚举除外)
 *
 * 5.函数使用块注释,代码逻辑使用行注释
 *
 * 6.文件头部必须写功能说明
 *
 * 7.所有代码文件头部必须包含规则说明
 *
 * 基础Activity
 *
 * 2015年5月13日
 *
 * @author xuhw
 */
public class BaseActivity extends Activity {
	/** IPC默认要修改的密码 */
	public static final String IPC_PWD_DEFAULT = "123456789";
	/** 手机创建热点默认密码 */
	public static final String MOBILE_HOT_PWD_DEFAULT = "123456789";
	
	// 写死ip,网关
	public static final String DEFAULT_IP = "192.168.1.103";
	public static final String DEFAULT_WAY = "192.168.1.103";

	public static final String WIFI_CONNING_FAILED_STR = "未连接Goluk";
	public static final String WIFI_CONNING_STR = "正在连接Goluk...";
	public static final String WIFI_CONNED_STR = "已连接Goluk";
	private boolean m_bJumpActivity = false;

	public Handler mBaseHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			hMessage(msg);
			super.handleMessage(msg);
		}

	};

	/**
	 * 处理Handler消息，子类需要复写此方法
	 * 
	 * @param msg
	 * @author jyf
	 * @date 2015年6月2日
	 */
	protected void hMessage(Message msg) {

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	protected void onResume() {
		m_bJumpActivity = false;
		super.onResume();
		MobclickAgent.onResume(this);
		GolukApplication.getInstance().setIsBackgroundState(false);
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);

	}

	@Override
	protected void onStop() {
		super.onStop();
		if (isBackground(this)) {
			GolukApplication.getInstance().setIsBackgroundState(true);
			if (GlobalWindow.getInstance().isShow()) {
				GlobalWindow.getInstance().dimissGlobalWindow();
			}
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	@Override
	public void startActivity(Intent intent)
	{
		m_bJumpActivity = true;
		super.startActivity(intent);
	}
	
	public boolean isAllowedClicked(){
		return m_bJumpActivity?false:true;
	}

	/**
	 * 判断当前应用程序处于前台还是后台
	 * 
	 * @param context
	 * @return
	 * @author xuhw
	 * @date 2015年5月13日
	 */
	public boolean isBackground(final Context context) {
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> tasks = am.getRunningTasks(1);
		if (!tasks.isEmpty()) {
			ComponentName topActivity = tasks.get(0).topActivity;
			if (!topActivity.getPackageName().equals(context.getPackageName())) {
				return true;
			}
		}
		return false;
	}

}
