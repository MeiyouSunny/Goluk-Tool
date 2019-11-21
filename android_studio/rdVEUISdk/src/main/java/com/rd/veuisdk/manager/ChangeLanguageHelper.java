package com.rd.veuisdk.manager;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;
import android.util.DisplayMetrics;

import com.rd.lib.utils.CoreUtils;

import java.util.Locale;

/**
 * 切换语言辅助
 *
 * @create 2018/11/26
 * @Describe
 */
public class ChangeLanguageHelper {
    public static final int LANGUAGE_SYSTEM = 0;
    public static final int LANGUAGE_CHINESE = 1;
    public static final int LANGUAGE_ENGLISH = 2;


    private static final String APP_LANGUAGE = "custom_language_pref_key";
    private static final String SP_NAME = "rdLanguageConfig";
    private static SharedPreferences mSharedPreferences;
    private static LocaleList sLocaleList = null;

    //https://www.jianshu.com/p/9a304c2047ff  解决跟随系统异常
    static {
        //由于API仅支持7.0，需要判断，否则程序会crash
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            sLocaleList = LocaleList.getDefault();
        }
    }


    public static void init(Context context) {
        mSharedPreferences = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        mLanguage = -1;
    }


    public static int getAppLanguage(Context context) {
        if (null == mSharedPreferences) {
            init(context);
        }
        return mSharedPreferences.getInt(APP_LANGUAGE, LANGUAGE_SYSTEM);
    }

    private static int mLanguage = -1;

    /**
     * 确保每次打开apk重新加载语言环境
     *
     * @return
     */
    public static int getCurrentLanguage() {
        return mLanguage;
    }


    /**
     * 更改语言环境
     *
     * @param context
     * @param newLanguage
     */
    @TargetApi(Build.VERSION_CODES.N)
    public static void changeAppLanguage(Context context, int newLanguage) {
        mLanguage = newLanguage;
        mSharedPreferences.edit().putInt(APP_LANGUAGE, newLanguage).commit();
        if (!CoreUtils.hasN()) {
//            <7.0
            Resources resources = context.getResources();
            Configuration configuration = resources.getConfiguration();
            Locale locale = getLocaleByLanguage(newLanguage);
            configuration.setLocale(locale);
            DisplayMetrics dm = resources.getDisplayMetrics();
            resources.updateConfiguration(configuration, dm);
        }
    }


    /**
     * @param language
     * @return
     */
    @TargetApi(Build.VERSION_CODES.N)
    private static Locale getLocaleByLanguage(int language) {
        Locale locale = null;
        if (language == LANGUAGE_CHINESE) {
            locale = Locale.CHINESE;
        } else if (language == LANGUAGE_ENGLISH) {
            locale = Locale.ENGLISH;
        } else {
            if (CoreUtils.hasN()) {
                locale = (null != sLocaleList && sLocaleList.size() >= 1) ? sLocaleList.get(0) : Locale.getDefault();
            } else {
                locale = Locale.getDefault();
            }
        }
        return locale;
    }

    /**
     * @param context
     * @param language
     * @return
     */
    public static Context attachBaseContext(Context context, int language) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return updateResources(context, language);
        } else {
            return context;
        }
    }

    static final String TAG = "language";

    @TargetApi(Build.VERSION_CODES.N)
    private static Context updateResources(Context context, int language) {
        Resources resources = context.getResources();
        Locale locale = getLocaleByLanguage(language);
        Configuration configuration = resources.getConfiguration();
        configuration.setLocale(locale);
        configuration.setLocales(new LocaleList(locale));
        return context.createConfigurationContext(configuration);
    }

}
