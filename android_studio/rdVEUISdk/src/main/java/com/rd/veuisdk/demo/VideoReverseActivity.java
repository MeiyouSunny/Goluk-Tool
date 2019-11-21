package com.rd.veuisdk.demo;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.rd.lib.ui.ExtButton;
import com.rd.lib.ui.PreviewFrameLayout;
import com.rd.vecore.VirtualVideo;
import com.rd.vecore.VirtualVideoView;
import com.rd.vecore.exception.InvalidArgumentException;
import com.rd.vecore.exception.InvalidStateException;
import com.rd.vecore.listener.ExportListener;
import com.rd.vecore.models.Scene;
import com.rd.vecore.models.VideoConfig;
import com.rd.vecore.utils.ExportUtils;
import com.rd.vecore.utils.MiscUtils;
import com.rd.veuisdk.BaseActivity;
import com.rd.veuisdk.ExportHandler;
import com.rd.veuisdk.R;
import com.rd.veuisdk.ui.HorizontalProgressDialog;
import com.rd.veuisdk.utils.FileUtils;
import com.rd.veuisdk.utils.IntentConstants;
import com.rd.veuisdk.utils.PathUtils;
import com.rd.veuisdk.utils.SysAlertDialog;

/**
 * @author JIAN
 * @create 2019/6/26
 * @Describe
 */
public class VideoReverseActivity extends BaseActivity {

    private PreviewFrameLayout mPflVideoPreview;
    private TextView mTvVideoCurrentPos;
    private TextView mTvVideoDuration;
    private SeekBar mSbPlayControl;
    private ImageView mIvVideoPlayState;
    private VirtualVideoView mPlayer;
    private int mLastPlayPostion;
    private Scene mScene;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_transcoding);
        mScene = getIntent().getParcelableExtra(IntentConstants.INTENT_EXTRA_SCENE);
        if (null == mScene) {
            onToast(R.string.select_media_hint);
            finish();
            return;
        }
        initView();
        initPlayer();
        mVirtualVideo = new VirtualVideo();
        SysAlertDialog.showLoadingDialog(this, R.string.loading);
        build();
    }

    private boolean bIsReverseMode = false;
    private ExtButton button;

    private void initView() {
        button = new ExtButton(this);
        button.setText(R.string.reverse_play);
        button.setBackgroundResource(0);
        button.setTextColor(getResources().getColor(R.color.white));
        button.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!bIsReverseMode) {
                    bIsReverseMode = true;
                    onReverseMode();
                } else {
                    bIsReverseMode = false;
                    button.setText(R.string.reverse_play);
                    build();
                }
            }
        });

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);

        ((ViewGroup) $(R.id.fragmentParent)).addView(button, params);
        mPflVideoPreview = $(R.id.rlPreview);
        mTvVideoCurrentPos = $(R.id.tvEditorCurrentPos);
        mTvVideoDuration = $(R.id.tvEditorDuration);
        mSbPlayControl = $(R.id.sbEditor);
        mIvVideoPlayState = $(R.id.ivPlayerState);
        mPlayer = $(R.id.epvPreview);
        $(R.id.btnLeft).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        setText(R.id.tvTitle, R.string.preview_reverse);
        Button btnRight = $(R.id.btnRight);
        btnRight.setText(R.string.export);
        btnRight.setTextColor(getResources().getColor(R.color.main_orange));
        btnRight.setVisibility(View.VISIBLE);
        btnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onExport();
            }
        });
        mPflVideoPreview.setClickable(true);
        $(R.id.ivPlayerState).setOnClickListener(mPlayStateListener);
        mPflVideoPreview.setOnClickListener(mPlayStateListener);
        mSbPlayControl.setOnSeekBarChangeListener(mOnSeekbarListener);
    }


    private String mReversePath;


    private boolean bCanceled = false;

    //取消倒序
    private void onCancelReverse() {
        bCanceled = true;
        bIsReverseMode = false;
        button.setText(R.string.reverse_play);
        mReversePath = null;
    }

    /**
     * 倒序播放
     */
    private void onReverseMode() {
        pauseVideo();
        if (TextUtils.isEmpty(mReversePath)) {
            mReversePath = PathUtils.getTempFileNameForSdcard("reverse", "mp4");
        }
        bCanceled = false;
        if (!FileUtils.isExist(mReversePath)) {
            final HorizontalProgressDialog dialog = SysAlertDialog.showHoriProgressDialog(this, getString(R.string.reversing), true,
                    true, new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            //取消倒序
                            onCancelReverse();
                        }
                    });
            dialog.setOnCancelClickListener(new HorizontalProgressDialog.onCancelClickListener() {
                @Override
                public void onCancel() {
                    dialog.dismiss();
                    onCancelReverse();
                }
            });
            dialog.setCanceledOnTouchOutside(false);
            dialog.setMax(1000);
            VideoConfig videoConfig = ExportHandler.getExportConfig(0);
            ExportUtils.reverseSave(this, mScene.getAllMedia().get(0), mReversePath, videoConfig, new ExportListener() {

                @Override
                public void onExportStart() {
                }

                @Override
                public boolean onExporting(int progress, int max) {
                    dialog.setProgress(progress);
                    return !bCanceled;
                }

                @Override
                public void onExportEnd(int result) {
                    dialog.dismiss();
                    if (result >= VirtualVideo.RESULT_SUCCESS) {
                        button.setText(R.string.normal_play);
                        build();
                    } else {
                        //成功取消
                        bIsReverseMode = false;
                        mReversePath = null;
                    }
                }
            });
        } else {
            build();
        }

    }


    private VirtualVideo mVirtualVideo;

    private void initPlayer() {
        mPlayer.setOnPlaybackListener(new VirtualVideoView.VideoViewListener() {
            @Override
            public void onPlayerPrepared() {
                SysAlertDialog.cancelLoadingDialog();
                int duration = MiscUtils.s2ms(mPlayer.getDuration());
                mSbPlayControl.setMax(duration);
                mTvVideoDuration.setText(getTime(duration));
                mTvVideoCurrentPos.setText(getTime(0));
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
                onComplete();
            }

            @Override
            public void onGetCurrentPosition(float position) {
                int progress = MiscUtils.s2ms(position);
                mTvVideoCurrentPos.setText(getTime(progress));
                mSbPlayControl.setProgress(progress);
            }
        });


    }

    /**
     * 加载媒体数据到虚拟视频
     */
    private void reload(VirtualVideo virtualVideo) {
        if (bIsReverseMode) {
            Scene scene = VirtualVideo.createScene();
            try {
                scene.addMedia(mReversePath);
                virtualVideo.addScene(scene);
            } catch (InvalidArgumentException e) {
                e.printStackTrace();
            }
        } else {
            virtualVideo.addScene(mScene);
        }
    }

    private void build() {
        pauseVideo();
        mVirtualVideo.reset();
        reload(mVirtualVideo);
        try {
            mVirtualVideo.build(mPlayer);
            playVideo();
        } catch (InvalidStateException e) {
            e.printStackTrace();
        }

    }


    private View.OnClickListener mPlayStateListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (mPlayer.isPlaying()) {
                pauseVideo();
            } else {
                playVideo();
            }
        }
    };

    private void onSeekTo(int seekto) {
        mPlayer.seekTo(MiscUtils.ms2s(seekto));
        mTvVideoCurrentPos.setText(getTime(seekto));
    }

    private void onComplete() {
        onSeekTo(0);
        mSbPlayControl.setProgress(0);
        mIvVideoPlayState.setImageResource(R.drawable.btn_play);
        mIvVideoPlayState.setVisibility(View.VISIBLE);

    }


    @Override
    protected void onResume() {
        super.onResume();
        // 从后台切换到前台，并且已经成功打开正常的视频，进行继续播放
        if (mLastPlayPostion > 0) {
            if (null != mPlayer) {
                onSeekTo(mLastPlayPostion);
                mLastPlayPostion = -1;
                playVideo();

            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (null != mPlayer) {
            mLastPlayPostion = MiscUtils.s2ms(mPlayer.getCurrentPosition());
            pauseVideo();
        }

    }

    @Override
    public void onBackPressed() {
        showCancelEditDialog();
    }

    @Override
    protected void onDestroy() {
        if (null != mPlayer) {
            mPlayer.stop();
            mPlayer.cleanUp();
            mPlayer = null;
        }
        if (null != mVirtualVideo) {
            mVirtualVideo.release();
            mVirtualVideo = null;
        }
        super.onDestroy();
    }

    private SeekBar.OnSeekBarChangeListener mOnSeekbarListener = new SeekBar.OnSeekBarChangeListener() {
        private boolean IsPlayingOnSeek; // Seek时是否播放中...

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                onSeekTo(progress);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            if (mPlayer.isPlaying()) {
                pauseVideo();
                IsPlayingOnSeek = true;
            } else {
                IsPlayingOnSeek = false;
            }
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            if (IsPlayingOnSeek) {
                playVideo();
            }
        }
    };

    private void playVideo() {
        mPlayer.start();
        mIvVideoPlayState.setImageResource(R.drawable.btn_pause);
        mIvVideoPlayState.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out));
        mIvVideoPlayState.setVisibility(View.INVISIBLE);

    }

    private void pauseVideo() {
        if (mPlayer.isPlaying()) {
            mPlayer.pause();
        }
        mIvVideoPlayState.setImageResource(R.drawable.btn_play);
        mIvVideoPlayState.setVisibility(View.VISIBLE);
    }


    /**
     * 导出
     */
    private void onExport() {
        mPlayer.stop();
        com.rd.veuisdk.ExportHandler exportHandler = new com.rd.veuisdk.ExportHandler(this, new com.rd.veuisdk.ExportHandler.IExport() {
            @Override
            public void addData(VirtualVideo virtualVideo) {
                reload(virtualVideo);
            }
        });
        exportHandler.onExport(
                mPlayer.getVideoWidth() / (float) mPlayer.getVideoHeight(), true);
    }
}
