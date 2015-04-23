package cn.com.mobnote.golukmobile.live;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;

public class LiveDialogManager {
	/** 单例实例 */
	private static LiveDialogManager mManagerInstance = null;
	/** 授权load对话框 */
	private AlertDialog mLoginDialog = null;
	private AlertDialog mLiveExitDialog = null;
	/**自动登录对话框**/
	private AlertDialog mAutoLoginDialog = null;

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

	public void showNoMobileDialog(Context context, String title, String message) {

		// String[] msg = SingleButtonCustomDialog.splitMessage(message);
		// if (null == msg) {
		// return;
		// }
		//
		// hideNoMobileDialog();
		//
		// mNoMobileDialog = new SingleButtonCustomDialog.Builder(context,
		// msg.length).setTitle(title).setMessage(msg)
		// .setLeftButton("确定", new View.OnClickListener() {
		// @Override
		// public void onClick(View v) {
		// hideNoMobileDialog();
		// }
		// }).setCancelable(false).create();
		// mNoMobileDialog.show();
	}

	public void hideNoMobileDialog() {
		// if (null != mNoMobileDialog) {
		// mNoMobileDialog.dismiss();
		// mNoMobileDialog = null;
		// }
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
	//显示正在自动登录中的对话框
	public void showAutoLoginingDialog(Context context, String message){
		if (null != mAutoLoginDialog) {
			return;
		}
		mAutoLoginDialog = new AlertDialog.Builder(context).create();
		mAutoLoginDialog.setTitle("提示");
		mAutoLoginDialog.setMessage(message);
		mAutoLoginDialog.setCancelable(false);
		
	}
	//销毁自动登录中的对话框
	public void dismissAutoLoginDialog(){
		if(null != mAutoLoginDialog){
			mAutoLoginDialog.dismiss();
			mAutoLoginDialog = null;
		}
	}

	// 销毁登录对话框
	public void dimissLoginExitDialog() {
		if (null != mLoginDialog) {
			mLoginDialog.dismiss();
			mLoginDialog = null;
		}
	}

	public void showLiveExitDialog(Context context, String message) {
		if (null != mLiveExitDialog) {
			return;
		}
		mLiveExitDialog = new AlertDialog.Builder(context).create();

		mLiveExitDialog.setTitle("提示");
		mLiveExitDialog.setMessage("直播时间到，请返回");

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
		// if (mAuthLoadingDialog == null) {
		// mAuthLoadingDialog = new
		// MeetRecordDialog.Builder(context).setMessage(message)
		// .setOnCloseListener(new View.OnClickListener() {
		// @Override
		// public void onClick(View v) {
		// hideAuthLoadingDialog();
		// if (null != dialogManagerFn) {
		// dialogManagerFn.dialogManagerCallBack(DIALOG_TYPE_AUTHENTICATION,
		// FUNCTION_DIALOG_CANCEL, null);
		// }
		// }
		// }).setCancelable(false).create();
		// mAuthLoadingDialog.show();
		// }
	}

	public boolean isAuthLoading() {
		// if (null == mAuthLoadingDialog) {
		// return false;
		// }
		// return mAuthLoadingDialog.isShowing();
		return false;
	}

	/**
	 * 取消授权对话框
	 * 
	 * @author jiayf
	 * @date 2014-5-22
	 */
	public void hideAuthLoadingDialog() {
		// if (mAuthLoadingDialog != null) {
		// mAuthLoadingDialog.dismiss();
		// mAuthLoadingDialog = null;
		// }
	}

}
