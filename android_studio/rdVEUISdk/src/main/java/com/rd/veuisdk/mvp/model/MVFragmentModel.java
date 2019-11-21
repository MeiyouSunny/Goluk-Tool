package com.rd.veuisdk.mvp.model;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.rd.http.MD5;
import com.rd.http.NameValuePair;
import com.rd.lib.utils.CoreUtils;
import com.rd.lib.utils.ThreadPoolUtils;
import com.rd.net.RdHttpClient;
import com.rd.vecore.RdVECore;
import com.rd.vecore.models.MVInfo;
import com.rd.veuisdk.database.MVData;
import com.rd.veuisdk.model.MVWebInfo;
import com.rd.veuisdk.utils.FileUtils;
import com.rd.veuisdk.utils.ModeDataUtils;
import com.rd.veuisdk.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * mv数据
 */
public class MVFragmentModel extends BaseModel {
    private Context mContext;
    private ArrayList<MVWebInfo> mList = null;

    public MVFragmentModel(Context context, @NonNull ICallBack callBack) {
        super(callBack);
        mContext = context;
        mList = new ArrayList<MVWebInfo>();
    }

    //防止频繁获取网络数据（一次加载成功即可加载本地离线数据）
    private boolean mLoadWebDataSuccessed = false;
    private File mCacheDir;
    private String mMvUrl;

    public void getWebMV(final String url, final boolean bUseNewMV) {
        mCacheDir = mContext.getCacheDir();
        mMvUrl = url;
        ThreadPoolUtils.execute(new ThreadPoolUtils.ThreadPoolRunnable() {

            @Override
            public void onBackground() {
                if (TextUtils.isEmpty(mMvUrl)) {
                } else {
                    if (bUseNewMV) {
                        //新的mv接口 (推荐)
                        getMVImp();
                    } else {
                        getMVImpDep();
                    }
                }
            }

            @Override
            public void onEnd() {
                super.onEnd();
                if (null != mList && mList.size() > 0) {
                    onSuccess(mList);
                } else {
                    onFailed();
                }
            }
        });
    }

    /**
     * 新的mv接口
     */
    private void getMVImp() {
        mList.clear();
        List data = ModeDataUtils.init(mContext, mMvUrl, ModeDataUtils.TYPE_MV);
        if (null != data && !isRecycled) {
            onCompareData(data);
        }
    }

    @Deprecated
    private void getMVImpDep() {
        File f = new File(mCacheDir, MD5.getMD5("mv_data.json"));
        if (!mLoadWebDataSuccessed && CoreUtils.checkNetworkInfo(mContext) != CoreUtils.UNCONNECTED) {
            String str = RdHttpClient.PostJson(mMvUrl, new NameValuePair("type", "android"));
            if (!TextUtils.isEmpty(str)) {// 加载网络数据
                onParseJson(str);
                try {
                    String data = URLEncoder.encode(str, "UTF-8");
                    FileUtils.writeText2File(data, f.getAbsolutePath());
                    mLoadWebDataSuccessed = true;
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();

                }
            }
        }
        if (!mLoadWebDataSuccessed && null != f && f.exists()) {// 加载离线数据
            String offline = FileUtils.readTxtFile(f.getAbsolutePath());
            try {
                offline = URLDecoder.decode(offline, "UTF-8");
                if (!TextUtils.isEmpty(offline)) {
                    onParseJson(offline);
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();

            }

        }
    }


    /**
     * json数据和本地数据库比较
     *
     * @param list json数据
     */
    private void onCompareData(List<MVWebInfo> list) {
        int len = list.size();
        for (int i = 0; i < len; i++) {
            if (isRecycled) {
                break;
            }
            MVWebInfo tmp = list.get(i);
            String url = tmp.getUrl();
            String name = tmp.getName();
            String cover = tmp.getCover();
            long updateTime = tmp.getUpdatetime();
            String localPath = "";
            MVWebInfo local = MVData.getInstance().quweryOne(url);
            boolean result = false;
            if (null != local) {
                //判断服务器是否已经更新 （如果有更新删除旧的文件或文件夹）
                if (updateTime != local.getUpdatetime()) {
                    //服务端已经更新，需要重新下载
                    FileUtils.deleteAll(local.getLocalPath());
                    result = true;
                }
            }
            if (result) {
                //旧的版本，服务端已经更新，需要重新下载
                local.setId(MVWebInfo.DEFAULT_MV_NO_REGISTED);
                local.setCover(cover);
                local.setHeadDuration(0);
                local.setLastDuration(0);
                local.setLocalPath("");
                local.setName(tmp.getName());
                local.setUpdatetime(updateTime);
                mList.add(local);
                //更新本地数据库
                MVData.getInstance().replace(local);
            } else {

                if (null != local) {
                    local.setUpdatetime(updateTime);
                    localPath = local.getLocalPath();
                    if (!TextUtils.isEmpty(localPath)) {
                        MVInfo mv = null;
                        try {
                            mv = RdVECore.registerMV(localPath);
                            if (null != mv) {
                                local.setHeadDuration(Utils.s2ms(mv.getHeadDuration()));
                                local.setLastDuration(Utils.s2ms(mv.getLastDuration()));
                                local.setId(mv.getId());
                                local.setCover(cover);
                                mList.add(local);
                            } else {
                                mList.add(new MVWebInfo(MVWebInfo.DEFAULT_MV_NO_REGISTED, url, cover,
                                        name, localPath, updateTime));
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            mList.add(new MVWebInfo(MVWebInfo.DEFAULT_MV_NO_REGISTED, url, cover,
                                    name, localPath, updateTime));
                        } catch (JSONException e) {
                            e.printStackTrace();
                            mList.add(new MVWebInfo(MVWebInfo.DEFAULT_MV_NO_REGISTED, url, cover,
                                    name, localPath, updateTime));
                        }

                    } else {
                        mList.add(new MVWebInfo(MVWebInfo.DEFAULT_MV_NO_REGISTED, url, cover,
                                name, localPath, updateTime));
                    }
                } else {
                    MVWebInfo info = new MVWebInfo(MVWebInfo.DEFAULT_MV_NO_REGISTED, url, cover,
                            name, localPath, updateTime);
                    mList.add(info);
                }
            }
        }

    }

    @Deprecated
    private void onParseJson(String data) {
        ArrayList<MVWebInfo> tmp = onParseJson3(data);
        mList.clear();
        if (null != tmp && !isRecycled) {
            onCompareData(tmp);
        }
    }

    @Deprecated
    private ArrayList<MVWebInfo> onParseJson3(String data) {
        ArrayList<MVWebInfo> mlist = null;
        try {
            JSONObject jobj = new JSONObject(data);
            JSONObject jtmp = jobj.optJSONObject("result");
            if (null != jtmp) {
                JSONArray jarr = jtmp.optJSONArray("mvlist");
                if (null == jarr) {
                    jarr = jtmp.optJSONArray("data");
                }
                if (null != jarr) {
                    int len = jarr.length();
                    JSONObject jt;
                    mlist = new ArrayList<>();
                    for (int i = 0; i < len; i++) {
                        jt = jarr.getJSONObject(i);
                        if (null != jt) {
                            String url = jt.getString("url");
                            String name = jt.getString("name");
                            String img = jt.getString("img");
                            mlist.add(new MVWebInfo(MVWebInfo.DEFAULT_MV_NO_REGISTED, url, img, name, ""));
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return mlist;
    }

}
