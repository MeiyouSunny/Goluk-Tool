package cn.com.mobnote.golukmobile;

import java.util.ArrayList;

import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.application.SysApplication;
import cn.com.mobnote.entity.WiFiInfo;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.list.WiFiListAdapter;
import cn.com.mobnote.list.WiFiListManage;
import cn.com.mobnote.list.WiFiListManage.WiFiListData;
import cn.com.mobnote.util.console;
import cn.com.mobnote.wifibind.WifiConnCallBack;
import cn.com.mobnote.wifibind.WifiConnectManager;
import cn.com.mobnote.wifibind.WifiConnectManagerSupport.WifiCipherType;
import cn.com.mobnote.wifibind.WifiRsBean;
import android.net.wifi.WifiManager;
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
import android.widget.ListView;
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
 * @ 功能描述:wifi列表
 * 
 * @author 陈宣宇
 * 
 */

public class WiFiLinkListActivity extends Activity implements OnClickListener,WifiConnCallBack {
	/** application */
	private GolukApplication mApp = null;
	/** 上下文 */
	private Context mContext = null;
	//private LayoutInflater mLayoutInflater = null;
	/** loading */
	private RelativeLayout mLoading = null;
	/** 返回按钮 */
	private ImageButton mBackBtn = null;
	/** 说明文字 */
	private TextView mDescTitleText = null;
	/** IPC信号动画 */
	private ImageView mIpcSignalImage = null;
	private AnimationDrawable mIpcSignalAnim = null;
	/** 刷新按钮 */
	private ImageButton mRefreshHelpBtn = null;
	/** 下一步按钮 */
	private Button mNextBtn = null;
	/** wifi列表 */
	private ListView mWiFiList = null;
	/** wifi列表manage */
	private WiFiListManage mWiFiListManage = null;
	private WifiConnectManager mWac = null;
	/** wifi列表适配器 */
	public WiFiListAdapter mWiFiListAdapter = null;
	public ArrayList<WiFiListData> mWiFiListData = null;
	/** 当前是否已连接ipc wifi */
	private boolean mHasLinked = false;
	/** 连接wifi名称 */
	public String mLinkWiFiName = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wifi_link_list);
		mContext = this;
		
		SysApplication.getInstance().addActivity(this);
		//获得GolukApplication对象
		mApp = (GolukApplication)getApplication();
		mApp.setContext(mContext,"WiFiLinkList");
		
		//页面初始化
		init();
		
		//获取wifi列表
		getWiFiList(false);
	}
	
	/**
	 * 页面初始化
	 */
	@SuppressLint("HandlerLeak")
	private void init(){
		//获取页面元素
		mLoading = (RelativeLayout)findViewById(R.id.loading_layout);
		mBackBtn = (ImageButton)findViewById(R.id.back_btn);
		mIpcSignalImage = (ImageView)findViewById(R.id.imageView2);
		mIpcSignalAnim = (AnimationDrawable)mIpcSignalImage.getBackground();
		
		mDescTitleText = (TextView)findViewById(R.id.textView1);
		mRefreshHelpBtn = (ImageButton)findViewById(R.id.refresh_help_btn);
		mNextBtn = (Button) findViewById(R.id.next_btn);
		
		mWiFiList = (ListView)findViewById(R.id.wifi_list_listview);
		
		mWiFiListManage = new WiFiListManage(mContext);
		mWiFiListData = mWiFiListManage.getWiFiList();
		mWiFiListAdapter = new WiFiListAdapter(mContext,mWiFiListData);
		mWiFiList.setAdapter(mWiFiListAdapter);
		
		//注册事件
		mBackBtn.setOnClickListener(this);
		mRefreshHelpBtn.setOnClickListener(this);
		mNextBtn.setOnClickListener(this);
		
		//启动动画
		mIpcSignalAnim.start();
		//修改title说明文字颜色
		mDescTitleText.setText(Html.fromHtml("1.确认<font color=\"#28b6a4\"> WiFi指示灯 </font>闪烁,连接名称为<font color=\"#28b6a4\"> Goluk xxxxx </font>的WiFi"));
	}

	/**
	 * 获取wifi列表
	 */
	private void getWiFiList(boolean b){
		mLoading.setVisibility(View.VISIBLE);
		WifiManager wm = (WifiManager)getSystemService(Context.WIFI_SERVICE);
		mWac = new WifiConnectManager(wm,this);
		// 获取文件列表tcay_ap_ipc
		mWac.scanWifiList("",b);
	}
	
	/**
	 * 连接指定wifi
	 * @param wifiName
	 * @param pwd
	 */
	public void connectWiFi(String wifiName,String mac,String pwd){
		mLoading.setVisibility(View.VISIBLE);
		//保存wifi校验名称 chenxy
		//WiFiConnection.SaveWiFiName(wifiName);
		//保存wifi名称
		mLinkWiFiName = wifiName;
		//保存ipc-wifi数据
		WiFiInfo.AP_SSID = mLinkWiFiName;
		WiFiInfo.AP_PWD = pwd;
		WiFiInfo.AP_MAC = mac;
		
		WifiManager wm = (WifiManager)getSystemService(Context.WIFI_SERVICE);
		mWac = new WifiConnectManager(wm,this);
		//连接wifi
		mWac.connectWifi(wifiName,pwd,WifiCipherType.WIFICIPHER_WPA);
		console.log("开始连接选定wifi---connectWiFi---" + wifiName + "---" + pwd);
	}
	
	/**
	 * 连接指定wifi
	 * @param wifiName
	 */
	public void connectWiFi(String wifiName,String mac){
		mLoading.setVisibility(View.VISIBLE);
		//保存wifi校验名称 chenxy
		//WiFiConnection.SaveWiFiName(wifiName);
		//保存wifi名称
		mLinkWiFiName = wifiName;
		//保存ipc-wifi数据
		WiFiInfo.AP_SSID = mLinkWiFiName;
		WiFiInfo.AP_PWD = "";
		WiFiInfo.AP_MAC = mac;
		
		WifiManager wm = (WifiManager)getSystemService(Context.WIFI_SERVICE);
		mWac = new WifiConnectManager(wm,this);
		//连接wifi
		mWac.connectWifi(wifiName,"",WifiCipherType.WIFICIPHER_NOPASS);
		console.log("开始连接选定wifi---connectWiFi---" + wifiName + "---pwd---空");
	}
	
	/**
	 * 通知logic连接ipc
	 */
	public void sendLogicLinkIpc(){
		//先获取ipc是否已连接
		boolean isLogin = mApp.getIpcIsLogin();
		console.log("ipc连接状态---WiFiLinkListActivity---b---" + isLogin);
		if(!isLogin){
			mLoading.setVisibility(View.VISIBLE);
			//连接ipc热点wifi---调用ipc接口
			console.log("通知logic连接ipc---sendLogicLinkIpc---1");
			boolean b = mApp.mIPCControlManager.setIPCWifiState(true,"192.168.62.1");
			console.log("通知logic连接ipc---sendLogicLinkIpc---2---b---" + b);
		}
		else{
			//ipc已连接
			ipcLinkedCallBack();
		}
	}
	
	/**
	 * ipc连接成功回调
	 */
	public void ipcLinkedCallBack(){
		console.log("ipc连接成功回调---ipcLinkedCallBack---1");
		mLoading.setVisibility(View.GONE);
		//标识已连接ipc热点,可以点击下一步
		mHasLinked = true;
		
		mWiFiListAdapter.changeWiFiStatus();
		mNextBtn.setBackgroundResource(R.drawable.connect_mianbtn);
	}
	
	@Override
	protected void onResume(){
		mApp.setContext(this,"WiFiLinkList");
		super.onResume();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		console.log("系统返回键-------停止连接------1");
		//mApp.mIPCControlManager.setIPCWifiState(false,null);
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
			case R.id.refresh_help_btn:
				//获取wifi列表
				getWiFiList(true);
			break;
			case R.id.next_btn:
				//已连接ipc热点,可以跳转到修改密码页面
				if(mHasLinked){
					//跳转到修改热点密码页面
					Intent modifyPwd = new Intent(WiFiLinkListActivity.this,WiFiLinkModifyPwdActivity.class);
					modifyPwd.putExtra("cn.com.mobnote.golukmobile.wifiname",mLinkWiFiName);
					startActivity(modifyPwd);
				}
				else{
					//灰色按钮不能点击
					console.toast("请先连接IPC-WIFI", mContext);
				}
			break;
		}
	}
	
	@Override
	public void wifiCallBack(int type, int state, int process, String message,Object arrays) {
		console.log("wifi链接接口回调---type---" + type + "---state---" + state + "---process---" + process + "---message---" + message + "---arrays---" + arrays);
		// TODO Auto-generated method stub
		mLoading.setVisibility(View.GONE);
		WifiRsBean[] beans = null;
		switch(type){
			case 1:
				if(state >= 0){
					//获取wifi列表
					beans = (WifiRsBean[]) arrays;
					if (beans != null) {
						mWiFiListManage.analyzeWiFiData(beans);
						mWiFiListAdapter.notifyDataSetChanged();
					}
					else {
						console.toast(message, mContext);
					}
				}
				else{
					console.toast(message, mContext);
				}
			break;
			case 2:
				if(state >= 0){
					//连接成功
					//通知ipc连接成功
					sendLogicLinkIpc();
				}
				else{
					mNextBtn.setBackgroundResource(R.drawable.connect_mianbtn_ash);
					mHasLinked = false;
					console.toast(message, mContext);
				}
			break;
			default:
				console.toast(message, mContext);
			break;
		}
	}
	
}






