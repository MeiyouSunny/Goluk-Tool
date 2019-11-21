package com.rd.veuisdk.mvp.model;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.rd.http.NameValuePair;
import com.rd.lib.utils.ThreadPoolUtils;
import com.rd.net.RdHttpClient;
import com.rd.veuisdk.model.IMusicApi;
import com.rd.veuisdk.utils.ModeDataUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * MoreMusicActivity 的音乐
 *
 * @create 2019/6/25
 */
public class MoreMusicModel extends BaseModel {

    public static interface IMusicCallBack extends ICallBack {

        /**
         * 音效、云音乐  --- 支持分页
         *
         * @param ids
         * @param musicApiArrayList
         */
        void onSound(ArrayList<String> ids, ArrayList<IMusicApi> musicApiArrayList);


        /**
         * 锐动素材 -云音乐
         *
         * @param musicApiArrayList
         */
        void onRdCloudMusic(ArrayList<IMusicApi> musicApiArrayList);

    }

    public MoreMusicModel(@NonNull ICallBack callBack) {
        super(callBack);
    }


    /**
     * 获取音效、云音乐 分类
     *
     * @param mSoundTypeUrl
     * @param type
     */
    public void getSoundType(final String mSoundTypeUrl, final @ModeDataUtils.ResourceType String type) {

        ThreadPoolUtils.execute(new Runnable() {
            @Override
            public void run() {
                String result = ModeDataUtils.getModeData(mSoundTypeUrl, type);
                if (!TextUtils.isEmpty(result)) {
                    onParseSoundTypeJson(result);
                }
            }
        });
    }

    /**
     * 云音乐
     */
    public void getRdCouldMusic(final String mCloudMusicUrl) {
        ThreadPoolUtils.execute(new Runnable() {
            @Override
            public void run() {
                String result = ModeDataUtils.getModeData(mCloudMusicUrl, ModeDataUtils.TYPE_CLOUD_MUSIC);
                onParseJson2(result);
            }
        });

    }

    private void onParseJson2(String result) {
        final ArrayList<IMusicApi> mMusicApiList = new ArrayList<>();
        try {
            JSONObject jobj = new JSONObject(result);
            if (jobj.getInt("code") == 0) {
                JSONArray jarr = jobj.getJSONArray("data");
                int len = 0;
                if (null != jarr && (len = jarr.length()) > 0) {
                    for (int i = 0; i < len; i++) {
                        mMusicApiList.add(new IMusicApi(jarr.getJSONObject(i)));
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
                    ((IMusicCallBack) mCallBack).onRdCloudMusic(mMusicApiList);
                }
            });
        }
    }

    /**
     * 解析音效分类
     *
     * @param result
     */
    private void onParseSoundTypeJson(String result) {
        final ArrayList<String> mClassification = new ArrayList<>();
        final ArrayList<IMusicApi> mMusicApiList = new ArrayList<>();
        try {
            JSONObject jobj = new JSONObject(result);
            JSONObject object;
            if (jobj.getInt("code") == 0) {
                JSONArray jarr = jobj.getJSONArray("data");
                int len = 0;
                if (null != jarr && (len = jarr.length()) > 0) {
                    for (int i = 0; i < len; i++) {
                        object = jarr.getJSONObject(i);
                        mClassification.add(object.getString("id"));
                        mMusicApiList.add(new IMusicApi(object.getString("name")));
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
                    ((IMusicCallBack) mCallBack).onSound(mClassification, mMusicApiList);
                }
            });
        }
    }


    /**
     * 云音乐-不支持分页 兼容第一批云音乐接口
     *
     * @param mSoundUrl
     */
    @Deprecated
    public void getMusic(final String mSoundUrl) {
        ThreadPoolUtils.execute(new ThreadPoolUtils.ThreadPoolRunnable() {
            final ArrayList<IMusicApi> mMusicApiList = new ArrayList<>();

            @Override
            public void onBackground() {
                String result = RdHttpClient.PostJson(mSoundUrl,
                        new NameValuePair("type", "android"));
                if (!TextUtils.isEmpty(result)) {
                    try {
                        JSONObject jobj = new JSONObject(result);
                        if (jobj.optBoolean("state", false)) {
                            jobj = jobj.optJSONObject("result");
                            JSONArray jarr = jobj.optJSONArray("bgmusic");
                            int len = 0;
                            if (null != jarr && (len = jarr.length()) > 0) {
                                for (int i = 0; i < len; i++) {
                                    mMusicApiList.add(new IMusicApi(jarr.getJSONObject(i)));
                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onEnd() {
                super.onEnd();
                if (null != mCallBack && !isRecycled) {
                    ((IMusicCallBack) mCallBack).onRdCloudMusic(mMusicApiList);
                }
            }
        });

    }


}
