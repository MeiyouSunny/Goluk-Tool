package com.mobnote.golukmain.carrecorder.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.elvishew.xlog.XLog;
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
import com.mobnote.golukmain.carrecorder.settings.bean.WonderfulVideoDisplay;
import com.mobnote.golukmain.carrecorder.util.IpcSettingUtil;
import com.mobnote.golukmain.carrecorder.view.CustomDialog;
import com.mobnote.golukmain.carrecorder.view.CustomDialog.OnLeftClickListener;
import com.mobnote.golukmain.carrecorder.view.CustomDialog.OnRightClickListener;
import com.mobnote.golukmain.carrecorder.view.CustomFormatDialog;
import com.mobnote.golukmain.carrecorder.view.CustomLoadingDialog;
import com.mobnote.golukmain.carrecorder.view.CustomLoadingDialog.ForbidBack;
import com.mobnote.golukmain.wifibind.WiFiInfo;
import com.mobnote.golukmain.wifidatacenter.WifiBindDataCenter;
import com.mobnote.golukmain.wifidatacenter.WifiBindHistoryBean;
import com.mobnote.log.app.LogConst;
import com.mobnote.util.GolukFastJsonUtil;
import com.mobnote.util.GolukFileUtils;
import com.mobnote.util.GolukUtils;
import com.mobnote.util.JsonUtil;
import com.mobnote.util.SharedPrefUtil;

import org.json.JSONException;
import org.json.JSONObject;

import cn.com.mobnote.module.ipcmanager.IPCManagerFn;
import cn.com.tiros.debug.GolukDebugUtils;
import de.greenrobot.event.EventBus;

import static com.mobnote.golukmain.carrecorder.IPCControlManager.T3U_SIGN;
import static com.mobnote.golukmain.carrecorder.IPCControlManager.T3_SIGN;

/**
 *
 * IPC????????????
 *
 * 2015???4???6???
 *
 * @author xuhw
 */
public class SettingsActivity extends BaseActivity implements OnClickListener, IPCManagerFn, ForbidBack {
	private final int STATE_CLOSE = 0;
	private final int STATE_OPEN = 1;

	public static final int REQUEST_CODE_PHOTO = 20;
	public static final int REQUEST_CODE_KIT = 30;
	public static final int REQUEST_CODE_ADAS_FCW_WARNING = 31;
	public static final int REQUEST_CODE_ADAS_LDW_WARNING = 32;
	public static final int REQUEST_CODE_WONDERFUL_VIDEO_QUALITY = 33;
	public static final int REQUEST_CODE_TONE_VOLUMN = 34;
	public static final int REQUEST_CODE_SHUTDOWN_TIME = 35;
	public static final int REQUEST_CODE_LANGUAGE = 36;
	/**??????????????????**/
	public static final int REQUEST_CODE_WONDERFUL_VIDEO_TYPE = 37;
	public static final int REQUEST_CODE_ANTI_FLICKER = 38;
	/** ???????????? */
	private boolean recordState = false;
	/** ?????????????????????????????? */
	private Button mAutoRecordBtn = null;
	/** ???????????????????????? */
	private Button mAudioBtn = null;
	/** ?????????????????? **/
	private Button mSwitchBtn = null;
	/** ????????????????????? */
	private VideoConfigState mVideoConfigState = null;
	private int enableSecurity = 0;
	private int snapInterval = 0;
	private CustomLoadingDialog mCustomProgressDialog = null;
	private boolean getRecordState = false;
	private boolean getMotionCfg = false;
	/** ?????????????????? */
	private TextView mStorayeText = null;
	/** ?????????????????? */
	private TextView mVideoText = null;
	/** ????????????????????? */
	private TextView mSensitivityText = null;
	/** HDR?????? **/
	private Button mISPBtn = null;
	/** HDR??????line **/
	private RelativeLayout mISPLayout;
	/** HDR?????? 0?????? 1?????? **/
	private int mISPSwitch = 0;
	/** ??????????????????????????? **/
	private Button mWonderVideoBtn = null;
	/** ????????????????????????????????? 0?????? 1?????? **/
	private int mWonderfulSwitchStatus = 1;
	/** ??????????????????(true)??????????????????????????????(false)?????? **/
	private boolean judgeSwitch = true;
	/** ?????????????????? 0?????? 1?????? **/
	private int speakerSwitch = 0;
	/** ??????????????? **/
	private String ipcVersion = "";
	/** ipc???????????? **/
	private String mIPCName = "";
	private String[] mResolutionArray = null;
	private String[] mBitrateArray = null;
	private String[] mArrayText = null;
	/** ????????????line **/
	private RelativeLayout mPhotoQualityLayout,mVideoLogoLayout;
	private TextView mPhotoQualityText = null;
	/** ?????????????????? **/
	private RelativeLayout mAutoPhotoItem;
	private ImageButton mAutoPhotoBtn = null;
	/** ???????????? **/
	private RelativeLayout mFatigueLayout;
	private Button mFatigueBtn = null;
	/** ?????????????????? **/
	private RelativeLayout mImageFlipLayout;
	private RelativeLayout mRlAntiFlicker;
	private Button mImageFlipBtn = null;
	/** ?????????????????? **/
	private RelativeLayout mParkingSleepLayout;
	private Button mParkingSleepBtn = null;
	/** ????????????????????? **/
	private RelativeLayout mHandsetLayout;
	private TextView mHandsetText = null;
	private TextView mTvAnti ;

	/** ?????????????????????????????? **/
	private TextView mParkingSleepHintText = null;
	/** ?????????????????????????????? **/
	private TextView mParkingSecurityHintText = null;

	private TextView mCarrecorderWonderfulLine;
//	mCarrecorderSensitivityLine;

	/**ADAS??????????????????**/
	private RelativeLayout mADASAssistanceLayout = null;
	private Button mADASAssistanceBtn = null;

	/**adas???????????? ????????????**/
//	/**???????????????????????????**/
//	private RelativeLayout mADASForwardWarningLayout = null;
//	private TextView mADASForwardWarningTextView = null;
//	/**???????????????????????????**/
//	private RelativeLayout mADASOffsetWarningLayout = null;
//	private TextView mADASOffsetWarningTextView = null;
	/**????????????????????????**/
	private RelativeLayout mADASFcarCloseLayout = null;
	private Button mADASFcarCloseBtn = null;
	/**????????????????????????**/
	private RelativeLayout mADASFcarSetupLayout = null;
	private Button mADASFcarSetupBtn = null;
	private Button mVideoLogoBtn, mMoveMotionBtn;
	/**??????????????????**/
//	private RelativeLayout mADASOsdLayout = null;
//	private Button mADASOsdBtn = null;
	/**ADAS??????**/
	private RelativeLayout mADASConfigLayout = null;

	private AdasConfigParamterBean mAdasConfigParamter;

	private CustomDialog mCustomDialog;
	/**?????????????????????????????????????????????**/

	boolean mAutoState = true;
	private TextView mTextWonderfulVideoQualityText, mVolumeText, mPowerTimeText, mVoiceTypeText;
	private RelativeLayout mWonderfulVideoQualityLayout, mVolumeLayout, mPowerTimeLayout, mVoiceTypeLayout, mMSLayout;
	/** ?????????????????? **/
	private String mWonderfulVideoResolution = "";
	/**??????????????????????????????**/
	private String mSaveLastResolution = "";
	/** ????????????????????? **/
	private String mVolume = "";
	/** ???????????? **/
	private String mPowerTime = "";
	/** ?????? **/
	private String mVoiceType = "";
	private boolean mSetMove = false;
	/*
??????????????????
 */
	/**?????????????????????**/
	private AlertDialog mRestartDialog = null;
	/** ?????????????????? **/
	private RelativeLayout mVideoTypeLayout = null;
	private TextView mVideoTypeDesc = null;
	private String mVideoType = "";
	private String mCurrentVideoType = "";
	private String mAntiFlicker = "";
	/**??????????????????**/
	private WonderfulVideoDisplay mDisplay;
	/**??????????????????**/
	private Button mBtnEmergencyVideoHint;
	private RelativeLayout rLayoutEmergencyVideoTone;
	/**????????????**/
	private Button mBtnTimeLapse;
	private RelativeLayout rLayoutTimeLapseVideo;
	/* ????????????????????????????????? */
	private int mEmgVideoSoundState;
	/* ???????????????????????? */
	private int mTimeslapseState;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.carrecorder_settings);
		// ???????????????
		ipcVersion = SharedPrefUtil.getIPCVersion();
		GolukDebugUtils.e("", "=========ipcVersion???" + ipcVersion);
		mIPCName = GolukApplication.getInstance().mIPCControlManager.mProduceName;
		loadRes();

		GolukDebugUtils.e("", "=========mIPCName???" + mIPCName);
		mAutoState = GolukFileUtils.loadBoolean(GolukFileUtils.PROMOTION_AUTO_PHOTO, true);
		initView();
		setListener();
		EventBus.getDefault().register(this);
		mKitShowUI = getResources().getStringArray(R.array.kit_setting_ui);
		mArrayText = getResources().getStringArray(R.array.list_quality_ui);
		mResolutionArray = SettingsUtil.returnResolution(this, mIPCName);
		mBitrateArray = SettingsUtil.returnBitrate(this, mIPCName);

		mCustomProgressDialog = new CustomLoadingDialog(this, null);
		if (null != GolukApplication.getInstance().getIPCControlManager()) {
			GolukApplication.getInstance().getIPCControlManager().addIPCManagerListener("settings", this);
		}
		firstRequest();

		XLog.tag(LogConst.TAG_SETTING).i("Enter IPC setting page.");
	}

	// ?????????????????????
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

		// ??????ipc????????????????????????
		boolean switchFlag = GolukApplication.getInstance().getIPCControlManager().getIPCSwitchState();
		GolukDebugUtils.e("lily", "---------------switchFlag----------------" + switchFlag);

		// ??????ISP??????
		boolean getISPMode = GolukApplication.getInstance().getIPCControlManager().getISPMode();
		GolukDebugUtils.e("", "--------------SettingsActivity-----getISPMode???" + getISPMode);

		// ?????????????????????G1???????????????????????????????????????
		boolean getFunctionMode = GolukApplication.getInstance().getIPCControlManager().getFunctionMode();
		GolukDebugUtils.e("", "--------------SettingsActivity-----getFunctionMode???" + getFunctionMode);

		// ????????????????????????
		boolean wonderfulType = GolukApplication.getInstance().getIPCControlManager().getWonderfulVideoType();
		GolukDebugUtils.e("", "TSettingsActivity-------------------wonderfulType???" + wonderfulType);

		// ????????????????????????
		boolean videoResolution = GolukApplication.getInstance().getIPCControlManager().getVideoResolution();
		GolukDebugUtils.e("", "--------------SettingsActivity-----videoResolution???" + videoResolution);

		boolean antiFlicker = GolukApplication.getInstance().getIPCControlManager().getAntiFlicker();
		GolukDebugUtils.e("", "--------------SettingsActivity-----getAntiFlicker???" + antiFlicker);
//		if (IPCControlManager.T1_SIGN.equals(GolukApplication.getInstance().getIPCControlManager().mProduceName)) {
////			// ???????????????????????????false
////			boolean getKitMode = GolukApplication.getInstance().getIPCControlManager().getKitMode();
////			GolukDebugUtils.e("", "--------------SettingsActivity-----getKitMode???" + getKitMode);
//			boolean t1VoiceState = GolukApplication.getInstance().getIPCControlManager().getAudioCfg_T1();
//			// ????????????false
////			boolean getPhotoQualityMode = GolukApplication.getInstance().getIPCControlManager().getPhotoQualityMode();
////			GolukDebugUtils.e("", "--------------SettingsActivity-----getPhotoQualityMode???" + getPhotoQualityMode);
//			GolukDebugUtils.e("", "--------------SettingsActivity-----t1VoiceState???" + t1VoiceState);
//			// ??????T1??????????????????
//			boolean t1GetAutoRotaing = GolukApplication.getInstance().getIPCControlManager().getT1AutoRotaing();
//			GolukDebugUtils.e("", "--------------SettingsActivity-----t1GetAutoRotaing???" + t1GetAutoRotaing);
//
//			boolean t1GetAdasCofig = GolukApplication.getInstance().getIPCControlManager().getT1AdasConfig();
//			GolukDebugUtils.e("", "--------------SettingsActivity-----t1GetAutoRotaing???" + t1GetAdasCofig);
//			
//			// ????????????????????????
//			boolean videoResolution = GolukApplication.getInstance().getIPCControlManager().getVideoResolution();
//			GolukDebugUtils.e("", "--------------SettingsActivity-----videoResolution???" + videoResolution);
//			// ???????????????????????????
//			boolean volume = GolukApplication.getInstance().getIPCControlManager().getVolume();
//			GolukDebugUtils.e("", "--------------SettingsActivity-----volume???" + volume);
//			// ??????????????????
//			boolean powerOffTime = GolukApplication.getInstance().getIPCControlManager().getPowerOffTime();
//			GolukDebugUtils.e("", "--------------SettingsActivity-----powerOffTime???" + powerOffTime);
			// ??????????????????
        if(T3U_SIGN.equals(GolukApplication.getInstance().getIPCControlManager().mProduceName)){
			boolean voiceType = GolukApplication.getInstance().getIPCControlManager().getVoiceType();
			GolukDebugUtils.e("", "--------------SettingsActivity-----voiceType???" + voiceType);
			GolukApplication.getInstance().getIPCControlManager().getT1SW();
		}
		// ??????????????????
		if(GolukApplication.getInstance().getIPCControlManager().isSupportVideoLogo()){
			boolean videoLogo = GolukApplication.getInstance().getIPCControlManager().getVideoLogo();
			GolukDebugUtils.e("", "TSettingsActivity-------------------videoLogo???" + videoLogo);
		}
		//????????????????????????
        boolean flagTimelapse = GolukApplication.getInstance().getIPCControlManager().getTimelapseCfg();
		GolukDebugUtils.e("", "SettingsActivity-------------------flagTimelapse???" + flagTimelapse);
		//?????????????????????????????????
        boolean flagEmergencyHint = GolukApplication.getInstance().getIPCControlManager().getEmgVideoSoundCfg();
		GolukDebugUtils.e("", "SettingsActivity-------------------flagEmgVideoSound???" + flagEmergencyHint);
		// ???????????????
		if (mBaseApp.mIPCControlManager.isSupportTimeslapse()) {
			boolean flagGSensorValue = GolukApplication.getInstance().getIPCControlManager().getGSensorMoreValueCfg();
			GolukDebugUtils.e("", "SettingsActivity===getIPCControlManager============getGSensorMoreValueCfg======flag="
					+ flagGSensorValue);
		} else {
			boolean flagGSensorValue = GolukApplication.getInstance().getIPCControlManager().getGSensorControlCfg();
			GolukDebugUtils.e("", "SettingsActivity===getIPCControlManager============getGSensorControlCfg======flag="
					+ flagGSensorValue);
		}
		showLoading();
	}

	private void switchAdasEnableUI(boolean isEnable) {
		if (mAdasConfigParamter == null) {
			return;
		}
		if (isEnable) {
			/**adas???????????? ????????????**/
//			mADASForwardWarningLayout.setVisibility(View.VISIBLE);
//			mADASOffsetWarningLayout.setVisibility(View.VISIBLE);
//			mADASOsdLayout.setVisibility(View.VISIBLE);
			mADASConfigLayout.setVisibility(View.VISIBLE);
			mADASFcarCloseLayout.setVisibility(View.VISIBLE);
			mADASFcarSetupLayout.setVisibility(View.VISIBLE);
			mADASAssistanceBtn.setBackgroundResource(R.drawable.set_open_btn);
			mADASAssistanceBtn.setTag(1);
		} else {
			mADASAssistanceBtn.setBackgroundResource(R.drawable.set_close_btn);
			mADASAssistanceBtn.setTag(0);
			/**adas???????????? ????????????**/
//			mADASForwardWarningLayout.setVisibility(View.GONE);
//			mADASOffsetWarningLayout.setVisibility(View.GONE);
//			mADASOsdLayout.setVisibility(View.GONE);
			mADASConfigLayout.setVisibility(View.GONE);
			mADASFcarCloseLayout.setVisibility(View.GONE);
			mADASFcarSetupLayout.setVisibility(View.GONE);
		}

		/**adas???????????? ????????????**/
//		refreshFCWUI();
//		refreshLDWUI();
//		refreshOSDUI();
		refreshFcarCloseUI();
		refreshFcarSetupUI();
	}

	private void checkGetState() {
		if (getRecordState && getMotionCfg) {
			closeLoading();
		}
	}

	private void activityResult_Photo(int resultCode, Intent data) {
		if (Activity.RESULT_OK != resultCode) {
			return;
		}
		if (null != data && null != mPhtoBean) {

			String photoselect = data.getStringExtra("photoselect");
			mPhtoBean.quality = photoselect;
			mCurrentResolution = photoselect;
			GolukDebugUtils.e("", "SettingsActivity----onActivityResult----photo------mCurrentResolution :"
					+ mCurrentResolution);
			refreshPhotoQuality();
			String requestS = GolukFastJsonUtil.setParseObj(mPhtoBean);
			GolukDebugUtils.e("", "SettingsActivity----onActivityResult----photo------requestS :" + requestS);
			GolukApplication.getInstance().getIPCControlManager().setPhotoQualityMode(requestS);
		}
	}

	private void activityResult_kit(int resultCode, Intent data) {
		if (Activity.RESULT_OK != resultCode) {
			return;
		}
		if (null != data) {
			record = data.getIntExtra("record", 1);
			snapshot = data.getIntExtra("snapshot", 0);
			refreshKitUi();
			GolukApplication.getInstance().getIPCControlManager().setKitMode(setKitJson());
		}
	}

	// ??????????????????
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

	// ?????????????????????
	private void activityResult_Volume(int resultCode, Intent data) {
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

	// ????????????
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

	// ????????????
	private void activityResult_VoiceType(int resultCode, Intent data) {
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
	//??????????????????
	private void activityResult_wonderfulVideoType(int resultCode, Intent data) {
		int historyTime = 0;
		int futureTime = 0;
		if (Activity.RESULT_OK != resultCode) {
			return;
		}
		if (null != data) {
			mVideoType = data.getStringExtra("params");
			if (null != mVideoType) {
				if (mVideoType.equals("6")) {
					historyTime = 6;
					futureTime = 6;
				} else if (mVideoType.equals("30")) {
					historyTime = 0;
					futureTime = 30;
				}
				GolukApplication.getInstance().getIPCControlManager().setWonderfulVideoType(historyTime, futureTime);
			}
		}
	}

	private void refreshFcarCloseUI() {
		if (mAdasConfigParamter == null) {
			return;
		}
		if (mAdasConfigParamter.fcw_enable == 0) {
			mADASFcarCloseBtn.setBackgroundResource(R.drawable.set_close_btn);
			mADASFcarCloseBtn.setTag(0);
		} else {
			mADASFcarCloseBtn.setBackgroundResource(R.drawable.set_open_btn);
			mADASFcarCloseBtn.setTag(1);
		}
	}

	private void refreshFcarSetupUI() {
		if (mAdasConfigParamter == null) {
			return;
		}
		if (mAdasConfigParamter.fcs_enable == 0) {
			mADASFcarSetupBtn.setBackgroundResource(R.drawable.set_close_btn);
			mADASFcarSetupBtn.setTag(0);
		} else {
			mADASFcarSetupBtn.setBackgroundResource(R.drawable.set_open_btn);
			mADASFcarSetupBtn.setTag(1);
		}
	}

//	private void refreshOSDUI() {
//		if (mAdasConfigParamter == null) {
//			return;
//		}
//		if (mAdasConfigParamter.osd == 0) {
//			mADASOsdBtn.setBackgroundResource(R.drawable.set_close_btn);
//			mADASOsdBtn.setTag(0);
//		} else {
//			mADASOsdBtn.setBackgroundResource(R.drawable.set_open_btn);
//			mADASOsdBtn.setTag(1);
//		}
//	}
	/**adas???????????? ????????????**/
//	private void refreshFCWUI() {
//		if (mAdasConfigParamter.fcw_warn_level == 0) {
//			mADASForwardWarningTextView.setText(R.string.str_low);
//		} else if (mAdasConfigParamter.fcw_warn_level == 1) {
//			mADASForwardWarningTextView.setText(R.string.str_middle);
//		} else if (mAdasConfigParamter.fcw_warn_level == 2) {
//			mADASForwardWarningTextView.setText(R.string.str_high);
//		} else {
//			mADASForwardWarningTextView.setText(R.string.carrecorder_tcaf_close);
//		}
//	}
//
//	private void refreshLDWUI() {
//		if (mAdasConfigParamter.ldw_warn_level == 0) {
//			mADASOffsetWarningTextView.setText(R.string.str_low);
//		} else if (mAdasConfigParamter.ldw_warn_level == 1) {
//			mADASOffsetWarningTextView.setText(R.string.str_middle);
//		} else if (mAdasConfigParamter.ldw_warn_level == 2) {
//			mADASOffsetWarningTextView.setText(R.string.str_high);
//		} else {
//			mADASOffsetWarningTextView.setText(R.string.carrecorder_tcaf_close);
//		}
//	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		GolukDebugUtils.e("", "SettingsActivity----onActivityResult----requestCode :" + requestCode + "   resultCode:"
				+ resultCode);
		switch (requestCode) {
		case REQUEST_CODE_PHOTO:
			activityResult_Photo(resultCode, data);
			break;
		case REQUEST_CODE_KIT:
			activityResult_kit(resultCode, data);
			break;
		case REQUEST_CODE_WONDERFUL_VIDEO_QUALITY:
			activityResult_wonderful(resultCode, data);
			break;
		case REQUEST_CODE_TONE_VOLUMN:
			activityResult_Volume(resultCode, data);
			break;
		case REQUEST_CODE_SHUTDOWN_TIME:
			activityResult_PowerTime(resultCode, data);
			break;
		case REQUEST_CODE_LANGUAGE:
			activityResult_VoiceType(resultCode, data);
			break;
		case REQUEST_CODE_WONDERFUL_VIDEO_TYPE:// ??????????????????
			activityResult_wonderfulVideoType(resultCode, data);
			break;
			/**adas???????????? ????????????**/
//		case REQUEST_CODE_ADAS_FCW_WARNING:
//			if (resultCode == Activity.RESULT_OK) {
//				mAdasConfigParamter.fcw_warn_level = data.getIntExtra(AdasSensibilityActivity.SENSIBILITY_DATA, 0);
//				refreshFCWUI();
//			}
//			break;
//		case REQUEST_CODE_ADAS_LDW_WARNING:
//			if (resultCode == Activity.RESULT_OK) {
//				mAdasConfigParamter.ldw_warn_level = data.getIntExtra(AdasSensibilityActivity.SENSIBILITY_DATA, 0);
//				refreshLDWUI();
//			}
//			break;
			case REQUEST_CODE_ANTI_FLICKER:
				activityResult_antiFlicker(resultCode, data);
				break;
		default:
			break;
		}
	}

	private void activityResult_antiFlicker(int resultCode, Intent data) {
		if (Activity.RESULT_OK != resultCode) {
			return;
		}
		if (null != data) {
			mAntiFlicker = data.getStringExtra("params");
			GolukApplication.getInstance().getIPCControlManager().setAntiFlicker(mAntiFlicker);
		}
	}

	/**
	 * ???????????????
	 *
	 * @author xuhw
	 * @date 2015???4???6???
	 */
	private void initView() {
		mAutoRecordBtn = (Button) findViewById(R.id.zdxhlx);
		mAudioBtn = (Button) findViewById(R.id.sylz);
		mSwitchBtn = (Button) findViewById(R.id.kgjtsy);
		mISPLayout = (RelativeLayout) findViewById(R.id.hdr_line);
		mISPBtn = (Button) findViewById(R.id.hdr);
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
		mCarrecorderWonderfulLine = (TextView) findViewById(R.id.tv_carrecorder_line);
		mVideoLogoLayout = (RelativeLayout) findViewById(R.id.ry_t_settings_video_logo);
		mVideoLogoBtn = (Button) findViewById(R.id.btn_t_settings_video_logo);
//		mCarrecorderSensitivityLine = (TextView) findViewById(R.id.tv_carrecorder_sensitivity_line);

		//mADASAssistanceLayout = (RelativeLayout) findViewById(R.id.layout_adas_assistance);
		mADASAssistanceBtn = (Button) findViewById(R.id.btn_adas_assistance);
		/**adas???????????? ????????????**/
//		mADASForwardWarningLayout = (RelativeLayout) findViewById(R.id.layout_settings_adas_forward_sensibility);
//		mADASForwardWarningTextView = (TextView) findViewById(R.id.tv_settings_adas_forward_sensibility);
//		mADASOffsetWarningLayout = (RelativeLayout) findViewById(R.id.layout_settings_adas_offset_sensibility);
//		mADASOffsetWarningTextView = (TextView) findViewById(R.id.tv_settings_adas_offset_sensibility);
		mADASFcarCloseLayout = (RelativeLayout) findViewById(R.id.layout_settings_forward_car_close_warning);
		mADASFcarCloseBtn = (Button) findViewById(R.id.btn_settings_forward_car_close_warning);
		mADASFcarSetupLayout = (RelativeLayout) findViewById(R.id.layout_settings_forward_car_setup_hint);
		mADASFcarSetupBtn = (Button) findViewById(R.id.btn_settings_forward_car_setup_hint);
//		mADASOsdLayout = (RelativeLayout) findViewById(R.id.layout_settings_assistance_info);
//		mADASOsdBtn = (Button) findViewById(R.id.btn_settings_assistance_info);
        mADASConfigLayout = (RelativeLayout) findViewById(R.id.layout_settings_adas_config);

		mAutoPhotoItem = (RelativeLayout) findViewById(R.id.ry_setup_autophoto);
		mAutoPhotoBtn = (ImageButton) findViewById(R.id.ib_setup_autophoto_btn);

		mTextWonderfulVideoQualityText = (TextView) findViewById(R.id.tv_carrecorder_settings_wonderfulvideo_quality_text);
		mWonderfulVideoQualityLayout = (RelativeLayout) findViewById(R.id.rl_carrecorder_settings_wonderfulvideo_quality);
		mVolumeText = (TextView) findViewById(R.id.tv_settings_tone_text);
		mVolumeLayout = (RelativeLayout) findViewById(R.id.rl_settings_tone_line);
		mPowerTimeText = (TextView) findViewById(R.id.tv_settings_shutdown_text);
		mPowerTimeLayout = (RelativeLayout) findViewById(R.id.rl_settings_shutdown_line);
		mVoiceTypeText = (TextView) findViewById(R.id.tv_settings_language_text);
		mVoiceTypeLayout = (RelativeLayout) findViewById(R.id.rl_settings_language_line);
		mRlAntiFlicker = (RelativeLayout) findViewById(R.id.rl_anti_flicker);
		mVideoTypeLayout = (RelativeLayout) findViewById(R.id.ry_settings_wonderful_video_type);
		mVideoTypeDesc = (TextView) findViewById(R.id.tv_settings_wonderfulvideo_type_desc);
		mTvAnti = (TextView) findViewById(R.id.tv_carrecorder_settings_anti_flicker);
		mMSLayout =  (RelativeLayout) findViewById(R.id.ry_t_settings_adas_move_motion);
		mMoveMotionBtn = (Button) findViewById(R.id.btn_t_settings_adas_move_motion);
		mBtnEmergencyVideoHint = (Button) findViewById(R.id.btn_emergency_switch);
		rLayoutEmergencyVideoTone = (RelativeLayout) findViewById(R.id.rlayout_emergency_video_tone);
		mBtnTimeLapse = (Button) findViewById(R.id.btn_time_lapse_video);
		rLayoutTimeLapseVideo = (RelativeLayout) findViewById(R.id.rlayout_time_lapse_video);
		// ipc????????????
		if (GolukApplication.getInstance().mIPCControlManager.isG1Relative()) {
			mISPLayout.setVisibility(View.GONE);
			mPhotoQualityLayout.setVisibility(View.GONE);
			mAutoPhotoItem.setVisibility(View.GONE);
			mHandsetLayout.setVisibility(View.GONE);
			mFatigueLayout.setVisibility(View.VISIBLE);
			mImageFlipLayout.setVisibility(View.VISIBLE);
			mParkingSleepLayout.setVisibility(View.VISIBLE);
			mCarrecorderWonderfulLine.setVisibility(View.GONE);
			mWonderfulVideoQualityLayout.setVisibility(View.GONE);
			mVolumeLayout.setVisibility(View.GONE);
			mPowerTimeLayout.setVisibility(View.GONE);
			mVoiceTypeLayout.setVisibility(View.GONE);
		} else if (mIPCName.equals(IPCControlManager.G2_SIGN)
				|| mIPCName.equals(IPCControlManager.T3_SIGN)
				|| mIPCName.equals(IPCControlManager.T3U_SIGN)) {
			if(mIPCName.equals(IPCControlManager.T3U_SIGN)) {
				mISPLayout.setVisibility(View.GONE);
				mRlAntiFlicker.setVisibility(View.VISIBLE);
				if(mIPCName.equals(T3U_SIGN) && getApp().isMainland()){
					mVoiceTypeLayout.setVisibility(View.GONE);
					mRlAntiFlicker.setVisibility(View.GONE);
				}else{
					mVoiceTypeLayout.setVisibility(View.VISIBLE);
					}
				if(GolukApplication.getInstance().mIPCControlManager.isSupportMoveDection()) {
					mMSLayout.setVisibility(View.VISIBLE);
				}
			} else {
				mISPLayout.setVisibility(View.VISIBLE);
				mRlAntiFlicker.setVisibility(View.GONE);
				mVoiceTypeLayout.setVisibility(View.GONE);
			}
			if(mIPCName.equals(T3_SIGN)) {
				mISPLayout.setVisibility(View.GONE);
			}
			if(GolukApplication.getInstance().mIPCControlManager.isSupportVideoLogo()){
				mVideoLogoLayout.setVisibility(View.VISIBLE);
			}
			if(T3U_SIGN.equals(mIPCName) ||T3_SIGN.equals(mIPCName)) {
				mISPLayout.setVisibility(View.GONE);
				if (GolukApplication.getInstance().mIPCControlManager.isSupportMoveDection()) {
					mMSLayout.setVisibility(View.VISIBLE);
				}
			}else{
				mMSLayout.setVisibility(View.GONE);
			}
			mPhotoQualityLayout.setVisibility(View.GONE);
			mAutoPhotoItem.setVisibility(View.GONE);
			mHandsetLayout.setVisibility(View.GONE);
			mFatigueLayout.setVisibility(View.VISIBLE);
			mImageFlipLayout.setVisibility(View.VISIBLE);
			mParkingSleepLayout.setVisibility(View.VISIBLE);
			mCarrecorderWonderfulLine.setVisibility(View.VISIBLE);
			mWonderfulVideoQualityLayout.setVisibility(View.VISIBLE);
			mVolumeLayout.setVisibility(View.GONE);
			mPowerTimeLayout.setVisibility(View.GONE);
			if(mIPCName.equals(IPCControlManager.T3_SIGN) || mIPCName.equals(IPCControlManager.T3U_SIGN)) {
				mVideoLogoLayout.setVisibility(View.VISIBLE);
			}
		}

		// T3??????????????????UI
		if(mIPCName.equals(IPCControlManager.T3_SIGN) || mIPCName.equals(IPCControlManager.T3U_SIGN)) {
			mParkingSleepHintText.setVisibility(View.GONE);
			mParkingSleepLayout.setVisibility(View.GONE);
		}

		mAutoRecordBtn.setBackgroundResource(R.drawable.set_open_btn);
		findViewById(R.id.tcaf).setBackgroundResource(R.drawable.set_close_btn);// ??????
		mStorayeText = (TextView) findViewById(R.id.mStorayeText);
		mVideoText = (TextView) findViewById(R.id.mVideoText);
		mSensitivityText = (TextView) findViewById(R.id.mSensitivityText);

		mStorayeText.setText("0MB/0MB");
		if (mAutoState) {
			mAutoPhotoBtn.setBackgroundResource(R.drawable.set_open_btn);
		} else {
			mAutoPhotoBtn.setBackgroundResource(R.drawable.set_close_btn);
		}
		if (mBaseApp.mIPCControlManager.isSupportTimeslapse()){
			rLayoutTimeLapseVideo.setVisibility(View.VISIBLE);
			rLayoutEmergencyVideoTone.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * ????????????
	 *
	 * @author xuhw
	 * @date 2015???4???6???
	 */
	private void setListener() {
		findViewById(R.id.back_btn).setOnClickListener(this);// ????????????
		findViewById(R.id.mVideoDefinition).setOnClickListener(this);// ????????????
		findViewById(R.id.zdxhlx).setOnClickListener(this);// ????????????????????????
		findViewById(R.id.tcaf).setOnClickListener(this);// ??????????????????
		findViewById(R.id.sylz).setOnClickListener(this);// ????????????
		findViewById(R.id.pzgylmd_line).setOnClickListener(this);// ?????????????????????
		findViewById(R.id.kgjtsy).setOnClickListener(this);// ??????????????????
		findViewById(R.id.hdr).setOnClickListener(this);// HDR??????
		findViewById(R.id.jcsp).setOnClickListener(this);// ???????????????????????????

		findViewById(R.id.rlcx_line).setOnClickListener(this);// ??????????????????
		findViewById(R.id.mFormatSDCard).setOnClickListener(this);// ?????????SD
		findViewById(R.id.sjsz_line).setOnClickListener(this);// ????????????
		findViewById(R.id.hfccsz_line).setOnClickListener(this);// ??????????????????
		findViewById(R.id.bbxx_line).setOnClickListener(this);// ????????????
		findViewById(R.id.mBugLayout).setOnClickListener(this);// ???????????????

		mPhotoQualityLayout.setOnClickListener(this);
		mAutoPhotoBtn.setOnClickListener(this);

		mFatigueBtn.setOnClickListener(this);// ????????????
		mImageFlipBtn.setOnClickListener(this);// ??????????????????
		mParkingSleepBtn.setOnClickListener(this);// ??????????????????
		mHandsetLayout.setOnClickListener(this);// ?????????????????????

		mADASAssistanceBtn.setOnClickListener(this);//ADAS??????????????????
		/**adas???????????? ????????????**/
//		mADASForwardWarningLayout.setOnClickListener(this);//???????????????????????????
//		mADASOffsetWarningLayout.setOnClickListener(this);//???????????????????????????
		mADASFcarCloseBtn.setOnClickListener(this);
		mADASFcarSetupBtn.setOnClickListener(this);
//		mADASOsdBtn.setOnClickListener(this);//??????????????????
		mADASConfigLayout.setOnClickListener(this);//ADAS??????
		mWonderfulVideoQualityLayout.setOnClickListener(this);//??????????????????
		mVolumeLayout.setOnClickListener(this);//???????????????
		mPowerTimeLayout.setOnClickListener(this);//????????????
		mVoiceTypeLayout.setOnClickListener(this);//????????????
		mVideoLogoBtn.setOnClickListener(this);//????????????
		mVideoTypeLayout.setOnClickListener(this);// ??????????????????
		mRlAntiFlicker.setOnClickListener(this); //?????????
		mMoveMotionBtn.setOnClickListener(this);
		mBtnEmergencyVideoHint.setOnClickListener(this);
		mBtnTimeLapse.setOnClickListener(this);
	}

	/**
	 * ???????????????????????????
	 *
	 * @author xuhw
	 * @date 2015???4???8???
	 */
	private void dialog() {
		if (mCustomDialog == null) {
			mCustomDialog = new CustomDialog(this);
		}

		mCustomDialog.setCancelable(false);
		mCustomDialog.setMessage(this.getResources().getString(R.string.str_ipc_dialog_normal));
		mCustomDialog.setLeftButton(this.getResources().getString(R.string.str_button_ok), new OnLeftClickListener() {
			@Override
			public void onClickListener() {
				finish();
			}
		});
		mCustomDialog.show();
	}

	@Override
	public void onClick(View arg0) {
		if (R.id.back_btn == arg0.getId()) {
			finish();
			return;
		}
		if (GolukApplication.getInstance().getIpcIsLogin()) {
			int id = arg0.getId();
			if (id == R.id.mVideoDefinition) {
				click_videQuality();
			} else if (id == R.id.zdxhlx) {
				//showLoading();
				if (recordState) {
					recordState = false;
					mAutoRecordBtn.setBackgroundResource(R.drawable.set_close_btn);
					boolean a = GolukApplication.getInstance().getIPCControlManager().stopRecord();
					GolukDebugUtils.e("xuhw", "video===========stopRecord=============a=" + a);
				} else {
					recordState = true;
					mAutoRecordBtn.setBackgroundResource(R.drawable.set_open_btn);
					boolean b = GolukApplication.getInstance().getIPCControlManager().startRecord();
					GolukDebugUtils.e("xuhw", "video===========startRecord=============b=" + b);
				}
			} else if (id == R.id.tcaf) {
				showLoading();
				if (1 == enableSecurity) {
					enableSecurity = 0;
				} else {
					enableSecurity = 1;
				}
				boolean c = GolukApplication.getInstance().getIPCControlManager()
						.setMotionCfg(enableSecurity, snapInterval);
				GolukDebugUtils.e("xuhw", "YYYYYY===========setMotionCfg==========a=" + c);
			} else if (id == R.id.sylz) {
				click_SoundRecord();
			} else if (id == R.id.pzgylmd_line) {
				Intent pzgylmd_line = new Intent(SettingsActivity.this, ImpactSensitivityActivity.class);
				startActivity(pzgylmd_line);
			} else if (id == R.id.rlcx_line) {
				Intent rlcx_line = new Intent(SettingsActivity.this, StorageCpacityQueryActivity.class);
				startActivity(rlcx_line);
			} else if (id == R.id.sjsz_line) {
				Intent sjsz_line = new Intent(SettingsActivity.this, TimeSettingActivity.class);
				startActivity(sjsz_line);
			} else if (id == R.id.hfccsz_line) {
				click_reset();
			} else if (id == R.id.bbxx_line) {
				Intent bbxx = new Intent(SettingsActivity.this, VersionActivity.class);
				startActivity(bbxx);
			} else if (id == R.id.mBugLayout) {
				Intent mBugLayout = new Intent(this, UserOpenUrlActivity.class);
				mBugLayout.putExtra(UserOpenUrlActivity.FROM_TAG, "buyline");
				startActivity(mBugLayout);
			} else if (id == R.id.hdr) {
				click_hdr();
			} else if (id == R.id.kgjtsy) {
				click_opencloseVoice();
			} else if (id == R.id.jcsp) {
				click_wonderfulVoice();
			} else if(id == R.id.btn_t_settings_video_logo) {//????????????
				click_videoLogo();
			} else if (id == R.id.btn_settings_fatigue) {
				click_Fatigue();
			} else if (id == R.id.btn_settings_image_flip) {
				// ??????????????????
				click_imageFlip();
			}else if (id == R.id.rl_anti_flicker){
				click_antiFlicker();
			} else if (id == R.id.btn_settings_parking_sleep) {
				// ????????????
				click_parkingSleep();
			} else if (id == R.id.handset_line) {
				// ?????????????????????
				click_handset();
			} else if(id == R.id.btn_t_settings_adas_move_motion) {//????????????
				click_move();
			} else if (id == R.id.photographic_quality_line) {
				// ??????????????????
				click_photoQuality();
			} else if (id == R.id.btn_adas_assistance) {
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
							// TODO Auto-generated method stub
							Intent intent = new Intent(SettingsActivity.this, AdasGuideActivity.class);
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
					GolukApplication.getInstance().getIPCControlManager()
							.setT1AdasConfigEnable(mAdasConfigParamter.enable);
				}
			} else if (id == R.id.btn_settings_forward_car_close_warning) {
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
			} else if (id == R.id.btn_settings_forward_car_setup_hint) {
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
			} else if (id == R.id.layout_settings_adas_config) {
				if (mAdasConfigParamter == null) {
					return;
				}
				Intent intent = new Intent(SettingsActivity.this, AdasConfigActivity.class);
				intent.putExtra(AdasVerificationActivity.ADASCONFIGDATA, mAdasConfigParamter);
				startActivity(intent);
			} else if (id == R.id.ib_setup_autophoto_btn) {
				if (mAutoState) {
					mAutoPhotoBtn.setBackgroundResource(R.drawable.set_close_btn);
					mAutoState = false;
				} else {
					mAutoPhotoBtn.setBackgroundResource(R.drawable.set_open_btn);
					mAutoState = true;
				}
				GolukFileUtils.saveBoolean(GolukFileUtils.PROMOTION_AUTO_PHOTO, mAutoState);
			} else if (id == R.id.rl_carrecorder_settings_wonderfulvideo_quality) {
				Intent itWonderful = new Intent(this, SettingsItemActivity.class);
				itWonderful.putExtra(SettingsItemActivity.TYPE, SettingsItemActivity.TYPE_WONDERFUL_VIDEO_QUALITY);
				itWonderful.putExtra(SettingsItemActivity.PARAM, mWonderfulVideoResolution);
				startActivityForResult(itWonderful, REQUEST_CODE_WONDERFUL_VIDEO_QUALITY);
			} else if (id == R.id.rl_settings_tone_line) {
				Intent itVolume = new Intent(this, SettingsItemActivity.class);
				itVolume.putExtra(SettingsItemActivity.TYPE, SettingsItemActivity.TYPE_TONE_VOLUME);
				itVolume.putExtra(SettingsItemActivity.PARAM, mVolume);
				startActivityForResult(itVolume, REQUEST_CODE_TONE_VOLUMN);
			} else if (id == R.id.rl_settings_shutdown_line) {
				Intent itPowerTime = new Intent(this, SettingsItemActivity.class);
				itPowerTime.putExtra(SettingsItemActivity.TYPE, SettingsItemActivity.TYPE_SHUTDOWN_TIME);
				itPowerTime.putExtra(SettingsItemActivity.PARAM, mPowerTime);
				startActivityForResult(itPowerTime, REQUEST_CODE_SHUTDOWN_TIME);
			} else if (id == R.id.rl_settings_language_line) {
				Intent itVoiceType = new Intent(this, SettingsItemActivity.class);
				if(mIPCName.equals(IPCControlManager.T3U_SIGN)){
					itVoiceType.putExtra(SettingsItemActivity.TYPE, SettingsItemActivity.TYPE_LANGUAGE_T);
				}else {
					itVoiceType.putExtra(SettingsItemActivity.TYPE, SettingsItemActivity.TYPE_LANGUAGE);
				}
				itVoiceType.putExtra(SettingsItemActivity.PARAM, mVoiceType);
				startActivityForResult(itVoiceType, REQUEST_CODE_LANGUAGE);
			} else if (id == R.id.ry_settings_wonderful_video_type) {
				Intent itWonderful = new Intent(this, SettingsItemActivity.class);
				itWonderful.putExtra(SettingsItemActivity.TYPE, SettingsItemActivity.TYPE_WONDERFUL_VIDEO_TYPE);
				itWonderful.putExtra(SettingsItemActivity.PARAM, mVideoType);
				startActivityForResult(itWonderful, REQUEST_CODE_WONDERFUL_VIDEO_TYPE);
			} else if (id == R.id.btn_emergency_switch){
				click_EmergencyVideoSound();
			} else if (id == R.id.btn_time_lapse_video) {
				click_Timelapse();
			} else if (id == R.id.mFormatSDCard) {
				showFormatSDDialog();
			}
		} else {
			dialog();
		}
	}

	CustomFormatDialog mCustomFormatDialog;

	private void showFormatSDDialog() {
		CustomDialog dialog = new CustomDialog(this);
		dialog.setMessage(
				this.getResources().getString(R.string.str_carrecorder_storage_format_sdcard_dialog_message),
				Gravity.CENTER);
		dialog.setLeftButton(
				this.getResources().getString(R.string.str_carrecorder_storage_format_sdcard_dialog_yes),
				new OnLeftClickListener() {
					@Override
					public void onClickListener() {
						if (GolukApplication.getInstance().getIpcIsLogin()) {
							boolean flag = GolukApplication.getInstance().getIPCControlManager().formatDisk();
							GolukDebugUtils.e("xuhw", "YYYYYY=====formatDisk===flag=" + flag);
							if (flag) {
								mCustomFormatDialog = new CustomFormatDialog(SettingsActivity.this);
								mCustomFormatDialog.setCancelable(false);
								mCustomFormatDialog.setMessage(SettingsActivity.this.getResources()
										.getString(R.string.str_carrecorder_storage_format_sdcard_formating));
								mCustomFormatDialog.show();
							}
						}
					}
				});
		dialog.setRightButton(
				this.getResources().getString(R.string.str_carrecorder_storage_format_sdcard_dialog_no), null);
		dialog.show();
	}

	private void click_antiFlicker() {
		Intent intentFlicker = new Intent(this, SettingsItemActivity.class);
		intentFlicker.putExtra(SettingsItemActivity.TYPE, SettingsItemActivity.TYPE_ANTI_FLICKER);
		intentFlicker.putExtra(SettingsItemActivity.PARAM, mAntiFlicker);
		startActivityForResult(intentFlicker, REQUEST_CODE_ANTI_FLICKER);
	}

	/**
	 * ???????????????????????????
	 *
	 * @author jyf
	 */
	private void click_wonderfulVoice() {
		showLoading();
		judgeSwitch = false;
		// ????????????
		int wonderfulStatus = 1;
		if (0 == mWonderfulSwitchStatus) {
			wonderfulStatus = 1;
		} else {
			wonderfulStatus = 0;
		}
		String json = JsonUtil.getSpeakerSwitchJson(speakerSwitch, wonderfulStatus);
		boolean setWonderful = GolukApplication.getInstance().getIPCControlManager().setIPCSwitchState(json);
		GolukDebugUtils.e("", "----------setWonderfulMode-----setWonderful???" + setWonderful);
		if (!setWonderful) {
			closeLoading();
			GolukUtils.showToast(this, getResources().getString(R.string.str_carrecoder_setting_failed));
		}
	}

	/**
	 * ??????????????????
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
		GolukDebugUtils.e("lily", "---------??????????????????-----------" + b);
		if (!b) {
			closeLoading();
			GolukUtils.showToast(this, getResources().getString(R.string.str_carrecoder_setting_failed));
		}
	}

	/**
	 * HDR??????
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
		GolukDebugUtils.e("", "----------setISPMode-----setISP???" + setISP);
		if (!setISP) {
			closeLoading();
			GolukUtils.showToast(this, getResources().getString(R.string.str_carrecoder_setting_failed));
		}
	}

	/**
	 * ??????????????????
	 *
	 * @author jyf
	 */
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
							WifiBindDataCenter.getInstance().deleteBindData(WiFiInfo.IPC_SSID);
							EventBindFinish eventFnish = new EventBindFinish(EventConfig.BIND_LIST_DELETE_CONFIG);
							EventBus.getDefault().post(eventFnish);
							WiFiInfo.clear();
							boolean a = GolukApplication.getInstance().getIPCControlManager().restoreIPC();
							GolukDebugUtils.e("xuhw", "YYYYYY=================restoreIPC============a=" + a);
						}
					}
				});
		mCustomDialog.setRightButton(this.getResources().getString(R.string.dialog_str_cancel), null);
		mCustomDialog.show();
	}

	/**
	 * ??????????????????
	 *
	 * @author jyf
	 */
	private void click_videQuality() {
		Intent mVideoDefinition = new Intent(SettingsActivity.this, VideoQualityActivity.class);
		startActivity(mVideoDefinition);
	}

	/**
	 * ????????????
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
	 * ??????????????????
	 *
	 * @author jyf
	 */
	private void click_imageFlip() {
		if (IPCControlManager.T1_SIGN.equals(GolukApplication.getInstance().getIPCControlManager().mProduceName)
				|| IPCControlManager.T2_SIGN.equals(GolukApplication.getInstance().getIPCControlManager().mProduceName)) {
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
	 * ????????????
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

	/** UI?????? **/
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
	 * ?????????????????????
	 *
	 * @author jyf
	 */
	private void click_handset() {
		Intent intent = new Intent(this, CarrecoderKitSettingActivity.class);
		intent.putExtra("record", record);
		intent.putExtra("snapshot", snapshot);
		this.startActivityForResult(intent, REQUEST_CODE_KIT);
	}

	// ??????????????????
	private void click_photoQuality() {
		Intent intent = new Intent(this, PhotoQualityActivity.class);
		intent.putExtra("photoselect", mCurrentResolution);
		this.startActivityForResult(intent, REQUEST_CODE_PHOTO);
	}

	/** T1????????? ???????????? ?????? */
	private int state_soundRecord_T1 = 0;

	private void click_SoundRecord() {
		if (IPCControlManager.T1_SIGN.equals(GolukApplication.getInstance().getIPCControlManager().mProduceName)
				|| IPCControlManager.T2_SIGN.equals(GolukApplication.getInstance().getIPCControlManager().mProduceName)) {
			state_soundRecord_T1 = state_soundRecord_T1 == 0 ? 1 : 0;
			GolukApplication.getInstance().setT1VideoCfgState(state_soundRecord_T1);
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
					mAudioBtn.setBackgroundResource(R.drawable.set_close_btn);
				} else {
					mVideoConfigState.AudioEnabled = 1;
					mAudioBtn.setBackgroundResource(R.drawable.set_open_btn);
				}
//				boolean a = GolukApplication.getInstance().getIPCControlManager().setAudioCfg(mVideoConfigState);
//				if (a) {
//					showLoading();
//				} else {
//					GolukUtils.showToast(this, getResources().getString(R.string.str_carrecoder_setting_failed));
//				}
			}
		}

	}
	/**
	 * ???????????????????????????????????????
	 */
	private void callback_getEmergencyVideoSound(int event, int msg, int param1, Object param2) {
		GolukDebugUtils.e("", "TSettingsActivity-----------callback_getEmgVideoSound-----param2: " + param2);
		closeLoading();
		if (RESULE_SUCESS == param1) {
			try {
				JSONObject obj = new JSONObject((String) param2);
				mEmgVideoSoundState = Integer.parseInt(obj.optString("urgentSwitch"));
				if (STATE_CLOSE != mEmgVideoSoundState) {
					mEmgVideoSoundState = STATE_OPEN;
				}
				refreshUI_emergencyVideoSound(mEmgVideoSoundState);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	/**
	 * ???????????????????????????????????????
	 */
	private void callback_setEmergencyVideoSound(int param1) {
		closeLoading();
		if (RESULE_SUCESS == param1) {
			GolukApplication.getInstance().getIPCControlManager().getEmgVideoSoundCfg();
		}
	}

	private void click_EmergencyVideoSound() {
		mEmgVideoSoundState = mEmgVideoSoundState == 0 ? 1 : 0;
		//GolukApplication.getInstance().setT1VideoCfgState(mEmgVideoSoundState);
		boolean isSuccess = GolukApplication.getInstance().getIPCControlManager().setEmgVideoSoundCfg(mEmgVideoSoundState);
		if (isSuccess) {
			showLoading();
		} else {
			GolukUtils.showToast(this, getResources().getString(R.string.str_carrecoder_setting_failed));
		}
	}


	/**
	 * ???????????????????????????????????????
	 */
	private void refreshUI_emergencyVideoSound(int state) {
		mBtnEmergencyVideoHint.setBackgroundResource(state == STATE_OPEN ? R.drawable.set_open_btn : R.drawable.set_close_btn);
	}

	/**
	 * ??????????????????????????????
	 */
	private void refreshUI_timelapse(int state) {
		mBtnTimeLapse.setBackgroundResource(state == STATE_OPEN ? R.drawable.set_open_btn : R.drawable.set_close_btn);
	}

	private void click_Timelapse(){
		mTimeslapseState = mTimeslapseState==0?1:0;
		boolean isSuccess = GolukApplication.getInstance().getIPCControlManager().setTimelapseCfg(mTimeslapseState);
		if (isSuccess){
			showLoading();
		}else {
			GolukUtils.showToast(null,getString(R.string.str_carrecoder_setting_failed));
		}
	}

	private void callback_getTimelapse(int event, int msg, int param1, Object param2){
		closeLoading();
		if (RESULE_SUCESS == param1) {
			try {
				JSONObject obj = new JSONObject((String) param2);
				mTimeslapseState = Integer.parseInt(obj.optString("timelapse"));
				if (STATE_CLOSE != mTimeslapseState) {
					mTimeslapseState = STATE_OPEN;
				}
				refreshUI_timelapse(mTimeslapseState);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	/*??????????????????????????????*/
	private void callback_setTimelapseVideo(int param1) {
		closeLoading();
		if (RESULE_SUCESS == param1) {
			GolukApplication.getInstance().getIPCControlManager().getTimelapseCfg();
		}
	}
	/*???????????????*/
	private void callback_getCollisionControlCfg(int msg, int param1, Object param2) {
		if (param1 == RESULE_SUCESS) {
			try {
				JSONObject json = new JSONObject((String) param2);
				int collisionValue = json.optInt("collisionValue");
				int textResId = IpcSettingUtil.getCollisionTextResIdByValue(collisionValue);
				if (textResId != -1)
					mSensitivityText.setText(textResId);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		GolukApplication.getInstance().setContext(this, "carrecordsettings");
		mVideoConfigState = GolukApplication.getInstance().getVideoConfigState();
		int t1VideoCfg = GolukApplication.getInstance().getT1VideoCfgState();
		if (IPCControlManager.T1_SIGN.equals(GolukApplication.getInstance().getIPCControlManager().mProduceName)
				|| IPCControlManager.T2_SIGN.equals(GolukApplication.getInstance().getIPCControlManager().mProduceName)) {
			refreshUI_soundRecod(t1VideoCfg);
		} else {
			if (null != mVideoConfigState) {
				refreshUI_soundRecod(mVideoConfigState.AudioEnabled);
			} else {
				mAudioBtn.setBackgroundResource(R.drawable.set_close_btn);
			}
		}
		if (null != mVideoConfigState) {
			setData2UI();
		}

	}

	// ???????????????????????????????????????UI
	private void setData2UI() {
		if (null != mVideoConfigState && null != mResolutionArray && null != mBitrateArray) {
			for (int i = 0; i < mResolutionArray.length; i++) {
				if (mVideoConfigState.resolution.equals(mResolutionArray[i])) {
					if (String.valueOf(mVideoConfigState.bitrate).equals(mBitrateArray[i])) {
						GolukDebugUtils.e("", "---------SettingsActivity-------mArrayText???" + mArrayText[i]);
						mVideoText.setText(mArrayText[i]);
						break;
					}
				}
			}

		}
	}

	@Override
	protected void onDestroy() {
		if (null != GolukApplication.getInstance().getIPCControlManager()) {
			GolukApplication.getInstance().getIPCControlManager().removeIPCManagerListener("settings");
		}
		EventBus.getDefault().unregister(this);
		closeLoading();
		if (mCustomDialog != null && mCustomDialog.isShowing()) {
			mCustomDialog.dismiss();
		}
		mCustomDialog = null;

		if (null != mRestartDialog) {
			mRestartDialog.dismiss();
			mRestartDialog = null;
		}
		super.onDestroy();
		XLog.tag(LogConst.TAG_SETTING).i("Leave IPC setting page.");
	}

	@Override
	public void IPCManage_CallBack(int event, int msg, int param1, Object param2) {
		GolukDebugUtils.e("jyf", "YYYYYYY----IPCManage_CallBack-----------event:" + event + " msg:" + msg + "  param1:"
				+ param1 + "==data:" + (String) param2);
		if (event == ENetTransEvent_IPC_VDCP_CommandResp) {

			XLog.tag(LogConst.TAG_SETTING).i("IPCManage_CallBack. msg:%s, param1:%s, param2:%s", msg, param1, (String) param2);

			if (msg == IPC_VDCP_Msg_GetRecordState) {// ??????IPC????????????????????????
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
					// ????????????????????????
					mAutoRecordBtn.setBackgroundResource(R.drawable.set_close_btn);
				}
			} else if (msg == IPC_VDCP_Msg_StartRecord) {// ??????IPC????????????????????????
				closeLoading();
				if (RESULE_SUCESS == param1) {
					recordState = true;
					mAutoRecordBtn.setBackgroundResource(R.drawable.set_open_btn);
				}
			} else if (msg == IPC_VDCP_Msg_StopRecord) {// ??????IPC????????????????????????
				closeLoading();
				if (RESULE_SUCESS == param1) {
					recordState = false;
					mAutoRecordBtn.setBackgroundResource(R.drawable.set_close_btn);
				}
			} else if (msg == IPC_VDCP_Msg_GetVedioEncodeCfg) {// ??????IPC???????????????????????????
				if (RESULE_SUCESS == param1) {
					mVideoConfigState = IpcDataParser.parseVideoConfigState((String) param2);
					// updateVideoQualityText();
					setData2UI();
					if (IPCControlManager.T1_SIGN
							.equals(GolukApplication.getInstance().getIPCControlManager().mProduceName)
							|| IPCControlManager.T2_SIGN.equals(GolukApplication.getInstance().getIPCControlManager().mProduceName)) {
						return;
					}
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
			} else if (msg == IPC_VDCP_Msg_SetVedioEncodeCfg) {// ??????IPC???????????????????????????
				closeLoading();
				if (RESULE_SUCESS == param1) {

				}
			} else if (msg == IPC_VDCP_Msg_GetMotionCfg) {// ???????????????????????????????????????
				closeLoading();
				getMotionCfg = true;
				checkGetState();
				if (RESULE_SUCESS == param1) {
					try {
						JSONObject json = new JSONObject((String) param2);
						if (null != json) {
							enableSecurity = json.getInt("enableSecurity");
							snapInterval = json.getInt("snapInterval");
							if (1 == enableSecurity) {
								findViewById(R.id.tcaf).setBackgroundResource(R.drawable.set_open_btn);// ??????
								if (GolukApplication.getInstance().mIPCControlManager.isSupportMoveDection()) {
									mMSLayout.setVisibility(View.VISIBLE);
								}
								if (mBaseApp.mIPCControlManager.isSupportTimeslapse()) {
									rLayoutTimeLapseVideo.setVisibility(View.VISIBLE);
								}
								if (1 == dormant) {
									dormant = 0;
									// ??????????????????
									boolean fatigue = GolukApplication.getInstance().getIPCControlManager()
											.setFunctionMode(getSetJson());
								}
							} else {
								findViewById(R.id.tcaf).setBackgroundResource(R.drawable.set_close_btn);// ??????
								mMSLayout.setVisibility(View.GONE);
								rLayoutTimeLapseVideo.setVisibility(View.GONE);
							}
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			} else if (msg == IPC_VDCP_Msg_SetMotionCfg) {// ???????????????????????????????????????
				closeLoading();
				if (RESULE_SUCESS == param1) {
					if (1 == enableSecurity) {
						findViewById(R.id.tcaf).setBackgroundResource(R.drawable.set_open_btn);// ??????
						if (GolukApplication.getInstance().mIPCControlManager.isSupportMoveDection()) {
							mMSLayout.setVisibility(View.VISIBLE);
						}
						if (mBaseApp.mIPCControlManager.isSupportTimeslapse()) {
							rLayoutTimeLapseVideo.setVisibility(View.VISIBLE);
						}
						// TODO ????????????????????????
						if (1 == dormant) {
							dormant = 0;
							// ??????????????????
							boolean fatigue = GolukApplication.getInstance().getIPCControlManager()
									.setFunctionMode(getSetJson());
						}
					} else {
						findViewById(R.id.tcaf).setBackgroundResource(R.drawable.set_close_btn);// ??????
						mMSLayout.setVisibility(View.GONE);
						rLayoutTimeLapseVideo.setVisibility(View.GONE);
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
							mSensitivityText.setText(this.getResources().getString(R.string.carrecorder_tcaf_close));
						} else if (1 == policy) {
							mSensitivityText.setText(this.getResources().getString(R.string.str_low));
						} else if (2 == policy) {
							mSensitivityText.setText(this.getResources().getString(R.string.str_middle));
						} else {
							mSensitivityText.setText(this.getResources().getString(R.string.str_high));
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
				IPCCallBack_Restore(msg, param1, param2);
			} else if (msg == IPC_VDCP_Msg_GetSpeakerSwitch) {// ??????ipc?????????????????????
				closeLoading();
				GolukDebugUtils.e("lily", "------IPC_VDCPCmd_GetSpeakerSwitch----------------param1:" + param1
						+ "----param2:--" + param2);
				if (param1 == RESULE_SUCESS) {
					try {
						JSONObject json = new JSONObject((String) param2);
						GolukDebugUtils.e("lily", "---------IPC_VDCPCmd_GetSpeakerSwitch--------" + json);
						// {"SpeakerSwitch":0} 0?????? 1??????
						speakerSwitch = json.optInt("SpeakerSwitch");
						mWonderfulSwitchStatus = json.optInt("WonderfulSwitch");
						if (0 == speakerSwitch) {
							mSwitchBtn.setBackgroundResource(R.drawable.set_close_btn);
						} else {
							mSwitchBtn.setBackgroundResource(R.drawable.set_open_btn);
						}
						// ??????????????????????????????
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
			} else if (msg == IPC_VDCP_Msg_SetSpeakerSwitch) {// ??????ipc?????????????????????
				closeLoading();
				GolukDebugUtils.e("lily", "-------IPC_VDCPCmd_SetSpeakerSwitch------msg-----" + msg
						+ "-------param2------" + param2 + "---param1:--" + param1);
				if (RESULE_SUCESS == param1) {
					if (judgeSwitch) {
						// ????????????????????????
						if (0 == speakerSwitch) {
							speakerSwitch = 1;
							mSwitchBtn.setBackgroundResource(R.drawable.set_open_btn);
						} else {
							speakerSwitch = 0;
							mSwitchBtn.setBackgroundResource(R.drawable.set_close_btn);
						}
					} else {
						// ???????????????????????????
						if (0 == mWonderfulSwitchStatus) {
							mWonderfulSwitchStatus = 1;
							mWonderVideoBtn.setBackgroundResource(R.drawable.set_open_btn);
						} else {
							mWonderfulSwitchStatus = 0;
							mWonderVideoBtn.setBackgroundResource(R.drawable.set_close_btn);
						}
					}
					GolukUtils.showToast(this, this.getResources().getString(R.string.str_set_ok));
				} else {
					GolukUtils.showToast(this, this.getResources().getString(R.string.str_ipc_unmatch));
				}
			} else if (msg == IPC_VDCP_Msg_GetISPMode) {
				getISPModeCallback(event, msg, param1, param2);
			} else if (msg == IPC_VDCP_Msg_SetISPMode) {
				setISPModeCallback(event, msg, param1, param2);
			} else if (msg == IPC_VDCP_Msg_GetPicCfg) {// ??????????????????
				IPCCallBackGetPhotoQuality(event, msg, param1, param2);
			} else if (msg == IPC_VDCP_Msg_SetPicCfg) {// ??????????????????
				IPCCallBackSetPhotoQuality(event, msg, param1, param2);
			} else if (msg == IPC_VDCP_Msg_GetOSDConf) {// ????????????
				callback_getVideoLogo(msg, param1, param2);
			} else if (msg == IPC_VDCP_Msg_SetOSDConf) {
				callback_setVideoLogo(msg, param1, param2);
			} else if (msg == IPC_VDCP_Msg_GetFunctionSwitch) {// ????????????????????????????????????????????????????????????
				getFunctionCallback(event, msg, param1, param2);
			} else if (msg == IPC_VDCP_Msg_SetFunctionSwitch) {// ????????????????????????????????????????????????????????????
				setFunctionCallback(event, msg, param1, param2);
			} else if (msg == IPC_VDCP_Msg_GetKitCfg) {// ???????????????????????????
				getKitConfigCallback(event, msg, param1, param2);
			} else if (msg == IPC_VDCP_Msg_SetKitCfg) {// ???????????????????????????
				setKitConfigCallback(event, msg, param1, param2);
			} else if (IPC_VDCP_Msg_GetRecAudioCfg == msg) {//????????????
				IPCCallBack_getRecAudioCfg(msg, param1, param2);
			} else if (IPC_VDCP_Msg_SetRecAudioCfg == msg) {
				IPCCallBack_setRecAudioCfg(msg, param1, param2);
			} else if (msg == IPC_VDCP_Msg_GetAutoRotationCfg) {// ??????T1??????????????????
				getT1AutoRotaingCallback(msg, param1, param2);
			} else if (msg == IPC_VDCP_Msg_SetAutoRotationCfg) {// ??????T1??????????????????
				setT1AutoRotaingCallback(msg, param1, param2);
			} else if (msg == IPC_VDCP_Msg_GetADASConfig) {
				if (RESULE_SUCESS == param1) {
					if (!TextUtils.isEmpty((String) param2)) {
						mAdasConfigParamter = JSON.parseObject((String)param2, AdasConfigParamterBean.class);
						switchAdasEnableUI(mAdasConfigParamter.enable == 1);
					}
				} else {
					mADASAssistanceLayout.setVisibility(View.GONE);
				}
			} else if (msg == IPC_VDCP_Msg_SetADASConfig){
				if (GolukApplication.getInstance().getContext() != this) {
					return;
				}
				closeLoading();
				if (RESULE_SUCESS == param1) {
					GolukFileUtils.saveInt(GolukFileUtils.ADAS_FLAG, mAdasConfigParamter.enable);
					switchAdasEnableUI(mAdasConfigParamter.enable == 1);
				} else {
					mAdasConfigParamter.enable = (Integer) mADASAssistanceBtn.getTag();
					mAdasConfigParamter.fcs_enable = (Integer) mADASFcarSetupBtn.getTag();
					mAdasConfigParamter.fcw_enable = (Integer) mADASFcarCloseBtn.getTag();
//					mAdasConfigParamter.osd = (Integer) mADASOsdBtn.getTag();
				}
			} else if (msg == IPC_VDCP_Msg_GetVideoResolution) {// ????????????????????????
				getVideoResolutionCallback(event, msg, param1, param2);
			} else if (msg == IPC_VDCP_Msg_SetVideoResolution) {// ????????????????????????
				setVideoResolutionCallback(event, msg, param1, param2);
			} else if (msg == IPC_VDCP_Msg_GetVolume) {// ???????????????????????????
				getVolumeCallback(event, msg, param1, param2);
			} else if (msg == IPC_VDCP_Msg_SetVolume) {// ???????????????????????????
				setVolumeCallback(event, msg, param1, param2);
			} else if (msg == IPC_VDCP_Msg_GetPowerOffTime) {// ??????????????????
				getPowerOffTimeCallback(event, msg, param1, param2);
			} else if (msg == IPC_VDCP_Msg_SetPowerOffTime) {// ??????????????????
				setPowerOffTimeCallback(event, msg, param1, param2);
			} else if (msg == IPC_VDCP_Msg_GetVoiceType) {// ????????????
				getVoiceTypeCallback(event, msg, param1, param2);
			} else if (msg == IPC_VDCP_Msg_SetVoiceType) {// ????????????
				setVoiceTypeCallback(event, msg, param1, param2);
			} else if (msg == IPC_VDCP_Msg_GetVideoTimeConf) {// ??????????????????
				callback_getWonderfulVideoType(event, msg, param1, param2);
			} else if (msg == IPC_VDCP_Msg_SetVideoTimeConf) {
				callback_setWonderfulVideoType(event, msg, param1, param2);
			} else if (msg == IPC_VDCP_Msg_GetDeflickerMode) {
				callback_getAntiFlicker(param1, param2);
			} else if( msg == IPC_VDCP_Msg_SetDeflickerMode ){
				if (param1 == RESULE_SUCESS) {
					GolukApplication.getInstance().getIPCControlManager().getAntiFlicker();
				}
			}else if (msg == IPC_VDCP_Msg_GetUrgentVoiceConf){//??????????????????????????????
				callback_getEmergencyVideoSound(event, msg, param1, param2);
			}else if (msg == IPC_VDCP_Msg_SetUrgentVoiceConf){//??????????????????????????????
				callback_setEmergencyVideoSound(param1);
			}else if (msg == IPC_VDCP_Msg_GetTimelapseConf){//??????????????????????????????
				callback_getTimelapse(event, msg, param1, param2);
			}else if (msg == IPC_VDCP_Msg_SetTimelapseConf){//??????????????????????????????
				callback_setTimelapseVideo(param1);
			}else if (msg == IPC_VDCPCmd_GetCollisionValueConf){//???????????????????????????
				callback_getCollisionControlCfg(msg,param1,param2);
			}else if (msg == IPC_VDCP_Msg_SetCollisionValueConf){//???????????????????????????
				GolukApplication.getInstance().getIPCControlManager().getGSensorMoreValueCfg();
			} else if (msg == IPC_VDCP_Msg_FormatDisk) {
				if (null != mCustomFormatDialog && mCustomFormatDialog.isShowing()) {
					mCustomFormatDialog.dismiss();
				}
				GolukDebugUtils.e("xuhw", "YYYYYY====IPC_VDCP_Msg_FormatDisk====msg=" + msg + "===param1=" + param1
						+ "==param2=" + param2);
				String message = "";
				if (param1 == RESULE_SUCESS) {
					message = this.getResources().getString(R.string.str_carrecorder_storage_format_sdcard_success);
					GolukApplication.getInstance().getIPCControlManager().queryRecordStorageStatus();
				} else {
					message = this.getResources().getString(R.string.str_carrecorder_storage_format_sdcard_fail);
				}
				CustomDialog dialog = new CustomDialog(this);
				dialog.setMessage(message, Gravity.CENTER);
				dialog.setLeftButton(this.getResources().getString(R.string.user_repwd_ok), null);
				dialog.show();
			}
		} else if (msg == IPC_VDCP_Msg_Reboot) {// ??????IPC
				GolukDebugUtils.e("", "SettingsActivity-----------IPC_VDCP_Msg_Reboot-----param2: " + param2);
			}
		}

	private void callback_getAntiFlicker( int param1, Object param2) {
		GolukDebugUtils.e("", "SettingsActivity-----------callback_getAntiFlicker-----param2: " + param2);
		if (RESULE_SUCESS == param1) {
			try {
				JSONObject json = new JSONObject((String) param2);
				int mode = json.getInt("mode");
				if (mode == 0){
					mTvAnti.setText(R.string.anti_flicker_50hz);
				} else if(mode == 1){
					mTvAnti.setText(R.string.anti_flicker_60hz);
				}
				mAntiFlicker = String.valueOf(mode);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}


	/**
	 * ??????????????????
	 *
	 * @author jyf
	 */
	private void IPCCallBack_Restore(int msg, int param1, Object param2) {
		String message = "";
		if (param1 == RESULE_SUCESS) {
			// ????????????
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

	private void restoreSuccess() {
		EventBindFinish eventFnish = new EventBindFinish(EventConfig.BIND_LIST_DELETE_CONFIG);
		EventBus.getDefault().post(eventFnish);
		GolukApplication.getInstance().setIpcDisconnect();
		WifiBindHistoryBean bean = WifiBindDataCenter.getInstance().getCurrentUseIpc();
		if (null != bean) {
			WifiBindDataCenter.getInstance().deleteBindData(bean.ipc_ssid);
		}
	}

	private void IPCCallBack_setRecAudioCfg(int msg, int param1, Object param2) {
		closeLoading();
		// ?????????????????????????????????????????????????????????
		if (RESULE_SUCESS != param1) {
			GolukUtils.showToast(this, this.getResources().getString(R.string.str_carrecoder_setting_failed));
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

	private void callback_setMotionSW() {
		if (1 == moveMonitor) {
			moveMonitor = 0;
			mMoveMotionBtn.setBackgroundResource(R.drawable.set_close_btn);// ??????
		} else {
			moveMonitor = 1;
			mMoveMotionBtn.setBackgroundResource(R.drawable.set_open_btn);// ??????
		}
	}

	private void getMotionSW() {
		if (1 == moveMonitor) {
			mMoveMotionBtn.setBackgroundResource(R.drawable.set_open_btn);// ??????
		}
		else {
			mMoveMotionBtn.setBackgroundResource(R.drawable.set_close_btn);// ??????
		}
	}

	private void click_move() {
		mSetMove = true;
		boolean c = GolukApplication.getInstance().getIPCControlManager().setT1SW(String.valueOf(moveMonitor==0?1:0));
		if(c) {
			showLoading();
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
	 * ??????HDR??????
	 *
	 * @param event
	 * @param msg
	 * @param param1
	 * @param param2
	 */
	private void getISPModeCallback(int event, int msg, int param1, Object param2) {
		GolukDebugUtils.e("", "--------getISPModeCallback------event???" + event + "------msg???" + msg + "-----param1???"
				+ param1 + "------param2???" + param2);
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
	 * ??????HDR??????
	 *
	 * @param event
	 * @param msg
	 * @param param1
	 * @param param2
	 */
	private void setISPModeCallback(int event, int msg, int param1, Object param2) {
		GolukDebugUtils.e("", "--------setISPModeCallback------event???" + event + "------msg???" + msg + "-----param1???"
				+ param1 + "------param2???" + param2);
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
		GolukDebugUtils.e("", "----IPCManage_CallBack------PhotoQuality----------event:" + event + " msg:" + msg + "==data:"
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


	/**
	 * ??????????????????
	 * @param msg
	 * @param param1
	 * @param param2
	 */
	private void callback_getVideoLogo(int msg, int param1, Object param2) {
		GolukDebugUtils.e("", "TSettingsActivity-----------callback_getVideoLogo-----param2: " + param2);
		closeLoading();
		if (RESULE_SUCESS == param1) {
			WonderfulVideoDisplay videoLogo = GolukFastJsonUtil.getParseObj((String) param2, WonderfulVideoDisplay.class);
			if (null != videoLogo) {
				mDisplay = videoLogo;
				if (0 == mDisplay.logo_visible) {
					mVideoLogoBtn.setBackgroundResource(R.drawable.set_close_btn);
				} else {
					mVideoLogoBtn.setBackgroundResource(R.drawable.set_open_btn);
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

	private void setFunctionCallback(int event, int msg, int param1, Object param2) {
		closeLoading();
		GolukDebugUtils.e("", "----IPCManage_CallBack------new----------event:" + event + " msg:" + msg + "==data:"
				+ (String) param2 + "---param1:" + param1);
		if (RESULE_SUCESS == param1) {
			if(mSetMove){
				callback_setMotionSW();
				mSetMove = false;
				return;
			}
			if (1 == dormant) {
				mParkingSleepBtn.setBackgroundResource(R.drawable.set_open_btn);
				if (1 == enableSecurity) {
					enableSecurity = 0;
					// TODO ??????????????????
					boolean c = GolukApplication.getInstance().getIPCControlManager()
							.setMotionCfg(enableSecurity, snapInterval);
					GolukDebugUtils.e("", "===========setMotionCfg===========c:" + c);
				}
			} else {
				mParkingSleepBtn.setBackgroundResource(R.drawable.set_close_btn);
			}
			if (1 == driveFatigue) {
				mFatigueBtn.setBackgroundResource(R.drawable.set_open_btn);
			} else {
				mFatigueBtn.setBackgroundResource(R.drawable.set_close_btn);
			}

			if (!IPCControlManager.T1_SIGN.equals(GolukApplication.getInstance().getIPCControlManager().mProduceName)
					&& !IPCControlManager.T2_SIGN.equals(GolukApplication.getInstance().getIPCControlManager().mProduceName)) {
				if (1 == autoRotation) {
					mImageFlipBtn.setBackgroundResource(R.drawable.set_open_btn);
				} else {
					mImageFlipBtn.setBackgroundResource(R.drawable.set_close_btn);
				}
			}
		} else {
			GolukUtils.showToast(this, this.getResources().getString(R.string.str_carrecoder_setting_failed));
		}
	}

	private void getKitConfigCallback(int event, int msg, int param1, Object param2) {
		GolukDebugUtils.e("", "----IPCManage_CallBack------new----------event:" + event + " msg:" + msg + "==data:"
				+ (String) param2 + "---param1:" + param1);
		if (param2 == null) {
			finish();
			return;
		}
		parseKitJson((String) param2);
		refreshKitUi();
	}

	private void setKitConfigCallback(int event, int msg, int param1, Object param2) {
		GolukDebugUtils.e("", "----IPCManage_CallBack------new----------event:" + event + " msg:" + msg + "==data:"
				+ (String) param2 + "---param1:" + param1);

		if (RESULE_SUCESS == param1) {
			GolukApplication.getInstance().getIPCControlManager().getKitMode();
		} else {
			GolukUtils.showToast(this, this.getResources().getString(R.string.str_carrecoder_setting_failed));
		}
	}

	int recbySec = 0;
	int moveMonitor = 0;
	// ????????????
	int dormant = 0;
	int recLight = 0;
	int wifiLight = 0;
	int securityLight = 0;
	// ????????????
	int autoRotation = 0;
	// ????????????
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
				if (1 == enableSecurity) {
					enableSecurity = 0;
					// TODO ??????????????????
					boolean c = GolukApplication.getInstance().getIPCControlManager()
							.setMotionCfg(enableSecurity, snapInterval);
					GolukDebugUtils.e("", "===========setMotionCfg===========c:" + c);
				}
			} else {
				mParkingSleepBtn.setBackgroundResource(R.drawable.set_close_btn);
			}

			if (1 == driveFatigue) {
				mFatigueBtn.setBackgroundResource(R.drawable.set_open_btn);
			} else {
				mFatigueBtn.setBackgroundResource(R.drawable.set_close_btn);
			}
			getMotionSW();
			if (!IPCControlManager.T1_SIGN.equals(GolukApplication.getInstance().getIPCControlManager().mProduceName)
					&& !IPCControlManager.T2_SIGN.equals(GolukApplication.getInstance().getIPCControlManager().mProduceName)) {
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
	/** ????????????????????? */
	private IPCSettingPhotoBean mPhtoBean = null;
	private String mCurrentResolution = "";
	/** ?????????????????? */
	private String[] mPhotoText = null;
	private String[] mPhotoValue = null;
	/** ?????????????????? **/
	private String[] mWonderfulVideo = null;
	private String[] mWonderfulVideoValue = null;
	/** ????????????????????? **/
	private String[] mVolumeList = null;
	private String[] mVolumeValue = null;
	/** ???????????? **/
	private String[] mPowerTimeList = null;
	/** ?????? **/
	private String[] mVoiceTypeList = null;

	private void loadRes() {
		mPhotoText = getResources().getStringArray(R.array.list_photo_quality_ui);
		mPhotoValue = getResources().getStringArray(R.array.list_photo_quality_list);
		mWonderfulVideo = getResources().getStringArray(R.array.list_wonderful_video_quality);
		mWonderfulVideoValue = getResources().getStringArray(R.array.list_wonderful_video_quality_value);
		mVolumeList = getResources().getStringArray(R.array.list_tone_volume);
		mVolumeValue = getResources().getStringArray(R.array.list_tone_volume_value);
		mPowerTimeList = getResources().getStringArray(R.array.list_shutdown_time);
		if (mIPCName.equals(IPCControlManager.T3U_SIGN)) {
			mVoiceTypeList = getResources().getStringArray(R.array.list_language_t);
		}else{
			mVoiceTypeList = getResources().getStringArray(R.array.list_language);
		}
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
	 * ????????????????????????
	 *
	 * @param size
	 *            ????????????
	 * @return
	 * @author xuhw
	 * @date 2015???4???11???
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
			mCustomProgressDialog.setListener(this);
		}
	}

	private void closeLoading() {
		if (mCustomProgressDialog.isShowing()) {
			mCustomProgressDialog.close();
		}
	}
	/**
	 *?????????IPC
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
									mRestartDialog.dismiss();
									mRestartDialog = null;
								}
							})
					.setPositiveButton(this.getString(R.string.str_reboot_ipc_now),
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface arg0, int arg1) {
									boolean reboot = GolukApplication.getInstance().getIPCControlManager()
											.setIPCReboot();
									if (reboot) {
										mRestartDialog.dismiss();
										mRestartDialog = null;
									}
								}
							}).setCancelable(false).show();
		}
	}

	@Override
	protected void hMessage(Message msg) {
		if (100 == msg.what) {
			restoreSuccess();
		}
	}

	// public void exit() {
	// if (null != GolukApplication.getInstance().getIPCControlManager()) {
	// GolukApplication.getInstance().getIPCControlManager().removeIPCManagerListener("settings");
	// }
	// closeLoading();
	// finish();
	// }

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			finish();
			return true;
		} else
			return super.onKeyDown(keyCode, event);
	}

	@Override
	public void forbidBackKey(int backKey) {
		if (1 == backKey) {
			finish();
		}
		mSetMove = false;
	}

	public void onEventMainThread(EventAdasConfigStatus event) {
		if (event == null) {
			return;
		}

		mAdasConfigParamter = event.getData();
		switchAdasEnableUI(true);
	}

	/**
	 * ????????????????????????
	 *
	 * @param event
	 * @param msg
	 * @param param1
	 * @param param2
	 */
	private void getVideoResolutionCallback(int event, int msg, int param1, Object param2) {
		GolukDebugUtils.e("", "--------getVideoResolutionCallback------event???" + event + "------msg???" + msg
				+ "-----param1???" + param1 + "------param2???" + param2);
		if (RESULE_SUCESS == param1) {
			try {
				JSONObject json = new JSONObject((String) param2);
				mWonderfulVideoResolution = json.getString("wonderful_resolution");
				mSaveLastResolution = mWonderfulVideoResolution;
				refreshWonderfulVideoData();
			} catch (Exception e) {
				mWonderfulVideoResolution = "1080P";
				mTextWonderfulVideoQualityText.setText(mWonderfulVideoResolution);
			}
		}
	}

	/**
	 * ????????????????????????
	 *
	 * @param event
	 * @param msg
	 * @param param1
	 * @param param2
	 */
	private void setVideoResolutionCallback(int event, int msg, int param1, Object param2) {
		GolukDebugUtils.e("", "--------setVideoResolutionCallback------event???" + event + "------msg???" + msg
				+ "-----param1???" + param1 + "------param2???" + param2);
		closeLoading();
		if (RESULE_SUCESS == param1) {
//			if (!mSaveLastResolution.equals(mWonderfulVideoResolution)) {
//				showRebootDialog();
//			}
			GolukApplication.getInstance().getIPCControlManager().getVideoResolution();
		}
	}

	/**
	 * ???????????????????????????
	 *
	 * @param event
	 * @param msg
	 * @param param1
	 * @param param2
	 */
	private void getVolumeCallback(int event, int msg, int param1, Object param2) {
		GolukDebugUtils.e("", "--------getVolumeCallback------event???" + event + "------msg???" + msg + "-----param1???"
				+ param1 + "------param2???" + param2);
		if (RESULE_SUCESS == param1) {
			try {
				JSONObject json = new JSONObject((String) param2);
				int value = json.getInt("value");
				mVolume = value + "";
				refreshVolume();
			} catch (Exception e) {

			}
		}
	}

	/**
	 * ???????????????????????????
	 *
	 * @param event
	 * @param msg
	 * @param param1
	 * @param param2
	 */
	private void setVolumeCallback(int event, int msg, int param1, Object param2) {
		GolukDebugUtils.e("", "--------setVolumeCallback------event???" + event + "------msg???" + msg + "-----param1???"
				+ param1 + "------param2???" + param2);
		closeLoading();
		if (RESULE_SUCESS == param1) {
			GolukApplication.getInstance().getIPCControlManager().getVolume();
		}
	}

	/**
	 * ??????????????????
	 *
	 * @param event
	 * @param msg
	 * @param param1
	 * @param param2
	 */
	private void getPowerOffTimeCallback(int event, int msg, int param1, Object param2) {
		GolukDebugUtils.e("", "--------getPowerOffTimeCallback------event???" + event + "------msg???" + msg
				+ "-----param1???" + param1 + "------param2???" + param2);
		if (RESULE_SUCESS == param1) {
			try{
				JSONObject json = new JSONObject((String)param2);
				int time = json.getInt("time_second");
				mPowerTime = time+"";
				refreshPowerTime();
			} catch (Exception e) {

			}
		}
	}

	/**
	 * ??????????????????
	 *
	 * @param event
	 * @param msg
	 * @param param1
	 * @param param2
	 */
	private void setPowerOffTimeCallback(int event, int msg, int param1, Object param2) {
		GolukDebugUtils.e("", "--------setPowerOffTimeCallback------event???" + event + "------msg???" + msg
				+ "-----param1???" + param1 + "------param2???" + param2);
		closeLoading();
		if (RESULE_SUCESS == param1) {
			GolukApplication.getInstance().getIPCControlManager().getPowerOffTime();
		}
	}

	/**
	 * ??????????????????
	 *
	 * @param event
	 * @param msg
	 * @param param1
	 * @param param2
	 */
	private void getVoiceTypeCallback(int event, int msg, int param1, Object param2) {
		GolukDebugUtils.e("", "--------getVoiceTypeCallback------event???" + event + "------msg???" + msg + "-----param1???"
				+ param1 + "------param2???" + param2);
		if (RESULE_SUCESS == param1) {
			try {
				JSONObject json = new JSONObject((String) param2);
				int type = json.getInt("type");
				mVoiceType = type + "";
				refreshVoiceType();
			} catch (Exception e) {

			}
		}
	}

	/**
	 * ??????????????????
	 *
	 * @param event
	 * @param msg
	 * @param param1
	 * @param param2
	 */
	private void setVoiceTypeCallback(int event, int msg, int param1, Object param2) {
		GolukDebugUtils.e("", "--------setVoiceTypeCallback------event???" + event + "------msg???" + msg + "-----param1???"
				+ param1 + "------param2???" + param2);
		closeLoading();
		if (RESULE_SUCESS == param1) {
			GolukApplication.getInstance().getIPCControlManager().getVoiceType();
		}
	}

	/**
	 * ??????????????????
	 * @param event
	 * @param msg
	 * @param param1
	 * @param param2
	 */
	private void callback_getWonderfulVideoType(int event, int msg, int param1, Object param2) {
		mVideoType = 6 + "";
		mCurrentVideoType = mVideoType;
		mVideoTypeDesc.setText(this.getString(R.string.str_settings_video_type1));
		GolukDebugUtils.e("", "SettingsActivity-----------callback_getWonderfulVideoType-----param2: " + param2);
		if (RESULE_SUCESS == param1) {
			try {
				JSONObject json = new JSONObject((String) param2);
				int wonder_history_time = json.getInt("wonder_history_time");
				int wonder_future_time = json.getInt("wonder_future_time");
				if (wonder_history_time == 6 && wonder_future_time == 6) {
					// ??????????????????6???6???
					mVideoType = wonder_future_time + "";
					mVideoTypeDesc.setText(this.getString(R.string.str_settings_video_type1));
				} else if (wonder_history_time == 0 && wonder_future_time == 30) {
					// ????????????
					mVideoType = wonder_future_time + "";
					mVideoTypeDesc.setText(this.getString(R.string.str_settings_video_type2));
				}
				mCurrentVideoType = mVideoType;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void callback_setWonderfulVideoType(int event, int msg, int param1, Object param2) {
		GolukDebugUtils.e("", "SettingsActivity-----------callback_setWonderfulVideoType-----param2: " + param2);
		if (RESULE_SUCESS == param1) {
			try {
				JSONObject obj = new JSONObject((String) param2);
				String needReboot = obj.optString("need_reboot");
				if (needReboot.equals("true") && !mCurrentVideoType.equals(mVideoType)) {
					showRebootDialog();
				}
				GolukApplication.getInstance().getIPCControlManager().getWonderfulVideoType();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * ????????????????????????
	 */
	private void refreshWonderfulVideoData() {
		int length = mWonderfulVideoValue.length;
		for (int i = 0; i < length; i++) {
			if (mWonderfulVideoResolution.equals(mWonderfulVideoValue[i])) {
				mTextWonderfulVideoQualityText.setText(mWonderfulVideo[i]);
			}
		}
	}

	/**
	 * ???????????????????????????
	 */
	private void refreshVolume() {
		int length = mVolumeValue.length;
		for (int i = 0; i < length; i++) {
			if (mVolumeValue[i].equals(mVolume)) {
				mVolumeText.setText(mVolumeList[i]);
			}
		}
	}

	/**
	 * ??????????????????
	 */
	private void refreshPowerTime() {
		int length = mPowerTimeList.length;
		String[] value = { "10", "60" };
		for (int i = 0; i < length; i++) {
			if (mPowerTime.equals(value[i])) {
				mPowerTimeText.setText(mPowerTimeList[i]);
			}
		}
	}

	/**
	 * ??????????????????
	 */
	private void refreshVoiceType() {
		int length = mVoiceTypeList.length;
		String[] type = { "0", "1" ,"2"};
		for (int i = 0; i < length; i++) {
			if (mVoiceType.equals(type[i])) {
				mVoiceTypeText.setText(mVoiceTypeList[i]);
			}
		}
	}

}
