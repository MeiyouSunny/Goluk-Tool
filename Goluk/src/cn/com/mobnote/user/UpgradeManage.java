package cn.com.mobnote.user;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.view.KeyEvent;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.application.SysApplication;
import cn.com.mobnote.golukmobile.UserStartActivity;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomLoadingDialog;
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
	private CustomLoadingDialog mCustomLoadingDialog = null;

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
			if(mApp.flag){
				GolukUtils.showToast(mApp.getContext(), "网络连接异常，请检查网络后重试");
			}
		}else{
			boolean b = mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage,IPageNotifyFn.PageType_CheckUpgrade, "fs6:/version");
			GolukDebugUtils.i("lily", "------upgradeGoluk()------"+b);
			if(b){
				//
				GolukDebugUtils.i("lily", "------CustomLoadingDialog-----show()-----before----"+mApp.flag);
				if(mApp.flag){
					GolukDebugUtils.i("upgrade", "--------CustomDialog-----111-----");
					if(null == mCustomLoadingDialog){
						GolukDebugUtils.i("upgrade", "--------CustomDialog-----222-----");
						mCustomLoadingDialog = new CustomLoadingDialog(this.mApp.getContext(), "检测中，请稍候……");
						GolukDebugUtils.i("upgrade", "--------CustomDialog-----333-----");
					}
					mCustomLoadingDialog.show();
					GolukDebugUtils.i("lily", "------CustomLoadingDialog-----show()-----after----");
				}
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
			GolukDebugUtils.i("lily", "------CustomLoadingDialog-----close()-----before----");
			if(mApp.flag){
				closeProgressDialog();
			}
			GolukDebugUtils.i("lily", "------CustomLoadingDialog-----close()-----after----");
			try {
				String dataObj = (String) obj;
				JSONObject json = new JSONObject(dataObj);
				GolukDebugUtils.i("lily", "------upgradeGoluk---"+json);
				String data = json.getString("data");
				JSONObject jsonData = new JSONObject(data);
				String goluk = jsonData.getString("goluk");
				GolukDebugUtils.i("lily", "-------goluk-----"+goluk);
				if(goluk.equals("{}")){
					if(mApp.flag){
						//设置页版本检测需要提示
						GolukUtils.showToast(mApp.getContext(), "当前已是最新版本");
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
					/**
					 * 0非强制升级   1强制升级
					 * 非强制升级不退出程序，强制升级退出程序
					 */
					if(isupdate.equals("1")){
						showUpgradeGoluk(mApp.getContext(),appcontent, url);
					}else if(isupdate.equals("0")){
						showUpgradeGoluk2(mApp.getContext(), appcontent, url);
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}else{
			GolukDebugUtils.i("lily", "-----网络链接超时---------"+codeOut);
			switch (codeOut) {
			case 1:
			case 2:
			case 3:
			default:
				if(mApp.flag){
					closeProgressDialog();
					GolukUtils.showToast(mApp.getContext(), "网络连接超时，请检查网络后重试");
				}
				break;
			}
		}
		
		mApp.flag = false;
		
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
	 * 关闭加载中对话框
	 */
	private void closeProgressDialog(){
		if(null != mCustomLoadingDialog){
			mCustomLoadingDialog.close();
			mCustomLoadingDialog = null;
		}
	}
}

