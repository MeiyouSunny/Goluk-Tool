package cn.com.mobnote.golukmobile.live;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;

public class MapDialogManager {
	/** 单例实例 */
	private static MapDialogManager mManagerInstance = null;
	/** 授权load对话框 */
	private AlertDialog mLoginDialog = null; 
	/** 对话框回调方法 */
	private MapDialogManagerFn dialogManagerFn = null;

	/** 对话框的“确定”按钮 */
	public static final int FUNCTION_DIALOG_OK = 0;
	/** 对话框的“取消”按钮 */
	public static final int FUNCTION_DIALOG_CANCEL = 1;

	/** 授权对话框类型 */
	public static final int DIALOG_TYPE_AUTHENTICATION = 0;

	/**
	 * 获取当前类的一个实例
	 * 
	 * @return MapDialogManager实例
	 * @author jiayf
	 * @date 2014-5-22
	 */
	public static MapDialogManager getManagerInstance() {
		if (null == mManagerInstance) {
			mManagerInstance = new MapDialogManager();
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
	public void setDialogManageFn(MapDialogManagerFn _fn) {
		dialogManagerFn = _fn;
	}

	/**
	 * 对话框回调接口
	 * */
	public interface MapDialogManagerFn {
		/**
		 * 对话框管理类的回调方法
		 * */
		public void dialogManagerCallBack(int dialogType, int function, String data);
	}

	public void showNoMobileDialog(Context context, String title, String message) {

//		String[] msg = SingleButtonCustomDialog.splitMessage(message);
//		if (null == msg) {
//			return;
//		}
//
//		hideNoMobileDialog();
//
//		mNoMobileDialog = new SingleButtonCustomDialog.Builder(context, msg.length).setTitle(title).setMessage(msg)
//				.setLeftButton("确定", new View.OnClickListener() {
//					@Override
//					public void onClick(View v) {
//						hideNoMobileDialog();
//					}
//				}).setCancelable(false).create();
//		mNoMobileDialog.show();
	}

	public void hideNoMobileDialog() {
//		if (null != mNoMobileDialog) {
//			mNoMobileDialog.dismiss();
//			mNoMobileDialog = null;
//		}
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
//		if (mAuthLoadingDialog == null) {
//			mAuthLoadingDialog = new MeetRecordDialog.Builder(context).setMessage(message)
//					.setOnCloseListener(new View.OnClickListener() {
//						@Override
//						public void onClick(View v) {
//							hideAuthLoadingDialog();
//							if (null != dialogManagerFn) {
//								dialogManagerFn.dialogManagerCallBack(DIALOG_TYPE_AUTHENTICATION,
//										FUNCTION_DIALOG_CANCEL, null);
//							}
//						}
//					}).setCancelable(false).create();
//			mAuthLoadingDialog.show();
//		}
	}

	public boolean isAuthLoading() {
//		if (null == mAuthLoadingDialog) {
//			return false;
//		}
//		return mAuthLoadingDialog.isShowing();
		return false;
	}

	/**
	 * 取消授权对话框
	 * 
	 * @author jiayf
	 * @date 2014-5-22
	 */
	public void hideAuthLoadingDialog() {
//		if (mAuthLoadingDialog != null) {
//			mAuthLoadingDialog.dismiss();
//			mAuthLoadingDialog = null;
//		}
	}

}
