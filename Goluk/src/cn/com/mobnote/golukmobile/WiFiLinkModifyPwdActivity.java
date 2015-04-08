package cn.com.mobnote.golukmobile;

import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.application.SysApplication;
import cn.com.mobnote.golukmobile.R;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

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
 * @ 功能描述:wifi连接修改热点密码
 * 
 * @author 陈宣宇
 * 
 */

public class WiFiLinkModifyPwdActivity extends Activity implements OnClickListener {
	/** application */
	private GolukApplication mApp = null;
	/** 上下文 */
	private Context mContext = null;
	/** 返回按钮 */
	private ImageButton mBackBtn = null;
	/** 跳过 */
	private Button mJumpBtn = null;
	/** 描述title*/
	private TextView mDescTitleText = null;
	/** IPCWIFI动画 */
	private ImageView mIpcWiFiImage = null;
	private AnimationDrawable mIpcWiFiAnim = null;
	/** 下一步按钮 */
	private Button mNextBtn = null;
	/** 连接wifi名称 */
	private String mLinkWiFiName = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wifi_link_modify_pwd);
		mContext = this;
		
		SysApplication.getInstance().addActivity(this);
		//获得GolukApplication对象
		mApp = (GolukApplication)getApplication();
		mApp.setContext(mContext,"WiFiLinkModifyPwd");
		//获取视频路径
		Intent intent = getIntent();
		mLinkWiFiName = intent.getStringExtra("cn.com.mobnote.golukmobile.wifiname");
		//页面初始化
		init();
	}
	
	/**
	 * 页面初始化
	 */
	@SuppressLint("HandlerLeak")
	private void init(){
		//获取页面元素
		mBackBtn = (ImageButton)findViewById(R.id.back_btn);
		mJumpBtn = (Button)findViewById(R.id.jump_btn);
		mDescTitleText = (TextView) findViewById(R.id.textView1);
		mIpcWiFiImage = (ImageView)findViewById(R.id.imageView2);
		mIpcWiFiAnim = (AnimationDrawable)mIpcWiFiImage.getBackground();
		mNextBtn = (Button)findViewById(R.id.next_btn);
		
		//注册事件
		mBackBtn.setOnClickListener(this);
		mJumpBtn.setOnClickListener(this);
		mNextBtn.setOnClickListener(this);
		
		//启动动画
		mIpcWiFiAnim.start();
		mDescTitleText.setText(Html.fromHtml("2.修改<font color=\"#28b6a4\">Goluk</font> WiFi热点信息"));
	}
	
	
	@Override
	protected void onResume(){
		mApp.setContext(this,"WiFiLinkModifyPwd");
		super.onResume();
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int id = v.getId();
		switch(id){
			case R.id.back_btn:
				//返回
				finish();
			break;
			case R.id.jump_btn:
				Intent jump = new Intent(WiFiLinkModifyPwdActivity.this,WiFiLinkCreateHotActivity.class);
				startActivity(jump);
			break;
			case R.id.next_btn:
				//Intent setup = new Intent(WiFiLinkModifyPwdActivity.this,WiFiLinkStep2Activity.class);
				//startActivity(setup);
			break;
		}
	}
	
}
