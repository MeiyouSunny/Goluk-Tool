package com.rd.veuisdk.mvp.model;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;

import java.util.List;

/**
 * 每个数据提供者，至少支持成功和失败
 */
public abstract class BaseModel {
    boolean isRecycled = false;
    final int MSG_SUCCESS = 200;
    ICallBack mCallBack;
    Handler mHandler = null;

    public BaseModel(@NonNull ICallBack callBack) {
        mCallBack = callBack;
        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case MSG_SUCCESS:
                        onSuccess((List) msg.obj);
                        break;
                    default:
                        break;
                }
            }
        };
    }

    /**
     * 获取数据成功
     */
    @MainThread
    void onSuccess(List data) {
        if (null != mCallBack && !isRecycled) {
            mCallBack.onSuccess(data);
        }
    }

    /**
     * 获取数据失败
     */
    void onFailed() {
        if (null != mCallBack && !isRecycled) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mCallBack.onFailed();
                }
            });
        }
    }

    /**
     * 销毁时主动调用
     */
    public void recycle() {
        isRecycled = true;
    }
}
