package com.rd.veuisdk.utils;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.rd.lib.utils.CoreUtils;
import com.rd.veuisdk.ui.ExtAdvancedProgressDialog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;

public class Utils {

    /**
     * 视频缩略图缓冲目录
     */
    public static final String VIDEO_THUMBNAIL_CACHE_DIR = "video_thumbnails";

    /**
     * 初始Utils
     */
    public static void initialize(Context context, String strRootDirPath) {
        PathUtils.initialize(context, TextUtils.isEmpty(strRootDirPath) ? null
                : new File(strRootDirPath));
        CoreUtils.init(context);
        FileLog.setLogPath(PathUtils.getRdLogPath());
        ThumbNailUtils.getInstance(context);
    }

    /**
     * 获取某个activity根View
     *
     * @param activity 某个activity
     * @return 根View
     */
    public static View getRootView(Activity activity) {
        return ((ViewGroup) activity.findViewById(android.R.id.content))
                .getChildAt(0);

    }

    private static Handler m_sMainHandler = new Handler(Looper.getMainLooper());

    /**
     * 主线程延迟指定时间执行的消息
     */
    public static void postMainHandlerMsg(Runnable runnable, int delayMillis) {
        m_sMainHandler.postDelayed(runnable, delayMillis);
    }

    /**
     * 主线程执行的消息
     */
    public static void postMainHandlerMsg(Runnable runnable) {
        m_sMainHandler.post(runnable);
    }

    public static void autoToastNomal(Context c, String msg) {
        SysAlertDialog.showAutoHideDialog(c, null, msg, Toast.LENGTH_SHORT);
    }

    public static void autoToastNomal(Context c, int msgId) {
        autoToastNomal(c, c.getString(msgId));

    }

    /**
     * 高级进度条
     */
    public static ExtAdvancedProgressDialog showAdvancedProgressDialog(
            Context context, String message, boolean indeterminate,
            boolean cancelable, OnCancelListener cancelListener) {

        ExtAdvancedProgressDialog advancedDialog = new ExtAdvancedProgressDialog(
                context);
        advancedDialog.setMessage(message);
        advancedDialog.setIndeterminate(indeterminate);
        advancedDialog.setCancelable(cancelable);
        advancedDialog.setOnCancelListener(cancelListener);
        advancedDialog.show();
        return advancedDialog;
    }

    /**
     * 字幕的id
     *
     * @return id
     */
    public static int getWordId() {
        String time = String.valueOf(System.currentTimeMillis());
        int len = time.length();
        return Integer.parseInt(time.substring(len - 6, len));
    }

    /**
     * 查找是否受支持的项目
     */
    public static boolean isSupported(String value, List<String> supported) {
        return supported != null && supported.indexOf(value) >= 0;
    }

    public static int getDisplayRotation(Activity activity) {
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        switch (rotation) {
            case Surface.ROTATION_0:
                return 0;
            case Surface.ROTATION_90:
                return 90;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_270:
                return 270;
        }
        return 0;
    }

    public static final int ORIENTATION_HYSTERESIS = 5;

    /**
     * 根据需要设置的方向角度和当前(旧的)方向角度,计算出合适的新的角度
     *
     * @param orientation        方向角度
     * @param orientationHistory 方向角度历史
     * @return 新的角度
     */
    public static int roundOrientation(int orientation, int orientationHistory) {
        boolean changeOrientation = false;
        if (orientationHistory == OrientationEventListener.ORIENTATION_UNKNOWN) {
            changeOrientation = true;
        } else {
            int dist = Math.abs(orientation - orientationHistory);
            dist = Math.min(dist, 360 - dist);
            changeOrientation = (dist >= 45 + ORIENTATION_HYSTERESIS);
        }
        if (changeOrientation) {
            return ((orientation + 45) / 90 * 90) % 360;
        } else {
            return orientationHistory;
        }
    }

    /**
     * 清理临时文件
     *
     * @param strTmpFilePath 临时文件目录
     */
    public static void cleanTempFile(String strTmpFilePath) {
        if (!TextUtils.isEmpty(strTmpFilePath)) {
            File fTmp = new File(strTmpFilePath);
            if (fTmp.exists()) {
                fTmp.delete();
            }
        }
    }

    private static final StringBuilder m_sbFormator = new StringBuilder();
    private static final Formatter m_formatter = new Formatter(m_sbFormator,
            Locale.getDefault());

    /**
     * 毫秒数转换为时间格式化字符串
     */
    public static String stringForTime(long timeMs) {
        return stringForTime(timeMs, false, false);
    }

    /**
     * 毫秒数转换为时间格式化字符串 existsHours支持是否显示小时,existsMs支持是否显示毫秒
     */
    public static String stringForTime(long timeMs, boolean existsHours,
                                       boolean existsMs) {
        int totalSeconds = (int) (timeMs / 1000);
        int ms = (int) (timeMs % 1000);
        ms = ms / 100;
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        m_sbFormator.setLength(0);
        if (hours > 0 || existsHours) {
            if (existsMs) {
                return m_formatter.format("%02d:%02d:%02d.%1d", hours, minutes,
                        seconds, ms).toString();
            } else {
                return m_formatter.format("%02d:%02d:%02d", hours, minutes,
                        seconds).toString();
            }

        } else {
            if (existsMs) {
                return m_formatter
                        .format("%02d:%02d.%1d", minutes, seconds, ms)
                        .toString();
            } else {
                return m_formatter.format("%02d:%02d", minutes, seconds)
                        .toString();
            }

        }
    }

    private static boolean m_sSupportExpandEffects = false,
            m_bUseInternalRecorder;

    /**
     * 使用App内置录制
     */
    public static boolean isUseInternalRecorder() {
        return m_bUseInternalRecorder;
    }

    public static boolean m_bNoMP4Metadata = false;

    /**
     * 设置是否允许设置MP4元数据
     */
    public static void setCanWriteMP4Metadata(boolean bSetValue) {
        m_bNoMP4Metadata = bSetValue;
    }

    /**
     * 获取是否允许设置MP4元数据
     */
    public static boolean isCanWriteMp4Metadata() {
        return m_bNoMP4Metadata;
    }

    /**
     * 获取是否支持扩展特效滤镜
     */
    public static boolean getSupportExpandEffects() {
        return m_sSupportExpandEffects;
    }

    /**
     * 调转到应用权限设置
     */

    public static void gotoAppInfo(Context context, String packagename) {
        try {
            Uri packageURI = Uri.parse("package:" + packagename);
            Intent intent = new Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 将asset文件保存为指定文件
     */
    public static boolean assetRes2File(AssetManager am, String strAssetFile,
                                        String strDstFile) {
        OutputStream os = null;
        InputStream is = null;
        try {
            if (null == am || TextUtils.isEmpty(strDstFile)
                    || TextUtils.isEmpty(strAssetFile)) {
                return false;
            }
            is = am.open(strAssetFile);
            File fileDst = new File(strDstFile);
            if (fileDst.exists() && is.available() == fileDst.length()) {
                return true;
            }
            os = new FileOutputStream(strDstFile);
            byte[] pBuffer = new byte[1024];
            int nReadLen;
            while ((nReadLen = is.read(pBuffer)) != -1) {
                os.write(pBuffer, 0, nReadLen);
            }
            os.flush();
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
            try {
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
            }
        }
    }

    /**
     * 获取Asset文件长度
     */
    public static long getAssetResourceLen(AssetManager am, String strAssetFile)
            throws IOException {
        if (null == am) {
            return -1;
        }
        InputStream is = am.open(strAssetFile);
        long lLen = is.available();
        is.close();
        return lLen;
    }

    /**
     * 判断是否存在虚拟导航键
     */
    public static boolean checkDeviceHasNavigationBar(Context context) {
        boolean hasNavigationBar = false;
        Resources rs = context.getResources();
        int id = rs.getIdentifier("config_showNavigationBar", "bool", "android");
        if (id > 0) {
            hasNavigationBar = rs.getBoolean(id);
        }
        try {
            Class<?> systemPropertiesClass = Class.forName("android.os.SystemProperties");
            Method m = systemPropertiesClass.getMethod("get", (Class<?>) String.class);
            String navBarOverride = (String) m.invoke(systemPropertiesClass, "qemu.hw.mainkeys");
            if ("1".equals(navBarOverride)) {
                hasNavigationBar = false;
            } else if ("0".equals(navBarOverride)) {
                hasNavigationBar = true;
            }
        } catch (Exception e) {
        }
        return hasNavigationBar;
    }

    /**
     * 时长单位的转换 秒->毫秒
     */
    public static int s2ms(float s) {
        return (int) (s * 1000);
    }

    /**
     * 时长单位的转换 毫秒->秒
     */
    public static float ms2s(int ms) {
        return (ms / 1000.0f);
    }

    /**
     * 时长单位的转换 毫秒->秒
     *
     * @param ms 时长单位的转换 毫秒->秒
     */
    public static float ms2s(long ms) {
        return (ms / 1000.0f);
    }
}
