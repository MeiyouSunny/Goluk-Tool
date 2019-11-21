package com.rd.veuisdk.mvp.model;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.rd.lib.utils.ThreadPoolUtils;
import com.rd.veuisdk.model.StyleInfo;

import java.util.ArrayList;

/**
 * 字幕、贴纸 公共部分
 *
 * @author JIAN
 * @create 2019/4/26
 * @Describe
 */
abstract class ISSModel extends BaseModel {

    Context mContext;
    boolean isPrepareing = false;//是否正在请求网络

    public ISSModel(@NonNull ICallBack callBack, Context context) {
        super(callBack);
        mContext = context;
    }


    /**
     * 是否正在请求网络
     *
     * @return true 请求网络中
     */
    public boolean isPrepareing() {
        return isPrepareing;
    }

    /**
     * 基于素材管理系统的数据
     *
     * @param url
     */
    abstract void getApiData(String url);

    /**
     * 兼容老版本的数据
     */
    @Deprecated
    abstract void getWebData();

    /**
     * 下载图标
     *
     * @param timeUnix
     * @param url
     * @param name
     */
    @Deprecated
    abstract void downloadIcon(String timeUnix, String url, String name);


    /**
     * @param url      素材管理系统的链接
     * @param callBack 请求回调
     */
    public void load(final String url, final ISSCallBack callBack) {
        mCallBack = callBack;
        isPrepareing = false;
        ThreadPoolUtils.execute(new Runnable() {
            @Override
            public void run() {
                isPrepareing = true;
                boolean isCustomApi = !TextUtils.isEmpty(url);
                if (isCustomApi) {
                    getApiData(url);
                } else {
                    getWebData();
                }
                isPrepareing = false;
            }
        });

    }


    /**
     * 本地数据库是否存在该项
     *
     * @param dbList
     * @param info
     * @return
     */
    StyleInfo checkExit(ArrayList<StyleInfo> dbList, StyleInfo info) {
        StyleInfo db = null;
        if (null != dbList) {
            int dblen = dbList.size();
            for (int j = 0; j < dblen; j++) {
                StyleInfo dbTemp = dbList.get(j);
                if (dbTemp.caption.equals(info.caption) && dbTemp.isbUseCustomApi() == info.isbUseCustomApi()) {
                    db = dbTemp;
                    break;
                }
            }
        }
        return db;
    }
}
