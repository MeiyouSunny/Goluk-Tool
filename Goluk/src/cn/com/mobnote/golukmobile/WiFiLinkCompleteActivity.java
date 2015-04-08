package cn.com.mobnote.golukmobile;

import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.application.SysApplication;
import cn.com.mobnote.entity.WiFiInfo;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.wifimanage.WifiApAdmin;
import cn.com.mobnote.util.console;
import android.os.Bundle;
import android.os.Handler;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
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
 * @ 功能描述:wifi连接完成
 * 第一步,通知logic断开ipc连接
 * 第二步,创建wifi热点,等待创建成功回调
 * 第三步,等待ipc连接热点回调
 * 第四步,通知logic-ipc已连接上设备
 * 
 * @author 陈宣宇
 * 
 */

public class WiFiLinkCompleteActivity extends Activity implements OnClickListener {
	/** application */
	private GolukApplication mApp = null;
	/** 上下文 */
	private Context mContext = null;
	/** 返回按钮 */
	private ImageButton mBackBtn = null;
	/** 创建热点描述title*/
	private TextView mCreateHotText = null;
	/** 完成按钮 */
	private Button mCompleteBtn = null;
	/** 开始使用状态 */
	private boolean mIsComplete = false;
	private WifiApAdmin mWifiApAdmin = null;
	
	@SuppressLint("HandlerLeak")
	public Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch(msg.what){
				case 10:
					//创建热点失败
				break;
				case 11:
					//创建热点成功
					console.log("创建热点成功---startWifiAp---2");
					//通知ipc连接手机
				//	setIpcLinkPhone();
				break;
			}
		};
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wifi_link_complete);
		mContext = this;
		
		SysApplication.getInstance().addActivity(this);
		//获得GolukApplication对象
		mApp = (GolukApplication)getApplication();
		mApp.setContext(mContext,"WiFiLinkComplete");
		
		//页面初始化
		init();
		
		//创建热点
		createPhoneHot();
	}
	
	/**
	 * 页面初始化
	 */
	@SuppressLint("HandlerLeak")
	private void init(){
		//获取页面元素
		mBackBtn = (ImageButton)findViewById(R.id.back_btn);
		mCreateHotText = (TextView) findViewById(R.id.textView1);
		mCompleteBtn = (Button)findViewById(R.id.complete_btn);
		
		//注册事件
		mBackBtn.setOnClickListener(this);
		mCompleteBtn.setOnClickListener(this);
		
		mCreateHotText.setText(Html.fromHtml("手机正在<font color=\"#28b6a4\">创建WiFi</font>个人热点...."));
	}
	
	/**
	 * 创建手机热点
	 */
	public void createPhoneHot(){
		//隐藏loading
		//mLoading.setVisibility(View.GONE);
		
		String wifiName = WiFiInfo.GolukSSID;
		String pwd = WiFiInfo.GolukPWD;
		//连接ipc热点wifi---调用ipc接口
		//调用韩峥接口创建手机热点
		console.log("创建手机热点---startWifiAp---1");
		mWifiApAdmin = new WifiApAdmin(this,mHandler);
		if(!mWifiApAdmin.isWifiApEnabled()){
			//创建之前先断开ipc连接
			mApp.mIPCControlManager.setIPCWifiState(false,null);
			//创建热点
			mWifiApAdmin.startWifiAp(wifiName, pwd);
		}
	}
	
	@Override
	protected void onResume(){
		mApp.setContext(this,"WiFiLinkComplete");
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
			case R.id.complete_btn:
				if(mIsComplete){
					
				}
				else{
					console.toast("IPC连接中....", mContext);
				}
//				Intent setup = new Intent(WiFiLinkCompleteActivity.this,WiFiLinkStep2Activity.class);
//				startActivity(setup);
			break;
		}
	}
	
}
