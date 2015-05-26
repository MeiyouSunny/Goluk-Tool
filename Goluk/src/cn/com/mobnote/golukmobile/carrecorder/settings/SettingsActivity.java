package cn.com.mobnote.golukmobile.carrecorder.settings;

import org.json.JSONException;
import org.json.JSONObject;

import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.BaseActivity;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.IpcDataParser;
import cn.com.mobnote.golukmobile.carrecorder.entity.VideoConfigState;
import cn.com.mobnote.golukmobile.carrecorder.util.SettingUtils;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomDialog;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomLoadingDialog;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomDialog.OnLeftClickListener;
import cn.com.mobnote.module.ipcmanager.IPCManagerFn;
import cn.com.tiros.utils.LogUtil;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

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
  * IPC设置界面
  *
  * 2015年4月6日
  *
  * @author xuhw
  */
public class SettingsActivity extends BaseActivity implements OnClickListener, IPCManagerFn{
	/**  录制状态  */
	private boolean recordState=false;
	/** 自动循环录像开关按钮 */
	private Button mAutoRecordBtn=null;
	/** 声音录制开关按钮 */
	private Button mAudioBtn=null;
	/**  音视频配置信息  */
	private VideoConfigState mVideoConfigState=null;
	private int enableSecurity=0;
	private int snapInterval = 0;
	private CustomLoadingDialog mCustomProgressDialog=null;
	private boolean getRecordState=false;
	private boolean getMotionCfg=false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.carrecorder_settings);
		initView();
		setListener();
		
		mCustomProgressDialog = new CustomLoadingDialog(this,null);
		if(null != GolukApplication.getInstance().getIPCControlManager()){
			GolukApplication.getInstance().getIPCControlManager().addIPCManagerListener("settings", this);
		}
		
		boolean record = GolukApplication.getInstance().getIPCControlManager().getRecordState();
		if(!record){
			getRecordState=true;
			checkGetState();
		}
		LogUtil.e("xuhw", "YYYYYY=========getRecordState=========" + record);
		boolean motionCfg = GolukApplication.getInstance().getIPCControlManager().getMotionCfg();
		if(!motionCfg){
			getMotionCfg=true;
			checkGetState();
		}
		LogUtil.e("xuhw", "YYYYYY=========getMotionCfg========="+ motionCfg);
		showLoading();
	}
	
	private void checkGetState(){
		if(getRecordState && getMotionCfg){
			closeLoading();
		}
	}
	
	/**
	 * 初始化控件
	 * @author xuhw
	 * @date 2015年4月6日
	 */
	private void initView(){
		mAutoRecordBtn = (Button)findViewById(R.id.zdxhlx);
		mAudioBtn = (Button)findViewById(R.id.sylz);
		
		mAutoRecordBtn.setBackgroundResource(R.drawable.carrecorder_setup_option_on);
		findViewById(R.id.tcaf).setBackgroundResource(R.drawable.carrecorder_setup_option_off);//打开
	}
	
	/**
	 * 设置监听
	 * @author xuhw
	 * @date 2015年4月6日
	 */
	private void setListener(){
		findViewById(R.id.back_btn).setOnClickListener(this);//返回按钮
		findViewById(R.id.mVideoDefinition).setOnClickListener(this);//视频质量
		findViewById(R.id.zdxhlx).setOnClickListener(this);//自动循环录像按钮
		findViewById(R.id.tcaf).setOnClickListener(this);//停车安防按钮
		findViewById(R.id.sylz).setOnClickListener(this);//声音录制
		findViewById(R.id.ztts).setOnClickListener(this);//状态提示灯
		findViewById(R.id.pzgylmd_line).setOnClickListener(this);//碰撞感应灵敏度
		
		findViewById(R.id.rlcx_line).setOnClickListener(this);//存储容量查询
		findViewById(R.id.sysz_line).setOnClickListener(this);//水印设置
		findViewById(R.id.sjsz_line).setOnClickListener(this);//时间设置
		findViewById(R.id.gshsdk_line).setOnClickListener(this);//格式化SDK卡
		findViewById(R.id.hfccsz_line).setOnClickListener(this);//恢复出厂设置
		findViewById(R.id.bbxx_line).setOnClickListener(this);//版本信息
	}
	
	/**
	 * 摄像头未连接提示框
	 * @author xuhw
	 * @date 2015年4月8日
	 */
	private void dialog(){
		CustomDialog d = new CustomDialog(this);
		d.setCancelable(false);
		d.setMessage("请检查摄像头是否正常连接");
		d.setLeftButton("确定", new OnLeftClickListener() {
			@Override
			public void onClickListener() {
				exit();
			}
		});
		d.show();
	}

	@Override
	public void onClick(View arg0) {
		if(R.id.back_btn == arg0.getId()){
			exit(); 
			return;
		}
		if(GolukApplication.getInstance().getIpcIsLogin()){
			switch (arg0.getId()) {
				case R.id.mVideoDefinition://视频质量
					Intent mVideoDefinition = new Intent(SettingsActivity.this, VideoQualityActivity.class);
					startActivity(mVideoDefinition);
					break;
				case R.id.zdxhlx://自动循环录像
					showLoading();
					if(recordState){
						boolean a = GolukApplication.getInstance().getIPCControlManager().stopRecord();
						LogUtil.e("xuhw", "video===========stopRecord=============a="+a);
					}else{
						boolean b = GolukApplication.getInstance().getIPCControlManager().startRecord();
						LogUtil.e("xuhw", "video===========startRecord=============b="+b);
					}
					break;
				case R.id.tcaf://停车安防
					showLoading();
					if(1 == enableSecurity){
						enableSecurity = 0;
					}else{
						enableSecurity = 1;
					}
					boolean c = GolukApplication.getInstance().getIPCControlManager().setMotionCfg(enableSecurity, snapInterval);
					LogUtil.e("xuhw", "YYYYYY===========setMotionCfg==========a="+c);
					break;
				case R.id.sylz://声音录制
					showLoading();
					if(null != mVideoConfigState){
						if(1 == mVideoConfigState.AudioEnabled){
							mVideoConfigState.AudioEnabled=0;
							boolean a = GolukApplication.getInstance().getIPCControlManager().setAudioCfg(mVideoConfigState);
							LogUtil.e("xuhw", "YYYYYY===========setAudioCfg=======close=======a="+a);
						}else{
							mVideoConfigState.AudioEnabled=1;
							boolean a = GolukApplication.getInstance().getIPCControlManager().setAudioCfg(mVideoConfigState);
							LogUtil.e("xuhw", "YYYYYY===========setAudioCfg=======open=======a="+a);
						}
					}
					break;
				case R.id.ztts://状态提示灯
					boolean ztts = SettingUtils.getInstance().getBoolean("ztts");
					this.setButtonsBk(ztts, R.id.ztts, "ztts");
					break;
				case R.id.pzgylmd_line://碰撞感应灵敏度
					Intent pzgylmd_line = new Intent(SettingsActivity.this, ImpactSensitivityActivity.class);
					startActivity(pzgylmd_line);
					break;
				case R.id.rlcx_line://容量查询
					Intent rlcx_line = new Intent(SettingsActivity.this, StorageCpacityQueryActivity.class);
					startActivity(rlcx_line);
					break;
				case R.id.sysz_line://水印设置
					Intent sysz_line = new Intent(SettingsActivity.this, WatermarkSettingActivity.class);
					startActivity(sysz_line);
					break;
				case R.id.sjsz_line://时间设置
					Intent sjsz_line = new Intent(SettingsActivity.this, TimeSettingActivity.class);
					startActivity(sjsz_line);
					break;
				case R.id.gshsdk_line://格式化SDK卡
					Intent gshsdk_line = new Intent(SettingsActivity.this, FormatSDCardActivity.class);
					startActivity(gshsdk_line);
					break;
				case R.id.hfccsz_line://恢复出厂设置
					Intent hfccsz_line = new Intent(SettingsActivity.this, RestoreFactorySettingsActivity.class);
					startActivity(hfccsz_line);
					break;
				case R.id.bbxx_line://版本信息
					Intent bbxx = new Intent(SettingsActivity.this, VersionActivity.class);
					startActivity(bbxx);
					break;
			default:
				break;
			}
		}else{
			dialog();
		}
	}
	
	/**
	 * 设置按钮的背景图片并保存属性值
	  * @Title: setButtonsBk 
	  * @Description: TODO
	  * @param flog
	  * @param id
	  * @param btn void 
	  * @author 曾浩 
	  * @throws
	 */
	private void setButtonsBk(boolean flog,int id,String btn){
		if(flog){
			findViewById(id).setBackgroundResource(R.drawable.carrecorder_setup_option_off);
			SettingUtils.getInstance().putBoolean(btn, false);
		}else{
			findViewById(id).setBackgroundResource(R.drawable.carrecorder_setup_option_on);
			SettingUtils.getInstance().putBoolean(btn, true);
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		GolukApplication.getInstance().setContext(this, "carrecordsettings");
		mVideoConfigState = GolukApplication.getInstance().getVideoConfigState();	
		if(null != mVideoConfigState){
			if(1 == mVideoConfigState.AudioEnabled){
				mAudioBtn.setBackgroundResource(R.drawable.carrecorder_setup_option_on);
			}else{
				mAudioBtn.setBackgroundResource(R.drawable.carrecorder_setup_option_off);
			}
		}else{
			mAudioBtn.setBackgroundResource(R.drawable.carrecorder_setup_option_off);
		}
		
		
//		recordState = GolukApplication.getInstance().getAutoRecordState();
//		if(!GolukApplication.getInstance().getAutoRecordState()){
//			mAutoRecordBtn.setBackgroundResource(R.drawable.carrecorder_setup_option_off);
//		}else{
//			mAutoRecordBtn.setBackgroundResource(R.drawable.carrecorder_setup_option_on);
//		}
//		
//		int[] motioncfg = GolukApplication.getInstance().getMotionCfg();
//		if(null != motioncfg){
//			if(2 == motioncfg.length){
//				enableSecurity = motioncfg[0];
//				snapInterval = motioncfg[1];
//			}
//			
//			if(1 == enableSecurity){
//				findViewById(R.id.tcaf).setBackgroundResource(R.drawable.carrecorder_setup_option_on);//打开
//			}else{
//				findViewById(R.id.tcaf).setBackgroundResource(R.drawable.carrecorder_setup_option_off);//关闭
//			}
//		}
		
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
	}

	@Override
	public void IPCManage_CallBack(int event, int msg, int param1, Object param2) {
		LogUtil.e("jyf", "YYYYYYY----IPCManage_CallBack-----44444-----------event:" + event + " msg:" + msg+"==data:"+(String)param2);
		if (event == ENetTransEvent_IPC_VDCP_CommandResp) {
			if(msg == IPC_VDCP_Msg_GetRecordState){//获取IPC行车影像录制状态
				getRecordState=true;
				checkGetState();
				if(RESULE_SUCESS == param1){
					recordState = IpcDataParser.getAutoRecordState((String)param2);
					if(!recordState){
						mAutoRecordBtn.setBackgroundResource(R.drawable.carrecorder_setup_option_off);
					}else{
						mAutoRecordBtn.setBackgroundResource(R.drawable.carrecorder_setup_option_on);
					}
				}else{
					//录制状态获取失败
					mAutoRecordBtn.setBackgroundResource(R.drawable.carrecorder_setup_option_off);
				}
			}else if(msg == IPC_VDCP_Msg_StartRecord){//设置IPC行车影像开始录制
				closeLoading();
				if(RESULE_SUCESS == param1){
					recordState=true;
					mAutoRecordBtn.setBackgroundResource(R.drawable.carrecorder_setup_option_on);
				}
			}else if(msg == IPC_VDCP_Msg_StopRecord){//设置IPC行车影像停止录制
				closeLoading();
				if(RESULE_SUCESS == param1){
					recordState=false;
					mAutoRecordBtn.setBackgroundResource(R.drawable.carrecorder_setup_option_off);
				}
			}else if(msg == IPC_VDCP_Msg_GetVedioEncodeCfg){//获取IPC系统音视频编码配置
				if(RESULE_SUCESS == param1){
					mVideoConfigState = IpcDataParser.parseVideoConfigState((String)param2);
					if(null != mVideoConfigState){
						if(1 == mVideoConfigState.AudioEnabled){
							mAudioBtn.setBackgroundResource(R.drawable.carrecorder_setup_option_on);
						}else{
							mAudioBtn.setBackgroundResource(R.drawable.carrecorder_setup_option_off);
						}
					}else{
						mAudioBtn.setBackgroundResource(R.drawable.carrecorder_setup_option_off);
					}
				}else{
					mAudioBtn.setBackgroundResource(R.drawable.carrecorder_setup_option_off);
				}
			}else if(msg == IPC_VDCP_Msg_SetVedioEncodeCfg){//设置IPC系统音视频编码配置
				closeLoading();
				if(RESULE_SUCESS == param1){
					
				}
			}else if(msg == IPC_VDCP_Msg_GetMotionCfg){//读取安防模式和移动侦测参数
				getMotionCfg=true;
				checkGetState();
				if(RESULE_SUCESS == param1){
					try {
						JSONObject json = new JSONObject((String)param2);
						if(null != json){
							enableSecurity = json.getInt("enableSecurity");
							snapInterval = json.getInt("snapInterval");
							if(1 == enableSecurity){
								findViewById(R.id.tcaf).setBackgroundResource(R.drawable.carrecorder_setup_option_on);//打开
							}else{
								findViewById(R.id.tcaf).setBackgroundResource(R.drawable.carrecorder_setup_option_off);//关闭
							}
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}else if(msg == IPC_VDCP_Msg_SetMotionCfg){//设置安防模式和移动侦测参数
				closeLoading();
				if(RESULE_SUCESS == param1){
					if(1 == enableSecurity){
						findViewById(R.id.tcaf).setBackgroundResource(R.drawable.carrecorder_setup_option_on);//打开
					}else{
						findViewById(R.id.tcaf).setBackgroundResource(R.drawable.carrecorder_setup_option_off);//关闭
					}
				}else{
					if(1 == enableSecurity){
						enableSecurity = 0;
					}else{
						enableSecurity = 1;
					}
				}
			}
		}
	}
	
	private void showLoading(){
		if(!mCustomProgressDialog.isShowing()){
			mCustomProgressDialog.show();
		}
	}
	
	private void closeLoading(){
		if(mCustomProgressDialog.isShowing()){
			mCustomProgressDialog.close();
		}
	}
	
	public void exit(){
		if(null != GolukApplication.getInstance().getIPCControlManager()){
			GolukApplication.getInstance().getIPCControlManager().removeIPCManagerListener("settings");
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
