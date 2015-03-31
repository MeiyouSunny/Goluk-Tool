package cn.com.mobnote.golukmobile.carrecorder;

import io.vov.vitamio.LibsChecker;
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.MediaPlayer.OnBufferingUpdateListener;
import io.vov.vitamio.MediaPlayer.OnCompletionListener;
import io.vov.vitamio.MediaPlayer.OnErrorListener;
import io.vov.vitamio.MediaPlayer.OnInfoListener;
import io.vov.vitamio.MediaPlayer.OnPreparedListener;
import io.vov.vitamio.MediaPlayer.OnVideoSizeChangedListener;

import java.io.IOException;

import cn.com.mobnote.golukmobile.R;
import android.app.Activity;
import android.graphics.PixelFormat;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
public class VideoPlayerActivity extends Activity implements OnCompletionListener, OnBufferingUpdateListener
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
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!LibsChecker.checkVitamioLibs(this))
			return;
		setContentView(R.layout.carrecorder_videoplayer);
		
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
		
		initView();
		
		
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
		
		showLoading();
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
		playVideo();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		// TODO Auto-generated method stub
	}
	
	/**
	 * 播放视频
	 * @author xuhw
	 * @date 2015年3月31日
	 */
	private void playVideo(){
		try {
			mMediaPlayer = new MediaPlayer(this);
			mMediaPlayer.setBufferSize(1024);
			mMediaPlayer.setDataSource(playUrl);
			mMediaPlayer.setDisplay(mSurfaceHolder);
			mMediaPlayer.prepareAsync();
			mMediaPlayer.setOnInfoListener(this);
			mMediaPlayer.setOnBufferingUpdateListener(this);
			mMediaPlayer.setOnCompletionListener(this);
			mMediaPlayer.setOnPreparedListener(this);
			mMediaPlayer.setOnVideoSizeChangedListener(this);
			mMediaPlayer.setOnErrorListener(this);
			setVolumeControlStream(AudioManager.STREAM_MUSIC);
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
		
	}

	@Override
	public boolean onError(MediaPlayer arg0, int arg1, int arg2) {
		hideLoading();
		Toast.makeText(VideoPlayerActivity.this, "播放错误", Toast.LENGTH_LONG).show();
		return false;
	}

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
