package cn.com.mobnote.golukmobile.carrecorder.settings;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.IpcDataParser;
import cn.com.mobnote.golukmobile.carrecorder.base.CarRecordBaseActivity;
import cn.com.mobnote.golukmobile.carrecorder.entity.VideoConfigState;
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
 * 视频质量设置页面
 *
 * 2015年4月7日
 *
 * @author xuhw
 */
@SuppressLint("InflateParams")
public class VideoQualityActivity extends CarRecordBaseActivity implements OnClickListener, IPCManagerFn {
	/** 视频类型文字显示 */
	private TextView mCloseText = null;
	private TextView mLowText = null;
	private TextView mMiddleText = null;
	private TextView mHighText = null;
	/** 视频类型选中高亮 */
	private ImageButton mCloseIcon = null;
	private ImageButton mLowIcon = null;
	private ImageButton mMiddleIcon = null;
	private ImageButton mHighIcon = null;

	/** 视频质量类型　1080高 1080低 720高 720低 */
	public static enum SensitivityType {
		_1080h, _1080l, _720h, _720l
	};

	/** 保存选中视频类型 */
	private SensitivityType curType = SensitivityType._1080h;
	/** 音视频配置信息 */
	private VideoConfigState mVideoConfigState = null;
	/** UI显示 **/
	private String[] mArrayText = null;
	/** 文字显示 **/
	private TextView[] mText = null;
	/** 按钮显示 **/
	private ImageButton[] mImageIcon = null;
	private String selectType = "";
	private String[] resolutionArray = null;
	private String[] bitrateArray = null;
	/** ipc设备型号 **/
	private String mIPCName = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addContentView(LayoutInflater.from(this).inflate(R.layout.carrecorder_video_quality, null));
		setTitle("视频质量");

		initView();
		mIPCName = GolukApplication.getInstance().mIPCControlManager.mProduceName;
		setListener();

		if (null != GolukApplication.getInstance().getIPCControlManager()) {
			GolukApplication.getInstance().getIPCControlManager().addIPCManagerListener("videoquality", this);
		}
	}

	/**
	 * 初始化控件
	 * 
	 * @author xuhw
	 * @date 2015年4月6日
	 */
	private void initView() {
		mCloseText = (TextView) findViewById(R.id.closeText);
		mLowText = (TextView) findViewById(R.id.lowText);
		mMiddleText = (TextView) findViewById(R.id.middleText);
		mHighText = (TextView) findViewById(R.id.highText);
		mCloseIcon = (ImageButton) findViewById(R.id.cRight);
		mLowIcon = (ImageButton) findViewById(R.id.dRight);
		mMiddleIcon = (ImageButton) findViewById(R.id.zRight);
		mHighIcon = (ImageButton) findViewById(R.id.gRight);

		mArrayText = getResources().getStringArray(R.array.list_quality_ui);
		mText = new TextView[] { mCloseText, mLowText, mMiddleText, mHighText };
		mImageIcon = new ImageButton[] { mCloseIcon, mLowIcon, mMiddleIcon, mHighIcon };
		resolutionArray = returnResolution(mIPCName);
		bitrateArray = returnBitrate(mIPCName);

		getArrays();
	}

	/**
	 * 设置控件监听事件
	 * 
	 * @author xuhw
	 * @date 2015年4月7日
	 */
	private void setListener() {
		findViewById(R.id.close).setOnClickListener(this);
		findViewById(R.id.low).setOnClickListener(this);
		findViewById(R.id.middle).setOnClickListener(this);
		findViewById(R.id.high).setOnClickListener(this);
	}

	/**
	 * 切换视频质量
	 * 
	 * @param type
	 *            视频类型
	 * @author xuhw
	 * @date 2015年4月6日
	 */
	private void updateSensitivity(SensitivityType type) {
		curType = type;
		mCloseText.setTextColor(getResources().getColor(R.color.setting_text_color_nor));
		mLowText.setTextColor(getResources().getColor(R.color.setting_text_color_nor));
		mMiddleText.setTextColor(getResources().getColor(R.color.setting_text_color_nor));
		mHighText.setTextColor(getResources().getColor(R.color.setting_text_color_nor));
		mCloseIcon.setVisibility(View.GONE);
		mLowIcon.setVisibility(View.GONE);
		mMiddleIcon.setVisibility(View.GONE);
		mHighIcon.setVisibility(View.GONE);

		if (SensitivityType._1080h == curType) {
			mCloseIcon.setVisibility(View.VISIBLE);
			mCloseText.setTextColor(getResources().getColor(R.color.setting_text_color_sel));
		} else if (SensitivityType._1080l == curType) {
			mLowIcon.setVisibility(View.VISIBLE);
			mLowText.setTextColor(getResources().getColor(R.color.setting_text_color_sel));
		} else if (SensitivityType._720h == curType) {
			mMiddleIcon.setVisibility(View.VISIBLE);
			mMiddleText.setTextColor(getResources().getColor(R.color.setting_text_color_sel));
		} else {
			mHighIcon.setVisibility(View.VISIBLE);
			mHighText.setTextColor(getResources().getColor(R.color.setting_text_color_sel));
		}

	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
		case R.id.back_btn:
			exit();
			break;
		case R.id.close:
			// updateSensitivity(SensitivityType._1080h);
			setArrayUI(getResources().getStringArray(R.array.list_quality_ui)[0]);
			break;
		case R.id.low:
			// updateSensitivity(SensitivityType._1080l);
			setArrayUI(getResources().getStringArray(R.array.list_quality_ui)[1]);
			break;
		case R.id.middle:
			// updateSensitivity(SensitivityType._720h);
			setArrayUI(getResources().getStringArray(R.array.list_quality_ui)[2]);
			break;
		case R.id.high:
			// updateSensitivity(SensitivityType._720l);
			setArrayUI(getResources().getStringArray(R.array.list_quality_ui)[3]);
			break;

		default:
			break;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		GolukApplication.getInstance().setContext(this, "videoquality");
		mVideoConfigState = GolukApplication.getInstance().getVideoConfigState();

		setData2UI(mVideoConfigState);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void IPCManage_CallBack(int event, int msg, int param1, Object param2) {
		if (event == ENetTransEvent_IPC_VDCP_CommandResp) {
			// 获取IPC系统音视频编码配置
			if (msg == IPC_VDCP_Msg_GetVedioEncodeCfg) {
				if (param1 == RESULE_SUCESS) {
					GolukDebugUtils.e("xuhw", "YYY================1111==================param2=" + param2);
					VideoConfigState mVideoConfigState = IpcDataParser.parseVideoConfigState((String) param2);

					setData2UI(mVideoConfigState);

				} else {
					// 获取失败默认显示1080P
					updateSensitivity(SensitivityType._1080h);
				}
				// 设置IPC系统音视频编码配置
			} else if (msg == IPC_VDCP_Msg_SetVedioEncodeCfg) {
				if (param1 == RESULE_SUCESS) {
					GolukApplication.getInstance().setVideoConfigState(mVideoConfigState);
				}
				GolukDebugUtils.e("xuhw", "YYY================IPC_VDCP_Msg_SetVedioEncodeCfg=============param1="
						+ param1);
			}
		}
	}

	public void exit() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				if (GolukApplication.getInstance().getIpcIsLogin()) {

					setArrayData(mIPCName, selectType);

					boolean flag = GolukApplication.getInstance().getIPCControlManager()
							.setVideoEncodeCfg(mVideoConfigState);
					GolukDebugUtils.e("xuhw", "YYY==========curType=========flag=" + flag);
				}
			}
		}).start();

		if (null != GolukApplication.getInstance().getIPCControlManager()) {
			GolukApplication.getInstance().getIPCControlManager().removeIPCManagerListener("videoquality");
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

	// 为UI赋值
	private void getArrays() {
		if (null != mArrayText) {
			int length = mArrayText.length;
			for (int i = 0; i < length; i++) {
				for (int j = i; j < mText.length; j++) {
					mText[j].setText(mArrayText[i]);
				}
			}
		}
	}

	// 点击选择视频质量
	private void setArrayUI(String type) {
		if (null != mArrayText) {
			int length = mArrayText.length;
			for (int i = 0; i < length; i++) {
				for (int j = i; j < mText.length; j++) {
					mImageIcon[j].setVisibility(View.GONE);
					mText[j].setTextColor(getResources().getColor(R.color.setting_text_color_nor));
					if (type.equals(mArrayText[i])) {
						mImageIcon[j].setVisibility(View.VISIBLE);
						mText[j].setTextColor(getResources().getColor(R.color.setting_text_color_sel));
						selectType = mArrayText[i];
					}
				}
			}
		}
	}

	// 保存选择的视频质量类型
	private void setArrayData(String ipcId, String type) {
		if (null != mArrayText) {
			int length = mArrayText.length;
			for (int i = 0; i < length; i++) {
				if (type.equals(mArrayText[i])) {
					mVideoConfigState.resolution = returnResolution(ipcId)[i];
					mVideoConfigState.bitrate = Integer.parseInt(returnBitrate(ipcId)[i]);
				}
			}
		}
	}

	// 遍历分辨率，区分码率，改变UI
	private void setData2UI(VideoConfigState videoConfigState) {
		GolukDebugUtils.e("", "--------VideoQualityActivity------setData2UI----resolution："+videoConfigState.resolution);
		GolukDebugUtils.e("", "--------VideoQualityActivity------setData2UI----bitrate："+videoConfigState.bitrate);
		if (null != mArrayText) {
			for (int m = 0; m < mArrayText.length; m++) {
				mImageIcon[m].setVisibility(View.GONE);
				mText[m].setTextColor(getResources().getColor(R.color.setting_text_color_nor));
			}
		}
		if (null != videoConfigState) {
			if (null != resolutionArray) {
				for (int i = 0; i < resolutionArray.length; i++) {
					if (videoConfigState.resolution.equals(resolutionArray[i])) {
						if (null != bitrateArray) {
							if (String.valueOf(videoConfigState.bitrate).equals(bitrateArray[i])) {
								mImageIcon[i].setVisibility(View.VISIBLE);
								mText[i].setTextColor(getResources().getColor(R.color.setting_text_color_sel));
								selectType = mArrayText[i];
								break;
							}
						}
					}
				}

			}
		}
	}

	// 视频分辨率
	private String[] returnResolution(String type) {
		String[] resolution = null;
		if ("G1".equals(type)) {
			resolution = getResources().getStringArray(R.array.list_quality_resolution1);
		} else {
			resolution = getResources().getStringArray(R.array.list_quality_resolution2);
		}
		return resolution;
	}

	// 视频质量码率
	private String[] returnBitrate(String type) {
		String[] bitrate = null;
		if ("G1".equals(type)) {
			bitrate = getResources().getStringArray(R.array.list_quality_bitrate1);
		} else {
			bitrate = getResources().getStringArray(R.array.list_quality_bitrate2);
		}
		return bitrate;
	}

}
