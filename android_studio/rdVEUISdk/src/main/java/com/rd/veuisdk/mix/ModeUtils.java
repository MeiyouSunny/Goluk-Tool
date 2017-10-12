package com.rd.veuisdk.mix;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.TextUtils;
import android.util.Log;

import com.rd.vecore.VirtualVideo;
import com.rd.vecore.exception.InvalidArgumentException;
import com.rd.vecore.models.MediaObject;
import com.rd.vecore.models.Scene;
import com.rd.vecore.models.VideoConfig;
import com.rd.vecore.utils.MiscUtils;
import com.rd.veuisdk.R;
import com.rd.veuisdk.SdkEntry;
import com.rd.veuisdk.SelectMediaActivity;
import com.rd.veuisdk.TrimMediaActivity;
import com.rd.veuisdk.manager.TrimConfiguration;
import com.rd.veuisdk.manager.UIConfiguration;
import com.rd.veuisdk.utils.IntentConstants;
import com.rd.veuisdk.utils.PathUtils;

import java.util.ArrayList;

/**
 * Created by JIAN on 2017/8/25.
 */

public class ModeUtils {
    private static ArrayList<ModeInfo> listMode = new ArrayList<>();


    public static ArrayList<ModeInfo> getListMode() {
        return listMode;
    }

    /**
     * 初始化画中画模块列表
     */
    public static void init() {
        listMode.clear();


        ArrayList<RectF> temp = new ArrayList<RectF>();

        ArrayList<RectF> noBorderLine = new ArrayList<RectF>();
        temp.add(new RectF(0, 0, 1f, 1f));
        noBorderLine.add(new RectF(0, 0, 1f, 1f));
        listMode.add(new ModeInfo(temp, noBorderLine, R.drawable.mix_icon_1, ""));


        //通过测量得到，边框线大约8个像素

        float fLineWidth = line();
        float halfLineWidth = fLineWidth / 2.0f;

        temp = new ArrayList<RectF>();
        temp.add(new RectF(0, 0, 1f, 0.5f - halfLineWidth));
        temp.add(new RectF(0, 0.5f + halfLineWidth, 1f, 1f));

        noBorderLine = new ArrayList<RectF>();
        noBorderLine.add(new RectF(0, 0, 1f, 0.5f));
        noBorderLine.add(new RectF(0, 0.5f, 1f, 1f));


        listMode.add(new ModeInfo(temp, noBorderLine, R.drawable.mix_icon_2, fixAsset("2_1.png")));


        temp = new ArrayList<RectF>();
        temp.add(new RectF(0, 0, 0.5f - halfLineWidth, 0.5f - halfLineWidth));
        temp.add(new RectF(0.5f + halfLineWidth, 0, 1f, 0.5f - halfLineWidth));
        temp.add(new RectF(0f, 0.5f + halfLineWidth, 1f, 1f));
        noBorderLine = new ArrayList<RectF>();
        noBorderLine.add(new RectF(0, 0, 0.5f, 0.5f));
        noBorderLine.add(new RectF(0.5f, 0, 1f, 0.5f));
        noBorderLine.add(new RectF(0f, 0.5f, 1f, 1f));
        listMode.add(new ModeInfo(temp, noBorderLine, R.drawable.mix_icon_3, fixAsset("3_1.png")));


        temp = new ArrayList<RectF>();
        temp.add(new RectF(0, 0, 0.5f - halfLineWidth, 0.5f - halfLineWidth));
        temp.add(new RectF(0.5f + halfLineWidth, 0, 1f, 0.5f - halfLineWidth));
        temp.add(new RectF(0f, 0.5f + halfLineWidth, 0.5f - halfLineWidth, 1f));
        temp.add(new RectF(0.5f + halfLineWidth, 0.5f + halfLineWidth, 1f, 1f));
        noBorderLine = new ArrayList<RectF>();
        noBorderLine.add(new RectF(0, 0, 0.5f, 0.5f));
        noBorderLine.add(new RectF(0.5f, 0, 1f, 0.5f));
        noBorderLine.add(new RectF(0f, 0.5f, 0.5f, 1f));
        noBorderLine.add(new RectF(0.5f, 0.5f, 1f, 1f));
        listMode.add(new ModeInfo(temp, noBorderLine, R.drawable.mix_icon_4, fixAsset("4_1.png")));


        temp = new ArrayList<RectF>();
        temp.add(new RectF(0, 0, 0.33f - halfLineWidth, 0.5f - halfLineWidth));
        temp.add(new RectF(0f, 0.5f, 0.33f - halfLineWidth, 1f));
        temp.add(new RectF(0.33f + halfLineWidth, 0f, 0.67f - halfLineWidth, 1f));
        temp.add(new RectF(0.67f + halfLineWidth, 0f, 1f, 0.5f - halfLineWidth));
        temp.add(new RectF(0.67f + halfLineWidth, 0.5f + halfLineWidth, 1f, 1f));
        noBorderLine = new ArrayList<RectF>();
        noBorderLine.add(new RectF(0, 0, 0.33f, 0.5f));
        noBorderLine.add(new RectF(0f, 0.5f, 0.33f, 1f));
        noBorderLine.add(new RectF(0.33f, 0f, 0.67f, 1f));
        noBorderLine.add(new RectF(0.67f, 0f, 1f, 0.5f));
        noBorderLine.add(new RectF(0.67f, 0.5f, 1f, 1f));
        listMode.add(new ModeInfo(temp, noBorderLine, R.drawable.mix_icon_5, fixAsset("5_1.png")));


        temp = new ArrayList<RectF>();
        temp.add(new RectF(0, 0, 0.5f - halfLineWidth, 0.33f - halfLineWidth));
        temp.add(new RectF(0.5f + halfLineWidth, 0, 1f, 0.33f - halfLineWidth));
        temp.add(new RectF(0f, 0.33f + halfLineWidth, 0.5f - halfLineWidth, 0.66f - halfLineWidth));
        temp.add(new RectF(0.5f + halfLineWidth, 0.33f + halfLineWidth, 1f, 0.66f - halfLineWidth));
        temp.add(new RectF(0f, 0.66f + halfLineWidth, 0.5f - halfLineWidth, 1f));
        temp.add(new RectF(0.5f + halfLineWidth, 0.66f + halfLineWidth, 1f, 1f));
        noBorderLine = new ArrayList<RectF>();
        noBorderLine.add(new RectF(0, 0, 0.5f, 0.33f));
        noBorderLine.add(new RectF(0.5f, 0, 1f, 0.33f));
        noBorderLine.add(new RectF(0f, 0.33f, 0.5f, 0.66f));
        noBorderLine.add(new RectF(0.5f, 0.33f, 1f, 0.66f));
        noBorderLine.add(new RectF(0f, 0.66f, 0.5f, 1f));
        noBorderLine.add(new RectF(0.5f, 0.66f, 1f, 1f));
        listMode.add(new ModeInfo(temp, noBorderLine, R.drawable.mix_icon_6, fixAsset("6_1.png")));


        temp = new ArrayList<RectF>();
        temp.add(new RectF(0, 0, 0.33f - halfLineWidth, 0.5f - halfLineWidth));
        temp.add(new RectF(0.33f + halfLineWidth, 0, 0.66f - halfLineWidth, 0.5f - halfLineWidth));
        temp.add(new RectF(0.66f + halfLineWidth, 0, 1, 0.5f - halfLineWidth));
        temp.add(new RectF(0, 0.5f + halfLineWidth, 0.25f - halfLineWidth, 1f));
        temp.add(new RectF(0.25f + halfLineWidth, 0.5f + halfLineWidth, 0.5f - halfLineWidth, 1f));
        temp.add(new RectF(0.5f + halfLineWidth, 0.5f + halfLineWidth, 0.75f - halfLineWidth, 1f));
        temp.add(new RectF(0.75f + halfLineWidth, 0.5f + halfLineWidth, 1f, 1f));
        noBorderLine = new ArrayList<RectF>();
        noBorderLine.add(new RectF(0, 0, 0.33f, 0.5f));
        noBorderLine.add(new RectF(0.33f, 0, 0.66f, 0.5f));
        noBorderLine.add(new RectF(0.66f, 0, 1, 0.5f));
        noBorderLine.add(new RectF(0, 0.5f, 0.25f, 1f));
        noBorderLine.add(new RectF(0.25f, 0.5f, 0.5f, 1f));
        noBorderLine.add(new RectF(0.5f, 0.5f, 0.75f, 1f));
        noBorderLine.add(new RectF(0.75f, 0.5f, 1f, 1f));
        listMode.add(new ModeInfo(temp, noBorderLine, R.drawable.mix_icon_7, fixAsset("7_1.png")));


        temp = new ArrayList<RectF>();
        temp.add(new RectF(0, 0, 0.25f - halfLineWidth, 0.5f - halfLineWidth));
        temp.add(new RectF(0.25f + halfLineWidth, 0f, 0.5f - halfLineWidth, 0.5f - halfLineWidth));
        temp.add(new RectF(0.5f + halfLineWidth, 0f, 0.75f - halfLineWidth, 0.5f - halfLineWidth));
        temp.add(new RectF(0.75f + halfLineWidth, 0f, 1f, 0.5f - halfLineWidth));

        temp.add(new RectF(0, 0.5f + halfLineWidth, 0.25f - halfLineWidth, 1f));
        temp.add(new RectF(0.25f + halfLineWidth, 0.5f + halfLineWidth, 0.5f - halfLineWidth, 1f));
        temp.add(new RectF(0.5f + halfLineWidth, 0.5f + halfLineWidth, 0.75f - halfLineWidth, 1f));
        temp.add(new RectF(0.75f + halfLineWidth, 0.5f + halfLineWidth, 1f, 1f));
        noBorderLine = new ArrayList<RectF>();
        noBorderLine.add(new RectF(0, 0, 0.25f, 0.5f));
        noBorderLine.add(new RectF(0.25f, 0f, 0.5f, 0.5f));
        noBorderLine.add(new RectF(0.5f, 0f, 0.75f, 0.5f));
        noBorderLine.add(new RectF(0.75f, 0f, 1f, 0.5f));

        noBorderLine.add(new RectF(0, 0.5f, 0.25f, 1f));
        noBorderLine.add(new RectF(0.25f, 0.5f, 0.5f, 1f));
        noBorderLine.add(new RectF(0.5f, 0.5f, 0.75f, 1f));
        noBorderLine.add(new RectF(0.75f, 0.5f, 1f, 1f));

        listMode.add(new ModeInfo(temp, noBorderLine, R.drawable.mix_icon_8, fixAsset("8_1.png")));

    }

    /**
     * 边框线的宽度
     *
     * @return
     */
    private static float line() {
        return 8.f / 750;
    }


    private static String fixAsset(String fileName) {
        return "asset:///mix/" + fileName;
    }

    /**
     * 通过区域生成一个唯一Id
     *
     * @param mixRect
     * @return
     */
    public static int getRect2Id(RectF mixRect) {
        return ((mixRect.left + "_" + mixRect.top + "_" + mixRect.right + "_" + mixRect.bottom).hashCode());
    }

    /***
     *打开系统图库，选折视频
     * @param context
     * @param requsetCode
     */
    public static void openGallery(Context context, int requsetCode) {

        UIConfiguration.Builder builder = new UIConfiguration.Builder().setMediaCountLimit(1).enableAlbumCamera(false);
        SdkEntry.getSdkService().initConfiguration(null, builder.get());
        Intent intent = new Intent(context,
                com.rd.veuisdk.SelectMediaActivity.class);
        intent.putExtra(SelectMediaActivity.ALBUM_ONLY, true);
        intent.putExtra(SelectMediaActivity.ALBUM_FORMAT_TYPE, UIConfiguration.ALBUM_SUPPORT_VIDEO_ONLY);
        ((Activity) context).startActivityForResult(intent, requsetCode);
    }

    /**
     * 获取视频末尾缩略图
     * 耗时操作
     *
     * @param videoPath
     * @param timeSecond
     * @return
     */
    public static String getLastThumb(String videoPath, float timeSecond) {
        VideoConfig vc = new VideoConfig();
        float duration = VirtualVideo.getMediaInfo(videoPath, vc);
        if (duration > 0) {
            int targetW = vc.getVideoWidth(), targetH = vc.getVideoHeight();
            String snap = PathUtils.getTempFileNameForSdcard(PathUtils.TEMP_THUMBNAIL, "jpg");
            //获取最后一帧数据有bug(待处理)，改为获取倒数第2秒的数据
            if (VirtualVideo.getSnapShot(videoPath, snap, Math.max(0.01f, timeSecond), targetW, targetH)) {
                return snap;
            }
        }
        return null;
    }

    private static final String TAG = "ModeUtils";

    /**
     * @param srcWidth
     * @param srcHeight
     */
    public static RectF getFixOutSize(int srcWidth, int srcHeight) {
        RectF out;
        final int MAXSIZE = 480;
        float aspectRatio = srcWidth / (srcHeight + 0.0f);

        Log.e(TAG, "getFixOutSize: " + srcWidth + "*" + srcHeight + ",,,,,>" + aspectRatio);
        //获取小图片 (最大MAXSIZE*MAXSIZE)
        if (Math.max(srcWidth, srcHeight) > MAXSIZE) {

            //缩小到MAXSIZE
            if (aspectRatio > 1) {
                out = new RectF(0, 0, MAXSIZE, MAXSIZE / aspectRatio);
            } else {
                out = new RectF(0, 0, (MAXSIZE * aspectRatio), MAXSIZE);
            }
        } else {
            int targetW = aspectRatio > 1 ? srcWidth : (int) (aspectRatio * srcHeight);
            out = new RectF(0, 0, targetW, targetW / aspectRatio);
        }
        return out;
    }


    /**
     * 进入裁剪
     *
     * @param context
     * @param filePath
     * @param requestCode
     * @param aspRatio
     * @param onlyTrimLine 是否自定义截取帧范围的数据（true不做clip， false 支持clip裁剪）
     */
    public static void gotoTrim(Context context, String filePath, int requestCode, float aspRatio, boolean onlyTrimLine) {

        Scene scene = VirtualVideo.createScene();
        try {
            scene.addMedia(filePath);
            Log.e(TAG, "gotoTrim: " + aspRatio);
            gotoTrim(context, scene, requestCode, aspRatio, onlyTrimLine);
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        }

    }

    public static void gotoTrim(Context context, Scene scene, int requestCode, float aspRatio, boolean onlyTrimLine) {

        SdkEntry.getSdkService()
                .initTrimConfiguration(new TrimConfiguration.Builder()
                        // 设置默认裁剪区域为1:1
                        .setDefault1x1CropMode(true)
                        // 设置是否显示1:1裁剪按钮
                        .enable1x1(true)
                        // 设置截取返回类型
                        .setTrimReturnMode(TrimConfiguration.TRIM_RETURN_MEDIA)
                        // 设置截取类型
                        .setTrimType(TrimConfiguration.TRIM_TYPE_FREE)
                        // 设置两定长截取时间
                        .setTrimDuration(0, 0)
                        // 设置单个定长截取时间
                        .setTrimDuration(0)
                        //保存到临时文件夹
                        .setSavePath(PathUtils.getRdTempPath())
                        .get());


        Intent intent = new Intent(context,
                com.rd.veuisdk.TrimMediaActivity.class);
        intent.putExtra(IntentConstants.INTENT_EXTRA_SCENE, scene);
        intent.putExtra(IntentConstants.TRIM_FROM_EDIT, onlyTrimLine ? true : false);
        intent.putExtra(TrimMediaActivity
                .RESULT_DATA, true);
        intent.putExtra(TrimMediaActivity
                .ONLYLINE, onlyTrimLine);
        intent.putExtra(TrimMediaActivity
                .CROP_ASPECTRATIO, aspRatio);
        ((Activity) context).startActivityForResult(intent, requestCode);
    }

    /**
     * 获取视频时长，单位:秒
     *
     * @param videoPath
     * @return
     */
    public static float getDuration(String videoPath) {
        if (!TextUtils.isEmpty(videoPath)) {
            return VirtualVideo.getMediaInfo(videoPath, null);
        }
        return 0;
    }


    /**
     * 根据视频的 显示位置，等比例的裁剪出需要保留的视频区域（相对于原始视频的尺寸）
     *
     * @param nSrcWidth       视频原始宽
     * @param nSrcHeight      视频原始高
     * @param fDstAspectRatio 视频显示位置的宽高比
     * @param rectCliped      视频要保留的裁剪部分
     * @param poff            上下、左右偏移(-1.0f<---->1.0f)
     */
    public static void getClipSrc(int nSrcWidth, int nSrcHeight, float fDstAspectRatio, Rect rectCliped, float poff) {


        //居中裁剪
        MediaObject.getClipSrcRect(nSrcWidth, nSrcHeight, fDstAspectRatio, rectCliped);
//        Log.e(TAG, "getClipSrc: " + rectCliped.toShortString());
        //计算偏移
        int dx = (int) (poff * ((nSrcWidth - rectCliped.width()) / 2));
        int dy = (int) (poff * ((nSrcHeight - rectCliped.height()) / 2));
        rectCliped.offset(dx, dy);
//        Log.e(TAG, fDstAspectRatio + "poff  getClipSrc: " + rectCliped.toShortString());


    }


    /**
     * 根据画框比例获取录制视频输出时的尺寸
     *
     * @param videoConfig
     * @param vAspRatio
     */
    public static void fixRecordOutSize(VideoConfig videoConfig, float vAspRatio) {
        int MAXSIZE = 480;
        int targetW = MAXSIZE;
        int targetH = MAXSIZE;
        if (vAspRatio > 1f) {
            targetW = MAXSIZE;
            targetH = MiscUtils.alignValue((int) (targetW / vAspRatio), 2);
        } else {
            targetH = MAXSIZE;
            targetW = MiscUtils.alignValue((int) (targetH * vAspRatio), 16);
        }
        videoConfig.setVideoSize(targetW, targetH);
    }

    /**
     * 为目标分辨率，设置匹配的码率
     *
     * @param targetW
     * @param targetH
     */
    public static int getRecordBit(int targetW, int targetH) {
        int max = 368 * 640;
        int bite = 4000 * 1000;
        int currentSize = targetW * targetH;

        if (currentSize < 200 * 300) {
            bite = 2800 * 1000;
        } else if (currentSize < 300 * 400) {
            bite = 3000 * 1000;
        } else if (currentSize < 480 * 480) {
            bite = 3500 * 1000;
        }
        return bite;
    }

}
