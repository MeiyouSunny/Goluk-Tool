package com.goluk.crazy.panda.common;

import android.app.Application;

public class CPApplication extends Application {
    private static CPApplication instance;

    private CPApplication() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public static CPApplication getApp() {
        return instance;
    }
}
