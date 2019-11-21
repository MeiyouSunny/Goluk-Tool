package com.rd.veuisdk.mvp.model;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.rd.http.MD5;
import com.rd.lib.utils.ThreadPoolUtils;
import com.rd.veuisdk.model.WebMusicInfo;
import com.rd.veuisdk.utils.ModeDataUtils;
import com.rd.veuisdk.utils.PathUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 云音乐（单个分类数据）
 */
public class CloudFragmentModel extends BaseModel {
    private Context mContext;
    private String TAG = "CloudFragmentModel";

    public CloudFragmentModel(Context context, @NonNull ICallBack callBack) {
        super(callBack);
        mContext = context;
    }

    public static interface CallBack<E> extends ICallBack {

        void onSuccess(List<E> list, int current_page, int last_page);

    }


    public void getWebData(final String mSoundUrl, final String mMusicType, final String id, final int current_page) {

        ThreadPoolUtils.execute(new ThreadPoolUtils.ThreadPoolRunnable() {
            private int currentPage, last_page;

            /**
             * 解析音效
             *
             * @param result
             */
            private void onParseSoundJson(String result) {
                try {
                    JSONObject jobj = new JSONObject(result);
                    if (jobj.getInt("code") == 0) {
                        JSONObject jobj2 = jobj.getJSONObject("data");
                        currentPage = jobj2.getInt("current_page");
                        last_page = jobj2.getInt("last_page");
                        JSONArray jarr = jobj2.getJSONArray("data");
                        if (jarr != null) {
                            getData(jarr);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            private List<WebMusicInfo> webInfos = new ArrayList<>();

            /**
             * 获取数据
             */
            private List<WebMusicInfo> getData(JSONArray jsonArray) throws JSONException {
                JSONObject object;
                WebMusicInfo info;
                StringBuffer sb = null;
                for (int i = 0; i < jsonArray.length(); i++) {
                    object = jsonArray.getJSONObject(i);
                    info = new WebMusicInfo();
                    info.setMusicUrl(object.optString("file"));
                    info.setMusicName(object.optString("name"));
                    info.setId(object.optLong("id"));
                    info.setDuration(object.optInt("duration") * 1000);
                    sb = new StringBuffer(100);
                    sb.append(PathUtils.getRdMusic());
                    sb.append("/");
                    sb.append(MD5.getMD5(info.getMusicUrl()));
                    sb.append(".mp3");
                    info.setLocalPath(sb.toString());
                    info.checkExists();
                    webInfos.add(info);
                }
                return webInfos;
            }

            @Override
            public void onBackground() {
                String result = ModeDataUtils.getModeData(mSoundUrl, mMusicType, id, current_page);
                if (!TextUtils.isEmpty(result)) {
                    onParseSoundJson(result);
                }
            }

            @Override
            public void onEnd() {
                super.onEnd();
                if (webInfos != null && webInfos.size() > 0) {
                    if (mCallBack instanceof CallBack && !isRecycled) {
                        ((CallBack) mCallBack).onSuccess(webInfos, currentPage, last_page);
                    }
                } else {
                    onFailed();
                }
            }
        });
    }


}
