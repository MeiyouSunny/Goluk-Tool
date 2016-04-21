package com.mobnote.golukmain.carrecorder.settings;

import java.util.List;

import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.mobnote.module.ipcmanager.IPCManagerFn;
import cn.com.tiros.debug.GolukDebugUtils;

import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.BaseActivity;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.carrecorder.IPCControlManager;
import com.mobnote.golukmain.carrecorder.IpcDataParser;
import com.mobnote.golukmain.carrecorder.entity.RecordStorgeState;
import com.mobnote.golukmain.carrecorder.entity.VideoConfigState;
import com.mobnote.golukmain.carrecorder.settings.bean.TSettingsJson;
import com.mobnote.golukmain.carrecorder.settings.bean.WonderfulVideoType;
import com.mobnote.golukmain.carrecorder.view.CustomLoadingDialog;
import com.mobnote.util.GolukFastJsonUtil;
import com.mobnote.util.GolukFileUtils;
import com.mobnote.util.GolukUtils;

public class TSettingsActivity extends BaseActivity implements OnClickListener,IPCManagerFn {

	/** 声音录制开关 **/
	private final int STATE_CLOSE = 0;
	private final int STATE_OPEN = 1;
	
	private static final String TAG = "tSettings";
	private RelativeLayout mSdLayout, mRecycleQualityLayout, mAutoRecycleLayout, mWonderfulTypeLayout,
			mWonderfulQualityLayout, mVoiceRecordLayout, mAutoPhotoLayout, mVolumeLayout, mKgjtsyLayout,
			mWonderfulTakephtotLayout, mImageFlipLayout, mVideoLogoLayout, mFatigueLayout, mUrgentCrashLayout,
			mParkingsleepLayout,mAFLayout,mAdasAssistanceLayout,
			mShutdownTimeLayout, mLanguageLayout, mTimeSetupLayout, mVersionLayout, mRestoreLayout;
	private TextView mSDDesc, mRecycleQualityDesc, mWonderfulTypeDesc, mWonderfulQualityDesc, mVolumeDesc,
			mUrgentCrashDesc, mShutdownTimeDesc, mLanguageDesc;
	private Button mAutoRecycleBtn, mVoiceRecordBtn, mAutophotoBtn, mKgjtsyBtn, mWonderfulTakephotoBtn, mImageFlipBtn,
			mVideoLogoBtn, mFatigueBtn, mParkingsleepBtn, mAFBtn;
	/** ADAS **/
	private Button mAdasAssistanceBtn, mForwardCloseBtn, mForwardSetupBtn;
	private RelativeLayout mForwardCloseLayout, mForwardSetupLayout, mAdasConfigLayout;
	
	private static final String GOLUK_LOCAL_LIST = "goluk_local_list_T1";
	private static final String GOLUK_BASIC_LIST = "goluk_basic_list_T1";
	/**精彩视频质量**/
	public static final int REQUEST_CODE_WONDERFUL_VIDEO_QUALITY = 33;
//	private List<TSettingsBean> mIPCList = null; 
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
	/** 提示音音量大小 **/
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
		requestIPCList();
		requestInfo();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
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
		mWonderfulQualityDesc = (TextView) findViewById(R.id.tv_t_settings_wonderfulvideo_quality_desc);
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
		
		mAutoRecycleBtn.setOnClickListener(this);//自动循环录像
		mVoiceRecordBtn.setOnClickListener(this);//声音录制
	}
	
	private void requestInfo() {
		boolean record = GolukApplication.getInstance().getIPCControlManager().getRecordState();
		 if (!record) {
		 getRecordState = true;
		 checkGetState();
		 }
		GolukDebugUtils.e("", "TSettingsActivity=========getRecordState=========" + record);
		boolean motionCfg = GolukApplication.getInstance().getIPCControlManager().getMotionCfg();
		 if (!motionCfg) {
		 getMotionCfg = true;
		 checkGetState();
		 }

		if (GolukApplication.getInstance().getIpcIsLogin()) {
			boolean flag = GolukApplication.getInstance().getIPCControlManager().queryRecordStorageStatus();
			GolukDebugUtils.e("xuhw", "TSettingsActivity======queryRecordStorageStatus=====flag=" + flag);
		}

		boolean flag = GolukApplication.getInstance().getIPCControlManager().getGSensorControlCfg();
		GolukDebugUtils.e("xuhw", "TSettingsActivity===getIPCControlManager============getGSensorControlCfg======flag="
				+ flag);

		// 获取ipc开关机提示音状态
		boolean switchFlag = GolukApplication.getInstance().getIPCControlManager().getIPCSwitchState();
		GolukDebugUtils.e("lily", "TSettingsActivity---------------switchFlag----------------" + switchFlag);

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
	}

	@Override
	public void onClick(View view) {
		int id = view.getId();
		if (id == R.id.ib_t_settings_back) {// 返回
			exit();
		} else if (id == R.id.ly_t_settings_buy) {// 购买链接
			
		} else if (id == R.id.ry_t_settings_sd) {// 存储卡容量查询
			click_sd();
		} else if (id == R.id.ry_t_settings_recycle_video_quality) {// 循环视频质量
			click_recycleVideoQuality();
		} else if (id == R.id.btn_t_settings_auto_recycle) {// 自动循环录像
			click_autoRecycle();
		} else if (id == R.id.ry_t_settings_wonderful_video_type) {// 精彩视频类型
			
		} else if (id == R.id.ry_t_settings_wonderful_video_quality) {// 精彩视频质量
			click_wonderfulVideoQuality();
		} else if (id == R.id.btn_t_settings_voice_record) {// 声音录制
			click_SoundRecord();
		} else if (id == R.id.ry_t_settings_volume) {// 提示音音量大小

		} else if (id == R.id.ry_t_settings_urgent_crash) {// 碰撞

		} else if (id == R.id.ry_t_settings_shutdown) {// 关机时间

		} else if (id == R.id.ry_t_settings_language) {// 语言设置

		} else if (id == R.id.ry_t_settings_time) {// 时间设置

		} else if (id == R.id.ry_t_settings_version) {// 版本信息

		} else if (id == R.id.ry_t_settings_restore) {// 恢复出厂

		}
	}
	
	private void exit() {
		this.finish();
	}
	
	private void matchDataToRefreshUI() {
		String[] mSettingList = null;
		String basicList = GolukUtils.getDataFromAssets(this, GOLUK_BASIC_LIST);
		String localList = GolukUtils.getDataFromAssets(this, GOLUK_LOCAL_LIST);
		TSettingsJson localJson = GolukFastJsonUtil.getParseObj(localList, TSettingsJson.class);
		if (null != localJson && null != localJson.data) {
			mSettingList = localJson.data.list;
			GolukDebugUtils.e("", "TSettingsActivity----------matchDataToRefreshUI------mSettingList1: "+mSettingList.length);
		}
		if (null != mIPCList && 0 != mIPCList.length && null != mSettingList) {
			for (int i = 0; i < mSettingList.length; i++) {
				for (int j = 0; j < mIPCList.length; j++) {
					if (mSettingList[i].equals(mIPCList[j])) {
						mLayoutList[i].setVisibility(View.VISIBLE);
					}
				}
			}
		} else {
			TSettingsJson basicJson = GolukFastJsonUtil.getParseObj(basicList, TSettingsJson.class);
			if (null != basicJson && null != basicJson.data) {
				mSettingList = basicJson.data.list;
			}
			if (null != mSettingList) {
				for (int k = 0; k <= mSettingList.length; k++) {
					mLayoutList[k].setVisibility(View.VISIBLE);
				}
			}
		}
		if (mAutoRecycleLayout.getVisibility() == View.VISIBLE) {
			mVoiceRecordLayout.setVisibility(View.VISIBLE);
			mAutoPhotoLayout.setVisibility(View.VISIBLE);
		} else {
			mVoiceRecordLayout.setVisibility(View.GONE);
			mAutoPhotoLayout.setVisibility(View.GONE);
		}
		if (mKgjtsyLayout.getVisibility() == View.VISIBLE) {
			mWonderfulTakephtotLayout.setVisibility(View.VISIBLE);
			mFatigueLayout.setVisibility(View.VISIBLE);
		} else {
			mWonderfulTakephtotLayout.setVisibility(View.GONE);
			mFatigueLayout.setVisibility(View.GONE);
		}
		if (mParkingsleepLayout.getVisibility() == View.VISIBLE) {
			mAFLayout.setVisibility(View.VISIBLE);
		} else {
			mAFLayout.setVisibility(View.GONE);
		}
		// TODO 判断Adas前向距离安全预警是否打开，如果打开，则显示
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
			} catch (Exception e) {
				e.printStackTrace();
			}
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
		if (RESULE_SUCESS == param1) {
			recordState = true;
			mAutoRecycleBtn.setBackgroundResource(R.drawable.set_open_btn);
		}
	}
	//停止自动循环录制
	private void callback_stopAutoRecycleRecord(int event, int msg, int param1, Object param2) {
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
			WonderfulVideoType videoType = GolukFastJsonUtil.getParseObj((String) param2, WonderfulVideoType.class);
			if (null != videoType) {
				if (videoType.wonder_history_time == 6 && videoType.wonder_history_time == 6) {
					// 精彩抓拍（前6后6）
					mWonderfulTypeDesc.setText("精彩抓拍（6+6）");
				} else {
					// 经典模式
					mWonderfulTypeDesc.setText("经典模式");
				}
			}
		}
	}
	//设置精彩视频类型
	private void callback_setWonderfulVideoType(int event, int msg, int param1, Object param2) {
		GolukDebugUtils.e("", "TSettingsActivity-----------callback_setWonderfulVideoType-----param2: " + param2);
		if (RESULE_SUCESS == param1) {
			
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
			if (!mSaveLastResolution.equals(mWonderfulVideoResolution)) {
				if (null == mRestartDialog) {
					mRestartDialog = new AlertDialog.Builder(this)
							.setTitle(this.getString(R.string.user_dialog_hint_title))
							.setMessage(this.getString(R.string.str_settings_restart_ipc))
							.setPositiveButton(this.getString(R.string.user_repwd_ok),
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(DialogInterface arg0, int arg1) {
											mRestartDialog.dismiss();
											mRestartDialog = null;
										}
									}).show();
				}
			}
			GolukApplication.getInstance().getIPCControlManager().getVideoResolution();
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
		if (RESULE_SUCESS != param1) {
			GolukUtils.showToast(this, this.getResources().getString(R.string.str_carrecoder_setting_failed));
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
		}
	}

	private void closeLoading() {
		if (mCustomProgressDialog.isShowing()) {
			mCustomProgressDialog.close();
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		GolukDebugUtils.e("", "TSettingsActivity----onActivityResult----requestCode :" + requestCode + "   resultCode:"
				+ resultCode);
		switch (requestCode) {
		case REQUEST_CODE_WONDERFUL_VIDEO_QUALITY://精彩视频质量
			activityResult_wonderful(resultCode, data);
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
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (null != GolukApplication.getInstance().getIPCControlManager()) {
			GolukApplication.getInstance().getIPCControlManager().removeIPCManagerListener(TAG);
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
		mVoiceTypeList = getResources().getStringArray(R.array.list_language);
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
//			showLoading();
		} else {
			GolukUtils.showToast(this, getResources().getString(R.string.str_carrecoder_setting_failed));
		}
	}
	
}
