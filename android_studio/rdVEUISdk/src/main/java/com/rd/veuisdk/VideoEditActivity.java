package com.rd.veuisdk;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.rd.lib.ui.ExtButton;
import com.rd.lib.ui.PreviewFrameLayout;
import com.rd.lib.utils.CoreUtils;
import com.rd.lib.utils.ThreadPoolUtils;
import com.rd.vecore.Music;
import com.rd.vecore.RdVECore;
import com.rd.vecore.VirtualVideo;
import com.rd.vecore.VirtualVideoView;
import com.rd.vecore.exception.InvalidArgumentException;
import com.rd.vecore.exception.InvalidStateException;
import com.rd.vecore.listener.ExportListener;
import com.rd.vecore.models.AspectRatioFitMode;
import com.rd.vecore.models.DewatermarkObject;
import com.rd.vecore.models.EffectInfo;
import com.rd.vecore.models.EffectType;
import com.rd.vecore.models.MediaObject;
import com.rd.vecore.models.MediaType;
import com.rd.vecore.models.MusicFilterType;
import com.rd.vecore.models.Scene;
import com.rd.vecore.models.VisualFilterConfig;
import com.rd.vecore.models.caption.CaptionLiteObject;
import com.rd.vecore.models.caption.CaptionObject;
import com.rd.vecore.utils.Log;
import com.rd.vecore.utils.MiscUtils;
import com.rd.veuisdk.ae.model.AETemplateInfo;
import com.rd.veuisdk.database.DraftData;
import com.rd.veuisdk.database.EffectData;
import com.rd.veuisdk.database.FilterData;
import com.rd.veuisdk.database.HistoryMusicCloud;
import com.rd.veuisdk.database.HistoryMusicData;
import com.rd.veuisdk.database.StickerData;
import com.rd.veuisdk.database.SubData;
import com.rd.veuisdk.database.TTFData;
import com.rd.veuisdk.fragment.AniTypeFragment;
import com.rd.veuisdk.fragment.AudioFragment;
import com.rd.veuisdk.fragment.AudioInfo;
import com.rd.veuisdk.fragment.BackgroundFragment;
import com.rd.veuisdk.fragment.BaseFragment;
import com.rd.veuisdk.fragment.CollageFragment;
import com.rd.veuisdk.fragment.CoverFragment;
import com.rd.veuisdk.fragment.EffectFragment;
import com.rd.veuisdk.fragment.FilterFragment;
import com.rd.veuisdk.fragment.FilterFragmentLookup;
import com.rd.veuisdk.fragment.FilterFragmentLookupLocal;
import com.rd.veuisdk.fragment.GraffitiFragment;
import com.rd.veuisdk.fragment.MVFragment;
import com.rd.veuisdk.fragment.MusicEffectFragment;
import com.rd.veuisdk.fragment.MusicFragmentEx;
import com.rd.veuisdk.fragment.MusicFragmentEx.IMusicListener;
import com.rd.veuisdk.fragment.MusicManyFragment;
import com.rd.veuisdk.fragment.OSDFragment;
import com.rd.veuisdk.fragment.ProportionFragment;
import com.rd.veuisdk.fragment.SoundFragment;
import com.rd.veuisdk.fragment.StickerFragment;
import com.rd.veuisdk.fragment.SubtitleFragment;
import com.rd.veuisdk.fragment.VideoEditFragment;
import com.rd.veuisdk.fragment.VolumeFragment;
import com.rd.veuisdk.fragment.helper.IFragmentHandler;
import com.rd.veuisdk.listener.ICollageListener;
import com.rd.veuisdk.listener.IFixPreviewListener;
import com.rd.veuisdk.manager.ExportConfiguration;
import com.rd.veuisdk.manager.UIConfiguration;
import com.rd.veuisdk.model.CollageInfo;
import com.rd.veuisdk.model.ShortVideoInfoImp;
import com.rd.veuisdk.model.SoundInfo;
import com.rd.veuisdk.model.StickerInfo;
import com.rd.veuisdk.model.VideoOb;
import com.rd.veuisdk.mvp.model.VideoEditModel;
import com.rd.veuisdk.net.StickerUtils;
import com.rd.veuisdk.net.SubUtils;
import com.rd.veuisdk.net.TTFUtils;
import com.rd.veuisdk.ui.HorizontalProgressDialog;
import com.rd.veuisdk.ui.HorizontalProgressDialog.onCancelClickListener;
import com.rd.veuisdk.ui.PaintView;
import com.rd.veuisdk.ui.ProgressView;
import com.rd.veuisdk.ui.RdSeekBar;
import com.rd.veuisdk.utils.AppConfiguration;
import com.rd.veuisdk.utils.BitmapUtils;
import com.rd.veuisdk.utils.CollageManager;
import com.rd.veuisdk.utils.CommonStyleUtils;
import com.rd.veuisdk.utils.ExportHandler;
import com.rd.veuisdk.utils.FileLog;
import com.rd.veuisdk.utils.FileUtils;
import com.rd.veuisdk.utils.IMediaParamImp;
import com.rd.veuisdk.utils.IParamDataImp;
import com.rd.veuisdk.utils.IParamHandler;
import com.rd.veuisdk.utils.IntentConstants;
import com.rd.veuisdk.utils.PathUtils;
import com.rd.veuisdk.utils.SysAlertDialog;
import com.rd.veuisdk.utils.Utils;
import com.rd.veuisdk.utils.VideoEditCollageHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 编辑预览页
 */
@SuppressLint("HandlerLeak")
public class VideoEditActivity extends BaseActivity implements
        IVideoEditorHandler, IParamHandler, MusicEffectFragment.IMusicEffectCallBack, EffectFragment.IEffectHandler {
    /*
     * 请求权限code:读取外置存储
     */
    private final int REQUEST_CODE_READ_EXTERNAL_STORAGE_PERMISSIONS = 1;

    /*
     * 预览播放器的容器 (支持可变长宽比例)
     */
    private PreviewFrameLayout mPflVideoPreview;
    /*
     * 预览播放器的控制进度条
     */
    private RdSeekBar mSbPlayControl;
    /*
     * 预览播放器居中显示的状态按钮
     */
    private ImageView mIvVideoPlayState;
    /*
     * 记录最后的播放时间
     */
    private float mLastPlayPostion;
    /**
     * 记录最后的播放状态
     */
    private boolean mLastPlaying;
    /*
     * 不同显示页面下的代表 取消、返回、确定、下一步 功能的按钮
     */
    private ExtButton mBtnLeft, mBtnRight;
    /*
     * 用于显示当前页面功能介绍
     */
    private TextView mTvTitle;

    /*
     * 字幕类对象
     */
    private SubtitleFragment mSubtitleFragment;
    /**
     * 马赛克
     */
    private OSDFragment mOSDFragment;
    /**
     * 音量
     */
    private VolumeFragment mVolumeFragment;

    /**
     * 画中画
     */
    private CollageFragment mCollageFragment;
    /*
     * 贴纸类对象
     */
    private StickerFragment mStickerFragment;

    /**
     * 特效
     */
    private EffectFragment mEffectFragment;
    /*
     * 音乐类对象
     */
    private MusicFragmentEx mMusicFragmentEx;

    /*
     * 音效
     */
    private SoundFragment mSoundFragment;

    /*
     * 多段配乐
     */
    private MusicManyFragment mMusicManyFragment;

    /**
     * 变声
     */
    private MusicEffectFragment mMusicEffectFragment;
    /*
     * MV类对象
     */
    private MVFragment mMVFragment;

    /*
     * 配音类对象
     */
    private AudioFragment mAudioFragment;
    /**
     * 比例界面
     */
    private ProportionFragment mProportionFragment;
    /**
     * 图片动画界面
     */
    private AniTypeFragment mAniTypeFragment;
    /**
     * 设置 背景
     */
    private BackgroundFragment mBackgroundFragment;
    /*
     * 滤镜类对象
     */
    private FilterFragment mFilterFragment;
    private FilterFragmentLookupLocal mLookupLocal;
    /*
     * 切换功能界面时是否使用动画效果
     */
    protected boolean mIsEditorMenuEnableAnim;
    /*
     * 盛放Scene的列表
     */
    private ArrayList<Scene> mSceneList;
    /*
     * 盛放EditorPreivewPositionListener的列表
     */
    private SparseArray<EditorPreivewPositionListener> mSaEditorPostionListener = new SparseArray<EditorPreivewPositionListener>();
    /*
     * 标记是否可以执行视频导出功能
     */
    private boolean mCanExport = true;

    /*
     * 字幕微调进度View
     */
    private ProgressView mProgressView;
    /*
     * 贴纸 、字幕  容器
     */
    private FrameLayout mLinearWords;

    private ViewGroup mGalleryFragmentContainer;
    private PreviewFrameLayout mPlayerContainer;

    /*
     * 记录播放器暂停状态
     */
    private boolean mIsPausing;
    /*
     * 是否要快速预览
     */
    static final String ACTION_FROM_CAMERA = "来自录像界面->editpriview->videoedit";
    /*
     * 启动是否来自摄像头录像界面
     */
    private boolean mIsFromCamera = false;
    /*
     * 需要更新比例
     */
    private boolean mUpdateAspectPending = true;
    /*
     * 预览比例
     */
    private float mCurProportion = 0;
    private VirtualVideo.Size mNewSize;
    private Handler mHandler;
    private final int REQUEST_FOR_EDIT = 2000;
    public static final int REQUSET_MUSICEX = 1000;
    public static final int REQUSET_SOUND = 1001;
    public static final int REQUSET_MUSIC_MANY = 1002;
    private String mTempRecfile = null;
    private boolean withWatermark = true;
    /*
     * 已经正常初始并获取到权限
     */
    private boolean mIsInitializedAndGotPremission = false;
    /*
     * 已经正常初始并获取到权限
     */
    private LinearLayout mRlPlayerBottomMenu;
    /*
     * 隐藏虚拟键区、播放器控制进度条 倒计时Timer
     */
    private TimerTask mTimerTask;
    private Timer mTimer;
    /*
     * 播放器进度条总时间显示
     */
    private TextView mTvTotalTime;
    /*
     * 播放器进度条当前时间显示
     */
    private TextView mTvCurTime;
    /*
     * 播放器全屏按钮
     */
    private ImageView mIvFullScreen;
    /*
     * 播放器全屏状态标识
     */
    private boolean mIsFullScreen;
    /*
     * 界面最顶部的返回、取消、下一步、确定区域容器，方便全屏时统一隐藏
     */
    private RelativeLayout mRlTitleBar;
    /*
     * 屏幕比例
     */
    private int mProportionStatus;

    private View mContent;


    /*
     * 第一次创建标识
     */
    private boolean mIsOnCreate = false;

    /*
     * 视频导出配置对象
     */
    private ExportConfiguration mExportConfig = null;
    /**
     * 界面配置对象
     */
    private UIConfiguration mUIConfig = null;
    /*
     * VirtualVideoView播放器对象
     */
    private VirtualVideoView mVirtualVideoView;
    /*
     * 视频接口类对象
     */
    private VirtualVideo mVirtualVideo;

    /**
     * 字幕贴纸，实时显示字幕贴纸需要
     *
     * @return
     */
    public VirtualVideo getVirtualVideo() {
        return mVirtualVideo;
    }

    private Button mBtnDraft;
    private ICollageListener mICollageListener;
    private IParamDataImp mParamDataImp = new IParamDataImp();

    /**
     * 保存到草稿箱
     */
    private void onSaveDraft(final boolean exportSave) {

        pause();
        SysAlertDialog.showLoadingDialog(this, R.string.isDrafting);
        DraftData.getInstance().initilize(this);
        ThreadPoolUtils.executeEx(new Runnable() {
            @Override
            public void run() {

                float nDuS = Utils.ms2s(duration);
                ShortVideoInfoImp draftInfo = null;
                if (null == shortVideoInfoImp) {
                    //新增
                    draftInfo = new ShortVideoInfoImp(System.currentTimeMillis(), nDuS);
                } else {
                    //更新
                    draftInfo = shortVideoInfoImp;
                    draftInfo.setDuration(nDuS);
                    draftInfo.setCreateTime(System.currentTimeMillis());
                }

                draftInfo.setSceneList(mSceneList);
                draftInfo.setWordInfoList(TempVideoParams.getInstance().getSubsDuraionChecked());
                draftInfo.setAudioInfos(TempVideoParams.getInstance().getAudios());
                draftInfo.setSoundInfos(TempVideoParams.getInstance().getSoundInfoList());
                draftInfo.setMusicInfos(TempVideoParams.getInstance().getMusicInfoList());
                draftInfo.setRSpecialInfos(TempVideoParams.getInstance().getSpecailsDurationChecked());
                draftInfo.setMOInfos(TempVideoParams.getInstance().getMosaicDuraionChecked());

                draftInfo.setExportConfiguration(mExportConfig);
                draftInfo.setUIConfiguration(mUIConfig);
                draftInfo.setEffectInfos(mEffectInfos);
                draftInfo.setSoundEffectId(mParamDataImp.getSoundEffectId());
                draftInfo.setZoomOut(mParamDataImp.isZoomOut());
                draftInfo.enableBackground(mParamDataImp.isEnableBackground());

                //配乐
                draftInfo.setMusic(mParamDataImp.getFactor(), mParamDataImp.getMusicFactor(), mParamDataImp.isMediaMute(),
                        mParamDataImp.getMusicIndex(), TempVideoParams.getInstance().getMusic(), mParamDataImp.getMusicName());


                draftInfo.setProportion(mProportionStatus, mCurProportion);

                draftInfo.setFilter(mParamDataImp.getFilterIndex(), mParamDataImp.getCurrentFilterType(), mParamDataImp.getLookupConfig());
                draftInfo.setMV(mParamDataImp.getMVId(), mParamDataImp.isRemoveMVMusic());

                //190410 新增画中画
                draftInfo.setCollageInfos(TempVideoParams.getInstance().getCollageDurationChecked());
                //190513 新增涂鸦
                draftInfo.setGraffitiList(mParamDataImp.getGraffitiList());
                //190515新增音调封面
                draftInfo.setMusicPitch(mParamDataImp.getMusicPitch());
                draftInfo.setCoverCaption(mParamDataImp.getCoverCaption());

                draftInfo.moveToDraft();
                {
                    if (null != draftInfo.getCoverCaption()) {
                        //设置的封面
                        draftInfo.setCover(draftInfo.getCoverCaption().getPath());
                    } else {
                        //封面
                        Rect rect = new Rect();
                        MiscUtils.fixZoomTarget(mPreviewWidth, mPreviewHeight, rect, 200);
                        VirtualVideo virtualVideo = getSnapshotEditor();
                        Bitmap bmp = null;
                        if (rect.width() != 0 && rect.height() != 0) {
                            bmp = Bitmap.createBitmap(rect.width(), rect.height(), Bitmap.Config.ARGB_8888);
                        } else {
                            bmp = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
                        }
                        if (virtualVideo.getSnapshot(VideoEditActivity.this, CoverFragment.DEFAULT_COVER_DURATION * 2 / 3, bmp)) {
                            String dst = PathUtils.getTempFileNameForSdcard(draftInfo.getBasePath(), "cover", "jpg");
                            BitmapUtils.saveBitmapToFile(bmp, dst);
                            bmp.recycle();
                            draftInfo.setCover(dst);
                        }
                    }
                }

                //190731背景色
                draftInfo.setBgColor(mParamDataImp.getBgColor());

                if (null == shortVideoInfoImp) {
                    DraftData.getInstance().insert(draftInfo);
                } else {
                    DraftData.getInstance().update(draftInfo);
                }
                mHandler.obtainMessage(DRAFT_SUCCESSED, exportSave).sendToTarget();
            }
        });

    }

    private ShortVideoInfoImp shortVideoInfoImp;

    private VideoEditFragment mVideoEditFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        TAG = "VideoEditActivity";
        super.onCreate(savedInstanceState);
        if (!RdVECore.isInitialized()) {
            android.util.Log.e(TAG, "onCreate: RdVECore not initialized!");
            finish();
            return;
        }
        mIsOnCreate = true;
        EffectData.getInstance().initilize(this);
        FilterData.getInstance().initilize(this);
        shortVideoInfoImp = null;
        int draftId = getIntent().getIntExtra(IntentConstants.INTENT_EXTRA_DRAFT, -1);

        mExportConfig = SdkEntry.getSdkService().getExportConfig();
        mUIConfig = SdkEntry.getSdkService().getUIConfig();
        if (draftId != -1) {
            //草稿箱视频有效
            DraftData.getInstance().initilize(this);
            shortVideoInfoImp = DraftData.getInstance().queryOne(draftId);
            if (null == shortVideoInfoImp) {
                finish(); //强制退出
                return;
            }
            int svDu = getIntent().getIntExtra(IntentConstants.EXTRA_LAST_DURATION, -1);
            if (svDu > 0) {
                shortVideoInfoImp.setDuration(Utils.ms2s(svDu));
            }
        }


        setContentView(R.layout.activity_video_edit);
        mGalleryFragmentContainer = $(R.id.galleryFragmentParent);
//        mBtnDraft = $(R.id.btnDraft);
//        if (mUIConfig.isEnableDraft()) {
//            mBtnDraft.setVisibility(View.VISIBLE);
//            mBtnDraft.setOnClickListener(new OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    onSaveDraft(false);
//                }
//            });
//        } else {
//            mBtnDraft.setVisibility(View.GONE);
//        }

        //修正播放器容器显示比例
        AppConfiguration.fixAspectRatio(this);
        initHandler();
        // 添加api 23权限控制
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int hasReadPermission = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);

            List<String> permissions = new ArrayList<String>();
            if (hasReadPermission != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            if (!permissions.isEmpty()) {
                requestPermissions(
                        permissions.toArray(new String[permissions.size()]),
                        REQUEST_CODE_READ_EXTERNAL_STORAGE_PERMISSIONS);
            } else {
                onInitialized();
            }
        } else {
            onInitialized();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_READ_EXTERNAL_STORAGE_PERMISSIONS: {
                for (int i = 0; i < permissions.length; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        onInitialized();
                    } else {
                        SysAlertDialog.showAutoHideDialog(this, null,
                                getString(R.string.un_allow_video_photo),
                                Toast.LENGTH_SHORT);
                        finish();
                    }
                }
            }
            break;
            default: {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
    }


    /**
     * 初始化
     */
    private void onInitialized() {
        Intent intent = getIntent();
        if (null != shortVideoInfoImp) {
            //草稿箱视频
            ArrayList<Scene> tmp = intent
                    .getParcelableArrayListExtra(IntentConstants.INTENT_EXTRA_SCENE);
            if (null != tmp) {
                mSceneList = tmp;
            } else {
                mSceneList = shortVideoInfoImp.getSceneList();
            }

            TempVideoParams.getInstance().setAudioList(shortVideoInfoImp.getAudioInfos());
            TempVideoParams.getInstance().setSubs(shortVideoInfoImp.getWordInfoList());
            TempVideoParams.getInstance().setSpecial(shortVideoInfoImp.getRSpecialInfos());
            TempVideoParams.getInstance().setMosaics(shortVideoInfoImp.getMOInfos());
            TempVideoParams.getInstance().setMusicObject(shortVideoInfoImp.getMusic());
            TempVideoParams.getInstance().setSoundInfoList(shortVideoInfoImp.getSoundInfos());
            TempVideoParams.getInstance().setMusicInfoList(shortVideoInfoImp.getMusicInfos());
            TempVideoParams.getInstance().setCollageList(shortVideoInfoImp.getCollageInfos());
            //显示比例
            mProportionStatus = shortVideoInfoImp.getProportionStatus();
            mCurProportion = shortVideoInfoImp.getCurProportion();
            mEffectInfos = shortVideoInfoImp.getEffectInfos();
            mCollageInfos = shortVideoInfoImp.getCollageInfos();
            //还原部分参数
            mParamDataImp.restore(shortVideoInfoImp);

        } else {

            //获取场景列表
            mSceneList = intent
                    .getParcelableArrayListExtra(IntentConstants.INTENT_EXTRA_SCENE);
            mProportionStatus = intent.getIntExtra(
                    IntentConstants.EDIT_PROPORTION_STATUS, 0);
            mCurProportion = intent.getFloatExtra(
                    IntentConstants.EXTRA_MEDIA_PROPORTION, 0);


            if (!mUIConfig.isEnableWizard()) {
                if (mUIConfig.videoProportion == 0) {
                    mCurProportion = 0;
                    mProportionStatus = 0;
                } else if (mUIConfig.videoProportion == 1) {
                    mCurProportion = 1;
                    mProportionStatus = 1;
                } else {
                    mCurProportion = (float) 16 / 9;
                    mProportionStatus = 2;
                }
            }

            mCurProportion = getParamData().getProportionAsp();

        }
        if (mSceneList == null || mSceneList.size() == 0) {
            //没有媒体
            onToast(R.string.no_media);
            finish();
            return;
        }
        addRandomTransition();
        mContent = $(android.R.id.content);
        mIsInitializedAndGotPremission = true;
        SubData.getInstance().initilize(this);//字幕初始化数据库
        StickerData.getInstance().initilize(this);//贴纸初始化数据库
        TTFData.getInstance().initilize(this);//字体初始化数据库
        mIsFromCamera = getIntent().getBooleanExtra(ACTION_FROM_CAMERA, false);
        mTempRecfile = getIntent().getStringExtra(EditPreviewActivity.TEMP_FILE);
        initView();

        int duration = null != shortVideoInfoImp ? Utils.s2ms(shortVideoInfoImp.getDuration()) : getEditingMediasDuration();
        TempVideoParams.getInstance().checkParams(duration);
        TempVideoParams.getInstance().setEditingVideoDuration(duration);

        mNewSize = new VirtualVideo.Size(0, 0);


        //计算预览size
        fixPreviewSize();
        //锁定父容器
        fixContainerAspRatio();

        IntentFilter inFilter = new IntentFilter();
        inFilter.addAction(SdkEntry.MSG_EXPORT);
        registerReceiver(mReceiver, inFilter);
        mVideoEditFragment = VideoEditFragment.newInstance(mUIConfig);
        mVideoEditFragment.setMenuListener(mMenuListener);
        changeFragment(R.id.fl_fragment_container, mVideoEditFragment);

        //根据配置的背景模式 设置到媒体
        onBackgroundModeChanged(mParamDataImp.isEnableBackground());
        start();
    }


    //锁定播放器比例
    private float mPreviewAsp = 0;

    /**
     * 计算预览比例
     */
    private void fixPreviewSize() {
        mNewSize.set(mVirtualVideoView.getPreviewMaxWH(), 0);
        VirtualVideo.getMediaObjectOutSize(mSceneList, mCurProportion, mNewSize);
        mPreviewAsp = mNewSize.width / (mNewSize.height + 0.0f);
    }


    /**
     * 添加随机转场
     */
    private void addRandomTransition() {
        if (mUIConfig.isEnableRandTransition()) {
            VideoEditModel model = new VideoEditModel(this);
            model.init();
            for (Scene scene : mSceneList) {
                if (scene.getTransition() == null) {
                    scene.setTransition(model.getRandomTransition());
                }
            }
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        mIsPausing = true;
        if (mVirtualVideoView != null) {
            mLastPlayPostion = mVirtualVideoView.getCurrentPosition();
            mLastPlaying = mVirtualVideoView.isPlaying();
            if (mLastPlaying) {
                pause();
            }
        }
    }

    /***
     *是否需要seekto
     */
    public void setResumeSeekto(boolean seekto) {
        this.bResumeSeekto = seekto;
    }

    private boolean bResumeSeekto = true;

    @Override
    protected void onResume() {
        super.onResume();
        mIsPausing = false;
        if (bResumeSeekto) {
            bResumeSeekto = false;
            if (mVirtualVideoView != null) {
                if (mLastPlayPostion > 0) {
                    mVirtualVideoView.seekTo(mLastPlayPostion);
                    mLastPlayPostion = -1;
                    if (!(mFragCurrent instanceof CoverFragment) && !isThemeMenuItem() && mLastPlaying && !isShowVideoSizeDialog) {
                        start();
                    }
                } else {
                    mVirtualVideoView.seekTo(0);
                }
            }
        }
    }


    private void onCreateDialogAlert(String strMessage, DialogInterface.OnClickListener listener1, DialogInterface.OnClickListener listener2) {
        SysAlertDialog.showAlertDialog(this, getString(R.string.dialog_tips), strMessage, getString(R.string.cancel), listener1, getString(R.string.sure), listener2);
    }

    /**
     * 响应audioFragment onbackpressed()
     */
    protected void onCreateDialogAudio() {
        onCreateDialogAlert(getString(R.string.cancel_all_changed),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 相当于点击完成按钮
                        if (null != mAudioFragment) {
                            mAudioFragment.onAudioFragmentClear();
                        }
                        onRightButtonClick();
                    }
                });

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (null != mVExportListener) {
            mVExportListener.cancelAlertDialog();
        }
    }

    @Override
    protected void onDestroy() {
        SysAlertDialog.cancelLoadingDialog();
        if (!mIsInitializedAndGotPremission) {
            super.onDestroy();
            return;
        }
        unregisterReceiver(mReceiver);

        if (null != mVirtualVideoView) {
            mVirtualVideoView.cleanUp();
            mVirtualVideoView = null;
        }
        if (null != mVirtualVideo) {
            mVirtualVideo.release();
            mVirtualVideo = null;
        }

        if (mSnapshotEditor != null) {
            mSnapshotEditor.release();
            mSnapshotEditor = null;
        }
        SubUtils.getInstance().recycle();
        StickerUtils.getInstance().recycle();

        if (!mUIConfig.isEnableWizard()) {
            // 删除倒序临时文件
            PathUtils.cleanTempFilesByPrefix("reverse");
            if (null != TempVideoParams.getInstance()) {
                TempVideoParams.getInstance().recycle();
            }
            if (!TextUtils.isEmpty(mTempRecfile)) {
                FileUtils.deleteAll(mTempRecfile);
                mTempRecfile = null;
            }
        }
        if (null != mTimerTask) {
            mTimerTask.cancel();
            mTimerTask = null;
        }
        if (null != mTimer) {
            mTimer.cancel();
            mTimer = null;
        }
        TTFUtils.recycle();
        SubData.getInstance().close();
        StickerData.getInstance().close();
        TTFData.getInstance().close();
        EffectData.getInstance().close();
        HistoryMusicData.getInstance().close();
        TempVideoParams.getInstance().setThemeId(0);
        FilterData.getInstance().close();
        HistoryMusicCloud.getInstance().close();
        Utils.cleanTempFile(mStrSaveVideoTrailerFileName);
        super.onDestroy();


        mMVFragment = null;
        mSubtitleFragment = null;
        mStickerFragment = null;
        mEffectFragment = null;
        mOSDFragment = null;
        mCollageFragment = null;
        if (null != mLookup) {
            mLookup.recycle();
            mLookup = null;
        }
        mLookupLocal = null;
        mFilterFragment = null;
        mMusicFragmentEx = null;
        mMusicEffectFragment = null;
        mAudioFragment = null;
        mICollageListener = null;


        if (null != mTempSpecials) {
            mTempSpecials.clear();
            mTempSpecials = null;
        }
        if (null != mCollageInfos) {
            mCollageInfos.clear();
            mCollageInfos = null;
        }
        if (null != mSceneList) {
            mSceneList.clear();
            mSceneList = null;
        }
        if (null != mEffectInfos) {
            mEffectInfos.clear();
            mEffectInfos = null;
        }

        if (null != mSaEditorPostionListener) {
            mSaEditorPostionListener.clear();
            mSaEditorPostionListener = null;
        }
        mParamDataImp = null;

        CollageManager.recycle();

    }

    /**
     * 重新加载配音 音乐到mVirualVideo
     */
    private void addMusic(VirtualVideo virtualVideo) {
        virtualVideo.clearMusic();
        List<Music> musicList = new ArrayList<>();
        if (mVideoEditFragment.getCheckedId() == R.id.rb_audio) {
            // 正在配音界面，处理试听
            if (mAudioFragment != null) {
                for (Music mo : mAudioFragment.getMusicObjects()) {
                    musicList.add(mo);
                }
            }
        } else {
            ArrayList<AudioInfo> temp = TempVideoParams.getInstance().getAudios();
            int len = temp.size();
            for (int i = 0; i < len; i++) {
                musicList.add(temp.get(i).getAudio());
            }
        }
        //音效
        ArrayList<SoundInfo> soundInfos = TempVideoParams.getInstance().getSoundInfoList();
        for (SoundInfo s : soundInfos) {
            musicList.add(s.getmMusic());
        }
        //多段配乐
        ArrayList<SoundInfo> musicInfos = TempVideoParams.getInstance().getMusicInfoList();
        for (SoundInfo s : musicInfos) {
            musicList.add(s.getmMusic());
        }

        ExportHandler.addMusic(virtualVideo, TempVideoParams.getInstance().getMusic(), musicList,
                mParamDataImp.isRemoveMVMusic(), mParamDataImp);
    }

    /**
     * build 或 加载缩略图
     */
    private void addDataSource(VirtualVideo virtualVideo, List<Scene> alReloadScenes) {
        List<CaptionLiteObject> tmp = null;
        //贴纸
        int len = mTempSpecials.size();
        tmp = new ArrayList<>();
        for (int i = 0; i < len; i++) {
            tmp.add(mTempSpecials.get(i));
        }

        len = alReloadScenes.size();
        for (int i = 0; i < len; i++) {
            //重置媒体音量
            List<MediaObject> list = alReloadScenes.get(i).getAllMedia();
            for (int j = 0; j < list.size(); j++) {
                list.get(j).setMixFactor(mParamDataImp.getFactor());
            }
        }

        ExportHandler.addDataSouce(virtualVideo, alReloadScenes, mEffectInfos, mParamDataImp.getMVId(), mUIConfig.enableTitlingAndSpecialEffectOuter,
                TempVideoParams.getInstance().getCaptionObjects(), tmp, TempVideoParams.getInstance().getMarkList(),
                mParamDataImp.getLookupConfig(), mParamDataImp.getCurrentFilterType(), mCollageInfos, mParamDataImp);
    }

    /**
     * 编辑整体滤镜后，重新设置媒体存储的滤镜
     *
     * @param alReloadScenes
     * @param filterIndex    新的滤镜
     * @param filterValue
     * @param config
     */
    private void clearMediaFilter(ArrayList<Scene> alReloadScenes, int filterIndex, int filterValue, VisualFilterConfig config) {
        for (Scene scene : alReloadScenes) {
            List<MediaObject> list = scene.getAllMedia();
            if (null != list) {
                int len = list.size();
                for (int i = 0; i < len; i++) {
                    MediaObject mediaObject = list.get(i);
                    Object tag = mediaObject.getTag();
                    if (null != tag && tag instanceof VideoOb) {
                        VideoOb videoOb = (VideoOb) tag;
                        if (null != videoOb) {
                            IMediaParamImp mediaParamImp = videoOb.getMediaParamImp();
                            if (null != mediaParamImp) {
                                mediaParamImp.setFilterIndex(filterIndex);
                                mediaParamImp.setCurrentFilterType(filterValue);
                                mediaParamImp.setLookupConfig(config);
                            } else {
                                videoOb.setMediaParamImp(new IMediaParamImp(filterIndex, filterValue, config));
                            }
                        }
                    } else {
                        VideoOb videoOb = new VideoOb(mediaObject);
                        videoOb.setMediaParamImp(new IMediaParamImp(filterIndex, filterValue, config));
                        mediaObject.setTag(videoOb);
                    }
                }
            }
        }
    }


    private boolean mCanShowDialog = true;

    @Override
    public void reload(boolean bOnlyAudio) {
        if (bOnlyAudio) {
            if (mVirtualVideoView.isPlaying()) {
                mVirtualVideoView.pause();
            }
            addMusic(mVirtualVideo);
            mVirtualVideo.updateMusic(mVirtualVideoView);
        } else {

            if (mVirtualVideoView == null) {
                return;
            }
            mVirtualVideoView.reset();
            mVirtualVideo.reset();
            //锁定修正播放器比例
            fixPlayerAspRatio();


            if (mVideoEditFragment.getCheckedId() != R.id.rb_sticker) {
                if (mCanShowDialog) {
                    mCanShowDialog = false;
                    showLoading();
                }
            }
            ArrayList<Scene> alReloadScenes = new ArrayList<>();
            if (mParamDataImp.isEnableBackground()) { //  true 有背景颜色 （按钮未选中） ；false 无背景 ，默认（按钮选中）
                mVirtualVideoView.setBackgroundColor(mParamDataImp.getBgColor());
            } else {
                mVirtualVideoView.setBackgroundColor(Color.BLACK);
            }

            loadAllMediaObjects(alReloadScenes);


            mSbPlayControl.setHighLights(null);
            addDataSource(mVirtualVideo, alReloadScenes);
            addMusic(mVirtualVideo);


            if (duration == 0) {
                duration = Utils.s2ms(mVirtualVideo.getDuration());
            }

            try {
                mVirtualVideo.build(mVirtualVideoView);
            } catch (InvalidStateException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void start() {
        if (mVirtualVideoView == null) {
            return;
        }
        mVirtualVideoView.start();

        mIvVideoPlayState.setBackgroundResource(R.drawable.btn_edit_pause);

        //隐藏编辑框
        if (mSoundFragment != null) {
            mSoundFragment.setHideEdit();
        } else if (mOSDFragment != null) {
            mOSDFragment.setHideEdit();
        } else if (mMusicManyFragment != null) {
            mMusicManyFragment.setHideEdit();
        } else if (mGraffitiFragment != null) {
            mGraffitiFragment.setHideEdit();
        } else if (mCollageFragment != null) {
            mCollageFragment.setHideEdit();
        } else if (mSubtitleFragment != null) {
            mSubtitleFragment.setHideEdit();
        } else if (mStickerFragment != null) {
            mStickerFragment.setHideEdit();
        }

    }

    @Override
    public void pause() {
        if (mVirtualVideoView.isPlaying()) {
            mVirtualVideoView.pause();
        }
        mIvVideoPlayState.setBackgroundResource(R.drawable.btn_edit_play);
    }

    @Override
    public void seekTo(int msec) {
        mVirtualVideoView.seekTo(Utils.ms2s(msec));
        mSbPlayControl.setProgress(msec);
        mProgressView.setProgress(msec);
        mTvCurTime.setText(getTime(msec));
    }

    /**
     * 播放器当前进度 单位：毫秒
     */
    @Override
    public int getCurrentPosition() {
        if (null != mVirtualVideoView) {
            return Utils.s2ms(mVirtualVideoView.getCurrentPosition());
        } else {
            return 0;
        }
    }

    @Override
    public void stop() {
        mVirtualVideoView.stop();
        mSbPlayControl.setProgress(0);
        mLastPlayPostion = -1;
    }

    @Override
    public boolean isPlaying() {
        return mVirtualVideoView != null && mVirtualVideoView.isPlaying();
    }


    @Override
    public void changeFilterType(int index, int nFilterType) {
        if (mVirtualVideoView != null) {
            if (!mVirtualVideoView.isPlaying()) {
                start();
            }
            mVirtualVideo.changeFilter(nFilterType);
        }

    }


    @Override
    public void changeFilterLookup(VisualFilterConfig lookup, int index) {
        if (mVirtualVideoView != null) {
            if (!mVirtualVideoView.isPlaying()) {
                start();
            }
            try {
                mVirtualVideo.changeFilter(lookup);
            } catch (InvalidArgumentException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public int getCurrentLookupIndex() {
        return mParamDataImp.getFilterIndex();
    }

    @Override
    public void changeMusicFilter() {
        if (null != mVirtualVideoView) {
            mVirtualVideo.setMusicFilter(MusicFilterType.valueOf(mParamDataImp.getSoundEffectId()));
            if (!isPlaying()) {
                start();
            }
        }
    }

    @Override
    public int getDuration() {
        if (null == mVirtualVideoView) {
            return 1;
        }
        return Utils.s2ms(mVirtualVideoView.getDuration());
    }

    @Override
    public void registerEditorPostionListener(
            EditorPreivewPositionListener listener) {
        mSaEditorPostionListener.append(listener.hashCode(), listener);
    }

    @Override
    public void unregisterEditorProgressListener(
            EditorPreivewPositionListener listener) {
        mSaEditorPostionListener.remove(listener.hashCode());
    }


    private int getEditingMediasDuration() {
        int mDuration = 0;
        int len = mSceneList.size();
        for (int i = 0; i < len; i++) {
            Scene scene = mSceneList.get(i);
            mDuration += Utils.s2ms(scene.getDuration());
        }
        return mDuration;
    }

    /**
     * 字幕、贴纸导出按钮
     */
    private void onResultWord() {
        mCanAutoPlay = false;
        stop();
        showLoading();
        mHandler.sendEmptyMessage(RESULT_STYLE);
    }

    /**
     * 马赛克|去水印
     */
    private void onResultMosaic() {
        onResult();
    }

    private void showLoading() {
        SysAlertDialog.showLoadingDialog(this, getString(R.string.isloading), false, null);
    }


    private void onResult() {
        setTitleBarVisible(true);
        mCanAutoPlay = false;
        stop();
        showLoading();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //字幕对象直接包含再WordInfo.getObject()中
        mHandler.sendEmptyMessage(RESULT_BY_COLLAGE);

    }

    public void onEffectBack() {
        mCanAutoPlay = false;
        stop();
        showLoading();
        mHandler.sendEmptyMessage(RESULT_STYLE);
    }

    @Override
    public void onBackPressed() {
        if (mIsFullScreen) {
            fullScreen(false);
            return;
        }
        if (getSupportFragmentManager().popBackStackImmediate()) {
            return;
        }
        int checkedId = mVideoEditFragment.getCheckedId();
        if (checkedId == R.id.rb_sound_effect) {
            mMusicEffectFragment.onBackPressed();
            return;
        } else if (checkedId == R.id.rb_cover) {
            mCoverFragment.onBackPressed();
            return;
        } else if (checkedId == R.id.rb_graffiti) {
            mGraffitiFragment.onBackPressed();
            return;
        } else if (checkedId == R.id.rb_word) {
            //字幕
            mSubtitleFragment.onBackPressed();
            return;
        } else if (checkedId == R.id.rb_osd) {
            int re = mOSDFragment.onBackPressed();
            if (re == 1) {
                iMosaicHandler.onBackPressed();
            }
            return;
        } else if (checkedId == R.id.rb_collage) {
            mCollageFragment.onLeftClick();
            return;
        } else if (checkedId == R.id.rb_sticker) {
            mStickerFragment.onBackPressed();
            return;
        } else if (checkedId == R.id.rb_effect) {
            if (0 != mEffectFragment.onBackPressed()) {
                onEffectBack();
                return;
            }
        } else if (checkedId == R.id.rb_filter) {
            mHandler.obtainMessage(RESULT_STYLE).sendToTarget();
            return;
        } else if (checkedId == R.id.rb_music) {
            onBack();
            return;
        } else if (checkedId == R.id.rb_sound) {
            mSoundFragment.onLeftClick();
            return;
        } else if (checkedId == R.id.rb_volume) {
            mVolumeFragment.onShowAlert();
            return;
        } else if (checkedId == R.id.rb_music_many) {
            mMusicManyFragment.onLeftClick();
            return;
        } else if (checkedId == R.id.rb_proportion) {
            mProportionFragment.onBack();
            return;
        } else if (checkedId == R.id.rb_animation_type) {
            onBack();
            return;
        } else if (checkedId == R.id.rb_background) {
            mBackgroundFragment.onBack();
            return;
        } else if (checkedId == R.id.rb_mv) {
            mMVFragment.onBack();
            return;
        } else if (checkedId == R.id.rb_audio) {
            if (mAudioFragment.hasChanged()) {
                onCreateDialogAudio();
            } else {
                mAudioFragment.resetAlreadyRecordAudioObject();
                mAudioFragment.setShowFactor(false);
                $(R.id.llAudioFactor).setVisibility(View.GONE);
                returnToMenuLastSelection();
            }
            return;
        }
        if (mUIConfig.isEnableWizard() && !mUIConfig.isHidePartEdit()) {
            backToPartEdit(false, null);
            return;
        }
        TempVideoParams.getInstance().setAspectRatio(
                TempVideoParams.mEditingVideoAspectRatio);

        onCreateDialog(DIALOG_RETURN_ID).show();
    }

    private void backToPartEdit(boolean forFinish, String mStrSaveMp4FileName) {
        if (forFinish) {
            Intent intent = new Intent();
            intent.putExtra(SdkEntry.EDIT_RESULT, mStrSaveMp4FileName);
            setResult(RESULT_CANCELED, intent);
            if (null != mVirtualVideo) {
                mVirtualVideo.release();
                mVirtualVideo = null;
            }
            finish();
            return;
        }

        ArrayList<MediaObject> mEditingMediaObjects = new ArrayList<>();
        int len = mSceneList.size();
        for (int i = 0; i < len; i++) {
            List<MediaObject> tmp = mSceneList.get(i).getAllMedia();
            if (null != tmp && tmp.size() > 0) {
                mEditingMediaObjects.addAll(tmp);
            }
        }
        Intent intent = new Intent();
        intent.putExtra(IntentConstants.EXTRA_MEDIA_LIST, mEditingMediaObjects);
        intent.putExtra(IntentConstants.EDIT_PROPORTION_STATUS, mProportionStatus);
        intent.putExtra(IntentConstants.EXTRA_MEDIA_PROPORTION, mCurProportion);
        intent.putExtra(IntentConstants.ALL_MEDIA_MUTE, mParamDataImp.isMediaMute());
        setResult(RESULT_OK, intent);
        overridePendingTransition(0, 0);
        finish();
    }


    /**
     * 初始化界面布局按钮的功能事件
     */
    private void initView() {
        mPlayerContainer = $(R.id.rlPlayerContainer);
        mTvTitle = $(R.id.tvTitle);
        mTvTitle.setText(R.string.video_edit);
        mRlTitleBar = $(R.id.titlebar_layout);
        mBtnLeft = $(R.id.btnLeft);
        mBtnLeft.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.public_menu_cancel, 0, 0, 0);
        mBtnLeft.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        mBtnLeft.setPadding(25, 0, 0, 0);
        mBtnRight = $(R.id.btnRight);
        mBtnRight.setVisibility(View.VISIBLE);
        mBtnRight.setText(R.string.export);
        mBtnRight.setTextColor(getResources().getColor(R.color.main_orange));
        mBtnRight.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAudioObjects();
                onRightButtonClick();
            }
        });
        mRlPlayerBottomMenu = $(R.id.rlPlayerBottomMenu);
        mTvCurTime = $(R.id.tvCurTime);
        mTvTotalTime = $(R.id.tvTotalTime);
        mTvTotalTime.measure(0, 0);
        int width = mTvTotalTime.getMeasuredWidth();
        mTvCurTime.setWidth(width + CoreUtils.dpToPixel(5));
        mIvFullScreen = $(R.id.ivFullScreen);
        mIvFullScreen.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if ($(R.id.rlEditorMenuAndSubLayout).getVisibility() == View.VISIBLE) {
                    fullScreen(true);
                } else {
                    fullScreen(false);
                }
            }
        });

        mPflVideoPreview = $(R.id.rlPreview);
        mLinearWords = $(R.id.linear_words);
        mProgressView = $(R.id.progressView);
        mProgressView.setScroll(true);
        mProgressView.setListener(onProgressViewListener);
        mIvVideoPlayState = $(R.id.ivPlayerState);
        mIvVideoPlayState.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onEditorPreivewClick();
            }
        });
        resetTitlebar();
        mSbPlayControl = $(R.id.sbEditor);
        mSbPlayControl.setOnSeekBarChangeListener(onSeekbarListener);
        mSbPlayControl.setMax(100);
        mLastPlayPostion = -1;
        mVirtualVideo = new VirtualVideo();
        mVirtualVideoView = $(R.id.epvPreview);
        mVirtualVideoView.setOnPlaybackListener(mPlayViewListener);
        mVirtualVideoView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                int checkId = mVideoEditFragment.getCheckedId();
                if (checkId != R.id.rb_audio && checkId != R.id.rb_word && checkId != R.id.rb_sticker) {
                    onEditorPreivewClick();
                }
            }
        });
        mVirtualVideoView.setOnInfoListener(new VirtualVideo.OnInfoListener() {

            @Override
            public boolean onInfo(int what, int extra,
                                  Object obj) {
                if (what == VirtualVideo.INFO_WHAT_PLAYBACK_PREPARING) {
                    if (mIsOnCreate) {
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                        mIsOnCreate = false;
                    }
                } else if (what == VirtualVideo.INFO_WHAT_GET_VIDEO_HIGHTLIGHTS && obj != null) {
                    int[] arrHightLights = (int[]) obj; // hightlight时间数组，单位为ms
                    mSbPlayControl.setHighLights(arrHightLights);
                }
                return false;
            }
        });
        // 草稿箱按钮是否可见
//        mBtnDraft.setVisibility(mUIConfig.isEnableDraft() ? View.VISIBLE : View.GONE);
    }


    /**
     * 视频比例
     */
    private void onProportionClick() {
        if (mProportionFragment == null) {
            mProportionFragment = new ProportionFragment();
        }
        changeToFragment(mProportionFragment, false);
    }

    /**
     * 图片动画
     */
    private void onImageAnim() {
        if (mAniTypeFragment == null) {
            mAniTypeFragment = new AniTypeFragment();
        }
        changeToFragment(mAniTypeFragment, false);
    }

    /**
     * 背景
     */
    private void onBackgroundClick() {
        if (mBackgroundFragment == null) {
            mBackgroundFragment = new BackgroundFragment();
        }
        changeToFragment(mBackgroundFragment, false);
    }

    /*
     * 音乐类监听回调
     */
    private IMusicListener mMusicListener = new IMusicListener() {

        @Override
        public void onVoiceChanged(boolean isChecked) {
            if (isChecked) {
                mParamDataImp.setMediaMute(false);
            } else {
                mParamDataImp.setMediaMute(true);
            }
            int len = mSceneList.size();
            for (int i = 0; i < len; i++) {
                List<MediaObject> list = mSceneList.get(i).getAllMedia();
                if (null != list && list.size() > 0) {
                    for (int j = 0; j < list.size(); j++) {
                        if (list.get(j).getMediaType() == MediaType.MEDIA_VIDEO_TYPE) {
                            list.get(j).setAudioMute(mParamDataImp.isMediaMute());
                        }
                    }
                }
            }
            reload(false);
            start();
        }

        @Override
        public void onVoiceClick(View v) {
            mVideoEditFragment.setChecked(R.id.rb_audio);
            mMenuListener.onAudio();
        }

    };


    private void fullScreen(boolean isFull) {
        if (isFull) {
            mRlTitleBar.setVisibility(View.GONE);
            if (CoreUtils.hasIceCreamSandwich()) {
                // 全屏时，隐藏虚拟键区
                mContent.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            }
            $(R.id.tmp).setVisibility(View.GONE);
            $(R.id.rlEditorMenuAndSubLayout).setVisibility(View.GONE);
            mIsFullScreen = true;
            if (mVirtualVideoView.getVideoWidth() > mVirtualVideoView.getVideoHeight()) {
                if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                }
            } else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mRlPlayerBottomMenu.getLayoutParams();
            lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            lp.bottomMargin = 0;
            mIvFullScreen.setBackgroundResource(R.drawable.edit_intercept_revert);
        } else {
            mRlTitleBar.setVisibility(View.VISIBLE);
            if (CoreUtils.hasIceCreamSandwich()) {
                mContent.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            }
            $(R.id.tmp).setVisibility(View.VISIBLE);
            $(R.id.rlEditorMenuAndSubLayout).setVisibility(View.VISIBLE);
            mIsFullScreen = false;

            if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mRlPlayerBottomMenu.getLayoutParams();
            lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0);
            lp.bottomMargin = 0;
            mIvFullScreen.setBackgroundResource(R.drawable.edit_intercept_fullscreen);
        }
    }

    private FilterFragmentLookup mLookup;


    private void notifyCurrentPosition(int positionMs) {
        for (int nTmp = 0; nTmp < mSaEditorPostionListener.size(); nTmp++) {
            mSaEditorPostionListener.valueAt(nTmp).onEditorGetPosition(positionMs, duration);
        }
    }


    private void notifyPreviewComplete() {
        for (int nTmp = 0; nTmp < mSaEditorPostionListener.size(); nTmp++) {
            mSaEditorPostionListener.valueAt(nTmp).onEditorPreviewComplete();
        }
    }

    //单位：毫秒
    private int duration = 1;

    private int mPreviewWidth, mPreviewHeight;
    /**
     * 播放器Listener
     */
    private VirtualVideoView.VideoViewListener mPlayViewListener = new VirtualVideoView.VideoViewListener() {
        private boolean isCheckFilter = true;

        @Override
        public void onPlayerPrepared() {
//            android.util.Log.e(TAG, "onPlayerPrepared: " + mUpdateAspectPending + " " + mVirtualVideoView.getDuration()
//                    + " video:" + mVirtualVideoView.getVideoWidth() + "* " + mVirtualVideoView.getVideoHeight() +
//                    " word:" + mVirtualVideoView.getWordLayout().getWidth() + "*" + mVirtualVideoView.getWordLayout().getHeight()
//                    + " view:" + mVirtualVideoView.getWidth() + "*" + mVirtualVideoView.getHeight());
            mPlayerContainer.setVisibility(View.VISIBLE);
            mPreviewWidth = mVirtualVideoView.getVideoWidth();
            mPreviewHeight = mVirtualVideoView.getVideoHeight();

            wordLayoutWidth = mVirtualVideoView.getVideoWidth();
            wordLayoutHeight = mVirtualVideoView.getVideoHeight();
            duration = Utils.s2ms(mVirtualVideoView.getDuration());
            TempVideoParams.getInstance().setEditingVideoDuration(duration);
            if (isCheckFilter
                    && (mParamDataImp.getCurrentFilterType() != 0)) {
                mVirtualVideoView.setFilterType(mParamDataImp.getCurrentFilterType());
                isCheckFilter = false;
            }
            mProgressView.setDuration(duration);
            SysAlertDialog.cancelLoadingDialog();
            mTvTotalTime.setText(getTime(duration));
            mSbPlayControl.setMax(duration);
            int len = mSaEditorPostionListener.size();
            for (int nTmp = 0; nTmp < len; nTmp++) {
                mSaEditorPostionListener.valueAt(nTmp).onEditorPrepred();
            }
            if (mUpdateAspectPending) {
                updatePreviewFrameAspect(mVirtualVideoView.getVideoWidth(),
                        mVirtualVideoView.getVideoHeight());
                mUpdateAspectPending = false;
            }
            notifyCurrentPosition(Utils.s2ms(mVirtualVideoView.getCurrentPosition()));
        }

        @Override
        public boolean onPlayerError(int what, int extra) {
            Log.e(TAG, "onPlayerError: " + what + " extra:" + extra);
            SysAlertDialog.cancelLoadingDialog();
            onToast(R.string.preview_error);
            return false;
        }

        @Override
        public void onPlayerCompletion() {
            notifyPreviewComplete();
            if (!(mFragCurrent instanceof AudioFragment)) {
                mIvVideoPlayState.setBackgroundResource(R.drawable.btn_edit_play);
                mProgressView.setProgress(0);
                mSbPlayControl.setProgress(0);
                notifyCurrentPosition(0);
                mTvCurTime.setText(getTime(0));
                mLastPlayPostion = 0;
                mVirtualVideoView.seekTo(0);
            }
            if (null != mEffectFragment && mFragCurrent == mEffectFragment) {
                mEffectFragment.onComplete();
            }
        }

        @Override
        public void onGetCurrentPosition(float position) {
            int positionMs = Utils.s2ms(position);
            mProgressView.setProgress(positionMs);
            mSbPlayControl.setProgress(positionMs);
            mLastPlayPostion = position;
            mTvCurTime.setText(getTime(positionMs));
            notifyCurrentPosition(positionMs);
            if (null != mEffectFragment && mFragCurrent == mEffectFragment) {
                mEffectFragment.setPosition(position);
            }
        }
    };

    private ProgressView.onProgressListener onProgressViewListener = new ProgressView.onProgressListener() {

        boolean isPlaying = false;

        @Override
        public void onStart() {
            isPlaying = isPlaying();
            if (isPlaying) {
                pause();
            }
        }

        @Override
        public void onProgressing(int progress) {
            seekTo(progress);
            mPlayViewListener.onGetCurrentPosition(Utils.ms2s(getCurrentPosition()));
        }

        @Override
        public void onChanged() {
            if (isPlaying && !isThemeMenuItem()) {
                start();
            }
        }

        @Override
        public void onClick() {
            onEditorPreivewClick();
        }

        @Override
        public void onSeekbarChanging(int scale) {

        }
    };


    /**
     * 获取当前功能项是否为主题
     *
     * @return
     */
    private boolean isThemeMenuItem() {
        return false;
    }

    private void onEditorPreivewClick() {
        if (isPlaying()) {
            pause();
            if (isThemeMenuItem()) {
                mPlayViewListener.onGetCurrentPosition(Utils.ms2s(getCurrentPosition()));
            }
        } else {
            start();
        }
    }


    /**
     * 返回到主菜单
     */
    private void returnToMenuLastSelection() {
        mTvTitle.setText(R.string.video_edit);
        resetTitlebar();
        //恢复按钮可见 片段编辑、特效....
        mProgressView.setScroll(true);
        mProgressView.setVisibility(View.VISIBLE);
        mVideoEditFragment.resetMenu();
        mCanExport = true;
        changeToFragment(mVideoEditFragment, true);
    }


    private boolean mCanAutoPlay = true;

    /**
     * 配乐
     */
    private void onVerVideoMusic() {
        mProgressView.setScroll(true);
        mTvTitle.setText(R.string.music);
        if (null == mMusicFragmentEx) {
            mMusicFragmentEx = MusicFragmentEx.newInstance();
        }
        //横向音乐列表
        String musicUrl = mUIConfig.newMusicUrl;
        boolean useNewMusicUrl = true;
        if (TextUtils.isEmpty(musicUrl)) {
            useNewMusicUrl = false;
            musicUrl = mUIConfig.musicUrl;
        }
        String typeUrl = "", cloudUrl = "";
        if (!TextUtils.isEmpty(typeUrl = mUIConfig.newCloudMusicTypeUrl) && !TextUtils.isEmpty(cloudUrl = mUIConfig.newCloudMusicUrl)) {
            //支持云音乐分页 (单独依赖云音乐配置)
        } else if (!TextUtils.isEmpty(typeUrl = mUIConfig.soundTypeUrl) && !TextUtils.isEmpty(cloudUrl = mUIConfig.soundUrl)) {
            //支持云音乐分页
        }
        if (!TextUtils.isEmpty(typeUrl) && !TextUtils.isEmpty(cloudUrl)) {
            //支持配乐分页
            mMusicFragmentEx.init(useNewMusicUrl, mExportConfig.trailerDuration, musicUrl, mUIConfig.voiceLayoutTpye,
                    mMusicListener, typeUrl, cloudUrl, true, mUIConfig.enableLocalMusic, mUIConfig.isHideDubbing(),
                    mUIConfig.mCloudAuthorizationInfo);
        } else {
            //旧版不支持分页 190625
            cloudUrl = mUIConfig.newCloudMusicUrl;
            boolean bNewCloud = true;
            if (TextUtils.isEmpty(cloudUrl)) {
                bNewCloud = false;
                cloudUrl = mUIConfig.cloudMusicUrl;
            }
            mMusicFragmentEx.init(useNewMusicUrl, mExportConfig.trailerDuration, musicUrl, mUIConfig.voiceLayoutTpye,
                    mMusicListener, "", cloudUrl, bNewCloud, mUIConfig.enableLocalMusic, mUIConfig.isHideDubbing(), mUIConfig.mCloudAuthorizationInfo);
        }
        changeToFragment(mMusicFragmentEx, false);
        if (!isPlaying() && mCanAutoPlay) {
            start();
        }
        if (mCanAutoPlay) {
            seekTo(0);
        }
    }

    /**
     * 音效
     */
    private void onVideoSound() {
        mProgressView.setScroll(true);
        mTvTitle.setText(R.string.sound);
        if (null == mSoundFragment) {
            mSoundFragment = SoundFragment.newInstance();
        }
        mSoundFragment.setSeekBar((LinearLayout) $(R.id.llAudioFactor));
        mSoundFragment.setUrl(mUIConfig.soundTypeUrl, mUIConfig.soundUrl);
        mSoundFragment.setShowFactor(true);
        changeToFragment(mSoundFragment, false);

        if (!isPlaying() && mCanAutoPlay) {
            start();
        }
        if (mCanAutoPlay) {
            seekTo(0);
        }
    }

    /**
     * 变声
     */
    private void onSoundEffect() {
        mProgressView.setScroll(true);
        mTvTitle.setText(getString(R.string.sound_effect));

        if (null == mMusicEffectFragment) {
            mMusicEffectFragment = MusicEffectFragment.newInstance();
        }
        changeToFragment(mMusicEffectFragment, false);

        if (!isPlaying() && mCanAutoPlay) {
            start();
        }
        if (mCanAutoPlay) {
            seekTo(0);
        }
    }

    /**
     * 切换到MV
     */
    private void onVerVideoMV() {
        hideTitlebar();
        if (null == mMVFragment) {
            boolean bUseNewMV = !TextUtils.isEmpty(mUIConfig.newMvUrl);
            mMVFragment = new MVFragment(bUseNewMV, bUseNewMV ? mUIConfig.newMvUrl : mUIConfig.mvUrl, true);
        }
        changeToFragment(mMVFragment, false);
        if (!isPlaying()) {
            start();
        }
        seekTo(0);
    }


    private void setTitleBarVisible(boolean visible) {
        mRlTitleBar.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
        mRlPlayerBottomMenu.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
    }


    private ArrayList<CollageInfo> mCollageInfos = new ArrayList<>();


    private CollageFragment.CallBack mCollageCallBack = new CollageFragment.CallBack() {
        @Override
        public void onLeftClick() {
            mCollageInfos.clear();
            mCollageInfos.addAll(TempVideoParams.getInstance().getCollageDurationChecked());
            if (null != mICollageListener) {
                mICollageListener.onCollageExit(new ICollageListener.CallBack() {
                    @Override
                    public void onAnimationComplete() {
                        onResult();
                    }
                });
            } else {
                onResult();
            }

        }

        @Override
        public void onRightClick(List<CollageInfo> mixList) {
            mCollageInfos.clear();
            if (null != mixList && mixList.size() > 0) {
                mCollageInfos.addAll(mixList);
            }
            TempVideoParams.getInstance().setCollageList(mixList);
            if (null != mICollageListener) {
                mICollageListener.onCollageExit(new ICollageListener.CallBack() {
                    @Override
                    public void onAnimationComplete() {
                        onResult();
                    }
                });
            } else {
                onResult();
            }
        }
    };

    /***
     * fragment公共UI部分 （2019新流程  去水印、画中画）
     */
    private void onFragmentUI19() {
        hideTitlebar();   // 2019新界面子界面统一隐藏titlebar
        stop();
        mIvVideoPlayState.setVisibility(View.GONE);
        mCanExport = false;
        mIsEditorMenuEnableAnim = false;
        mVirtualVideoView.setAutoRepeat(false); // 字幕不需要自动重播
    }

    private CoverFragment mCoverFragment;
    private GraffitiFragment mGraffitiFragment;

    /**
     * 显示封面
     */
    private void onCoverClick() {

        hideTitlebar();
        mProgressView.setScroll(false);
        mProgressView.setVisibility(View.GONE);

        if (null == mCoverFragment) {
            mCoverFragment = CoverFragment.newInstance();
        }
        VirtualVideo virtualVideo = new VirtualVideo();
        for (Scene scene : mSceneList) {
            virtualVideo.addScene(scene);
        }
        pause();
        seekTo(0);
        mCoverFragment.initThumbnail(virtualVideo, mPreviewAsp);
        changeToFragment(mCoverFragment, false);
    }


    /**
     * 显示涂鸦
     */
    private void onGraffitiClick() {
        hideTitlebar();
        mProgressView.setScroll(false);
        mProgressView.setVisibility(View.GONE);

        if (null == mGraffitiFragment) {
            mGraffitiFragment = GraffitiFragment.newInstance();
        }
        mGraffitiFragment.setPaintView((PaintView) $(R.id.paintView));
        mGraffitiFragment.setListener(new GraffitiFragment.IGraffitiListener() {

            @Override
            public void onUpdate(CaptionLiteObject liteObject) {
                mVirtualVideo.updateSubtitleObject(liteObject);
                mVirtualVideoView.refresh();
            }

            @Override
            public void onDelete(CaptionLiteObject liteObject) {
                mVirtualVideo.deleteSubtitleObject(liteObject);
                mVirtualVideoView.refresh();
            }

        });
        pause();
        seekTo(0);
        changeToFragment(mGraffitiFragment, false);
    }


    private VideoEditFragment.IMenuListener mMenuListener = new VideoEditFragment.IMenuListener() {


        @Override
        public void checkItemBefore() {
            mCanExport = true;
        }

        @Override
        public void checkItemEnd() {
            mProgressView.setScroll(false);
            mProgressView.setVisibility(View.GONE);
        }

        @Override
        public void onCover() {
            onCoverClick();
        }

        @Override
        public void onGraffiti() {
            onGraffitiClick();
        }

        @Override
        public void onMV() {
            onVerVideoMV();
        }

        @Override
        public void onCaption() {
            hideTitlebar();
            mVirtualVideoView.setAutoRepeat(false); // 字幕不需要自动重播
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            stop();
            if (null == mSubtitleFragment) {
                mSubtitleFragment = SubtitleFragment.newInstance(mUIConfig.subUrl, mUIConfig.fontUrl);
            }
            mSubtitleFragment.setExtractAudio(new SubtitleFragment.IExtractAudio() {
                @Override
                public List<Scene> getAudioSceneList() {
                    List<Scene> list = new ArrayList<>();
                    list.addAll(mSceneList);
                    return list;
                }
            });
            mSubtitleFragment.setFragmentContainer($(R.id.rlEditorMenuAndSubLayout));
            changeToFragment(mSubtitleFragment, false);
            mTvTitle.setText(R.string.subtitle);
            mCanExport = false;
            mIsEditorMenuEnableAnim = false;
        }

        @Override
        public void onSticker() {
            hideTitlebar();
            // 贴纸
            mVirtualVideoView.setAutoRepeat(false); // 贴纸不需要自动重播
            stop();
            if (null == mStickerFragment) {
                mStickerFragment = StickerFragment.newInstance(mUIConfig.soundTypeUrl, mUIConfig.stickerUrl);
            }
            mStickerFragment.setHandler(mLinearWords);
            changeToFragment(mStickerFragment, false);
            mTvTitle.setText(R.string.sticker);
            mCanExport = false;
            mIsEditorMenuEnableAnim = false;
        }

        @Override
        public void onFilter() {
            onFitlerClick();
        }

        @Override
        public void onEffect() {
            hideTitlebar();
            // 特效
            mVirtualVideoView.setAutoRepeat(false);
            stop();
            if (null == mEffectFragment) {
                String typeUrl = mUIConfig.mResTypeUrl;
                String url = mUIConfig.getEffectUrl();
                mEffectFragment = EffectFragment.newInstance(typeUrl, url);
            }
            changeToFragment(mEffectFragment, false);
            mCanExport = false;
            mIsEditorMenuEnableAnim = false;
        }

        @Override
        public void onCollage() {
            //切换到画中画
            onFragmentUI19();

            View view = $(R.id.edit_video_layout);
            if (null == mICollageListener) {
                mICollageListener = new VideoEditCollageHandler(view, mGalleryFragmentContainer, getSupportFragmentManager());
            }
            seekTo(0);
            if (null == mCollageFragment) {
                mCollageFragment = CollageFragment.newInstance();
                mCollageFragment.setOtherFragmentHeight(view.getHeight());
            }
            mCollageFragment.setLinearWords(mLinearWords);
            mCollageFragment.setCallBack(mCollageCallBack);
            mICollageListener.onCollage(mCollageFragment);
            setFragmentCurrent(mCollageFragment);
        }

        @Override
        public void onOSD() {
            hideTitlebar();
            //马赛克
            mVirtualVideoView.setAutoRepeat(false); // 字幕不需要自动重播
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//            stop();
            mVirtualVideoView.pause();
            mVirtualVideoView.seekTo(0);
            if (null == mOSDFragment) {
                mOSDFragment = OSDFragment.newInstance();
            }
            mOSDFragment.setHandler(iMosaicHandler);
            changeToFragment(mOSDFragment, false);
            mCanExport = false;
            mIsEditorMenuEnableAnim = false;
        }

        @Override
        public void onVolume() {

            if (null == mVolumeFragment) {
                mVolumeFragment = VolumeFragment.newInstance();
            }
            seekTo(0);
            mCanExport = false;
            changeToFragment(mVolumeFragment, false);
        }

        @Override
        public void onMusic() {
            hideTitlebar();
            onVerVideoMusic();
        }

        @Override
        public void onAudio() {
            hideTitlebar();
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            stop();
            if (mAudioFragment == null) {
                mAudioFragment = new AudioFragment();
                mAudioFragment.setSeekBar((LinearLayout) $(R.id.llAudioFactor));
            }
            mAudioFragment.setShowFactor(true);
            changeToFragment(mAudioFragment, false);
            mCanExport = false;
        }

        @Override
        public void onSound() {
            hideTitlebar();
            onVideoSound();
        }

        @Override
        public void onMusicMany() {
            hideTitlebar();
            mProgressView.setScroll(true);
            mTvTitle.setText(R.string.sound);

            if (null == mMusicManyFragment) {
                mMusicManyFragment = mMusicManyFragment.newInstance();
                mMusicManyFragment.setSeekBar((LinearLayout) $(R.id.llAudioFactor));
            }

            if (!TextUtils.isEmpty(mUIConfig.soundTypeUrl) && !TextUtils.isEmpty(mUIConfig.soundUrl)) {
                //云音乐分页 190625
                mMusicManyFragment.setUrl(mUIConfig.soundTypeUrl, mUIConfig.soundUrl, true, mUIConfig.mCloudAuthorizationInfo);
            } else {
                //云音乐
                String cloudUrl = mUIConfig.newCloudMusicUrl;
                boolean bNewCloud = true;
                if (TextUtils.isEmpty(cloudUrl)) {
                    bNewCloud = false;
                    cloudUrl = mUIConfig.cloudMusicUrl;
                }
                //兼容旧版-么有分页
                mMusicManyFragment.setUrl("", cloudUrl, bNewCloud, mUIConfig.mCloudAuthorizationInfo);
            }


            mMusicManyFragment.setShowFactor(true);
            changeToFragment(mMusicManyFragment, false);

            if (!isPlaying() && mCanAutoPlay) {
                start();
            }
            if (mCanAutoPlay) {
                seekTo(0);
            }
        }

        @Override
        public void onMusicEffect() {
            hideTitlebar();
            onSoundEffect();
        }

        @Override
        public void onPartEdit() {
            resetTitlebar();
            stop();
            EditPreviewActivity.gotoEditPreview(VideoEditActivity.this, mSceneList,
                    mCurProportion, mProportionStatus, mParamDataImp.isEnableBackground(),
                    mParamDataImp.isMediaMute(), REQUEST_FOR_EDIT);
        }

        @Override
        public void onProportion() {
            hideTitlebar();
            onProportionClick();
        }

        @Override
        public void onAnimType() {
            hideTitlebar();
            onImageAnim();
        }

        @Override
        public void onBackground() {
            hideTitlebar();
            onBackgroundClick();
        }

    };


    /***
     * 滤镜功能
     */
    private void onFitlerClick() {
        ///
        final String url = "http://d.56show.com/filemanage2/public/filemanage/file/appData";
        mUIConfig.filterUrl = url;
        ///
        hideTitlebar();
        if (!TextUtils.isEmpty(mUIConfig.filterUrl)) {
            //网络lookup滤镜
            if (null == mLookup) {
                mLookup = FilterFragmentLookup.newInstance(mUIConfig.filterUrl);
            }
            changeToFragment(mLookup, false);
        } else if (mUIConfig.filterLayoutTpye == UIConfiguration.FILTER_LAYOUT_3) {
            //本地lookup
            if (null == mLookupLocal) {
                mLookupLocal = FilterFragmentLookupLocal.newInstance();
            }
            changeToFragment(mLookupLocal, false);
        } else {
            if (mFilterFragment == null) {
                mFilterFragment = FilterFragment.newInstance();
            }
            if (mUIConfig.filterLayoutTpye == UIConfiguration.FILTER_LAYOUT_2) {
                //jlk滤镜(单行)
                mFilterFragment.setJLKStyle(true);
                changeToFragment(mFilterFragment, false);
            } else {
                // 分组滤镜 （acv）
                changeToFragment(mFilterFragment, false);
            }
        }
        mCanExport = false;
        if (!isPlaying()) {
            start();
        }
        seekTo(0);
        mTvTitle.setText(R.string.filter);
    }

    /**
     * 隐藏titlebar 2019新版所有子界面都需要隐藏
     */
    private void hideTitlebar() {
        mRlTitleBar.setVisibility(View.INVISIBLE);
        mRlPlayerBottomMenu.setVisibility(View.INVISIBLE);
    }

    /**
     * 标题栏还原到默认状态
     */
    public void resetTitlebar() {
        mRlTitleBar.setVisibility(View.VISIBLE);
        mRlPlayerBottomMenu.setVisibility(View.VISIBLE);
        mBtnLeft.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.edit_back_button, 0, 0, 0);
        mBtnRight.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        mBtnRight.setText(R.string.export);
        mIvVideoPlayState.setVisibility(View.VISIBLE);
    }


    private void setFragmentCurrent(BaseFragment fragment) {
        this.mFragCurrent = fragment;
        if (mUIConfig.enableAutoRepeat && fragment instanceof VideoEditFragment) {
            mVirtualVideoView.setAutoRepeat(true);
        } else {
            mVirtualVideoView.setAutoRepeat(false);
        }
    }

    private BaseFragment mFragCurrent;


    /**
     * 切换fragment
     *
     * @param fragment        需要切换的fragment
     * @param enableAnimation 确定是否使用动画
     */
    private void changeToFragment(final BaseFragment fragment, boolean enableAnimation) {
        if (mFragCurrent == fragment) {
            // 未实际切换fragment时，直接返回
            setFragmentCurrent(fragment); // 重新设置，刷新自动重播状态
            return;
        }
        try {
            if (!enableAnimation) {
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.fl_fragment_container, fragment);
                ft.commit();
                setFragmentCurrent(fragment);
            } else {
                Animation aniSlideOut = AnimationUtils.loadAnimation(this, R.anim.editor_preview_slide_out);
                $(R.id.rlEditorMenuAndSubLayout).startAnimation(aniSlideOut);
                aniSlideOut.setAnimationListener(new Animation.AnimationListener() {

                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                        ft.replace(R.id.fl_fragment_container, fragment);
                        ft.commit();
                        Animation aniSlideIn = AnimationUtils.loadAnimation(getBaseContext(), R.anim.editor_preview_slide_in);
                        $(R.id.rlEditorMenuAndSubLayout).startAnimation(aniSlideIn);
                        setFragmentCurrent(fragment);
                    }
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    protected void updatePreviewFrameAspect(int videoWidth, int videoHeight) {
        float aspVideo = (videoWidth + .0f) / videoHeight;
        mPflVideoPreview.setAspectRatio(aspVideo);
        TempVideoParams.mEditingVideoAspectRatio = aspVideo;
        mPflVideoPreview.post(new Runnable() {

            @Override
            public void run() {
                CommonStyleUtils.init(mPflVideoPreview.getWidth(),
                        mPflVideoPreview.getHeight());
            }
        });
        mPflVideoPreview.setVisibility(View.VISIBLE);
    }

    /**
     * 播放器控制进度条的监听回调
     */
    private OnSeekBarChangeListener onSeekbarListener = new OnSeekBarChangeListener() {
        private boolean m_bIsPlayingOnSeek;

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                onProgressViewListener.onProgressing(progress);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            if (mVirtualVideoView.isPlaying()) {
                pause();
                m_bIsPlayingOnSeek = true;
            } else {
                m_bIsPlayingOnSeek = false;
            }
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            if (m_bIsPlayingOnSeek && !isThemeMenuItem()) {
                // 当前功能项为主题时，不需要恢复播放
                start();
            }
        }
    };


    private IFragmentHandler iMosaicHandler = new IFragmentHandler() {

        @Override
        public void onBackPressed() {
            onResultMosaic();
        }
    };

    //贴纸
    private List<CaptionLiteObject> mTempSpecials = new ArrayList<>();

    /**
     * 读取贴纸
     */
    private void readSpEffect() {
        mTempSpecials.clear();
        List<StickerInfo> lstspecials = TempVideoParams.getInstance().getRSpEffects();
        int splen = 0;
        if (lstspecials != null && (splen = lstspecials.size()) > 0) {
            StickerInfo sinfo;
            for (int i = 0; i < splen; i++) {
                sinfo = lstspecials.get(i);
                if (null != sinfo) {
                    ArrayList<CaptionLiteObject> titem = sinfo.getList();
                    if (null != titem && titem.size() > 0) {
                        mTempSpecials.addAll(titem);
                    }
                }
            }
        }

    }

    private void loadAllMediaObjects(List<Scene> lstScenes) {
        //是否需要清理默认动画
        for (Scene scene : mSceneList) {
            for (MediaObject mediaObject : scene.getAllMedia()) {
                if (getParamData().isZoomOut()) {
                    mediaObject.setClearImageDefaultAnimation(false);
                } else {
                    mediaObject.setClearImageDefaultAnimation(true);
                }
            }
        }

        //背景模式
        setMediaBGMode(mParamDataImp.isEnableBackground());

        if (mParamDataImp.isEnableBackground()) {
            // 有背景
            setBackgroundVisiableFilter(mParamDataImp.getBgColor() == VisualFilterConfig.FILTER_ID_GAUSSIAN_BLUR);
        } else {
            setBackgroundVisiableFilter(false);
        }

        lstScenes.addAll(mSceneList);
        if (mVideoEditFragment.getCheckedId() == R.id.rb_sticker) {
            //贴纸交由界面处理预览逻辑
        } else {
            //贴纸交由core预览
            readSpEffect();
        }
    }


    /**
     * 保存配音
     */
    private void saveAudioObjects() {
        // 配乐2->配音
        if (null != mAudioFragment && mAudioFragment.isVisible()) {
            mAudioFragment.saveAudioData();
            mAudioFragment.setShowFactor(false);
            $(R.id.llAudioFactor).setVisibility(View.GONE);
        }
    }

    //导出成功后，是否删除此草稿 （demo不删除，但秀拍客默认需要删除）
    private boolean deleteDraft = AppConfiguration.isDeleteDraft();
    //默认导出成功后，不用存为草稿
    private boolean bNeedSaveToDraft = false;

    /**
     * 响应确定与导出
     */
    protected void onRightButtonClick() {
        if (!mCanExport) {
            int checkedId = mVideoEditFragment.getCheckedId();
            if (checkedId == R.id.rb_audio) {
                if (null != mAudioFragment) {
                    mAudioFragment.setShowFactor(false);
                    $(R.id.llAudioFactor).setVisibility(View.GONE);
                }
            }
            returnToMenuLastSelection();
            return;
        }
        if (mExportConfig.exportVideoDuration != 0 && mExportConfig.exportVideoDuration < mVirtualVideoView.getDuration()) {
            onCreateDialog(DIALOG_EXPORT_ID);
            return;
        }
        showExportDialog();
    }

    private void export() {
        if (null != mExportHandler) {
            Log.e(TAG, "is exporting....");
            return;
        }
        mVirtualVideoView.stop();
        mExportHandler = new ExportHandler(this);
        exportVideo();
    }


    /**
     * 当前需要导出视频结尾图片路径
     */
    protected String mStrSaveVideoTrailerFileName;


    private ExportHandler mExportHandler;

    private VExportListener mVExportListener;

    /**
     * 导出视频
     */
    private void exportVideo() {
        ArrayList<Scene> alReloadScenes = new ArrayList<Scene>();
        loadAllMediaObjects(alReloadScenes);
        //字幕
        List<CaptionObject> mListCaptions = TempVideoParams.getInstance().getCaptionObjects();

        //马赛克|去水印
        List<DewatermarkObject> mMarkObjectList = TempVideoParams.getInstance().getMarkList();
        mVExportListener = new VExportListener();
        mVExportListener.setStrSaveMp4FileName(mExportHandler.export(alReloadScenes, mParamDataImp, mUIConfig, mListCaptions, mTempSpecials,
                TempVideoParams.getInstance().getMusic(), TempVideoParams.getInstance().getAudios(),
                TempVideoParams.getInstance().getSoundInfoList(), TempVideoParams.getInstance().getMusicInfoList(), mExportConfig,
                mVirtualVideoView.getVideoWidth() / (float) mVirtualVideoView.getVideoHeight(), mVExportListener, withWatermark, mEffectInfos, mMarkObjectList, mCollageInfos,
                mVirtualVideoView.getBackgroundColor(), mParamDataImp));
    }

    /**
     * 当前需要导出的mp4文件路径
     */
    private String mStrSaveMp4FileName;

    /**
     * 导出文件时的回调
     */
    private class VExportListener implements ExportListener {

        private HorizontalProgressDialog epdExport = null;
        private Dialog mAlertCancelDialog = null;

        /**
         * 取消 (导出取消)弹窗
         */
        public void cancelAlertDialog() {
            if (null != mAlertCancelDialog) {
                mAlertCancelDialog.dismiss();
                mAlertCancelDialog = null;
            }
        }

        private boolean cancelExport = false;

        /**
         * @param strSaveMp4FileName
         */
        public void setStrSaveMp4FileName(String strSaveMp4FileName) {
            mStrSaveMp4FileName = strSaveMp4FileName;
        }


        @Override
        public boolean onExporting(int nProgress, int nMax) {
            if (null != epdExport) {
                epdExport.setProgress(nProgress);
                epdExport.setMax(nMax);
            }
            if (cancelExport) {
                return false;
            }
            return true;
        }

        @Override
        public void onExportStart() {
            cancelExport = false;
            if (epdExport == null) {
                epdExport = SysAlertDialog.showHoriProgressDialog(
                        VideoEditActivity.this, getString(R.string.exporting),
                        false, true, new OnCancelListener() {

                            @Override
                            public void onCancel(DialogInterface dialog) {
                                cancelExport = true;
                                mHandler.obtainMessage(CANCEL_EXPORT).sendToTarget();

                            }
                        });
                epdExport.setCanceledOnTouchOutside(false);
                epdExport.setOnCancelClickListener(new onCancelClickListener() {

                    @Override
                    public void onCancel() {
                        mAlertCancelDialog = SysAlertDialog.showAlertDialog(
                                VideoEditActivity.this, "",
                                getString(R.string.cancel_export),
                                getString(R.string.no),
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {

                                    }

                                }, getString(R.string.yes),
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        if (epdExport != null) {
                                            epdExport.cancel();
                                        }
                                    }
                                });
                    }
                });
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

            }
            getWindow()
                    .addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        @Override
        public void onExportEnd(int nResult) {
            isShowVideoSizeDialog = false;
            mGotoBack = false;

            if (!VideoEditActivity.this.isFinishing()) {
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                if (epdExport != null) {
                    epdExport.dismiss();
                    epdExport = null;
                }
                if (mAlertCancelDialog != null) {
                    mAlertCancelDialog.dismiss();
                    mAlertCancelDialog = null;
                }
            }
            if (nResult >= VirtualVideo.RESULT_SUCCESS) {
                mExportHandler = null;
                if (mIsFromCamera) {
                    SdkEntryHandler.getInstance().onExportRecorderEdit(
                            VideoEditActivity.this, mStrSaveMp4FileName);
                } else {
                    SdkEntryHandler.getInstance().onExport(
                            VideoEditActivity.this, mStrSaveMp4FileName);
                }
                if (mUIConfig.isEnableWizard()) {
                    backToPartEdit(true, mStrSaveMp4FileName);
                } else {
                    if (bNeedSaveToDraft) {
                        //秀拍客导出时，选中需要保存草稿 （如果当前已经是草稿就执行更新逻辑）
                        onSaveDraft(true);
                    } else {
                        //导出成功，退出界面
                        onExportSuccess();

                        //明确了当前短视频不需要存为草稿
                        if (null != shortVideoInfoImp) {
                            //清理草稿箱类型的视频
                            if (deleteDraft) {
                                SdkEntry.deleteDraft(VideoEditActivity.this, shortVideoInfoImp);
                            }
                        }
                    }
                }
            } else {
                new File(mStrSaveMp4FileName).delete();
                if (nResult != VirtualVideo.RESULT_EXPORT_CANCEL) {
                    if (nResult == VirtualVideo.RESULT_APPVERIFY_ERROR) {
                        String strMessage = getString(R.string.export_failed);
                        onToast(strMessage);
                        strMessage = getString(R.string.export_failed_by_appverify);
                        Log.e(TAG, "onExportEnd:" + strMessage + ",result:" + nResult);
                        if (null != mExportHandler) {
                            mExportHandler = null;
                        }
                    } else {
                        if ((nResult == VirtualVideo.RESULT_CORE_ERROR_ENCODE_VIDEO ||
                                nResult == VirtualVideo.RESULT_CORE_ERROR_OPEN_VIDEO_ENCODER)
                                && mExportHandler.isHWCodecEnabled()) {
                            mExportHandler.setHWCodecEnabled(false);
                            exportVideo();
                            return;
                        } else {
                            if (null != mExportHandler) {
                                mExportHandler = null;
                            }
                            String strMessage = getString(R.string.export_failed);
                            if (nResult == VirtualVideo.RESULT_CORE_ERROR_LOW_DISK) {
                                strMessage = getString(R.string.export_failed_no_free_space);
                            }
                            onToast(strMessage);
                            FileLog.writeLog(strMessage + ",result:" + nResult);
                            Log.e(TAG, strMessage + ",result:" + nResult);
                        }
                    }
                } else {
                    if (null != mExportHandler) {
                        mExportHandler = null;
                    }
                }
                reload(false);
                if (!mIsPausing) {
                    start();
                }
            }
        }
    }


    private final int CANCEL_EXPORT = 6;
    private final int RESULT_STYLE = 55;
    private final int RESULT_BY_COLLAGE = 56;
    private final int DRAFT_SUCCESSED = 58;
    private Dialog mCancelLoading;
    private final int HIDE_BOTTOM_VIEW = 7;
    private int RECREATE = 21;
    private final int RELOAD = 22;
    private boolean mGotoBack = true;

    /**
     * 导出成功，退出当前界面
     */
    private void onExportSuccess() {
        Intent intent = new Intent();
        intent.putExtra(SdkEntry.EDIT_RESULT, mStrSaveMp4FileName);
        setResult(RESULT_OK, intent);
        VideoEditActivity.this.finish();
    }

    private void initHandler() {
        mHandler = new Handler(getMainLooper()) {
            @Override
            public void handleMessage(android.os.Message msg) {
                if (msg.what == DRAFT_SUCCESSED) {
                    SysAlertDialog.cancelLoadingDialog();
                    if ((boolean) msg.obj) {
                        onExportSuccess();
                    } else {
                        setResult(RESULT_CANCELED);
                        VideoEditActivity.this.finish();
                    }

                } else if (msg.what == CANCEL_EXPORT) {
                    mGotoBack = true;
                    mCancelLoading = SysAlertDialog.showLoadingDialog(
                            VideoEditActivity.this,
                            getString(R.string.canceling), false,
                            new OnCancelListener() {

                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    mCancelLoading = null;
                                    if (mGotoBack) {
                                        finish();
                                    }
                                    mExportHandler = null;
                                }
                            });
                    mHandler.postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            if (null != mCancelLoading) {
                                mCancelLoading.setCancelable(true);
                            }
                        }
                    }, 5000);
                } else if (msg.what == RESULT_STYLE || msg.what == RESULT_BY_COLLAGE) {
                    mCanAutoPlay = true;
                    mCanShowDialog = false;
                    mIvVideoPlayState.setVisibility(View.VISIBLE);
                    returnToMenuLastSelection();
                    setViewVisibility(R.id.edit_video_layout, true);
                    reload(false);
                    start();
                    getWindow().clearFlags(
                            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                } else if (msg.what == HIDE_BOTTOM_VIEW) {
                    if (mRlPlayerBottomMenu.getVisibility() == View.VISIBLE) {
                        mRlPlayerBottomMenu.setVisibility(View.INVISIBLE);
                        if (CoreUtils.hasIceCreamSandwich()) {
                            // 全屏时，隐藏虚拟键区
                            mContent.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
                        }
                    }

                } else if (msg.what == RECREATE) {
                    recreate();
                } else if (msg.what == RELOAD) {
                    reload(false);
                    start();
                }
            }
        };
    }

    @Override
    public void cancelLoading() {
        SysAlertDialog.cancelLoadingDialog();
    }

    @Override
    public VirtualVideoView getEditor() {
        if (mVirtualVideoView != null) {
            return mVirtualVideoView;
        } else {
            return null;
        }
    }

    @Override
    public VirtualVideo getEditorVideo() {
        if (mVirtualVideo != null) {
            return mVirtualVideo;
        } else {
            return null;
        }
    }

    @Override
    public FrameLayout getSubEditorParent() {
        return mLinearWords;
    }

    private VirtualVideo mSnapshotEditor;

    private void getSnapshotEditorImp() {
        mSnapshotEditor = new VirtualVideo();
        //普通截图
        for (Scene scene : mSceneList) {
            mSnapshotEditor.addScene(scene);
        }
        //测试增强模式截图（支持滤镜、mv、字幕、贴纸、特效 等资源）
//        addDataSource(mSnapshotEditor, mSceneList);

    }


    @Override
    public VirtualVideo getSnapshotEditor() {
        //截图与预览的虚拟视频需分开
        if (mSnapshotEditor == null) {
            getSnapshotEditorImp();
        } else {
        }
        return mSnapshotEditor;
    }

    private boolean isShowVideoSizeDialog = false;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (TextUtils.equals(action, SdkEntry.MSG_EXPORT)) {
                withWatermark = intent.getBooleanExtra(SdkEntry.EXPORT_WITH_WATERMARK, true);
                if (mExportConfig.useExportVideoSizeDialog) {
                    isShowVideoSizeDialog = true;
                    mVirtualVideoView.stop();
                    ExportHandler.showExportVideoSizeDialog(VideoEditActivity.this, new ExportHandler.ExportVideoSizeListener() {

                        @Override
                        public void onCancel() {
                            isShowVideoSizeDialog = false;
                        }

                        @Override
                        public void onContinue(boolean saveDraft) {
                            VideoEditActivity.this.bNeedSaveToDraft = saveDraft;
                            export();
                        }
                    }, null != shortVideoInfoImp);
                } else {
                    export();
                }
            }
        }
    };

    private int wordLayoutWidth = 0, wordLayoutHeight = 0;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CoverFragment.REQUST_ABLUM_COVER || requestCode == CoverFragment.REQUST_CROP_COVER) {
            if (mFragCurrent instanceof CoverFragment && null != mCoverFragment) {
                mCoverFragment.onActivityResult(requestCode, resultCode, data);
            }
        } else if (requestCode == CollageFragment.REQUESTCODE_FOR_ADD_MEDIA) {
            if (mFragCurrent instanceof CollageFragment && null != mCollageFragment) {
                mCollageFragment.onActivityResult(requestCode, resultCode, data);
            }
        } else if (requestCode == REQUSET_MUSICEX) {
            if (null != mMusicFragmentEx) {
                mVirtualVideo.reset();
                if (resultCode == RESULT_OK) {
                    mVirtualVideoView.reset();
                    mLastPlayPostion = -1;
                }
                mMusicFragmentEx.onActivityResult(requestCode, resultCode, data);
            }
        } else if (requestCode == REQUEST_FOR_EDIT) {
            mLastPlayPostion = 0;
            if (resultCode == RESULT_OK && null != data) {
                float oldAsp = wordLayoutWidth / (wordLayoutHeight + 0.0f);
                mVirtualVideoView.reset();
                mUpdateAspectPending = true;
                mSceneList.clear();
                int partDurationMs = data.getIntExtra(IntentConstants.EXTRA_LAST_DURATION, 0);
                duration = partDurationMs;
                if (partDurationMs < TempVideoParams.getInstance().getEditingVideoDuration()) {
                    //视频变短，虚拟视频的特效全部清理
                    if (null != mEffectInfos) {
                        mEffectInfos.clear();
                    }
                    mCollageInfos.clear();
                    //根据新的时间线重新调整画中画
                    List<CollageInfo> list = TempVideoParams.getInstance().getCollageDurationChecked(Utils.ms2s(partDurationMs));
                    mCollageInfos.addAll(list);
                    TempVideoParams.getInstance().setCollageList(mCollageInfos);
                }
                if (partDurationMs > 0) {
                    TempVideoParams.getInstance().setEditingVideoDuration(partDurationMs);
                }
                mSceneList = data.getParcelableArrayListExtra(IntentConstants.INTENT_EXTRA_SCENE);
                if (mSceneList.size() > 1 || mSceneList.get(0).getAllMedia().get(0).getMediaType() != MediaType.MEDIA_VIDEO_TYPE) {
                    //多个媒体或者！视频媒体时，清理虚拟视频特效中的时间特效
                    if (null != mEffectInfos) {
                        for (int i = 0; i < mEffectInfos.size(); i++) {
                            EffectInfo effectInfo = mEffectInfos.get(i);
                            EffectType effectType = effectInfo.getEffectType();
                            if (null != effectType && (effectType.ordinal() >= EffectType.SLOW.ordinal() && effectType.ordinal() <= EffectType.REVERSE.ordinal())) {
                                mEffectInfos.remove(i);
                            }
                        }
                    }
                }
                if (!mParamDataImp.isEnableBackground()) {
                    onBackgroundModeChanged(false);
                }
                mProportionStatus = data.getIntExtra(IntentConstants.EDIT_PROPORTION_STATUS, 0);
                if (mSnapshotEditor != null) {
                    mSnapshotEditor.reset();
                    for (Scene scene : mSceneList) {
                        mSnapshotEditor.addScene(scene);
                    }
                }
                showLoading();
                //新增了媒体，重新计算显示比例
                fixDataSourceAfterReload(oldAsp);
            } else {
                start();
            }
        } else if (requestCode == REQUSET_SOUND) {
            if (null != mSoundFragment) {
                mVirtualVideo.reset();
                if (resultCode == RESULT_OK) {
                    mVirtualVideoView.reset();
                    mLastPlayPostion = -1;
                }
                mSoundFragment.onActivityResult(requestCode, resultCode, data);
            }
        } else if (requestCode == REQUSET_MUSIC_MANY) {
            if (null != mMusicManyFragment) {
                mVirtualVideo.release();
                if (requestCode == RESULT_OK) {
                    mVirtualVideoView.reset();
                    mLastPlayPostion = -1;
                }
                mMusicManyFragment.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    /**
     * 修正父容器的比例
     */
    private void fixContainerAspRatio() {
        if (mCurProportion == 0) {
            mPlayerContainer.setAspectRatio((float) mNewSize.getWidth() / mNewSize.getHeight());
        } else {
            mPlayerContainer.setAspectRatio(mCurProportion);
        }
    }

    /**
     * 锁定播放器比例
     */
    private void fixPlayerAspRatio() {
        if (mCurProportion != 0) {
            //片段编辑锁定了比例
            mVirtualVideoView.setPreviewAspectRatio(mCurProportion);
        } else {
            //锁定比例
            mVirtualVideoView.setPreviewAspectRatio(mPreviewAsp);
        }
    }

    /***
     * 根据新的预览比例，修正（字幕、贴纸、画中画 、水印）
     * @param oldAsp 之前的预览比例
     */
    private void fixDataSourceAfterReload(float oldAsp) {

        View viewParent = (View) mPlayerContainer.getParent();
        int pW = viewParent.getWidth();
        int pH = viewParent.getHeight();


        //新增了媒体，重新计算预览size
        fixPreviewSize();
        //修正容器比例
        fixContainerAspRatio();

        //是否需要重新调整字幕
        float asp = mNewSize.width / (mNewSize.height + 0.0f);


        int dstWidth, dstHeight;
        if (asp > 1) {
            //宽铺满
            dstWidth = pW;
            dstHeight = (int) (dstWidth / asp);
        } else {
            //高铺满
            dstHeight = pH;
            dstWidth = (int) (dstHeight * asp);
        }

        if (asp != oldAsp) {
            Utils.onFixPreviewDataSource(oldAsp, mNewSize.width, mNewSize.height, mCollageInfos,
                    new IFixPreviewListener() {
                        @Override
                        public void onComplete() {
                            mHandler.sendEmptyMessage(RELOAD);
                        }
                    }, dstWidth, dstHeight, mVirtualVideo, mVirtualVideoView);
        } else {
            mHandler.sendEmptyMessage(RELOAD);
        }
    }


    @Override
    public void changeAnimation(int animation) {

    }

    @Override
    public void setAETemplateInfo(AETemplateInfo aeTemplateInfo) {

    }

    private void showExportDialog() {
        if (mExportConfig.useCustomExportGuide) {
            SdkEntryHandler.getInstance().onExportClick(VideoEditActivity.this);
        } else {
            if (mExportConfig.useExportVideoSizeDialog) {
                mVirtualVideoView.stop();
                isShowVideoSizeDialog = true;
                ExportHandler.showExportVideoSizeDialog(VideoEditActivity.this, new ExportHandler.ExportVideoSizeListener() {
                    @Override
                    public void onCancel() {
                        isShowVideoSizeDialog = false;
                    }

                    @Override
                    public void onContinue(boolean deleteDraft) {
                        VideoEditActivity.this.deleteDraft = deleteDraft;
                        export();
                    }
                }, null != shortVideoInfoImp);
            } else {
                export();
            }
        }
    }


    private final int DIALOG_RETURN_ID = 1;
    private final int DIALOG_EXPORT_ID = 2;

    @Override
    protected Dialog onCreateDialog(int id) {

        Dialog dialog = null;
        if (id == DIALOG_RETURN_ID) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            dialog = showCancelEditDialog();
        } else if (id == DIALOG_EXPORT_ID) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            String strMessage = getString(R.string.export_duration_limit, (int) mExportConfig.exportVideoDuration);
            dialog = SysAlertDialog.showAlertDialog(this, "", strMessage,
                    getString(R.string.close),
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }, getString(R.string.continue_txt),
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            showExportDialog();
                        }
                    });
        }

        return dialog;
    }

    @Override
    public boolean isMediaMute() {
        return mParamDataImp.isMediaMute();
    }

    /**
     * 切换配乐强制清除mv中的音乐 (同理：切换mv，清除配乐)
     */
    @Override
    public void removeMvMusic(boolean remove) {
        VirtualVideo video;
        mParamDataImp.setRemoveMVMusic(remove);
        if (null != (video = getEditorVideo())) {
            video.removeMVMusic(remove);
        }
    }

    @Override
    public void onProportionChanged(float aspect) {
        float oldAsp = wordLayoutWidth / (wordLayoutHeight + 0.0f);
        mPlayerContainer.setVisibility(View.INVISIBLE);
        onPause();

        showLoading();
        mCurProportion = aspect;
        mUpdateAspectPending = true;
        mVirtualVideoView.reset();

        //保存预览比例
        getParamData().setProportionAsp(aspect);

        //调整字幕、马赛克、水印、画中画
        fixDataSourceAfterReload(oldAsp);
    }

    @Override
    public void onBackgroundModeChanged(boolean isEnableBg) {
        reload(false);
    }

    /**
     * 改变媒体显示模式
     */
    private void setMediaBGMode(boolean isEnableBg) {
        for (Scene scene : mSceneList) {
            for (MediaObject mediaObject : scene.getAllMedia()) {
                if (isEnableBg) {
                    mediaObject.setAspectRatioFitMode(AspectRatioFitMode.KEEP_ASPECTRATIO);
                } else {
                    mediaObject.setAspectRatioFitMode(AspectRatioFitMode.KEEP_ASPECTRATIO_EXPANDING);
                }
            }
        }
    }

    private boolean lastIsBlur = true;

    /**
     * 单个媒体设置背景是否可见
     *
     * @param isBlur true 背景高斯模糊；false 隐藏背景
     */
    private void setBackgroundVisiableFilter(boolean isBlur) {
        for (Scene scene : mSceneList) {
            for (MediaObject mediaObject : scene.getAllMedia()) {
                if (isBlur) {
                    mediaObject.setBackgroundFilterType(VisualFilterConfig.FILTER_ID_GAUSSIAN_BLUR, 0.1f);
                }
                mediaObject.setBackgroundVisiable(isBlur);
            }
        }
        lastIsBlur = isBlur;
    }


    @Override
    public void onBackgroundColorChanged(int color) {
        getParamData().setBgColor(color);
        if (lastIsBlur || color == VisualFilterConfig.FILTER_ID_GAUSSIAN_BLUR) {
            //模糊 、清除模糊必须build
            reload(false);
        } else {
            mVirtualVideoView.setBackgroundColor(color);
        }
        start();
    }

    /**
     * 2019新界面底部菜单返回回调
     */
    @Override
    public void onBack() {
        int checkedId = mVideoEditFragment.getCheckedId();
        if (checkedId == R.id.rb_cover) {
            mHandler.obtainMessage(RESULT_STYLE).sendToTarget();
            return;
        } else if (checkedId == R.id.rb_graffiti) {
            mHandler.obtainMessage(RESULT_STYLE).sendToTarget();
            return;
        } else if (checkedId == R.id.rb_word) {
            onResultWord();
        } else {
            if (checkedId == R.id.rb_filter) {
                mHandler.obtainMessage(RESULT_STYLE).sendToTarget();
            } else if (checkedId == R.id.rb_audio) {
                onBackPressed();
                return;
            } else if (checkedId == R.id.rb_sticker) {
                onResultWord();
                return;
            }
            returnToMenuLastSelection();
        }
    }

    /**
     * 2019新界面底部菜单确认回调
     */
    @Override
    public void onSure() {
        int mCheckedId = mVideoEditFragment.getCheckedId();
        if (mCheckedId == R.id.rb_graffiti) {
            onResultWord();
        } else if (mCheckedId == R.id.rb_word) {
            onResultWord();
        } else if (mCheckedId == R.id.rb_sticker) {
            onResultWord();
        } else {
            if (mCheckedId == R.id.rb_filter) {
                if (null != mLookupLocal) {
                    mLookupLocal.onSure();
                } else if (null != mLookup) {
                    mLookup.onSure();
                } else if (null != mFilterFragment) {
                    mParamDataImp.setFilterIndex(mFilterFragment.getMenuIndex());
                    mParamDataImp.setCurrentFilterType(mFilterFragment.getFilterId());
                    mParamDataImp.setLookupConfig(null);
                }
                clearMediaFilter(mSceneList, mParamDataImp.getFilterIndex(), mParamDataImp.getCurrentFilterType(), mParamDataImp.getLookupConfig());
            } else if (mCheckedId == R.id.rb_audio) {
                saveAudioObjects();
            } else if (mCheckedId == R.id.rb_volume) {
                for (Scene scene : mSceneList) {
                    for (MediaObject object : scene.getAllMedia()) {
                        object.setMixFactor(mParamDataImp.getFactor());
                    }
                }
                returnToMenuLastSelection();
            }
            returnToMenuLastSelection();
        }
    }

    @Override
    public IParamDataImp getParamData() {
        return mParamDataImp;
    }


    private ArrayList<EffectInfo> mEffectInfos;

    @Override
    public VirtualVideoView getPlayer() {
        return mVirtualVideoView;
    }

    @Override
    public ArrayList<EffectInfo> getEffectInfos() {
        if (null == mEffectInfos) {
            mEffectInfos = new ArrayList<>();
        }
        return mEffectInfos;
    }

    @Override
    public VirtualVideo getSnapVideo() {
        return getSnapshotEditor();
    }

    /**
     * 实时更新贴纸（主要是滤镜贴纸）
     */
    @Override
    public void updateEffects(ArrayList<EffectInfo> list) {
        boolean isPlaying = mVirtualVideoView.isPlaying();
        if (isPlaying) {
            mVirtualVideoView.pause();
        }
        mEffectInfos = list;
        //清理播放器中已加载的全部特效 （实时预览必须先删除再新增）
        mVirtualVideo.clearEffects(mVirtualVideoView);
        //再次往虚拟视频添加特效
        ExportHandler.updateEffects(mVirtualVideo, mEffectInfos);
        //更新播放器
        mVirtualVideo.updateEffects(mVirtualVideoView);
        if (isPlaying) {
            mVirtualVideoView.start();
        }


    }

    @Override
    public void updateEffectsReload(ArrayList<EffectInfo> list, int seekto) {
        boolean isPlaying = mVirtualVideoView.isPlaying();
        if (isPlaying) {
            mVirtualVideoView.pause();
        }
        mEffectInfos = list;
        reload(false);
        seekTo(seekto);
    }

    @Override
    public void onEffectBackToMain() {
        onBackPressed();
    }

    @Override
    public void onEffectSure(ArrayList<EffectInfo> list) {
        mEffectInfos = list;
        onEffectBack();
    }

    /**
     * 只有一个视频时才能操作时间贴纸
     *
     * @return
     */
    @Override
    public MediaObject getReverseMediaObjcet() {
        MediaObject tmp;
        if (mSceneList.size() == 1 && mSceneList.get(0).getAllMedia().size() == 1 && (tmp = mSceneList.get(0).getAllMedia().get(0)).getMediaType() == MediaType.MEDIA_VIDEO_TYPE) {
            //只有一个视频且视频无任何超作时有效
            return tmp;
        }
        return null;
    }


    @Override
    public boolean enableMultiEffect() {
        return true;
    }
}
