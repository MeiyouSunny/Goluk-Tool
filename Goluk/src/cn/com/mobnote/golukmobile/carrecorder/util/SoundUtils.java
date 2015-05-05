package cn.com.mobnote.golukmobile.carrecorder.util;

import java.util.HashMap;

import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.R;
import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.DisplayMetrics;
import android.view.WindowManager;

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
  * 声音播放处理
  *
  * 2015年3月8日
  *
  * @author xuhw
  */
public class SoundUtils {
	/** 播放提示音 */
	private SoundPool mSoundPool;
	/** 保存所有提示音数据 */
	private HashMap<String, Integer> soundData;
	private static SoundUtils instance = null; 
	private Context mContext= null;
	/** 拍照提示音标识 */
	public static  final String RECORD_CAMERA="recode_camera";
	/** 紧急提示音标识 */
	public static String RECORD_EMERGENT="recorde_emergent";
	/** 8s提示音标识 */
	public static String RECORD_SEC="recorde_sec";
	/** 分辨率 */
	private DisplayMetrics mDisplayMetrics=null;
	
	public static synchronized SoundUtils getInstance() { 
		if (instance==null)
			synchronized (SoundUtils.class) { 
				instance = new SoundUtils();
			}
			return instance;  
	}
	
	public SoundUtils(){
		mContext=GolukApplication.getInstance();
		mSoundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
		soundData = new HashMap<String, Integer>();
//		soundData.put(RECORD_CAMERA, mSoundPool.load(mContext, R.raw.recode_camera, 1));
//		soundData.put(RECORD_EMERGENT, mSoundPool.load(mContext, R.raw.recorde_emergent, 1));
//		soundData.put(RECORD_SEC, mSoundPool.load(mContext, R.raw.recorde_sec, 1));
		
		mDisplayMetrics = new DisplayMetrics();
		WindowManager windowManager = (WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE);  
		windowManager.getDefaultDisplay().getMetrics( mDisplayMetrics );
		
	}
	
	public DisplayMetrics getDisplayMetrics(){
		return mDisplayMetrics;
	}
	 
	/**
	 * 播放声音
	 * @param sound 声音标识符
	 * @author xuhw
	 * @date 2015年3月8日
	 */
//	public  void play(String sound) {
//		new playSound(sound).start();
//	}

	/**
	 *  播放声音
	 * @param sound 声音标识符
	 * @param loop 循环播放次数
	 * @author xuhw
	 * @date 2015年3月8日
	 */
	public void play(String sound, int loop) {
		new playSound(sound, loop).start();
	}
	
	
	class playSound extends Thread {
		private String sound;
		private int loop = 0;

		public playSound(String sound) {
			this.sound = sound;
		}

		public playSound(String sound, int loop) {
			this.sound = sound;
			this.loop = loop;
		}

		@Override
		public void run() {
			super.run();
			play(sound);
		}

		private void play(String sound) {
			AudioManager mAudioManager = (AudioManager)mContext.getSystemService(mContext.AUDIO_SERVICE);
			float cur = mAudioManager.getStreamVolume(AudioManager.STREAM_RING);
			float max = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
			float rate = cur / max;
			mSoundPool.play((Integer) soundData.get(sound), rate, rate, 0, loop, 1);
		}
	}
	
	
}
