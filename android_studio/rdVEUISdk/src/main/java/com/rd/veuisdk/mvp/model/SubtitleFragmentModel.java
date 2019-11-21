package com.rd.veuisdk.mvp.model;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.rd.http.NameValuePair;
import com.rd.lib.utils.CoreUtils;
import com.rd.lib.utils.FileUtils;
import com.rd.lib.utils.LogUtil;
import com.rd.lib.utils.ThreadPoolUtils;
import com.rd.net.JSONObjectEx;
import com.rd.net.RdHttpClient;
import com.rd.vecore.VirtualVideo;
import com.rd.vecore.listener.ExportListener;
import com.rd.vecore.models.MediaObject;
import com.rd.vecore.models.Scene;
import com.rd.vecore.models.VideoConfig;
import com.rd.veuisdk.R;
import com.rd.veuisdk.database.SubData;
import com.rd.veuisdk.model.StyleInfo;
import com.rd.veuisdk.model.VideoOb;
import com.rd.veuisdk.model.bean.AppData;
import com.rd.veuisdk.model.bean.DataBean;
import com.rd.veuisdk.model.bean.FindText;
import com.rd.veuisdk.net.IconUtils;
import com.rd.veuisdk.net.SubUtils;
import com.rd.veuisdk.utils.AppConfiguration;
import com.rd.veuisdk.utils.CommonStyleUtils;
import com.rd.veuisdk.utils.ModeDataUtils;
import com.rd.veuisdk.utils.PathUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 字幕网络数据
 *
 * @author JIAN
 * @create 2019/4/26
 * @Describe 网络数据和界面解耦
 */
public class SubtitleFragmentModel extends ISSModel {
    private String TAG = "SubtitleFragmentModel";

    public SubtitleFragmentModel(Context context) {
        super(null, context);

    }

    @Override
    void getApiData(String url) {
        String data = ModeDataUtils.getData(mContext, url, ModeDataUtils.TYPE_SUB_TITLE);
        if (!TextUtils.isEmpty(data)) {
            AppData aeData = JSON.parseObject(data, AppData.class);
            if (null != aeData && aeData.getData() != null) {
                int len = aeData.getData().size();
                DataBean dataBean;
                ArrayList<StyleInfo> dbList = SubData.getInstance().getAll(true);
                StyleInfo tmp;
                for (int i = 0; i < len; i++) {
                    dataBean = aeData.getData().get(i);
                    tmp = new StyleInfo(true, true);
                    tmp.code = dataBean.getName();
                    tmp.caption = dataBean.getFile();
                    tmp.icon = dataBean.getCover();
                    tmp.pid = tmp.code.hashCode();
                    tmp.index = i;
                    tmp.nTime = dataBean.getUpdatetime();
                    StyleInfo dbTemp = checkExit(dbList, tmp);

                    if (null != dbTemp) {
                        if (SubData.getInstance().checkDelete(tmp, dbTemp)) {
                            tmp.isdownloaded = false;
                        } else {
                            tmp.isdownloaded = true;
                            tmp.isdownloaded = dbTemp.isdownloaded;
                            if (tmp.isdownloaded) {
                                tmp.mlocalpath = dbTemp.mlocalpath;
                                CommonStyleUtils.checkStyle(new File(tmp.mlocalpath), tmp);
                            }
                        }
                    }
                    SubUtils.getInstance().putStyleInfo(tmp);
                }
                if (null != dbList) {
                    dbList.clear();
                }
                ArrayList<StyleInfo> newall = SubUtils.getInstance().getStyleInfos();
                SubData.getInstance().replaceAll(newall);
                aeData.getData().clear();
                if (null != mCallBack) {
                    mHandler.obtainMessage(MSG_SUCCESS, newall).sendToTarget();
                }
            } else {
                onFailed();
            }
        } else {
            onFailed();
        }
    }


    @Deprecated
    void getWebData() {
        String content = null;
        if (CoreUtils.checkNetworkInfo(mContext) != CoreUtils.UNCONNECTED) {
            content = SubUtils.getInstance().getSubJson();
        }
        if (!TextUtils.isEmpty(content)) {
            JSONObjectEx jex;
            try {
                jex = new JSONObjectEx(content);
                if (null != jex && jex.getInt("code") == 200) {
                    ArrayList<StyleInfo> dbList = SubData.getInstance()
                            .getAll(false);
                    StyleInfo tmp = null;
                    JSONObject jobj = null;
                    JSONArray jarr = jex.getJSONArray("data");
                    JSONObject jicon = jex.getJSONObject("icon");
                    String timeunix = jicon.getString("timeunix");
                    if (!AppConfiguration.checkSubIconIsLasted(timeunix)) {
                        downloadIcon(timeunix, jicon.optString("zimu"), jicon.optString("name"));
                    }
                    int len = jarr.length();
                    for (int i = 0; i < len; i++) {
                        jobj = jarr.getJSONObject(i);
                        tmp = new StyleInfo(false, true);
                        tmp.code = jobj.optString("name");
                        tmp.caption = jobj.optString("zimu");
                        tmp.pid = tmp.code.hashCode();
                        tmp.index = i;
                        tmp.nTime = jobj.getLong("timeunix");
                        StyleInfo dbTemp = checkExit(dbList, tmp);

                        if (null != dbTemp) {
                            if (SubData.getInstance().checkDelete(tmp, dbTemp)) {
                                tmp.isdownloaded = false;
                            } else {
                                tmp.isdownloaded = true;
                                tmp.isdownloaded = dbTemp.isdownloaded;
                                if (tmp.isdownloaded) {
                                    tmp.mlocalpath = dbTemp.mlocalpath;
                                    CommonStyleUtils.checkStyle(new File(tmp.mlocalpath), tmp);
                                }
                            }
                        }
                        SubUtils.fixLocalIcon(tmp);
                        SubUtils.getInstance().putStyleInfo(tmp);
                    }
                    if (null != dbList) {
                        dbList.clear();
                    }
                    ArrayList<StyleInfo> list = SubUtils.getInstance().getStyleInfos();
                    SubData.getInstance().replaceAll(list);
                    if (null != mCallBack) {
                        mHandler.obtainMessage(MSG_SUCCESS, list).sendToTarget();
                    }
                } else {
                    onFailed();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(TAG, "run: data error: " + content);
                onFailed();
            }
        } else {
            onFailed();
        }
    }

    /**
     * 下载字幕图标
     */
    @Override
    @Deprecated
    void downloadIcon(String timeUnix, String url, String name) {
        IconUtils.downIcon(2, mContext, name, url, timeUnix, PathUtils.getRdSubPath(), new IconUtils.IconListener() {
            @Override
            public void prepared() {
                if (mCallBack instanceof ISSCallBack && !isRecycled) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            ((ISSCallBack) mCallBack).onIconSuccess();
                        }
                    });

                }
            }
        });

    }


    //回调 URL，用户自行搭建的用于接收识别结果的服务器地址
    private final String CALLBACK_RUL = "http://d.56show.com/filemanage2/public/filemanage/voice2text/audio2text4tencent";

    //https://cloud.tencent.com/document/product/1093/35800  腾讯录音文件识别

    //********************************** SECRET_ID、SECRET_KEY  需自行到腾讯云平台注册申请*******************************
    private final String SECRET_ID = "AKIDmOlskNuJdiY8Sqhxf8LI5wXtzpQ63K4Y";
    private final String SECRET_KEY = "OXQcYEiwusa1EAqGPIxM5apoXzCBuACy";
    private final String APPID = "1259660397";
    //**********************************************end****************************************************

    /**
     * 上传语音文件到腾讯云识别，返回文字、时间段信息
     *
     * @param path
     */
    private void uploadAudioFile(String path, final IAudioAutoRecognitionCallBack callBack) {
        long st = System.currentTimeMillis() / 1000; //单位：秒
        VideoConfig videoConfig = new VideoConfig();
        VirtualVideo.getMediaInfo(path, videoConfig, true);
        String url = "aai.qcloud.com/asr/v1/" + APPID + "?callback_url=" + CALLBACK_RUL + "&channel_num=" + videoConfig.getAudioNumChannels() + "&engine_model_type=8k_0" +
                "&expired=" + (st + 3600) + "&nonce=" + (int) ((Math.random() * 1000000)) + "&projectid=0&res_text_format=0&res_type=1&" +
                "secretid=" + SECRET_ID + "&source_type=1&sub_service_type=0" +
                "&timestamp=" + st;

        String key = HMAC_SHA1.genHMAC("POST" + url, SECRET_KEY);
        File file = new File(path);
        MediaType mediaType = MediaType.parse("text/x-markdown; charset=utf-8");
        Request.Builder builder = new Request.Builder();
        builder.url("https://" + url).post(RequestBody.create(mediaType, file));
        builder.addHeader("Host", "aai.qcloud.com");
        builder.addHeader("Authorization", key);
        builder.addHeader("Content-Type", "application/octet-stream");
        builder.addHeader("Content-Length", String.valueOf(file.length()));
        OkHttpClient client = new OkHttpClient();
        Call call = client.newCall(builder.build());
        call.enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "onFailure: " + e.getMessage());
                onFailed(callBack, null);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                if (!isRecycled) {
                    if (!TextUtils.isEmpty(result)) {
                        try {
                            JSONObject jsonObject = new JSONObject(result);
                            int code = jsonObject.optInt("code", -1);
                            if (code == 0) {
                                int requestId = jsonObject.optInt("requestId", 0);
                                if (0 != requestId && !isRecycled) {
                                    findText(requestId, callBack);
                                }
                            } else if (code == 1016) { //服务超额
                                onFailed(callBack, mContext.getString(R.string.auto_server_error));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            onFailed(callBack, null);
                        }

                    } else {
                        onFailed(callBack, null);
                    }
                }
            }
        });
    }

    private void onFailed(final IAudioAutoRecognitionCallBack callBack, final String msg) {
        if (!isRecycled) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (null != callBack) {
                        callBack.onResult(null, msg);
                    }
                }
            });
        }
    }

    //轮询返回信息
    private final String FIND_TEXT = "http://d.56show.com/filemanage2/public/filemanage/voice2text/findText";

    /**
     * 轮询 腾讯云返回值
     */
    private void findText(final int requestId, final IAudioAutoRecognitionCallBack callBack) {
        ThreadPoolUtils.executeEx(new ThreadPoolUtils.ThreadPoolRunnable() {
            FindText findtext;

            /**
             * 轮询获取腾讯识别的结果  （最大等待1分钟）
             */
            @Override
            public void onBackground() {
                long st = System.currentTimeMillis();
                //最大等待1分钟
                while (!isRecycled && (System.currentTimeMillis() - st) < 60 * 1000) {
                    String result = RdHttpClient.post(FIND_TEXT, new NameValuePair("requestId", Integer.toString(requestId)));
                    LogUtil.i(TAG, "onBackground: " + result);
                    if (!TextUtils.isEmpty(result)) {
                        findtext = JSON.parseObject(result, FindText.class);
                        if (null != findtext && findtext.getCode() == 0) {
                            break;
                        }
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onEnd() {
                super.onEnd();
                if (!isRecycled) {
                    if (null != callBack) {
                        callBack.onResult(null != findtext ? findtext.getList() : null, null);
                    }
                }
            }
        });

    }


    /**
     * AI回调
     */
    public interface IAudioAutoRecognitionCallBack {
        /**
         * @param list 文字列表
         * @param msg  错误信息
         */
        void onResult(List<FindText.TextInfo> list, String msg);

    }


    /**
     * 是否启用ai识别
     *
     * @param sceneList
     */
    public boolean checkEnableAI(List<Scene> sceneList) {
        int len = 0;
        if (null != sceneList && (len = sceneList.size()) > 0) {
            for (int i = 0; i < len; i++) {
                Scene scene = sceneList.get(i);
                List<MediaObject> list = scene.getAllMedia();
                for (int j = 0; j < list.size(); j++) {
                    MediaObject tmp = list.get(j);
                    if (tmp.getMediaType() == com.rd.vecore.models.MediaType.MEDIA_VIDEO_TYPE && !tmp.isAudioMute() && tmp.getMixFactor() > 0) {
                        Object tag = tmp.getTag();
                        if (tag instanceof VideoOb) {
                            VideoOb videoOb = (VideoOb) tag;
                            if (videoOb.getVideoObjectPack() != null && videoOb.getVideoObjectPack().isReverse) {
                                //倒序状态
                            } else {
                                return true;
                            }
                        } else {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    //记录上次修正zip时的时间
    private final long last_update_time = new BigDecimal("1568603370000").longValue();

    /**
     * 构建默认的字幕样式(防止所有字幕样式未下载)
     */
    public StyleInfo initDefaultStyle(Context context) {
        String path = PathUtils.getRdAssetPath();
        String fileName = "text_sample";
        File dstZip = new File(path, fileName + ".zip");
        if (dstZip.exists()) {
            if (dstZip.lastModified() <= last_update_time) {
                dstZip.delete();
                CoreUtils.assetRes2File(context.getAssets(), fileName + ".zip", dstZip.getAbsolutePath());
            }
        } else {
            CoreUtils.assetRes2File(context.getAssets(), fileName + ".zip", dstZip.getAbsolutePath());
        }
        File dirTarget = new File(path, fileName);
        if (dirTarget.exists()) {
            if (dirTarget.lastModified() <= last_update_time) {
                dirTarget.delete();
                try {
                    dirTarget = new File(FileUtils.unzip(dstZip.getAbsolutePath(), new File(path).getAbsolutePath()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            try {
                dirTarget = new File(FileUtils.unzip(dstZip.getAbsolutePath(), new File(path).getAbsolutePath()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        StyleInfo styleInfo = new StyleInfo(true, true);
        CommonStyleUtils.getConfig(new File(dirTarget, "config.json"), styleInfo);
        return styleInfo;
    }

    /**
     * AI识别
     *
     * @param sceneList 场景文件
     * @param callBack  回调
     */
    public void onAI(Context context, List<Scene> sceneList, final @NonNull IAudioAutoRecognitionCallBack callBack) {
        final VirtualVideo virtualVideo = new VirtualVideo();
        int len = sceneList.size();
        for (int i = 0; i < len; i++) {
            virtualVideo.addScene(sceneList.get(i));
        }
        final String path = PathUtils.getTempFileNameForSdcard("Temp_audio", "m4a");
        final VideoConfig videoConfig = new VideoConfig();
        videoConfig.setAudioEncodingParameters(2, 44100, 128 * 1000);
        //提取音频必须m4a
        virtualVideo.export(context, path, videoConfig, new ExportListener() {
            @Override
            public void onExportStart() {
            }

            @Override
            public boolean onExporting(int progress, int max) {
                return true;
            }

            @Override
            public void onExportEnd(int result) {
                virtualVideo.release();
                if (result >= VirtualVideo.RESULT_SUCCESS) {
                    //step 2:上传到腾讯云并获取识别的信息
                    uploadAudioFile(path, callBack);
                } else {
                    callBack.onResult(null, null);
                }
            }
        });
    }
}
