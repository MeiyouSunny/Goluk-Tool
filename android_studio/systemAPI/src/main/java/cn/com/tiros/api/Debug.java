package cn.com.tiros.api;

import android.util.Log;

public class Debug {
	
	public static void sys_assert(boolean exp){
		assert(exp);
	}
	
	public static void sys_dbgprintf(String msg){
		if(msg != null){
			Log.e("sys_dbgprintf", msg);
		}
	}
	
}
