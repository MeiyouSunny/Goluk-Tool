package cn.com.mobnote.receiver;

import cn.com.mobnote.golukmobile.MainActivity;
import cn.com.mobnote.util.console;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Message;
/**
 * <pre>
 * 1.类命名首字母大写
 * 2.公共函数驼峰式命名
 * 3.属性函数驼峰式命名
 * 4.变量/参数驼峰式命名
 * 5.操作符之间必须加空格
 * 6.注释都在行首写.(枚举除外)
 * 7.编辑器必须显示空白处
 * 8.所有代码必须使用TAB键缩进
 * 9.函数使用块注释,代码逻辑使用行注释
 * 10.文件头部必须写功能说明
 * 11.后续人员开发保证代码格式一致
 * </pre>
 * 
 * @ 功能描述:网络状态广播监听
 * 
 * @author 陈宣宇
 * 
 */

public class NetworkStateReceiver extends BroadcastReceiver {
	//private static int lastType = -1;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		console.log("wifi---网络广播........");
		// log.debug("网络状态改变");
		String action = intent.getAction();
		Message msg = new Message();
		msg.what = 3;
		if(!isNetworkAvailable(context)){
			console.log("wifi---网络广播....网络不可用...." + action);
			if(null != MainActivity.mMainHandler){
				MainActivity.mMainHandler.sendMessage(msg);
			}
		}
		else{
			console.log("wifi---网络广播....网络可用...." + action);
			if(null != MainActivity.mMainHandler){
				MainActivity.mMainHandler.sendMessage(msg);
			}
		}
	}
	
	/**
	 * 网络是否可用
	 *
	 * @param context
	 * @return
	 */
	public static boolean isNetworkAvailable(Context context){
		ConnectivityManager mgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo[] info = mgr.getAllNetworkInfo();
		if (info != null) {
			for (int i = 0; i < info.length; i++) {
				if (info[i].getState() == NetworkInfo.State.CONNECTED) {
					return true;
				}
			}
		}
		return false;
	}

}




