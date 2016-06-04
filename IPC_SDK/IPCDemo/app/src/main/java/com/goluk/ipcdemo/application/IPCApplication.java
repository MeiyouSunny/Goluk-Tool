package com.goluk.ipcdemo.application;

import android.app.Application;

import com.goluk.ipcsdk.main.GolukIPCSdk;

/**
 * Created by leege100 on 16/5/31.
 */
public class IPCApplication extends Application{

    @Override
    public void onCreate() {
        super.onCreate();

        GolukIPCSdk.getInstance().initSDK(this,"");
    }

}
