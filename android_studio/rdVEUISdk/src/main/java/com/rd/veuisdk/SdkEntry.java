package com.rd.veuisdk;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.RectF;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.facebook.common.internal.Supplier;
import com.facebook.common.util.ByteConstants;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.cache.MemoryCacheParams;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.rd.auth.RdAuth;
import com.rd.lib.utils.CoreUtils;
import com.rd.lib.utils.ThreadPoolUtils;
import com.rd.net.RdHttpClient;
import com.rd.recorder.api.RecorderCore;
import com.rd.vecore.RdVECore;
import com.rd.vecore.VirtualVideo;
import com.rd.vecore.listener.ExportListener;
import com.rd.vecore.models.AspectRatioFitMode;
import com.rd.vecore.models.MediaObject;
import com.rd.vecore.models.MediaType;
import com.rd.vecore.models.PermutationMode;
import com.rd.vecore.models.Scene;
import com.rd.vecore.models.Trailer;
import com.rd.vecore.models.VideoConfig;
import com.rd.vecore.models.Watermark;
import com.rd.veuisdk.callback.ICompressVideoCallback;
import com.rd.veuisdk.callback.ISdkCallBack;
import com.rd.veuisdk.database.SDMusicData;
import com.rd.veuisdk.database.SpecialData;
import com.rd.veuisdk.database.SubData;
import com.rd.veuisdk.database.TTFData;
import com.rd.veuisdk.database.WebMusicData;
import com.rd.veuisdk.manager.CameraConfiguration;
import com.rd.veuisdk.manager.EditObject;
import com.rd.veuisdk.manager.UIConfiguration;
import com.rd.veuisdk.manager.VEOSDBuilder;
import com.rd.veuisdk.model.ImageCacheUtils;
import com.rd.veuisdk.ui.SubFunctionUtils;
import com.rd.veuisdk.utils.AppConfiguration;
import com.rd.veuisdk.utils.IntentConstants;
import com.rd.veuisdk.utils.PathUtils;
import com.rd.veuisdk.utils.SysAlertDialog;
import com.rd.veuisdk.utils.Utils;

import java.lang.reflect.Constructor;
import java.util.ArrayList;

import static com.rd.veuisdk.EditPreviewActivity.APPEND_IMAGE;

/**
 * RdVEUISdk API调用入口
 */
public final class SdkEntry {
    /**
     * Intent key定义<br>
     * 视频路径
     */
    public static final String INTENT_KEY_VIDEO_PATH = "intent_key_video_path";

    /**
     * Intent key定义<br>
     * FaceU 美颜参数
     */
    public static final String INTENT_KEY_FACEU = "intent_key_faceu";
    /**
     * Intent key定义<br>
     * 图片路径
     */
    public static final String INTENT_KEY_PICTURE_PATH = "intent_key_picture_path";

    /**
     * 录制时使用的是否是mv模式
     */
    public static final String INTENT_KEY_USE_MV_EDIT = "intent_key_use_mv_edit";
    /**
     * 简单录制的回调路径
     */
    public static final int CAMERA_EXPORT = 1;
    /**
     * 录制并编辑的回调路径
     */
    public static final int CAMERA_EDIT_EXPORT = 2;
    /**
     * 视频编辑后的回调路径
     */
    public static final int EDIT_EXPORT = 3;
    /**
     * 普通截取视频
     */
    public static final int TRIMVIDEO_EXPORT = 4;
    /**
     * 定长截取视频
     */
    public static final int TRIMVIDEO_DURATION_EXPORT = 5;
    /**
     * 拍摄界面相册按钮ResultCode
     */
    public static final int RESULT_CAMERA_TO_ALBUM = 10;
    /**
     * 相册界面拍摄按钮ResultCode
     */
    public static final int RESULT_ALBUM_TO_CAMERA = 11;

    /**
     * 相册返回路径
     */
    public static final String ALBUM_RESULT = "album_result";
    /**
     * 编辑返回路径
     */
    public static final String EDIT_RESULT = "edit_result";
    public static final String TRIM_MEDIA_PATH = "trim_media_path";
    public static final String TRIM_START_TIME = "trim_start_time";
    public static final String TRIM_END_TIME = "trim_end_time";
    public static final String TRIM_CROP_RECT = "trim_crop_rect";

    static final String TRIM_RETURN = "trim_return";
    static final String TRIM_RETURN_TYPE = "trim_return_type";
    static final String ALBUM_CUSTOMIZE = "album_customize";
    static final String MEDIA_PATH_LIST = "media_path_list";
    static final String TAG = "SdkEntry";
    private static final String SDK_NOT_INITIALIZED_INFO = "RdVEUISdk not initialized!";
    private static final String NOT_SUPPORTED_CPU_ARCH = "Not supported CPU architecture!";

    private static boolean mIsInitialized;
    private static boolean mIsAutoDebugEnabled = false;
    private static final String LOW_API_LEVEL_18 = "Low API level, need 18+.";
    private static final String LOW_API_LEVEL_16 = "Low API level, need 16+.";

    /**
     * 启用调试日志
     *
     * @param enabled 为true代表启用调试日志
     */
    public static void enableDebugLog(boolean enabled) {
        mIsAutoDebugEnabled = enabled;
    }


    /**
     * 获取SDK版本
     *
     * @return 版本
     */
    public static String getVersion() {
        return RdVECore.getVersion();
    }

    /**
     * 初始化SDK
     *
     * @param context        应用上下文
     * @param customRootPath 自定义的工作目录，不设置采用默认设置
     * @param appkey         在平台申请的Appkey
     * @param appScrect      在平台申请的appScrect
     * @param callBack       回调接口
     * @return 返回true代表正常初始化SDK
     */
    public static boolean initialize(Context context, String customRootPath,
                                     String appkey, String appScrect,
                                     ISdkCallBack callBack) {
        if (isInitialized()) {
            SdkEntryHandler.getInstance().setICallBack(callBack);
            Log.w(TAG, "rdveuisdk is initialized");
            return true;
        } else {
            try {
                //编辑初始化
                RdVECore.initialize(context, customRootPath, appkey, appScrect, mIsAutoDebugEnabled);

                Utils.initialize(context, customRootPath);
                AppConfiguration.initContext(context);
                SdkEntryHandler.getInstance().setICallBack(callBack);

                Fresco.initialize(context, getFrescoConfigureCaches(context));
                mIsInitialized = true;
                return true;
            } catch (IllegalAccessError ex) {
                if (!TextUtils.isEmpty(ex.getMessage())) {
                    Log.e(TAG, ex.getMessage());
                }
                return false;
            } catch (Exception ex) {
                if (!TextUtils.isEmpty(ex.getMessage())) {
                    Log.e(TAG, ex.getMessage());
                }
                return false;
            }
        }
    }

    /**
     * 返回是否已正常初始
     *
     * @return 是否已正常初始
     */
    public static boolean isInitialized() {
        return mIsInitialized;
    }

    /**
     * 返回是否支持cup架构
     *
     * @return
     */
    public static boolean isSupportCPUArch() {
        return RdVECore.isSupportCPUArch();
    }

    private static SdkService sdkService;

    public static SdkService getSdkService() {
        if (sdkService == null) {
            sdkService = new SdkService();
        }
        return sdkService;
    }

    /**
     * 调用摄像录制
     *
     * @param context     应用上下文
     * @param gotoEdit    是否录制完成后，进入高级编辑界面
     * @param requestCode 用于onActivityResult时的requestCode
     */
    public static void record(Context context, boolean gotoEdit,
                              int requestCode) {
        if (!checkAppKey(context)) {
            return;
        }
        if (CoreUtils.hasJELLY_BEAN_MR2()) {
            Intent intent = new Intent(context,
                    com.rd.veuisdk.RecorderActivity.class);
            intent.putExtra(RecorderActivity.ACTION_TO_EDIT, gotoEdit);
            if (context instanceof Activity && requestCode > 0) {
                ((Activity) context).startActivityForResult(intent, requestCode);
            } else {
                context.startActivity(intent);
            }
        } else {
            Log.e(TAG, LOW_API_LEVEL_18);
        }
    }


    /**
     * 调用摄像录制
     *
     * @param context     应用上下文
     * @param requestCode 用于onActivityResult时的requestCode
     */
    public static void record(Context context, int requestCode) {
        if (!checkAppKey(context)) {
            return;
        }
        if (CoreUtils.hasJELLY_BEAN_MR2()) {
            Intent intent = new Intent(context,
                    com.rd.veuisdk.RecorderActivity.class);
            if (context instanceof Activity && requestCode > 0) {
                ((Activity) context).startActivityForResult(intent, requestCode);
            } else {
                context.startActivity(intent);
            }
        } else {
            Log.e(TAG, LOW_API_LEVEL_18);
        }
    }

//    /**
//     * 画中画功能
//     *
//     * @param context
//     * @param requestCode
//     */
//    public static void mixVideo(Context context, int requestCode) {
//        if (!checkAppKey(context)) {
//            return;
//        }
//        Intent intent = new Intent(context,
//                com.rd.veuisdk.SelectModeActivity.class);
//        if (context instanceof Activity && requestCode > 0) {
//            ((Activity) context).startActivityForResult(intent, requestCode);
//        } else {
//            context.startActivity(intent);
//        }
//    }

    /**
     * 选择媒体资源
     *
     * @param context     应用上下文
     * @param requestCode 用于ActivityResult的requestCode
     */
    public static void selectMedia(Context context, int requestCode) {
        if (!checkAppKey(context)) {
            return;
        }
        if (CoreUtils.hasJellyBean()) {
            Intent intent = new Intent(context,
                    com.rd.veuisdk.SelectMediaActivity.class);
            if (getSdkService().getUIConfig().openEditbyPicture) {
                intent.putExtra(APPEND_IMAGE, true);
            }
            intent.putExtra(IntentConstants.EDIT_TWO_WAY, true);
            ((Activity) context).startActivityForResult(intent, requestCode);
        } else {
            Log.e(TAG, LOW_API_LEVEL_16);
        }
    }

    /**
     * 选择媒体资源
     *
     * @param context 应用上下文
     */
    public static void selectMedia(Context context) {
        selectMedia(context, -1);
    }

    /***
     * 直接进入编辑界面
     *
     * @param context
     *            应用上下文
     * @param medialist
     *            媒体资源路径集合
     * @param requestCode
     *            用于ActivityResult的requestCode
     * @return true代表直播进入编辑界面成功
     */
    public static boolean editMedia(Context context,
                                    ArrayList<String> medialist, int requestCode) {
        if (!checkAppKey(context)) {
            return false;
        }
        if (null == medialist || 0 == medialist.size()) {
            Log.e(TAG, context.getString(R.string.select_medias));
            return false;
        } else {
            if (CoreUtils.hasJellyBean()) {
                ArrayList<Scene> list = new ArrayList<>();
                for (String nMediaKey : medialist) {
                    if (null != nMediaKey) {
                        Scene scene = VirtualVideo.createScene();
                        MediaObject mo = new MediaObject(nMediaKey);
                        if (mo != null) {
                            mo.setTimeRange(0, mo.getDuration());
                            scene.addMedia(mo);
                            list.add(scene);
                        }
                    }
                }

                if (list.size() == 0) {
                    Log.e(TAG, context.getString(R.string.select_medias));
                    return false;
                }
                Intent intent;
                if (SubFunctionUtils.isEnableWizard()) {
                    intent = new Intent(context, EditPreviewActivity.class);
                } else {
                    intent = new Intent(context, VideoEditActivity.class);
                }
                intent.putParcelableArrayListExtra(
                        IntentConstants.INTENT_EXTRA_SCENE, list);
                ((Activity) context).startActivityForResult(intent, requestCode);
                return true;
            } else {
                Log.e(TAG, LOW_API_LEVEL_16);
                return false;
            }
        }
    }

    /***
     * 直接进入编辑界面
     *
     * @param context
     *            应用上下文
     * @param editObject
     *            媒体对象
     * @param requestCode
     *            用于ActivityResult的requestCode
     * @return true代表直播进入编辑界面成功
     */
    public static boolean editMedia(Context context, EditObject editObject,
                                    int requestCode) {
        if (!checkAppKey(context)) {
            return false;
        }
        if (editObject == null || null == editObject.getObjectPath()) {
            Log.e(TAG, context.getString(R.string.select_medias));
            return false;
        } else {
            if (CoreUtils.hasJellyBean()) {
                ArrayList<Scene> list = new ArrayList<Scene>();
                Scene scene = VirtualVideo.createScene();
                MediaObject mo = new MediaObject(editObject.getObjectPath());
                if (mo != null) {
                    mo.setClipRectF(editObject.getCropRect());
                    mo.setShowRectF(null);
                    mo.setTimeRange(editObject.getStartTime(),
                            editObject.getEndTime());
                    scene.addMedia(mo);
                    list.add(scene);
                }
                if (list.size() == 0) {
                    Log.e(TAG, context.getString(R.string.select_medias));
                    return false;
                }
                Intent intent;
                if (SubFunctionUtils.isEnableWizard()) {
                    intent = new Intent(context, EditPreviewActivity.class);
                } else {
                    intent = new Intent(context, VideoEditActivity.class);
                }
                intent.putParcelableArrayListExtra(
                        IntentConstants.INTENT_EXTRA_SCENE, list);
                ((Activity) context).startActivityForResult(intent, requestCode);
                return true;
            } else {
                Log.e(TAG, LOW_API_LEVEL_16);
                return false;
            }
        }
    }

    /***
     * 直接进入编辑界面
     *
     * @param context
     *            应用上下文
     * @param medialist
     *            媒体资源路径集合
     * @return true代表直播进入编辑界面成功
     */
    public static boolean editMedia(Context context, ArrayList<String> medialist) {
        return editMedia(context, medialist, -1);
    }


    /**
     * 定长截取
     *
     * @param context     应用上下文
     * @param videoPath   视频地址
     * @param requestCode 返回所需要的requestCode
     */
    public static void trimVideo(Context context, String videoPath,
                                 int requestCode) {
        if (!checkAppKey(context)) {
            return;
        }
        if (CoreUtils.hasJellyBean()) {
            Scene scene = null;
            if (videoPath != null) {
                scene = VirtualVideo.createScene();
                MediaObject media = scene.addMedia(videoPath);
                if (!(media.getMediaType() == MediaType.MEDIA_VIDEO_TYPE)) {
                    SysAlertDialog.showAutoHideDialog(
                            context,
                            null,
                            context.getResources().getString(
                                    R.string.trim_support_video_only),
                            Toast.LENGTH_SHORT);
                    return;
                }
            }
            Intent intent = new Intent(context,
                    com.rd.veuisdk.TrimMediaActivity.class);
            intent.putExtra(IntentConstants.INTENT_EXTRA_SCENE, scene);
            ((Activity) context).startActivityForResult(intent, requestCode);
        } else {
            Log.e(TAG, LOW_API_LEVEL_16);
        }
    }


    /**
     * 进入相册
     *
     * @param formatType 相册显示内容
     */
    public static void openAlbum(Context context, int formatType,
                                 int requestCode) {
        if (!checkAppKey(context)) {
            return;
        }
        reInitCameraConfig(formatType);
        Intent intent = new Intent(context,
                com.rd.veuisdk.SelectMediaActivity.class);
        formatType = Math.max(UIConfiguration.ALBUM_SUPPORT_DEFAULT,
                Math.min(formatType, UIConfiguration.ALBUM_SUPPORT_IMAGE_ONLY));
        intent.putExtra(SelectMediaActivity.ALBUM_FORMAT_TYPE, formatType);
        intent.putExtra(SelectMediaActivity.ALBUM_ONLY, true);
        ((Activity) context).startActivityForResult(intent, requestCode);
    }

    /**
     * 重新初始拍摄配置（防止相册拍摄部分设置冲突）
     */
    private static void reInitCameraConfig(int formatType) {
        boolean hideMV = false;
        boolean hideRec = false;
        boolean hidePhoto = false;
        if (formatType == UIConfiguration.ALBUM_SUPPORT_IMAGE_ONLY) {
            hideMV = true;
            hideRec = true;
        } else if (formatType == UIConfiguration.ALBUM_SUPPORT_VIDEO_ONLY) {
            hidePhoto = true;
        }
        CameraConfiguration cameraConfig = getSdkService().getCameraConfig();
        getSdkService().initConfiguration(
                new CameraConfiguration.Builder().hidePhoto(hidePhoto)
                        .hideMV(hideMV).hideRec(hideRec)
                        .enableAlbum(cameraConfig.enableAlbum)
                        .enableAntiChange(cameraConfig.enableAntiChange)
                        .setAudioMute(cameraConfig.audioMute)
                        .setCameraMVMaxTime(cameraConfig.cameraMVMaxTime)
                        .setCameraMVMinTime(cameraConfig.cameraMVMinTime)
                        .setCameraUIType(cameraConfig.cameraUIType)
                        .setDefaultRearCamera(cameraConfig.dafaultRearCamera)
                        .enableFaceu(cameraConfig.enableFaceU)
                        .setPack(cameraConfig.pack)
                        .setSingleCameraSaveToAlbum(cameraConfig.isSaveToAlbum)
                        .setVideoMaxTime(cameraConfig.videoMaxTime)
                        .setVideoMinTime(cameraConfig.videoMinTime).get());
    }

    /**
     * 第一个编辑界面
     *
     * @param context
     * @param list
     */
    static void gotoEdit(Context context, ArrayList<MediaObject> list,
                         Intent intent) {

        if (CoreUtils.hasJellyBean()) {
            Intent i = new Intent(context, EditPreviewActivity.class);

            i.putExtra(IntentConstants.EXTRA_MEDIA_LIST, list);
            i.putExtra(VideoEditActivity.ACTION_FROM_CAMERA, true);

            i.putExtra(EditPreviewActivity.TEMP_FILE, list.get(0)
                    .getMediaPath());
            if (intent.getExtras() != null && intent.getExtras().size() > 0) {
                i.putExtras(intent.getExtras());
            }
            context.startActivity(i);
        } else {
            Log.e(TAG, LOW_API_LEVEL_16);
        }
    }


    private static double _VideoEncodingBitRate = -1;
    private static int maxWH = 640;

    @Deprecated
    static double getVideoEncodingBitRate() {
        return _VideoEncodingBitRate;
    }

    /**
     * 设置保存导出视频时的码流
     *
     * @param videoEncodingBitRate 码流大小（单位：M）
     */
    @Deprecated
    public static void setVideoEncodingBitRate(double videoEncodingBitRate) {
        _VideoEncodingBitRate = Math.max(videoEncodingBitRate, 1);
    }

    /**
     * 跳转到视频播放页
     *
     * @param context   上下文
     * @param videoPath 本地视频播放地址
     */
    @Deprecated
    public static void playVideo(Context context, String videoPath) {
        if (!checkAppKey(context)) {
            return;
        }
        if (CoreUtils.hasJellyBean()) {
            Intent intent = new Intent(context,
                    com.rd.veuisdk.VideoPreviewActivity.class);
            intent.putExtra(com.rd.veuisdk.VideoPreviewActivity.ACTION_PATH,
                    videoPath);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } else {
            Log.e(TAG, LOW_API_LEVEL_16);
        }
    }

    /**
     * 发送截取通知
     *
     * @param context    上下文
     * @param returnType 返回截取类型 0:返回截取视频 1:返回截取时间
     */
    public static void videoTrim(Context context, int returnType) {
        if (!checkAppKey(context)) {
            return;
        }
        Intent i = new Intent(SdkEntry.TRIM_RETURN);
        i.putExtra(SdkEntry.TRIM_RETURN_TYPE, returnType);
        context.sendBroadcast(i);
    }

    /**
     * 发送自定义相册媒体路径列表
     *
     * @param context   上下文
     * @param medialist 媒体资源地址列表
     */
    public static void onCustomizeAlbum(Context context,
                                        ArrayList<String> medialist) {
        if (null == medialist || 0 == medialist.size()) {
            Log.e(TAG, context.getString(R.string.select_medias));
            return;
        } else {
            if (!checkAppKey(context)) {
                return;
            }
            Intent intent = new Intent(SdkEntry.ALBUM_CUSTOMIZE);
            intent.putStringArrayListExtra(SdkEntry.MEDIA_PATH_LIST, medialist);
            context.sendBroadcast(intent);
        }
    }

    /**
     * 压缩媒体
     *
     * @param context   上下文
     * @param mediaPath 媒体资源地址
     * @param callback  压缩视频回调接口
     */
    public static void onCompressVideo(Context context, String mediaPath,
                                       ICompressVideoCallback callback) {
        if (!checkAppKey(context)) {
            return;
        }
        if (CoreUtils.hasJellyBean()) {
            CompressVideo.compressVideo(context, mediaPath, callback);
        } else {
            Log.e(TAG, LOW_API_LEVEL_16);
        }
    }

    /**
     * 取消压缩
     */
    public static void cancelCompressVideo() {
        CompressVideo.cancelCompress();
    }

    private static Class<?> m_clsOSDBuilder;

    /**
     * 注册水印类 VEOSDBuilder.class 的具体实现
     *
     * @param builderClass
     */
    public static void registerOSDBuilder(Class<?> builderClass) {
        m_clsOSDBuilder = builderClass;
    }

    /**
     * @param context
     * @param isSquare
     * @return
     */
    static VEOSDBuilder createOSDBuilder(Context context, boolean isSquare) {
        if (m_clsOSDBuilder != null) {
            try {
                @SuppressWarnings("unchecked")
                Constructor<VEOSDBuilder> constructor = (Constructor<VEOSDBuilder>) m_clsOSDBuilder
                        .getConstructor(Context.class, Boolean.class);
                return constructor.newInstance(context, isSquare);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Log.e("createOSDBuilder", "m_clsOSDBuilder is null");
        }
        return null;
    }

    /**
     * 退出程序
     *
     * @param context
     */
    public static void onExitApp(Context context) {
        TTFData.getInstance().close();
        SubData.getInstance().close();
        SpecialData.getInstance().close();
        SDMusicData.getInstance().close();
        WebMusicData.getInstance().close();
        RecorderCore.onExit(context);
        RdVECore.recycle();
        ImageCacheUtils.getInstance(context).recycle();
        SdkService server = getSdkService();
        RdHttpClient.ShutDown();
        if (null != server) {
            server.reset();
        }
        sdkService = null;
        ThreadPoolUtils.executeEx(new Runnable() {

            @Override
            public void run() {
                PathUtils.clearTemp();
            }
        });
    }

    /**
     * 退出App
     */
    @SuppressWarnings("deprecation")
    static void onExitApp(Context context, boolean bRestart) {
        try {
            if (bRestart) {
                ActivityManager activityMgr = (ActivityManager) context
                        .getSystemService(Context.ACTIVITY_SERVICE);
                System.exit(0);
                activityMgr.restartPackage(context.getPackageName());
            } else {
                System.gc();
            }

        } catch (Exception e) {
        }
    }


    private static boolean checkAppKey(Context context) {
        if (!RdVECore.isSupportCPUArch()) {
            Log.e(TAG, NOT_SUPPORTED_CPU_ARCH);
            Utils.autoToastNomal(context, context.getString(R.string.not_support_cup_arch));
            return false;
        }
        if (!mIsInitialized) {
            Log.e(TAG, SDK_NOT_INITIALIZED_INFO);
            return false;
        }
        int re = RdAuth.enableEditorBase();
        if (re == RdAuth.AUTH_OK) {
            return true;
        } else if (re == RdAuth.AUTH_EXPIRED) {
            Log.e(TAG, RdAuth.AUTH_EXPIRED_STR);
            return false;
        } else if (re == RdAuth.AUTH_NONACTIVATED) {
            Log.e(TAG, RdAuth.AUTH_NONACTIVATED_STR);
            return false;
        } else {
            return true;
        }
    }

    private static int MAX_MEM = 30 * ByteConstants.MB;

    private static ImagePipelineConfig getFrescoConfigureCaches(Context context) {
        final MemoryCacheParams bitmapCacheParams = new MemoryCacheParams(
                MAX_MEM,// 内存缓存中总图片的最大大小,以字节为单位。
                Integer.MAX_VALUE,// 内存缓存中图片的最大数量。
                MAX_MEM,// 内存缓存中准备清除但尚未被删除的总图片的最大大小,以字节为单位。
                Integer.MAX_VALUE,// 内存缓存中准备清除的总图片的最大数量。
                Integer.MAX_VALUE);// 内存缓存中单个图片的最大大小。

        Supplier<MemoryCacheParams> mSupplierMemoryCacheParams = new Supplier<MemoryCacheParams>() {
            @Override
            public MemoryCacheParams get() {
                return bitmapCacheParams;
            }
        };
        ImagePipelineConfig.Builder builder = ImagePipelineConfig.newBuilder(context);
        builder.setBitmapMemoryCacheParamsSupplier(mSupplierMemoryCacheParams);
        builder.setDownsampleEnabled(true);
        return builder.build();
    }

    private static VirtualVideo exportVideo;

    /**
     * * 简单文件导出
     *
     * @param context
     * @param videoConfig    输出文件的参数
     * @param videoList      文件源
     * @param outPath        输出文件的路径
     * @param watermark      水印
     * @param trailer        视频片尾
     * @param exportListener 视频导出回调
     */
    public static void exportVideo(Context context, VideoConfig videoConfig, ArrayList<String> videoList, String outPath, Watermark watermark, Trailer trailer, final ExportListener exportListener) {
        if ((null != context) && !TextUtils.isEmpty(outPath) && (null != videoList && videoList.size() > 0)) {

            Scene scene = VirtualVideo.createScene();
            int len = videoList.size();
            for (int i = 0; i < len; i++) {
                MediaObject media = new MediaObject(videoList.get(i));
                media.setShowRectF(new RectF(0, 0, 1f, 1f));
                media.setAspectRatioFitMode(AspectRatioFitMode.KEEP_ASPECTRATIO_EXPANDING);
                scene.addMedia(media);
            }
            scene.setPermutationMode(PermutationMode.LINEAR_MODE);

            exportVideo = new VirtualVideo();
            exportVideo.addScene(scene);
            if (watermark != null) {
                exportVideo.addWatermark(watermark);
            }

            if (null != trailer) {
                exportVideo.setTrailer(trailer);
            }

            VideoConfig vc;
            if (null != videoConfig) {
                vc = videoConfig;
            } else {
                vc = new VideoConfig();
            }

            exportVideo.export(context, outPath, vc, new ExportListener() {
                @Override
                public void onExportStart() {
                    if (null != exportListener) {
                        exportListener.onExportStart();
                    }
                }

                @Override
                public boolean onExporting(int progress, int max) {
                    if (null != exportListener) {
                        exportListener.onExporting(progress, max);
                    }
                    return true;
                }

                @Override
                public void onExportEnd(int result) {
                    if (null != exportVideo) {
                        exportVideo.release();
                        exportVideo = null;
                    }
                    if (null != exportListener) {
                        exportListener.onExportEnd(result);
                    }
                }
            });

        } else {
            Log.e(TAG, "onExportVideo:  videoPath  or  outPath  is null");
        }

    }

    /**
     * 取消导出
     */
    public static void cancelExport() {
        if (null != exportVideo) {
            exportVideo.cancelExport();
        }
    }

}
