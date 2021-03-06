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
     * ???????????????????????????
     */
    public static final String VIDEO_THUMBNAIL_CACHE_DIR = "video_thumbnails";

    /**
     * ??????Utils
     */
    public static void initialize(Context context, String strRootDirPath) {
        PathUtils.initialize(context, TextUtils.isEmpty(strRootDirPath) ? null
                : new File(strRootDirPath));
        CoreUtils.init(context);
        FileLog.setLogPath(PathUtils.getRdLogPath());
        ThumbNailUtils.getInstance(context);
    }

    /**
     * ??????????????????????????????
     */
    public static <T extends View> T $(View mRoot, int resId) {
        return mRoot.findViewById(resId);
    }

    /**
     * ????????????activity???View
     *
     * @param activity ??????activity
     * @return ???View
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
     * ???????????????
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
     * ?????????id
     *
     * @return id
     */
    public static int getWordId() {
        String time = String.valueOf(System.currentTimeMillis());
        int len = time.length();
        return Integer.parseInt(time.substring(len - 6, len));
    }

    /**
     * ??????????????????????????????
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
     * ??????????????????????????????????????????(??????)????????????,??????????????????????????????
     *
     * @param orientation        ????????????
     * @param orientationHistory ??????????????????
     * @return ????????????
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
     * ??????????????????
     *
     * @param strTmpFilePath ??????????????????
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
     * ??????????????????????????????????????????
     */
    public static String stringForTime(long timeMs) {
        return stringForTime(timeMs, false, false);
    }

    /**
     * ?????????????????????????????????????????? existsHours????????????????????????,existsMs????????????????????????
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
     * ??????App????????????
     */
    public static boolean isUseInternalRecorder() {
        return m_bUseInternalRecorder;
    }

    public static boolean m_bNoMP4Metadata = false;

    /**
     * ????????????????????????MP4?????????
     */
    public static void setCanWriteMP4Metadata(boolean bSetValue) {
        m_bNoMP4Metadata = bSetValue;
    }

    /**
     * ????????????????????????MP4?????????
     */
    public static boolean isCanWriteMp4Metadata() {
        return m_bNoMP4Metadata;
    }

    /**
     * ????????????????????????????????????
     */
    public static boolean getSupportExpandEffects() {
        return m_sSupportExpandEffects;
    }

    /**
     * ???????????????????????????
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
     * ?????????????????????????????????
     */
    public static boolean checkDeviceHasNavigationBar(Context context) {
        return CoreUtils.checkDeviceVirtualBar(context);
    }

    /**
     * ????????????????????? ???->??????
     */
    public static int s2ms(float s) {
        return (int) (s * 1000);
    }

    /**
     * ????????????????????? ??????->???
     */
    public static float ms2s(int ms) {
        return (ms / 1000.0f);
    }

    /**
     * ????????????????????? ??????->???
     *
     * @param ms ????????????????????? ??????->???
     */
    public static float ms2s(long ms) {
        return (ms / 1000.0f);
    }

    /**
     * ?????????????????????
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
     * ???????????????????????????????????????????????? ??????????????????
     */
    public static List<VisualFilterConfig> getFilterList(IMediaParamImp mediaParamImp) {
        ArrayList<VisualFilterConfig> configs = new ArrayList<>();

        if (null != mediaParamImp) {
            float sharpen = Float.NaN;
            VisualFilterConfig config = null;
            if (null != mediaParamImp.getLookupConfig()) {
                //????????????lookup
                config = mediaParamImp.getLookupConfig();
                sharpen = config.getSharpen();
                config.resetParams();
            } else {
                config = new VisualFilterConfig(mediaParamImp.getCurrentFilterType());
            }

            config = onConfig(config, mediaParamImp);
            //??????
            if (!Float.isNaN(sharpen)) {
                config.setSharpen(sharpen);
            }
            configs.add(config);

            if (mediaParamImp.getVignetteId() != IMediaParamImp.NO_VIGNETTEDID) {
                //????????????
                VisualFilterConfig vignetted = new VisualFilterConfig(VisualFilterConfig.FILTER_ID_VIGNETTE);
                vignetted.setDefaultValue(mediaParamImp.getVignette());
                configs.add(vignetted);
            }
        }
        return configs;
    }


    /**
     * ????????????????????????
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
     * ??????????????????????????????????????????????????????????????????
     *
     * @param collageInfos ??????
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
     * ????????????????????????????????????
     * @param asp
     * @param rectF  ??????????????????
     */
    private static void getPreviewSizeByAsp(float asp, RectF rectF) {
        //640 ?????????????????????640??????????????????????????? >100??????????????????????????????????????????rectF???0~1.0f???
        if (asp >= ASP_43) {
            rectF.set(0, 0, 640, (640 / asp));
        } else if (asp <= ASP_34) {
            rectF.set(0, 0, 640 * asp, 640);
        } else {
            rectF.set(0, 0, 640, 640);
        }

    }

    /**
     * @param srcPreviewAsp ?????????????????????
     * @param newPreviewAsp ??????????????????
     * @param rectF         ?????????????????????
     * @return ??????????????????
     */
    private static RectF fixPreviewRect(float srcPreviewAsp, float newPreviewAsp, RectF rectF) {
        //??????????????????????????????????????????
        RectF previewRectF = new RectF();
        getPreviewSizeByAsp(srcPreviewAsp, previewRectF);
        float width = previewRectF.width();
        float height = previewRectF.height();

        float centerX = rectF.centerX(), centerY = rectF.centerY();

        //???????????????
        rectF.left *= width;
        rectF.top *= height;

        rectF.right *= width;
        rectF.bottom *= height;


        //????????????size
        previewRectF = new RectF();
        getPreviewSizeByAsp(newPreviewAsp, previewRectF);
        float previewWidth = previewRectF.width();
        float previewHeight = previewRectF.height();


        //?????????????????????
        float centerXPx = centerX * previewWidth;
        float centerYPx = centerY * previewHeight;
        //?????????
        RectF showRectF = new RectF(centerXPx, centerYPx, centerXPx, centerYPx);


        showRectF.inset(-(rectF.width() / 2.0f), -(rectF.height() / 2.0f));


//        Log.e(TAG, "fixPreviewRect:  dst:" + showRectF);


        {
            //??????????????????????????????previewWidth*previewHeight??????
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
     * ??????size?????????????????????????????????????????????????????????centerF?????? ????????????size?????????????????????????????????
     *
     * @param srcPreviewAsp ????????????
     * @param newPreviewAsp ????????????
     * @param collageInfos  ?????????
     */
    private static void fixCollage(float srcPreviewAsp, float newPreviewAsp, final List<CollageInfo> collageInfos) {
        if (srcPreviewAsp != newPreviewAsp && null != collageInfos) {
            //???????????????????????????????????????????????????
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
     * ???????????????|?????????????????????
     *
     * @param srcPreviewAsp ???????????????size
     * @param newPreviewAsp ????????????size
     * @param list          ???change?????????
     * @param width         ???????????????
     * @param height        ???????????????
     */
    private static void fixMO(float srcPreviewAsp, float newPreviewAsp, List<MOInfo> list, int width, int height) {
        if (srcPreviewAsp != newPreviewAsp && null != list) {
            //???????????????????????????????????? ?????????|???????????????
            int len = list.size();
            ArrayList<MOInfo> tmp = new ArrayList<>();
            for (int i = 0; i < len; i++) {
                MOInfo moInfo = list.get(i);
                RectF showRectF = fixPreviewRect(srcPreviewAsp, newPreviewAsp, new RectF(moInfo.getShowRectF()));
                moInfo.setShowRectF(showRectF);
                if (moInfo.getObject().getType() == DewatermarkObject.Type.mosaic||moInfo.getObject().getType()== DewatermarkObject.Type.blur) {
                    //????????????????????????
                    moInfo.getObject().setParentSize(width, height);
                    try {
                        //??????????????????jni??????
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
     * ?????????????????????????????????config
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
     * ?????????????????????????????? ?????????????????????????????? ????????????
     *
     * @param oldPreviewAsp      ??????????????????
     * @param newWidth           ???????????????
     * @param newHeight
     * @param collageInfos       ?????????
     * @param fixPreviewListener
     * @param playerWidth        ?????????????????????????????????
     * @param playerHeight
     */
    public static void onFixPreviewDataSource(float oldPreviewAsp, int newWidth, int newHeight, List<CollageInfo> collageInfos,
                                              final IFixPreviewListener fixPreviewListener, int playerWidth, int playerHeight, VirtualVideo virtualVideo, VirtualVideoView player) {
        float asp = newWidth / (newHeight + 0.0f);

        //?????????????????????
        fixCollage(oldPreviewAsp, asp, collageInfos);
        {
            //?????????|?????????
            Utils.fixMO(oldPreviewAsp, asp, TempVideoParams.getInstance().getMosaicDuraionChecked(), newWidth, newHeight);
        }
        {
            //?????????size
            CommonStyleUtils.init(playerWidth, playerHeight);
            {
                //???????????????????????????????????????config
                fixConfig(SubUtils.getInstance().getDBStyleInfos());
                fixConfig(SubUtils.getInstance().getStyleInfos());
            }
            //???????????????????????????
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
                    //????????????????????????
                    captionObject.updateStyleDisf(styleInfo.disf);
                }
                //??????????????????????????????
                captionObject.setParentSize(playerWidth, playerHeight);
                try {
                    //??????????????????jni??????
                    captionObject.apply(false);
                } catch (InvalidArgumentException e) {
                    e.printStackTrace();
                }
            }
        }
        {
            //??????
            {
                ArrayList<StyleInfo> tmp = StickerUtils.getInstance().getDBStyleInfos();
                if (null == tmp || tmp.size() == 0) { //??????????????????->???????????????????????????
                    //????????????????????????
                    StickerUtils.getInstance().getStyleDownloaded(true);
                }
                //???????????????????????????????????????config
                fixConfig(StickerUtils.getInstance().getStyleInfos());
                fixConfig(StickerUtils.getInstance().getDBStyleInfos());
            }
            //????????????????????????????????????
            ArrayList<StickerInfo> list = TempVideoParams.getInstance().getRSpecialInfos();
            new StickerExportHandler(player.getContext(), list, playerWidth, playerHeight).export(null);
            if (null != fixPreviewListener) {
                fixPreviewListener.onComplete();
            }
        }
    }

    /**
     * ?????????????????????????????????????????????????????????
     *
     * @param mListPointF ???????????????
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
     * ??????????????????
     *
     * @param backup ??????????????????
     * @param list   ??????????????????
     * @return true ??????????????????false ???????????????
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
     * ??????90???270 ??????????????????????????????
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
        //??????90???270 ??????????????????????????????
        vc.setVideoSize(tmpW, tmpH);
    }

}
