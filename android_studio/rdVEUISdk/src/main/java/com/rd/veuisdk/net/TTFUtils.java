package com.rd.veuisdk.net;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;

import com.rd.http.MD5;
import com.rd.http.NameValuePair;
import com.rd.net.RdHttpClient;
import com.rd.veuisdk.database.TTFData;
import com.rd.veuisdk.model.TtfInfo;
import com.rd.veuisdk.utils.AppConfiguration;
import com.rd.veuisdk.utils.FileUtils;
import com.rd.veuisdk.utils.ModeDataUtils;
import com.rd.veuisdk.utils.PathUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
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
     */
    public static ArrayList<TtfInfo> getTTFNew(String url) {

        String content = ModeDataUtils.getModeData(url, ModeDataUtils.TYPE_FONT);
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
                if (json.optInt("code", -1) != 0) {
                    return null;
                }
                if (TTFData.getInstance().getDataBaseRoot() == null) {
                    return null;
                }
                ArrayList<TtfInfo> dbList = TTFData.getInstance().getAll(true);

                JSONArray jarr = json.getJSONArray("data");
                int len = jarr.length();
                ArrayList<TtfInfo> list = new ArrayList<>();
                TtfInfo info;
                JSONObject jitem;
                for (int i = 0; i < len; i++) {
                    info = new TtfInfo();
                    jitem = jarr.getJSONObject(i);

                    info.code = jitem.getString("name");
                    info.url = jitem.getString("file");
                    info.icon = jitem.getString("cover");
                    info.id = info.code.hashCode();
                    info.index = i;
                    info.timeunix = jitem.getLong("updatetime");
                    TtfInfo dbTemp = checkExit(dbList, info);
                    info.local_path = null;
                    info.bCustomApi = true;
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

    /***
     * 用高清封面
     * @param context
     * @return
     */
    public static boolean isHDIcon(Context context) {
        return context.getResources().getDisplayMetrics().density > 2.01;
    }

    @Deprecated
    public static ArrayList<TtfInfo> getTTF(Context context, IconUtils.IconListener iconListener) {

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
                ArrayList<TtfInfo> dbList = TTFData.getInstance().getAll(false);
                JSONArray jarr = json.getJSONArray("data");
                int len = jarr.length();
                ArrayList<TtfInfo> list = new ArrayList<TtfInfo>();
                TtfInfo info;
                JSONObject jitem;
                boolean isHDIcon =isHDIcon(context);
                for (int i = 0; i < len; i++) {
                    info = new TtfInfo();
                    jitem = jarr.getJSONObject(i);

                    info.code = jitem.getString("name");
                    info.url = jitem.getString("caption");
                    info.id = info.code.hashCode();
                    info.index = i;
                    info.timeunix = jitem.getLong("timeunix");

                    if (isHDIcon) {
                        info.icon = PathUtils.getRdTtfPath() + "/icon/icon_2_" + info.code + "_n_@3x.png";
                    } else {
                        info.icon = PathUtils.getRdTtfPath() + "/icon/icon_2_" + info.code + "_n_@2x.png";
                    }

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
                JSONObject jicon = json.getJSONObject("icon");
                String timeIconUnix = jicon.getString("timeunix");

                if (!AppConfiguration.checkTTFVersionIsLasted(timeIconUnix)) {
                    String name = jicon.optString("name");
                    //下载图标
                    IconUtils.downIcon(1, context, name, jicon.optString("caption"), timeIconUnix, PathUtils.getRdTtfPath(), iconListener);
                }
                return list;

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        return null;

    }


    private static final String TAG = "TTFUtils";

    private static String getTtf() {
        return RdHttpClient.post(URLConstants.GETFONT, new NameValuePair("os",
                Integer.toString(2)));
    }

}
