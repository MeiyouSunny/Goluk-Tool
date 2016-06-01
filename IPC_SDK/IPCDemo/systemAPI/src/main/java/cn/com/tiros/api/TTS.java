package cn.com.tiros.api;

import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import cn.com.tiros.debug.GolukDebugUtils;

import com.iflytek.speech.ErrorCode;
import com.iflytek.speech.ISpeechModule;
import com.iflytek.speech.InitListener;
import com.iflytek.speech.SpeechConstant;
import com.iflytek.speech.SpeechSynthesizer;
import com.iflytek.speech.SpeechUtility;
import com.iflytek.speech.SynthesizerListener;

public class TTS implements ITTS {
	/** TTS c端句柄 */
	private int mTTSHandler; //
	/** 是否停止语音合成 */
	private boolean stopTtsSynth = false;
	/** 语音合成对象 */
	private SpeechSynthesizer mTts;

	private boolean isInit = false;

	/**
	 * @brief 创建TTS引擎句柄
	 * @return - 成功:TTS引擎句柄,失败:NULL
	 */
	public void sys_ttscreate(int tts) {
		ttsCreate(tts);
	}

	/**
	 * @brief 销毁TTS引擎句柄艺龙数据的上线
	 * @param[in] ptts - TTS引擎结构体指针
	 * @return -无
	 */
	public void sys_ttsdestory() {
		ttsDestroy();
	}

	/**
	 * @brief 设置授权相关信息
	 * @param[in] ptts - TTS引擎结构体指针
	 * @param[in] pauthinfo - 授权相关数据
	 * @return - 无
	 */
	public void sys_ttssetauthinfo(String pauthinfo) {

	}

	/**
	 * @brief TTS引擎初始化
	 * @param[in] ptts - TTS引擎结构体指针
	 * @return - 成功:true 失败:false
	 */
	public int sys_ttsinit() {
		return 1;
	}

	/**
	 * @brief 开始播放指定文本
	 * @param[in] ptts - TTS引擎结构体指针
	 * @param[in] text - 要播放的文本内容
	 * @return - TTS_PlayerState类型枚举值
	 */
	public int sys_ttsplayer_start(final String text) {
		return ttsPlayerStart(text);
	}

	/**
	 * @brief 获取授权剩余时间
	 * @param[in] ptts - TTS引擎结构体指针
	 * @return - 实际获取的授权剩余时间
	 * 
	 *         HCI_ERR_SYS_NOT_INIT 100 << HCI_SYS未初始化 HCI_ERR_PARAM_INVALID 1
	 *         << 函数的传入参数错误
	 */
	public int sys_ttsgetauthexpiretime() {
		return 100000000;
	}

	/**
	 * @brief 开始校验授权
	 * @param[in] ptts - TTS引擎结构体指针
	 * @return - 无
	 */
	public void sys_ttscheckauth() {

	}

	/**
	 * @brief 获取当前TTS引擎播放器状态
	 * @param[in] ptts - TTS引擎结构体指针
	 * @return - TTS_PlayerState类型枚举值
	 */
	public int sys_ttsgetplayerstate() {
		return ttsGetPlayerState();
	}

	/**
	 * @brief 暂停当前播放
	 * @param[in] ptts - TTS引擎结构体指针
	 * @return - TTS_PlayerState类型枚举值
	 */
	public int sys_ttsplayer_pause() {
		return ttsPlayerPause();
	}

	/**
	 * @brief 恢复当前播放
	 * @param[in] ptts - TTS引擎结构体指针
	 * @return - TTS_PlayerState类型枚举值
	 */
	public int sys_ttsplayer_resume() {
		return ttsPlayerResume();
	}

	/**
	 * @brief 停止当前播放
	 * @param[in] ptts - TTS引擎结构体指针
	 * @return - TTS_PlayerState类型枚举值
	 */
	public int sys_ttsplayer_stop() {
		return ttsPlayerStop();
	}

	/**
	 * 获取语音资源存放路径
	 * 
	 * @return
	 */
	public String sys_ttsgetdatapath() {
		return "fs0:";
	}

	public void sys_setvoicetype(String capkey) {

	}

	/**
	 * 合成回调监听。
	 */
	private SynthesizerListener mTtsListener = new SynthesizerListener.Stub() {
		@Override
		public void onBufferProgress(int progress) throws RemoteException {
			Message msg = new Message();
			msg.what = MSG_BUFFERPROGRESS;
			msg.obj = progress;
			mHandler.sendMessage(msg);
		}

		@Override
		public void onCompleted(int code) throws RemoteException {
			Message msg = new Message();
			msg.what = MSG_PLAYCOMPLETED;
			msg.obj = code;
			mHandler.sendMessage(msg);
		}

		@Override
		public void onSpeakBegin() throws RemoteException {
			mHandler.sendEmptyMessage(MSG_STARTPLAYER);
		}

		@Override
		public void onSpeakPaused() throws RemoteException {
			mHandler.sendEmptyMessage(MSG_PAUSEDPLAYER);
		}

		@Override
		public void onSpeakProgress(int progress) throws RemoteException {
			Message msg = new Message();
			msg.what = MSG_PLAY_PROGRESS;
			msg.obj = progress;
			mHandler.sendMessage(msg);
		}

		@Override
		public void onSpeakResumed() throws RemoteException {
			mHandler.sendEmptyMessage(MSG_RESUME);
		}
	};

	/**
	 * 授权回调消息
	 */
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_STARTPLAYER:
				sys_ttsEvent(mTTSHandler, SYS_TTS_PLAYER_EVENT_BEGIN, 0, 0);
				break;
			case MSG_PAUSEDPLAYER:
				sys_ttsEvent(mTTSHandler, SYS_TTS_PLAYER_EVENT_PAUSE, 0, 0);
				break;
			case MSG_BUFFERPROGRESS:
				int progress = ((Integer) msg.obj).intValue();
				sys_ttsEvent(mTTSHandler, SYS_TTS_PLAYER_EVENT_BUFFERPROGRESS, progress, 0);
				break;
			case MSG_RESUME:
				sys_ttsEvent(mTTSHandler, SYS_TTS_PLAYER_EVENT_RESUME, 0, 0);
				break;
			case MSG_PLAYCOMPLETED:
				int completeCode = ((Integer) msg.obj).intValue();
				sys_ttsEvent(mTTSHandler, SYS_TTS_PLAYER_EVENT_END, completeCode, 0);
				break;
			case MSG_PLAY_PROGRESS:
				int playProgress = ((Integer) msg.obj).intValue();
				sys_ttsEvent(mTTSHandler, SYS_TTS_PLAYER_EVENT_PROGRESS, playProgress, 0);
				break;
			case MSG_ERROR:
				int errorType = ((Integer) msg.obj).intValue();
				sys_ttsEvent(mTTSHandler, SYS_TTS_PLAYER_EVENT_ERROR, errorType, 0);
				break;
			default: // 操作失败
				sys_ttsEvent(mTTSHandler, SYS_TTS_PLAYER_EVENT_ERROR, ERROR_TYPE_DEVICE, 0);
				break;
			}

		}
	};

	private void init() {
		GolukDebugUtils.i("", "TTS+----------ttsCreate-----init:---111");
		// 语音+
		SpeechUtility.getUtility(Const.getAppContext()).setAppid(APPID);
		mTts = new SpeechSynthesizer(Const.getAppContext(), mTtsInitListener);
		GolukDebugUtils.i("", "TTS+----------ttsCreate-----init:---222");
	}

	/**
	 * 初期化监听。
	 */
	private InitListener mTtsInitListener = new InitListener() {

		@Override
		public void onInit(ISpeechModule arg0, int code) {
			GolukDebugUtils.i("", "TTS+----------onInit-----code:" + code);
			if (code == ErrorCode.SUCCESS) {
				isInit = true;
			}
		}
	};

	private void ttsCreate(int tts) {
		GolukDebugUtils.i("", "TTS+----------ttsCreate-----111:");
		mTTSHandler = tts;
		init();
	}

	private void ttsDestroy() {
		if (null != mTts) {
			mTts.stopSpeaking(mTtsListener);
			// 退出时释放连接
			mTts.destory();
		}
		mTTSHandler = 0;
	}

	private int ttsPlayerStart(final String text) {
		GolukDebugUtils.i("", "TTS-----API-----start11:" + text);
		final String playText = getRealPlayContent(text); // 最终播放的文本
		if (null == playText || "".equals(playText)) {
			return 1;
		}
		GolukDebugUtils.i("", "TTS+----------start11-----init:---222:");
		setParam();
		// 设置参数
		int code = mTts.startSpeaking(playText, mTtsListener);
		GolukDebugUtils.i("", "TTS+----------start11-----init:---33333:" + code);
		if (code != 0) {
			// ERROR
			sendError(ERROR_TYPE_ENGINE);
		}
		return 1;
	}

	private int ttsPlayerStop() {
		if (null != mTts) {
			mTts.stopSpeaking(mTtsListener);
		}
		stopTtsSynth = true;
		return 1;
	}

	private int ttsPlayerResume() {
		if (null != mTts) {
			mTts.resumeSpeaking(mTtsListener);
		}
		return 0;
	}

	private int ttsPlayerPause() {
		if (null != mTts) {
			mTts.pauseSpeaking(mTtsListener);
		}
		return 0;
	}

	private int ttsGetPlayerState() {
		if (!isInit) {
			return STATE_NOT_INIT;
		}
		if (mTts.isSpeaking()) {
			return STATE_PLAYING;
		}
		return STATE_IDLE;
	}

	/**
	 * 得到最终要播放的字符，有时候传来的字符串要做大小写转换与字符串替换
	 * 
	 * @param text
	 *            原始的字符串
	 * @return 转换后的最新的字符串
	 * @author jiayf
	 * @date 2014-7-23
	 */
	private String getRealPlayContent(final String text) {
		if (null == text || "".equals(text)) {
			return null;
		}
		final String lowerText = text.toLowerCase();// 把文本全部转换成小写
		String tempText = lowerText;

		int index = lowerText.indexOf("null"); // 查找字符串是否有"null"
		if (-1 != index) {
			tempText = lowerText.substring(0, index);
		}
		return tempText;
	}

	/**
	 * 设置TTS参数设置
	 * 
	 * @author jiayf
	 * @date 2014-7-23
	 */
	private void setParam() {
		mTts.setParameter(SpeechConstant.ENGINE_TYPE, "local");
		mTts.setParameter(SpeechSynthesizer.VOICE_NAME, "xiaoyan");
		mTts.setParameter(SpeechSynthesizer.SPEED, "50");
		mTts.setParameter(SpeechSynthesizer.PITCH, "50");
		mTts.setParameter(SpeechSynthesizer.VOLUME, "100");
	}

	private void sendError(int errorTytpe) {
		Message msg = new Message();
		msg.what = MSG_ERROR;
		msg.obj = errorTytpe;
		mHandler.sendMessage(msg);
	}

	/**
	 * @brief 注册tts引擎回调函数
	 * @param[in] ptts - TTS对象结构体指针
	 * @param[in] pfnnotify - TTS事件回调函数指针
	 * @param[in] pvuser - TTS事件回调对象指针
	 * @return - 无
	 */
	public static native void sys_ttsEvent(int tts, int dwEvent, int param1, int param2);

	public static native void sys_ttsEvent(int tts, int dwEvent, int param1, byte[] param2);

}
