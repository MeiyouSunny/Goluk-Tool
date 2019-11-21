package com.rd.veuisdk.net;

import android.content.Context;
import android.text.TextUtils;

import com.rd.http.NameValuePair;
import com.rd.net.RdHttpClient;
import com.rd.veuisdk.database.SubData;
import com.rd.veuisdk.model.StyleInfo;
import com.rd.veuisdk.utils.CommonStyleUtils;
import com.rd.veuisdk.utils.FileUtils;
import com.rd.veuisdk.utils.PathUtils;
import com.rd.veuisdk.utils.WebpUtils;

import java.io.File;
import java.util.ArrayList;

public class SubUtils {

    public static final int DEFAULT_ID = "text_sample".hashCode();

    /**
     * @param key
     * @return
     */
    public StyleInfo getStyleInfo(int key) {
        try {
            if (sArray.size() != 0) {
                return getIndex2(sArray, key);
            }
            if (downloaded.size() != 0) {
                return getIndex2(downloaded, key);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    public String getSubJson() {
        try {
            return RdHttpClient.post(URLConstants.GETZIMU, new NameValuePair(
                    "os", Integer.toString(2)));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    private StyleInfo getIndex2(ArrayList<StyleInfo> list, int styleId) {
        StyleInfo temp = null, result = list.get(0);
        int len = list.size();
        for (int i = 0; i < len; i++) {
            temp = list.get(i);
            if (temp.pid == styleId) {
                result = temp;
                break;
            }

        }

        return result;
    }

    /**
     * @return
     */
    public ArrayList<StyleInfo> getStyleInfos() {

        return sArray;

    }

    /**
     * @return
     */
    public ArrayList<StyleInfo> getDBStyleInfos() {
        return downloaded;

    }

    public void putStyleInfo(StyleInfo info) {
        sArray.add(info);
//        Log.e("subUtils", "putStyleInfo: "+sArray.size() );
    }

    /**
     * 旧版字幕的icon存放路径
     */
    @Deprecated
    public static void fixLocalIcon(StyleInfo info) {
        info.icon = PathUtils.getRdSubPath() + "/icon/" + info.code + ".png";
    }

    private static SubUtils instance;

    public static SubUtils getInstance() {
        if (null == instance) {
            instance = new SubUtils();
        }
        return instance;
    }

    private SubUtils() {

    }

    private static ArrayList<StyleInfo> sArray = new ArrayList<StyleInfo>(),
            downloaded = new ArrayList<StyleInfo>();

    public void clearArray() {
        sArray.clear();
    }

    public ArrayList<StyleInfo> getDownLoadedList(Context context, boolean bCustomApi) {
        getStyleDownloaded(context, bCustomApi);
        return downloaded;
    }

    /**
     * @param context
     * @param bCustomApi
     */
    public void getStyleDownloaded(Context context, boolean bCustomApi) {
        downloaded.clear();
        ArrayList<StyleInfo> dblist = SubData.getInstance().getAll(bCustomApi);
        for (int i = 0; i < dblist.size(); i++) {
            StyleInfo tempInfo = dblist.get(i);
            if (!TextUtils.isEmpty(tempInfo.mlocalpath)) {
                File f = new File(tempInfo.mlocalpath);
                CommonStyleUtils.checkStyle(f, tempInfo);
            }
            downloaded.add(tempInfo);
        }
        dblist.clear();

    }

    /**
     * Activity onDestory() 释放内存
     */
    public void recycle() {
        downloaded.clear();
        sArray.clear();
    }

    public static void webp2png(StyleInfo styleInfo) {
        String srcWebp = new File(styleInfo.mlocalpath, styleInfo.code + "0.webp").getAbsolutePath();
        if (com.rd.lib.utils.FileUtils.isExist(srcWebp)) {
            //zipp文件中部分字幕时.webp资源，部分又是.png资源需要特别处理
            String dst = new File(styleInfo.mlocalpath, styleInfo.code + "0.png").getAbsolutePath();
            if (!FileUtils.isExist(dst)) {
                WebpUtils.locWebpSaveToLocPng(srcWebp, dst);
            }
        }
    }

}
