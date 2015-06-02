package cn.com.mobnote.application;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.mobnote.golukmobile.R;
import cn.com.tiros.debug.GolukDebugUtils;

public class GlobalWindow {

	private GolukApplication mApplication = null;
	/** 全局提示框 */
	public WindowManager mWindowManager = null;
	public WindowManager.LayoutParams mWMParams = null;

	/** 总体布局 */
	public RelativeLayout mVideoUploadLayout = null;
	/** 显示当前的传输状态，传输中/传输完成/传输失败 */
	private ImageView mStateImg = null;
	/** 传输过程中提示文字 */
	private TextView mPrompTv = null;
	/** 百分比 显示 */
	public TextView mPrecentTv = null;

	/** 用来控制顶层窗口只显示一次 */
	private boolean isShowGlobalwindow = false;

	private Context mContext = null;

	private static GlobalWindow mInstance = new GlobalWindow();

	public final int MSG_H_COUNT = 10;
	/** 统计 */
	private int finishShowCount = 0;

	private ProgressBar mProgressBar = null;

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_H_COUNT:
				finishShowCount++;
				if (finishShowCount >= 3) {
					GlobalWindow.getInstance().dimissGlobalWindow();
					mHandler.removeMessages(MSG_H_COUNT);
					finishShowCount = 0;
				} else {
					mHandler.sendEmptyMessageDelayed(MSG_H_COUNT, 1000);
				}
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}

	};

	private GlobalWindow() {

	}

	public void setApplication(GolukApplication app) {
		mApplication = app;
	}
	
	public GolukApplication getApplication() {
		return mApplication;
	}

	public static GlobalWindow getInstance() {
		return mInstance;
	}

	@SuppressLint("InflateParams")
	public void createVideoUploadWindow(String promptText) {
		GolukDebugUtils.e("", "jyf----------createVideoUploadWindow:-------111: " + promptText);
		if (null == mApplication) {
			return;
		}
		
		GolukDebugUtils.e("", "jyf----------createVideoUploadWindow:-------2222: ");

		if (isShowGlobalwindow) {
			dimissGlobalWindow();
		}
		
		GolukDebugUtils.e("", "jyf----------createVideoUploadWindow:-------333333: ");

		cancelTimer();

		GolukDebugUtils.e("", "jyf----------createVideoUploadWindow:-------444444: ");

		mContext = mApplication.getContext();

		// 获取LayoutParams对象
		mWMParams = new WindowManager.LayoutParams();
		// 获取的是CompatModeWrapper对象
		mWindowManager = (WindowManager) mApplication.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);

		mWMParams.type = LayoutParams.TYPE_SYSTEM_ERROR;
		// mWMParams.type = LayoutParams.TYPE_PRIORITY_PHONE;
		mWMParams.format = PixelFormat.RGBA_8888;
		// mWMParams.flags = LayoutParams.FLAG_FULLSCREEN |
		// LayoutParams.FLAG_LAYOUT_IN_SCREEN;
		mWMParams.flags = LayoutParams.FLAG_FULLSCREEN | LayoutParams.FLAG_LAYOUT_IN_SCREEN
				| LayoutParams.FLAG_NOT_TOUCH_MODAL | LayoutParams.FLAG_NOT_FOCUSABLE | LayoutParams.FLAG_NOT_TOUCHABLE;
		mWMParams.gravity = Gravity.LEFT | Gravity.TOP;
		mWMParams.x = 0;
		mWMParams.y = 0;
		mWMParams.width = WindowManager.LayoutParams.MATCH_PARENT;
		// /获得根视图
		// View v = ((Activity)
		// mContext).getWindow().findViewById(Window.ID_ANDROID_CONTENT);
		// /状态栏标题栏的总高度,所以标题栏的高度为top2-top
		// int top2 = v.getTop();
		mWMParams.height = 85;

		LayoutInflater inflater = LayoutInflater.from(mContext);
		mVideoUploadLayout = (RelativeLayout) inflater.inflate(R.layout.video_share_upload_window, null);
		mStateImg = (ImageView) mVideoUploadLayout.findViewById(R.id.video_loading_img);
		mPrompTv = (TextView) mVideoUploadLayout.findViewById(R.id.video_upload_text);
		mPrecentTv = (TextView) mVideoUploadLayout.findViewById(R.id.video_upload_percent);

		mProgressBar = (ProgressBar) mVideoUploadLayout.findViewById(R.id.progress_horizontal);
		mProgressBar.setProgress(0);

		// 显示顶层窗口
		mWindowManager.addView(mVideoUploadLayout, mWMParams);

		GolukDebugUtils.e("", "jyf----------createVideoUploadWindow:----55555---showTopwindow: ");

		isShowGlobalwindow = true;

		mPrompTv.setText(promptText);
		// 开启传输中的菊花
		Animation rotateAnimation = AnimationUtils.loadAnimation(mContext, R.anim.upload_loading);
		LinearInterpolator lin = new LinearInterpolator();
		rotateAnimation.setInterpolator(lin);
		mStateImg.startAnimation(rotateAnimation);

	}

	public boolean isShow() {
		return isShowGlobalwindow;
	}

	/**
	 * 更新文本信息
	 * 
	 * @param promptText
	 *            文本信息
	 * @author xuhw
	 * @date 2015年4月23日
	 */
	public void updateText(String promptText) {
		if (!isShowGlobalwindow) {
			// 窗口未显示
			return;
		}

		if (null != mPrompTv) {
			mPrompTv.setText(promptText);
		}
	}

	/**
	 * 更新当前的进度
	 * 
	 * @param percent
	 *            　百分比
	 * @author jiayf
	 * @date Apr 17, 2015
	 */
	public void refreshPercent(int percent) {
		GolukDebugUtils.e("", "jyf----------createVideoUploadWindow:-------refreshPercent: " + percent);
		if (!isShowGlobalwindow) {
			// 窗口未显示
			return;
		}
		if (null != mProgressBar) {
			mProgressBar.setProgress(percent);
		}
		GolukDebugUtils.e("","upload service--VideoShareActivity-handleCancel----Application---refreshPercent: " + percent);
		if (null != mPrecentTv) {
			GolukDebugUtils.e("","upload service--VideoShareActivity-handleCancel----Application---refreshPercent3333: ");
			mPrecentTv.setText("" + percent + "%");
		}
	}

	/**
	 * 操作失败
	 * 
	 * @param msg
	 *            提示信息
	 * @author jiayf
	 * @date Apr 17, 2015
	 */
	public void toFailed(String msg) {
		if (!isShowGlobalwindow) {
			// 窗口未显示
			return;
		}
		if (null != mStateImg) {
			mStateImg.clearAnimation();
			mStateImg.setBackgroundResource(R.drawable.tips_close);
		}

		if (null != mPrompTv) {
			mPrompTv.setText(msg);
		}
		
		if (null != mPrecentTv) {
			mPrecentTv.setVisibility(View.GONE);
		}

		startTimer();

	}

	private void cancelTimer() {
		finishShowCount = 0;
		mHandler.removeMessages(MSG_H_COUNT);
	}

	private void startTimer() {
		finishShowCount = 0;
		mHandler.removeMessages(MSG_H_COUNT);
		mHandler.sendEmptyMessageDelayed(MSG_H_COUNT, 1000);
	}

	/**
	 * 操作成功
	 * 
	 * @param message
	 *            提示信息
	 * @author jiayf
	 * @date Apr 17, 2015
	 */
	public void topWindowSucess(String message) {
		GolukDebugUtils.e("", "jyf----------topWindowSucess:-------refreshPercent: ");
		if (!isShowGlobalwindow) {
			// 窗口未显示
			return;
		}

		if (null != mStateImg) {
			mStateImg.clearAnimation();
			mStateImg.setBackgroundResource(R.drawable.tips_success);
		}

		if (null != mPrompTv) {
			mPrompTv.setText(message);
		}

		if (null != mPrecentTv) {
			mPrecentTv.setText("100%");
		}

		startTimer();
	}

	/**
	 * 销毁全局窗口
	 * 
	 * @author jiayf
	 * @date Apr 17, 2015
	 */
	public void dimissGlobalWindow() {
		GolukDebugUtils.e("", "jyf----------createVideoUploadWindow:-------dimissGlobalWindow: ");
		if (!isShowGlobalwindow) {
			return;
		}
		isShowGlobalwindow = false;
		mWindowManager.removeView(mVideoUploadLayout);
		mStateImg = null;
		mPrompTv = null;
		mPrecentTv = null;
		mProgressBar = null;
		mVideoUploadLayout = null;
	}
}
