package com.rd.veuisdk.mvp.model;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.rd.http.MD5;
import com.rd.lib.utils.ThreadPoolUtils;
import com.rd.vecore.RdVECore;
import com.rd.vecore.models.MVInfo;
import com.rd.veuisdk.ae.AETemplateUtils;
import com.rd.veuisdk.ae.model.AETemplateInfo;
import com.rd.veuisdk.database.MVData;
import com.rd.veuisdk.model.MVWebInfo;
import com.rd.veuisdk.utils.FileUtils;
import com.rd.veuisdk.utils.ModeDataUtils;
import com.rd.veuisdk.utils.PathUtils;
import com.rd.veuisdk.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * AE模板
 */
public class AEFragmentModel extends BaseModel {
    private Context mContext;
    private String TAG = "AEFragmentModel";

    public AEFragmentModel(Context context, @NonNull IAECallBack callBack) {
        super(callBack);
        mContext = context;
    }


    public static interface IAECallBack<E, F> extends ICallBack<E> {


        void onSuccess(List<E> list, List<F> aeList);
    }


    private List<AETemplateInfo> mAETemplateInfoList = new ArrayList<>();
    private String mUrl;
    private boolean isAE;

    public void getWebData(String url, final boolean ae) {
        mUrl = url;
        isAE = ae;
        ThreadPoolUtils.execute(new ThreadPoolUtils.ThreadPoolRunnable() {

            @Override
            public void onBackground() {

                if (TextUtils.isEmpty(mUrl)) {
                    Log.e(TAG, "mv  config.getUrl()  is null");
                } else {
                    getDataImp(isAE);
                }
            }

            @Override
            public void onEnd() {
                super.onEnd();
                if (mList != null && mList.size() > 0) {
                    if (mCallBack instanceof IAECallBack && !isRecycled) {
                        ((IAECallBack) mCallBack).onSuccess(mList, mAETemplateInfoList);
                    }
                } else {
                    onFailed();
                }

            }
        });
    }

    /**
     *
     */
    private void getDataImp(boolean isAE) {
        String type;
        if (isAE) {
            type = ModeDataUtils.TYPE_VIDEO_AE;
        } else {
            type = ModeDataUtils.TYPE_MVAE;
        }
        String str = ModeDataUtils.getModeData(mUrl, type);
        if (!TextUtils.isEmpty(str)) {// 加载网络数据
            onParseJson2(str);
        }
    }

    private ArrayList<MVWebInfo> mList = new ArrayList<>();

    public String getAEFilePath(String url) {
        return PathUtils.getRdAEPath() + "/" + MD5.getMD5(url) + ".zip";
    }

    private void onParseJson2(String data) {
        try {
            JSONObject jobj = new JSONObject(data);

            if (null != jobj && jobj.optInt("code", -1) == 0) {
                JSONArray jarr = jobj.optJSONArray("data");
                if (null != jarr) {
                    int len = jarr.length();
                    JSONObject jt;
                    mList.clear();
                    mAETemplateInfoList.clear();
                    for (int i = 0; i < len; i++) {
                        if (isRecycled) {
                            break;
                        }
                        jt = jarr.getJSONObject(i);
                        if (null != jt) {
                            String url = jt.getString("file");
                            String name = jt.getString("name");
                            String cover = jt.getString("cover");
                            if (isAE) {
                                AETemplateInfo aeTemplateInfo = new AETemplateInfo();
                                aeTemplateInfo.setUrl(url);
                                File zip = new File(getAEFilePath(url));
                                if (zip.exists()) {
                                    try {
                                        mAETemplateInfoList.add(AETemplateUtils.parseAE(zip.getAbsolutePath()));
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    mAETemplateInfoList.add(aeTemplateInfo);
                                }
                            }
                            long updateTime = jt.optLong("updatetime", 0);
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
                                local.setName(name);
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
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
