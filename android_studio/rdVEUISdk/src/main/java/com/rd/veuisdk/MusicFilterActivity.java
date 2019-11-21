package com.rd.veuisdk;

import android.content.DialogInterface;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.rd.lib.ui.ExtButton;
import com.rd.lib.ui.PreviewFrameLayout;
import com.rd.lib.ui.RotateRelativeLayout;
import com.rd.vecore.VirtualVideo;
import com.rd.vecore.VirtualVideoView;
import com.rd.vecore.exception.InvalidStateException;
import com.rd.vecore.models.AspectRatioFitMode;
import com.rd.vecore.models.MediaObject;
import com.rd.vecore.models.MusicFilterType;
import com.rd.vecore.models.PermutationMode;
import com.rd.vecore.models.Scene;
import com.rd.vecore.models.VideoConfig;
import com.rd.vecore.utils.Log;
import com.rd.veuisdk.ui.RdSeekBar;
import com.rd.veuisdk.utils.DateTimeUtils;
import com.rd.veuisdk.utils.IntentConstants;
import com.rd.veuisdk.utils.SysAlertDialog;
import com.rd.veuisdk.utils.Utils;
import com.rd.veuisdk.utils.ViewUtils;

import java.util.List;


/**
 * 设置音效界面
 */
public class MusicFilterActivity extends BaseActivity {
    private PreviewFrameLayout mPreviewFrame;
    private ExtButton mBtnNext;
    private TextView mTvTitle;
    private VirtualVideoView player;
    private TextView mMusicFilter1;
    private TextView mMusicFilter2;
    private TextView mMusicFilterAll;
    private ImageView mIvVideoPlayState;
    private RdSeekBar mRdSeekBar;

    private SeekBar mSbVoice1;
    private SeekBar mSbVoice2;
    private TextView currentTv, totalTv;
    private RotateRelativeLayout mProgressLayout;

    private int mFilterInfoIndex1 = 0, mFilterInfoIndex2 = 0, mFilterInfoIndexAll = 0;

    private List<MediaObject> mMediaList;

    static class FilterInfo {
        /**
         * 音效滤镜
         */
        public final MusicFilterType filterType;
        /**
         * 提示字串
         */
        public final String title;

        public FilterInfo(String title, MusicFilterType filterType) {
            this.filterType = filterType;
            this.title = title;
        }
    }

    private FilterInfo[] mFilterInfos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TAG = "MusicFilterActivity";
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_music_filter_layout);
        mFilterInfos = buildFilterInfo(getResources().getStringArray(R.array.music_filter_titles));
        mMediaList = getIntent().getParcelableArrayListExtra(IntentConstants.EXTRA_MEDIA_LIST);
        initView();
        mTvTitle.setText(R.string.priview_title);
        mPreviewFrame.setAspectRatio(1);
        player.setPreviewAspectRatio(1);
        player.setAutoRepeat(true);
        mVirtualVideo = new VirtualVideo();

        player.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (!bInterceptRepeat) {
                    //防止重复点击
                    bInterceptRepeat = true;
                    player.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            bInterceptRepeat = false;
                        }
                    }, 500);

                    if (isPlayingORecording) {
                        //暂停播放
                        player.pause();
                        mIvVideoPlayState.clearAnimation();
                        mIvVideoPlayState.setImageResource(R.drawable.btn_play);
                        mIvVideoPlayState.setVisibility(View.VISIBLE);
                        isPlayingORecording = false;
                    } else {
                        isPlayingORecording = true;
                        player.start();
                        mIvVideoPlayState.setImageResource(R.drawable.btn_pause);
                        ViewUtils.fadeOut(MusicFilterActivity.this, mIvVideoPlayState);
                    }
                }
            }
        });
        initPlayerListener(player);
        initPlayerData();
    }


    private void initView() {
        mProgressLayout = $(R.id.rlPlayerBottomMenu);
        mRdSeekBar = $(R.id.sbEditor);
        currentTv = $(R.id.tvCurTime);
        totalTv = $(R.id.tvTotalTime);
        mPreviewFrame = $(R.id.previewFrame);
        mBtnNext = $(R.id.btnRight);
        mTvTitle = $(R.id.tvTitle);
        player = $(R.id.palyer);
        mSbVoice1 = $(R.id.sb_volume1);
        mSbVoice2 = $(R.id.sb_volume2);
        mMusicFilter1 = $(R.id.tvMusicFilter1);
        mMusicFilter2 = $(R.id.tvMusicFilter2);
        mMusicFilterAll = $(R.id.tvMusicFilterAll);
        mIvVideoPlayState = $(R.id.ivPlayerState);
        if (mMediaList.size() == 1) {
            $(R.id.llSingleSoundEffect).setVisibility(View.GONE);
            $(R.id.llVoice2).setVisibility(View.GONE);
        }
        mBtnNext.setText(R.string.export);
        mBtnNext.setVisibility(View.VISIBLE);
        $(R.id.llMusicFilter1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final FilterInfo filterInfo = getFilterInfo(++mFilterInfoIndex1 % mFilterInfos.length);
                //方式1：
                mMediaList.get(0).setMusicFilterType(filterInfo.filterType);
                //方式2：
//                mVirtualVideo.setMusicFilter(mMediaList.get(0).getMediaPath(), mAudioType1);
                mMusicFilter1.setText(filterInfo.title);
                mFilterInfoIndexAll = 0;
                mMusicFilterAll.setText(getFilterInfo(mFilterInfoIndexAll).title);
            }
        });

        $(R.id.llMusicFilter2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final FilterInfo filterInfo = getFilterInfo(++mFilterInfoIndex2 % mFilterInfos.length);

                mVirtualVideo.setMusicFilter(mMediaList.get(1).getMediaPath(), filterInfo.filterType);
                mMusicFilter2.setText(filterInfo.title);

                mFilterInfoIndexAll = 0;
                mMusicFilterAll.setText(getFilterInfo(0).title);
            }
        });
        $(R.id.llMusicFilterAll).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFilterInfoIndexAll = ++mFilterInfoIndexAll % mFilterInfos.length;

                onSetMusicFilterAll();

                //方式1：
                {
                    int len = mMediaList.size();
                    for (int i = 0; i < len; i++) {
                        mMediaList.get(i).setMusicFilterType(getFilterInfo(mFilterInfoIndexAll).filterType);
                    }
                }
                //方式2：
                {
//                    if (null != mVirtualVideo) {
//                        mVirtualVideo.setMusicFilter(mAudioTypeAll);
//                    }
                }
            }
        });

        mSbVoice1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mVirtualVideo.setOriginalMixFactor(mMediaList.get(0).getMediaPath(), progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        mSbVoice2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mVirtualVideo.setOriginalMixFactor(mMediaList.get(1).getMediaPath(), progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        $(R.id.btnLeft).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onMBtnBackClicked();
            }
        });
        mBtnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onMBtnNextClicked();
            }
        });


        mRdSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                if (fromUser) {
                    float p = Utils.ms2s(progress);
                    player.seekTo(p);
                    currentTv.setText(getFormatTime(progress));
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
        onSetMusicFilterAll();
    }

    private void onSetMusicFilterAll() {
        final FilterInfo filterInfo = getFilterInfo(mFilterInfoIndexAll);
        mMusicFilterAll.setText(filterInfo.title);
        mFilterInfoIndex2 = mFilterInfoIndex1 = mFilterInfoIndexAll;
        mMusicFilter1.setText(getFilterInfo(mFilterInfoIndex1).title);
        mMusicFilter2.setText(getFilterInfo(mFilterInfoIndex2).title);
    }

    private boolean isPlayingORecording = false;
    private boolean bInterceptRepeat = false;


    private String getFormatTime(int msec) {
        return DateTimeUtils.stringForMillisecondTime(msec, true, true);
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

                float dura = player.getDuration();
                SysAlertDialog.cancelLoadingDialog();
                int ms = Utils.s2ms(dura);
                mRdSeekBar.setMax(ms);
                totalTv.setText(getFormatTime(ms));
                onSeekTo(0);
                mProgressLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public boolean onPlayerError(int what, int extra) {
                Log.e(TAG, "mute-onPlayerError: " + what + "..." + extra);
                onSeekTo(0);
                return false;
            }

            @Override
            public void onPlayerCompletion() {
                Log.i(TAG, "onPlayerCompletion:  ->" + player.getDuration());
            }

            @Override
            public void onGetCurrentPosition(float position) {
                onSeekTo(position);
            }
        });


        player.setOnInfoListener(new VirtualVideo.OnInfoListener() {
            @Override
            public boolean onInfo(int what, int extra, Object obj) {
                Log.i(TAG, "onInfo: " + what + "..." + extra + "..." + obj);
                return true;
            }
        });


    }

    private VirtualVideo mVirtualVideo;

    /***
     * 初始化播放器媒体资源
     */
    private void initPlayerData() {
        mVirtualVideo.reset();
        player.reset();
        try {
            player.setAspectRatioFitMode(AspectRatioFitMode.IGNORE_ASPECTRATIO);
            //加载全部视频
            boolean hasVideo = reload(mVirtualVideo);
            if (hasVideo) {
                mVirtualVideo.build(player);
            } else {
                Log.e(TAG, "initPlayerData: no video!");
            }

        } catch (InvalidStateException e) {
            e.printStackTrace();
        }
        onSeekTo(0);
    }


    private void onSeekTo(float progress) {
        int tp = Utils.s2ms(progress);
        currentTv.setText(getFormatTime(tp));
        mRdSeekBar.setProgress(tp);
    }

    @Override
    protected void onDestroy() {
        if (null != player) {
            player.setOnPlaybackListener(null);
            player.stop();
            player.cleanUp();
        }
        mVirtualVideo.release();
        super.onDestroy();
    }


    private float lastProgress = -1f;

    @Override
    protected void onStart() {
        super.onStart();
        isPlayingORecording = false;
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (lastProgress != -1f) {
            //*****如果播放器在切到后台时，已经stop(),需要重新build(),再预览
            //还原播放器位置，恢复缩略图
            player.seekTo(lastProgress);
            onSeekTo(lastProgress);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        lastProgress = -1f;

        if (null != player) {
            if (player.isPlaying()) {
                //暂停
                player.pause();
            }
            //记录播放器位置
            lastProgress = player.getCurrentPosition();
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
                        dialog.dismiss();
                    }
                }, getString(R.string.sure),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (null != player) {
                            player.stop();
                        }
                        finish();
                    }
                });


    }


    private void onMBtnBackClicked() {
        onBackPressed();
    }

    private void onMBtnNextClicked() {
        onExport();
    }

    /**
     * 加载视频资源
     *
     * @param virtualVideo
     */
    private boolean reload(VirtualVideo virtualVideo) {
        Scene scene = VirtualVideo.createScene();
        boolean canBuild = false;

        int len = mMediaList.size();
        for (int i = 0; i < len; i++) {
            canBuild = true;
            MediaObject media = mMediaList.get(i);
            if (len == 1) {
                media.setAspectRatioFitMode(AspectRatioFitMode.KEEP_ASPECTRATIO);
            } else {
                if (i == 0) {
                    media.setShowRectF(new RectF(0, 0, 0.5f, 0.5f));
                } else {
                    media.setShowRectF(new RectF(0.5f, 0.5f, 1, 1));
                }
                media.setAspectRatioFitMode(AspectRatioFitMode.KEEP_ASPECTRATIO_EXPANDING);
            }
            if (i == 0) {
                media.setMixFactor(mSbVoice1.getProgress());
                media.setMusicFilterType(getFilterInfo(mFilterInfoIndex1).filterType);
            } else {
                media.setMixFactor(mSbVoice2.getProgress());
                media.setMusicFilterType(getFilterInfo(mFilterInfoIndex2).filterType);
            }
            scene.addMedia(media);
        }
        if (canBuild) {
            scene.setPermutationMode(PermutationMode.COMBINATION_MODE);
            virtualVideo.addScene(scene);
        }
        return canBuild;
    }


    /**
     * 点击下一步->导出
     */
    private void onExport() {
        onPause();
        ExportHandler exportHandler = new ExportHandler(this, new ExportHandler.IExport() {
            @Override
            public void addData(VirtualVideo virtualVideo) {
                reload(virtualVideo);
            }
        });
        VideoConfig videoConfig = exportHandler.getExportConfig(1);
        exportHandler.onExport(true, videoConfig);
    }

    private FilterInfo getFilterInfo(int index) {
        if (index >= 0 && index < mFilterInfos.length) {
            return mFilterInfos[index];
        } else {
            return mFilterInfos[0];
        }
    }

    /**
     * 构造音效及提示字串列表
     */
    static FilterInfo[] buildFilterInfo(String[] stringArray) {
        /**
         * <item>自定义</item>
         * <item>正常</item>
         <item>男声</item>
         <item>女声</item>
         <item>怪兽</item>
         <item>卡通</item>
         <item>混响</item>
         <item>回声</item>
         <item>房间</item>
         <item>舞台</item>
         <item>KTV</item>
         <item>厂房</item>
         <item>竞技场</item>
         <item>电音</item>
         */
        return new FilterInfo[]
                {
                        new FilterInfo(stringArray[0], MusicFilterType.MUSIC_FILTER_CUSTOM),
                        new FilterInfo(stringArray[1], MusicFilterType.MUSIC_FILTER_NORMAL),
                        new FilterInfo(stringArray[2], MusicFilterType.MUSIC_FILTER_BOY),
                        new FilterInfo(stringArray[3], MusicFilterType.MUSIC_FILTER_GIRL),
                        new FilterInfo(stringArray[4], MusicFilterType.MUSIC_FILTER_MONSTER),
                        new FilterInfo(stringArray[5], MusicFilterType.MUSIC_FILTER_CARTOON),
                        new FilterInfo(stringArray[6], MusicFilterType.MUSIC_FILTER_REVERB),
                        new FilterInfo(stringArray[7], MusicFilterType.MUSIC_FILTER_ECHO),
                        new FilterInfo(stringArray[8], MusicFilterType.MUSIC_FILTER_ROOM),
                        new FilterInfo(stringArray[9], MusicFilterType.MUSIC_FILTER_DANCE),
                        new FilterInfo(stringArray[10], MusicFilterType.MUSIC_FILTER_KTV),
                        new FilterInfo(stringArray[11], MusicFilterType.MUSIC_FILTER_FACTORY),
                        new FilterInfo(stringArray[12], MusicFilterType.MUSIC_FILTER_ARENA),
                        new FilterInfo(stringArray[13], MusicFilterType.MUSIC_FILTER_ELECTRI),
                };
    }
}
