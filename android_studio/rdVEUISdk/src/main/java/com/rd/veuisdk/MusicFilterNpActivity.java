package com.rd.veuisdk;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.rd.lib.ui.ExtButton;
import com.rd.lib.ui.PreviewFrameLayout;
import com.rd.lib.ui.RotateRelativeLayout;
import com.rd.lib.utils.FileUtils;
import com.rd.vecore.Music;
import com.rd.vecore.PlayerControl;
import com.rd.vecore.VirtualAudio;
import com.rd.vecore.VirtualVideo;
import com.rd.vecore.VirtualVideoView;
import com.rd.vecore.exception.InvalidArgumentException;
import com.rd.vecore.listener.ExportListener;
import com.rd.vecore.models.AudioConfig;
import com.rd.vecore.models.MediaObject;
import com.rd.vecore.models.MusicFilterType;
import com.rd.vecore.models.VideoConfig;
import com.rd.vecore.utils.Log;
import com.rd.veuisdk.ui.HorizontalProgressDialog;
import com.rd.veuisdk.ui.RdSeekBar;
import com.rd.veuisdk.utils.DateTimeUtils;
import com.rd.veuisdk.utils.IntentConstants;
import com.rd.veuisdk.utils.PathUtils;
import com.rd.veuisdk.utils.SysAlertDialog;
import com.rd.veuisdk.utils.Utils;
import com.rd.veuisdk.utils.ViewUtils;

import java.util.ArrayList;
import java.util.List;

import static com.rd.vecore.VirtualVideo.getMediaInfo;


/**
 * 设置音效界面
 */
public class MusicFilterNpActivity extends BaseActivity {
    private final int CANCEL_EXPORT = 1 << 7;//取消导出

    private PreviewFrameLayout mPreviewFrame;
    private ExtButton mBtnNext;
    private TextView mTvTitle;
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

    private List<Music> mMusicList;
    private MusicFilterActivity.FilterInfo[] mFilterInfos;
    private VirtualAudio mVirtualAudio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TAG = "MusicFilterNpActivity";
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_music_filter_np_layout);
        mFilterInfos = MusicFilterActivity.buildFilterInfo(getResources().getStringArray(R.array.music_filter_titles));
        List<MediaObject> mediaObjects = getIntent().getParcelableArrayListExtra(IntentConstants.EXTRA_MEDIA_LIST);
        mMusicList = new ArrayList<>();
        for (MediaObject mediaObject : mediaObjects) {
            mMusicList.add(VirtualVideo.createMusic(mediaObject.getMediaPath()));
        }
        initView();
        mTvTitle.setText(R.string.priview_title);
        mPreviewFrame.setAspectRatio(1);
        mVirtualAudio = new VirtualAudio(this);
        mVirtualAudio.setAutoRepeat(true);

        mPreviewFrame.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (!bInterceptRepeat) {
                    //防止重复点击
                    bInterceptRepeat = true;
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            bInterceptRepeat = false;
                        }
                    }, 500);

                    if (isPlayingORecording) {
                        //暂停播放
                        mVirtualAudio.pause();
                        mIvVideoPlayState.clearAnimation();
                        mIvVideoPlayState.setImageResource(R.drawable.btn_play);
                        mIvVideoPlayState.setVisibility(View.VISIBLE);
                        isPlayingORecording = false;
                    } else {
                        isPlayingORecording = true;
                        mVirtualAudio.start();
                        mIvVideoPlayState.setImageResource(R.drawable.btn_pause);
                        ViewUtils.fadeOut(MusicFilterNpActivity.this, mIvVideoPlayState);
                    }
                }
            }
        });
        initPlayerListener(mVirtualAudio);
        initPlayerData();
    }


    private void initView() {
        mProgressLayout = (RotateRelativeLayout) findViewById(R.id.rlPlayerBottomMenu);
        mRdSeekBar = (RdSeekBar) findViewById(R.id.sbEditor);
        currentTv = (TextView) findViewById(R.id.tvCurTime);
        totalTv = (TextView) findViewById(R.id.tvTotalTime);
        mPreviewFrame = (PreviewFrameLayout) findViewById(R.id.previewFrame);
        mBtnNext = (ExtButton) findViewById(R.id.btnRight);
        mTvTitle = (TextView) findViewById(R.id.tvTitle);
        mSbVoice1 = (SeekBar) findViewById(R.id.sb_volume1);
        mSbVoice2 = (SeekBar) findViewById(R.id.sb_volume2);
        mMusicFilter1 = (TextView) findViewById(R.id.tvMusicFilter1);
        mMusicFilter2 = (TextView) findViewById(R.id.tvMusicFilter2);
        mMusicFilterAll = (TextView) findViewById(R.id.tvMusicFilterAll);
        mIvVideoPlayState = (ImageView) findViewById(R.id.ivPlayerState);

        if (mMusicList.size() == 1) {
            findViewById(R.id.llSingleSoundEffect).setVisibility(View.GONE);
            findViewById(R.id.llVoice2).setVisibility(View.GONE);
        }
        mBtnNext.setText(R.string.export);
        mBtnNext.setVisibility(View.VISIBLE);
        findViewById(R.id.llMusicFilter1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final MusicFilterActivity.FilterInfo filterInfo = getFilterInfo(++mFilterInfoIndex1 % mFilterInfos.length);
                //方式1：
                mMusicList.get(0).setMusicFilter(filterInfo.filterType);
                mMusicFilter1.setText(filterInfo.title);
                mFilterInfoIndexAll = 0;
                mMusicFilterAll.setText(getFilterInfo(mFilterInfoIndexAll).title);
            }
        });

        findViewById(R.id.llMusicFilter2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final MusicFilterActivity.FilterInfo filterInfo = getFilterInfo(++mFilterInfoIndex2 % mFilterInfos.length);

                mVirtualAudio.setMusicFilter(mMusicList.get(1).getMusicPath(), filterInfo.filterType);
                mMusicFilter2.setText(filterInfo.title);

                mFilterInfoIndexAll = 0;
                mMusicFilterAll.setText(getFilterInfo(0).title);
            }
        });
        findViewById(R.id.llMusicFilterAll).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFilterInfoIndexAll = ++mFilterInfoIndexAll % mFilterInfos.length;

                onSetMusicFilterAll();

                //方式1：
                {
                    MusicFilterType type = getFilterInfo(mFilterInfoIndexAll).filterType;
                    int len = mMusicList.size();
                    for (int i = 0; i < len; i++) {


                        android.util.Log.e(TAG, "onClick: " + type);
                        if (type == MusicFilterType.MUSIC_FILTER_ECHO) {
                            MusicFilterType.MusicReverbOption[] echo = new MusicFilterType.MusicReverbOption[4];
                            echo[0] = new MusicFilterType.MusicReverbOption(0.2f, 0.8f);
                            echo[1] = new MusicFilterType.MusicReverbOption(0.4f, 0.6f);
                            echo[2] = new MusicFilterType.MusicReverbOption(0.6f, 0.4f);
                            echo[3] = new MusicFilterType.MusicReverbOption(0.8f, 0.2f);
                            mMusicList.get(i).setMusicFilter(MusicFilterType.MUSIC_FILTER_CUSTOM, 0.5f, echo, null);
                        } else if (type == MusicFilterType.MUSIC_FILTER_ROOM) {
                            MusicFilterType.MusicReverbOption[] echo = new MusicFilterType.MusicReverbOption[1];
                            echo[0] = new MusicFilterType.MusicReverbOption(0.02f, 0.2f);

                            MusicFilterType.MusicReverbOption[] reverb = new MusicFilterType.MusicReverbOption[1];
                            reverb[0] = new MusicFilterType.MusicReverbOption(0.02f, 0.25f);
                            mMusicList.get(i).setMusicFilter(MusicFilterType.MUSIC_FILTER_CUSTOM, 0.5f, echo, reverb);
                        } else {
                            mMusicList.get(i).setMusicFilter(type);
                        }


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

                mVirtualAudio.setOriginalMixFactor(mMusicList.get(0).getMusicPath(), progress);
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

                mVirtualAudio.setOriginalMixFactor(mMusicList.get(1).getMusicPath(), progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        findViewById(R.id.btnLeft).setOnClickListener(new View.OnClickListener() {
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
                    mVirtualAudio.seekTo(p);
                    currentTv.setText(getFormatTime(progress));
                }
            }

            private boolean isPlaying = false;

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if ((isPlaying = mVirtualAudio.isPlaying())) {
                    isPlaying = true;
                    mVirtualAudio.pause();
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (isPlaying) {
                    mVirtualAudio.start();
                }
            }
        });
        onSetMusicFilterAll();
    }

    private void onSetMusicFilterAll() {
        final MusicFilterActivity.FilterInfo filterInfo = getFilterInfo(mFilterInfoIndexAll);
        mMusicFilterAll.setText(filterInfo.title);
        mFilterInfoIndex2 = mFilterInfoIndex1 = mFilterInfoIndexAll;
        mMusicFilter1.setText(getFilterInfo(mFilterInfoIndex1).title);
        mMusicFilter2.setText(getFilterInfo(mFilterInfoIndex2).title);
    }


    private Dialog mCancelLoading;
    private Handler mHandler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case CANCEL_EXPORT: {
                    mCancelLoading = SysAlertDialog.showLoadingDialog(
                            MusicFilterNpActivity.this, R.string.canceling, false,
                            new DialogInterface.OnCancelListener() {

                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    mCancelLoading = null;
                                }
                            });
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (null != mCancelLoading)
                                mCancelLoading.setCancelable(true);
                        }
                    }, 5000);
                }
                break;
                default: {
                }
                break;
            }
        }
    };

    private boolean isPlayingORecording = false;
    private boolean bInterceptRepeat = false;


    private String getFormatTime(int msec) {
        return DateTimeUtils.stringForMillisecondTime(msec, true, true);
    }

    /**
     * 注册播放器回调
     */
    private void initPlayerListener(final PlayerControl player) {

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

    /***
     * 初始化播放器媒体资源
     */
    private void initPlayerData() {
        //加载全部视频
        reload(mVirtualAudio);
        onSeekTo(0);
    }


    private void onSeekTo(float progress) {
        int tp = Utils.s2ms(progress);
        currentTv.setText(getFormatTime(tp));
        mRdSeekBar.setProgress(tp);
    }

    @Override
    protected void onDestroy() {
        if (null != mVirtualAudio) {
            mVirtualAudio.cleanUp();
            mVirtualAudio = null;
        }
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
            mVirtualAudio.seekTo(lastProgress);
            onSeekTo(lastProgress);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        lastProgress = -1f;

        if (null != mVirtualAudio) {
            if (mVirtualAudio.isPlaying()) {
                //暂停
                mVirtualAudio.pause();
            }
            //记录播放器位置
            lastProgress = mVirtualAudio.getCurrentPosition();
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
                        if (null != mVirtualAudio) {
                            mVirtualAudio.stop();
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
     */
    private void reload(VirtualAudio virtualAudio) {
        int size = mMusicList.size();
        Music music;

        for (int index = 0; index < size; index++) {
            music = mMusicList.get(index);
            if (index == 0) {
                music.setMixFactor(mSbVoice1.getProgress());
                music.setMusicFilter(getFilterInfo(mFilterInfoIndex1).filterType);
            } else {
                music.setMixFactor(mSbVoice2.getProgress());
                music.setMusicFilter(getFilterInfo(mFilterInfoIndex2).filterType);
            }
            try {
                music.setTimelineRange(0,music.getDuration());
                virtualAudio.addMusic(music, true);
            } catch (InvalidArgumentException e) {
                e.printStackTrace();
            }
            virtualAudio.build();
        }
    }

    private HorizontalProgressDialog vExpDialog;

    /**
     * 导出
     */
    private void onExport() {
        onPause();
        //可以导出有资源
        final String mOutFilePath = PathUtils.getTempFileNameForSdcard("audio", "m4a");
        AudioConfig audioConfig = new AudioConfig();
        audioConfig.setAudioEncodingParameters(2, 44100, 128000);
        mVirtualAudio.export(this, mOutFilePath, audioConfig, new ExportListener() {

            @Override
            public void onExportStart() {
                vExpDialog = SysAlertDialog.showHoriProgressDialog(
                        MusicFilterNpActivity.this, "音频",
                        false, false, new DialogInterface.OnCancelListener() {

                            @Override
                            public void onCancel(DialogInterface dialog) {

                            }
                        });
                vExpDialog.setCanceledOnTouchOutside(false);
                vExpDialog.setOnCancelClickListener(new HorizontalProgressDialog.onCancelClickListener() {

                    @Override
                    public void onCancel() {
                        SysAlertDialog.showAlertDialog(
                                MusicFilterNpActivity.this,
                                "",
                                getString(R.string.cancel_export),
                                getString(R.string.no), new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                },
                                getString(R.string.yes), new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        vExpDialog.cancel();
                                        vExpDialog.dismiss();
                                        mVirtualAudio.cancelExport();
                                        mHandler.obtainMessage(CANCEL_EXPORT)
                                                .sendToTarget();
                                    }
                                });
                    }
                });
            }

            @Override
            public boolean onExporting(int progress, int max) {
                if (null != vExpDialog) {
                    vExpDialog.setMax(max);
                    vExpDialog.setProgress(progress);
                }
                return true;
            }

            @Override
            public void onExportEnd(int nResult) {
                if (null != vExpDialog) {
                    vExpDialog.cancel();
                    vExpDialog = null;
                }
                SysAlertDialog.cancelLoadingDialog();
                if (nResult >= VirtualVideo.RESULT_SUCCESS) {
                    VideoConfig tmp = new VideoConfig();
                    float du = getMediaInfo(mOutFilePath, tmp);
                    gotoNext(mOutFilePath);
                } else {
                    if (null != mCancelLoading) {
                        mCancelLoading.cancel();
                        mCancelLoading.dismiss();
                        mCancelLoading = null;
                    }
                    if (nResult == VirtualVideo.RESULT_EXPORT_CANCEL) {
                        onToast(getString(R.string.export_canceled));
                    } else {
                        onToast(getString(R.string.export_failed));
                    }
                    FileUtils.deleteAll(mOutFilePath);//清除失败的临时文件
                }
            }
        });
    }

    /**
     * 返回数据
     */
    private void gotoNext(String outpath) {
        Intent intent = new Intent();
        intent.putExtra(SdkEntry.EDIT_RESULT, outpath);
        setResult(RESULT_OK, intent);
        finish();
    }

    private MusicFilterActivity.FilterInfo getFilterInfo(int index) {
        if (index >= 0 && index < mFilterInfos.length) {
            return mFilterInfos[index];
        } else {
            return mFilterInfos[0];
        }
    }
}
