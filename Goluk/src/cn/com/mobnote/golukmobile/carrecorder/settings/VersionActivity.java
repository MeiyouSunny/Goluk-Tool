package cn.com.mobnote.golukmobile.carrecorder.settings;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.IpcDataParser;
import cn.com.mobnote.golukmobile.carrecorder.base.CarRecordBaseActivity;
import cn.com.mobnote.golukmobile.carrecorder.entity.IPCIdentityState;
import cn.com.mobnote.golukmobile.carrecorder.util.SoundUtils;
import cn.com.mobnote.map.LngLat;
import cn.com.mobnote.module.ipcmanager.IPCManagerFn;
import cn.com.tiros.debug.GolukDebugUtils;

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
  * 版本信息
  *
  * 2015年4月9日
  *
  * @author xuhw
  */
@SuppressLint("InflateParams")
public class VersionActivity extends CarRecordBaseActivity implements IPCManagerFn{
	/** Goluk设备编号 */
	private TextView mDeviceId=null;
	/** 固件版本号 */
	private TextView mVersion=null;
	/**ipc设备型号**/
	private TextView mTextIpcModel = null;
	/****/
	private String mIpcModelName = "";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addContentView(LayoutInflater.from(this).inflate(R.layout.carrecorder_version, null)); 
		setTitle("版本信息");
		
		if(null != GolukApplication.getInstance().getIPCControlManager()){
			GolukApplication.getInstance().getIPCControlManager().addIPCManagerListener("carversion", this);
		}
		mIpcModelName = GolukApplication.getInstance().mSharedPreUtil.getIpcModel();
		mDeviceId = (TextView)findViewById(R.id.mDeviceId);
		mVersion = (TextView)findViewById(R.id.mVersion);
		mTextIpcModel = (TextView) findViewById(R.id.text_model);
		
		mDeviceId.setText("");
		mVersion.setText("");
		mTextIpcModel.setText("极路客"+mIpcModelName);
		if(GolukApplication.getInstance().getIpcIsLogin()){
			boolean a = GolukApplication.getInstance().getIPCControlManager().getIPCIdentity();
			GolukDebugUtils.e("xuhw","YYYYYY=======getIPCIdentity============a="+a);
			
			boolean v = GolukApplication.getInstance().getIPCControlManager().getVersion();
			GolukDebugUtils.e("xuhw","YYYYYY=======getVersion============v="+v);
		}
		
		float density = SoundUtils.getInstance().getDisplayMetrics().density;
		int screedWidth = SoundUtils.getInstance().getDisplayMetrics().widthPixels;
		LinearLayout idlayout = (LinearLayout)findViewById(R.id.idlayout);
		LinearLayout vsnlayout = (LinearLayout)findViewById(R.id.vsnlayout);
		int paddingLeft = (int)(screedWidth/2 - 85*density);
		idlayout.setPadding(paddingLeft, (int)(58*density), 0, 0);
		vsnlayout.setPadding(paddingLeft, (int)(6*density), 0, 0);
		
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
	}

	@Override
	public void IPCManage_CallBack(int event, int msg, int param1, Object param2) {
		GolukDebugUtils.e("xuhw", "YYYYYY====IPC_VDCP_Msg_GetIdentity====msg="+msg+"===param1="+param1+"==param2="+param2);
		
		if(event == ENetTransEvent_IPC_VDCP_CommandResp){
			if(msg == IPC_VDCP_Msg_GetIdentity){
				if(param1 == RESULE_SUCESS){
					final IPCIdentityState mVersionState = IpcDataParser.parseVersionState((String)param2);
					if(null != mVersionState){
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
//								mDeviceId.setText(""+mVersionState.code);
								mDeviceId.setText(mVersionState.name);
							}
						});
					}
				}
			}else if(IPC_VDCP_Msg_GetVersion == msg){
				if(param1 == RESULE_SUCESS){
					String str = (String)param2;
					if(TextUtils.isEmpty(str)){
						return;
					}
					
					try {
						JSONObject json = new JSONObject(str);
						String version = json.optString("version");
						mVersion.setText(version);
					} catch (JSONException e) {
						e.printStackTrace();
					}
					
				}
			}
		}
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		GolukApplication.getInstance().setContext(this, "carrecordversion");
	}
	
	public void exit(){
		if(null != GolukApplication.getInstance().getIPCControlManager()){
			GolukApplication.getInstance().getIPCControlManager().removeIPCManagerListener("carversion");
		}
		finish();
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.back_btn:
				exit(); 
				break;
				
			default:
				break;
		}
	}  
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {
    	if(keyCode==KeyEvent.KEYCODE_BACK){
    		exit(); 
        	return true;
        }else
        	return super.onKeyDown(keyCode, event); 
	}

}
