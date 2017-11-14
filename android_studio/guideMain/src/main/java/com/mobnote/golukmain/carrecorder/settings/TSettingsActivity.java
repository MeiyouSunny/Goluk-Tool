package com.mobnote.golukmain.carrecorder.settings;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.mobnote.module.ipcmanager.IPCManagerFn;
import cn.com.tiros.debug.GolukDebugUtils;

import com.alibaba.fastjson.JSON;
import com.mobnote.application.GolukApplication;
import com.mobnote.eventbus.EventAdasConfigStatus;
import com.mobnote.eventbus.EventBindFinish;
import com.mobnote.eventbus.EventConfig;
import com.mobnote.golukmain.BaseActivity;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.UserOpenUrlActivity;
import com.mobnote.golukmain.adas.AdasConfigActivity;
import com.mobnote.golukmain.adas.AdasConfigParamterBean;
import com.mobnote.golukmain.adas.AdasGuideActivity;
import com.mobnote.golukmain.adas.AdasVerificationActivity;
import com.mobnote.golukmain.carrecorder.IPCControlManager;
import com.mobnote.golukmain.carrecorder.IpcDataParser;
import com.mobnote.golukmain.carrecorder.entity.RecordStorgeState;
import com.mobnote.golukmain.carrecorder.entity.VideoConfigState;
import com.mobnote.golukmain.carrecorder.settings.bean.TSettingsJson;
import com.mobnote.golukmain.carrecorder.settings.bean.VideoLogoJson;
import com.mobnote.golukmain.carrecorder.settings.bean.WonderfulVideoDisplay;
import com.mobnote.golukmain.carrecorder.settings.bean.WonderfulVideoJson;
import com.mobnote.golukmain.carrecorder.view.CustomDialog;
import com.mobnote.golukmain.carrecorder.view.CustomLoadingDialog;
import com.mobnote.golukmain.carrecorder.view.CustomDialog.OnLeftClickListener;
import com.mobnote.golukmain.carrecorder.view.CustomDialog.OnRightClickListener;
import com.mobnote.golukmain.carrecorder.view.CustomLoadingDialog.ForbidBack;
import com.mobnote.golukmain.wifidatacenter.WifiBindDataCenter;
import com.mobnote.golukmain.wifidatacenter.WifiBindHistoryBean;
import com.mobnote.util.GolukFastJsonUtil;
import com.mobnote.util.GolukFileUtils;
import com.mobnote.util.GolukUtils;
import com.mobnote.util.JsonUtil;

import de.greenrobot.event.EventBus;

import static com.mobnote.golukmain.carrecorder.IPCControlManager.T1U_SIGN;
import static com.mobnote.golukmain.carrecorder.IPCControlManager.T1_SIGN;
import static com.mobnote.golukmain.carrecorder.IPCControlManager.T2U_SIGN;
import static com.mobnote.golukmain.carrecorder.IPCControlManager.T3U_SIGN;

public class TSettingsActivity extends BaseActivity implements OnClickListener,IPCManagerFn,ForbidBack {

	/** 声音录制开关 **/
	private final int STATE_CLOSE = 0;
	private final int STATE_OPEN = 1;

	private static final String TAG = "tSettings";
	private RelativeLayout mSdLayout, mRecycleQualityLayout, mAutoRecycleLayout, mWonderfulTypeLayout,
			mWonderfulQualityLayout, mVoiceRecordLayout, mAutoPhotoLayout, mVolumeLayout, mKgjtsyLayout,
			mWonderfulTakephtotLayout, mImageFlipLayout, mVideoLogoLayout, mFatigueLayout, mUrgentCrashLayout,
			mParkingsleepLayout,mAFLayout,mAdasAssistanceLayout, mMSLayout,
			mShutdownTimeLayout, mLanguageLayout, mTimeSetupLayout, mVersionLayout, mRestoreLayout;
	private TextView mSDDesc, mRecycleQualityDesc, mWonderfulTypeDesc, mWonderfulQualityDesc, mVolumeDesc,
			mUrgentCrashDesc, mShutdownTimeDesc, mLanguageDesc;
	private Button mAutoRecycleBtn, mVoiceRecordBtn, mAutophotoBtn, mKgjtsyBtn, mWonderfulTakephotoBtn, mImageFlipBtn,
			mVideoLogoBtn, mFatigueBtn, mParkingsleepBtn, mAFBtn;
	/** ADAS **/
	private Button mAdasAssistanceBtn, mForwardCloseBtn, mForwardSetupBtn, mMoveMotionBtn;
	private RelativeLayout mForwardCloseLayout, mForwardSetupLayout, mAdasConfigLayout;

	private static final String GOLUK_LOCAL_LIST_T1 = "goluk_local_list_T1";
	private static final String GOLUK_BASIC_LIST_T1 = "goluk_basic_list_T1";
	private static final String GOLUK_LOCAL_LIST_T2 = "goluk_local_list_T2";
	private static final String GOLUK_BASIC_LIST_T2 = "goluk_basic_list_T2";
	/**精彩视频质量**/
	public static final int REQUEST_CODE_WONDERFUL_VIDEO_QUALITY = 33;
	/**提示音音量大小**/
	public static final int REQUEST_CODE_TONE_VOLUMN = 34;
	/**关机时间**/
	public static final int REQUEST_CODE_SHUTDOWN_TIME = 35;
	/**语言设置**/
	public static final int REQUEST_CODE_LANGUAGE = 36;
	/**精彩视频类型**/
	public static final int REQUEST_CODE_WONDERFUL_VIDEO_TYPE = 37;
	private String[] mIPCList = null;
	private RelativeLayout[] mLayoutList = null;
	private CustomLoadingDialog mCustomProgressDialog = null;

	/** ipc设备型号 **/
	private String mIPCName = "";
	/**分辨率**/
	private String[] mResolutionArray = null;
	/**码率**/
	private String[] mBitrateArray = null;
	/**循环视频质量**/
	private String[] mArrayText = null;
	/** 精彩视频质量 **/
	private String[] mWonderfulVideo = null;
	private String[] mWonderfulVideoValue = null;
	/** 提示音音量大小List **/
	private String[] mVolumeList = null;
	private String[] mVolumeValue = null;
	/** 关机时间 **/
	private String[] mPowerTimeList = null;
	/** 语言 **/
	private String[] mVoiceTypeList = null;
	/** 音视频配置信息 */
	private VideoConfigState mVideoConfigState = null;
	/**自动循环录像**/
	private boolean getRecordState = false;
	/** 自动循环录制状态 */
	private boolean recordState = false;
	private boolean getMotionCfg = false;
	/** 精彩视频质量 **/
	private String mWonderfulVideoResolution = "";
	/**保存上次精彩视频质量**/
	private String mSaveLastResolution = "";
	/**提示摄像头重启**/
	private AlertDialog mRestartDialog = null;
	/**自动同步照片到手机相册开关状态**/
	private boolean mAutoState = true;
	/**声音录制**/
	private int mVoiceRecordState = 0;
	/**精彩视频类型**/
	private String mWonderfulVideoType = "";
	private String mCurrentWonderfulVideoType = "";
	/**提示音音量大小**/
	private String mVolume = "";
	/** 精彩视频拍摄提示音状态 0关闭 1打开 **/
	private int mWonderfulSwitchStatus = 1;
	/** 开关机提示音(true)和精彩视频拍摄提示音(false)区分 **/
	private boolean judgeSwitch = true;
	/**
	 * 国际版本固件是否支持俄语语言切换
	 */
	private boolean mSupportRussian = false;
	/** 开关机提示音 0关闭 1打开 **/
	private int speakerSwitch = 0;
	/**疲劳驾驶**/
	private int enableSecurity = 0;
	/*
	移动侦测配置
	 */
	private int enableMoveMonitor = 0;
	/**停车休眠**/
	private int snapInterval = 0;
	/**ADAS参数**/
	private AdasConfigParamterBean mAdasConfigParamter;
	private CustomDialog mCustomDialog;
	/** 关机时间 **/
	private String mPowerTime = "";
	/** 语言 **/
	private String mVoiceType = "";
	/**视频水印参数**/
	private WonderfulVideoDisplay mDisplay;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_t_settings_layout);

		mIPCName = GolukApplication.getInstance().mIPCControlManager.mProduceName;
		mAutoState = GolukFileUtils.loadBoolean(GolukFileUtils.PROMOTION_AUTO_PHOTO, true);
		loadRes();
		mCustomProgressDialog = new CustomLoadingDialog(this, null);

		if (null != GolukApplication.getInstance().getIPCControlManager()) {
			GolukApplication.getInstance().getIPCControlManager().addIPCManagerListener(TAG, this);
		}

		initView();
		setListener();
		EventBus.getDefault().register(this);
		requestIPCList();
		requestInfo();
	}

	@Override
	protected void onResume() {
		super.onResume();
		GolukApplication.getInstance().setContext(this, "tsettings");
		// 获取循环视频质量
		mVideoConfigState = GolukApplication.getInstance().getVideoConfigState();
		int t1VideoCfg = GolukApplication.getInstance().getT1VideoCfgState();
		refreshUI_soundRecod(t1VideoCfg);
		if (null != mVideoConfigState) {
			setData2UI();
		}
	}

	private void requestIPCList() {
		boolean capacityList = GolukApplication.getInstance().getIPCControlManager().getCapacityList();
		GolukDebugUtils.e("", "TSettingsActivity-----------requestIPCList-------capacityList: " + capacityList);
		if (!capacityList) {
			// 显示基础列表
		}
	}

	private void initView() {
		mSdLayout = (RelativeLayout) findViewById(R.id.ry_t_settings_sd);
		mSDDesc = (TextView) findViewById(R.id.tv_t_settings_sd_desc);
		mRecycleQualityLayout = (RelativeLayout) findViewById(R.id.ry_t_settings_recycle_video_quality);
		mRecycleQualityDesc = (TextView) findViewById(R.id.tv_t_settings_recycle_video_quality_desc);
		mAutoRecycleLayout = (RelativeLayout) findViewById(R.id.ry_t_settings_auto_recycle);
		mAutoRecycleBtn = (Button) findViewById(R.id.btn_t_settings_auto_recycle);
		mWonderfulTypeLayout = (RelativeLayout) findViewById(R.id.ry_t_settings_wonderful_video_type);
		mWonderfulTypeDesc = (TextView) findViewById(R.id.tv_t_settings_wonderfulvideo_type_desc);
		mWonderfulQualityLayout = (RelativeLayout) findViewById(R.id.ry_t_settings_wonderful_video_quality);
		mWonderfulQualityDesc = (TextView) findViewById(R.id.tv_t_settings_wonderful_video_quality_desc);
		mVoiceRecordLayout = (RelativeLayout) findViewById(R.id.ry_t_settings_voice_record);
		mVoiceRecordBtn = (Button) findViewById(R.id.btn_t_settings_voice_record);
		mAutoPhotoLayout = (RelativeLayout) findViewById(R.id.ry_t_settings_autophoto);
		mAutophotoBtn = (Button) findViewById(R.id.btn_t_settings_autophoto);
		mVolumeLayout = (RelativeLayout) findViewById(R.id.ry_t_settings_volume);
		mVolumeDesc = (TextView) findViewById(R.id.tv_t_settings_volume_desc);
		mKgjtsyLayout = (RelativeLayout) findViewById(R.id.ry_t_settings_kgjtsy);
		mKgjtsyBtn = (Button) findViewById(R.id.btn_t_settings_kgjtsy);
		mWonderfulTakephtotLayout = (RelativeLayout) findViewById(R.id.ry_t_settings_wonderful_takephoto_voice);
		mWonderfulTakephotoBtn = (Button) findViewById(R.id.btn_t_settings_wonderful_takephoto_voice);
		mImageFlipLayout = (RelativeLayout) findViewById(R.id.ry_t_settings_image_flip);
		mImageFlipBtn = (Button) findViewById(R.id.btn_t_settings_image_flip);
		mVideoLogoLayout = (RelativeLayout) findViewById(R.id.ry_t_settings_video_logo);
		mVideoLogoBtn = (Button) findViewById(R.id.btn_t_settings_video_logo);
		mFatigueLayout = (RelativeLayout) findViewById(R.id.ry_t_settings_fatigue);
		mFatigueBtn = (Button) findViewById(R.id.btn_t_settings_fatigue);
		mUrgentCrashLayout = (RelativeLayout) findViewById(R.id.ry_t_settings_urgent_crash);
		mUrgentCrashDesc = (TextView) findViewById(R.id.tv_t_settings_urgent_crash_desc);
		mParkingsleepLayout = (RelativeLayout) findViewById(R.id.ry_t_settings_parking_sleep);
		mParkingsleepBtn = (Button) findViewById(R.id.btn_t_settings_parking_sleep);
		mAFLayout = (RelativeLayout) findViewById(R.id.ry_t_settings_tcaf);
		mAFBtn = (Button) findViewById(R.id.btn_t_settings_tcaf);
		mAdasAssistanceLayout = (RelativeLayout) findViewById(R.id.ry_t_settings_adas_assistance);
		mAdasAssistanceBtn = (Button) findViewById(R.id.btn_t_settings_adas_assistance);
		mMoveMotionBtn = (Button) findViewById(R.id.btn_t_settings_adas_move_motion);
		mForwardCloseLayout = (RelativeLayout) findViewById(R.id.ry_t_settings_forward_car_close_warning);
		mForwardCloseBtn = (Button) findViewById(R.id.btn_t_settings_forward_car_close_warning);
		mForwardSetupLayout = (RelativeLayout) findViewById(R.id.ry_t_settings_forward_car_setup_hint);
		mForwardSetupBtn = (Button) findViewById(R.id.btn_t_settings_forward_car_setup_hint);
		mAdasConfigLayout = (RelativeLayout) findViewById(R.id.ry_t_settings_adas_config);
		mShutdownTimeLayout = (RelativeLayout) findViewById(R.id.ry_t_settings_shutdown);
		mShutdownTimeDesc = (TextView) findViewById(R.id.tv_t_settings_shutdown_desc);
		mLanguageLayout = (RelativeLayout) findViewById(R.id.ry_t_settings_language);
		mLanguageDesc = (TextView) findViewById(R.id.tv_t_settings_language_desc);
		mTimeSetupLayout = (RelativeLayout) findViewById(R.id.ry_t_settings_time);
		mVersionLayout = (RelativeLayout) findViewById(R.id.ry_t_settings_version);
		mRestoreLayout = (RelativeLayout) findViewById(R.id.ry_t_settings_restore);
		mMSLayout =  (RelativeLayout) findViewById(R.id.ry_t_settings_adas_move_motion);
		mLayoutList = new RelativeLayout[] { mSdLayout, mRecycleQualityLayout, mAutoRecycleLayout,
				mWonderfulTypeLayout, mWonderfulQualityLayout, mVolumeLayout, mKgjtsyLayout, mImageFlipLayout,
				mVideoLogoLayout, mUrgentCrashLayout, mParkingsleepLayout, mAdasAssistanceLayout, mShutdownTimeLayout,
				mLanguageLayout, mTimeSetupLayout, mVersionLayout, mRestoreLayout };

		if (mAutoState) {
			mAutophotoBtn.setBackgroundResource(R.drawable.set_open_btn);
		} else {
			mAutophotoBtn.setBackgroundResource(R.drawable.set_close_btn);
		}
	}

	private void setListener() {
		findViewById(R.id.ib_t_settings_back).setOnClickListener(this);
		findViewById(R.id.ry_t_settings_sd).setOnClickListener(this);
		findViewById(R.id.ry_t_settings_recycle_video_quality).setOnClickListener(this);
		findViewById(R.id.ry_t_settings_wonderful_video_type).setOnClickListener(this);
		findViewById(R.id.ry_t_settings_wonderful_video_quality).setOnClickListener(this);
		findViewById(R.id.ry_t_settings_volume).setOnClickListener(this);
		findViewById(R.id.ry_t_settings_urgent_crash).setOnClickListener(this);
		findViewById(R.id.ry_t_settings_shutdown).setOnClickListener(this);
		findViewById(R.id.ry_t_settings_language).setOnClickListener(this);
		findViewById(R.id.ry_t_settings_time).setOnClickListener(this);
		findViewById(R.id.ry_t_settings_version).setOnClickListener(this);
		findViewById(R.id.ry_t_settings_restore).setOnClickListener(this);
		findViewById(R.id.ly_t_settings_buy).setOnClickListener(this);

		mAutoRecycleBtn.setOnClickListener(this);// 自动循环录像
		mVoiceRecordBtn.setOnClickListener(this);// 声音录制
		mAutophotoBtn.setOnClickListener(this);// 自动同步照片到手机相册
		mKgjtsyBtn.setOnClickListener(this);// 开关机提示音
		mWonderfulTakephotoBtn.setOnClickListener(this);// 精彩视频拍照提示音
		mImageFlipBtn.setOnClickListener(this);// 图像自动翻转
		mVideoLogoBtn.setOnClickListener(this);//视频水印
		mFatigueBtn.setOnClickListener(this);//疲劳驾驶语音提示
		mParkingsleepBtn.setOnClickListener(this);//停车休眠
		mAFBtn.setOnClickListener(this);//停车安防
		mAdasAssistanceBtn.setOnClickListener(this);//前向距离安全预警
		mMoveMotionBtn.setOnClickListener(this);
		mForwardCloseBtn.setOnClickListener(this);//前向距离过近提示
		mForwardSetupBtn.setOnClickListener(this);//前向车辆启动提示
		mAdasConfigLayout.setOnClickListener(this);//前向距离预警配置
	}

	private void requestInfo() {
		boolean record = GolukApplication.getInstance().getIPCControlManager().getRecordState();
		if (!record) {
			getRecordState = true;
			checkGetState();
		}
		GolukDebugUtils.e("", "TSettingsActivity=========getRecordState=========" + record);
		// 获取停车安防状态
		boolean motionCfg = GolukApplication.getInstance().getIPCControlManager().getMotionCfg();
		if (!motionCfg) {
			getMotionCfg = true;
			checkGetState();
		}

		if (GolukApplication.getInstance().getIpcIsLogin()) {
			boolean flag = GolukApplication.getInstance().getIPCControlManager().queryRecordStorageStatus();
			GolukDebugUtils.e("", "TSettingsActivity======queryRecordStorageStatus=====flag=" + flag);
		}

		boolean flag = GolukApplication.getInstance().getIPCControlManager().getGSensorControlCfg();
		GolukDebugUtils.e("", "TSettingsActivity===getIPCControlManager============getGSensorControlCfg======flag="
				+ flag);

		// 获取ipc开关机提示音状态
		boolean switchFlag = GolukApplication.getInstance().getIPCControlManager().getIPCSwitchState();
		GolukDebugUtils.e("", "TSettingsActivity---------------switchFlag----------------" + switchFlag);

		// 获取疲劳驾驶、停车休眠模式
		boolean getFunctionMode = GolukApplication.getInstance().getIPCControlManager().getFunctionMode();
		GolukDebugUtils.e("", "TSettingsActivity-------------------getFunctionMode：" + getFunctionMode);

		boolean t1VoiceState = GolukApplication.getInstance().getIPCControlManager().getAudioCfg_T1();
		GolukDebugUtils.e("", "TSettingsActivity-------------------t1VoiceState：" + t1VoiceState);
		// 获取T1图像自动翻转
		boolean t1GetAutoRotaing = GolukApplication.getInstance().getIPCControlManager().getT1AutoRotaing();
		GolukDebugUtils.e("", "TSettingsActivity-------------------t1GetAutoRotaing：" + t1GetAutoRotaing);

		boolean t1GetAdasCofig = GolukApplication.getInstance().getIPCControlManager().getT1AdasConfig();
		GolukDebugUtils.e("", "TSettingsActivity-------------------t1GetAutoRotaing：" + t1GetAdasCofig);

		// 获取精彩视频质量
		boolean videoResolution = GolukApplication.getInstance().getIPCControlManager().getVideoResolution();
		GolukDebugUtils.e("", "TSettingsActivity-------------------videoResolution：" + videoResolution);
		// 获取提示音音量大小
		boolean volume = GolukApplication.getInstance().getIPCControlManager().getVolume();
		GolukDebugUtils.e("", "TSettingsActivity-------------------volume：" + volume);
		// 获取关机时间
		boolean powerOffTime = GolukApplication.getInstance().getIPCControlManager().getPowerOffTime();
		GolukDebugUtils.e("", "TSettingsActivity-------------------powerOffTime：" + powerOffTime);
		// 获取语言设置
		boolean voiceType = GolukApplication.getInstance().getIPCControlManager().getVoiceType();
		GolukDebugUtils.e("", "TSettingsActivity-------------------voiceType：" + voiceType);
		// 获取精彩视频类型
		boolean wonderfulType = GolukApplication.getInstance().getIPCControlManager().getWonderfulVideoType();
		GolukDebugUtils.e("", "TSettingsActivity-------------------wonderfulType：" + wonderfulType);
		// 获取视频水印
		boolean videoLogo = GolukApplication.getInstance().getIPCControlManager().getVideoLogo();
		GolukDebugUtils.e("", "TSettingsActivity-------------------videoLogo：" + videoLogo);
		if(IPCControlManager.T1U_SIGN.equals(mIPCName) || IPCControlManager.T2U_SIGN.equals(mIPCName)) {
			GolukApplication.getInstance().getIPCControlManager().getT1SW();
		}
//		if(mBaseApp.getIPCControlManager().isSupportMoveDection()){
//			mMSLayout.setVisibility(View.VISIBLE);
//		}else{
//			mMSLayout.setVisibility(View.GONE);
//		}
		if(!mBaseApp.isMainland()){
			GolukApplication.getInstance().getIPCControlManager().getTxLanguage();
		}
		showLoading();
	}

	@Override
	public void onClick(View view) {
		int id = view.getId();
		if (id == R.id.ib_t_settings_back) {// 返回
			exit();
		} else if (id == R.id.ly_t_settings_buy) {// 购买链接
			Intent mBugLayout = new Intent(this, UserOpenUrlActivity.class);
			mBugLayout.putExtra(UserOpenUrlActivity.FROM_TAG, "buyline");
			startActivity(mBugLayout);
		} else if (id == R.id.ry_t_settings_sd) {// 存储卡容量查询
			click_sd();
		} else if (id == R.id.ry_t_settings_recycle_video_quality) {// 循环视频质量
			click_recycleVideoQuality();
		} else if (id == R.id.btn_t_settings_auto_recycle) {// 自动循环录像
			click_autoRecycle();
		} else if (id == R.id.ry_t_settings_wonderful_video_type) {// 精彩视频类型
			click_wonderfulVideoType();
		} else if (id == R.id.ry_t_settings_wonderful_video_quality) {// 精彩视频质量
			click_wonderfulVideoQuality();
		} else if (id == R.id.btn_t_settings_voice_record) {// 声音录制
			click_SoundRecord();
		} else if (id == R.id.btn_t_settings_autophoto) {// 自动同步照片到手机相册
			click_autoPhoto();
		} else if (id == R.id.ry_t_settings_volume) {// 提示音音量大小
			click_volume();
		} else if(id == R.id.btn_t_settings_kgjtsy) {//开关机提示音
			click_kgjtsy();
		} else if(id == R.id.btn_t_settings_wonderful_takephoto_voice) {//精彩视频拍照提示音
			click_wonderfulTakePhoto();
		} else if(id == R.id.btn_t_settings_image_flip) {//T1图像自动翻转
			click_autoRoating();
		} else if(id == R.id.btn_t_settings_video_logo) {//视频水印
			click_videoLogo();
		} else if(id == R.id.btn_t_settings_fatigue) {//疲劳驾驶语音提示
			click_Fatigue();
		} else if (id == R.id.ry_t_settings_urgent_crash) {// 紧急碰撞视频感应
			click_gSensorControlCfg();
		} else if(id == R.id.btn_t_settings_parking_sleep) {//停车休眠
			click_parkingSleep();
		} else if(id == R.id.btn_t_settings_tcaf) {//停车安防
			click_tcaf();
		} else if (id == R.id.btn_t_settings_adas_assistance) {// 前向距离安全预警
			click_assistance();
		} else if(id == R.id.btn_t_settings_adas_move_motion) {//停车安防
			click_move();
		} else if (id == R.id.btn_t_settings_forward_car_close_warning) {// 前向车距过近提示
			click_carClose();
		} else if (id == R.id.btn_t_settings_forward_car_setup_hint) {// 前向车辆启动提示
			click_carSetup();
		} else if (id == R.id.ry_t_settings_adas_config) {// 前向距离预警配置
			click_adasCfg();
		} else if (id == R.id.ry_t_settings_shutdown) {// 关机时间
			click_powerOffTime();
		} else if (id == R.id.ry_t_settings_language) {// 语言设置
			click_voiceType();
		} else if (id == R.id.ry_t_settings_time) {// 时间设置
			Intent sjsz_line = new Intent(this, TimeSettingActivity.class);
			startActivity(sjsz_line);
		} else if (id == R.id.ry_t_settings_version) {// 版本信息
			Intent bbxx = new Intent(this, VersionActivity.class);
			startActivity(bbxx);
		} else if (id == R.id.ry_t_settings_restore) {// 恢复出厂
			click_reset();
		}
	}

	private void exit() {
		this.finish();
	}

	private void matchDataToRefreshUI() {
		String[] settingList = null;
		String[] layoutList = new String[] { "sd", "conf_stream", "record", "conf_event_time", "conf_event_resolution",
				"conf_volume", "conf_voice", "auto_flip", "conf_logo", "conf_gsensor", "conf_mode", "adas",
				"conf_powoff_time", "conf_voice_type", "time", "systeminfo", "restore" };

		String basicList = "";
		String localList = "";
		if(IPCControlManager.T1_SIGN.equals(mIPCName)) {
			basicList = GolukUtils.getDataFromAssets(this, GOLUK_BASIC_LIST_T1);
			localList = GolukUtils.getDataFromAssets(this, GOLUK_LOCAL_LIST_T1);
		} else {
			basicList = GolukUtils.getDataFromAssets(this, GOLUK_BASIC_LIST_T2);
			localList = GolukUtils.getDataFromAssets(this, GOLUK_LOCAL_LIST_T2);
		}
		TSettingsJson localJson = GolukFastJsonUtil.getParseObj(localList, TSettingsJson.class);
		if (null != localJson && null != localJson.data) {
			settingList = localJson.data.list;
			GolukDebugUtils.e("", "TSettingsActivity----------matchDataToRefreshUI------mSettingList1: "
					+ settingList.length);
		}
		if (null != mIPCList && 0 != mIPCList.length && null != settingList) {
			for (int k = 0; k < layoutList.length; k++) {
				for (int i = 0; i < settingList.length; i++) {
					for (int j = 0; j < mIPCList.length; j++) {
						if (settingList[i].equals(mIPCList[j]) && settingList[i].equals(layoutList[k])) {
							mLayoutList[k].setVisibility(View.VISIBLE);
						}
					}
				}
			}

		} else {
			TSettingsJson basicJson = GolukFastJsonUtil.getParseObj(basicList, TSettingsJson.class);
			if (null != basicJson && null != basicJson.data) {
				settingList = basicJson.data.list;
			}
			if (null != settingList) {
				for (int m = 0; m < layoutList.length; m++) {
					for (int n = 0; n < settingList.length; n++) {
						if (layoutList[m].equals(settingList[n])) {
							mLayoutList[m].setVisibility(View.VISIBLE);
						}
					}
				}
			}
		}
		if (mAutoRecycleLayout.getVisibility() == View.VISIBLE) {
			mVoiceRecordLayout.setVisibility(View.VISIBLE);
//			mAutoPhotoLayout.setVisibility(View.VISIBLE);
		} else {
			mVoiceRecordLayout.setVisibility(View.GONE);
			mAutoPhotoLayout.setVisibility(View.GONE);
		}
		if (mKgjtsyLayout.getVisibility() == View.VISIBLE) {
			mWonderfulTakephtotLayout.setVisibility(View.VISIBLE);
			mFatigueLayout.setVisibility(View.VISIBLE);
			findViewById(R.id.tv_t_settings_smartgo_text).setVisibility(View.VISIBLE);
		} else {
			mWonderfulTakephtotLayout.setVisibility(View.GONE);
			mFatigueLayout.setVisibility(View.GONE);
			findViewById(R.id.tv_t_settings_smartgo_text).setVisibility(View.GONE);
		}
		if (mParkingsleepLayout.getVisibility() == View.VISIBLE) {
			mAFLayout.setVisibility(View.VISIBLE);
			findViewById(R.id.tv_t_settings_parking_sleep_desc).setVisibility(View.VISIBLE);
			findViewById(R.id.tv_t_settings_security_desc).setVisibility(View.VISIBLE);
		} else {
			mAFLayout.setVisibility(View.GONE);
			findViewById(R.id.tv_t_settings_parking_sleep_desc).setVisibility(View.GONE);
			findViewById(R.id.tv_t_settings_security_desc).setVisibility(View.GONE);
		}
		//国际版购买链接https://www.amazon.com/Spy-Tec-Dash-Camera-Vehicle/dp/B00MH4ZVHO/ref=sr_1_8?ie=UTF8&qid=1478595877
		//国际版不显示语言设置
		if (mBaseApp.isMainland()
				|| GolukApplication.getInstance().mIpcVersion.toUpperCase().startsWith(T1U_SIGN)
				|| GolukApplication.getInstance().mIpcVersion.toUpperCase().startsWith(T2U_SIGN)
				|| GolukApplication.getInstance().mIpcVersion.toUpperCase().startsWith(T3U_SIGN)) {
			mLanguageLayout.setVisibility(View.VISIBLE);
		} else {
			mLanguageLayout.setVisibility(View.GONE);
		}
	}

	@Override
	public void IPCManage_CallBack(int event, int msg, int param1, Object param2) {
		GolukDebugUtils.e("", "TSettingsActivity---------------IPCManage_CallBack-------msg: " + msg
				+ "-------param1: " + param1 + "---------param2: " + param2);
		if (event == ENetTransEvent_IPC_VDCP_CommandResp) {
			if (msg == IPC_VDCP_Msg_GetCapacityList) {// 获取列表
				callback_getCapacityList(event, msg, param1, param2);
			} else if (msg == IPC_VDCP_Msg_RecPicUsage) {//存储容量查询
				callback_getSd(event, msg, param1, param2);
			} else if (msg == IPC_VDCP_Msg_GetVedioEncodeCfg) {//循环视频质量
				callback_getRecycleQuality(event, msg, param1, param2);
			} else if (msg == IPC_VDCP_Msg_SetVedioEncodeCfg) {
				callback_setRecycleQuality(event, msg, param1, param2);
			} else if (msg == IPC_VDCP_Msg_GetRecordState) {// 自动循环录像
				callback_getAutoRecycleRecord(event, msg, param1, param2);
			} else if (msg == IPC_VDCP_Msg_StartRecord) {
				callback_startAutoRecycleRecord(event, msg, param1, param2);
			} else if (msg == IPC_VDCP_Msg_StopRecord) {
				callback_stopAutoRecycleRecord(event, msg, param1, param2);
			} else if (msg == IPC_VDCP_Msg_GetVideoTimeConf) {// 精彩视频类型
				callback_getWonderfulVideoType(event, msg, param1, param2);
			} else if (msg == IPC_VDCP_Msg_SetVideoTimeConf) {
				callback_setWonderfulVideoType(event, msg, param1, param2);
			} else if (msg == IPC_VDCP_Msg_GetVideoResolution) {// 获取精彩视频质量
				callback_getVideoResolution(event, msg, param1, param2);
			} else if (msg == IPC_VDCP_Msg_SetVideoResolution) {
				callback_setVideoResolution(event, msg, param1, param2);
			}  else if (IPC_VDCP_Msg_GetRecAudioCfg == msg) {//声音录制
				callback_getVoiceRecord(event, msg, param1, param2);
			} else if (IPC_VDCP_Msg_SetRecAudioCfg == msg) {
				callback_setVoiceRecord(event, msg, param1, param2);
			} else if(msg == IPC_VDCP_Msg_GetVolume) {//提示音音量大小
				callback_getVolume(event, msg, param1, param2);
			} else if(msg == IPC_VDCP_Msg_SetVolume) {
				callback_setVolume(event, msg, param1, param2);
			} else if (msg == IPC_VDCP_Msg_GetSpeakerSwitch) {// 开关机提示音、精彩视频拍摄提示音
				callback_getSpeakerSwitch(event, msg, param1, param2);
			} else if (msg == IPC_VDCP_Msg_SetSpeakerSwitch) {
				callback_setSpeakerSwitch(event, msg, param1, param2);
			} else if (msg == IPC_VDCP_Msg_GetAutoRotationCfg) {// 获取T1图像自动翻转
				callback_getAutoRotaing(msg, param1, param2);
			} else if (msg == IPC_VDCP_Msg_SetAutoRotationCfg) {
				callback_setAutoRotaing(msg, param1, param2);
			} else if (msg == IPC_VDCP_Msg_GetOSDConf) {// 视频水印
				callback_getVideoLogo(msg, param1, param2);
			} else if (msg == IPC_VDCP_Msg_SetOSDConf) {
				callback_setVideoLogo(msg, param1, param2);
			} else if(msg == IPC_VDCP_Msg_GetFunctionSwitch) {//疲劳驾驶、停车休眠
				callback_getFunction(msg, param1, param2);
			} else if(msg == IPC_VDCP_Msg_SetFunctionSwitch) {
				callback_setFunction(msg, param1, param2);
			} else if (msg == IPC_VDCP_Msg_GetGSensorControlCfg) {//碰撞感应灵敏度
				callback_getGSensorControlCfg(msg, param1, param2);
			} else if (msg == IPC_VDCP_Msg_SetGSensorControlCfg) {
				callback_setGSensorControlCfg(msg, param1, param2);
			} else if (msg == IPC_VDCP_Msg_GetMotionCfg) {// 停车安防
				callback_getMotionCfg(msg, param1, param2);
			} else if (msg == IPC_VDCP_Msg_GetMotionSW) {
				callback_getMotionSW(msg, param1, param2);
			} else if (msg == IPC_VDCP_Msg_SetMotionSW) {
				callback_setMotionSW(msg, param1, param2);
			} else if (msg == IPC_VDCP_Msg_SetMotionCfg) {
				callback_setMotionCfg(msg, param1, param2);
			} else if (msg == IPC_VDCP_Msg_GetADASConfig) {// ADAS
				callback_getADASCfg(msg, param1, param2);
			} else if (msg == IPC_VDCP_Msg_SetADASConfig) {
				callback_setADASCfg(msg, param1, param2);
			} else if (msg == IPC_VDCP_Msg_GetPowerOffTime) {// 获取关机时间
				callback_getPowerOffTime(msg, param1, param2);
			} else if (msg == IPC_VDCP_Msg_SetPowerOffTime) {
				callback_setPowerOffTime(msg, param1, param2);
			} else if (msg == IPC_VDCP_Msg_GetVoiceTypeList) {
				callback_getSupportRussian(msg, param1, param2);
			} else if (msg == IPC_VDCP_Msg_GetVoiceType) {// 获取语言
				callback_getVoiceType(msg, param1, param2);
			} else if (msg == IPC_VDCP_Msg_SetVoiceType) {
				callback_setVoiceType(msg, param1, param2);
			}  else if (msg == IPC_VDCP_Msg_Restore) {//恢复出厂
				callback_restore(msg, param1, param2);
			} else if(msg == IPC_VDCP_Msg_Reboot) {//重启IPC
				GolukDebugUtils.e("", "TSettingsActivity-----------IPC_VDCP_Msg_Reboot-----param2: " + param2);
			}
		}
	}

	private void callback_getSupportRussian(int msg, int param1, Object param2) {
		closeLoading();
		if (RESULE_SUCESS == param1) {
			String language = (String) param2;
			if(!TextUtils.isEmpty(language)){
				mSupportRussian = true;
				return;
			}
		}
		mSupportRussian = false;
	}

	private void callback_setMotionSW(int msg, int param1, Object param2) {
		closeLoading();
		if (RESULE_SUCESS == param1) {
			if (1 == enableMoveMonitor) {
				enableMoveMonitor = 0;
				mMoveMotionBtn.setBackgroundResource(R.drawable.set_close_btn);// 关闭
			} else {
				enableMoveMonitor = 1;
				mMoveMotionBtn.setBackgroundResource(R.drawable.set_open_btn);// 打开
			}
		}
	}

	private void callback_getMotionSW(int msg, int param1, Object param2) {
		closeLoading();
		if (RESULE_SUCESS == param1) {
			try {
				JSONObject obj = new JSONObject((String) param2);
				enableMoveMonitor = obj.optInt("MoveMonitor");
				if (1 == enableMoveMonitor) {
					mMoveMotionBtn.setBackgroundResource(R.drawable.set_open_btn);// 打开
				}
				else {
					mMoveMotionBtn.setBackgroundResource(R.drawable.set_close_btn);// 关闭
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 获取全设置列表
	 * @param event
	 * @param msg
	 * @param param1
	 * @param param2
	 */
	private void callback_getCapacityList(int event, int msg, int param1, Object param2) {
		GolukDebugUtils.e("", "TSettingsActivity-----------callback_getCapacityList-----param2: " + param2);
		if (RESULE_SUCESS == param1) {
			try {
				TSettingsJson tJson = GolukFastJsonUtil.getParseObj((String) param2, TSettingsJson.class);
				if (null != tJson && null != tJson.data) {
					mIPCList = tJson.data.list;
					GolukDebugUtils.e("", "TSettingsActivity-----------callback_getCapacityList-----mIPCList: " + mIPCList.length);
				}
				matchDataToRefreshUI();
			} catch (Exception e) {
				e.printStackTrace();
				matchDataToRefreshUI();
			}
		} else {
			matchDataToRefreshUI();
		}
	}
	
	/**
	 * 存储容量查询
	 * @param event
	 * @param msg
	 * @param param1
	 * @param param2
	 */
	private void callback_getSd(int event, int msg, int param1, Object param2) {
		GolukDebugUtils.e("", "TSettingsActivity-----------callback_getSd-----param2: " + param2);
		if (RESULE_SUCESS == param1) {
			RecordStorgeState mRecordStorgeState = IpcDataParser.parseRecordStorageStatus((String) param2);
			if (null != mRecordStorgeState) {
				double usedsize = mRecordStorgeState.totalSdSize - mRecordStorgeState.leftSize;
				mSDDesc.setText(GolukUtils.getSize(usedsize) + "/" + GolukUtils.getSize(mRecordStorgeState.totalSdSize));
			}
		}
	}
	
	/**
	 * 循环视频质量
	 * @param event
	 * @param msg
	 * @param param1
	 * @param param2
	 */
	private void callback_getRecycleQuality(int event, int msg, int param1, Object param2) {
		GolukDebugUtils.e("", "TSettingsActivity-----------callback_getRecycleQuality-----param2: " + param2);
		if (RESULE_SUCESS == param1) {
			mVideoConfigState = IpcDataParser.parseVideoConfigState((String) param2);
			setData2UI();
		}
	}
	
	private void callback_setRecycleQuality(int event, int msg, int param1, Object param2) {
		GolukDebugUtils.e("", "TSettingsActivity-----------callback_setRecycleQuality-----param2: " + param2);
		
	}
	
	/**
	 * 获取自动循环录像状态
	 * @param event
	 * @param msg
	 * @param param1
	 * @param param2
	 */
	private void callback_getAutoRecycleRecord(int event, int msg, int param1, Object param2) {
		GolukDebugUtils.e("", "TSettingsActivity-----------callback_getAutoRecycleRecord-----param2: " + param2);
		if (RESULE_SUCESS == param1) {
			getRecordState = true;
			checkGetState();
			if (RESULE_SUCESS == param1) {
				recordState = IpcDataParser.getAutoRecordState((String) param2);
				if (!recordState) {
					mAutoRecycleBtn.setBackgroundResource(R.drawable.set_close_btn);
				} else {
					mAutoRecycleBtn.setBackgroundResource(R.drawable.set_open_btn);
				}
			} else {
				// 录制状态获取失败
				mAutoRecycleBtn.setBackgroundResource(R.drawable.set_close_btn);
			}
		}
	}
	
	//开启自动循环录制
	private void callback_startAutoRecycleRecord(int event, int msg, int param1, Object param2) {
		closeLoading();
		if (RESULE_SUCESS == param1) {
			recordState = true;
			mAutoRecycleBtn.setBackgroundResource(R.drawable.set_open_btn);
		}
	}
	//停止自动循环录制
	private void callback_stopAutoRecycleRecord(int event, int msg, int param1, Object param2) {
		closeLoading();
		if (RESULE_SUCESS == param1) {
			recordState = false;
			mAutoRecycleBtn.setBackgroundResource(R.drawable.set_close_btn);
		}
	}
	/**
	 * 获取精彩视频类型
	 * @param event
	 * @param msg
	 * @param param1
	 * @param param2
	 */
	private void callback_getWonderfulVideoType(int event, int msg, int param1, Object param2) {
		GolukDebugUtils.e("", "TSettingsActivity-----------callback_getWonderfulVideoType-----param2: " + param2);
		if (RESULE_SUCESS == param1) {
			WonderfulVideoJson videoJson = GolukFastJsonUtil.getParseObj((String) param2, WonderfulVideoJson.class);
			if (null != videoJson && null != videoJson.data) {
				if (videoJson.data.wonder_history_time == 6 && videoJson.data.wonder_future_time == 6) {
					// 精彩抓拍（前6后6）
					mWonderfulVideoType = videoJson.data.wonder_future_time + "";
					mWonderfulTypeDesc.setText(this.getString(R.string.str_settings_video_type1));
				} else if (videoJson.data.wonder_history_time == 0 && videoJson.data.wonder_future_time == 30) {
					// 经典模式
					mWonderfulVideoType = videoJson.data.wonder_future_time + "";
					mWonderfulTypeDesc.setText(this.getString(R.string.str_settings_video_type2));
				}
				mCurrentWonderfulVideoType = mWonderfulVideoType;
				GolukDebugUtils.e("", "TSettingsActivity------------mCurrentWonderfulVideoType: "
						+ mCurrentWonderfulVideoType+"-----------mWonderfulVideoType: "+mWonderfulVideoType);
			}
		}
	}
	//设置精彩视频类型
	private void callback_setWonderfulVideoType(int event, int msg, int param1, Object param2) {
		GolukDebugUtils.e("", "TSettingsActivity-----------callback_setWonderfulVideoType-----param2: " + param2);
		if (RESULE_SUCESS == param1) {
			try {
				JSONObject obj = new JSONObject((String) param2);
				String needReboot = obj.optString("need_reboot");
				if (needReboot.equals("true") && !mCurrentWonderfulVideoType.equals(mWonderfulVideoType)) {
					showRebootDialog();
				}
				GolukApplication.getInstance().getIPCControlManager().getWonderfulVideoType();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 获取精彩视频质量
	 * @param event
	 * @param msg
	 * @param param1
	 * @param param2
	 */
	private void callback_getVideoResolution(int event, int msg, int param1, Object param2) {
		GolukDebugUtils.e("", "TSettingsActivity-----------callback_getVideoResolution-----param2: " + param2);
		if (RESULE_SUCESS == param1) {
			try {
				JSONObject json = new JSONObject((String) param2);
				mWonderfulVideoResolution = json.getString("wonderful_resolution");
				mSaveLastResolution = mWonderfulVideoResolution;
				refreshWonderfulVideoData();
			} catch (Exception e) {
				mWonderfulVideoResolution = "1080P";
				mWonderfulQualityDesc.setText(mWonderfulVideoResolution);
			}
		}
	}
	//设置精彩视频质量
	private void callback_setVideoResolution(int event, int msg, int param1, Object param2) {
		GolukDebugUtils.e("", "TSettingsActivity-----------callback_setVideoResolution-----param2: " + param2);
		if (RESULE_SUCESS == param1) {
			try {
				JSONObject obj = new JSONObject((String) param2);
				String needReboot = obj.optString("need_reboot");
				if (needReboot.equals("true") && !mSaveLastResolution.equals(mWonderfulVideoResolution)) {
					showRebootDialog();
				}
				GolukApplication.getInstance().getIPCControlManager().getVideoResolution();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 声音录制
	 * @param event
	 * @param msg
	 * @param param1
	 * @param param2
	 */
	private void callback_getVoiceRecord(int event, int msg, int param1, Object param2) {
		GolukDebugUtils.e("", "TSettingsActivity-----------callback_getVoiceRecord-----param2: " + param2);
		closeLoading();
		if (RESULE_SUCESS == param1) {
			try {
				JSONObject obj = new JSONObject((String) param2);
				mVoiceRecordState = Integer.parseInt(obj.optString("AudioEnable"));
				if (STATE_CLOSE != mVoiceRecordState) {
					mVoiceRecordState = STATE_OPEN;
				}
				refreshUI_soundRecod(mVoiceRecordState);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private void callback_setVoiceRecord(int event, int msg, int param1, Object param2) {
		GolukDebugUtils.e("", "TSettingsActivity-----------callback_setVoiceRecord-----param2: " + param2);
		// 设置完成后，不更新，等待查询成功后更新
		closeLoading();
		if (RESULE_SUCESS != param1) {
			GolukUtils.showToast(this, this.getResources().getString(R.string.str_carrecoder_setting_failed));
		}
	}
	
	/**
	 * 提示音音量大小
	 * @param event
	 * @param msg
	 * @param param1
	 * @param param2
	 */
	private void callback_getVolume(int event, int msg, int param1, Object param2) {
		GolukDebugUtils.e("", "TSettingsActivity-----------callback_getVolume-----param2: " + param2);
		if (RESULE_SUCESS == param1) {
			try {
				JSONObject json = new JSONObject((String) param2);
				int value = json.getInt("value");
				mVolume = value + "";
				refreshVolume();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private void callback_setVolume(int event, int msg, int param1, Object param2) {
		GolukDebugUtils.e("", "TSettingsActivity-----------callback_setVolume-----param2: " + param2);
		if (RESULE_SUCESS == param1) {
			GolukApplication.getInstance().getIPCControlManager().getVolume();
		}
	}
	
	/**
	 * 开关机提示音、精彩视频拍摄提示音
	 * @param event
	 * @param msg
	 * @param param1
	 * @param param2
	 */
	private void callback_getSpeakerSwitch(int event, int msg, int param1, Object param2) {
		GolukDebugUtils.e("", "TSettingsActivity-----------callback_getSpeakerSwitch-----param2: " + param2);
		closeLoading();
		if (RESULE_SUCESS == param1) {
			try {
				JSONObject json = new JSONObject((String) param2);
				GolukDebugUtils.e("", "TSettingsActivity---------IPC_VDCPCmd_GetSpeakerSwitch--------" + json);
				// {"SpeakerSwitch":0} 0关闭 1打开
				speakerSwitch = json.optInt("SpeakerSwitch");
				mWonderfulSwitchStatus = json.optInt("WonderfulSwitch");
				if (0 == speakerSwitch) {
					mKgjtsyBtn.setBackgroundResource(R.drawable.set_close_btn);
				} else {
					mKgjtsyBtn.setBackgroundResource(R.drawable.set_open_btn);
				}
				// 精彩视频拍照提示音
				if (0 == mWonderfulSwitchStatus) {
					mWonderfulTakephotoBtn.setBackgroundResource(R.drawable.set_close_btn);
				} else {
					mWonderfulTakephotoBtn.setBackgroundResource(R.drawable.set_open_btn);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void callback_setSpeakerSwitch(int event, int msg, int param1, Object param2) {
		GolukDebugUtils.e("", "TSettingsActivity-----------callback_setSpeakerSwitch-----param2: " + param2);
		closeLoading();
		if (RESULE_SUCESS == param1) {
			if (judgeSwitch) {
				// 设置开关机提示音
				if (0 == speakerSwitch) {
					speakerSwitch = 1;
					mKgjtsyBtn.setBackgroundResource(R.drawable.set_open_btn);
				} else {
					speakerSwitch = 0;
					mKgjtsyBtn.setBackgroundResource(R.drawable.set_close_btn);
				}
			} else {
				// 精彩视频拍摄提示音
				if (0 == mWonderfulSwitchStatus) {
					mWonderfulSwitchStatus = 1;
					mWonderfulTakephotoBtn.setBackgroundResource(R.drawable.set_open_btn);
				} else {
					mWonderfulSwitchStatus = 0;
					mWonderfulTakephotoBtn.setBackgroundResource(R.drawable.set_close_btn);
				}
			}
			GolukUtils.showToast(this, this.getResources().getString(R.string.str_set_ok));
		} else {
			GolukUtils.showToast(this, this.getResources().getString(R.string.str_ipc_unmatch));
		}
	}
	
	int autoRoatingEnable = 0;
	
	/**
	 * 获取图像自动翻转状态
	 * @param msg
	 * @param param1
	 * @param param2
	 */
	private void callback_getAutoRotaing(int msg, int param1, Object param2) {
		GolukDebugUtils.e("", "TSettingsActivity-----------callback_getAutoRotaing-----param2: " + param2);
		if (RESULE_SUCESS == param1) {
			try {
				JSONObject obj = new JSONObject((String) param2);
				autoRoatingEnable = obj.optInt("enable");
				if (1 == autoRoatingEnable) {
					mImageFlipBtn.setBackgroundResource(R.drawable.set_open_btn);
				} else {
					mImageFlipBtn.setBackgroundResource(R.drawable.set_close_btn);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void callback_setAutoRotaing(int msg, int param1, Object param2) {
		GolukDebugUtils.e("", "TSettingsActivity-----------callback_setAutoRotaing-----param2: " + param2);
		closeLoading();
		if (RESULE_SUCESS == param1) {
			if (1 == autoRoatingEnable) {
				mImageFlipBtn.setBackgroundResource(R.drawable.set_open_btn);
			} else {
				mImageFlipBtn.setBackgroundResource(R.drawable.set_close_btn);
			}

		} else {
			GolukUtils.showToast(this, getResources().getString(R.string.str_carrecoder_setting_failed));
		}
	}
	
	/**
	 * 获取视频水印
	 * @param msg
	 * @param param1
	 * @param param2
	 */
	private void callback_getVideoLogo(int msg, int param1, Object param2) {
		GolukDebugUtils.e("", "TSettingsActivity-----------callback_getVideoLogo-----param2: " + param2);
		if (RESULE_SUCESS == param1) {
			VideoLogoJson videoLogo = GolukFastJsonUtil.getParseObj((String) param2, VideoLogoJson.class);
			if (null != videoLogo && null != videoLogo.data) {
				List<WonderfulVideoDisplay> displayList = videoLogo.data.list;
				if (null != displayList && displayList.size() > 0) {
					mDisplay = displayList.get(0);
					if (0 == mDisplay.logo_visible) {
						mVideoLogoBtn.setBackgroundResource(R.drawable.set_close_btn);
					} else {
						mVideoLogoBtn.setBackgroundResource(R.drawable.set_open_btn);
					}
				}
			}
		}
	}
	
	private void callback_setVideoLogo(int msg, int param1, Object param2) {
		GolukDebugUtils.e("", "TSettingsActivity-----------callback_setVideoLogo-----param2: " + param2);
		closeLoading();
		if (RESULE_SUCESS == param1) {
			if (null != mDisplay) {
				if (0 == mDisplay.logo_visible) {
					mVideoLogoBtn.setBackgroundResource(R.drawable.set_close_btn);
				} else {
					mVideoLogoBtn.setBackgroundResource(R.drawable.set_open_btn);
				}
			} else {
				GolukUtils.showToast(this, getResources().getString(R.string.str_carrecoder_setting_failed));
			}
		} else {
			GolukUtils.showToast(this, getResources().getString(R.string.str_carrecoder_setting_failed));
		}
	}
	
	/**
	 * 疲劳驾驶、停车休眠
	 * @param msg
	 * @param param1
	 * @param param2
	 */
	private void callback_getFunction(int msg, int param1, Object param2) {
		GolukDebugUtils.e("", "TSettingsActivity-----------callback_getFunction-----param2: " + param2);
		if (RESULE_SUCESS == param1) {
			parseJson((String) param2);
		}
	}
	
	private void callback_setFunction(int msg, int param1, Object param2) {
		GolukDebugUtils.e("", "TSettingsActivity-----------callback_setFunction-----param2: " + param2);
		closeLoading();
		if (RESULE_SUCESS == param1) {
			if (1 == dormant) {
				mParkingsleepBtn.setBackgroundResource(R.drawable.set_open_btn);
				if (1 == enableSecurity) {
					enableSecurity = 0;
					// TODO 设置停车安防
					boolean c = GolukApplication.getInstance().getIPCControlManager()
							.setMotionCfg(enableSecurity, snapInterval);
				}
			} else {
				mParkingsleepBtn.setBackgroundResource(R.drawable.set_close_btn);
			}
			if (1 == driveFatigue) {
				mFatigueBtn.setBackgroundResource(R.drawable.set_open_btn);
			} else {
				mFatigueBtn.setBackgroundResource(R.drawable.set_close_btn);
			}

		} else {
			GolukUtils.showToast(this, this.getResources().getString(R.string.str_carrecoder_setting_failed));
		}
	}
	
	/**
	 * 紧急碰撞感应灵敏度
	 * @param msg
	 * @param param1
	 * @param param2
	 */
	private void callback_getGSensorControlCfg(int msg, int param1, Object param2) {
		if (param1 == RESULE_SUCESS) {
			try {
				JSONObject json = new JSONObject((String) param2);
				int policy = json.optInt("policy");
				if (0 == policy) {
					mUrgentCrashDesc.setText(this.getResources().getString(R.string.carrecorder_tcaf_close));
				} else if (1 == policy) {
					mUrgentCrashDesc.setText(this.getResources().getString(R.string.str_low));
				} else if (2 == policy) {
					mUrgentCrashDesc.setText(this.getResources().getString(R.string.str_middle));
				} else {
					mUrgentCrashDesc.setText(this.getResources().getString(R.string.str_high));
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void callback_setGSensorControlCfg(int msg, int param1, Object param2) {
		if (param1 == RESULE_SUCESS) {
			GolukApplication.getInstance().getIPCControlManager().getGSensorControlCfg();
		}
	}
	
	/**
	 * 停车安防
	 * @param msg
	 * @param param1
	 * @param param2
	 */
	private void callback_getMotionCfg(int msg, int param1, Object param2) {
		getMotionCfg = true;
		checkGetState();
		if (RESULE_SUCESS == param1) {
			try {
				JSONObject json = new JSONObject((String) param2);
				if (null != json) {
					enableSecurity = json.getInt("enableSecurity");
					snapInterval = json.getInt("snapInterval");
					if (1 == enableSecurity) {
						if(mBaseApp.getIPCControlManager().isSupportMoveDection()) {
							mMSLayout.setVisibility(View.VISIBLE);
						}
						mAFBtn.setBackgroundResource(R.drawable.set_open_btn);// 打开
						if (1 == dormant) {
							dormant = 0;
							// 设置停车休眠
							boolean fatigue = GolukApplication.getInstance().getIPCControlManager()
									.setFunctionMode(getSetJson());
						}
					} else {
						mAFBtn.setBackgroundResource(R.drawable.set_close_btn);// 关闭
						mMSLayout.setVisibility(View.GONE);
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void callback_setMotionCfg(int msg, int param1, Object param2) {
		closeLoading();
		if (RESULE_SUCESS == param1) {
			if (1 == enableSecurity) {
				mAFBtn.setBackgroundResource(R.drawable.set_open_btn);// 打开
				if(mBaseApp.getIPCControlManager().isSupportMoveDection()) {
					mMSLayout.setVisibility(View.VISIBLE);
				}
				// TODO 判断休眠是否打开
				if (1 == dormant) {
					dormant = 0;
					// 设置停车休眠
					boolean fatigue = GolukApplication.getInstance().getIPCControlManager()
							.setFunctionMode(getSetJson());
				}
			} else {
				mAFBtn.setBackgroundResource(R.drawable.set_close_btn);// 关闭
				mMSLayout.setVisibility(View.GONE);
			}
		} else {
			if (1 == enableSecurity) {
				enableSecurity = 0;
			} else {
				enableSecurity = 1;
			}
		}
	}
	/**
	 * ADAS
	 * @param msg
	 * @param param1
	 * @param param2
	 */
	private void callback_getADASCfg(int msg, int param1, Object param2) {
		if (RESULE_SUCESS == param1) {
			if (!TextUtils.isEmpty((String) param2)) {
				mAdasConfigParamter = JSON.parseObject((String) param2, AdasConfigParamterBean.class);
				switchAdasEnableUI(mAdasConfigParamter.enable == 1);
			} else {
				mAdasAssistanceLayout.setVisibility(View.GONE);
			}
		}
	}
	
	private void callback_setADASCfg(int msg, int param1, Object param2) {
		if (GolukApplication.getInstance().getContext() != this) {
			return;
		}
		closeLoading();
		if (RESULE_SUCESS == param1) {
			GolukFileUtils.saveInt(GolukFileUtils.ADAS_FLAG, mAdasConfigParamter.enable);
			switchAdasEnableUI(mAdasConfigParamter.enable == 1);
		} else {
			mAdasConfigParamter.enable = (Integer) mAdasAssistanceBtn.getTag();
			mAdasConfigParamter.fcs_enable = (Integer) mForwardSetupBtn.getTag();
			mAdasConfigParamter.fcw_enable = (Integer) mForwardCloseBtn.getTag();
		}
	}
	
	public void onEventMainThread(EventAdasConfigStatus event) {
		if (event == null) {
			return;
		}

		mAdasConfigParamter = event.getData();
		switchAdasEnableUI(true);
	}
	
	/**
	 * 获取关机时间
	 * @param msg
	 * @param param1
	 * @param param2
	 */
	private void callback_getPowerOffTime(int msg, int param1, Object param2) {
		GolukDebugUtils.e("", "TSettingsActivity--------callback_getPowerOffTime------------param2：" + param2);
		if (RESULE_SUCESS == param1) {
			try {
				JSONObject json = new JSONObject((String) param2);
				int time = json.getInt("time_second");
				mPowerTime = time + "";
				refreshPowerTime();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private void callback_setPowerOffTime(int msg, int param1, Object param2) {
		GolukDebugUtils.e("", "TSettingsActivity--------callback_setPowerOffTime----------param2：" + param2);
		closeLoading();
		if (RESULE_SUCESS == param1) {
			GolukApplication.getInstance().getIPCControlManager().getPowerOffTime();
		}
	}
	
	/**
	 * 获取语言设置
	 * @param msg
	 * @param param1
	 * @param param2
	 */
	private void callback_getVoiceType(int msg, int param1, Object param2) {
		GolukDebugUtils.e("", "TSettings--------callback_getVoiceType----------param2：" + param2);
		if (RESULE_SUCESS == param1) {
			try {
				JSONObject json = new JSONObject((String) param2);
				int type = json.getInt("type");
				mVoiceType = type + "";
				refreshVoiceType();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void callback_setVoiceType(int msg, int param1, Object param2) {
		GolukDebugUtils.e("", "TSettingsActivity--------callback_setVoiceType---------param2：" + param2);
		closeLoading();
		if (RESULE_SUCESS == param1) {
			GolukApplication.getInstance().getIPCControlManager().getVoiceType();
		}
	}
	
	@Override
	protected void hMessage(Message msg) {
		if (100 == msg.what) {
			restoreSuccess();
		}
	}
	
	private void restoreSuccess() {
		GolukDebugUtils.e("","restore  init-----SettingActivity--------restoreSuccess: 11");
		EventBindFinish eventFnish = new EventBindFinish(EventConfig.BIND_LIST_DELETE_CONFIG);
		EventBus.getDefault().post(eventFnish);
		GolukApplication.getInstance().setIpcDisconnect();
		WifiBindHistoryBean bean = WifiBindDataCenter.getInstance().getCurrentUseIpc();
		
		GolukDebugUtils.e("","restore  init-----SettingActivity--------restoreSuccess: 22: " + bean.ipc_ssid);
		
		if (null != bean) {
			WifiBindDataCenter.getInstance().deleteBindData(bean.ipc_ssid);
		}
	}

	
	/**
	 * 恢复出厂设置
	 * @param msg
	 * @param param1
	 * @param param2
	 */
	private void callback_restore(int msg, int param1, Object param2) {
		String message = "";
		if (param1 == RESULE_SUCESS) {
			// 断开连接
			mBaseHandler.sendEmptyMessageDelayed(100, 2 * 100);
			message = this.getResources().getString(R.string.str_restore_success);
		} else {
			message = this.getResources().getString(R.string.str_restore_fail);
		}

		if (isFinishing()) {
			return;
		}
		CustomDialog mCustomDialog = new CustomDialog(this);
		mCustomDialog.setCancelable(false);
		mCustomDialog.setMessage(message, Gravity.CENTER);
		mCustomDialog.setLeftButton(this.getResources().getString(R.string.user_personal_sign_title),
				new OnLeftClickListener() {
					@Override
					public void onClickListener() {
						finish();
					}
				});
		mCustomDialog.show();
	}
	
	private void switchAdasEnableUI(boolean isEnable) {
		if (mAdasConfigParamter == null) {
			return;
		}
		if (isEnable) {
			mAdasConfigLayout.setVisibility(View.VISIBLE);
			mForwardCloseLayout.setVisibility(View.VISIBLE);
			mForwardSetupLayout.setVisibility(View.VISIBLE);
			mAdasAssistanceBtn.setBackgroundResource(R.drawable.set_open_btn);
			mAdasAssistanceBtn.setTag(1);
		} else {
			mAdasAssistanceBtn.setBackgroundResource(R.drawable.set_close_btn);
			mAdasAssistanceBtn.setTag(0);
			mAdasConfigLayout.setVisibility(View.GONE);
			mForwardCloseLayout.setVisibility(View.GONE);
			mForwardSetupLayout.setVisibility(View.GONE);
		}

		refreshFcarCloseUI();
		refreshFcarSetupUI();
	}
	
	private void refreshFcarCloseUI() {
		if (mAdasConfigParamter == null) {
			return;
		}
		if (mAdasConfigParamter.fcw_enable == 0) {
			mForwardCloseBtn.setBackgroundResource(R.drawable.set_close_btn);
			mForwardCloseBtn.setTag(0);
		} else {
			mForwardCloseBtn.setBackgroundResource(R.drawable.set_open_btn);
			mForwardCloseBtn.setTag(1);
		}
	}

	private void refreshFcarSetupUI() {
		if (mAdasConfigParamter == null) {
			return;
		}
		if (mAdasConfigParamter.fcs_enable == 0) {
			mForwardSetupBtn.setBackgroundResource(R.drawable.set_close_btn);
			mForwardSetupBtn.setTag(0);
		} else {
			mForwardSetupBtn.setBackgroundResource(R.drawable.set_open_btn);
			mForwardSetupBtn.setTag(1);
		}
	}
	
	private void checkGetState() {
		if (getRecordState && getMotionCfg) {
			closeLoading();
		}
	}
	
	private void showLoading() {
		if (!mCustomProgressDialog.isShowing()) {
			mCustomProgressDialog.show();
			mCustomProgressDialog.setListener(this);
		}
	}

	private void closeLoading() {
		if (mCustomProgressDialog.isShowing()) {
			mCustomProgressDialog.close();
		}
	}
	/**
	 * 重启IPC
	 */
	private void showRebootDialog() {
		if (null == mRestartDialog) {
			mRestartDialog = new AlertDialog.Builder(this)
					.setTitle(this.getString(R.string.user_dialog_hint_title))
					.setMessage(this.getString(R.string.str_settings_restart_ipc))
					.setNegativeButton(this.getString(R.string.str_reboot_ipc_later),
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface arg0, int arg1) {
                                    if(null != mRestartDialog && mRestartDialog.isShowing()) {
                                        mRestartDialog.dismiss();
                                        mRestartDialog = null;
                                    }
								}
							})
					.setPositiveButton(this.getString(R.string.str_reboot_ipc_now),
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface arg0, int arg1) {
									boolean reboot = GolukApplication.getInstance().getIPCControlManager()
											.setIPCReboot();
									GolukDebugUtils.e("", "TSettingsActivity-----------IPC_VDCP_Msg_Reboot-----reboot: " + reboot);
									if (reboot) {
                                        if(null != mRestartDialog && mRestartDialog.isShowing()) {
                                            mRestartDialog.dismiss();
                                            mRestartDialog = null;
                                        }
									}
								}
							}).setCancelable(false).show();
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		GolukDebugUtils.e("", "TSettingsActivity----onActivityResult----requestCode :" + requestCode + "   resultCode:"
				+ resultCode);
		switch (requestCode) {
		case REQUEST_CODE_WONDERFUL_VIDEO_QUALITY:// 精彩视频质量
			activityResult_wonderful(resultCode, data);
			break;
		case REQUEST_CODE_WONDERFUL_VIDEO_TYPE:// 精彩视频类型
			activityResult_wonderfulVideoType(resultCode, data);
			break;
		case REQUEST_CODE_TONE_VOLUMN://提示音音量大小
			activityResult_volume(resultCode, data);
			break;
		case REQUEST_CODE_SHUTDOWN_TIME://开机时间
			activityResult_PowerTime(resultCode, data);
			break;
		case REQUEST_CODE_LANGUAGE://语言设置
			activityResult_voiceType(resultCode, data);
			break;

		default:
			break;
		}
	}
	
	// 精彩视频质量
	private void activityResult_wonderful(int resultCode, Intent data) {
		if (Activity.RESULT_OK != resultCode) {
			return;
		}
		if (null != data) {
			mWonderfulVideoResolution = data.getStringExtra("params");
			refreshWonderfulVideoData();
			GolukApplication.getInstance().getIPCControlManager().setVideoResolution(mWonderfulVideoResolution);
		}
	}
	
	private void activityResult_wonderfulVideoType(int resultCode, Intent data) {
		int historyTime = 0;
		int futureTime = 0;
		if (Activity.RESULT_OK != resultCode) {
			return;
		}
		if (null != data) {
			mWonderfulVideoType = data.getStringExtra("params");
			if (null != mWonderfulVideoType) {
				if (mWonderfulVideoType.equals("6")) {
					historyTime = 6;
					futureTime = 6;
				} else if (mWonderfulVideoType.equals("30")) {
					historyTime = 0;
					futureTime = 30;
				}
				GolukApplication.getInstance().getIPCControlManager().setWonderfulVideoType(historyTime, futureTime);
			}
		}
	}
	
	// 提示音音量大小
	private void activityResult_volume(int resultCode, Intent data) {
		if (Activity.RESULT_OK != resultCode) {
			return;
		}
		if (null != data) {
			mVolume = data.getStringExtra("params");
			refreshVolume();
			if (null == mVolume || "".equals(mVolume)) {
				return;
			}
			GolukApplication.getInstance().getIPCControlManager().setVolume(Integer.parseInt(mVolume));
		}
	}
	
	//关机时间
	private void activityResult_PowerTime(int resultCode, Intent data) {
		if (Activity.RESULT_OK != resultCode) {
			return;
		}
		if (null != data) {
			mPowerTime = data.getStringExtra("params");
			refreshPowerTime();
			if (null == mPowerTime || "".equals(mPowerTime)) {
				return;
			}
			GolukApplication.getInstance().getIPCControlManager().setPowerOffTime(Integer.parseInt(mPowerTime));
		}
	}
	
	//语言设置
	private void activityResult_voiceType(int resultCode, Intent data) {
		if (Activity.RESULT_OK != resultCode) {
			return;
		}
		if (null != data) {
			mVoiceType = data.getStringExtra("params");
			refreshVoiceType();
			if (null == mVoiceType || "".equals(mVoiceType)) {
				return;
			}
			GolukApplication.getInstance().getIPCControlManager().setVoiceType(Integer.parseInt(mVoiceType));
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (null != GolukApplication.getInstance().getIPCControlManager()) {
			GolukApplication.getInstance().getIPCControlManager().removeIPCManagerListener(TAG);
		}
		EventBus.getDefault().unregister(this);
		if (mCustomDialog != null && mCustomDialog.isShowing()) {
			mCustomDialog.dismiss();
		}
		mCustomDialog = null;
		
		if (null != mRestartDialog) {
			mRestartDialog.dismiss();
			mRestartDialog = null;
		}
	}

	private void loadRes() {
		mArrayText = getResources().getStringArray(R.array.list_quality_ui);
		mResolutionArray = SettingsUtil.returnResolution(this, mIPCName);
		mBitrateArray = SettingsUtil.returnBitrate(this, mIPCName);
		mWonderfulVideo = getResources().getStringArray(R.array.list_wonderful_video_quality);
		mWonderfulVideoValue = getResources().getStringArray(R.array.list_wonderful_video_quality_value);
		mVolumeList = getResources().getStringArray(R.array.list_tone_volume);
		mVolumeValue = getResources().getStringArray(R.array.list_tone_volume_value);
		mPowerTimeList = getResources().getStringArray(R.array.list_shutdown_time);
		if(mBaseApp.isMainland()) {
			mVoiceTypeList = getResources().getStringArray(R.array.list_language);
		}else{
			mVoiceTypeList = getResources().getStringArray(R.array.list_language_t);
		}
	}
	
	// 遍历分辨率，区分码率，改变UI
	private void setData2UI() {
		if (null != mVideoConfigState && null != mResolutionArray && null != mBitrateArray) {
			for (int i = 0; i < mResolutionArray.length; i++) {
				if (mVideoConfigState.resolution.equals(mResolutionArray[i])) {
					if (String.valueOf(mVideoConfigState.bitrate).equals(mBitrateArray[i])) {
						GolukDebugUtils.e("", "TSettingsActivity--------------mArrayText：" + mArrayText[i]);
						mRecycleQualityDesc.setText(mArrayText[i]);
						break;
					}
				}
			}

		}
	}

	/**
	 * 更新精彩视频质量
	 */
	private void refreshWonderfulVideoData() {
		int length = mWonderfulVideoValue.length;
		for (int i = 0; i < length; i++) {
			if (mWonderfulVideoResolution.equals(mWonderfulVideoValue[i])) {
				mWonderfulQualityDesc.setText(mWonderfulVideo[i]);
			}
		}
	}
	
	/**
	 * 更新T1声音录制开关状态
	 * @param state
	 */
	private void refreshUI_soundRecod(int state) {
		switch (state) {
		case STATE_CLOSE:
			mVoiceRecordBtn.setBackgroundResource(R.drawable.set_close_btn);
			break;
		case STATE_OPEN:
			mVoiceRecordBtn.setBackgroundResource(R.drawable.set_open_btn);
			break;
		default:
			mVoiceRecordBtn.setBackgroundResource(R.drawable.set_close_btn);
			break;
		}
	}
	
	/**
	 * 更新提示音音量大小
	 */
	private void refreshVolume() {
		int length = mVolumeValue.length;
		for (int i = 0; i < length; i++) {
			if (mVolumeValue[i].equals(mVolume)) {
				mVolumeDesc.setText(mVolumeList[i]);
			}
		}
	}
	
	/**
	 * 更新关机时间
	 */
	private void refreshPowerTime() {
		int length = mPowerTimeList.length;
		String[] value = { "10", "60" };
		for (int i = 0; i < length; i++) {
			if (mPowerTime.equals(value[i])) {
				mShutdownTimeDesc.setText(mPowerTimeList[i]);
			}
		}
	}
	
	/**
	 * 更新语言设置
	 */
	private void refreshVoiceType() {
		int length = mVoiceTypeList.length;

		String[] type;
		if (mBaseApp.isMainland()){
			type = new String[] { "0", "1" };
		}else{
			type = new String[] { "0", "1" ,"2"};
		}
		for (int i = 0; i < length; i++) {
			if (mVoiceType.equals(type[i])) {
				mLanguageDesc.setText(mVoiceTypeList[i]);
			}
		}
	}
	
	
	
	
	
	// 点击存储卡容量查询
	private void click_sd() {
		Intent sdIntent = new Intent(this, StorageCpacityQueryActivity.class);
		startActivity(sdIntent);
	}

	// 循环视频质量
	private void click_recycleVideoQuality() {
		Intent recycleVideoIntent = new Intent(this, VideoQualityActivity.class);
		startActivity(recycleVideoIntent);
	}

	// 自动循环录像
	private void click_autoRecycle() {
		if (recordState) {
			boolean a = GolukApplication.getInstance().getIPCControlManager().stopRecord();
			GolukDebugUtils.e("", "TSettingsActivity-----------click_autoRecycle-----a: " + a);
		} else {
			boolean b = GolukApplication.getInstance().getIPCControlManager().startRecord();
			GolukDebugUtils.e("", "TSettingsActivity-----------click_autoRecycle-----b: " + b);
		}
		showLoading();
	}
	
	//精彩视频类型
	private void click_wonderfulVideoType() {
		Intent itWonderful = new Intent(this, SettingsItemActivity.class);
		itWonderful.putExtra(SettingsItemActivity.TYPE, SettingsItemActivity.TYPE_WONDERFUL_VIDEO_TYPE);
		itWonderful.putExtra(SettingsItemActivity.PARAM, mWonderfulVideoType);
		startActivityForResult(itWonderful, REQUEST_CODE_WONDERFUL_VIDEO_TYPE);
	}
	
	//精彩视频质量
	private void click_wonderfulVideoQuality() {
		Intent itWonderful = new Intent(this, SettingsItemActivity.class);
		itWonderful.putExtra(SettingsItemActivity.TYPE, SettingsItemActivity.TYPE_WONDERFUL_VIDEO_QUALITY);
		itWonderful.putExtra(SettingsItemActivity.PARAM, mWonderfulVideoResolution);
		startActivityForResult(itWonderful, REQUEST_CODE_WONDERFUL_VIDEO_QUALITY);
	}
	//声音录制
	private void click_SoundRecord() {
		mVoiceRecordState = mVoiceRecordState == 0 ? 1 : 0;
		GolukApplication.getInstance().setT1VideoCfgState(mVoiceRecordState);
		boolean isSuccess = GolukApplication.getInstance().getIPCControlManager().setAudioCfg_T1(mVoiceRecordState);
		if (isSuccess) {
			showLoading();
		} else {
			GolukUtils.showToast(this, getResources().getString(R.string.str_carrecoder_setting_failed));
		}
	}
	//自动同步照片
	private void click_autoPhoto() {
		if (mAutoState) {
			mAutophotoBtn.setBackgroundResource(R.drawable.set_close_btn);
			mAutoState = false;
		} else {
			mAutophotoBtn.setBackgroundResource(R.drawable.set_open_btn);
			mAutoState = true;
		}
		GolukFileUtils.saveBoolean(GolukFileUtils.PROMOTION_AUTO_PHOTO, mAutoState);
	}
	//提示音音量大小
	private void click_volume() {
		Intent itVolume = new Intent(this, SettingsItemActivity.class);
		itVolume.putExtra(SettingsItemActivity.TYPE, SettingsItemActivity.TYPE_TONE_VOLUME);
		itVolume.putExtra(SettingsItemActivity.PARAM, mVolume);
		startActivityForResult(itVolume, REQUEST_CODE_TONE_VOLUMN);
	}
	//开关机提示音
	private void click_kgjtsy() {
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
	//精彩视频拍摄提示音
	private void click_wonderfulTakePhoto() {
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
		} else {
			showLoading();
		}
	}
	//图像自动翻转
	private void click_autoRoating() {
		if (1 == autoRoatingEnable) {
			autoRoatingEnable = 0;
		} else {
			autoRoatingEnable = 1;
		}
		boolean t1Auto = GolukApplication.getInstance().getIPCControlManager().setT1AutoRotaing(setAutoRotaingJson());
		if (!t1Auto) {
			GolukUtils.showToast(this, getResources().getString(R.string.str_carrecoder_setting_failed));
		} else {
			showLoading();
		}
	} 
	//视频水印
	private void click_videoLogo() {
		if (null != mDisplay) {
			if (0 == mDisplay.logo_visible) {
				mDisplay.logo_visible = 1;
			} else {
				mDisplay.logo_visible = 0;
			}
			boolean a = GolukApplication.getInstance().getIPCControlManager()
					.setVideoLogo(mDisplay.logo_visible, mDisplay.time_visible);
			if (a) {
				showLoading();
			}
		}
	}
	
	//疲劳驾驶
	private void click_Fatigue() {
		if (driveFatigue == 1) {
			driveFatigue = 0;
		} else {
			driveFatigue = 1;
		}

		boolean fatigue = GolukApplication.getInstance().getIPCControlManager().setFunctionMode(getSetJson());
		if (!fatigue) {
			GolukUtils.showToast(this, getResources().getString(R.string.str_carrecoder_setting_failed));
		} else {
			showLoading();
		}
	}
	//停车休眠
	private void click_parkingSleep() {
		if (dormant == 1) {
			dormant = 0;
		} else {
			dormant = 1;
		}
		boolean parkingSleep = GolukApplication.getInstance().getIPCControlManager().setFunctionMode(getSetJson());
		if (!parkingSleep) {
			GolukUtils.showToast(this, getResources().getString(R.string.str_carrecoder_setting_failed));
		} else {
			showLoading();
		}
	}
	//停车安防
	private void click_tcaf() {
		if (1 == enableSecurity) {
			enableSecurity = 0;
		} else {
			enableSecurity = 1;
		}
		boolean c = GolukApplication.getInstance().getIPCControlManager().setMotionCfg(enableSecurity, snapInterval);
		if(c) {
			showLoading();
		}
	}

	private void click_move() {
		boolean c = GolukApplication.getInstance().getIPCControlManager().setT1SW(String.valueOf(enableMoveMonitor==0?1:0));
		if(c) {
			showLoading();
		}
	}

	//前向距离安全预警
	private void click_assistance() {
		if (mAdasConfigParamter == null) {
			return;
		}
		if (mAdasConfigParamter.head_offset == 0) {
			if (mCustomDialog == null) {
				mCustomDialog = new CustomDialog(this);
			}

			mCustomDialog.setMessage(getString(R.string.str_adas_hint), Gravity.CENTER);
			mCustomDialog.setLeftButton(getString(R.string.dialog_str_cancel), null);
			mCustomDialog.setRightButton(getString(R.string.str_adas_dialog_confirm), new OnRightClickListener() {

				@Override
				public void onClickListener() {
					Intent intent = new Intent(TSettingsActivity.this, AdasGuideActivity.class);
					intent.putExtra(AdasVerificationActivity.ADASCONFIGDATA, mAdasConfigParamter);
					startActivity(intent);
				}
			});
			mCustomDialog.show();
		} else {
			showLoading();
			if (mAdasConfigParamter.enable == 0) {
				mAdasConfigParamter.enable = 1;
			} else {
				mAdasConfigParamter.enable = 0;
			}
			GolukApplication.getInstance().getIPCControlManager().setT1AdasConfigEnable(mAdasConfigParamter.enable);
		}
	}
	//前向车距过近提示 
	private void click_carClose() {
		if (mAdasConfigParamter == null) {
			return;
		}
		showLoading();
		if (mAdasConfigParamter.fcw_enable == 0) {
			mAdasConfigParamter.fcw_enable = 1;
		} else {
			mAdasConfigParamter.fcw_enable = 0;
		}
		GolukApplication.getInstance().getIPCControlManager().setT1AdasConfigFcw(mAdasConfigParamter.fcw_enable);
	}
	//前向车辆启动提示
	private void click_carSetup() {
		if (mAdasConfigParamter == null) {
			return;
		}
		showLoading();
		if (mAdasConfigParamter.fcs_enable == 0) {
			mAdasConfigParamter.fcs_enable = 1;
		} else {
			mAdasConfigParamter.fcs_enable = 0;
		}
		GolukApplication.getInstance().getIPCControlManager().setT1AdasConfigFcs(mAdasConfigParamter.fcs_enable);
	}
	//前向距离预警配置
	private void click_adasCfg() {
		if (mAdasConfigParamter == null) {
			return;
		}
		Intent intent = new Intent(this, AdasConfigActivity.class);
		intent.putExtra(AdasVerificationActivity.ADASCONFIGDATA, mAdasConfigParamter);
		startActivity(intent);
	}
	//关机时间
	private void click_powerOffTime() {
		Intent itPowerTime = new Intent(this, SettingsItemActivity.class);
		itPowerTime.putExtra(SettingsItemActivity.TYPE, SettingsItemActivity.TYPE_SHUTDOWN_TIME);
		itPowerTime.putExtra(SettingsItemActivity.PARAM, mPowerTime);
		startActivityForResult(itPowerTime, REQUEST_CODE_SHUTDOWN_TIME);
	}
	//语言设置
	private void click_voiceType() {
		Intent itVoiceType = new Intent(this, SettingsItemActivity.class);
		if(mSupportRussian) {
			itVoiceType.putExtra(SettingsItemActivity.TYPE, SettingsItemActivity.TYPE_LANGUAGE_T);
		}else {
			itVoiceType.putExtra(SettingsItemActivity.TYPE, SettingsItemActivity.TYPE_LANGUAGE);
		}
		itVoiceType.putExtra(SettingsItemActivity.PARAM, mVoiceType);
		startActivityForResult(itVoiceType, REQUEST_CODE_LANGUAGE);
	}
	
	//恢复出厂
	private void click_reset() {
		if (mCustomDialog == null) {
			mCustomDialog = new CustomDialog(this);
		}
		mCustomDialog.setMessage(this.getResources().getString(R.string.str_reset_message), Gravity.CENTER);
		mCustomDialog.setLeftButton(this.getResources().getString(R.string.user_personal_sign_title),
				new OnLeftClickListener() {
					@Override
					public void onClickListener() {
						if (GolukApplication.getInstance().getIpcIsLogin()) {
							boolean a = GolukApplication.getInstance().getIPCControlManager().restoreIPC();
							GolukDebugUtils.e("", "YYYYYY=================restoreIPC============a=" + a);
						}
					}
				});
		mCustomDialog.setRightButton(this.getResources().getString(R.string.dialog_str_cancel), null);
		mCustomDialog.show();
	}
	
	private void click_gSensorControlCfg() {
		Intent pzgylmd_line = new Intent(this, ImpactSensitivityActivity.class);
		startActivity(pzgylmd_line);
	}
	
	private String setAutoRotaingJson() {
		try {
			JSONObject obj = new JSONObject();
			obj.put("enable", autoRoatingEnable);
			return obj.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
	
	int recbySec = 0;
	int moveMonitor = 0;
	// 停车休眠
	int dormant = 0;
	int recLight = 0;
	int wifiLight = 0;
	int securityLight = 0;
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
			driveFatigue = obj.optInt("DriveFatigue");

			if (1 == dormant) {
				mParkingsleepBtn.setBackgroundResource(R.drawable.set_open_btn);
				if (1 == enableSecurity) {
					enableSecurity = 0;
					// TODO 设置停车安防
					boolean c = GolukApplication.getInstance().getIPCControlManager()
							.setMotionCfg(enableSecurity, snapInterval);
				}
			} else {
				mParkingsleepBtn.setBackgroundResource(R.drawable.set_close_btn);
			}

			if (1 == driveFatigue) {
				mFatigueBtn.setBackgroundResource(R.drawable.set_open_btn);
			} else {
				mFatigueBtn.setBackgroundResource(R.drawable.set_close_btn);
			}
		} catch (Exception e) {
			e.printStackTrace();
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
			obj.put("AutoRotation", 0);// G1、Ｇ2图像翻转
			obj.put("DriveFatigue", driveFatigue);

			return obj.toString();

		} catch (Exception e) {

		}
		return "";
	}

	@Override
	public void forbidBackKey(int backKey) {
		if(1 == backKey) {
			exit();
		}
	}
	
}
