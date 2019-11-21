package com.rd.veuisdk.utils;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.MediaStore.Video;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.rd.lib.utils.CoreUtils;
import com.rd.lib.utils.FileUtils;
import com.rd.vecore.VirtualVideo;
import com.rd.vecore.VirtualVideoView;
import com.rd.vecore.exception.InvalidArgumentException;
import com.rd.vecore.models.DewatermarkObject;
import com.rd.vecore.models.MediaObject;
import com.rd.vecore.models.Scene;
import com.rd.vecore.models.VideoConfig;
import com.rd.vecore.models.VisualFilterConfig;
import com.rd.vecore.models.caption.CaptionObject;
import com.rd.vecore.utils.MiscUtils;
import com.rd.veuisdk.R;
import com.rd.veuisdk.TempVideoParams;
import com.rd.veuisdk.export.StickerExportHandler;
import com.rd.veuisdk.listener.IFixPreviewListener;
import com.rd.veuisdk.model.CollageInfo;
import com.rd.veuisdk.model.GraffitiInfo;
import com.rd.veuisdk.model.MOInfo;
import com.rd.veuisdk.model.StickerInfo;
import com.rd.veuisdk.model.StyleInfo;
import com.rd.veuisdk.model.WordInfo;
import com.rd.veuisdk.net.StickerUtils;
import com.rd.veuisdk.net.SubUtils;
import com.rd.veuisdk.ui.ExtAdvancedProgressDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;

public class Utils {

    /**
     * 视频缩略图缓冲目录
     */
    public static final String VIDEO_THUMBNAIL_CACHE_DIR = "video_thumbnails";

    /**
     * 初始Utils
     */
    public static void initialize(Context context, String strRootDirPath) {
        PathUtils.initialize(context, TextUtils.isEmpty(strRootDirPath) ? null
                : new File(strRootDirPath));
        CoreUtils.init(context);
        FileLog.setLogPath(PathUtils.getRdLogPath());
        ThumbNailUtils.getInstance(context);
    }

    /**
     * 查找容器范围内的组件
     */
    public static <T extends View> T $(View mRoot, int resId) {
        return mRoot.findViewById(resId);
    }

    /**
     * 获取某个activity根View
     *
     * @param activity 某个activity
     * @return 根View
     */
    public static View getRootView(Activity activity) {
        return ((ViewGroup) activity.findViewById(android.R.id.content))
                .getChildAt(0);

    }

    public static void autoToastNomal(Context c, String msg) {
        SysAlertDialog.showAutoHideDialog(c, null, msg, Toast.LENGTH_SHORT);
    }

    public static void autoToastNomal(Context c, int msgId) {
        autoToastNomal(c, c.getString(msgId));

    }

    /**
     * 高级进度条
     */
    public static ExtAdvancedProgressDialog showAdvancedProgressDialog(
            Context context, String message, boolean indeterminate,
            boolean cancelable, OnCancelListener cancelListener) {

        ExtAdvancedProgressDialog advancedDialog = new ExtAdvancedProgressDialog(
                context);
        advancedDialog.setMessage(message);
        advancedDialog.setIndeterminate(indeterminate);
        advancedDialog.setCancelable(cancelable);
        advancedDialog.setOnCancelListener(cancelListener);
        advancedDialog.show();
        return advancedDialog;
    }

    /**
     * 字幕的id
     *
     * @return id
     */
    public static int getWordId() {
        String time = String.valueOf(System.currentTimeMillis());
        int len = time.length();
        return Integer.parseInt(time.substring(len - 6, len));
    }

    /**
     * 查找是否受支持的项目
     */
    public static boolean isSupported(String value, List<String> supported) {
        return supported != null && supported.indexOf(value) >= 0;
    }

    public static int getDisplayRotation(Activity activity) {
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        switch (rotation) {
            case Surface.ROTATION_0:
                return 0;
            case Surface.ROTATION_90:
                return 90;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_270:
                return 270;
        }
        return 0;
    }

    public static final int ORIENTATION_HYSTERESIS = 5;

    /**
     * 根据需要设置的方向角度和当前(旧的)方向角度,计算出合适的新的角度
     *
     * @param orientation        方向角度
     * @param orientationHistory 方向角度历史
     * @return 新的角度
     */
    public static int roundOrientation(int orientation, int orientationHistory) {
        boolean changeOrientation = false;
        if (orientationHistory == OrientationEventListener.ORIENTATION_UNKNOWN) {
            changeOrientation = true;
        } else {
            int dist = Math.abs(orientation - orientationHistory);
            dist = Math.min(dist, 360 - dist);
            changeOrientation = (dist >= 45 + ORIENTATION_HYSTERESIS);
        }
        if (changeOrientation) {
            return ((orientation + 45) / 90 * 90) % 360;
        } else {
            return orientationHistory;
        }
    }

    /**
     * 清理临时文件
     *
     * @param strTmpFilePath 临时文件目录
     */
    public static void cleanTempFile(String strTmpFilePath) {
        if (!TextUtils.isEmpty(strTmpFilePath)) {
            File fTmp = new File(strTmpFilePath);
            if (fTmp.exists()) {
                fTmp.delete();
            }
        }
    }

    private static final StringBuilder m_sbFormator = new StringBuilder();
    private static final Formatter m_formatter = new Formatter(m_sbFormator,
            Locale.getDefault());

    /**
     * 毫秒数转换为时间格式化字符串
     */
    public static String stringForTime(long timeMs) {
        return stringForTime(timeMs, false, false);
    }

    /**
     * 毫秒数转换为时间格式化字符串 existsHours支持是否显示小时,existsMs支持是否显示毫秒
     */
    public static String stringForTime(long timeMs, boolean existsHours,
                                       boolean existsMs) {
        int totalSeconds = (int) (timeMs / 1000);
        int ms = (int) (timeMs % 1000);
        ms = ms / 100;
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        m_sbFormator.setLength(0);
        if (hours > 0 || existsHours) {
            if (existsMs) {
                return m_formatter.format("%02d:%02d:%02d.%1d", hours, minutes,
                        seconds, ms).toString();
            } else {
                return m_formatter.format("%02d:%02d:%02d", hours, minutes,
                        seconds).toString();
            }

        } else {
            if (existsMs) {
                return m_formatter
                        .format("%02d:%02d.%1d", minutes, seconds, ms)
                        .toString();
            } else {
                return m_formatter.format("%02d:%02d", minutes, seconds)
                        .toString();
            }

        }
    }

    private static boolean m_sSupportExpandEffects = false,
            m_bUseInternalRecorder;

    /**
     * 使用App内置录制
     */
    public static boolean isUseInternalRecorder() {
        return m_bUseInternalRecorder;
    }

    public static boolean m_bNoMP4Metadata = false;

    /**
     * 设置是否允许设置MP4元数据
     */
    public static void setCanWriteMP4Metadata(boolean bSetValue) {
        m_bNoMP4Metadata = bSetValue;
    }

    /**
     * 获取是否允许设置MP4元数据
     */
    public static boolean isCanWriteMp4Metadata() {
        return m_bNoMP4Metadata;
    }

    /**
     * 获取是否支持扩展特效滤镜
     */
    public static boolean getSupportExpandEffects() {
        return m_sSupportExpandEffects;
    }

    /**
     * 调转到应用权限设置
     */

    public static void gotoAppInfo(Context context, String packagename) {
        try {
            Uri packageURI = Uri.parse("package:" + packagename);
            Intent intent = new Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断是否存在虚拟导航键
     */
    public static boolean checkDeviceHasNavigationBar(Context context) {
        return CoreUtils.checkDeviceVirtualBar(context);
    }

    /**
     * 时长单位的转换 秒->毫秒
     */
    public static int s2ms(float s) {
        return (int) (s * 1000);
    }

    /**
     * 时长单位的转换 毫秒->秒
     */
    public static float ms2s(int ms) {
        return (ms / 1000.0f);
    }

    /**
     * 时长单位的转换 毫秒->秒
     *
     * @param ms 时长单位的转换 毫秒->秒
     */
    public static float ms2s(long ms) {
        return (ms / 1000.0f);
    }

    /**
     * 保存到系统图库
     *
     * @param context
     * @param path
     * @param duration
     * @param width
     * @param height
     */
    public static void insertToGallery(Context context, String path, int duration, int width,
                                       int height) {
        ContentValues videoValues = new ContentValues();
        String artist = context.getString(R.string.app_name);
        videoValues.put(Video.Media.TITLE, artist);
        videoValues.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
        videoValues.put(MediaStore.Video.Media.DATA, path);
        videoValues.put(MediaStore.Video.Media.ARTIST, artist);
        videoValues.put(Video.Media.DATE_TAKEN,
                String.valueOf(System.currentTimeMillis()));
        videoValues.put(Video.Media.DESCRIPTION, artist);
        videoValues.put(Video.Media.DURATION, duration);
        videoValues.put(Video.Media.WIDTH, width);
        videoValues.put(Video.Media.HEIGHT, height);
        context.getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, videoValues);
    }

    private static final String TAG = "Utils";

    /**
     * @param config
     * @param mMediaParamImp
     * @return
     */
    private static VisualFilterConfig onConfig(VisualFilterConfig config, IMediaParamImp mMediaParamImp) {
        if (null == config) {
            config = new VisualFilterConfig(VisualFilterConfig.FILTER_ID_NORMAL);
        }
        config.setBrightness(mMediaParamImp.getBrightness());
        config.setContrast(mMediaParamImp.getContrast());
        config.setSaturation(mMediaParamImp.getSaturation());
        config.setSharpen(mMediaParamImp.getSharpen());
        config.setWhiteBalance(mMediaParamImp.getWhite());
        return config;

    }

    /**
     * 当前媒体在片段编辑中，绑定的滤镜 、调色等信息
     */
    public static List<VisualFilterConfig> getFilterList(IMediaParamImp mediaParamImp) {
        ArrayList<VisualFilterConfig> configs = new ArrayList<>();

        if (null != mediaParamImp) {
            float sharpen = Float.NaN;
            VisualFilterConfig config = null;
            if (null != mediaParamImp.getLookupConfig()) {
                //片段编辑lookup
                config = mediaParamImp.getLookupConfig();
                sharpen = config.getSharpen();
                config.resetParams();
            } else {
                config = new VisualFilterConfig(mediaParamImp.getCurrentFilterType());
            }

            config = onConfig(config, mediaParamImp);
            //程度
            if (!Float.isNaN(sharpen)) {
                config.setSharpen(sharpen);
            }
            configs.add(config);

            if (mediaParamImp.getVignetteId() != IMediaParamImp.NO_VIGNETTEDID) {
                //暗角有效
                VisualFilterConfig vignetted = new VisualFilterConfig(VisualFilterConfig.FILTER_ID_VIGNETTE);
                vignetted.setDefaultValue(mediaParamImp.getVignette());
                configs.add(vignetted);
            }
        }
        return configs;
    }


    /**
     * 构建媒体的缩率图
     *
     * @param context
     * @param mediaObject
     * @return
     */
    public static String fixThumb(@NonNull Context context, @NonNull MediaObject mediaObject) {

        String path = PathUtils.getTempFileNameForSdcard("Temp_imix_thumb", ".png");
        VirtualVideo virtualVideo = new VirtualVideo();
        Scene scene = VirtualVideo.createScene();
        scene.addMedia(mediaObject);
        virtualVideo.addScene(scene);
        Rect targetRect = new Rect();
        MiscUtils.fixZoomTarget(mediaObject.getWidth(), mediaObject.getHeight(), targetRect, 400);
        Bitmap bmp = Bitmap.createBitmap(targetRect.width(), targetRect.height(), Bitmap.Config.ARGB_8888);
        boolean re = virtualVideo.getSnapshot(context, 1f, bmp);
        if (re) {
            BitmapUtils.saveBitmapToFile(bmp, path, true);
        } else {
            path = null;
        }
        bmp.recycle();
        virtualVideo.release();
        return path;


    }

    /**
     * 退出编辑时，判断数据是否有变化，提示是否退出
     *
     * @param collageInfos 新的
     * @param dbList
     * @return
     */
    public static boolean isEqualsMixList(List<CollageInfo> collageInfos, List<CollageInfo> dbList) {
        int len = collageInfos.size();
        if (len != dbList.size()) {
            return false;
        }
        boolean isequals = true;
        CollageInfo info;
        for (int i = 0; i < len; i++) {
            info = collageInfos.get(i);
            if (!info.equals(dbList.get(i))) {
                isequals = false;
                break;
            }
        }
        return isequals;
    }

    private static final float ASP_43 = 4 / 3.0f;
    private static final float ASP_34 = 3 / 4.0f;


    /***
     * 根据比例获取一个预览尺寸
     * @param asp
     * @param rectF  获取显示像素
     */
    private static void getPreviewSizeByAsp(float asp, RectF rectF) {
        //640 无真实的意义（640仅参与运算，可以是 >100的任意数），最终绑定的依然是rectF（0~1.0f）
        if (asp >= ASP_43) {
            rectF.set(0, 0, 640, (640 / asp));
        } else if (asp <= ASP_34) {
            rectF.set(0, 0, 640 * asp, 640);
        } else {
            rectF.set(0, 0, 640, 640);
        }

    }

    /**
     * @param srcPreviewAsp 原始的预览比例
     * @param newPreviewAsp 新的预览比例
     * @param rectF         原始的显示位置
     * @return 新的显示位置
     */
    private static RectF fixPreviewRect(float srcPreviewAsp, float newPreviewAsp, RectF rectF) {
        //预览比例变化了，需要修正位置
        RectF previewRectF = new RectF();
        getPreviewSizeByAsp(srcPreviewAsp, previewRectF);
        float width = previewRectF.width();
        float height = previewRectF.height();

        float centerX = rectF.centerX(), centerY = rectF.centerY();

        //转化为像素
        rectF.left *= width;
        rectF.top *= height;

        rectF.right *= width;
        rectF.bottom *= height;


        //新的预览size
        previewRectF = new RectF();
        getPreviewSizeByAsp(newPreviewAsp, previewRectF);
        float previewWidth = previewRectF.width();
        float previewHeight = previewRectF.height();


        //中心点（像素）
        float centerXPx = centerX * previewWidth;
        float centerYPx = centerY * previewHeight;
        //中心点
        RectF showRectF = new RectF(centerXPx, centerYPx, centerXPx, centerYPx);


        showRectF.inset(-(rectF.width() / 2.0f), -(rectF.height() / 2.0f));


//        Log.e(TAG, "fixPreviewRect:  dst:" + showRectF);


        {
            //防止越界到预览区域（previewWidth*previewHeight）外
            if (showRectF.left < 0) {
                showRectF.offset(-showRectF.left, 0);
            }
            if (showRectF.top < 0) {
                showRectF.offset(0, -showRectF.top);
            }
            if (showRectF.right > previewWidth) {
                showRectF.offset(previewWidth - showRectF.right, 0);
            }
            if (showRectF.bottom > previewHeight) {
                showRectF.offset(previewHeight - showRectF.bottom, 0);
            }
        }

        showRectF.left /= previewWidth;
        showRectF.top /= previewHeight;
        showRectF.right /= previewWidth;
        showRectF.bottom /= previewHeight;


        return showRectF;

    }

    /**
     * 预览size改变，调整水印位置，依据：（保证水印的centerF不变 ，区域的size不变，只是平移了而已）
     *
     * @param srcPreviewAsp 原始比例
     * @param newPreviewAsp 新的比例
     * @param collageInfos  画中画
     */
    private static void fixCollage(float srcPreviewAsp, float newPreviewAsp, final List<CollageInfo> collageInfos) {
        if (srcPreviewAsp != newPreviewAsp && null != collageInfos) {
            //预览比例变化了，需要修正画中画位置
            int len = collageInfos.size();
            for (int i = 0; i < len; i++) {
                CollageInfo collageInfo = collageInfos.get(i);
                RectF showRectF = fixPreviewRect(srcPreviewAsp, newPreviewAsp, new RectF(collageInfo.getMediaObject().getShowRectF()));
                collageInfo.getMediaObject().setShowRectF(showRectF);
            }
            TempVideoParams.getInstance().setCollageList(collageInfos);
        }
    }


    /**
     * 修正去水印|马赛克显示位置
     *
     * @param srcPreviewAsp 原始的预览size
     * @param newPreviewAsp 新的预览size
     * @param list          要change的列表
     * @param width         新的预览宽
     * @param height        新的预览高
     */
    private static void fixMO(float srcPreviewAsp, float newPreviewAsp, List<MOInfo> list, int width, int height) {
        if (srcPreviewAsp != newPreviewAsp && null != list) {
            //预览比例变化了，需要修正 去水印|马赛克位置
            int len = list.size();
            ArrayList<MOInfo> tmp = new ArrayList<>();
            for (int i = 0; i < len; i++) {
                MOInfo moInfo = list.get(i);
                RectF showRectF = fixPreviewRect(srcPreviewAsp, newPreviewAsp, new RectF(moInfo.getShowRectF()));
                moInfo.setShowRectF(showRectF);
                if (moInfo.getObject().getType() == DewatermarkObject.Type.mosaic||moInfo.getObject().getType()== DewatermarkObject.Type.blur) {
                    //主动修正容器大小
                    moInfo.getObject().setParentSize(width, height);
                    try {
                        //重新应用生成jni对象
                        moInfo.getObject().apply(false);
                    } catch (InvalidArgumentException e) {
                        e.printStackTrace();
                    }
                }
                tmp.add(moInfo);
            }
            TempVideoParams.getInstance().setMosaics(tmp);
        }
    }


    /**
     * 根据新的宽高，重新修正config
     *
     * @param tmp
     */
    private static void fixConfig(ArrayList<StyleInfo> tmp) {
        if (null != tmp && tmp.size() > 0) {
            for (StyleInfo item : tmp) {
                String file = item.mlocalpath;
                if (!TextUtils.isEmpty(file) && FileUtils.isExist(file) && item.isdownloaded) {
                    CommonStyleUtils.checkStyle(new File(item.mlocalpath), item);
                }
            }
        }
    }

    /**
     * 修正要调整位置的资源 （字幕、贴纸、水印、 画中画）
     *
     * @param oldPreviewAsp      原始预览比例
     * @param newWidth           新的宽、高
     * @param newHeight
     * @param collageInfos       画中画
     * @param fixPreviewListener
     * @param playerWidth        字幕、贴纸的容器的宽高
     * @param playerHeight
     */
    public static void onFixPreviewDataSource(float oldPreviewAsp, int newWidth, int newHeight, List<CollageInfo> collageInfos,
                                              final IFixPreviewListener fixPreviewListener, int playerWidth, int playerHeight, VirtualVideo virtualVideo, VirtualVideoView player) {
        float asp = newWidth / (newHeight + 0.0f);

        //修正画中画比例
        fixCollage(oldPreviewAsp, asp, collageInfos);
        {
            //马赛克|去水印
            Utils.fixMO(oldPreviewAsp, asp, TempVideoParams.getInstance().getMosaicDuraionChecked(), newWidth, newHeight);
        }
        {
            //容器的size
            CommonStyleUtils.init(playerWidth, playerHeight);
            {
                //根据新的宽高，重新修正字幕config
                fixConfig(SubUtils.getInstance().getDBStyleInfos());
                fixConfig(SubUtils.getInstance().getStyleInfos());
            }
            //修正已经存在的字幕
            ArrayList<WordInfo> wordInfos = TempVideoParams.getInstance().getWordInfos();
            int size = wordInfos.size();
            for (int i = 0; i < size; i++) {
                WordInfo info = wordInfos.get(i);
                CaptionObject captionObject = info.getCaptionObject();
                try {
                    captionObject.setVirtualVideo(virtualVideo, player);
                } catch (InvalidArgumentException e) {
                    e.printStackTrace();
                }

                StyleInfo styleInfo = SubUtils.getInstance().getStyleInfo(info.getStyleId());
                if (null != styleInfo) {
                    //修正默认显示比例
                    captionObject.updateStyleDisf(styleInfo.disf);
                }
                //主动修正字幕容器大小
                captionObject.setParentSize(playerWidth, playerHeight);
                try {
                    //重新应用生成jni对象
                    captionObject.apply(false);
                } catch (InvalidArgumentException e) {
                    e.printStackTrace();
                }
            }
        }
        {
            //贴纸
            {
                ArrayList<StyleInfo> tmp = StickerUtils.getInstance().getDBStyleInfos();
                if (null == tmp || tmp.size() == 0) { //防止从草稿箱->编辑，没有进入贴纸
                    //查询已下载的贴纸
                    StickerUtils.getInstance().getStyleDownloaded(true);
                }
                //根据新的宽高，重新修正贴纸config
                fixConfig(StickerUtils.getInstance().getStyleInfos());
                fixConfig(StickerUtils.getInstance().getDBStyleInfos());
            }
            //改变播放器大小，修正贴纸
            ArrayList<StickerInfo> list = TempVideoParams.getInstance().getRSpecialInfos();
            new StickerExportHandler(player.getContext(), list, playerWidth, playerHeight).export(null);
            if (null != fixPreviewListener) {
                fixPreviewListener.onComplete();
            }
        }
    }

    /**
     * 判断当前点击的坐标，是否在字幕预览框内
     *
     * @param mListPointF 字幕预览框
     * @param fx
     * @param fy
     * @return
     */
    public static boolean isContains(List<PointF> mListPointF, float fx, float fy) {
        if (null != mListPointF && mListPointF.size() == 4) {
            RectF r = new RectF();
            Path path = new Path();
            path.moveTo(mListPointF.get(0).x, mListPointF.get(0).y);
            path.lineTo(mListPointF.get(1).x, mListPointF.get(1).y);
            path.lineTo(mListPointF.get(2).x, mListPointF.get(2).y);
            path.lineTo(mListPointF.get(3).x, mListPointF.get(3).y);
            path.close();
            path.computeBounds(r, true);
            Region region = new Region();
            region.setPath(path, new Region((int) r.left, (int) r.top, (int) r.right, (int) r.bottom));
            return region.contains((int) fx, (int) fy);
        }
        return false;
    }


    /**
     * 涂鸦是否一致
     *
     * @param backup 原始涂鸦列表
     * @param list   新的涂鸦列表
     * @return true 涂鸦未改变；false 涂鸦有变化
     */
    public static boolean isEquals(@Nullable List<GraffitiInfo> backup, @NonNull List<GraffitiInfo> list) {
        boolean isEquals = true;
        if (null == backup && list.size() == 0) {
            return true;
        }
        if ((null != backup) && backup.size() == list.size()) {
            int len = backup.size();
            for (int i = 0; i < len; i++) {
                if (!backup.get(i).equals(list.get(i))) {
                    isEquals = false;
                    break;
                }
            }
        } else {
            isEquals = false;
        }
        return isEquals;

    }

    /**
     * 旋转90、270 且镜像时，要修正宽高
     *
     * @param vc
     * @param mMedia
     */
    public static void fixVideoSize(VideoConfig vc, MediaObject mMedia) {
        VirtualVideo.getMediaInfo(mMedia.getMediaPath(), vc);
        int tmpW = vc.getVideoWidth();
        int tmpH = vc.getVideoHeight();
        if (mMedia.getAngle() == 90 || mMedia.getAngle() == 270) {
            int tmp = tmpW;
            tmpW = tmpH;
            tmpH = tmp;
        }
        //旋转90、270 且镜像时，要修正宽高
        vc.setVideoSize(tmpW, tmpH);
    }

}
