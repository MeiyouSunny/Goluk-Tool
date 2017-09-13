package com.rd.veuisdk;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.provider.MediaStore.Video;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
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

import com.rd.gallery.ImageManager;
import com.rd.lib.ui.ExtButton;
import com.rd.lib.ui.PreviewFrameLayout;
import com.rd.lib.ui.Rotatable;
import com.rd.lib.ui.RotateImageView;
import com.rd.lib.ui.RotateRelativeLayout;
import com.rd.lib.utils.CoreUtils;
import com.rd.lib.utils.ThreadPoolUtils;
import com.rd.recorder.AudioPlayer;
import com.rd.recorder.ResultConstants;
import com.rd.recorder.api.IRecorderCallBackShot;
import com.rd.recorder.api.RecorderConfig;
import com.rd.recorder.api.RecorderCore;
import com.rd.vecore.VirtualVideo;
import com.rd.vecore.listener.ExportListener;
import com.rd.vecore.models.MediaObject;
import com.rd.vecore.models.VideoConfig;
import com.rd.vecore.utils.ExportUtils;
import com.rd.vecore.utils.Log;
import com.rd.veuisdk.database.HistoryMusicCloud;
import com.rd.veuisdk.faceu.FaceuHandler;
import com.rd.veuisdk.faceu.IReloadListener;
import com.rd.veuisdk.manager.CameraConfiguration;
import com.rd.veuisdk.manager.VEOSDBuilder;
import com.rd.veuisdk.manager.VEOSDBuilder.OSDState;
import com.rd.veuisdk.model.AudioMusicInfo;
import com.rd.veuisdk.ui.GlTouchView;
import com.rd.veuisdk.ui.GlTouchView.CameraCoderViewListener;
import com.rd.veuisdk.ui.HorizontalListViewCamera;
import com.rd.veuisdk.utils.AppConfiguration;
import com.rd.veuisdk.utils.CheckSDSize;
import com.rd.veuisdk.utils.FileLog;
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
public class RecorderActivity extends BaseActivity {

    private final int BUTTON_STATE_START = 0;
    private final int BUTTON_STATE_LIVING = 1;
    private final int BUTTON_STATE_PAUSE = 2;
    private final int REQUEST_CODE_PERMISSIONS = 1;
    private final String LOG_TAG = "RecorderActivity";
    private final String TAG = RecorderActivity.class.toString();
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
    private RotateImageView mBtnFlashModeCtrl, mBtnFlashModeCtrl1;
    private RotateRelativeLayout mRlFilterList, mSelectRec1, mSelectMV1,
            mSelectMV2, mRecordRRL, mSelectRec2, mSelectPhoto1,
            mSelectPhoto2, mBtnBottomRightForLandscape,
            mBtnBottomRight, mBtnBottomLeftLayout, mTimerTv;

    private RotateImageView mBtnShootingRatio, mBtnShootingRatio1;

    /**
     * 摄象头滤镜Listview
     */
    protected HorizontalListViewCamera mLvCameraFilter;
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
    private boolean bCameraPrepared = false, bSelectPhoto;
    private GlTouchView mGlTouchView;
    /**
     * 变焦handler
     */
    private CameraZoomHandler m_hlrCameraZoom;
    private Dialog mDlgCameraFailed;
    private com.rd.lib.ui.PreviewFrameLayout mRlframeSquarePreview, mFocusFrameLayout;
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
    private LinearLayout mLinearSeekbar, mLinearSeekbar1, mllAddMusic,
            mllAddMusic1;

    private ExtButton m_btnBottomRight, m_btnBottomRightForSquare,
            m_btnBottomRightForLandscape;
    private ArrayList<MediaObject> mRecordVideoList = new ArrayList<MediaObject>();
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
    private int formatType = -1;
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

    //录制模式（长方形、正方形）
    private boolean isScreen = true;

    private PreviewFrameLayout cameraPreview;
    private RelativeLayout cameraParent;

    //onprepared 已经回调成功
    private boolean bOnPrepred = false;
    private int mSquareTitlebarHeight = 0;
    private FrameLayout.LayoutParams mCameraParams = null;
    //录制方向
    private int mRecordOrientation = CameraConfiguration.ORIENTATION_AUTO;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCurrentEffectIndex = 0;
        hasJben_MR2 = CoreUtils.hasJELLY_BEAN_MR2();//4.3以下不支持切换录制模式
        requestWindowFeature(Window.FEATURE_NO_TITLE); // 设置无标题
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);// 去掉信息栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

//        Log.e("oncreate", "______>>>>>>>>>>>>>" + this.toString());
        lastEnableBeauty = AppConfiguration.enableBeauty();
        iListener = new IRecoder();
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
        // trailerTime = Math.max(0,
        // Math.min(1000, (int) (cameraConfig.cameraTrailerTime * 1000)));
        osdHeader = Math.max(0,
                Math.min(2000, (int) (cameraConfig.cameraOsdHeader * 1000)));
        osdEnd = Math.max(0,
                Math.min(2000, (int) (cameraConfig.cameraOsdEnd * 1000)));
        trailerTime = osdEnd;

        // Log.e("osdHeader", osdHeader + "..." + osdEnd + "..." +
        // enableWatermark
        // + "...." + isEncryption);

        hideAlbum = !cameraConfig.enableAlbum;
        isEncryption = cameraConfig.enableAntiChange;
        enableFace = cameraConfig.enableFaceU;
        byte[] pack = cameraConfig.pack;

        hideMV = cameraConfig.hideMV;
        hideRec = cameraConfig.hideRec;
        hidePhoto = cameraConfig.hidePhoto;
        // m_bTakePhotoReturnPath = !mUseMultiShoot;

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
        mStrActivityPageName = getString(R.string.activity_page_name_livecamera);
        setContentView(R.layout.main_camera);
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

        // Log.e("editResult", editResult + "...." + mRecordOrientation
        // + "......." + cameraConfig.orientation);
        mRecordOrientation = CameraConfiguration.ORIENTATION_AUTO;
        if (editResult) {
            mSquareCameraLayout.setVisibility(View.INVISIBLE);
            mScreenCameraLayout.setVisibility(View.VISIBLE);
            isScreen = true;

            if (mUIType == CameraConfiguration.WIDE_SCREEN_CAN_CHANGE) {

                /**
                 * 默认16：9
                 */
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
                isScreen = true;
                /**
                 * 默认16：9
                 */
                mRecordOrientation = cameraConfig.orientation;
            } else if (mUIType == CameraConfiguration.SQUARE_SCREEN_CAN_CHANGE) {
                mScreenCameraLayout.setVisibility(View.INVISIBLE);
                mSquareCameraLayout.setVisibility(View.VISIBLE);
                isScreen = false;
                // 可自动切换时，16:9锁屏有效
                mRecordOrientation = cameraConfig.orientation;
            } else if (mUIType == CameraConfiguration.ONLY_SQUARE_SCREEN) {
                mScreenCameraLayout.setVisibility(View.INVISIBLE);
                mSquareCameraLayout.setVisibility(View.VISIBLE);
                mBtnShootingRatio1.setVisibility(View.GONE);
                isScreen = false;
            } else if (mUIType == CameraConfiguration.ONLY_WIDE_SCREEN) {
                mScreenCameraLayout.setVisibility(View.VISIBLE);
                mSquareCameraLayout.setVisibility(View.INVISIBLE);
                mBtnShootingRatio.setVisibility(View.GONE);
                isScreen = true;
                // 仅全屏录制时，锁屏有效
                mRecordOrientation = cameraConfig.orientation;
            }
        }

        if (!hasJben_MR2) {//4.3以下只支持竖屏全屏(16:9)
            mScreenCameraLayout.setVisibility(View.VISIBLE);
            mSquareCameraLayout.setVisibility(View.INVISIBLE);
            mUIType = CameraConfiguration.ONLY_WIDE_SCREEN;
            isScreen = true;
            mRecordOrientation = cameraConfig.orientation;
        }

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
                ((RadioGroup) findViewById(R.id.camare_filter_s)),
                mLvCameraFilter, findViewById(R.id.camare_filter_layout),
                (enableFace ? pack : null), SdkEntry.getSdkService()
                .getFaceUnityConfig(), null,
                (LinearLayout) findViewById(R.id.fuLayout),
                (LinearLayout) findViewById(R.id.fuLayout_parent), ((LinearLayout) findViewById(R.id.filter_parent_layout)), new IReloadListener() {
            @Override
            public void onReloadFilters() {
                if (bCameraPrepared) {
                    List<String> effects = RecorderCore.getSupportedColorEffects();
                    if (null != mCameraEffectHandler) {
                        mCameraEffectHandler.initAllEffects(mLvCameraFilter, effects);
                        mLvCameraFilter.selectListItem(mCurrentEffectIndex);
                    }
                }
            }
        });
        if (hasJben_MR2 && enableFace) {
            faceUnityHandler.registerCallBack();
        }
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
        // Log.e("onCheckLock", nowIsSQUARE + ".." + editResult + "...."
        // + mRecordOrientation + "...." + mUIType);
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
        if (null == cameraParent) {
            cameraParent = (RelativeLayout) findViewById(R.id.cameraParentLayout);
            cameraPreview = (PreviewFrameLayout) findViewById(R.id.cameraPreviewLayout);
            mFocusFrameLayout = (PreviewFrameLayout) findViewById(R.id.focus_frame_layout);
            //设置滤镜左右切换，限制可组件的区域部分支持，注意保留选音乐、切前后置可点击
            mFocusFrameLayout.setAspectRatio(1.2f);
            mSquareTitlebarHeight = getResources().getDimensionPixelSize(R.dimen.record_titlebar_height);
            mCameraParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        }
        if (isScreen) {//初始化全屏模式
            //在设置录制父容器大小之前设置录制视频的参数
            RecorderConfig config = new RecorderConfig().setVideoSize(360, 640)
                    .setVideoFrameRate(24).setVideoBitrate(BIT).setEnableFront(isFrontCamera)
                    .setEnableBeautify(canBeautiy).setBeauitifyLevel(5)
                    .setEnableFrontMirror(enableFrontMirror);
            RecorderCore.setEncoderConfig(config);
            RecorderCore.enableFaceU(enableFace);

            //设置录制父容器的大小
            DisplayMetrics display = CoreUtils.getMetrics();
            mCameraParams.setMargins(0, 0, 0, 0);
            cameraPreview.setLayoutParams(mCameraParams);
            cameraPreview.setAspectRatio(display.widthPixels / (display.heightPixels + 0.0));
        } else {//初始化正方形模式

            //在设置录制父容器大小之前设置录制视频的参数
            RecorderConfig config = new RecorderConfig().setVideoSize(480, 480)
                    .setVideoFrameRate(24).setVideoBitrate(BIT).setEnableFront(isFrontCamera)
                    .setEnableBeautify(canBeautiy).setBeauitifyLevel(5)
                    .setEnableFrontMirror(enableFrontMirror)
                    .setEnableAutoFocus(true).setEnableAutoFocusRecording(false);
            RecorderCore.setEncoderConfig(config);
            RecorderCore.enableFaceU(enableFace);

            //设置录制父容器的大小
            mCameraParams.setMargins(0, mSquareTitlebarHeight, 0, 0);
            cameraPreview.setLayoutParams(mCameraParams);
            cameraPreview.setAspectRatio(1.0f);
        }
        if (!bOnPrepred) {
            bOnPrepred = true;
            //清理，防止之前已经初始过
            RecorderCore.recycleCameraView();
            //准备录制界面
            RecorderCore.onPrepare(cameraParent, iListener);
            //设置放大缩小Handler
            RecorderCore.setCameraZoomHandler(m_hlrCameraZoom);
            //是否静音
            RecorderCore.setMute(cameraConfig.audioMute);
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
        isScreen = true;
        initCameraLayout();


    }

    private void onRegisterOsd() {
        if (RecorderCore.isRegistedOsd()) {
            RecorderCore.registerOSD(null);
        }
        if (enableWatermark) {// 必须在主线程创建
            osd = SdkEntry.createOSDBuilder(RecorderActivity.this, (!isScreen));
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
        isScreen = false;
        initCameraLayout();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_PERMISSIONS: {
                for (int i = 0; i < permissions.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        if (permissions[i] == Manifest.permission.CAMERA) {
                            onAutoToast(null, getString(R.string.permission_camera_error));
                        } else {
                            onAutoToast(null, getString(R.string.permission_audio_error));
                        }
                        finish();
                        return;
                    }
                    if (mSquareCameraLayout.getVisibility() == View.VISIBLE) {
                        onInitializeSquareRecorder();
                    } else {
                        onInitializeScreenRecorder();
                    }
                }
            }
            break;
            default:
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        android.util.Log.e(TAG, "onStart:" + bCameraPrepared);
        if (!ImageManager.hasStorage()) {
            Dialog dlg = SysAlertDialog.showAlertDialog(this,
                    R.string.app_name, R.string.record_no_external_storage,
                    android.R.string.cancel,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    }, -1, null);
            dlg.setCancelable(false);
            dlg.setCanceledOnTouchOutside(false);
        }
    }

    @Override
    protected void onResume() {
//        android.util.Log.e(TAG, "onResume:" + bCameraPrepared);
        try {
            mLayoutBlackScreen.setVisibility(View.INVISIBLE);// 取消黑屏

            // 显示切换摄像头按钮，即是否为多个摄像头，
            mBtnSwitchCamera.setVisibility(View.VISIBLE);
            mBtnSwitchCamera.postDelayed(new Runnable() {

                @Override
                public void run() {
                    checkFlashMode();
                }
            }, 300);

            // Log.d(LOG_TAG, "onResume");
            if (!enableLockScreen) {
                mOrientationListener.enable();
            }
            mLvCameraFilter.post(new Runnable() {

                @Override
                public void run() {
                    mLvCameraFilter.selectListItem(mLvCameraFilter
                            .getCurrentItemId() != -1 ? mLvCameraFilter
                            .getCurrentItemId() : 0);
                }
            });

            if (AppConfiguration.isTrainingCaptureVideo()) {
                AppConfiguration.setTrainingCaptureVideo(true);
            }
        } catch (Exception ex) {
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        // Log.e("onPause", this.toString());
        mOrientationListener.disable();
        synchronized (this) {
            if (mIsRecording) {
                stopLiveOrRecordStream(false);
            }
        }
        super.onPause();
        if (null != faceUnityHandler) {
            faceUnityHandler.onPasue();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (null != mHandler) {
            mHandler.removeCallbacks(mRunnableEffect);
            if (null != m_runnableWaiting) {
                mHandler.removeCallbacks(m_runnableWaiting);
            }
            if (null != runnable) {
                handler.removeCallbacks(runnable);
            }
        }
        // Log.e("onStop", this.toString());

    }

    private Runnable mRunnableEffect = new Runnable() {
        @Override
        public void run() {
            onCheckEffect();
        }
    };

    @Override
    protected void onDestroy() {
//        Log.e("ondestory", this.toString());

        if (null != mAudioPlayer) {
            mAudioPlayer.stop();
            mAudioPlayer.release();
        }

        if (null != mGlTouchView) {
            mGlTouchView.setViewHandler(null);
            mGlTouchView.setZoomHandler(null);
            mGlTouchView = null;

        }
        m_hlrCameraZoom = null;
        glListener = null;

        mOrientationListener = null;
        if (null != mLvCameraFilter) {
            mLvCameraFilter.removeAllListItem();
            mLvCameraFilter = null;
        }
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
        ThreadPoolUtils.execute(new Runnable() {
            public void run() {
                gotoEdit = false;
                stopLiveOrRecordStream(false);
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
        // Log.e("setOrientationIndicator liveActivty", "nOrientation-- "
        // + nOrientation);
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
                    // mBtnAddMusic,
                    // mBtnRecord,
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
            //
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

            ((TextView) findViewById(R.id.tvItembtnSelectMVCaption2))
                    .setText(getString(R.string.m_short_mv, mMVMaxTime / 1000));
            ((TextView) findViewById(R.id.tvItembtnSelectMVCaption1))
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

            ((TextView) findViewById(R.id.tvItembtnSelectMVCaption2))
                    .setText(getString(R.string.m_short_mv, mMVMaxTime / 1000));
            ((TextView) findViewById(R.id.tvItembtnSelectMVCaption1))
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
            // m_btnDeleteVideo
            if (mLayoutSelectRecOrPhoto1.getVisibility() == View.VISIBLE) {

            } else {

            }
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

            ((TextView) findViewById(R.id.tvItembtnSelectMVCaption2))
                    .setText(getString(R.string.m_short_mv_no_line,
                            mMVMaxTime / 1000));
            ((TextView) findViewById(R.id.tvItembtnSelectMVCaption1))
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
        if (null != mDlgCameraFailed) {
            mDlgCameraFailed.dismiss();
            mDlgCameraFailed = null;
        }

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

    /**
     * 调用finish
     */
    @SuppressWarnings("deprecation")
    private void finishCamGate() {
        int height = this.getWindowManager().getDefaultDisplay().getHeight();
        LayoutParams lParams = mIvOpenCamAnimBottom.getLayoutParams();
        lParams.height = height / 2 + 100;
        mIvOpenCamAnimBottom.setLayoutParams(lParams);

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
                RecorderActivity.super.finish();
                RecorderActivity.this.overridePendingTransition(0, 0);
            }
        }, 300);
    }

    /**
     * 开始直播或录制
     */
    synchronized void startLiveOrRecordStream() {
        // 是否为竖屏
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                onRegisterOsd();
                if (mSquareCameraLayout.getVisibility() == View.VISIBLE) {
                    mLocalSaveFileNameStr = PathUtils
                            .getMp4FileNameForSdcard();

                    RecorderCore.setOrientation(tempVideoOrientaion);
                    try {
                        RecorderCore.startRecord(mLocalSaveFileNameStr);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    mLocalSaveFileNameStr = PathUtils
                            .getMp4FileNameForSdcard();
                    RecorderCore.setOrientation(tempVideoOrientaion);
                    try {
                        RecorderCore.startRecord(mLocalSaveFileNameStr);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

    }

    synchronized int pauseLiveRecordStream() {
        mIsRecording = false;
        return RecorderCore.onPauseRecord();
    }

    synchronized int continueRecordStream() {
        return RecorderCore.onContinueRecord();
    }

    private void gotoEdit() {
        if (curPosition == POSITION_REC) {
            //最大最小时长很接近
            if (!bSelectPhoto && isInMin()) {
                onAutoToast("",
                        getString(R.string.camera_min_limit_text));
                return;
            }
        } else if (curPosition == POSITION_MV) {
            if (isInMin()) {
                onAutoToast("", getString(R.string.camera_min_limit_text));
                return;
            }
        }
        // Log.e("gotoEdit", osdHeader + "..." + osdEnd + "..." +
        // enableWatermark
        // + "...." + isEncryption);
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
                    gotoEdit(VirtualVideo.createScene().addMedia(mLocalSaveFileNameStr));
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
                onAutoToast("", getString(R.string.video_save_fail));
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
//        Log.e("savemdea", gotoEdit + "___" + mUseMultiShoot + "....." + mRecordVideoList.size() + "....." + mIsSaveToAlbum + "...." + editResult);
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
                        onAutoToast("", getString(R.string.video_save_fail));
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

        RecorderActivity.this.finishCamGate();
        mLocalSaveFileNameStr = mRecordVideoList.get(0)
                .getMediaPath();
        if (!TextUtils.isEmpty(mLocalSaveFileNameStr)) {
            onSaveSuccessed();
        }

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
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            RecorderActivity.this.finishCamGate();
                            // 合并成功删除临时文件
                            for (MediaObject mo : mRecordVideoList) {
                                Utils.cleanTempFile(mo.getMediaPath());
                            }
                            if (!TextUtils.isEmpty(mLocalSaveFileNameStr)) {
                                onSaveSuccessed();
                            }
                        }
                    });
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
            goToEditResult();
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
            finish();
        } else {
            SdkEntryHandler.getInstance().onExportRecorder(RecorderActivity.this, mLocalSaveFileNameStr);
        }

    }

    /**
     * 返回编辑
     */
    private void goToEditResult() {
        Intent intent = new Intent();
        if (bSelectPhoto) {
            intent.putExtra(SdkEntry.INTENT_KEY_PICTURE_PATH, mLocalSavePicNameStr);
        } else {
            intent.putExtra(SdkEntry.INTENT_KEY_VIDEO_PATH, mLocalSaveFileNameStr);
        }
        backBeauty(intent);
        backUseMvEdit(intent);
        setResult(RESULT_OK, intent);
        finish();
    }

    /***
     * 合并视频failed后，准备即将返回的数据界面
     */
    private void onSingleVideoSavedEndGoResultSizeBiggerFailed() {
        if (editResult) {
            goToEditResult();
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
            goToEditResult();
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
    @SuppressWarnings("deprecation")
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

    private Runnable runnablePause = new Runnable() {

        @Override
        public void run() {
            if (mSquareCameraLayout.getVisibility() == View.VISIBLE) {
                mBtnRecord1.setEnabled(true);
                pauseLiveRecordStream();
                mBtnRecord1
                        .setBackgroundResource(R.drawable.btn_shutter_stop_record);
            } else {
                mBtnRecord.setEnabled(true);
                pauseLiveRecordStream();
                mBtnRecord
                        .setBackgroundResource(R.drawable.btn_shutter_stop_record);
            }
        }
    };
    private Runnable runnableContinue = new Runnable() {

        @Override
        public void run() {
            if (mSquareCameraLayout.getVisibility() == View.VISIBLE) {
                mBtnRecord1.setEnabled(true);
                continueRecordStream();
                mBtnRecord1.setBackgroundResource(R.drawable.btn_record_n);
            } else {
                mBtnRecord.setEnabled(true);
                continueRecordStream();
                mBtnRecord.setBackgroundResource(R.drawable.btn_record_n);
            }
        }
    };


    private class StopImp implements Runnable {

        private boolean save = false;

        public StopImp(boolean msave) {
            save = msave;
        }

        @Override
        public void run() {
            (iListener).setSave(save);
            RecorderCore.stopRecord();
        }
    }

    ;

    /**
     * 执行录制完成后加密视频
     */
    private void onCheckRDEncypt(String path) {
        if (isEncryption && !TextUtils.isEmpty(path)) {
            File f = new File(path);
            if (f.exists() && f.length() > 0) {
                if (RecorderCore.apiIsRDEncyptVideo(path) <= 0) {
                    RecorderCore.apiRDVideoEncypt(path);
                    // Log.e("apiIsRDEncyptVideo", re + "");
                }
            }
        }
    }

    private StopImp istopImp;

    /**
     * 停止直播或录制
     */
    synchronized void stopLiveOrRecordStream(boolean save) {

        if (null != istopImp) {
            mHandler.removeCallbacks(istopImp);
        }
        istopImp = new StopImp(save);

        mHandler.post(istopImp);


    }

    private synchronized void setLiveStreamStatus(final Boolean bRecord) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                mIsRecording = bRecord;
                if (startTrailer) {
                    return;
                }

                if (mIsRecording) {
                    if (SdkEntry.getSdkService().getCameraConfig().enablePlayMusic
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
                    if (SdkEntry.getSdkService().getCameraConfig().enablePlayMusic
                            && mAudioPlayer.isPlaying()) {
                        mAudioPlayer.pause();
                        isPause = true;
                    }
                }
                if (mSquareCameraLayout.getVisibility() == View.VISIBLE) {// 方形录制界面
                    if (mIsRecording) {
                        buttonState = BUTTON_STATE_LIVING;
                        m_btnBottomRightForSquare
                                .setBackgroundResource(R.drawable.camera_sure_button);
                        mBtnBottomLeftForSquare
                                .setBackgroundResource(R.drawable.camera_face_button);
                    } else {
                        buttonState = BUTTON_STATE_PAUSE;
                        m_btnBottomRightForSquare
                                .setBackgroundResource(R.drawable.camera_sure_button);
                        mBtnBottomLeftForSquare
                                .setBackgroundResource(R.drawable.camera_delete_button);
                    }
                    m_btnBottomRightForSquare.setVisibility(View.VISIBLE);
                    // m_btnBottomRightForSquare.setEnabled(!mIsRecording);

                    if (mIsRecording) {
                        mBtnRecord1
                                .setBackgroundResource(R.drawable.btn_record_n);
                    } else {
                        mBtnRecord1
                                .setBackgroundResource(R.drawable.btn_shutter_stop_record);
                    }

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
                    // m_btnBottomRight.setEnabled(!mIsRecording);

                    if (mIsRecording) {
                        mBtnRecord
                                .setBackgroundResource(R.drawable.btn_record_n);
                        // 黑屏情况
                        // m_btnBlackScreen
                        // .setCompoundDrawablesWithIntrinsicBounds(
                        // 0,
                        // R.drawable.main_camera_blackscreen_btn_back,
                        // 0, 0);
                        // m_btnBlackScreen.setBackgroundResource(0);
                        m_btnBlackScreen.setVisibility(View.VISIBLE);

                    } else {
                        mBtnRecord
                                .setBackgroundResource(R.drawable.btn_shutter_stop_record);
                    }
                    mBtnRecord.setEnabled(true);

                    if (mIsRecording) {
                        List<String> lstColorEffects = RecorderCore
                                .getSupportedColorEffects();
                        mBtnBottomLeft.setEnabled(mUseMediaRecorder
                                && lstColorEffects != null
                                && lstColorEffects.size() >= 2);
                        if (!mBtnBottomLeft.isEnabled()) {
                            onFilterListCtrlClick();
                        }

                    } else {
                        mBtnBottomLeft.setEnabled(true);
                    }

                }
                checkFlashMode();
                InitBtnBlack();
                InitBtnShootingRatio();

            }
        });
    }

    private void checkFlashMode() {
        //正方形录制
        if (!RecorderCore.isFaceFront()) {
            isFrontCamera = false;
            mBtnFlashModeCtrl1.setSelected(RecorderCore.getFlashMode());
            mBtnFlashModeCtrl1.setEnabled(true);
            mBtnFlashModeCtrl1
                    .setImageResource(R.drawable.camera_flash_status);

        } else {
            isFrontCamera = true;
            mBtnFlashModeCtrl1.setEnabled(false);
            mBtnFlashModeCtrl1
                    .setImageResource(R.drawable.camare_flare_un);
        }
        //长方形录制
        if (!RecorderCore.isFaceFront()) {
            isFrontCamera = false;
            mBtnFlashModeCtrl.setSelected(RecorderCore.getFlashMode());
            mBtnFlashModeCtrl.setEnabled(true);
            mBtnFlashModeCtrl
                    .setImageResource(R.drawable.camera_flash_status);

        } else {
            isFrontCamera = true;
            mBtnFlashModeCtrl.setEnabled(false);
            mBtnFlashModeCtrl.setImageResource(R.drawable.camare_flare_un);

        }

    }

    /**
     * 初始摄像头滤镜各项目
     */
    private void initCameraFilterListItems() {
        if (null == mCameraEffectHandler) {
            mCameraEffectHandler = new CameraEffectHandler(this);
        }

        mLvCameraFilter
                .setListItemSelectListener(new HorizontalListViewCamera.OnListViewItemSelectListener() {

                    @Override
                    public void onSelected(View view, int nItemId, boolean user) {

                        if (com.rd.veuisdk.utils.Utils
                                .getSupportExpandEffects()) {
                            Log.d(LOG_TAG,
                                    getString(R.string.livecamera_record_switch_filter_failed));
                        } else {
                            mCurrentEffectIndex = nItemId;
                            onCheckEffect();

                        }
                    }

                    @Override
                    public boolean onBeforeSelect(View view, int nItemId) {
                        return false;
                    }
                });

        List<String> effects = RecorderCore.getSupportedColorEffects();
        mCameraEffectHandler.initAllEffects(mLvCameraFilter, effects);
        mLvCameraFilter.selectListItem(0);
    }

    /**
     * 响应滤镜
     */
    private void onCheckEffect() {
        if (null != mCameraEffectHandler) {
            String strEffectName = mCameraEffectHandler
                    .getInternalColorEffectByItemId(mCurrentEffectIndex);
            RecorderCore.setColorEffect(strEffectName);
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
        // mLvCameraFilter
        // m_llFilterList
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
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (!bShowFitlerListLayout) {
                    lp.height = 0;
                    mRlFilterList.setLayoutParams(lp);
                }
            }
        });

        taCameraFilter.setDuration(800);
        mRlFilterList.clearAnimation();
        taCameraFilter.setFillEnabled(true);
        taCameraFilter.setFillAfter(true);
        mRlFilterList.startAnimation(taCameraFilter);
        taCameraFilter = null;
        onOrientationFilter();
    }

    /**
     * 响应控制录像按钮
     */
    protected void onRecordButtonClick() {
        if (mSquareCameraLayout.getVisibility() == View.VISIBLE) {
            mBtnRecord1.setEnabled(false);
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
        } else {
            mBtnRecord.setEnabled(false);
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

    }

    protected void onSwitchCameraButtonClick() {
        // Log.e("switch", "--before");

        if (null != faceUnityHandler) {
            faceUnityHandler.onSwitchCamare(true);
        }

        try {
            RecorderCore.switchCamera();
            checkFlashMode();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // Log.e("switch", "--end");
        if (null != faceUnityHandler) {
            faceUnityHandler.onSwitchCamare(false);
        }
    }

    protected void onCameraMoreClick() {
        if (mRecordingCameraMoreBar.getVisibility() == View.INVISIBLE) {
            mRecordingCameraMoreBar.setVisibility(View.VISIBLE);
        } else {
            mRecordingCameraMoreBar.setVisibility(View.INVISIBLE);
        }
    }

    protected void onFlashModeClick() {
        boolean re = RecorderCore.getFlashMode();

        if (mSquareCameraLayout.getVisibility() == View.VISIBLE) {
            if (RecorderCore.setFlashMode(!re)) {
                mBtnFlashModeCtrl1.setSelected(!re);
            }
        } else {
            if (RecorderCore.setFlashMode(!re)) {
                mBtnFlashModeCtrl.setSelected(!re);
                // m_btnFlashModeCtrl90.setSelected(!re);
                // m_btnFlashModeCtrl270.setSelected(!re);
            }
        }

    }

    private final int BIT = 2500 * 1000;
    private boolean isFrontCamera = true;
    private boolean canBeautiy = false;

    protected void goPreviewByCameraSizeMode() {
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

        //先确保相机录音权限,再初始化摄像头
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int hasReadPermission = checkSelfPermission(Manifest.permission.RECORD_AUDIO);

            List<String> permissions = new ArrayList<String>();
            if (hasReadPermission != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.RECORD_AUDIO);
            }

            hasReadPermission = checkSelfPermission(Manifest.permission.CAMERA);

            if (hasReadPermission != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.CAMERA);
            }
            if (!permissions.isEmpty()) {
                requestPermissions(
                        permissions.toArray(new String[permissions.size()]),
                        REQUEST_CODE_PERMISSIONS);
            } else {
                if ((!isScreen)) {
                    onInitializeSquareRecorder();
                } else {
                    onInitializeScreenRecorder();
                }
            }
        } else {
            if ((!isScreen)) {
                onInitializeSquareRecorder();
            } else {
                onInitializeScreenRecorder();
            }
        }
    }

    protected void onChangeCameraSizeModeClick() {
        if (mSquareCameraLayout.getVisibility() == View.VISIBLE) {
            mSquareCameraLayout.setVisibility(View.INVISIBLE);
            mScreenCameraLayout.setVisibility(View.VISIBLE);
            isScreen = true;
            onCheckLock(false);
        } else if (mScreenCameraLayout.getVisibility() == View.VISIBLE) {
            mScreenCameraLayout.setVisibility(View.INVISIBLE);
            mSquareCameraLayout.setVisibility(View.VISIBLE);
            isScreen = false;
            mOrientationCompensation = 0;
            onCheckLock(true);

        }
        goPreviewByCameraSizeMode();
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

            // Log.e("onOrientationChanged", mOrientation + "....");
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
                    // Log.e("tempOrientation", tempOrientation + "...");

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
                        // tvTimer.setText("00:00:00");
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
        // InitQualityOrBlack();
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
    protected void onCloseOrPauseRecordClick() {

        if (mIsRecording) {
            if (curPosition == POSITION_REC) {
                if (!bSelectPhoto && isInMin()) {
                    onAutoToast(
                            "", getString(R.string.camera_min_limit_text));
                    return;
                }
            } else if (curPosition == POSITION_MV) {
                if (isInMin()) {
                    onAutoToast(
                            "", getString(R.string.camera_min_limit_text));
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
        if (mVideoMinTime == 0) {
            return false;
        }
        //最小、最大两个值相等
        if (Math.abs(mVideoMinTime - mVideoMaxTime) < 800) {
            return curTotal < (mVideoMinTime - 500);
        } else {
            return curTotal < mVideoMinTime;
        }


    }

    private int curTotal;

    private boolean finishWithoutGate = false;

    @Override
    public void finish() {
//        Log.e("finsih", this.toString() + ".........over" +
//                finishWithoutGate);
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
            // Log.e("finsih", this.toString() + "......super...over");
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

    @SuppressWarnings("deprecation")
    private void deleteVideo() {
        int maxTime = 0;
        if (curPosition == POSITION_MV) {
            maxTime = mMVMaxTime;
        } else if (curPosition == POSITION_REC) {
            maxTime = mVideoMaxTime;
        }
        if (!isScreen) {
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
                    MediaObject mo = mRecordVideoList.remove(mRecordVideoList
                            .size() - 1);
                    totalTime -= Utils.s2ms(mo.getDuration());
                    if (maxTime != 0) {
                        tvTimer.setText(Utils.stringForTime(totalTime, false,
                                true));
                    } else {
                        tvTimer.setText(Utils.stringForTime(totalTime, true,
                                false));
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
                    shootMP = MediaPlayer
                            .create(this,
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
    @SuppressWarnings("deprecation")
    private void initLayouts() {
        String str = getString(R.string.m_short_mv_no_line, mMVMaxTime / 1000);
        ((TextView) findViewById(R.id.tvItembtnSelectMVCaption2)).setText(str);
        ((TextView) findViewById(R.id.tvItembtnSelectMVCaption1)).setText(str);

        mIvOpenCamAnimTop = (ImageView) this
                .findViewById(R.id.ivOpenCamAnimTop);
        mIvOpenCamAnimBottom = (ImageView) this
                .findViewById(R.id.ivOpenCamAnimBottom);

        mRecordRRL = (RotateRelativeLayout) findViewById(R.id.rrlbtnRecord);
        mBtnRecord = (Button) findViewById(R.id.btnRecord);
        // mBtnRecord.setRepeatClickIntervalTime(2500);
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
                            RecorderCore.screenshot(true, PathUtils
                                    .getTempFileNameForSdcard(
                                            PathUtils.getRdImagePath(), "PIC",
                                            "jpg"), 360, 640, 50);
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

        mRllivingBar0 = (RelativeLayout) findViewById(R.id.lrliving_bar0);

        mRecordingBar90 = (LinearLayout) findViewById(R.id.llliving_bar90);
        mRecordingBar270 = (LinearLayout) findViewById(R.id.llliving_bar270);

        mBtnFlashModeCtrl = (RotateImageView) findViewById(R.id.btnFlashModeCtrl);
        mBtnFlashModeCtrl.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                onFlashModeClick();
            }
        });

        mRlFilterList = (RotateRelativeLayout) findViewById(R.id.rlFilterList);
        mBtnBottomRightForLandscape = (RotateRelativeLayout) findViewById(R.id.rrlbtnBottomRightForLandscape);
        m_btnBottomRightForLandscape = (ExtButton) findViewById(R.id.btnBottomRightForLandscape);
        if (hideAlbum) {
            m_btnBottomRightForLandscape.setVisibility(View.GONE);
        }
        m_btnBottomRightForLandscape
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        android.util.Log.e(TAG, "onClick:  buttonState" + buttonState);
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
        mllAddMusic1 = (LinearLayout) this.findViewById(R.id.llAddMusic1);

        mTvMusicNameSquare = (TextView) this
                .findViewById(R.id.edit_text_music_name1);
        mBtnDelMusic1 = (ImageView) this
                .findViewById(R.id.btn_edit_text_music_del1);
        mBtnDelMusic1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteSelectMusic();
            }
        });
        mBtnAddMusic1 = (RotateImageView) this.findViewById(R.id.btnAddMusic1);
        mBtnAddMusic1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onMusicYUN();
            }
        });

        //长方形添加音乐部分
        mllAddMusic = (LinearLayout) this.findViewById(R.id.llAddMusic);

        mTvMusicNameScreen = (TextView) this.findViewById(R.id.edit_text_music_name);
        mBtnDelMusic = (ImageView) this
                .findViewById(R.id.btn_edit_text_music_del);
        mBtnDelMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteSelectMusic();

            }
        });
        mBtnAddMusic = (RotateImageView) this.findViewById(R.id.btnAddMusic);
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


        mTimerTv = (RotateRelativeLayout) findViewById(R.id.rrltvTimer);
        mBtnBottomRight = (RotateRelativeLayout) findViewById(R.id.rrlbtnBottomRight);

        mLvCameraFilter = (HorizontalListViewCamera) findViewById(R.id.lvFilterList);
        mRecordingCameraMoreBar = (RelativeLayout) findViewById(R.id.living_cameramore_bar);

        mBtnSelectPhoto1 = (ExtButton) findViewById(R.id.btnSelectPhoto1);

        mBtnSwitchCamera = (RotateImageView) this
                .findViewById(R.id.btnSwitchCamera5);
        mBtnSwitchCamera.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                onSwitchCameraButtonClick();
            }
        });

        m_btnBlackScreen = (RotateImageView) this
                .findViewById(R.id.btnMenuBlackScreen);

        m_btnBlackScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onQualityOrBlackScreen();
            }
        });

        mBtnBlackScreen1 = (RotateImageView) findViewById(R.id.btnMenuBlackScreen1);
        mBtnBlackScreen1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onQualityOrBlackScreen();
            }
        });
        mBtnBottomLeftLayout = (RotateRelativeLayout) this
                .findViewById(R.id.rrlbtnBottomLeft);
        mBtnBottomLeft = (ExtButton) this.findViewById(R.id.btnBottomLeft);
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
        mBtncloseFilterList = (RotateImageView) this
                .findViewById(R.id.btncloseFilterList);
        mBtncloseFilterList.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                onFilterListCtrlClick();
            }
        });

        mLayoutBlackScreen = (FrameLayout) this.findViewById(R.id.flBlackScreen);
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
        tvTimer = (TextView) findViewById(R.id.tvTimer);
        if (!Utils.isUseInternalRecorder()) {
            mUseMediaRecorder = true;
            Utils.setCanWriteMP4Metadata(false);
        } else {
            Utils.setCanWriteMP4Metadata(true);
        }
        mBtnBeauty = (RotateImageView) findViewById(R.id.btnbeauty);
        mBtnBeauty.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                onBeautifyClick();
            }
        });
        mGlTouchView = (GlTouchView) findViewById(R.id.glTouch);
        mGlTouchView.setViewHandler(glListener);
        // 处理相机变焦
        m_hlrCameraZoom = new CameraZoomHandler(this, null);
        mGlTouchView.setZoomHandler(m_hlrCameraZoom);

        m_btnWaiting = (RotateImageView) findViewById(R.id.btnWating);
        m_btnWaiting.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                startWaitingRecord(5);
            }
        });
        mBtnCancelRecord = (RotateImageView) findViewById(R.id.btnCancelRecord);
        mBtnCancelRecord.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        mLayoutSelectRecOrPhoto1 = (RelativeLayout) findViewById(rlSelectRecOrPhoto1);
        mLayoutSelectRecOrPhoto2 = (RelativeLayout) findViewById(rlSelectRecOrPhoto2);
        mLayoutSelectRecOrPhoto1.setVisibility(View.VISIBLE);
        mLayoutSelectRecOrPhoto2.setVisibility(View.VISIBLE);

        mSelectRec1 = (RotateRelativeLayout) findViewById(R.id.lvSelectRec1);
        mSelectRec2 = (RotateRelativeLayout) findViewById(R.id.lvSelectRec2);
        mSelectMV1 = (RotateRelativeLayout) findViewById(R.id.lvSelectMV1);
        mSelectMV2 = (RotateRelativeLayout) findViewById(R.id.lvSelectMV2);
        mSelectPhoto1 = (RotateRelativeLayout) findViewById(R.id.lvSelectPhoto1);
        mSelectPhoto2 = (RotateRelativeLayout) findViewById(R.id.lvSelectPhoto2);

        mSelectRec1.setOnClickListener(onSwitchButtonClickListener);
        mSelectRec2.setOnClickListener(onSwitchButtonClickListener);
        mSelectMV1.setOnClickListener(onSwitchButtonClickListener);
        mSelectMV2.setOnClickListener(onSwitchButtonClickListener);
        mSelectPhoto1.setOnClickListener(onSwitchButtonClickListener);
        mSelectPhoto2.setOnClickListener(onSwitchButtonClickListener);

        // /////////////////////////////////////////////新加入的方形界面处理
        // 两个界面（全屏和方形）
        mSquareCameraLayout = (RelativeLayout) findViewById(R.id.rl_square_camera);
        mScreenCameraLayout = (RelativeLayout) findViewById(R.id.rl_fullscreen_camera);

        // /////////////////
        mVideoNewRelative = (RelativeLayout) findViewById(R.id.video_new_relative);
        mVideoNewRelative1 = (RelativeLayout) findViewById(R.id.video_new_relative1);
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
        mBtnShootingRatio1 = (RotateImageView) findViewById(R.id.btnShootingRatio1);
        mBtnShootingRatio1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onChangeCameraSizeModeClick();
            }
        });
        // 全屏界面的切换按钮
        mBtnShootingRatio = (RotateImageView) findViewById(R.id.btnShootingRatio);
        mBtnShootingRatio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onChangeCameraSizeModeClick();
            }
        });

        // ////////////////////
        m_DisplayWidth = CoreUtils.getMetrics().widthPixels;

        handler.postDelayed(runnable, 0);
        mImgFlashMVScreen = (ImageView) findViewById(R.id.record_progress_flash_screen);
        mImgFlashMVSquare = (ImageView) findViewById(R.id.record_progress_flash_square);
        mLinearSeekbar = (LinearLayout) findViewById(R.id.video_new_seekbar);
        mLinearSeekbar1 = (LinearLayout) findViewById(R.id.video_new_seekbar1);

        m_btnBottomRight = (ExtButton) findViewById(R.id.btnBottomRight);
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

        m_btnBottomRightForSquare = (ExtButton) findViewById(R.id.btnBottomRightForSquare);
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

        mBtnRecord1 = (Button) findViewById(R.id.btnRecord1);
        // mBtnRecord1.setRepeatClickIntervalTime(2500);
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
                            RecorderCore.screenshot(true, PathUtils
                                    .getTempFileNameForSdcard(
                                            PathUtils.getRdImagePath(), "PIC",
                                            "jpg"), 480, 480, 50);
                        } else {
                            if (mIsRecording)
                                stopLiveOrRecordStream(false);
                        }

                        break;
                }
                return false;
            }
        });

        mBtnFlashModeCtrl1 = (RotateImageView) findViewById(R.id.btnFlashModeCtrl1);
        mBtnFlashModeCtrl1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFlashModeClick();
            }
        });

        mBtnSwitchCamera1 = (RotateImageView) this
                .findViewById(R.id.btnSwitchCamera1);
        mBtnSwitchCamera1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                onSwitchCameraButtonClick();
            }
        });


        mBtnBottomLeftForSquare = (Button) this
                .findViewById(R.id.btnBottomLeftForSquare);
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

        tvTimer1 = (TextView) findViewById(R.id.tvTimer1);
        mRlframeSquarePreview = (PreviewFrameLayout) findViewById(R.id.frameSquarePreview);
        mRlframeSquarePreview.setAspectRatio(1.0);
        mBtnBeauty1 = (RotateImageView) findViewById(R.id.btnbeauty1);
        mBtnBeauty1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onBeautifyClick();
            }
        });

        m_btnWaiting1 = (RotateImageView) findViewById(R.id.btnWating1);
        m_btnWaiting1.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                startWaitingRecord(5);
            }
        });
        mBtnCancelRecord1 = (RotateImageView) findViewById(R.id.btnCancelRecord1);
        mBtnCancelRecord1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // 90度横屏和270度横屏是相关按钮初始化
        mBtnCancelRecord90 = (RotateImageView) findViewById(R.id.btnCancelRecord90);
        mBtnCancelRecord90.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mBtnBlackScreen90 = (RotateImageView) findViewById(R.id.btnMenuBlackScreen90);
        mBtnBlackScreen90.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onQualityOrBlackScreen();
            }
        });
        mBtnBlackScreen270 = (RotateImageView) findViewById(R.id.btnMenuBlackScreen270);
        mBtnBlackScreen270.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onQualityOrBlackScreen();
            }
        });

        mBtnShootingRatio90 = (RotateImageView) findViewById(R.id.btnShootingRatio90);
        mBtnShootingRatio90.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onChangeCameraSizeModeClick();
            }
        });
        // m_btnbeauty90 = (RotateImageView) findViewById(R.id.btnbeauty90);
        // m_btnbeauty90.setOnClickListener(new OnClickListener() {
        //
        // @Override
        // public void onClick(View v) {
        // onBeautifyClick();
        // }
        // });
        mBtnWating90 = (RotateImageView) findViewById(R.id.btnWating90);
        mBtnWating90.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                startWaitingRecord(5);
            }
        });
        // m_btnSwitchCamera90 = (RotateImageView)
        // findViewById(R.id.btnSwitchCamera90);
        // m_btnSwitchCamera90.setOnClickListener(new OnClickListener() {
        //
        // @Override
        // public void onClick(View v) {
        // onSwitchCameraButtonClick();
        // }
        // });
        // m_btnFlashModeCtrl90 = (RotateImageView)
        // findViewById(R.id.btnFlashModeCtrl90);
        // m_btnFlashModeCtrl90.setOnClickListener(new OnClickListener() {
        //
        // @Override
        // public void onClick(View v) {
        // onFlashModeClick();
        // }
        // });
        // /////270度
        mBtnCancelRecord270 = (RotateImageView) findViewById(R.id.btnCancelRecord270);
        mBtnCancelRecord270.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        mBtnShootingRatio270 = (RotateImageView) findViewById(R.id.btnShootingRatio270);
        mBtnShootingRatio270.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onChangeCameraSizeModeClick();
            }
        });
        // m_btnbeauty270 = (RotateImageView) findViewById(R.id.btnbeauty270);
        // m_btnbeauty270.setOnClickListener(new OnClickListener() {
        //
        // @Override
        // public void onClick(View v) {
        // onBeautifyClick();
        // }
        // });
        mBtnWating270 = (RotateImageView) findViewById(R.id.btnWating270);
        mBtnWating270.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                startWaitingRecord(5);
            }
        });
        // m_btnSwitchCamera270 = (RotateImageView)
        // findViewById(R.id.btnSwitchCamera270);
        // m_btnSwitchCamera270.setOnClickListener(new OnClickListener() {
        //
        // @Override
        // public void onClick(View v) {
        // onSwitchCameraButtonClick();
        // }
        // });
        // m_btnFlashModeCtrl270 = (RotateImageView)
        // findViewById(R.id.btnFlashModeCtrl270);
        // m_btnFlashModeCtrl270.setOnClickListener(new OnClickListener() {
        //
        // @Override
        // public void onClick(View v) {
        // onFlashModeClick();
        // }
        // });
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

    private void onMusicYUN() {
        HistoryMusicCloud.getInstance().initilize(this);
        Intent music = new Intent(this, MoreMusicActivity.class);
        music.putExtra(MoreMusicActivity.PARAM_TYPE,
                MoreMusicActivity.TYPE_MUSIC_YUN);
        music.putExtra(MoreMusicActivity.PARAM_CLOUDMUSIC, SdkEntry
                .getSdkService().getUIConfig().cloudMusicUrl);
        this.startActivityForResult(music, VideoEditActivity.REQUSET_MUSICEX);
        this.overridePendingTransition(R.anim.push_bottom_in,
                R.anim.push_top_out);
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
            // mBtnRecord.setImageResource(R.drawable.btn_record_n);//btn_record_n
            // btn_shutter_stop_record
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
    private Handler mHandler = new Handler();

    private Runnable m_runnableWaiting = new Runnable() {

        @Override
        public void run() {
            if (m_bIsWaiting) {
                onToast(step);
                if (step-- < 1) {
                    finishWaitingRecord();
                    synchronized (RecorderActivity.this) {
                        if (bSelectPhoto) {
                            if (mSquareCameraLayout.getVisibility() == View.VISIBLE) {
                                RecorderCore.screenshot(true, PathUtils
                                        .getTempFileNameForSdcard(
                                                PathUtils.getRdImagePath(),
                                                "PIC", "jpg"), 480, 480, 50);
                            } else {
                                RecorderCore.screenshot(true, PathUtils
                                        .getTempFileNameForSdcard(
                                                PathUtils.getRdImagePath(),
                                                "PIC", "jpg"), 360, 640, 50);
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

    private void onToast(int ts) {
        setText(R.id.waiting_text, ts + "");
    }

    private GlTouchView.CameraCoderViewListener glListener = new CameraCoderViewListener() {

        @Override
        public void onSwitchFilterToRight() {
            // Log.e("re", "onSwitchFilterToRight-->");
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
//            Log.e("re", "onSwitchFilterToLeft-->");
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
            RecorderCore.cameraAutoFocus();
        }

        @Override
        public void onDoubleTap(MotionEvent e) {

        }

        @Override
        public void onFilterChangeStart(boolean leftORight, double nfilterProportion) {
            bLeftOright = leftORight;
            onSureBg();
        }

        @Override
        public void onFilterChanging(boolean leftORight, double filterProportion) {
            bLeftOright = leftORight;
            if (leftORight) {
                RecorderCore.setColorEffect(mCameraEffectHandler.getInternalColorEffectByItemId(mTempCurrentIndex),
                        mCameraEffectHandler.getInternalColorEffectByItemId(mEffectRightIndex), filterProportion);
            } else {
                RecorderCore.setColorEffect(mCameraEffectHandler.getInternalColorEffectByItemId(mEffectLeftIndex),
                        mCameraEffectHandler.getInternalColorEffectByItemId(mTempCurrentIndex), filterProportion);
            }


        }

        @Override
        public void onFilterChangeEnd() {
            if (bLeftOright) {
                mLvCameraFilter.selectListItem(mEffectRightIndex, true);
            } else {
                mLvCameraFilter.selectListItem(mEffectLeftIndex, true);
            }
            onSureBg();
        }

        @Override
        public void onFilterCanceling(boolean leftORight, double filterProportion) {
            bLeftOright = leftORight;
            if (leftORight) {
                RecorderCore.setColorEffect(
                        mCameraEffectHandler.getInternalColorEffectByItemId(mEffectLeftIndex)
                        , mCameraEffectHandler.getInternalColorEffectByItemId(mTempCurrentIndex),
                        filterProportion);
            } else {
                RecorderCore.setColorEffect(
                        mCameraEffectHandler.getInternalColorEffectByItemId(mTempCurrentIndex),
                        mCameraEffectHandler.getInternalColorEffectByItemId(mEffectRightIndex),
                        filterProportion);
            }

        }

        @Override
        public void onFilterChangeCanceled() {
            onSureBg();
            mLvCameraFilter.selectListItem(mTempCurrentIndex, true);

        }
    };
    private boolean bLeftOright = false;
    private int mTempCurrentIndex = 0;
    private int mEffectRightIndex, mEffectLeftIndex;

    /**
     * 找到相邻的3个滤镜的所需图标的背景Index
     */
    private void onSureBg() {
        mTempCurrentIndex = mLvCameraFilter.getCurrentItemId();
        // Log.e("onSureBg", "onSureBg: " + nTempId);
        int count = mCameraEffectHandler.getEffectCount();
        mEffectLeftIndex = 0;
        if ((mEffectLeftIndex = (mTempCurrentIndex - 1)) < 0) {
            mEffectLeftIndex = count - 1;
        } else {
        }
        mEffectRightIndex = mTempCurrentIndex;
        if (mEffectRightIndex < (count - 1)) {
            ++mEffectRightIndex;
        } else {
            mEffectRightIndex = 0;
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
        if ((!isScreen)) {
            mSelectMV2.setLayoutParams(mvlp);
            findViewById(R.id.btnSelectMV2).setVisibility(View.VISIBLE);
            findViewById(R.id.lvSelectMV2).setVisibility(currentVisisble);
            ((TextView) findViewById(R.id.tvItembtnSelectMVCaption2))
                    .setTextColor(mTxColorP);

            if (hideRec) {
                mSelectRec2.setVisibility(View.GONE);
            }
            reclp.addRule(RelativeLayout.RIGHT_OF, R.id.lvSelectMV2);
            mSelectRec2.setLayoutParams(reclp);
            findViewById(R.id.btnSelectRec2).setVisibility(View.INVISIBLE);
            ((TextView) findViewById(R.id.tvItembtnSelectRecCaption2))
                    .setTextColor(mTxColorN);
            if (hideRec) {
                photolp.addRule(RelativeLayout.RIGHT_OF, R.id.lvSelectMV2);
            } else {
                photolp.addRule(RelativeLayout.RIGHT_OF, R.id.lvSelectRec2);
            }
            if (hidePhoto) {
                mSelectPhoto2.setVisibility(View.GONE);
            }
            mSelectPhoto2.setLayoutParams(photolp);
            findViewById(R.id.btnSelectPhoto2)
                    .setVisibility(View.INVISIBLE);
            ((TextView) findViewById(R.id.tvItembtnSelectPhotoCaption2))
                    .setTextColor(mTxColorN);
        } else {
            mSelectMV1.setLayoutParams(mvlp);
            findViewById(R.id.btnSelectMV1).setVisibility(View.VISIBLE);
            findViewById(R.id.lvSelectMV1).setVisibility(currentVisisble);
            ((TextView) findViewById(R.id.tvItembtnSelectMVCaption1))
                    .setTextColor(mTxColorP);
            if (hideRec) {
                mSelectRec1.setVisibility(View.GONE);
            }
            reclp.addRule(RelativeLayout.RIGHT_OF, R.id.lvSelectMV1);
            mSelectRec1.setLayoutParams(reclp);
            findViewById(R.id.btnSelectRec1).setVisibility(View.INVISIBLE);
            ((TextView) findViewById(R.id.tvItembtnSelectRecCaption1))
                    .setTextColor(mTxColorN);

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
            findViewById(R.id.btnSelectPhoto1)
                    .setVisibility(View.INVISIBLE);
            ((TextView) findViewById(R.id.tvItembtnSelectPhotoCaption1))
                    .setTextColor(mTxColorN);
        }
    }

    /***
     * 切换到录制模式
     */
    private void onPositionRec(int currentVisisble) {
        reclp.addRule(RelativeLayout.CENTER_HORIZONTAL);
        int mTxColorN = getResources().getColor(R.color.white);
        int mTxColorP = getResources().getColor(R.color.record_type_textcolor_p);
        if ((!isScreen)) {
            mSelectRec2.setLayoutParams(reclp);
            findViewById(R.id.btnSelectRec2).setVisibility(View.VISIBLE);
            findViewById(R.id.lvSelectRec2).setVisibility(currentVisisble);
            ((TextView) findViewById(R.id.tvItembtnSelectRecCaption2))
                    .setTextColor(mTxColorP);

            if (hidePhoto) {
                mSelectPhoto2.setVisibility(View.GONE);
            }
            photolp.addRule(RelativeLayout.RIGHT_OF, R.id.lvSelectRec2);
            mSelectPhoto2.setLayoutParams(photolp);
            findViewById(R.id.btnSelectPhoto2)
                    .setVisibility(View.INVISIBLE);
            ((TextView) findViewById(R.id.tvItembtnSelectPhotoCaption2))
                    .setTextColor(mTxColorN);

            if (hideMV) {
                mSelectMV2.setVisibility(View.GONE);
            }
            mvlp.addRule(RelativeLayout.LEFT_OF, R.id.lvSelectRec2);
            mSelectMV2.setLayoutParams(mvlp);
            findViewById(R.id.btnSelectMV2).setVisibility(View.INVISIBLE);
            ((TextView) findViewById(R.id.tvItembtnSelectMVCaption2))
                    .setTextColor(mTxColorN);
        } else {
            mSelectRec1.setLayoutParams(reclp);
            findViewById(R.id.btnSelectRec1).setVisibility(View.VISIBLE);
            findViewById(R.id.lvSelectRec1).setVisibility(currentVisisble);
            ((TextView) findViewById(R.id.tvItembtnSelectRecCaption1))
                    .setTextColor(mTxColorP);

            if (hidePhoto) {
                mSelectPhoto1.setVisibility(View.GONE);
            }
            photolp.addRule(RelativeLayout.RIGHT_OF, R.id.lvSelectRec1);
            mSelectPhoto1.setLayoutParams(photolp);
            findViewById(R.id.btnSelectPhoto1)
                    .setVisibility(View.INVISIBLE);
            ((TextView) findViewById(R.id.tvItembtnSelectPhotoCaption1))
                    .setTextColor(mTxColorN);

            if (hideMV) {
                mSelectMV1.setVisibility(View.GONE);
            }
            mvlp.addRule(RelativeLayout.LEFT_OF, R.id.lvSelectRec1);
            mSelectMV1.setLayoutParams(mvlp);
            findViewById(R.id.btnSelectMV1).setVisibility(View.INVISIBLE);
            ((TextView) findViewById(R.id.tvItembtnSelectMVCaption1))
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
        if ((!isScreen)) {
            mSelectPhoto2.setLayoutParams(photolp);
            findViewById(R.id.btnSelectPhoto2).setVisibility(View.VISIBLE);
            findViewById(R.id.lvSelectPhoto2).setVisibility(currentVisisble);
            ((TextView) findViewById(R.id.tvItembtnSelectPhotoCaption2))
                    .setTextColor(mTxColorP);
            if (hideRec) {
                mSelectRec2.setVisibility(View.GONE);
            }
            reclp.addRule(RelativeLayout.LEFT_OF, R.id.lvSelectPhoto2);
            mSelectRec2.setLayoutParams(reclp);
            findViewById(R.id.btnSelectRec2).setVisibility(View.INVISIBLE);
            ((TextView) findViewById(R.id.tvItembtnSelectRecCaption2))
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
            findViewById(R.id.btnSelectMV2).setVisibility(View.INVISIBLE);
            ((TextView) findViewById(R.id.tvItembtnSelectMVCaption2))
                    .setTextColor(mTxColorN);
        } else {
            mSelectPhoto1.setLayoutParams(photolp);
            findViewById(R.id.lvSelectPhoto1).setVisibility(View.VISIBLE);
            findViewById(R.id.btnSelectPhoto1).setVisibility(currentVisisble);
            ((TextView) findViewById(R.id.tvItembtnSelectPhotoCaption1))
                    .setTextColor(mTxColorP);
            if (hideRec) {
                mSelectRec1.setVisibility(View.GONE);
            }
            reclp.addRule(RelativeLayout.LEFT_OF, R.id.lvSelectPhoto1);
            mSelectRec1.setLayoutParams(reclp);
            findViewById(R.id.btnSelectRec1).setVisibility(View.INVISIBLE);
            ((TextView) findViewById(R.id.tvItembtnSelectRecCaption1))
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
            findViewById(R.id.btnSelectMV1).setVisibility(View.INVISIBLE);
            ((TextView) findViewById(R.id.tvItembtnSelectMVCaption1))
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
        findViewById(R.id.rrlAddMusic).setVisibility(View.GONE);
        findViewById(R.id.rrlAddMusic1).setVisibility(View.GONE);
    }

    /**
     * 显示音乐菜单
     */
    private void showMusicLayout() {
        findViewById(R.id.rrlAddMusic).setVisibility(View.VISIBLE);
        findViewById(R.id.rrlAddMusic1).setVisibility(View.VISIBLE);
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
            if (SdkEntry.getSdkService().getCameraConfig().enablePlayMusic) {
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
            if (SdkEntry.getSdkService().getCameraConfig().enablePlayMusic) {
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
     * isScreen  是否是全屏模式
     * 录制回调
     */
    class IRecoder implements IRecorderCallBackShot {

        @Override
        public void onPermissionFailed(int nResult, String strResultInfo) {
            android.util.Log.e(TAG, "onPermissionFailed: " + nResult + "->" + strResultInfo);
            if (isScreen) {
                mIsRecording = false;
                setLiveStreamStatus(false);
                tvTimer.setVisibility(View.GONE);
                mLocalSaveFileNameStr = null;
            } else {
                mIsRecording = false;
                tvTimer1.setVisibility(View.GONE);
                setLiveStreamStatus(false);
                mLocalSaveFileNameStr = null;
            }
        }

        /**
         * 截取回调
         *
         * @param nResult       =ResultConstants.SUCCESS
         * @param strResultInfo 图片本地路径
         */
        @Override
        public void onScreenShot(int nResult, String strResultInfo) {
            if (isScreen) {
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
            } else {
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
        }

        @Override
        public void onRecordFailed(int nResult, String strResultInfo) {
            Log.e("IRecorderListener", "onRecordFailed->" + nResult + "..."
                    + strResultInfo);
            if (isScreen) {
                isFailed = true;
                if (nResult == -3) {
                    onAutoToast(
                            getString(R.string.dialog_tips),
                            getString(R.string.permission_audio_error_p_allow));
                } else if (nResult == -16) {
                    onAutoToast(
                            getString(R.string.dialog_tips),
                            getString(R.string.error_code, nResult));
                }
                mIsRecording = false;
                tvTimer.setVisibility(View.GONE);
                setLiveStreamStatus(false);
                mLocalSaveFileNameStr = null;
            } else {
                isFailed = true;
                if (nResult == -3) {
                    SysAlertDialog.showAutoHideDialog(RecorderActivity.this,
                            R.string.dialog_tips, R.string.permission_camera_error,
                            nResult);
                }
                mIsRecording = false;
                tvTimer1.setVisibility(View.GONE);
                setLiveStreamStatus(false);
                mLocalSaveFileNameStr = null;
            }
        }

        private boolean isFailed = false;
        private boolean save = true;

        public void setSave(boolean isSave) {
            save = isSave;
        }

        @Override
        public void onRecordEnd(int nResult, String strResultInfo) {

            if (isScreen) {
                boolean bResult;

                bResult = true;
                VideoConfig vcRecord = new VideoConfig();
                if (bResult) {
                    onCheckRDEncypt(mLocalSaveFileNameStr);
                    float ftemp = VirtualVideo.getMediaInfo(
                            mLocalSaveFileNameStr, vcRecord);
                    int lDuration = Utils.s2ms(ftemp);
                    bResult = lDuration > 0;
                    if (bResult) {
                        if (!startTrailer) {
                            ImageView img = (ImageView) mLinearSeekbar
                                    .getChildAt(mLinearSeekbar.getChildCount() - 1);
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
                        }
                    }
                }
                if (!startTrailer) {
                    if (curPosition == POSITION_MV) {
                        tvTimer.setText(Utils.stringForTime(totalTime, false, true));
                    } else if (curPosition == POSITION_REC) {
                        if (mVideoMaxTime != 0) {
                            tvTimer.setText(Utils.stringForTime(totalTime, false,
                                    true));
                        } else {
                            tvTimer.setText(Utils.stringForTime(totalTime, true,
                                    false));
                        }
                    } else {
                        tvTimer.setText(Utils.stringForTime(totalTime, true, false));
                    }
                }
                curTotal = totalTime;

                MediaObject vo = VirtualVideo.createScene().addMedia(mLocalSaveFileNameStr);
                mRecordVideoList.add(vo);

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
            } else {


                boolean bResult = nResult >= ResultConstants.SUCCESS;

                if (bResult) {
                    VideoConfig vcRecord = new VideoConfig();
                    onCheckRDEncypt(mLocalSaveFileNameStr);
                    long lDuration = Utils.s2ms(VirtualVideo.getMediaInfo(mLocalSaveFileNameStr, vcRecord));
                    bResult = lDuration > 0;
                    if (bResult) {
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
                if (!startTrailer) {
                    if (curPosition == POSITION_MV) {
                        tvTimer1.setText(Utils
                                .stringForTime(totalTime, false, true));
                    } else if (curPosition == POSITION_REC) {
                        if (mVideoMaxTime != 0) {
                            tvTimer1.setText(Utils.stringForTime(totalTime, false,
                                    true));
                        } else {
                            tvTimer1.setText(Utils.stringForTime(totalTime, true,
                                    false));
                        }
                    } else {
                        tvTimer1.setText(Utils
                                .stringForTime(totalTime, true, false));
                    }

                }

                mRecordVideoList.add(VirtualVideo.createScene().addMedia(mLocalSaveFileNameStr));
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

        @SuppressWarnings("deprecation")
        @Override
        public void onRecordBegin(int nResult, String strResultInfo) {
            if (isScreen) {
                if (isFailed) {
                    isFailed = false;
                    return;
                }

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
                if (RecorderCore.isRecording()) {
                    setLiveStreamStatus(true);
                    if (!startTrailer) {
                        ctlTimerCounter(true);
                    }
                } else {
                    FileLog.writeLog(String.format(
                            getString(R.string.livecamera_record_cannot_start)
                                    + ":%d", 0));
                    setLiveStreamStatus(false);
                    ctlTimerCounter(false);
                    onError();
                }
            } else {
                if (isFailed) {
                    isFailed = false;
                    return;
                }
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
                if (RecorderCore.isRecording()) {
                    setLiveStreamStatus(true);
                    if (!startTrailer) {
                        ctlTimerCounter(true);
                    }
                } else {
                    FileLog.writeLog(String.format(
                            getString(R.string.livecamera_record_cannot_start)
                                    + ":%d", 0));
                    setLiveStreamStatus(false);
                    ctlTimerCounter(false);
                    onError();
                }
            }
        }

        @Override
        public void onCamera(int nResult, String strResultInfo) {
            if (isScreen) {
                if (nResult == ResultConstants.ERROR_CAMERA_OPEN_FAILED) {
                    bCameraPrepared = false;
                    if (null != mDlgCameraFailed) {
                        mDlgCameraFailed.dismiss();
                        mDlgCameraFailed = null;
                    }
                    mDlgCameraFailed = SysAlertDialog.showAlertDialog(
                            RecorderActivity.this,
                            getString(R.string.dialog_tips),
                            getString(R.string.permission_camera_error_p_allow),
                            getString(R.string.exit),
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    RecorderActivity.this.finish();
                                }
                            }, getString(R.string.setting),
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    Utils.gotoAppInfo(RecorderActivity.this,
                                            RecorderActivity.this
                                                    .getPackageName());
                                }
                            });
                    mDlgCameraFailed.setCancelable(false);
                    mDlgCameraFailed.setCanceledOnTouchOutside(false);
                }
            } else {
                if (nResult == ResultConstants.ERROR_CAMERA_OPEN_FAILED) {
                    bCameraPrepared = false;
                    if (null != mDlgCameraFailed) {
                        mDlgCameraFailed.dismiss();
                        mDlgCameraFailed = null;
                    }
                    mDlgCameraFailed = SysAlertDialog.showAlertDialog(
                            RecorderActivity.this,
                            getString(R.string.dialog_tips),
                            getString(R.string.permission_camera_error_p_allow),
                            getString(R.string.exit),
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    RecorderActivity.this.finish();

                                }
                            }, getString(R.string.setting),
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    Utils.gotoAppInfo(RecorderActivity.this,
                                            RecorderActivity.this
                                                    .getPackageName());
                                }
                            });
                    mDlgCameraFailed.setCancelable(false);
                    mDlgCameraFailed.setCanceledOnTouchOutside(false);
                }
            }
        }

        @Override
        public void onPrepared(int nResult, String strResultInfo) {
            if (nResult == ResultConstants.SUCCESS) {
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
            }
        }

        @Override
        public void onGetRecordStatus(int nPosition, int arg1, int arg2) {
            // android.util.Log.e("onGetRecordStatus", nPosition + "...." + arg1
            // + "..." + arg2);
            if (isScreen) {
                if (startTrailer) {
                    if (nPosition > trailerTime || nPosition > osdEnd) {
                        onCloseOrPauseRecordClick();
                    }
                    return;
                }
                if (curPosition == POSITION_MV) {
                    int position = nPosition;
                    if (mLinearSeekbar.getChildCount() > 0 && mIsRecording) {
                        ImageView img = (ImageView) mLinearSeekbar
                                .getChildAt(mLinearSeekbar.getChildCount() - 1);
                        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) img
                                .getLayoutParams();
                        int childWidth = (int) (m_DisplayWidth * ((float) position / mMVMaxTime)) + 1;

                        if ((totalTime + nPosition) > mMVMaxTime) {
                            onCloseOrPauseRecordClick();
                            return;
                        } else {
                            layoutParams.width = childWidth;
                            img.setLayoutParams(layoutParams);
                        }
                    }
                    tvTimer.setText(Utils.stringForTime(totalTime + nPosition,
                            false, true));
                } else if (curPosition == POSITION_REC) {
                    if (mVideoMaxTime != 0) {
                        int position = nPosition;
                        if (mLinearSeekbar.getChildCount() > 0 && mIsRecording) {
                            ImageView img = (ImageView) mLinearSeekbar
                                    .getChildAt(mLinearSeekbar.getChildCount() - 1);
                            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) img
                                    .getLayoutParams();
                            int childWidth = (int) (m_DisplayWidth * ((float) position / mVideoMaxTime)) + 1;

                            if ((totalTime + nPosition) > mVideoMaxTime) {
                                onCloseOrPauseRecordClick();
                                return;
                            } else {
                                layoutParams.width = childWidth;
                                img.setLayoutParams(layoutParams);
                            }
                        }
                        tvTimer.setText(Utils.stringForTime(totalTime + nPosition,
                                false, true));
                    } else {
                        tvTimer.setText(Utils.stringForTime(totalTime + nPosition,
                                true, false));
                    }
                }
                curTotal = totalTime + nPosition;
                setRecordOSDProgress(curTotal);
            } else {
                if (startTrailer) {
                    if (nPosition > trailerTime || nPosition > osdEnd) {
                        onCloseOrPauseRecordClick();
                    }
                    return;
                }
                if (curPosition == POSITION_MV) {
                    int position = nPosition;
                    if (mLinearSeekbar1.getChildCount() > 0 && mIsRecording) {
                        ImageView img = (ImageView) mLinearSeekbar1
                                .getChildAt(mLinearSeekbar1.getChildCount() - 1);
                        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) img
                                .getLayoutParams();
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
                        tvTimer1.setText(Utils.stringForTime(totalTime + nPosition,
                                false, true));
                    } else {
                        tvTimer1.setText(Utils.stringForTime(totalTime + nPosition,
                                true, false));
                    }

                }
                curTotal = totalTime + nPosition;
                setRecordOSDProgress(curTotal);
                // tvTimer1.setText(DateTimeUtils.stringForMillisecondTime(totalTime+nPosition));
            }
        }

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
            mBtnShootingRatio270
                    .setImageResource(R.drawable.btn_shooting_ratio_n);
        }
    }

    // 保存视频信息
    private void insertToGallery(String path, int duration, int width,
                                 int height) {
        ContentValues videoValues = new ContentValues();
        videoValues.put(Video.Media.TITLE, getString(R.string.undefine));
        videoValues.put(Video.Media.MIME_TYPE, "video/mp4");
        videoValues.put(Video.Media.DATA, path);
        videoValues.put(Video.Media.ARTIST, getString(R.string.app_name));
        videoValues.put(Video.Media.DATE_TAKEN,
                String.valueOf(System.currentTimeMillis()));
        videoValues.put(Video.Media.DESCRIPTION, getString(R.string.app_name));
        videoValues.put(Video.Media.DURATION, duration);
        videoValues.put(Video.Media.WIDTH, width);
        videoValues.put(Video.Media.HEIGHT, height);
        getContentResolver().insert(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI, videoValues);

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
        if (SdkEntry.getSdkService().getCameraConfig().enablePlayMusic) {
            if (null != mAudioPlayer) {
                mAudioPlayer.stop();
            } else {
                mAudioPlayer = new AudioPlayer();
                mAudioPlayer
                        .setOnPreparedListener(new AudioPlayer.OnPreparedListener() {

                            @Override
                            public void onPrepared(AudioPlayer mp) {
//                                Log.e("onPrepared", "onPrepared.." + mp.getDuration());
                            }
                        });
                mAudioPlayer.setOnInfoListener(new AudioPlayer.OnInfoListener() {

                    @Override
                    public boolean onInfo(AudioPlayer mp, int what, int extra) {
//                        Log.e("onInfo", "onInfo.." + what + "..." + extra);
                        return false;
                    }
                });
                mAudioPlayer.setOnErrorListener(new AudioPlayer.OnErrorListener() {

                    @Override
                    public boolean onError(AudioPlayer mp, int what, int extra) {
                        Log.e("RecorderActivity", "AudioPlayer_onerror.." + what
                                + "..." + extra);
                        onAutoToast("", "不支持该音乐");
                        return false;
                    }
                });
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == ALBUM_REQUEST_CODE) {
                setResult(RESULT_OK, data);
                finishWithoutGate = true;
                finish();
            } else if (requestCode == VideoEditActivity.REQUSET_MUSICEX) {
                if (null != data) {

                    mAudioMusic = (AudioMusicInfo) data
                            .getParcelableExtra(MoreMusicActivity.MUSIC_INFO);

                    if (mAudioPlayer != null && mAudioMusic != null) {
                        mAudioPlayer.stop();
                        try {
                            mAudioPlayer.setDataSource(mAudioMusic.getPath());
                            mAudioPlayer.setAutoRepeat(true);
                            mAudioPlayer.setTimeRange(mAudioMusic.getStart(),
                                    mAudioMusic.getEnd());
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
            }
        } else if (resultCode == RESULT_FIRST_USER) {
            finishWithoutGate = true;
            finish();
        }
    }

    /**
     * 返回主界面 提示调用系统相机
     */
    private void onError() {
        Intent intent = new Intent();
        intent.setAction(LOG_TAG);
        sendBroadcast(intent);
        Log.e(LOG_TAG, "onError called!");
        this.runOnUiThread(new Runnable() {
            public void run() {
                onBackPressed();
            }
        });
    }

    /**
     * 改变设备方向，防止m_btnRecord响应onTouch();
     */
    private void onOrientationFilter() {
        // Log.e("re-onOrientationChanged--", mOrientationCompensation + ".."
        // + bShowFitlerListLayout);
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
                insertToGallery(videoPath, duration, vcMediaInfo.getVideoWidth(), vcMediaInfo.getVideoHeight());
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
        public void onExportEnd(int nResult);
    }


    private void backUseMvEdit(Intent intent) {
        if (curPosition == POSITION_MV) {
            intent.putExtra(SdkEntry.INTENT_KEY_USE_MV_EDIT, true);
        } else {
            intent.putExtra(SdkEntry.INTENT_KEY_USE_MV_EDIT, false);
        }
    }

}