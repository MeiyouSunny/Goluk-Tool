package cn.com.mobnote.golukmobile.carrecorder;

import java.io.File;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;
import android.widget.SeekBar.OnSeekBarChangeListener;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.BaseActivity;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.util.BitmapManager;
import cn.com.mobnote.golukmobile.carrecorder.util.GFileUtils;
import cn.com.mobnote.golukmobile.carrecorder.util.SettingUtils;
import cn.com.mobnote.golukmobile.carrecorder.util.SoundUtils;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomDialog;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomDialog.OnLeftClickListener;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomVideoView;
import cn.com.mobnote.util.GolukUtils;
import cn.com.tiros.debug.GolukDebugUtils;

@SuppressLint({ "NewApi", "HandlerLeak" })
public class VideoPlayer extends Activity implements OnClickListener, OnInfoListener, OnErrorListener, OnCompletionListener{
	/** 视频播放器 */
	private CustomVideoView mVideoView = null;
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
	/** 原始视频宽度 */
	private int mVideoWidth;
	/** 原始视频高度 */
	private int mVideoHeight;
	/** 播放器尺寸变化标识 */
	private boolean mIsVideoSizeKnown = false;
	/** 播放器准备就绪标识 */
	private boolean mIsVideoReadyToBePlayed = false;
	/** 显示当前播放时间 */
	private TextView mCurTime=null;
	/** 显示视频总时间 */
	private TextView mTotalTime=null;
	/** 播放按钮 */
	private ImageButton mPlayBtn=null;
	/** 居中播放大按钮 */
	private ImageButton mPlayBigBtn=null;
	/** 播放器进度显示 */
	private SeekBar mSeekBar=null;
	/** 顶部布局 */
	private RelativeLayout mTitleLayout=null;
	/** 底部布局 */
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
	/** 网络连接超时 */
	private int networkConnectTimeOut = 0;
	private RelativeLayout mClickLayout = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_videoview);
		getPlayAddr();
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
						
						netWorkTimeoutCheck();
						updatePlayerProcess();
						mHandler.sendEmptyMessageDelayed(GETPROGRESS, 100);
						break;
						
					default:
						break;
				}
			};
		};
		
		mVideoView.setVideoURI(Uri.parse(playUrl));
		mVideoView.start();
		mHandler.sendEmptyMessage(GETPROGRESS);
	}
	
	/**
	 * 获取播放地址
	 * @author xuhw
	 * @date 2015年6月5日
	 */
	private void getPlayAddr(){
		from = getIntent().getStringExtra("from");
		image = getIntent().getStringExtra("image");
		filename = getIntent().getStringExtra("filename");
		GolukDebugUtils.e("xuhw", "YYYYYY==VideoPlayerActivity==2222===filename="+filename+"===from="+from);
		String ip = SettingUtils.getInstance().getString("IPC_IP");
		if (TextUtils.isEmpty(from)) {
			return;
		}
		
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
		
		GolukDebugUtils.e("xuhw", "YYYYYY==VideoPlayerActivity==playUrl="+playUrl);
	}
	
	/**
	 * 初始化控件
	 * @author xuhw
	 * @date 2015年3月31日
	 */
	private void initView(){
		mVideoLayout = (RelativeLayout)findViewById(R.id.mVideoLayout);
		mClickLayout = (RelativeLayout)findViewById(R.id.mClickLayout);
		mTitleLayout = (RelativeLayout)findViewById(R.id.title_layout);
		mBottomLayout = (RelativeLayout)findViewById(R.id.mBottomLayout);
		mVideoView = (CustomVideoView)findViewById(R.id.mVideoView);
		mLoadingLayout = (LinearLayout) findViewById(R.id.mLoadingLayout);
		mLoading = (ImageView) findViewById(R.id.mLoading);
		mPreLoading = (ImageView)findViewById(R.id.mPreLoading);
		mLoading.setBackgroundResource(R.anim.video_loading);
		mAnimationDrawable = (AnimationDrawable) mLoading.getBackground();
		
		TextView title = (TextView)findViewById(R.id.title);
		title.setText(filename);
		
		mCurTime = (TextView) findViewById(R.id.mCurTime);
		mTotalTime = (TextView) findViewById(R.id.mTotalTime);
		mPlayBtn = (ImageButton) findViewById(R.id.mPlayBtn);
		mPlayBigBtn = (ImageButton) findViewById(R.id.mPlayBigBtn);
		mSeekBar = (SeekBar) findViewById(R.id.mSeekBar);
		
		mPreLoading.setBackgroundResource(R.drawable.tacitly_pic);
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
		mClickLayout.setOnClickListener(this);
		findViewById(R.id.back_btn).setOnClickListener(this);
		findViewById(R.id.title).setOnClickListener(this);
		mPlayBtn.setOnClickListener(this);
		mPlayBigBtn.setOnClickListener(this);
		mSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar arg0) {
				int progress = mSeekBar.getProgress();
//				if(null != mMediaPlayer){
//					mMediaPlayer.seekTo(progress);
//					if(!mMediaPlayer.isPlaying()){
//						mMediaPlayer.start();
//					}
//				}
				
				if (null != mVideoView) {
					mVideoView.seekTo(progress);
				}
				
			}
				
			@Override
			public void onStartTrackingTouch(SeekBar arg0) {
			}
			
			@Override
			public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
			}
		});

		
		mVideoView.setOnErrorListener(this);
		if (GolukUtils.getSystemSDK() >= 17) {
			try {
				mVideoView.setOnInfoListener(this);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		
		mVideoView.setOnCompletionListener(this);
		mVideoView.setZOrderMediaOverlay(true);
		
		mVideoView.setOnPreparedListener(new OnPreparedListener() {
			@Override
			public void onPrepared(MediaPlayer arg0) {
				mVideoView.setVideoWidth(arg0.getVideoWidth());
				mVideoView.setVideoHeight(arg0.getVideoHeight());
			}
		});
	}
	
	/**
	 * 无网络超时检查
	 * @author xuhw
	 * @date 2015年6月5日
	 */
	private void netWorkTimeoutCheck() {
		if (!from.equals("suqare")) {
			return;
		}
		
		if (!isNetworkConnected()) {
			networkConnectTimeOut++;
			if (networkConnectTimeOut > 100) {
				if (!reset) {
					hideLoading();
					dialog("网络访问异常，请重试！");
					return;
				}
			}
		}else{
			networkConnectTimeOut = 0;
		}
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
	
	/**
	 * 更新播放器显示进度
	 * @author xuhw
	 * @date 2015年6月5日
	 */
	private void updatePlayerProcess() {
		if (null == mVideoView) {
			return;
		}
		
		if(mVideoView.isPlaying()){
			hideLoading();
			mPreLoading.setVisibility(View.GONE);
			long curPosition = mVideoView.getCurrentPosition();
			long duration = mVideoView.getDuration();
			
			GolukDebugUtils.e("xuhw", "TTT========duration=="+duration+"=====curPosition="+curPosition);
			mCurTime.setText(long2TimeStr(curPosition));
			mTotalTime.setText(long2TimeStr(duration));
			mSeekBar.setMax((int)duration);
			mSeekBar.setProgress((int)curPosition);
			mPlayBigBtn.setVisibility(View.GONE);
			mPlayBtn.setBackgroundResource(R.drawable.player_pause_btn);
		}else{
//			mPlayBigBtn.setVisibility(View.VISIBLE);
			mPlayBtn.setBackgroundResource(R.drawable.player_play_btn);
		}
	}
	
	/**
	 * 显示加载中布局
	 * 
	 * @author xuhw
	 * @date 2015年3月8日
	 */
	private void showLoading() {
		if (!isShow) {
			isShow = true;
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
		if (isShow) {
			isShow = false;
			
			mPreLoading.setVisibility(View.GONE);
			if (mAnimationDrawable != null) {
				if (mAnimationDrawable.isRunning()) {
					mAnimationDrawable.stop();
				}
			}
			mLoadingLayout.setVisibility(View.GONE);
		}
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
	
	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
			case R.id.back_btn:
			case R.id.title:
				exit();
				break;
			case R.id.mPlayBtn:
//				if(null != mMediaPlayer){
//					if(mMediaPlayer.isPlaying()){
//						mMediaPlayer.pause();
//						mPlayBigBtn.setVisibility(View.VISIBLE);
//						mPlayBtn.setBackgroundResource(R.drawable.player_pause_btn);
//						
//					}else{
//						if (reset) {
//							reset = false;
//							showLoading();
//							playVideo();
//						}else{
//							mMediaPlayer.start();
//							mPlayBigBtn.setVisibility(View.GONE);
//							mPlayBtn.setBackgroundResource(R.drawable.player_play_btn);
//						}
//					}
//				}else{
//					playVideo();
//				}
				break;
			case R.id.mPlayBigBtn:
//				if(null != mMediaPlayer){
//					if(mMediaPlayer.isPlaying()){
//						mMediaPlayer.pause();
//					}else{
//						mMediaPlayer.start();
//						mPlayBigBtn.setVisibility(View.GONE);
//					}
//				}else{
//					playVideo();
//				}
				
				break;
			case R.id.mClickLayout:
				if (View.VISIBLE == mTitleLayout.getVisibility()) {
					GolukDebugUtils.e("", "BBBBBBBB========mVideoView====11====");
					mTitleLayout.setVisibility(View.GONE);
					mBottomLayout.setVisibility(View.GONE);
				} else {
					GolukDebugUtils.e("", "BBBBBBBB========mVideoView=====22===");
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
	public boolean onError(MediaPlayer arg0, int arg1, int arg2) {
		if(error){
			return false;
		}
		
		String msg = "播放错误";
		switch (arg1) {
			case 1:
			case -1010:
				msg = "视频出错，请重试！";
				break;
			case -110:
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
		
		return true;
	}
	
	@Override
	public void onCompletion(MediaPlayer arg0) {
		if(error){
			return;
		}
		
		long duration = mVideoView.getDuration();
		GolukDebugUtils.e("xuhw", "YYYY====onCompletion===duration="+duration);
		mCurTime.setText(long2TimeStr(0));
		mTotalTime.setText(long2TimeStr(duration));
		mSeekBar.setMax((int)duration);
		mSeekBar.setProgress(0);
		
		if(null != mVideoView){
//			reset = true;
//			isShow = false;
//			mVideoView.reset();
//			mPreLoading.setVisibility(View.VISIBLE);
		}
		
	}
	
	
	
		
	@Override
	protected void onPause() {
		super.onPause();
//	    mVideoView.start();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		GolukApplication.getInstance().setContext(this, "videoplayer");
//		mVideoView.pause();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (!TextUtils.isEmpty(image)) {
			BitmapManager.getInstance().mBitmapUtils.clearMemoryCache(image);
		}
		
		if (null != mVideoView) {
			mVideoView.stopPlayback();
			mVideoView = null;
		}
		
	}
	
	private void exit(){
//		android.os.Process.killProcess(android.os.Process.myPid());
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
	
	RelativeLayout mVideoLayout;
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		int          height     = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200.0f,getResources().getDisplayMetrics());
	    LayoutParams params     = (RelativeLayout.LayoutParams)mVideoLayout.getLayoutParams();
	    int w = SoundUtils.getInstance().getDisplayMetrics().widthPixels;
    	int h = SoundUtils.getInstance().getDisplayMetrics().heightPixels;
	    if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
	    	
	    	
	    	GolukDebugUtils.e("", "BBBBBBB======ORIENTATION_LANDSCAPE=========w="+w+"=h="+h);
	        params.width  = w;
	        params.height = RelativeLayout.LayoutParams.MATCH_PARENT;
	        
	        mVideoView.getHolder().setFixedSize(w, h);
	        mVideoView.getHolder().setSizeFromLayout();
	        mVideoLayout.requestLayout();
	    } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
	    	GolukDebugUtils.e("", "BBBBBBB======ORIENTATION_PORTRAIT==========");

	        params.width  = w;
	        params.height = h;
	        
	        mVideoView.getHolder().setFixedSize(w, h);
	        mVideoLayout.requestLayout();
	    }

	    super.onConfigurationChanged(newConfig);
	} 
	

}
