package com.rd.veuisdk.net;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;

import com.rd.downfile.utils.DownLoadUtils;
import com.rd.downfile.utils.IDownFileListener;
import com.rd.http.MD5;
import com.rd.http.NameValuePair;
import com.rd.lib.utils.CoreUtils;
import com.rd.net.RdHttpClient;
import com.rd.veuisdk.database.TTFData;
import com.rd.veuisdk.model.TtfInfo;
import com.rd.veuisdk.utils.AppConfiguration;
import com.rd.veuisdk.utils.FileUtils;
import com.rd.veuisdk.utils.PathUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * font
 *
 * @author JIAN
 */
public class TTFUtils {

    private TTFUtils() {
    }

    private static HashMap<String, Typeface> tfs = new HashMap<String, Typeface>();

    public static Typeface gettfs(String path) {

        if (!tfs.containsKey(path)) {
            if (!TextUtils.isEmpty(path) && new File(path).exists()) {
                tfs.put(path, Typeface.createFromFile(path));
                return tfs.get(path);
            } else {
                return null;
            }
        } else {
            return tfs.get(path);
        }
    }

    public static void recycle() {
        tfs.clear();
    }

    private static TtfInfo checkExit(ArrayList<TtfInfo> dbList, TtfInfo temp) {
        int dblen = dbList.size();
        TtfInfo db = null;
        for (int j = 0; j < dblen; j++) {
            TtfInfo dbTemp = dbList.get(j);
            if (dbTemp.url.equals(temp.url)) {
                db = dbTemp;
                break;
            }
        }
        return db;

    }

    /**
     * 获取网络字体
     *
     * @return
     */
    public static ArrayList<TtfInfo> getTTF() {

        String content = getTtf();
        if (TextUtils.isEmpty(content)) {
            return null;
        }
        JSONObject json = null;
        try {
            json = new JSONObject(content);
        } catch (JSONException e1) {
            e1.printStackTrace();
        }

        if (null != json) {
            try {
                if (json.getInt("code") != 200) {
                    return null;
                }
                if (TTFData.getInstance().getDataBaseRoot() == null) {
                    return null;
                }
                ArrayList<TtfInfo> dbList = TTFData.getInstance().getAll();
                JSONArray jarr = json.getJSONArray("data");
                final JSONObject jicon = json.getJSONObject("icon");
                final String timeIconUnix = jicon.getString("timeunix");

                if (!AppConfiguration.checkTTFVersionIsLasted(timeIconUnix)) {
                    DownLoadUtils utils = new DownLoadUtils(jicon.optString(
                            "name").hashCode(), jicon.optString("caption"),
                            ".zipp");
                    utils.DownFile(new IDownFileListener() {

                        @Override
                        public void onProgress(long arg0, int arg1) {
                        }

                        @Override
                        public void Canceled(long arg0) {
                        }

                        @Override
                        public void Finished(long mid, String localPath) {
                            File fold = new File(localPath);
                            File zip = new File(fold.getParent() + "/"
                                    + jicon.optString("name") + ".zipp");
                            fold.renameTo(zip);
                            if (zip.exists()) { // 解压
                                try {
                                    FileUtils.deleteAll(new File(PathUtils
                                            .getRdTtfPath(), "icon"));
                                    String dirpath = FileUtils.unzip(
                                            zip.getAbsolutePath(),
                                            PathUtils.getRdTtfPath());

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
                                        AppConfiguration.setTTFVersion(
                                                timeIconUnix, dirpath,
                                                (null != icons) ? icons.length
                                                        : 0);
                                        zip.delete(); // 删除原zip
                                    }

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });

                }
                int len = jarr.length();
                ArrayList<TtfInfo> list = new ArrayList<TtfInfo>();
                TtfInfo info;
                JSONObject jitem;
                for (int i = 0; i < len; i++) {
                    info = new TtfInfo();
                    jitem = jarr.getJSONObject(i);

                    info.code = jitem.getString("name");
                    info.url = jitem.getString("caption");
                    info.id = info.code.hashCode();
                    info.index = i;
                    info.timeunix = jitem.getLong("timeunix");
                    TtfInfo dbTemp = checkExit(dbList, info);
                    info.local_path = null;
                    if (null != dbTemp) {
                        if (TTFData.getInstance().checkDelete(info, dbTemp)) {
                            info.local_path = null;
                        } else {
                            if (FileUtils.isExist(dbTemp.local_path)) {
                                info.local_path = dbTemp.local_path;
                            }
                        }
                    } else {
                        String spath = PathUtils.getTTFNameForSdcard(MD5
                                .getMD5(info.url));
                        if (FileUtils.isExist(spath)) {
                            info.local_path = spath;

                        }
                    }

                    list.add(info);
                }
                return list;

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        return null;

    }

    private static String getTtf() {
        return RdHttpClient.post(URLConstants.GETFONT, new NameValuePair("os",
                Integer.toString(2)));
    }

    /**
     * 下载默认的特效字体
     *
     * @param context
     */
    public static void getDefalut(final Context context) {

        int re = CoreUtils.checkNetworkInfo(context);

        if (re != CoreUtils.UNCONNECTED) {
            final String tfurl = "http://d.56show.com/upload/xiupaike_sdk/font/SentyTEA.ttf";
            String spath = PathUtils.getTTFNameForSdcard(MD5.getMD5(tfurl));
            if (!FileUtils.isExist(spath)) {

                new Thread(new Runnable() {

                    @Override
                    public void run() {

                        String content = getTtf();
                        if (!TextUtils.isEmpty(content)) {

                            JSONObject json = null;
                            try {
                                json = new JSONObject(content);
                            } catch (JSONException e1) {
                                e1.printStackTrace();
                            }

                            if (null != json) {
                                try {
                                    if (json.getInt("code") == 0) {

                                        JSONArray jarr = json
                                                .getJSONArray("data");
                                        int len = jarr.length();

                                        JSONObject jitem;

                                        if (len > 0) {
                                            final TtfInfo info = new TtfInfo();
                                            jitem = jarr.getJSONObject(0);
                                            info.code = jitem.getString("code");
                                            info.url = jitem.getString("font");
                                            info.id = info.code.hashCode();
                                            info.timeunix = jitem
                                                    .getLong("timeunix");
                                            DownLoadUtils utils = new DownLoadUtils(
                                                    info.id, info.url,
                                                    FileUtils.TTF_EXTENSION);
                                            utils.DownFile(new IDownFileListener() {

                                                @Override
                                                public void onProgress(
                                                        long mid, int progress) {

                                                }

                                                @Override
                                                public void Finished(long mid,
                                                                     String localPath) {
                                                    TTFData.getInstance()
                                                            .initilize(context);
                                                    info.local_path = localPath;
                                                    // 更新单个
                                                    TTFData.getInstance()
                                                            .replace(info);

                                                }

                                                @Override
                                                public void Canceled(long mid) {

                                                }
                                            });
                                        }
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                    }
                }).start();

            }
        }

    }

}
