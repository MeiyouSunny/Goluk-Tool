package cn.com.tiros.api;

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
 * 主要保存TTS的常量
 * 
 * 2014-7-23
 */
public interface ITTS {
	/** 授权ID */
	public static final String APPID = "534742e2";

	/** 语音合成进度 */
	public static final int SYS_TTS_PLAYER_EVENT_BUFFERPROGRESS = -1;
	/** 开始播放 */
	public static final int SYS_TTS_PLAYER_EVENT_BEGIN = 0;
	/** 暂停播放 */
	public static final int SYS_TTS_PLAYER_EVENT_PAUSE = 1;
	/** 恢复播放 */
	public static final int SYS_TTS_PLAYER_EVENT_RESUME = 2;
	/** 播放进度 */
	public static final int SYS_TTS_PLAYER_EVENT_PROGRESS = 3;
	/** 播放完成 */
	public static final int SYS_TTS_PLAYER_EVENT_END = 4;
	/** 播放发生错误 */
	public static final int SYS_TTS_PLAYER_EVENT_ERROR = 5;

	/** 引擎出错 */
	public static final int ERROR_TYPE_ENGINE = 0;
	/** 设备出错 */
	public static final int ERROR_TYPE_DEVICE = 1;
	/** 分配空间出错 */
	public static final int ERROR_TYPE_MALLOC = 2;

	/** Handler事件的开始播放 */
	public static final int MSG_STARTPLAYER = 1000;
	/** Handler事件的暂停播放 */
	public static final int MSG_PAUSEDPLAYER = 1001;
	/** Handler事件的语音合成进度 */
	public static final int MSG_BUFFERPROGRESS = 1002;
	/** Handler事件的恢复播放 */
	public static final int MSG_RESUME = 1003;
	/** Handler事件的播放完成 */
	public static final int MSG_PLAYCOMPLETED = 1005;
	/** Handler事件的播放进度 (百分比) */
	public static final int MSG_PLAY_PROGRESS = 1006;
	/** 错误 */
	public static final int MSG_ERROR = 1007;

	/** 播放状态 */
	/** 未初始化 */
	public static final int STATE_NOT_INIT = 0;
	/** 空闲 */
	public static final int STATE_IDLE = 1;
	/** 播放中 */
	public static final int STATE_PLAYING = 2;
	/** 暂停 */
	public static final int STATE_PAUSE = 3;
	/** 错误 */
	public static final int STATE_ERROR = 4;

}
