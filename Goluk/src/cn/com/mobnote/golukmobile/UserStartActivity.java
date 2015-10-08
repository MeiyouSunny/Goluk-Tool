package cn.com.mobnote.golukmobile;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.application.SysApplication;
import cn.com.mobnote.golukmobile.carrecorder.util.ImageManager;
import cn.com.mobnote.golukmobile.carrecorder.util.SoundUtils;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomVideoView;
import cn.com.tiros.debug.GolukDebugUtils;

/**
 * 
 * 个人中心启动模块
 * 
 * 1、我有Goluk——跳转到登陆界面 2、随便看看——跳转到app主页
 * 
 * @author mobnote
 * 
 */
public class UserStartActivity extends BaseActivity implements OnClickListener, OnErrorListener {

	private ImageView mImageViewHave, mImageViewLook;
	//
	private Context mContext = null;
	private GolukApplication mApp = null;
	/** 如果是注销进来的，需要将手机号填进去 **/
	private SharedPreferences mPreferences = null;
	private String phone = null;
	public static Handler mHandler = null;
	public static final int EXIT = -1;
	private Editor mEditor = null;
	private Bitmap mBGBitmap = null;
	private CustomVideoView videoStart = null;
	private int screenWidth = SoundUtils.getInstance().getDisplayMetrics().widthPixels;
	private int screenHeight = SoundUtils.getInstance().getDisplayMetrics().heightPixels;
	/** 我有Goluk和随便看看两个按钮 **/
	private LinearLayout mClickLayout = null;
	/** 欢迎页右上角关闭按钮 **/
	private ImageView mImageClose = null;
	/**true欢迎页   false开屏页**/
	private boolean judge = false;

	@SuppressLint("HandlerLeak")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.user_start);
		mBGBitmap = ImageManager.getBitmapFromResource(R.drawable.guide_page, screenWidth, screenHeight);
		RelativeLayout main = (RelativeLayout) findViewById(R.id.main);
		main.setBackgroundDrawable(new BitmapDrawable(mBGBitmap));

		mContext = this;
		mApp = (GolukApplication) getApplication();
		mApp.setContext(mContext, "UserStart");

		SysApplication.getInstance().addActivity(this);

		initView();
		// true ----欢迎页 false开屏页
		Intent it = getIntent();
		judge = it.getBooleanExtra("judgeVideo", false);
		GolukDebugUtils.e("lily", judge + "--------judgeVideo-----");
		if (judge) {
			mClickLayout.setVisibility(View.GONE);
			mImageClose.setVisibility(View.VISIBLE);
		} else {
			mClickLayout.setVisibility(View.VISIBLE);
			mImageClose.setVisibility(View.GONE);
		}
		videoStart = (CustomVideoView) findViewById(R.id.videoStart);
		videoStart.setVideoURI(Uri.parse("android.resource://cn.com.mobnote.golukmobile/" + R.raw.start_video));
		videoStart.start();
		videoStart.setOnErrorListener(this);
		videoStart.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

			@Override
			public void onPrepared(MediaPlayer mp) {
				mp.start();
				mp.setLooping(true);

			}
		});

		videoStart.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

			@Override
			public void onCompletion(MediaPlayer mp) {
				videoStart.setVideoPath("android.resource://cn.com.mobnote.golukmobile/" + R.raw.start_video);
				videoStart.start();

			}
		});

		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case EXIT:
					mHandler = null;
					finish();
					break;

				default:
					break;
				}
				super.handleMessage(msg);
			}
		};
	}

	public void initView() {
		mClickLayout = (LinearLayout) findViewById(R.id.user_start_click);
		mImageViewHave = (ImageView) findViewById(R.id.user_start_have);
		mImageViewLook = (ImageView) findViewById(R.id.user_start_look);
		mImageClose = (ImageView) findViewById(R.id.click_close_btn);
		// 获取注销成功后传来的信息
		mPreferences = getSharedPreferences("setup", MODE_PRIVATE);
		phone = mPreferences.getString("setupPhone", "");// 最后一个参数为默认值

		mImageViewHave.setOnClickListener(this);
		mImageViewLook.setOnClickListener(this);
		mImageClose.setOnClickListener(this);
	}

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.user_start_have:
			// 我有Goluk
			Intent itHave = new Intent(UserStartActivity.this, MainActivity.class);
			itHave.putExtra("userstart", "start_have");
			startActivity(itHave);
			this.finish();
			break;

		case R.id.user_start_look:
			// 随便看看
			Intent itLook = new Intent(UserStartActivity.this, MainActivity.class);
			GolukDebugUtils.i("lily", "======MainActivity==UserStartActivity====");
			startActivity(itLook);
			this.finish();
			break;
		// 关闭
		case R.id.click_close_btn:
			finish();
			break;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (null != mBGBitmap) {
			if (!mBGBitmap.isRecycled()) {
				mBGBitmap.recycle();
				mBGBitmap = null;
			}
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if(!judge){
				if (null != mBaseApp) {
					mBaseApp.setExit(true);
					mBaseApp.destroyLogic();
					mBaseApp.appFree();
				}
			}
			finish();
		}
		return false;
	}

	@Override
	public boolean onError(MediaPlayer arg0, int arg1, int arg2) {
		return true;
	}
}
