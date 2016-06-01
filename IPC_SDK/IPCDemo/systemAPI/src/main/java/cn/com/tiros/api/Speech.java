package cn.com.tiros.api;

import org.json.JSONObject;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;
import cn.com.tiros.debug.GolukDebugUtils;

import com.iflytek.speech.ErrorCode;
import com.iflytek.speech.ISpeechModule;
import com.iflytek.speech.InitListener;
import com.iflytek.speech.SpeechConstant;
import com.iflytek.speech.SpeechUnderstander;
import com.iflytek.speech.SpeechUnderstanderListener;
import com.iflytek.speech.UnderstanderResult;

/**
 * 录音，主要用于语音搜索
 * 
 * @author jiayf
 * @date 2014/04/09
 * */
public class Speech {

	/** 录音结束 */
	private static final int STATE_SPEEK_END = 2;
	/** 网络错误 */
	private static final int STATE_NETSTATE = 5;
	/** 取消识别 */
	private static final int STATE_CANCEL = 6;
	/** 当前说话声音大小 */
	private static final int MSG_UPDATE_VOLUME = 7;
	/** 录音完成，开始识别 */
	private static final int MSG_START_RECONGIZER = 8;
	/** 客户端错误 */
	private static final int MSG_CLIENT_ERROR = 9;
	/** 服务端错误 */
	private static final int MSG_SERVERS_ERROR = 10;
	/** 语音识别取消完成 */
	private static final int MSG_STATE_CANCELOK = 13;
	/** 错误提示好像没有说话 */
	private static final int MSG_STATE_NOVOICE = 14;
	/** 打开设备出错 */
	private static final int MSG_STETE_DEVICEERROR = 16;
	/** 有正确的识别结果 */
	private static final int MSG_STATE_RESULT = 17;
	/** 拼JSON串的key值 表示状态 */
	private static final String KEY_STATE = "state";
	/** 拼JSON串的key值 表示数据 */
	private static final String KEY_DATA = "data";

	private static final int STATE_SUCESS = 0;

	private int mSpeechHandler; // speech c端句柄

	/** 取消状态 */
	private boolean mCancelHasDone = true; // 取消是否完成
	private boolean mCancelOnGoing = false;// 取消是否正在进行

	/**
	 * 以下为科大讯飞的开发
	 * */
	// 函数调用返回值
	private int ret = 0;
	private static final String APPID = "appid=534742e2";
	// 语义理解
	// 语义理解对象（语音到语义）。
	private SpeechUnderstander mSpeechUnderstander;

	/**
	 * Speech引擎的创建初始化
	 * 
	 * @param speech
	 *            指针
	 * @author jiayf
	 * @date 2014/04/09
	 * */
	public void sys_speechCreate(int speech) {
		speechCreate(speech);
	}

	/**
	 * Speech引擎的销毁
	 * 
	 * @author jiayf
	 * @date 2014/04/09
	 * */
	public void sys_speechDestroy() {
		speechDestroy();
	}

	/**
	 * 开始录音
	 * 
	 * @author jiayf
	 * @date 2014/04/09
	 * */
	public void sys_speechStart() {
		speechStart();
	}

	/**
	 * 设置录音的一些参数，需要在sys_speechStart之前调用，每次调用start前都要调用此函数
	 * 
	 * @param url
	 *            ：地址
	 * @param mapcenterlon
	 *            地图中心点经度
	 * @param mapcenterlat
	 *            :地图中心点纬度
	 * @param lon
	 *            :当前的位置
	 * @param lat
	 *            :当前的位置
	 * @author jiayf
	 * @date 2014/04/09
	 * 
	 * */
	public void sys_speechSetEngine(String url, int mapcenterlon,
			int mapcenterlat, int lon, int lat) {
		GolukDebugUtils.i("", "voiceService--------------API  ---sys_speechSetEngine--"
				+ url + " lon:" + lon);
		speechStop();
	}

	/**
	 * 取消录音
	 * 
	 * @author jiayf
	 * @date 2014/04/09
	 * */
	public void sys_speechCancel() {
		GolukDebugUtils.i("", "voiceService--------------API  ---sys_speechCancel--:");
		speechCancel();
	}

	/**
	 * 获取录音的状态
	 * 
	 * @author jiayf
	 * @date 2014/04/09
	 * */
	public int sys_speechGetState() {
		return 1;
	}

	/**
	 * 开始语音识别
	 * 
	 * @author jiayf
	 * @date 2014-7-14
	 */
	private void speechStart() {
		GolukDebugUtils.i("", "voiceService--------------API  ---sys_speechStart--");
		setEngineParam();
		GolukDebugUtils.i("", "voiceService--------------API  ---startUnderstand--111");
		if (mSpeechUnderstander.isUnderstanding()) {// 开始前检查状态
			GolukDebugUtils.i("",
					"voiceService--------------API  ---startUnderstand--22222");
			mSpeechUnderstander.stopUnderstanding(mRecognizerListener);
		} else {
			GolukDebugUtils.i("",
					"voiceService--------------API  ---startUnderstand--44444");
			ret = mSpeechUnderstander.startUnderstanding(mRecognizerListener);
			GolukDebugUtils.i("",
					"voiceService--------------API  ---startUnderstand--111: "
							+ ret);
			if (STATE_SUCESS != ret) {
				shareToLogic(STATE_NETSTATE, "开始录音错误");
			}
		}
	}

	/**
	 * 识别回调。
	 */
	private SpeechUnderstanderListener mRecognizerListener = new SpeechUnderstanderListener.Stub() {

		@Override
		public void onVolumeChanged(int v) throws RemoteException {
			sendVolume(v);
		}

		@Override
		public void onError(int errorCode) throws RemoteException {
			GolukDebugUtils.i("",
					"voiceService--------------API  ---startUnderstand--onError: --"
							+ errorCode);
			if (10118 == errorCode) {
				noSpeekResult();
			} else {
				sendClientError(errorCode);
			}

		}

		@Override
		public void onEndOfSpeech() throws RemoteException {
			// 结束说话,开始识别
			mHandler.sendEmptyMessage(MSG_START_RECONGIZER);
		}

		@Override
		public void onBeginOfSpeech() throws RemoteException {

		}

		@Override
		public void onResult(final UnderstanderResult result)
				throws RemoteException {
			GolukDebugUtils.i("",
					"voiceService--------------API  ---startUnderstand--onResult: --111");
			if (null != result) {

				GolukDebugUtils.i("",
						"voiceService--------------API  ---startUnderstand--onResult: --222");

				// 显示
				String text = result.getResultString();
				if (!TextUtils.isEmpty(text)) {
					// mUnderstanderText.setText(text);

					GolukDebugUtils.i("",
							"voiceService--------------API  ---startUnderstand--onResult: --444:"
									+ text);

					sendSpeechResult(text);
				}
			} else {
				GolukDebugUtils.i("",
						"voiceService--------------API  ---startUnderstand--onResult: --No Result");
				sendClientError(2222);
			}
		}
	};

	/**
	 * 发送客户端错误回调
	 * 
	 * @param 错误码
	 * @author jiayf
	 * @date 2014-7-14
	 */
	private void sendClientError(int errorCode) {
		Message msg = new Message();
		msg.what = MSG_CLIENT_ERROR;
		msg.arg1 = errorCode;
		mHandler.sendMessage(msg);
	}

	/**
	 * 将当前说话声音的大小发送给逻辑组
	 * 
	 * @param volume
	 *            说话音量大小
	 * @author jiayf
	 * @date 2014-7-1
	 */
	private void sendVolume(int volume) {
		int result = 4 * volume / 10;
		if (result == 4) {
			result = 3;
		}
		Message msg = new Message();
		msg.what = MSG_UPDATE_VOLUME;
		msg.arg1 = result;
		mHandler.sendMessage(msg);
	}

	/**
	 * 发送语音识别结果给逻辑组
	 * 
	 * @param data
	 *            语音识别结果 一般为json格式
	 * @author jiayf
	 * @date 2014-7-1
	 */
	private void sendSpeechResult(String data) {
		Message msg = new Message();
		msg.what = MSG_STATE_RESULT;
		msg.obj = data;
		mHandler.sendMessage(msg);
	}

	/**
	 * 当前没有识别结果，(没有说话)
	 * 
	 * @author jiayf
	 * @date 2014-7-1
	 */
	private void noSpeekResult() {
		Message msg = new Message();
		msg.what = MSG_STATE_NOVOICE;
		mHandler.sendMessage(msg);
	}

	/**
	 * 设置语音搜索的参数
	 * 
	 * @author jiayf
	 * @date 2014-7-1
	 */
	private void setEngineParam() {
		if (null == mSpeechUnderstander) {
			return;
		}
		final String lag = "mandarin";
		// 设置语言
		mSpeechUnderstander.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
		// 设置语言区域　mandarin(普通话，默认)
		mSpeechUnderstander.setParameter(SpeechConstant.ACCENT, lag);
		// 设置语音前端点(开始录音前，未说话多少秒就会停止录音)
		mSpeechUnderstander.setParameter(SpeechConstant.VAD_BOS, "4000");
		// 设置语音后端点(开始录音后，未说话多少秒就会停止录音)
		mSpeechUnderstander.setParameter(SpeechConstant.VAD_EOS, "1000");
		// 搜索时，以POI为主
		mSpeechUnderstander.setParameter(SpeechConstant.DOMAIN, "poi");
		// 设置音频保存路径
		String exterPath = Environment.getExternalStorageDirectory().getPath();
		// 设置参数
		String param = "asr_ptt = 0"
				+ ",nlp_version=2.0,rst=json,plain_result=1,"
				+ ",asr_audio_path=" + exterPath + "/iflytek/wavaudio.pcm";
		mSpeechUnderstander.setParameter(SpeechConstant.PARAMS, param);

	}

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_UPDATE_VOLUME:
				int volume = 4 * msg.arg1 / 10;
				if (volume == 4) {
					volume = 3;
				}
				shareToLogic(MSG_UPDATE_VOLUME, String.valueOf(volume));
				break;
			case MSG_START_RECONGIZER:// 录音完成，开始识别
				shareToLogic(STATE_SPEEK_END, null);
				break;
			case MSG_CLIENT_ERROR: // 客户端错误
				if (mCancelHasDone) {
					String errorMsg = "网络错误，请重试" + " [" + msg.arg1 + "]";
					shareToLogic(MSG_CLIENT_ERROR, errorMsg);
				}
				break;
			case MSG_SERVERS_ERROR: // 服务端错误
				if (mCancelHasDone) {
					String errorMsg = "识别错误，请重试" + " [" + msg.arg1 + "]";
					shareToLogic(MSG_SERVERS_ERROR, errorMsg);
				}
				break;
			case MSG_STATE_NOVOICE:
				if (mCancelHasDone) {
					String errorData = "您好像没有说话哦！";
					shareToLogic(MSG_STATE_NOVOICE, errorData);
				}
				break;
			case MSG_STETE_DEVICEERROR: // 设备错误
				if (mCancelHasDone) {
					String errorData = "打开设备出错！" + " [" + msg.arg1 + "]";
					shareToLogic(MSG_STETE_DEVICEERROR, errorData);
				}
				break;
			case MSG_STATE_CANCELOK:// 取消成功
				mCancelOnGoing = false;
				mCancelHasDone = true;
				shareToLogic(MSG_STATE_CANCELOK, null);
				break;

			case MSG_STATE_RESULT: // 有识别结果，通知平台显示
				String data = (String) msg.obj;
				shareToLogic(MSG_STATE_RESULT, data);
				break;
			}
			super.handleMessage(msg);
		}
	};

	/**
	 * 初始化录音对象
	 * 
	 * @author jiayf
	 * @date 2014-7-14
	 */
	private void initSpeech() {
		mSpeechUnderstander = new SpeechUnderstander(Const.getAppContext(),
				mSpeechInitListener);
	}

	/**
	 * 初始化监听器（语音到语义）。
	 */
	private InitListener mSpeechInitListener = new InitListener() {

		@Override
		public void onInit(ISpeechModule arg0, int code) {

			if (code == ErrorCode.SUCCESS) {

			}
		}
	};

	private void speechCreate(int speech) {
		mSpeechHandler = speech;
		initSpeech();
		GolukDebugUtils.i("", "voiceService--------------API  ---sys_speechCreate--");
	}

	/**
	 * 销毁录音引擎
	 * 
	 * @author jiayf
	 * @date Jul 14, 2014
	 */
	private void speechDestroy() {
		GolukDebugUtils.i("", "voiceService--------------API  ---sys_speechDestroy--");
		if (null != mSpeechUnderstander) {
			mSpeechUnderstander.destory();
			mSpeechUnderstander = null;
		}
		mSpeechHandler = 0;
	}

	/**
	 * 取消录音
	 * 
	 * @author jiayf
	 * @date Jul 14, 2014
	 */
	private void speechCancel() {
		if (null != mSpeechUnderstander
				&& mSpeechUnderstander.isUnderstanding()) {
			mSpeechUnderstander.cancel(mRecognizerListener);
		}
		mHandler.sendEmptyMessage(MSG_STATE_CANCELOK);
	}

	private void speechStop() {
		mSpeechUnderstander.stopUnderstanding(mRecognizerListener);
	}

	/**
	 * 将识别结果及状态发送给JNI
	 * 
	 * @param state
	 *            :语音结果状态
	 * @param msg
	 *            :语音结果数据及错误提示信息
	 * @author jiayf
	 * @date 2014/04/11
	 * */
	private void shareToLogic(int state, String msg) {
		if (null == msg) {
			msg = "";
		}
		try {
			JSONObject jsonObj = new JSONObject();
			jsonObj.put(KEY_STATE, state);
			jsonObj.put(KEY_DATA, msg);
			String data = jsonObj.toString();
			GolukDebugUtils.i("", "Speech-----Jar-----Systemapi-----shareToLogic:" + data);
			// 发送给Logic
			sys_speechEvent(mSpeechHandler, data);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 将回调结果发送给JNI
	 * */
	public static native void sys_speechEvent(int speech, String data);

}
