package cn.com.mobnote.user;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Message;

import com.umeng.socialize.utils.Log;

import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.UserSetupActivity;
import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.mobnote.util.console;

/**
 * 软件升级
 * @author mobnote
 *
 */
public class UpgradeManage {

	private GolukApplication mApp = null;
	private static final String TAG="lily";

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
			Log.i(TAG, "------upgradeGoluk()------"+b);
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
		console.log("----------版本更新回调-------upgradeGolukCallback---" + success + "-------" + obj);
		int codeOut = (Integer) outTime;
		if(1 == success){
			try {
				String dataObj = (String) obj;
				JSONObject json = new JSONObject(dataObj);
				Log.i(TAG, "------upgradeGoluk---"+json);
				String data = json.getString("data");
				JSONObject jsonData = new JSONObject(data);
				String goluk = jsonData.getString("goluk");
				Log.i(TAG, "-------goluk-----"+goluk);
				if(goluk.equals("{}")){
					Log.i(TAG, "------goluk为空，不用进行升级------");
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
					Log.i(TAG, "version="+version);
					UserUtils.showUpgradeGoluk(mApp.getContext(),appcontent, url);
				
					SharedPreferences mPreferencesVersion = mApp.getContext().getSharedPreferences("version", Context.MODE_PRIVATE);
					Editor mEditor = mPreferencesVersion.edit();
					mEditor.putString("versionCode", version);
					mEditor.commit();
					
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}else{
			android.util.Log.i(TAG, "-----网络链接超时---------"+codeOut);
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
}

