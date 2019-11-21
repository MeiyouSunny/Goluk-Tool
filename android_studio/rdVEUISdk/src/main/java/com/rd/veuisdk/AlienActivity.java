package com.rd.veuisdk;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.rd.lib.ui.ExtButton;
import com.rd.lib.ui.RotateRelativeLayout;
import com.rd.lib.utils.CoreUtils;
import com.rd.lib.utils.FileUtils;
import com.rd.vecore.VirtualVideo;
import com.rd.vecore.VirtualVideoView;
import com.rd.vecore.exception.InvalidStateException;
import com.rd.vecore.models.AspectRatioFitMode;
import com.rd.vecore.models.MaskObject;
import com.rd.vecore.models.MediaObject;
import com.rd.vecore.models.PermutationMode;
import com.rd.vecore.models.Scene;
import com.rd.vecore.models.VideoConfig;
import com.rd.vecore.utils.Log;
import com.rd.veuisdk.ui.RdSeekBar;
import com.rd.veuisdk.utils.DateTimeUtils;
import com.rd.veuisdk.utils.IntentConstants;
import com.rd.veuisdk.utils.PathUtils;
import com.rd.veuisdk.utils.SysAlertDialog;
import com.rd.veuisdk.utils.Utils;
import com.rd.veuisdk.utils.ViewUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 异形演示
 */
public class AlienActivity extends BaseActivity {
    private String TAG = "AlienActivity";
    private ExtButton mBtnNext, mBtnLeft;
    private TextView mTvTitle;
    private VirtualVideoView player;
    private ImageView mIvVideoPlayState;
    private RdSeekBar mRdSeekBar;
    private TextView currentTv;
    private TextView totalTv;
    private RotateRelativeLayout mProgressLayout;
    private List<MediaObject> mArrImage = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_alien_layout);
        mArrImage = getIntent().getParcelableArrayListExtra(IntentConstants.EXTRA_MEDIA_LIST);
        MediaObject mediaObject;
        if (mArrImage.size() > 1) {
            mediaObject = mArrImage.get(1);
        } else {
            mediaObject = mArrImage.get(0);
        }
        String dst = PathUtils.getAssetFileNameForSdcard("mask", ".png");
        CoreUtils.assetRes2File(getAssets(), "mask.png", dst);
        if (FileUtils.isExist(dst)) {
            MaskObject maskObject = new MaskObject();
            maskObject.setMediaPath(dst);
            mediaObject.setMaskObject(maskObject);
        }
        initView();
        mTvTitle.setText(R.string.priview_title);
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
                        pause();
                    } else {
                        start();
                    }
                }
            }
        });
        initPlayerListener(player);
        initPlayerData();
    }

    private void start() {
        isPlayingORecording = true;
        player.start();
        mIvVideoPlayState.setImageResource(R.drawable.btn_pause);
        ViewUtils.fadeOut(AlienActivity.this, mIvVideoPlayState);
    }

    /**
     * 暂停播放
     */
    private void pause() {
        player.pause();
        mIvVideoPlayState.clearAnimation();
        mIvVideoPlayState.setImageResource(R.drawable.btn_play);
        mIvVideoPlayState.setVisibility(View.VISIBLE);
        isPlayingORecording = false;
    }

    private void initView() {
        mProgressLayout = (RotateRelativeLayout) findViewById(R.id.rlPlayerBottomMenu);
        mRdSeekBar = (RdSeekBar) findViewById(R.id.sbEditor);
        currentTv = (TextView) findViewById(R.id.tvCurTime);
        totalTv = (TextView) findViewById(R.id.tvTotalTime);
        mBtnNext = (ExtButton) findViewById(R.id.btnRight);
        mBtnLeft = (ExtButton) findViewById(R.id.btnLeft);
        mTvTitle = (TextView) findViewById(R.id.tvTitle);
        player = (VirtualVideoView) findViewById(R.id.player);
        mIvVideoPlayState = (ImageView) findViewById(R.id.ivPlayerState);
        mBtnNext.setVisibility(View.VISIBLE);
        mBtnNext.setText(R.string.export);

        mBtnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onExport();
            }
        });

        mBtnLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
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
    }


    private void build() {
        reload(mVirtualVideo);
        try {
            mVirtualVideo.build(player);
        } catch (InvalidStateException e) {
            e.printStackTrace();
        }
        start();
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
                SysAlertDialog.cancelLoadingDialog();
                int ms = Utils.s2ms(player.getDuration());
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
                Log.i(TAG, "onPlayerCompletion:  播放完毕-->" + player.getDuration());
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
        player.setAspectRatioFitMode(AspectRatioFitMode.KEEP_ASPECTRATIO);
        player.setPreviewAspectRatio(1f);
        //加载全部视频
        boolean hasVideo = reload(mVirtualVideo);
        if (hasVideo) {
            try {
                mVirtualVideo.build(player);
            } catch (InvalidStateException e) {
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, "initPlayerData: 没有视频!");
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

    public void onToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
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


    /**
     * 加载视频资源
     *
     * @param virtualVideo
     */
    private boolean reload(VirtualVideo virtualVideo) {
        virtualVideo.reset();
        Scene scene = VirtualVideo.createScene();
        for (MediaObject mediaObject : mArrImage) {
            scene.addMedia(mediaObject);
        }
        scene.setPermutationMode(PermutationMode.COMBINATION_MODE);
        virtualVideo.addScene(scene);
        return true;
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
        VideoConfig videoConfig = exportHandler.getExportConfig(9 / 16.0f);
        exportHandler.onExport(true, videoConfig);

    }


}
