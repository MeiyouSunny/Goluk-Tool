package com.rd.veuisdk.mvp.model;

import android.content.Context;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.rd.lib.utils.CoreUtils;
import com.rd.net.JSONObjectEx;
import com.rd.veuisdk.database.StickerData;
import com.rd.veuisdk.model.StyleInfo;
import com.rd.veuisdk.model.bean.AppData;
import com.rd.veuisdk.model.bean.DataBean;
import com.rd.veuisdk.net.IconUtils;
import com.rd.veuisdk.net.StickerUtils;
import com.rd.veuisdk.utils.AppConfiguration;
import com.rd.veuisdk.utils.CommonStyleUtils;
import com.rd.veuisdk.utils.ModeDataUtils;
import com.rd.veuisdk.utils.PathUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

/**
 * 贴纸网络数据
 *
 * @author JIAN
 * @create 2019/4/26
 * @Describe
 */
public class StickerFragmentModel extends ISSModel {


    public StickerFragmentModel(Context context) {
        super(null, context);
    }


    /**
     * 获取网络贴纸
     */
    @Override
    void getApiData(String url) {
        String data = ModeDataUtils.getData(mContext, url, ModeDataUtils.TYPE_EFFECTS);
        if (!TextUtils.isEmpty(data)) {
            AppData aeData = JSON.parseObject(data, AppData.class);
            if (null != aeData && aeData.getData() != null) {
                int len = aeData.getData().size();
                DataBean dataBean;
                StickerUtils.getInstance().clearArray();
                ArrayList<StyleInfo> dbList = StickerData.getInstance().getAll(true);
                StyleInfo tmp;
                for (int i = 0; i < len; i++) {
                    dataBean = aeData.getData().get(i);
                    tmp = new StyleInfo(true, false);
                    tmp.code = dataBean.getName();
                    tmp.caption = dataBean.getFile();
                    tmp.icon = dataBean.getCover();
                    tmp.pid = tmp.code.hashCode();
                    tmp.nTime = dataBean.getUpdatetime();
                    tmp.st = CommonStyleUtils.STYPE.special;
                    tmp.index = i;
                    StyleInfo dbTmp = checkExit(dbList, tmp);
                    if (null != dbTmp) {
                        if (StickerData.getInstance().checkDelete(tmp, dbTmp)) {
                            tmp.isdownloaded = false;
                        } else {
                            tmp.isdownloaded = dbTmp.isdownloaded;
                            if (tmp.isdownloaded) {
                                tmp.mlocalpath = dbTmp.mlocalpath;
                                File idfile = new File(tmp.mlocalpath);
                                CommonStyleUtils.checkStyle(idfile, tmp);
                            }
                        }
                    }
                    StickerUtils.getInstance().putStyleInfo(tmp);
                }

                ArrayList<StyleInfo> newList = StickerUtils
                        .getInstance().getStyleInfos();
                StickerData.getInstance().replaceAll(newList);

                if (null != dbList) {
                    dbList.clear();
                }
                aeData.getData().clear();
                if (null != mCallBack) {
                    mHandler.obtainMessage(MSG_SUCCESS, newList).sendToTarget();
                }
            } else {
                onFailed();
            }
        } else {
            onFailed();
        }


    }

    @Override
    @Deprecated
    void getWebData() {
        String content = null;
        if (CoreUtils.checkNetworkInfo(mContext) != CoreUtils.UNCONNECTED) {
            content = StickerUtils.getInstance().getSpecialJson();
        }
        if (!TextUtils.isEmpty(content)) {
            JSONObjectEx jex;
            try {
                jex = new JSONObjectEx(content);
                if (null != jex && jex.getInt("code") == 200) {
                    ArrayList<StyleInfo> dbList = StickerData.getInstance().getAll(false);
                    StyleInfo tmp = null;
                    JSONObject jobj = null;
                    JSONArray jarr = jex.getJSONArray("data");
                    final JSONObject jicon = jex.getJSONObject("icon");
                    String timeunix = jicon.getString("timeunix");
                    if (!AppConfiguration.checkSpecialIconIsLasted(timeunix)) {
                        downloadIcon(timeunix, jicon.optString("caption"), jicon.optString("name"));
                    }
                    int len = jarr.length();
                    StickerUtils.getInstance().clearArray();
                    for (int i = 0; i < len; i++) {
                        jobj = jarr.getJSONObject(i);
                        tmp = new StyleInfo(false, false);
                        tmp.code = jobj.optString("name");
                        tmp.caption = jobj.optString("caption");
                        tmp.pid = tmp.code.hashCode();
                        tmp.nTime = jobj.getLong("timeunix");
                        tmp.st = CommonStyleUtils.STYPE.special;
                        tmp.index = i;
                        StyleInfo dbTemp = checkExit(dbList, tmp);
                        if (null != dbTemp) {
                            if (StickerData.getInstance().checkDelete(tmp,
                                    dbTemp)) {
                                tmp.isdownloaded = false;
                            } else {
                                tmp.isdownloaded = dbTemp.isdownloaded;
                                if (tmp.isdownloaded) {
                                    tmp.mlocalpath = dbTemp.mlocalpath;
                                    CommonStyleUtils.checkStyle(new File(tmp.mlocalpath), tmp);
                                }
                            }
                        }
                        StickerUtils.fixLocalIcon(tmp);
                        StickerUtils.getInstance().putStyleInfo(tmp);
                    }

                    ArrayList<StyleInfo> newList = StickerUtils.getInstance().getStyleInfos();
                    StickerData.getInstance().replaceAll(newList);
                    if (null != dbList) {
                        dbList.clear();
                    }
                    if (null != mCallBack) {
                        mHandler.obtainMessage(MSG_SUCCESS, newList).sendToTarget();
                    }
                } else {
                    onFailed();
                }


            } catch (JSONException e) {
                e.printStackTrace();
                onFailed();
            }
        }

    }

    @Override
    @Deprecated
    void downloadIcon(String timeUnix, String url, String name) {
        IconUtils.downIcon(3, mContext, name, url, timeUnix, PathUtils.getRdSpecialPath(), new IconUtils.IconListener() {
            @Override
            public void prepared() {
                if (mCallBack instanceof ISSCallBack && !isRecycled) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            ((ISSCallBack) mCallBack).onIconSuccess();
                        }
                    });

                }
            }
        });
    }
}
