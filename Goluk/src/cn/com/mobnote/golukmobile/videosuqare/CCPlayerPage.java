package cn.com.mobnote.golukmobile.videosuqare;

import java.io.IOException;
import java.util.Map;

import cn.com.mobnote.golukmobile.R;
import cn.com.tiros.utils.LogUtil;

import com.bokecc.sdk.mobile.play.DWMediaPlayer;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

public class CCPlayerPage extends Activity implements OnPreparedListener, OnBufferingUpdateListener
	,OnErrorListener, OnCompletionListener, Callback, OnClickListener{
	private SurfaceView mSurfaceView=null;
	private SurfaceHolder mSurfaceHolder=null;
	private DWMediaPlayer mDWMediaPlayer=null;
	private final String USERID = "77D36B9636FF19CF";
	private final String API_KEY = "O8g0bf8kqiWroHuJaRmihZfEmj7VWImF";
	private ImageView mPreLoading=null;
	private RingView mRingView=null;
	private DisplayImageOptions options;
	private ImageLoader imageLoader = ImageLoader.getInstance();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ccplayer);
		
		options = new DisplayImageOptions.Builder()
		.imageScaleType(ImageScaleType.IN_SAMPLE_INT)
		.cacheInMemory(true).cacheOnDisc(true).considerExifParams(true)
		.bitmapConfig(Bitmap.Config.RGB_565)
		.build();
		
		String videoid = getIntent().getStringExtra("videoid");
		String image = getIntent().getStringExtra("image");
		LogUtil.e("xuhw", "YYYYYYY======CCPlayerPage=====videoid="+videoid);
		mSurfaceView = (SurfaceView)findViewById(R.id.mSurfaceView);
		mPreLoading = (ImageView)findViewById(R.id.mPreLoading);
		mRingView = (RingView)findViewById(R.id.mRingView);
		mDWMediaPlayer = new DWMediaPlayer();
		mDWMediaPlayer.setVideoPlayInfo(videoid, USERID, API_KEY, this);
		mDWMediaPlayer.setOnPreparedListener(this);
		mDWMediaPlayer.setOnBufferingUpdateListener(this);
		mDWMediaPlayer.setOnErrorListener(this);
		mDWMediaPlayer.setOnCompletionListener(this);
		mSurfaceHolder = mSurfaceView.getHolder();
		mSurfaceHolder.addCallback(this);
		findViewById(R.id.back_btn).setOnClickListener(this);
		imageLoader.displayImage(image,mPreLoading, options, null);
		mRingView.setProcess(0);
		mRingView.setVisibility(View.VISIBLE);
		mPreLoading.setVisibility(View.VISIBLE);
		
	}

	@Override
	public void onPrepared(MediaPlayer arg0) {
		LogUtil.e("xuhw", "YYYYYYY========onPrepared===========");
//		try {
//			// 获取清晰度列表
//			Map<String, Integer> definitions = mDWMediaPlayer.getDefinitions();
//			LogUtil.e("xuhw", "YYYYYY===definitions="+definitions.toString());
//			//获取某种清晰度对应的状态码
//			int definitionCode = definitions.get("definitionInfo");
//			// 设置播放清晰度
//			mDWMediaPlayer.setDefinition(this, definitionCode);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
		arg0.start();		
	}

	@Override
	public void onCompletion(MediaPlayer arg0) {
		mDWMediaPlayer.seekTo(0);
		mDWMediaPlayer.start();
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
			mRingView.setVisibility(View.GONE);
		}else{
			mRingView.setProcess(arg1);
			mRingView.setVisibility(View.VISIBLE);
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
		mSurfaceHolder=arg0;
		mDWMediaPlayer.setDisplay(mSurfaceHolder);
		mDWMediaPlayer.prepareAsync();
		LogUtil.e("xuhw", "YYYYYYY========surfaceCreated===========");
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		mDWMediaPlayer.setDisplay(null);
		if(mDWMediaPlayer.isPlaying()){
			mDWMediaPlayer.pause();
		}
		LogUtil.e("xuhw", "YYYYYYY========surfaceDestroyed===========");
	}

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.back_btn:
			finish();
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
		}
	}

}
