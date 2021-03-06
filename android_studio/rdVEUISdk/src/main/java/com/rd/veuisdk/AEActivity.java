package com.rd.veuisdk;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.fragment.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.SeekBar;
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
import com.rd.vecore.models.AEFragmentInfo;
import com.rd.vecore.models.BlendEffectObject;
import com.rd.vecore.models.MediaObject;
import com.rd.vecore.models.Scene;
import com.rd.vecore.models.Trailer;
import com.rd.vecore.models.VisualFilterConfig;
import com.rd.vecore.models.caption.CaptionObject;
import com.rd.vecore.utils.AEFragmentUtils;
import com.rd.veuisdk.ae.model.AETemplateInfo;
import com.rd.veuisdk.ae.model.BackgroundMedia;
import com.rd.veuisdk.database.FilterData;
import com.rd.veuisdk.database.SubData;
import com.rd.veuisdk.database.TTFData;
import com.rd.veuisdk.fragment.AEFragment;
import com.rd.veuisdk.fragment.AudioFragment;
import com.rd.veuisdk.fragment.BaseFragment;
import com.rd.veuisdk.fragment.FilterFragment;
import com.rd.veuisdk.fragment.FilterFragmentLookup;
import com.rd.veuisdk.fragment.FilterFragmentLookupBase;
import com.rd.veuisdk.fragment.FilterFragmentLookupLocal;
import com.rd.veuisdk.fragment.PartEditFragment;
import com.rd.veuisdk.fragment.SubtitleFragment;
import com.rd.veuisdk.listener.IFixPreviewListener;
import com.rd.veuisdk.manager.ExportConfiguration;
import com.rd.veuisdk.manager.UIConfiguration;
import com.rd.veuisdk.net.SubUtils;
import com.rd.veuisdk.net.TTFUtils;
import com.rd.veuisdk.ui.ProgressView;
import com.rd.veuisdk.ui.RdSeekBar;
import com.rd.veuisdk.utils.AppConfiguration;
import com.rd.veuisdk.utils.CommonStyleUtils;
import com.rd.veuisdk.utils.DateTimeUtils;
import com.rd.veuisdk.utils.IntentConstants;
import com.rd.veuisdk.utils.PathUtils;
import com.rd.veuisdk.utils.SysAlertDialog;
import com.rd.veuisdk.utils.Utils;
import com.rd.veuisdk.utils.ViewUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * AE????????????
 */
public class AEActivity extends BaseActivity implements IVideoEditorHandler {
    private final String WEB_URL = "http://d.56show.com/filemanage2/public/filemanage/file/appData";
    private final String TAG = "AEActivity";
    /*
     * ????????????code:??????????????????
     */
    private final int REQUEST_CODE_READ_EXTERNAL_STORAGE_PERMISSIONS = 1;

    /*
     * ?????????????????????????????????
     */
    private RdSeekBar mSbPlayControl;
    /*
     * ??????????????????????????????????????????
     */
    private ImageView mIvVideoPlayState;
    /*
     * ???????????????????????????
     */
    private float mLastPlayPostion;
    /**
     * ???????????????????????????
     */
    private boolean mLastPlaying;
    /*
     * ?????????????????????????????? ???????????????????????????????????? ???????????????
     */
    private ExtButton mBtnLeft, mBtnRight;
    /*
     * ????????????????????????????????????
     */
    private TextView mTvTitle;
    /*
     * ????????????MV???????????????????????????????????????RadioButton???RadioGroup
     */
    private RadioGroup mEditorMenuGroups;
    /*
     * ???????????????????????????
     */
    private int mLastEditorMenuCheckId;
    /*
     * ???????????????
     */
    private SubtitleFragment mSubtitleFragment;

    private AEFragment mAEFragment;

    private PartEditFragment mPartEditFragment;

    /*
     * ???????????????
     */
    private FilterFragment mFilterFragment;
    /*
     * ?????????????????????????????????????????????
     */
    protected boolean mIsEditorMenuEnableAnim;
    /*
     * ???????????????????????????
     */
    public static int mCurrentFilterType = 0;
    /*
     * ??????MediaObject?????????
     */
    private ArrayList<MediaObject> mEditingMediaObjects;
    /*
     * ??????Scene?????????
     */
    private ArrayList<Scene> mSceneList;
    /*
     * ??????EditorPreivewPositionListener?????????
     */
    private SparseArray<EditorPreivewPositionListener> mSaEditorPostionListener = new SparseArray<EditorPreivewPositionListener>();
    /*
     * ??????????????????????????????????????????
     */
    private boolean mCanExport = true;
    /*
     * ??????????????????mivVideoPlayState??????
     */
    private boolean mPlayStatusShowing = true;
    /*
     * ??????????????????View
     */
    private ProgressView mProgressView;
    /*
     * ?????? ?????????  ??????
     */
    private FrameLayout mLinearWords;

    /*
     * ???????????????????????????
     */
    private boolean mIsPausing;

    private int mAnimationId;
    /*
     * ?????????????????????
     */
    private PreviewFrameLayout mPreviewLayout;

    /*
     * ??????????????????
     */
    private boolean mUpdateAspectPending = true;
    /*
     * ????????????
     */
    private float mCurProportion = 0;
    private VirtualVideo.Size mNewSize;
    private Handler mHandler;
    public static final int REQUESTCODE_FOR_APPEND = 2;
    private String mTempRecfile = null;
    /*
     * ????????????????????????????????????
     */
    private boolean mIsInitializedAndGotPremission = false;
    /*
     * ????????????????????????????????????
     */
    private RotateRelativeLayout mRlPlayerBottomMenu;

    /*
     * ?????????????????????????????????
     */
    private TextView mTvTotalTime;
    /*
     * ????????????????????????????????????
     */
    private TextView mTvCurTime;

    /*
     * ????????????
     */
    private boolean mMediaMute = false;


    /*
     * ????????????????????????
     */
    private ExportConfiguration mExportConfig = null;
    /**
     * ??????????????????
     */
    private UIConfiguration mUIConfig = null;
    /*
     * VirtualVideoView???????????????
     */
    private VirtualVideoView mVirtualVideoView;
    /*
     * ?????????????????????
     */
    private VirtualVideo mVirtualVideo;

    /**
     * ?????????????????????????????????????????????
     *
     * @return
     */
    public VirtualVideo getVirtualVideo() {
        return mVirtualVideo;
    }


    private boolean mSupportVideo = false;

    private AETemplateInfo mAETemplateInfo;

    private boolean isExport = false;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FilterData.getInstance().initilize(this);
        mExportConfig = SdkEntry.getSdkService().getExportConfig();
        mUIConfig = SdkEntry.getSdkService().getUIConfig();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lottie_layout);
        AppConfiguration.fixAspectRatio(this);
        initHandler();
        // ??????api 23????????????
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int hasReadPermission = checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);

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

    public void setExportButtonVisibility(int visibility) {
        mBtnRight.setVisibility(visibility);
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
     * ?????????
     */
    private void onInitialized() {
        Intent intent = getIntent();
        //??????????????????
        mSceneList = intent.getParcelableArrayListExtra(IntentConstants.INTENT_EXTRA_SCENE);
        mSupportVideo = intent.getBooleanExtra(IntentConstants.INTENT_AE_SUPPORT_VIDEO, false);
        //???????????????????????????malEditingMediaObjects
        mEditingMediaObjects = new ArrayList<>();
        for (Scene scene : mSceneList) {
            mEditingMediaObjects.add(scene.getAllMedia().get(0));
        }
        mExportDuration = mExportConfig.exportVideoDuration;
        mIsInitializedAndGotPremission = true;
        SubData.getInstance().initilize(this);//????????????????????????
        TTFData.getInstance().initilize(this);//????????????????????????
        mTempRecfile = getIntent().getStringExtra(EditPreviewActivity.TEMP_FILE);
        initView();

        int nduration = getEditingMediasDuration();
        TempVideoParams.getInstance().checkParams(nduration);
        TempVideoParams.getInstance().setEditingVideoDuration(nduration);
        mVirtualVideoView.setPreviewAspectRatio(mCurProportion);
        mNewSize = new VirtualVideo.Size(0, 0);
        VirtualVideo.getMediaObjectOutSize(mSceneList, mCurProportion,
                mNewSize);

        IntentFilter inFilter = new IntentFilter();
        inFilter.addAction(SdkEntry.MSG_EXPORT);
        registerReceiver(mReceiver, inFilter);

        mLoadDataHandler.removeCallbacks(mLoadDataRunnable);
        mLoadDataHandler.postDelayed(mLoadDataRunnable, 500);//??????????????????
    }

    private Handler mLoadDataHandler = new Handler(Looper.getMainLooper());

    private Runnable mLoadDataRunnable = new Runnable() {

        @Override
        public void run() {
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        mIsPausing = false;

        if (mVirtualVideoView != null) {
            if (mLastPlayPostion > 0) {
                mVirtualVideoView.seekTo(mLastPlayPostion);
                mLastPlayPostion = -1;
                if (!isThemeMenuItem() && mLastPlaying && !isExport) {
                    start();
                }
            } else {
                mVirtualVideoView.seekTo(0);
            }
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
        TempVideoParams.getInstance().recycle();
        if (!mUIConfig.isEnableWizard()) {
            // ????????????????????????
            PathUtils.cleanTempFilesByPrefix("reverse");
            if (!TextUtils.isEmpty(mTempRecfile)) {
                try {
                    new File(mTempRecfile).delete(); // ???????????????????????????
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mTempRecfile = null;
            }
        }
        // ?????????????????????????????????
        if (!TextUtils.isEmpty(mStrCustomWatermarkTempPath)) {
            try {
                new File(mStrCustomWatermarkTempPath).delete();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mStrCustomWatermarkTempPath = null;
        }
        TTFUtils.recycle();
        SubData.getInstance().close();
        TTFData.getInstance().close();
        TempVideoParams.getInstance().setThemeId(0);
        FilterData.getInstance().close();
        if (null != mSaEditorPostionListener) {
            mSaEditorPostionListener.clear();
        }
        if (null != mFilterFragmentLookup) {
            mFilterFragmentLookup.recycle();
            mFilterFragmentLookup = null;
        }
        mSubtitleFragment = null;

        super.onDestroy();

    }


    /**
     * ??????????????????
     *
     * @param bOnlyAudio       ???????????????????????????
     * @param bFastPreview     ???????????????????????????
     * @param lstEditingScenes
     */
    private void reload(boolean bOnlyAudio, boolean bFastPreview, List<Scene> lstEditingScenes) {
        if (bOnlyAudio) {
            if (mVirtualVideoView.isPlaying()) {
                mVirtualVideoView.pause();
            }
            mVirtualVideo.updateMusic(mVirtualVideoView);

        } else {
            if (mVirtualVideoView == null) {
                return;
            }
            mVirtualVideoView.reset();
            mVirtualVideo.reset();

            addDataSource(mVirtualVideo);

            boolean hasAEFragment = mAETemplateInfo != null && mAETemplateInfo.getAEFragmentInfo() != null;
            if (hasAEFragment) {
                float aspectRatio = (float) mAETemplateInfo.getAEFragmentInfo().getWidth() / mAETemplateInfo.getAEFragmentInfo().getHeight();
                mVirtualVideoView.setPreviewAspectRatio(aspectRatio);
            } else {
                mVirtualVideoView.setPreviewAspectRatio(0);
            }

            //??????????????????????????????????????????????????????????????????????????????????????????????????????size???
            if (hasAEFragment) {
                //ae????????????app???????????????????????????
                AEFragmentInfo info = mAETemplateInfo.getAEFragmentInfo();
                float asp = info.getWidth() / (info.getHeight() + 0.0f);
                VirtualVideo.Size mOutputSize = new VirtualVideo.Size(mVirtualVideoView.getPreviewMaxWH(), 0);
                mVirtualVideo.getMediaObjectOutSize(asp, mOutputSize);
                //?????????????????????????????????
                mPreviewLayout.setAspectRatio(asp);
                addCaptionAndBuild(mVirtualVideo, mOutputSize);
            } else {      // mv?????? ???????????????
                try {
                    mVirtualVideo.asyncGetPreviewSize(mVirtualVideoView, new VirtualVideo.PreviewSizeCallBack() {
                        @Override
                        public void onPreivewSize(VirtualVideo.Size size) {
                            if (null != size) {
                                mPreviewLayout.setAspectRatio(size.width / (float) size.height);
                            }
                            addCaptionAndBuild(mVirtualVideo, size);
                        }
                    });
                } catch (InvalidArgumentException e) {
                    e.printStackTrace();
                    addCaptionAndBuild(mVirtualVideo, null);
                }
            }
        }
    }

    /**
     * ??????????????????size?????????????????????
     *
     * @param virtualVideo
     * @param newPreviewSize ????????????size
     */
    private void addCaptionAndBuild(final VirtualVideo virtualVideo, VirtualVideo.Size newPreviewSize) {
        if (null != newPreviewSize) {
            int newWidht = newPreviewSize.width;
            int newHeight = newPreviewSize.height;
            //??????????????????????????????
            boolean bNeedChange = (newWidht != wordLayoutWidth || newHeight != wordLayoutHeight);
            if (bNeedChange) {
                //???????????????????????????
                Utils.onFixPreviewDataSource(0, newWidht, newHeight, null,     new IFixPreviewListener() {
                    @Override
                    public void onComplete() {
                        prepareBuild(virtualVideo);
                    }
                },newWidht,newHeight,mVirtualVideo,mVirtualVideoView);
            } else {
                prepareBuild(virtualVideo);
            }
        } else {
            prepareBuild(virtualVideo);
        }


    }

    private void prepareBuild(VirtualVideo virtualVideo) {
        //??????
        addCaption(mVirtualVideo);
        try {
            virtualVideo.build(mVirtualVideoView);
        } catch (InvalidStateException e) {
            e.printStackTrace();
        }
    }

    /**
     * ??????
     *
     * @param virtualVideo
     */
    private void addCaption(VirtualVideo virtualVideo) {
        //??????
        List<CaptionObject> mListCaptions = TempVideoParams.getInstance().getCaptionObjects();
        if (null != mListCaptions) {
            //????????????????????????????????????CaptionObject
            int len = mListCaptions.size();
            for (int i = 0; i < len; i++) {
                CaptionObject st = mListCaptions.get(i);
                virtualVideo.addCaption(st);
            }
        }
    }


    @Override
    public void reload(boolean bOnlyAudio) {
        reload(bOnlyAudio, false, null);
    }

    @Override
    public void start() {
        if (mVirtualVideoView == null) {
            return;
        }
        mVirtualVideoView.start();
        mIvVideoPlayState.setImageResource(R.drawable.btn_pause);
        int checkedId = mEditorMenuGroups.getCheckedRadioButtonId();
        if (checkedId == R.id.rb_word) {
            if (null != mSubtitleFragment) {
                mSubtitleFragment.setImage(R.drawable.edit_music_pause);
            }
        } else if (mPlayStatusShowing) {
            ViewUtils.fadeOut(this, mIvVideoPlayState);
        }
    }

    @Override
    public void pause() {

        if (mVirtualVideoView.isPlaying()) {
            mVirtualVideoView.pause();
        }
        int checkedId = mEditorMenuGroups.getCheckedRadioButtonId();
        if (checkedId == R.id.rb_word) {
            mSubtitleFragment.setImage(R.drawable.edit_music_play);
        } else {
            mIvVideoPlayState.clearAnimation();
            mIvVideoPlayState.setImageResource(R.drawable.btn_play);
            if (mPlayStatusShowing) {
                mIvVideoPlayState.setVisibility(View.VISIBLE);
            }
        }
    }

    private int mDurationMs = 1000;

    @Override
    public void seekTo(int msec) {
        mVirtualVideoView.seekTo(Utils.ms2s(Math.min(msec, mDurationMs)));
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
            if (null != mSubtitleFragment) {
                mSubtitleFragment.setImage(R.drawable.edit_music_play);
            }
        }

        mIvVideoPlayState.clearAnimation();
        mIvVideoPlayState.setImageResource(R.drawable.btn_play);
        if (mPlayStatusShowing) {
            mIvVideoPlayState.setVisibility(View.VISIBLE);
        }
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


    private VisualFilterConfig lookupConfig;
    private int lookupIndex = 0;

    @Override
    public void changeFilterLookup(VisualFilterConfig lookup, int index) {
        lookupIndex = index;
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
        return lookupIndex;
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
        for (int i = 0; i < mEditingMediaObjects.size(); i++) {
            mDuration += Utils.s2ms(mEditingMediaObjects.get(i).getDuration());
        }
        return mDuration;
    }

    /**
     * ?????? ????????????
     */
    private void onResultWord() {
        stop();
        SysAlertDialog.showLoadingDialog(this, getString(R.string.isloading),
                false, null).show();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mHandler.sendEmptyMessage(RESULT_STYLE);
    }


    @Override
    public void onBackPressed() {
        int checkId = mEditorMenuGroups.getCheckedRadioButtonId();
        if (checkId == R.id.rb_word) {
            mSubtitleFragment.onBackPressed();
            return;
        } else if (checkId == R.id.rb_filter) {
            reload(false);
            returnToMenuLastSelection();
            return;
        }
        TempVideoParams.getInstance().setAspectRatio(
                TempVideoParams.mEditingVideoAspectRatio);

        onCreateDialog(DIALOG_RETURN_ID).show();
    }


    /**
     * ??????????????????????????????????????????
     */
    private void initView() {
        mPreviewLayout = $(R.id.rlPreviewLayout);
        mPreviewLayout.setAspectRatio(1f);
        mTvTitle = $(R.id.tvTitle);
        mTvTitle.setText(R.string.priview_title);
        mEditorMenuGroups = $(R.id.edit_groups);
        $(R.id.rb_word).setOnClickListener(onVerVideoMenuListener);
        $(R.id.rb_filter).setOnClickListener(onVerVideoMenuListener);
        $(R.id.rb_partedit).setOnClickListener(onVerVideoMenuListener);
        $(R.id.rb_lottie).setOnClickListener(onVerVideoMenuListener);
        $(R.id.rb_word).measure(0, 0);
        mBtnLeft = $(R.id.btnLeft);
        mBtnLeft.setCompoundDrawablesWithIntrinsicBounds(R.drawable.public_menu_cancel, 0, 0, 0);
        mBtnLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        mBtnLeft.setPadding(25, 0, 0, 0);
        mBtnRight = $(R.id.btnRight);
        setExportButtonVisibility(View.VISIBLE);
        mBtnRight.setText(R.string.export);
        mBtnRight.setTextColor(getResources().getColor(R.color.main_orange));
        mBtnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRightButtonClick();
            }
        });


        mRlPlayerBottomMenu = $(R.id.rlPlayerBottomMenu);

        mTvCurTime = $(R.id.tvCurTime);

        mTvTotalTime = $(R.id.tvTotalTime);
        mTvTotalTime.measure(0, 0);
        int width = mTvTotalTime.getMeasuredWidth();
        mTvCurTime.setWidth(width + CoreUtils.dpToPixel(5));

        mLinearWords = $(R.id.linear_words);

        mProgressView = $(R.id.progressView);
        mProgressView.setScroll(true);
        mProgressView.setListener(onProgressViewListener);

        resetTitlebar();
        mIvVideoPlayState = $(R.id.ivPlayerState);
        mSbPlayControl = $(R.id.sbEditor);
        mSbPlayControl.setOnSeekBarChangeListener(onSeekbarListener);
        mSbPlayControl.setMax(100);

        mLastPlayPostion = -1;
        mVirtualVideo = new VirtualVideo();

        mVirtualVideoView = $(R.id.epvPreview);
        mVirtualVideoView.setOnPlaybackListener(mPlayViewListener);
        mVirtualVideoView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (!(mFragCurrent instanceof SubtitleFragment) && mPlayStatusShowing) {
                    onEditorPreivewClick();
                }
            }
        });
        mVirtualVideoView.setOnInfoListener(new VirtualVideo.OnInfoListener() {

            @Override
            public boolean onInfo(int what, int extra, Object obj) {
                return false;
            }
        });
    }

    private FilterFragmentLookupBase mFilterFragmentLookup;

    private void notifyCurrentPosition(int positionMs) {
        for (int nTmp = 0; nTmp < mSaEditorPostionListener.size(); nTmp++) {
            mSaEditorPostionListener.valueAt(nTmp).onEditorGetPosition(positionMs, mDurationMs);
        }
    }

    /**
     * ?????????Listener
     */
    private VirtualVideoView.VideoViewListener mPlayViewListener = new VirtualVideoView.VideoViewListener() {
        private boolean isCheckFilter = true;
        private boolean isCheckDefaultMenu = true; // ???????????????????????????

        @Override
        public void onPlayerPrepared() {

            wordLayoutWidth = mVirtualVideoView.getVideoWidth();
            wordLayoutHeight = mVirtualVideoView.getVideoHeight();
            mDurationMs = Utils.s2ms(mVirtualVideoView.getDuration());
            TempVideoParams.getInstance().setEditingVideoDuration(mDurationMs);
            if (isCheckFilter && (mCurrentFilterType != 0)) {
                mVirtualVideoView.setFilterType(mCurrentFilterType);
                isCheckFilter = false;
            }
            mProgressView.setDuration(mDurationMs);
            SysAlertDialog.cancelLoadingDialog();
            mTvCurTime.setText(getFormatTime(0));
            mTvTotalTime.setText(getFormatTime(mDurationMs));
            mSbPlayControl.setMax(mDurationMs);
            int len = mSaEditorPostionListener.size();
            for (int nTmp = 0; nTmp < len; nTmp++) {
                mSaEditorPostionListener.valueAt(nTmp).onEditorPrepred();
            }
            updatePreviewFrameAspect();
            if (mUpdateAspectPending) {
                mUpdateAspectPending = false;
                updateMenuStatus(isCheckDefaultMenu);
                isCheckDefaultMenu = false;
            }
            notifyCurrentPosition(Utils.s2ms(mVirtualVideoView.getCurrentPosition()));
            if (mEditorMenuGroups.getCheckedRadioButtonId() == R.id.rb_lottie) {
                seekTo(0);
                start();
            }
        }

        @Override
        public boolean onPlayerError(int what, int extra) {
            SysAlertDialog.cancelLoadingDialog();
            onToast(R.string.preview_error);
            return false;
        }

        @Override
        public void onPlayerCompletion() {
            if (!(mFragCurrent instanceof SubtitleFragment) && !(mFragCurrent instanceof AudioFragment)) {
                mIvVideoPlayState.setImageResource(R.drawable.btn_play);
                if (mPlayStatusShowing) {
                    mIvVideoPlayState.setVisibility(View.VISIBLE);
                }
                mProgressView.setProgress(0);
                mSbPlayControl.setProgress(0);
                notifyCurrentPosition(0);
                mTvCurTime.setText(getFormatTime(0));
                mLastPlayPostion = 0;
                mVirtualVideoView.seekTo(0);
            }
            for (int nTmp = 0; nTmp < mSaEditorPostionListener.size(); nTmp++) {
                mSaEditorPostionListener.valueAt(nTmp).onEditorPreviewComplete();
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
                // ???????????????????????????????????????????????????
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
     * ????????????????????????????????????
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
                    mPlayViewListener.onGetCurrentPosition(Utils.ms2s(getCurrentPosition()));
                }
            } else {
                start();
            }
        }
        mRlPlayerBottomMenu.setVisibility(View.VISIBLE);

    }

    /**
     * ?????????????????????
     */
    protected void updateMenuStatus(boolean bCheckDefaultMenu) {
        if (bCheckDefaultMenu) {
            mLastEditorMenuCheckId = R.id.rb_lottie;
            mEditorMenuGroups.check(mLastEditorMenuCheckId);
            onCheckItem(mLastEditorMenuCheckId);
        }
    }


    /**
     * ????????????????????????????????????
     */

    private void returnToMenuLastSelection() {
        mEditorMenuGroups.check(mLastEditorMenuCheckId);
        onCheckItem(mLastEditorMenuCheckId);
    }


    /**
     * ?????????AE??????
     */
    private void onVerVideoAE(boolean bOldCanExport) {
        resetTitlebar();
        setViewVisibility(R.id.titlebar_layout, true);
        if (mAEFragment == null) {
            mAEFragment = new AEFragment(mUIConfig.lottieUrl, true, mSupportVideo);
        }
        changeToFragment(mAEFragment, true);
        if (!isPlaying()) {
            start();
        }
        if (!bOldCanExport) {
            seekTo(0);
        }
        mTvTitle.setText(getString(R.string.temp));
    }

    private void onPartEdit(boolean canExport) {
        setViewVisibility(R.id.titlebar_layout, true);
        resetTitlebar();
        if (mPartEditFragment == null) {
            mPartEditFragment = new PartEditFragment();
            mPartEditFragment.setUIConfig(mUIConfig);
            mPartEditFragment.setScene(mSceneList);
        }
        changeToFragment(mPartEditFragment, true);
        if (!isPlaying()) {
            start();
        }
        if (!canExport) {
            seekTo(0);
        }
        mTvTitle.setText(getString(R.string.partedit));
    }

    private View.OnClickListener onVerVideoMenuListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            onCheckItem(v.getId());
        }
    };


    private void onCheckItem(int checkedId) {
        boolean bOldCanExport = mCanExport;
        mCanExport = true;
        mPlayStatusShowing = true;
        mProgressView.setScroll(true);
        mProgressView.setVisibility(View.VISIBLE);
        setExportButtonVisibility(View.VISIBLE);
        if (checkedId == R.id.rb_lottie) {
            onVerVideoAE(bOldCanExport);
        } else {
            if (checkedId == R.id.rb_word) {
                setViewVisibility(R.id.titlebar_layout, false);
                mVirtualVideoView.setAutoRepeat(false); // ???????????????????????????
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                mPlayStatusShowing = false;
                stop();
                mRlPlayerBottomMenu.setVisibility(View.INVISIBLE);
                mIvVideoPlayState.setVisibility(View.GONE);
                if (null == mSubtitleFragment) {
                    mSubtitleFragment = SubtitleFragment.newInstance(WEB_URL,
                            WEB_URL);
                }
                mSubtitleFragment.setFragmentContainer($(R.id.rlEditorMenuAndSubLayout));
                changeToFragment(mSubtitleFragment, false);
                mTvTitle.setText(R.string.add_subtitle);
                mCanExport = false;
                checkedId = -1;
                mIsEditorMenuEnableAnim = false;
            } else if (checkedId == R.id.rb_filter) {
                setViewVisibility(R.id.titlebar_layout, false);
                if (!TextUtils.isEmpty(mUIConfig.filterUrl)) {
                    if (mFilterFragmentLookup == null) {
                        mFilterFragmentLookup = FilterFragmentLookup.newInstance(mUIConfig.filterUrl);
                    }
                    changeToFragment(mFilterFragmentLookup, false);
                } else if (mUIConfig.filterLayoutTpye == UIConfiguration.FILTER_LAYOUT_3) {
                    if (mFilterFragmentLookup == null) {
                        mFilterFragmentLookup = FilterFragmentLookupLocal.newInstance();
                    }
                    changeToFragment(mFilterFragmentLookup, false);
                } else {
                    if (mFilterFragment == null) {
                        mFilterFragment = FilterFragment.newInstance();
                    }
                    if (mUIConfig.filterLayoutTpye == UIConfiguration.FILTER_LAYOUT_2) {
                        //jlk??????(acv ??????)
                        mFilterFragment.setJLKStyle(true);
                        changeToFragment(mFilterFragment, true);
                    } else {
                        // ???????????? ???acv???
                        changeToFragment(mFilterFragment, false);
                    }
                }
                mCanExport = false;
                checkedId = -1;
                if (!isPlaying()) {
                    start();
                }
                if (!bOldCanExport) {
                    seekTo(0);
                }
                mTvTitle.setText(R.string.filter);
            } else if (checkedId == R.id.rb_partedit) {
                onPartEdit(true);
            } else {
                Log.e(TAG, "onCheckItem: other: " + checkedId);
            }
        }
        if (checkedId != -1) {
            mIsEditorMenuEnableAnim = true;
        }
        if (!mPlayStatusShowing) {
            mIvVideoPlayState.setVisibility(View.GONE);
            mProgressView.setScroll(false);
            mProgressView.setVisibility(View.GONE);
        }

    }


    /**
     * ??????????????????????????????
     */
    public void resetTitlebar() {
        mBtnLeft.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.edit_back_button, 0, 0, 0);
        mBtnRight.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        mBtnRight.setText(R.string.export);

    }

    /**
     * ??????fragment
     */
    private void changeToFragment(BaseFragment fragment, boolean bEditorGroupsVisible) {
        changeToFragment(fragment, mIsEditorMenuEnableAnim, bEditorGroupsVisible);
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
     * ??????fragment
     *
     * @param fragment             ???????????????fragment
     * @param enableAnimation      ????????????????????????
     * @param bEditorGroupsVisible ?????????????????????????????????
     */
    private void changeToFragment(final BaseFragment fragment,
                                  boolean enableAnimation, final boolean bEditorGroupsVisible) {
        if (mFragCurrent == fragment) {
            // ???????????????fragment??????????????????
            setFragmentCurrent(fragment); // ???????????????????????????????????????
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
                $(R.id.rlEditorMenuAndSubLayout).startAnimation(
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
                                $(R.id.rlEditorMenuAndSubLayout)
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

    private void updatePreviewFrameAspect() {

        CommonStyleUtils.init(mLinearWords.getWidth(), mLinearWords.getHeight());

    }

    /**
     * ???????????????????????????????????????
     */
    private SeekBar.OnSeekBarChangeListener onSeekbarListener = new SeekBar.OnSeekBarChangeListener() {
        private boolean m_bIsPlayingOnSeek;

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
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
                start();
            }
        }
    };


    /**
     * ?????????????????????
     */
    protected void onRightButtonClick() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            return;
        }
        if (mExportDuration != 0 && mExportDuration < mVirtualVideoView.getDuration()) {
            onCreateDialog(DIALOG_EXPORT_ID);
            return;
        }
        if (mExportConfig.useCustomExportGuide) {
            SdkEntryHandler.getInstance().onExportClick(AEActivity.this);
        } else {
            export();
        }
    }

    private void export() {
        pause();
        ExportHandler exportHandler = new ExportHandler(this, new ExportHandler.IExport() {
            @Override
            public void addData(VirtualVideo virtualVideo) {
                addDataSource(virtualVideo);
                //??????
                addCaption(virtualVideo);
                if (mExportConfig.trailerPath != null) {
                    Trailer trailer = new Trailer(mExportConfig.trailerPath,
                            mExportConfig.trailerDuration, mExportConfig.trailerFadeDuration);
                    virtualVideo.setTrailer(trailer);
                }
            }
        });
        exportHandler.onExport(mVirtualVideoView.getPreviewAspectRatio(), withWatermark);

    }

    private void addDataSource(VirtualVideo virtualVideo) {
        for (Scene scene : mSceneList) {
            virtualVideo.addScene(scene);
        }
        if (mAETemplateInfo != null && mAETemplateInfo.getAEFragmentInfo() != null) {
            Music music = mAETemplateInfo.getMusic();
            if (null != music) {
                try {
                    mVirtualVideo.addMusic(music);
                } catch (InvalidArgumentException ignored) {
                }
            }

            for (BackgroundMedia backgroundMedia : mAETemplateInfo.getBackground()) {
                MediaObject mediaObject = backgroundMedia.toMediaObject();
                if (mediaObject != null) {
                    virtualVideo.addBackgroundMedia(mediaObject);
                }
            }

            for (BlendEffectObject effectObject : mAETemplateInfo.getBlendEffectObject()) {
                virtualVideo.addMVEffect(effectObject);
            }
            virtualVideo.addAEFragment(mAETemplateInfo.getAEFragmentInfo());
        } else {
            virtualVideo.setMV(mAnimationId);
        }

        virtualVideo.setEnableTitlingAndSpEffectOuter(mUIConfig.enableTitlingAndSpecialEffectOuter);
        if (null != lookupConfig) {
            try {
                virtualVideo.changeFilter(lookupConfig);
            } catch (InvalidArgumentException e) {
                e.printStackTrace();
            }
        } else {
            virtualVideo.changeFilter(mCurrentFilterType);
        }
    }


    /**
     * ??????????????????????????????
     */
    private String mStrCustomWatermarkTempPath;


    private float mExportDuration;
    private final int CANCEL_EXPORT = 6;
    private final int RESULT_STYLE = 55;
    private Dialog mCancelLoading;
    private int RECREATE = 21;
    private int RELOAD = 22;
    private boolean mGotoBack = true;

    private void initHandler() {
        mHandler = new Handler(getMainLooper()) {
            @Override
            public void handleMessage(android.os.Message msg) {
                if (msg.what == CANCEL_EXPORT) {
                    mGotoBack = true;
                    mCancelLoading = SysAlertDialog.showLoadingDialog(
                            AEActivity.this,
                            getString(R.string.canceling), false,
                            new DialogInterface.OnCancelListener() {

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
                    mIvVideoPlayState.setVisibility(View.GONE);
                    returnToMenuLastSelection();
                    setViewVisibility(R.id.edit_video_layout, true);
                    reload(false);
                    start();
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                } else if (msg.what == RECREATE) {
                    recreate();
                } else if (msg.what == RELOAD) {
                    reload(false);
                    start();
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

    @Override
    public FrameLayout getSubEditorParent() {
        return mLinearWords;
    }

    private VirtualVideo mSnapshotEditor;

    private void getSnapshotEditorImp() {
        mSnapshotEditor = new VirtualVideo();
        for (Scene scene : mSceneList) {
            mSnapshotEditor.addScene(scene);
        }
    }


    @Override
    public VirtualVideo getSnapshotEditor() {
        //???????????????????????????????????????
        if (mSnapshotEditor == null) {
            getSnapshotEditorImp();
        }
        return mSnapshotEditor;
    }

    private boolean withWatermark = true;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(android.content.Context context, Intent intent) {
            String action = intent.getAction();
            if (TextUtils.equals(action, SdkEntry.MSG_EXPORT)) {
                withWatermark = intent.getBooleanExtra(SdkEntry.EXPORT_WITH_WATERMARK, true);
                export();
            }
        }
    };

    private int wordLayoutWidth = 0, wordLayoutHeight = 0;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUESTCODE_FOR_APPEND) {
            if (resultCode == RESULT_OK) {
                mPartEditFragment.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    @Override
    public void changeAnimation(int animation) {
        mAnimationId = animation;
        SysAlertDialog.showLoadingDialog(this,
                getString(R.string.isloading), false, null);
    }

    @Override
    public void setAETemplateInfo(AETemplateInfo aeTemplateInfo) {
        mAETemplateInfo = aeTemplateInfo;
        if (mAETemplateInfo != null) {
            List<MediaObject> mediaObjects = new ArrayList<>();
            for (Scene scene : mSceneList) {
                for (MediaObject mediaObject : scene.getAllMedia()) {
                    mediaObjects.add(mediaObject);
                }
            }
            mAETemplateInfo.setMediaObjects(mediaObjects);

            try {
                AEFragmentUtils.load(aeTemplateInfo.getDataPath(), new AEFragmentUtils.AEFragmentListener() {

                    /**
                     * ?????????????????????
                     *
                     * @param aeFragmentInfo AE????????????
                     */
                    @Override
                    public void onLoadComplete(AEFragmentInfo aeFragmentInfo) {
                        if (aeFragmentInfo != null) {
                            mAETemplateInfo.setAEFragmentInfo(false, aeFragmentInfo);
                        }
                        reload(false);
                    }

                    @Override
                    public void onLoadFailed(int errorCode, String message) {
                        Log.e(TAG, message);
                    }
                });
            } catch (InvalidArgumentException e) {
                e.printStackTrace();
            }
        } else {
            reload(false);
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
                    }, getString(R.string.continue_txt),
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (mExportConfig.useCustomExportGuide) {
                                SdkEntryHandler.getInstance().onExportClick(AEActivity.this);
                            } else {
                                export();
                            }
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
     * ????????????????????????mv???????????? (???????????????mv???????????????)
     */
    @Override
    public void removeMvMusic(boolean remove) {
        VirtualVideo video;
        if (null != (video = getEditorVideo())) {
            video.removeMVMusic(remove);
        }
    }

    @Override
    public void onProportionChanged(float aspect) {

    }

    @Override
    public void onBackgroundModeChanged(boolean isEnableBg) {

    }

    @Override
    public void onBackgroundColorChanged(int color) {

    }

    @Override
    public void onBack() {
        mEditorMenuGroups.check(R.id.rb_lottie);
        onResultWord();
        onCheckItem(R.id.rb_lottie);

    }

    @Override
    public void onSure() {
        int checkId = mEditorMenuGroups.getCheckedRadioButtonId();
        if (checkId == R.id.rb_word) {
            onResultWord();
            return;
        } else if (checkId == R.id.rb_filter) {
            if (null != mFilterFragmentLookup) {
                lookupConfig = mFilterFragmentLookup.getLookup();
            } else if (null != mFilterFragment) {
                mCurrentFilterType = mFilterFragment.getFilterId();
            }

        }
        returnToMenuLastSelection();
    }

}
