package com.goluk.ipcdemo.application;

import android.app.Application;

import cn.com.tiros.api.Const;

/**
 * Created by leege100 on 16/5/31.
 */
public class IPCApplication extends Application{

    private static IPCApplication instance ;

    @Override
    public void onCreate() {
        super.onCreate();

        Const.setAppContext(this);
        instance = this;
    }

    public static IPCApplication getInstance(){
        return instance;
    }
}
