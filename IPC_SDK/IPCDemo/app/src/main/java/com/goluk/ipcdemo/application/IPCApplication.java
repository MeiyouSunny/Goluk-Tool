package com.goluk.ipcdemo.application;

import android.app.Application;
import android.util.Log;

import com.goluk.ipcsdk.listener.IPCInitListener;
import com.goluk.ipcsdk.main.GolukIPCSdk;

/**
 * Created by leege100 on 16/5/31.
 */
public class IPCApplication extends Application{

    @Override
    public void onCreate() {
        super.onCreate();

        GolukIPCSdk.getInstance().initSDK(this, "10000005", new IPCInitListener() {
            @Override
            public void initCallback(boolean isSuccess, String msg) {
                Log.i("IPCApplication","isSuccess: " + isSuccess + "    msg: " + msg);
            }
        });
    }

}
