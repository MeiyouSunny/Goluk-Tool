package cn.com.mobnote.golukmobile.carrecorder.settings;

import org.json.JSONException;
import org.json.JSONObject;

import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.BaseActivity;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.UserOpenUrlActivity;
import cn.com.mobnote.golukmobile.carrecorder.IpcDataParser;
import cn.com.mobnote.golukmobile.carrecorder.entity.RecordStorgeState;
import cn.com.mobnote.golukmobile.carrecorder.entity.VideoConfigState;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomDialog;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomLoadingDialog;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomDialog.OnLeftClickListener;
import cn.com.mobnote.module.ipcmanager.IPCManagerFn;
import cn.com.mobnote.util.GolukUtils;
import cn.com.tiros.debug.GolukDebugUtils;
import android.os.Bundle;
import android.content.Intent;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

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
public class SettingsActivity extends BaseActivity implements OnClickListener, IPCManagerFn {
	/** 录制状态 */
	private boolean recordState = false;
	/** 自动循环录像开关按钮 */
	private Button mAutoRecordBtn = null;
	/** 声音录制开关按钮 */
	private Button mAudioBtn = null;
	/** 开关机提示音 **/
	private Button mSwitchBtn = null;
	/** 音视频配置信息 */
	private VideoConfigState mVideoConfigState = null;
	private int enableSecurity = 0;
	private int snapInterval = 0;
	private CustomLoadingDialog mCustomProgressDialog = null;
	private boolean getRecordState = false;
	private boolean getMotionCfg = false;
	/** 存储容量显示 */
	private TextView mStorayeText = null;
	/** 视频质量显示 */
	private TextView mVideoText = null;
	/** 碰撞灵敏度显示 */
	private TextView mSensitivityText = null;
	/** IPC开关机提示音状态 true打开  false关闭**/
	private boolean setSwitchStatus = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.carrecorder_settings);
		initView();
		setListener();

		mCustomProgressDialog = new CustomLoadingDialog(this, null);
		if (null != GolukApplication.getInstance().getIPCControlManager()) {
			GolukApplication.getInstance().getIPCControlManager().addIPCManagerListener("settings", this);
		}

		boolean record = GolukApplication.getInstance().getIPCControlManager().getRecordState();
		if (!record) {
			getRecordState = true;
			checkGetState();
		}
		GolukDebugUtils.e("xuhw", "YYYYYY=========getRecordState=========" + record);
		boolean motionCfg = GolukApplication.getInstance().getIPCControlManager().getMotionCfg();
		if (!motionCfg) {
			getMotionCfg = true;
			checkGetState();
		}

		if (GolukApplication.getInstance().getIpcIsLogin()) {
			boolean flag = GolukApplication.getInstance().getIPCControlManager().queryRecordStorageStatus();
			GolukDebugUtils.e("xuhw", "YYY======queryRecordStorageStatus=====flag=" + flag);
		}

		boolean flag = GolukApplication.getInstance().getIPCControlManager().getGSensorControlCfg();
		GolukDebugUtils.e("xuhw", "YYYYY===getIPCControlManager============getGSensorControlCfg======flag=" + flag);

		// 获取ipc开关机提示音状态
		boolean switchFlag = GolukApplication.getInstance().getIPCControlManager().getIPCSwitchState();
		GolukDebugUtils.e("lily", "---------------switchFlag----------------" + switchFlag);

		GolukDebugUtils.e("xuhw", "YYYYYY=========getMotionCfg=========" + motionCfg);
		showLoading();
	}

	private void checkGetState() {
		if (getRecordState && getMotionCfg) {
			closeLoading();
		}
	}

	/**
	 * 初始化控件
	 * 
	 * @author xuhw
	 * @date 2015年4月6日
	 */
	private void initView() {
		mAutoRecordBtn = (Button) findViewById(R.id.zdxhlx);
		mAudioBtn = (Button) findViewById(R.id.sylz);
		mSwitchBtn = (Button) findViewById(R.id.kgjtsy);

		mAutoRecordBtn.setBackgroundResource(R.drawable.set_open_btn);
		findViewById(R.id.tcaf).setBackgroundResource(R.drawable.set_close_btn);// 打开
		mStorayeText = (TextView) findViewById(R.id.mStorayeText);
		mVideoText = (TextView) findViewById(R.id.mVideoText);
		mSensitivityText = (TextView) findViewById(R.id.mSensitivityText);

		mStorayeText.setText("0MB/0MB");
	}

	/**
	 * 设置监听
	 * 
	 * @author xuhw
	 * @date 2015年4月6日
	 */
	private void setListener() {
		findViewById(R.id.back_btn).setOnClickListener(this);// 返回按钮
		findViewById(R.id.mVideoDefinition).setOnClickListener(this);// 视频质量
		findViewById(R.id.zdxhlx).setOnClickListener(this);// 自动循环录像按钮
		findViewById(R.id.tcaf).setOnClickListener(this);// 停车安防按钮
		findViewById(R.id.sylz).setOnClickListener(this);// 声音录制
		findViewById(R.id.pzgylmd_line).setOnClickListener(this);// 碰撞感应灵敏度
		findViewById(R.id.kgjtsy).setOnClickListener(this);// 开关机提示音

		findViewById(R.id.rlcx_line).setOnClickListener(this);// 存储容量查询
		findViewById(R.id.sjsz_line).setOnClickListener(this);// 时间设置
		findViewById(R.id.hfccsz_line).setOnClickListener(this);// 恢复出厂设置
		findViewById(R.id.bbxx_line).setOnClickListener(this);// 版本信息
		findViewById(R.id.mBugLayout).setOnClickListener(this);// 购买降压线
	}

	/**
	 * 摄像头未连接提示框
	 * 
	 * @author xuhw
	 * @date 2015年4月8日
	 */
	private void dialog() {
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
		if (R.id.back_btn == arg0.getId()) {
			exit();
			return;
		}
		if (GolukApplication.getInstance().getIpcIsLogin()) {
			switch (arg0.getId()) {
			case R.id.mVideoDefinition:// 视频质量
				Intent mVideoDefinition = new Intent(SettingsActivity.this, VideoQualityActivity.class);
				startActivity(mVideoDefinition);
				break;
			case R.id.zdxhlx:// 自动循环录像
				showLoading();
				if (recordState) {
					boolean a = GolukApplication.getInstance().getIPCControlManager().stopRecord();
					GolukDebugUtils.e("xuhw", "video===========stopRecord=============a=" + a);
				} else {
					boolean b = GolukApplication.getInstance().getIPCControlManager().startRecord();
					GolukDebugUtils.e("xuhw", "video===========startRecord=============b=" + b);
				}
				break;
			case R.id.tcaf:// 停车安防
				showLoading();
				if (1 == enableSecurity) {
					enableSecurity = 0;
				} else {
					enableSecurity = 1;
				}
				boolean c = GolukApplication.getInstance().getIPCControlManager()
						.setMotionCfg(enableSecurity, snapInterval);
				GolukDebugUtils.e("xuhw", "YYYYYY===========setMotionCfg==========a=" + c);
				break;
			case R.id.sylz:// 声音录制
				showLoading();
				if (null != mVideoConfigState) {
					if (1 == mVideoConfigState.AudioEnabled) {
						mVideoConfigState.AudioEnabled = 0;
						boolean a = GolukApplication.getInstance().getIPCControlManager()
								.setAudioCfg(mVideoConfigState);
						GolukDebugUtils.e("xuhw", "YYYYYY===========setAudioCfg=======close=======a=" + a);
					} else {
						mVideoConfigState.AudioEnabled = 1;
						boolean a = GolukApplication.getInstance().getIPCControlManager()
								.setAudioCfg(mVideoConfigState);
						GolukDebugUtils.e("xuhw", "YYYYYY===========setAudioCfg=======open=======a=" + a);
					}
				}
				break;
			case R.id.pzgylmd_line:// 碰撞感应灵敏度
				Intent pzgylmd_line = new Intent(SettingsActivity.this, ImpactSensitivityActivity.class);
				startActivity(pzgylmd_line);
				break;
			case R.id.kgjtsy://开关机提示音
				showLoading();
				String status = "";
				if(setSwitchStatus){
					status = "0";
				}else{
					status = "1";
				}
				String condi = "{\"SpeakerSwitch\":" + status + "}";
				boolean b = GolukApplication.getInstance().getIPCControlManager().setIPCSwitchState(condi);
				GolukDebugUtils.e("lily", "---------点击开关结果-----------" + b);
				if(b){
					
				}else{
					closeLoading();
					GolukUtils.showToast(this, "设置失败");
				}
				break;
			case R.id.rlcx_line:// 容量查询
				Intent rlcx_line = new Intent(SettingsActivity.this, StorageCpacityQueryActivity.class);
				startActivity(rlcx_line);
				break;
			case R.id.sjsz_line:// 时间设置
				Intent sjsz_line = new Intent(SettingsActivity.this, TimeSettingActivity.class);
				startActivity(sjsz_line);
				break;
			case R.id.hfccsz_line:// 恢复出厂设置
				CustomDialog mCustomDialog = new CustomDialog(this);
				mCustomDialog.setMessage("是否确认恢复Goluk出厂设置", Gravity.CENTER);
				mCustomDialog.setLeftButton("确认", new OnLeftClickListener() {
					@Override
					public void onClickListener() {
						if (GolukApplication.getInstance().getIpcIsLogin()) {
							boolean a = GolukApplication.getInstance().getIPCControlManager().restoreIPC();
							GolukDebugUtils.e("xuhw", "YYYYYY=================restoreIPC============a=" + a);
						}
					}
				});
				mCustomDialog.setRightButton("取消", null);
				mCustomDialog.show();
				break;
			case R.id.bbxx_line:// 版本信息
				Intent bbxx = new Intent(SettingsActivity.this, VersionActivity.class);
				startActivity(bbxx);
				break;
			case R.id.mBugLayout:
				Intent mBugLayout = new Intent(this, UserOpenUrlActivity.class);
				mBugLayout.putExtra(UserOpenUrlActivity.FROM_TAG, "buyline");
				startActivity(mBugLayout);
				break;

			default:
				break;
			}
		} else {
			dialog();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		GolukApplication.getInstance().setContext(this, "carrecordsettings");
		mVideoConfigState = GolukApplication.getInstance().getVideoConfigState();
		if (null != mVideoConfigState) {
			if (1 == mVideoConfigState.AudioEnabled) {
				mAudioBtn.setBackgroundResource(R.drawable.set_open_btn);
			} else {
				mAudioBtn.setBackgroundResource(R.drawable.set_close_btn);
			}

			updateVideoQualityText();
		} else {
			mAudioBtn.setBackgroundResource(R.drawable.set_close_btn);
		}

	}

	private void updateVideoQualityText() {
		if ("1080P".equals(mVideoConfigState.resolution)) {
			if (8192 == mVideoConfigState.bitrate) {
				mVideoText.setText("1080P高质量");
			} else {
				mVideoText.setText("1080P中等质量");
			}
		} else {
			if (4096 == mVideoConfigState.bitrate) {
				mVideoText.setText("720P高质量");
			} else {
				mVideoText.setText("720P中等质量");
			}
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

	}

	@Override
	public void IPCManage_CallBack(int event, int msg, int param1, Object param2) {
		GolukDebugUtils.e("jyf", "YYYYYYY----IPCManage_CallBack-----44444-----------event:" + event + " msg:" + msg
				+ "==data:" + (String) param2);
		if (event == ENetTransEvent_IPC_VDCP_CommandResp) {
			if (msg == IPC_VDCP_Msg_GetRecordState) {// 获取IPC行车影像录制状态
				getRecordState = true;
				checkGetState();
				if (RESULE_SUCESS == param1) {
					recordState = IpcDataParser.getAutoRecordState((String) param2);
					if (!recordState) {
						mAutoRecordBtn.setBackgroundResource(R.drawable.set_close_btn);
					} else {
						mAutoRecordBtn.setBackgroundResource(R.drawable.set_open_btn);
					}
				} else {
					// 录制状态获取失败
					mAutoRecordBtn.setBackgroundResource(R.drawable.set_close_btn);
				}
			} else if (msg == IPC_VDCP_Msg_StartRecord) {// 设置IPC行车影像开始录制
				closeLoading();
				if (RESULE_SUCESS == param1) {
					recordState = true;
					mAutoRecordBtn.setBackgroundResource(R.drawable.set_open_btn);
				}
			} else if (msg == IPC_VDCP_Msg_StopRecord) {// 设置IPC行车影像停止录制
				closeLoading();
				if (RESULE_SUCESS == param1) {
					recordState = false;
					mAutoRecordBtn.setBackgroundResource(R.drawable.set_close_btn);
				}
			} else if (msg == IPC_VDCP_Msg_GetVedioEncodeCfg) {// 获取IPC系统音视频编码配置
				if (RESULE_SUCESS == param1) {
					mVideoConfigState = IpcDataParser.parseVideoConfigState((String) param2);
					updateVideoQualityText();
					if (null != mVideoConfigState) {
						if (1 == mVideoConfigState.AudioEnabled) {
							mAudioBtn.setBackgroundResource(R.drawable.set_open_btn);
						} else {
							mAudioBtn.setBackgroundResource(R.drawable.set_close_btn);
						}
					} else {
						mAudioBtn.setBackgroundResource(R.drawable.set_close_btn);
					}
				} else {
					mAudioBtn.setBackgroundResource(R.drawable.set_close_btn);
				}
			} else if (msg == IPC_VDCP_Msg_SetVedioEncodeCfg) {// 设置IPC系统音视频编码配置
				closeLoading();
				if (RESULE_SUCESS == param1) {

				}
			} else if (msg == IPC_VDCP_Msg_GetMotionCfg) {// 读取安防模式和移动侦测参数
				getMotionCfg = true;
				checkGetState();
				if (RESULE_SUCESS == param1) {
					try {
						JSONObject json = new JSONObject((String) param2);
						if (null != json) {
							enableSecurity = json.getInt("enableSecurity");
							snapInterval = json.getInt("snapInterval");
							if (1 == enableSecurity) {
								findViewById(R.id.tcaf).setBackgroundResource(R.drawable.set_open_btn);// 打开
							} else {
								findViewById(R.id.tcaf).setBackgroundResource(R.drawable.set_close_btn);// 关闭
							}
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			} else if (msg == IPC_VDCP_Msg_SetMotionCfg) {// 设置安防模式和移动侦测参数
				closeLoading();
				if (RESULE_SUCESS == param1) {
					if (1 == enableSecurity) {
						findViewById(R.id.tcaf).setBackgroundResource(R.drawable.set_open_btn);// 打开
					} else {
						findViewById(R.id.tcaf).setBackgroundResource(R.drawable.set_close_btn);// 关闭
					}
				} else {
					if (1 == enableSecurity) {
						enableSecurity = 0;
					} else {
						enableSecurity = 1;
					}
				}
			} else if (msg == IPC_VDCP_Msg_RecPicUsage) {
				if (RESULE_SUCESS == param1) {
					RecordStorgeState mRecordStorgeState = IpcDataParser.parseRecordStorageStatus((String) param2);
					if (null != mRecordStorgeState) {
						double usedsize = mRecordStorgeState.totalSdSize - mRecordStorgeState.leftSize;
						mStorayeText.setText(getSize(usedsize) + "/" + getSize(mRecordStorgeState.totalSdSize));
					}
				}
			} else if (msg == IPC_VDCP_Msg_GetGSensorControlCfg) {
				if (param1 == RESULE_SUCESS) {
					try {
						JSONObject json = new JSONObject((String) param2);
						int policy = json.optInt("policy");
						if (0 == policy) {
							mSensitivityText.setText("关闭");
						} else if (1 == policy) {
							mSensitivityText.setText("低");
						} else if (2 == policy) {
							mSensitivityText.setText("中");
						} else {
							mSensitivityText.setText("高");
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			} else if (msg == IPC_VDCP_Msg_SetGSensorControlCfg) {
				if (param1 == RESULE_SUCESS) {
					GolukApplication.getInstance().getIPCControlManager().getGSensorControlCfg();
				}
			} else if (msg == IPC_VDCP_Msg_Restore) {
				String message = "";
				if (param1 == RESULE_SUCESS) {
					message = "恢复出厂设置成功";
				} else {
					message = "恢复出厂设置失败";
				}

				if (isFinishing()) {
					return;
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
			} else if (msg == IPC_VDCPCmd_GetSpeakerSwitch) {// 获取ipc开关机声音状态
				closeLoading();
				GolukDebugUtils.e("lily", "------IPC_VDCPCmd_GetSpeakerSwitch----------------param1:"+param1+"----param2:--"+param2);
				if (param1 == RESULE_SUCESS) {
					try {
						JSONObject json = new JSONObject((String) param2);
						GolukDebugUtils.e("lily", "---------IPC_VDCPCmd_GetSpeakerSwitch--------" + json);
						// {"SpeakerSwitch":0} 0关闭 1打开
						String speakerSwitch = json.optString("SpeakerSwitch");
						if ("0".equals(speakerSwitch)) {
							mSwitchBtn.setBackgroundResource(R.drawable.set_close_btn);
							setSwitchStatus = false;
						} else {
							mSwitchBtn.setBackgroundResource(R.drawable.set_open_btn);
							setSwitchStatus = true;
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}else{
					mSwitchBtn.setBackgroundResource(R.drawable.set_open_btn);
					setSwitchStatus = true;
				}
			} else if (msg == IPC_VDCPCmd_SetSpeakerSwitch) {// 设置ipc开关机声音状态
				closeLoading();
				GolukDebugUtils.e("lily", "-------IPC_VDCPCmd_SetSpeakerSwitch------msg-----" + msg+ "-------param2------" + param2+"---param1:--"+param1);
				if (RESULE_SUCESS == param1) {
					if (setSwitchStatus) {
						mSwitchBtn.setBackgroundResource(R.drawable.set_close_btn);
						setSwitchStatus = false;
					} else {
						mSwitchBtn.setBackgroundResource(R.drawable.set_open_btn);
						setSwitchStatus = true;
					}
					GolukUtils.showToast(this, "设置成功");
				}else{
					GolukUtils.showToast(this, "当前固件不支持此项设置，请升级固件后再试");
				}
			}

		}
	}

	/**
	 * 容量大小转字符串
	 * 
	 * @param size
	 *            容量大小
	 * @return
	 * @author xuhw
	 * @date 2015年4月11日
	 */
	private String getSize(double size) {
		String result = "";
		double totalsize = 0;

		java.text.DecimalFormat df = new java.text.DecimalFormat("#.##");
		if (size >= 1024) {
			totalsize = size / 1024;
			result = df.format(totalsize) + "GB";
		} else {
			totalsize = size;
			result = df.format(totalsize) + "MB";
		}

		return result;
	}

	private void showLoading() {
		if (!mCustomProgressDialog.isShowing()) {
			mCustomProgressDialog.show();
		}
	}

	private void closeLoading() {
		if (mCustomProgressDialog.isShowing()) {
			mCustomProgressDialog.close();
		}
	}

	public void exit() {
		if (null != GolukApplication.getInstance().getIPCControlManager()) {
			GolukApplication.getInstance().getIPCControlManager().removeIPCManagerListener("settings");
		}
		finish();
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			exit();
			return true;
		} else
			return super.onKeyDown(keyCode, event);
	}

}
