package cn.com.mobnote.golukmobile.carrecorder;

import io.vov.vitamio.LibsChecker;
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.MediaPlayer.OnBufferingUpdateListener;
import io.vov.vitamio.MediaPlayer.OnCompletionListener;
import io.vov.vitamio.MediaPlayer.OnErrorListener;
import io.vov.vitamio.MediaPlayer.OnInfoListener;
import io.vov.vitamio.MediaPlayer.OnPreparedListener;
import io.vov.vitamio.MediaPlayer.OnSeekCompleteListener;
import io.vov.vitamio.MediaPlayer.OnVideoSizeChangedListener;

import java.io.IOException;

import cn.com.mobnote.golukmobile.R;
import android.app.Activity;
import android.graphics.PixelFormat;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

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
  * 视频播放页面
  *
  * 2015年3月31日
  *
  * @author xuhw
  */
public class VideoPlayerActivity extends Activity implements OnCompletionListener, OnBufferingUpdateListener, OnSeekCompleteListener
,OnErrorListener, OnInfoListener, OnPreparedListener, OnClickListener, SurfaceHolder.Callback, OnVideoSizeChangedListener{
	/** 视频播放器 */
	private MediaPlayer mMediaPlayer=null;
	private SurfaceHolder mSurfaceHolder=null;
	private SurfaceView mSurfaceView=null;
	/** 播放地址 */
	private String playUrl=null;
	/** 加载中布局 */
	private LinearLayout mLoadingLayout = null;
	/** 加载中动画显示控件 */
	private ImageView mLoading = null;
	/** 加载中动画对象 */
	private AnimationDrawable mAnimationDrawable = null;
	/** 文件名字 */
	private String filename="";
	
	private int mVideoWidth;
	private int mVideoHeight;
	private boolean mIsVideoSizeKnown = false;
	private boolean mIsVideoReadyToBePlayed = false;
	
	private TextView mCurTime=null;
	private TextView mTotalTime=null;
	private ImageButton mPlayBtn=null;
	private ImageButton mPlayBigBtn=null;
	private SeekBar mSeekBar=null;
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		System.out.println("TTT======11111111111111111111=");
		if (!LibsChecker.checkVitamioLibs(this))
			return;
		System.out.println("TTT======22222222222222222222222=");
		setContentView(R.layout.carrecorder_videoplayer);
		System.out.println("TTT======３３３３３３３３３３３３３３３３３３=");
		String from = getIntent().getStringExtra("from");
		filename = getIntent().getStringExtra("filename");
		if(!TextUtils.isEmpty(from)){
			if(from.equals("local")){
				playUrl=getIntent().getStringExtra("path");
			}else if(from.equals("ipc")){
				int type = getIntent().getIntExtra("type", -1);
				if(4 == type){
					playUrl="http://192.168.43.234:5080/rec/wonderful/"+filename;
				}else if(2 == type){
					playUrl="http://192.168.43.234:5080/rec/urgent/"+filename;
				}else{
					playUrl="http://192.168.43.234:5080/rec/normal/"+filename;
				}
			}
		}
		System.out.println("TTT===４４４４４４４４４４４４４４４４===playUrl="+playUrl);
		initView();
		setListener();
		
		//http://192.168.43.234:5080/rec/wonderful/WND1_100101153739_0012.mp4
	}
	
	/**
	 * 初始化控件
	 * @author xuhw
	 * @date 2015年3月31日
	 */
	private void initView(){
		mSurfaceView = (SurfaceView)findViewById(R.id.mSurfaceView);
		mSurfaceHolder = mSurfaceView.getHolder();
		mSurfaceHolder.addCallback(this);
		mSurfaceHolder.setFormat(PixelFormat.RGBA_8888); 
		mLoadingLayout = (LinearLayout) findViewById(R.id.mLoadingLayout);
		mLoading = (ImageView) findViewById(R.id.mLoading);
		mLoading.setBackgroundResource(R.anim.video_loading);
		mAnimationDrawable = (AnimationDrawable) mLoading.getBackground();
		findViewById(R.id.back_btn).setOnClickListener(this);
		findViewById(R.id.title).setOnClickListener(this);
		TextView title = (TextView)findViewById(R.id.title);
		title.setText(filename);
		
		mCurTime = (TextView) findViewById(R.id.mCurTime);
		mTotalTime = (TextView) findViewById(R.id.mTotalTime);
		mPlayBtn = (ImageButton) findViewById(R.id.mPlayBtn);
		mPlayBigBtn = (ImageButton) findViewById(R.id.mPlayBigBtn);
		mSeekBar = (SeekBar) findViewById(R.id.mSeekBar);
		  

		showLoading();
	}
	
	/**
	 * 设置监听
	 * @author xuhw
	 * @date 2015年4月1日
	 */
	private void setListener(){
		mPlayBtn.setOnClickListener(this);
		mPlayBigBtn.setOnClickListener(this);
		mSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar arg0) {
				int progress = mSeekBar.getProgress();
				System.out.println("TTT===========aaaaaa==========");
				if(null != mMediaPlayer){
					mMediaPlayer.seekTo(progress);
					if(!mMediaPlayer.isPlaying()){
						mMediaPlayer.start();
						System.out.println("TTT===========bbbbbb==========");
					}
				}
			}
				
			@Override
			public void onStartTrackingTouch(SeekBar arg0) {
			}
			
			@Override
			public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
			}
		});
	}
	
	private final int GETPROGRESS=1;
	Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
				case GETPROGRESS:
					if(null != mMediaPlayer){
						if(mMediaPlayer.isPlaying()){
							long curPosition = mMediaPlayer.getCurrentPosition();
							long duration = mMediaPlayer.getDuration();
							
							System.out.println("TTT========duration=="+duration+"=====curPosition="+curPosition);
							mCurTime.setText(long2TimeStr(curPosition));
							mTotalTime.setText(long2TimeStr(duration));
							mSeekBar.setMax((int)duration);
							mSeekBar.setProgress((int)curPosition);
							mPlayBigBtn.setVisibility(View.GONE);
							mPlayBtn.setBackgroundResource(R.drawable.player_pause_btn);
						}else{
//							mPlayBigBtn.setVisibility(View.VISIBLE);
							mPlayBtn.setBackgroundResource(R.drawable.player_play_btn);
						}
					}
					
					mHandler.removeMessages(GETPROGRESS);
					mHandler.sendEmptyMessageDelayed(GETPROGRESS, 500);
					break;
	
				default:
					break;
			}
		};
	};
	
	/**
	 * 毫秒格式化时间字符串
	 * @param milliseconds 毫秒
	 * @return
	 * @author xuhw
	 * @date 2015年4月1日
	 */
	private String long2TimeStr(long milliseconds){
		String time="";
		
		int seconds = (int)(milliseconds/1000);
		if(seconds > 60){
			int min = seconds/60;
			int sec = seconds%60;
			if(min > 9){
				time = min+":";
			}else{
				time = "0"+min+":";
			}
			
			if(sec > 9){
				time += sec;
			}else{
				time += "0"+sec;
			}
		}else{
			if(seconds > 9){
				time = "00:"+seconds;
			}else{
				time = "00:0"+seconds;
			}
		}
		
		return time;
	}
	
	/**
	 * 显示加载中布局
	 * 
	 * @author xuhw
	 * @date 2015年3月8日
	 */
	private void showLoading() {
		mLoadingLayout.setVisibility(View.VISIBLE);
		mLoading.setVisibility(View.VISIBLE);
		mLoading.postDelayed(new Runnable() {
			@Override
			public void run() {
				if (mAnimationDrawable != null) {
					if (!mAnimationDrawable.isRunning()) {
						mAnimationDrawable.start();
					}
				}
			}
		}, 100);
	}
	
	/**
	 * 隐藏加载中显示画面
	 * 
	 * @author xuhw
	 * @date 2015年3月8日
	 */
	private void hideLoading() {
		if (mAnimationDrawable != null) {
			if (mAnimationDrawable.isRunning()) {
				mAnimationDrawable.stop();
			}
		}
		mLoadingLayout.setVisibility(View.GONE);
	}

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
			case R.id.back_btn:
			case R.id.title:
				exit();
				break;
			case R.id.mPlayBtn:
				if(null != mMediaPlayer){
					if(mMediaPlayer.isPlaying()){
						mMediaPlayer.pause();
						mPlayBigBtn.setVisibility(View.VISIBLE);
						mPlayBtn.setBackgroundResource(R.drawable.player_pause_btn);
						
					}else{
						mMediaPlayer.start();
						mPlayBigBtn.setVisibility(View.GONE);
						mPlayBtn.setBackgroundResource(R.drawable.player_play_btn);
					}
				}else{
					playVideo();
				}
				break;
			case R.id.mPlayBigBtn:
				if(null != mMediaPlayer){
					if(mMediaPlayer.isPlaying()){
						mMediaPlayer.pause();
					}else{
						mMediaPlayer.start();
						mPlayBigBtn.setVisibility(View.GONE);
					}
				}else{
					playVideo();
				}
				
				break;
	
			default:
				break;
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		releaseMediaPlayer();
		doCleanUp();
	}

	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
	}

	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		mSurfaceHolder=arg0;
		if(null == mMediaPlayer){
			playVideo();
		}else{
			mMediaPlayer.setDisplay(arg0);
			mMediaPlayer.start();
		}
		
		if(!isGet){
			isGet=true;
			mHandler.sendEmptyMessage(GETPROGRESS);
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		if(null != mMediaPlayer){
			mMediaPlayer.pause();
		}
	}
	
	/**
	 * 播放视频
	 * @author xuhw
	 * @date 2015年3月31日
	 */
	private void playVideo(){
		System.out.println("TTT=============playVideo=");
		try {
			mMediaPlayer = new MediaPlayer(this);
			mMediaPlayer.setBufferSize(1024);
//			mMediaPlayer.setLooping(true);
			mMediaPlayer.setDataSource(playUrl);
			mMediaPlayer.setDisplay(mSurfaceHolder);
			mMediaPlayer.setOnInfoListener(this);
			mMediaPlayer.setOnBufferingUpdateListener(this);
			mMediaPlayer.setOnCompletionListener(this);
			mMediaPlayer.setOnPreparedListener(this);
			mMediaPlayer.setOnVideoSizeChangedListener(this);
			mMediaPlayer.setOnErrorListener(this);
			mMediaPlayer.setOnSeekCompleteListener(this);
			setVolumeControlStream(AudioManager.STREAM_MUSIC);
			mMediaPlayer.prepareAsync();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onCompletion(MediaPlayer arg0) {
		long duration = mMediaPlayer.getDuration();
		System.out.println("TTT========onCompletion=====duration="+duration);
		
		mCurTime.setText(long2TimeStr(0));
		mTotalTime.setText(long2TimeStr(duration));
		mSeekBar.setMax((int)duration);
		mSeekBar.setProgress(0);
		
		if(null != mMediaPlayer){
			mMediaPlayer.seekTo(0);
			mMediaPlayer.pause();
//			mMediaPlayer.release();
//			mMediaPlayer = null;
		}
	}

	@Override
	public void onSeekComplete(MediaPlayer mp) {
		
//		if(!mMediaPlayer.isPlaying()){
//			mMediaPlayer.start();
//			System.out.println("TTT===========bbbbbb==========");
//		}
	}

	@Override
	public boolean onError(MediaPlayer arg0, int arg1, int arg2) {
		System.out.println("TTT=============onError=");
		hideLoading();
		Toast.makeText(VideoPlayerActivity.this, "播放错误", Toast.LENGTH_LONG).show();
		mCurTime.setText("00:00");
		mTotalTime.setText("00:00");
		return false;
	}

	boolean isGet=false;
	@Override
	public boolean onInfo(MediaPlayer arg0, int arg1, int arg2) {
		switch (arg1) {
			case MediaPlayer.MEDIA_INFO_BUFFERING_START:
				showLoading();
				break;
			case MediaPlayer.MEDIA_INFO_BUFFERING_END:
				hideLoading();
				break;
	
			default:
				break;
		}
		return false;
	}

	@Override
	public void onPrepared(MediaPlayer arg0) {
		System.out.println("TTT=============onPrepared=");
		mIsVideoReadyToBePlayed = true;
		if (mIsVideoReadyToBePlayed && mIsVideoSizeKnown) {
			startVideoPlayback();
		}
	}

	@Override
	public void onBufferingUpdate(MediaPlayer arg0, int arg1) {
		
	}

	@Override
	public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
		if (width == 0 || height == 0) {
			return;
		}
		
		mIsVideoSizeKnown = true;
		mVideoWidth = width;
		mVideoHeight = height;
		if (mIsVideoReadyToBePlayed && mIsVideoSizeKnown) {
			startVideoPlayback();
		}
	}
	
	private void releaseMediaPlayer() {
		if (mMediaPlayer != null) {
			mMediaPlayer.release();
			mMediaPlayer = null;
		}
	}

	private void doCleanUp() {
		mVideoWidth = 0;
		mVideoHeight = 0;
		mIsVideoReadyToBePlayed = false;
		mIsVideoSizeKnown = false;
	}

	private void startVideoPlayback() {
		mSurfaceHolder.setFixedSize(mVideoWidth, mVideoHeight);
		mMediaPlayer.start();
	}
	
	private void exit(){
		android.os.Process.killProcess(android.os.Process.myPid());
		this.finish();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			exit();
			return true;
		}
		 
		return super.onKeyDown(keyCode, event);
	}

}
