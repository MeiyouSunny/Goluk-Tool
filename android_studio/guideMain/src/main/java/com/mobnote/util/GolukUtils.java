package com.mobnote.util;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.MainActivity;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.UpdateActivity;
import com.mobnote.golukmain.carrecorder.IPCControlManager;
import com.mobnote.golukmain.carrecorder.entity.VideoInfo;
import com.mobnote.golukmain.photoalbum.PhotoAlbumPlayer;
import com.mobnote.golukmain.promotion.PromotionSelectItem;
import com.mobnote.golukmain.userlogin.UserInfo;
import com.mobnote.golukmain.videosuqare.VideoSquareInfo;
import com.mobnote.t1sp.ui.album.PhotoAlbumPlayerF5;
import com.mobnote.user.IPCInfo;
import com.mobnote.user.UserUtils;
import com.mobnote.videoedit.AfterEffectActivity;
import com.mobnote.view.FlowLayout;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.com.tiros.debug.GolukDebugUtils;

public class GolukUtils {

    /**
     * 1 表示国际版 ，0 表示国内版 （主要与服务器交互）
     */
    public static final String GOLUK_APP_VERSION = "1";
    public static final int GOLUK_APP_VERSION_OVERSEA = 1;
    public static final int GOLUK_APP_VERSION_MAINLAND = 0;

    /**
     * Goluk绑定连接出现问题URL
     */
    public static final String URL_BIND_CONN_PROBLEM = "https://surl.goluk.cn/faq/link.html";

    public static float mDensity = 1.0f;

    public static final String T2_WIFINAME_SIGN = "Goluk_T2";
    public static final String T1S_WIFINAME_SIGN = "Goluk_T1S";
    public static final String T3_WIFINAME_SIGN = "Goluk_T3";
    public static final String T1_WIFINAME_SIGN = "Goluk_T1";
    public static final String G1G2_WIFINAME_SIGN = "Goluk";
    public static final String IPC_MODEL_T1 = "t1";
    public static final String IPC_MODEL_T1S = "t1s";
    public static final String IPC_MODEL_T3 = "t3";

    // 键盘的高度
    private static int keyBoardHeight = 250;
    private static boolean isSettingBoardHeight = false;

    public static int getKeyBoardHeight() {
        return keyBoardHeight;
    }

    public static void setKeyBoardHeight(int height) {
        if (height <= 0) {
            return;
        }
        keyBoardHeight = height;
        isSettingBoardHeight = true;
    }

    public static boolean isSettingBoardHeight() {
        return isSettingBoardHeight;
    }

    public static void getMobileInfo(Activity activity) {
        DisplayMetrics metric = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metric);
        int width = metric.widthPixels; // 屏幕宽度（像素）
        int height = metric.heightPixels; // 屏幕高度（像素）
        mDensity = metric.density; // 屏幕密度（0.75 / 1.0 / 1.5）
        int densityDpi = metric.densityDpi; // 屏幕密度DPI（120 / 160 / 240）

        keyBoardHeight = (int) (keyBoardHeight * mDensity);

        GolukDebugUtils.e("", " mobile info:" + mDensity);
    }

    public static String getDefaultZone() {
        String current = getLanguageAndCountry();
        if (current.equals("zh_CN")) {
            return "CN +86";
        } else {
            return "US +1";
        }
    }

    /**
     * 秒转换为 时：分：秒
     *
     * @param second
     * @return
     * @author jiayf
     * @date Apr 13, 2015
     */
    public static String secondToString(final int second) {
        String timeStr = "";
        if (second >= 60) {
            int hour = second / 3600; // 时
            int restMinS = second - hour * 3600;
            int min = restMinS / 60; // 分
            int sec = restMinS % 60; // 秒

            String hourStr = "";
            String minStr = "";
            String secStr = "";

            if (hour > 0) {
                if (hour < 10) {
                    hourStr = "0" + hour + ":";
                } else {
                    hourStr = "" + hour + ":";
                }

            }

            if (min >= 10) {
                minStr = min + ":";
            } else {
                minStr = "0" + min + ":";
            }
            if (sec >= 10) {
                secStr = sec + "";
            } else {
                secStr = "0" + sec;
            }

            timeStr = hourStr + minStr + secStr;

        } else {
            if (second >= 10) {
                timeStr = "00:" + second;
            } else {
                timeStr = "00:0" + second;
            }
        }

        return timeStr;
    }

    /**
     * 默认浏览器打开指定的url
     *
     * @param url
     * @param mContext
     */
    public static void openUrl(String url, Context mContext) {
        // 版本升级---打开浏览器
        try {
            Intent intent = new Intent();
            intent.setAction("android.intent.action.VIEW");
            Uri content_url = Uri.parse(url);
            intent.setData(content_url);
            mContext.startActivity(intent);
        } catch (ActivityNotFoundException anfe) {
            Toast.makeText(mContext,
                    mContext.getString(R.string.str_no_browser_found),
                    Toast.LENGTH_SHORT).show();
            anfe.printStackTrace();
        }
    }

    // 获取版本号
    public static String getVersion(Context context) {
        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0);
            return pi.versionName;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static int getVersionCode(Context context)// 获取版本号(内部识别号)
    {
        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0);
            return pi.versionCode;
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 显示短提示
     *
     * @param context
     * @param text    需要显示的文本信息
     * @author xuhw
     * @date 2015年5月29日
     */
    public static void showToast(Context context, String text) {
        if (mToast == null) {
            mToast = Toast.makeText(GolukApplication.getInstance(), text,
                    Toast.LENGTH_SHORT);
        } else {
            mToast.setText(text);
        }

        mToast.show();
    }

    /**
     * 显示短提示
     *
     * @param context
     * 上下文
     * @param text
     * 需要显示的文本信息
     * @param duration
     * 信息显示持续时间
     * @author xuhw
     * @date 2015年5月29日
     */
    private static Toast mToast = null;

    public static void showToast(Context context, String text, int duration) {
        if (mToast == null) {
            mToast = Toast.makeText(GolukApplication.getInstance(), text,
                    duration);
        } else {
            mToast.setText(text);
            mToast.setDuration(duration);
        }

        mToast.show();
    }

    /**
     * 写文件
     *
     * @param filename 文件绝对路径
     * @param msg      写入文件的信息
     * @param append   ture:追加方式写入文件 flase:覆盖的方式写入文件
     * @author xuhw
     * @date 2015年5月29日
     */
    public static void writeFile(String filename, String msg, boolean append) {
        try {
            FileOutputStream fos = new FileOutputStream(filename, append);
            fos.write(msg.getBytes());
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void freeBitmap(Bitmap bitmap) {
        if (null != bitmap && !bitmap.isRecycled()) {
            bitmap.recycle();
            bitmap = null;
        }
    }

    public static void freeBitmap(Drawable drawable) {
        if (null == drawable) {
            return;
        }
        BitmapDrawable bd = (BitmapDrawable) drawable;
        Bitmap bitmap = bd.getBitmap();
        if (null != bitmap && !bitmap.isRecycled()) {
            bitmap.recycle();
            bitmap = null;
        }
    }

    /**
     * 检查sd卡剩余容量是否可用
     *
     * @param filesize 文件大小 MB
     * @return
     * @author xuhw
     * @date 2015年6月10日
     */
    public static boolean checkSDStorageCapacity(double filesize) {
        float availableSize = 0;
        File path = Environment.getExternalStorageDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        availableSize = (float) (blockSize * availableBlocks / 1024) / 1024;
        if ((availableSize - 10) >= filesize) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 打开手机系统WIFI列表
     *
     * @param context
     * @author jyf
     * @date 2015年7月3日
     */
    public static void showSystemWifiList(Context context) {
        Intent intent = new Intent();
        intent.setAction("android.net.wifi.PICK_WIFI_NETWORK");
        context.startActivity(intent);
    }

    public static int getSystemSDK() {
        try {
            return Integer.parseInt(android.os.Build.VERSION.SDK);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    /**
     * 获取系统版本号
     */
    public static String getSystem_version() {
        return android.os.Build.VERSION.RELEASE;
    }

    /**
     * 获取手机型号
     */
    public static String getPhone_models() {
        return android.os.Build.MODEL;
    }

    @SuppressLint("NewApi")
    public static Bitmap createVideoThumbnail(String filePath) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(filePath);
            bitmap = retriever.getFrameAtTime(1000000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
        } catch (IllegalArgumentException ex) {
        } catch (RuntimeException ex) {
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException ex) {
            }
        }
        return bitmap;
    }

    public static String getCurrentTime() {
        StringBuffer buffer = new StringBuffer();
        Time t = new Time();
        t.setToNow();
        buffer.append(t.hour);
        buffer.append(":");
        int minute = t.minute;
        String aa = "" + minute;
        if (minute < 10) {
            aa = "0" + minute;
        }
        buffer.append(aa);
        return new String(buffer);
    }

    @SuppressLint("SimpleDateFormat")
    public static String formatTime(String date) {
        String time = "";
        if (null != date) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");

            try {
                Date strtodate = formatter.parse(date);
                if (null != strtodate) {
                    // formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    if (null != formatter) {
                        time = formatter.format(strtodate);
                    }
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return time;
    }

    /**
     * 弹出软键盘
     *
     * @param edit
     * @author jyf
     * @date 2015年8月7日
     */
    public static void showSoft(final EditText edit) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                InputMethodManager inputManager = (InputMethodManager) edit
                        .getContext().getSystemService(
                                Context.INPUT_METHOD_SERVICE);
                inputManager.showSoftInput(edit, 0);
            }
        }, 500);
    }

    public static final void showSoftNotThread(final View view) {
        InputMethodManager inputManager = (InputMethodManager) view
                .getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.showSoftInput(view, 0);
    }

    // 强制隐藏键盘
    @SuppressLint("NewApi")
    public static void hideSoft(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static String getCurrentFormatTime(Context context) {
        String time = DateFormat.format(
                context.getString(R.string.str_date_formatter),
                Calendar.getInstance().getTime()).toString();
        return time;
    }

    public static String getCurrentCommentTime() {
        Calendar calar = Calendar.getInstance();
        int year = calar.get(Calendar.YEAR);
        int month = calar.get(Calendar.MONTH) + 1;
        int day = calar.get(Calendar.DAY_OF_MONTH);
        int h = calar.get(Calendar.HOUR_OF_DAY);
        int m = calar.get(Calendar.MINUTE);
        int s = calar.get(Calendar.SECOND);
        int hm = calar.get(Calendar.MILLISECOND);

        StringBuffer sb = new StringBuffer();
        sb.append(year);
        if (month >= 10) {
            sb.append(month);
        } else {
            sb.append("0" + month);
        }

        if (day >= 10) {
            sb.append(day);
        } else {
            sb.append("0" + day);
        }
        if (h >= 10) {
            sb.append(h);
        } else {
            sb.append("0" + h);
        }

        if (m >= 10) {
            sb.append(m);
        } else {
            sb.append("0" + m);
        }
        if (s >= 10) {
            sb.append(s);
        } else {
            sb.append("0" + s);
        }

        if (hm >= 100) {
            sb.append(hm);
        } else if (hm >= 10 && hm < 100) {
            sb.append("0" + hm);
        } else {
            sb.append("00" + hm);
        }

        return sb.toString();
    }

    public static String formatTimeYMDHMS(String date) {
        String time = "";
        if (null != date) {
            SimpleDateFormat formatter = new SimpleDateFormat(
                    "yyyyMMddHHmmssSSS", Locale.CHINESE);

            try {
                Date strtodate = formatter.parse(date);
                if (null != strtodate) {
                    formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                            Locale.CHINESE);
                    if (null != formatter) {
                        time = formatter.format(strtodate);
                    }
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return time;
    }

    public static String getCommentShowFormatTime(Context context, long time) {
        return getCommentShowFormatTime(context, parseMillesToTimeStr(time));
    }

    /**
     * 获取评论列表显示时间规则()
     *
     * @param time 类似2010-11-20 11:10:10
     * @return
     * @author jyf
     * @date 2015年8月7日
     */
    public static String getCommentShowFormatTime(Context context, String time) {
        if(TextUtils.isEmpty(time))
            return "";

        try {
            String result = formatTimeYMDHMS(time);
            SimpleDateFormat formatter = new SimpleDateFormat(
                    "yyyy-MM-dd HH:mm", Locale.CHINESE);
            Date oldDate = formatter.parse(result);
            // 转换成 2010-11-20 11:10
            String ymdhm = formatter.format(oldDate);

            result = ymdhm;

            // 视频相关时间
            Calendar c1 = Calendar.getInstance();
            c1.setTime(oldDate);
            int oldYear = c1.get(Calendar.YEAR);
            int oldMonth = c1.get(Calendar.MONTH) + 1;
            int oldDay = c1.get(Calendar.DAY_OF_MONTH);

            // 当前时间
            Calendar currentCalendar = Calendar.getInstance();
            currentCalendar.setTime(new Date());
            int currentYear = currentCalendar.get(Calendar.YEAR);
            int currentMonth = currentCalendar.get(Calendar.MONTH) + 1;
            int currentDay = currentCalendar.get(Calendar.DAY_OF_MONTH);

            if (currentYear == oldYear && oldMonth == currentMonth
                    && oldDay == currentDay) {
                // 今天
                SimpleDateFormat hhmmFormat = new SimpleDateFormat("HH:mm",
                        Locale.CHINESE);
                String todayFormatStr = hhmmFormat.format(oldDate);
                result = context.getString(R.string.str_today) + " "
                        + todayFormatStr;
            } else if (currentYear == oldYear && oldMonth == currentMonth
                    && oldDay + 1 == currentDay) {
                // 昨天
                SimpleDateFormat hhmmFormat = new SimpleDateFormat("HH:mm",
                        Locale.CHINESE);
                String todayFormatStr = hhmmFormat.format(oldDate);
                result = context.getString(R.string.str_yestoday) + " "
                        + todayFormatStr;
            } else if (currentYear == oldYear) {
                // 本年
                SimpleDateFormat hhmmFormat = new SimpleDateFormat(
                        "MM-dd HH:mm", Locale.CHINESE);
                String todayFormatStr = hhmmFormat.format(oldDate);
                result = todayFormatStr;
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return time;
    }

    public static String getFormatNumber(String fmtnumber) {
        String number;
        try {
            int wg = Integer.parseInt(fmtnumber);

            if (wg < 100000) {
                DecimalFormat df = new DecimalFormat("#,###");
                number = df.format(wg);
            } else {
                number = "100,000+";
            }
        } catch (Exception e) {
            return fmtnumber;
        }

        return number;
    }

    public static String getFormatedNumber(String fmtnumber) {
        String number;
        try {
            int wg = Integer.parseInt(fmtnumber);

            if (wg >= 10000) {
                DecimalFormat df = new DecimalFormat("0.0");
                number = df.format((float) wg / 1000) + "K";
            } else {
                number = "" + fmtnumber;
            }
        } catch (Exception e) {
            return fmtnumber;
        }

        return number;
    }

    public static String getFormatNumber(int fmtnumber) {
        String number;

        if (fmtnumber >= 10000) {
            DecimalFormat df = new DecimalFormat("0.0");
            number = df.format((float) fmtnumber / 1000) + "K";
        } else {
            number = "" + fmtnumber;
        }
        return number;
    }

    public static String getNewCategoryShowTime(Context context, long timeMilles) {
        Date date = new Date(timeMilles);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        return getNewCategoryShowTime(context, formatter.format(date));
    }

    public static String getNewCategoryShowTime(Context context, String date) {
        return "";
    }

    public static boolean isCanClick = true;
    private static Timer mTimer = null;

    public static void startTimer(int time) {
        isCanClick = false;
        cancelTimer();
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                isCanClick = true;
            }
        }, time);
    }

    public static void cancelTimer() {
        if (null != mTimer) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    /**
     * 把drawable中的资源图片转换成Uri格式
     *
     * @param resId
     * @return
     * @author jyf
     */
    public static Integer getResourceUri(int resId) {
        return Integer.valueOf(resId);
    }

    @SuppressLint("SimpleDateFormat")
    public static String getTime(String date) {
        String time = null;
        try {
            long curTime = System.currentTimeMillis();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            Date strtodate = formatter.parse(date);

            Date curDate = new Date(curTime);
            int curYear = curDate.getYear();
            int history = strtodate.getYear();

            if (curYear == history) {
                SimpleDateFormat jn = new SimpleDateFormat("-MM.dd-");
                return jn.format(strtodate);// 今年内：月日更新
            } else {
                SimpleDateFormat jn = new SimpleDateFormat("-yyyy.MM.dd-");
                return jn.format(strtodate);// 非今年：年月日更新
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return time;
    }

    public static String getAssestFileContent(String fileName) {
        if (null == fileName || "".equals(fileName)) {
            return "";
        }
        String result = "";
        InputStream is = null;
        try {
            is = GolukApplication.getInstance().getAssets().open(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            result = new String(buffer, "GB2312");
        } catch (Exception e) {

        } finally {
            if (null != is) {
                try {
                    is.close();
                    is = null;
                } catch (IOException e) {
                }

            }
        }

        return result;

    }

    public static String compute32(byte[] content) {
        StringBuffer buf = new StringBuffer("");
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            try {
                md.update(content);
            } catch (Exception e) {
                e.printStackTrace();
            }
            byte b[] = md.digest();
            int i;
            for (int offset = 0; offset < b.length; offset++) {
                i = b[offset];
                if (i < 0)
                    i += 256;
                if (i < 16)
                    buf.append("0");
                buf.append(Integer.toHexString(i));
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return buf.toString();
    }

    public static String getFileMD5(File file) {
        if (!file.isFile()) {
            return null;
        }
        MessageDigest digest = null;
        FileInputStream in = null;
        byte buffer[] = new byte[1024];
        int len;
        try {
            digest = MessageDigest.getInstance("MD5");
            in = new FileInputStream(file);
            while ((len = in.read(buffer, 0, 1024)) != -1) {
                digest.update(buffer, 0, len);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        BigInteger bigInt = new BigInteger(1, digest.digest());
        return bigInt.toString(16);
    }

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
        if (mNetworkInfo != null) {
            return mNetworkInfo.isAvailable();
        }
        return false;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static boolean isActivityAlive(Activity activity) {
        if (activity == null) {
            return false;
        }

        if (Build.VERSION.SDK_INT > 16) {
            if (activity.isDestroyed() || activity.isFinishing()) {
                return false;
            }
        } else {
            if (activity.isFinishing()) {
                return false;
            }
        }
        return true;
    }

    /**
     * 打开系统wifi列表
     *
     * @param context
     * @author jyf
     */
    public static void startSystemWifiList(Context context) {
        if (null == context) {
            return;
        }
        try {
            Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
            // intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            // intent.setAction("android.net.wifi.PICK_WIFI_NETWORK");
            context.startActivity(intent);
        } catch (Exception e) {

        }

    }

    public static boolean isTestServer() {
        String serverSign = GolukUtils.getAssestFileContent("serverflag");
        GolukDebugUtils.e("aaa", "serverSign: " + serverSign);
        if (null != serverSign
                && (serverSign.trim().equals("test") || serverSign.trim()
                .equals("dev"))) {
            return true;
        } else {
            return false;
        }
    }

    private static long lastClickTime = 0;
    public static final int MIN_CLICK_DELAY_TIME = 500;

    public static boolean isFastDoubleClick() {
        long time = System.currentTimeMillis();
        if (Math.abs(time - lastClickTime) < MIN_CLICK_DELAY_TIME) {
            return true;
        }
        lastClickTime = time;
        return false;
    }

    /**
     * 通过wifi的原始 ssid ，获取设备类型 (G系列，T系列)
     *
     * @param mWillConnName
     * @return
     * @author jyf
     */
    public static String getIpcTypeFromName(String mWillConnName) {
        if (null == mWillConnName) {
            return "";
        }
        String ipcType = "";
        if (mWillConnName.startsWith(T2_WIFINAME_SIGN)) {
            ipcType = IPCControlManager.MODEL_T;
        } else if (mWillConnName.startsWith(T1S_WIFINAME_SIGN)) {
            ipcType = IPCControlManager.MODEL_G;
        } else if (mWillConnName.startsWith(T1_WIFINAME_SIGN)) {
            ipcType = IPCControlManager.MODEL_T;
        } else if (mWillConnName.startsWith(G1G2_WIFINAME_SIGN)) {
            ipcType = IPCControlManager.MODEL_G;
        } else if (mWillConnName.startsWith(T3_WIFINAME_SIGN)) {
            ipcType = IPCControlManager.MODEL_G;
        } else {

        }
        GolukDebugUtils.e("", "WifiBindList----getIpcType: " + ipcType);
        return ipcType;
    }

    /**
     * 获取国家语言编码
     *
     * @return
     */
    public static String getLanguage() {
        return Locale.getDefault().getLanguage();
    }

    /**
     * 获取国家地区编码
     *
     * @return
     */
    private static String getCountry() {
        return Locale.getDefault().getCountry();
    }

    /**
     * 判断时国际版还是国内版
     * <p/>
     * 国内０ 国际１ 默认为国际
     *
     * @return
     */
    public static String getCommversionX() {
        String commversion = "1";
        if (!"zh".equals(getLanguage())) {
            commversion = "1";
        } else {
            commversion = "0";
        }
        return commversion;
    }

    /**
     * 判断时国际版还是国内版
     * <p/>
     * 国内０ 国际１ 默认为国际
     *
     * @return
     */
    public static String getCommversion() {
        String commversion = "0";
        if (GolukApplication.getInstance().isMainland()) {
            commversion = "0";
        } else {
            commversion = "1";
        }
        return commversion;
    }

    /**
     * 获取语言与国家
     *
     * @return
     */
    public static String getLanguageAndCountry() {

        final String realZone = getLanguage() + "_" + getCountry();

        String[] allZone = GolukApplication.getInstance()
                .getApplicationContext().getResources()
                .getStringArray(R.array.zone_array);
        if (null == allZone || allZone.length <= 0) {
            return realZone;
        }
        final int length = allZone.length;
        for (int i = 0; i < length; i++) {
            if (realZone.equals(allZone[i])) {
                return allZone[i];
            }
        }
        return realZone;
    }

    public static void startPhotoAlbumPlayerActivity(Context context, int type, String vidFrom, String path, String filename, String createTime,
                                                     String videoHP, String size, PromotionSelectItem promotionItem) {
        Intent intent = new Intent(context, PhotoAlbumPlayer.class);
        intent.putExtra(PhotoAlbumPlayer.TYPE, type);
        intent.putExtra(PhotoAlbumPlayer.VIDEO_FROM, vidFrom);
        intent.putExtra(PhotoAlbumPlayer.PATH, path);
        intent.putExtra(PhotoAlbumPlayer.FILENAME, filename);
        intent.putExtra(PhotoAlbumPlayer.DATE, createTime);
        intent.putExtra(PhotoAlbumPlayer.HP, videoHP);
        intent.putExtra(PhotoAlbumPlayer.SIZE, size);
        intent.putExtra(PhotoAlbumPlayer.ACTIVITY_INFO, promotionItem);
        context.startActivity(intent);
    }

    public static void startPhotoAlbumPlayerActivityT2S(Context context, int type, String vidFrom, String path, String relativePath, String filename, String createTime,
                                                     String videoHP, String size, PromotionSelectItem promotionItem) {
        Intent intent = new Intent(context, PhotoAlbumPlayer.class);
        intent.putExtra(PhotoAlbumPlayer.TYPE, type);
        intent.putExtra(PhotoAlbumPlayer.VIDEO_FROM, vidFrom);
        intent.putExtra(PhotoAlbumPlayer.PATH, path);
        intent.putExtra(PhotoAlbumPlayer.RELATIVE_PATH, relativePath);
        intent.putExtra(PhotoAlbumPlayer.FILENAME, filename);
        intent.putExtra(PhotoAlbumPlayer.DATE, createTime);
        intent.putExtra(PhotoAlbumPlayer.HP, videoHP);
        intent.putExtra(PhotoAlbumPlayer.SIZE, size);
//        intent.putExtra(PhotoAlbumPlayer.ACTIVITY_INFO, promotionItem);
        context.startActivity(intent);
    }

    public static void startPhotoAlbumPlayerActivityT2S(Context context, int type, String vidFrom, String path, String relativePath, String filename, String createTime,
                                                        String videoHP, String size, PromotionSelectItem promotionItem, boolean isFromPreviewPage,
                                                        VideoInfo videoInfo) {
        Intent intent = new Intent(context, PhotoAlbumPlayer.class);
        intent.putExtra(PhotoAlbumPlayer.TYPE, type);
        intent.putExtra(PhotoAlbumPlayer.VIDEO_FROM, vidFrom);
        intent.putExtra(PhotoAlbumPlayer.PATH, path);
        intent.putExtra(PhotoAlbumPlayer.RELATIVE_PATH, relativePath);
        intent.putExtra(PhotoAlbumPlayer.FILENAME, filename);
        intent.putExtra(PhotoAlbumPlayer.DATE, createTime);
        intent.putExtra(PhotoAlbumPlayer.HP, videoHP);
        intent.putExtra(PhotoAlbumPlayer.SIZE, size);
        intent.putExtra(PhotoAlbumPlayer.ACTIVITY_INFO, promotionItem);
        intent.putExtra(PhotoAlbumPlayer.KEY_IS_FROM_T2S_PREVIEW_PAGE, isFromPreviewPage);
        intent.putExtra("videoInfoT2S", videoInfo);
        context.startActivity(intent);
    }

    public static void startPhotoAlbumPlayerF5Activity(Context context, int type, String vidFrom, String path, String filename, String createTime,
                                                     String videoHP, String size, PromotionSelectItem promotionItem) {
        Intent intent = new Intent(context, PhotoAlbumPlayerF5.class);
        intent.putExtra(PhotoAlbumPlayer.TYPE, type);
        intent.putExtra(PhotoAlbumPlayer.VIDEO_FROM, vidFrom);
        intent.putExtra(PhotoAlbumPlayer.PATH, path);
        intent.putExtra(PhotoAlbumPlayer.FILENAME, filename);
        intent.putExtra(PhotoAlbumPlayer.DATE, createTime);
        intent.putExtra(PhotoAlbumPlayer.HP, videoHP);
        intent.putExtra(PhotoAlbumPlayer.SIZE, size);
        intent.putExtra(PhotoAlbumPlayer.ACTIVITY_INFO, promotionItem);
        context.startActivity(intent);
    }

    public static void startUserCenterActivity(Context context, String userId) {
    }

    public static void startVideoShareActivity(Context context, int type, String path, String filename,
                                               boolean shouldDelete, int duration, String quality, PromotionSelectItem promotionSelectItem) {
    }

    public static void startAEActivity(Context context, int type, String path, PromotionSelectItem promotionSelectItem) {
        //视频后处理页面访问统计
        ZhugeUtils.eventVideoEdit(context);
        Intent intent = new Intent(context, AfterEffectActivity.class);
        intent.putExtra("vidType", type);
        intent.putExtra("vidPath", path);
        intent.putExtra(PhotoAlbumPlayer.ACTIVITY_INFO, promotionSelectItem);
        context.startActivity(intent);
    }

    public static void changePraiseStatus(List<VideoSquareInfo> dataList,
                                          boolean status, String videoId) {
        if (TextUtils.isEmpty(videoId) || null == dataList
                || dataList.size() == 0) {
            return;
        }

        for (int i = 0; i < dataList.size(); i++) {
            VideoSquareInfo vs = dataList.get(i);
            if (videoId.equals(vs.mVideoEntity.videoid)) {
                int number = Integer.parseInt(vs.mVideoEntity.praisenumber);
                if (status) {
                    number++;
                } else {
                    number--;
                }

                vs.mVideoEntity.praisenumber = "" + number;
                vs.mVideoEntity.ispraise = status ? "1" : "0";
                // mNewestAdapter.notifyDataSetChanged();
                break;
            }
        }
    }

    /**
     * 读取assets下文件
     *
     * @param context
     * @param fileName
     * @return
     */
    public static String getDataFromAssets(Context context, String fileName) {
        InputStreamReader inputReader = null;
        try {
            inputReader = new InputStreamReader(context.getAssets().open(
                    fileName));
            BufferedReader bufReader = new BufferedReader(inputReader);
            String line = "";
            String result = "";
            while ((line = bufReader.readLine()) != null) {
                result += line;
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (inputReader != null) {
                try {
                    inputReader.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

        }
        return null;
    }

    /**
     * 容量大小转字符串
     *
     * @param size 容量大小
     * @return
     * @author xuhw
     * @date 2015年4月11日
     */
    public static String getSize(double size) {
        String result = "";
        double totalsize = 0;

        java.text.DecimalFormat df = new java.text.DecimalFormat("#.##");
        if (size >= 1024) {
            totalsize = size / 1024;
            result = df.format(totalsize) + "GB";
        } else {
            totalsize = size;
            result = df.format(totalsize) + "MB";
        }

        return result;
    }

    public static void setTabHostVisibility(boolean visible, Activity activity) {
        if (!isActivityAlive(activity)) {
            return;
        }
        if (activity instanceof MainActivity) {
            MainActivity main = (MainActivity) activity;
            main.setTabHostVisibility(visible);
        }
    }

    public static void startH5(Context context, WebView webview, String url) {
        //test Url http://surl3.goluk.cn/activity/src/html/ad_beta.html
        //WebView加载web资源
        webview.getSettings().setJavaScriptEnabled(true);
        webview.setWebChromeClient(new WebChromeClient());
        webview.loadUrl(url);
        webview.addJavascriptInterface(new JavaScriptInterface(context), "mobile");
    }

    /**
     * 返回当前程序版本名称
     */
    public static String getAppVersionName(Context context) {
        String versionName = "";
        try {
            // Get the package info
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(GolukApplication.getInstance().getPackageName(), 0);
            versionName = pi.versionName;
            if (TextUtils.isEmpty(versionName)) {
                return "";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
        return versionName;
    }

    /**
     * Check wifi connect or not
     */
    public static boolean checkWifiStatus(Context context) {
        boolean isWifiConnect = true;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        //check the networkInfos numbers
        NetworkInfo[] networkInfos = cm.getAllNetworkInfo();
        for (int i = 0; i < networkInfos.length; i++) {
            if (networkInfos[i].getState() == NetworkInfo.State.CONNECTED) {
                if (networkInfos[i].getType() == cm.TYPE_MOBILE) {
                    isWifiConnect = false;
                }
                if (networkInfos[i].getType() == cm.TYPE_WIFI) {
                    isWifiConnect = true;
                }
            }
        }
        return isWifiConnect;
    }

    public static boolean isTokenValid(int code) {
        if (code == GolukConfig.SERVER_TOKEN_DEVICE_INVALID ||
                code == GolukConfig.SERVER_TOKEN_INVALID ||
                code == GolukConfig.SERVER_TOKEN_EXPIRED) {
            return false;
        }
        return true;
    }

    public static boolean isTokenValid(String result) {
        if (!TextUtils.isEmpty(result) &&
                (String.valueOf(GolukConfig.SERVER_TOKEN_DEVICE_INVALID).equals(result) ||
                        String.valueOf(GolukConfig.SERVER_TOKEN_INVALID).equals(result) ||
                        String.valueOf(GolukConfig.SERVER_TOKEN_EXPIRED).equals(result))) {
            return false;
        }
        return true;
    }

    /**
     * @return -1: error happen, 0: for mainland, 1: for oversea, others TBD
     */
    public static int judgeIPCDistrict(String model, String ipcVersion) {
        if (null == model || model.length() == 0) {
            return -1;
        }
        if (null == ipcVersion || ipcVersion.length() == 0) {
            return -1;
        }

        model = model.toLowerCase();
        if (model.startsWith("g") || model.equals("t1s")) {
            return 1;
        }

        String lowerCase = ipcVersion.toLowerCase();
        Pattern pattern = Pattern.compile("t[0-9]*u.*");
        Matcher matcher = pattern.matcher(lowerCase);
        if (matcher.matches()) {
            return 1;
        }
        return 0;
    }

    public static boolean isIPCTypeT1(final String model) {
        if (TextUtils.isEmpty(model)) {
            return false;
        }
        if (model.equalsIgnoreCase(IPC_MODEL_T1)) {
            return true;
        }
        return false;
    }

    public static boolean isIPCTypeT1S(final String model) {
        if (TextUtils.isEmpty(model)) {
            return false;
        }
        if (model.equalsIgnoreCase(IPC_MODEL_T1S)) {
            return true;
        }
        return false;
    }

    public static boolean isIPCTypeT3(final String model) {
        if (TextUtils.isEmpty(model)) {
            return false;
        }
        if (model.equalsIgnoreCase(IPC_MODEL_T3)) {
            return true;
        }
        return false;
    }

    public static boolean isIPCTypeG(final String model) {
        if (TextUtils.isEmpty(model)) {
            return false;
        }
        if (model.startsWith("g") || model.startsWith("G")) {
            return true;
        }
        return false;
    }

    public static String getExceptionStackString(Exception ex) {
        StringWriter errors = new StringWriter();
        ex.printStackTrace(new PrintWriter(errors));
        return errors.toString();
    }

    public static void startUpdateActivity(Context context, int sign, IPCInfo ipcInfo, boolean downloadOnCreate) {
        Intent intent = new Intent(context, UpdateActivity.class);
        intent.putExtra(UpdateActivity.UPDATE_SIGN, sign);
        intent.putExtra(UpdateActivity.UPDATE_DATA, ipcInfo);
        intent.putExtra(UpdateActivity.DOWNLOAD_ON_CREATE, downloadOnCreate);
        context.startActivity(intent);
    }

    public static String toUtf8(String str) {
        String result = null;
        try {
            result = URLEncoder.encode(str, "UTF-8").replaceAll("\\+", "%20");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static void startTagActivity(Context context, String topicId, String topicName, int topicType) {
        if (TextUtils.isEmpty(topicId)) {
            return;
        }
    }


    public static boolean isCurrWifiGolukT(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo != null && !TextUtils.isEmpty(wifiInfo.getSSID())) {
            String currSSID = wifiInfo.getSSID().toLowerCase();
            if (!TextUtils.isEmpty(currSSID) && currSSID.contains("\"")) {
                currSSID = currSSID.replaceAll("\"", "");
            }
            if (currSSID.startsWith("goluk_t")) {
                return true;
            }
        }
        return false;
    }

    /**
     * 时间戳转换为 yyyyMMddHHmmssSSS
     */
    public static String parseMillesToTimeStr(long timeMilles) {
        if(timeMilles <= 0)
            return "";

        Date date = new Date(timeMilles);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        return formatter.format(date);
    }

}
