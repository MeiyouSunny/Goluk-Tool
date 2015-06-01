package cn.com.mobnote.user;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnKeyListener;
import android.content.SharedPreferences.Editor;
import android.view.KeyEvent;

import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.application.SysApplication;
import cn.com.mobnote.golukmobile.UserStartActivity;
import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.mobnote.util.GolukUtils;
import cn.com.tiros.debug.GolukDebugUtils;

/**
 * 软件升级
 * @author mobnote
 *
 */
public class UpgradeManage {

	private GolukApplication mApp = null;
	private SharedPreferences mPreferences = null;
	private Editor mEditor = null;

	public UpgradeManage(GolukApplication mApp) {
		super();
		this.mApp = mApp;
	}
	
	/**
	 * 版本更新
	 * ?method=upgradeGoluk&xieyi=100&version=1.0.0.1
	 */
	public void upgradeGoluk(){
		if(!UserUtils.isNetDeviceAvailable(mApp.getContext())){
			//没有网络
		}else{
			boolean b = mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage,IPageNotifyFn.PageType_CheckUpgrade, "fs6:/version");
			GolukDebugUtils.i("lily", "------upgradeGoluk()------"+b);
			if(b){
				//
			}
		}
	}
	/**
	 * 版本更新回调
	 * 
	 */
	@SuppressWarnings("unused")
	public void upgradeGolukCallback(int success,Object outTime,Object obj){
		GolukDebugUtils.e("","----------版本更新回调-------upgradeGolukCallback---" + success + "-------" + obj);
		int codeOut = (Integer) outTime;
		if(1 == success){
			try {
				String dataObj = (String) obj;
				JSONObject json = new JSONObject(dataObj);
				GolukDebugUtils.i("lily", "------upgradeGoluk---"+json);
				String data = json.getString("data");
				JSONObject jsonData = new JSONObject(data);
				String goluk = jsonData.getString("goluk");
				GolukDebugUtils.i("lily", "-------goluk-----"+goluk);
				if(goluk.equals("{}")){
					mPreferences = mApp.getContext().getSharedPreferences("setupUpdate", Context.MODE_PRIVATE);
					boolean flag = mPreferences.getBoolean("update", false);
					if(flag){
						//设置页版本检测需要提示
						GolukUtils.showToast(mApp.getContext(), "当前已是最新版本");
						mPreferences = mApp.getContext().getSharedPreferences("setupUpdate", Context.MODE_PRIVATE);
						mEditor = mPreferences.edit();
						mEditor.putBoolean("update", false);
						mEditor.commit();
					}else{
						//启动APP进行升级时不需要提示
						GolukDebugUtils.i("lily", "------goluk为空，不用进行升级------");
					}
				}else{
					JSONObject jsonGoluk = new JSONObject(goluk);
					String appcontent = jsonGoluk.getString("appcontent");
					String filesize = jsonGoluk.getString("filesize");
					String isupdate = jsonGoluk.getString("isupdate");
					String md5 = jsonGoluk.getString("md5");
					String path = jsonGoluk.getString("path");
					String releasetime = jsonGoluk.getString("releasetime");
					String url = jsonGoluk.getString("url");
					String version = jsonGoluk.getString("version");
					GolukDebugUtils.i("lily", "version="+version);
					//0非强制升级   1强制升级
					if(isupdate.equals("1")){
						showUpgradeGoluk(mApp.getContext(),appcontent, url);
					}else if(isupdate.equals("0")){
						showUpgradeGoluk2(mApp.getContext(), appcontent, url);
					}
				
					SharedPreferences mPreferencesVersion = mApp.getContext().getSharedPreferences("version", Context.MODE_PRIVATE);
					Editor mEditor = mPreferencesVersion.edit();
					mEditor.putString("versionCode", version);
					mEditor.commit();
					
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}else{
			GolukDebugUtils.i("lily", "-----网络链接超时---------"+codeOut);
			switch (codeOut) {
			case 1:
				break;
			case 2:
				break;
			case 3:
				break;
			default:
				break;
			}
		}
		
	}
	
	
	/**
	 * 强制升级提示
	 * @param mContext
	 * @param message1
	 * @param message2
	 */
	public void showUpgradeGoluk(final Context mContext,String message, final String url){
		Builder mBuilder = new AlertDialog.Builder(mContext);
		AlertDialog dialog = mBuilder.setTitle("发现新版本")
				.setMessage(message)
				.setPositiveButton("马上升级", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						//浏览器打开url
						GolukUtils.openUrl(url, mContext);
						
						if(GolukApplication.mMainActivity != null){
							GolukApplication.mMainActivity.finish();
							GolukApplication.mMainActivity = null;
						}
						SysApplication.getInstance().exit();
						
						mApp.mIPCControlManager.setIPCWifiState(false, "");
			    		mApp.mGoluk.GolukLogicDestroy();
			    		if (null != UserStartActivity.mHandler) {
			    			UserStartActivity.mHandler.sendEmptyMessage(UserStartActivity.EXIT);
			    		}
			    		int PID = android.os.Process.myPid();
			    		android.os.Process.killProcess(PID);
			            System.exit(0);
			            
					}
				})
				.setCancelable(false)
				.setOnKeyListener(new OnKeyListener() {
					@Override
					public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
						if(keyCode == KeyEvent.KEYCODE_BACK){
							return true;
						}
						return false;
					}
				})
				.create();
		dialog.show();
	}
	
	/**
	 * 非强制升级提示
	 * @param mContext
	 * @param message1
	 * @param message2
	 */
	public void showUpgradeGoluk2(final Context mContext,String message, final String url){
		Builder mBuilder = new AlertDialog.Builder(mContext);
		AlertDialog dialog = mBuilder.setTitle("发现新版本")
				.setMessage(message)
				.setPositiveButton("稍后再说", null)
				.setNegativeButton("马上升级", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						//浏览器打开url
						GolukUtils.openUrl(url, mContext);
						
						if(GolukApplication.mMainActivity != null){
							GolukApplication.mMainActivity.finish();
							GolukApplication.mMainActivity = null;
						}
						SysApplication.getInstance().exit();
						
						mApp.mIPCControlManager.setIPCWifiState(false, "");
			    		mApp.mGoluk.GolukLogicDestroy();
			    		if (null != UserStartActivity.mHandler) {
			    			UserStartActivity.mHandler.sendEmptyMessage(UserStartActivity.EXIT);
			    		}
			    		int PID = android.os.Process.myPid();
			    		android.os.Process.killProcess(PID);
			            System.exit(0);
			            
					}
				})
				.setCancelable(false)
				.setOnKeyListener(new OnKeyListener() {
					@Override
					public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
						if(keyCode == KeyEvent.KEYCODE_BACK){
							return true;
						}
						return false;
					}
				})
				.create();
		dialog.show();
	}
}

