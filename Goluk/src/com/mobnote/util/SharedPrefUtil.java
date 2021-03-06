package com.mobnote.util;

import com.mobnote.application.GolukApplication;

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
	
	/**用户的token**/
	public static final String USER_TOKEN = "user_token";
	
	/**当前登录的用户信息**/
	public static final String USER_INFO = "user_info";
	
	/**登陆的用户密码 和手机号**/
	public static final String USER_PASSWORD = "user_password";

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Android config, by Micle
	public static final String PROPERTY_Config_Server_Flag = "property_config_serverflag";
	public static final String PROPERTY_Config_Storage = "property_config_storage";

	/** 信鸽的tokenid */
	public static final String PROPERTY_SAVE_XG_TOKEN_ID = "property_xg_tokenid";
	/** ipc型号 **/
	public static final String PROPERTY_SAVE_IPC_MODEL = "property_ipc_model";
	/** 下载的ipc升级文件型号 **/
	public static final String PROPERTY_SAVE_IPC_DOWNLOAD_MODEL = "property_ipc_download_model";
	/**保存ipc升级文件路径 **/
	public static final String PROPERTY_SAVE_IPC_FILE_PATH = "property_ipc_file_path";

	public static final String BANNER_LIST_STRING = "banner_list_string";
	public static final String LOCATION_CITY_ID = "location_city_id";
	/**保存设备编号**/
	public static final String PROPERTY_SAVE_IPC_NUMBER = "ipc_number";


	public static void setTokenId(String tokenid) {
		SharedPreferences preference = GolukApplication.getInstance().getSharedPreferences("GuideActivity", Activity.MODE_PRIVATE);
		preference.edit().putString(PROPERTY_SAVE_XG_TOKEN_ID, tokenid).commit();
	}

	public static String getTolenId() {
		SharedPreferences preference = GolukApplication.getInstance().getSharedPreferences("GuideActivity", Activity.MODE_PRIVATE);
		return preference.getString(PROPERTY_SAVE_XG_TOKEN_ID, "");
	}

	public static void setIsLiveNormalExit(boolean isExit) {
		SharedPreferences preference = GolukApplication.getInstance().getSharedPreferences("GuideActivity", Activity.MODE_PRIVATE);
		preference.edit().putBoolean(PROPERTY_LIVE_NORMAL_EXIT, isExit).commit();
	}

	public static boolean getIsLiveNormalExit() {
		SharedPreferences preference = GolukApplication.getInstance().getSharedPreferences("GuideActivity", Activity.MODE_PRIVATE);
		return preference.getBoolean(PROPERTY_LIVE_NORMAL_EXIT, true);
	}

	/**
	 * 保存下载成功的BIN文件信息
	 * 
	 * @param ipcInfo
	 * @author jyf
	 * @date 2015年6月25日
	 */
	public static void setIpcLocalFileInfo(String ipcInfo) {
		SharedPreferences preference = GolukApplication.getInstance().getSharedPreferences("GuideActivity", Activity.MODE_PRIVATE);
		preference.edit().putString(PROPERTY_IPC_UPGRADE_LOCALFILE_INFO, ipcInfo).commit();
	}

	/**
	 * 获取本地升级文件信息
	 * 
	 * @return
	 * @author jyf
	 * @date 2015年6月25日
	 */
	public static String getIpcLocalFileInfo() {
		SharedPreferences preference = GolukApplication.getInstance().getSharedPreferences("GuideActivity", Activity.MODE_PRIVATE);
		return preference.getString(PROPERTY_IPC_UPGRADE_LOCALFILE_INFO, "");
	}

	/**
	 * 是否显示 开机时的 新版本特性
	 * 
	 * @author 钱伟
	 * @date 2012/09/14
	 */
	public static void setIsShowHelp(boolean isShow) {
		SharedPreferences preference = GolukApplication.getInstance().getSharedPreferences("GuideActivity", Activity.MODE_PRIVATE);
		preference.edit().putBoolean(PROPERTY_SHOW_HELP, isShow).commit();
	}

	/**
	 * 获取 开机时的 新版本特性的显示状态
	 * 
	 * @author 钱伟
	 * @date 2012/09/14
	 */
	public static boolean getIsShowHelp() {
		SharedPreferences preference = GolukApplication.getInstance().getSharedPreferences("GuideActivity", Activity.MODE_PRIVATE);
		return preference.getBoolean(PROPERTY_SHOW_HELP, false);
	}

	/**
	 * 设置是否为首次启动
	 * 
	 * @param isFirst
	 */
	public static void setIsFirstStart(boolean isFirst) {
		SharedPreferences preference = GolukApplication.getInstance().getSharedPreferences("GuideActivity", Activity.MODE_PRIVATE);
		preference.edit().putBoolean(PROPERTY_IS_FIRST_START, isFirst).commit();
	}

	/**
	 * 获取是否为首次启动
	 * 
	 * @return
	 */
	public static boolean getIsFirstStart() {
		SharedPreferences preference = GolukApplication.getInstance().getSharedPreferences("GuideActivity", Activity.MODE_PRIVATE);
		return preference.getBoolean(PROPERTY_IS_FIRST_START, true);
	}

	/**
	 * 判断是否需要显示Logo
	 * 
	 * @return
	 */
	public static boolean getIsShowLogoAnim() {
		SharedPreferences preference = GolukApplication.getInstance().getSharedPreferences("GuideActivity", Activity.MODE_PRIVATE);
		return preference.getBoolean(PROPERTY_SHOW_LOGOANMI, false);
	}

	/**
	 * 保存Logo显示完成后的数据
	 */
	public static void setShowLogoAnimPreferencesEdit() {
		SharedPreferences preference = GolukApplication.getInstance().getSharedPreferences("GuideActivity", Activity.MODE_PRIVATE);
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
	public static boolean getIsShowSetUserInfo() {
		SharedPreferences preference = GolukApplication.getInstance().getSharedPreferences("GuideActivity", Activity.MODE_PRIVATE);
		return preference.getBoolean(PROPERTY_SHOW_NAVIMASK, true);
	}

	/**
	 * 设置是否显示第一次显示的选择男女
	 */
	public static void setIsShowSetUserInfo(boolean isShow) {
		SharedPreferences preference = GolukApplication.getInstance().getSharedPreferences("GuideActivity", Activity.MODE_PRIVATE);
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
	public static boolean getIsOpenContact() {
		SharedPreferences preference = GolukApplication.getInstance().getSharedPreferences("GuideActivity", Activity.MODE_PRIVATE);
		return preference.getBoolean(PROPERTY_OPEN_CONTACT, false);
	}

	/**
	 * 保存是否已经开启通讯录
	 * 
	 * @param flag
	 */
	public static void setOpenContact(boolean flag) {
		SharedPreferences preference = GolukApplication.getInstance().getSharedPreferences("GuideActivity", Activity.MODE_PRIVATE);
		preference.edit().putBoolean(PROPERTY_OPEN_CONTACT, flag).commit();
	}

	/**
	 * 保存 见面功能显示所有用户以及目的地 提示气泡状态
	 * 
	 * @param state
	 * @author qianwei
	 * @date 2013/01/05
	 */
	public static void setMeetShowAllUsersPointState(boolean state) {
		SharedPreferences preference = GolukApplication.getInstance().getSharedPreferences("GuideActivity", Activity.MODE_PRIVATE);
		preference.edit().putBoolean(PROPERTY_MEET_SHOW_ALL_USERS_POINT, state).commit();
	}

	/**
	 * 获取 见面功能显示所有用户以及目的地 提示气泡状态
	 * 
	 * @param state
	 * @author qianwei
	 * @date 2013/01/05
	 */
	public static boolean getMeetShowAllUsersPointState() {
		SharedPreferences preference = GolukApplication.getInstance().getSharedPreferences("GuideActivity", Activity.MODE_PRIVATE);
		return preference.getBoolean(PROPERTY_MEET_SHOW_ALL_USERS_POINT, false);
	}

	/**
	 * 见面三创建群组后，有人加入群组前，地图界面催促 提示
	 * 
	 * @param state
	 * @author qianwei
	 * @date 2013/01/05
	 */
	public static void setMapMeetConsoleSound(boolean state) {
		SharedPreferences preference = GolukApplication.getInstance().getSharedPreferences("GuideActivity", Activity.MODE_PRIVATE);
		preference.edit().putBoolean(PROPERTY_MAP_MEET_CONSOLE_SOUND, state).commit();
	}

	/**
	 * 见面三创建群组后，有人加入群组前，地图界面催促 提示
	 * 
	 * @param state
	 * @author qianwei
	 * @date 2013/01/05
	 */
	public static boolean getMapMeetConsoleSound() {
		SharedPreferences preference = GolukApplication.getInstance().getSharedPreferences("GuideActivity", Activity.MODE_PRIVATE);
		return preference.getBoolean(PROPERTY_MAP_MEET_CONSOLE_SOUND, false);
	}

	/**
	 * 判断是否预加载主页面
	 * 
	 * @param load
	 */
	public static void setPrepareLoadMain(boolean load) {
		SharedPreferences preference = GolukApplication.getInstance().getSharedPreferences("GuideActivity", Activity.MODE_PRIVATE);
		preference.edit().putBoolean(PROPERTY_PREPARE_LOADMAIN, load).commit();
	}

	/**
	 * 获取是否load主页面,默认为load
	 * 
	 * @return
	 */
	public static boolean getPrepareLoadMain() {
		SharedPreferences preference = GolukApplication.getInstance().getSharedPreferences("GuideActivity", Activity.MODE_PRIVATE);
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
	public static void setInputMethodState(int state) {
		SharedPreferences preference = GolukApplication.getInstance().getSharedPreferences("GuideActivity", Activity.MODE_PRIVATE);
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
	public static int getInputMethodState() {
		SharedPreferences preference = GolukApplication.getInstance().getSharedPreferences("GuideActivity", Activity.MODE_PRIVATE);
		return preference.getInt(PROPERTY_CHAT_INPUT_METHOD, 1);
	}

	/**
	 * 设置是否显示导航时实时下载实景图提示对话框
	 * 
	 * @param state
	 * @author caoyp
	 * @date 2013/08/12
	 */
	public static void setIsShowDownloadNaviRealSenseMapDialog(boolean state) {
		SharedPreferences preference = GolukApplication.getInstance().getSharedPreferences("GuideActivity", Activity.MODE_PRIVATE);
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
	public static boolean getIsShowDownloadNaviRealSenseMapDialog() {
		SharedPreferences preference = GolukApplication.getInstance().getSharedPreferences("GuideActivity", Activity.MODE_PRIVATE);
		return preference.getBoolean(PROPERTY_IS_SHOWDOWNLOADNAVIREALSENSEMAPDIALOG, true);
	}

	/**
	 * 设置是否需要显示引导图
	 * 
	 * @param state
	 * @author caoyp
	 * @date 2013/08/12
	 */
	public static void setIsShowNewUserGuide(boolean state) {
		SharedPreferences preference = GolukApplication.getInstance().getSharedPreferences("GuideActivity", Activity.MODE_PRIVATE);
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
	public static boolean getIsShowNewUserGuide() {
		SharedPreferences preference = GolukApplication.getInstance().getSharedPreferences("GuideActivity", Activity.MODE_PRIVATE);
		return preference.getBoolean(PROPERTY_IS_SHOWNEWUSERGUIDE, true);
	}

	/**
	 * 保存ipc版本号
	 * 
	 * @param ipcVersion
	 */
	public static void saveIPCVersion(String ipcVersion) {
		SharedPreferences preference = GolukApplication.getInstance().getSharedPreferences("GuideActivity", Activity.MODE_PRIVATE);
		preference.edit().putString(PROPERTY_SAVE_IPCVERSION, ipcVersion).commit();
	}

	/**
	 * 获取保存的版本号
	 * 
	 * @return
	 */
	public static String getIPCVersion() {
		SharedPreferences preference = GolukApplication.getInstance().getSharedPreferences("GuideActivity", Activity.MODE_PRIVATE);
		return preference.getString(PROPERTY_SAVE_IPCVERSION, "");
	}

	/**
	 * 保存ipc匹配信息
	 * 
	 * @param jsonArray
	 */
	public static void saveIPCMatchInfo(String jsonArray) {
		SharedPreferences preference = GolukApplication.getInstance().getSharedPreferences("GuideActivity", Activity.MODE_PRIVATE);
		preference.edit().putString(PROPERTY_SAVE_IPCMATCH_INFO, jsonArray).commit();
	}

	public static String getIPCMatchInfo() {
		SharedPreferences preference = GolukApplication.getInstance().getSharedPreferences("GuideActivity", Activity.MODE_PRIVATE);
		return preference.getString(PROPERTY_SAVE_IPCMATCH_INFO, "");
	}

	/**
	 * 保存ipc文件大小
	 * 
	 * @param filesize
	 */
	public static void saveIpcFileSize(String filesize) {
		SharedPreferences preference = GolukApplication.getInstance().getSharedPreferences("GuideActivity", Activity.MODE_PRIVATE);
		preference.edit().putString(PROPERTY_SAVE_IPC_FILESIZE, filesize).commit();
	}

	public static String getIPCFileSize() {
		SharedPreferences preference = GolukApplication.getInstance().getSharedPreferences("GuideActivity", Activity.MODE_PRIVATE);
		return preference.getString(PROPERTY_SAVE_IPC_FILESIZE, "");
	}

	/**
	 * 保存ipc更新描述
	 * 
	 * @param appcontent
	 */
	public static void saveIpcContent(String appcontent) {
		SharedPreferences preference = GolukApplication.getInstance().getSharedPreferences("GuideActivity", Activity.MODE_PRIVATE);
		preference.edit().putString(PROPERTY_SAVE_IPC_CONTENT, appcontent).commit();
	}

	public static String getIPCContent() {
		SharedPreferences preference = GolukApplication.getInstance().getSharedPreferences("GuideActivity", Activity.MODE_PRIVATE);
		return preference.getString(PROPERTY_SAVE_IPC_CONTENT, "");
	}

	/**
	 * 保存ipc url
	 * 
	 * @param ipcVersion
	 */
	public static void saveIPCURL(String ipcUrl) {
		SharedPreferences preference = GolukApplication.getInstance().getSharedPreferences("GuideActivity", Activity.MODE_PRIVATE);
		preference.edit().putString(PROPERTY_SAVE_IPC_URL, ipcUrl).commit();
	}

	public static String getIPCURL() {
		SharedPreferences preference = GolukApplication.getInstance().getSharedPreferences("GuideActivity", Activity.MODE_PRIVATE);
		return preference.getString(PROPERTY_SAVE_IPC_URL, "");
	}

	/**
	 * 保存ipc 的Path
	 * 
	 * @param ipcVersion
	 */
	public static void saveIPCPath(String ipcPath) {
		SharedPreferences preference = GolukApplication.getInstance().getSharedPreferences("GuideActivity", Activity.MODE_PRIVATE);
		preference.edit().putString(PROPERTY_SAVE_IPC_PATH, ipcPath).commit();
	}

	public static String getIPCPath() {
		SharedPreferences preference = GolukApplication.getInstance().getSharedPreferences("GuideActivity", Activity.MODE_PRIVATE);
		return preference.getString(PROPERTY_SAVE_IPC_PATH, "");
	}

	/**
	 * 下载时保存ipcVersion
	 */
	public static void saveIPCDownVersion(String ipcVersion) {
		SharedPreferences preference = GolukApplication.getInstance().getSharedPreferences("GuideActivity", Activity.MODE_PRIVATE);
		preference.edit().putString(PROPERTY_SAVE_IPC_DOWN_VERSION, ipcVersion).commit();
	}

	public static String getIPCDownVersion() {
		SharedPreferences preference = GolukApplication.getInstance().getSharedPreferences("GuideActivity", Activity.MODE_PRIVATE);
		return preference.getString(PROPERTY_SAVE_IPC_DOWN_VERSION, "");
	}
	
	/**
	 * 清除数据
	 */
	public static void removeIPC() {
		SharedPreferences preference = GolukApplication.getInstance().getSharedPreferences("GuideActivity", Activity.MODE_PRIVATE);
		Editor editor = preference.edit();
		editor.remove(PROPERTY_SAVE_IPCVERSION);
		editor.remove(PROPERTY_SAVE_IPC_FILESIZE);
		editor.remove(PROPERTY_SAVE_IPC_CONTENT);
		editor.remove(PROPERTY_SAVE_IPC_URL);
		editor.remove(PROPERTY_SAVE_IPC_PATH);
		editor.commit();
	}

	public static void saveIpcPwd(String ipcPwd) {
		SharedPreferences preference = GolukApplication.getInstance().getSharedPreferences("GuideActivity", Activity.MODE_PRIVATE);
		preference.edit().putString(PROPERTY_SAVE_IPC_PASSWORD, ipcPwd).commit();
	}

	public static String getIpcPwd() {
		SharedPreferences preference = GolukApplication.getInstance().getSharedPreferences("GuideActivity", Activity.MODE_PRIVATE);
		return preference.getString(PROPERTY_SAVE_IPC_PASSWORD, "");
	}
	
	/**
	 * 保存ipcModel
	 * @param ipcModel
	 */
	public static void saveDownloadIpcModel(String ipcModel) {
		SharedPreferences preference = GolukApplication.getInstance().getSharedPreferences("GuideActivity", Activity.MODE_PRIVATE);
		preference.edit().putString(PROPERTY_SAVE_IPC_DOWNLOAD_MODEL, ipcModel).commit();
	}
	/**
	 * 获取ipcModel
	 */
	public static String getDownloadIpcModel() {
		SharedPreferences preference = GolukApplication.getInstance().getSharedPreferences("GuideActivity", Activity.MODE_PRIVATE);
		return preference.getString(PROPERTY_SAVE_IPC_DOWNLOAD_MODEL, "");
	}
	
	public static void saveIpcModel(String ipcModel) {
		SharedPreferences preference = GolukApplication.getInstance().getSharedPreferences("GuideActivity", Activity.MODE_PRIVATE);
		preference.edit().putString(PROPERTY_SAVE_IPC_MODEL, ipcModel).commit();
	}
	
	public static String getIpcModel() {
		SharedPreferences preference = GolukApplication.getInstance().getSharedPreferences("GuideActivity", Activity.MODE_PRIVATE);
		return preference.getString(PROPERTY_SAVE_IPC_MODEL, "");
	}

	public static String getBannerListString() {
		SharedPreferences preference = GolukApplication.getInstance().getSharedPreferences("GuideActivity", Activity.MODE_PRIVATE);
		return preference.getString(BANNER_LIST_STRING, "");
	}
	
	public static void saveBannerListString(String bannerString) {
		SharedPreferences preference = GolukApplication.getInstance().getSharedPreferences("GuideActivity", Activity.MODE_PRIVATE);
		preference.edit().putString(BANNER_LIST_STRING, bannerString).commit();
	}

	public static String getCityIDString() {
		SharedPreferences preference = GolukApplication.getInstance().getSharedPreferences("MainActivity", Activity.MODE_PRIVATE);
		return preference.getString(LOCATION_CITY_ID, "");
	}
	
	public static void setCityIDString(String cityCode) {
		SharedPreferences preference = GolukApplication.getInstance().getSharedPreferences("MainActivity", Activity.MODE_PRIVATE);
		preference.edit().putString(LOCATION_CITY_ID, cityCode).commit();
	}
	
	public static void saveIPCNumber(String ipcNumber) {
		SharedPreferences preference = GolukApplication.getInstance().getSharedPreferences("UnbindActivity", Activity.MODE_PRIVATE);
		preference.edit().putString(PROPERTY_SAVE_IPC_NUMBER, ipcNumber).commit();
	}
	
	public static String getIPCNumber() {
		SharedPreferences preference = GolukApplication.getInstance().getSharedPreferences("UnbindActivity", Activity.MODE_PRIVATE);
		return preference.getString(PROPERTY_SAVE_IPC_NUMBER, "");
	}
	
	public static String getUserToken(){
		SharedPreferences preference = GolukApplication.getInstance().getSharedPreferences("GuideActivity", Activity.MODE_PRIVATE);
		return preference.getString(USER_TOKEN, "");
	}
	
	public static void saveUserToken(String token) {
		SharedPreferences preference = GolukApplication.getInstance().getSharedPreferences("GuideActivity", Activity.MODE_PRIVATE);
		preference.edit().putString(USER_TOKEN, token).commit();
	}
	
	public static String getUserInfo(){
		SharedPreferences preference = GolukApplication.getInstance().getSharedPreferences("GuideActivity", Activity.MODE_PRIVATE);
		return preference.getString(USER_INFO, "");
	}
	
	public static void saveUserInfo(String user) {
		SharedPreferences preference = GolukApplication.getInstance().getSharedPreferences("GuideActivity", Activity.MODE_PRIVATE);
		preference.edit().putString(USER_INFO, user).commit();
	}
	
	public static void saveUserPwd(String pwd){
		SharedPreferences preference = GolukApplication.getInstance().getSharedPreferences("GuideActivity", Activity.MODE_PRIVATE);
		preference.edit().putString(USER_PASSWORD, pwd).commit();
	}
	
	public static String getUserPwd(){
		SharedPreferences preference = GolukApplication.getInstance().getSharedPreferences("GuideActivity", Activity.MODE_PRIVATE);
		return preference.getString(USER_PASSWORD, "");
	}
	
	
}
