package com.rd.veuisdk;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.rd.lib.ui.ExtButton;
import com.rd.lib.ui.PreviewFrameLayout;
import com.rd.lib.utils.LogUtil;
import com.rd.lib.utils.ThreadPoolUtils;
import com.rd.vecore.Music;
import com.rd.vecore.VirtualVideo;
import com.rd.vecore.VirtualVideoView;
import com.rd.vecore.exception.InvalidArgumentException;
import com.rd.vecore.exception.InvalidStateException;
import com.rd.vecore.models.AEFragmentInfo;
import com.rd.vecore.models.AspectRatioFitMode;
import com.rd.vecore.models.BlendEffectObject;
import com.rd.vecore.models.MediaObject;
import com.rd.vecore.models.MediaType;
import com.rd.vecore.models.Scene;
import com.rd.vecore.models.VideoConfig;
import com.rd.vecore.utils.AEFragmentUtils;
import com.rd.vecore.utils.MiscUtils;
import com.rd.veuisdk.adapter.AEMediaAdapter;
import com.rd.veuisdk.ae.model.AETemplateInfo;
import com.rd.veuisdk.ae.model.AETextLayerInfo;
import com.rd.veuisdk.ae.model.BackgroundMedia;
import com.rd.veuisdk.database.HistoryMusicCloud;
import com.rd.veuisdk.manager.ExportConfiguration;
import com.rd.veuisdk.manager.UIConfiguration;
import com.rd.veuisdk.model.AEMediaInfo;
import com.rd.veuisdk.model.AudioMusicInfo;
import com.rd.veuisdk.model.VideoOb;
import com.rd.veuisdk.ui.RdSeekBar;
import com.rd.veuisdk.utils.BitmapUtils;
import com.rd.veuisdk.utils.DateTimeUtils;
import com.rd.veuisdk.utils.IntentConstants;
import com.rd.veuisdk.utils.PathUtils;
import com.rd.veuisdk.utils.SysAlertDialog;
import com.rd.veuisdk.utils.ThumbNailUtils;
import com.rd.veuisdk.utils.Utils;
import com.rd.veuisdk.utils.ViewUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 预览AE效果，选择媒体
 */
public class AEPreviewActivity extends BaseActivity {
    private static final String PARAM_AE = "ae_Info";
    private static final String PARAM_AE_MEDIA = "ae_Info_media";
    private static final String PARAM_AE_ENABLED_REPEAT = "ae_enabled_repeat";

    /**
     * 进入AE预览
     */
    static void gotoAEPreview(Context context, AETemplateInfo aeTemplateInfo, ArrayList<MediaObject> list,
                              boolean enableRepeat, int requestCode) {
        Intent intent = new Intent(context, AEPreviewActivity.class);
        intent.putExtra(PARAM_AE, aeTemplateInfo);
        intent.putExtra(PARAM_AE_MEDIA, list);
        intent.putExtra(PARAM_AE_ENABLED_REPEAT, enableRepeat);
        ((Activity) context).startActivityForResult(intent, requestCode);
    }


    private PreviewFrameLayout mPreviewFrame, mContentFrame;
    private ExtButton mBtnNext, mBtnLeft;
    private TextView mTvTitle;
    private VirtualVideoView player;
    private ImageView mIvVideoPlayState;
    private TextView mCurrentTv;
    private TextView mTotalTv;
    private RecyclerView mRecyclerView;
    private AETemplateInfo mAETemplateInfo;
    private VirtualVideo mVirtualVideo;
    private Music mBuildMusic = null;
    private boolean mEnabledRepeat = false;
    private boolean mInterceptRepeatClick = false;
    private ArrayList<MediaObject> picList = new ArrayList<>(), videoList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        TAG = "AEPreviewActivity";
        super.onCreate(savedInstanceState);
        mAETemplateInfo = getIntent().getParcelableExtra(PARAM_AE);
        mEnabledRepeat = getIntent().getBooleanExtra(PARAM_AE_ENABLED_REPEAT, false);
        if (null == mAETemplateInfo) {
            finish();
            return;
        }

        setContentView(R.layout.activity_ae_preview_layout);
        $(R.id.titlebar_layout).setBackgroundColor(getResources().getColor(R.color.transparent));
        findViewById(R.id.btnResetMusic).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onResetMusic();
            }
        });

        mVirtualVideo = new VirtualVideo();
        initView();
        IntentFilter intentFilter = new IntentFilter(SdkEntry.MSG_EXPORT);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, intentFilter);
        initPlayerListener(player);

        ArrayList<MediaObject> aeMedias = getIntent().getParcelableArrayListExtra(PARAM_AE_MEDIA);

        if (null != aeMedias) {
            if (mAETemplateInfo.getPicNum() == 0 && mAETemplateInfo.getVideoNum() > 0) {
                //190505 没有图片时,把所有媒体全部当成视频处理
                videoList.addAll(aeMedias);
            } else {
                int len = aeMedias.size();
                for (int i = 0; i < len; i++) {
                    MediaObject item = aeMedias.get(i);
                    if (item.getMediaType() == MediaType.MEDIA_VIDEO_TYPE) {
                        videoList.add(item);
                    } else {
                        picList.add(item);
                    }
                }
            }
        }  //防止null  (纯文字板)

        initAEFragment();

        SysAlertDialog.showLoadingDialog(this, R.string.isloading);
    }


    /**
     * 恢复默认音乐
     */
    private void onResetMusic() {
        pause();
        mTvMusic.setText(R.string.ae_preview_change_music_btn);
        mMusic = null;
        aeReload(true);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!AEPreviewActivity.this.isDestroyed()) {
                String action = intent.getAction();
                if (TextUtils.equals(action, SdkEntry.MSG_EXPORT)) {
                    withWatermark = intent.getBooleanExtra(SdkEntry.EXPORT_WITH_WATERMARK, true);
                    onExport();
                }
            }
        }
    };

    /**
     * 记录重复的AE模板repeat  (不包含 mAETemplateInfo中绑定的模板)
     */
    private List<AEFragmentInfo> mAEList = new ArrayList<>();
    //AE模板
    private AEFragmentInfo mAEFragmentInfo;

    /**
     * 解析出AE模板，需要视频、图片 、文字版， 各种layer的时间线，用来匹配SelectMedia返回的数据
     */
    private void initAEFragment() {
        try {
            mAEList.clear();
            AEFragmentUtils.load(mAETemplateInfo.getDataPath(), new AEFragmentUtils.AEFragmentListener() {

                /**
                 * 响应加载完成后
                 * @param aeFragmentInfo AE片段对象
                 */
                @Override
                public void onLoadComplete(AEFragmentInfo aeFragmentInfo) {
                    mList.clear();
                    if (aeFragmentInfo != null) {
                        if (!mEnabledRepeat) {
                            fixAEVideoList(aeFragmentInfo); //校准资源类型（视频/图片）
                        }
                        mAEFragmentInfo = aeFragmentInfo;
                        mAETemplateInfo.setAEFragmentInfo(false, aeFragmentInfo);
                        {
                            //layer绑定媒体
                            int nPicIndex = 0, nVideoIndex = 0;
                            int editLayerCount = initItemAE(nPicIndex, nVideoIndex, picList, videoList, aeFragmentInfo);
                            mAETemplateInfo.setEditLayerNum(editLayerCount);
                            //多余的图片媒体-用来循环 (*********只使用纯图片的AE模板)
                            int count = (int) Math.ceil(picList.size() / (editLayerCount + 0.0f)) - 1;
                            if (mEnabledRepeat && count > 0) {
                                //需要for循环
                                for (int i = 0; i < count; i++) {
                                    AEFragmentInfo tmp = aeFragmentInfo.clone();
                                    initItemAE((editLayerCount * (i + 1)), nVideoIndex, picList, videoList, tmp);
                                    mAEList.add(tmp);
                                }
                            }
                            mAdapter.update(mList);
                        }
                    }
                    aeReload(false);
                }

                @Override
                public void onLoadFailed(int errorCode, String message) {
                    Log.e(TAG, message);
                    initPlayerData();
                }
            });
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        }

    }


    /**
     * 修正视频集合 （场景：视频类型的layer，选中的是图片资源  ）
     */
    private void fixAEVideoList(AEFragmentInfo aeFragmentInfo) {
        List<AEFragmentInfo.LayerInfo> layers = aeFragmentInfo.getLayers();
        int len = layers.size();
        int nVideoIndex = 0; //视频类型
        int nPicIndex = 0; //图片类型
        for (int i = 0; i < len; i++) {
            AEFragmentInfo.LayerInfo layerInfo = layers.get(i);
            if (layerInfo.getLayerType() == AEFragmentInfo.LayerType.EDIT) {
                String name = layerInfo.getName();
                if (name.startsWith("ReplaceableVideoOrPic")) {
                    nVideoIndex++;
                    if (videoList.size() < nVideoIndex) { //视频集合不足时，从图片集合找
                        //从图片集合中移除一个到视频集合中 (移动的这个资源必须是 （图片类型已经排序的后面的index)）
                        if (nPicIndex < picList.size()) {
                            videoList.add(picList.remove(nPicIndex));
                        }
                    }
                } else if (name.startsWith("ReplaceablePic")) {
                    nPicIndex++;
                } else if (name.startsWith("ReplaceableText")) {
                } else {
                    //防止模板所需要的媒体没有分类 (比如：爱心9宫格 、iphoneX，layer当成图片处理)
                    nPicIndex++;
                }
            }
        }
    }


    /**
     * 给AE模板内的layer 匹配指定类型的数据
     *
     * @return 当前模板下的需要编辑的layer
     */
    private int initItemAE(int nPicIndex, int nVideoIndex, final ArrayList<MediaObject> picList, final ArrayList<MediaObject> videoList, AEFragmentInfo aeFragmentInfo) {
        List<AEFragmentInfo.LayerInfo> layers = aeFragmentInfo.getLayers();
        int len = layers.size();
        int editLayer = 0;
        for (int i = 0; i < len; i++) {
            AEFragmentInfo.LayerInfo layerInfo = layers.get(i);
            if (layerInfo.getLayerType() == AEFragmentInfo.LayerType.EDIT) {
                editLayer++;
                String name = layerInfo.getName();
                float duration = (layerInfo.getEndTime() - layerInfo.getStartTime());
                if (name.startsWith("ReplaceableVideoOrPic")) {
                    MediaObject mediaObject = nVideoIndex >= videoList.size() ? null : videoList.get(nVideoIndex);
                    mList.add(new AEMediaInfo(AEMediaInfo.MediaType.VIDEO, layerInfo.getAspectRatio(), duration, mediaObject));
                    nVideoIndex++;
                } else if (name.startsWith("ReplaceablePic")) {
                    MediaObject mediaObject = nPicIndex >= picList.size() ? null : picList.get(nPicIndex);
                    mList.add(new AEMediaInfo(AEMediaInfo.MediaType.IMAGE, layerInfo.getAspectRatio(), duration, mediaObject));
                    nPicIndex++;
                } else if (name.startsWith("ReplaceableText")) {
                    AETextLayerInfo aeTextLayerInfo = mAETemplateInfo.getTargetAETextLayer(layerInfo.getName());
                    String file = AETextActivity.fixAEText(aeTextLayerInfo, aeTextLayerInfo.getTextContent(), aeTextLayerInfo.getTtfPath());
                    MediaObject mediaObject = null;
                    try {
                        mediaObject = new MediaObject(this, file);
                    } catch (InvalidArgumentException e) {
                        e.printStackTrace();
                    }
                    AEMediaInfo aeMediaInfo = new AEMediaInfo(aeTextLayerInfo, AEMediaInfo.MediaType.TEXT,
                            layerInfo.getAspectRatio(), duration, mediaObject);
                    aeMediaInfo.setText(aeTextLayerInfo.getTextContent());
                    mList.add(aeMediaInfo);
                } else {
                    //防止模板所需要的媒体没有分类 (比如：爱心9宫格 、iphoneX，layer当成图片处理)
                    MediaObject mediaObject = nPicIndex >= picList.size() ? null : picList.get(nPicIndex);
                    //默认的，全选图
                    mList.add(new AEMediaInfo(AEMediaInfo.MediaType.IMAGE, layerInfo.getAspectRatio(),
                            (layerInfo.getEndTime() - layerInfo.getStartTime()), mediaObject));
                    nPicIndex++;
                }
            }
        }
        return editLayer;
    }

    /***
     * 按照显示比例裁剪一个有效的区域
     *
     * @param mediaObject   媒体
     * @param asp   目标显示比例
     */
    private void setDefaultClipRect(MediaObject mediaObject, float asp) {
        if ((mediaObject.getClipRectF() == null || mediaObject.getClipRectF().isEmpty())) {
            int tW = mediaObject.getWidth();
            int tH = mediaObject.getHeight();
            if (mediaObject.getAngle() == 90 || mediaObject.getAngle() == 270) {
                int tmp = tW;
                tW = tH;
                tH = tmp;
            }
            Rect rect = new Rect();
            MiscUtils.fixClipRect(asp, tW, tH, rect);
            Object tag = mediaObject.getTag();
            if (!(tag instanceof VideoOb)) {
                VideoOb videoOb = VideoOb.createVideoOb(mediaObject.getMediaPath());
                mediaObject.setTag(videoOb);
            }
            mediaObject.setClipRect(rect);
        }
    }

    private void aeReload(boolean showLoadingDialog) {
        if (showLoadingDialog) {
            SysAlertDialog.showLoadingDialog(this, R.string.isloading);
        }
        ThreadPoolUtils.executeEx(new ThreadPoolUtils.ThreadPoolRunnable() {
            @Override
            public void onBackground() {
                mMediaObjects.clear();
                for (AEMediaInfo aeMediaInfo : mList) {
                    MediaObject tmp = aeMediaInfo.getMediaObject();
                    //允许传null 进行占位，比如：爱心9拼格
                    if (null != tmp) {
                        setDefaultClipRect(tmp, aeMediaInfo.getAsp());
                    }
                    mMediaObjects.add(tmp);
                }
                mAETemplateInfo.setMediaObjects(mMediaObjects);
                //AE模板
                if (mAEFragmentInfo != null) {
                    mAETemplateInfo.setAEFragmentInfo(false, mAEFragmentInfo);
                }
            }

            @Override
            @MainThread
            public void onEnd() {
                //设置数据
                setOtherData();
                //初始化播放器
                initPlayerData();
            }
        });

    }

    /**
     * 给循环的其他AE模板设置数据
     */
    private void setOtherData() {
        int len = mAEList.size();
        int index = mAETemplateInfo.getEditLayerNum();
        for (int i = 0; i < len; i++) {
            AEFragmentInfo tmp = mAEList.get(i);
            List<AEFragmentInfo.LayerInfo> list = tmp.getLayers();
            int size = list.size();
            for (int j = 0; j < size; j++) {
                AEFragmentInfo.LayerInfo layerInfo = list.get(j);
                if (layerInfo.getLayerType() == AEFragmentInfo.LayerType.EDIT) {
                    AEMediaInfo aeMediaInfo = mList.get(index);
                    layerInfo.setMediaObject(aeMediaInfo.getMediaObject());
                    index++;
                }
            }
        }
    }


    private void start() {
        player.start();
        mIvVideoPlayState.setImageResource(R.drawable.btn_pause);
        ViewUtils.fadeOut(this, mIvVideoPlayState);
    }

    /**
     * 暂停播放
     */
    private void pause() {
        player.pause();
        mIvVideoPlayState.clearAnimation();
        mIvVideoPlayState.setImageResource(R.drawable.btn_play);
        mIvVideoPlayState.setVisibility(View.VISIBLE);
    }


    private void seekTo(int msec) {
        player.seekTo(Utils.ms2s(msec), VirtualVideoView.SEEK_OPTION_CLOSEST);
        onSeekTo(msec);
    }

    public void stop() {
        player.stop();
        onSeekTo(0);
        mIvVideoPlayState.clearAnimation();
        mIvVideoPlayState.setImageResource(R.drawable.btn_play);
        mIvVideoPlayState.setVisibility(View.VISIBLE);
    }


    private void onSeekTo(int progress) {
        mCurrentTv.setText(getFormatTime(progress));
        mRdSeekBar.setProgress(progress);
    }

    private boolean isPlaying() {
        return player.isPlaying();
    }


    /***
     * 初始化播放器媒体资源
     */
    private void initPlayerData() {
        mVirtualVideo.reset();
        player.reset();
        player.setAspectRatioFitMode(AspectRatioFitMode.IGNORE_ASPECTRATIO);
        player.setPreviewAspectRatio(0);
        SysAlertDialog.showLoadingDialog(this, R.string.isloading);
        //加载全部视频
        reload(mVirtualVideo);
        try {
            mVirtualVideo.build(player);
            start();
        } catch (InvalidStateException e) {
            e.printStackTrace();
        }

    }

    /**
     * 获取模板时长
     */
    private float getTemplateDuration() {
        return mAETemplateInfo.getAEFragmentInfo().getDuration();
    }

    /**
     * 加载数据到虚拟视频
     */
    private void reload(VirtualVideo virtualVideo) {
        if (mAETemplateInfo != null && mAETemplateInfo.getAEFragmentInfo() != null) {
            float templateDuration = getTemplateDuration();
            if (null == mMusic) {
                mBuildMusic = mAETemplateInfo.getMusic();
                if (null != mBuildMusic) {
                    mBuildMusic.setTimeRange(mBuildMusic.getTrimStart(),
                            Math.min(templateDuration, mBuildMusic.getDuration()));
                    mBuildMusic.setMixFactor(mMusicFactor);
                }
            } else {
                //自定义配乐
                mMusic.setMixFactor(mMusicFactor);
                mBuildMusic = mMusic;
            }


            //主AE模板
            virtualVideo.addAEFragment(mAETemplateInfo.getAEFragmentInfo());

            int len = mAEList.size();
            //记录总的时长
            float vduration = templateDuration;
            for (int i = 0; i < len; i++) {//循环的AE模板
                AEFragmentInfo tmp = mAEList.get(i);
                virtualVideo.addAEFragment(tmp, templateDuration * (i + 1));
                vduration += templateDuration;
            }

            //配乐
            addMusic(virtualVideo, templateDuration);
            //背景和blend
            addBlend(virtualVideo, vduration);

            if (virtualVideo == AEPreviewActivity.this.mVirtualVideo) {
                float aspectRatio = (float) mAETemplateInfo.getAEFragmentInfo().getWidth() /
                        mAETemplateInfo.getAEFragmentInfo().getHeight();
                mPreviewFrame.setAspectRatio(aspectRatio);
                player.setPreviewAspectRatio(aspectRatio);
            }
        }

    }

    /**
     * blend视频
     */
    private void addBlend(VirtualVideo virtualVideo, float duration) {
        for (BackgroundMedia backgroundMedia : mAETemplateInfo.getBackground()) {
            MediaObject mediaObject = backgroundMedia.toMediaObject();
            if (mediaObject != null) {
                if (mAEList.size() > 0) {
                    mediaObject.setTimelineRange(0, duration);//指定时间线，根据时间线来判断是否需要循环
                }
                virtualVideo.addBackgroundMedia(mediaObject);
            }
        }

        for (BlendEffectObject effectObject : mAETemplateInfo.getBlendEffectObject()) {
            virtualVideo.addMVEffect(effectObject); //blend 自动循环
            if ((!effectObject.isSameMediaPath() && virtualVideo == mVirtualVideo)
                    || mAETemplateInfo.isSwDecode()) {
                //如果叠加的mask效果对象包含两个不同文件时，为了预览同步，需要采用软解
                //强制软解时
                effectObject.setForceSWDecoder(true);
            }
        }
        //模板的帧率
        player.setPreviewFrameRate(mAETemplateInfo.getFrame());
    }

    /**
     * 添加配乐
     *
     * @param templateDuration 模板时长
     */
    private void addMusic(VirtualVideo virtualVideo, float templateDuration) {
        try {
            if (null != mBuildMusic) {
                //启用重复播放
                mBuildMusic.setEnableRepeat(true);
                virtualVideo.addMusic(mBuildMusic);
            }
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        }
    }

    private List<AEMediaInfo> mList;
    private RdSeekBar mRdSeekBar;

    private String getFormatTime(int msec) {
        return DateTimeUtils.stringForMillisecondTime(msec, true, true);
    }

    private TextView tvMusicFactor;
    private ExportConfiguration mExportConfig;

    private void initView() {
        mList = new ArrayList<>();
        mCurrentTv = $(R.id.tvCurTime);
        mTotalTv = $(R.id.tvTotalTime);
        mContentFrame = $(R.id.contentFrame);
        mContentFrame.setAspectRatio(1f);
        mPreviewFrame = $(R.id.previewFrame);
        mPreviewFrame.setAspectRatio(1f);
        mBtnNext = $(R.id.btnRight);
        mBtnLeft = $(R.id.btnLeft);
        mBtnLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        mTvTitle = $(R.id.tvTitle);
        mTvTitle.setText(mAETemplateInfo.getName());
        player = $(R.id.player);
        mIvVideoPlayState = $(R.id.ivPlayerState);
        mBtnNext.setText(R.string.export);
        mBtnNext.setTextColor(getResources().getColor(R.color.one_key_make_solid));
        mBtnNext.setVisibility(View.VISIBLE);
        mExportConfig = SdkEntry.getSdkService().getExportConfig();
        mBtnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mExportConfig.useCustomExportGuide) {
                    SdkEntryHandler.getInstance().onExportClick(AEPreviewActivity.this);
                } else {
                    onExport();
                }
            }
        });
        tvMusicFactor = $(R.id.tvMusicFactor);
        mRdSeekBar = $(R.id.sbEditor);
        mRdSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    seekTo(progress);
                }
            }

            private boolean isPlaying = false;

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

                if ((isPlaying = player.isPlaying())) {
                    isPlaying = true;
                    player.pause();
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (isPlaying) {
                    player.start();
                }
            }
        });
        RdSeekBar mMusicFactorBar = $(R.id.sbMusicFactor);
        mMusicFactor = 50;
        mMusicFactorBar.setProgress(mMusicFactor);
        mMusicFactorBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mMusicFactor = progress;
                    tvMusicFactor.setText(Integer.toString(progress));
                    if (null != mBuildMusic) {
                        mBuildMusic.setMixFactor(progress);
                    }
                }
            }


            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        mTvMusic = $(R.id.changeMusic);
        mTvMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onMusicLocal();
            }
        });

        mRecyclerView = $(R.id.recyclerView);


        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        //设置添加或删除item时的动画，这里使用默认动画
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mAdapter = new AEMediaAdapter(this);
        mAdapter.setOnItemClickListener(new AEMediaAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                onSelectedImp(position, mAdapter.getItem(position));
            }
        });
        //设置适配器
        mRecyclerView.setAdapter(mAdapter);
    }

    private boolean withWatermark = true;

    /**
     * 导出
     */
    private void onExport() {
        stop();
        ExportHandler exportHandler = new ExportHandler(this, new ExportHandler.ExportCallBack() {
            @Override
            public void onCancel() {
                initPlayerData();
            }

            @Override
            public void addData(VirtualVideo virtualVideo) {
                reload(virtualVideo);
            }
        });
        float aspectRatio = (float) mAETemplateInfo.getAEFragmentInfo().getWidth() /
                mAETemplateInfo.getAEFragmentInfo().getHeight();

        VideoConfig videoConfig = exportHandler.getExportConfig(aspectRatio);
        //帧率
        videoConfig.setVideoFrameRate(mAETemplateInfo.getFrame());
        exportHandler.onExport(withWatermark, videoConfig);
    }


    private TextView mTvMusic;


    /**
     * 选择进入本地音乐界面
     */
    private void onMusicLocal() {
        HistoryMusicCloud.getInstance().initilize(this);
        MoreMusicActivity.onLocalMusic(this, REQUESTCODE_FOR_LOCALMUSIC);
    }


    /**
     * 注册播放器回调
     *
     * @param player
     */
    private void initPlayerListener(final VirtualVideoView player) {

        player.setOnPlaybackListener(new VirtualVideoView.VideoViewListener() {
            @Override
            public void onPlayerPrepared() {
                int ms = Utils.s2ms(player.getDuration());
                TempVideoParams.getInstance().setEditingVideoDuration(ms);
                mRdSeekBar.setMax(ms);
                mTotalTv.setText(getFormatTime(ms));
                onSeekTo(0);
                SysAlertDialog.cancelLoadingDialog();
            }

            @Override
            public boolean onPlayerError(int what, int extra) {
                LogUtil.e(TAG, "mute-onPlayerError: " + what + "..." + extra);
                onSeekTo(0);
                return false;
            }

            @Override
            public void onPlayerCompletion() {
                LogUtil.i(TAG, "onPlayerCompletion:  播放完毕-->" + player.getDuration());
                player.seekTo(0);
                onSeekTo(0);
            }

            @Override
            public void onGetCurrentPosition(float position) {
                onSeekTo(Utils.s2ms(position));
            }
        });


        player.setOnInfoListener(new VirtualVideo.OnInfoListener() {
            @Override
            public boolean onInfo(int what, int extra, Object obj) {
//                com.rd.vecore.utils.Log.i(TAG, "onInfo: " + what + "..." + extra + "..." + obj);
                return true;
            }
        });

        player.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (!mInterceptRepeatClick) {
                    //防止重复点击
                    mInterceptRepeatClick = true;
                    player.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mInterceptRepeatClick = false;
                        }
                    }, 500);
                    if (player.isPlaying()) {
                        pause();
                    } else {
                        start();
                    }
                }
            }
        });
    }


    private final int REQUESTCODE_FOR_APPEND = 10;
    private final int REQUESTCODE_FOR_TRIM = 14;
    private final int REQUESTCODE_FOR_EDIT = 16;
    private final int REQUESTCODE_FOR_LOCALMUSIC = 20;
    private final int REQUESTCODE_FOR_AETEXT = 21;


    private void onSelectedImp(int position, AEMediaInfo item) {
        lastMediaIndex = position;
        if (item.getType() == AEMediaInfo.MediaType.VIDEO) {
            MediaObject mediaObject = item.getMediaObject();
            if (null != mediaObject) {
                if (mediaObject.getMediaType() == MediaType.MEDIA_VIDEO_TYPE) {
                    //编辑单个视频
                    showVideoMenu(item);
                } else {
                    //编辑单个图片 (crop)
                    showImageMenu(item);
                }
            } else {
                SelectMediaActivity.appendMedia(this, true, UIConfiguration.ALBUM_SUPPORT_DEFAULT,
                        1, REQUESTCODE_FOR_APPEND);

            }
        } else if (item.getType() == AEMediaInfo.MediaType.IMAGE) {
            MediaObject mediaObject = item.getMediaObject();
            if (null != mediaObject) {
                showImageMenu(item);
            } else {
                SelectMediaActivity.appendMedia(this, true, UIConfiguration.ALBUM_SUPPORT_IMAGE_ONLY,
                        1, REQUESTCODE_FOR_APPEND);
            }
        } else {
            AEMediaInfo aeMediaInfo = mAdapter.getItem(lastMediaIndex);
            String ttfPath = aeMediaInfo.getTtf();
            if (TextUtils.isEmpty(ttfPath)) {
                ttfPath = aeMediaInfo.getAETextLayerInfo().getTtfPath();
            }
            AETextActivity.onAEText(this, aeMediaInfo.getAETextLayerInfo(), aeMediaInfo.getText(), aeMediaInfo.getTtfIndex(), ttfPath, REQUESTCODE_FOR_AETEXT);
        }

    }

    /**
     * 图片替换和编辑操作
     */
    private void showImageMenu(final AEMediaInfo item) {
        String[] menu = getResources().getStringArray(R.array.ae_image_menu);
        SysAlertDialog.showListviewAlertMenu(this, null, menu,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (which == 0) {
                            MediaObject mediaObject = item.getMediaObject();
                            Scene scene = VirtualVideo.createScene();
                            scene.addMedia(mediaObject);
                            //编辑单个图片 (crop)
                            CropRotateMirrorActivity.onAECropRotateMirror(AEPreviewActivity.this, scene, item.getAsp(), true, REQUESTCODE_FOR_EDIT);
                        } else if (which == 1) {
                            //替换
                            if (item.getType() == AEMediaInfo.MediaType.VIDEO) {
                                SelectMediaActivity.appendMedia(AEPreviewActivity.this, true, UIConfiguration.ALBUM_SUPPORT_DEFAULT,
                                        1, REQUESTCODE_FOR_APPEND);
                            } else {
                                SelectMediaActivity.appendMedia(AEPreviewActivity.this, true, UIConfiguration.ALBUM_SUPPORT_IMAGE_ONLY,
                                        1, REQUESTCODE_FOR_APPEND);
                            }
                        }
                    }
                });
    }

    /**
     * 视频替换和编辑
     */
    private void showVideoMenu(final AEMediaInfo item) {
        String[] menu = getResources().getStringArray(R.array.ae_image_menu);
        SysAlertDialog.showListviewAlertMenu(this, null, menu,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (which == 0) {
                            MediaObject mediaObject = item.getMediaObject();
                            Scene scene = VirtualVideo.createScene();
                            scene.addMedia(mediaObject);
                            //编辑单个视频
                            TrimMediaActivity.onAETrim(AEPreviewActivity.this, scene, true, item.getDuration(), item.getAsp(), REQUESTCODE_FOR_EDIT);
                        } else if (which == 1) {
                            //替换
                            SelectMediaActivity.appendMedia(AEPreviewActivity.this, true, UIConfiguration.ALBUM_SUPPORT_DEFAULT,
                                    1, REQUESTCODE_FOR_APPEND);
                        }
                    }
                });
    }

    private AEMediaAdapter mAdapter;
    private int lastMediaIndex = 0;
    private Music mMusic;
    private int mMusicFactor = 50;
    private List<MediaObject> mMediaObjects = new ArrayList<>();

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUESTCODE_FOR_AETEXT) {
                if (null != data) {
                    AETextActivity.AEText aeText = AETextActivity.getAEText(data);
                    AEMediaInfo aeMediaInfo = mAdapter.getItem(lastMediaIndex);
                    AETextLayerInfo aeTextLayerInfo = aeMediaInfo.getAETextLayerInfo();
                    if (null != aeTextLayerInfo) {
                        aeMediaInfo.setText(aeText.getText());
                        aeMediaInfo.setTtf(aeText.getTtf(), aeText.getTtfIndex());
                        String file = AETextActivity.fixAEText(aeTextLayerInfo, aeText.getText(), aeMediaInfo.getTtf());
                        try {
                            MediaObject mediaObject = new MediaObject(file);
                            aeMediaInfo.setMediaObject(mediaObject);
                            mAdapter.update(lastMediaIndex, aeMediaInfo);
                        } catch (InvalidArgumentException e) {
                            e.printStackTrace();
                        }
                        aeReload(true);
                    }
                }
            } else if (requestCode == REQUESTCODE_FOR_LOCALMUSIC) {
                if (null != data) {     //配乐
                    AudioMusicInfo audioMusic = data.getParcelableExtra(MoreMusicActivity.MUSIC_INFO);
                    if (null != audioMusic) {
                        mMusic = VirtualVideo.createMusic(audioMusic.getPath());
                        float duration = Utils.ms2s(audioMusic.getEnd() - audioMusic.getStart());
                        mMusic.setTimeRange(Utils.ms2s(audioMusic.getStart()),
                                Math.min(getTemplateDuration(), duration));
                        mMusic.setMixFactor(mMusicFactor);
                        mTvMusic.setText(audioMusic.getName());
                    } else {
                        mTvMusic.setText(R.string.ae_preview_change_music);
                        mMusic = null;
                    }
                }
                initPlayerData();
            } else if (requestCode == REQUESTCODE_FOR_EDIT || requestCode == REQUESTCODE_FOR_TRIM) {
                if (null != data) {
                    Scene scene = data.getParcelableExtra(IntentConstants.INTENT_EXTRA_SCENE);
                    if (null != scene) {
                        AEMediaInfo info = mAdapter.getItem(lastMediaIndex);
                        if (null != info) {
                            MediaObject mediaObject = scene.getAllMedia().get(0);
                            if (null != mediaObject && initThumb(mediaObject, info)) { //没有滤镜
                                info.setMediaObject(mediaObject);
                                mAdapter.update(lastMediaIndex, info);
                                aeReload(true);
                            }
                        } else {
                            aeReload(true);
                        }
                    } else {
                        aeReload(true);
                    }
                }
            } else if (requestCode == REQUESTCODE_FOR_APPEND) {
                if (null != data) {
                    ArrayList<String> medias = data.getStringArrayListExtra(SdkEntry.ALBUM_RESULT);
                    if (medias.size() > 0) {
                        Scene scene = VirtualVideo.createScene();
                        try {
                            MediaObject mediaObject = scene.addMedia(medias.get(0));
                            AEMediaInfo info = mAdapter.getItem(lastMediaIndex);
                            if (mediaObject.getMediaType() == MediaType.MEDIA_IMAGE_TYPE) {
                                //编辑单个图片 (crop)
                                CropRotateMirrorActivity.onAECropRotateMirror(this, scene, info.getAsp(), true, REQUESTCODE_FOR_EDIT);
                            } else {
                                // 视频 定长截取 、裁剪
                                TrimMediaActivity.onAETrim(this, scene, true, info.getDuration(), info.getAsp(), REQUESTCODE_FOR_TRIM);
                            }
                        } catch (InvalidArgumentException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } else if (resultCode == TrimMediaActivity.RESULT_AE_REPLACE) {
            //替换AE视频
            SelectMediaActivity.appendMedia(this, true, UIConfiguration.ALBUM_SUPPORT_DEFAULT, 1, REQUESTCODE_FOR_APPEND);
        } else if (resultCode == CropRotateMirrorActivity.RESULT_AE_REPLACE) {
            //替换AE图片
            SelectMediaActivity.appendMedia(this, true, UIConfiguration.ALBUM_SUPPORT_IMAGE_ONLY, 1, REQUESTCODE_FOR_APPEND);
        } else {
        }
    }


    /**
     * 创建缩略图
     */
    private boolean initThumb(final MediaObject mediaObject, final AEMediaInfo aeMediaInfo) {
        if (mediaObject.getFilterList() == null || mediaObject.getFilterList().size() == 0) {
            aeMediaInfo.setThumbPath(null);
            return true;
        }
        ThreadPoolUtils.executeEx(new ThreadPoolUtils.ThreadPoolRunnable() {
            @Override
            public void onBackground() {
                Bitmap bmp = Bitmap.createBitmap(
                        ThumbNailUtils.THUMB_HEIGHT,
                        ThumbNailUtils.THUMB_HEIGHT, Bitmap.Config.ARGB_8888);
                VirtualVideo virtualVideo = new VirtualVideo();
                Scene scene = VirtualVideo.createScene();
                scene.addMedia(mediaObject);
                virtualVideo.addScene(scene);
                aeMediaInfo.setMediaObject(mediaObject);
                if (virtualVideo.getSnapshot(AEPreviewActivity.this, 1f, bmp, true)) {
                    String thumb = PathUtils.getTempFileNameForSdcard("Temp", "png");
                    BitmapUtils.saveBitmapToFile(bmp, thumb, true);
                    aeMediaInfo.setThumbPath(thumb);
                }
                virtualVideo.release();
                bmp.recycle();
            }

            @Override
            @MainThread
            public void onEnd() {
                mAdapter.update(lastMediaIndex, aeMediaInfo);
                aeReload(true);
            }
        });
        return false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isPlaying()) {
            pause();
        }
    }


    @Override
    public void onBackPressed() {
        String strMessage = getString(R.string.quit_edit);
        SysAlertDialog.showAlertDialog(this, "", strMessage,
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
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
        super.onDestroy();
        // 删除自定义水印临时文件
        if (null != player) {
            player.stop();
            player.cleanUp();
            player = null;
        }
        if (null != mVirtualVideo) {
            mVirtualVideo.release();
            mVirtualVideo = null;
        }
    }

}
