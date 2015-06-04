package cn.com.mobnote.golukmobile;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.application.SysApplication;
import cn.com.mobnote.util.GolukUtils;

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

	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wifi_link_index);
		mContext = this;

		SysApplication.getInstance().addActivity(this);
		// 获得GolukApplication对象
		mApp = (GolukApplication) getApplication();
		mApp.setContext(mContext, "WiFiLinkIndex");
		// 断开连接
		mApp.mIPCControlManager.setIPCWifiState(false, null);
		// 改变Application-IPC退出登录
		mApp.setIpcLoginOut();
		// 页面初始化
		init();
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
	}

	@Override
	protected void onResume() {
		mApp.setContext(this, "WiFiLinkIndex");
		super.onResume();
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
			Intent list = new Intent(WiFiLinkIndexActivity.this, WiFiLinkListActivity.class);
			startActivity(list);
			break;
		case R.id.wifi_link_index_help:
			GolukUtils.showToast(this, "帮助");
			break;
		}
	}

}
