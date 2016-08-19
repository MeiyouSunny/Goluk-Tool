package com.goluk.crazy.panda.common.application;

import android.app.Application;

import com.goluk.crazy.panda.ipc.database.DatabaseHelper;

public class CPApplication extends Application {
    private static CPApplication instance;
    private DatabaseHelper helper;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        helper = DatabaseHelper.getHelper();
    }

    public static CPApplication getApp() {
        return instance;
    }
}
