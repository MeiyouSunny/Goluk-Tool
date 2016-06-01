package com.goluk.ipcsdk.bean;

import android.content.Context;

import com.goluk.ipcsdk.main.GolukIPCSdk;

import cn.com.mobnote.module.ipcmanager.IPCManagerFn;

/**
 * Created by leege100 on 16/6/1.
 */
public abstract class BaseIPCCommand implements IPCManagerFn{
    Context mContext;
    protected BaseIPCCommand(Context cxt){
        this.mContext = cxt;
        GolukIPCSdk.getInstance().registerCommand(this);
    }

    public Context getContext() {
        return mContext;
    }

    public void setContext(Context context) {
        this.mContext = context;
    }
}
