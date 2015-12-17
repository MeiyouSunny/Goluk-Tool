package cn.com.mobnote.golukmobile.carrecorder.settings;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.BaseActivity;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.UserOpenUrlActivity;
import cn.com.mobnote.golukmobile.carrecorder.IPCControlManager;
import cn.com.mobnote.golukmobile.carrecorder.IpcDataParser;
import cn.com.mobnote.golukmobile.carrecorder.entity.RecordStorgeState;
import cn.com.mobnote.golukmobile.carrecorder.entity.VideoConfigState;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomDialog;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomDialog.OnLeftClickListener;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomLoadingDialog;
import cn.com.mobnote.module.ipcmanager.IPCManagerFn;
import cn.com.mobnote.util.GolukFastJsonUtil;
import cn.com.mobnote.util.GolukUtils;
import cn.com.mobnote.util.JsonUtil;
import cn.com.mobnote.util.SharedPrefUtil;
import cn.com.tiros.debug.GolukDebugUtils;

/**
 * 
 * IPC设置界面
 *
 * 2015年4月6日
 *
 * @author xuhw
 */
public class SettingsActivity extends BaseActivity implements OnClickListener, IPCManagerFn {
	private final int STATE_CLOSE = 0;
	private final int STATE_OPEN = 1;

	public static final int RESULT_CODE_PHOTO = 20;
	public static final int RESULT_CODE_KIT = 30;

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
	/** HDR模式line **/
	private RelativeLayout mISPLayout, mWonderfulLayout;
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
	/** 照片质量line **/
	private RelativeLayout mPhotoQualityLayout;

	private TextView mPhotoQualityText = null;
	/** 疲劳驾驶 **/
	private RelativeLayout mFatigueLayout;
	private Button mFatigueBtn = null;
	/** 图像自动翻转 **/
	private RelativeLayout mImageFlipLayout;
	private Button mImageFlipBtn = null;
	/** 停车休眠模式 **/
	private RelativeLayout mParkingSleepLayout;
	private Button mParkingSleepBtn = null;
	/** 遥控器按键功能 **/
	private RelativeLayout mHandsetLayout;
	private TextView mHandsetText = null;

	/** 停车休眠模式提示文字 **/
	private TextView mParkingSleepHintText = null;
	/** 停车安防模式提示文字 **/
	private TextView mParkingSecurityHintText = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.carrecorder_settings);
		// 固件版本号
		ipcVersion = SharedPrefUtil.getIPCVersion();
		GolukDebugUtils.e("", "=========ipcVersion：" + ipcVersion);

		loadRes();

		mIPCName = GolukApplication.getInstance().mIPCControlManager.mProduceName;
		GolukDebugUtils.e("", "=========mIPCName：" + mIPCName);
		initView();
		setListener();
		mKitShowUI = getResources().getStringArray(R.array.kit_setting_ui);
		mArrayText = getResources().getStringArray(R.array.list_quality_ui);
		mResolutionArray = SettingsUtil.returnResolution(this, mIPCName);
		mBitrateArray = SettingsUtil.returnBitrate(this, mIPCName);

		mCustomProgressDialog = new CustomLoadingDialog(this, null);
		if (null != GolukApplication.getInstance().getIPCControlManager()) {
			GolukApplication.getInstance().getIPCControlManager().addIPCManagerListener("settings", this);
		}
		firstRequest();
	}

	// 刚进入界面请求
	private void firstRequest() {
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

		// 获取疲劳驾驶、G1图像自动翻转、停车休眠模式
		boolean getFunctionMode = GolukApplication.getInstance().getIPCControlManager().getFunctionMode();
		GolukDebugUtils.e("", "--------------SettingsActivity-----getFunctionMode：" + getFunctionMode);
		// 获取遥控器按键功能false
		boolean getKitMode = GolukApplication.getInstance().getIPCControlManager().getKitMode();
		GolukDebugUtils.e("", "--------------SettingsActivity-----getKitMode：" + getKitMode);

		if (IPCControlManager.T1_SIGN.equals(GolukApplication.getInstance().getIPCControlManager().mProduceName)) {
			boolean t1VoiceState = GolukApplication.getInstance().getIPCControlManager().getAudioCfg_T1();
			// 照片质量false
			boolean getPhotoQualityMode = GolukApplication.getInstance().getIPCControlManager().getPhotoQualityMode();
			GolukDebugUtils.e("", "--------------SettingsActivity-----getPhotoQualityMode：" + getPhotoQualityMode);
			GolukDebugUtils.e("", "--------------SettingsActivity-----t1VoiceState：" + t1VoiceState);
		}
		// 获取T1图像自动翻转
		if (IPCControlManager.T1_SIGN.equals(GolukApplication.getInstance().getIPCControlManager().mProduceName)) {
			boolean t1GetAutoRotaing = GolukApplication.getInstance().getIPCControlManager().getT1AutoRotaing();
			GolukDebugUtils.e("", "--------------SettingsActivity-----t1GetAutoRotaing：" + t1GetAutoRotaing);
		}

		showLoading();
	}

	private void checkGetState() {
		if (getRecordState && getMotionCfg) {
			closeLoading();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		GolukDebugUtils.e("", "SettingsActivity----onActivityResult----requestCode :" + requestCode + "   resultCode:"
				+ resultCode);
		if (10 == requestCode) {
			if (RESULT_CODE_PHOTO == resultCode) {
				if (null != data) {
					String photoselect = data.getStringExtra("photoselect");
					mPhtoBean.quality = photoselect;
					mCurrentResolution = photoselect;
					GolukDebugUtils.e("", "SettingsActivity----onActivityResult----photo------mCurrentResolution :" + mCurrentResolution);
					refreshPhotoQuality();
					String requestS = GolukFastJsonUtil.setParseObj(mPhtoBean);
					GolukDebugUtils.e("", "SettingsActivity----onActivityResult----photo------requestS :" + requestS);
					GolukApplication.getInstance().getIPCControlManager().setPhotoQualityMode(requestS);
				}
			} else if (RESULT_CODE_KIT == resultCode) {
				if (null != data) {
					record = data.getIntExtra("record", 1);
					snapshot = data.getIntExtra("snapshot", 0);
					refreshKitUi();
					GolukApplication.getInstance().getIPCControlManager().setKitMode(setKitJson());
				}
			}
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
		mFatigueLayout = (RelativeLayout) findViewById(R.id.fatigue_line);
		mFatigueBtn = (Button) findViewById(R.id.btn_settings_fatigue);
		mImageFlipLayout = (RelativeLayout) findViewById(R.id.image_flip_line);
		mImageFlipBtn = (Button) findViewById(R.id.btn_settings_image_flip);
		mParkingSleepLayout = (RelativeLayout) findViewById(R.id.parking_sleep_line);
		mParkingSleepBtn = (Button) findViewById(R.id.btn_settings_parking_sleep);
		mHandsetLayout = (RelativeLayout) findViewById(R.id.handset_line);
		mParkingSleepHintText = (TextView) findViewById(R.id.tv_settings_parking_sleep_hint_text);
		mParkingSecurityHintText = (TextView) findViewById(R.id.tv_settings_security_hint_text);
		mPhotoQualityText = (TextView) findViewById(R.id.tv_settings_photographic_quality);
		mHandsetText = (TextView) findViewById(R.id.tv_settings_handset);
		// ipc设备型号
		if (mIPCName.equals(IPCControlManager.G1_SIGN)) {
			mISPLayout.setVisibility(View.GONE);
			mPhotoQualityLayout.setVisibility(View.GONE);
			mHandsetLayout.setVisibility(View.GONE);
			mFatigueLayout.setVisibility(View.VISIBLE);
			mImageFlipLayout.setVisibility(View.VISIBLE);
			mParkingSleepLayout.setVisibility(View.VISIBLE);
			mParkingSleepHintText.setText(this.getResources().getString(R.string.str_settings_sleep_hint_text_g1));
			mParkingSecurityHintText
					.setText(this.getResources().getString(R.string.str_settings_security_hint_text_g1));
		} else if (mIPCName.equals("G2")) {
			mISPLayout.setVisibility(View.VISIBLE);
			mPhotoQualityLayout.setVisibility(View.GONE);
			mFatigueLayout.setVisibility(View.GONE);
			mImageFlipLayout.setVisibility(View.GONE);
			mParkingSleepLayout.setVisibility(View.GONE);
			mHandsetLayout.setVisibility(View.GONE);
			mParkingSleepHintText.setVisibility(View.GONE);
			mParkingSecurityHintText
					.setText(this.getResources().getString(R.string.str_settings_security_hint_text_g2));
		} else {
			mISPLayout.setVisibility(View.VISIBLE);
			mPhotoQualityLayout.setVisibility(View.VISIBLE);
			mFatigueLayout.setVisibility(View.VISIBLE);
			mImageFlipLayout.setVisibility(View.VISIBLE);
			mParkingSleepLayout.setVisibility(View.VISIBLE);
			mHandsetLayout.setVisibility(View.VISIBLE);
			mParkingSleepHintText.setText(this.getResources().getString(R.string.str_settings_sleep_hint_text_t1));
			mParkingSecurityHintText
					.setText(this.getResources().getString(R.string.str_settings_security_hint_text_g2));
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

		mPhotoQualityLayout.setOnClickListener(this);

		mFatigueBtn.setOnClickListener(this);// 疲劳驾驶
		mImageFlipBtn.setOnClickListener(this);// 图像自动翻转
		mParkingSleepBtn.setOnClickListener(this);// 停车休眠模式
		mHandsetLayout.setOnClickListener(this);// 遥控器按键功能
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
		d.setMessage(this.getResources().getString(R.string.str_ipc_dialog_normal));
		d.setLeftButton(this.getResources().getString(R.string.str_button_ok), new OnLeftClickListener() {
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
				click_videQuality();
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
				click_SoundRecord();
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
				click_reset();
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
				click_hdr();
				break;
			case R.id.kgjtsy:// 开关机提示音
				click_opencloseVoice();
				break;
			// 精彩视频拍摄提示音
			case R.id.jcsp:
				click_wonderfulVoice();
				break;
			// 疲劳驾驶
			case R.id.btn_settings_fatigue:
				click_Fatigue();
				break;
			case R.id.btn_settings_image_flip:
				// 图像自动翻转
				click_imageFlip();
				break;
			case R.id.btn_settings_parking_sleep:
				// 停车休眠
				click_parkingSleep();
				break;
			case R.id.handset_line:
				// 遥控器按键功能
				click_handset();
				break;
			case R.id.photographic_quality_line:
				// 点击图片质量
				click_photoQuality();
				break;
			default:
				break;
			}
		} else {
			dialog();
		}
	}

	/**
	 * 精彩视频拍摄提示音
	 * 
	 * @author jyf
	 */
	private void click_wonderfulVoice() {
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
			GolukUtils.showToast(this, getResources().getString(R.string.str_carrecoder_setting_failed));
		}
	}

	/**
	 * 开关机提示音
	 * 
	 * @author jyf
	 */
	private void click_opencloseVoice() {
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
			GolukUtils.showToast(this, getResources().getString(R.string.str_carrecoder_setting_failed));
		}
	}

	/**
	 * HDR模式
	 * 
	 * @author jyf
	 */
	private void click_hdr() {
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
			GolukUtils.showToast(this, getResources().getString(R.string.str_carrecoder_setting_failed));
		}
	}

	/**
	 * 恢复出厂设置
	 * 
	 * @author jyf
	 */
	private void click_reset() {
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
	}

	/**
	 * 视频质量界面
	 * 
	 * @author jyf
	 */
	private void click_videQuality() {
		Intent mVideoDefinition = new Intent(SettingsActivity.this, VideoQualityActivity.class);
		startActivity(mVideoDefinition);
	}

	/**
	 * 疲劳驾驶
	 * 
	 * @author jyf
	 */
	private void click_Fatigue() {
		if (driveFatigue == 1) {
			driveFatigue = 0;
		} else {
			driveFatigue = 1;
		}

		boolean fatigue = GolukApplication.getInstance().getIPCControlManager().setFunctionMode(getSetJson());
		if (!fatigue) {
			GolukUtils.showToast(this, getResources().getString(R.string.str_carrecoder_setting_failed));
		}
	}

	/**
	 * 图像自动翻转
	 * 
	 * @author jyf
	 */
	private void click_imageFlip() {
		if (IPCControlManager.T1_SIGN.equals(GolukApplication.getInstance().getIPCControlManager().mProduceName)) {
			if (1 == t1AutpRotaingEnable) {
				t1AutpRotaingEnable = 0;
			} else {
				t1AutpRotaingEnable = 1;
			}
			boolean t1Auto = GolukApplication.getInstance().getIPCControlManager()
					.setT1AutoRotaing(setT1AutoRotaingJson());
			GolukDebugUtils.e("", "--------------click_imageflip------t1Atuo:" + t1Auto);
			if (!t1Auto) {
				GolukUtils.showToast(this, getResources().getString(R.string.str_carrecoder_setting_failed));
			}
		} else {
			if (autoRotation == 1) {
				autoRotation = 0;
			} else {
				autoRotation = 1;
			}
			boolean imageFlip = GolukApplication.getInstance().getIPCControlManager().setFunctionMode(getSetJson());
			if (!imageFlip) {
				GolukUtils.showToast(this, getResources().getString(R.string.str_carrecoder_setting_failed));
			}
		}
	}

	/**
	 * 停车休眠
	 * 
	 * @author jyf
	 */
	private void click_parkingSleep() {
		if (dormant == 1) {
			dormant = 0;
		} else {
			dormant = 1;
		}
		boolean parkingSleep = GolukApplication.getInstance().getIPCControlManager().setFunctionMode(getSetJson());
		if (!parkingSleep) {
			GolukUtils.showToast(this, getResources().getString(R.string.str_carrecoder_setting_failed));
		}
	}

	/** UI显示 **/
	private String[] mKitShowUI = null;

	private void refreshKitUi() {
		try {
			if (1 == record && 1 == snapshot) {
				mHandsetText.setText(mKitShowUI[0]);
			} else if (1 == record && 0 == snapshot) {
				mHandsetText.setText(mKitShowUI[1]);
			}
		} catch (Exception e) {

		}

	}

	/**
	 * 遥控器按键功能
	 * 
	 * @author jyf
	 */
	private void click_handset() {
		Intent intent = new Intent(this, CarrecoderKitSettingActivity.class);
		intent.putExtra("record", record);
		intent.putExtra("snapshot", snapshot);
		this.startActivityForResult(intent, 10);
	}

	// 点击照片质量
	private void click_photoQuality() {
		Intent intent = new Intent(this, PhotoQualityActivity.class);
		intent.putExtra("photoselect", mCurrentResolution);
		this.startActivityForResult(intent, 10);
	}

	/** T1设备的 声音录制 开关 */
	private int state_soundRecord_T1 = 0;

	private void click_SoundRecord() {
		if (IPCControlManager.T1_SIGN.equals(GolukApplication.getInstance().getIPCControlManager().mProduceName)) {
			state_soundRecord_T1 = state_soundRecord_T1 == 0 ? 1 : 0;
			boolean isSuccess = GolukApplication.getInstance().getIPCControlManager()
					.setAudioCfg_T1(state_soundRecord_T1);
			if (isSuccess) {
				showLoading();
			} else {
				GolukUtils.showToast(this, getResources().getString(R.string.str_carrecoder_setting_failed));
			}
		} else {
			if (null != mVideoConfigState) {
				if (1 == mVideoConfigState.AudioEnabled) {
					mVideoConfigState.AudioEnabled = 0;
				} else {
					mVideoConfigState.AudioEnabled = 1;
				}
				boolean a = GolukApplication.getInstance().getIPCControlManager().setAudioCfg(mVideoConfigState);
				if (a) {
					showLoading();
				} else {
					GolukUtils.showToast(this, getResources().getString(R.string.str_carrecoder_setting_failed));
				}
			}
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		GolukApplication.getInstance().setContext(this, "carrecordsettings");
		mVideoConfigState = GolukApplication.getInstance().getVideoConfigState();
		if (null != mVideoConfigState) {
			refreshUI_soundRecod(mVideoConfigState.AudioEnabled);
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
		GolukDebugUtils.e("jyf", "YYYYYYY----IPCManage_CallBack-----------event:" + event + " msg:" + msg + "  param1:"
				+ param1 + "==data:" + (String) param2);
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
				IPCCallBackGetPhotoQuality(event, msg, param1, param2);
			} else if (msg == IPC_VDCP_Msg_SetPicCfg) {// 设置照片质量
				IPCCallBackSetPhotoQuality(event, msg, param1, param2);
			} else if (msg == IPC_VDCP_Msg_GetFunctionSwitch) {// 获取疲劳驾驶、图像自动翻转、停车休眠模式
				getFunctionCallback(event, msg, param1, param2);
			} else if (msg == IPC_VDCP_Msg_SetFunctionSwitch) {// 设置疲劳驾驶、图像自动翻转、停车休眠模式
				setFunctionCallback(event, msg, param1, param2);
			} else if (msg == IPC_VDCP_Msg_GetKitCfg) {// 获取遥控器按键功能
				getKitConfigCallback(event, msg, param1, param2);
			} else if (msg == IPC_VDCP_Msg_SetKitCfg) {// 设置遥控器按键功能
				setKitConfigCallback(event, msg, param1, param2);
			} else if (IPC_VDCP_Msg_GetRecAudioCfg == msg) {
				IPCCallBack_getRecAudioCfg(msg, param1, param2);
			} else if (IPC_VDCP_Msg_SetRecAudioCfg == msg) {
				IPCCallBack_setRecAudioCfg(msg, param1, param2);
			} else if (msg == IPC_VDCP_Msg_GetAutoRotationCfg) {// 获取T1图像自动翻转
				getT1AutoRotaingCallback(msg, param1, param2);
			} else if (msg == IPC_VDCP_Msg_SetAutoRotationCfg) {// 设置T1图像自动翻转
				setT1AutoRotaingCallback(msg, param1, param2);
			}
		}
	}

	private void IPCCallBack_setRecAudioCfg(int msg, int param1, Object param2) {
		closeLoading();
		// 设置完成后，不更新，等待查询成功后更新
		if (RESULE_SUCESS != param1) {
			GolukUtils.showToast(this, "设置失败");
		}
	}

	private void IPCCallBack_getRecAudioCfg(int msg, int param1, Object param2) {
		closeLoading();
		if (RESULE_SUCESS == param1) {
			try {
				JSONObject obj = new JSONObject((String) param2);
				state_soundRecord_T1 = Integer.parseInt(obj.optString("AudioEnable"));
				if (STATE_CLOSE != state_soundRecord_T1) {
					state_soundRecord_T1 = STATE_OPEN;
				}
				refreshUI_soundRecod(state_soundRecord_T1);
			} catch (Exception e) {

			}
		}
	}

	int t1AutpRotaingEnable = 0;

	private void getT1AutoRotaingCallback(int msg, int param1, Object param2) {
		if (RESULE_SUCESS == param1) {
			try {
				JSONObject obj = new JSONObject((String) param2);
				GolukDebugUtils.e("", "------------getT1AutoRotaingCallback------param2:" + (String) param2);
				t1AutpRotaingEnable = obj.optInt("enable");
				if (1 == t1AutpRotaingEnable) {
					mImageFlipBtn.setBackgroundResource(R.drawable.set_open_btn);
				} else {
					mImageFlipBtn.setBackgroundResource(R.drawable.set_close_btn);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	private void setT1AutoRotaingCallback(int msg, int param1, Object param2) {
		if (RESULE_SUCESS == param1) {
			GolukDebugUtils.e("", "------------setT1AutoRotaingCallback------param2:" + (String) param2);
			if (1 == t1AutpRotaingEnable) {
				mImageFlipBtn.setBackgroundResource(R.drawable.set_open_btn);
			} else {
				mImageFlipBtn.setBackgroundResource(R.drawable.set_close_btn);
			}

		} else {
			GolukUtils.showToast(this, getResources().getString(R.string.str_carrecoder_setting_failed));
		}
	}

	private String setT1AutoRotaingJson() {
		try {
			JSONObject obj = new JSONObject();
			obj.put("enable", t1AutpRotaingEnable);
			return obj.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	private void refreshUI_soundRecod(int state) {
		switch (state) {
		case STATE_CLOSE:
			mAudioBtn.setBackgroundResource(R.drawable.set_close_btn);
			break;
		case STATE_OPEN:
			mAudioBtn.setBackgroundResource(R.drawable.set_open_btn);
			break;
		default:
			mAudioBtn.setBackgroundResource(R.drawable.set_close_btn);
			break;
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

	private void IPCCallBackGetPhotoQuality(int event, int msg, int param1, Object param2) {
		GolukDebugUtils.e("", "----IPCManage_CallBack------new----------event:" + event + " msg:" + msg + "==data:"
				+ (String) param2 + "---param1:" + param1);
		this.closeLoading();
		if (RESULE_SUCESS == param1) {
			parsePhotoQualityJson((String) param2);
		}
	}

	private void IPCCallBackSetPhotoQuality(int event, int msg, int param1, Object param2) {
		GolukDebugUtils.e("", "----IPCManage_CallBack------new----------event:" + event + " msg:" + msg + "==data:"
				+ (String) param2 + "---param1:" + param1);
		if (RESULE_SUCESS == param1) {
			GolukApplication.getInstance().getIPCControlManager().getPhotoQualityMode();
		}
	}

	private void getFunctionCallback(int event, int msg, int param1, Object param2) {
		GolukDebugUtils.e("", "----IPCManage_CallBack------new----------event:" + event + " msg:" + msg + "==data:"
				+ (String) param2 + "---param1:" + param1);
		if (RESULE_SUCESS == param1) {
			parseJson((String) param2);
		}
	}

	private void setFunctionCallback(int event, int msg, int param1, Object param2) {
		GolukDebugUtils.e("", "----IPCManage_CallBack------new----------event:" + event + " msg:" + msg + "==data:"
				+ (String) param2 + "---param1:" + param1);
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

			if (!IPCControlManager.T1_SIGN.equals(GolukApplication.getInstance().getIPCControlManager().mProduceName)) {
				if (1 == autoRotation) {
					mImageFlipBtn.setBackgroundResource(R.drawable.set_open_btn);
				} else {
					mImageFlipBtn.setBackgroundResource(R.drawable.set_close_btn);
				}
			}
		} else {
			GolukUtils.showToast(this, "设置功能设置失败");
		}
	}

	private void getKitConfigCallback(int event, int msg, int param1, Object param2) {
		GolukDebugUtils.e("", "----IPCManage_CallBack------new----------event:" + event + " msg:" + msg + "==data:"
				+ (String) param2 + "---param1:" + param1);
		parseKitJson((String) param2);
		refreshKitUi();
	}

	private void setKitConfigCallback(int event, int msg, int param1, Object param2) {
		GolukDebugUtils.e("", "----IPCManage_CallBack------new----------event:" + event + " msg:" + msg + "==data:"
				+ (String) param2 + "---param1:" + param1);

		if (RESULE_SUCESS == param1) {
			GolukApplication.getInstance().getIPCControlManager().getKitMode();
		} else {
			GolukUtils.showToast(this, "设置功能设置失败");
		}
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

			if (!IPCControlManager.T1_SIGN.equals(GolukApplication.getInstance().getIPCControlManager().mProduceName)) {
				if (1 == autoRotation) {
					mImageFlipBtn.setBackgroundResource(R.drawable.set_open_btn);
				} else {
					mImageFlipBtn.setBackgroundResource(R.drawable.set_close_btn);
				}
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

	int interval = 0;
	/** 照片质量实体类 */
	private IPCSettingPhotoBean mPhtoBean = null;
	private String mCurrentResolution = "";
	/** 图片质量相关 */
	private String[] mPhotoText = null;
	private String[] mPhotoValue = null;

	private void loadRes() {
		mPhotoText = getResources().getStringArray(R.array.list_photo_quality_ui);
		mPhotoValue = getResources().getStringArray(R.array.list_photo_quality_list);
	}

	private void parsePhotoQualityJson(String str) {
		mPhtoBean = GolukFastJsonUtil.getParseObj(str, IPCSettingPhotoBean.class);
		if (null == mPhtoBean) {
			return;
		}
		refreshPhotoQuality();
	}

	private void refreshPhotoQuality() {
		final int length = mPhotoText.length;
		for (int i = 0; i < length; i++) {
			if (mPhtoBean.quality.equals(mPhotoValue[i])) {
				mPhotoQualityText.setText(mPhotoText[i]);
				mCurrentResolution = mPhtoBean.quality;
				break;
			}
		}
	}

	int record = 0;
	int snapshot = 0;
	int wifi = 0;
	int long_shut = 0;

	private void parseKitJson(String str) {
		try {
			JSONObject obj = new JSONObject(str);
			record = obj.optInt("record");
			snapshot = obj.optInt("snapshot");
			if (0 == record) {
				record = 1;
			}
			wifi = obj.optInt("wifi");
			long_shut = obj.optInt("long_shut");
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private String setKitJson() {
		try {
			JSONObject obj = new JSONObject();
			obj.put("record", record);
			obj.put("snapshot", snapshot);
			obj.put("wifi", wifi);
			obj.put("long_shut", long_shut);
			return obj.toString();
		} catch (Exception e) {

		}
		return "";
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
