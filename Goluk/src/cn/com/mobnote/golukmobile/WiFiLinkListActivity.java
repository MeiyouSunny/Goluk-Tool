package cn.com.mobnote.golukmobile;

import java.util.ArrayList;

import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.application.SysApplication;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.list.WiFiListAdapter;
import cn.com.mobnote.list.WiFiListManage;
import cn.com.mobnote.list.WiFiListManage.WiFiListData;
import cn.com.mobnote.util.console;
import cn.com.mobnote.wifi.WiFiConnection;
import cn.com.mobnote.wifi.WifiAutoConnectManager;
import cn.com.mobnote.wifi.WifiConnCallBack;
import cn.com.mobnote.wifi.WifiRsBean;
import cn.com.mobnote.wifi.WifiConnectManagerSupport.WifiCipherType;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

public class WiFiLinkListActivity extends Activity implements OnClickListener, WifiConnCallBack {
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
	private WifiAutoConnectManager mWac = null;
	/** wifi列表适配器 */
	public WiFiListAdapter mWiFiListAdapter = null;
	public ArrayList<WiFiListData> mWiFiListData = null;
	/** 当前是否已连接ipc wifi */
	private boolean mHasLinked = false;
	
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
		getWiFiList();
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
		
		//启动loading动画
		mIpcSignalAnim.start();
		//修改title说明文字颜色
		mDescTitleText.setText(Html.fromHtml("1.确认<font color=\"#28b6a4\"> WiFi指示灯 </font>闪烁,连接名称为<font color=\"#28b6a4\"> Goluk xxx </font>的WiFi"));
	}
	
	/**
	 * 创建wifi回调广播
	 * @param wac
	 */
	private void createReceiver(WifiAutoConnectManager wac){
		String  action = WifiManager.SCAN_RESULTS_AVAILABLE_ACTION;
		IntentFilter filter = new IntentFilter();
		filter.addAction(action);
		registerReceiver(wac, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
	}

	/**
	 * 获取wifi列表
	 */
	private void getWiFiList(){
		mLoading.setVisibility(View.VISIBLE);
		WifiManager wm = (WifiManager)getSystemService(Context.WIFI_SERVICE);
		mWac = new WifiAutoConnectManager(wm,this);
		createReceiver(mWac);
		// 获取文件列表
		mWac.getWifiList();
	}
	
	/**
	 * 连接指定wifi
	 * @param wifiName
	 * @param pwd
	 */
	public void connectWiFi(String wifiName,String pwd){
		mLoading.setVisibility(View.VISIBLE);
		//保存wifi校验名称 chenxy
		WiFiConnection.SaveWiFiName(wifiName);
		
		WifiManager wm = (WifiManager)getSystemService(Context.WIFI_SERVICE);
		mWac = new WifiAutoConnectManager(wm,this);
		createReceiver(mWac);
		//连接wifi
		mWac.connect(wifiName,pwd,WifiCipherType.WIFICIPHER_WPA);
		console.log("开始连接选定wifi---connectWiFi---" + wifiName + "---" + pwd);
	}
	
	/**
	 * 连接指定wifi
	 * @param wifiName
	 */
	public void connectWiFi(String wifiName){
		mLoading.setVisibility(View.VISIBLE);
		//保存wifi校验名称
		WiFiConnection.SaveWiFiName(wifiName);
		
		WifiManager wm = (WifiManager)getSystemService(Context.WIFI_SERVICE);
		mWac = new WifiAutoConnectManager(wm,this);
		createReceiver(mWac);
		//连接wifi
		mWac.connect(wifiName,"123456789",WifiCipherType.WIFICIPHER_NOPASS);
		console.log("开始连接选定wifi---connectWiFi---" + wifiName + "---");
	}
	
	/**
	 * 判断已连接的wifi是否是小车本热点
	 */
	public boolean checkLinkWiFi(){
		WifiManager mWifiManage = (WifiManager)getSystemService(Context.WIFI_SERVICE);
		WiFiConnection connection = new WiFiConnection(mWifiManage,mContext);
		WifiInfo info = connection.getWiFiInfo();
		WifiAutoConnectManager wac = new WifiAutoConnectManager(mWifiManage,this);
		boolean b = wac.getEffectiveWifi(info);
		console.log("判断已连接的wifi是否是小车本热点---b---" + b + "---wifi---" + info.getSSID());
		return b;
	}
	
	/**
	 * 通知logic连接ipc
	 */
	public void sendLogicLinkIpc(){
		//检测是否连接ipc-wifi
		boolean hasLink = checkLinkWiFi();
		if(hasLink){
			//连接ipc热点wifi---调用ipc接口
			console.log("通知logic连接ipc---sendLogicLinkIpc---1");
			//写死ipc ip地址
			String ip = "192.168.62.1";
			boolean b =mApp.mIPCControlManager.setIPCWifiState(true,ip);
			console.log("通知logic连接ipc---sendLogicLinkIpc---2---b---" + b);
		}
		
		// wifi连接成功
		mWiFiListAdapter.changeWiFiStatus();
	}
	
	
	
	/**
	 * ipc连接成功回调
	 */
	public void ipcLinkedCallBack(){
		console.log("ipc连接成功回调---ipcLinkedCallBack---1");
		mLoading.setVisibility(View.GONE);
		//标识已连接ipc热点,可以点击下一步
		mHasLinked = true;
	}
	
	@Override
	protected void onResume(){
		mApp.setContext(this,"WiFiLinkList");
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
			case R.id.refresh_help_btn:
				//获取wifi列表
				getWiFiList();
			break;
			case R.id.next_btn:
				//已连接ipc热点,可以跳转到修改密码页面
				if(mHasLinked){
					//跳转到修改热点密码页面
					Intent modifyPwd = new Intent(WiFiLinkListActivity.this,WiFiLinkModifyPwdActivity.class);
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
	public void wifiCallBack(int state, String message, WifiRsBean[] arrays) {
		console.log("wifi链接接口回调---state---" + state + "---message---" + message + "---arrays---" + arrays);
		switch (state) {
			case -1:
				console.toast(message, mContext);
				mLoading.setVisibility(View.GONE);
			break;
			case 1:
				//通知logic连接ipc
				sendLogicLinkIpc();
				
				
				// wifi连接成功
				//mWiFiListAdapter.changeWiFiStatus();
				//回到首页
				//SysApplication.getInstance().exit();
			break;
			
			case 11:
				// 获取连接列表
				if(null != arrays){
					mWiFiListManage.analyzeWiFiData(arrays);
					mWiFiListAdapter.notifyDataSetChanged();
				}
				else{
					console.toast("没有搜索到小车点热点WiFi", mContext);
				}
				mLoading.setVisibility(View.GONE);
			break;
		}
		unregisterReceiver(mWac);
	}
	
}
