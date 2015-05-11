package cn.com.mobnote.golukmobile.videosuqare;

import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.util.BitmapManager;
import cn.com.tiros.utils.LogUtil;

import com.bokecc.sdk.mobile.play.DWMediaPlayer;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.os.Handler;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class CCPlayerPage extends Activity implements OnPreparedListener, OnBufferingUpdateListener
	,OnErrorListener, OnCompletionListener, Callback, OnClickListener{
	private SurfaceView mSurfaceView=null;
	private SurfaceHolder mSurfaceHolder=null;
	private DWMediaPlayer mDWMediaPlayer=null;
	private final String USERID = "77D36B9636FF19CF";
	private final String API_KEY = "O8g0bf8kqiWroHuJaRmihZfEmj7VWImF";
	private ImageView mPreLoading=null;
	private ImageView mPlayBigBtn=null;
	private boolean allow=false;
	/** 加载中布局 */
	private LinearLayout mLoadingLayout = null;
	/** 加载中动画显示控件 */
	private ImageView mLoading = null;
	/** 加载中动画对象 */
	private AnimationDrawable mAnimationDrawable = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ccplayer);
		
		String videoid = getIntent().getStringExtra("videoid");
		String image = getIntent().getStringExtra("image");
		LogUtil.e("xuhw", "YYYYYYY======CCPlayerPage=====videoid="+videoid);
		mSurfaceView = (SurfaceView)findViewById(R.id.mSurfaceView);
		mPreLoading = (ImageView)findViewById(R.id.mPreLoading);
		mPlayBigBtn = (ImageView)findViewById(R.id.mPlayBigBtn);
		mDWMediaPlayer = new DWMediaPlayer();
		mDWMediaPlayer.setVideoPlayInfo(videoid, USERID, API_KEY, this);
		mDWMediaPlayer.setOnPreparedListener(this);
		mDWMediaPlayer.setOnBufferingUpdateListener(this);
		mDWMediaPlayer.setOnErrorListener(this);
		mDWMediaPlayer.setOnCompletionListener(this);
		mSurfaceHolder = mSurfaceView.getHolder();
		mSurfaceHolder.addCallback(this);
		mSurfaceView.setOnClickListener(this);
		findViewById(R.id.back_btn).setOnClickListener(this);
		BitmapManager.getInstance().mBitmapUtils.display(mPreLoading, image);
		mPreLoading.setVisibility(View.VISIBLE);
		mLoadingLayout = (LinearLayout) findViewById(R.id.mLoadingLayout);
		mLoading = (ImageView) findViewById(R.id.mLoading);
		mLoading.setBackgroundResource(R.anim.video_loading);
		mAnimationDrawable = (AnimationDrawable) mLoading.getBackground();
		showLoading();
	}
	
	Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
				case 1:
					mHandler.removeMessages(1);
					if(null != mDWMediaPlayer){
						if(mDWMediaPlayer.isPlaying()){
							hideLoading();
						}else{
							mHandler.sendEmptyMessageDelayed(1, 500);
						}
					}
					break;
	
				default:
					break;
			}
		};
	};
	
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
	public void onPrepared(MediaPlayer arg0) {
		LogUtil.e("xuhw", "YYYYYYY========onPrepared===========");
		arg0.start();		
	}

	@Override
	public void onCompletion(MediaPlayer arg0) {
		mPlayBigBtn.setVisibility(View.VISIBLE);
//		if(null != mDWMediaPlayer){
//			mDWMediaPlayer.seekTo(0);
//			mDWMediaPlayer.start();
//			mDWMediaPlayer.prepareAsync();
//		}
		
		LogUtil.e("xuhw", "YYYYYYY========onCompletion===========");
	}

	@Override
	public boolean onError(MediaPlayer arg0, int arg1, int arg2) {
		LogUtil.e("xuhw", "YYYYYYY========onError===========");
		return false;
	}

	@Override
	public void onBufferingUpdate(MediaPlayer arg0, int arg1) {
		// TODO Auto-generated method stub
		LogUtil.e("xuhw", "YYYYYYY========onBufferingUpdate==========arg1="+arg1);
		if(arg1 >= 100){
			mPreLoading.setVisibility(View.GONE);
			hideLoading();
		}else{
			mPreLoading.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int width, int height) {
		arg0.setFixedSize(width, height);
		mDWMediaPlayer.setDisplay(arg0);
	}

	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		allow=true;
		mSurfaceHolder=arg0;
		mDWMediaPlayer.setDisplay(mSurfaceHolder);
		mDWMediaPlayer.prepareAsync();
		mHandler.sendEmptyMessage(1);
		LogUtil.e("xuhw", "YYYYYYY========surfaceCreated===========");
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		allow=false;
		if(null != mDWMediaPlayer){
			mDWMediaPlayer.setDisplay(null);
			if(mDWMediaPlayer.isPlaying()){
				mDWMediaPlayer.pause();
			}
		}
		
		LogUtil.e("xuhw", "YYYYYYY========surfaceDestroyed===========");
	}

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.back_btn:
			exit(); 
			break;
		case R.id.mSurfaceView:
			if(allow){
				if(mDWMediaPlayer.isPlaying()){
					mDWMediaPlayer.pause();
					mPlayBigBtn.setVisibility(View.VISIBLE);
				}else{
					mDWMediaPlayer.start();
					mPlayBigBtn.setVisibility(View.GONE);
				}
			}
			break;

		default:
			break;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(null != mDWMediaPlayer){
			mDWMediaPlayer.release();
			mDWMediaPlayer = null;
		}
	}
	
	private void exit(){
		hideLoading();
		if(null != mDWMediaPlayer){
			if(mDWMediaPlayer.isPlaying()){
				mDWMediaPlayer.pause();
			}
		}
		finish();
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {
    	if(keyCode==KeyEvent.KEYCODE_BACK){
    		exit(); 
        	return true;
        }else
        	return super.onKeyDown(keyCode, event); 
	}
	
}
