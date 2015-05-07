package cn.com.mobnote.golukmobile;

import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.application.SysApplication;
import cn.com.mobnote.entity.WiFiInfo;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.util.console;
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
import android.widget.EditText;
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
 * @ 功能描述:wifi连接修改热点密码,
 * 第一步,把热点的信息通知给ipc,等待回调
 * 第二步,跳转到完成页面去创建热点
 * 
 * @author 陈宣宇
 * 
 */

public class WiFiLinkCreateHotActivity extends Activity implements OnClickListener{
	/** application */
	private GolukApplication mApp = null;
	/** 上下文 */
	private Context mContext = null;
	/** 返回按钮 */
	private ImageButton mBackBtn = null;
	/** 描述title*/
	private TextView mDescTitleText = null;
	/** wifi名字 */
	private EditText mWiFiName = null;
	/** wifi密码 */
	private EditText mWiFiPwd = null;
	/** 创建热点动画 */
	private ImageView mPhoneWiFiImage = null;
	private AnimationDrawable mPhoneWiFiAnim = null;
	/** 下一步按钮 */
	private Button mNextBtn = null;
	/** loading */
	private RelativeLayout mLoading = null;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wifi_link_create_hot);
		mContext = this;
		
		SysApplication.getInstance().addActivity(this);
		//获得GolukApplication对象
		mApp = (GolukApplication)getApplication();
		mApp.setContext(mContext,"WiFiLinkCreateHot");
		
		//页面初始化
		init();
	}
	
	/**
	 * 页面初始化
	 */
	@SuppressLint("HandlerLeak")
	private void init(){
		//获取页面元素
		mLoading = (RelativeLayout)findViewById(R.id.loading_layout);
		mBackBtn = (ImageButton)findViewById(R.id.back_btn);
		mDescTitleText = (TextView) findViewById(R.id.textView1);
		mWiFiName = (EditText) findViewById(R.id.wifi_name_text);
		mWiFiPwd = (EditText) findViewById(R.id.wifi_pwd_text);
		mPhoneWiFiImage = (ImageView)findViewById(R.id.imageView2);
		mPhoneWiFiAnim = (AnimationDrawable)mPhoneWiFiImage.getBackground();
		mNextBtn = (Button)findViewById(R.id.next_btn);
		
		//注册事件
		mBackBtn.setOnClickListener(this);
		mNextBtn.setOnClickListener(this);
		
		//启动动画
		mPhoneWiFiAnim.start();
		mDescTitleText.setText(Html.fromHtml("3.修改与<font color=\"#28b6a4\">Goluk 相连手机</font>的 WiFi 热点信息"));
	}
	
	/**
	 * 设置ipc连接手机热点
	 */
	private void setIpcLinkPhoneHot(){
		//获取wifi名字
		String wifiName = mWiFiName.getText().toString().trim();
		if(null != wifiName && !"".equals(wifiName)){
			//获取pwd
			String pwd = mWiFiPwd.getText().toString().trim();
			if(null != pwd && !"".equals(pwd)){
				if(pwd.length() > 7){
					//显示loading
					mLoading.setVisibility(View.VISIBLE);
					
					//保存wifi账户密码
					WiFiInfo.GolukSSID = wifiName;
					WiFiInfo.GolukPWD = pwd;
					String golukMac = WiFiInfo.GolukMAC.toString();
					//写死ip,网关
					String ip = golukMac.substring(0,golukMac.lastIndexOf(".")) + ".103";
					String way = WiFiInfo.GolukMAC;
					//连接ipc热点wifi---调用ipc接口
					console.log("通知ipc连接手机热点--setIpcLinkPhoneHot---1");
					String appwd = WiFiInfo.AP_PWD;
					String json = "";
					if(null != appwd && !"".equals(appwd)){
						json = "{\"AP_SSID\":\"" + WiFiInfo.AP_SSID + "\",\"AP_PWD\":\"" + WiFiInfo.AP_PWD + "\",\"GolukSSID\":\"" + wifiName + "\",\"GolukPWD\":\"" + pwd + "\",\"GolukIP\":\"" + ip + "\",\"GolukGateway\":\"" + way + "\" }";
					}
					else{
						json = "{\"GolukSSID\":\"" + wifiName + "\",\"GolukPWD\":\"" + pwd + "\",\"GolukIP\":\"" + ip + "\",\"GolukGateway\":\"" + way + "\" }";
					}
					console.log("通知ipc连接手机热点--setIpcLinkPhoneHot---2---josn---" + json);
					boolean b = mApp.mIPCControlManager.setIpcLinkPhoneHot(json);
					if(!b){
						console.toast("调用设置IPC连接热点失败", mContext);
						mLoading.setVisibility(View.GONE);
					}
					console.log("通知ipc连接手机热点--setIpcLinkPhoneHot---3---b---" + b);
				}
				else{
					console.toast("WiFi热点密码长度必须大于等于8位", mContext);
				}
			}
			else{
				console.toast("WiFi热点密码不能为空", mContext);
			}
		}
		else{
			console.toast("WiFi热点名称不能为空", mContext);
		}
	}
	
	/**
	 * 设置热点信息成功回调
	 */
	public void setIpcLinkWiFiCallBack(){
		//隐藏loading
		mLoading.setVisibility(View.GONE);
		console.log("设置热点信息成功回调---setIpcLinkWiFiCallBack");
		//设置热点信息成功,跳转到成功页面创建热点
		Intent complete = new Intent(WiFiLinkCreateHotActivity.this,WiFiLinkCompleteActivity.class);
		startActivity(complete);
	}
	
	@Override
	protected void onResume(){
		mApp.setContext(this,"WiFiLinkCreateHot");
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
			case R.id.next_btn:
				//设置ipc连接手机热点信息
				setIpcLinkPhoneHot();
			break;
		}
	}
}





