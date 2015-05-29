package cn.com.mobnote.golukmobile.carrecorder.settings;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.base.CarRecordBaseActivity;
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
  * 水印设置
  *
  * 2015年4月9日
  *
  * @author xuhw
  */
@SuppressLint("InflateParams")
public class WatermarkSettingActivity extends CarRecordBaseActivity implements OnClickListener, IPCManagerFn{
	private Button mLogoBtn=null;
	private Button mTimeBtn=null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addContentView(LayoutInflater.from(this).inflate(R.layout.carrecorder_watermark_setting, null)); 
		setTitle("水印设置");
		
		initView();
		GolukApplication.getInstance().getIPCControlManager().addIPCManagerListener("watermark", this);
		if(GolukApplication.getInstance().getIpcIsLogin()){
			boolean a = GolukApplication.getInstance().getIPCControlManager().getWatermarkShowState();
			GolukDebugUtils.e("xuhw", "YYY=================getWatermarkShowState============a="+a);
		}
	}
	
	private void initView(){
		mLogoBtn = (Button)findViewById(R.id.mLogoBtn);
		mTimeBtn = (Button)findViewById(R.id.mTimeBtn);
		mLogoBtn.setOnClickListener(this);
		mTimeBtn.setOnClickListener(this);
		
		
	}
	
	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
			case R.id.mLogoBtn:
				
				break;
			case R.id.mTimeBtn:
				
				break;
	
			default:
				break;
		}
	}

	@Override
	public void IPCManage_CallBack(int event, int msg, int param1, Object param2) {
		if(event == ENetTransEvent_IPC_VDCP_CommandResp){
			if(msg == IPC_VDCP_Msg_GetImprintShow){
				GolukDebugUtils.e("xuhw", "YYY====IPC_VDCP_Msg_GetImprintShow====msg="+msg+"===param1="+param1+"==param2="+param2);
				if(param1 == RESULE_SUCESS){
					GolukDebugUtils.e("xuhw", "YYY=====IPC_VDCP_Msg_GetImprintShow============OK=============");
					
					
				}
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		GolukApplication.getInstance().setContext(this, "watermarksetting");
	}
}
