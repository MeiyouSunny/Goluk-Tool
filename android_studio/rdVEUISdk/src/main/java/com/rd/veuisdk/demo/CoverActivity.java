package com.rd.veuisdk.demo;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.fragment.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.rd.lib.ui.PreviewFrameLayout;
import com.rd.lib.utils.BitmapUtils;
import com.rd.lib.utils.CoreUtils;
import com.rd.lib.utils.ThreadPoolUtils;
import com.rd.vecore.VirtualVideo;
import com.rd.vecore.VirtualVideoView;
import com.rd.vecore.exception.InvalidArgumentException;
import com.rd.vecore.exception.InvalidStateException;
import com.rd.vecore.models.MediaObject;
import com.rd.vecore.models.Scene;
import com.rd.vecore.models.VisualFilterConfig;
import com.rd.vecore.models.caption.CaptionObject;
import com.rd.veuisdk.BaseActivity;
import com.rd.veuisdk.CropRotateMirrorActivity;
import com.rd.veuisdk.IVideoEditorHandler;
import com.rd.veuisdk.R;
import com.rd.veuisdk.SdkEntry;
import com.rd.veuisdk.SelectMediaActivity;
import com.rd.veuisdk.TempVideoParams;
import com.rd.veuisdk.ae.model.AETemplateInfo;
import com.rd.veuisdk.database.SubData;
import com.rd.veuisdk.database.TTFData;
import com.rd.veuisdk.fragment.SubtitleFragment;
import com.rd.veuisdk.manager.UIConfiguration;
import com.rd.veuisdk.model.WordInfo;
import com.rd.veuisdk.net.SubUtils;
import com.rd.veuisdk.net.TTFUtils;
import com.rd.veuisdk.ui.RulerSeekbar;
import com.rd.veuisdk.ui.ThumbNailLine;
import com.rd.veuisdk.utils.AppConfiguration;
import com.rd.veuisdk.utils.CommonStyleUtils;
import com.rd.veuisdk.utils.DateTimeUtils;
import com.rd.veuisdk.utils.IntentConstants;
import com.rd.veuisdk.utils.PathUtils;
import com.rd.veuisdk.utils.SysAlertDialog;
import com.rd.veuisdk.utils.Utils;
import com.rd.veuisdk.videoeditor.widgets.IViewTouchListener;
import com.rd.veuisdk.videoeditor.widgets.ScrollViewListener;
import com.rd.veuisdk.videoeditor.widgets.TimelineHorizontalScrollView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class CoverActivity extends BaseActivity implements View.OnClickListener,
        IVideoEditorHandler {

    private static final String TAG = "CoverActivity";
    /*
     * ????????????code:??????????????????
     */
    private final int REQUEST_CODE_READ_EXTERNAL_STORAGE_PERMISSIONS = 1;
    /*
     * VirtualVideoView???????????????   ?????????????????????
     */
    private VirtualVideoView mVirtualVideoView;
    private VirtualVideo mVirtualVideo;
    /**
     * ??????????????????
     */
    private UIConfiguration mUIConfig = null;
    /*
     * ??????  ??????
     */
    private FrameLayout mLinearWords;
    private PreviewFrameLayout mPlayerContainer, mWordContainer;
    //?????? ?????? ????????? ??????
    private View addLayout, durationLayout, thumbnailLayout,
            subtitleLayout;
    //????????????
    private TextView tvShowDuration;
    private RulerSeekbar mSeekbarDuration;
    private ImageView ivPlayState;
    //??????????????????
    private ThumbNailLine mThumbNailLine;
    private TimelineHorizontalScrollView mTimeline;
    private LinearLayout mLinearLayout;
    private TextView tvTNProgress, tvTNDuration;
    /**
     * ??????
     */
    private Scene mScene = null;
    /**
     * ????????? ??????
     */
    private int mDuration = 0;
    /**
     * ?????????????????? 0 ?????? 1??????
     */
    private int mHeadOrTail = 0;
    /**
     * ?????? ??????
     */
    private CoverInfo mHeadInfo = new CoverInfo();
    private CoverInfo mTailInfo = new CoverInfo();
    /**
     * ????????????
     */
    private float mMaxCoverDuration = 2f;
    private float mCoverDurationHead = mMaxCoverDuration / 2;//??????1???
    private float mCoverDurationTail = mMaxCoverDuration / 2;//??????1???
    private float mChangeCoverDuration = mMaxCoverDuration / 2;//?????????????????????????????????
    /**
     * ????????????
     * ??????1  ??????2  ??????3
     */
    private int mState = 0;
    //??????
    private SeekBar mSbPlayControl;
    //????????????
    private float mPreviewAsp = 0;
    /**
     * ????????????
     */
    private SubtitleFragment mSubtitleFragment;
    /**
     * ???????????????????????????
     */
    private ArrayList<WordInfo> mHeadWordInfos = new ArrayList<>();
    private ArrayList<WordInfo> mTailWordInfos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cover);
        initView();
        init();
    }

    private void init() {
        SysAlertDialog.showLoadingDialog(CoverActivity.this, R.string.isloading);
        //?????????????????????????????????
        AppConfiguration.fixAspectRatio(this);
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

    private void initView() {
        mVirtualVideo = new VirtualVideo();
        mVirtualVideoView = (VirtualVideoView) findViewById(R.id.epvPreview);
        mLinearWords = (FrameLayout) findViewById(R.id.linear_words);
        mPlayerContainer = findViewById(R.id.rlPlayerContainer);
        mWordContainer = findViewById(R.id.rlPreview);
        //???????????????
        btnHead = findViewById(R.id.btnHead);
        btnTail = findViewById(R.id.btnTail);
        //????????????????????????????????????
        addLayout = findViewById(R.id.cover_add_layout);
        durationLayout = findViewById(R.id.cover_change_duration);
        thumbnailLayout = findViewById(R.id.cover_thumbnail);
        subtitleLayout = findViewById(R.id.cover_subtitle);

        ivPlayState = findViewById(R.id.ivPlayerState);
        mTimeline = findViewById(R.id.priview_sticker_line);
        mLinearLayout = findViewById(R.id.subtitleline_media);
        mThumbNailLine = findViewById(R.id.subline_view);
        tvTNProgress = findViewById(R.id.split_item_progress);
        tvTNDuration = findViewById(R.id.tvEnd);
        tvShowDuration = findViewById(R.id.tvShowDuration);
        mSeekbarDuration = findViewById(R.id.cover_duration);
        mSbPlayControl = findViewById(R.id.sbEditor);

        btnHead.setOnClickListener(this);
        btnTail.setOnClickListener(this);
        ivPlayState.setOnClickListener(this);
        //????????????????????????????????????????????????
        findViewById(R.id.ivCancel).setOnClickListener(this);
        findViewById(R.id.ivSure).setOnClickListener(this);
        findViewById(R.id.tvAddPicture).setOnClickListener(this);
        findViewById(R.id.tvAddText).setOnClickListener(this);
        findViewById(R.id.tvDuration).setOnClickListener(this);

        ((TextView) findViewById(R.id.tvBottomTitle)).setText(getString(R.string.cover));

        //?????????
        mVirtualVideoView.setOnPlaybackListener(mPlayViewListener);
        mVirtualVideoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mState == 3) {
                    return;
                }
                if (isPlaying()) {
                    pause();
                    mPlayViewListener.onGetCurrentPosition(Utils.ms2s(getCurrentPosition()));
                } else {
                    start();
                }
            }
        });

        //?????????
        mTimeline.addScrollListener(new ScrollViewListener() {
            @Override
            public void onScrollBegin(View view, int scrollX, int scrollY, boolean appScroll) {
                mThumbNailLine.setStartThumb(mTimeline.getScrollX());
                if (!appScroll) {
                    int progress = mTimeline.getProgress();
                    pause();
                    setProgressText(progress);
                }
            }

            @Override
            public void onScrollProgress(View view, int scrollX, int scrollY, boolean appScroll) {
                mThumbNailLine.setStartThumb(mTimeline.getScrollX());
                if (!appScroll) {
                    int progress = mTimeline.getProgress();
                    seekTo(progress);
                    setProgressText(progress);
                }
            }

            @Override
            public void onScrollEnd(View view, int scrollX, int scrollY, boolean appScroll) {
                int progress = mTimeline.getProgress();
                mThumbNailLine.setStartThumb(mTimeline.getScrollX());
                if (!appScroll) {
                    seekTo(progress);
                }
                setProgressText(progress);
            }
        });
        mTimeline.setViewTouchListener(new IViewTouchListener() {
            @Override
            public void onActionDown() {

            }

            @Override
            public void onActionMove() {

            }

            @Override
            public void onActionUp() {
                mTimeline.resetForce();
                int progress = mTimeline.getProgress();
                setProgressText(progress);
            }
        });

        //????????????
        mSeekbarDuration.setMax(100);
        mSeekbarDuration.setOnSeekListener(new RulerSeekbar.OnSeekListener() {

            @Override
            public void onSeekStart(float progress, int max) {
                resetCoverDurationText(calculateTime(progress, max));
            }

            @Override
            public void onSeek(float progress, int max) {
                mChangeCoverDuration = progress;
                resetCoverDurationText(calculateTime(progress, max));
            }

            @Override
            public void onSeekEnd(float progress, int max) {
                mChangeCoverDuration = progress;
            }
        });

        mSbPlayControl.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (mVirtualVideoView.isPlaying()) {
                    pause();
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                start();
            }
        });

    }

    private void onInitialized() {
        //????????????
        mScene = getIntent().getParcelableExtra(IntentConstants.INTENT_EXTRA_SCENE);
        if (mScene == null) {
            //????????????
            onToast(R.string.no_media);
            finish();
            return;
        }
        SubData.getInstance().initilize(this);//????????????????????????
        TTFData.getInstance().initilize(this);//????????????????????????
        //uiconfig
        mUIConfig = SdkEntry.getSdkService().getUIConfig();
        mStateSize = getResources().getDimensionPixelSize(R.dimen.add_sub_play_state_size);
        mDisplayMetrics = CoreUtils.getMetrics();
        buildAll();
        //????????????size
        mNewSize = new VirtualVideo.Size(0, 0);
        fixPreviewSize();
        mPlayerContainer.setAspectRatio(mPreviewAsp);
        mWordContainer.setAspectRatio(mPreviewAsp);
        mVirtualVideoView.setPreviewAspectRatio(mPreviewAsp);
        mWordContainer.post(new Runnable() {

            @Override
            public void run() {
                CommonStyleUtils.init(mWordContainer.getWidth(),
                        mWordContainer.getHeight());
            }
        });

        mDuration = Utils.s2ms(mScene.getDuration());
        TempVideoParams.getInstance().checkParams(mDuration + Utils.s2ms(mMaxCoverDuration));
        TempVideoParams.getInstance().setEditingVideoDuration(mDuration + Utils.s2ms(mMaxCoverDuration));
    }

    private VirtualVideo.Size mNewSize;

    /**
     * ??????????????????
     */
    private void fixPreviewSize() {
        mNewSize.set(mVirtualVideoView.getPreviewMaxWH(), 0);
        ArrayList<Scene> list = new ArrayList<>();
        list.add(mScene);
        VirtualVideo.getMediaObjectOutSize(list, 0,
                mNewSize);
        mPreviewAsp = mNewSize.width / (mNewSize.height + 0.0f);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btnHead) {
            if (mHeadOrTail == 0 || mState == 3) {
                return;
            }
            setHeadOrTail(0);
            setStatus(0);
        } else if (id == R.id.btnTail) {
            if (mHeadOrTail == 1 || mState == 3) {
                return;
            }
            setHeadOrTail(1);
            setStatus(0);
        } else if (id == R.id.ivSure) {
            //??????
            onSure();
        } else if (id == R.id.ivCancel) {
            //??????
            onBackPressed();
        } else if (id == R.id.tvAddPicture) {
            //????????????
            addPictureDialog();
            setStatus(0);
        } else if (id == R.id.tvAddText) {
            //????????????
            if (mHeadOrTail == 0 && mHeadInfo.getPath() == null) {
                Toast.makeText(this, getString(R.string.cover_prompt_add_image), Toast.LENGTH_SHORT).show();
                return;
            }
            if (mHeadOrTail == 1 && mTailInfo.getPath() == null) {
                Toast.makeText(this, getString(R.string.cover_prompt_add_image), Toast.LENGTH_SHORT).show();
                return;
            }
            //????????????
            setStatus(3);
            addText();
        } else if (id == R.id.tvDuration) {
            //???????????????????????????
            setStatus(1);
            mSeekbarDuration.postDelayed(new Runnable() {
                @Override
                public void run() {
                    setSeekBarDuration();
                }
            }, 200);
        } else if (id == R.id.ivPlayerState) {
            if (isPlaying()) {
                pause();
            } else {
                if (Math.abs(getCurrentPosition() - getDuration()) < 300) {
                    seekTo(0);
                }
                start();
            }
        }
    }

    /**
     * ?????? ????????????
     */
    private Scene getHeadScene() {
        if (mHeadInfo.getPath() != null) {
            try {
                Scene head = VirtualVideo.createScene();
                MediaObject objectHead = new MediaObject(mHeadInfo.getPath());
                objectHead.setTimeRange(0, mHeadInfo.getDuration());
                objectHead.setClearImageDefaultAnimation(true);
                head.addMedia(objectHead);
                return head;
            } catch (InvalidArgumentException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * ?????? ????????????
     */
    private Scene getTailScene() {
        if (mTailInfo.getPath() != null) {
            try {
                Scene tail = VirtualVideo.createScene();
                MediaObject objectTail = new MediaObject(mTailInfo.getPath());
                objectTail.setTimeRange(0, mTailInfo.getDuration());
                objectTail.setClearImageDefaultAnimation(true);
                tail.addMedia(objectTail);
                return tail;
            } catch (InvalidArgumentException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * ??????(??????????????????)
     */
    private void buildHead() {
        SysAlertDialog.showLoadingDialog(CoverActivity.this, R.string.isloading);
        mVirtualVideoView.reset();
        mVirtualVideo.reset();
        mVirtualVideo.addScene(getHeadScene());
        //??????
        for (WordInfo wordInfo : mHeadWordInfos) {
            mVirtualVideo.addCaption(wordInfo.getCaptionObject());
        }
        try {
            mVirtualVideo.build(mVirtualVideoView);
        } catch (InvalidStateException e) {
            e.printStackTrace();
        }
    }

    /**
     * ??????(??????????????????)
     */
    private void buildTail() {
        SysAlertDialog.showLoadingDialog(CoverActivity.this, R.string.isloading);
        mVirtualVideoView.reset();
        mVirtualVideo.reset();
        mVirtualVideo.addScene(getTailScene());
        //??????
        for (WordInfo wordInfo : mTailWordInfos) {
            mVirtualVideo.addCaption(wordInfo.getCaptionObject());
        }
        try {
            mVirtualVideo.build(mVirtualVideoView);
        } catch (InvalidStateException e) {
            e.printStackTrace();
        }
    }

    /**
     * ??????
     */
    private void buildSource() {
        SysAlertDialog.showLoadingDialog(CoverActivity.this, R.string.isloading);
        mVirtualVideoView.reset();
        mVirtualVideo.reset();
        mVirtualVideo.addScene(mScene);
        try {
            mVirtualVideo.build(mVirtualVideoView);
        } catch (InvalidStateException e) {
            e.printStackTrace();
        }
    }

    /**
     * ?????? ???????????????
     *
     * @param virtualVideo
     */
    private void buildOrExport(VirtualVideo virtualVideo) {

        //??????
        Scene headScene = getHeadScene();
        if (headScene != null) {
            virtualVideo.addScene(headScene);
        }
        virtualVideo.addScene(mScene);
        //??????
        Scene TailScene = getTailScene();
        if (TailScene != null) {
            virtualVideo.addScene(TailScene);
        }

        //????????????
        ArrayList<WordInfo> tmp = new ArrayList<>();
        for (WordInfo wordInfo : mHeadWordInfos) {
            tmp.add(wordInfo.clone());
        }
        if (tmp.size() > 0) {
            for (WordInfo head : tmp) {
                CaptionObject object = head.getCaptionObject();
                if (object.getTimelineStart() < 0) {
                    object.setTimelineRange(0, object.getTimelineEnd());
                }
                virtualVideo.addCaption(object);
            }
        }
        //??????
        tmp.clear();
        for (WordInfo wordInfo : mTailWordInfos) {
            tmp.add(wordInfo.clone());
        }
        if (tmp.size() > 0) {
            for (WordInfo tail : tmp) {
                CaptionObject object = tail.getCaptionObject();
                if (mHeadInfo.getPath() == null) {
                    object.setTimelineRange(Utils.ms2s(mDuration) + object.getTimelineStart(),
                            Utils.ms2s(mDuration) + object.getTimelineEnd());
                } else {
                    object.setTimelineRange(Utils.ms2s(mDuration) + mCoverDurationHead + object.getTimelineStart(),
                            Utils.ms2s(mDuration) + mCoverDurationHead + object.getTimelineEnd());
                }
                virtualVideo.addCaption(object);
            }
        }
    }

    /**
     * ??????
     */
    private void buildAll() {
        SysAlertDialog.showLoadingDialog( this, R.string.isloading);
        mVirtualVideoView.reset();
        mVirtualVideo.reset();
        mVirtualVideoView.setPreviewAspectRatio(mPreviewAsp);
        buildOrExport(mVirtualVideo);
        try {
            mVirtualVideo.build(mVirtualVideoView);
        } catch (InvalidStateException e) {
            e.printStackTrace();
        }
    }

    /**
     * ???????????? mState ????????????
     */
    public void setFeaturesUI() {
        if (mState == 0) {
            addLayout.setVisibility(View.VISIBLE);
            durationLayout.setVisibility(View.GONE);
            thumbnailLayout.setVisibility(View.GONE);
            subtitleLayout.setVisibility(View.GONE);
        } else if (mState == 1) {
            addLayout.setVisibility(View.GONE);
            durationLayout.setVisibility(View.VISIBLE);
            thumbnailLayout.setVisibility(View.GONE);
            subtitleLayout.setVisibility(View.GONE);
        } else if (mState == 2) {
            addLayout.setVisibility(View.GONE);
            durationLayout.setVisibility(View.GONE);
            thumbnailLayout.setVisibility(View.VISIBLE);
            subtitleLayout.setVisibility(View.GONE);
        } else if (mState == 3) {
            //????????????
            addLayout.setVisibility(View.GONE);
            durationLayout.setVisibility(View.GONE);
            thumbnailLayout.setVisibility(View.GONE);
            subtitleLayout.setVisibility(View.VISIBLE);
        }
    }

    /**
     * ???????????? UI?????? ???????????????????????????0?????????
     */
    private void setStatus(int state) {
        if (mState != state) {
            mState = state;
            setFeaturesUI();
        }
        pause();
        seekTo();
    }

    /**
     * ??????????????????????????????
     */
    private void setHeadOrTail(int status) {
        if (mHeadOrTail == status) {
            return;
        }
        mHeadOrTail = status;
        if (mHeadOrTail == 0) {
            btnHead.setBackgroundResource(R.drawable.cover_head_p);
            btnHead.setTextColor(Color.BLACK);
            btnTail.setBackgroundResource(R.drawable.cover_tail_n);
            btnTail.setTextColor(Color.WHITE);
        } else {
            btnHead.setBackgroundResource(R.drawable.cover_head_n);
            btnHead.setTextColor(Color.WHITE);
            btnTail.setBackgroundResource(R.drawable.cover_tail_p);
            btnTail.setTextColor(Color.BLACK);
        }
    }

    /**
     * ?????????????????? ??????
     */
    private void addPictureDialog() {
        String[] menu = getResources().getStringArray(R.array.cover_picture);
        SysAlertDialog.showListviewAlertMenu(this, getString(R.string.cover_select), menu,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (which == 0) {
                            //??????
                            addMediaCover();
                        } else if (which == 1) {
                            //??????
                            buildSource();
                            mState = 2;
                            setFeaturesUI();
                            onInitThumbTimeLine();
                            onInitThumbTimeLine(mVirtualVideo);
                        }
                    }
                });
    }

    /**
     * ????????????
     */
    private void addText() {
        //??????????????????????????????
        if (mHeadOrTail == 0) {
            buildHead();
        } else if (mHeadOrTail == 1) {
            buildTail();
        }

        //?????????????????????
        TempVideoParams.getInstance().setSubs(getWordinfos());

        //fragment
        mVirtualVideoView.setAutoRepeat(false); // ???????????????????????????
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mSubtitleFragment = SubtitleFragment.newInstance(mUIConfig.subUrl, mUIConfig.fontUrl);
        mSubtitleFragment.setExtractAudio(new SubtitleFragment.IExtractAudio() {
            @Override
            public List<Scene> getAudioSceneList() {
                List<Scene> list = new ArrayList<>();
                list.add(mScene.clone());
                return list;
            }
        });
        mSubtitleFragment.setFragmentContainer(findViewById(R.id.rlEditorMenuAndSubLayout));
        mSubtitleFragment.setHideApplyToAll(true);
        mSubtitleFragment.setThumbMoveItem(false);
        mSubtitleFragment.setHideAI();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.cover_subtitle, mSubtitleFragment);
        ft.commit();
    }

    /***
     * ????????????????????????????????????????????????????????????
     */
    private void addVirtualCover() {
        ThreadPoolUtils.executeEx(new Runnable() {
            @Override
            public void run() {
                //?????????????????????
                Bitmap bitmap = Bitmap.createBitmap(mVirtualVideoView.getVideoWidth(), mVirtualVideoView.getVideoHeight(), Bitmap.Config.ARGB_8888);
                BitmapFactory.Options options = new BitmapFactory.Options();
                if (mVirtualVideo.getSnapshot(CoverActivity.this, Utils.ms2s(getCurrentPosition()), bitmap)) {
                    String path = getCoverPath();
                    try {
                        com.rd.lib.utils.BitmapUtils.saveBitmapToFile(bitmap, true, 100, path);
                        bitmap.recycle();
                        mHandler.obtainMessage(MSG_COVER, path).sendToTarget();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    bitmap.recycle();
                }
            }
        });
    }

    /**
     * ????????????
     */
    private void addMediaCover() {
        SelectMediaActivity.appendMedia(CoverActivity.this, true, UIConfiguration.ALBUM_SUPPORT_IMAGE_ONLY, 1, REQUST_ABLUM_COVER);
    }

    /**
     * ??????????????????
     *
     * @return
     */
    private String getCoverPath() {
        return PathUtils.getTempFileNameForSdcard("Temp_virtual_cover", "png");
    }

    /**
     * ????????????????????????
     *
     * @param path
     */
    private void saveCover(String path) {
        if (mHeadOrTail == 0) {
            mHeadInfo.setPath(path);
        } else if (mHeadOrTail == 1) {
            mTailInfo.setPath(path);
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
     * ?????????????????????
     */
    private VirtualVideoView.VideoViewListener mPlayViewListener = new VirtualVideoView.VideoViewListener() {

        @Override
        public void onPlayerPrepared() {
            SysAlertDialog.cancelLoadingDialog();
            //???????????????
            tvTNDuration.setText(DateTimeUtils.stringForMillisecondTime(getDuration()));
            mSbPlayControl.setMax(getDuration());

            for (int nTmp = 0; nTmp < mSaEditorPostionListener.size(); nTmp++) {
                mSaEditorPostionListener.valueAt(nTmp).onEditorPrepred();
            }
            notifyCurrentPosition(Utils.s2ms(mVirtualVideoView.getCurrentPosition()));
        }

        @Override
        public void onPlayerCompletion() {
            onScrollCompleted();
            for (int nTmp = 0; nTmp < mSaEditorPostionListener.size(); nTmp++) {
                mSaEditorPostionListener.valueAt(nTmp).onEditorPreviewComplete();
            }
            notifyCurrentPosition(0);
        }

        @Override
        public void onGetCurrentPosition(float position) {
            onScrollProgress(Utils.s2ms(position));
            mSbPlayControl.setProgress(Utils.s2ms(position));
            notifyCurrentPosition(Utils.s2ms(position));
        }

        @Override
        public boolean onPlayerError(int what, int extra) {
            Log.e(TAG, "Player error:" + what + "," + extra);
            SysAlertDialog.cancelLoadingDialog();
            SysAlertDialog.showAlertDialog(CoverActivity.this,
                    "",
                    getString(R.string.preview_error),
                    getString(R.string.sure), null, null, null);
            return false;
        }

    };

    private int mHalfWidth;
    private int mStateSize = 0;
    private DisplayMetrics mDisplayMetrics;

    /**
     * ??????????????????
     */
    private void onInitThumbTimeLine() {
        mHalfWidth = mDisplayMetrics.widthPixels / 2;
        mTimeline.setHalfParentWidth(mHalfWidth - mStateSize);
        int duration = Utils.s2ms(mVirtualVideo.getDuration());
        int[] mSizeParams = mThumbNailLine.setDuration(duration, mTimeline.getHalfParentWidth());
        mTimeline.setLineWidth(mSizeParams[0]);
        mTimeline.setDuration(duration);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(mSizeParams[0]
                + 2 * mThumbNailLine.getpadding(), mSizeParams[1]);

        lp.setMargins(mTimeline.getHalfParentWidth() - mThumbNailLine.getpadding(),
                0, mHalfWidth - mThumbNailLine.getpadding(), 0);

        mThumbNailLine.setLayoutParams(lp);

        FrameLayout.LayoutParams lframe = new FrameLayout.LayoutParams(lp.width
                + lp.leftMargin + lp.rightMargin, lp.height);
        lframe.setMargins(0, 0, 0, 0);
        mLinearLayout.setLayoutParams(lframe);
        findViewById(R.id.word_hint_view).setVisibility(View.GONE);
    }

    /**
     * ??????????????????????????????????????????
     *
     * @param virtualVideo
     */
    private void onInitThumbTimeLine(VirtualVideo virtualVideo) {
        mThumbNailLine.setVirtualVideo(virtualVideo, false);
        mThumbNailLine.prepare(mTimeline.getHalfParentWidth() + mHalfWidth);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                onScrollProgress(0);
                mThumbNailLine.setStartThumb(mTimeline.getScrollX());
            }
        }, 100);
    }

    /**
     * ????????????
     */
    private void onScrollCompleted() {
        seekTo(0);
        onScrollTo(0);
        setProgressText(0);
        mSbPlayControl.setProgress(0);
        ivPlayState.setImageResource(R.drawable.edit_music_play);
    }

    /**
     * ??????????????????
     *
     * @param progress (??????ms)
     */
    private void onScrollProgress(int progress) {
        onScrollTo(getScrollX(progress));
        setProgressText(progress);
    }

    private int getScrollX(long progress) {
        return (int) (progress * (mThumbNailLine.getThumbWidth() / getDuration()));
    }

    /**
     * ??????????????????
     *
     * @param mScrollX ??????
     */
    private void onScrollTo(int mScrollX) {
        mTimeline.appScrollTo(mScrollX, true);
    }

    private void setProgressText(int progress) {
        tvTNProgress.setText(DateTimeUtils.stringForMillisecondTime(progress));
    }

    /**
     * ?????????????????? ??????
     *
     * @param time
     */
    private void resetCoverDurationText(float time) {
        DecimalFormat fnum = new DecimalFormat("##0.0");
        String dd = fnum.format(time);
        tvShowDuration.setText(dd + "s");
    }

    /**
     * ???????????????????????????
     *
     * @param time
     * @return
     */
    private float calculateProgress(float time, int max) {
        float progress = time / mMaxCoverDuration * max;
        return progress;
    }

    /**
     * ???????????????????????????
     *
     * @return
     */
    private float calculateTime(float progress, int max) {
        float speed = progress / max * mMaxCoverDuration;
        return speed;
    }

    /**
     * ??????????????????
     */
    private void setSeekBarDuration() {
        float t = 0;
        if (mHeadOrTail == 0) {
            resetCoverDurationText(mCoverDurationHead);
            t = calculateProgress(mCoverDurationHead, 100);
        } else {
            resetCoverDurationText(mCoverDurationTail);
            t = calculateProgress(mCoverDurationTail, 100);
        }
        mSeekbarDuration.setProgress(t);
    }

    //????????????????????????
    public static final int REQUST_ABLUM_COVER = 600;
    public static final int REQUST_CROP_COVER = 601;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        buildAll();
        if (requestCode == REQUST_CROP_COVER) {
            if (resultCode == Activity.RESULT_OK) {
                Scene scene = data.getParcelableExtra(IntentConstants.INTENT_EXTRA_SCENE);
                if (null != scene) {
                    final VirtualVideo virtualVideo = new VirtualVideo();
                    final float asp = mPreviewAsp;
                    scene.setDisAspectRatio(asp);
                    virtualVideo.addScene(scene);
                    final float nTime = scene.getDuration() * 2 / 3;
                    ThreadPoolUtils.executeEx(new Runnable() {
                        @Override
                        public void run() {
                            Bitmap bitmap = Bitmap.createBitmap(mVirtualVideoView.getVideoWidth(), mVirtualVideoView.getVideoHeight(), Bitmap.Config.ARGB_8888);
                            if (virtualVideo.getSnapshot(CoverActivity.this, nTime, bitmap)) {
                                String path = getCoverPath();
                                try {
                                    BitmapUtils.saveBitmapToFile(bitmap, true, 100, path);
                                    mHandler.obtainMessage(MSG_COVER, MSG_ARG1_BUILD, 0, path).sendToTarget();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            bitmap.recycle();
                        }
                    });
                    //??????onResume ??????????????????????????????????????????prepared????????? ??????reload ????????????????????????
                }
            }
        } else if (requestCode == REQUST_ABLUM_COVER) {
            if (resultCode == Activity.RESULT_OK) {
                ArrayList<String> medias = data.getStringArrayListExtra(SdkEntry.ALBUM_RESULT);
                if (null != medias && medias.size() > 0) {
                    try {
                        MediaObject mMediaCover = new MediaObject(medias.get(0));
                        mMediaCover.setClearImageDefaultAnimation(true);
                        Scene scene = VirtualVideo.createScene();
                        scene.addMedia(mMediaCover);
                        CropRotateMirrorActivity.onAECropRotateMirror(this, scene, mPreviewAsp, true, false, REQUST_CROP_COVER);
                    } catch (InvalidArgumentException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private final int MSG_COVER = 1900;
    private final int MSG_ARG1_BUILD = 200;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_COVER) {
                SysAlertDialog.cancelLoadingDialog();
                setStatus(0);
                //????????????  path msg.obj
                saveCover((String) msg.obj);
                buildAll();
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SysAlertDialog.cancelLoadingDialog();
        SubUtils.getInstance().recycle();
        TTFUtils.recycle();
        TTFData.getInstance().close();
        SubData.getInstance().close();
        if (null != mVirtualVideoView) {
            mVirtualVideoView.cleanUp();
            mVirtualVideoView = null;
        }
        if (null != mVirtualVideo) {
            mVirtualVideo.release();
            mVirtualVideo = null;
        }
        if (null != mSaEditorPostionListener) {
            mSaEditorPostionListener.clear();
            mSaEditorPostionListener = null;
        }
        mSubtitleFragment = null;
        TempVideoParams.getInstance().recycle();
        mHandler.removeCallbacksAndMessages(null);
    }

    /**
     * ?????? ??????
     */
    private Button btnHead;
    private Button btnTail;

    /**
     * ???????????? ?????? ?????? ??????
     *
     * @return
     */
    public boolean isPlaying() {
        return mVirtualVideoView != null && mVirtualVideoView.isPlaying();
    }

    public void start() {
        if (mVirtualVideoView == null) {
            return;
        }
        mVirtualVideoView.start();
    }

    public void pause() {
        if (isPlaying()) {
            mVirtualVideoView.pause();
        }
    }

    public void stop() {
        if (mVirtualVideoView == null) {
            return;
        }
        mVirtualVideoView.stop();
    }

    /**
     * ??????
     */
    private void seekTo() {
        if (mHeadOrTail == 0) {
            seekTo(0);
        } else if (mHeadOrTail == 1) {
            if (mTailInfo.getPath() == null) {
                if (mHeadInfo.getPath() == null) {
                    seekTo(mDuration - 100);
                } else {
                    seekTo(mDuration + Utils.s2ms(mCoverDurationHead) - 100);
                }
            } else {
                if (mHeadInfo.getPath() == null) {
                    seekTo(mDuration + Utils.s2ms(mCoverDurationTail / 5));
                } else {
                    seekTo(mDuration + Utils.s2ms(mCoverDurationHead + mCoverDurationTail / 5));
                }
            }
        }
    }

    /**
     * ?????? ??????
     *
     * @param msec
     */
    public void seekTo(int msec) {
        mVirtualVideoView.seekTo(Utils.ms2s(msec));
        mSbPlayControl.setProgress(msec);
    }

    /**
     * ???????????? ??????
     *
     * @return
     */
    public int getDuration() {
        if (null == mVirtualVideoView) {
            return 1;
        }
        return Utils.s2ms(mVirtualVideoView.getDuration());
    }

    /**
     * ??????????????????
     *
     * @return
     */
    public int getCurrentPosition() {
        if (null != mVirtualVideoView) {
            return Utils.s2ms(mVirtualVideoView.getCurrentPosition());
        } else {
            return 0;
        }
    }

    /**
     * ????????????????????????
     */
    private void onShowAlert() {
        SysAlertDialog.createAlertDialog(this,
                getString(R.string.dialog_tips),
                getString(R.string.cancel_all_changed),
                getString(R.string.cancel),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }, getString(R.string.sure),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                }, false, null).show();
    }

    public void setWordinfos(ArrayList<WordInfo> wordinfos) {
        if (mHeadOrTail == 0) {
            mHeadWordInfos.clear();
            mHeadWordInfos.addAll(wordinfos);
        } else if (mHeadOrTail == 1) {
            mTailWordInfos.clear();
            mTailWordInfos.addAll(wordinfos);
        }
    }

    private ArrayList<WordInfo> getWordinfos() {
        if (mHeadOrTail == 0) {
            return mHeadWordInfos;
        } else if (mHeadOrTail == 1) {
            return mTailWordInfos;
        }
        return null;
    }

    @Override
    public boolean isMediaMute() {
        return false;
    }

    @Override
    public void reload(boolean onlyMusic) {
        if (mState == 3) {
            //??????build
            if (mHeadOrTail == 0) {
                buildHead();
            } else if (mHeadOrTail == 1) {
                buildTail();
            }
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
    public VirtualVideoView getEditor() {
        if (mVirtualVideoView != null) {
            return mVirtualVideoView;
        } else {
            return null;
        }
    }

    @Override
    public FrameLayout getSubEditorParent() {
        return mLinearWords;
    }

    private VirtualVideo mSnapshotEditor;

    @Override
    public VirtualVideo getSnapshotEditor() {
        //???????????????????????????????????????
        mSnapshotEditor = null;
        mSnapshotEditor = new VirtualVideo();
        if (mHeadOrTail == 0) {
            mSnapshotEditor.addScene(getHeadScene());
        } else if (mHeadOrTail == 1) {
            mSnapshotEditor.addScene(getTailScene());
        } else {
            mSnapshotEditor.addScene(mScene);
        }
        return mSnapshotEditor;
    }

    @Override
    public void cancelLoading() {
        SysAlertDialog.cancelLoadingDialog();
    }

    /*
     * ??????EditorPreivewPositionListener?????????
     */
    private SparseArray<EditorPreivewPositionListener> mSaEditorPostionListener = new SparseArray<EditorPreivewPositionListener>();

    @Override
    public void registerEditorPostionListener(EditorPreivewPositionListener listener) {
        mSaEditorPostionListener.append(listener.hashCode(), listener);
    }

    @Override
    public void unregisterEditorProgressListener(EditorPreivewPositionListener listener) {
        mSaEditorPostionListener.remove(listener.hashCode());
    }

    private void notifyCurrentPosition(int positionMs) {
        for (int nTmp = 0; nTmp < mSaEditorPostionListener.size(); nTmp++) {
            mSaEditorPostionListener.valueAt(nTmp).onEditorGetPosition(
                    positionMs, Utils.s2ms(mVirtualVideoView.getDuration()));
        }
    }

    @Override
    public void onBack() {
        if (mState == 3) {
            mState = 0;
            buildAll();
            setFeaturesUI();
        } else {
            onBackPressed();
        }
        seekTo();
    }

    @Override
    public void onSure() {
        if (mState == 3) {
            //????????????
            setWordinfos(TempVideoParams.getInstance().getWordInfos());
            //??????build??????
            buildAll();
            setStatus(0);
        } else if (mState == 1) {
            //??????
            if (mHeadOrTail == 0) {
                mCoverDurationHead = calculateTime(mChangeCoverDuration, 100);
                resetCoverDurationText(mCoverDurationHead);
                mHeadInfo.setDuration(mCoverDurationHead);
                for (WordInfo wordInfo : mHeadWordInfos) {
                    wordInfo.setEnd(Utils.s2ms(mHeadInfo.getDuration()));
                }
            } else {
                mCoverDurationTail = calculateTime(mChangeCoverDuration, 100);
                resetCoverDurationText(mCoverDurationTail);
                mTailInfo.setDuration(mCoverDurationTail);
                for (WordInfo wordInfo : mTailWordInfos) {
                    wordInfo.setEnd(Utils.s2ms(mTailInfo.getDuration()));
                }
            }
            buildAll();
            mState = 0;
            setFeaturesUI();
        } else if (mState == 2) {
            //????????????
            if (isPlaying()) {
                pause();
            } else {
                SysAlertDialog.showLoadingDialog(this, getString(R.string.loading));
                addVirtualCover();
            }
        } else if (mState == 0) {
            //??????
            if (mState != 0) {
                return;
            }
            mVirtualVideoView.stop();
            com.rd.veuisdk.ExportHandler exportHandler = new com.rd.veuisdk.ExportHandler( this, new com.rd.veuisdk.ExportHandler.IExport() {

                @Override
                public void addData(VirtualVideo virtualVideo) {
                    buildOrExport(virtualVideo);
                }
            });
            exportHandler.onExport(mVirtualVideoView.getVideoWidth() / mVirtualVideoView.getVideoHeight(), true);
        } else {
            onBackPressed();
        }
    }

    @Override
    public void onBackPressed() {
        if (mState == 3) {
            //??????
            mSubtitleFragment.onBackPressed();
        } else if (mState == 2) {
            //????????????
            buildAll();
            setStatus(0);
        } else if (mState == 1) {
            //??????
            mState = 0;
            setFeaturesUI();
        } else if (mState != 0) {
            setStatus(0);
        } else {
            onShowAlert();
        }
    }

    public VirtualVideo getVirtualVideo() {
        return mVirtualVideo;
    }

    @Override
    public void changeFilterType(int index, int nFilterType) {

    }

    @Override
    public void changeFilterLookup(VisualFilterConfig lookup, int index) {

    }

    @Override
    public int getCurrentLookupIndex() {
        return 0;
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
    public void removeMvMusic(boolean remove) {

    }

    @Override
    public void changeAnimation(int animation) {

    }

    @Override
    public void setAETemplateInfo(AETemplateInfo aeTemplateInfo) {

    }

    class CoverInfo {

        //????????????
        private String mPath = null;
        //?????? ???
        private float mDuration;

        public CoverInfo() {
            mDuration = 1;
        }

        public String getPath() {
            return mPath;
        }

        public void setPath(String path) {
            this.mPath = path;
        }

        public float getDuration() {
            return mDuration;
        }

        public void setDuration(float duration) {
            duration = duration == 0 ? 0.1f : duration;//?????????????????????0
            this.mDuration = duration;
        }

    }
}
