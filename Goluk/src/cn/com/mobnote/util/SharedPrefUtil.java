package cn.com.mobnote.util;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * 2012-10-09
 * 
 * @author caoyingpeng
 * 
 */
public class SharedPrefUtil {

	/** 是否显示新版本特性帮助 */
	public static final String PROPERTY_SHOW_HELP = "property_show_help";
	/** 保存是否展示logo页面动画 */
	public static final String PROPERTY_SHOW_LOGOANMI = "property_show_logoanmi";
	/** 是否显示第一次的设置性别与男女 */
	public static final String PROPERTY_SHOW_NAVIMASK = "property_show_setusername";
	/** 见面三地图 显示所有联系人按钮 **/
	public static final String PROPERTY_MEET_SHOW_ALL_USERS_POINT = "meetshowallusers_point";
	/** 保存是否已经开启通讯录，false未开启， true已经开启 */
	public static final String PROPERTY_OPEN_CONTACT = "open_contact";
	/** 见面三创建群组后，有人加入群组前，地图界面催促 提示 */
	public static final String PROPERTY_MAP_MEET_CONSOLE_SOUND = "map_meet_console_sound";
	/** 是否预加载主页面 初次进入预加载。如果程序上次使用正常退出，那么预加载，否则不做预加载，默认不预加载 */
	public static final String PROPERTY_PREPARE_LOADMAIN = "prepare_loadmain";
	/** 聊天列表输入方式 */
	public static final String PROPERTY_CHAT_INPUT_METHOD = "chat_input_method_state";
	/** 是否首次启动 */
	public static final String PROPERTY_IS_FIRST_START = "property_is_first_start";
	/** 是否首次显示提示用户导航时实时现在实景图对话框 */
	public static final String PROPERTY_IS_SHOWDOWNLOADNAVIREALSENSEMAPDIALOG = "property_is_showdownloadnavirealsensemapdialog";
	/** 是否显示新手引导页 */
	public static final String PROPERTY_IS_SHOWNEWUSERGUIDE = "property_is_shownewuserguide";

	/** 直播是否正常退出 */
	public static final String PROPERTY_LIVE_NORMAL_EXIT = "property_is_normal_exit";

	/** 保存ipc版本号 **/
	public static final String PROPERTY_SAVE_IPCVERSION = "property_save_ipcversion";

	/** 保存ipc匹配信息 **/
	public static final String PROPERTY_SAVE_IPCMATCH_INFO = "property_save_ipcmatch_info";

	/** 保存ipc文件大小 **/
	public static final String PROPERTY_SAVE_IPC_FILESIZE = "property_save_ipc_filesize";

	/** 保存ipc更新信息 **/
	public static final String PROPERTY_SAVE_IPC_CONTENT = "property_save_ipc_content";

	/** 保存ipc更新信息 **/
	public static final String PROPERTY_SAVE_IPC_URL = "property_save_ipc_url";

	/** 保存ipc更新信息 **/
	public static final String PROPERTY_SAVE_IPC_PATH = "property_save_ipc_path";

	/** 保存下载本地的IPC升级BIN文件信息 */
	public static final String PROPERTY_IPC_UPGRADE_LOCALFILE_INFO = "property_ipc_upgrade_localfileInfo";

	/** 下载ipc升级文件时保存version **/
	public static final String PROPERTY_SAVE_IPC_DOWN_VERSION = "property_ipc_down_version";

	/** ipc密码 **/
	public static final String PROPERTY_SAVE_IPC_PASSWORD = "property_ipc_password";
	/** 信鸽的tokenid */
	public static final String PROPERTY_SAVE_XG_TOKEN_ID = "property_xg_tokenid";
	/** ipc型号 **/
	public static final String PROPERTY_SAVE_IPC_MODEL = "property_ipc_model";
	/** 下载的ipc升级文件型号 **/
	public static final String PROPERTY_SAVE_IPC_DOWNLOAD_MODEL = "property_ipc_download_model";
	/**保存ipc升级文件路径 **/
	public static final String PROPERTY_SAVE_IPC_FILE_PATH = "property_ipc_file_path";

	private SharedPreferences preference = null;

	private Editor mEditor = null;

	public SharedPrefUtil(Activity activity) {
		preference = activity.getPreferences(Activity.MODE_PRIVATE);
	}

	public void setTokenId(String tokenid) {
		preference.edit().putString(PROPERTY_SAVE_XG_TOKEN_ID, tokenid).commit();
	}

	public String getTolenId() {
		return preference.getString(PROPERTY_SAVE_XG_TOKEN_ID, "");
	}

	public void setIsLiveNormalExit(boolean isExit) {
		preference.edit().putBoolean(PROPERTY_LIVE_NORMAL_EXIT, isExit).commit();
	}

	public boolean getIsLiveNormalExit() {
		return preference.getBoolean(PROPERTY_LIVE_NORMAL_EXIT, true);
	}

	/**
	 * 保存下载成功的BIN文件信息
	 * 
	 * @param ipcInfo
	 * @author jyf
	 * @date 2015年6月25日
	 */
	public void setIpcLocalFileInfo(String ipcInfo) {
		preference.edit().putString(PROPERTY_IPC_UPGRADE_LOCALFILE_INFO, ipcInfo).commit();
	}

	/**
	 * 获取本地升级文件信息
	 * 
	 * @return
	 * @author jyf
	 * @date 2015年6月25日
	 */
	public String getIpcLocalFileInfo() {
		return preference.getString(PROPERTY_IPC_UPGRADE_LOCALFILE_INFO, "");
	}

	/**
	 * 是否显示 开机时的 新版本特性
	 * 
	 * @author 钱伟
	 * @date 2012/09/14
	 */
	public void setIsShowHelp(boolean isShow) {
		preference.edit().putBoolean(PROPERTY_SHOW_HELP, isShow).commit();
	}

	/**
	 * 获取 开机时的 新版本特性的显示状态
	 * 
	 * @author 钱伟
	 * @date 2012/09/14
	 */
	public boolean getIsShowHelp() {
		return preference.getBoolean(PROPERTY_SHOW_HELP, false);
	}

	/**
	 * 设置是否为首次启动
	 * 
	 * @param isFirst
	 */
	public void setIsFirstStart(boolean isFirst) {
		preference.edit().putBoolean(PROPERTY_IS_FIRST_START, isFirst).commit();
	}

	/**
	 * 获取是否为首次启动
	 * 
	 * @return
	 */
	public boolean getIsFirstStart() {
		return preference.getBoolean(PROPERTY_IS_FIRST_START, true);
	}

	/**
	 * 判断是否需要显示Logo
	 * 
	 * @return
	 */
	public boolean getIsShowLogoAnim() {
		return preference.getBoolean(PROPERTY_SHOW_LOGOANMI, false);
	}

	/**
	 * 保存Logo显示完成后的数据
	 */
	public void setShowLogoAnimPreferencesEdit() {
		Editor editor = preference.edit();
		editor.putBoolean(PROPERTY_SHOW_LOGOANMI, true);
		editor.commit();
		editor = null;
	}

	/**
	 * 判断是否显示第一次显示的选择男女
	 * 
	 * @return
	 */
	public boolean getIsShowSetUserInfo() {
		return preference.getBoolean(PROPERTY_SHOW_NAVIMASK, true);
	}

	/**
	 * 设置是否显示第一次显示的选择男女
	 */
	public void setIsShowSetUserInfo(boolean isShow) {
		SharedPreferences.Editor editor = preference.edit();
		editor.putBoolean(PROPERTY_SHOW_NAVIMASK, isShow);
		editor.commit();
		editor = null;
	}

	/**
	 * 获取是否已经开启通讯录
	 * 
	 * @return
	 */
	public boolean getIsOpenContact() {
		return preference.getBoolean(PROPERTY_OPEN_CONTACT, false);
	}

	/**
	 * 保存是否已经开启通讯录
	 * 
	 * @param flag
	 */
	public void setOpenContact(boolean flag) {
		preference.edit().putBoolean(PROPERTY_OPEN_CONTACT, flag).commit();
	}

	/**
	 * 保存 见面功能显示所有用户以及目的地 提示气泡状态
	 * 
	 * @param state
	 * @author qianwei
	 * @date 2013/01/05
	 */
	public void setMeetShowAllUsersPointState(boolean state) {
		preference.edit().putBoolean(PROPERTY_MEET_SHOW_ALL_USERS_POINT, state).commit();
	}

	/**
	 * 获取 见面功能显示所有用户以及目的地 提示气泡状态
	 * 
	 * @param state
	 * @author qianwei
	 * @date 2013/01/05
	 */
	public boolean getMeetShowAllUsersPointState() {
		return preference.getBoolean(PROPERTY_MEET_SHOW_ALL_USERS_POINT, false);
	}

	/**
	 * 见面三创建群组后，有人加入群组前，地图界面催促 提示
	 * 
	 * @param state
	 * @author qianwei
	 * @date 2013/01/05
	 */
	public void setMapMeetConsoleSound(boolean state) {
		preference.edit().putBoolean(PROPERTY_MAP_MEET_CONSOLE_SOUND, state).commit();
	}

	/**
	 * 见面三创建群组后，有人加入群组前，地图界面催促 提示
	 * 
	 * @param state
	 * @author qianwei
	 * @date 2013/01/05
	 */
	public boolean getMapMeetConsoleSound() {
		return preference.getBoolean(PROPERTY_MAP_MEET_CONSOLE_SOUND, false);
	}

	/**
	 * 判断是否预加载主页面
	 * 
	 * @param load
	 */
	public void setPrepareLoadMain(boolean load) {
		preference.edit().putBoolean(PROPERTY_PREPARE_LOADMAIN, load).commit();
	}

	/**
	 * 获取是否load主页面,默认为load
	 * 
	 * @return
	 */
	public boolean getPrepareLoadMain() {
		return preference.getBoolean(PROPERTY_PREPARE_LOADMAIN, true);
	}

	/**
	 * 设置聊天列表输入方式选择
	 * 
	 * @param state
	 *            INPUT_MODE_TEXT 0 INPUT_MODE_VOICE 1
	 * @author qianwei
	 * @date 2013/06/25
	 */
	public void setInputMethodState(int state) {
		preference.edit().putInt(PROPERTY_CHAT_INPUT_METHOD, state).commit();
	}

	/**
	 * 获取聊天列表输入方式选择
	 * 
	 * @param state
	 *            INPUT_MODE_TEXT 0 INPUT_MODE_VOICE 1
	 * @author qianwei
	 * @date 2013/06/25
	 */
	public int getInputMethodState() {
		return preference.getInt(PROPERTY_CHAT_INPUT_METHOD, 1);
	}

	/**
	 * 设置是否显示导航时实时下载实景图提示对话框
	 * 
	 * @param state
	 * @author caoyp
	 * @date 2013/08/12
	 */
	public void setIsShowDownloadNaviRealSenseMapDialog(boolean state) {
		preference.edit().putBoolean(PROPERTY_IS_SHOWDOWNLOADNAVIREALSENSEMAPDIALOG, state).commit();
	}

	/**
	 * 获取是否显示导航时实时现在实景图提示对话框
	 * 
	 * @param state
	 *            true 显示，false 不显示
	 * @author caoyp
	 * @date 2013/08/12
	 */
	public boolean getIsShowDownloadNaviRealSenseMapDialog() {
		return preference.getBoolean(PROPERTY_IS_SHOWDOWNLOADNAVIREALSENSEMAPDIALOG, true);
	}

	/**
	 * 设置是否需要显示引导图
	 * 
	 * @param state
	 * @author caoyp
	 * @date 2013/08/12
	 */
	public void setIsShowNewUserGuide(boolean state) {
		preference.edit().putBoolean(PROPERTY_IS_SHOWNEWUSERGUIDE, state).commit();
	}

	/**
	 * 获取是否显示导航时实时现在实景图提示对话框
	 * 
	 * @param state
	 *            true 显示，false 不显示
	 * @author caoyp
	 * @date 2013/08/12
	 */
	public boolean getIsShowNewUserGuide() {
		return preference.getBoolean(PROPERTY_IS_SHOWNEWUSERGUIDE, true);
	}

	/**
	 * 保存ipc版本号
	 * 
	 * @param ipcVersion
	 */
	public void saveIPCVersion(String ipcVersion) {
		preference.edit().putString(PROPERTY_SAVE_IPCVERSION, ipcVersion).commit();
	}

	/**
	 * 获取保存的版本号
	 * 
	 * @return
	 */
	public String getIPCVersion() {
		return preference.getString(PROPERTY_SAVE_IPCVERSION, "");
	}

	/**
	 * 保存ipc匹配信息
	 * 
	 * @param jsonArray
	 */
	public void saveIPCMatchInfo(String jsonArray) {
		preference.edit().putString(PROPERTY_SAVE_IPCMATCH_INFO, jsonArray).commit();
	}

	public String getIPCMatchInfo() {
		return preference.getString(PROPERTY_SAVE_IPCMATCH_INFO, "");
	}

	/**
	 * 保存ipc文件大小
	 * 
	 * @param filesize
	 */
	public void saveIpcFileSize(String filesize) {
		preference.edit().putString(PROPERTY_SAVE_IPC_FILESIZE, filesize).commit();
	}

	public String getIPCFileSize() {
		return preference.getString(PROPERTY_SAVE_IPC_FILESIZE, "");
	}

	/**
	 * 保存ipc更新描述
	 * 
	 * @param appcontent
	 */
	public void saveIpcContent(String appcontent) {
		preference.edit().putString(PROPERTY_SAVE_IPC_CONTENT, appcontent).commit();
	}

	public String getIPCContent() {
		return preference.getString(PROPERTY_SAVE_IPC_CONTENT, "");
	}

	/**
	 * 保存ipc url
	 * 
	 * @param ipcVersion
	 */
	public void saveIPCURL(String ipcUrl) {
		preference.edit().putString(PROPERTY_SAVE_IPC_URL, ipcUrl).commit();
	}

	public String getIPCURL() {
		return preference.getString(PROPERTY_SAVE_IPC_URL, "");
	}

	/**
	 * 保存ipc 的Path
	 * 
	 * @param ipcVersion
	 */
	public void saveIPCPath(String ipcPath) {
		preference.edit().putString(PROPERTY_SAVE_IPC_PATH, ipcPath).commit();
	}

	public String getIPCPath() {
		return preference.getString(PROPERTY_SAVE_IPC_PATH, "");
	}

	/**
	 * 下载时保存ipcVersion
	 */
	public void saveIPCDownVersion(String ipcVersion) {
		preference.edit().putString(PROPERTY_SAVE_IPC_DOWN_VERSION, ipcVersion).commit();
	}

	public String getIPCDownVersion() {
		return preference.getString(PROPERTY_SAVE_IPC_DOWN_VERSION, "");
	}

	/**
	 * 清除数据
	 */
	public void removeIPC() {
		mEditor = preference.edit();
		mEditor.remove(PROPERTY_SAVE_IPCVERSION);
		mEditor.remove(PROPERTY_SAVE_IPC_FILESIZE);
		mEditor.remove(PROPERTY_SAVE_IPC_CONTENT);
		mEditor.remove(PROPERTY_SAVE_IPC_URL);
		mEditor.remove(PROPERTY_SAVE_IPC_PATH);
		mEditor.commit();
	}

	public void saveIpcPwd(String ipcPwd) {
		preference.edit().putString(PROPERTY_SAVE_IPC_PASSWORD, ipcPwd).commit();
	}

	public String getIpcPwd() {
		return preference.getString(PROPERTY_SAVE_IPC_PASSWORD, "");
	}
	
	/**
	 * 保存ipcModel
	 * @param ipcModel
	 */
	public void saveDownloadIpcModel(String ipcModel) {
		preference.edit().putString(PROPERTY_SAVE_IPC_DOWNLOAD_MODEL, ipcModel).commit();
	}
	/**
	 * 获取ipcModel
	 */
	public String getDownloadIpcModel() {
		return preference.getString(PROPERTY_SAVE_IPC_DOWNLOAD_MODEL, "");
	}
	
	public void saveIpcModel(String ipcModel) {
		preference.edit().putString(PROPERTY_SAVE_IPC_MODEL, ipcModel).commit();
	}
	
	public String getIpcModel() {
		return preference.getString(PROPERTY_SAVE_IPC_MODEL, "");
	}

}
