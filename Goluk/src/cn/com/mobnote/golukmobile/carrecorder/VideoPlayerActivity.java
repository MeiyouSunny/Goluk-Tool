package cn.com.mobnote.golukmobile.carrecorder;

import java.io.IOException;

import cn.com.mobnote.golukmobile.R;
import android.app.Activity;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.VideoView;

public class VideoPlayerActivity extends Activity implements OnCompletionListener, OnBufferingUpdateListener
,OnErrorListener, OnInfoListener, OnPreparedListener, OnClickListener, SurfaceHolder.Callback{
	private MediaPlayer mMediaPlayer=null;
	private SurfaceHolder mSurfaceHolder=null;
	private SurfaceView mSurfaceView=null;
	private String playUrl=null;
	/** 加载中布局 */
	private LinearLayout mLoadingLayout = null;
	/** 加载中动画显示控件 */
	private ImageView mLoading = null;
	/** 加载中动画对象 */
	private AnimationDrawable mAnimationDrawable = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.carrecorder_videoplayer);
		initView();
		
		int type = getIntent().getIntExtra("type", -1);
		String name = getIntent().getStringExtra("filename");
		TextView title = (TextView)findViewById(R.id.title);
		title.setText(name);
//		String time = getIntent().getStringExtra("time");
		if(4 == type){
			playUrl="http://192.168.43.234:5080/rec/wonderful/"+name;
		}else if(2 == type){
			playUrl="http://192.168.43.234:5080/rec/urgent/"+name;
		}else{
			playUrl="http://192.168.43.234:5080/rec/normal/"+name;
		}
		
		showLoading();
	}
	
	private void initView(){
		mSurfaceView = (SurfaceView)findViewById(R.id.mSurfaceView);
		mSurfaceHolder = mSurfaceView.getHolder();
		mSurfaceHolder.addCallback(this);
		mLoadingLayout = (LinearLayout) findViewById(R.id.mLoadingLayout);
		mLoading = (ImageView) findViewById(R.id.mLoading);
		mLoading.setBackgroundResource(R.anim.video_loading);
		mAnimationDrawable = (AnimationDrawable) mLoading.getBackground();
		findViewById(R.id.back_btn).setOnClickListener(this);
		findViewById(R.id.title).setOnClickListener(this);
		
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
	public void onCompletion(MediaPlayer arg0) {
//		arg0.start();
	}

	@Override
	public boolean onError(MediaPlayer arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
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
	public void onClick(View arg0) {
		switch (arg0.getId()) {
			case R.id.back_btn:
			case R.id.title:
				finish();
				break;
	
			default:
				break;
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(null != mMediaPlayer){
			mMediaPlayer.stop();
			mMediaPlayer.release();
			mMediaPlayer = null;
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
	}

	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		mSurfaceHolder=arg0;
		mHandle.sendEmptyMessageDelayed(1, 100);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		// TODO Auto-generated method stub
	}

	private void start(){
		try {
			mMediaPlayer = new MediaPlayer();
			mMediaPlayer.setDisplay(mSurfaceHolder);
//			mMediaPlayer.setSurface(mSurfaceHolder.getSurface());
			mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);  
			mMediaPlayer.setOnInfoListener(this);
			mMediaPlayer.setOnBufferingUpdateListener(this);
			mMediaPlayer.setOnPreparedListener(this);
			mMediaPlayer.setDataSource(playUrl);
			mMediaPlayer.prepareAsync();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	Handler mHandle = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 1:
				start();
				break;

			default:
				break;
			}
		};
	};

	@Override
	public void onPrepared(MediaPlayer arg0) {
		arg0.start();
	}

	@Override
	public void onBufferingUpdate(MediaPlayer arg0, int arg1) {
		System.out.println("TTT====arg"+arg1);
		if(arg1 > 7){
			hideLoading();
		}
	}

}
