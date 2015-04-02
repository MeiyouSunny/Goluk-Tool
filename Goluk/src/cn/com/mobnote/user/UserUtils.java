package cn.com.mobnote.user;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
	/**
	 * 常用手机号的判断
	 */
	public static boolean isMobileNO(String mobiles) {
        boolean flag = false;
        try {
            Pattern p = Pattern.compile("^[1][3,4,5,8][0-9]{9}$"); // 验证手机号
            Matcher m = p.matcher(mobiles);
            flag = m.matches();
        } catch (Exception e) {
            flag = false;
        }
        return flag;
    }
	 public static boolean isNetDeviceAvailable(Context context){
	        boolean bisConnFlag=false;
	        ConnectivityManager conManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
	        NetworkInfo network = conManager.getActiveNetworkInfo();
	        if(network!=null){
	            bisConnFlag=conManager.getActiveNetworkInfo().isAvailable();
	        }
	        return bisConnFlag;
	    }
}
