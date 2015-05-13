package cn.com.mobnote.golukmobile;

import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.application.SysApplication;
import cn.com.mobnote.entity.WiFiInfo;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.CarRecorderActivity;
import cn.com.mobnote.golukmobile.carrecorder.util.SettingUtils;
import cn.com.mobnote.util.console;
import cn.com.mobnote.wifibind.WifiConnCallBack;
import cn.com.mobnote.wifibind.WifiConnectManager;
import cn.com.mobnote.wifibind.WifiRsBean;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.AnimationDrawable;
import android.text.Html;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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

public class WiFiLinkCompleteActivity extends BaseActivity implements OnClickListener,WifiConnCallBack{
	/** application */
	private GolukApplication mApp = null;
	/** 上下文 */
	private Context mContext = null;
	/** 返回按钮 */
	private ImageButton mBackBtn = null;
	/** 创建热点描述title*/
	private TextView mCreateHotText = null;
	/** 连接成功描述title layout*/
	private RelativeLayout mLinkedLayout = null;
	private TextView mLinkedDesc = null;
	
	private WifiConnectManager mWac = null;
	/** 连接图片 */
	private ImageView mLinkImage = null;
	private AnimationDrawable mLinkAnim = null;
	/** 完成按钮 */
	private Button mCompleteBtn = null;
	/** 开始使用状态 */
	private boolean mIsComplete = false;
	/** ipc连接mac地址 */
	private String mIpcMac = "";
	private String mWiFiIp = "";
	
	public static Handler mPageHandler = null;
	
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
		mLinkedLayout = (RelativeLayout) findViewById(R.id.linked_layout);
		mLinkedDesc = (TextView) findViewById(R.id.linked_desc);
		mLinkImage = (ImageView) findViewById(R.id.imageView2);
		mLinkAnim = (AnimationDrawable)mLinkImage.getBackground();
		mLinkAnim.start();
		mCompleteBtn = (Button)findViewById(R.id.complete_btn);
		
		//注册事件
		mBackBtn.setOnClickListener(this);
		mCompleteBtn.setOnClickListener(this);
		
		mCreateHotText.setText(Html.fromHtml("手机正在<font color=\"#28b6a4\">创建WiFi</font>个人热点...."));
		
		mPageHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				int what = msg.what;
				switch(what){
					case 1:
						//测试热点创建成功
						//hotWiFiCreateSuccess();
					break;
					case 2:
						//sendLogicLinkIpc("");
					break;
				}
			}
		};
	}
	
	/**
	 * 创建手机热点
	 */
	private void createPhoneHot(){
		//隐藏loading
		//mLoading.setVisibility(View.GONE);
		
		String wifiName = WiFiInfo.GolukSSID;
		String pwd = WiFiInfo.GolukPWD;
		String ipcssid = WiFiInfo.AP_SSID;
		String ipcmac = WiFiInfo.AP_MAC;
		
		//创建热点之前先断开ipc连接
		mApp.mIPCControlManager.setIPCWifiState(false,null);
		//改变Application-IPC退出登录
		mApp.setIpcLoginOut();
		
		//调用韩峥接口创建手机热点
		console.log("创建手机热点---startWifiAp---1---" + wifiName + "---" + pwd + "---" + ipcssid + "---" + ipcmac);
		WifiManager wm = (WifiManager)getSystemService(Context.WIFI_SERVICE);
		mWac = new WifiConnectManager(wm,this);
		mWac.createWifiAP(wifiName,pwd,ipcssid,ipcmac);
		
		//测试代码
		//mPageHandler.sendEmptyMessageDelayed(1, 5000);
	}
	
	/**
	 * wifi热点创建成功
	 */
	private void hotWiFiCreateSuccess(){
		mLinkedLayout.setVisibility(View.GONE);
		mCreateHotText.setText(Html.fromHtml("等待Goluk<font color=\"#28b6a4\">连接</font>到手机...."));
		
		//停止动画
		mLinkAnim.stop();
		
		//更换背景图片
		mLinkImage.setBackgroundResource(R.drawable.connect_gif_line);
		
		//测试代码
		//mPageHandler.sendEmptyMessageDelayed(2, 5000);
	}
	
	/**
	 * 通知logic连接ipc
	 */
	private void sendLogicLinkIpc(String ip,String ipcmac){
		//连接ipc热点wifi---调用ipc接口
		console.log("通知logic连接ipc---sendLogicLinkIpc---1---ip---" + ip);
		mApp.mIpcIp = ip;
		mIpcMac = ipcmac;
		mWiFiIp = ip;
		
		boolean b = mApp.mIPCControlManager.setIPCWifiState(true,ip);
		console.log("通知logic连接ipc---sendLogicLinkIpc---2---b---" + b);
	}
	
	/**
	 * 退出页面设置
	 */
	private void backSetup(){
		if(mIsComplete){
			//如果连接上了 保存标识
			saveBindMark();
		}
		else{
			//没连接,关闭热点
			mWac.closeWifiAP();
			
			//返回关闭全部页面
			SysApplication.getInstance().exit();
		}
	}
	
	/**
	 * 保存wifi绑定标识
	 */
	private void saveBindMark(){
		//绑定完成,保存标识
		SharedPreferences preferences = mContext.getSharedPreferences("ipc_wifi_bind",Context.MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.putBoolean("isbind",true);
		// 提交修改
		editor.commit();
	}
	
	/**
	 * ipc连接成功回调
	 */
	public void ipcLinkWiFiCallBack(){
		mCreateHotText.setVisibility(View.GONE);
		mLinkedLayout.setVisibility(View.VISIBLE);
		mLinkedDesc.setText(Html.fromHtml("你的Goluk已<font color=\"#28b6a4\">成功连接</font>到手机"));
		mCompleteBtn.setBackgroundResource(R.drawable.connect_mianbtn);
		mIsComplete = true;
		
		//保存连接数据
		WifiRsBean beans = new WifiRsBean();
		beans.setIpc_mac(mIpcMac);
		beans.setIpc_ssid(WiFiInfo.AP_SSID);
		beans.setPh_ssid(WiFiInfo.GolukSSID);
		beans.setPh_pass(WiFiInfo.GolukPWD);
		beans.setIpc_ip(mWiFiIp);
		mWac.saveConfiguration(beans);
		
		//保存绑定标识
		saveBindMark();
	}
	
	@Override
	public boolean onKeyDown(int keyCode,KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			console.log("按下系统返回键---WiFiLinkCompleteActivity---1");
			//返回关闭全部页面
			backSetup();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	};
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		console.log("通知logic停止连接ipc---WiFiLinkCompleteActivity---onDestroy---1");
		//mApp.mIPCControlManager.setIPCWifiState(false,null);
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
				backSetup();
			break;
			case R.id.complete_btn:
				if(mIsComplete){
					saveBindMark();
					//关闭wifi绑定全部页面
					SysApplication.getInstance().exit();
					
					//跳转到ipc预览页面
					Intent i = new Intent(mContext, CarRecorderActivity.class);
					startActivity(i);
				}
				else{
					console.toast("IPC连接中....", mContext);
				}
			break;
		}
	}

	@Override
	public void wifiCallBack(int type, int state, int process, String message,Object arrays) {
		// TODO Auto-generated method stub
		console.log("wifi链接接口回调---type---" + type + "---state---" + state + "---process---" + process + "---message---" + message + "---arrays---" + arrays);
		switch(type){
			case 3:
				if(state == 0){
					switch(process){
						case 0:
							//创建热点成功
							hotWiFiCreateSuccess();
						break;
						case 1:
							//ipc成功连接上热点
							try{
								WifiRsBean[] bean = (WifiRsBean[])arrays;
								if(null != bean){
									console.log("IPC连接上WIFI热点回调---length---" + bean.length);
									if(bean.length > 0){
										sendLogicLinkIpc(bean[0].getIpc_ip(),bean[0].getIpc_mac());
									}
								}
							}
							catch(Exception e){
								console.toast("IPC连接热点返回信息不是数组", mContext);
							}
						break;
						default:
							console.toast(message, mContext);
						break;
					}
				}
				else{
					console.toast(message, mContext);
				}
			break;
		}
	}
	
}
