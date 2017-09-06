package com.rd.veuisdk.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;

import com.rd.lib.utils.CoreUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FilenameFilter;

/**
 * 应用持久化配置
 * 
 * @author abreal
 */
public class AppConfiguration {

	public static final int DEFAULT_WIDTH = 100;// 字幕特效默认100px宽高

	public static float ASPECTRATIO = 1.15f;
	private static SharedPreferences sp;
	private static boolean m_bHWCodecEnabled = CoreUtils.hasJELLY_BEAN_MR2();

	public static void initContext(Context context) {
		sp = context.getSharedPreferences("xpk_data", Context.MODE_PRIVATE);
	}

	private static final String ISFIRSTSHOWSPLIT = "isFirstShowSplit";
	private static final String ISFIRSTSHOWDRAG_LISTVIEW = "isfirstshowdrag_listview";
	private static final String ISFIRSTSHOWDRAG_SPLIT = "isfirstshowdrag_split";
	private static final String ISFIRSTSHOW_DEL = "isfirstshow_del";
	private static final String ISFIRSTSHOW_SUB = "isfirstshow_sub";
	private static final String ISFIRSTSHOW_SP = "isfirstshow_sp";
	private static final String ISFIRSTSHOW_CROP = "裁剪横屏视频";
	private static final String ISFIRSTSHOW_AUDIO = "isfirstshow_audio";
	private static final String TRAININGCAPTUREVIDEO = "TrainingCaptureVideo";

	private static final String ISFIRSTSHOW_INSERT_SUB = "isfirstshow_insert_sub";
	private static final String ISFIRSTSHOW_INSERT_SP = "isfirstshow_insert_sp";
	private static final String ISFIRSTSHOW_DRAG_SP = "拖动边框,调整特效起始位置";
	private static final String ISFIRSTSHOW_DRAG_SUB = "拖动边框,调整字幕起始位置";
	private static final String ISFIRSTSHOW_DIALOG_SPLIT = "分割提示";
	private static final String ISFIRSTSHOW_TRANSITION = "点击小图标更换转场样式";
	private static final String USE_HWCODER_CHECKING = "use_hwcoder_checking";
	private static final String USE_CUSTOM_UI = "use_custom_ui";
	public static final String COMPRESS_CONFIGURATION_KEY = "compress_configuration_key";
	public static final String TRIM_CONFIGURATION_KEY = "trim_configuration_key";
	public static final String FACEU_CONFIGURATION_KEY = "faceu_configuration_key";
	public static final String CAMERA_CONFIGURATION_KEY = "camera_configuration_key";
	public static final String UI_CONFIGURATION_KEY = "ui_configuration_key";
	public static final String EXPORT_CONFIGURATION_KEY = "export_configuration_key";
	public static final String CAMERA_ENABLE_BEAUTY_KEY = "camera_enable_beauty";

	private static final String TTF_ICON_VERSION = "ttf_icon_version";
	private static final String SUB_ICON_VERSION = "sub_icon_version";
	private static final String SPECIAL_ICON_VERSION = "special_icon_version";

	/**
	 * 字体icon版本 是最新且存在
	 * 
	 * @param timeUnix
	 * @return
	 */
	public static boolean checkTTFVersionIsLasted(String timeUnix) {

		String infos = sp.getString(TTF_ICON_VERSION, null);
		if (TextUtils.isEmpty(infos)) {
			return false;
		}
		return checkExist(infos, timeUnix);
	}

	private static boolean checkExist(String infos, String timeUnix) {
		String[] arr = jstringToArr(infos);
		boolean exist = false;
		if (null != arr) {
			exist = arr[0].equals(timeUnix); // 第一步：检测版本号一致
			if (exist) {
				File f = new File(arr[1]); // 第二步:检测文件夹存在
				exist = f.isDirectory() && f.exists();
				if (exist) {
					// 第三步:检测png文件数目一致
					String[] iconNames = f.list(new FilenameFilter() {

						@Override
						public boolean accept(File dir, String filename) {
							return filename.endsWith(".png");
						}
					});
					exist = (null != iconNames && arr[2].equals(Integer
							.toString(iconNames.length)));

				}
			}
		}
		return exist;
	}

	private static String versionToJstring(String timeUnix, String DirPath,
			int count) {
		JSONObject jobj = new JSONObject();
		try {
			jobj.put("timeunix", timeUnix);
			jobj.put("DirPath", DirPath);
			jobj.put("count", count);
			return jobj.toString();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;

	}

	private static String[] jstringToArr(String jstr) {
		try {
			JSONObject jobj = new JSONObject(jstr);
			// 顺序：timeunix、dirpath、count
			String[] arr = { jobj.optString("timeunix", "0"),
					jobj.optString("DirPath", ""), jobj.optString("count", "") };
			return arr;

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;

	}

	public static void setTTFVersion(String timeUnix, String DirPath, int count) {
		Editor editor = sp.edit();
		editor.putString(TTF_ICON_VERSION,
				versionToJstring(timeUnix, DirPath, count));
		editor.commit();
	}

	/**
	 * 字幕icon版本
	 * 
	 * @return
	 */
	public static boolean checkSubIconIsLasted(String timeUnix) {

		String infos = sp.getString(SUB_ICON_VERSION, null);
		if (TextUtils.isEmpty(infos)) {
			return false;
		}
		return checkExist(infos, timeUnix);
	}

	public static void setSubIconVersion(String timeUnix, String DirPath,
			int count) {
		Editor editor = sp.edit();
		editor.putString(SUB_ICON_VERSION,
				versionToJstring(timeUnix, DirPath, count));
		editor.commit();
	}

	/**
	 * 特效icon版本
	 * 
	 * @return
	 */
	public static boolean checkSpecialIconIsLasted(String timeUnix) {
		String infos = sp.getString(SPECIAL_ICON_VERSION, null);
		if (TextUtils.isEmpty(infos)) {
			return false;
		}
		return checkExist(infos, timeUnix);
	}

	public static void setSpecialIconVersion(String timeUnix, String DirPath,
			int count) {
		Editor editor = sp.edit();
		editor.putString(SPECIAL_ICON_VERSION,
				versionToJstring(timeUnix, DirPath, count));
		editor.commit();
	}

	/**
	 * 录制是否打开美颜
	 * 
	 * @param enable
	 */
	public static void enableBeauty(boolean enable) {
		Editor editor = sp.edit();
		editor.putBoolean(CAMERA_ENABLE_BEAUTY_KEY, enable);
		editor.commit();
	}

	/**
	 * 上一次录制是否打开美颜
	 * 
	 * @return
	 */
	public static boolean enableBeauty() {
		return sp.getBoolean(CAMERA_ENABLE_BEAUTY_KEY, true);

	}

	public static void setAspectRatio(float aspect) {
		ASPECTRATIO = aspect;
	}

	public static boolean isFirstShowSplit() {
		return sp.getBoolean(ISFIRSTSHOWSPLIT, true);
	}

	public static void setIsFirstShowSplit() {
		Editor editor = sp.edit();
		editor.putBoolean(ISFIRSTSHOWSPLIT, false);
		editor.commit();
	}

	public static boolean isFirstShowDrag_listview() {
		return sp.getBoolean(ISFIRSTSHOWDRAG_LISTVIEW, true);

	}

	public static void setIsFirstDrag_listview() {
		Editor editor = sp.edit();
		editor.putBoolean(ISFIRSTSHOWDRAG_LISTVIEW, false);
		editor.commit();
	}

	public static boolean isFirstShowDragSplit() {
		return sp.getBoolean(ISFIRSTSHOWDRAG_SPLIT, true);

	}

	public static void setIsFirstDragSplit() {
		Editor editor = sp.edit();
		editor.putBoolean(ISFIRSTSHOWDRAG_SPLIT, false);
		editor.commit();
	}

	public static boolean isFirstShowDel() {
		return sp.getBoolean(ISFIRSTSHOW_DEL, true);

	}

	public static void setIsFirstDel() {
		Editor editor = sp.edit();
		editor.putBoolean(ISFIRSTSHOW_DEL, false);
		editor.commit();
	}

	public static boolean isFirstShowSub() {
		return sp.getBoolean(ISFIRSTSHOW_SUB, true);

	}

	public static void setIsFirstSub() {
		Editor editor = sp.edit();
		editor.putBoolean(ISFIRSTSHOW_SUB, false);
		editor.commit();
	}

	public static boolean isFirstShowSp() {
		return sp.getBoolean(ISFIRSTSHOW_SP, true);

	}

	public static void setIsFirstSp() {
		Editor editor = sp.edit();
		editor.putBoolean(ISFIRSTSHOW_SP, false);
		editor.commit();
	}

	public static boolean isFirstShowCrop() {
		return sp.getBoolean(ISFIRSTSHOW_CROP, true);

	}

	public static void setIsFirstCrop() {
		Editor editor = sp.edit();
		editor.putBoolean(ISFIRSTSHOW_CROP, false);
		editor.commit();
	}

	public static boolean isFirstShowAudio() {
		return sp.getBoolean(ISFIRSTSHOW_AUDIO, true);

	}

	public static void setIsFirstAudio() {
		Editor editor = sp.edit();
		editor.putBoolean(ISFIRSTSHOW_AUDIO, false);
		editor.commit();
	}

	public static boolean isFirstShowInsertSub() {
		return sp.getBoolean(ISFIRSTSHOW_INSERT_SUB, true);

	}

	public static void setIsFirstInsertSub() {
		Editor editor = sp.edit();
		editor.putBoolean(ISFIRSTSHOW_INSERT_SUB, false);
		editor.commit();
	}

	public static boolean isFirstShowInsertSp() {
		return sp.getBoolean(ISFIRSTSHOW_INSERT_SP, true);

	}

	public static void setIsFirstInsertSp() {
		Editor editor = sp.edit();
		editor.putBoolean(ISFIRSTSHOW_INSERT_SP, false);
		editor.commit();
	}

	public static boolean isFirstShowDragSp() {
		return sp.getBoolean(ISFIRSTSHOW_DRAG_SP, true);

	}

	public static void setIsFirstDragSp() {
		Editor editor = sp.edit();
		editor.putBoolean(ISFIRSTSHOW_DRAG_SP, false);
		editor.commit();
	}

	public static boolean isFirstShowDragSub() {
		return sp.getBoolean(ISFIRSTSHOW_DRAG_SUB, true);

	}

	public static void setIsFirstDragSub() {
		Editor editor = sp.edit();
		editor.putBoolean(ISFIRSTSHOW_DRAG_SUB, false);
		editor.commit();
	}

	public static boolean isFirstShowTransition() {
		return sp.getBoolean(ISFIRSTSHOW_TRANSITION, true);

	}

	public static void setIsFirstTransition() {
		Editor editor = sp.edit();
		editor.putBoolean(ISFIRSTSHOW_TRANSITION, false);
		editor.commit();
	}

	public static boolean isFirstShowDialogSplit() {
		return sp.getBoolean(ISFIRSTSHOW_DIALOG_SPLIT, true);

	}

	public static void setIsFirstDialogSplit() {
		Editor editor = sp.edit();
		editor.putBoolean(ISFIRSTSHOW_DIALOG_SPLIT, false);
		editor.commit();
	}

	/**
	 * 获取是否显示摄像界面提示帮助
	 * 
	 * @return
	 */
	public static boolean isTrainingCaptureVideo() {
		return sp.getBoolean(TRAININGCAPTUREVIDEO, true);
	}

	/**
	 * 设置是否显示摄像界面提示帮助
	 * 
	 * @param bTrainingCaptureVideo
	 */
	public static void setTrainingCaptureVideo(boolean bTrainingCaptureVideo) {
		Editor editor = sp.edit();
		editor.putBoolean(TRAININGCAPTUREVIDEO, bTrainingCaptureVideo);
		editor.commit();
	}

	/**
	 * 获取是否硬件编解码正在检查中...
	 * 
	 * @return
	 */
	public static boolean isHWCoderChecking() {
		return sp.getBoolean(USE_HWCODER_CHECKING, false);
	}

	/**
	 * 设置硬件编解码检查状态
	 */
	public static void setHWCoderChecking(boolean checking) {
		Editor editor = sp.edit();
		editor.putBoolean(USE_HWCODER_CHECKING, checking);
		editor.commit();
	}

	/**
	 * 设置是否启用确件编解编
	 * 
	 * @param enabled
	 */
	public static void enableHWCodec(boolean enabled) {
		m_bHWCodecEnabled = enabled;
	}

	/**
	 * 获取是否启用确件编解编
	 * 
	 * @return
	 */
	public static boolean HWCoderEnabled() {
		return m_bHWCodecEnabled;
	}

	public static SharedPreferences getSharedPreferences() {
		return sp;
	}
}
