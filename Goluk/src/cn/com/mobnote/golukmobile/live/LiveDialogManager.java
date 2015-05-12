package cn.com.mobnote.golukmobile.live;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;

public class LiveDialogManager {
	/** 单例实例 */
	private static LiveDialogManager mManagerInstance = null;
	/** 授权load对话框 */
	private AlertDialog mLoginDialog = null;
	private AlertDialog mLiveExitDialog = null;
	/** 用户主动点击退出时，提示对话框 */
	private AlertDialog mLiveBackDialog = null;

	private AlertDialog mSingleButtonDialog = null;
	private AlertDialog mTwoButtonDialog = null;

	/** 对话框回调方法 */
	private ILiveDialogManagerFn dialogManagerFn = null;

	/** 对话框的“确定”按钮 */
	public static final int FUNCTION_DIALOG_OK = 0;
	/** 对话框的“取消”按钮 */
	public static final int FUNCTION_DIALOG_CANCEL = 1;

	/** 授权对话框类型 */
	public static final int DIALOG_TYPE_AUTHENTICATION = 0;
	/** 结束直播提示框 */
	public static final int DIALOG_TYPE_EXIT_LIVE = 1;
	/** 登录对话框 */
	public static final int DIALOG_TYPE_LOGIN = 2;
	/** 直播返回 */
	public static final int DIALOG_TYPE_LIVEBACK = 3;
	/** 直播超时 */
	public static final int DIALOG_TYPE_LIVE_TIMEOUT = 4;
	/** 直播服务下线 */
	public static final int DIALOG_TYPE_LIVE_OFFLINE = 5;

	public static final int DIALOG_TYPE_LIVE_CONTINUE = 6;
	/** 进入直播 */
	public static final int DIALOG_TYPE_LIVE_START = 7;
	/** ipc未登录提示 */
	public static final int DIALOG_TYPE_IPC_LOGINOUT = 8;
	/** 程序退出提示框 */
	public static final int DIALOG_TYPE_APP_EXIT = 9;
	/** 重新上传视频 */
	public static final int DIALOG_TYPE_LIVE_RELOAD_UPLOAD = 10;
	/** 重新请服务 */
	public static final int DIALOG_TYPE_LIVE_REQUEST_SERVER = 11;
	/** 直播分享 */
	public static final int DIALOG_TYPE_LIVE_SHARE = 12;

	private int mCurrentDialogType = 0;

	/**
	 * 获取当前类的一个实例
	 * 
	 * @return MapDialogManager实例
	 * @author jiayf
	 * @date 2014-5-22
	 */
	public static LiveDialogManager getManagerInstance() {
		if (null == mManagerInstance) {
			mManagerInstance = new LiveDialogManager();
		}
		return mManagerInstance;
	}

	/**
	 * 设置对话框回调接口
	 * 
	 * @param _fn
	 *            回调接口
	 * @author jiayf
	 * @date 2014-5-22
	 */
	public void setDialogManageFn(ILiveDialogManagerFn _fn) {
		dialogManagerFn = _fn;
	}

	/**
	 * 对话框回调接口
	 * */
	public interface ILiveDialogManagerFn {
		/**
		 * 对话框管理类的回调方法
		 * */
		public void dialogManagerCallBack(int dialogType, int function, String data);
	}

	ProgressDialog mProgressDialog = null;
	ProgressDialog mShareDialog = null;

	public void showShareProgressDialog(Context context, int type, String title, String message) {
		dismissShareProgressDialog();
		mCurrentDialogType = type;
		mShareDialog = ProgressDialog.show(context, title, message, true, false);
		mShareDialog.setOnCancelListener(new OnCancelListener() {

			@Override
			public void onCancel(DialogInterface arg0) {
				// TODO Auto-generated method stub
				sendMessageCallBack(mCurrentDialogType, FUNCTION_DIALOG_CANCEL, null);
			}
		});
	}

	public void dismissShareProgressDialog() {
		if (null != mShareDialog) {
			mShareDialog.dismiss();
			mShareDialog = null;
		}
	}

	public void showProgressDialog(Context context, String title, String message) {
		dismissProgressDialog();
		mProgressDialog = ProgressDialog.show(context, title, message, true, false);
		mProgressDialog.setCancelable(false);
	}

	public void setProgressDialogMessage(String message) {
		if (null != mProgressDialog) {
			mProgressDialog.setMessage(message);
		}
	}

	public void dismissProgressDialog() {
		if (null != mProgressDialog) {
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}
	}

	public void showSingleBtnDialog(Context context, int type, String title, String message) {
		if (null != mSingleButtonDialog) {
			return;
		}
		mCurrentDialogType = type;

		mSingleButtonDialog = new AlertDialog.Builder(context).create();

		mSingleButtonDialog.setTitle(title);
		mSingleButtonDialog.setMessage(message);
		mSingleButtonDialog.setCancelable(false);

		mSingleButtonDialog.setButton(DialogInterface.BUTTON_POSITIVE, "确认", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialoginterface, int i) {
				sendMessageCallBack(mCurrentDialogType, FUNCTION_DIALOG_OK, null);
				dismissSingleBtnDialog();
			}
		});
		mSingleButtonDialog.show();
	}

	public void dismissSingleBtnDialog() {
		if (null != mSingleButtonDialog) {
			mSingleButtonDialog.dismiss();
			mSingleButtonDialog = null;
		}
	}

	public void showTwoBtnDialog(Context context, int function, String title, String message) {

		if (null != mLiveBackDialog) {
			return;
		}
		mCurrentDialogType = function;
		mTwoButtonDialog = new AlertDialog.Builder(context).create();

		mTwoButtonDialog.setTitle(title);
		mTwoButtonDialog.setMessage(message);
		mTwoButtonDialog.setCancelable(false);

		mTwoButtonDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				sendMessageCallBack(mCurrentDialogType, FUNCTION_DIALOG_CANCEL, null);
				dismissTwoButtonDialog();
			}
		});

		mTwoButtonDialog.setButton(DialogInterface.BUTTON_POSITIVE, "确认", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialoginterface, int i) {
				sendMessageCallBack(mCurrentDialogType, FUNCTION_DIALOG_OK, null);

				dismissTwoButtonDialog();
			}
		});
		mTwoButtonDialog.show();

	}

	private void dismissTwoButtonDialog() {
		if (null != mTwoButtonDialog) {
			mTwoButtonDialog.dismiss();
			mTwoButtonDialog = null;
		}
	}

	public void showNoMobileDialog(Context context, String title, String message) {

	}

	public void hideNoMobileDialog() {

	}

	private void sendMessageCallBack(int dialogType, int function, String data) {
		if (null == dialogManagerFn) {
			return;
		}
		dialogManagerFn.dialogManagerCallBack(dialogType, function, data);
	}

	// 显示登录对话框
	public void showLoginDialog(Context context, String message) {
		if (null != mLoginDialog) {
			return;
		}
		mLoginDialog = new AlertDialog.Builder(context).create();

		mLoginDialog.setTitle("提示");
		mLoginDialog.setMessage(message);
		mLoginDialog.setCancelable(false);

		mLoginDialog.setButton(DialogInterface.BUTTON_POSITIVE, "确认", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialoginterface, int i) {
				sendMessageCallBack(DIALOG_TYPE_LOGIN, FUNCTION_DIALOG_OK, null);
				dimissLoginExitDialog();
			}
		});
		mLoginDialog.show();

	}

	// 销毁登录对话框
	public void dimissLoginExitDialog() {
		if (null != mLoginDialog) {
			mLoginDialog.dismiss();
			mLoginDialog = null;
		}
	}

	public void showLiveExitDialog(Context context, String title, String message) {
		if (null != mLiveExitDialog) {
			return;
		}
		mLiveExitDialog = new AlertDialog.Builder(context).create();

		mLiveExitDialog.setTitle("提示");
		mLiveExitDialog.setMessage(message);
		mLiveExitDialog.setCancelable(false);

		mLiveExitDialog.setButton(DialogInterface.BUTTON_POSITIVE, "确认", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialoginterface, int i) {
				sendMessageCallBack(DIALOG_TYPE_EXIT_LIVE, FUNCTION_DIALOG_OK, null);
				dimissLiveExitDialog();
			}
		});
		mLiveExitDialog.show();

	}

	public void dimissLiveExitDialog() {
		if (null != mLiveExitDialog) {
			mLiveExitDialog.dismiss();
			mLiveExitDialog = null;
		}
	}

	public void showLiveBackDialog(Context context, int function, String message) {

		dismissLiveBackDialog();

		mCurrentDialogType = function;
		mLiveBackDialog = new AlertDialog.Builder(context).create();

		mLiveBackDialog.setTitle("提示");
		mLiveBackDialog.setMessage(message);
		mLiveBackDialog.setCancelable(false);

		mLiveBackDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				sendMessageCallBack(mCurrentDialogType, FUNCTION_DIALOG_CANCEL, null);
				dismissLiveBackDialog();
			}
		});

		mLiveBackDialog.setButton(DialogInterface.BUTTON_POSITIVE, "确认", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialoginterface, int i) {
				sendMessageCallBack(mCurrentDialogType, FUNCTION_DIALOG_OK, null);

				dismissLiveBackDialog();
			}
		});
		mLiveBackDialog.show();

	}

	public void dismissLiveBackDialog() {
		if (null != mLiveBackDialog) {
			mLiveBackDialog.dismiss();
			mLiveBackDialog = null;
		}
	}

	/**
	 * 显示授权中对话框
	 * 
	 * @param context
	 *            上下文
	 * @param message
	 *            对话框显示的文字
	 * @author jiayf
	 * @date 2014-5-22
	 */
	public void showAuthLoadingDialog(Context context, String message) {

	}

	public boolean isAuthLoading() {

		return false;
	}

	/**
	 * 取消授权对话框
	 * 
	 * @author jiayf
	 * @date 2014-5-22
	 */
	public void hideAuthLoadingDialog() {

	}

}
