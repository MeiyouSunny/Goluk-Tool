package cn.com.mobnote.golukmobile.carrecorder.settings;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.base.BaseActivity;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomFormatDialog;
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
  * 格式化SD卡
  *
  * 2015年4月9日
  *
  * @author xuhw
  */
public class FormatSDCardActivity extends BaseActivity implements OnClickListener, IPCManagerFn{
	private CustomFormatDialog mCustomFormatDialog=null;
	
	@SuppressLint("InflateParams")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addContentView(LayoutInflater.from(this).inflate(R.layout.carrecorder_format_sd_card, null)); 
		setTitle("格式化SD卡");
		
		findViewById(R.id.mFormat).setOnClickListener(this);
		GolukApplication.getInstance().getIPCControlManager().addIPCManagerListener("format", this);
	}
	
	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
			case R.id.mFormat:
				if(GolukApplication.getInstance().getIpcIsLogin()){
					
				}
				
				if(null == mCustomFormatDialog){
					mCustomFormatDialog = new CustomFormatDialog(this);
//					mCustomFormatDialog.cancel();
					mCustomFormatDialog.setMessage("正在格式化SD卡，请稍候...");
					mCustomFormatDialog.show();
				}
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
		GolukApplication.getInstance().getIPCControlManager().removeIPCManagerListener("format");
	}

	@Override
	public void IPCManage_CallBack(int event, int msg, int param1, Object param2) {
		if(event == ENetTransEvent_IPC_VDCP_CommandResp){
			if(msg == IPC_VDCP_Msg_FormatDisk){
				System.out.println("YYY====IPC_VDCP_Msg_FormatDisk====msg="+msg+"===param1="+param1+"==param2="+param2);
				if(param1 == RESULE_SUCESS){
					
				}
			}
		}
	}

}
