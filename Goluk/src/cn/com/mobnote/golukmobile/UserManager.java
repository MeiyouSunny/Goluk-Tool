package cn.com.mobnote.golukmobile;

import com.tencent.bugly.elfparser.Main;

import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.guide.GolukGuideManage;
import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.page.IPageNotifyFn;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;

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
 * @ 功能描述:Goluk引导页
 * 
 * @author 陈宣宇
 * 
 */
@SuppressLint("HandlerLeak")
public class UserManager {
	/** application */
	//private GolukApplication mApp = null;
	//private LayoutInflater mLayoutInflater = null;
	/** 上下文 */
	public Context mContext = null;
	public GolukApplication mApplication = null;
	/** 引导页管理类 */
	public static GolukGuideManage mGolukGuideManage = null;
	
	public int state = 0;
	public UserManager(){
//		mApplication = mContext.getApplicationInfo();
//		mApplication.setContext(mContext, "UserLogin");
	}
	
	public int GeState(){
		return state;
	}
	
	public boolean loginStatus(String phone, String pwd)
	{
		String condi = "{\"PNumber\":\"" + phone + "\",\"Password\":\"" + pwd + "\",\"tag\":\"android\"}";
		boolean b = mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage, IPageNotifyFn.PageType_Login, condi);
		if(b){  
			state = 1;
		}
		else
		{
			state = 0;
		}
		return b;
	}
	
	public void logout()
	{
		mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage, IPageNotifyFn.PageType_SignOut, "");
		state = 0;
	}
	
	public void LoginCallback(int cmd, Object param1, Object param2)
	{
		
	}
	
}
