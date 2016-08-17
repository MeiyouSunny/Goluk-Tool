package com.goluk.crazy.panda.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.goluk.crazy.panda.common.application.CPApplication;

public class SharedPrefUtil {
    /**
     * 功能模块xml文件列表定义区域
     */
    private static final String PRE_FILE_IPC = "ipc_function_file";

    /**
     * 根据不同的功能模块，将数据存储在不同的文件时，可以使用该函数。
     * 否则请使用{@link #getDefaultPreference}将数据存储到公共保存文件
     *
     * @param fileName preference的xml文件名称
     * @return SharedPreferences 实例
     */
    private static SharedPreferences getSharedPreference(String fileName) {
        return CPApplication.getApp().getSharedPreferences(fileName, Context.MODE_PRIVATE);
    }

    /**
     * 所存储的信息不用区分模块，整个app公用
     *
     * @return SharedPreferences 实例
     */
    private static SharedPreferences getDefaultPreference() {
        return PreferenceManager.getDefaultSharedPreferences(CPApplication.getApp());
    }


    private static final String Property_Ipc_Launch_Mode = "ipc_launch_mode";

    /**
     * 使用实例Demo
     * 将信息保存至某个功能文件
     */
    public void setIPCLaunchMode(int mode) {
        SharedPreferences sharedPreferences = getSharedPreference(PRE_FILE_IPC);
        sharedPreferences.edit().putInt(Property_Ipc_Launch_Mode, mode).commit();
    }

    public int getIPCLaunchMode() {
        SharedPreferences sharedPreferences = getSharedPreference(PRE_FILE_IPC);
        return sharedPreferences.getInt(Property_Ipc_Launch_Mode, 0);
    }


    private static final String Property_User_First_Launch = "user_first_launch";

    /**
     * 使用实例Demo
     * 将信息保存至公共区域
     */
    public boolean setFirstLaunch(boolean value) {
        SharedPreferences sharedPreferences = getDefaultPreference();
        return sharedPreferences.edit().putBoolean(Property_User_First_Launch, value).commit();
    }

    public boolean isFirstLaunch() {
        SharedPreferences sharedPreferences = getDefaultPreference();
        return sharedPreferences.getBoolean(Property_User_First_Launch, true);
    }
}
