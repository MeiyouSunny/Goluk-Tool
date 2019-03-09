package com.rd.veuisdk;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.rd.lib.ui.ExtButton;
import com.rd.lib.ui.PreviewFrameLayout;
import com.rd.lib.ui.RotateRelativeLayout;
import com.rd.lib.utils.CoreUtils;
import com.rd.vecore.Music;
import com.rd.vecore.VirtualVideo;
import com.rd.vecore.VirtualVideoView;
import com.rd.vecore.exception.InvalidArgumentException;
import com.rd.vecore.exception.InvalidStateException;
import com.rd.vecore.listener.ExportListener;
import com.rd.vecore.models.MediaObject;
import com.rd.vecore.models.MediaType;
import com.rd.vecore.models.Scene;
import com.rd.vecore.models.SubtitleObject;
import com.rd.vecore.models.Trailer;
import com.rd.vecore.models.VideoConfig;
import com.rd.vecore.models.Watermark;
import com.rd.vecore.utils.Log;
import com.rd.veuisdk.SpecialHandler.ISpecailListener;
import com.rd.veuisdk.SubtitleHandler.ISubHandler;
import com.rd.veuisdk.adapter.SpecialStyleAdapter;
import com.rd.veuisdk.adapter.TTFAdapter;
import com.rd.veuisdk.database.HistoryMusicCloud;
import com.rd.veuisdk.database.HistoryMusicData;
import com.rd.veuisdk.database.MVData;
import com.rd.veuisdk.database.SpecialData;
import com.rd.veuisdk.database.SubData;
import com.rd.veuisdk.database.TTFData;
import com.rd.veuisdk.export.IExportSpecial;
import com.rd.veuisdk.export.IExportSub;
import com.rd.veuisdk.fragment.AudioFragment;
import com.rd.veuisdk.fragment.AudioInfo;
import com.rd.veuisdk.fragment.BaseFragment;
import com.rd.veuisdk.fragment.FilterFragment;
import com.rd.veuisdk.fragment.FilterFragmentEx;
import com.rd.veuisdk.fragment.MVFragment;
import com.rd.veuisdk.fragment.MenuUncheckedFragment;
import com.rd.veuisdk.fragment.MusicFragmentEx;
import com.rd.veuisdk.fragment.MusicFragmentEx.IMusicListener;
import com.rd.veuisdk.manager.ExportConfiguration;
import com.rd.veuisdk.manager.TextWatermarkBuilder;
import com.rd.veuisdk.manager.UIConfiguration;
import com.rd.veuisdk.model.MusicItem;
import com.rd.veuisdk.model.SpecialInfo;
import com.rd.veuisdk.model.WordInfo;
import com.rd.veuisdk.net.SpecialUtils;
import com.rd.veuisdk.net.SubUtils;
import com.rd.veuisdk.net.TTFUtils;
import com.rd.veuisdk.ui.HorizontalProgressDialog;
import com.rd.veuisdk.ui.HorizontalProgressDialog.onCancelClickListener;
import com.rd.veuisdk.ui.ProgressView;
import com.rd.veuisdk.ui.RdSeekBar;
import com.rd.veuisdk.utils.AppConfiguration;
import com.rd.veuisdk.utils.CommonStyleUtils;
import com.rd.veuisdk.utils.DateTimeUtils;
import com.rd.veuisdk.utils.FileLog;
import com.rd.veuisdk.utils.IntentConstants;
import com.rd.veuisdk.utils.PathUtils;
import com.rd.veuisdk.utils.SysAlertDialog;
import com.rd.veuisdk.utils.Utils;
import com.rd.veuisdk.utils.ViewUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 编辑预览页
 */
@SuppressLint("HandlerLeak")
public class VideoEditActivity extends BaseActivity implements
        IVideoEditorHandler,
        IVideoEditorHandler.IEditorThemeTitleHandler {
    private final String TAG = "VideoEditActivity";
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
      * 用于盛放MV、音乐、配音、字幕、特效等RadioButton的RadioGroup
     */
    private RadioGroup mEditorMenuGroups;
    /*
     * 功能菜单最后选择项
     */
    private int mLastEditorMenuCheckId;
    /*
      * 字幕类对象
     */
    private SubtitleHandler mSubtitleHandler;
    /*
     * 特效类对象
    */
    private SpecialHandler mSpecialHandler;
    /*
    * 音乐类对象
   */
    private MusicFragmentEx mMusicFragmentEx;
    /*
    * MV类对象
   */
    private MVFragment mMVFragment;

    /*
     * 配音类对象
    */
    private AudioFragment mAudioFragment;
    /*
    * 是否正处在配音界面
    */
    private boolean mIsInAudioFragment = false;
    /*
    * 滤镜类对象
    */
    private FilterFragment mFilterFragment;
    /*
   * 未选中任何功能界面类的对象
   */
    private MenuUncheckedFragment mMenuUncheckedFragment;
    /*
   * 切换功能界面时是否使用动画效果
   */
    protected boolean mIsEditorMenuEnableAnim;
    /*
   * 记录当前的滤镜效果
   */
    public static int mCurrentFilterType = 0;
    /*
   * 盛放MediaObject的列表
   */
    private ArrayList<MediaObject> mEditingMediaObjects;
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
     * 标记是否显示mivVideoPlayState按钮
     */
    private boolean mPlayStatusShowing = true;
    /*
    * 字体 、特效 、字幕  IntentFilter
    */
    private IntentFilter mTtfFilter, mSpecialFilter, mSubFilter;
    /*
    * 字幕微调进度View
    */
    private ProgressView mProgressView;
    /*
    * 特效 、字幕  容器
    */
    private FrameLayout mLinearWords;

    /*
    * 记录播放器暂停状态
    */
    private boolean mIsPausing;
    /*
   * 是否要快速预览
   */
    private boolean mIsFastPreview;
    /*
   * 是否要快速预览
   */
    static final String ACTION_FROM_CAMERA = "来自录像界面->editpriview->videoedit";
    /*
   * 启动是否来自摄像头录像界面
   */
    private boolean mIsFromCamera = false;
    private PreviewFrameLayout mPreviewLayout;
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
    private final int REQUEST_FOR_EDIT = 1;
    private String mTempRecfile = null;
    /*
   * 已经正常初始并获取到权限
   */
    private boolean mIsInitializedAndGotPremission = false;
    /*
  * 已经正常初始并获取到权限
  */
    private RotateRelativeLayout mRlPlayerBottomMenu;
    /*
   * 隐藏虚拟键区、播放器控制进度条 倒计时Timer
   */
    private TimerTask mTimerTask;
    private Timer mTimer;
    /*
   * mTimerTask 计数用
   */
    private int mTimerCount;
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
    * 导出路径
    */
    private String mSavePath = null;
    /*
    * 屏幕比例
    */
    private float mScreenRatio;
    private int mProportionStatus;
    /*
    * 原音开关
    */
    private boolean mMediaMute = false;
    private View mContent;


    //private boolean isScreenChange = false;
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

    /*
    * 视频接口类对象用于导出视频
    */
    private VirtualVideo mVirtualVideoSave;


    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mIsOnCreate = true;
        MVData.getInstance().initilize(this);
        mExportConfig = SdkEntry.getSdkService().getExportConfig();
        mUIConfig = SdkEntry.getSdkService().getUIConfig();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_edit);
        mStrActivityPageName = getString(R.string.video_edit_ac_name);

        if (!Utils.checkDeviceHasNavigationBar(this)) {
            AppConfiguration.setAspectRatio(1);
        }
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
                super.onRequestPermissionsResult(requestCode, permissions,
                        grantResults);
            }
        }
    }

    /**
     * 初始化
     */
    private void onInitialized() {
        Intent intent = getIntent();
        //获取场景列表
        mSceneList = intent
                .getParcelableArrayListExtra(IntentConstants.INTENT_EXTRA_SCENE);
        //读取场景列表写入到malEditingMediaObjects
        mEditingMediaObjects = new ArrayList<>();
        for (Scene scene : mSceneList) {
            mEditingMediaObjects.add(scene.getAllMedia().get(0));
        }

        mSavePath = mExportConfig.savePath;
        mExportDuration = mExportConfig.exportVideoDuration;

        mProportionStatus = intent.getIntExtra(
                IntentConstants.EDIT_PROPORTION_STATUS, 0);
        mCurProportion = intent.getFloatExtra(
                IntentConstants.EXTRA_MEDIA_PROPORTION, 0);

        mContent = findViewById(android.R.id.content);

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

        mIsInitializedAndGotPremission = true;
        SubData.getInstance().initilize(this);//字幕初始化数据库
        SpecialData.getInstance().initilize(this);//特效初始化数据库
        TTFData.getInstance().initilize(this);//字体初始化数据库
        mIsFromCamera = getIntent().getBooleanExtra(ACTION_FROM_CAMERA, false);
        mTempRecfile = getIntent().getStringExtra(EditPreviewActivity.TEMP_FILE);
        initView();
        initSubtitleEffects();

        int nduration = getEditingMediasDuration();
        TempVideoParams.getInstance().checkParams(nduration);
        TempVideoParams.getInstance().setEditingVideoDuration(nduration);
        mVirtualVideoView.setPreviewAspectRatio(mCurProportion);
        mNewSize = new VirtualVideo.Size(0, 0);
        VirtualVideo.getMediaObjectOutSize(mSceneList, mCurProportion,
                mNewSize);

        mTtfFilter = new IntentFilter(TTFAdapter.ACTION_TTF);
        mSpecialFilter = new IntentFilter(SpecialStyleAdapter.ACTION_SPECIAL);
        mSubFilter = new IntentFilter(SpecialStyleAdapter.ACTION_SUB);
        registerReceiver(mSubtitleHandler.mSubtitleReceiver, mSubFilter);
        registerReceiver(mSubtitleHandler.mReceiver, mTtfFilter);
        registerReceiver(mSpecialHandler.mReceiver, mSpecialFilter);
        IntentFilter inFilter = new IntentFilter();
        inFilter.addAction(SpecialStyleAdapter.ACTION_SHOW_RIGHT);
        registerReceiver(mReceiver, inFilter);
        mPreviewLayout.post(new Runnable() {

            @Override
            public void run() {
                DisplayMetrics disp = CoreUtils.getMetrics();
                View target = findViewById(R.id.rlEditorMenuAndSubLayout);
                int[] location = new int[2];
                target.getLocationInWindow(location);
                android.widget.LinearLayout.LayoutParams lp;

                lp = new LinearLayout.LayoutParams(disp.widthPixels,
                        disp.heightPixels - location[1]);

                lp.setMargins(0, location[1], 0, disp.heightPixels);
                findViewById(R.id.theframelayout).setLayoutParams(lp);
            }
        });

        mLoadDataHandler.removeCallbacks(mLoadDataRunnable);
        mLoadDataHandler.postDelayed(mLoadDataRunnable, 100);//获取字幕列表
    }

    private static Handler mLoadDataHandler = new Handler(Looper.getMainLooper());

    /**
     * 获取字幕列表
     */
    private Runnable mLoadDataRunnable = new Runnable() {

        @Override
        public void run() {
            ArrayList<SubtitleObject> effects = mSubtitleHandler.onExport(
                    mNewSize.width, mNewSize.height);
            TempVideoParams.getInstance().setSubEffects(effects);
            reload(false);
            start();
        }
    };

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
        if (mSubtitleHandler != null) {
            mSubtitleHandler.onPasue();
        }
        if (mSpecialHandler != null) {
            mSpecialHandler.onPasue();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mIsPausing = false;

        if (mSubtitleHandler != null) {
            mSubtitleHandler.onResume();
        }
        if (mSpecialHandler != null) {
            mSpecialHandler.onResume();
        }

        if (mVirtualVideoView != null) {
            if (mLastPlayPostion > 0) {
                mVirtualVideoView.seekTo(mLastPlayPostion);
                mLastPlayPostion = -1;
                if (!isThemeMenuItem() && mLastPlaying) {
                    start();
                }
            } else {
                mVirtualVideoView.seekTo(0);
            }
        }
    }


    private void onCreateDialogAlert(String strMessage,
                                     DialogInterface.OnClickListener listenern,
                                     DialogInterface.OnClickListener listenery) {
        SysAlertDialog.showAlertDialog(this, getString(R.string.dialog_tips),
                strMessage, getString(R.string.cancel), listenern,
                getString(R.string.sure), listenery);
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
    protected void onDestroy() {
        SysAlertDialog.cancelLoadingDialog();
        if (!mIsInitializedAndGotPremission) {
            super.onDestroy();
            return;
        }
        unregisterReceiver(mSubtitleHandler.mReceiver);
        unregisterReceiver(mSpecialHandler.mReceiver);
        unregisterReceiver(mSubtitleHandler.mSubtitleReceiver);
        unregisterReceiver(mReceiver);
        if (null != mVirtualVideoView) {
            mVirtualVideoView.cleanUp();
            mVirtualVideoView = null;
        }
        if (mSnapshotEditor != null) {
            mSnapshotEditor.release();
            mSnapshotEditor = null;
        }
        SubUtils.getInstance().recycle();
        SpecialUtils.getInstance().recycle();

        if (!mUIConfig.isEnableWizard()) {
            // 删除倒序临时文件
            PathUtils.cleanTempFilesByPrefix("reverse");
            if (null != TempVideoParams.getInstance()) {
                TempVideoParams.getInstance().recycle();
            }
            if (!TextUtils.isEmpty(mTempRecfile)) {
                try {
                    new File(mTempRecfile).delete(); // 删除临时录制的文件
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mTempRecfile = null;
            }
        }
        // 删除自定义水印临时文件
        if (!TextUtils.isEmpty(mStrCustomWatermarkTempPath)) {
            try {
                new File(mStrCustomWatermarkTempPath).delete();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mStrCustomWatermarkTempPath = null;
        }
        if (null != mTimerTask) {
            mTimerTask.cancel();
            mTimerTask = null;
        }
        if (null != mTimer) {
            mTimer.cancel();
            mTimer = null;
        }
        mSpecialHandler.onDestory();
        mSubtitleHandler.onDestory();
        TTFUtils.recycle();
        SubData.getInstance().close();
        SpecialData.getInstance().close();
        TTFData.getInstance().close();
        HistoryMusicData.getInstance().close();
        TempVideoParams.getInstance().setThemeId(0);
        MVData.getInstance().close();
        HistoryMusicCloud.getInstance().close();
        Utils.cleanTempFile(mStrSaveVideoTrailerFileName);
        super.onDestroy();


        if (null != mMVFragment) {
            mMVFragment = null;
        }
    }

    /**
     * 重新加载配音 音乐到mVirualVideo
     */
    private void addMusic(VirtualVideo virtualVideo) {
        virtualVideo.clearMusic();
        Music ao = TempVideoParams.getInstance().getMusic();
        if (ao != null) {
            try {
                virtualVideo.addMusic(ao);
            } catch (InvalidArgumentException e) {
                e.printStackTrace();
            }
        }
        if (mIsInAudioFragment) {// 正在配音界面，处理试听
            if (mAudioFragment != null) {
                for (Music mo : mAudioFragment.getMusicObjects()) {
                    try {
                        virtualVideo.addMusic(mo);
                    } catch (InvalidArgumentException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            ArrayList<AudioInfo> temp = TempVideoParams.getInstance()
                    .getAudios();
            int len = temp.size();
            for (int i = 0; i < len; i++) {
                try {
                    virtualVideo.addMusic(temp.get(i).getAudio());
                } catch (InvalidArgumentException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    @Override
    public void reload(boolean bFastPreview,
                       List<Scene> lstEditingScenes) {
        reload(false, bFastPreview, lstEditingScenes);
    }

    /**
     * 预览重新加载
     *
     * @param bOnlyAudio       是否只重新加载音频
     * @param bFastPreview     确定是否为快速预览
     * @param lstEditingScenes
     */
    private void reload(boolean bOnlyAudio, boolean bFastPreview,
                        List<Scene> lstEditingScenes) {
//        Log.e("reload",
//                "---"
//                        + bOnlyAudio
//                        + "--"
//                        + bFastPreview
//                        + "--"
//                        + ((null != lstEditingScenes) ? lstEditingScenes
//                        .size() : "xxx"));
        mIsFastPreview = bFastPreview;
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
            float fOldAspectRatio = (float) mVirtualVideoView.getWidth()
                    / mVirtualVideoView.getHeight();
            mVirtualVideoView.reset();
            mVirtualVideo.reset();
            if (mIsFastPreview) {
                mVirtualVideoView.setPreviewAspectRatio(fOldAspectRatio);
            }

            ArrayList<Scene> alReloadScenes = new ArrayList<Scene>();
            if (lstEditingScenes != null
                    && lstEditingScenes.size() > 0) {
                alReloadScenes.addAll(lstEditingScenes);
            } else {
                if (!mSpecialHandler.isEditing() && !mSubtitleHandler.isEditing()) {
                    if (mCanShowDialog) {
                        mCanShowDialog = false;
                        SysAlertDialog.showLoadingDialog(this,
                                getString(R.string.isloading), false, null);
                    }
                }
                loadAllMediaObjects(alReloadScenes);
            }
            mSbPlayControl.setHighLights(null);


            for (Scene scene : alReloadScenes) {
                mVirtualVideo.addScene(scene);
            }

            //mv必须在字幕\特效之前加载
            if (null != mMVFragment) {
                mVirtualVideo.setMV(mMVFragment.getCurrentMVId());
            }
            mVirtualVideo.setEnableTitlingAndSpEffectOuter(mUIConfig.enableTitlingAndSpecialEffectOuter);
            mVirtualVideo.removeMVMusic(mIsRemoveMVMusic);
            if (!mSubtitleHandler.isEditing() && null != mListSubtitles) {
                int len = mListSubtitles.size();
                for (int i = 0; i < len; i++) {
                    SubtitleObject st = mListSubtitles.get(i);
//                    Log.e("sub", st.getTimelineStart() + "................" + st.getTimelineEnd());
                    mVirtualVideo.addSubtitle(st);
                }
            }
            if (!mSpecialHandler.isEditing() && null != mTempSpecials) {
                int len = mTempSpecials.size();
                for (int i = 0; i < len; i++) {
                    SubtitleObject st = mTempSpecials.get(i);
                    // Log.e("sp", st.getTimeLineStart() + "................" + st.getTimeLineEnd() + "..." + st.getShowRectF().toShortString() + ".." + st.getWidth() + "*" + st.getHeight() + ".." + st.getShowWidth() + "*" + st.getShowHeight());
                    mVirtualVideo.addSubtitle(st);
                }
            }

            addMusic(mVirtualVideo);
            mVirtualVideo.changeFilter(mCurrentFilterType);
            try {
                mVirtualVideo.build(mVirtualVideoView);
            } catch (InvalidStateException e) {
                e.printStackTrace();
            }
            mVirtualVideoView.setFilterType(mCurrentFilterType);
        }
    }

    private boolean mCanShowDialog = true;

    @Override
    public void reload(boolean bOnlyAudio) {
        reload(bOnlyAudio, false, null);
    }

    @Override
    public void start() {
        // Log.e("start", "startplayer");
        if (mVirtualVideoView == null) {
            return;
        }
        mVirtualVideoView.start();

        mIvVideoPlayState.setImageResource(R.drawable.btn_pause);

        int checkedId = 0;
        checkedId = mEditorMenuGroups.getCheckedRadioButtonId();

        if (checkedId == R.id.rb_word) {
            mSubtitleHandler.setImage(R.drawable.edit_music_pause);
        } else if (checkedId == R.id.rb_special) {
            mSpecialHandler.setImage(R.drawable.edit_music_pause);
        } else if (mPlayStatusShowing) {
            ViewUtils.fadeOut(this, mIvVideoPlayState);
        }
    }

    @Override
    public void pause() {

        if (mVirtualVideoView.isPlaying()) {
            mVirtualVideoView.pause();
        }
        int checkedId = 0;

        checkedId = mEditorMenuGroups.getCheckedRadioButtonId();

        if (checkedId == R.id.rb_word) {
            mSubtitleHandler.setImage(R.drawable.edit_music_play);
        } else if (checkedId == R.id.rb_special) {
            mSpecialHandler.setImage(R.drawable.edit_music_play);
        } else {
            mIvVideoPlayState.clearAnimation();
            mIvVideoPlayState.setImageResource(R.drawable.btn_play);
            if (mPlayStatusShowing) {
                mIvVideoPlayState.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void seekTo(int msec) {
        mVirtualVideoView.seekTo(Utils.ms2s(msec));
        mSbPlayControl.setProgress(msec);
        mProgressView.setProgress(msec);
        mTvCurTime.setText(getFormatTime(msec));
    }

    /**
     * @return
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

        if (mEditorMenuGroups.getCheckedRadioButtonId() == R.id.rb_word) {
            mSubtitleHandler.setImage(R.drawable.edit_music_play);
        }
        if (mEditorMenuGroups.getCheckedRadioButtonId() == R.id.rb_special) {
            mSpecialHandler.setImage(R.drawable.edit_music_play);
        } else {
            mIvVideoPlayState.clearAnimation();
            mIvVideoPlayState.setImageResource(R.drawable.btn_play);
            if (mPlayStatusShowing) {
                mIvVideoPlayState.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public boolean isPlaying() {
        return mVirtualVideoView != null && mVirtualVideoView.isPlaying();
    }

    @Override
    public void changeFilterType(int nFilterType) {
        if (mVirtualVideoView != null) {
            if (!mVirtualVideoView.isPlaying()) {
                start();
            }
            if (mFiterFragmentEx != null) {
                mCurrentFilterType = nFilterType;
            }
            mVirtualVideoView.setFilterType(nFilterType);
        }

    }

    @Override
    public int getCurrentFilterType() {
        return mCurrentFilterType;
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

    @Override
    public List<MediaObject> getEditingMediaObjectsWithTransition() {
        return mEditingMediaObjects;
    }

    private int getEditingMediasDuration() {
        int mDuration = 0;
        for (int i = 0; i < mEditingMediaObjects.size(); i++) {
            mDuration += Utils.s2ms(mEditingMediaObjects.get(i).getDuration());
        }
        return mDuration;
    }

    /**
     * 字幕、特效导出按钮
     *
     * @param isSub
     */
    private void onResultWord(boolean isSub) {
        mCanAutoPlay = false;
        stop();
        SysAlertDialog.showLoadingDialog(this, getString(R.string.isloading),
                false, null).show();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (isSub) {
            mSubtitleHandler.onExport(mVirtualVideoView.getVideoWidth(),
                    mVirtualVideoView.getVideoHeight(), new IExportSub() {

                        @Override
                        public void onSub(
                                ArrayList<SubtitleObject> effects) {
                            TempVideoParams.getInstance().setSubEffects(effects);
                            mHandler.sendEmptyMessage(RESULT_STYLE);
                        }

                    });

        } else {
            mSpecialHandler.onExport(mVirtualVideoView.getVideoWidth(),
                    mVirtualVideoView.getVideoHeight(), new IExportSpecial() {
                        @Override
                        public void onSpecial(ArrayList<SpecialInfo> infos) {
                            TempVideoParams.getInstance().setSpEffects(infos);
                            mHandler.sendEmptyMessage(RESULT_STYLE);
                        }
                    });
        }

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

        if (mSubtitleHandler != null && mSubtitleHandler.isEditing()) {
            int re = mSubtitleHandler.onSubBackPressed();
            if (re == 1) {
                iSubHandler.onBackPressed();
            }
            return;
        }
        if (mSpecialHandler != null && mSpecialHandler.isEditing()) {
            int re = mSpecialHandler.onSubBackPressed();
            if (re == 1) {
                iSpecialListener.onBackPressed();
            }
            return;
        }

        if (null == mEditorMenuGroups) {
            return;
        }
        int checkId = mEditorMenuGroups.getCheckedRadioButtonId();


        if (checkId == R.id.rb_filter) {
            if (mUIConfig.filterLayoutTpye != UIConfiguration.FILTER_LAYOUT_2) {
                returnToMenuLastSelection();
                mVirtualVideoView.setFilterType(mCurrentFilterType);
                mFilterFragment.resetFliterItem(mCurrentFilterType);
                return;
            }
        }


        if (mIsInAudioFragment) {
            if (mAudioFragment != null && mAudioFragment.hasChanged()) {
                onCreateDialogAudio();
            } else {
                mAudioFragment.resetAlreadyRecordAudioObject();
                mIsInAudioFragment = false;
                mAudioFragment.setShowFactor(false);
                findViewById(R.id.llAudioFactor).setVisibility(View.GONE);
                returnToMenuLastSelection();
            }
            return;
        }
        if (mUIConfig.isEnableWizard() && !mUIConfig.isHidePartEdit()) {
            backToPartEdit(false);
            return;
        }
        TempVideoParams.getInstance().setAspectRatio(
                TempVideoParams.mEditingVideoAspectRatio);

        onCreateDialog(DIALOG_RETURN_ID).show();
    }

    private void backToPartEdit(boolean forFinish) {
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
        Intent i = new Intent();
//        ArrayList<MediaObject> arrTrans = new ArrayList<MediaObject>();
//        for (MediaObject mo : mEditingMediaObjects) {
//            if (mo == null) {
//                continue;
//            }
//            if (mo instanceof Transition) {
//                arrTrans.add(mo);
//            }
//            mo.resetSortWeight();
//        }
//        for (MediaObject mo : arrTrans) {
//            mEditingMediaObjects.remove(mo);
//        }
        i.putExtra(IntentConstants.EXTRA_MEDIA_LIST, mEditingMediaObjects);
        i.putExtra(IntentConstants.EDIT_PROPORTION_STATUS, mProportionStatus);
        i.putExtra(IntentConstants.EXTRA_MEDIA_PROPORTION, mCurProportion);
        i.putExtra(IntentConstants.ALL_MEDIA_MUTE, mMediaMute);
        setResult(RESULT_OK, i);
        overridePendingTransition(0, 0);
        finish();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private int getHasVirtualKey() {
        int dpi = 0;
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        Class c;
        try {
            c = Class.forName("android.view.Display");
            Method method = c.getMethod("getRealMetrics", DisplayMetrics.class);
            method.invoke(display, dm);
            dpi = dm.heightPixels;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dpi;
    }

    /**
     * 初始化界面布局按钮的功能事件
     */
    @SuppressWarnings("deprecation")
    private void initView() {
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Point pntDisplay = new Point();
        wm.getDefaultDisplay().getSize(pntDisplay);
        int y = getHasVirtualKey();
        if (y == 0) {
            y = pntDisplay.y;
        }
        mScreenRatio = (float) pntDisplay.x / y;

        mPreviewLayout = (PreviewFrameLayout) findViewById(R.id.rlPreviewLayout);


        mTvTitle = (TextView) findViewById(R.id.tvTitle);

        mTvTitle.setText(R.string.priview_title);
        mEditorMenuGroups = (RadioGroup) findViewById(R.id.edit_groups);
        findViewById(R.id.rb_theme).setOnClickListener(
                onVerVideoMenuListener);
        findViewById(R.id.rb_music).setOnClickListener(
                onVerVideoMenuListener);
        findViewById(R.id.rb_audio).setOnClickListener(
                onVerVideoMenuListener);
        findViewById(R.id.rb_word).setOnClickListener(
                onVerVideoMenuListener);
        findViewById(R.id.rb_filter).setOnClickListener(
                onVerVideoMenuListener);
        findViewById(R.id.rb_special).setOnClickListener(
                onVerVideoMenuListener);
        findViewById(R.id.rb_partedit).setOnClickListener(
                onVerVideoMenuListener);
        findViewById(R.id.rb_mv).setOnClickListener(
                onVerVideoMenuListener);

        int rbCount = 7;

        if (!mUIConfig.enableMV) {
            setViewVisibility(R.id.rb_mv, false);
            rbCount -= 1;
        }
        if (mUIConfig.isHideDubbing()) {
            mHideMusicFragmentAudioBtn = true;
            setViewVisibility(R.id.rb_audio, false);
            setViewVisibility(R.id.btnVoice2, false);
            rbCount -= 1;
        } else {
            if (mUIConfig.voiceLayoutTpye == UIConfiguration.VOICE_LAYOUT_2) {
                setViewVisibility(R.id.rb_audio, false);
                rbCount -= 1;
                mHideMusicFragmentAudioBtn = false;
            } else {
                mHideMusicFragmentAudioBtn = true;
                setViewVisibility(R.id.rb_audio, true);
                setViewVisibility(R.id.btnVoice2, false);
            }
        }

        if (mUIConfig.isHideSoundTrack()) {
            setViewVisibility(R.id.rb_music, false);
            View llEditorGroups = findViewById(R.id.llEditorGroups);
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) llEditorGroups
                    .getLayoutParams();
            layoutParams.height = CoreUtils.dpToPixel(75);
            llEditorGroups.setLayoutParams(layoutParams);
            rbCount -= 1;
        }
        if (mUIConfig.isHideTitling()) {
            setViewVisibility(R.id.rb_word, false);
            rbCount -= 1;
        }
        if (mUIConfig.isHideFilter()) {
            setViewVisibility(R.id.rb_filter, false);
            rbCount -= 1;
        }
        if (mUIConfig.isHideSpecialEffects()) {
            setViewVisibility(R.id.rb_special, false);
            rbCount -= 1;
        }

        findViewById(R.id.rb_word).measure(0, 0);

        int rbWidth = findViewById(R.id.rb_word).getMeasuredWidth();
        int padding = 0;

        if (mUIConfig.isHidePartEdit() || mUIConfig.isEnableWizard()) {
            setViewVisibility(R.id.rb_partedit, false);
            rbCount -= 1;
            padding = (pntDisplay.x
                    - (rbWidth + CoreUtils.dpToPixel(15)) * rbCount - CoreUtils
                    .dpToPixel(15)) / (rbCount + 1);
//            findViewById(R.id.rb_filter1).setPadding(padding, 0, 0, 0);
            findViewById(R.id.rb_word).setPadding(padding, 0, 0, 0);
            findViewById(R.id.rb_filter).setPadding(padding, 0, 0, 0);
            findViewById(R.id.rb_special).setPadding(padding, 0, 0, 0);
            findViewById(R.id.rb_music).setPadding(padding, 0, 0, 0);
            findViewById(R.id.rb_audio).setPadding(padding, 0, 0, 0);
            findViewById(R.id.rb_mv).setPadding(padding, 0, 0, 0);
        } else if (rbCount < 6) {
            findViewById(R.id.rb_partedit).measure(0, 0);
            int rbPartEditWidth = findViewById(R.id.rb_partedit)
                    .getMeasuredWidth();
            padding = (pntDisplay.x - (rbCount - 1) * rbWidth
                    - rbPartEditWidth - (rbCount + 1)
                    * CoreUtils.dpToPixel(15))
                    / (rbCount + 1);
            findViewById(R.id.rb_word).setPadding(padding, 0, 0, 0);
            findViewById(R.id.rb_music).setPadding(padding, 0, 0, 0);
            findViewById(R.id.rb_audio).setPadding(padding, 0, 0, 0);
            findViewById(R.id.rb_filter).setPadding(padding, 0, 0, 0);
            findViewById(R.id.rb_special).setPadding(padding, 0, 0, 0);
            findViewById(R.id.rb_partedit).setPadding(padding, 0, 0, 0);
            findViewById(R.id.rb_mv).setPadding(padding, 0, 0, 0);
        }


        mRlTitleBar = (RelativeLayout) findViewById(R.id.titlebar_layout);

        mBtnLeft = (ExtButton) findViewById(R.id.btnLeft);
        mBtnLeft.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.public_menu_cancel, 0, 0, 0);
        mBtnLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        mBtnLeft.setPadding(25, 0, 0, 0);
        mBtnRight = (ExtButton) findViewById(R.id.btnRight);
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

        mPreviewLayout.setAspectRatio(AppConfiguration.ASPECTRATIO);
        mRlPlayerBottomMenu = (RotateRelativeLayout) findViewById(R.id.rlPlayerBottomMenu);

        mTvCurTime = (TextView) findViewById(R.id.tvCurTime);

        mTvTotalTime = (TextView) findViewById(R.id.tvTotalTime);
        mTvTotalTime.measure(0, 0);
        int width = mTvTotalTime.getMeasuredWidth();
        mTvCurTime.setWidth(width + CoreUtils.dpToPixel(5));
        mIvFullScreen = (ImageView) findViewById(R.id.ivFullScreen);
        mIvFullScreen.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mRlTitleBar.getVisibility() == View.VISIBLE) {
                    fullScreen(true);
                } else {
                    fullScreen(false);
                }
            }
        });


        mPflVideoPreview = (PreviewFrameLayout) findViewById(R.id.rlPreview);

        mLinearWords = (FrameLayout) findViewById(R.id.linear_words);
        //musicTrackLayout = (RelativeLayout) findViewById(R.id.rl_music_soundtrack_layout);

        mProgressView = (ProgressView) findViewById(R.id.progressView);
        mProgressView.setScroll(true);
        mProgressView.setListener(onProgressViewListener);
        mProgressView.setListener(onEditTitleListener);

        resetTitlebar();
        mIvVideoPlayState = (ImageView) findViewById(R.id.ivPlayerState);

        mSbPlayControl = (RdSeekBar) findViewById(R.id.sbEditor);
        mSbPlayControl.setOnSeekBarChangeListener(onSeekbarListener);
        mSbPlayControl.setMax(100);

        mLastPlayPostion = -1;

        mVirtualVideo = new VirtualVideo();

        mVirtualVideoView = (VirtualVideoView) findViewById(R.id.epvPreview);
        mVirtualVideoView.setOnPlaybackListener(mPlayViewListener);
        mVirtualVideoView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!mSubtitleHandler.isEditing() && !mSpecialHandler.isEditing()
                        && mPlayStatusShowing) {
                    onEditorPreivewClick();
                }
            }
        });
        mVirtualVideoView.setOnInfoListener(new VirtualVideo.OnInfoListener() {

            @Override
            public boolean onInfo(int what, int extra,
                                  Object obj) {
                if (what == VirtualVideo.INFO_WHAT_PLAYBACK_PREPARING) {
                    SysAlertDialog.showLoadingDialog(VideoEditActivity.this,
                            getString(R.string.isloading), false, null);
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
    }

    /*
　* 音乐类监听回调
　*/
    private IMusicListener mMusicListener = new IMusicListener() {

        @Override
        public void onVoiceChanged(boolean isChecked) {
            if (isChecked) {
                mMediaMute = false;
                for (MediaObject media : mEditingMediaObjects) {
                    if (media.getMediaType() == MediaType.MEDIA_VIDEO_TYPE) {
                        media.setAudioMute(mMediaMute);
                    }
                }
            } else {
                mMediaMute = true;
                for (MediaObject media : mEditingMediaObjects) {
                    if (media.getMediaType() == MediaType.MEDIA_VIDEO_TYPE) {
                        media.setAudioMute(mMediaMute);
                    }
                }
            }
            reload(false);
            start();
        }

        @Override
        public void onVoiceClick(View v) {
            onVerVideoMenuListener.onClick(v);
        }

    };

    private void onEdit() {
        stop();
        Intent i = new Intent(VideoEditActivity.this, EditPreviewActivity.class);
        ArrayList<MediaObject> arrTrans = new ArrayList<MediaObject>();
        for (MediaObject mo : arrTrans) {
            mEditingMediaObjects.remove(mo);
        }
        i.putExtra(IntentConstants.INTENT_EXTRA_SCENE, mSceneList);
        i.putExtra(IntentConstants.EXTRA_MEDIA_LIST, mEditingMediaObjects);
        i.putExtra(IntentConstants.EXTRA_MEDIA_PROPORTION, mCurProportion);
        i.putExtra(IntentConstants.EDIT_PROPORTION_STATUS, mProportionStatus);
        i.putExtra(IntentConstants.ALL_MEDIA_MUTE, mMediaMute);
        startActivityForResult(i, REQUEST_FOR_EDIT);
        overridePendingTransition(0, 0);
    }


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
            findViewById(R.id.rlEditorMenuAndSubLayout)
                    .setVisibility(View.GONE);
            mTimerCount = 0;
            mIsFullScreen = true;
            if (mVirtualVideoView.getVideoWidth() > mVirtualVideoView.getVideoHeight()) {
                mPreviewLayout.setAspectRatio(1 / mScreenRatio);
                if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                }
            } else {
                mPreviewLayout.setAspectRatio(mScreenRatio);
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }

            mIvFullScreen
                    .setBackgroundResource(R.drawable.edit_intercept_revert);
        } else {
            mRlTitleBar.setVisibility(View.VISIBLE);
            if (CoreUtils.hasIceCreamSandwich()) {
                mContent.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            }

            findViewById(R.id.rlEditorMenuAndSubLayout).setVisibility(
                    View.VISIBLE);
            mTimerCount = 0;
            mIsFullScreen = false;
            mPreviewLayout.setAspectRatio(AppConfiguration.ASPECTRATIO);

            if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
            mIvFullScreen
                    .setBackgroundResource(R.drawable.edit_intercept_fullscreen);

        }

    }

    private FilterFragmentEx mFiterFragmentEx;


    private void notifyCurrentPosition(int positionMs) {
        for (int nTmp = 0; nTmp < mSaEditorPostionListener.size(); nTmp++) {
            mSaEditorPostionListener.valueAt(nTmp).onEditorGetPosition(
                    positionMs, Utils.s2ms(mVirtualVideoView.getDuration()));
        }
    }

    /**
     * 播放器Listener
     */
    private VirtualVideoView.VideoViewListener mPlayViewListener = new VirtualVideoView.VideoViewListener() {
        private boolean isCheckFilter = true;
        private boolean isCheckDefaultMenu = true; // 选择功能菜单默认项

        @Override
        public void onPlayerPrepared() {
            int duration = Utils.s2ms(mVirtualVideoView.getDuration());
            if (!mIsFastPreview) {
                TempVideoParams.getInstance().setEditingVideoDuration(duration);
            }
            if (isCheckFilter
                    && (mCurrentFilterType != 0)) {
                mVirtualVideoView.setFilterType(mCurrentFilterType);
                isCheckFilter = false;
            }

            mProgressView.setDuration(duration);
            SysAlertDialog.cancelLoadingDialog();
            setText(R.id.tvEditorDuration,
                    getFormatTime(duration));
            mTvTotalTime.setText(getFormatTime(duration));
            mSbPlayControl.setMax(duration);
            int len = mSaEditorPostionListener.size();
            for (int nTmp = 0; nTmp < len; nTmp++) {
                mSaEditorPostionListener.valueAt(nTmp).onEditorPrepred();
            }
            if (mUpdateAspectPending) {
                updatePreviewFrameAspect(mVirtualVideoView.getVideoWidth(),
                        mVirtualVideoView.getVideoHeight());
                mUpdateAspectPending = false;
                updateMenuStatus(isCheckDefaultMenu);
                isCheckDefaultMenu = false;
            }
            notifyCurrentPosition(Utils.s2ms(mVirtualVideoView.getCurrentPosition()));
        }

        @Override
        public boolean onPlayerError(int what, int extra) {
            SysAlertDialog.cancelLoadingDialog();
            com.rd.veuisdk.utils.Utils.autoToastNomal(VideoEditActivity.this,
                    R.string.preview_error);
            return false;
        }

        @Override
        public void onPlayerCompletion() {
            if (!mSubtitleHandler.isEditing() && !mSpecialHandler.isEditing()
                    && !(mFragCurrent instanceof AudioFragment)) {
                mIvVideoPlayState.setImageResource(R.drawable.btn_play);
                if (mPlayStatusShowing) {
                    mIvVideoPlayState.setVisibility(View.VISIBLE);
                }
                //seekto 0
                mProgressView.setProgress(0);
                mSbPlayControl.setProgress(0);
                notifyCurrentPosition(0);
                mTvCurTime.setText(getFormatTime(0));
                mLastPlayPostion = 0;
                mVirtualVideoView.seekTo(0);
            }
            for (int nTmp = 0; nTmp < mSaEditorPostionListener.size(); nTmp++) {
                mSaEditorPostionListener.valueAt(nTmp)
                        .onEditorPreviewComplete();
            }
        }

        @Override
        public void onGetCurrentPosition(float position) {
            int positionMs = Utils.s2ms(position);
            mProgressView.setProgress(positionMs);
            mSbPlayControl.setProgress(positionMs);
            mLastPlayPostion = position;
            mTvCurTime.setText(getFormatTime(positionMs));
            notifyCurrentPosition(positionMs);
        }
    };

    private ProgressView.onProgressListener onProgressViewListener = new ProgressView.onProgressListener() {

        boolean isplaying = false;

        @Override
        public void onStart() {
            isplaying = isPlaying();
            if (isplaying) {
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
            if (isplaying && !isThemeMenuItem()) {
                // 当前功能项为主题时，不需要恢复播放
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

    private ProgressView.onEditTitleListener onEditTitleListener = new ProgressView.onEditTitleListener() {

        @Override
        public void onEditTitleClick(View view, Rect rectTitleRegion) {
//            ThemeTitlesInfo.TitleInfo ti = themeFragment
//                    .getEditingTitle(getCurrentPosition());
//            if (ti != null) {
//                int[] arrLocation = new int[2];
//                view.getLocationOnScreen(arrLocation);
//                int nTitleBarHeight = findViewById(R.id.titlebar_layout)
//                        .getMeasuredHeight();
//                rectTitleRegion.offset(arrLocation[0], arrLocation[1]
//                        - nTitleBarHeight);
//                ThemeTitleEditorFragment frag = ThemeTitleEditorFragment.show(
//                        getSupportFragmentManager(), rectTitleRegion);
//
//                frag.setTitleTextSizeRatio(((float) view.getWidth() / MVUtils.DEFAULT_VIDEO_WIDTH));
//                frag.bindTitleInfo(ti);
//
//                setSureCancelTitleBar();
//            }
        }
    };

    /**
     * 获取当前功能项是否为主题
     *
     * @return
     */
    protected boolean isThemeMenuItem() {
        return mEditorMenuGroups.getCheckedRadioButtonId() == R.id.rb_theme;
    }

    private void onEditorPreivewClick() {

        if (mRlPlayerBottomMenu.getVisibility() == View.VISIBLE) {
            if (isPlaying()) {
                pause();
                if (isThemeMenuItem()) {
                    mPlayViewListener.onGetCurrentPosition(
                            Utils.ms2s(getCurrentPosition()));
                }
            } else {
                start();
            }
        } else {
            if (mTimerTask == null) {
                mTimerTask = new TimerTask() {

                    @Override
                    public void run() {
                        mTimerCount += 1;
                        if (mTimerCount > 8) {
                            mHandler.sendEmptyMessage(HIDE_BOTTOM_VIEW);
                        }
                    }
                };
            }
            if (mTimer == null) {
                mTimer = new Timer();
                mTimer.schedule(mTimerTask, 0, 500);
            }

        }
        mTimerCount = 0;
        mRlPlayerBottomMenu.setVisibility(View.VISIBLE);

    }

    /**
     * 更新菜单项显示
     */
    protected void updateMenuStatus(boolean bCheckDefaultMenu) {
        // FIXME:现只支持横屏时才启用主题
        // if (TempVideoParams.isLandscapeVideo()) {
        // setViewVisibility(R.id.rb_theme, true);
        // if (bCheckDefaultMenu) {
        // mEditorMenuGroups.check(R.id.rb_theme);
        // }
        // } else {
        setViewVisibility(R.id.rb_theme, false);
        if (bCheckDefaultMenu) {
            if (mUIConfig.enableMV) {
                mLastEditorMenuCheckId = R.id.rb_mv;
            } else if (!mUIConfig.isHideSoundTrack()) {
                mLastEditorMenuCheckId = R.id.rb_music;
            } else {
                mLastEditorMenuCheckId = -1;
            }
            mEditorMenuGroups.check(mLastEditorMenuCheckId);
            if (mLastEditorMenuCheckId == -1) {
                onCheckItem(UNCHECKED_ID);
            } else {
                onCheckItem(mLastEditorMenuCheckId);
            }
        }

        // }
    }


    /**
     * 返回到功能菜单最后选择项
     */

    private void returnToMenuLastSelection() {
        mEditorMenuGroups.check(mLastEditorMenuCheckId);
        if (mLastEditorMenuCheckId == -1) {
            onCheckItem(UNCHECKED_ID);
        } else {
            onCheckItem(mLastEditorMenuCheckId);
        }
    }

    private boolean mCanAutoPlay = true;

    private void onVerVideoMusic(boolean bOldCanExport, String menu) {
        resetTitlebar();
        mProgressView.setScroll(true);
        menu = getString(R.string.music);
        mTvTitle.setText(menu);

        if (null == mMusicFragmentEx) {
            mMusicFragmentEx = new MusicFragmentEx();
            mMusicFragmentEx.init(mExportConfig.trailerDuration, mUIConfig.musicUrl, mUIConfig.voiceLayoutTpye,
                    mMusicListener, mUIConfig.cloudMusicUrl, mUIConfig.enableLocalMusic, mUIConfig.isHideDubbing());

        }
        changeToFragment(mMusicFragmentEx, true);

        boolean isplaying = isPlaying();

        if (!isplaying && mCanAutoPlay) {
            start();
        }
        if (!bOldCanExport && mCanAutoPlay) {
            seekTo(0);
        }
    }

    private void onVerVideoTheme(boolean bOldCanExport, String menu) {
        resetTitlebar();
        if (!isPlaying()) {
            start();
        }
        if (!bOldCanExport) {
            seekTo(0);
        }
        mTvTitle.setText(menu);
    }

    /**
     * 切换到MV
     *
     * @param bOldCanExport
     * @param menu
     */
    private void onVerVideoMV(boolean bOldCanExport, String menu) {
        menu = getString(R.string.mv);
        resetTitlebar();
        if (mMVFragment == null) {
            mMVFragment = new MVFragment(mUIConfig.mvUrl, true);
        }
        changeToFragment(mMVFragment, true);
        if (!isPlaying()) {
            start();
        }
        if (!bOldCanExport) {
            seekTo(0);
        }
        mTvTitle.setText(getString(R.string.mv));
    }

    private OnClickListener onVerVideoMenuListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            int checkedId = v.getId();
            onCheckItem(checkedId);
        }
    };

    private final int UNCHECKED_ID = 15;
    private boolean mHideMusicFragmentAudioBtn = true;

    private void onCheckItem(int checkedId) {
        boolean isChecked = false;
        boolean bOldCanExport = mCanExport;
        if (checkedId != -1) {
            if (checkedId != UNCHECKED_ID) {
                View taget = findViewById(checkedId);
                RadioButton rb = null;
                if (null != taget && taget instanceof RadioButton) {
                    rb = ((RadioButton) taget);
                }

                if (rb != null) {
                    isChecked = rb.isChecked();
                }
            }
        }

        mCanExport = true;
        mIsInAudioFragment = false;
        mPlayStatusShowing = true;
        mProgressView.setScroll(true);
        mProgressView.setVisibility(View.VISIBLE);
        // setViewVisibility(R.id.rlEditorControl, true);
        mBtnRight.setVisibility(View.VISIBLE);
        String menu = "kong";
        if (checkedId == UNCHECKED_ID) {
            menu = getString(R.string.priview_title);
            if (mMenuUncheckedFragment == null) {
                mMenuUncheckedFragment = new MenuUncheckedFragment();
            }
            changeToFragment(mMenuUncheckedFragment, true);
            resetTitlebar();
            start();
        } else if (checkedId == R.id.rb_music) {
            onVerVideoMusic(bOldCanExport, menu);
        } else if (checkedId == R.id.rb_theme) {
            onVerVideoTheme(bOldCanExport, menu);
        } else if (checkedId == R.id.rb_mv) {
            onVerVideoMV(bOldCanExport, menu);
        } else {
            setSureCancelTitleBar();
            if (checkedId == R.id.rb_audio || checkedId == R.id.btnVoice2) {

                menu = getString(R.string.audio);
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                // setViewVisibility(R.id.rlEditorControl, false);
                mRlPlayerBottomMenu.setVisibility(View.INVISIBLE);
                mPlayStatusShowing = false;
                mIsInAudioFragment = true;
                stop();
                if (mAudioFragment == null) {
                    mAudioFragment = new AudioFragment();
                    LinearLayout llAudioFactor = (LinearLayout) findViewById(R.id.llAudioFactor);
                    mAudioFragment.setSeekBar(llAudioFactor);
                }
                mAudioFragment.setShowFactor(true);
                changeToFragment(mAudioFragment, false);
                mTvTitle.setText(menu);
                mCanExport = false;
                checkedId = -1;
            } else if (checkedId == R.id.rb_word) {
                mVirtualVideoView.setAutoRepeat(false); // 字幕不需要自动重播
                menu = getString(R.string.subtitle);
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                mPlayStatusShowing = false;
                stop();
                mRlPlayerBottomMenu.setVisibility(View.INVISIBLE);
                mIvVideoPlayState.setVisibility(View.GONE);
                onAnim(new IAnimCallBack() {
                    @Override
                    public void onAnimationEnd() {
                        mSubtitleHandler.init((FrameLayout) findViewById(R.id.linear_words));
                    }
                });
                mTvTitle.setText(R.string.add_subtitle);
                mCanExport = false;
                checkedId = -1;
                mIsEditorMenuEnableAnim = false;
            } else if (checkedId == R.id.rb_filter) {
                menu = getString(R.string.filter);
                if (mUIConfig.filterLayoutTpye == UIConfiguration.FILTER_LAYOUT_2) {
                    if (null == mFiterFragmentEx) {
                        mFiterFragmentEx = new FilterFragmentEx();
                    }
                    changeToFragment(mFiterFragmentEx, true);
                    resetTitlebar();
                } else {
                    boolean hideSoundTrack = mUIConfig.isHideSoundTrack();
                    if (mFilterFragment == null) {
                        mFilterFragment = new FilterFragment(hideSoundTrack);
                    }
                    changeToFragment(mFilterFragment, false);
                    mCanExport = false;
                    checkedId = -1;
                }
                if (!isPlaying()) {
                    start();
                }
                if (!bOldCanExport) {
                    seekTo(0);
                }
                mTvTitle.setText(menu);


            } else if (checkedId == R.id.rb_special) { // 特效
                mVirtualVideoView.setAutoRepeat(false); // 特效不需要自动重播
                menu = getString(R.string.special);
                mPlayStatusShowing = false;
                mRlPlayerBottomMenu.setVisibility(View.INVISIBLE);
                stop();
                mIvVideoPlayState.setVisibility(View.GONE);
                onAnim(new IAnimCallBack() {
                    @Override
                    public void onAnimationEnd() {
                        mSpecialHandler.init();
                    }
                });

                mTvTitle.setText(R.string.add_special);
                mCanExport = false;
                checkedId = -1;
                mIsEditorMenuEnableAnim = false;
            } else if (checkedId == R.id.rb_partedit) {
                resetTitlebar();
                stop();
                Intent i = new Intent(VideoEditActivity.this,
                        EditPreviewActivity.class);
                i.putExtra(IntentConstants.INTENT_EXTRA_SCENE, mSceneList);
                i.putExtra(IntentConstants.EXTRA_MEDIA_PROPORTION, mCurProportion);
                i.putExtra(IntentConstants.EDIT_PROPORTION_STATUS, mProportionStatus);
                i.putExtra(IntentConstants.ALL_MEDIA_MUTE, mMediaMute);
                startActivityForResult(i, REQUEST_FOR_EDIT);
                overridePendingTransition(0, 0);
                checkedId = -1;
            } else {
                menu = getString(R.string.other_menu);
            }
        }
        if (checkedId != -1) {
//            mLastEditorMenuCheckId = isChecked ? checkedId : R.id.rb_music;
            mIsEditorMenuEnableAnim = true;
        }
        if (!mPlayStatusShowing) {
            mIvVideoPlayState.setVisibility(View.GONE);
            mProgressView.setScroll(false);
            mProgressView.setVisibility(View.GONE);
        }

    }


    /**
     * 同步fragment切换的动画(字幕、特效)
     *
     * @param iback
     */
    private void onAnim(final IAnimCallBack iback) {
        Animation aniSlideOut = AnimationUtils.loadAnimation(this,
                R.anim.editor_preview_slide_out);
        aniSlideOut
                .setAnimationListener(new Animation.AnimationListener() {

                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        animation.setAnimationListener(null);
                        findViewById(R.id.edit_video_layout).clearAnimation();
                        setViewVisibility(R.id.rlEditorMenuAndSubLayout, true);
                        setViewVisibility(R.id.edit_video_layout, false);
                        if (null != iback) {
                            iback.onAnimationEnd();
                        }

                    }
                });
        findViewById(R.id.edit_video_layout).startAnimation(
                aniSlideOut);

    }

    interface IAnimCallBack {
        void onAnimationEnd();
    }


    /**
     * 标题栏还原到默认状态
     */
    public void resetTitlebar() {

        mBtnLeft.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.edit_back_button, 0, 0, 0);
        mBtnRight.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        mBtnRight.setText(R.string.export);

    }

    protected void setSureCancelTitleBar() {
        mBtnRight.setText("");
        mBtnLeft.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.public_menu_cancel, 0, 0, 0);
        mBtnRight.setCompoundDrawablesWithIntrinsicBounds(0, 0,
                R.drawable.public_menu_sure, 0);
    }

    /**
     * 切换fragment
     *
     * @param fragment
     * @param bEditorGroupsVisible
     */
    private void changeToFragment(BaseFragment fragment,
                                  boolean bEditorGroupsVisible) {
        changeToFragment(fragment, mIsEditorMenuEnableAnim,
                bEditorGroupsVisible);
    }

    private void setFragmentCurrent(BaseFragment fragment) {
        this.mFragCurrent = fragment;
        if (mUIConfig.enableAutoRepeat && fragment != null && !(fragment instanceof AudioFragment)) {
            mVirtualVideoView.setAutoRepeat(true);
        } else {
            mVirtualVideoView.setAutoRepeat(false);
        }
    }

    private BaseFragment mFragCurrent;

    /**
     * 切换fragment
     *
     * @param fragment             需要切换的fragment
     * @param enableAnimation      确定是否使用动画
     * @param bEditorGroupsVisible 编辑功能组设置是否显示
     */
    private void changeToFragment(final BaseFragment fragment,
                                  boolean enableAnimation, final boolean bEditorGroupsVisible) {
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
                setViewVisibility(R.id.llEditorGroups, bEditorGroupsVisible);
            } else {
                Animation aniSlideOut = AnimationUtils.loadAnimation(this,
                        R.anim.editor_preview_slide_out);
                findViewById(R.id.rlEditorMenuAndSubLayout).startAnimation(
                        aniSlideOut);
                aniSlideOut
                        .setAnimationListener(new Animation.AnimationListener() {

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
                                Animation aniSlideIn = AnimationUtils
                                        .loadAnimation(getBaseContext(),
                                                R.anim.editor_preview_slide_in);
                                findViewById(R.id.rlEditorMenuAndSubLayout)
                                        .startAnimation(aniSlideIn);
                                setViewVisibility(R.id.llEditorGroups,
                                        bEditorGroupsVisible);
                                setFragmentCurrent(fragment);
                            }
                        });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    protected void updatePreviewFrameAspect(int m_nVideoWidth,
                                            int m_nVideoHeight) {
        mPflVideoPreview.setAspectRatio((float) (m_nVideoWidth + .0)
                / m_nVideoHeight);
        TempVideoParams.mEditingVideoAspectRatio = (double) m_nVideoWidth
                / m_nVideoHeight;
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
        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
            if (fromUser) {
                onProgressViewListener.onProgressing(progress);
                mTimerCount = 0;
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

    private ISubHandler iSubHandler = new ISubHandler() {

        @Override
        public void setTitle(int strResId) {
            mTvTitle.setText(strResId);

        }

        @Override
        public void onBackPressed() {
            onResultWord(true);
        }

        @Override
        public void onViewVisible(boolean mIsVisible) {
            mBtnRight.setVisibility(mIsVisible ? View.VISIBLE : View.INVISIBLE);
        }
    };
    /**
     * 特效回调
     */
    private ISpecailListener iSpecialListener = new ISpecailListener() {

        @Override
        public void onViewVisible(boolean mIsVisible) {

            mBtnRight.setVisibility(mIsVisible ? View.VISIBLE : View.INVISIBLE);

        }

        @Override
        public void onBackPressed() {
            onResultWord(false);

        }
    };


    /**
     * 初始化字幕特效
     */
    private void initSubtitleEffects() {
        View rootView = findViewById(R.id.roottreeview);
        mSubtitleHandler = new SubtitleHandler(rootView, iSubHandler,
                VideoEditActivity.this, mLinearWords);

        mSpecialHandler = new SpecialHandler(
                findViewById(R.id.edit_video_special_layout),
                VideoEditActivity.this, mLinearWords, iSpecialListener);
        rootView.post(new Runnable() {
            public void run() {
                mSubtitleHandler.initView(findViewById(R.id.edit_video_sub_layout));
            }
        });
    }

    private List<SubtitleObject> mTempSpecials = null;
    private List<SubtitleObject> mListSubtitles = null;

    private void loadAllMediaObjects(List<Scene> lstScenes) {
        lstScenes.addAll(mSceneList);

        if (!mSubtitleHandler.isEditing()) {
            mListSubtitles = TempVideoParams.getInstance().getSubEffects();
        }

        if (!mSpecialHandler.isEditing()) {
            mTempSpecials = new ArrayList<SubtitleObject>();
            List<SpecialInfo> lstspecials = TempVideoParams.getInstance()
                    .getSpEffects();
            if (lstspecials != null && lstspecials.size() > 0) {
                int splen = lstspecials.size();
                SpecialInfo sinfo;
                for (int i = 0; i < splen; i++) {
                    sinfo = lstspecials.get(i);
                    if (null != sinfo) {
                        ArrayList<SubtitleObject> titem = sinfo
                                .getList();
                        if (null != titem && titem.size() > 0) {
                            mTempSpecials.addAll(titem);
                        }
                    }
                }
            }
        }


//
//        if (uiConfig.enableTitlingOuter) {
//            if (mListSubtitles != null && mListSubtitles.size() > 0) {
//                lstMediaObjects.addAll(mListSubtitles);
//            }
//        }
//
//        if (uiConfig.enableSpecialeffectsOuter) {
//            if (mTempSpecials != null && mTempSpecials.size() > 0) {
//                lstMediaObjects.addAll(mTempSpecials);
//            }
//        }

//        if (null != mTempSpecials) {
//            mTempSpecials.clear();
//            mTempSpecials = null;
//        }


//        List<MediaObject> themes = null;
//        if (themeFragment != null
//                && (themes = themeFragment.getMediaObjects()) != null) {
//            lstMediaObjects.addAll(themes);
//        }

    }

    private boolean mHWCodecEnabled;

    /**
     * 保存配音
     */
    private void saveAudioObjects() {
        // 配乐2->配音
        if (null != mAudioFragment && mAudioFragment.isVisible()) {
            mAudioFragment.saveAudioData();
            mAudioFragment.setShowFactor(false);
            findViewById(R.id.llAudioFactor).setVisibility(View.GONE);
        }
    }

    /**
     * 响应确定与导出
     */
    protected void onRightButtonClick() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
//            ThemeTitleEditorFragment.dismiss(true);
            return;
        }

        if (!mCanExport) {
            // 当前选择功能项id
            int checkId = mEditorMenuGroups.getCheckedRadioButtonId();
            if (checkId == R.id.rb_music) {
                if (null != mAudioFragment) {
                    mAudioFragment.setShowFactor(false);
                }
                findViewById(R.id.llAudioFactor).setVisibility(
                        View.GONE);
            } else if (checkId == R.id.rb_audio) {
                if (null != mAudioFragment) {
                    mAudioFragment.setShowFactor(false);
                    findViewById(R.id.llAudioFactor).setVisibility(
                            View.GONE);
                }
            } else if (checkId == R.id.rb_word) {
                if (mSubtitleHandler.isEditing()) {
                    if (mSubtitleHandler.onSurebtn() == 1) {
                        mSubtitleHandler.onSubBackPressed();
                        iSubHandler.onBackPressed();
                    }
                    return;
                }
            } else if (checkId == R.id.rb_special) {
                if (mSpecialHandler.isEditing()) {
                    mSpecialHandler.onSure();
                    iSpecialListener.onBackPressed();
                    return;
                }
            } else if (checkId == R.id.rb_filter) {
//                mCurrentFilterType = FilterFragment.checkFilterId;
                if (null != mFiterFragmentEx) {
                    mCurrentFilterType = FilterFragmentEx.mCurrentFilterType;
                }
                if (null != mFilterFragment) {
                    mCurrentFilterType = FilterFragment.checkFilterId;
                }
            }
            returnToMenuLastSelection();
            return;
        }


        if (mExportDuration != 0 && mExportDuration < mVirtualVideoView.getDuration()) {
            onCreateDialog(DIALOG_EXPORT_ID);
            return;
        }
        export();
    }

    private void export() {
        if (null != mVirtualVideoSave) {
            Log.e(TAG, "is exporting...." + mStrSaveMp4FileName);
            return;
        }
        mVirtualVideoView.stop();
        mHWCodecEnabled = CoreUtils.hasJELLY_BEAN_MR2();
        exportVideo();
    }


    /**
     * 当前需要导出的mp4文件路径
     */
    protected String mStrSaveMp4FileName;
    /**
     * 自定义水印临保存路径
     */
    private String mStrCustomWatermarkTempPath;
    /**
     * 当前需要导出视频结尾图片路径
     */
    protected String mStrSaveVideoTrailerFileName;


    private float mExportDuration;
    private boolean mIsRemoveMVMusic = false;

    /**
     * 导出视频
     */
    private void exportVideo() {
        mVirtualVideoSave = new VirtualVideo();
        ArrayList<Scene> alReloadScenes = new ArrayList<Scene>();
        loadAllMediaObjects(alReloadScenes);

        for (Scene scene : alReloadScenes) {
            mVirtualVideoSave.addScene(scene);
        }
        //mv必须在字幕\特效之前加载
        if (null != mMVFragment) {
            mVirtualVideoSave.setMV(mMVFragment.getCurrentMVId());
            mVirtualVideoSave.removeMVMusic(mIsRemoveMVMusic);
        }

        mVirtualVideoSave.setEnableTitlingAndSpEffectOuter(mUIConfig.enableTitlingAndSpecialEffectOuter);
        if (!mSpecialHandler.isEditing() && null != mListSubtitles) {
            int len = mListSubtitles.size();
            for (int i = 0; i < len; i++) {
                SubtitleObject st = mListSubtitles.get(i);
                // Log.e("sub", st.getTimeLineStart() + "................" + st.getTimeLineEnd());
                mVirtualVideoSave.addSubtitle(st);
            }
        }
        if (!mSpecialHandler.isEditing() && null != mTempSpecials) {
            int len = mTempSpecials.size();
            for (int i = 0; i < len; i++) {
                SubtitleObject st = mTempSpecials.get(i);
                // Log.e("sp", st.getTimeLineStart() + "................" + st.getTimeLineEnd() + "..." + st.getShowRectF().toShortString() + ".." + st.getWidth() + "*" + st.getHeight() + ".." + st.getShowWidth() + "*" + st.getShowHeight());
                mVirtualVideoSave.addSubtitle(st);
            }
        }

        addMusic(mVirtualVideoSave);
        mVirtualVideoSave.updateMusic(mVirtualVideoView);

        if (mExportConfig.enableTextWatermark) {  // 自定义view水印
            mStrCustomWatermarkTempPath = PathUtils.getTempFileNameForSdcard(mSavePath, "png");
            TextWatermarkBuilder textWatermarkBuilder = new TextWatermarkBuilder(this, mStrCustomWatermarkTempPath);
            textWatermarkBuilder.setWatermarkContent(mExportConfig.textWatermarkContent);
            textWatermarkBuilder.setTextSize(mExportConfig.textWatermarkSize);
            textWatermarkBuilder.setTextColor(mExportConfig.textWatermarkColor);
            textWatermarkBuilder.setShowRect(mExportConfig.watermarkShowRectF);
            textWatermarkBuilder.setTextShadowColor(mExportConfig.textWatermarkShadowColor);
            mVirtualVideoSave.setWatermark(textWatermarkBuilder);
        } else if (mExportConfig.watermarkPath != null) {  //图片水印
            Watermark watermark = new Watermark(mExportConfig.watermarkPath);
            watermark.setShowRect(mExportConfig.watermarkShowRectF);
            mVirtualVideoSave.setWatermark(watermark);
        }

        if (mExportConfig.trailerPath != null) {
            Trailer trailer = new Trailer(mExportConfig.trailerPath, mExportConfig.trailerDuration, mExportConfig.trailerFadeDuration);
            mVirtualVideoSave.setTrailer(trailer);
        }

        mVirtualVideoSave.changeFilter(mCurrentFilterType);

        VideoConfig vc = new VideoConfig();
        vc.setVideoEncodingBitRate(SdkEntry.getSdkService().getExportConfig().getVideoBitratebps());
        vc.setVideoFrameRate(SdkEntry.getSdkService().getExportConfig().exportVideoFrameRate);
        vc.enableHWEncoder(mHWCodecEnabled);
        vc.enableHWDecoder(mHWCodecEnabled);
        vc.setAspectRatio(SdkEntry.getSdkService().getExportConfig().getVideoMaxWH(), mCurProportion);

        if (!TextUtils.isEmpty(mSavePath)) {
            File path = new File(mSavePath);
            PathUtils.checkPath(path);
            mStrSaveMp4FileName = PathUtils.getTempFileNameForSdcard(mSavePath,
                    "VIDEO", "mp4");
        } else {
            mStrSaveMp4FileName = PathUtils.getMp4FileNameForSdcard();
        }
        if (mExportDuration != 0) {
            mVirtualVideoSave.setExportDuration(mExportDuration);
        }
        mVirtualVideoSave.export(this, mStrSaveMp4FileName, vc, mExportListener);
    }

    private String saveBitmap(Bitmap bmp, Bitmap trailerBmp) {
        String tempPath = PathUtils
                .getTempFileNameForSdcard("Temp_bmp_", "png");

        if (trailerBmp == null) {
            return null;
        }

        Canvas canvas = new Canvas(bmp);
        Paint bmpPaint = new Paint();
        bmpPaint.setAntiAlias(true);
        bmpPaint.setFilterBitmap(true);

        float scale = (float) bmp.getHeight() / (2 * trailerBmp.getHeight());

        int left = (int) (bmp.getWidth() - scale * trailerBmp.getWidth()
                / (2 * scale));
        int top = (int) (bmp.getHeight() - scale * trailerBmp.getHeight()
                / (2 * scale));
        canvas.scale(scale, scale);
        canvas.drawBitmap(trailerBmp, left, top, bmpPaint);

        File file = new File(tempPath);
        if (file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        bmp.recycle();
        trailerBmp.recycle();
        bmp = null;
        return tempPath;
    }

    private ExportListener mExportListener = new ExportListener() {
        private HorizontalProgressDialog epdExport = null;
        private Dialog dialog = null;
        private boolean cancelExport = false;

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
                        false, true, new DialogInterface.OnCancelListener() {

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
                        dialog = SysAlertDialog.showAlertDialog(
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
                                            epdExport = null;
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

            if (null != mVirtualVideoSave) {
                mVirtualVideoSave = null;
            }
            mGotoBack = false;
            getWindow().clearFlags(
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            if (!VideoEditActivity.this.isFinishing()) {
                if (epdExport != null && epdExport.isShowing()) {
                    epdExport.dismiss();
                    epdExport = null;
                }
                if (dialog != null) {
                    dialog.dismiss();
                    dialog.cancel();
                    dialog = null;
                }
            }
            if (nResult >= VirtualVideo.RESULT_SUCCESS) {
                if (mIsFromCamera) {
                    SdkEntryHandler.getInstance().onExportRecorderEdit(
                            VideoEditActivity.this, mStrSaveMp4FileName);
                } else {
                    SdkEntryHandler.getInstance().onExport(
                            VideoEditActivity.this, mStrSaveMp4FileName);
                }
                if (mUIConfig.isEnableWizard()) {
                    backToPartEdit(true);
                } else {
                    Intent intent = new Intent();
                    intent.putExtra(SdkEntry.EDIT_RESULT,
                            mStrSaveMp4FileName);
                    setResult(RESULT_OK, intent);
                    VideoEditActivity.this.finish();
                }
            } else {
                new File(mStrSaveMp4FileName).delete();
                if (nResult != VirtualVideo.RESULT_EXPORT_CANCEL) {
                    if ((nResult == VirtualVideo.RESULT_CORE_ERROR_ENCODE_VIDEO ||
                            nResult == VirtualVideo.RESULT_CORE_ERROR_OPEN_VIDEO_ENCODER)
                            && mHWCodecEnabled) {
                        // FIXME:开启硬编后出现了编码错误，使用软编再试一次
                        mHWCodecEnabled = false;
                        exportVideo();
                        return;
                    } else {
                        String strMessage = getString(R.string.export_failed);
                        if (nResult == VirtualVideo.RESULT_CORE_ERROR_LOW_DISK) {
                            strMessage = getString(R.string.export_failed_no_free_space);
                        }
                        SysAlertDialog.showAutoHideDialog(
                                VideoEditActivity.this, null, strMessage,
                                Toast.LENGTH_SHORT);
                        FileLog.writeLog(strMessage + ",result:" + nResult);
                        Log.e(TAG, strMessage + ",result:" + nResult);
                    }
                }
                reload(false);
                if (!mIsPausing) {
                    start();
                }
            }
        }
    };

    private void resetEffects() {
        TempVideoParams.getInstance().setSubs(new ArrayList<WordInfo>());
        TempVideoParams.getInstance().setSubEffects(
                new ArrayList<SubtitleObject>());
        TempVideoParams.getInstance()
                .setSpEffects(new ArrayList<SpecialInfo>());
        TempVideoParams.getInstance().setSpecial(new ArrayList<WordInfo>());
    }

    private final int CANCEL_EXPORT = 6;
    private final int RESULT_STYLE = 55;
    private Dialog mCancelLoading;
    private final int HIDE_BOTTOM_VIEW = 7;
    private int RECREATE = 21;
    private boolean mGotoBack = true;

    private void initHandler() {
        mHandler = new Handler(getMainLooper()) {
            @Override
            public void handleMessage(android.os.Message msg) {
                if (msg.what == CANCEL_EXPORT) {
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
                } else if (msg.what == RESULT_STYLE) {
                    mCanAutoPlay = true;
                    mCanShowDialog = false;
                    // setViewVisibility(R.id.rlEditorControl, true);
                    mIvVideoPlayState.setVisibility(View.GONE);


                    returnToMenuLastSelection();
                    setViewVisibility(R.id.edit_video_layout, true);


                    reload(false);
                    start();
                    getWindow().clearFlags(
                            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                } else if (msg.what == HIDE_BOTTOM_VIEW) {
                    if (mRlPlayerBottomMenu.getVisibility() == View.VISIBLE) {
                        mRlPlayerBottomMenu.setVisibility(View.INVISIBLE);
                        if (mRlTitleBar.getVisibility() != View.VISIBLE) {
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
                    }

                } else if (msg.what == RECREATE) {
                    recreate();
                }
            }
        };
    }

    private String getFormatTime(int msec) {
        return DateTimeUtils.stringForMillisecondTime(msec, true, true);
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

    private VirtualVideo mSnapshotEditor;

    @Override
    public VirtualVideo getSnapshotEditor() {
        if (mSnapshotEditor == null) {
            mSnapshotEditor = new VirtualVideo();
            for (Scene scene : mSceneList) {
                mSnapshotEditor.addScene(scene);
            }
        }
        return mSnapshotEditor;
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(android.content.Context context, Intent intent) {
            String action = intent.getAction();
            if (TextUtils.equals(action, SpecialStyleAdapter.ACTION_SHOW_RIGHT)) {
                boolean isloading = intent.getBooleanExtra(
                        SpecialStyleAdapter.ITEM_IS_DOWNLOADING, true);
                mSpecialHandler.setIsLoading(isloading);
                mSubtitleHandler.setIsLoading(isloading);
            }
        }

        ;
    };

    public final static int REQUSET_MOREMUSIC = 119,
            REQUSET_MUSICFRAGMENT = 226,
            REQUSET_MUSICEX = 1000, REQUSET_THEME = 156;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Log.e("onactivity", this.toString() + "..." + requestCode + "..."
        // + resultCode);
        if (requestCode == REQUSET_MUSICFRAGMENT) {

        } else if (requestCode == REQUSET_MUSICEX) {
            if (null != mMusicFragmentEx) {
                if (resultCode == Activity.RESULT_OK) {
                    mVirtualVideoView.reset();
                    mLastPlayPostion = -1;
                }
                mMusicFragmentEx.onActivityResult(requestCode, resultCode, data);
            }
        } else if (requestCode == REQUSET_THEME) {
//            if (null != themeFragment) {
//                themeFragment.onActivityResult(requestCode, resultCode, data);
//            }
        } else if (requestCode == REQUEST_FOR_EDIT) {
            mLastPlayPostion = 0;
            if (resultCode == RESULT_OK) {
                mUpdateAspectPending = true;
                mEditingMediaObjects.clear();
                mSceneList.clear();
                mCurProportion = data.getFloatExtra(
                        IntentConstants.EXTRA_MEDIA_PROPORTION, 0);

                mSceneList = data
                        .getParcelableArrayListExtra(IntentConstants.INTENT_EXTRA_SCENE);
                mProportionStatus = data.getIntExtra(
                        IntentConstants.EDIT_PROPORTION_STATUS, 0);

                ArrayList<Scene> sceneList = new ArrayList<Scene>();
                for (Scene scene : mSceneList) {
                    mEditingMediaObjects.add(scene.getAllMedia().get(0));
                }
                if (mSnapshotEditor != null) {
                    mSnapshotEditor.reset();
                    for (Scene scene : mSceneList) {
                        mSnapshotEditor.addScene(scene);
                    }
                }
                VirtualVideo.getMediaObjectOutSize(sceneList, mCurProportion, mNewSize);
                mVirtualVideoView.setPreviewAspectRatio(mCurProportion);
                updateMenuStatus(true);
                reload(false);
            } else {
                updateMenuStatus(true);
            }
            start();
        }
    }

    @Override
    public void onMenuChanged() {
        mCanExport = false;
        mIsInAudioFragment = false;
        mPlayStatusShowing = true;
        mProgressView.setScroll(true);
        mProgressView.setVisibility(View.VISIBLE);
        // setViewVisibility(R.id.rlEditorControl, true);

        mBtnRight.setText("");
        mBtnRight.setCompoundDrawablesWithIntrinsicBounds(0, 0,
                R.drawable.public_menu_sure, 0);
        mBtnRight.setVisibility(View.VISIBLE);

        mIsEditorMenuEnableAnim = true;

        if (!mPlayStatusShowing) {
            mIvVideoPlayState.setVisibility(View.GONE);
            mProgressView.setScroll(false);
            mProgressView.setVisibility(View.GONE);
        }
    }

//    @SuppressWarnings("unused")
//    private void onCancelMusic() {
//        boolean bOldCanExport = mCanExport;
//        mCanExport = true;
//        mIsInAudioFragment = false;
//        mPlayStatusShowing = true;
//        mProgressView.setScroll(true);
//        mProgressView.setVisibility(View.VISIBLE);
//        // setViewVisibility(R.id.rlEditorControl, true);
//
//        right.setVisibility(View.VISIBLE);
//        returnToMenuLastSelection();
//        if (!isPlaying()) {
//            start();
//        }
//        if (!bOldCanExport) {
//            seekTo(0);// Log.e("robeein", "seekTo5");
//        }
//        mIsEditorMenuEnableAnim = true;
//        if (!mPlayStatusShowing) {
//            mIvVideoPlayState.setVisibility(View.GONE);
//            mProgressView.setScroll(false);
//            mProgressView.setVisibility(View.GONE);
//        }
//    }

    @Override
    public void showTitleBack(boolean bShowing, RectF rectTitleBack) {
        mProgressView.showThemeTitleBack(bShowing, rectTitleBack);
    }


    private MusicItem addAssetMusic(String strItemName,
                                    final String strCaption, String strAssetName) {
        Resources res = getResources();
        String stAssName = strAssetName;
        if (strAssetName.contains("/")) {
            stAssName = strAssetName.substring(
                    strAssetName.lastIndexOf("/") + 1, strAssetName.length());
        }

        String path = PathUtils.getAssetFileNameForSdcard(strItemName,
                stAssName);

        try {
            File f = new File(path);
            if (f.exists()) {
                long lAssetFileLength = CoreUtils.getAssetResourceLen(
                        res.getAssets(), strAssetName);
                if (lAssetFileLength != f.length()) {
                    CoreUtils.assetRes2File(res.getAssets(), strAssetName,
                            f.getAbsolutePath());
                }
            } else {
                CoreUtils.assetRes2File(res.getAssets(), strAssetName,
                        f.getAbsolutePath());
            }
            if (f.exists()) {
                // Log.e("exit",
                // strCaption + "---" + strAssetName + "--"
                // + f.getAbsolutePath());
                MusicItem musicItem = new MusicItem();
                musicItem.setAssetsName(strAssetName);
                musicItem
                        .setDuration(getMusicItemDuration(f.getAbsolutePath()));
                if (musicItem.getDuration() < 0) {
                    musicItem.setDuration(0);
                }
                musicItem.setPath(f.getAbsolutePath());
                musicItem.setTitle(strCaption);
                return musicItem;
            } else {
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private int getMusicItemDuration(String path) {
        MediaPlayer mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(path);
            mPlayer.prepare();
            return mPlayer.getDuration();
        } catch (Exception e) {
            return 0;
        } finally {
            mPlayer.release();
            mPlayer = null;
        }
    }

    private final int DIALOG_RETURN_ID = 1;
    private final int DIALOG_EXPORT_ID = 2;

    @Override
    protected Dialog onCreateDialog(int id) {

        Dialog dialog = null;
        String strMessage = null;

        if (id == DIALOG_RETURN_ID) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

            strMessage = getString(R.string.quit_edit);
            dialog = SysAlertDialog.showAlertDialog(this, "", strMessage,
                    getString(R.string.cancel),
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }, getString(R.string.sure),
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            setResult(RESULT_CANCELED);
                            finish();
                        }
                    });
        } else if (id == DIALOG_EXPORT_ID) {

            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

            strMessage = getString(R.string.export_duration_limit, (int) mExportDuration);
            dialog = SysAlertDialog.showAlertDialog(this, "", strMessage,
                    getString(R.string.close),
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }, getString(R.string.tocontinue),
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            export();
                        }
                    });
        }

        return dialog;
    }

    @Override
    public boolean isMediaMute() {
        return mMediaMute;
    }


    /**
     * 切换配乐强制清除mv中的音乐 (同理：切换mv，清除配乐)
     */
    @Override
    public void removeMvMusic(boolean remove) {
        VirtualVideo video;
        mIsRemoveMVMusic = remove;
        if (null != (video = getEditorVideo())) {
            video.removeMVMusic(remove);
        }
    }

}
