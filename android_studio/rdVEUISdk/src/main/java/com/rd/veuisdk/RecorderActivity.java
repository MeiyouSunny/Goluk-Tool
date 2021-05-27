package com.rd.veuisdk;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.MediaStore;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.rd.lib.ui.ExtButton;
import com.rd.lib.ui.PreviewFrameLayout;
import com.rd.lib.ui.Rotatable;
import com.rd.lib.ui.RotateImageView;
import com.rd.lib.ui.RotateRelativeLayout;
import com.rd.lib.utils.CoreUtils;
import com.rd.lib.utils.LogUtil;
import com.rd.lib.utils.ThreadPoolUtils;
import com.rd.recorder.AudioPlayer;
import com.rd.recorder.api.IRecorderCallBackShot;
import com.rd.recorder.api.RecorderConfig;
import com.rd.recorder.api.RecorderCore;
import com.rd.recorder.api.ResultConstants;
import com.rd.vecore.VirtualVideo;
import com.rd.vecore.exception.InvalidArgumentException;
import com.rd.vecore.listener.ExportListener;
import com.rd.vecore.models.MediaObject;
import com.rd.vecore.models.VideoConfig;
import com.rd.vecore.utils.ExportUtils;
import com.rd.vecore.utils.Log;
import com.rd.vecore.utils.MiscUtils;
import com.rd.veuisdk.database.HistoryMusicCloud;
import com.rd.veuisdk.faceu.FaceuHandler;
import com.rd.veuisdk.faceu.IReloadListener;
import com.rd.veuisdk.manager.CameraConfiguration;
import com.rd.veuisdk.manager.UIConfiguration;
import com.rd.veuisdk.manager.VEOSDBuilder;
import com.rd.veuisdk.manager.VEOSDBuilder.OSDState;
import com.rd.veuisdk.model.AudioMusicInfo;
import com.rd.veuisdk.ui.GlTouchView;
import com.rd.veuisdk.ui.GlTouchView.CameraCoderViewListener;
import com.rd.veuisdk.utils.AppConfiguration;
import com.rd.veuisdk.utils.CheckSDSize;
import com.rd.veuisdk.utils.FileUtils;
import com.rd.veuisdk.utils.IntentConstants;
import com.rd.veuisdk.utils.PathUtils;
import com.rd.veuisdk.utils.StorageUtils;
import com.rd.veuisdk.utils.SysAlertDialog;
import com.rd.veuisdk.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.rd.veuisdk.R.id.rlSelectRecOrPhoto1;
import static com.rd.veuisdk.R.id.rlSelectRecOrPhoto2;

/**
 * 相机
 *
 * @author abreal
 */
@TargetApi(23)
public class RecorderActivity extends AbstractRecordActivity {

    private final int BUTTON_STATE_START = 0;
    private final int BUTTON_STATE_LIVING = 1;
    private final int BUTTON_STATE_PAUSE = 2;
    static final String ACTION_TO_EDIT = "action_to_edit";
    private MyOrientationEventListener mOrientationListener;
    /**
     * 修正的方向度数(90的倍数）
     */
    private int mOrientationCompensation = 0;
    /**
     * mBtnSwitchCamera（切换前后摄像头）
     */
    private RotateImageView mBtnCancelRecord, mBtnCancelRecord1, mBtnBeauty,
            mBtnBeauty1, mBtnSwitchCamera, mBtnSwitchCamera1,
            mBtnBlackScreen1, mBtnAddMusic, mBtnAddMusic1;
    private ImageView mBtnDelMusic, mBtnDelMusic1;
    /**
     * 闪光灯
     */
    private RotateImageView mBtnConfig, mBtnConfig1, mBtnFlashModeCtrl, mBtnFlashModeCtrl1;
    private RotateRelativeLayout mRlFilterList, mSelectRec1, mSelectMV1,
            mSelectMV2, mRecordRRL, mSelectRec2, mSelectPhoto1,
            mSelectPhoto2, mBtnBottomRightForLandscape,
            mBtnBottomRight, mBtnBottomLeftLayout, mTimerTv;

    private RotateImageView mBtnShootingRatio, mBtnShootingRatio1;

    /**
     * 控制特效是否显示滤镜列表
     */
    private ExtButton mBtnBottomLeft;
    private Button mBtnBottomLeftForSquare, mBtnRecord, mBtnRecord1;
    private RotateImageView mBtncloseFilterList;
    private boolean enableFace = false;
    /**
     * 录制与暂停录制状态
     */
    protected boolean mIsRecording;
    /**
     * mBtnRecord（拍摄按钮） m_btnCloseOrPauseRecord（关闭与暂停按钮）
     * m_btnQualityOrBlackScreen(黑屏与质量按钮)
     */
    protected ExtButton mBtnSelectPhoto1;
    protected RotateImageView m_btnBlackScreen;
    /**
     * 摄像头特效handler
     */
    private CameraEffectHandler mCameraEffectHandler;
    private String mLocalSaveFileNameStr, mLocalSavePicNameStr;

    /**
     * 录制黑屏,铺满
     */
    protected FrameLayout mLayoutBlackScreen;
    /**
     * 控制黑屏显示
     */
    protected GestureDetector mGdBlackScreen;
    /**
     * 使用框架接口MediaRecorder录制
     */
    protected boolean mUseMediaRecorder = false;
    private ImageView mIvOpenCamAnimTop, mIvOpenCamAnimBottom;

    private boolean gotoEdit = false;// 是否进入高级编辑界
    private boolean editResult = false;// 此值用于sdk内部媒体选择界面的拍照录像使用
    private boolean mGoTakePhotoMode = false;// 此值用于启动录像还是照片模式
    private int mLandscapeMode = -1; // 1代表为横屏模式,0代表为竖屏模式，-1代表未知
    /**
     * 摄像头初始化成功
     */
    private boolean bSelectPhoto;
    private GlTouchView mGlTouchView;
    /**
     * 变焦handler
     */
    private CameraZoomHandler m_hlrCameraZoom;
    private PreviewFrameLayout mRlframeSquarePreview;
    private RelativeLayout
            mRecordingCameraMoreBar, mVideoNewRelative,
            mVideoNewRelative1, mRllivingBar0;
    private LinearLayout mRecordingBar90, mRecordingBar270;

    private RotateImageView mBtnCancelRecord90, mBtnShootingRatio90,
            mBtnWating90, mBtnCancelRecord270, mBtnShootingRatio270,
            mBtnWating270, mBtnBlackScreen90, mBtnBlackScreen270;
    private RelativeLayout mSquareCameraLayout, mScreenCameraLayout,
            mLayoutSelectRecOrPhoto1, mLayoutSelectRecOrPhoto2;
    private int m_DisplayWidth;
    private int mTotalWidth = 0;
    /***
     * 录制mv时 闪现光标图片
     */
    private ImageView mImgFlashMVScreen, mImgFlashMVSquare;
    /**
     * 录制进度控件
     */
    private LinearLayout mLinearSeekbar, mLinearSeekbar1, mllAddMusic, mllAddMusic1;

    private ExtButton m_btnBottomRight, m_btnBottomRightForSquare, m_btnBottomRightForLandscape;
    private ArrayList<MediaObject> mRecordVideoList = new ArrayList<>();
    private IRecoder iListener;
    /**
     * 视频录制时间限制 单位为ms 0代表没有时间限制
     */
    private int mVideoMaxTime = 0;
    /**
     * 视频录制最小时间限制 单位为ms 0代表没有时间限制
     */
    private int mVideoMinTime = 0;
    /**
     * 录制界面启动默认页面 0代表全屏界面可切换到1：1界面 1代表1：1界面可切换到全屏界面 2代表只能使用1：1界面
     */
    private int mUIType = 0;
    /**
     * mv最大时长
     */
    private int mMVMaxTime = 0;
    /**
     * mv最小时长
     */
    private int mMVMinTime = 0;
    private boolean mUseMultiShoot = false;
    private boolean mIsSaveToAlbum = false;
    private boolean enableRecPhotoSwitch = true;
    private int buttonState = BUTTON_STATE_START;
    private boolean hideAlbum = false, isEncryption = false;
    private boolean enableWatermark = false;
    private int trailerTime = 0;
    private int osdHeader = 0;
    private int osdEnd = 0;
    // 开始录制片尾
    private boolean startTrailer = false;

    private boolean lastEnableBeauty = false;
    // 功能隐藏
    private boolean hideMV = false;
    private boolean hideRec = false;
    private boolean hidePhoto = false;

    private final int POSITION_MV = 0;
    private final int POSITION_REC = 1;
    private final int POSITION_PHOTO = 2;

    // mv 视频 拍照状态保存
    private int curPosition = POSITION_REC;
    private CameraConfiguration cameraConfig;
    private boolean enableFrontMirror = false;
    //混音播放器
    private AudioPlayer mAudioPlayer;
    private AudioMusicInfo mAudioMusic;
    private boolean isPause;
    private TextView mTvMusicNameScreen, mTvMusicNameSquare;
    //当前被应用的滤镜下标
    private int mCurrentEffectIndex = 0;

    private boolean hasJben_MR2 = false;
    private boolean enableLockScreen = false;

    private FaceuHandler faceUnityHandler;

    //录制模式（长方形(true)、正方形(false)）
    private boolean isFullScreen = true;
    private PreviewFrameLayout cameraPreview;
    private RelativeLayout cameraParent;
    private int mSquareTitlebarHeight = 0;

    //录制方向
    private int mRecordOrientation = CameraConfiguration.ORIENTATION_AUTO;
    private RecyclerView mRecyclerViewFilter;
    private LinearLayout mStrengthLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        TAG = "RecorderActivity";
        CoreUtils.hideVirtualBar(this);
        super.onCreate(savedInstanceState);
        mCurrentEffectIndex = 0;
        hasJben_MR2 = CoreUtils.hasJELLY_BEAN_MR2();//4.3以下不支持切换录制模式
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        lastEnableBeauty = AppConfiguration.enableBeauty();

        gotoEdit = getIntent().getBooleanExtra(ACTION_TO_EDIT, false);
        editResult = getIntent().getBooleanExtra(
                IntentConstants.EDIT_CAMERA_WAY, false);

        cameraConfig = SdkEntry.getSdkService().getCameraConfig();
        lastEnableBeauty = cameraConfig.enableBeauty;
        enableFrontMirror = cameraConfig.enableFrontMirror;
        RecorderCore.setPreviewCallBack(null);
        RecorderCore.setTextureCallBack(null);
        RecorderCore.enableFaceU(false);
        mVideoMaxTime = cameraConfig.videoMaxTime * 1000; // 外部传入是秒为单位，转换一下s->ms
        mVideoMinTime = cameraConfig.videoMinTime * 1000;
        mMVMaxTime = cameraConfig.cameraMVMaxTime * 1000;
        if (mMVMaxTime == 0) {
            mMVMaxTime = 15 * 1000;
        }
        mMVMinTime = cameraConfig.cameraMVMinTime * 1000;

        mUIType = cameraConfig.cameraUIType;
        mUseMultiShoot = cameraConfig.useMultiShoot;
        mIsSaveToAlbum = cameraConfig.isSaveToAlbum;
        enableWatermark = cameraConfig.enableWatermark;

        if (cameraConfig.dafaultRearCamera) {//每次进入录制界面由外面控制是否前置
            isFrontCamera = false;
        } else {
            isFrontCamera = true;
        }
        osdHeader = Math.max(0,
                Math.min(2000, (int) (cameraConfig.cameraOsdHeader * 1000)));
        osdEnd = Math.max(0,
                Math.min(2000, (int) (cameraConfig.cameraOsdEnd * 1000)));
        trailerTime = osdEnd;

        hideAlbum = !cameraConfig.enableAlbum;
        isEncryption = cameraConfig.enableAntiChange;
        enableFace = cameraConfig.enableFaceU;
        byte[] pack = cameraConfig.pack;

        hideMV = cameraConfig.hideMV;
        hideRec = cameraConfig.hideRec;
        hidePhoto = cameraConfig.hidePhoto;

        if (hideRec) {
            curPosition = POSITION_MV;
            if (hideMV) {
                curPosition = POSITION_PHOTO;
            }
        }
        if (editResult) {
            mUseMultiShoot = false;
            hideAlbum = true;
        }
        setContentView(R.layout.main_camera);
        mRecyclerViewFilter = $(R.id.recyclerViewFilter);
        mStrengthLayout = $(R.id.strengthLayout);
        mOrientationListener = new MyOrientationEventListener(this);
        if (CoreUtils.hasIceCreamSandwich()) {
            // 响应退出虚拟键
            Utils.getRootView(this).setOnSystemUiVisibilityChangeListener(
                    new View.OnSystemUiVisibilityChangeListener() {

                        @Override
                        public void onSystemUiVisibilityChange(int visibility) {
                            if (visibility != View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) {
                                mLayoutBlackScreen.setVisibility(View.GONE);
                            }
                        }
                    });
        }

        initLayouts();

        mRecordOrientation = CameraConfiguration.ORIENTATION_AUTO;
        if (editResult) {
            mSquareCameraLayout.setVisibility(View.INVISIBLE);
            mScreenCameraLayout.setVisibility(View.VISIBLE);
            isFullScreen = true;

            if (mUIType == CameraConfiguration.WIDE_SCREEN_CAN_CHANGE) {
                //默认16：9
                mRecordOrientation = cameraConfig.orientation;
            } else if (mUIType == CameraConfiguration.SQUARE_SCREEN_CAN_CHANGE) {
                // 可自动切换时，16:9锁屏有效
                mRecordOrientation = cameraConfig.orientation;
            } else if (mUIType == CameraConfiguration.ONLY_WIDE_SCREEN) {
                // 仅全屏录制时，锁屏有效
                mRecordOrientation = cameraConfig.orientation;
            }

        } else {
            if (mUIType == CameraConfiguration.WIDE_SCREEN_CAN_CHANGE) {
                mSquareCameraLayout.setVisibility(View.INVISIBLE);
                mScreenCameraLayout.setVisibility(View.VISIBLE);
                isFullScreen = true;
                //默认16：9
                mRecordOrientation = cameraConfig.orientation;
            } else if (mUIType == CameraConfiguration.SQUARE_SCREEN_CAN_CHANGE) {
                mScreenCameraLayout.setVisibility(View.INVISIBLE);
                mSquareCameraLayout.setVisibility(View.VISIBLE);
                isFullScreen = false;
                // 可自动切换时，16:9锁屏有效
                mRecordOrientation = cameraConfig.orientation;
            } else if (mUIType == CameraConfiguration.ONLY_SQUARE_SCREEN) {
                mScreenCameraLayout.setVisibility(View.INVISIBLE);
                mSquareCameraLayout.setVisibility(View.VISIBLE);
                mBtnShootingRatio1.setVisibility(View.GONE);
                isFullScreen = false;
            } else if (mUIType == CameraConfiguration.ONLY_WIDE_SCREEN) {
                mScreenCameraLayout.setVisibility(View.VISIBLE);
                mSquareCameraLayout.setVisibility(View.INVISIBLE);
                mBtnShootingRatio.setVisibility(View.GONE);
                mBtnShootingRatio90.setVisibility(View.GONE);
                mBtnShootingRatio270.setVisibility(View.GONE);
                isFullScreen = true;
                // 仅全屏录制时，锁屏有效
                mRecordOrientation = cameraConfig.orientation;
            }
        }
        if (!hasJben_MR2) {//4.3以下只支持竖屏全屏(16:9)
            mScreenCameraLayout.setVisibility(View.VISIBLE);
            mSquareCameraLayout.setVisibility(View.INVISIBLE);
            mUIType = CameraConfiguration.ONLY_WIDE_SCREEN;
            isFullScreen = true;
            mRecordOrientation = cameraConfig.orientation;
        }
        //检查 相机权限
        checkPermission();
        goPreviewByCameraSizeMode();
        changeLayoutWithOrientation(0);
        if (editResult) {
            mGoTakePhotoMode = getIntent().getBooleanExtra(
                    IntentConstants.DEFAULT_OPEN_PHOTO_MODE, false);
            if (mGoTakePhotoMode)// 切换到照片模式
            {
                glListener.onSwitchFilterToLeft();
            }
        }

        if (!hasJben_MR2)// 4.3之前的系统不使用1：1比例录制，画面会被压扁
        {
            mBtnShootingRatio.setVisibility(View.GONE);
            mBtnShootingRatio90.setVisibility(View.GONE);
            mBtnShootingRatio270.setVisibility(View.GONE);
        }
        faceUnityHandler = new FaceuHandler(this,
                ((RadioGroup) $(R.id.camare_filter_s)),
                $(R.id.filterLayout), $(R.id.rg_menu_parent),
                (enableFace ? pack : null), SdkEntry.getSdkService()
                .getFaceUnityConfig(), (LinearLayout) $(R.id.fuLayout),
                (LinearLayout) $(R.id.fuLayout_parent), ((LinearLayout) $(R.id.filter_parent_layout)), new IReloadListener() {
            @Override
            public void onReloadFilters(boolean isVer) {
                if (bCameraPrepared) {
                    if (null != mCameraEffectHandler) {
                        if (mCameraEffectHandler.isLookup()) {
                            mCameraEffectHandler.notifyDataSetChanged(isVer);
                        } else {
                            List<String> effects = RecorderCore.getSupportedColorEffects();
                            mCameraEffectHandler.initAllEffects(isVer, mRecyclerViewFilter, mStrengthLayout, effects, mCurrentEffectIndex);
                        }
                    }
                }
            }
        });
        // 仅全屏录制时， 确定是否锁屏
        if (mUIType == CameraConfiguration.ONLY_WIDE_SCREEN
                || mUIType == CameraConfiguration.SQUARE_SCREEN_CAN_CHANGE
                || mUIType == CameraConfiguration.WIDE_SCREEN_CAN_CHANGE) {
            onCheckLock(mUIType == CameraConfiguration.SQUARE_SCREEN_CAN_CHANGE
                    || mUIType == CameraConfiguration.ONLY_SQUARE_SCREEN);
        }


    }

    /***
     * 当前状态是否是正方形
     *
     * @param nowIsSQUARE
     */
    private void onCheckLock(boolean nowIsSQUARE) {
        tempVideoOrientaion = VIDEO_OUT_ORIENTAION;
        if (mRecordOrientation == CameraConfiguration.ORIENTATION_AUTO) {
            enableLockScreen = false;// zidong
        } else if (mRecordOrientation == CameraConfiguration.ORIENTATION_PORTRAIT) {
            enableLockScreen = true;
            onVerOHor(true);// 强制锁定竖屏(不论是(默认1：1再 16：9) 、还是 (仅全屏) )
        } else {
            enableLockScreen = true;
            onVerOHor((!nowIsSQUARE) ? false : true);
        }
    }


    @Override
    public void setRequestedOrientation(int orientation) {
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }


    /**
     * 初始化录制视频的父容器大小和编码视频的参数
     */
    private void initCameraLayout() {
        if (hasJben_MR2 && enableFace) {
            faceUnityHandler.registerCallBack();
        }
        if (null != faceUnityHandler) {
            faceUnityHandler.setFuNotifyPause(true);
        }

        if (null == cameraParent) {
            cameraParent = $(R.id.cameraParentLayout);
            cameraPreview = $(R.id.cameraPreviewLayout);
            mSquareTitlebarHeight = getResources().getDimensionPixelSize(R.dimen.record_titlebar_height);
        }
        FrameLayout.LayoutParams mCameraParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);


        if (isFullScreen) {
            //初始化全屏模式
            //设置录制父容器的大小
            mCameraParams.setMargins(0, 0, 0, 0);
            cameraPreview.setLayoutParams(mCameraParams);
            //全面屏
            cameraPreview.setAspectRatio(0f);
        } else {
            //初始化正方形模式
            //设置录制父容器的大小
            mCameraParams.setMargins(0, mSquareTitlebarHeight, 0, 0);
            cameraPreview.setLayoutParams(mCameraParams);
            cameraPreview.setAspectRatio(1.0f);
        }
        //录制参数
        VirtualVideo.Size size = AppConfiguration.getRecorderSize(!isFullScreen);
        RecorderConfig config = new RecorderConfig().setVideoSize(size.getWidth(), size.getHeight()).
                setVideoFrameRate(cameraConfig.getRecordVideoFrameRate())
                .setVideoBitrate(AppConfiguration.getRecorderBitrate() * 1000)
                .setEnableFront(isFrontCamera)
                .setEnableBeautify(canBeautiy).setBeauitifyLevel(5)
                .setEnableFrontMirror(enableFrontMirror)
                .setKeyFrameTime(cameraConfig.recordVideoKeyFrameTime)
                .setEnableAutoFocus(true).setEnableAutoFocusRecording(false);
        RecorderCore.setEncoderConfig(config);
        RecorderCore.enableFaceU(enableFace);
        if (!bRecordPrepared) {
            iListener = new IRecoder();
            bRecordPrepared = true;
            //清理，防止之前已经初始过
            RecorderCore.recycleCameraView();
            //准备录制界面
            RecorderCore.onPrepare(cameraParent, iListener);

            //设置放大缩小Handler
            RecorderCore.setCameraZoomHandler(m_hlrCameraZoom);
            //是否静音
            RecorderCore.setMute(cameraConfig.audioMute);
        }
        //防止离开绘制道具，造成道具画面闪硕

        if (null != faceUnityHandler) {
            faceUnityHandler.setFuNotifyPause(false);
        }


    }

    /**
     * * 初始化长方形录制
     */
    private void onInitializeScreenRecorder() {
        switchRecOrPhotoItemLayout();
        switchRecOrPhoto();

        if (RecorderCore.isRegistedOsd()) {
            RecorderCore.registerOSD(null);
        }
        isFullScreen = true;
        initCameraLayout();


    }

    private void onRegisterOsd() {
        if (RecorderCore.isRegistedOsd()) {
            RecorderCore.registerOSD(null);
        }
        if (enableWatermark) {// 必须在主线程创建
            osd = SdkEntry.createOSDBuilder(RecorderActivity.this, (!isFullScreen));
            if (null != osd) {
                osd.setOSDState(OSDState.header);
                RecorderCore.registerOSD(osd);
            }

        }
    }

    /**
     * 设置当前录制进度
     *
     * @param progress
     */
    private void setRecordOSDProgress(int progress) {
        if (null != osd) {
            if (progress >= osdHeader) {
                if (osd.mState != OSDState.end) {
                    if (osd.mState != OSDState.recording) {
                        osd.setOSDState(OSDState.recording);
                    }
                    osd.recorderTime = progress;
                }
            }
        }
    }

    /**
     * 正方形录制
     */
    private void onInitializeSquareRecorder() {
        switchRecOrPhotoItemLayout();
        switchRecOrPhoto();
        //清除水印
        RecorderCore.registerOSD(null);
        isFullScreen = false;
        initCameraLayout();

    }

    /**
     * 摄像头授权成功，初始化摄像头
     */
    @Override
    public void onCameraPermissionGranted() {
        exportDefaultMusic();
        initDefaultMusic();
        if ((!isFullScreen)) {
            onInitializeSquareRecorder();
        } else {
            onInitializeScreenRecorder();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        needPostRecycleCameraView = false;
        if (null != faceUnityHandler) {
            faceUnityHandler.setFuNotifyPause(false);
        }
        try {
            mLayoutBlackScreen.setVisibility(View.INVISIBLE);// 取消黑屏
            // 显示切换摄像头按钮，即是否为多个摄像头，
            mBtnSwitchCamera.setVisibility(View.VISIBLE);
            if (!enableLockScreen) {
                mOrientationListener.enable();
            }
            if (AppConfiguration.isTrainingCaptureVideo()) {
                AppConfiguration.setTrainingCaptureVideo(true);
            }
        } catch (Exception ex) {
        }

    }

    @Override
    public void onPause() {
        mOrientationListener.disable();
        if (mIsRecording) {
            stopLiveOrRecordStream(false);
        }
        super.onPause();
        if (null != faceUnityHandler) {
            faceUnityHandler.onPasue();
        }
        if (null != mHandler && null != m_runnableWaiting) {
            mHandler.removeCallbacks(m_runnableWaiting);
        }
        mBtnConfig.setEnabled(true);
        mBtnConfig1.setEnabled(true);

    }

    private boolean needPostRecycleCameraView = false;

    @Override
    protected void onStop() {
        super.onStop();
        if (!isFinish && permissionGranted) {
            if (mIsRecording) {
                needPostRecycleCameraView = true;
            } else {
                needPostRecycleCameraView = false;
                RecorderCore.recycleCameraView();
            }
        }
        mIsRecording = false;
        if (null != mHandler) {
            mHandler.removeCallbacks(mRunnableEffect);
            if (null != m_runnableWaiting) {
                mHandler.removeCallbacks(m_runnableWaiting);
            }
            if (null != runnable) {
                handler.removeCallbacks(runnable);
            }
        }
    }

    private Runnable mRunnableEffect = new Runnable() {
        @Override
        public void run() {
            onCheckEffect();
        }
    };

    @Override
    protected void onDestroy() {
        if (null != mHandler && null != m_runnableWaiting) {
            mHandler.removeCallbacks(m_runnableWaiting);
        }
        releaseAudioPlayer();
        if (null != mGlTouchView) {
            mGlTouchView.setViewHandler(null);
            mGlTouchView.setZoomHandler(null);
            mGlTouchView = null;

        }
        m_hlrCameraZoom = null;
        glListener = null;

        mOrientationListener = null;
        if (null != mCameraEffectHandler) {
            mCameraEffectHandler.recycle();
            mCameraEffectHandler = null;
        }
        if (null != faceUnityHandler) {
            faceUnityHandler.unRegister();
            faceUnityHandler.onDestory();
            faceUnityHandler = null;
        }
        osd = null;
        iListener = null;
        if (null != mIvOpenCamAnimTop) {
            mIvOpenCamAnimTop.setImageResource(0);
            mIvOpenCamAnimTop = null;
        }
        if (null != mIvOpenCamAnimBottom) {
            mIvOpenCamAnimBottom.setImageResource(0);
            mIvOpenCamAnimBottom = null;
        }

        super.onDestroy();
        System.gc();// 用System.gc()的时候有时不一定会去处理垃圾
        System.runFinalization();
        photolp = null;
        reclp = null;
        mvlp = null;
    }

    @Override
    public void onBackPressed() {
        if (m_bIsWaiting) {
            cancelWaitingRecord();
            return;
        }
        if (RecorderCore.isRecording()) {
            return;
        }
        gotoEdit = false;
        stopLiveOrRecordStream(false);
        ThreadPoolUtils.execute(new Runnable() {
            public void run() {
                // 先删除临时文件
                for (MediaObject mo : mRecordVideoList) {
                    Utils.cleanTempFile(mo.getMediaPath());
                }
                Utils.cleanTempFile(mLocalSaveFileNameStr);
            }
        });
        finish();


    }

    /**
     * 给支持方向的按钮等，指定方向
     *
     * @param nOrientation 0 90 180 etc...
     */
    protected void setOrientationIndicator(int nOrientation) {
        tempOrientation = nOrientation;
        // if (!mIsRecording) // 录制进行中时不进行界面旋转同步
        {
            tempVideoOrientaion = nOrientation;
            RecorderCore.setOrientation(nOrientation);
            final Rotatable[] arrRotatable = {
                    mSelectMV1,
                    mSelectRec1,
                    mSelectPhoto1,
                    mRecordRRL,
                    mBtnBeauty,
                    m_btnWaiting,
                    mBtnSwitchCamera,
                    mBtnFlashModeCtrl,
                    mBtnBottomLeft,
                    mBtnCancelRecord,
                    mRlFilterList,// mLvCameraFilter,//m_rrlIndicatorBar,
                    m_btnBottomRightForLandscape, m_btnBottomRight,
                    m_btnBlackScreen, mTimerTv, mBtnCancelRecord90,
                    mBtnShootingRatio90, mBtnWating90, mBtnCancelRecord270,
                    mBtnShootingRatio270, mBtnWating270, mBtnBlackScreen90,
                    mBtnBlackScreen270};
            boolean bSetLanscape = nOrientation == 90 || nOrientation == 270;

            for (int nTmp = 0; nTmp < arrRotatable.length; nTmp++) {
                if (nOrientation != 180) {
                    if (arrRotatable[nTmp] != null) {
                        arrRotatable[nTmp].setOrientation(nOrientation);
                    }
                }
            }

            if (mLandscapeMode == -1
                    || (mLandscapeMode == 1) != bSetLanscape) {
                mLandscapeMode = bSetLanscape ? 1 : 0;
            }

            changeLayoutWithOrientation(nOrientation);
            if (null != faceUnityHandler) {
                faceUnityHandler.setOrientation(nOrientation);
            }
        }
    }

    private void changeLayoutWithOrientation(int nOrientation) {
        int dp2px30 = CoreUtils.dip2px(this, 30);
        if (nOrientation == 90) {
            mRecordingCameraMoreBar.setVisibility(View.INVISIBLE);
            // 滤镜按钮
            RelativeLayout.LayoutParams lp1 = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            lp1.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            lp1.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            lp1.bottomMargin = dp2px30;
            lp1.leftMargin = 0;
            lp1.rightMargin = dp2px30;
            mBtnBottomLeftLayout.setLayoutParams(lp1);// m_btnFilterListControl


            mBtnBottomRight.setVisibility(View.GONE);

            // 横屏删除按钮
            RelativeLayout.LayoutParams lp4 = new RelativeLayout.LayoutParams(
                    CoreUtils.dip2px(this, 45), CoreUtils.dip2px(this, 45));
            lp4.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            lp4.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            lp4.leftMargin = CoreUtils.dip2px(this, 72);
            lp4.rightMargin = 0;
            lp4.bottomMargin = CoreUtils.dip2px(this, 30);

            // 横屏完成OK按钮
            RelativeLayout.LayoutParams lp3 = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            lp3.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            lp3.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            lp3.bottomMargin = dp2px30;
            lp3.leftMargin = dp2px30;
            lp3.rightMargin = 0;
            mBtnBottomRightForLandscape.setLayoutParams(lp3);
            mBtnBottomRightForLandscape.setVisibility(View.VISIBLE);

            // 控制按钮排列显示
            mRllivingBar0.setVisibility(View.GONE);
            mRecordingBar90.setVisibility(View.VISIBLE);
            mRecordingBar270.setVisibility(View.GONE);

            // 时间显示
            RelativeLayout.LayoutParams lp5 = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            lp5.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            lp5.topMargin = CoreUtils.dip2px(this, 200);
            lp5.rightMargin = CoreUtils.dip2px(this, 0);
            mTimerTv.setLayoutParams(lp5);

            ((TextView) $(R.id.tvItembtnSelectMVCaption2))
                    .setText(getString(R.string.m_short_mv, mMVMaxTime / 1000));
            ((TextView) $(R.id.tvItembtnSelectMVCaption1))
                    .setText(getString(R.string.m_short_mv, mMVMaxTime / 1000));
        } else if (nOrientation == 270) {
            //
            mRecordingCameraMoreBar.setVisibility(View.INVISIBLE);
            // 滤镜按钮
            RelativeLayout.LayoutParams lp1 = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            lp1.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            lp1.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            lp1.bottomMargin = dp2px30;
            lp1.leftMargin = dp2px30;
            lp1.rightMargin = 0;
            mBtnBottomLeftLayout.setLayoutParams(lp1);


            mBtnBottomRight.setVisibility(View.GONE);
            // 横屏删除按钮
            int dp2px45 = CoreUtils.dip2px(this, 45);
            RelativeLayout.LayoutParams lp4 = new RelativeLayout.LayoutParams(
                    dp2px45, dp2px45);
            lp4.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            lp4.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            lp4.leftMargin = 0;
            lp4.rightMargin = CoreUtils.dip2px(this, 72);
            lp4.bottomMargin = dp2px30;

            // 横屏完成OK按钮
            RelativeLayout.LayoutParams lp3 = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            lp3.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            lp3.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            lp3.bottomMargin = dp2px30;
            lp3.leftMargin = 0;
            lp3.rightMargin = dp2px30;
            mBtnBottomRightForLandscape.setLayoutParams(lp3);
            mBtnBottomRightForLandscape.setVisibility(View.VISIBLE);

            // 控制按钮排列显示
            mRllivingBar0.setVisibility(View.GONE);
            mRecordingBar90.setVisibility(View.GONE);
            mRecordingBar270.setVisibility(View.VISIBLE);

            // 时间显示
            RelativeLayout.LayoutParams lp5 = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            lp5.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            lp5.topMargin = CoreUtils.dip2px(this, 200);
            lp5.rightMargin = CoreUtils.dip2px(this, 0);
            mTimerTv.setLayoutParams(lp5);

            ((TextView) $(R.id.tvItembtnSelectMVCaption2))
                    .setText(getString(R.string.m_short_mv, mMVMaxTime / 1000));
            ((TextView) $(R.id.tvItembtnSelectMVCaption1))
                    .setText(getString(R.string.m_short_mv, mMVMaxTime / 1000));
        } else if (nOrientation == 0) {
            // 滤镜按钮
            RelativeLayout.LayoutParams lp1 = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            lp1.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            lp1.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

            lp1.bottomMargin = dp2px30;
            lp1.leftMargin = dp2px30;
            lp1.rightMargin = 0;
            mBtnBottomLeftLayout.setLayoutParams(lp1);

            // 删除按钮
            RelativeLayout.LayoutParams lp2 = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            lp2.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            lp2.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            lp2.bottomMargin = dp2px30;
            lp2.leftMargin = 0;
            lp2.rightMargin = dp2px30;
            mBtnBottomRight.setLayoutParams(lp2);// m_rrlbtnDeleteVideo
            mBtnBottomRight.setVisibility(View.VISIBLE);
            // 横屏删除按钮
            RelativeLayout.LayoutParams lp4 = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            lp4.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            lp4.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            lp4.bottomMargin = dp2px30;
            lp4.leftMargin = 0;
            lp4.rightMargin = CoreUtils.dip2px(this, 72);

            // 横屏完成OK按钮
            RelativeLayout.LayoutParams lp3 = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            lp3.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            lp3.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            lp3.bottomMargin = dp2px30;
            lp3.leftMargin = 0;
            lp3.rightMargin = dp2px30;
            mBtnBottomRightForLandscape.setLayoutParams(lp3);
            mBtnBottomRightForLandscape.setVisibility(View.GONE);

            // 控制按钮排列显示
            mRllivingBar0.setVisibility(View.VISIBLE);
            mRecordingBar90.setVisibility(View.GONE);
            mRecordingBar270.setVisibility(View.GONE);

            // 时间显示
            RelativeLayout.LayoutParams lp5 = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            lp5.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            lp5.leftMargin = CoreUtils.dip2px(this, 8);
            lp5.topMargin = CoreUtils.dip2px(this, 12);
            mTimerTv.setLayoutParams(lp5);

            ((TextView) $(R.id.tvItembtnSelectMVCaption2))
                    .setText(getString(R.string.m_short_mv_no_line,
                            mMVMaxTime / 1000));
            ((TextView) $(R.id.tvItembtnSelectMVCaption1))
                    .setText(getString(R.string.m_short_mv_no_line,
                            mMVMaxTime / 1000));
        }

    }

    /**
     * 显示打开摄像头闸门动画
     */
    private void startOpenCamGate() {

        // 设置刷新所有参数，准备拍摄
        if (bCameraPrepared) {
            return;
        }
        closeCameraFailedDialog();

        Animation animForTop = AnimationUtils.loadAnimation(
                RecorderActivity.this, R.anim.slide_out_to_top);
        animForTop.setFillAfter(true);
        Animation animForBottom = AnimationUtils.loadAnimation(
                RecorderActivity.this, R.anim.slide_down_out);
        animForBottom.setFillAfter(true);
        mIvOpenCamAnimTop.startAnimation(animForTop);
        mIvOpenCamAnimBottom.startAnimation(animForBottom);
        bCameraPrepared = true;

    }

    private boolean isFinish = false;

    /**
     * 调用finish
     */
    private void finishCamGate() {
        int height = this.getWindowManager().getDefaultDisplay().getHeight();
        ViewGroup.LayoutParams lParams = mIvOpenCamAnimBottom.getLayoutParams();
        if (null != lParams) {
            lParams.height = height / 2 + 100;
            mIvOpenCamAnimBottom.setLayoutParams(lParams);
        }
        Animation animForTopDown = new TranslateAnimation(0.0f, 0.0f,
                -height / 2, 0.0f);
        animForTopDown.setDuration(400);

        Animation animForBottomUp = new TranslateAnimation(0.0f, 0.0f,
                height / 2, 0.0f);
        animForBottomUp.setDuration(400);

        animForTopDown.setFillAfter(true);

        animForBottomUp.setFillAfter(true);
        if (bCameraPrepared) {
            mIvOpenCamAnimTop.startAnimation(animForTopDown);
            mIvOpenCamAnimBottom.startAnimation(animForBottomUp);
        }
        mIvOpenCamAnimBottom.postDelayed(new Runnable() {

            @Override
            public void run() {
                isFinish = true;
                RecorderCore.recycleCameraView();
                releaseAudioPlayer();
                RecorderCore.onDestory();
                RecorderActivity.super.finish();
                RecorderActivity.this.overridePendingTransition(0, 0);
            }
        }, 300);
    }

    private void releaseAudioPlayer() {
        if (null != mAudioPlayer) {
            synchronized (mAudioPlayer) {
                if (null != mAudioPlayer) {
                    try {
                        mAudioPlayer.stop();
                        mAudioPlayer.release();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    } finally {
                        mAudioPlayer = null;
                    }
                }
            }
        }
    }

    /**
     * 开始直播或录制
     */
    private synchronized void startLiveOrRecordStream() {
        // 是否为竖屏
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                onRegisterOsd();
                mLocalSaveFileNameStr = PathUtils.getMp4FileNameForSdcard();

                RecorderCore.setOrientation(tempVideoOrientaion);
                try {
                    RecorderCore.startRecord(mLocalSaveFileNameStr);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

    }


    private void gotoEdit() {
        if (curPosition == POSITION_REC) {
            //最大最小时长很接近
            if (!bSelectPhoto && isInMin()) {
                String msg = getString(R.string.camera_min_limit_text, String.valueOf(mVideoMinTime / 1000));
                onAutoToast("", msg);
                return;
            }
        } else if (curPosition == POSITION_MV) {
            if (isInMin()) {
                String msg = getString(R.string.camera_min_limit_text, String.valueOf(mMVMinTime / 1000));
                onAutoToast("", msg);
                return;
            }

        }
        if (curPosition == POSITION_REC || curPosition == POSITION_MV) {
            //防止录制的video太短，无法预览
            if (curTotal < 800) {
                String msg = getString(R.string.camera_min_limit_text, String.valueOf(0.8));
                onAutoToast("", msg);
                return;
            }
        }

        if (enableWatermark) {
            if (startTrailer) {
                saveMedia();
            } else {
                startTrailer = true;

                if (trailerTime > 0 || osdEnd > 0) {
                    if (null != osd) {
                        osd.setOSDState(OSDState.end);
                    }
                    SysAlertDialog.showLoadingDialog(RecorderActivity.this,
                            null);
                    mLocalSaveFileNameStr = PathUtils
                            .getMp4FileNameForSdcard();
                    RecorderCore.setOrientation(tempVideoOrientaion);
                    try {
                        RecorderCore.startRecord(mLocalSaveFileNameStr);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return;
                }
            }
        } else {
            saveMedia();
        }

    }

    private void gotoEdit(MediaObject moEdit) {
        // 先删除临时文件
        for (MediaObject mo : mRecordVideoList) {
            if (!mo.equals(moEdit)) {
                Utils.cleanTempFile(mo.getMediaPath());
            }
        }
        ArrayList<MediaObject> list = new ArrayList<MediaObject>();
        list.add(moEdit);
        SdkEntry.gotoEdit(this, list, getIntent());
        finish();
    }

    /**
     * 执行保存 然后 前往编辑界面
     */
    private void doSaveAndGoEdit() {
        if (mRecordVideoList.size() == 1) {
            gotoEdit(mRecordVideoList.get(0));
            return;
        }
        mLocalSaveFileNameStr = PathUtils.getMp4FileNameForSdcard();
        fastSave(new ExportEndListener() {
            @Override
            public void onExportEnd(int result) {
                SysAlertDialog.cancelLoadingDialog();
                onCheckRDEncypt(mLocalSaveFileNameStr);
                if (result >= VirtualVideo.RESULT_SUCCESS) {
                    try {
                        gotoEdit(VirtualVideo.createScene().addMedia(mLocalSaveFileNameStr));
                    } catch (InvalidArgumentException e) {
                        e.printStackTrace();
                    }
                } else {
                    gotoEdit(mRecordVideoList.get(0));
                }
            }
        });

    }

    /**
     * save media  to edit check
     */
    private void saveMediaGoEdit() {
        if (mRecordVideoList.size() > 0) {
            doSaveAndGoEdit();
        } else {
            if (!mUseMultiShoot) {
                Intent intent = new Intent();
                intent.putExtra(SdkEntry.INTENT_KEY_PICTURE_PATH,
                        mLocalSavePicNameStr);
                setResult(RESULT_OK, intent);
                finish();
            } else {
                onBackPressed();
            }
        }
    }

    /**
     * multishoot record
     */
    private void saveMediaMultiShoot() {
        if (mRecordVideoList.size() == 1) {
            mLocalSaveFileNameStr = mRecordVideoList.get(0).getMediaPath();
            if (!TextUtils.isEmpty(mLocalSaveFileNameStr)) {
                try {
                    onCheckRDEncypt(mLocalSaveFileNameStr);
                    doSaveAlbum(mLocalSaveFileNameStr);
                    onAutoToast("", getString(R.string.video_save_success));
                } catch (Exception ex) {
                }
            } else {
                onAutoToast("", getString(R.string.video_save_failed));
            }
            resetVideo();
        } else if (mRecordVideoList.size() > 1) {
            onMultiShootVideoSave();
        }
    }

    /**
     * 保存媒体
     */
    private void saveMedia() {
        if (gotoEdit) {
            saveMediaGoEdit();
        } else if (mUseMultiShoot) {
            saveMediaMultiShoot();
        } else {
            if (mRecordVideoList.size() == 1) {
                onSingleVideoSavedEndGoResultSize1();
            } else if (mRecordVideoList.size() > 1) {
                onSingleVideoSavedEndGoResultMore();
            } else {
                onSingleVideoSavedEndGoResultSize0();
            }
        }
    }

    /**
     * 响应导出多段录制的视频
     */
    private void onMultiShootVideoSave() {
        mLocalSaveFileNameStr = PathUtils.getMp4FileNameForSdcard();
        fastSave(new ExportEndListener() {
            @Override
            public void onExportEnd(int result) {
                SysAlertDialog.cancelLoadingDialog();
                onCheckRDEncypt(mLocalSaveFileNameStr);
                if (result >= VirtualVideo.RESULT_SUCCESS) {
                    // 合并成功删除临时文件
                    for (MediaObject mo : mRecordVideoList) {
                        Utils.cleanTempFile(mo.getMediaPath());
                    }
                    doSaveAlbum(mLocalSaveFileNameStr);
                    if (!TextUtils.isEmpty(mLocalSaveFileNameStr)) {
                        doSaveAlbum(mLocalSaveFileNameStr);
                        onAutoToast("", getString(R.string.video_save_success));
                    } else {
                        onAutoToast("", getString(R.string.video_save_failed));
                    }
                } else {
                    // 先删除临时文件
                    for (MediaObject mo : mRecordVideoList) {
                        if (!mo.equals(mRecordVideoList.get(0))) {
                            Utils.cleanTempFile(mo.getMediaPath());
                        }
                    }
                }
                resetVideo();
            }
        });

    }

    /**
     * 仅有一个视频对象
     */
    private void onSingleVideoSavedEndGoResultSize1() {
        mLocalSaveFileNameStr = mRecordVideoList.get(0).getMediaPath();
        if (!TextUtils.isEmpty(mLocalSaveFileNameStr)) {
            onSaveSuccessed();
        }
        finishCamGate();
    }


    /***
     *
     * single list.size()>1
     */
    private void onSingleVideoSavedEndGoResultMore() {
        mLocalSaveFileNameStr = PathUtils.getMp4FileNameForSdcard();
        fastSave(new ExportEndListener() {
            @Override
            public void onExportEnd(int nResult) {
                SysAlertDialog.cancelLoadingDialog();
                onCheckRDEncypt(mLocalSaveFileNameStr);
                if (nResult >= VirtualVideo.RESULT_SUCCESS) {
                    // 合并成功删除临时文件
                    for (MediaObject mo : mRecordVideoList) {
                        Utils.cleanTempFile(mo.getMediaPath());
                    }
                    if (!TextUtils.isEmpty(mLocalSaveFileNameStr)) {
                        onSaveSuccessed();
                    }
                    RecorderActivity.this.finishCamGate();
                } else {
                    // 先删除临时文件
                    for (MediaObject mo : mRecordVideoList) {
                        if (!mo.equals(mRecordVideoList.get(0))) {
                            Utils.cleanTempFile(mo.getMediaPath());
                        }
                    }
                    onSingleVideoSavedEndGoResultSizeBiggerFailed();
                }
            }
        });

    }

    /***
     *
     * 视频段数>1
     * 合并视频成功
     */
    private void onSaveSuccessed() {
        if (editResult) {
            goToEditResult(false);
        } else if (!mUseMultiShoot) {
            Intent intent = new Intent();
            intent.putExtra(SdkEntry.INTENT_KEY_PICTURE_PATH, mLocalSavePicNameStr);
            intent.putExtra(SdkEntry.INTENT_KEY_VIDEO_PATH, mLocalSaveFileNameStr);
            backUseMvEdit(intent);
            backBeauty(intent);
            if (mIsSaveToAlbum) {
                doSaveAlbum(mLocalSaveFileNameStr);
            }
            setResult(RESULT_OK, intent);
        } else {
            SdkEntryHandler.getInstance().onExportRecorder(RecorderActivity.this, mLocalSaveFileNameStr);
        }

    }

    /**
     * 返回编辑
     */
    private void goToEditResult(boolean finish) {
        Intent intent = new Intent();
        if (bSelectPhoto) {
            intent.putExtra(SdkEntry.INTENT_KEY_PICTURE_PATH, mLocalSavePicNameStr);
        } else {
            intent.putExtra(SdkEntry.INTENT_KEY_VIDEO_PATH, mLocalSaveFileNameStr);
        }
        backBeauty(intent);
        backUseMvEdit(intent);
        setResult(RESULT_OK, intent);
        if (finish) {
            finish();
        }
    }

    /***
     * 合并视频failed后，准备即将返回的数据界面
     */
    private void onSingleVideoSavedEndGoResultSizeBiggerFailed() {
        if (editResult) {
            goToEditResult(true);
        } else if (!mUseMultiShoot) {
            Intent intent = new Intent();
            intent.putExtra(SdkEntry.INTENT_KEY_PICTURE_PATH, mLocalSavePicNameStr);
            intent.putExtra(SdkEntry.INTENT_KEY_VIDEO_PATH, mRecordVideoList.get(0).getMediaPath());
            backUseMvEdit(intent);
            backBeauty(intent);
            setResult(RESULT_OK, intent);
            finish();
        } else {
            onBackPressed();
        }

    }

    /***
     * 保存图片
     */
    private void onSingleVideoSavedEndGoResultSize0() {
        if (editResult) {
            goToEditResult(true);
        } else if (!mUseMultiShoot) {
            Intent intent = new Intent();
            intent.putExtra(SdkEntry.INTENT_KEY_PICTURE_PATH, mLocalSavePicNameStr);
            backBeauty(intent);
            setResult(RESULT_OK, intent);
            finish();
        } else {
            onBackPressed();
        }
    }


    /**
     * 添加红色进度条
     */
    private void addView_Red() {
        ImageView img = new ImageView(this);
        img.setBackgroundColor(getResources().getColor(
                R.color.linear_seekbar_color));
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                CoreUtils.dip2px(this, 1),
                LinearLayout.LayoutParams.MATCH_PARENT);
        img.setLayoutParams(layoutParams);
        if (mSquareCameraLayout.getVisibility() == View.VISIBLE) {
            mLinearSeekbar1.addView(img);
        } else {
            mLinearSeekbar.addView(img);
        }
    }

    /**
     * 添加黑色断条
     */
    private void addView_black() {
        ImageView img = new ImageView(this);
        img.setBackgroundColor(Color.BLACK);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                CoreUtils.dip2px(this, 1),
                LinearLayout.LayoutParams.MATCH_PARENT);
        img.setLayoutParams(layoutParams);
        if (mSquareCameraLayout.getVisibility() == View.VISIBLE) {
            mLinearSeekbar1.addView(img);
        } else {
            mLinearSeekbar.addView(img);
        }
    }


    /**
     * 执行录制完成后加密视频
     */
    private void onCheckRDEncypt(String path) {
        if (isEncryption && !TextUtils.isEmpty(path)) {
            File f = new File(path);
            if (f.exists() && f.length() > 0) {
                if (RecorderCore.apiIsRDEncyptVideo(path) <= 0) {
                    RecorderCore.apiRDVideoEncypt(path);
                }
            }
        }
    }


    /**
     * 停止直播或录制
     */
    private void stopLiveOrRecordStream(boolean save) {
        if (mIsRecording) {
            (iListener).setSave(save);
            try {
                RecorderCore.stopRecord();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void setLiveStreamStatus(final Boolean bRecord) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                mIsRecording = bRecord;
                if (startTrailer) {
                    return;
                }
                if (mIsRecording) {
                    if (cameraConfig.enablePlayMusic
                            && !mAudioPlayer.isPlaying() && mAudioMusic != null) {
                        RecorderCore.enableMixAudio(true);
                        if (isPause) {
                            mAudioPlayer.start();
                            isPause = false;
                        } else {
                            mAudioPlayer.seekTo(mAudioMusic.getStart());
                            mAudioPlayer.start();
                        }
                    }
                } else {
                    if (cameraConfig.enablePlayMusic
                            && mAudioPlayer.isPlaying()) {
                        mAudioPlayer.pause();
                        isPause = true;
                    }
                }
                if (mSquareCameraLayout.getVisibility() == View.VISIBLE) {
                    // 方形录制界面
                    if (mIsRecording) {
                        buttonState = BUTTON_STATE_LIVING;
                        m_btnBottomRightForSquare
                                .setBackgroundResource(R.drawable.camera_sure_button);
                        mBtnBottomLeftForSquare
                                .setBackgroundResource(R.drawable.camera_face_button);
                        mBtnRecord1
                                .setBackgroundResource(R.drawable.btn_record_n);
                    } else {
                        buttonState = BUTTON_STATE_PAUSE;
                        m_btnBottomRightForSquare
                                .setBackgroundResource(R.drawable.camera_sure_button);
                        mBtnBottomLeftForSquare
                                .setBackgroundResource(R.drawable.camera_delete_button);
                        mBtnRecord1
                                .setBackgroundResource(R.drawable.btn_shutter_stop_record);
                    }
                    m_btnBottomRightForSquare.setVisibility(View.VISIBLE);
                    mBtnRecord1.setEnabled(true);

                    if (mIsRecording) {
                        List<String> lstColorEffects = RecorderCore
                                .getSupportedColorEffects();
                        mBtnBottomLeftForSquare.setEnabled(mUseMediaRecorder
                                && lstColorEffects != null
                                && lstColorEffects.size() >= 2);
                        if (!mBtnBottomLeftForSquare.isEnabled()) {
                            onFilterListCtrlClick();
                        }

                    } else {
                        mBtnBottomLeftForSquare.setEnabled(true);
                    }

                } else { // 全屏录制界面

                    if (mIsRecording) {
                        buttonState = BUTTON_STATE_LIVING;
                        m_btnBottomRight
                                .setBackgroundResource(R.drawable.camera_sure_button);
                        mBtnBottomLeft
                                .setBackgroundResource(R.drawable.camera_face_button);
                        m_btnBottomRightForLandscape
                                .setBackgroundResource(R.drawable.camera_sure_button);
                    } else {
                        buttonState = BUTTON_STATE_PAUSE;
                        m_btnBottomRight
                                .setBackgroundResource(R.drawable.camera_sure_button);
                        mBtnBottomLeft
                                .setBackgroundResource(R.drawable.camera_delete_button);
                        m_btnBottomRightForLandscape
                                .setBackgroundResource(R.drawable.camera_sure_button);
                    }
                    m_btnBottomRight.setVisibility(View.VISIBLE);
                    m_btnBottomRightForLandscape.setVisibility(View.VISIBLE);
                    mBtnRecord.setEnabled(true);
                    if (mIsRecording) {
                        mBtnRecord.setBackgroundResource(R.drawable.btn_record_n);
                        m_btnBlackScreen.setVisibility(View.VISIBLE);


                        List<String> lstColorEffects = RecorderCore
                                .getSupportedColorEffects();
                        mBtnBottomLeft.setEnabled(mUseMediaRecorder
                                && lstColorEffects != null
                                && lstColorEffects.size() >= 2);
                        if (!mBtnBottomLeft.isEnabled()) {
                            onFilterListCtrlClick();
                        }
                    } else {
                        mBtnRecord
                                .setBackgroundResource(R.drawable.btn_shutter_stop_record);
                        mBtnBottomLeft.setEnabled(true);
                    }

                }
                checkFlashMode();
                InitBtnBlack();
                InitBtnShootingRatio();

            }
        });
    }

    /**
     * 检测闪光灯和摄像头方向
     */
    @Override
    public void checkFlashMode() {
        if (isFullScreen) {
            //长方形录制
            if (!isFrontCamera) {
                mBtnFlashModeCtrl.setSelected(RecorderCore.getFlashMode());
                mBtnFlashModeCtrl.setEnabled(true);
                mBtnFlashModeCtrl
                        .setImageResource(R.drawable.camera_flash_status);

            } else {
                mBtnFlashModeCtrl.setEnabled(false);
                mBtnFlashModeCtrl.setImageResource(R.drawable.camare_flare_un);

            }
        } else {
            //正方形录制
            if (!isFrontCamera) {
                mBtnFlashModeCtrl1.setSelected(RecorderCore.getFlashMode());
                mBtnFlashModeCtrl1.setEnabled(true);
                mBtnFlashModeCtrl1
                        .setImageResource(R.drawable.camera_flash_status);

            } else {
                mBtnFlashModeCtrl1.setEnabled(false);
                mBtnFlashModeCtrl1
                        .setImageResource(R.drawable.camare_flare_un);
            }
        }
    }

    /**
     * 初始摄像头滤镜各项目
     */
    private void initCameraFilterListItems() {
        if (null == mCameraEffectHandler) {
            mCameraEffectHandler = new CameraEffectHandler(this, cameraConfig.fitlerUrl, new CameraEffectHandler.IFilterCheck() {
                @Override
                public void onSelected(int nItemId, boolean user) {
                    mCurrentEffectIndex = nItemId;
                    onCheckEffect();
                }
            });
        }
        if (mCameraEffectHandler.isLookup()) {
            mCameraEffectHandler.initAllEffects(faceUnityHandler.isCurrentIsVer(), mRecyclerViewFilter, mStrengthLayout, null, mCurrentEffectIndex);
        } else {
            List<String> effects = RecorderCore.getSupportedColorEffects();
            mCameraEffectHandler.initAllEffects(faceUnityHandler.isCurrentIsVer(), mRecyclerViewFilter, mStrengthLayout, effects, mCurrentEffectIndex);
        }
    }

    /**
     * 响应滤镜
     */
    private void onCheckEffect() {
        if (null != mCameraEffectHandler) {
            String strEffectName = mCameraEffectHandler
                    .getInternalColorEffectByItemId(mCurrentEffectIndex);
            if (strEffectName.startsWith("/")) {
                //lookup滤镜
                RecorderCore.setLookFilter(strEffectName);
            } else {
                RecorderCore.setColorEffect(strEffectName);
            }
        }
    }

    /**
     * 是否隐藏特效
     */
    private boolean bShowFitlerListLayout;

    /**
     * 响应控制特效控件显示
     */
    protected void onFilterListCtrlClick() {
        if (null != faceUnityHandler) {
            faceUnityHandler.setOrientation(tempOrientation);
        }
        TranslateAnimation taCameraFilter;
        final LayoutParams lp = mRlFilterList.getLayoutParams();// mRlFilterList
        if (lp.height == 0) {
            if (mBtnBottomLeft.getVisibility() != View.VISIBLE
                    || !mBtnBottomLeft.isEnabled()) {
                return;
            }
            lp.height = LayoutParams.WRAP_CONTENT;
            mRlFilterList.setLayoutParams(lp);
            bShowFitlerListLayout = true;
        } else {
            bShowFitlerListLayout = false;
        }
        if (!bShowFitlerListLayout) {
            if (mOrientationCompensation == 90) {
                taCameraFilter = new TranslateAnimation(
                        Animation.RELATIVE_TO_SELF, 0.0f,
                        Animation.RELATIVE_TO_SELF, 1.0f,
                        Animation.RELATIVE_TO_SELF, 0.0f,
                        Animation.RELATIVE_TO_SELF, 0.0f);
            } else if (mOrientationCompensation == 270) {
                taCameraFilter = new TranslateAnimation(
                        Animation.RELATIVE_TO_SELF, 0.0f,
                        Animation.RELATIVE_TO_SELF, -1.0f,
                        Animation.RELATIVE_TO_SELF, 0.0f,
                        Animation.RELATIVE_TO_SELF, 0.0f);
            } else {
                taCameraFilter = new TranslateAnimation(
                        Animation.RELATIVE_TO_SELF, 0.0f,
                        Animation.RELATIVE_TO_SELF, 0.0f,
                        Animation.RELATIVE_TO_SELF, 0.0f,
                        Animation.RELATIVE_TO_SELF, 1.0f);
            }
        } else {
            if (mOrientationCompensation == 90) {
                taCameraFilter = new TranslateAnimation(
                        Animation.RELATIVE_TO_SELF, 1.0f,
                        Animation.RELATIVE_TO_SELF, 0.0f,
                        Animation.RELATIVE_TO_SELF, 0.0f,
                        Animation.RELATIVE_TO_SELF, 0.0f);
            } else if (mOrientationCompensation == 270) {
                taCameraFilter = new TranslateAnimation(
                        Animation.RELATIVE_TO_SELF, -1.0f,
                        Animation.RELATIVE_TO_SELF, 0.0f,
                        Animation.RELATIVE_TO_SELF, 0.0f,
                        Animation.RELATIVE_TO_SELF, 0.0f);
            } else {
                taCameraFilter = new TranslateAnimation(
                        Animation.RELATIVE_TO_SELF, 0.0f,
                        Animation.RELATIVE_TO_SELF, 0.0f,
                        Animation.RELATIVE_TO_SELF, 1.0f,
                        Animation.RELATIVE_TO_SELF, 0.0f);
            }
        }
        taCameraFilter.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                if (bShowFitlerListLayout) {
                    if (null != mCameraEffectHandler) {
                        //防止有时没有刷新成功  (在可见的情况下刷新UI)
                        mCameraEffectHandler.notifyDataSetChanged(faceUnityHandler.isCurrentIsVer());
                    }
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (!bShowFitlerListLayout) {
                    lp.height = 0;
                    mRlFilterList.setLayoutParams(lp);
                } else {
                }
            }
        });
        taCameraFilter.setDuration(800);
        mRlFilterList.clearAnimation();
        taCameraFilter.setFillEnabled(true);
        taCameraFilter.setFillAfter(true);
        mRlFilterList.startAnimation(taCameraFilter);
        onOrientationFilter();
    }

    /**
     * 响应控制录像按钮
     */
    protected void onRecordButtonClick() {
        if (mSquareCameraLayout.getVisibility() == View.VISIBLE) {
            mBtnRecord1.setEnabled(false);
        } else {
            mBtnRecord.setEnabled(false);
        }
        if (!CheckSDSize.getSDIsThanCurrentSize(StorageUtils
                .getStorageDirectory())) {
            onAutoToast("",
                    getString(R.string.sd_not_enough_record));
        } else {
            ThreadPoolUtils
                    .execute(new ThreadPoolUtils.ThreadPoolRunnable() {

                        @Override
                        public void onBackground() {
                            synchronized (this) {
                                if (!mIsRecording) {
                                    startLiveOrRecordStream();
                                } else {
                                    stopLiveOrRecordStream(false);
                                }
                            }
                        }
                    });
        }

    }

    @Override
    public void onSwitchCameraButtonClick() {
        if (null != faceUnityHandler) {
            faceUnityHandler.onSwitchCamareBefore();
        }
        RecorderCore.switchCamera();
        if (null != faceUnityHandler) {
            faceUnityHandler.onSwitchCamareAfter();
        }
        isFrontCamera = RecorderCore.isFaceFront();
        checkFlashMode();
    }

    @Override
    public void onFlashModeClick() {
        boolean re = RecorderCore.getFlashMode();
        if (mSquareCameraLayout.getVisibility() == View.VISIBLE) {
            if (RecorderCore.setFlashMode(!re)) {
                mBtnFlashModeCtrl1.setSelected(!re);
            }
        } else {
            if (RecorderCore.setFlashMode(!re)) {
                mBtnFlashModeCtrl.setSelected(!re);
            }
        }
    }

    private final int REQUEST_CONFIG = 265;

    /**
     * 编辑录制配置
     */
    private void onConfigClick() {
        startActivityForResult(new Intent(this, RecorderConfigActivity.class), REQUEST_CONFIG);

    }

    private boolean isFrontCamera = true;
    private boolean canBeautiy = false;

    private void goPreviewByCameraSizeMode() {
        mTotalWidth = 0;
        totalTime = 0;
        even = 0;
        mRecordVideoList.clear();
        mLinearSeekbar.removeAllViews();
        mLinearSeekbar1.removeAllViews();
        mIsRecording = false;

        canBeautiy = RecorderCore.isBeautifyEnabled();
        if (null != faceUnityHandler && hasJben_MR2) {
            if (enableFace) {
                faceUnityHandler.enableBeautify(true);
                canBeautiy = false;
            }
        }
    }

    /***
     * 正方形/长方形切换
     */
    private void onChangeCameraSizeModeClick() {
        if (mSquareCameraLayout.getVisibility() == View.VISIBLE) {
            mSquareCameraLayout.setVisibility(View.INVISIBLE);
            mScreenCameraLayout.setVisibility(View.VISIBLE);
            isFullScreen = true;
            onCheckLock(false);
        } else if (mScreenCameraLayout.getVisibility() == View.VISIBLE) {
            mScreenCameraLayout.setVisibility(View.INVISIBLE);
            mSquareCameraLayout.setVisibility(View.VISIBLE);
            isFullScreen = false;
            mOrientationCompensation = 0;
            onCheckLock(true);
        }
        goPreviewByCameraSizeMode();
        //准备打开摄像头
        onCameraPermissionGranted();
    }

    private int tempOrientation = 0;// 防止频繁通知改变方向

    private class MyOrientationEventListener extends OrientationEventListener {
        /**
         * 当前方向度数
         */
        int mOrientation = OrientationEventListener.ORIENTATION_UNKNOWN;

        public MyOrientationEventListener(Context context) {
            super(context);
        }

        @Override
        public void onOrientationChanged(int orientation) {
            // 手机平放时，检测不到有效的角度
            if (orientation == ORIENTATION_UNKNOWN)
                return;

            mOrientation = Utils.roundOrientation(orientation, mOrientation);

            // 根据显示方向和当前手机方向，得出当前各个需要与方向适应的控件修正方向
            int orientationCompensation = mOrientation
                    + Utils.getDisplayRotation(RecorderActivity.this);

            if (mOrientationCompensation != orientationCompensation) {
                if (mSquareCameraLayout.getVisibility() == View.VISIBLE) {
                    mOrientationCompensation = 0;
                } else {
                    mOrientationCompensation = orientationCompensation;
                }
                if (tempOrientation != mOrientationCompensation) {
                    if (!RecorderCore.isRecording() && (mRecordVideoList.size() <= 0)) {//合并视频(必须视频尺寸、码率、帧率一致)
                        setOrientationIndicator(mOrientationCompensation);
                        onOrientationFilter();
                    }
                }
            }
        }
    }

    private final int VIDEO_OUT_ORIENTAION = 0;
    /**
     * 防止切换正方形->全屏(横屏时) 设置输出视频角度无效 (解决方案:开始录制前设置输出角度)
     */
    private int tempVideoOrientaion = VIDEO_OUT_ORIENTAION;

    protected void onVerOHor(boolean isVer) {
        // 0 标准竖屏 ， 270 标准横屏
        setOrientationIndicator(isVer ? 0 : 270);
        onOrientationFilter();
    }

    private TextView tvTimer, tvTimer1;

    /**
     * 显示录制时间
     *
     * @param start
     */
    private void ctlTimerCounter(final Boolean start) {
        this.runOnUiThread(new Runnable() {
            public void run() {
                if (mSquareCameraLayout.getVisibility() == View.VISIBLE) {
                    if (!start) {
                        tvTimer1.setVisibility(View.INVISIBLE);
                    } else {
                        tvTimer1.setVisibility(View.VISIBLE);
                    }
                } else {
                    if (!start) {
                        tvTimer.setVisibility(View.INVISIBLE);
                    } else {
                        tvTimer.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
    }

    /**
     * 响应关闭与黑屏
     */
    protected void onQualityOrBlackScreen() {
        if (mIsRecording) {
            mLayoutBlackScreen.setVisibility(View.VISIBLE);
            if (CoreUtils.hasIceCreamSandwich()) {
                // 全屏时，隐藏虚拟键区
                Utils.getRootView(this).setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
            }
            onAutoToast(null, getString(R.string.exit_black_screen));
        }
    }

    /**
     * 响应图库与暂停录制
     */
    private void onCloseOrPauseRecordClick() {

        if (mIsRecording) {
            if (curPosition == POSITION_REC) {
                if (!bSelectPhoto && isInMin()) {
                    String msg = getString(R.string.camera_min_limit_text, String.valueOf(mVideoMinTime / 1000));
                    onAutoToast("", msg);
                    return;
                }
            } else if (curPosition == POSITION_MV) {
                if (isInMin()) {
                    String msg = getString(R.string.camera_min_limit_text, String.valueOf(mMVMinTime / 1000));
                    onAutoToast("", msg);
                    return;
                }
            }
            stopLiveOrRecordStream(true);
        } else {
            gotoEdit();
        }
    }

    /**
     * 是否超过最小时长
     *
     * @return
     */
    private boolean isInMin() {

        if (curPosition == POSITION_REC) {
            //普通视频
            if (mVideoMinTime == 0) {
                return false;
            }

            //最小、最大两个值相等
            if (mVideoMaxTime > 0 && Math.abs(mVideoMinTime - mVideoMaxTime) < 800) {
                return curTotal < (mVideoMinTime - 500);
            } else {
                return curTotal < mVideoMinTime;
            }
        } else if (curPosition == POSITION_MV) {
            //mv
            if (mMVMinTime == 0) {
                return false;
            }
            //最小、最大两个值相等
            if (mMVMaxTime > 0 && Math.abs(mMVMinTime - mMVMaxTime) < 800) {
                return curTotal < (mMVMinTime - 500);
            } else {
                return curTotal < mMVMinTime;
            }
        } else {
            return false;
        }

    }

    private int curTotal;

    private boolean finishWithoutGate = false;

    @Override
    public void finish() {
        //清理回调接口
        //必须调用清除广播的方法
        RecorderCore.unRegisterReceiver();
        // 不再调用onExit(this),防止三星note2 摄像头卡死
        // finish () -> onpasue() ->onstop()->ondestory()
        // 该方法在Activity生命周期中过早,不适合onExit(this)
        // 也不能放在onDestory() ,原因：oncreate(new) ->onstop(old)->ondestoy(old) 防止新的
        // 静态对象被置NULL
        // RecorderCore.onExit(this);
        if (finishWithoutGate) {
            super.finish();
            return;
        }
        finishCamGate();
    }

    /**
     * 录制总时间
     */
    private int totalTime = 0;

    private Handler handler = new Handler();

    private Runnable runnable = new Runnable() {

        @Override
        public void run() {
            if (mSquareCameraLayout.getVisibility() == View.VISIBLE) {
                if (mImgFlashMVSquare.isShown()) {
                    mImgFlashMVSquare.setVisibility(View.GONE);
                } else {
                    mImgFlashMVSquare.setVisibility(View.VISIBLE);
                }
                handler.postDelayed(runnable, 500);
            } else {
                if (mImgFlashMVScreen.isShown()) {
                    mImgFlashMVScreen.setVisibility(View.GONE);
                } else {
                    mImgFlashMVScreen.setVisibility(View.VISIBLE);
                }
                handler.postDelayed(runnable, 500);
            }

        }
    };

    private int even = 0;

    /**
     * 全屏录制进度
     *
     * @param durationStr
     */
    private void initScreenDuration(String durationStr) {
        int len = mRecordVideoList.size();
        if (len > 0) {
            tvTimer.setText(durationStr + "  " + len);
        } else {
            tvTimer.setText(durationStr);
        }

    }

    @SuppressWarnings("deprecation")
    private void deleteVideo() {
        int maxTime = 0;
        if (curPosition == POSITION_MV) {
            maxTime = mMVMaxTime;
        } else if (curPosition == POSITION_REC) {
            maxTime = mVideoMaxTime;
        }
        if (!isFullScreen) {
            if (maxTime == 0 || even % 2 == 0) {
                if (mLinearSeekbar1.getChildCount() > 1) {
                    mLinearSeekbar1.removeViewAt(mLinearSeekbar1
                            .getChildCount() - 1);
                    mLinearSeekbar1.removeViewAt(mLinearSeekbar1
                            .getChildCount() - 1);
                }
                if (mRecordVideoList.size() > 0) {
                    MediaObject mo = mRecordVideoList.remove(mRecordVideoList
                            .size() - 1);
                    totalTime -= Utils.s2ms(mo.getDuration());
                    if (maxTime != 0) {
                        tvTimer1.setText(Utils.stringForTime(totalTime, false,
                                true));
                    } else {
                        tvTimer1.setText(Utils.stringForTime(totalTime, true,
                                false));
                    }
                    PathUtils.deleteFile(mo.getMediaPath());
                }
                int total = 0;
                for (int n = 0; n < mLinearSeekbar1.getChildCount(); n++) {
                    int width = mLinearSeekbar1.getChildAt(n).getWidth();
                    if (width == 0) {
                        width = CoreUtils.dpToPixel(1);
                    }
                    total += width;
                }
                mTotalWidth = total;
                m_btnBottomRightForSquare.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        if (mRecordVideoList.size() > 0) {
                            buttonState = BUTTON_STATE_PAUSE;
                            m_btnBottomRightForSquare
                                    .setBackgroundResource(R.drawable.camera_sure_button);
                            mBtnBottomLeftForSquare
                                    .setBackgroundResource(R.drawable.camera_delete_button);
                        } else {
                            if (hideAlbum) {
                                m_btnBottomRightForSquare
                                        .setVisibility(View.INVISIBLE);
                            }
                            m_btnBottomRightForSquare
                                    .setBackgroundResource(R.drawable.camera_album_button);
                            mBtnBottomLeftForSquare
                                    .setBackgroundResource(R.drawable.camera_face_button);
                            buttonState = BUTTON_STATE_START;
                        }

                    }
                }, 100);
            } else {
                if (mLinearSeekbar1.getChildCount() > 1) {
                    ((ImageView) mLinearSeekbar1.getChildAt(mLinearSeekbar1
                            .getChildCount() - 2))
                            .setBackgroundColor(getResources().getColor(
                                    R.color.linear_seekbar_color_translucent));
                }
            }
            even++;
        } else {
            if (maxTime == 0 || even % 2 == 0) {
                if (mLinearSeekbar.getChildCount() > 1) {
                    mLinearSeekbar
                            .removeViewAt(mLinearSeekbar.getChildCount() - 1);
                    mLinearSeekbar
                            .removeViewAt(mLinearSeekbar.getChildCount() - 1);
                }
                if (mRecordVideoList.size() > 0) {
                    MediaObject mo = mRecordVideoList.remove(mRecordVideoList.size() - 1);
                    totalTime -= Utils.s2ms(mo.getDuration());
                    if (maxTime != 0) {
                        initScreenDuration(Utils.stringForTime(totalTime, false, true));
                    } else {
                        initScreenDuration(Utils.stringForTime(totalTime, true, false));
                    }
                    PathUtils.deleteFile(mo.getMediaPath());
                }
                int total = 0;
                for (int n = 0; n < mLinearSeekbar.getChildCount(); n++) {
                    int width = mLinearSeekbar.getChildAt(n).getWidth();
                    if (width == 0) {
                        width = CoreUtils.dpToPixel(1);
                    }
                    total += width;
                }
                mTotalWidth = total;
                m_btnBottomRight.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        if (mRecordVideoList.size() > 0) {
                            buttonState = BUTTON_STATE_PAUSE;
                            m_btnBottomRight
                                    .setBackgroundResource(R.drawable.camera_sure_button);
                            mBtnBottomLeft
                                    .setBackgroundResource(R.drawable.camera_delete_button);
                            m_btnBottomRightForLandscape
                                    .setBackgroundResource(R.drawable.camera_sure_button);
                        } else {
                            if (hideAlbum) {
                                m_btnBottomRight.setVisibility(View.GONE);
                                m_btnBottomRightForLandscape
                                        .setVisibility(View.GONE);
                            }
                            m_btnBottomRight
                                    .setBackgroundResource(R.drawable.camera_album_button);
                            mBtnBottomLeft
                                    .setBackgroundResource(R.drawable.camera_face_button);
                            m_btnBottomRightForLandscape
                                    .setBackgroundResource(R.drawable.camera_album_button);
                            buttonState = BUTTON_STATE_START;
                        }
                    }
                }, 100);
            } else {
                if (mLinearSeekbar.getChildCount() > 1) {
                    ((ImageView) mLinearSeekbar.getChildAt(mLinearSeekbar
                            .getChildCount() - 2))
                            .setBackgroundColor(getResources().getColor(
                                    R.color.linear_seekbar_color_translucent));
                }
            }
            even++;
        }
        InitBtnShootingRatio();
    }

    /**
     * 重置视频及界面
     */
    private void resetVideo() {
        mLocalSaveFileNameStr = "";
        if (mSquareCameraLayout.getVisibility() == View.VISIBLE) {

            mLinearSeekbar1.removeAllViews();
            mRecordVideoList.clear();
            totalTime = 0;
            tvTimer1.setText(Utils.stringForTime(totalTime, true, false));

            mTotalWidth = 0;
            m_btnBottomRightForSquare.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mRecordVideoList.size() > 0) {
                        buttonState = BUTTON_STATE_PAUSE;
                        m_btnBottomRightForSquare
                                .setBackgroundResource(R.drawable.camera_sure_button);
                        mBtnBottomLeftForSquare
                                .setBackgroundResource(R.drawable.camera_delete_button);
                    } else {
                        if (hideAlbum) {
                            m_btnBottomRightForSquare
                                    .setVisibility(View.INVISIBLE);
                        }
                        m_btnBottomRightForSquare
                                .setBackgroundResource(R.drawable.camera_album_button);
                        mBtnBottomLeftForSquare
                                .setBackgroundResource(R.drawable.camera_face_button);
                        buttonState = BUTTON_STATE_START;
                    }
                    // m_btnBottomRightForSquare.setEnabled(false);
                }
            }, 100);

        } else {
            mLinearSeekbar.removeAllViews();
            mRecordVideoList.clear();
            totalTime = 0;
            tvTimer.setText(Utils.stringForTime(totalTime, false, true));
            mTotalWidth = 0;
            m_btnBottomRight.postDelayed(new Runnable() {

                @Override
                public void run() {
                    if (mRecordVideoList.size() > 0) {
                        buttonState = BUTTON_STATE_PAUSE;
                        m_btnBottomRight
                                .setBackgroundResource(R.drawable.camera_sure_button);
                        mBtnBottomLeft
                                .setBackgroundResource(R.drawable.camera_delete_button);
                        m_btnBottomRightForLandscape
                                .setBackgroundResource(R.drawable.camera_sure_button);
                    } else {
                        if (hideAlbum) {
                            m_btnBottomRight.setVisibility(View.GONE);
                            m_btnBottomRightForLandscape
                                    .setVisibility(View.GONE);
                        }
                        m_btnBottomRight
                                .setBackgroundResource(R.drawable.camera_album_button);
                        mBtnBottomLeft
                                .setBackgroundResource(R.drawable.camera_face_button);
                        m_btnBottomRightForLandscape
                                .setBackgroundResource(R.drawable.camera_album_button);
                        buttonState = BUTTON_STATE_START;
                    }
                    // m_btnBottomRight.setEnabled(false);
                }
            }, 100);

        }
        InitBtnShootingRatio();
    }

    /**
     * 将图片信息存入相册数据库
     *
     * @param path
     */
    private void insertPicToGallery(String path) {
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        ContentValues values = new ContentValues();
        File file = new File(path);
        values.put(MediaStore.Images.ImageColumns.TITLE, "title");
        values.put(MediaStore.Images.ImageColumns.DISPLAY_NAME, file.getName());
        values.put(MediaStore.Images.ImageColumns.DATE_TAKEN,
                System.currentTimeMillis());
        values.put(MediaStore.Images.ImageColumns.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.Images.ImageColumns.ORIENTATION, 0);
        values.put(MediaStore.Images.ImageColumns.DATA, path);
        values.put(MediaStore.Images.ImageColumns.WIDTH, bitmap.getWidth());
        values.put(MediaStore.Images.ImageColumns.HEIGHT, bitmap.getHeight());
        try {
            Uri uri = getContentResolver().insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (uri == null) {

            } else {
                sendBroadcast(new Intent("com.android.camera.NEW_PICTURE", uri));
            }
            onAutoToast("",
                    getString(R.string.photo_save_success));
        } catch (Exception e) {
            onAutoToast("",
                    getString(R.string.photo_save_fail));
        }
    }

    /**
     * 播放系统拍照声音
     */
    public void shootSound() {
        try {
            MediaPlayer shootMP = null;
            AudioManager meng = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            int volume = meng.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
            if (volume != 0) {
                if (shootMP == null)
                    shootMP = MediaPlayer.create(this,
                            Uri.parse("file:///system/media/audio/ui/camera_click.ogg"));
                if (shootMP != null)
                    shootMP.start();
            }
        } catch (Exception ex) {
        }
    }

    /**
     * 初始各布局
     */
    private void initLayouts() {
        String str = getString(R.string.m_short_mv_no_line, mMVMaxTime / 1000);
        ((TextView) $(R.id.tvItembtnSelectMVCaption2)).setText(str);
        ((TextView) $(R.id.tvItembtnSelectMVCaption1)).setText(str);

        mIvOpenCamAnimTop = $(R.id.ivOpenCamAnimTop);
        mIvOpenCamAnimBottom = $(R.id.ivOpenCamAnimBottom);

        mRecordRRL = $(R.id.rrlbtnRecord);
        mBtnRecord = $(R.id.btnRecord);
        mBtnRecord.setOnTouchListener(new OnTouchListener() {
            private long m_lLastClickTime;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (bSelectPhoto) {

                        } else {
                            if (SystemClock.uptimeMillis() - m_lLastClickTime < 1000) {
                                // 防止频繁调用
                                break;
                            }
                            if (mTotalWidth >= m_DisplayWidth) {
                                break;
                            }
                            m_lLastClickTime = SystemClock.uptimeMillis();
                            even = 1;
                            onRecordButtonClick();
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        if (bSelectPhoto) {
                            //截图
                            VirtualVideo.Size size = AppConfiguration.getRecorderSize(false);
                            if (null != size) {
                                RecorderCore.screenshot(true, PathUtils.getShotPath(mIsSaveToAlbum), size.width, size.height, 100);
                            }
                        } else {
                            if (mIsRecording) {
                                stopLiveOrRecordStream(false);
                            }
                        }

                        break;
                }
                return false;
            }
        });

        mRllivingBar0 = $(R.id.lrliving_bar0);

        mRecordingBar90 = $(R.id.llliving_bar90);
        mRecordingBar270 = $(R.id.llliving_bar270);

        mBtnFlashModeCtrl = $(R.id.btnFlashModeCtrl);
        mBtnFlashModeCtrl.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                onFlashModeClick();
            }
        });
        mBtnConfig = $(R.id.btnConfig);
        mBtnConfig.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                onConfigClick();
            }
        });
        mBtnConfig1 = $(R.id.btnConfig1);
        mBtnConfig1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                onConfigClick();
            }
        });

        mRlFilterList = $(R.id.rlFilterList);
        mBtnBottomRightForLandscape = $(R.id.rrlbtnBottomRightForLandscape);
        m_btnBottomRightForLandscape = $(R.id.btnBottomRightForLandscape);
        if (hideAlbum) {
            m_btnBottomRightForLandscape.setVisibility(View.GONE);
        }
        m_btnBottomRightForLandscape
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (buttonState == BUTTON_STATE_START) {
                            goToAlbum();
                        } else if (buttonState == BUTTON_STATE_LIVING) {
                            onCloseOrPauseRecordClick();
                        } else if (buttonState == BUTTON_STATE_PAUSE) {
                            onCloseOrPauseRecordClick();
                        }
                    }
                });
        // 正方形添加云音乐
        mllAddMusic1 = $(R.id.llAddMusic1);

        mTvMusicNameSquare = $(R.id.edit_text_music_name1);
        mBtnDelMusic1 = $(R.id.btn_edit_text_music_del1);
        mBtnDelMusic1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteSelectMusic();
            }
        });
        mBtnAddMusic1 = $(R.id.btnAddMusic1);
        mBtnAddMusic1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onMusicYUN();
            }
        });

        //长方形添加音乐部分
        mllAddMusic = $(R.id.llAddMusic);

        mTvMusicNameScreen = $(R.id.edit_text_music_name);
        mBtnDelMusic = $(R.id.btn_edit_text_music_del);
        mBtnDelMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteSelectMusic();

            }
        });
        mBtnAddMusic = $(R.id.btnAddMusic);
        mBtnAddMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onMusicYUN();
            }
        });
        initPlayer();

        mllAddMusic.setVisibility(View.GONE);
        mBtnAddMusic.setVisibility(View.VISIBLE);
        mllAddMusic1.setVisibility(View.GONE);
        mBtnAddMusic1.setVisibility(View.VISIBLE);


        mTimerTv = $(R.id.rrltvTimer);
        mBtnBottomRight = $(R.id.rrlbtnBottomRight);

        mRecordingCameraMoreBar = $(R.id.living_cameramore_bar);

        mBtnSelectPhoto1 = $(R.id.btnSelectPhoto1);

        mBtnSwitchCamera = $(R.id.btnSwitchCamera5);
        mBtnSwitchCamera.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                onSwitchCameraButtonClick();
            }
        });

        m_btnBlackScreen = $(R.id.btnMenuBlackScreen);

        m_btnBlackScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onQualityOrBlackScreen();
            }
        });

        mBtnBlackScreen1 = $(R.id.btnMenuBlackScreen1);
        mBtnBlackScreen1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onQualityOrBlackScreen();
            }
        });
        mBtnBottomLeftLayout = $(R.id.rrlbtnBottomLeft);
        mBtnBottomLeft = $(R.id.btnBottomLeft);
        mBtnBottomLeft.setRepeatClickIntervalTime(30);
        mBtnBottomLeft.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (buttonState == BUTTON_STATE_START) {
                    onFilterListCtrlClick();
                } else if (buttonState == BUTTON_STATE_LIVING) {
                    onFilterListCtrlClick();
                } else if (buttonState == BUTTON_STATE_PAUSE) {
                    deleteVideo();
                }
            }
        });
        mBtncloseFilterList = $(R.id.btncloseFilterList);
        mBtncloseFilterList.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                onFilterListCtrlClick();
            }
        });

        mLayoutBlackScreen = $(R.id.flBlackScreen);
        mGdBlackScreen = new GestureDetector(this,
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onDoubleTap(MotionEvent e) {
                        mLayoutBlackScreen.setVisibility(View.GONE);
                        return true;
                    }

                });
        mLayoutBlackScreen.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return mGdBlackScreen.onTouchEvent(event);
            }
        });
        tvTimer = $(R.id.tvTimer);
        if (!Utils.isUseInternalRecorder()) {
            mUseMediaRecorder = true;
            Utils.setCanWriteMP4Metadata(false);
        } else {
            Utils.setCanWriteMP4Metadata(true);
        }
        mBtnBeauty = $(R.id.btnbeauty);
        mBtnBeauty.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                onBeautifyClick();
            }
        });
        mGlTouchView = $(R.id.glTouch);
        mGlTouchView.setViewHandler(glListener);
        // 处理相机变焦
        m_hlrCameraZoom = new CameraZoomHandler(this, null);
        mGlTouchView.setZoomHandler(m_hlrCameraZoom);

        m_btnWaiting = $(R.id.btnWating);
        m_btnWaiting.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                startWaitingRecord(5);
            }
        });
        mBtnCancelRecord = $(R.id.btnCancelRecord);
        mBtnCancelRecord.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        mLayoutSelectRecOrPhoto1 = $(rlSelectRecOrPhoto1);
        mLayoutSelectRecOrPhoto2 = $(rlSelectRecOrPhoto2);
        mLayoutSelectRecOrPhoto1.setVisibility(View.VISIBLE);
        mLayoutSelectRecOrPhoto2.setVisibility(View.VISIBLE);

        mSelectRec1 = $(R.id.lvSelectRec1);
        mSelectRec2 = $(R.id.lvSelectRec2);
        mSelectMV1 = $(R.id.lvSelectMV1);
        mSelectMV2 = $(R.id.lvSelectMV2);
        mSelectPhoto1 = $(R.id.lvSelectPhoto1);
        mSelectPhoto2 = $(R.id.lvSelectPhoto2);

        mSelectRec1.setOnClickListener(onSwitchButtonClickListener);
        mSelectRec2.setOnClickListener(onSwitchButtonClickListener);
        mSelectMV1.setOnClickListener(onSwitchButtonClickListener);
        mSelectMV2.setOnClickListener(onSwitchButtonClickListener);
        mSelectPhoto1.setOnClickListener(onSwitchButtonClickListener);
        mSelectPhoto2.setOnClickListener(onSwitchButtonClickListener);

        // /////////////////////////////////////////////新加入的方形界面处理
        // 两个界面（全屏和方形）
        mSquareCameraLayout = $(R.id.rl_square_camera);
        mScreenCameraLayout = $(R.id.rl_fullscreen_camera);

        // /////////////////
        mVideoNewRelative = $(R.id.video_new_relative);
        mVideoNewRelative1 = $(R.id.video_new_relative1);
        if (curPosition == POSITION_REC) {
            if (mVideoMaxTime != 0) {
                mVideoNewRelative.setVisibility(View.VISIBLE);
            } else {
                mVideoNewRelative.setVisibility(View.INVISIBLE);
            }
            if (mVideoMaxTime != 0) {
                mVideoNewRelative1.setVisibility(View.VISIBLE);
            } else {
                mVideoNewRelative1.setVisibility(View.INVISIBLE);
            }
        } else if (curPosition == POSITION_MV) {
            mVideoNewRelative.setVisibility(View.VISIBLE);
            mVideoNewRelative1.setVisibility(View.VISIBLE);
        } else {
            mVideoNewRelative.setVisibility(View.INVISIBLE);
            mVideoNewRelative1.setVisibility(View.INVISIBLE);
        }

        // 方形界面的切换按钮
        mBtnShootingRatio1 = $(R.id.btnShootingRatio1);
        mBtnShootingRatio1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onChangeCameraSizeModeClick();
            }
        });
        // 全屏界面的切换按钮
        mBtnShootingRatio = $(R.id.btnShootingRatio);
        mBtnShootingRatio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onChangeCameraSizeModeClick();
            }
        });

        // ////////////////////
        m_DisplayWidth = CoreUtils.getMetrics().widthPixels;

        handler.postDelayed(runnable, 0);
        mImgFlashMVScreen = $(R.id.record_progress_flash_screen);
        mImgFlashMVSquare = $(R.id.record_progress_flash_square);
        mLinearSeekbar = $(R.id.video_new_seekbar);
        mLinearSeekbar1 = $(R.id.video_new_seekbar1);

        m_btnBottomRight = $(R.id.btnBottomRight);
        if (hideAlbum) {
            m_btnBottomRight.setVisibility(View.GONE);
        }
        m_btnBottomRight.setRepeatClickIntervalTime(10);
        m_btnBottomRight.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (buttonState == BUTTON_STATE_START) {
                    goToAlbum();
                } else if (buttonState == BUTTON_STATE_LIVING) {
                    onCloseOrPauseRecordClick();
                } else if (buttonState == BUTTON_STATE_PAUSE) {
                    onCloseOrPauseRecordClick();
                }
            }
        });

        m_btnBottomRightForSquare = $(R.id.btnBottomRightForSquare);
        if (hideAlbum) {
            m_btnBottomRightForSquare.setVisibility(View.INVISIBLE);
        }
        m_btnBottomRightForSquare.setRepeatClickIntervalTime(10);
        m_btnBottomRightForSquare.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (buttonState == BUTTON_STATE_START) {
                    goToAlbum();
                } else if (buttonState == BUTTON_STATE_LIVING) {
                    onCloseOrPauseRecordClick();
                } else if (buttonState == BUTTON_STATE_PAUSE) {
                    onCloseOrPauseRecordClick();
                }
            }
        });

        mBtnRecord1 = $(R.id.btnRecord1);
        mBtnRecord1.setOnTouchListener(new OnTouchListener() {
            private long m_lLastClickTime;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {

                    case MotionEvent.ACTION_DOWN:
                        if (bSelectPhoto) {

                        } else {
                            if (SystemClock.uptimeMillis() - m_lLastClickTime < 1000) {
                                // 防止频繁调用
                                break;
                            }
                            if (mTotalWidth >= m_DisplayWidth) {
                                break;
                            }
                            m_lLastClickTime = SystemClock.uptimeMillis();
                            even = 1;
                            onRecordButtonClick();
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        if (bSelectPhoto) {
                            RecorderCore.screenshot(true, PathUtils.getShotPath(mIsSaveToAlbum), 480, 480, 90);
                        } else {
                            if (mIsRecording)
                                stopLiveOrRecordStream(false);
                        }

                        break;
                }
                return false;
            }
        });

        mBtnFlashModeCtrl1 = $(R.id.btnFlashModeCtrl1);
        mBtnFlashModeCtrl1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFlashModeClick();
            }
        });

        mBtnSwitchCamera1 = $(R.id.btnSwitchCamera1);
        mBtnSwitchCamera1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                onSwitchCameraButtonClick();
            }
        });


        mBtnBottomLeftForSquare = $(R.id.btnBottomLeftForSquare);
        mBtnBottomLeftForSquare.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (buttonState == BUTTON_STATE_START) {
                    onFilterListCtrlClick();
                } else if (buttonState == BUTTON_STATE_LIVING) {
                    onFilterListCtrlClick();
                } else if (buttonState == BUTTON_STATE_PAUSE) {
                    deleteVideo();
                }
            }
        });

        tvTimer1 = $(R.id.tvTimer1);
        mRlframeSquarePreview = $(R.id.frameSquarePreview);
        mRlframeSquarePreview.setAspectRatio(1.0);
        mBtnBeauty1 = $(R.id.btnbeauty1);
        mBtnBeauty1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onBeautifyClick();
            }
        });

        m_btnWaiting1 = $(R.id.btnWating1);
        m_btnWaiting1.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                startWaitingRecord(5);
            }
        });
        mBtnCancelRecord1 = $(R.id.btnCancelRecord1);
        mBtnCancelRecord1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // 90度横屏和270度横屏是相关按钮初始化
        mBtnCancelRecord90 = $(R.id.btnCancelRecord90);
        mBtnCancelRecord90.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mBtnBlackScreen90 = $(R.id.btnMenuBlackScreen90);
        mBtnBlackScreen90.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onQualityOrBlackScreen();
            }
        });
        mBtnBlackScreen270 = $(R.id.btnMenuBlackScreen270);
        mBtnBlackScreen270.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onQualityOrBlackScreen();
            }
        });

        mBtnShootingRatio90 = $(R.id.btnShootingRatio90);
        mBtnShootingRatio90.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onChangeCameraSizeModeClick();
            }
        });

        mBtnWating90 = $(R.id.btnWating90);

        mBtnWating90.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                startWaitingRecord(5);
            }
        });

        // /////270度
        mBtnCancelRecord270 = $(R.id.btnCancelRecord270);
        mBtnCancelRecord270.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        mBtnShootingRatio270 = $(R.id.btnShootingRatio270);
        mBtnShootingRatio270.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onChangeCameraSizeModeClick();
            }
        });

        mBtnWating270 = $(R.id.btnWating270);
        mBtnWating270.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                startWaitingRecord(5);
            }
        });

        InitBtnBlack();
        switchRecOrPhotoItemLayout();
        switchRecOrPhoto();
        InitBtnShootingRatio();
    }

    private void deleteSelectMusic() {
        if (!mIsRecording && mAudioMusic != null) {
            if (mAudioPlayer != null)
                mAudioPlayer.stop();
            mAudioMusic = null;
            mllAddMusic.setVisibility(View.GONE);
            mBtnAddMusic.setVisibility(View.VISIBLE);
            mllAddMusic1.setVisibility(View.GONE);
            mBtnAddMusic1.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 云音乐
     */
    private void onMusicYUN() {
        HistoryMusicCloud.getInstance().initilize(this);
        if (!TextUtils.isEmpty(cameraConfig.cloudMusicUrl)) {
            //云音乐(是否支持分页  musicTypeUrl ==“”? )
            String musicTypeUrl = cameraConfig.cloudMusicTypeUrl;
            String cloudUrl = cameraConfig.cloudMusicUrl;
            MoreMusicActivity.onYunMusic(this, true, musicTypeUrl, cloudUrl, cameraConfig.mCloudAuthorizationInfo);
        } else {
            UIConfiguration uiConfiguration = SdkEntry.getSdkService().getUIConfig();
            MoreMusicActivity.onYunMusic(this, false, "", uiConfiguration.cloudMusicUrl, null);
        }
    }

    private OnClickListener onSwitchButtonClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (id == R.id.lvSelectRec1) {
                curPosition = POSITION_REC;
            } else if (id == R.id.lvSelectRec2) {
                curPosition = POSITION_REC;
            } else if (id == R.id.lvSelectMV1) {
                curPosition = POSITION_MV;
            } else if (id == R.id.lvSelectMV2) {
                curPosition = POSITION_MV;
            } else if (id == R.id.lvSelectPhoto1) {
                curPosition = POSITION_PHOTO;
            } else if (id == R.id.lvSelectPhoto2) {
                curPosition = POSITION_PHOTO;
            }
            switchRecOrPhotoItemLayout();
            switchRecOrPhoto();
        }
    };

    protected void startWaitingRecord(int initiStep) {
        if (m_bIsWaiting || mIsRecording) {
            return;
        }
        mBtnConfig.setEnabled(false);
        mBtnConfig1.setEnabled(false);
        m_bIsWaiting = true;
        if (mSquareCameraLayout.getVisibility() == View.VISIBLE) {
            m_btnWaiting1.setEnabled(false);
            mBtnRecord1.setEnabled(false);
            setViewVisibility(R.id.waiting_text, true);
            step = initiStep;
            mHandler.post(m_runnableWaiting);
        } else {
            m_btnWaiting.setEnabled(false);
            mBtnRecord.setEnabled(false);
            setViewVisibility(R.id.waiting_text, true);
            step = initiStep;
            mHandler.post(m_runnableWaiting);
        }
    }

    private void cancelWaitingRecord() {
        finishWaitingRecord();
    }

    private void finishWaitingRecord() {
        if (mSquareCameraLayout.getVisibility() == View.VISIBLE) {
            m_bIsWaiting = false;
            m_btnWaiting1.setEnabled(true);
            mBtnRecord1.setEnabled(true);
            setViewVisibility(R.id.waiting_text, false);
            mHandler.removeCallbacks(m_runnableWaiting);
        } else {
            m_bIsWaiting = false;
            m_btnWaiting.setEnabled(true);
            mBtnRecord.setEnabled(true);
            setViewVisibility(R.id.waiting_text, false);
            mHandler.removeCallbacks(m_runnableWaiting);
        }
    }

    private RotateImageView m_btnWaiting, m_btnWaiting1;
    private boolean m_bIsWaiting = false;
    private int step = 5;

    private Runnable m_runnableWaiting = new Runnable() {

        @Override
        public void run() {
            if (m_bIsWaiting) {
                setText(R.id.waiting_text, Integer.toString(step));
                if (step-- < 1) {
                    finishWaitingRecord();
                    synchronized (RecorderActivity.this) {
                        if (bSelectPhoto) {
                            VirtualVideo.Size size;
                            if (mSquareCameraLayout.getVisibility() == View.VISIBLE) {
                                size = AppConfiguration.getRecorderSize(true);
                            } else {
                                size = AppConfiguration.getRecorderSize(false);
                            }
                            if (null != size) {
                                RecorderCore.screenshot(true, PathUtils.getShotPath(mIsSaveToAlbum), size.width, size.height, 100);
                            }
                        } else {
                            onRecordButtonClick();
                        }
                    }
                } else {
                    mHandler.postDelayed(m_runnableWaiting, 1000);
                }
            }
        }
    };


    private GlTouchView.CameraCoderViewListener glListener = new CameraCoderViewListener() {

        @Override
        public void onSwitchFilterToRight() {
            if (!enableRecPhotoSwitch) {
                return;
            }
            if (!mIsRecording && mRecordVideoList.size() == 0) {
                if (curPosition == POSITION_REC) {
                    if (hideMV) {
                        return;
                    }
                    curPosition = POSITION_MV;
                } else if (curPosition == POSITION_PHOTO) {
                    if (hideRec) {
                        if (hideMV) {
                            return;
                        }
                        curPosition = POSITION_MV;
                    } else {
                        curPosition = POSITION_REC;
                    }
                }
                switchRecOrPhotoItemLayout();
                switchRecOrPhoto();
            }
        }

        @Override
        public void onSwitchFilterToLeft() {
            if (!enableRecPhotoSwitch) {
                return;
            }
            if (!mIsRecording && mRecordVideoList.size() == 0) {
                if (curPosition == POSITION_REC) {
                    if (hidePhoto) {
                        return;
                    }
                    curPosition = POSITION_PHOTO;
                } else if (curPosition == POSITION_MV) {
                    if (hideRec) {
                        if (hidePhoto) {
                            return;
                        }
                        curPosition = POSITION_PHOTO;
                    } else {
                        curPosition = POSITION_REC;
                    }
                }
                switchRecOrPhotoItemLayout();
                switchRecOrPhoto();
            }

        }

        @Override
        public void onSingleTapUp(MotionEvent e) {
            if (null != mRlFilterList && mRlFilterList.getHeight() > 100) {
                onFilterListCtrlClick();// 触摸空白区域关闭滤镜Layout
            }
            if (!RecorderCore.isFaceFront()) {
                RecorderCore.cameraFocus((int) e.getX(), (int) e.getY(), null);
            }
        }

        @Override
        public void onDoubleTap(MotionEvent e) {

        }

        @Override
        public void onFilterChangeStart(boolean leftToRight, double filterProportion) {
            bLeftToRight = leftToRight;
            onSureBg();
        }

        @Override
        public void onFilterChanging(boolean leftToRight, double filterProportion) {
            bLeftToRight = leftToRight;
            if (null != mCameraEffectHandler) {
                if (mCameraEffectHandler.isLookup()) {
                    if (leftToRight) {
                        RecorderCore.setLookupFilter(mCameraEffectHandler.getInternalColorEffectByItemId(mEffectLeftIndex),
                                mCameraEffectHandler.getInternalColorEffectByItemId(mTempCurrentIndex), filterProportion);
                    } else {
                        RecorderCore.setLookupFilter(mCameraEffectHandler.getInternalColorEffectByItemId(mTempCurrentIndex),
                                mCameraEffectHandler.getInternalColorEffectByItemId(mEffectRightIndex), filterProportion);
                    }
                } else {
                    if (leftToRight) {
                        RecorderCore.setColorEffect(mCameraEffectHandler.getInternalColorEffectByItemId(mEffectLeftIndex),
                                mCameraEffectHandler.getInternalColorEffectByItemId(mTempCurrentIndex), filterProportion);
                    } else {
                        RecorderCore.setColorEffect(mCameraEffectHandler.getInternalColorEffectByItemId(mTempCurrentIndex),
                                mCameraEffectHandler.getInternalColorEffectByItemId(mEffectRightIndex), filterProportion);
                    }
                }
            }

        }

        @Override
        public void onFilterChangeEnd() {
            if (null != mCameraEffectHandler) {
                if (bLeftToRight) {
                    mCameraEffectHandler.selectListItem(mEffectLeftIndex);
                } else {
                    mCameraEffectHandler.selectListItem(mEffectRightIndex);
                }
            }
            onSureBg();
        }

        @Override
        public void onFilterCanceling(boolean leftToRight, double filterProportion) {
            bLeftToRight = leftToRight;
            if (null != mCameraEffectHandler) {
                if (mCameraEffectHandler.isLookup()) {
                    if (!leftToRight) {
                        RecorderCore.setLookupFilter(mCameraEffectHandler.getInternalColorEffectByItemId(mEffectLeftIndex),
                                mCameraEffectHandler.getInternalColorEffectByItemId(mTempCurrentIndex), filterProportion);
                    } else {
                        RecorderCore.setLookupFilter(mCameraEffectHandler.getInternalColorEffectByItemId(mTempCurrentIndex),
                                mCameraEffectHandler.getInternalColorEffectByItemId(mEffectRightIndex), filterProportion);
                    }
                } else {
                    if (!leftToRight) {
                        RecorderCore.setColorEffect(mCameraEffectHandler.getInternalColorEffectByItemId(mEffectLeftIndex),
                                mCameraEffectHandler.getInternalColorEffectByItemId(mTempCurrentIndex), filterProportion);
                    } else {
                        RecorderCore.setColorEffect(mCameraEffectHandler.getInternalColorEffectByItemId(mTempCurrentIndex),
                                mCameraEffectHandler.getInternalColorEffectByItemId(mEffectRightIndex), filterProportion);
                    }
                }
            }
        }

        @Override
        public void onFilterChangeCanceled() {
            onSureBg();
            if (null != mCameraEffectHandler) {
                mCameraEffectHandler.selectListItem(mTempCurrentIndex);
            }
        }
    };
    private boolean bLeftToRight = false; //为true 从左往右，否则从右往左;
    private int mTempCurrentIndex = 0;
    private int mEffectRightIndex, mEffectLeftIndex;

    /**
     * 找到相邻的3个滤镜的所需图标的背景Index
     */
    private void onSureBg() {
        if (null != mCameraEffectHandler) {
            mTempCurrentIndex = mCameraEffectHandler.getCurrentItemId();
            int count = mCameraEffectHandler.getEffectCount();
            mEffectLeftIndex = 0;
            if ((mEffectLeftIndex = (mTempCurrentIndex - 1)) < 0) {
                mEffectLeftIndex = count - 1;
            }
            mEffectRightIndex = mTempCurrentIndex;
            if (mEffectRightIndex < (count - 1)) {
                ++mEffectRightIndex;
            } else {
                mEffectRightIndex = 0;
            }
        }
    }

    private RelativeLayout.LayoutParams photolp;
    private RelativeLayout.LayoutParams reclp;
    private RelativeLayout.LayoutParams mvlp;

    /***
     * 准备容器的Params
     */
    private void createLayoutParams() {
        int margin = CoreUtils.dpToPixel(7);
        photolp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        photolp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        photolp.setMargins(margin, margin, 0, 0);

        reclp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        reclp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        reclp.setMargins(margin, margin, 0, 0);

        mvlp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        mvlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        mvlp.setMargins(margin, margin, 0, 0);

    }


    /**
     * 默认切换到MV模式
     *
     * @param currentVisisble
     */
    private void onPositionMV(int currentVisisble) {
        //
        int mTxColorN = getResources().getColor(R.color.white);
        int mTxColorP = getResources().getColor(R.color.record_type_textcolor_p);
        mvlp.addRule(RelativeLayout.CENTER_HORIZONTAL);
        if ((!isFullScreen)) {
            mSelectMV2.setLayoutParams(mvlp);
            $(R.id.btnSelectMV2).setVisibility(View.VISIBLE);
            $(R.id.lvSelectMV2).setVisibility(currentVisisble);
            ((TextView) $(R.id.tvItembtnSelectMVCaption2))
                    .setTextColor(mTxColorP);

            if (hideRec) {
                mSelectRec2.setVisibility(View.GONE);
            }
            reclp.addRule(RelativeLayout.RIGHT_OF, R.id.lvSelectMV2);
            mSelectRec2.setLayoutParams(reclp);
            $(R.id.btnSelectRec2).setVisibility(View.INVISIBLE);
            ((TextView) $(R.id.tvItembtnSelectRecCaption2)).setTextColor(mTxColorN);
            if (hideRec) {
                photolp.addRule(RelativeLayout.RIGHT_OF, R.id.lvSelectMV2);
            } else {
                photolp.addRule(RelativeLayout.RIGHT_OF, R.id.lvSelectRec2);
            }
            if (hidePhoto) {
                mSelectPhoto2.setVisibility(View.GONE);
            }
            mSelectPhoto2.setLayoutParams(photolp);
            $(R.id.btnSelectPhoto2).setVisibility(View.INVISIBLE);
            ((TextView) $(R.id.tvItembtnSelectPhotoCaption2)).setTextColor(mTxColorN);
        } else {
            mSelectMV1.setLayoutParams(mvlp);
            $(R.id.btnSelectMV1).setVisibility(View.VISIBLE);
            $(R.id.lvSelectMV1).setVisibility(currentVisisble);
            ((TextView) $(R.id.tvItembtnSelectMVCaption1)).setTextColor(mTxColorP);
            if (hideRec) {
                mSelectRec1.setVisibility(View.GONE);
            }
            reclp.addRule(RelativeLayout.RIGHT_OF, R.id.lvSelectMV1);
            mSelectRec1.setLayoutParams(reclp);
            $(R.id.btnSelectRec1).setVisibility(View.INVISIBLE);
            ((TextView) $(R.id.tvItembtnSelectRecCaption1)).setTextColor(mTxColorN);

            if (hideRec) {
                photolp.addRule(RelativeLayout.RIGHT_OF, R.id.lvSelectMV1);
            } else {
                photolp.addRule(RelativeLayout.RIGHT_OF, R.id.lvSelectRec1);
            }
            if (hidePhoto) {
                mSelectPhoto1.setVisibility(View.GONE);
            }
            photolp.addRule(RelativeLayout.RIGHT_OF, R.id.lvSelectRec1);
            mSelectPhoto1.setLayoutParams(photolp);
            $(R.id.btnSelectPhoto1).setVisibility(View.INVISIBLE);
            ((TextView) $(R.id.tvItembtnSelectPhotoCaption1)).setTextColor(mTxColorN);
        }
    }

    /***
     * 切换到录制模式
     */
    private void onPositionRec(int currentVisisble) {
        reclp.addRule(RelativeLayout.CENTER_HORIZONTAL);
        int mTxColorN = getResources().getColor(R.color.white);
        int mTxColorP = getResources().getColor(R.color.record_type_textcolor_p);
        if ((!isFullScreen)) {
            mSelectRec2.setLayoutParams(reclp);
            $(R.id.btnSelectRec2).setVisibility(View.VISIBLE);
            $(R.id.lvSelectRec2).setVisibility(currentVisisble);
            ((TextView) $(R.id.tvItembtnSelectRecCaption2)).setTextColor(mTxColorP);

            if (hidePhoto) {
                mSelectPhoto2.setVisibility(View.GONE);
            }
            photolp.addRule(RelativeLayout.RIGHT_OF, R.id.lvSelectRec2);
            mSelectPhoto2.setLayoutParams(photolp);
            $(R.id.btnSelectPhoto2).setVisibility(View.INVISIBLE);
            ((TextView) $(R.id.tvItembtnSelectPhotoCaption2))
                    .setTextColor(mTxColorN);

            if (hideMV) {
                mSelectMV2.setVisibility(View.GONE);
            }
            mvlp.addRule(RelativeLayout.LEFT_OF, R.id.lvSelectRec2);
            mSelectMV2.setLayoutParams(mvlp);
            $(R.id.btnSelectMV2).setVisibility(View.INVISIBLE);
            ((TextView) $(R.id.tvItembtnSelectMVCaption2)).setTextColor(mTxColorN);
        } else {
            mSelectRec1.setLayoutParams(reclp);
            $(R.id.btnSelectRec1).setVisibility(View.VISIBLE);
            $(R.id.lvSelectRec1).setVisibility(currentVisisble);
            ((TextView) $(R.id.tvItembtnSelectRecCaption1))
                    .setTextColor(mTxColorP);

            if (hidePhoto) {
                mSelectPhoto1.setVisibility(View.GONE);
            }
            photolp.addRule(RelativeLayout.RIGHT_OF, R.id.lvSelectRec1);
            mSelectPhoto1.setLayoutParams(photolp);
            $(R.id.btnSelectPhoto1)
                    .setVisibility(View.INVISIBLE);
            ((TextView) $(R.id.tvItembtnSelectPhotoCaption1))
                    .setTextColor(mTxColorN);

            if (hideMV) {
                mSelectMV1.setVisibility(View.GONE);
            }
            mvlp.addRule(RelativeLayout.LEFT_OF, R.id.lvSelectRec1);
            mSelectMV1.setLayoutParams(mvlp);
            $(R.id.btnSelectMV1).setVisibility(View.INVISIBLE);
            ((TextView) $(R.id.tvItembtnSelectMVCaption1))
                    .setTextColor(mTxColorN);
        }
    }

    /**
     * 切到图片模式
     *
     * @param currentVisisble
     */
    private void onPositionPhoto(int currentVisisble) {
        int mTxColorN = getResources().getColor(R.color.white);
        int mTxColorP = getResources().getColor(R.color.record_type_textcolor_p);
        photolp.addRule(RelativeLayout.CENTER_HORIZONTAL);
        if ((!isFullScreen)) {
            mSelectPhoto2.setLayoutParams(photolp);
            $(R.id.btnSelectPhoto2).setVisibility(View.VISIBLE);
            $(R.id.lvSelectPhoto2).setVisibility(currentVisisble);
            ((TextView) $(R.id.tvItembtnSelectPhotoCaption2))
                    .setTextColor(mTxColorP);
            if (hideRec) {
                mSelectRec2.setVisibility(View.GONE);
            }
            reclp.addRule(RelativeLayout.LEFT_OF, R.id.lvSelectPhoto2);
            mSelectRec2.setLayoutParams(reclp);
            $(R.id.btnSelectRec2).setVisibility(View.INVISIBLE);
            ((TextView) $(R.id.tvItembtnSelectRecCaption2))
                    .setTextColor(mTxColorN);

            if (hideRec) {
                mvlp.addRule(RelativeLayout.LEFT_OF, R.id.lvSelectPhoto2);
            } else {
                mvlp.addRule(RelativeLayout.LEFT_OF, R.id.lvSelectRec2);
            }
            if (hideMV) {
                mSelectMV2.setVisibility(View.GONE);
            }
            mSelectMV2.setLayoutParams(mvlp);
            $(R.id.btnSelectMV2).setVisibility(View.INVISIBLE);
            ((TextView) $(R.id.tvItembtnSelectMVCaption2))
                    .setTextColor(mTxColorN);
        } else {
            mSelectPhoto1.setLayoutParams(photolp);
            $(R.id.lvSelectPhoto1).setVisibility(View.VISIBLE);
            $(R.id.btnSelectPhoto1).setVisibility(currentVisisble);
            ((TextView) $(R.id.tvItembtnSelectPhotoCaption1))
                    .setTextColor(mTxColorP);
            if (hideRec) {
                mSelectRec1.setVisibility(View.GONE);
            }
            reclp.addRule(RelativeLayout.LEFT_OF, R.id.lvSelectPhoto1);
            mSelectRec1.setLayoutParams(reclp);
            $(R.id.btnSelectRec1).setVisibility(View.INVISIBLE);
            ((TextView) $(R.id.tvItembtnSelectRecCaption1))
                    .setTextColor(mTxColorN);

            if (hideRec) {
                mvlp.addRule(RelativeLayout.LEFT_OF, R.id.lvSelectPhoto1);
            } else {
                mvlp.addRule(RelativeLayout.LEFT_OF, R.id.lvSelectRec1);
            }
            if (hideMV) {
                mSelectMV1.setVisibility(View.GONE);
            }
            mvlp.addRule(RelativeLayout.LEFT_OF, R.id.lvSelectRec1);
            mSelectMV1.setLayoutParams(mvlp);
            $(R.id.btnSelectMV1).setVisibility(View.INVISIBLE);
            ((TextView) $(R.id.tvItembtnSelectMVCaption1))
                    .setTextColor(mTxColorN);
        }
    }

    private void switchRecOrPhotoItemLayout() {
        createLayoutParams();
        //其他两项功能都关闭，则当前功能的提示也关闭
        if (curPosition == POSITION_MV) {
            onPositionMV((hideRec && hidePhoto) ? View.INVISIBLE : View.VISIBLE);
        } else if (curPosition == POSITION_REC) {
            onPositionRec((hideMV && hidePhoto) ? View.INVISIBLE : View.VISIBLE);
        } else if (curPosition == POSITION_PHOTO) {
            onPositionPhoto((hideMV && hideRec) ? View.INVISIBLE : View.VISIBLE);
        }
    }

    /**
     * 隐藏音乐菜单
     */
    private void goneMusicLayout() {
        $(R.id.rrlAddMusic).setVisibility(View.GONE);
        $(R.id.rrlAddMusic1).setVisibility(View.GONE);
    }

    /**
     * 显示音乐菜单
     */
    private void showMusicLayout() {
        $(R.id.rrlAddMusic).setVisibility(View.VISIBLE);
        $(R.id.rrlAddMusic1).setVisibility(View.VISIBLE);
    }

    /**
     * 切换拍摄和照相功能
     */
    private void switchRecOrPhoto() {

        if (curPosition == POSITION_PHOTO) {
            bSelectPhoto = true;
            mBtnRecord.setBackgroundResource(R.drawable.btn_photo_n);
            mBtnRecord1.setBackgroundResource(R.drawable.btn_photo_n);
            mVideoNewRelative.setVisibility(View.INVISIBLE);
            mVideoNewRelative1.setVisibility(View.INVISIBLE);
            tvTimer.setVisibility(View.INVISIBLE);
            tvTimer1.setVisibility(View.INVISIBLE);
            goneMusicLayout();
        } else if (curPosition == POSITION_MV) {
            bSelectPhoto = false;
            mBtnRecord
                    .setBackgroundResource(R.drawable.btn_shutter_stop_record);
            mBtnRecord1
                    .setBackgroundResource(R.drawable.btn_shutter_stop_record);
            mVideoNewRelative.setVisibility(View.VISIBLE);
            mVideoNewRelative1.setVisibility(View.VISIBLE);
            if (cameraConfig.enablePlayMusic) {
                showMusicLayout();
            } else {
                goneMusicLayout();
            }

        } else if (curPosition == POSITION_REC) {
            bSelectPhoto = false;
            mBtnRecord
                    .setBackgroundResource(R.drawable.btn_shutter_stop_record);
            mBtnRecord1
                    .setBackgroundResource(R.drawable.btn_shutter_stop_record);
            if (mVideoMaxTime == 0) {
                mVideoNewRelative.setVisibility(View.INVISIBLE);
                mVideoNewRelative1.setVisibility(View.INVISIBLE);
            } else {
                mVideoNewRelative.setVisibility(View.VISIBLE);
                mVideoNewRelative1.setVisibility(View.VISIBLE);
            }
            if (cameraConfig.enablePlayMusic) {
                showMusicLayout();
            } else {
                goneMusicLayout();
            }

        }
        changeLayoutWithOrientation(mOrientationCompensation);
    }

    /**
     * 重置美颜按钮的状态
     */
    private void checkBeauty() {
        boolean enabled = true;
        if (hasJben_MR2 && enableFace) {
            enabled = ((null != faceUnityHandler) ? faceUnityHandler
                    .isEnabledBeautify() : false);
        } else {
            enabled = RecorderCore.isBeautifyEnabled();
        }
        boolean isSupportBeauty = RecorderCore.isSupportBeautify();
        //正方形录制时的美颜按钮
        if (enabled) {
            mBtnBeauty1.setImageResource(R.drawable.living_beauty_p);
        } else {
            mBtnBeauty1.setImageResource(R.drawable.living_beauty_n);
        }
        if (isSupportBeauty) {
            mBtnBeauty1.setVisibility(View.VISIBLE);
        } else {
            mBtnBeauty1.setVisibility(View.GONE);
        }
        //长方形录制时的美颜按钮
        if (enabled) {
            mBtnBeauty.setImageResource(R.drawable.living_beauty_p);
        } else {
            mBtnBeauty.setImageResource(R.drawable.living_beauty_n);
        }
        if (isSupportBeauty) {
            mBtnBeauty.setVisibility(View.VISIBLE);
        } else {
            mBtnBeauty.setVisibility(View.GONE);
        }


    }


    /**
     * isFullScreen  是否是全屏模式
     * 录制回调
     */
    class IRecoder implements IRecorderCallBackShot {

        @Override
        public void onPermissionFailed(int nResult, String strResultInfo) {
            mIsRecording = false;
            if (isFullScreen) {
                setLiveStreamStatus(false);
                tvTimer.setVisibility(View.GONE);
            } else {
                tvTimer1.setVisibility(View.GONE);
                setLiveStreamStatus(false);
            }
            mLocalSaveFileNameStr = null;
        }

        /**
         * 截取回调
         *
         * @param nResult       =ResultConstants.SUCCESS
         * @param strResultInfo 图片本地路径
         */
        @Override
        public void onScreenShot(int nResult, String strResultInfo) {
            LogUtil.i(TAG, "onScreenShot: " + nResult + " > " + strResultInfo);
            shootSound();
            if (editResult) {
                insertPicToGallery(strResultInfo);
                mLocalSavePicNameStr = strResultInfo;
                gotoEdit();
            } else if (mUseMultiShoot) {
                insertPicToGallery(strResultInfo);
            } else {
                if (mIsSaveToAlbum) {
                    insertPicToGallery(strResultInfo);
                }
                mLocalSavePicNameStr = strResultInfo;
                gotoEdit();
            }
        }

        @Override
        public void onRecordFailed(int nResult, String strResultInfo) {
            Log.e(TAG, "onRecordFailed:" + nResult + " >" + strResultInfo);
            mIsRecording = false;
            isFailed = true;
            if (nResult == ResultConstants.ERROR_AUDIO_RECORD_START) {
                onAutoToast(
                        getString(R.string.dialog_tips),
                        getString(R.string.permission_audio_error_p_allow));
            } else if (nResult == ResultConstants.ERROR_ENCODE_AUDIO) {
                onAutoToast(
                        getString(R.string.dialog_tips),
                        getString(R.string.error_code, nResult));
            }
            if (isFullScreen) {
                tvTimer.setVisibility(View.GONE);
            } else {
                tvTimer1.setVisibility(View.GONE);

            }
            setLiveStreamStatus(false);
            mLocalSaveFileNameStr = null;
        }

        private boolean isFailed = false;
        private boolean save = true;

        public void setSave(boolean isSave) {
            save = isSave;
        }

        @Override
        public void onRecordEnd(int nResult, String strResultInfo) {
            mIsRecording = false;
            if (needPostRecycleCameraView) {
                needPostRecycleCameraView = false;
                RecorderCore.recycleCameraView();
            }
            recordEndUI();
            if (isFullScreen) {
                if (nResult >= ResultConstants.SUCCESS) {
                    VideoConfig vcRecord = new VideoConfig();
                    onCheckRDEncypt(mLocalSaveFileNameStr);
                    int lDuration = Utils.s2ms(VirtualVideo.getMediaInfo(mLocalSaveFileNameStr, vcRecord));
                    if (lDuration > 0) {
                        if (!startTrailer) {
                            ImageView img = (ImageView) mLinearSeekbar.getChildAt(mLinearSeekbar.getChildCount() - 1);
                            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) img.getLayoutParams();
                            int maxTime = 0;
                            if (curPosition == POSITION_REC) {
                                maxTime = mVideoMaxTime;
                            } else if (curPosition == POSITION_MV) {
                                maxTime = mMVMaxTime;
                            }
                            layoutParams.width = (int) (m_DisplayWidth * ((float) lDuration / maxTime)) + 1;
                            img.setLayoutParams(layoutParams);
                            totalTime += lDuration;
                        }
                    }
                    curTotal = totalTime;
                    try {
                        MediaObject vo = VirtualVideo.createScene().addMedia(mLocalSaveFileNameStr);
                        mRecordVideoList.add(vo);
                    } catch (InvalidArgumentException e) {
                        e.printStackTrace();
                    }
                    if (!startTrailer) {
                        if (curPosition == POSITION_MV) {
                            initScreenDuration(Utils.stringForTime(totalTime, false, true));
                        } else if (curPosition == POSITION_REC) {
                            if (mVideoMaxTime != 0) {
                                initScreenDuration(Utils.stringForTime(totalTime, false, true));
                            } else {
                                initScreenDuration(Utils.stringForTime(totalTime, true, false));
                            }
                        } else {
                            initScreenDuration(Utils.stringForTime(totalTime, true, false));
                        }
                    }
                    setLiveStreamStatus(false);
                    if (!startTrailer) {
                        InitBtnBlack();
                        addView_black();
                        int total = 0;
                        for (int n = 0; n < mLinearSeekbar.getChildCount(); n++) {
                            int width = mLinearSeekbar.getChildAt(n).getWidth();
                            if (width == 0) {
                                width = CoreUtils.dpToPixel(1);
                            }
                            total += width;
                        }
                        mTotalWidth = total;
                    }
                    if (save) {
                        gotoEdit();
                    }

                }

            } else {
                if (nResult >= ResultConstants.SUCCESS) {
                    VideoConfig vcRecord = new VideoConfig();
                    onCheckRDEncypt(mLocalSaveFileNameStr);
                    long lDuration = Utils.s2ms(VirtualVideo.getMediaInfo(mLocalSaveFileNameStr, vcRecord));
                    if (lDuration > 0) {
                        if (!startTrailer) {
                            ImageView img = (ImageView) mLinearSeekbar1
                                    .getChildAt(mLinearSeekbar1.getChildCount() - 1);
                            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) img
                                    .getLayoutParams();
                            int maxTime = 0;
                            if (curPosition == POSITION_REC) {
                                maxTime = mVideoMaxTime;
                            } else if (curPosition == POSITION_MV) {
                                maxTime = mMVMaxTime;
                            }
                            int childWidth = (int) (m_DisplayWidth * ((float) lDuration / maxTime)) + 1;
                            layoutParams.width = childWidth;
                            img.setLayoutParams(layoutParams);
                            totalTime += lDuration;
                            curTotal = totalTime;
                        }
                    } else {
                        setLiveStreamStatus(false);
                        return;
                    }
                } else {
                    setLiveStreamStatus(false);
                    return;
                }

                try {
                    mRecordVideoList.add(VirtualVideo.createScene().addMedia(mLocalSaveFileNameStr));
                } catch (InvalidArgumentException e) {
                    e.printStackTrace();
                }
                if (!startTrailer) {
                    if (curPosition == POSITION_MV) {
                        tvTimer1.setText(Utils.stringForTime(totalTime, false, true));
                    } else if (curPosition == POSITION_REC) {
                        if (mVideoMaxTime != 0) {
                            tvTimer1.setText(Utils.stringForTime(totalTime, false, true));
                        } else {
                            tvTimer1.setText(Utils.stringForTime(totalTime, true, false));
                        }
                    } else {
                        tvTimer1.setText(Utils.stringForTime(totalTime, true, false));
                    }
                }
                if (!startTrailer) {
                    setLiveStreamStatus(false);
                    addView_black();
                    int total = 0;
                    for (int n = 0; n < mLinearSeekbar1.getChildCount(); n++) {
                        int width = mLinearSeekbar1.getChildAt(n).getWidth();
                        if (width == 0) {
                            width = CoreUtils.dpToPixel(1);
                        }
                        total += width;
                    }
                    mTotalWidth = total;
                }
                if (save) {
                    gotoEdit();
                }
            }
        }

        @Override
        public void onRecordBegin(int nResult, String strResultInfo) {
            recordBeginUI();
            if (isFailed) {
                isFailed = false;
                return;
            }
            if (isFullScreen) {
                if (!startTrailer) {
                    if (mLinearSeekbar.getChildCount() >= 2) {
                        ((ImageView) mLinearSeekbar.getChildAt(mLinearSeekbar
                                .getChildCount() - 2))
                                .setBackgroundColor(getResources().getColor(
                                        R.color.linear_seekbar_color));
                    }
                    addView_Red();
                    mBtnRecord.setBackgroundResource(R.drawable.btn_record_n);
                }
            } else {
                if (!startTrailer) {
                    if (mLinearSeekbar1.getChildCount() >= 2) {
                        ((ImageView) mLinearSeekbar1.getChildAt(mLinearSeekbar1
                                .getChildCount() - 2))
                                .setBackgroundColor(getResources().getColor(
                                        R.color.linear_seekbar_color));
                    }
                    addView_Red();
                    mBtnRecord1.setBackgroundResource(R.drawable.btn_record_n);
                }
            }
            if (RecorderCore.isRecording()) {
                setLiveStreamStatus(true);
                if (!startTrailer) {
                    ctlTimerCounter(true);
                }
            } else {
                setLiveStreamStatus(false);
                ctlTimerCounter(false);
            }
        }

        @Override
        public void onCamera(int nResult, String strResultInfo) {
            if (nResult == ResultConstants.ERROR_CAMERA_OPEN_FAILED) {
                bCameraPrepared = false;
                onCameraPermissionFailed();
            }
        }

        @Override
        public void onPrepared(int nResult, String strResultInfo) {
            if (nResult == ResultConstants.SUCCESS) {
                isFrontCamera = RecorderCore.isFaceFront();
                if (enableFace && null != faceUnityHandler) {
                    faceUnityHandler.onInitFaceunity();
                }
                resetBeautify();
                mBtnSwitchCamera.setVisibility(View.VISIBLE);
                mBtnSwitchCamera1.setVisibility(View.VISIBLE);
                initCameraFilterListItems(); // 初始滤镜
                mHandler.postDelayed(mRunnableEffect, 300);
                startOpenCamGate();
                onSureBg();
                if (null != faceUnityHandler) {
                    faceUnityHandler.setFuNotifyPause(false);
                }
            }
        }

        @Override
        public void onGetRecordStatus(int nPosition, int arg1, int arg2) {
            if (startTrailer) {
                if (nPosition > trailerTime || nPosition > osdEnd) {
                    onCloseOrPauseRecordClick();
                }
                return;
            }
            if (isFullScreen) {
                if (curPosition == POSITION_MV) {
                    int position = nPosition;
                    if (mLinearSeekbar.getChildCount() > 0 && mIsRecording) {
                        ImageView img = (ImageView) mLinearSeekbar.getChildAt(mLinearSeekbar.getChildCount() - 1);
                        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) img.getLayoutParams();
                        int childWidth = (int) (m_DisplayWidth * ((float) position / mMVMaxTime)) + 1;
                        if ((totalTime + nPosition) > mMVMaxTime) {
                            onCloseOrPauseRecordClick();
                            return;
                        } else {
                            layoutParams.width = childWidth;
                            img.setLayoutParams(layoutParams);
                        }
                    }
                    initScreenDuration(Utils.stringForTime(totalTime + nPosition, false, true));
                } else if (curPosition == POSITION_REC) {
                    if (mVideoMaxTime != 0) {
                        int position = nPosition;
                        if (mLinearSeekbar.getChildCount() > 0 && mIsRecording) {
                            ImageView img = (ImageView) mLinearSeekbar.getChildAt(mLinearSeekbar.getChildCount() - 1);
                            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) img.getLayoutParams();
                            if ((totalTime + nPosition) > mVideoMaxTime) {
                                onCloseOrPauseRecordClick();
                                return;
                            } else {
                                layoutParams.width = (int) (m_DisplayWidth * ((float) position / mVideoMaxTime)) + 1;
                                img.setLayoutParams(layoutParams);
                            }
                        }
                        initScreenDuration(Utils.stringForTime(totalTime + nPosition, false, true));
                    } else {
                        initScreenDuration(Utils.stringForTime(totalTime + nPosition, true, false));
                    }
                }
                curTotal = totalTime + nPosition;
                setRecordOSDProgress(curTotal);
            } else {
                if (curPosition == POSITION_MV) {
                    int position = nPosition;
                    if (mLinearSeekbar1.getChildCount() > 0 && mIsRecording) {
                        ImageView img = (ImageView) mLinearSeekbar1
                                .getChildAt(mLinearSeekbar1.getChildCount() - 1);
                        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) img.getLayoutParams();
                        int childWidth = (int) (m_DisplayWidth * ((float) position / mMVMaxTime)) + 1;

                        if ((totalTime + nPosition) > mMVMaxTime) {
                            onCloseOrPauseRecordClick();
                            return;
                        } else {
                            layoutParams.width = childWidth;
                            img.setLayoutParams(layoutParams);
                        }
                    }
                    tvTimer1.setText(Utils.stringForTime(totalTime + nPosition,
                            false, true));
                } else if (curPosition == POSITION_REC) {
                    if (mVideoMaxTime != 0) {
                        int position = nPosition;
                        if (mLinearSeekbar1.getChildCount() > 0 && mIsRecording) {
                            ImageView img = (ImageView) mLinearSeekbar1
                                    .getChildAt(mLinearSeekbar1.getChildCount() - 1);
                            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) img
                                    .getLayoutParams();
                            int childWidth = (int) (m_DisplayWidth * ((float) position / mVideoMaxTime)) + 1;
                            curTotal = totalTime + nPosition;
                            if ((totalTime + nPosition) > mVideoMaxTime) {
                                onCloseOrPauseRecordClick();
                                return;
                            } else {
                                layoutParams.width = childWidth;
                                img.setLayoutParams(layoutParams);
                            }
                        }
                        tvTimer1.setText(Utils.stringForTime(totalTime + nPosition, false, true));
                    } else {
                        tvTimer1.setText(Utils.stringForTime(totalTime + nPosition, true, false));
                    }

                }
                curTotal = totalTime + nPosition;
                setRecordOSDProgress(curTotal);
            }
        }

    }

    private void recordBeginUI() {
        setConfigEnabled(false);
        m_btnWaiting.setEnabled(false);
        m_btnWaiting1.setEnabled(false);
    }

    private void recordEndUI() {
        m_btnWaiting.setEnabled(true);
        m_btnWaiting1.setEnabled(true);
    }

    private void setConfigEnabled(boolean enable) {
        mBtnConfig.setEnabled(enable);
        mBtnConfig1.setEnabled(enable);
    }

    /**
     * 切换录制比例按钮状态设置
     */
    private void InitBtnShootingRatio() {
        if (mRecordVideoList.size() > 0 || mIsRecording) {
            mBtnShootingRatio1.setEnabled(false);
            mBtnShootingRatio.setEnabled(false);
            mBtnShootingRatio90.setEnabled(false);
            mBtnShootingRatio270.setEnabled(false);
            mLayoutSelectRecOrPhoto1.setVisibility(View.INVISIBLE);
            mLayoutSelectRecOrPhoto2.setVisibility(View.INVISIBLE);
            setConfigEnabled(false);
        } else {
            mLayoutSelectRecOrPhoto1.setVisibility(View.VISIBLE);
            mLayoutSelectRecOrPhoto2.setVisibility(View.VISIBLE);
            mBtnShootingRatio1.setEnabled(true);
            mBtnShootingRatio1
                    .setImageResource(R.drawable.btn_shooting_ratio_n);
            mBtnShootingRatio.setEnabled(true);
            mBtnShootingRatio
                    .setImageResource(R.drawable.btn_shooting_ratio_n);
            mBtnShootingRatio90.setEnabled(true);
            mBtnShootingRatio90
                    .setImageResource(R.drawable.btn_shooting_ratio_n);
            mBtnShootingRatio270.setEnabled(true);
            mBtnShootingRatio270.setImageResource(R.drawable.btn_shooting_ratio_n);
            setConfigEnabled(true);
        }
    }


    /**
     * 黑屏按钮设置
     */
    private void InitBtnBlack() {
        if (mIsRecording) {
            m_btnBlackScreen
                    .setImageResource(R.drawable.main_camera_blackscreen_btn_back);
            mBtnBlackScreen1
                    .setImageResource(R.drawable.main_camera_blackscreen_btn_back);
            mBtnBlackScreen90
                    .setImageResource(R.drawable.main_camera_blackscreen_btn_back);
            mBtnBlackScreen270
                    .setImageResource(R.drawable.main_camera_blackscreen_btn_back);
            m_btnBlackScreen.setAlpha(1f);
            mBtnBlackScreen1.setAlpha(1f);
            mBtnBlackScreen90.setAlpha(1f);
            mBtnBlackScreen270.setAlpha(1f);
        } else {
            m_btnBlackScreen
                    .setImageResource(R.drawable.main_camera_blackscreen_btn_back_un);
            mBtnBlackScreen1
                    .setImageResource(R.drawable.main_camera_blackscreen_btn_back_un);
            mBtnBlackScreen90
                    .setImageResource(R.drawable.main_camera_blackscreen_btn_back_un);
            mBtnBlackScreen270
                    .setImageResource(R.drawable.main_camera_blackscreen_btn_back_un);
            m_btnBlackScreen.setAlpha(0.5f);
            mBtnBlackScreen1.setAlpha(0.5f);
            mBtnBlackScreen90.setAlpha(0.5f);
            mBtnBlackScreen270.setAlpha(0.5f);
        }
    }

    private final int ALBUM_REQUEST_CODE = 11;

    /**
     * 调用相册
     */
    private void goToAlbum() {
        setResult(SdkEntry.RESULT_CAMERA_TO_ALBUM);
        finish();
    }

    /**
     * 创建音频播放器
     */
    private void initPlayer() {
        if (cameraConfig.enablePlayMusic) {
            if (null != mAudioPlayer) {
                mAudioPlayer.stop();
            } else {
                mAudioPlayer = new AudioPlayer();
                mAudioPlayer.setOnErrorListener(new AudioPlayer.OnErrorListener() {

                    @Override
                    public boolean onError(AudioPlayer mp, int what, int extra) {
                        Log.e(TAG, "AudioPlayer_onerror.." + what
                                + "..." + extra);
                        onAutoToast("", "不支持该音乐");
                        return false;
                    }
                });
            }
        }
    }

    private String mDefaultMusicPath;

    /**
     * 初始化默认的音乐
     */
    private void exportDefaultMusic() {
        if (cameraConfig.enablePlayMusic) {
            String name = "huiyi.mp3";
            mDefaultMusicPath = PathUtils.getAssetFileNameForSdcard("huiyi", ".mp3");
            if (!FileUtils.isExist(mDefaultMusicPath)) {
                CoreUtils.assetRes2File(getAssets(), name, mDefaultMusicPath);
            }
        }

    }

    /**
     * 初始化默认混音
     */
    private void initDefaultMusic() {

        if (cameraConfig.enablePlayMusic && FileUtils.isExist(mDefaultMusicPath)) {
            int duration = MiscUtils.s2ms(VirtualVideo.getMediaInfo(mDefaultMusicPath, null));
            mAudioMusic = new AudioMusicInfo(mDefaultMusicPath, "回忆", 0, duration, duration);
            initMP3Player();
        }
    }


    //是否需要重新初始化播放器资源
    private boolean bNeedInitMp3Player = true;

    /**
     * 初始化混音播放器
     */
    private void initMP3Player() {
        if (mAudioPlayer != null && mAudioMusic != null && bNeedInitMp3Player) {
            bNeedInitMp3Player = false;
            mAudioPlayer.stop();
            try {
                mAudioPlayer.setDataSource(mAudioMusic.getPath());
                mAudioPlayer.setAutoRepeat(true);
                mAudioPlayer.setTimeRange(mAudioMusic.getStart(), mAudioMusic.getEnd());
                mAudioPlayer.prepareAsync();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mllAddMusic.setVisibility(View.VISIBLE);
            mBtnAddMusic.setVisibility(View.GONE);
            mTvMusicNameScreen.setText(mAudioMusic.getName());
            mllAddMusic1.setVisibility(View.VISIBLE);
            mBtnAddMusic1.setVisibility(View.GONE);
            mTvMusicNameSquare.setText(mAudioMusic.getName());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CONFIG) {
                //onstart()重新初始化摄像头
            } else if (requestCode == ALBUM_REQUEST_CODE) {
                setResult(RESULT_OK, data);
                finishWithoutGate = true;
                finish();
            } else if (requestCode == VideoEditActivity.REQUSET_MUSICEX) {
                if (null != data) {
                    //混音
                    mAudioMusic = (AudioMusicInfo) data
                            .getParcelableExtra(MoreMusicActivity.MUSIC_INFO);
                    bNeedInitMp3Player = true;
                    initMP3Player();
                }
            }
        } else if (resultCode == RESULT_FIRST_USER) {
            finishWithoutGate = true;
            finish();
        }
    }


    /**
     * 改变设备方向，防止m_btnRecord响应onTouch();
     */
    private void onOrientationFilter() {
        if (mOrientationCompensation == 0 && bShowFitlerListLayout) {
            mBtnRecord.setEnabled(false);
        } else {
            mBtnRecord.setEnabled(true);
        }
    }

    private VEOSDBuilder osd = null;

    /**
     * 退出后重新设置是否开启美颜
     */
    private void resetBeautify() {
        if (RecorderCore.isSupportBeautify()) {
            if (hasJben_MR2 && enableFace) {
                AppConfiguration.enableBeauty(lastEnableBeauty);
                faceUnityHandler.enableBeautify(lastEnableBeauty);

            } else {
                AppConfiguration.enableBeauty(lastEnableBeauty);
                RecorderCore.enableBeautify(lastEnableBeauty);
            }
        }
        checkBeauty();
    }

    private void onBeautifyClick() {
        if (RecorderCore.isSupportBeautify()) {
            if (hasJben_MR2 && enableFace) {
                boolean re = !faceUnityHandler.isEnabledBeautify();
                AppConfiguration.enableBeauty(re);
                lastEnableBeauty = re;
                faceUnityHandler.enableBeautify(re);

            } else {
                boolean re = !RecorderCore.isBeautifyEnabled();
                AppConfiguration.enableBeauty(re);
                lastEnableBeauty = re;
                RecorderCore.enableBeautify(re);
            }
        }
        checkBeauty();
    }

    private void backBeauty(Intent intent) {
        if (enableFace) {
            if (null != faceUnityHandler) {
                faceUnityHandler.saveFaceU(intent);
            }

        }
    }

    /**
     * 获取当前视频的信息，保存到图库
     *
     * @param videoPath
     */
    private void doSaveAlbum(String videoPath) {
        if (!TextUtils.isEmpty(videoPath)) {
            try {
                VideoConfig vcMediaInfo = new VideoConfig();
                int duration = Utils.s2ms(VirtualVideo.getMediaInfo(videoPath, vcMediaInfo));
                Utils.insertToGallery(this, videoPath, duration, vcMediaInfo.getVideoWidth(), vcMediaInfo.getVideoHeight());
            } catch (Exception ex) {
            }
        }
    }

    /***
     * 显示Toast
     * @param title
     * @param msg
     */
    private void onAutoToast(String title, String msg) {
        SysAlertDialog.showAutoHideDialog(RecorderActivity.this, title, msg, Toast.LENGTH_SHORT);
    }

    /**
     * 合并多段视频
     *
     * @param listener
     */
    private void fastSave(final ExportEndListener listener) {
        ExportUtils.fastSave(RecorderActivity.this, mRecordVideoList, mLocalSaveFileNameStr, new ExportListener() {
            @Override
            public void onExportStart() {
                SysAlertDialog.showLoadingDialog(
                        RecorderActivity.this, null);
            }

            @Override
            public boolean onExporting(int progress, int max) {
                return true;
            }

            @Override
            public void onExportEnd(int nResult) {

                if (null != listener) {
                    listener.onExportEnd(nResult);
                }
            }
        });
    }

    interface ExportEndListener {
        void onExportEnd(int nResult);
    }


    private void backUseMvEdit(Intent intent) {
        if (curPosition == POSITION_MV) {
            intent.putExtra(SdkEntry.INTENT_KEY_USE_MV_EDIT, true);
        } else {
            intent.putExtra(SdkEntry.INTENT_KEY_USE_MV_EDIT, false);
        }
    }

}