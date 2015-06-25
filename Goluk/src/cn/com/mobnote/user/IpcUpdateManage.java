package cn.com.mobnote.user;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.view.KeyEvent;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.UserSetupActivity;
import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.mobnote.util.GolukUtils;
import cn.com.mobnote.util.JsonUtil;
import cn.com.tiros.debug.GolukDebugUtils;

/**
 * app升级+ipc升级
 * @author mobnote
 *
 */
public class IpcUpdateManage {

	private static final String TAG = "lily";
	private GolukApplication mApp = null;
	private static final String BIN_PATH = "fs1:/update/ipc_upgrade_2015-04-30-15-58.bin";
	
	/**启动APP**/
	public static final int FUNCTION_AUTO = 0;
	/**连接ipc**/
	public static final int FUNCTION_CONNECTIPC = 1;
	/**设置中点击版本检测**/
	public static final int FUNCTION_SETTING_APP = 2;
	/**设置中点击ipc升级**/
	public static final int FUNCTION_SETTING_IPC = 3;
	
	private int mFunction = -1;

	public IpcUpdateManage(GolukApplication mApp) {
		super();
		this.mApp = mApp;
	}
	
	/**
	 * 升级
	 * 将appVersion和ipcVersion信息请求给服务器
	 * @param function
	 * @param vipc
	 */
	public void requestInfo(int function ,String vipc){
		if(!UserUtils.isNetDeviceAvailable(mApp.getContext())){
			GolukDebugUtils.i(TAG, "网络连接异常，请检查网络后重试");
		}else{
			//{“AppVersionFilePath”:”fs6:/version”, “IpcVersion”:”1.2.3.4”}
			String ipcString = JsonUtil.putIPC("fs6:/version",vipc);
			boolean b = mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage,IPageNotifyFn.PageType_CheckUpgrade, ipcString);
			GolukDebugUtils.i(TAG, "====="+b+"===ipcUpdateManage======");
			if(b){
				mFunction = function;
			}
		}
	}
	//ipc连接成功后调用
	public int connectIpc(){
		return mFunction;
	}
	/**
	 * 升级回调
	 * @param success
	 * @param outTime
	 * @param obj
	 */
	public void requestInfoCallback(int success,Object outTime,Object obj){
		GolukDebugUtils.i(TAG, "=======requestInfoCallback=======");
		int codeOut = (Integer) outTime;
		if(1 == success){
			try{
				String dataObj = (String) obj;
				JSONObject json = new JSONObject(dataObj);
				GolukDebugUtils.i(TAG, "------upgradeGoluk---"+json);
				String data = json.getString("data");
				JSONObject jsonData = new JSONObject(data);
				
				String goluk = jsonData.getString("goluk");
				JSONArray ipc = jsonData.getJSONArray("ipc");
				//保存ipc匹配信息
				mApp.mSharedPreUtil.saveIPCMatchInfo(ipc.toString());
				
				GolukDebugUtils.i(TAG, "===ipc----goluk=------"+goluk);
				
				if(mFunction == 0){
					if(goluk.equals("{}")){
						GolukDebugUtils.i("lily", "------goluk为空，不用进行升级------");
						/**
						 * APP不需要升级，判断ipc是否需要升级
						 */
						IPCInfo ipcInfo = ipcUpdateUtils(ipc);
						
					}else{
						appUpgradeUtils(goluk);
					}
				}else if(mFunction == 1){
					//ipc连接后不匹配
					if(goluk.equals("{}")){
						GolukDebugUtils.i("lily", "------goluk为空，不用进行升级------");
						/**
						 * APP不需要升级，判断ipc是否需要升级
						 */
						IPCInfo ipcInfo = ipcUpdateUtils(ipc);
						
					}else{
						appUpgradeUtils(goluk);
					}
				}else if(mFunction == 2){
					if(goluk.equals("{}")){
						//设置页版本检测需要提示
						GolukUtils.showToast(mApp.getContext(), "当前已是最新版本");
					}else{
						appUpgradeUtils(goluk);
					}
				}else if(mFunction == 3){
					IPCInfo ipcInfo = ipcUpdateUtils(ipc);
					if(ipcInfo == null){
						//ipc不需要升级  ---->  提示   ------>  结束
						GolukUtils.showToast(mApp.getContext(), "极路客固件当前已是最新版本");
					}else{
						/**
						 * ipc需要升级，前提是判断app是否需要升级
						 * app不需要升级，直接下载并升级ipc
						 * app需要升级，设个年级app
						 */
						if(goluk.equals("{}")){
							//APP不需要升级
							GolukDebugUtils.i("lily", "------goluk为空，不用进行升级------");
							//提示下载并升级ipc
							ipcUpgrade(ipcInfo);
						}else{
							appUpgradeUtils(goluk);
						}
					}
					
				}
				
			}catch(Exception e){
				e.printStackTrace();
			}
		}else{
			GolukDebugUtils.i(TAG, "-----网络链接超时---------"+codeOut);
			switch (codeOut) {
			case 1:
			case 2:
			case 3:
			default:
				GolukDebugUtils.i(TAG, "网络连接超时，请检查后重试");
			break;
			}
		}
		mFunction = -1;
	}
	
	/**
	 * 下载ipc文件
	 */
	public boolean download(String url,String path){
		//{“url”:”http://www.baidu.com”, “savePath”:”fs1:/update/ipc_upgrade_2015-04-30-15-58.bin”}
		String str = JsonUtil.ipcDownLoad(url, path);
		return mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage,IPageNotifyFn.PageType_CommDownloadFile, str);
	}
	
	/**
	 * 下载ipc文件回调
	 * void* pvUser, int type ,int state , unsigned long param1, unsigned long param2 
	 * 其中的state==2时，param1为下载进度数值（0~100）
	 * @param state
	 * @param param1
	 * @param param2
	 */
	public void downloadCallback(int state,Object param1,Object param2){
		GolukDebugUtils.i(TAG, "------------downloadCallback-----------");
		if(state == 2){
			//下载中
			int progress = (Integer)param1;
		}else if(state == 1){
			//下载成功
		}else if(state == 0){
			//下载失败
		}
	}
	
	/**
	 * ipc升级
	 * 
	 */
	public void ipcUpgrade(final IPCInfo ipcInfo){
		GolukDebugUtils.i(TAG, "------------isConnect-----------"+mApp.isIpcLoginSuccess);
		if(!mApp.isIpcLoginSuccess){
			//true   ipc未连接
			UserSetupActivity.mUpdateHandler.sendEmptyMessage(UserSetupActivity.UPDATE_IPC_UNUNITED);
		}else{
			//false   ipc已连接
			new AlertDialog.Builder(mApp.getContext())
			.setMessage("是否下载固件升级文件？")
			.setPositiveButton("", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					boolean b = download(ipcInfo.getUrl(),BIN_PATH);
					if(b){
						
					}
				}
			})
			/*.setMessage("是否给您的摄像头进行固件升级？")
			.setPositiveButton("确认", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					//判断是否有升级文件
					boolean isHasFile = UserUtils.fileIsExists();
					if(isHasFile){
						if(GolukApplication.getInstance().getIpcIsLogin()){
							boolean u = GolukApplication.getInstance().getIPCControlManager().ipcUpgrade(ipcInfo.getUrl());
							GolukDebugUtils.e(TAG, "YYYYYY=======ipcUpgrade()============u="+u);
							if(u){
								//正在准备文件，请稍候……
								UserSetupActivity.mUpdateHandler.sendEmptyMessage(UserSetupActivity.UPDATE_PREPARE_FILE);//正在准备文件，请稍候……
							}
						}
					}else{
						//文件不存在
						UserSetupActivity.mUpdateHandler.sendEmptyMessage(UserSetupActivity.UPDATE_FILE_NOT_EXISTS);//文件不存在
					}
				}
			})*/
			.setNegativeButton("取消", null)
			.create().show();
		}
	}
	
	/**
	 * ipc升级解析
	 * @param ipc
	 * @return
	 */
	public IPCInfo ipcUpdateUtils(JSONArray ipc){
		/**
		 * APP不用升级的话，判断ipc是否需要升级
		 * isnew   0不需要升级   1需要升级
		 */
		IPCInfo ipcInfo = null;
		IPCInfo[] upgradeArray = JsonUtil.upgradeJson(ipc);
		final int length = upgradeArray.length;
		for(int i=0;i<length;i++){
			String ipc_filesize = upgradeArray[i].getFilesize();
			String ipc_content = upgradeArray[i].getAppcontent();
			//保存ipc文件大小
			mApp.mSharedPreUtil.saveIpcFileSize(ipc_filesize);
			//保存ipc更新信息
			mApp.mSharedPreUtil.saveIpcContent(ipc_content);
			
			String ipc_isnew = upgradeArray[i].getIsnew();
			if("0".equals(ipc_isnew)){
				GolukDebugUtils.i(TAG, "ipc当前已是最新版本，不需要升级");
			}else if("1".equals(ipc_isnew)){
				GolukDebugUtils.i(TAG, "ipc需要升级");
				ipcInfo = upgradeArray[i];
			}
		}
		if(null != ipcInfo){
			ipcUpgrade(ipcInfo);
		}
		return ipcInfo;
	}
	
	/**
	 * 
	 * @param goluk
	 */
	public void appUpgradeUtils(String goluk){
		try {
			JSONObject jsonGoluk = new JSONObject(goluk);
			APPInfo appInfo = JsonUtil.appUpgradeJson(jsonGoluk);
			String appcontent = appInfo.getAppcontent();
			String url = appInfo.getUrl();
			String isUpdate = appInfo.getIsupdate();
			/**
			 * APP升级
			 * 0非强制升级   1强制升级
			 * 非强制升级不退出程序，强制升级退出程序
			 */
			if(isUpdate.equals("1")){
				mApp.mUpgrade.showUpgradeGoluk(mApp.getContext(),appcontent, url);
			}else if(isUpdate.equals("0")){
				mApp.mUpgrade.showUpgradeGoluk2(mApp.getContext(), appcontent, url);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 不匹配升级提示
	 * @param mContext
	 * @param message
	 * @param vIpc
	 */
	public void showUnMatchDialog(final Context mContext,String message, final String vIpc){
		Builder mBuilder = new AlertDialog.Builder(mContext);
		AlertDialog dialog = mBuilder.setTitle("发现新版本")
				.setMessage(message)
				.setPositiveButton("稍后再说", null)
				.setNegativeButton("马上升级", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						requestInfo(FUNCTION_CONNECTIPC, vIpc);
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
