package com.rd.veuisdk.net;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.rd.downfile.utils.DownLoadUtils;
import com.rd.downfile.utils.IDownFileListener;
import com.rd.veuisdk.utils.AppConfiguration;
import com.rd.veuisdk.utils.FileUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

/**
 * 下载图标(兼容图标在一个zipp文件夹中)
 *
 * @author JIAN
 * @create 2019/4/23
 * @Describe
 */
public class IconUtils {

    /**
     * 下载字体图标
     */
    public static interface IconListener {
        void prepared();
    }

    private static final String TAG = "IconUtils";

    /**
     * 下载图标 （兼容旧版 字体图标 、字幕图标、贴纸图标）
     *
     * @param type          1 字体icon、2 字幕icon 、 3 贴纸icon
     * @param timeIconUnix  当前版本号
     * @param parentDirPath 文件目标存放路径
     */
    @Deprecated
    public static void downIcon(final int type, Context context, String name, String url, final String timeIconUnix, final String parentDirPath, final IconListener iconListener) {
        {
            String tmpPath = new File(parentDirPath, name + ".zipp").getAbsolutePath();
            DownLoadUtils utils = new DownLoadUtils(context, name.hashCode(), url, tmpPath);
            utils.DownFile(new IDownFileListener() {

                @Override
                public void onProgress(long arg0, int arg1) {
                }

                @Override
                public void Canceled(long arg0) {
                    Log.e(TAG, "Canceled: " + arg0);
                }

                @Override
                public void Finished(long mid, String localPath) {
                    File zip = new File(localPath);
                    if (zip.exists()) { // 解压
                        try {
                            FileUtils.deleteAll(new File(parentDirPath, "icon"));
                            String dirpath = FileUtils.unzip(zip.getAbsolutePath(), parentDirPath);
                            if (null != iconListener) {
                                iconListener.prepared();
                            }
                            if (!TextUtils.isEmpty(dirpath)) {

                                String[] icons = new File(dirpath)
                                        .list(new FilenameFilter() {

                                            @Override
                                            public boolean accept(
                                                    File dir,
                                                    String filename) {
                                                return filename
                                                        .endsWith(".png");
                                            }
                                        });


                                zip.delete(); // 删除原zip
                                if (type == 1) {
                                    //字体icon
                                    AppConfiguration.setTTFVersion(
                                            timeIconUnix, dirpath,
                                            (null != icons) ? icons.length
                                                    : 0);
                                } else if (type == 2) {
                                    //字幕icon
                                    AppConfiguration
                                            .setSubIconVersion(timeIconUnix, dirpath,
                                                    (null != icons) ? icons.length : 0);
                                } else if (type == 3) {
                                    //贴纸icon
                                    AppConfiguration
                                            .setSpecialIconVersion(timeIconUnix, dirpath,
                                                    (null != icons) ? icons.length : 0);

                                }

                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

        }
    }

}
