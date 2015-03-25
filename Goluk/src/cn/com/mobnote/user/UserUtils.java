package cn.com.mobnote.user;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.widget.Toast;

public class UserUtils {

	//手机号格式错误
	public static void isPhoneNumber(String phoneNumber,final Activity activity){
		if (!phoneNumber.startsWith("1") || phoneNumber.length() < 11) {
			Builder mAlertDialog = new AlertDialog.Builder(activity)
					.setTitle("错误提示")
					.setMessage("手机格式输入错误,请重新输入")
					.setPositiveButton("确定",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									Toast.makeText(activity, "点击了确定按钮",Toast.LENGTH_SHORT).show();
								}
							});
			mAlertDialog.create();
			mAlertDialog.show();
		}
	}
	//密码格式错误
	public static void isPwd(String password,final Activity activity){
		//密码格式输入不正确,请输入 6-16 位数字、字母,字母区分大小
		Pattern pattern = Pattern.compile("[0-9]*");
		Matcher isNum =  pattern.matcher(password);
		
		if(password.length()<6 || password.length()>16 || ! isNum.matches()){
			Builder mAlertDialogPwd = new AlertDialog.Builder(activity);
			mAlertDialogPwd.setTitle("错误信息提示")
			.setMessage("密码格式输入不正确,请输入 6-16 位数字、字母,字母区分大小")
			.setPositiveButton("确定", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					// TODO Auto-generated method stub
					Toast.makeText(activity, "您点击了确定按钮", Toast.LENGTH_SHORT).show();
				}
			});
			mAlertDialogPwd.create();
			mAlertDialogPwd.show();
		}
		/*调用静态判断密码格式的方法
		if(!isMatch(pwd)){
			
		}*/
	}
	
	
	/**
	 * 判断字符串是否为数字、字母
	 * 根据字符的unicode的编码范围
	 */
	public static boolean isMatch(String str){
		String regex = "^[a-z0-9A-Z]";
		return str.matches(regex);
	}
}
