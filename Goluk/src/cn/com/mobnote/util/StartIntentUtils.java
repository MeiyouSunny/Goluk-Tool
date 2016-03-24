package cn.com.mobnote.util;

import android.content.Context;
import android.content.Intent;
import cn.com.mobnote.golukmobile.usercenter.NewUserCenterActivity;
import cn.com.mobnote.golukmobile.usercenter.UCUserInfo;

/**
 * intent跳转activity
 * @author leege100
 *
 */
public class StartIntentUtils {
	
	/**
	 * 跳转到用户中心
	 * @param context
	 * @param userInfo
	 */
	public static void intentToUserCenter(Context context,UCUserInfo userInfo){
		
		Intent intent = new Intent(context, NewUserCenterActivity.class);
		intent.putExtra("userinfo", userInfo);
		context.startActivity(intent);
	}

}
