package cn.com.mobnote.golukmobile.carrecorder.settings;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.base.CarRecordBaseActivity;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomDialog;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomDialog.OnLeftClickListener;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomFormatDialog;
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
  * 格式化SD卡
  *
  * 2015年4月9日
  *
  * @author xuhw
  */
public class FormatSDCardActivity extends CarRecordBaseActivity implements OnClickListener, IPCManagerFn{
	private CustomFormatDialog mCustomFormatDialog=null;
	
	@SuppressLint("InflateParams")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addContentView(LayoutInflater.from(this).inflate(R.layout.carrecorder_format_sd_card, null)); 
		setTitle("格式化SD卡");
		
		findViewById(R.id.mFormat).setOnClickListener(this);
		if(null != GolukApplication.getInstance().getIPCControlManager()){
			GolukApplication.getInstance().getIPCControlManager().addIPCManagerListener("format", this);
		}
	}
	
	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
			case R.id.back_btn:
				exit(); 
				break;
			case R.id.mFormat:
				CustomDialog dialog = new CustomDialog(this);
				dialog.setMessage("是否格式化SD卡？", Gravity.CENTER);
				dialog.setLeftButton("是", new OnLeftClickListener() {
					@Override
					public void onClickListener() {
						if(GolukApplication.getInstance().getIpcIsLogin()){
							boolean flag = GolukApplication.getInstance().getIPCControlManager().formatDisk();
							GolukDebugUtils.e("xuhw", "YYYYYY=====formatDisk===flag="+flag);
							if(flag){
//								if(null == mCustomFormatDialog){
									mCustomFormatDialog = new CustomFormatDialog(FormatSDCardActivity.this);
									mCustomFormatDialog.setCancelable(false);
									mCustomFormatDialog.setMessage("正在格式化SD卡，可能需要1~2分钟，请稍候...");
									mCustomFormatDialog.show();
//								}
							}
						}
					}
				});
				dialog.setRightButton("否", null);
				dialog.show();
				break;
	
			default:
				break;
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		GolukApplication.getInstance().setContext(this, "formatsdcard");
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
	}

	@Override
	public void IPCManage_CallBack(int event, int msg, int param1, Object param2) {
		if(event == ENetTransEvent_IPC_VDCP_CommandResp){
			if(msg == IPC_VDCP_Msg_FormatDisk){
				if(null != mCustomFormatDialog && mCustomFormatDialog.isShowing()){
					mCustomFormatDialog.dismiss();
				}
				GolukDebugUtils.e("xuhw", "YYYYYY====IPC_VDCP_Msg_FormatDisk====msg="+msg+"===param1="+param1+"==param2="+param2);
				String message="";
				if(param1 == RESULE_SUCESS){
					message = "SD卡格式化成功";
				}else{
					message = "SD卡格式化失败";
				}
				CustomDialog dialog = new CustomDialog(this);
				dialog.setMessage(message, Gravity.CENTER);
				dialog.setLeftButton("确定", null);
				dialog.show();
			}
		}
	}

	public void exit(){
		if(null != GolukApplication.getInstance().getIPCControlManager()){
			GolukApplication.getInstance().getIPCControlManager().removeIPCManagerListener("format");
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
