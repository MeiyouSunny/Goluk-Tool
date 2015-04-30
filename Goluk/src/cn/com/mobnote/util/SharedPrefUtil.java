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

	private SharedPreferences preference = null;

	public SharedPrefUtil(Activity activity) {
		preference = activity.getPreferences(Activity.MODE_PRIVATE);
	}

	public void setIsLiveNormalExit(boolean isExit) {
		preference.edit().putBoolean(PROPERTY_LIVE_NORMAL_EXIT, isExit).commit();
	}

	public boolean getIsLiveNormalExit() {
		return preference.getBoolean(PROPERTY_LIVE_NORMAL_EXIT, true);
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
}
