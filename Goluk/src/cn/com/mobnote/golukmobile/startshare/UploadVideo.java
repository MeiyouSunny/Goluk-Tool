package cn.com.mobnote.golukmobile.startshare;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import cn.com.mobnote.application.GlobalWindow;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.mobnote.util.GolukUtils;
import cn.com.tiros.api.FileUtils;
import cn.com.tiros.debug.GolukDebugUtils;

public class UploadVideo {
	/** 上传视频更新进度 */
	private final int MSG_H_UPLOAD_PROGRESS = 2;
	/** 上传成功 */
	private final int MSG_H_UPLOAD_SUCESS = 3;
	/** 上传视频失败 */
	private final int MSG_H_UPLOAD_ERROR = 4;
	private final int MSG_H_COUNT = 7;
	/** 重新上传 */
	private final int MSG_H_RETRY_UPLOAD = 8;

	private GolukApplication mApp = null;
	private Context mContext = null;
	private final int UPLOAD_FAILED_UP = 3;
	private int uploadCount = 0;
	private boolean isUploading = false;
	/** 统计 */
	private int finishShowCount = 0;
	private String mVideoPath = null;
	private boolean mIsExit = false;
	private String mVideoVid = "";
	/** 上传视频是否完成 */
	private boolean mIsUploadSucess = false;
	private int mCurrentPercent = 0;

	private AlertDialog mErrorDialog = null;

	private IUploadVideoFn mFn = null;

	public Handler mBaseHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_H_UPLOAD_PROGRESS:
				if (!GolukApplication.getInstance().getIsBackgroundState()) {
					if (GlobalWindow.getInstance().isShow()) {
						// 更新进度条
						int percent = ((Integer) msg.obj).intValue();
						GlobalWindow.getInstance().refreshPercent(percent);
						GolukDebugUtils.e("", "upload service--VideoShareActivity-mmmHandler percent:" + percent);
					} else {
						if (null == GlobalWindow.getInstance().getApplication()) {
							GlobalWindow.getInstance().setApplication(mApp);
						}
						GlobalWindow.getInstance().createVideoUploadWindow("正在上传Goluk视频");
					}
				}
				break;
			case MSG_H_UPLOAD_SUCESS:
				GlobalWindow.getInstance().topWindowSucess("视频上传成功");
				break;
			case MSG_H_UPLOAD_ERROR:
				if (mIsExit) {
					return;
				}
				// 上传失败
				uploadFailed();
				break;
			case MSG_H_COUNT:
				finishShowCount++;
				if (finishShowCount >= 3) {
					GlobalWindow.getInstance().dimissGlobalWindow();
					mBaseHandler.removeMessages(MSG_H_COUNT);
					finishShowCount = 0;
				} else {
					mBaseHandler.sendEmptyMessageDelayed(MSG_H_COUNT, 1000);
				}
				break;
			case MSG_H_RETRY_UPLOAD:
				if (mIsExit) {
					return;
				}
				uploadVideoFile(mVideoPath);
				if (null == GlobalWindow.getInstance().getApplication()) {
					GlobalWindow.getInstance().setApplication(mApp);
				}
				break;
			default:
				break;
			}
			super.handleMessage(msg);

		}

	};

	public UploadVideo(Context context, GolukApplication application) {
		mContext = context;
		mApp = application;
	}

	// CC上传失败，提示用户重试或退出
	private void uploadFailed() {
		dimissErrorDialog();
		if (mIsExit) {
			return;
		}
		GlobalWindow.getInstance().toFailed("视频上传失败");

		mErrorDialog = new AlertDialog.Builder(mContext).setTitle("提示").setMessage("上传失败")
				.setPositiveButton("重试", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						uploadVideoFile(mVideoPath);
						dimissErrorDialog();
						GolukUtils.showToast(mContext, "重新开始上传");
						if (null == GlobalWindow.getInstance().getApplication()) {
							GlobalWindow.getInstance().setApplication(mApp);
						}
						GlobalWindow.getInstance().createVideoUploadWindow("正在上传Goluk视频");

					}

				}).setNegativeButton("取消", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dimissErrorDialog();
						GlobalWindow.getInstance().toFailed("视频上传取消");
						// exit(false);

						sendData(IUploadVideoFn.EVENT_EXIT, false);

					}

				}).create();

		mErrorDialog.show();
	}

	private void sendData(int event, Object data) {
		if (null != mFn) {
			mFn.CallBack_UploadVideo(event, data);
		}
	}

	private void dimissErrorDialog() {
		if (null != mErrorDialog) {
			mErrorDialog.dismiss();
			mErrorDialog = null;
		}
	}

	public void setExit(boolean isExit) {
		mIsExit = isExit;
	}

	public void uploadVideoFile(String videoPath) {
		if (null == videoPath || "".equals(videoPath)) {
			return;
		}
		mVideoPath = videoPath;
		uploadCount++;
		GolukDebugUtils.e("", "jyf-----VideoShareActivity-------------uploadVideoFile :" + uploadCount);
		isUploading = true;
		final String filePath = FileUtils.javaToLibPath(videoPath);
		boolean isSucess = mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage,
				IPageNotifyFn.PageType_UploadVideo, filePath);

		GolukDebugUtils.e("", "Request---------:" + isSucess);
	}

	// 上传视频成功
	private void uploadSucess() {
		mIsUploadSucess = true;
		isUploading = false;
		GolukDebugUtils.e("", "upload service--VideoShareActivity-handleStatus---上传完成---FINISH----");
		// 通知上传成功
		mBaseHandler.sendEmptyMessage(MSG_H_UPLOAD_SUCESS);
	}

	// 上传视频失败
	private void uploadError() {
		if (mIsExit) {
			return;
		}
		mCurrentPercent = 0;
		GolukDebugUtils.e("", "jyf-----VideoShareActivity-------------uploadError :uploadCount:  " + uploadCount);
		if (uploadCount >= UPLOAD_FAILED_UP) {
			// 报错
			mBaseHandler.sendEmptyMessage(MSG_H_UPLOAD_ERROR);
			isUploading = false;
		} else {
			mBaseHandler.sendEmptyMessageDelayed(MSG_H_RETRY_UPLOAD, 500);
		}
	}

	private void updateFreshProgress(int per) {
		if (mIsExit) {
			return;
		}
		mCurrentPercent = per;
		Message msg = new Message();
		msg.what = MSG_H_UPLOAD_PROGRESS;
		msg.obj = per;
		mBaseHandler.sendMessage(msg);
	}

	public void videoUploadCallBack(int success, Object param1, Object param2) {
		GolukDebugUtils.e("", "jyf-----VideoShareActivity-------------videoUploadCallBack :success:  " + success);
		if (1 == success) {
			// 保存视频上传ID
			mVideoVid = (String) param2;
			uploadSucess();
			GolukDebugUtils.e("", "视频上传返回id--VideoShareActivity-videoUploadCallBack---vid---" + mVideoVid);
		} else if (2 == success) {
			// 上传进度
			final int per = (Integer) param1;
			updateFreshProgress(per);
		} else {
			// GolukUtils.showToast(mContext, "视频上传失败");
			uploadError();
		}
	}

}
