package com.rd.veuisdk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.facebook.cache.disk.DiskCacheConfig;
import com.facebook.common.internal.Supplier;
import com.facebook.common.util.ByteConstants;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.cache.MemoryCacheParams;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.rd.lib.utils.CoreUtils;
import com.rd.lib.utils.ThreadPoolUtils;
import com.rd.net.RdHttpClient;
import com.rd.recorder.api.RecorderCore;
import com.rd.vecore.RdVECore;
import com.rd.vecore.VirtualVideo;
import com.rd.vecore.exception.InvalidArgumentException;
import com.rd.vecore.listener.ExportListener;
import com.rd.vecore.models.MediaObject;
import com.rd.vecore.models.MediaType;
import com.rd.vecore.models.Scene;
import com.rd.vecore.models.Trailer;
import com.rd.vecore.models.VideoConfig;
import com.rd.vecore.models.Watermark;
import com.rd.veuisdk.callback.ICompressVideoCallback;
import com.rd.veuisdk.callback.IExportCallBack;
import com.rd.veuisdk.callback.ISdkCallBack;
import com.rd.veuisdk.database.DraftData;
import com.rd.veuisdk.database.SDMusicData;
import com.rd.veuisdk.database.StickerData;
import com.rd.veuisdk.database.SubData;
import com.rd.veuisdk.database.TTFData;
import com.rd.veuisdk.database.WebMusicData;
import com.rd.veuisdk.demo.CoverActivity;
import com.rd.veuisdk.demo.InterceptActivity;
import com.rd.veuisdk.demo.VideoCompressActivity;
import com.rd.veuisdk.demo.VideoDubbingActivity;
import com.rd.veuisdk.demo.VideoEditAloneActivity;
import com.rd.veuisdk.demo.VideoReverseActivity;
import com.rd.veuisdk.demo.VideoSoundEffectActivity;
import com.rd.veuisdk.demo.VideoTranscodActivity;
import com.rd.veuisdk.demo.VideoTransitionActivity;
import com.rd.veuisdk.demo.zishuo.VoiceTextActivity;
import com.rd.veuisdk.manager.CameraConfiguration;
import com.rd.veuisdk.manager.EditObject;
import com.rd.veuisdk.manager.UIConfiguration;
import com.rd.veuisdk.manager.VEOSDBuilder;
import com.rd.veuisdk.model.ImageCacheUtils;
import com.rd.veuisdk.model.ShortVideoInfoImp;
import com.rd.veuisdk.utils.apng.ApngImageLoader;
import com.rd.veuisdk.utils.AppConfiguration;
import com.rd.veuisdk.utils.EffectManager;
import com.rd.veuisdk.utils.ExportHandler;
import com.rd.veuisdk.utils.FileUtils;
import com.rd.veuisdk.utils.IntentConstants;
import com.rd.veuisdk.utils.ModeDataUtils;
import com.rd.veuisdk.utils.MyOkHttpImagePipelineConfigFactory;
import com.rd.veuisdk.utils.PathUtils;
import com.rd.veuisdk.utils.SysAlertDialog;
import com.rd.veuisdk.utils.TransitionManager;
import com.rd.veuisdk.utils.Utils;
import com.rd.veuisdk.utils.cache.CacheManager;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;

import static com.rd.veuisdk.SelectMediaActivity.APPEND_IMAGE;


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

    static final String MSG_EXPORT = "msg_export";
    static final String EXPORT_WITH_WATERMARK = "export_with_watermark";
    public static final String INTENT_KEY_MEDIA_MIME = "mime_type";

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
                                     String appkey, String appScrect, ISdkCallBack callBack) {
        return initialize(context, customRootPath, appkey, appScrect, null, callBack);
    }

    /**
     * 初始化SDK
     *
     * @param context        应用上下文
     * @param customRootPath 自定义的工作目录，不设置采用默认设置
     * @param appkey         在平台申请的Appkey
     * @param appScrect      在平台申请的appScrect
     * @param callBack       回调接口
     * @param exportCallBack 自定义导出回调接口
     * @return 返回true代表正常初始化SDK
     */
    public static boolean initialize(Context context, String customRootPath,
                                     String appkey, String appScrect, ISdkCallBack callBack, IExportCallBack exportCallBack) {
        return initialize(context, customRootPath, appkey, appScrect, null, callBack, exportCallBack);
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
                                     String appkey, String appScrect, String licenseKey, ISdkCallBack callBack) {
        return initialize(context, customRootPath, appkey, appScrect, licenseKey, callBack, null);
    }

    //是否是精简版（true 精简版；）
    private static boolean isLite = false;


    /**
     * 初始化SDK
     *
     * @param context        应用上下文
     * @param customRootPath 自定义的工作目录，不设置采用默认设置
     * @param appkey         在平台申请的Appkey
     * @param appScrect      在平台申请的appScrect
     * @param callBack       回调接口
     * @param exportCallBack 自定义导出回调接口
     * @return 返回true代表正常初始化SDK
     */
    public static boolean initialize(Context context, String customRootPath,
                                     String appkey, String appScrect, String licenseKey, ISdkCallBack callBack, IExportCallBack exportCallBack) {
        if (isInitialized()) {
            SdkEntryHandler.getInstance().setICallBack(callBack);
            Log.w(TAG, "rdveuisdk is initialized");
            return true;
        } else {
            try {
                //编辑初始化
                boolean forceSWDecoder = Build.VERSION.SDK_INT >= 29;//android 10(q)暂时强制软解
                RdVECore.initialize(context, customRootPath, appkey, appScrect, licenseKey, mIsAutoDebugEnabled,
                        forceSWDecoder);
                Utils.initialize(context, customRootPath);
                AppConfiguration.initContext(context);
                ApngImageLoader.getInstance().init(context);
                SdkEntryHandler.getInstance().setICallBack(callBack);
                SdkEntryHandler.getInstance().setIExportCallBack(exportCallBack);
                Fresco.initialize(context, getFrescoConfigureCaches(context));
                ModeDataUtils.init(appkey);
                mIsInitialized = true;
                isLite = checkIsLite(context);
                EffectManager.getInstance().init(context);
                TransitionManager.getInstance().init(context);
                CacheManager.init(context);
                return true;
            } catch (IllegalAccessError ex) {
                ex.printStackTrace();
                if (!TextUtils.isEmpty(ex.getMessage())) {
                    Log.e(TAG, ex.getMessage());
                }
                return false;
            } catch (Exception ex) {
                ex.printStackTrace();
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
        if (appKeyIsInvalid(context)) {
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
     * 检查appkey是否无效,返回true代表无效
     */
    private static boolean appKeyIsInvalid(Context context) {
        if (!RdVECore.isSupportCPUArch()) {
            Log.e(TAG, NOT_SUPPORTED_CPU_ARCH);
            Utils.autoToastNomal(context, context.getString(R.string.not_support_cup_arch));
            return true;
        }
        if (!mIsInitialized) {
            Log.e(TAG, SDK_NOT_INITIALIZED_INFO);
            return true;
        }
        return !RdVECore.checkAppKey(context);
    }


    /**
     * 调用摄像录制
     *
     * @param context     应用上下文
     * @param requestCode 用于onActivityResult时的requestCode
     */
    public static void record(Context context, int requestCode) {
        if (appKeyIsInvalid(context)) {
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


    /**
     * 选择媒体资源
     *
     * @param context     应用上下文
     * @param requestCode 用于ActivityResult的requestCode
     */
    public static void selectMedia(Context context, int requestCode) {
        if (appKeyIsInvalid(context)) {
            return;
        }
        if (CoreUtils.hasJellyBean()) {
            Intent intent = new Intent(context, SelectMediaActivity.class);
            if (getSdkService().getUIConfig().openEditbyPicture) {
                intent.putExtra(APPEND_IMAGE, true);
            }
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
    public static boolean editMedia(Context context, ArrayList<String> medialist, int requestCode)
            throws InvalidArgumentException {
        if (appKeyIsInvalid(context)) {
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
                        MediaObject mo = null;
                        mo = new MediaObject(nMediaKey);

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
                if (getSdkService().getUIConfig().isEnableWizard()) {
                    intent = new Intent(context, EditPreviewActivity.class);
                } else {
                    intent = new Intent(context, VideoEditActivity.class);
                }
                intent.putParcelableArrayListExtra(
                        IntentConstants.INTENT_EXTRA_SCENE, list);
                if (context instanceof Activity) {
                    ((Activity) context).startActivityForResult(intent, requestCode);
                    ((Activity) context).overridePendingTransition(0, 0);
                }
                return true;
            } else {
                Log.e(TAG, LOW_API_LEVEL_16);
                return false;
            }
        }
    }

    /**
     * 视频封面
     *
     * @param videoPath   媒体路径
     * @param requestCode 用于ActivityResult的requestCode
     * @return true 代表直播进入编辑界面成功
     */
    public static boolean videoCover(Context context, String videoPath, int requestCode) throws InvalidArgumentException {
        if (appKeyIsInvalid(context) || !FileUtils.isExist(videoPath)) {
            return false;
        }
        Scene scene = VirtualVideo.createScene();
        scene.addMedia(videoPath);
        Intent intent = new Intent(context,
                CoverActivity.class);
        intent.putExtra(IntentConstants.INTENT_EXTRA_SCENE, scene);
        ((Activity) context).startActivityForResult(intent, requestCode);
        return true;
    }

    /**
     * 视频截取
     *
     * @param videoPath   媒体路径
     * @param requestCode 用于ActivityResult的requestCode
     * @return true 代表直播进入编辑界面成功
     */
    public static boolean videoIntercept(Context context, String videoPath, int requestCode) throws InvalidArgumentException {
        if (appKeyIsInvalid(context) || !FileUtils.isExist(videoPath)) {
            return false;
        }
        Scene scene = VirtualVideo.createScene();
        scene.addMedia(videoPath);
        Intent intent = new Intent(context,
                InterceptActivity.class);
        intent.putExtra(IntentConstants.INTENT_EXTRA_SCENE, scene);
        ((Activity) context).startActivityForResult(intent, requestCode);
        return true;
    }

    /**
     * 视频字幕
     *
     * @param videoPath   媒体路径
     * @param requestCode 用于ActivityResult的requestCode
     * @return true 代表直播进入编辑界面成功
     */
    public static boolean videoSubtitle(Context context, String videoPath, int requestCode) throws InvalidArgumentException {
        if (appKeyIsInvalid(context) || !FileUtils.isExist(videoPath)) {
            return false;
        }
        Scene scene = VirtualVideo.createScene();
        scene.addMedia(videoPath);
        VideoEditAloneActivity.newInstance(context, scene, VideoEditAloneActivity.TYPE_SUBTITLE, requestCode);
        return true;
    }

    /**
     * 视频贴纸
     *
     * @param videoPath   媒体路径
     * @param requestCode 用于ActivityResult的requestCode
     * @return true 代表直播进入编辑界面成功
     */
    public static boolean videoStiker(Context context, String videoPath, int requestCode) throws InvalidArgumentException {
        if (appKeyIsInvalid(context) || !FileUtils.isExist(videoPath)) {
            return false;
        }
        Scene scene = VirtualVideo.createScene();
        scene.addMedia(videoPath);
        VideoEditAloneActivity.newInstance(context, scene, VideoEditAloneActivity.TYPE_STICKER, requestCode);
        return true;
    }

    /**
     * 视频特效
     *
     * @param videoPath   媒体路径
     * @param requestCode 用于ActivityResult的requestCode
     * @return true 代表直播进入编辑界面成功
     */
    public static boolean videoEffect(Context context, String videoPath, int requestCode) throws InvalidArgumentException {
        if (appKeyIsInvalid(context) || !FileUtils.isExist(videoPath)) {
            return false;
        }
        Scene scene = VirtualVideo.createScene();
        scene.addMedia(videoPath);
        VideoEditAloneActivity.newInstance(context, scene, VideoEditAloneActivity.TYPE_EFFECT, requestCode);
        return true;
    }

    /**
     * 视频滤镜
     *
     * @param videoPath   媒体路径
     * @param requestCode 用于ActivityResult的requestCode
     * @return true 代表直播进入编辑界面成功
     */
    public static boolean videoFilter(Context context, String videoPath, int requestCode) throws InvalidArgumentException {
        if (appKeyIsInvalid(context) || !FileUtils.isExist(videoPath)) {
            return false;
        }
        Scene scene = VirtualVideo.createScene();
        scene.addMedia(videoPath);
        VideoEditAloneActivity.newInstance(context, scene, VideoEditAloneActivity.TYPE_FILTER, requestCode);
        return true;
    }

    /**
     * 视频去水印
     *
     * @param videoPath   媒体路径
     * @param requestCode 用于ActivityResult的requestCode
     * @return true 代表直播进入编辑界面成功
     */
    public static boolean videoOSD(Context context, String videoPath, int requestCode) throws InvalidArgumentException {
        if (appKeyIsInvalid(context) || !FileUtils.isExist(videoPath)) {
            return false;
        }
        Scene scene = VirtualVideo.createScene();
        scene.addMedia(videoPath);
        VideoEditAloneActivity.newInstance(context, scene, VideoEditAloneActivity.TYPE_OSD, requestCode);
        return true;
    }

    /**
     * 视频画中画
     *
     * @param videoPath   媒体路径
     * @param requestCode 用于ActivityResult的requestCode
     * @return true 代表直播进入编辑界面成功
     */
    public static boolean videoCollage(Context context, String videoPath, int requestCode) throws InvalidArgumentException {
        if (appKeyIsInvalid(context) || !FileUtils.isExist(videoPath)) {
            return false;
        }
        Scene scene = VirtualVideo.createScene();
        scene.addMedia(videoPath);
        VideoEditAloneActivity.newInstance(context, scene, VideoEditAloneActivity.TYPE_COLLAGE, requestCode);
        return true;
    }

    /**
     * 视频涂鸦
     *
     * @param videoPath   媒体路径
     * @param requestCode 用于ActivityResult的requestCode
     * @return true 代表直播进入编辑界面成功
     */
    public static boolean videoGraffiti(Context context, String videoPath, int requestCode) throws InvalidArgumentException {
        if (appKeyIsInvalid(context) || !FileUtils.isExist(videoPath)) {
            return false;
        }
        Scene scene = VirtualVideo.createScene();
        scene.addMedia(videoPath);
        VideoEditAloneActivity.newInstance(context, scene, VideoEditAloneActivity.TYPE_GRAFFITI, requestCode);
        return true;
    }

    /**
     * 视频压缩
     *
     * @param videoPath   媒体路径
     * @param requestCode 用于ActivityResult的requestCode
     * @return true 代表直播进入编辑界面成功
     */
    public static boolean videoCompress(Context context, String videoPath, int requestCode) throws InvalidArgumentException {
        if (appKeyIsInvalid(context) || !FileUtils.isExist(videoPath)) {
            return false;
        }
        Scene scene = VirtualVideo.createScene();
        scene.addMedia(videoPath);
        Intent intent = new Intent(context,
                VideoCompressActivity.class);
        intent.putExtra(IntentConstants.INTENT_EXTRA_SCENE, scene);
        ((Activity) context).startActivityForResult(intent, requestCode);
        return true;
    }

    /**
     * 视频转码
     *
     * @param videoPath   媒体路径
     * @param requestCode 用于ActivityResult的requestCode
     * @return true 代表直播进入编辑界面成功
     */
    public static boolean videoTrans(Context context, String videoPath, int requestCode) throws InvalidArgumentException {
        if (appKeyIsInvalid(context) || !FileUtils.isExist(videoPath)) {
            return false;
        }
        Intent intent = new Intent(context, VideoTranscodActivity.class);
        Scene scene = VirtualVideo.createScene();
        scene.addMedia(videoPath);
        intent.putExtra(IntentConstants.INTENT_EXTRA_SCENE, scene);
        if (context instanceof Activity && requestCode > 0) {
            ((Activity) context).startActivityForResult(intent, requestCode);
        } else {
            context.startActivity(intent);
        }
        return true;
    }

    /**
     * 视频裁剪
     *
     * @param videoPath   媒体路径
     * @param requestCode 用于ActivityResult的requestCode
     * @return true 代表直播进入编辑界面成功
     */
    public static boolean videoCrop(Context context, String videoPath, int requestCode) throws InvalidArgumentException {
        if (appKeyIsInvalid(context) || !FileUtils.isExist(videoPath)) {
            return false;
        }
        Scene scene = VirtualVideo.createScene();
        scene.addMedia(videoPath);
        CropRotateMirrorActivity.onCrop(context, scene, requestCode);
        return true;
    }

    /**
     * 视频调色
     *
     * @param videoPath   媒体路径
     * @param requestCode 用于ActivityResult的requestCode
     * @return true 代表直播进入编辑界面成功
     */
    public static boolean videoTon(Context context, String videoPath, int requestCode) throws InvalidArgumentException {
        if (appKeyIsInvalid(context) || !FileUtils.isExist(videoPath)) {
            return false;
        }
        Scene scene = VirtualVideo.createScene();
        scene.addMedia(videoPath);
        MediaFilterConfigActivity.onTon(context, scene, true, requestCode);
        return true;
    }

    /**
     * 视频调速
     *
     * @param videoPath   媒体路径
     * @param requestCode 用于ActivityResult的requestCode
     * @return true 代表直播进入编辑界面成功
     */
    public static boolean videoSpeed(Context context, String videoPath, int requestCode) throws InvalidArgumentException {
        if (appKeyIsInvalid(context) || !FileUtils.isExist(videoPath)) {
            return false;
        }
        Scene scene = VirtualVideo.createScene();
        scene.addMedia(videoPath);
        SpeedPreviewActivity.onSpeed(context, scene, true, requestCode);
        return true;
    }

    /**
     * 视频变声
     *
     * @param videoPath   媒体路径
     * @param requestCode 用于ActivityResult的requestCode
     * @return true 代表直播进入编辑界面成功
     */
    public static boolean videoSoundEffect(Context context, String videoPath, int requestCode) throws InvalidArgumentException {
        if (appKeyIsInvalid(context) || !FileUtils.isExist(videoPath)) {
            return false;
        }
        Scene scene = VirtualVideo.createScene();
        scene.addMedia(videoPath);
        VideoSoundEffectActivity.videoSoundEffect(context, scene, true, requestCode);
        return true;
    }

    /**
     * 视频倒序
     *
     * @param videoPath   媒体路径
     * @param requestCode 用于ActivityResult的requestCode
     * @return true 代表直播进入编辑界面成功
     */
    public static boolean videoReverse(Context context, String videoPath, int requestCode) throws InvalidArgumentException {
        if (appKeyIsInvalid(context) || !FileUtils.isExist(videoPath)) {
            return false;
        }
        Scene scene = VirtualVideo.createScene();
        scene.addMedia(videoPath);
        Intent intent = new Intent(context, VideoReverseActivity.class);
        intent.putExtra(IntentConstants.INTENT_EXTRA_SCENE, scene);
        ((Activity) context).startActivityForResult(intent, requestCode);
        return true;
    }

    /**
     * 视频配音
     *
     * @param videoPath   媒体路径
     * @param requestCode 用于ActivityResult的requestCode
     * @return true 代表直播进入编辑界面成功
     */
    public static boolean videoDubbing(Context context, String videoPath, int requestCode) throws InvalidArgumentException {
        if (appKeyIsInvalid(context) || !FileUtils.isExist(videoPath)) {
            return false;
        }
        Scene scene = VirtualVideo.createScene();
        scene.addMedia(videoPath);
        Intent intent = new Intent(context, VideoDubbingActivity.class);
        intent.putExtra(IntentConstants.INTENT_EXTRA_SCENE, scene);
        ((Activity) context).startActivityForResult(intent, requestCode);
        return true;
    }

    /**
     * 视频转场
     *
     * @param medialist   媒体路径
     * @param requestCode 用于ActivityResult的requestCode
     * @return true 代表直播进入编辑界面成功
     */
    public static boolean videoTransition(Context context, ArrayList<String> medialist, int requestCode) throws InvalidArgumentException {
        if (appKeyIsInvalid(context)) {
            return false;
        }
        if (null == medialist || 0 == medialist.size()) {
            Log.e(TAG, context.getString(R.string.select_medias));
            return false;
        }

        ArrayList<Scene> list = new ArrayList<>();
        for (String nMediaKey : medialist) {
            Scene scene = VirtualVideo.createScene();
            scene.addMedia(nMediaKey);
            list.add(scene);
        }

        Intent intent = new Intent(context, VideoTransitionActivity.class);
        intent.putParcelableArrayListExtra(IntentConstants.INTENT_EXTRA_SCENE, list);
        if (context instanceof Activity) {
            ((Activity) context).startActivityForResult(intent, requestCode);
            ((Activity) context).overridePendingTransition(0, 0);
        }
        return true;
    }

    /**
     * 图片时长
     *
     * @param imagePath
     */
    public static boolean imageDuration(Context context, String imagePath, int requestCode) throws InvalidArgumentException {
        if (appKeyIsInvalid(context)) {
            return false;
        }
        if (null == imagePath) {
            Log.e(TAG, context.getString(R.string.select_medias));
            return false;
        }
        Scene scene = VirtualVideo.createScene();
        scene.addMedia(imagePath);
        ImageDurationActivity.onImageDuration(context, scene, true, requestCode);
        return true;
    }


    /**
     * 动画演示
     *
     * @param context          应用上下文
     * @param arrMediaListPath 是否录制完成后，进入高级编辑界面
     * @param enableAnim       是否时照片电影模式
     * @param requestCode      用于onActivityResult时的requestCode
     */
    public static void onAnimation(Context context, ArrayList<String> arrMediaListPath, boolean enableAnim,
                                   int requestCode) {
        if (appKeyIsInvalid(context)) {
            return;
        }
        if (CoreUtils.hasJELLY_BEAN_MR2()) {
            Intent intent = new Intent(context,
                    com.rd.veuisdk.TestAnimation.class);
            intent.putExtra(TestAnimation.VIDEOPATH, arrMediaListPath);
            intent.putExtra(TestAnimation.ENABLEANIM, enableAnim);
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
     * Quik演示
     */
    public static boolean quik(Context context, ArrayList<String> medialist, int requestCode) throws InvalidArgumentException {
        if (appKeyIsInvalid(context)) {
            return false;
        }
        if (null == medialist || medialist.size() == 0) {
            Log.e(TAG, context.getString(R.string.select_medias));
            return false;
        } else {
            if (CoreUtils.hasJellyBean()) {
                if (!isLite(context)) {
                    ArrayList<Scene> list = new ArrayList<>();
                    for (String nMediaKey : medialist) {
                        if (null != nMediaKey) {
                            Scene scene = VirtualVideo.createScene();
                            MediaObject mo = null;
                            mo = new MediaObject(nMediaKey);
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
                    Intent intent = new Intent(context, QuikActivity.class);
                    intent.putParcelableArrayListExtra(IntentConstants.INTENT_EXTRA_SCENE, list);
                    ((Activity) context).startActivityForResult(intent, requestCode);
                    return true;
                } else {
                    Log.e(TAG, "quik: " + context.getString(R.string.version_lite));
                }
            } else {
                Log.e(TAG, LOW_API_LEVEL_18);
            }
        }
        return false;
    }


    /**
     * 音效演示
     */
    public static void musicFilter(Context context, ArrayList<String> medialist, int requestCode) throws InvalidArgumentException {
        if (appKeyIsInvalid(context)) {
            return;
        }
        if (null == medialist || 0 == medialist.size()) {
            Log.e(TAG, context.getString(R.string.select_medias));
            return;
        } else {
            if (CoreUtils.hasJellyBean()) {
                ArrayList<MediaObject> list = new ArrayList<>();
                for (String nMediaKey : medialist) {
                    if (null != nMediaKey) {
                        MediaObject mo = null;
                        mo = new MediaObject(nMediaKey);
                        mo.setTimeRange(0, mo.getDuration());
                        list.add(mo);
                    }
                }
                if (list.size() == 0) {
                    Log.e(TAG, context.getString(R.string.select_medias));
                    return;
                }
                Intent intent = new Intent(context, MusicFilterActivity.class);
                intent.putExtra(IntentConstants.EXTRA_MEDIA_LIST, list);
                ((Activity) context).startActivityForResult(intent, requestCode);
            }
        }
    }

    /**
     * 音效演示(使用Virtual
     */
    public static void virtualAudioFilter(Context context, ArrayList<String> medialist, int requestCode)
            throws InvalidArgumentException {
        if (appKeyIsInvalid(context)) {
            return;
        }
        if (null == medialist || 0 == medialist.size()) {
            Log.e(TAG, context.getString(R.string.select_medias));
            return;
        } else {
            if (CoreUtils.hasJellyBean()) {
                ArrayList<MediaObject> list = new ArrayList<>();
                for (String nMediaKey : medialist) {
                    if (null != nMediaKey) {
                        MediaObject mo = null;
                        mo = new MediaObject(nMediaKey);
                        mo.setTimeRange(0, mo.getDuration());
                        list.add(mo);
                    }
                }
                if (list.size() == 0) {
                    Log.e(TAG, context.getString(R.string.select_medias));
                    return;
                }
                Intent intent = new Intent(context, MusicFilterNpActivity.class);
                intent.putExtra(IntentConstants.EXTRA_MEDIA_LIST, list);
                ((Activity) context).startActivityForResult(intent, requestCode);
            }
        }
    }


    /**
     * 异形演示
     */
    public static void alien(Context context, ArrayList<String> medialist, int requestCode) throws InvalidArgumentException {
        if (appKeyIsInvalid(context)) {
            return;
        }
        if (null == medialist || 0 == medialist.size()) {
            Log.e(TAG, context.getString(R.string.select_medias));
            return;
        } else {
            if (CoreUtils.hasJellyBean()) {
                ArrayList<MediaObject> list = new ArrayList<>();
                for (String nMediaKey : medialist) {
                    if (null != nMediaKey) {
                        MediaObject mo = null;
                        mo = new MediaObject(nMediaKey);
                        mo.setTimeRange(0, mo.getDuration());
                        list.add(mo);
                    }
                }
                if (list.size() == 0) {
                    Log.e(TAG, context.getString(R.string.select_medias));
                    return;
                }
                Intent intent = new Intent(context, AlienActivity.class);
                intent.putExtra(IntentConstants.EXTRA_MEDIA_LIST, list);
                ((Activity) context).startActivityForResult(intent, requestCode);
            }
        }
    }

    /**
     * 搞怪小视频-api实现（ 剪影演示）
     */
    public static void silhouette(Context context, String imagePath, int requestCode) throws InvalidArgumentException {
        if (appKeyIsInvalid(context)) {
            return;
        }
        if (null == imagePath || imagePath.isEmpty() || imagePath.equals("")) {
            Log.e(TAG, context.getString(R.string.select_medias));
            return;
        } else {
            if (CoreUtils.hasJellyBean()) {
                if (!isLite(context)) {
                    ArrayList<MediaObject> list = new ArrayList<>();
                    MediaObject mo = null;
                    mo = new MediaObject(imagePath);
                    mo.setTimeRange(0, mo.getDuration());
                    list.add(mo);
                    if (list.size() == 0) {
                        Log.e(TAG, context.getString(R.string.select_medias));
                        return;
                    }
                    Intent intent = new Intent(context, SilhouetteActivity.class);
                    intent.putExtra(IntentConstants.EXTRA_MEDIA_LIST, list);
                    ((Activity) context).startActivityForResult(intent, requestCode);

                } else {
                    Log.e(TAG, "silhouette: " + context.getString(R.string.version_lite));
                }
            } else {

            }
        }
    }

    /**
     * AE动画
     *
     * @param context
     * @param mediaList
     * @param requestCode
     * @param onlyImage   true为仅支持图片，false为支持图片和视频
     * @throws InvalidArgumentException
     */
    public static void AEAnimation(Context context, ArrayList<String> mediaList, int requestCode, boolean onlyImage) throws InvalidArgumentException {
        if (appKeyIsInvalid(context)) {
            return;
        }
        if (null == mediaList || mediaList.size() == 0) {
            Log.e(TAG, context.getString(R.string.select_medias));
            return;
        } else {
            if (CoreUtils.hasJellyBean()) {
                ArrayList<Scene> list = new ArrayList<>();
                for (String nMediaKey : mediaList) {
                    if (null != nMediaKey) {
                        Scene scene = VirtualVideo.createScene();
                        MediaObject mo = null;
                        mo = new MediaObject(nMediaKey);

                        if (mo != null) {
                            mo.setTimeRange(0, mo.getDuration());
                            scene.addMedia(mo);
                            list.add(scene);
                        }
                    }
                }

                if (list.size() == 0) {
                    Log.e(TAG, context.getString(R.string.select_medias));
                    return;
                }
                Intent intent = new Intent(context, AEActivity.class);
                intent.putParcelableArrayListExtra(
                        IntentConstants.INTENT_EXTRA_SCENE, list);
                intent.putExtra(IntentConstants.INTENT_AE_SUPPORT_VIDEO, !onlyImage);
                ((Activity) context).startActivityForResult(intent, requestCode);
            }
        }
    }

    /**
     * 多格拼接
     *
     * @param mediaList 媒体数目1~9个
     */
    public static void splice(Context context, ArrayList<String> mediaList, int requestCode) throws InvalidArgumentException {
        if (appKeyIsInvalid(context)) {
            return;
        }
        if (null == mediaList || mediaList.size() == 0 || mediaList.size() > 9) {
            Log.e(TAG, context.getString(R.string.select_medias));
            return;
        } else {
            if (CoreUtils.hasJellyBean()) {
                int len = mediaList.size();
                ArrayList<MediaObject> list = new ArrayList<>();
                for (int i = 0; i < len; i++) {
                    list.add(new MediaObject(mediaList.get(i)));
                }
                if (list.size() == 0) {
                    Log.e(TAG, context.getString(R.string.select_medias));
                    return;
                }
                Intent intent = new Intent(context, SpliceActivitiy.class);
                intent.putParcelableArrayListExtra(
                        IntentConstants.EXTRA_MEDIA_LIST, list);
                ((Activity) context).startActivityForResult(intent, requestCode);
            }
        }
    }

    /**
     * AE 列表
     */
    public static void AEList(Context context, int requestCode) {
        if (appKeyIsInvalid(context)) {
            return;
        }
        if (CoreUtils.hasJellyBean()) {
            Intent intent = new Intent(context, AEListActivity.class);
            ((Activity) context).startActivityForResult(intent, requestCode);
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
                                    int requestCode) throws InvalidArgumentException {
        if (appKeyIsInvalid(context)) {
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
                if (getSdkService().getUIConfig().isEnableWizard()) {
                    intent = new Intent(context, EditPreviewActivity.class);
                } else {
                    intent = new Intent(context, VideoEditActivity.class);
                }
                intent.putParcelableArrayListExtra(
                        IntentConstants.INTENT_EXTRA_SCENE, list);
                if (context instanceof Activity) {
                    ((Activity) context).startActivityForResult(intent, requestCode);
                    ((Activity) context).overridePendingTransition(0, 0);
                }
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
    public static boolean editMedia(Context context, ArrayList<String> medialist) throws InvalidArgumentException {
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
                                 int requestCode) throws InvalidArgumentException {
        if (appKeyIsInvalid(context)) {
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
    public static void openAlbum(Context context, int formatType, int requestCode) {
        openAlbum(context, 0, formatType, requestCode);
    }

    /**
     * @param nMediaLimitMin 限制至少选择多少个  默认：0 无限制
     * @param formatType     相册显示内容
     */
    public static void openAlbum(Context context, int nMediaLimitMin, int formatType, int requestCode) {
        if (appKeyIsInvalid(context)) {
            return;
        }
        reInitCameraConfig(formatType);
        Intent intent = new Intent(context, SelectMediaActivity.class);
        formatType = Math.max(UIConfiguration.ALBUM_SUPPORT_DEFAULT,
                Math.min(formatType, UIConfiguration.ALBUM_SUPPORT_IMAGE_ONLY));
        intent.putExtra(SelectMediaActivity.ALBUM_FORMAT_TYPE, formatType);
        intent.putExtra(SelectMediaActivity.PARAM_LIMIT_MIN, nMediaLimitMin);
        intent.putExtra(SelectMediaActivity.ALBUM_ONLY, true);
        ((Activity) context).startActivityForResult(intent, requestCode);
    }

    /**
     * 进入图片变形
     */
    public static boolean openImageTrans(Context context) {
        if (appKeyIsInvalid(context)) {
            return false;
        }
        if (!isLite(context)) {
            Intent intent = new Intent(context, ImageTransActivity.class);
            ((Activity) context).startActivity(intent);
            return true;
        }
        return false;
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
    public static void playVideo(Context context, String videoPath) {
        if (appKeyIsInvalid(context)) {
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
        if (appKeyIsInvalid(context)) {
            return;
        }
        Intent i = new Intent(SdkEntry.TRIM_RETURN);
        i.putExtra(SdkEntry.TRIM_RETURN_TYPE, returnType);
        context.sendBroadcast(i);
    }

    /**
     * * 自说（基于AE模板）
     */
    public static void zishuo(Context context, int requestCode) {
        if (CoreUtils.hasJellyBean()) {
            Intent intent = new Intent(context, ZishuoActivity.class);
            ((Activity) context).startActivityForResult(intent, requestCode);
        } else {
            Log.e(TAG, LOW_API_LEVEL_18);
        }
    }

    /**
     * 自说（自绘）
     */
    public static void zishuo2(Context context, int requestCode) {
        if (CoreUtils.hasJellyBean()) {
            Intent intent = new Intent(context, ZishuoDrawActivity.class);
            ((Activity) context).startActivityForResult(intent, requestCode);
        } else {
            Log.e(TAG, LOW_API_LEVEL_18);
        }
    }

    /**
     * 字说(自绘 文字转语音)
     */
    public static void voiceTexteTransform(Context context, int requestCode) {
        if (CoreUtils.hasJellyBean()) {
            Intent intent = new Intent(context, VoiceTextActivity.class);
            ((Activity) context).startActivityForResult(intent, requestCode);
        } else {
            Log.e(TAG, LOW_API_LEVEL_18);
        }
    }

    /**
     * 发送导出通知
     *
     * @param context       上下文
     * @param withWatermark 导出是否有水印 true 有 ,false 无
     */
    public static void videoExport(Context context, boolean withWatermark) {
        if (appKeyIsInvalid(context)) {
            return;
        }
        Intent i = new Intent(SdkEntry.MSG_EXPORT);
        i.putExtra(SdkEntry.EXPORT_WITH_WATERMARK, withWatermark);
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
            if (appKeyIsInvalid(context)) {
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
        if (appKeyIsInvalid(context)) {
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
        StickerData.getInstance().close();
        SDMusicData.getInstance().close();
        WebMusicData.getInstance().close();
        DraftData.getInstance().close();
        RecorderCore.onDestory();
        RdVECore.recycle();
        ImageCacheUtils.getInstance(context).recycle();
        SdkService server = getSdkService();
        RdHttpClient.ShutDown();
        if (null != server) {
            server.reset();
        }
        sdkService = null;
        if (CacheManager.getInstance() != null)
            CacheManager.getInstance().close();
        ThreadPoolUtils.executeEx(new Runnable() {

            @Override
            public void run() {
                PathUtils.clearTemp();
            }
        });
    }

    private static int MAX_MEM = 30 * ByteConstants.MB;
    public static final int MAX_DISK_CACHE_SIZE = 300 * ByteConstants.MB;

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
        OkHttpClient okHttpClient = MyOkHttpImagePipelineConfigFactory.getHttpClient();  // build on your own
        ImagePipelineConfig.Builder builder = null;
        if (null != okHttpClient) {
            //支持https
            builder = MyOkHttpImagePipelineConfigFactory.newBuilder(context, okHttpClient);
        } else {
            //只支持http
            builder = ImagePipelineConfig.newBuilder(context);
        }
        builder.setBitmapMemoryCacheParamsSupplier(mSupplierMemoryCacheParams);
        File cache = getExternalCacheDir(context);
        builder.setMainDiskCacheConfig(DiskCacheConfig.newBuilder(context)
                .setBaseDirectoryPath(cache)
                .setBaseDirectoryName("fresco")
                .setMaxCacheSize(MAX_DISK_CACHE_SIZE)
                .build());
        builder.setDownsampleEnabled(true);
        return builder.build();
    }


    public static File getExternalCacheDir(Context context) {
        return context.getExternalCacheDir();
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
    public static void exportVideo(Context context, VideoConfig videoConfig,
                                   ArrayList<String> videoList, String outPath,
                                   Watermark watermark, Trailer trailer,
                                   final ExportListener exportListener) throws InvalidArgumentException {
        if ((null != context) && !TextUtils.isEmpty(outPath)
                && (null != videoList && videoList.size() > 0)) {

            Scene scene = VirtualVideo.createScene();
            int len = videoList.size();
            for (int i = 0; i < len; i++) {
                scene.addMedia(videoList.get(i));
            }

            exportVideo = new VirtualVideo();
            exportVideo.addScene(scene);
            if (watermark != null) {
                exportVideo.setWatermark(watermark);
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
     * 草稿箱视频
     *
     * @param context
     * @return
     */
    public static List<IShortVideoInfo> getDraftList(Context context) {
        if (appKeyIsInvalid(context)) {
            return null;
        }
        DraftData.getInstance().initilize(context);
        return DraftData.getInstance().getAll();

    }

    /**
     * 删除草稿视频
     *
     * @param context
     * @param shortVideoInfo
     * @return
     */
    public static boolean deleteDraft(Context context, final IShortVideoInfo shortVideoInfo) {
        if (appKeyIsInvalid(context) || null == shortVideoInfo) {
            return false;
        }
        DraftData.getInstance().initilize(context);
        boolean re = DraftData.getInstance().delete(shortVideoInfo.getId()) == 1;
        if (shortVideoInfo instanceof ShortVideoInfoImp) {
            ThreadPoolUtils.executeEx(new Runnable() {
                @Override
                public void run() {
                    //清理草稿箱文件夹数据
                    ((ShortVideoInfoImp) shortVideoInfo).deleteData();
                }
            });

        }
        return re;

    }


    /**
     * 继续编辑草稿箱视频
     *
     * @param context
     * @param shortVideoInfo
     * @param requestCode
     * @return
     */
    public static boolean onEditDraft(Context context, IShortVideoInfo shortVideoInfo, int requestCode) throws InvalidArgumentException {

        if (appKeyIsInvalid(context) || null == shortVideoInfo) {
            return false;
        }
        ShortVideoInfoImp shortVideoInfoImp = (ShortVideoInfoImp) shortVideoInfo;
        if (!shortVideoInfoImp.isExit()) {
            throw new InvalidArgumentException("MediaObject is deleted...");
        }

        SdkEntry.getSdkService().initConfiguration(shortVideoInfoImp.getExportConfiguration(), shortVideoInfoImp.getUIConfiguration());
        Intent intent;
        if (getSdkService().getUIConfig().isEnableWizard()) {
            intent = new Intent(context, EditPreviewActivity.class);
        } else {
            intent = new Intent(context, VideoEditActivity.class);
        }
        intent.putExtra(IntentConstants.INTENT_EXTRA_DRAFT, shortVideoInfo.getId());
        if (context instanceof Activity) {
            ((Activity) context).startActivityForResult(intent, requestCode);
            ((Activity) context).overridePendingTransition(0, 0);
        } else {
            context.startActivity(intent);
        }
        return true;
    }


    /**
     * 导出草稿箱视频
     *
     * @param context
     * @param shortVideoInfo 草稿箱视频
     * @param exportListener 回调导出进度
     * @return 返回目标文件路径
     */
    public static String onExportDraft(Context context, IShortVideoInfo shortVideoInfo, ExportListener exportListener) throws InvalidArgumentException {
        return onExportDraft(context, shortVideoInfo, exportListener, true);
    }

    /***
     *导出草稿箱视频
     * @param context
     * @param shortVideoInfo 草稿箱视频
     * @param exportListener  回调导出进度
     * @param withWatermark  是否显示水印
     * @return 返回目标文件路径
     * @throws InvalidArgumentException
     */
    public static String onExportDraft(Context context, IShortVideoInfo shortVideoInfo, ExportListener exportListener, boolean withWatermark) throws InvalidArgumentException {

        if (appKeyIsInvalid(context) || null == shortVideoInfo || null == exportListener) {
            return null;
        }
        exportVideo = new VirtualVideo();
        return new ExportHandler(context).export(exportVideo, shortVideoInfo, exportListener, withWatermark);
    }


    /**
     * 取消导出
     */
    public static void cancelExport() {
        if (null != exportVideo) {
            exportVideo.cancelExport();
        }


    }

    /**
     * 是否是精简版
     *
     * @return true 精简版；false 非精简版
     */
    public static boolean isLite(Context context) {
        if (mIsInitialized) {
            return isLite;
        }
        return true;
    }

    /**
     * 是否支持ae 相关 （屏蔽asset中部分资源）
     *
     * @return true 精简版，false 完整版
     */
    private static boolean checkIsLite(Context context) {
        String[] src = new String[]{"jyMV", "music", "tantan", "tantan9-16", "tantan16-9"};
        try {
            String[] arr = context.getAssets().list("");
            if (null != arr) {
                int len = src.length;
                boolean isAllExit = false;
                for (int i = 0; i < len; i++) {
                    if (isEixt(src[i], arr)) {
                        isAllExit = true;
                    } else {
                        break;
                    }
                }

                if (isAllExit) {
                    return false;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * 是否存在此资源
     *
     * @param src
     * @return
     */
    private static boolean isEixt(String src, String[] arr) {
        int len = arr.length;
        for (int i = 0; i < len; i++) {
            if (!TextUtils.isEmpty(src) && arr[i].equals(src)) {
                return true;
            }
        }
        return false;
    }


}
