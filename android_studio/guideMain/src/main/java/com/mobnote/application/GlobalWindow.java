package com.mobnote.application;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mobnote.golukmain.R;
import com.mobnote.t1sp.download.DownloaderT1spImpl;

import java.lang.reflect.Method;

import cn.com.tiros.debug.GolukDebugUtils;

public class GlobalWindow implements View.OnClickListener {

	/** 错误提示显示超时时间 */
	private static final int SHOW_TIMER_OUT = 1000;

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
	private boolean toastShowed = false;
	private ProgressBar mProgressBar = null;

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_H_COUNT:
				GlobalWindow.getInstance().dimissGlobalWindow();
				mHandler.removeMessages(MSG_H_COUNT);
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

	public void createVideoUploadWindow(int currentCount, int totalCount) {
		if (mContext == null)
			return;
		String content = mContext.getString(R.string.str_video_transfer_ongoing)
				+ currentCount + mContext.getString(R.string.str_slash) + totalCount;
		createVideoUploadWindow(content);
	}
	@SuppressLint("InflateParams")
	public void createVideoUploadWindow(String promptText) {
		GolukDebugUtils.e("", "jyf----------createVideoUploadWindow:-------111: " + promptText);
		if (null == mApplication) {
			return;
		}
		if (isShowGlobalwindow) {
			dimissGlobalWindow();
		}
		cancelTimer();
		mContext = mApplication;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean allowDrawOverlays = true;
            try {
                Class<?> c = Class.forName("android.provider.Settings");
                Method canDrawOverlays = c.getDeclaredMethod("canDrawOverlays", Context.class);

                if(canDrawOverlays != null) {
                    allowDrawOverlays = (boolean)canDrawOverlays.invoke(null, mContext);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
			if ((!Settings.canDrawOverlays(mContext) || !allowDrawOverlays)) {
				if (!toastShowed) {
					toastShowed = true;
					Toast.makeText(mContext, mContext.getString(R.string.str_system_window_not_allowed), Toast.LENGTH_LONG).show();
				}
				return;
			}
        }

		// 获取LayoutParams对象
		mWMParams = new WindowManager.LayoutParams();
		// 获取的是CompatModeWrapper对象
		mWindowManager = (WindowManager) mApplication.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);

        // 适配悬浮窗, Android 8之后需要用 TYPE_APPLICATION_OVERLAY
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mWMParams.type = LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            mWMParams.type = LayoutParams.TYPE_SYSTEM_ALERT;
        }

        // mWMParams.type = LayoutParams.TYPE_PRIORITY_PHONE;
        mWMParams.format = PixelFormat.RGBA_8888;
        // mWMParams.flags = LayoutParams.FLAG_FULLSCREEN |
        // LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        mWMParams.flags = LayoutParams.FLAG_FULLSCREEN | LayoutParams.FLAG_LAYOUT_IN_SCREEN
                | LayoutParams.FLAG_NOT_TOUCH_MODAL | LayoutParams.FLAG_NOT_FOCUSABLE;
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
		if (null != mWindowManager && null != mVideoUploadLayout) {
			mWindowManager.addView(mVideoUploadLayout, mWMParams);
		}
		isShowGlobalwindow = true;
		mPrompTv.setText(promptText);
		mVideoUploadLayout.setOnClickListener(this);
	}

	public boolean isShow() {
		return isShowGlobalwindow;
	}

	public void updateText(int currentCount, int totalCount) {
		if (!isShowGlobalwindow) {
			// 窗口未显示
			return;
		}
		if (mContext == null)
			return;
		String content = mContext.getString(R.string.str_video_transfer_ongoing)
				+ currentCount + mContext.getString(R.string.str_slash) + totalCount;
		if (null != mPrompTv) {
			mPrompTv.setText(content);
		}
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
		GolukDebugUtils.e("", "upload service--VideoShareActivity-handleCancel----Application---refreshPercent: "
				+ percent);
		if (null != mPrecentTv) {
			GolukDebugUtils.e("",
					"upload service--VideoShareActivity-handleCancel----Application---refreshPercent3333: ");
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
		mHandler.removeMessages(MSG_H_COUNT);
	}

	private void startTimer() {
		mHandler.removeMessages(MSG_H_COUNT);
		mHandler.sendEmptyMessageDelayed(MSG_H_COUNT, SHOW_TIMER_OUT);
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
		this.dimissDialog();
        mWindowManager.removeView(mVideoUploadLayout);
		isShowGlobalwindow = false;
		mStateImg = null;
		mPrompTv = null;
		mPrecentTv = null;
		mProgressBar = null;
		mVideoUploadLayout = null;
	}


	public void reset(){
		toastShowed = false;
	}

	private AlertDialog mTwoButtonDialog = null;

	public void dimissDialog() {
		if (null != mTwoButtonDialog) {
			mTwoButtonDialog.dismiss();
			mTwoButtonDialog = null;
		}
	}

	private void showDialog() {
		dimissDialog();
		Context cc = GolukApplication.getInstance().getContext();
		String title = cc.getString(R.string.str_global_dialog_title);
		String message = cc.getString(R.string.str_global_dialog_msg);
		mTwoButtonDialog = new AlertDialog.Builder(cc).create();
		mTwoButtonDialog.setTitle(title);
		mTwoButtonDialog.setMessage(message);
		mTwoButtonDialog.setCancelable(false);

		mTwoButtonDialog.setButton(DialogInterface.BUTTON_NEGATIVE, cc.getString(R.string.dialog_str_cancel),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						dimissDialog();
					}
				});

		mTwoButtonDialog.setButton(DialogInterface.BUTTON_POSITIVE, cc.getString(R.string.str_global_dialog_right_btn),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialoginterface, int i) {
						dimissDialog();
                        if (GolukApplication.getInstance().getIPCControlManager().isT2S())
                            DownloaderT1spImpl.getInstance().cancelAllDownloadTask(true);
                        else
                            GolukApplication.getInstance().userStopDownLoadList();
					}
				});
		mTwoButtonDialog.show();
	}

	@Override
	public void onClick(View v) {
		if (v == mVideoUploadLayout) {
			showDialog();
		}
	}
}
