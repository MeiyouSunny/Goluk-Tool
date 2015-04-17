package cn.com.mobnote.application;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.mobnote.golukmobile.R;

public class GlobalWindow {
	
	/** 全局提示框 */
	public WindowManager mWindowManager = null;
	public WindowManager.LayoutParams mWMParams = null;
	public RelativeLayout mVideoUploadLayout = null;
	
	private GolukApplication mApplication = null;
	private TextView tv = null;
	
	@SuppressLint("InflateParams")
	public void createVideoUploadWindow(Context mContext){
		//获取LayoutParams对象
		mWMParams = new WindowManager.LayoutParams();
		//获取的是CompatModeWrapper对象
		mWindowManager = (WindowManager)mApplication.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
		
		mWMParams.type = LayoutParams.TYPE_SYSTEM_ERROR;
		//mWMParams.type = LayoutParams.TYPE_PRIORITY_PHONE;
		mWMParams.format = PixelFormat.RGBA_8888;
		//mWMParams.flags = LayoutParams.FLAG_FULLSCREEN | LayoutParams.FLAG_LAYOUT_IN_SCREEN;
		mWMParams.flags = LayoutParams.FLAG_FULLSCREEN | LayoutParams.FLAG_LAYOUT_IN_SCREEN | LayoutParams.FLAG_NOT_TOUCH_MODAL |LayoutParams.FLAG_NOT_FOCUSABLE | LayoutParams.FLAG_NOT_TOUCHABLE;
		mWMParams.gravity = Gravity.LEFT | Gravity.TOP;
		mWMParams.x = 0;
		mWMParams.y = 0;
		mWMParams.width = WindowManager.LayoutParams.MATCH_PARENT;
		///获得根视图
		View v = ((Activity) mContext).getWindow().findViewById(Window.ID_ANDROID_CONTENT);
		///状态栏标题栏的总高度,所以标题栏的高度为top2-top
		int top2 = v.getTop();
		mWMParams.height = top2;
		
		LayoutInflater inflater = LayoutInflater.from(mContext);
		mVideoUploadLayout = (RelativeLayout) inflater.inflate(R.layout.video_share_upload_window, null);
		tv = (TextView) mVideoUploadLayout.findViewById(R.id.video_upload_percent);
		mWindowManager.addView(mVideoUploadLayout,mWMParams);
		ImageView view = (ImageView)mVideoUploadLayout.findViewById(R.id.video_loading_img);
		Animation rotateAnimation = AnimationUtils.loadAnimation(mContext, R.anim.upload_loading);
		LinearInterpolator lin = new LinearInterpolator();
		rotateAnimation.setInterpolator(lin);
		view.startAnimation(rotateAnimation);
		//setContentView(R.layout.main);
		//mFloatView = (Button)mFloatLayout.findViewById(R.id.float_id);
	}

}
