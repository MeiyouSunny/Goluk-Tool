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

import java.io.File;
import java.io.IOException;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.BaseActivity;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.util.BitmapManager;
import cn.com.mobnote.golukmobile.carrecorder.util.GFileUtils;
import cn.com.mobnote.golukmobile.carrecorder.util.SettingUtils;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomDialog;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomDialog.OnLeftClickListener;
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
  * 视频播放页面
  *
  * 2015年3月31日
  *
  * @author xuhw
  */
@SuppressLint("HandlerLeak")
public class VideoPlayerActivity extends BaseActivity implements OnCompletionListener, OnBufferingUpdateListener, OnSeekCompleteListener
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
	private RelativeLayout mTitleLayout=null;
	private RelativeLayout mBottomLayout=null;
	public  static Handler mHandler=null;
	private final int GETPROGRESS=1;
	/** 来源标志 */
	private String from;
	private boolean isShow = false;
	/** 播放器报错标识 */
	private boolean error = false;
	/** 预加载图片 */
	private ImageView mPreLoading = null;
	/** 视频第一帧图片地址 */
	private String image = "";
	/** 播放重置标识 */
	private boolean reset = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!LibsChecker.checkVitamioLibs(this))
			return;
		GolukDebugUtils.e("xuhw", "YYYYYY==VideoPlayerActivity==111111111=");
		setContentView(R.layout.carrecorder_videoplayer);
		from = getIntent().getStringExtra("from");
		image = getIntent().getStringExtra("image");
		filename = getIntent().getStringExtra("filename");
		GolukDebugUtils.e("xuhw", "YYYYYY==VideoPlayerActivity==2222===filename="+filename+"===from="+from);
		String ip = SettingUtils.getInstance().getString("IPC_IP");
		if(!TextUtils.isEmpty(from)){
			String path = Environment.getExternalStorageDirectory()+ File.separator + "goluk" + File.separator + "goluk_carrecorder";
			GFileUtils.makedir(path);
			String filePath = path + File.separator + "image";
			GolukDebugUtils.e("xuhw", "YYYYYY==VideoPlayerActivity==filePath="+filePath);
			if(from.equals("local")){
				playUrl=getIntent().getStringExtra("path");
				String fileName = playUrl.substring(playUrl.lastIndexOf("/")+1);
				fileName = fileName.replace(".mp4", ".jpg");
				image = filePath + File.separator + fileName;
				GolukDebugUtils.e("xuhw", "YYYYYY==VideoPlayerActivity==image="+image);
			}else if(from.equals("suqare")){
				playUrl=getIntent().getStringExtra("playUrl");
			}else if(from.equals("ipc")){
				String fileName = filename;
				fileName = fileName.replace(".mp4", ".jpg");
				image = filePath + File.separator + fileName;
				int type = getIntent().getIntExtra("type", -1);
				if(4 == type){
					playUrl="http://"+ ip + ":5080/rec/wonderful/"+filename;
				}else if(2 == type){
					playUrl="http://" + ip + ":5080/rec/urgent/"+filename;
				}else{
					playUrl="http://" + ip + ":5080/rec/normal/"+filename;
				}
			}
		}

		GolukDebugUtils.e("xuhw", "YYYYYY==VideoPlayerActivity==playUrl="+playUrl);
		initView();
		setListener();
		
		mHandler = new Handler(){
			public void handleMessage(android.os.Message msg) {
				switch (msg.what) {
					case GETPROGRESS:
						mHandler.removeMessages(GETPROGRESS);
						if(error){
							return;
						}
						
						if(null != mMediaPlayer){
							if(mMediaPlayer.isPlaying()){
								hideLoading();
								mPreLoading.setVisibility(View.GONE);
								long curPosition = mMediaPlayer.getCurrentPosition();
								long duration = mMediaPlayer.getDuration();
								
								GolukDebugUtils.e("xuhw", "TTT========duration=="+duration+"=====curPosition="+curPosition);
								mCurTime.setText(long2TimeStr(curPosition));
								mTotalTime.setText(long2TimeStr(duration));
								mSeekBar.setMax((int)duration);
								mSeekBar.setProgress((int)curPosition);
								mPlayBigBtn.setVisibility(View.GONE);
								mPlayBtn.setBackgroundResource(R.drawable.player_pause_btn);
							}else{
//								mPlayBigBtn.setVisibility(View.VISIBLE);
								mPlayBtn.setBackgroundResource(R.drawable.player_play_btn);
							}
						}
						
						mHandler.sendEmptyMessageDelayed(GETPROGRESS, 100);
						break;
		
					default:
						break;
				}
			};
		};
	}
	
	/**
	 * 初始化控件
	 * @author xuhw
	 * @date 2015年3月31日
	 */
	private void initView(){
		mTitleLayout = (RelativeLayout)findViewById(R.id.title_layout);
		mBottomLayout = (RelativeLayout)findViewById(R.id.mBottomLayout);
		mSurfaceView = (SurfaceView)findViewById(R.id.mSurfaceView);
		mSurfaceHolder = mSurfaceView.getHolder();
		mSurfaceHolder.addCallback(this);
		mSurfaceHolder.setFormat(PixelFormat.RGBA_8888); 
		mLoadingLayout = (LinearLayout) findViewById(R.id.mLoadingLayout);
		mLoading = (ImageView) findViewById(R.id.mLoading);
		mPreLoading = (ImageView)findViewById(R.id.mPreLoading);
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
		
		if (from.equals("suqare")) {
			mPreLoading.setBackgroundResource(R.drawable.tacitly_pic);
		}else {
			mPreLoading.setBackgroundResource(R.drawable.carrecorder_xcjlybj);
		}
		mPreLoading.setVisibility(View.VISIBLE);
		
		if (!TextUtils.isEmpty(image)) {
			BitmapManager.getInstance().mBitmapUtils.display(mPreLoading, image);
		}
		
		showLoading();
	}
	
	/**
	 * 设置监听
	 * @author xuhw
	 * @date 2015年4月1日
	 */
	private void setListener(){
		mSurfaceView.setOnClickListener(this);
		mPlayBtn.setOnClickListener(this);
		mPlayBigBtn.setOnClickListener(this);
		mSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar arg0) {
				int progress = mSeekBar.getProgress();
				if(null != mMediaPlayer){
					mMediaPlayer.seekTo(progress);
					if(!mMediaPlayer.isPlaying()){
						mMediaPlayer.start();
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
				if(min > 59){
					time = "00:";
				}else{
					time = min+":";
				}
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
		if(!isShow){
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
	}
	
	/**
	 * 隐藏加载中显示画面
	 * 
	 * @author xuhw
	 * @date 2015年3月8日
	 */
	private void hideLoading() {
		if(!isShow){
			isShow=true;
			
			if (mAnimationDrawable != null) {
				if (mAnimationDrawable.isRunning()) {
					mAnimationDrawable.stop();
				}
			}
			mLoadingLayout.setVisibility(View.GONE);
		}
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
						if (reset) {
							reset = false;
							showLoading();
							playVideo();
						}else{
							mMediaPlayer.start();
							mPlayBigBtn.setVisibility(View.GONE);
							mPlayBtn.setBackgroundResource(R.drawable.player_play_btn);
						}
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
			case R.id.mSurfaceView:
				if(View.VISIBLE == mTitleLayout.getVisibility()){
					mTitleLayout.setVisibility(View.GONE);
					mBottomLayout.setVisibility(View.GONE);
				}else{
					mTitleLayout.setVisibility(View.VISIBLE);
					mBottomLayout.setVisibility(View.VISIBLE);
					mTitleLayout.removeCallbacks(mRunnable);
					mTitleLayout.postDelayed(mRunnable, 3000);
				}
				break;
	
			default:
				break;
		}
	}
	
	Runnable mRunnable = new Runnable() {
		@Override
		public void run() {
			mTitleLayout.setVisibility(View.GONE);
			mBottomLayout.setVisibility(View.GONE);
		}
	};
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (!TextUtils.isEmpty(image)) {
			BitmapManager.getInstance().mBitmapUtils.clearMemoryCache(image);
		}
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
	private void playVideo() {
		System.out.println("TTT=============playVideo=");
		try {
			mMediaPlayer = new MediaPlayer(this);
			 if(getIntent().getStringExtra("from").equals("ipc")){
				 mMediaPlayer.setBufferSize(100*1024);
			 }else{
				 mMediaPlayer.setBufferSize(0);
			 }
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
		if(error){
			return;
		}
		
		long duration = mMediaPlayer.getDuration();
		GolukDebugUtils.e("xuhw", "YYYY====onCompletion===duration="+duration);
		mCurTime.setText(long2TimeStr(0));
		mTotalTime.setText(long2TimeStr(duration));
		mSeekBar.setMax((int)duration);
		mSeekBar.setProgress(0);
		
		if(null != mMediaPlayer){
			reset = true;
			isShow = false;
			mMediaPlayer.reset();
			mPreLoading.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onSeekComplete(MediaPlayer mp) {
		
	}

	@Override
	public boolean onError(MediaPlayer arg0, int arg1, int arg2) {
		if(error){
			return false;
		}
		
		String msg = "播放错误";
		switch (arg1) {
			case MediaPlayer.MEDIA_ERROR_UNKNOWN:
			case MediaPlayer.MEDIA_ERROR_UNSUPPORTED:
				msg = "视频出错，请重试！";
				break;
			case MediaPlayer.MEDIA_ERROR_TIMED_OUT:
				msg = "网络访问异常，请重试！";
				break;
				
			default:
				break;
		}
		
		if (!from.equals("local")) {
			if (!isNetworkConnected()) {
				msg = "网络访问异常，请重试！";
			}
		}
		
		error=true;
		mHandler.removeMessages(GETPROGRESS);
		GolukDebugUtils.e("xuhw", "BBBBBB=====onError==arg1="+arg1+"==arg2="+arg2);
		hideLoading();
		mCurTime.setText("00:00");
		mTotalTime.setText("00:00");
		dialog(msg);
		return false;
	}
	
	/**
	 * 提示对话框
	 * @param msg 提示信息
	 * @author xuhw
	 * @date 2015年6月5日
	 */
	private void dialog(String msg) {
		CustomDialog mCustomDialog = new CustomDialog(this);
		mCustomDialog.setCancelable(false);
		mCustomDialog.setMessage(msg, Gravity.CENTER);
		mCustomDialog.setLeftButton("确定", new OnLeftClickListener() {
			@Override
			public void onClickListener() {
				exit();
			}
		});
		mCustomDialog.show();
	}
	
	/**
	 * 检查是否有可用网络
	 * @return
	 * @author xuhw
	 * @date 2015年6月5日
	 */
	public boolean isNetworkConnected() {
		ConnectivityManager mConnectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
		if (mNetworkInfo != null) {
			return mNetworkInfo.isAvailable();
		}
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
		GolukDebugUtils.e("xuhw", "TTT=============onPrepared=");
		mIsVideoReadyToBePlayed = true;
		if (mIsVideoReadyToBePlayed && mIsVideoSizeKnown) {
			startVideoPlayback();
		}
	}

	@Override
	public void onBufferingUpdate(MediaPlayer arg0, int arg1) {
//		GolukDebugUtils.e("xuhw", "YYYY====onBufferingUpdate===arg1="+arg1);
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
		reset = false;
//		mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
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
	
	@Override
	protected void onResume() {
		super.onResume();
		GolukApplication.getInstance().setContext(this, "videoplayer");
	}

}
