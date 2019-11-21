package com.rd.veuisdk.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import com.rd.lib.utils.CoreUtils;
import com.rd.vecore.VirtualVideo;
import com.rd.veuisdk.model.AppConfigInfo;

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


    public static float ASPECTRATIO = 1f;
    private static SharedPreferences mSharedPreferences;

    private static final String APP_CONFIG = "app_config2";

    public static void initContext(Context context) {
        mSharedPreferences = context.getSharedPreferences("xpk_data", Context.MODE_PRIVATE);
        String text = mSharedPreferences.getString(APP_CONFIG, "");
        mAppConfigInfo = ParcelableUtils.toParcelObj(text, AppConfigInfo.CREATOR);
        if (null == mAppConfigInfo) {
            mAppConfigInfo = new AppConfigInfo();
        }
    }

    public static AppConfigInfo getAppConfig() {
        return mAppConfigInfo;
    }

    /**
     * 保存设置配置
     */
    public static void saveAppConfig() {
        Editor editor = mSharedPreferences.edit();
        editor.putString(APP_CONFIG, ParcelableUtils.toParcelStr(mAppConfigInfo));
        editor.apply();
    }

    private static AppConfigInfo mAppConfigInfo;

    //防止部分按钮频繁点击
    public static final int REPEAT_DELAY = 800;

    /**
     * 导出成功后，是否需要删除草稿数据库
     *
     * @return
     */
    public static boolean isDeleteDraft() {
        return deleteDraft;
    }

    public static void setDeleteDraft(boolean deleteDraft) {
        AppConfiguration.deleteDraft = deleteDraft;
    }

    //草稿导出成功后，是否需要删除草稿数据
    private static boolean deleteDraft = false;

    private static final String ISFIRSTSHOW_AUDIO = "isfirstshow_audio";
    private static final String TRAININGCAPTUREVIDEO = "TrainingCaptureVideo";

    private static final String ISFIRSTSHOW_INSERT_SUB = "isfirstshow_insert_sub";
    private static final String ISFIRSTSHOW_INSERT_SP = "isfirstshow_insert_sp";
    private static final String ISFIRSTSHOW_DRAG_SP = "isfirstshow_drag_sp";
    private static final String ISFIRSTSHOW_DRAG_SUB = "isfirstshow_drag_sub";
    private static final String ISFIRSTSHOW_DIALOG_SPLIT = "isfirstshow_dialog_split";
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


    private static final String RECORDER_BITRATE = "recorder_bitrate";
    private static final String RECORDER_SIZE = "RECORDER_SIZE";

    /**
     * 字体icon版本 是最新且存在
     *
     * @param timeUnix
     * @return
     */
    public static boolean checkTTFVersionIsLasted(String timeUnix) {

        String infos = mSharedPreferences.getString(TTF_ICON_VERSION, null);
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
            String[] arr = {jobj.optString("timeunix", "0"),
                    jobj.optString("DirPath", ""), jobj.optString("count", "")};
            return arr;

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;

    }

    public static void setTTFVersion(String timeUnix, String DirPath, int count) {
        Editor editor = mSharedPreferences.edit();
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
        String infos = mSharedPreferences.getString(SUB_ICON_VERSION, null);
        if (TextUtils.isEmpty(infos)) {
            return false;
        }
        return checkExist(infos, timeUnix);
    }

    public static void setSubIconVersion(String timeUnix, String DirPath,
                                         int count) {
        Editor editor = mSharedPreferences.edit();
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
        String infos = mSharedPreferences.getString(SPECIAL_ICON_VERSION, null);
        if (TextUtils.isEmpty(infos)) {
            return false;
        }
        return checkExist(infos, timeUnix);
    }

    public static void setSpecialIconVersion(String timeUnix, String DirPath,
                                             int count) {
        Editor editor = mSharedPreferences.edit();
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
        Editor editor = mSharedPreferences.edit();
        editor.putBoolean(CAMERA_ENABLE_BEAUTY_KEY, enable);
        editor.commit();
    }

    /**
     * 上一次录制是否打开美颜
     *
     * @return
     */
    public static boolean enableBeauty() {
        return mSharedPreferences.getBoolean(CAMERA_ENABLE_BEAUTY_KEY, true);

    }

    /**
     * 支持全面屏
     * 防止有的16:9 的屏幕带有虚拟导航键占用位置
     *
     * @param context
     */
    public static void fixAspectRatio(Context context) {
        DisplayMetrics metrics = null;
        // 没有虚拟导航 || 全面屏
        if (!Utils.checkDeviceHasNavigationBar(context) || (null != (metrics = CoreUtils.getMetrics()) && ((float) metrics.widthPixels / metrics.heightPixels) < (9 / 16.0f))) {
            ASPECTRATIO = 1f;
        }
    }

    public static boolean isFirstShowAudio() {
        return mSharedPreferences.getBoolean(ISFIRSTSHOW_AUDIO, true);

    }

    public static void setIsFirstAudio() {
        Editor editor = mSharedPreferences.edit();
        editor.putBoolean(ISFIRSTSHOW_AUDIO, false);
        editor.commit();
    }

    public static boolean isFirstShowInsertSub() {
        return mSharedPreferences.getBoolean(ISFIRSTSHOW_INSERT_SUB, true);

    }

    public static void setIsFirstInsertSub() {
        Editor editor = mSharedPreferences.edit();
        editor.putBoolean(ISFIRSTSHOW_INSERT_SUB, false);
        editor.commit();
    }

    public static boolean isFirstShowInsertSp() {
        return mSharedPreferences.getBoolean(ISFIRSTSHOW_INSERT_SP, true);

    }

    public static void setIsFirstInsertSp() {
        Editor editor = mSharedPreferences.edit();
        editor.putBoolean(ISFIRSTSHOW_INSERT_SP, false);
        editor.commit();
    }

    public static boolean isFirstShowDragSp() {
        return mSharedPreferences.getBoolean(ISFIRSTSHOW_DRAG_SP, true);

    }

    public static void setIsFirstDragSp() {
        Editor editor = mSharedPreferences.edit();
        editor.putBoolean(ISFIRSTSHOW_DRAG_SP, false);
        editor.commit();
    }

    public static boolean isFirstShowDragSub() {
        return mSharedPreferences.getBoolean(ISFIRSTSHOW_DRAG_SUB, true);

    }

    public static void setIsFirstDragSub() {
        Editor editor = mSharedPreferences.edit();
        editor.putBoolean(ISFIRSTSHOW_DRAG_SUB, false);
        editor.commit();
    }

    public static boolean isFirstShowDialogSplit() {
        return mSharedPreferences.getBoolean(ISFIRSTSHOW_DIALOG_SPLIT, true);

    }

    public static void setIsFirstDialogSplit() {
        Editor editor = mSharedPreferences.edit();
        editor.putBoolean(ISFIRSTSHOW_DIALOG_SPLIT, false);
        editor.commit();
    }

    /**
     * 获取是否显示摄像界面提示帮助
     *
     * @return
     */
    public static boolean isTrainingCaptureVideo() {
        return mSharedPreferences.getBoolean(TRAININGCAPTUREVIDEO, true);
    }

    /**
     * 设置是否显示摄像界面提示帮助
     *
     * @param bTrainingCaptureVideo
     */
    public static void setTrainingCaptureVideo(boolean bTrainingCaptureVideo) {
        Editor editor = mSharedPreferences.edit();
        editor.putBoolean(TRAININGCAPTUREVIDEO, bTrainingCaptureVideo);
        editor.commit();
    }

    public static SharedPreferences getSharedPreferences() {
        return mSharedPreferences;
    }


    /**
     * 保存录制配置
     *
     * @param bitrate
     * @param sizeMode
     */
    public static void saveRecorderConfig(int bitrate, int sizeMode) {
        Editor editor = mSharedPreferences.edit();
        editor.putInt(RECORDER_BITRATE, bitrate);
        editor.putInt(RECORDER_SIZE, sizeMode);
        editor.commit();
    }

    /**
     * 录制输出码率
     *
     * @return
     */
    public static int getRecorderBitrate() {

        return Math.max(400, mSharedPreferences.getInt(RECORDER_BITRATE, 1800));
    }

    /**
     * 预览size
     *
     * @return
     */
    public static int getRecorderSizeMode() {
        return mSharedPreferences.getInt(RECORDER_SIZE, 2);
    }

    public static VirtualVideo.Size getRecorderSize(boolean isSquare) {
        int mode = getRecorderSizeMode();
        VirtualVideo.Size size = new VirtualVideo.Size(0, 0);
        if (mode == 0) {
            if (isSquare) {
                size.set(368, 368);
            } else {
                size.set(368, 640);
            }
        } else if (mode == 1) {
            if (isSquare) {
                size.set(480, 480);
            } else {
                size.set(480, 854);
            }
        } else if (mode == 3) {
            if (isSquare) {
                size.set(1088, 1088);
            } else {
                size.set(1080, 1920);
            }
        } else {
            if (isSquare) {
                size.set(720, 720);
            } else {
                size.set(720, 1280);
            }
        }
        return size;


    }
}
