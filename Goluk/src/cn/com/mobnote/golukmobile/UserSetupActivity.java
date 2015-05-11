package cn.com.mobnote.golukmobile;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnKeyListener;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.carrecorder.base.BaseActivity;
import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.ipcmanager.IPCManagerFn;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.mobnote.user.DataCleanManage;
import cn.com.mobnote.user.UserInterface;
import cn.com.mobnote.user.UserUtils;
import cn.com.mobnote.util.console;
import cn.com.tiros.api.Const;
import cn.com.tiros.utils.LogUtil;
/**
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
 * @ 功能描述:Goluk个人设置
 * 
 * @author 陈宣宇
 * 
 */

public class UserSetupActivity extends BaseActivity implements OnClickListener,UserInterface,IPCManagerFn {
	/** application */
	private GolukApplication mApp = null;
	/** 上下文 */
	private Context mContext = null;
	//private LayoutInflater mLayoutInflater = null;
	/** 返回按钮 */
	private Button mBackBtn = null;
	
	/** 个人中心页面handler用来接收消息,更新UI*/
	public static Handler mUserCenterHandler = null;
	
	/**退出按钮**/
	private Button btnLoginout;
	/**用户信息**/
	private String phone = null;
	/**登录的状态**/
	private SharedPreferences mPreferences = null;
	private boolean isFirstLogin = false;
	private Editor mEditor = null;
	/**正在登录对话框*/
	private Builder mBuilder = null;
	private AlertDialog dialog = null;
	/**清楚缓存**/
	private RelativeLayout mClearCache = null;
	public static Handler mHandler = null;
	/**固件升级*/
	private RelativeLayout mUpdateItem = null;
	/**传输文件*/
	private Builder mSendMessageBuilder = null;
	private AlertDialog mSendDialog = null;
	/**正在升级中*/
	private Builder mUpdateBuilder = null;
	private AlertDialog mUpdateDialog = null;
	/**升级成功**/
	private Builder mUpdateSuccess = null;
	private AlertDialog mUpdateDialogSuccess = null;
	/**升级失败**/
	private Builder mUpdateFail = null;
	private AlertDialog mUpdateDialogFail = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_personal_setup);
		
	}
	
	@SuppressLint("HandlerLeak")
	@Override
	protected void onResume(){
		super.onResume();
		
		mContext = this;
		
		//获得GolukApplication对象
		mApp = (GolukApplication)getApplication();
		mApp.setContext(mContext,"UserSetup");
		
		//页面初始化
		init();
		
		mHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				if(msg.what == 0){
					Log.i("lily", "已清除过缓存");
				}
			}
		};
		
		if(null != GolukApplication.getInstance().getIPCControlManager()){
			GolukApplication.getInstance().getIPCControlManager().addIPCManagerListener("carupgrade", this);
		}
	}
	
	/**
	 * 页面初始化
	 */
	@SuppressLint("HandlerLeak")
	private void init(){
		//获取页面元素
		mBackBtn = (Button)findViewById(R.id.back_btn);
		//退出按钮
		btnLoginout = (Button) findViewById(R.id.loginout_btn);
		
		//没有登录过的状态
		mPreferences = getSharedPreferences("firstLogin", MODE_PRIVATE);
		isFirstLogin = mPreferences.getBoolean("FirstLogin", true);
		Log.i("lily", "----------UserSetupActivity11111-------"+mApp.registStatus);
		if(!isFirstLogin ){//登录过
			Log.i("lily", "----------UserSetupActivity-------"+mApp.registStatus);
			if(mApp.loginStatus == 1 || mApp.registStatus == 2 || mApp.autoLoginStatus == 2 ||mApp.isUserLoginSucess == true){//上次登录成功
				btnLoginout.setText("退出登录");
			}else{
				btnLoginout.setText("登录");
			}
		}else{
			if( mApp.registStatus == 2){
				btnLoginout.setText("退出登录");
			}else{
				btnLoginout.setText("登录");
			}
		}
		btnLoginout.setOnClickListener(this);
		
		//注册事件
		mBackBtn.setOnClickListener(this);
		
		//更新UI handler
		mUserCenterHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
			}
		};
		/**清除缓存*/
		mClearCache = (RelativeLayout) findViewById(R.id.remove_cache_item);
		mClearCache.setOnClickListener(this);
		/**固件升级*/
		mUpdateItem = (RelativeLayout) findViewById(R.id.update_item);
		mUpdateItem.setOnClickListener(this);
	}
		
	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch(id){
			case R.id.back_btn:
				//返回
				this.finish();
			break;
			case R.id.setup_item:
				//跳转到设置页面
				console.log("onclick---setup--item");
			break;
		//退出按钮
			case R.id.loginout_btn:
				if(btnLoginout.getText().toString().equals("登录")){
					mApp.mUser.setUserInterface(this);
					if(mApp.autoLoginStatus == 1){
						mBuilder = new AlertDialog.Builder(mContext);
						 dialog = mBuilder.setMessage("正在为您登录，请稍候……")
						.setCancelable(false)
						.setOnKeyListener(new OnKeyListener() {
							@Override
							public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
								if(keyCode == KeyEvent.KEYCODE_BACK){
									return true;
								}
								return false;
							}
						}).create();
						dialog	.show();
						return ;
					}
					initIntent(UserLoginActivity.class);
				}else if(btnLoginout.getText().toString().equals("退出登录")){
						new AlertDialog.Builder(mContext)
						.setMessage("是否确认退出？")
						.setPositiveButton("确认", new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								getLoginout();
							}
						})
						.setNegativeButton("取消", null)
						.create().show();
				}
				break;
				//清除缓存
			case R.id.remove_cache_item:
				Log.i("lily", "----清除缓存-----"+Const.getAppContext().getCacheDir().getPath());
				new AlertDialog.Builder(mContext)
				.setMessage("确定清除缓存？")
				.setNegativeButton("取消", null)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						DataCleanManage.deleteFile(Const.getAppContext().getCacheDir());
					}
				}).create().show();
				break;
				//固件升级
			case R.id.update_item:
				/**
				 * 固件升级
				 */
				new AlertDialog.Builder(mContext)
				.setMessage("是否给您的摄像头进行固件升级？")
				.setPositiveButton("确认", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						if(GolukApplication.getInstance().getIpcIsLogin()){
							boolean u = GolukApplication.getInstance().getIPCControlManager().ipcUpgrade();
							LogUtil.e("lily","YYYYYY=======ipcUpgrade()============u="+u);
						}
					}
				})
				.setNegativeButton("取消", null)
				.create().show();
				
				break;
		}
	}
	/**
	 * 退出
	 */
	public void getLoginout(){
		if(!UserUtils.isNetDeviceAvailable(mContext)){
			console.toast("当前网络不可用，请检查网络后重试", mContext);
		}else{
			boolean b = mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage, IPageNotifyFn.PageType_SignOut, "");
			console.log(b+"");
			if(b){
				//注销成功
				mApp.isUserLoginSucess = false;
				mApp.loginoutStatus = true;//注销成功
				mApp.registStatus = 3;//注册失败
				
				mPreferences = getSharedPreferences("firstLogin", Context.MODE_PRIVATE);
				mEditor = mPreferences.edit();
				mEditor.putBoolean("FirstLogin", true);//注销完成后，设置为没有登录过的一个状态
				//提交修改
				mEditor.commit();
				
				console.toast("退出登录成功", mContext);
				btnLoginout.setText("登录");
				
			}else{
				//注销失败
				mApp.loginoutStatus = false;
				mApp.isUserLoginSucess = true;
			}
		}
		
	}
	
	/**
	 * 退出登录的回调
	 */
	public void getLogintoutCallback(int success,Object obj){
		console.log("-----------------退出登录回调--------------------");
		
	}
	/**
	 * 同步获取用户信息
	 */
	public void initData(){
		String info = mApp.mGoluk.GolukLogicCommGet(GolukModule.Goluk_Module_HttpPage, 0, "");
		try{
			JSONObject json = new JSONObject(info);
			
			Log.i("info", "====json()===="+json);
			phone = json.getString("phone");
			//退出登录后，将信息存储
			mPreferences = getSharedPreferences("setup", MODE_PRIVATE);
			mEditor = mPreferences.edit();
			mEditor.putString("setupPhone", phone);
			mEditor.commit();
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * 没有登录过、登录失败、正在登录需要登录
	 */
	@SuppressWarnings("rawtypes")
	public void initIntent(Class intentClass){
		Intent it = new Intent(UserSetupActivity.this, intentClass);
		it.putExtra("isInfo", "setup");
		startActivity(it);
//		this.finish();
	}
	
	/**
	 * 退出登录后，点击返回键，返回到无用户信息的页面
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			this.finish();
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * 取消正在自动登录的对话框
	 */
	public void dismissAutoDialog(){
		if (null != dialog){
			dialog.dismiss();
			dialog = null;
		}
	}
	
	@Override
	public void statusChange() {
		if(mApp.autoLoginStatus !=1){
			dismissAutoDialog();
			if(mApp.autoLoginStatus == 2 ){
				btnLoginout.setText("退出登录");
			}
		}
	}
	
    /**
	 * 固件升级
	 */
    @Override
	public void IPCManage_CallBack(int event, int msg, int param1, Object param2) {
    	LogUtil.e("lily", "YYYYYY====IPC_VDCP_Msg_IPCUpgrade====msg="+msg+"===param1="+param1+"==param2="+param2+"--------event-----"+event);
		if(event == ENetTransEvent_IPC_UpGrade_Resp){
			if(IPC_VDCP_Msg_IPCUpgrade == msg){
				LogUtil.e("lily", "---------连接ipc-------");
				if(param1 == RESULE_SUCESS){
					String str = (String)param2;
					Log.i("lily", "--str----"+str);
					if(TextUtils.isEmpty(str)){
						return ;
					}
					try{
						JSONObject json = new JSONObject(str);
						Log.i("lily", "-------设置页固件升级返回-----"+json);
						String stage = json.getString("stage");
						String percent = json.getString("percent");
						Log.i("lily", "---------stage-----"+stage+"-------percent----"+percent);
						if(stage.equals("1")){
							if(mSendMessageBuilder==null){
								mSendMessageBuilder = new AlertDialog.Builder(mContext);
								mSendDialog = mSendMessageBuilder.setMessage("正在传输文件，请稍候……"+percent+"%")
										.setCancelable(false)
										.setOnKeyListener(new OnKeyListener() {
											@Override
											public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
												if(keyCode == KeyEvent.KEYCODE_BACK){
													return true;
												}
												return false;
											}
										}).create();
										 mSendDialog.show();
							}else{
								mSendDialog.setMessage("正在传输文件，请稍候……"+percent+"%");
							}
						}
						if(stage.equals("2")){
							dismissSendDialog();//正在传输文件的对话框消失
							if(mUpdateBuilder == null){
								mUpdateBuilder = new AlertDialog.Builder(mContext);
								 mUpdateDialog = mUpdateBuilder.setMessage("开始升级，可能需要几分钟，请不要给摄像头断电"+percent+"%")
								.setCancelable(false)
								.setOnKeyListener(new OnKeyListener() {

									@Override
									public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
										if(keyCode == KeyEvent.KEYCODE_BACK){
											return true;
										}
										return false;
									}
								}).create();
								 mUpdateDialog.show();
							}else{
								mUpdateDialog.setMessage("开始升级，可能需要几分钟，请不要给摄像头断电。"+percent+"%");
							}
						}
						if(stage.equals("2") && percent.equals("100")){
							//升级成功
							dismissUpdateDialog();
							if(mUpdateSuccess == null){
								mUpdateSuccess = new AlertDialog.Builder(mContext);
								mUpdateDialogSuccess = mUpdateSuccess.setMessage("升级成功")
										.setPositiveButton("确定", null)
										.create();
								mUpdateDialogSuccess.show();
							}
						}
					}catch(Exception e){
						e.printStackTrace();
					}
				}else{
					if(mUpdateFail == null){
						mUpdateFail = new AlertDialog.Builder(mContext);
						mUpdateDialogFail = mUpdateFail.setMessage("升级失败")
								.setPositiveButton("确定", null)
								.create();
						mUpdateDialogFail.show();
					}
				}
			}
			
		}
	} 
    
    /**
     * 固件升级正在传输文件对话框消失
     */
    public void dismissSendDialog(){
		if (null != mSendDialog){
			mSendDialog.dismiss();
			mSendDialog = null;
		}
	}
    
    /**
     * 固件升级正在升级中对话框消失
     */
    public void dismissUpdateDialog(){
		if (null != mUpdateDialog){
			mUpdateDialog.dismiss();
			mUpdateDialog = null;
		}
	}
    
    @Override
	protected void onDestroy() {
		super.onDestroy();
		if(null != GolukApplication.getInstance().getIPCControlManager()){
			GolukApplication.getInstance().getIPCControlManager().removeIPCManagerListener("carversion");
		}
	}

}
