package com.rd.veuisdk.mvp.model;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.rd.http.NameValuePair;
import com.rd.lib.utils.LogUtil;
import com.rd.lib.utils.ThreadPoolUtils;
import com.rd.net.RdHttpClient;
import com.rd.vecore.Music;
import com.rd.vecore.VirtualVideo;
import com.rd.vecore.exception.InvalidArgumentException;
import com.rd.vecore.listener.ExportListener;
import com.rd.vecore.models.VideoConfig;
import com.rd.veuisdk.demo.zishuo.TextNode;
import com.rd.veuisdk.model.bean.FindText;
import com.rd.veuisdk.utils.PathUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class VoiceTextModel {
    private static final String TAG = "VoiceTextModel";

    //回调 URL，用户自行搭建的用于接收识别结果的服务器地址
    private final String CALLBACK_RUL = "http://d.56show.com/filemanage2/public/filemanage/voice2text/audio2text4tencent";

    //https://cloud.tencent.com/document/product/1093/35800  腾讯录音文件识别
    private final String SECRET_ID = "AKIDmOlskNuJdiY8Sqhxf8LI5wXtzpQ63K4Y";
    private final String SECRET_KEY = "OXQcYEiwusa1EAqGPIxM5apoXzCBuACy";
    private final String APPID = "1259660397";

    private boolean isRecycled = false;
    private Handler mHandler;

    public VoiceTextModel(){
        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                }
        };
    }

    /**
     * 上传语音文件到腾讯云识别，返回文字、时间段信息
     *
     * @param path
     */
    private void uploadAudioFile(String path, final IVoice2TextCallBack callBack) {
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
//        OkHttpClient client = new OkHttpClient.Builder()
//                .connectTimeout(10, TimeUnit.SECONDS)
//                .readTimeout(20, TimeUnit.SECONDS).build();
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
                                onFailed(callBack, "error");
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

    /**
     * 失败
     * @param callBack
     */
    private void onFailed(final IVoice2TextCallBack callBack, final String msg) {
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
    private void findText(final int requestId, final IVoice2TextCallBack callBack) {
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
                        callBack.onResult(null != findtext ? findtext.getTextNode() : null, null);
                    }
                }
            }
        });
    }

    /**
     * AI识别
     *
     * @param callBack        回调
     */
    public void onAI(Context context, List<Music> musics, final @NonNull IVoice2TextCallBack callBack) {
        final VirtualVideo virtualVideo = new VirtualVideo();
        int len = musics.size();
        try {
            for (int i = 0; i < len; i++) {
                virtualVideo.addMusic(musics.get(i));
            }
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
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

    /**
     * 销毁时主动调用
     */
    public void recycle() {
        isRecycled = true;
    }

    /**
     * AI回调
     */
    public interface IVoice2TextCallBack {

        void onResult(List<TextNode> list, String msg);

    }

}
