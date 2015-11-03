package cn.com.mobnote.golukmobile.wifibind;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.application.SysApplication;
import cn.com.mobnote.golukmobile.BaseActivity;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.UserOpenUrlActivity;
import cn.com.mobnote.golukmobile.carrecorder.util.ImageManager;
import cn.com.mobnote.util.GolukUtils;
import cn.com.tiros.debug.GolukDebugUtils;

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
 * @ 功能描述:wifi链接首页
 * 
 * @author 陈宣宇
 * 
 */

public class WiFiLinkIndexActivity extends BaseActivity implements OnClickListener {

	private static final String TAG = "WiFiLinkIndexActivity";
	/** application */
	private GolukApplication mApp = null;
	/** 上下文 */
	private Context mContext = null;
	/** 返回按钮 */
	private ImageButton mBackBtn = null;
	/** 说明文字 */
	private TextView mDescTitleText1 = null;
	private TextView mDescTitleText2 = null;
	/** 继续按钮 */
	private Button mKeepBtn = null;
	private TextView mHelpTv = null;
	private ImageView mMiddleImg = null;
	private ImageView mProgressImg = null;

	private Bitmap mMiddleBitmap = null;
	private Bitmap mProgressBitmap = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wifi_link_index);
		mContext = this;
		// 获得GolukApplication对象
		mApp = (GolukApplication) getApplication();
		mApp.setContext(mContext, "WiFiLinkIndex");

		// collectLog("onCreate", "-----111111");

		SysApplication.getInstance().addActivity(this);

		// 断开连接
		mApp.mIPCControlManager.setIPCWifiState(false, "");
		// 改变Application-IPC退出登录
		mApp.setIpcLoginOut();
		// 页面初始化
		initBitmap();
		init();
	}

	private void initBitmap() {
		mMiddleBitmap = ImageManager.getBitmapFromResource(R.drawable.connect_banner_1);
		mProgressBitmap = ImageManager.getBitmapFromResource(R.drawable.setp_1);
	}

	/**
	 * 页面初始化
	 */
	@SuppressLint("HandlerLeak")
	private void init() {
		// 获取页面元素
		mBackBtn = (ImageButton) findViewById(R.id.back_btn);
		mDescTitleText1 = (TextView) findViewById(R.id.textView1);
		mDescTitleText2 = (TextView) findViewById(R.id.textView2);
		mKeepBtn = (Button) findViewById(R.id.keep_btn);
		// 注册事件
		mBackBtn.setOnClickListener(this);
		mKeepBtn.setOnClickListener(this);
		// 修改title说明文字颜色
		final String showTxt = "需要<font color=\"#0587ff\"> 手机 </font>与<font color=\"#0587ff\"> 极路客 </font>建立WiFi连接";
		mDescTitleText2.setText(Html.fromHtml(showTxt));

		mHelpTv = (TextView) findViewById(R.id.wifi_link_index_help);
		mHelpTv.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG); // 下划线
		mHelpTv.setOnClickListener(this);

		mDescTitleText1.getPaint().setFakeBoldText(true);
		mDescTitleText2.getPaint().setFakeBoldText(true);

		mMiddleImg = (ImageView) findViewById(R.id.imageView1);
		mProgressImg = (ImageView) findViewById(R.id.wifilink_progress);

		mMiddleImg.setImageBitmap(mMiddleBitmap);
		mProgressImg.setImageBitmap(mProgressBitmap);
	}

	// private void collectLog(String method, String msg) {
	// mApp.uploadMsg(
	// JsonUtil.getReportJson(IMessageReportFn.KEY_WIFI_BIND,
	// JsonUtil.getReportData(TAG, method, msg)), false);
	// }

	// private void collectLog(String method, String msg) {
	// ReportLogManager.getInstance().getReport(IMessageReportFn.KEY_WIFI_BIND)
	// .addLogData(JsonUtil.getReportData(TAG, method, msg));
	//
	// }

	@Override
	protected void onResume() {
		mApp.setContext(this, "WiFiLinkIndex");
		super.onResume();
	}

	private void free() {
		if (null != mMiddleImg) {
			GolukUtils.freeBitmap(mMiddleBitmap);
		}

		if (null != mProgressImg) {
			GolukUtils.freeBitmap(mProgressBitmap);
		}

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		GolukDebugUtils.e("", "jyf-----WifiBind-----Index-----onDestroy----");
		free();
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.back_btn:
			// 返回
			finish();
			break;
		case R.id.keep_btn:
			// 新版需求,直接跳转到wifi列表页面
			// collectLog("onClick", "-----jump----to----WiFiLinkListActivity");

			Intent list = new Intent(WiFiLinkIndexActivity.this, WiFiLinkListActivity.class);
			startActivity(list);
			break;
		case R.id.wifi_link_index_help:
//			GolukUtils.openUrl(GolukUtils.URL_BIND_HELP, this);
			Intent mHelpIntent = new Intent(this, UserOpenUrlActivity.class);
			mHelpIntent.putExtra(UserOpenUrlActivity.FROM_TAG, "wifihelp");
			startActivity(mHelpIntent);
			break;
		}
	}

}
