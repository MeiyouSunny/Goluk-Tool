package cn.com.mobnote.golukmobile.carrecorder;

import java.util.HashMap;
import java.util.Iterator;

import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.tachograph.comm.IPCManagerAdapter;
import cn.com.mobnote.tachograph.comm.IPCManagerClass;
import cn.com.mobnote.tachograph.comm.IPCManagerFn;
import cn.com.tiros.api.FileUtils;


 /**
  * 1.编辑器必须显示空白处
  *
  * 2.所有代码必须使用TAB键缩进
  *
  * 3.类首字母大写,函数、变量使用驼峰式命名,常量所有字母大写
  *
  * 4.注释必须在行首写.(枚举除外)
  *
  * 5.函数使用块注释,代码逻辑使用行注释
  *
  * 6.文件头部必须写功能说明
  *
  * 7.所有代码文件头部必须包含规则说明
  *
  * IPC控制管理
  *
  * 2015年3月21日
  *
  * @author xuhw
  */
public class IPCControlManager implements IPCManagerFn{
	/** IPC管理 */
	private IPCManagerClass mIPCManagerClass=null;
	/** IPC回调监听列表 */
	private HashMap<String, IPCManagerFn> mIpcManagerListener = null;
	public IPCControlManager(){
		mIpcManagerListener = new HashMap<String, IPCManagerFn>();
		mIPCManagerClass = new IPCManagerClass();
		mIPCManagerClass.IPCManager_Create();
		mIPCManagerClass.IPCManager_RegisterNetTransNotify(this);
		mIPCManagerClass.IPCManager_SetMode(IPCMgrMode_IPCDirect);
		IPCManagerAdapter.setIPcManageListener(this);
		setIPCWifiState(true);
	}
	
	/**
	 * 告知IPC wifi连接状态
	 * @param isConnect ture:连接　false:未连接
	 * @author xuhw
	 * @date 2015年3月21日
	 */
	public void setIPCWifiState(boolean isConnect){
		if(isConnect){
			mIPCManagerClass.IPCManager_WifiStateChanged(1);
		}else{
			mIPCManagerClass.IPCManager_WifiStateChanged(0);
		}
	}
	
	/**
	 * 视频截图
	 * @author xuhw
	 * @date 2015年3月21日
	 */
	public void screenShot() {
		if (GolukApplication.getInstance().getIpcIsLogin()) {
			GFileUtils.writeShootLog("========发起ipc图片截图========");
			boolean isSuccess = mIPCManagerClass.IPCManager_VDCP_CommRequest(IPC_VDCPCmd_SnapPic, "");
			if (!isSuccess) {
				GFileUtils.writeShootLog("========ipc截图命令 　发送失败========");
			}
		} else {
			GFileUtils.writeShootLog("========ipc截图命令失败　未登录========");
		}
	}
	
	/**
	 * 发起精彩视频截取功能
	 * @return true:命令发送成功 false:失败
	 * @author xuhw
	 * @date 2015年3月21日
	 */
	public boolean startWonderfulVideo(){
		String queryParam = IpcDataParser.getTriggerRecordJson(TYPE_SHORTCUT, 4, 8);
		return mIPCManagerClass.IPCManager_VDCP_CommRequest(IPC_VDCPCmd_TriggerRecord, queryParam);
	}
	
	/**
	 * 发起紧急视频截取功能
	 * @return true:命令发送成功 false:失败
	 * @author xuhw
	 * @date 2015年3月21日
	 */
	public boolean startEmergencyVideo(){
		String queryParam = IpcDataParser.getTriggerRecordJson(TYPE_URGENT, 8, 8);
		return mIPCManagerClass.IPCManager_VDCP_CommRequest(IPC_VDCPCmd_TriggerRecord, queryParam);
	}
	
	/**
	 * 文件查询
	 * @param filename 要查询的文件名
	 * @return true:命令发送成功 false:失败
	 * @author xuhw
	 * @date 2015年3月21日
	 */
	public boolean querySingleFile(String filename){
		return mIPCManagerClass.IPCManager_VDCP_CommRequest(IPC_VDCPCmd_SingleQuery, filename);
	}

	/**
	 * 查询文件列表信息
	 * @param filetype 1:循环影像 2:紧急视频 4:精彩视频
	 * @param limitCount 最多查询条数
	 * @param timestart 查询起始时间（0表示查询所有）
	 * @return true:命令发送成功 false:失败
	 * @author xuhw
	 * @date 2015年3月21日
	 */
	public boolean queryFileListInfo(int filetype, int limitCount, int timestart){
		String queryParam = IpcDataParser.getQueryMoreFileJson(filetype, limitCount, timestart, 2147483647);
		GFileUtils
				.writeIPCLog("===========获取文件列表===1111=====================queryParam="
						+ queryParam);

		return mIPCManagerClass.IPCManager_VDCP_CommRequest(IPC_VDCPCmd_Query, queryParam);
	}
	
	/**
	 * 下载文件
	 * @param filename 文件名称
	 * @param tag 唯一标识
	 * @param savepath 文件保存路径
	 * @author xuhw
	 * @date 2015年3月25日
	 */
	public void downloadFile(String filename, String tag, String savepath){
		mIPCManagerClass.IPCManager_AddDownloadFile(filename, tag, savepath);
	}
	
	/**
	 * 删除文件
	 * @param filename 文件名称
	 * @author xuhw
	 * @date 2015年3月25日
	 */
	public void deleteFile(String filename){
		mIPCManagerClass.IPCManager_VDCP_CommRequest(IPC_VDCPCmd_Erase, filename);
	}

	/**
	 * 添加IPC管理监听
	 * @param from 来自哪里的
	 * @param fn 监听接口
	 * @author xuhw
	 * @date 2015年3月21日
	 */
	public void addIPCManagerListener(String from, IPCManagerFn fn) {
		this.mIpcManagerListener.put(from, fn);
	}
	
	/**
	 * 删除IPC监听
	 * @param from 来自那里的
	 * @author xuhw
	 * @date 2015年3月21日
	 */
	public void removeIPCManagerListener(String from){
		this.mIpcManagerListener.remove(from);
	}
	
	@Override
	public void IPCManage_CallBack(int event, int msg, int param1, Object param2) {
		
		Iterator iter = mIpcManagerListener.keySet().iterator();  
		while (iter.hasNext()) {  
		    Object key = iter.next();  
		    if(null != key){
		    	IPCManagerFn fn = mIpcManagerListener.get(key);  
		    	if(null != fn){
			    	fn.IPCManage_CallBack(event, msg, param1, param2);
			    }
		    }
		}  
		
	}
	
}
