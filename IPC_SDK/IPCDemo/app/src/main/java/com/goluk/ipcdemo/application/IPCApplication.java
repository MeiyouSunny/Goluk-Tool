package com.goluk.ipcdemo.application;

import android.app.Application;

import com.goluk.ipcsdk.main.GolukIPCSdk;

/**
 * Created by leege100 on 16/5/31.
 */
public class IPCApplication extends Application{

    private static IPCApplication instance ;

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;
        GolukIPCSdk.getInstance().initSDK(this);
    }

    public static IPCApplication getInstance(){
        return instance;
    }
}
