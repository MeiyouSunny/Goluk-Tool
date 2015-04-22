package cn.com.mobnote.golukmobile.carrecorder.settings;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.TextView;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.IpcDataParser;
import cn.com.mobnote.golukmobile.carrecorder.base.BaseActivity;
import cn.com.mobnote.golukmobile.carrecorder.entity.VersionState;
import cn.com.mobnote.module.ipcmanager.IPCManagerFn;

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
public class VersionActivity extends BaseActivity implements IPCManagerFn{
	/** Goluk设备编号 */
	private TextView mDeviceId=null;
	/** 固件版本号 */
	private TextView mVersion=null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addContentView(LayoutInflater.from(this).inflate(R.layout.carrecorder_version, null)); 
		setTitle("版本信息");
		
		GolukApplication.getInstance().getIPCControlManager().addIPCManagerListener("version", this);
		mDeviceId = (TextView)findViewById(R.id.mDeviceId);
		mVersion = (TextView)findViewById(R.id.mVersion);
		
		mDeviceId.setText("IPC Camera");
		mVersion.setText("V1.0");
		if(GolukApplication.getInstance().getIpcIsLogin()){
			boolean a = GolukApplication.getInstance().getIPCControlManager().getIPCIdentity();
			System.out.println("YYY=======getIPCIdentity============a="+a);
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		GolukApplication.getInstance().getIPCControlManager().removeIPCManagerListener("version");
	}

	@Override
	public void IPCManage_CallBack(int event, int msg, int param1, Object param2) {
		if(event == ENetTransEvent_IPC_VDCP_CommandResp){
			if(msg == IPC_VDCP_Msg_GetIdentity){
				System.out.println("YYY====IPC_VDCP_Msg_GetIdentity====msg="+msg+"===param1="+param1+"==param2="+param2);
				if(param1 == RESULE_SUCESS){
					final VersionState mVersionState = IpcDataParser.parseVersionState((String)param2);
					if(null != mVersionState){
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								mDeviceId.setText(""+mVersionState.code);
								mVersion.setText(mVersionState.name);
							}
						});
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

}
