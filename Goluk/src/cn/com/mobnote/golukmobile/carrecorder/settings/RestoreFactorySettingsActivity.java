package cn.com.mobnote.golukmobile.carrecorder.settings;

import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.base.BaseActivity;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomDialog;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomDialog.OnLeftClickListener;
import cn.com.mobnote.module.ipcmanager.IPCManagerFn;
import cn.com.tiros.utils.LogUtil;

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
  * 恢复出厂设置
  *
  * 2015年4月9日
  *
  * @author xuhw
  */
public class RestoreFactorySettingsActivity extends BaseActivity implements OnClickListener, IPCManagerFn{
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addContentView(LayoutInflater.from(this).inflate(R.layout.carrecorder_restore_factory_settings, null)); 
		setTitle("恢复出厂设置");
		
		findViewById(R.id.mFormat).setOnClickListener(this);
		GolukApplication.getInstance().getIPCControlManager().addIPCManagerListener("restore", this);
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
			case R.id.back_btn:
				exit(); 
				break;
			case R.id.mFormat:
					CustomDialog mCustomDialog = new CustomDialog(this);
					mCustomDialog.setMessage("是否确认恢复Goluk出厂设置", Gravity.CENTER);
					mCustomDialog.setLeftButton("确认", new OnLeftClickListener() {
						@Override
						public void onClickListener() {
							if(GolukApplication.getInstance().getIpcIsLogin()){
								boolean a = GolukApplication.getInstance().getIPCControlManager().restoreIPC();
								LogUtil.e("xuhw", "YYYYYY=================restoreIPC============a="+a);
							}
						}
					});
					mCustomDialog.setRightButton("取消", null);
					mCustomDialog.show();
				break;
	
			default:
				break;
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		GolukApplication.getInstance().setContext(this, "restorefactory");
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
	}

	@Override
	public void IPCManage_CallBack(int event, int msg, int param1, Object param2) {
		if(event == ENetTransEvent_IPC_VDCP_CommandResp){
			if(msg == IPC_VDCP_Msg_Restore){
				System.out.println("YYY====IPC_VDCP_Msg_Restore====msg="+msg+"===param1="+param1+"==param2="+param2);
				String message="";
				if(param1 == RESULE_SUCESS){
					System.out.println("YYY=====IPC_VDCP_Msg_Restore============OK=============");
					message = "恢复出厂设置成功";
				}else{
					message = "恢复出厂设置失败";
				}
				
				CustomDialog mCustomDialog = new CustomDialog(this);
				mCustomDialog.setCancelable(false);
				mCustomDialog.setMessage(message, Gravity.CENTER);
				mCustomDialog.setLeftButton("确认", new OnLeftClickListener() {
					@Override
					public void onClickListener() {
						exit();
					}
				});
				mCustomDialog.show();
			}
		}
	}
	
	public void exit(){
		if(null != GolukApplication.getInstance().getIPCControlManager()){
			GolukApplication.getInstance().getIPCControlManager().removeIPCManagerListener("restore");
		}
		finish();
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {
    	if(keyCode==KeyEvent.KEYCODE_BACK){
    		exit(); 
        	return true;
        }else
        	return super.onKeyDown(keyCode, event); 
	}

}
