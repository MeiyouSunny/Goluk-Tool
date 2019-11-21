package com.rd.veuisdk.net;

import android.graphics.BitmapFactory;
import android.text.TextUtils;

import com.rd.http.NameValuePair;
import com.rd.net.RdHttpClient;
import com.rd.vecore.utils.MiscUtils;
import com.rd.veuisdk.database.StickerData;
import com.rd.veuisdk.model.ApngInfo;
import com.rd.veuisdk.model.FrameInfo;
import com.rd.veuisdk.model.StyleInfo;
import com.rd.veuisdk.model.TimeArray;
import com.rd.veuisdk.utils.ApngExtractFrames;
import com.rd.veuisdk.utils.CommonStyleUtils;
import com.rd.veuisdk.utils.CommonStyleUtils.STYPE;
import com.rd.veuisdk.utils.FileUtils;
import com.rd.veuisdk.utils.PathUtils;
import com.rd.veuisdk.utils.WebpUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

/**
 * 贴纸相关辅助
 */
public class StickerUtils {

    public StyleInfo getStyleInfo(int key) {
        try {
            StyleInfo info = null;
            if (sArray.size() != 0)
                info = getIndex2(sArray, key);
            if (null == info) {
                if (downloaded.size() != 0) {
                    info = getIndex2(downloaded, key);
                }
            }
            if (null != info) {
                return info;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    private StyleInfo getIndex2(ArrayList<StyleInfo> list, int styleId) {
        StyleInfo temp = null, result = null;
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

    public ArrayList<StyleInfo> getStyleInfos() {
        return sArray;
    }

    public ArrayList<StyleInfo> getDBStyleInfos() {
        return downloaded;
    }

    public void clearArray() {
        sArray.clear();
    }

    private String TAG = "StickerUtils";

    public void putStyleInfo(StyleInfo info) {
        int i = 0;
        for (; i < sArray.size(); i++) {
            if (info.pid == sArray.get(i).pid) {
                sArray.set(i, info);
                break;
            }
        }
        if (i >= sArray.size()) {
            sArray.add(info);
        }
    }

    /**
     * @param info
     */
    public static void fixLocalIcon(StyleInfo info) {
        info.icon = PathUtils.getRdSpecialPath() + "/icon/" + info.code
                + ".png";
    }

    private static StickerUtils instance;

    public static StickerUtils getInstance() {
        if (null == instance) {
            instance = new StickerUtils();
        }
        return instance;
    }

    /**
     * Activity onDestory() 释放内存
     */
    public void recycle() {
        downloaded.clear();
        sArray.clear();
    }

    private StickerUtils() {

    }

    public String getSpecialJson() {
        try {
            return RdHttpClient.post(URLConstants.STYLEURL, new NameValuePair(
                    "os", Integer.toString(2)));
        } catch (Exception e) {
            e.printStackTrace();

        }
        return null;
    }

    private static ArrayList<StyleInfo> sArray = new ArrayList<StyleInfo>(),
            downloaded = new ArrayList<StyleInfo>();

    /**
     * 自定义网络特效接口的数据
     *
     * @param bCustomApi
     * @return
     */
    public ArrayList<StyleInfo> getStyleDownloaded(boolean bCustomApi) {
        getDownloadData(bCustomApi);
        return downloaded;

    }

    private void getDownloadData(boolean bCustomApi) {
        downloaded.clear();
        ArrayList<StyleInfo> dblist = StickerData.getInstance().getAll(bCustomApi);
        for (int i = 0; i < dblist.size(); i++) {
            StyleInfo tempInfo = dblist.get(i);
            if (!TextUtils.isEmpty(tempInfo.mlocalpath)) {
                File f = new File(tempInfo.mlocalpath);
                CommonStyleUtils.checkStyle(f, tempInfo);
            }
            tempInfo.st = STYPE.special;
            downloaded.add(tempInfo);
        }
        dblist.clear();

    }


    /**
     * 把apng 解析成png 序列，并构造帧列表
     *
     * @param baseName
     * @param baseDir  当前贴纸的文件目录
     */
    public static void initApng(String baseName, File baseDir, StyleInfo info) {
        File srcApng = new File(baseDir, baseName + ".png");
        if (!srcApng.exists()) {
            //兼容
            srcApng = new File(baseDir, baseName + ".apng");
        }
        info.frameArray.clear();
        if (ApngExtractFrames.process(srcApng) > 0) { //优先检查是否是apng文件
            //apng 转png序列
            ApngInfo apng = ApngInfo.createApng(srcApng, baseName);
            //构造图片序列
            int len = apng.getFrameList().size();
            int itemDuration = MiscUtils.s2ms(apng.getItemDuration());
            for (int i = 0; i < len; i++) {
                int time = itemDuration * i;
                FrameInfo frameInfo = new FrameInfo();
                frameInfo.time = time;
                frameInfo.pic = apng.getFrameList().get(i);
                info.frameArray.put(time, frameInfo);
            }
            info.du = itemDuration * len;
        } else {
            //解析apng失败 ( 当前普通图片png处理)
            File dst = new File(baseDir, baseName + "0.png");
            FileUtils.syncCopyFile(srcApng, dst, null);
            FrameInfo frameInfo = new FrameInfo();
            frameInfo.time = 0;
            frameInfo.pic = dst.getAbsolutePath();
            info.frameArray.put(0, frameInfo);
            info.du = 200;
        }
        if (info.timeArrays.isEmpty()) {
            //防止没有配置时间
            info.timeArrays.add(new TimeArray(0, info.du));
        }
    }

    /**
     * 读取size
     *
     * @param info
     */
    public static void readSize(StyleInfo info) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        if (info.frameArray.size() > 0) {
            String path = info.frameArray.valueAt(0).pic;
            if (FileUtils.isExist(path)) {
                BitmapFactory.decodeFile(path, options);
                info.w = options.outWidth;
                info.h = options.outHeight;
            }
        }
    }

    /**
     * 贴纸webp->png  ()
     *
     * @param styleInfo
     */
    public static void webp2png(StyleInfo styleInfo) {
        File file = new File(styleInfo.mlocalpath);
        File[] files = file.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if (name.endsWith("webp")) {
                    return true;
                }
                return false;
            }
        });
        if (null == files || files.length == 0) {
            return;
        }
        for (File item : files) {
            // 判断是否为文件夹
            String path = item.getAbsolutePath();
            String target = path.replace("webp", "png");
            if (!FileUtils.isExist(target)) {
                WebpUtils.locWebpSaveToLocPng(path, target);
            }
        }
    }


}
