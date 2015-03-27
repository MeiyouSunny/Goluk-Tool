package cn.com.mobnote.user;

import cn.com.mobnote.golukmobile.UserRepwdActivity;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.view.inputmethod.InputMethodManager;

public class UserUtils {

	/**
	 * AlertDialog
	 */
	public static void showDialog(Context context,String message){
		new AlertDialog.Builder(context)
		.setTitle("Goluk温馨提示：")
		.setMessage(message)
		.setPositiveButton("确定", null)
		.create().show();
	}
	/**
	 * 隐藏软件盘
	 */
	public static void hideSoftMethod(Activity activity){
		 InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
		  imm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
	}
	
}
