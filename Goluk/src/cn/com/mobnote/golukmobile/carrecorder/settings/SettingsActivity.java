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
import cn.com.mobnote.util.JsonUtil;
import cn.com.mobnote.util.SharedPrefUtil;
import cn.com.tiros.debug.GolukDebugUtils;
import android.os.Bundle;
import android.content.Intent;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
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
	/** HDR模式 **/
	private Button mISPBtn = null;
	/**HDR模式line**/
	private RelativeLayout mISPLayout ,mWonderfulLayout;
	/** HDR模式 0关闭 1打开 **/
	private int mISPSwitch = 0;
	/** 精彩视频拍摄提示音 **/
	private Button mWonderVideoBtn = null;
	/** 精彩视频拍摄提示音状态 0关闭 1打开 **/
	private int mWonderfulSwitchStatus = 1;
	/** 开关机提示音(true)和精彩视频拍摄提示音(false)区分 **/
	private boolean judgeSwitch = true;
	/** 开关机提示音 0关闭 1打开 **/
	private int speakerSwitch = 0;
	/** 固件版本号 **/
	private String ipcVersion = "";
	/** ipc设备型号 **/
	private String mIPCName = "";
	private String[] mResolutionArray = null;
	private String[] mBitrateArray = null;
	private String[] mArrayText = null;
	/**照片质量line**/
	private RelativeLayout mPhotoQualityLayout;
	private Button mPhotoQualityBtn = null;
	/**疲劳驾驶**/
	private RelativeLayout mFatigueLayout;
	private Button mFatigueBtn = null;
	/**图像自动翻转**/
	private RelativeLayout mImageFlipLayout;
	private Button mImageFlipBtn = null;
	/**停车休眠模式**/
	private RelativeLayout mParkingSleepLayout;
	private Button mParkingSleepBtn = null;
	/**遥控器按键功能**/
	private RelativeLayout mHandsetLayout;
	/**停车休眠模式提示文字**/
	private TextView mParkingSleepHintText = null;
	/**停车安防模式提示文字**/
	private TextView mParkingSecurityHintText = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.carrecorder_settings);
		// 固件版本号
		ipcVersion = SharedPrefUtil.getIPCVersion();
		GolukDebugUtils.e("", "=========ipcVersion：" + ipcVersion);

		mIPCName = GolukApplication.getInstance().mIPCControlManager.mProduceName;
		GolukDebugUtils.e("", "=========mIPCName：" + mIPCName);
		initView();
		setListener();

		mArrayText = getResources().getStringArray(R.array.list_quality_ui);
		mResolutionArray = SettingsUtil.returnResolution(this, mIPCName);
		mBitrateArray = SettingsUtil.returnBitrate(this, mIPCName);

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

		// 获取ISP模式
		boolean getISPMode = GolukApplication.getInstance().getIPCControlManager().getISPMode();
		GolukDebugUtils.e("", "--------------SettingsActivity-----getISPMode：" + getISPMode);
		
		//照片质量false
		boolean getPhotoQualityMode = GolukApplication.getInstance().getIPCControlManager().getPhotoQualityMode();
		GolukDebugUtils.e("", "--------------SettingsActivity-----getPhotoQualityMode：" + getPhotoQualityMode);
		//获取疲劳驾驶、图像自动翻转、停车休眠模式
		boolean getFunctionMode = GolukApplication.getInstance().getIPCControlManager().getFunctionMode();
		GolukDebugUtils.e("", "--------------SettingsActivity-----getFunctionMode：" + getFunctionMode);
		//获取遥控器按键功能false
		boolean getKitMode = GolukApplication.getInstance().getIPCControlManager().getKitMode();
		GolukDebugUtils.e("", "--------------SettingsActivity-----getKitMode：" + getKitMode);
		
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
		mISPLayout = (RelativeLayout) findViewById(R.id.hdr_line);
		mISPBtn = (Button) findViewById(R.id.hdr);
		mWonderfulLayout = (RelativeLayout) findViewById(R.id.jcsp_line);
		mWonderVideoBtn = (Button) findViewById(R.id.jcsp);
		mPhotoQualityLayout = (RelativeLayout) findViewById(R.id.photographic_quality_line);
		mPhotoQualityBtn = (Button) findViewById(R.id.btn_settings_photographic_quality);
		mFatigueLayout = (RelativeLayout) findViewById(R.id.fatigue_line);
		mFatigueBtn = (Button) findViewById(R.id.btn_settings_fatigue);
		mImageFlipLayout = (RelativeLayout) findViewById(R.id.image_flip_line);
		mImageFlipBtn = (Button) findViewById(R.id.btn_settings_image_flip);
		mParkingSleepLayout = (RelativeLayout) findViewById(R.id.parking_sleep_line);
		mParkingSleepBtn = (Button) findViewById(R.id.btn_settings_parking_sleep);
		mHandsetLayout = (RelativeLayout) findViewById(R.id.handset_line);
		mParkingSleepHintText = (TextView) findViewById(R.id.tv_settings_parking_sleep_hint_text);
		mParkingSecurityHintText = (TextView) findViewById(R.id.tv_settings_security_hint_text);
		// ipc设备型号
		if (mIPCName.equals("G1")) {
			mISPLayout.setVisibility(View.GONE);
			mPhotoQualityLayout.setVisibility(View.GONE);
			mHandsetLayout.setVisibility(View.GONE);
			mFatigueLayout.setVisibility(View.VISIBLE);
			mImageFlipLayout.setVisibility(View.VISIBLE);
			mParkingSleepLayout.setVisibility(View.VISIBLE);
			mParkingSleepHintText.setText(this.getResources().getString(R.string.str_settings_sleep_hint_text_g1));
			mParkingSecurityHintText.setText(this.getResources().getString(R.string.str_settings_security_hint_text_g1));
		} else if (mIPCName.equals("G2")) {
			mISPLayout.setVisibility(View.VISIBLE);
			mPhotoQualityLayout.setVisibility(View.GONE);
			mFatigueLayout.setVisibility(View.GONE);
			mImageFlipLayout.setVisibility(View.GONE);
			mParkingSleepLayout.setVisibility(View.GONE);
			mHandsetLayout.setVisibility(View.GONE);
			mParkingSleepHintText.setVisibility(View.GONE);
			mParkingSecurityHintText.setText(this.getResources().getString(R.string.str_settings_security_hint_text_g2));
		} else {
			mISPLayout.setVisibility(View.VISIBLE);
			mPhotoQualityLayout.setVisibility(View.VISIBLE);
			mFatigueLayout.setVisibility(View.VISIBLE);
			mImageFlipLayout.setVisibility(View.VISIBLE);
			mParkingSleepLayout.setVisibility(View.VISIBLE);
			mHandsetLayout.setVisibility(View.VISIBLE);
			mParkingSleepHintText.setText(this.getResources().getString(R.string.str_settings_sleep_hint_text_t1));
			mParkingSecurityHintText.setText(this.getResources().getString(R.string.str_settings_security_hint_text_g1));
		}

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
		findViewById(R.id.hdr).setOnClickListener(this);// HDR模式
		findViewById(R.id.jcsp).setOnClickListener(this);// 精彩视频拍摄提示音

		findViewById(R.id.rlcx_line).setOnClickListener(this);// 存储容量查询
		findViewById(R.id.sjsz_line).setOnClickListener(this);// 时间设置
		findViewById(R.id.hfccsz_line).setOnClickListener(this);// 恢复出厂设置
		findViewById(R.id.bbxx_line).setOnClickListener(this);// 版本信息
		findViewById(R.id.mBugLayout).setOnClickListener(this);// 购买降压线
		
		mPhotoQualityBtn.setOnClickListener(this);//照片质量
		mFatigueBtn.setOnClickListener(this);//疲劳驾驶
		mImageFlipBtn.setOnClickListener(this);//图像自动翻转
		mParkingSleepBtn.setOnClickListener(this);//停车休眠模式
		mHandsetLayout.setOnClickListener(this);//遥控器按键功能
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
			// HDR模式
			case R.id.hdr:
				showLoading();
				int ispStatus = 0;
				if (0 == mISPSwitch) {
					ispStatus = 1;
				} else {
					ispStatus = 0;
				}
				String ispCondi = "{\"ISPMode\":" + ispStatus + "}";
				boolean setISP = GolukApplication.getInstance().getIPCControlManager().setISPMode(ispCondi);
				GolukDebugUtils.e("", "----------setISPMode-----setISP：" + setISP);
				if (!setISP) {
					closeLoading();
					GolukUtils.showToast(this, "设置失败");
				}
				break;
			case R.id.kgjtsy:// 开关机提示音
				judgeSwitch = true;
				showLoading();
				int status = 1;
				if (0 == speakerSwitch) {
					status = 1;
				} else {
					status = 0;
				}
				String condi = JsonUtil.getSpeakerSwitchJson(status, mWonderfulSwitchStatus);
				boolean b = GolukApplication.getInstance().getIPCControlManager().setIPCSwitchState(condi);
				GolukDebugUtils.e("lily", "---------点击开关结果-----------" + b);
				if (!b) {
					closeLoading();
					GolukUtils.showToast(this, "设置失败");
				}
				break;
			// 精彩视频拍摄提示音
			case R.id.jcsp:
				showLoading();
				judgeSwitch = false;
				// 精彩视频
				int wonderfulStatus = 1;
				if (0 == mWonderfulSwitchStatus) {
					wonderfulStatus = 1;
				} else {
					wonderfulStatus = 0;
				}
				String json = JsonUtil.getSpeakerSwitchJson(speakerSwitch, wonderfulStatus);
				boolean setWonderful = GolukApplication.getInstance().getIPCControlManager().setIPCSwitchState(json);
				GolukDebugUtils.e("", "----------setWonderfulMode-----setWonderful：" + setWonderful);
				if (!setWonderful) {
					closeLoading();
					GolukUtils.showToast(this, "设置失败");
				}
				break;
			//照片质量
			case R.id.btn_settings_photographic_quality:
				
				break;
			//疲劳驾驶
			case R.id.btn_settings_fatigue:
				if (driveFatigue == 1) {
					driveFatigue = 0;
				} else {
					driveFatigue = 1;
				}

				boolean fatigue = GolukApplication.getInstance().getIPCControlManager().setFunctionMode(getSetJson());
				if (fatigue) {
					GolukUtils.showToast(this, "设置...");
				} else {
					GolukUtils.showToast(this, "设置失败");
				}
				break;
			//图像自动翻转
			case R.id.btn_settings_image_flip:
				if (autoRotation == 1) {
					autoRotation = 0;
				} else {
					autoRotation = 1;
				}

				boolean imageFlip = GolukApplication.getInstance().getIPCControlManager().setFunctionMode(getSetJson());
				if (imageFlip) {
					GolukUtils.showToast(this, "设置成功");
				} else {
					GolukUtils.showToast(this, "设置失败");
				}
				break;
			//停车休眠
			case R.id.btn_settings_parking_sleep:
				if (dormant == 1) {
					dormant = 0;
				} else {
					dormant = 1;
				}

				boolean parkingSleep = GolukApplication.getInstance().getIPCControlManager().setFunctionMode(getSetJson());
				if (parkingSleep) {
					GolukUtils.showToast(this, "设置成功");
				} else {
					GolukUtils.showToast(this, "设置失败");
				}
				break;
			//遥控器按键功能
			case R.id.handset_line:
				
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

			setData2UI();
		} else {
			mAudioBtn.setBackgroundResource(R.drawable.set_close_btn);
		}

	}

	// 遍历分辨率，区分码率，改变UI
	private void setData2UI() {
		if (null != mVideoConfigState && null != mResolutionArray && null != mBitrateArray) {
			for (int i = 0; i < mResolutionArray.length; i++) {
				if (mVideoConfigState.resolution.equals(mResolutionArray[i])) {
					if (String.valueOf(mVideoConfigState.bitrate).equals(mBitrateArray[i])) {
						GolukDebugUtils.e("", "---------SettingsActivity-------mArrayText：" + mArrayText[i]);
						mVideoText.setText(mArrayText[i]);
						break;
					}
				}
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
					// updateVideoQualityText();
					setData2UI();
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
			} else if (msg == IPC_VDCP_Msg_GetSpeakerSwitch) {// 获取ipc开关机声音状态
				closeLoading();
				GolukDebugUtils.e("lily", "------IPC_VDCPCmd_GetSpeakerSwitch----------------param1:" + param1
						+ "----param2:--" + param2);
				if (param1 == RESULE_SUCESS) {
					try {
						JSONObject json = new JSONObject((String) param2);
						GolukDebugUtils.e("lily", "---------IPC_VDCPCmd_GetSpeakerSwitch--------" + json);
						// {"SpeakerSwitch":0} 0关闭 1打开
						speakerSwitch = json.optInt("SpeakerSwitch");
						mWonderfulSwitchStatus = json.optInt("WonderfulSwitch");
						if (0 == speakerSwitch) {
							mSwitchBtn.setBackgroundResource(R.drawable.set_close_btn);
						} else {
							mSwitchBtn.setBackgroundResource(R.drawable.set_open_btn);
						}
						// 精彩视频开关机提示音
						if (0 == mWonderfulSwitchStatus) {
							mWonderVideoBtn.setBackgroundResource(R.drawable.set_close_btn);
						} else {
							mWonderVideoBtn.setBackgroundResource(R.drawable.set_open_btn);
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				} else {
					mSwitchBtn.setBackgroundResource(R.drawable.set_open_btn);
					mWonderVideoBtn.setBackgroundResource(R.drawable.set_open_btn);
					speakerSwitch = 1;
					mWonderfulSwitchStatus = 1;
				}
			} else if (msg == IPC_VDCP_Msg_SetSpeakerSwitch) {// 设置ipc开关机声音状态
				closeLoading();
				GolukDebugUtils.e("lily", "-------IPC_VDCPCmd_SetSpeakerSwitch------msg-----" + msg
						+ "-------param2------" + param2 + "---param1:--" + param1);
				if (RESULE_SUCESS == param1) {
					if (judgeSwitch) {
						// 设置开关机提示音
						if (0 == speakerSwitch) {
							speakerSwitch = 1;
							mSwitchBtn.setBackgroundResource(R.drawable.set_open_btn);
						} else {
							speakerSwitch = 0;
							mSwitchBtn.setBackgroundResource(R.drawable.set_close_btn);
						}
					} else {
						// 精彩视频拍摄提示音
						if (0 == mWonderfulSwitchStatus) {
							mWonderfulSwitchStatus = 1;
							mWonderVideoBtn.setBackgroundResource(R.drawable.set_open_btn);
						} else {
							mWonderfulSwitchStatus = 0;
							mWonderVideoBtn.setBackgroundResource(R.drawable.set_close_btn);
						}
					}
					GolukUtils.showToast(this, "设置成功");
				} else {
					GolukUtils.showToast(this, "当前固件不支持此项设置，请升级固件后再试");
				}
			} else if (msg == IPC_VDCP_Msg_GetISPMode) {
				getISPModeCallback(event, msg, param1, param2);
			} else if (msg == IPC_VDCP_Msg_SetISPMode) {
				setISPModeCallback(event, msg, param1, param2);
			} else if (msg == IPC_VDCP_Msg_GetPicCfg) {// 获取照片质量
				getPhotoQualityCallback(event, msg, param1, param2);
			} else if (msg == IPC_VDCP_Msg_SetPicCfg) {// 设置照片质量
				setPhotoQualityCallback(event, msg, param1, param2);
			} else if (msg == IPC_VDCP_Msg_GetFunctionSwitch) {// 获取疲劳驾驶、图像自动翻转、停车休眠模式
				getFunctionCallback(event, msg, param1, param2);
			} else if (msg == IPC_VDCP_Msg_SetFunctionSwitch) {// 设置疲劳驾驶、图像自动翻转、停车休眠模式
				setFunctionCallback(event, msg, param1, param2);
			} else if (msg == IPC_VDCP_Msg_GetKitCfg) {// 获取遥控器按键功能
				getKitConfigCallback(event, msg, param1, param2);
			} else if (msg == IPC_VDCP_Msg_SetKitCfg) {// 设置遥控器按键功能
				setKitConfigCallback(event, msg, param1, param2);
			}
		}
	}

	/**
	 * 获取HDR模式
	 * 
	 * @param event
	 * @param msg
	 * @param param1
	 * @param param2
	 */
	private void getISPModeCallback(int event, int msg, int param1, Object param2) {
		GolukDebugUtils.e("", "--------getISPModeCallback------event：" + event + "------msg：" + msg + "-----param1："
				+ param1 + "------param2：" + param2);
		if (RESULE_SUCESS == param1) {
			try {
				JSONObject json = new JSONObject((String) param2);
				GolukDebugUtils.e("lily", "---------IPC_VDCPCmd_GetISPMode--------" + json);
				mISPSwitch = json.optInt("ISPMode");
				if (0 == mISPSwitch) {
					mISPBtn.setBackgroundResource(R.drawable.set_close_btn);
				} else {
					mISPBtn.setBackgroundResource(R.drawable.set_open_btn);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} else {
			mISPSwitch = 0;
			mISPBtn.setBackgroundResource(R.drawable.set_close_btn);
		}
	}

	/**
	 * 设置HDR模式
	 * 
	 * @param event
	 * @param msg
	 * @param param1
	 * @param param2
	 */
	private void setISPModeCallback(int event, int msg, int param1, Object param2) {
		GolukDebugUtils.e("", "--------setISPModeCallback------event：" + event + "------msg：" + msg + "-----param1："
				+ param1 + "------param2：" + param2);
		closeLoading();
		if (RESULE_SUCESS == param1) {
			if (0 == mISPSwitch) {
				mISPSwitch = 1;
				mISPBtn.setBackgroundResource(R.drawable.set_open_btn);
			} else {
				mISPSwitch = 0;
				mISPBtn.setBackgroundResource(R.drawable.set_close_btn);
			}
		} else {
			mISPSwitch = 0;
			mISPBtn.setBackgroundResource(R.drawable.set_close_btn);
		}
	}
	
	private void getPhotoQualityCallback(int event, int msg, int param1, Object param2) {
		GolukDebugUtils.e("", "----IPCManage_CallBack------new----------event:" + event + " msg:" + msg
				+ "==data:" + (String) param2+"---param1:"+param1);
	}

	private void setPhotoQualityCallback(int event, int msg, int param1, Object param2) {
		GolukDebugUtils.e("", "----IPCManage_CallBack------new----------event:" + event + " msg:" + msg
				+ "==data:" + (String) param2+"---param1:"+param1);
	}

	private void getFunctionCallback(int event, int msg, int param1, Object param2) {
		GolukDebugUtils.e("", "----IPCManage_CallBack------new----------event:" + event + " msg:" + msg
				+ "==data:" + (String) param2+"---param1:"+param1);
		if (RESULE_SUCESS == param1) {
			parseJson((String) param2);
		}
	}

	private void setFunctionCallback(int event, int msg, int param1, Object param2) {
		GolukDebugUtils.e("", "----IPCManage_CallBack------new----------event:" + event + " msg:" + msg
				+ "==data:" + (String) param2+"---param1:"+param1);
		if (RESULE_SUCESS == param1) {
			if (1 == dormant) {
				mParkingSleepBtn.setBackgroundResource(R.drawable.set_open_btn);
			} else {
				mParkingSleepBtn.setBackgroundResource(R.drawable.set_close_btn);
			}
			
			if (1 == driveFatigue) {
				mFatigueBtn.setBackgroundResource(R.drawable.set_open_btn);
			} else {
				mFatigueBtn.setBackgroundResource(R.drawable.set_close_btn);
			}

			if (1 == autoRotation) {
				mImageFlipBtn.setBackgroundResource(R.drawable.set_open_btn);
			} else {
				mImageFlipBtn.setBackgroundResource(R.drawable.set_close_btn);
			}
		} else {
			GolukUtils.showToast(this, "设置功能设置失败");
		}
	}

	private void getKitConfigCallback(int event, int msg, int param1, Object param2) {
		GolukDebugUtils.e("", "----IPCManage_CallBack------new----------event:" + event + " msg:" + msg
				+ "==data:" + (String) param2+"---param1:"+param1);
	}

	private void setKitConfigCallback(int event, int msg, int param1, Object param2) {
		GolukDebugUtils.e("", "----IPCManage_CallBack------new----------event:" + event + " msg:" + msg
				+ "==data:" + (String) param2+"---param1:"+param1);
	}
	
	int recbySec = 0;
	int moveMonitor = 0;
	// 停车休眠
	int dormant = 0;
	int recLight = 0;
	int wifiLight = 0;
	int securityLight = 0;
	// 图像翻转
	int autoRotation = 0;
	// 疲劳驾驶
	int driveFatigue = 0;

	private void parseJson(String str) {
		try {
			JSONObject obj = new JSONObject(str);
			recbySec = obj.optInt("RecbySec");
			moveMonitor = obj.optInt("MoveMonitor");
			dormant = obj.optInt("Dormant");
			recLight = obj.optInt("RecLight");
			wifiLight = obj.optInt("WifiLight");

			securityLight = obj.optInt("SecurityLight");
			autoRotation = obj.optInt("AutoRotation");
			driveFatigue = obj.optInt("DriveFatigue");

			if (1 == dormant) {
				mParkingSleepBtn.setBackgroundResource(R.drawable.set_open_btn);
			} else {
				mParkingSleepBtn.setBackgroundResource(R.drawable.set_close_btn);
			}

			if (1 == driveFatigue) {
				mFatigueBtn.setBackgroundResource(R.drawable.set_open_btn);
			} else {
				mFatigueBtn.setBackgroundResource(R.drawable.set_close_btn);
			}

			if (1 == autoRotation) {
				mImageFlipBtn.setBackgroundResource(R.drawable.set_open_btn);
			} else {
				mImageFlipBtn.setBackgroundResource(R.drawable.set_close_btn);
			}
		} catch (Exception e) {

		}
	}

	private String getSetJson() {
		try {
			JSONObject obj = new JSONObject();
			obj.put("RecbySec", recbySec);
			obj.put("MoveMonitor", moveMonitor);
			obj.put("Dormant", dormant);
			obj.put("RecLight", recLight);
			obj.put("WifiLight", wifiLight);
			obj.put("SecurityLight", securityLight);
			obj.put("AutoRotation", autoRotation);
			obj.put("DriveFatigue", driveFatigue);

			return obj.toString();

		} catch (Exception e) {

		}
		return "";
	}
	
	int quality = 0;
	String resolution = "";
	
	private void parsePhotoQualityJson(String str){
		try {
			JSONObject obj = new JSONObject(str);
			obj.optInt("quality");
			obj.optString("resolution");
			if("1080P".equals(resolution)) {
				
			} else if ("720P".equals(resolution)) {
				
			} else if ("480P".equals(resolution)) {
				
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	private void setPhotoQualityJson(){
		
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
