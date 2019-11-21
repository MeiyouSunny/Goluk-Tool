package com.rd.veuisdk.mvp.model;

import android.text.TextUtils;

import com.rd.lib.utils.ThreadPoolUtils;
import com.rd.veuisdk.database.StickerData;
import com.rd.veuisdk.model.IStickerSortApi;
import com.rd.veuisdk.model.StyleInfo;
import com.rd.veuisdk.net.StickerUtils;
import com.rd.veuisdk.utils.CommonStyleUtils;
import com.rd.veuisdk.utils.ModeDataUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Sticker2FragmentModel extends BaseModel {

    private String mType = "http://d.56show.com/filemanage2/public/filemanage/file/typeData";
    private String mData = "http://d.56show.com/filemanage2/public/filemanage/file/appData";

    public Sticker2FragmentModel(ICallBack callBack, String typeurl, String dataurl) {
        super(callBack);
        if (!TextUtils.isEmpty(typeurl) && !TextUtils.isEmpty(mData)) {
            mType = typeurl;
            mData = dataurl;
        }
    }

    /**
     * 获取分类
     */
    public void getApiSort() {
        ThreadPoolUtils.execute(new Runnable() {
            @Override
            public void run() {
                String result = ModeDataUtils.getModeData(mType, ModeDataUtils.TYPE_STICKERS);
                if (!TextUtils.isEmpty(result)) {
                    StickerUtils.getInstance().clearArray();
                    //解析分类
                    onParseStickerTypeJson(result);
                }
            }
        });
    }

    /**
     * 解析贴纸分类
     *
     * @param result
     */
    private void onParseStickerTypeJson(String result) {
        final ArrayList<IStickerSortApi> stickerApis = new ArrayList<>();
        try {
            JSONObject jobj = new JSONObject(result);
            JSONObject object;
            if (jobj.getInt("code") == 0) {
                JSONArray jarr = jobj.getJSONArray("data");
                int len = 0;
                if (null != jarr && (len = jarr.length()) > 0) {
                    for (int i = 0; i < len; i++) {
                        object = jarr.getJSONObject(i);
                        IStickerSortApi stickerApi = new IStickerSortApi();
                        stickerApi.setId(object.getString("id"));
                        stickerApi.setName(object.getString("name"));
                        stickerApi.setIcon(object.getString("icon_checked"));
                        stickerApi.setIconP(object.getString("icon_unchecked"));
                        stickerApi.setUpdatetime(object.getString("updatetime"));
                       stickerApis.add(stickerApi);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (null != mCallBack && !isRecycled) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    ((StickerCallBack)mCallBack).onStickerSort(stickerApis);
                }
            });
        }
    }

    /**
     * 获取分类数据
     */
    public void getStickerData(final String category) {
        ThreadPoolUtils.execute(new Runnable() {
            @Override
            public void run() {
                String result = ModeDataUtils.getModeData(mData, ModeDataUtils.TYPE_STICKERS, category);
                if (!TextUtils.isEmpty(result)) {
                    //解析具体数据
                    onParseStickerDataJson(result, category);
                }
            }
        });
    }

    /**
     * 解析分类数据
     */
    private void onParseStickerDataJson(String result, final String category){
        try {
            JSONObject jobj = new JSONObject(result);
            JSONObject object;
            if (jobj.getInt("code") == 0) {
                JSONArray jarr = jobj.getJSONArray("data");
                int len = 0;
                if (null != jarr && (len = jarr.length()) > 0) {
                    ArrayList<StyleInfo> dbList = StickerData.getInstance().getAll(true);
                    ArrayList<StyleInfo> newStyleInfo = new ArrayList<>();
                    StyleInfo tmp;
                    for (int i = 0; i < len; i++) {
                        object = jarr.getJSONObject(i);
                        tmp = new StyleInfo(true, false);
                        tmp.code = object.getString("name");
                        tmp.caption = object.getString("file");
                        tmp.icon = object.getString("cover");
                        tmp.pid = tmp.code.hashCode();
                        tmp.nTime = object.getLong("updatetime");
                        tmp.st = CommonStyleUtils.STYPE.special;
                        tmp.index = tmp.caption.hashCode();
                        tmp.category = category;
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
                        newStyleInfo.add(tmp);
                        StickerUtils.getInstance().putStyleInfo(tmp);
                    }
                    StickerData.getInstance().replaceAll(newStyleInfo);
                    if (null != dbList) {
                        dbList.clear();
                    }
                    if (null != mCallBack && !isRecycled) {
                        final ArrayList<StyleInfo> finalNewStyleInfo = newStyleInfo;
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                ((StickerCallBack)mCallBack).onStickerSortData(finalNewStyleInfo, category);
                            }
                        });
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

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

    public interface StickerCallBack  extends ICallBack {

        /**
         * 贴纸分类
         */
        void onStickerSort(ArrayList<IStickerSortApi> stickerApis);

        /**
         * 贴纸分类数据
         */
        void onStickerSortData(List<StyleInfo> list, String category);

    }

}
