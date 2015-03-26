package cn.com.mobnote.user;

import android.app.AlertDialog;
import android.content.Context;

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
	
}
