package cn.com.tiros.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import android.content.res.AssetFileDescriptor;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public class MediaPlay {

	// MediaPlayer.OnCompletionListener

	// typedef enum _SYS_MediaPlayer_Event
	// {
	private static final int SYS_MEDIA_PLAYER_EVENT_BEGIN = 0; // /<
																// 开始播放dwParam1
																// = 0,dwParam2
																// = 0;
	private static final int SYS_MEDIA_PLAYER_EVENT_PAUSE = 1; // /<
																// 暂停播放dwParam1
																// = 0,dwParam2
																// = 0;
	private static final int SYS_MEDIA_PLAYER_EVENT_RESUME = 2; // /<
																// 恢复播放dwParam1
																// = 0,dwParam2
																// = 0;
	private static final int SYS_MEDIA_PLAYER_EVENT_END = 3; // /< 播放完毕dwParam1
																// = 0,dwParam2
																// = 0;
	// }SYS_MediaPlayer_Event;

	// typedef enum _SYS_MediaPlayer_State
	// {
	private static final int SYS_MEDIA_PLAYER_STATE_NOT_INIT = 0; // /< 没有初始化
	private static final int SYS_MEDIA_PLAYER_STATE_IDLE = 1; // /< 空闲状态，可以启动播放
	private static final int SYS_MEDIA_PLAYER_STATE_PLAYING = 2; // /< 正在播放状态
	private static final int SYS_MEDIA_PLAYER_STATE_BUFFERING = 3; // /< 缓冲中状态
	private static final int SYS_MEDIA_PLAYER_STATE_PAUSE = 4; // /< 暂停播放状态
	private static final int SYS_MEDIA_PLAYER_STATE_ERROR = 5; // /< 出错
	// }SYS_MediaPlayer_State;

	// typedef enum _SYS_MediaPlayer_ErrCode
	// {
	private static final int SYS_MEDIA_PLAYER_ERR_UNKNOWN = -1; // /< -1: 未知错误
	private static final int SYS_MEDIA_PLAYER_ERR_NONE = 0; // /< 0: 成功
	private static final int SYS_MEDIA_PLAYER_ERR_NOT_INIT = 1; // /< 1: 没有初始化
	private static final int SYS_MEDIA_PLAYER_ERR_ALREADY_INIT = 2; // /< 2:
																	// 已经初始化
	private static final int SYS_MEDIA_PLAYER_ERR_OPEN_DEVICE = 3; // /< 3:
																	// 打开设备出错
	private static final int SYS_MEDIA_PLAYER_ERR_ALREADY_BEGIN = 4; // /< 4:
																		// 已经开始
	private static final int SYS_MEDIA_PLAYER_ERR_NOT_BEGIN = 5; // /< 5: 没有开始
	private static final int SYS_MEDIA_PLAYER_ERR_OUT_OF_MEMORY = 6; // /< 6:
																		// 分配空间失败
	// }SYS_MediaPlayer_ErrCode;

	private MediaPlayer mMediaPlayer = null;

	private int mMediaPlayerState = 0;

	private int mhandler = 0;
	
	/** 播放语音类型，1，本地文件。2，流媒体。*/
	private int mPlayType = 0;

	private String mFileName = null;

	private AssetFileDescriptor fileDescriptor = null;

	private static long startTime = 0;

	// 流媒体播放线程
	private StreamPlayThread mStreamPlayThread = null;
	

	public MediaPlay() {
	}

	public void sys_mediaplayercreate(int handler) {
		mhandler = handler;
	
		mMediaPlayerState = SYS_MEDIA_PLAYER_STATE_NOT_INIT;

		mStreamPlayThread = new StreamPlayThread();
		mStreamPlayThread.start();

		// 让子线程开始执行
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
//		GolukDebugUtils.i("MediaPlay", "TTTTTTTTT sys_mediaplayercreate  handler  = "
//				+ handler);
	}

	public void sys_mediaplayerdestory() {
//		GolukDebugUtils.i("MediaPlay", "TTTTTTTTT sys_mediaplayerdestory");
		mMediaPlayerState = SYS_MEDIA_PLAYER_STATE_NOT_INIT;

		if (mPlayType == 1 && mMediaPlayer != null) {
			mMediaPlayer.stop();
			mMediaPlayer.release();
		}else if(mPlayType == 2){
			mStreamPlayThread.mStreamPlayHandler.sendEmptyMessage(1001);
			mStreamPlayThread.mStreamPlayHandler.getLooper().quit();
		}
		mMediaPlayer = null;
		mStreamPlayThread = null;
	}

	public void sys_mediaplayerregistnotify() {}

	public void sys_mediaplayerinit() {
//		GolukDebugUtils.i("MediaPlay", "TTTTTTTTT sys_mediaplayerinit");
		// mMediaPlayer.setWakeMode(mContext, PowerManager.PARTIAL_WAKE_LOCK);
		mMediaPlayerState = SYS_MEDIA_PLAYER_STATE_IDLE;
//		if (mStreamPlayThread.mStreamPlayHandler == null) {
//			GolukDebugUtils.i("MediaPlay",
//					"TTTTTTTTT sys_mediaplayerinit  mStreamPlayThread.mStreamPlayHandler == null");
//		}
//		mStreamPlayThread.mStreamPlayHandler.sendEmptyMessage(1000);
	}

	public int sys_mediaplayergetstate() {
//		 GolukDebugUtils.i("MediaPlay", "TTTTTTTTT sys_mediaplayergetstate  mMediaPlayerState = " + mMediaPlayerState);
		return mMediaPlayerState;
	}

	@SuppressWarnings("resource")
	public int sys_mediaplayer_playlocalfile(String filename) {
//		GolukDebugUtils.i("MediaPlay",
//				"TTTTTTTTT sys_mediaplayer_playlocalfile  filename = "
//						+ filename);
		
		if (mMediaPlayerState != SYS_MEDIA_PLAYER_STATE_IDLE) {
			mMediaPlayerState = SYS_MEDIA_PLAYER_STATE_NOT_INIT;
			// 没有初始化
			return SYS_MEDIA_PLAYER_ERR_NOT_INIT;
		}
		if (mMediaPlayerState == SYS_MEDIA_PLAYER_STATE_PLAYING) {
			// 已经开始播放
			mMediaPlayerState = SYS_MEDIA_PLAYER_STATE_PLAYING;
			return SYS_MEDIA_PLAYER_ERR_ALREADY_BEGIN;
		}
		
//		GolukDebugUtils.i("MediaPlay",
//				"TTTTTTTTT sys_mediaplayer_playlocalfile  0");
		
		//播放本地文件
		mPlayType = 1;
		
		if(mMediaPlayer != null ){
			mMediaPlayer.stop();
			mMediaPlayer.release();
			mMediaPlayer = null;
		}
		mMediaPlayer = new MediaPlayer();
		String filepath = FileUtils.libToJavaPath(filename);
		FileInputStream fis = null;
		try {
			File file = new File(filepath);
//			GolukDebugUtils.i("MediaPlay",
//					"TTTTTTTTT sys_mediaplayer_playlocalfile  file.length() = " + file.length());
			fis = new FileInputStream(file);
			mMediaPlayer.reset();
			mMediaPlayer.setDataSource(fis.getFD());
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			return SYS_MEDIA_PLAYER_ERR_OPEN_DEVICE;
		} catch (IllegalStateException e) {
			e.printStackTrace();
			return SYS_MEDIA_PLAYER_ERR_OPEN_DEVICE;
		} catch (Exception e) {
			e.printStackTrace();
			return SYS_MEDIA_PLAYER_ERR_OPEN_DEVICE;
		}
		try {
			mMediaPlayer.prepare();
		} catch (IllegalStateException e) {
			e.printStackTrace();
			return SYS_MEDIA_PLAYER_ERR_OPEN_DEVICE;
		} catch (Exception e) {
			e.printStackTrace();
			return SYS_MEDIA_PLAYER_ERR_OPEN_DEVICE;
		}
		
//		GolukDebugUtils.i("MediaPlay",
//				"TTTTTTTTT sys_mediaplayer_playlocalfile 1");
		
		mMediaPlayer.start();
		mMediaPlayerState = SYS_MEDIA_PLAYER_STATE_PLAYING;
		sys_meidaplayernotify(mhandler, SYS_MEDIA_PLAYER_EVENT_BEGIN, 0, 0);
		mMediaPlayer
				.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
					@Override
					public void onCompletion(MediaPlayer mp) {
						// 播放完成监听
						mMediaPlayerState = SYS_MEDIA_PLAYER_STATE_IDLE;
//						GolukDebugUtils.i("MediaPlay",
//								"TTTTTTTTT sys_mediaplayer_playlocalfile 2");
						if (mMediaPlayer != null) {
//							GolukDebugUtils.i("MediaPlay", "TTTTTTTTT sys_mediaplayer_stop  1");
							mMediaPlayer.stop();
							mMediaPlayer.release();
							mMediaPlayer = null;
						}
						sys_meidaplayernotify(mhandler,
								SYS_MEDIA_PLAYER_EVENT_END, 0, 0);
					}
				});
		if (fis != null) {
			try {
				fis.close();
				fis = null;
			} catch (IOException e) {}
		}
		return SYS_MEDIA_PLAYER_ERR_NONE;
	}

	public int sys_mediaplayer_playstream(final byte[] pstream, final int nlen) {
		
//		GolukDebugUtils.i("MediaPlay", "TTTTTTTTT sys_mediaplayer_playstream  pstream =  " + pstream);
//		GolukDebugUtils.i("MediaPlay", "TTTTTTTTT sys_mediaplayer_playstream  nlen =  " + nlen);

		if (pstream == null) {
			mMediaPlayerState = SYS_MEDIA_PLAYER_STATE_IDLE;
			return SYS_MEDIA_PLAYER_ERR_NONE;
		}
		//播放流媒体
		mPlayType = 2;
//		GolukDebugUtils.i("MediaPlay", "TTTTTTTTT sys_mediaplayer_playstream  0");

		mMediaPlayerState = SYS_MEDIA_PLAYER_STATE_PLAYING;
		sys_meidaplayernotify(mhandler, SYS_MEDIA_PLAYER_EVENT_BEGIN, 0, 0);
		Message msg = mStreamPlayThread.mStreamPlayHandler.obtainMessage();
		msg.what = 1002;
		msg.obj = pstream;
		msg.arg1 = nlen;
		mStreamPlayThread.mStreamPlayHandler.sendMessage(msg);
		return SYS_MEDIA_PLAYER_ERR_NONE;
	}

	public int sys_mediaplayer_pause() {
//		GolukDebugUtils.i("MediaPlay", "TTTTTTTTT sys_mediaplayer_pause");
		mMediaPlayerState = SYS_MEDIA_PLAYER_STATE_PAUSE;
		
		if(mPlayType == 1){
			if (mMediaPlayer != null) {
				mMediaPlayer.pause();
			}
		}else if(mPlayType == 2){
			mStreamPlayThread.mStreamPlayHandler.sendEmptyMessage(1004);
		}

		sys_meidaplayernotify(mhandler, SYS_MEDIA_PLAYER_EVENT_PAUSE, 0, 0);
		return SYS_MEDIA_PLAYER_ERR_NONE;
	}

	public int sys_mediaplayer_resume() {
//		GolukDebugUtils.i("MediaPlay", "TTTTTTTTT sys_mediaplayer_resume");
		
		if(mPlayType == 1){
			if (mMediaPlayer != null) {
				mMediaPlayer.start();
			}
		}else if(mPlayType == 2){
			mStreamPlayThread.mStreamPlayHandler.sendEmptyMessage(1005);
		}

		mMediaPlayerState = SYS_MEDIA_PLAYER_STATE_PLAYING;
		sys_meidaplayernotify(mhandler, SYS_MEDIA_PLAYER_EVENT_RESUME, 0, 0);
		return SYS_MEDIA_PLAYER_ERR_NONE;
	}

	public int sys_mediaplayer_stop() {
//		GolukDebugUtils.i("MediaPlay", "TTTTTTTTT sys_mediaplayer_stop  0");
		mMediaPlayerState = SYS_MEDIA_PLAYER_STATE_IDLE;
		
		if(mPlayType == 1){
			if (mMediaPlayer != null) {
//				GolukDebugUtils.i("MediaPlay", "TTTTTTTTT sys_mediaplayer_stop  1");
				mMediaPlayer.stop();
				mMediaPlayer.release();
				mMediaPlayer = null;
			}
		}else if(mPlayType == 2){
			mStreamPlayThread.mStreamPlayHandler.sendEmptyMessage(1003);
		}

		return SYS_MEDIA_PLAYER_ERR_NONE;
	}
	
	public int sys_mediaplayer_stopall(){
//		GolukDebugUtils.i("MediaPlay", "TTTTTTTTT sys_mediaplayer_stopall");
		mMediaPlayerState = SYS_MEDIA_PLAYER_STATE_IDLE;
		if(mPlayType == 1){
			if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
				mMediaPlayer.stop();
			}
		}else if(mPlayType == 2){
			mStreamPlayThread.mStreamPlayHandler.removeMessages(1002);
			mStreamPlayThread.mStreamPlayHandler.sendEmptyMessage(1003);	
		}

		return SYS_MEDIA_PLAYER_ERR_NONE;
	}

	/**
	 * AudioTrack 流播放 线程
	 * 
	 * @author caoyingpeng
	 * 
	 */
	class StreamPlayThread extends Thread {

		private AudioTrack at = null;
		// 语音流播放handler
		private Handler mStreamPlayHandler = null;
		
		public void run() {
//			GolukDebugUtils.i(CHILD_TAG, "TTTTTTTT StreamPlayThread  run");
			// 初始化消息循环队列，需要在Handler创建之前
			Looper.prepare();

			mStreamPlayHandler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					switch (msg.what) {
					case 1000: // init
//						GolukDebugUtils.i("MediaPlay", "TTTTTTTTT StreamPlayThread 1000");
//						if (at == null) {
//							int mInitSize = AudioTrack.getMinBufferSize(
//									16 * 1000,
//									AudioFormat.CHANNEL_CONFIGURATION_MONO,
//									AudioFormat.ENCODING_PCM_16BIT);
//
//							at = new AudioTrack(AudioManager.STREAM_MUSIC,
//									16 * 1000,
//									AudioFormat.CHANNEL_CONFIGURATION_MONO,
//									AudioFormat.ENCODING_PCM_16BIT,
//									mInitSize * 4, AudioTrack.MODE_STREAM);
//						}
						break;
					case 1001: // destory
//						GolukDebugUtils.i("MediaPlay", "TTTTTTTTT StreamPlayThread 1001");
						if (at != null) {
							at.stop();
							at.release();
							at = null;
						}
						break;
					case 1002: // play
//						GolukDebugUtils.i("MediaPlay", "TTTTTTTTT StreamPlayThread 1002  msg.arg1 = " + msg.arg1);
						if (at == null) {
							int mInitSize = AudioTrack.getMinBufferSize(
									16 * 1000,
									AudioFormat.CHANNEL_CONFIGURATION_MONO,
									AudioFormat.ENCODING_PCM_16BIT);

							at = new AudioTrack(AudioManager.STREAM_MUSIC,
									16 * 1000,
									AudioFormat.CHANNEL_CONFIGURATION_MONO,
									AudioFormat.ENCODING_PCM_16BIT,
									mInitSize * 4, AudioTrack.MODE_STREAM);
						}
						at.setStereoVolume(AudioTrack.getMaxVolume(),
								AudioTrack.getMaxVolume());
						at.play();
						at.write((byte[]) msg.obj, 0, msg.arg1);
						at.setNotificationMarkerPosition(msg.arg1 / 2 * 3 / 4);
						at.setPlaybackPositionUpdateListener(new AudioTrack.OnPlaybackPositionUpdateListener() {
							public void onPeriodicNotification(AudioTrack track) {
							}
							public void onMarkerReached(AudioTrack track) {
//								GolukDebugUtils.i("MediaPlay", "TTTTTTTTT StreamPlayThread  1002  1");
								startTime = System.currentTimeMillis()
										- startTime;
								mHandler.sendEmptyMessageDelayed(0,
										startTime / 3);
//								GolukDebugUtils.i("MediaPlay", "TTTTTTTTT StreamPlayThread  1002  2");
							}
						});
						startTime = System.currentTimeMillis();
						break;
					case 1003: // stop
//						GolukDebugUtils.i("MediaPlay", "TTTTTTTTT StreamPlayThread 1003");
						if (at != null) {
							mHandler.removeMessages(0);
							at.setStereoVolume(AudioTrack.getMinVolume(),
									AudioTrack.getMinVolume());
							at.flush();
							at.stop();
						}
						break;
					case 1004: // pause
//						GolukDebugUtils.i("MediaPlay", "TTTTTTTTT StreamPlayThread 1004");
						if (at != null) {
							at.pause();
						}
						break;
					case 1005: // resume
//						GolukDebugUtils.i("MediaPlay", "TTTTTTTTT StreamPlayThread 1005");
						if (at != null) {
							at.play();
						}
						break;

					}
				}

			};
			// 启动子线程消息循环队列
			Looper.loop();
		}
	}

	/**
	 * MediaPlay 主线程handler
	 */
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				mMediaPlayerState = SYS_MEDIA_PLAYER_STATE_IDLE;
				sys_meidaplayernotify(mhandler, SYS_MEDIA_PLAYER_EVENT_END, 0,
						0);
				break;
			}
		}
	};

	public static native void sys_meidaplayernotify(int handler, int dwEvent,
			int dwParam1, int dwParam2);
}
