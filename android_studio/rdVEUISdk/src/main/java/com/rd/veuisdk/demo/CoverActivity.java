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
     * 请求权限code:读取外置存储
     */
    private final int REQUEST_CODE_READ_EXTERNAL_STORAGE_PERMISSIONS = 1;
    /*
     * VirtualVideoView播放器对象   视频接口类对象
     */
    private VirtualVideoView mVirtualVideoView;
    private VirtualVideo mVirtualVideo;
    /**
     * 界面配置对象
     */
    private UIConfiguration mUIConfig = null;
    /*
     * 字幕  容器
     */
    private FrameLayout mLinearWords;
    private PreviewFrameLayout mPlayerContainer, mWordContainer;
    //增加 时长 缩略图 文字
    private View addLayout, durationLayout, thumbnailLayout,
            subtitleLayout;
    //时长显示
    private TextView tvShowDuration;
    private RulerSeekbar mSeekbarDuration;
    private ImageView ivPlayState;
    //截取视频本身
    private ThumbNailLine mThumbNailLine;
    private TimelineHorizontalScrollView mTimeline;
    private LinearLayout mLinearLayout;
    private TextView tvTNProgress, tvTNDuration;
    /**
     * 场景
     */
    private Scene mScene = null;
    /**
     * 源时长 毫秒
     */
    private int mDuration = 0;
    /**
     * 片头还是片尾 0 片头 1片尾
     */
    private int mHeadOrTail = 0;
    /**
     * 片头 片尾
     */
    private CoverInfo mHeadInfo = new CoverInfo();
    private CoverInfo mTailInfo = new CoverInfo();
    /**
     * 封面时长
     */
    private float mMaxCoverDuration = 2f;
    private float mCoverDurationHead = mMaxCoverDuration / 2;//默认1秒
    private float mCoverDurationTail = mMaxCoverDuration / 2;//默认1秒
    private float mChangeCoverDuration = mMaxCoverDuration / 2;//调整封面时长记录调整前
    /**
     * 功能状态
     * 时长1  截取2  文字3
     */
    private int mState = 0;
    //进度
    private SeekBar mSbPlayControl;
    //预览比例
    private float mPreviewAsp = 0;
    /**
     * 字幕实例
     */
    private SubtitleFragment mSubtitleFragment;
    /**
     * 片头、片尾信息保存
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
        //修正播放器容器显示比例
        AppConfiguration.fixAspectRatio(this);
        // 添加api 23权限控制
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
        //片头、片尾
        btnHead = findViewById(R.id.btnHead);
        btnTail = findViewById(R.id.btnTail);
        //添加、时长、缩略图、字幕
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
        //取消、确认、添加图片、文字、时长
        findViewById(R.id.ivCancel).setOnClickListener(this);
        findViewById(R.id.ivSure).setOnClickListener(this);
        findViewById(R.id.tvAddPicture).setOnClickListener(this);
        findViewById(R.id.tvAddText).setOnClickListener(this);
        findViewById(R.id.tvDuration).setOnClickListener(this);

        ((TextView) findViewById(R.id.tvBottomTitle)).setText(getString(R.string.cover));

        //播放器
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

        //缩略图
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

        //封面时长
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
        //获取场景
        mScene = getIntent().getParcelableExtra(IntentConstants.INTENT_EXTRA_SCENE);
        if (mScene == null) {
            //没有媒体
            onToast(R.string.no_media);
            finish();
            return;
        }
        SubData.getInstance().initilize(this);//字幕初始化数据库
        TTFData.getInstance().initilize(this);//字体初始化数据库
        //uiconfig
        mUIConfig = SdkEntry.getSdkService().getUIConfig();
        mStateSize = getResources().getDimensionPixelSize(R.dimen.add_sub_play_state_size);
        mDisplayMetrics = CoreUtils.getMetrics();
        buildAll();
        //计算预览size
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
     * 计算预览比例
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
            //确认
            onSure();
        } else if (id == R.id.ivCancel) {
            //取消
            onBackPressed();
        } else if (id == R.id.tvAddPicture) {
            //添加图片
            addPictureDialog();
            setStatus(0);
        } else if (id == R.id.tvAddText) {
            //添加文字
            if (mHeadOrTail == 0 && mHeadInfo.getPath() == null) {
                Toast.makeText(this, getString(R.string.cover_prompt_add_image), Toast.LENGTH_SHORT).show();
                return;
            }
            if (mHeadOrTail == 1 && mTailInfo.getPath() == null) {
                Toast.makeText(this, getString(R.string.cover_prompt_add_image), Toast.LENGTH_SHORT).show();
                return;
            }
            //底部显示
            setStatus(3);
            addText();
        } else if (id == R.id.tvDuration) {
            //设置片头、片尾时长
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
     * 片头 图片场景
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
     * 片尾 图片场景
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
     * 片头(点击添加字幕)
     */
    private void buildHead() {
        SysAlertDialog.showLoadingDialog(CoverActivity.this, R.string.isloading);
        mVirtualVideoView.reset();
        mVirtualVideo.reset();
        mVirtualVideo.addScene(getHeadScene());
        //字幕
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
     * 片尾(点击添加字幕)
     */
    private void buildTail() {
        SysAlertDialog.showLoadingDialog(CoverActivity.this, R.string.isloading);
        mVirtualVideoView.reset();
        mVirtualVideo.reset();
        mVirtualVideo.addScene(getTailScene());
        //字幕
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
     * 截图
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
     * 导出 添加所有的
     *
     * @param virtualVideo
     */
    private void buildOrExport(VirtualVideo virtualVideo) {

        //片头
        Scene headScene = getHeadScene();
        if (headScene != null) {
            virtualVideo.addScene(headScene);
        }
        virtualVideo.addScene(mScene);
        //片尾
        Scene TailScene = getTailScene();
        if (TailScene != null) {
            virtualVideo.addScene(TailScene);
        }

        //片头字幕
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
        //片尾
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
     * 全部
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
     * 根据状态 mState 底部显示
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
            //添加文字
            addLayout.setVisibility(View.GONE);
            durationLayout.setVisibility(View.GONE);
            thumbnailLayout.setVisibility(View.GONE);
            subtitleLayout.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 点击功能 UI设置 设置底部显示、回到0、暂停
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
     * 设置片头片尾按钮背景
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
     * 添加图片来源 弹窗
     */
    private void addPictureDialog() {
        String[] menu = getResources().getStringArray(R.array.cover_picture);
        SysAlertDialog.showListviewAlertMenu(this, getString(R.string.cover_select), menu,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (which == 0) {
                            //相册
                            addMediaCover();
                        } else if (which == 1) {
                            //截图
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
     * 添加文字
     */
    private void addText() {
        //只能在片头片尾加字幕
        if (mHeadOrTail == 0) {
            buildHead();
        } else if (mHeadOrTail == 1) {
            buildTail();
        }

        //设置保存的字幕
        TempVideoParams.getInstance().setSubs(getWordinfos());

        //fragment
        mVirtualVideoView.setAutoRepeat(false); // 字幕不需要自动重播
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
     * 从虚拟视频中获取指定时间点的画面作为封面
     */
    private void addVirtualCover() {
        ThreadPoolUtils.executeEx(new Runnable() {
            @Override
            public void run() {
                //必须子线程取图
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
     * 添加媒体
     */
    private void addMediaCover() {
        SelectMediaActivity.appendMedia(CoverActivity.this, true, UIConfiguration.ALBUM_SUPPORT_IMAGE_ONLY, 1, REQUST_ABLUM_COVER);
    }

    /**
     * 临时文件位置
     *
     * @return
     */
    private String getCoverPath() {
        return PathUtils.getTempFileNameForSdcard("Temp_virtual_cover", "png");
    }

    /**
     * 保存封面图片地址
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
     * 播放器预览回调
     */
    private VirtualVideoView.VideoViewListener mPlayViewListener = new VirtualVideoView.VideoViewListener() {

        @Override
        public void onPlayerPrepared() {
            SysAlertDialog.cancelLoadingDialog();
            //设置总时长
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
     * 缩略图初始化
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
     * 初始化缩略图时间轴和恢复数据
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
     * 播放完成
     */
    private void onScrollCompleted() {
        seekTo(0);
        onScrollTo(0);
        setProgressText(0);
        mSbPlayControl.setProgress(0);
        ivPlayState.setImageResource(R.drawable.edit_music_play);
    }

    /**
     * 播放中的进度
     *
     * @param progress (单位ms)
     */
    private void onScrollProgress(int progress) {
        onScrollTo(getScrollX(progress));
        setProgressText(progress);
    }

    private int getScrollX(long progress) {
        return (int) (progress * (mThumbNailLine.getThumbWidth() / getDuration()));
    }

    /**
     * 设置播放进度
     *
     * @param mScrollX 像素
     */
    private void onScrollTo(int mScrollX) {
        mTimeline.appScrollTo(mScrollX, true);
    }

    private void setProgressText(int progress) {
        tvTNProgress.setText(DateTimeUtils.stringForMillisecondTime(progress));
    }

    /**
     * 显示封面时长 格式
     *
     * @param time
     */
    private void resetCoverDurationText(float time) {
        DecimalFormat fnum = new DecimalFormat("##0.0");
        String dd = fnum.format(time);
        tvShowDuration.setText(dd + "s");
    }

    /**
     * 根据时间计算进度条
     *
     * @param time
     * @return
     */
    private float calculateProgress(float time, int max) {
        float progress = time / mMaxCoverDuration * max;
        return progress;
    }

    /**
     * 根据进度条计算时间
     *
     * @return
     */
    private float calculateTime(float progress, int max) {
        float speed = progress / max * mMaxCoverDuration;
        return speed;
    }

    /**
     * 设置封面时长
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

    //添加图片以后返回
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
                    //因为onResume 无法实时插入，需要等到播放器prepared，索性 整体reload （防止界面闪烁）
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
                //保存封面  path msg.obj
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
     * 片头 片尾
     */
    private Button btnHead;
    private Button btnTail;

    /**
     * 判断播放 开始 暂停 停止
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
     * 跳转
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
     * 跳转 毫秒
     *
     * @param msec
     */
    public void seekTo(int msec) {
        mVirtualVideoView.seekTo(Utils.ms2s(msec));
        mSbPlayControl.setProgress(msec);
    }

    /**
     * 获取时长 毫秒
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
     * 获取当前进度
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
     * 提示是否放弃保存
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
            //重新build
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
        //截图与预览的虚拟视频需分开
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
     * 盛放EditorPreivewPositionListener的列表
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
            //保存字幕
            setWordinfos(TempVideoParams.getInstance().getWordInfos());
            //重新build全部
            buildAll();
            setStatus(0);
        } else if (mState == 1) {
            //时长
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
            //截取本身
            if (isPlaying()) {
                pause();
            } else {
                SysAlertDialog.showLoadingDialog(this, getString(R.string.loading));
                addVirtualCover();
            }
        } else if (mState == 0) {
            //导出
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
            //字幕
            mSubtitleFragment.onBackPressed();
        } else if (mState == 2) {
            //截取本身
            buildAll();
            setStatus(0);
        } else if (mState == 1) {
            //时长
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

        //图片地址
        private String mPath = null;
        //时长 秒
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
            duration = duration == 0 ? 0.1f : duration;//避免出现时间为0
            this.mDuration = duration;
        }

    }
}
