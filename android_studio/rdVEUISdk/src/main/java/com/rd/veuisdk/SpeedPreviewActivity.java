package com.rd.veuisdk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.rd.lib.ui.PreviewFrameLayout;
import com.rd.vecore.VirtualVideo;
import com.rd.vecore.VirtualVideoView;
import com.rd.vecore.exception.InvalidStateException;
import com.rd.vecore.models.EffectInfo;
import com.rd.vecore.models.MediaObject;
import com.rd.vecore.models.Scene;
import com.rd.veuisdk.model.VideoOb;
import com.rd.veuisdk.ui.RdSeekBar;
import com.rd.veuisdk.ui.RulerSeekbar;
import com.rd.veuisdk.utils.EffectManager;
import com.rd.veuisdk.utils.IntentConstants;
import com.rd.veuisdk.utils.SysAlertDialog;
import com.rd.veuisdk.utils.Utils;
import com.rd.veuisdk.utils.ViewUtils;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * 视频调速
 */
public class SpeedPreviewActivity extends BaseActivity {
    private final String TAG = "SpeedPreviewActivity";

    PreviewFrameLayout mPflVideoPreview;
    TextView mTvVideoDuration;
    ImageView mIvVideoPlayState;
    VirtualVideoView mMediaPlayer;
    RdSeekBar mPbPreview;
    RulerSeekbar mDragSpeed;
    TextView tvCurSpeed;
    CheckBox mCbApplyToAll;

    private int mLastPlayPostion;
    private MediaObject mMedia;
    private boolean mIsAutoRepeat = true;
    private ArrayList<EffectInfo> mEffectInfos;

    /**
     * sdk-调速
     *
     * @param needExport 是否直接导出视频
     */
    public static void onSpeed(Context context, Scene scene, boolean needExport, int requestCode) {
        Intent intent = new Intent(context, SpeedPreviewActivity.class);
        intent.putExtra(IntentConstants.INTENT_EXTRA_SCENE, scene);
        intent.putExtra(IntentConstants.INTENT_NEED_EXPORT, needExport);
        ((Activity) context).startActivityForResult(intent, requestCode);
    }

    private boolean bExportVideo = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speed_prieview);
        Scene scene = getIntent().getParcelableExtra(IntentConstants.INTENT_EXTRA_SCENE);
        bExportVideo = getIntent().getBooleanExtra(IntentConstants.INTENT_NEED_EXPORT, false);
        mMedia = scene.getAllMedia().get(0);

        mEffectInfos = new ArrayList<>();
        if (null != mMedia.getEffectInfos()) {
            mEffectInfos.addAll(mMedia.getEffectInfos());
        }
        mMedia.setEffectInfos(null);

        mOldSpeed = mMedia.getSpeed();
        initView();
        onLoad();
        playVideo();
        mDragSpeed.setMax(100);
        mDragSpeed.postDelayed(new Runnable() {

            @Override
            public void run() {
                resetSpeedText(mMedia.getSpeed());
                mDragSpeed.setProgress(calculateProgress(mMedia.getSpeed(), 100));
            }
        }, 200);
        mDragSpeed.setOnSeekListener(new RulerSeekbar.OnSeekListener() {

            @Override
            public void onSeekStart(float progress, int max) {
                resetSpeedText(calculateSpeed(progress, max));
            }

            @Override
            public void onSeek(float progress, int max) {
                resetSpeedText(calculateSpeed(progress, max));
            }

            @Override
            public void onSeekEnd(float progress, int max) {
                mMedia.setSpeed(calculateSpeed(progress, max));
                resetSpeedText(mMedia.getSpeed());
                mMediaPlayer.stop();
                mMediaPlayer.reset();
                onLoad();
                playVideo();
            }
        });
    }

    private void resetSpeedText(float speed) {
        DecimalFormat fnum = new DecimalFormat("##0.00");
        String dd = fnum.format(speed);
        tvCurSpeed.setText("x" + dd);
    }


    /**
     * 根据速度计算进度条
     *
     * @param speed
     * @return
     */
    private float calculateProgress(float speed, int max) {
        float progress;
        if (speed < 0.5f) {
            progress = (speed - 0.25f) * (max / 4) / (0.5f - 0.25f);
        } else if (speed < 1) {
            progress = max / 4 + (speed - 0.5f) * (max / 4) / (1 - 0.5f);
        } else if (speed < 2) {
            progress = max / 2 + (speed - 1) * (max / 4) / (2 - 1);
        } else {
            progress = max * 3 / 4 + (speed - 2) * (max / 4) / (4 - 2);
        }
        return progress;
    }

    /**
     * 根据进度条计算速度
     *
     * @return
     */
    private float calculateSpeed(float progress, int max) {
        float speed;
        if (progress < max / 4) {
            speed = 0.25f + ((0.5f - 0.25f) * progress / (max / 4));
        } else if (progress < max / 2) {
            speed = 0.5f + ((1 - 0.5f) * (progress - max / 4) / (max / 4));
        } else if (progress < max * 3 / 4) {
            speed = 1 + ((2 - 1) * (progress - max / 2) / (max / 4));
        } else {
            speed = 2 + ((4 - 2) * (progress - max * 3 / 4) / (max / 4));
        }
        return speed;
    }


    /**
     * 加载媒体资源
     */
    private void onLoad() {
        Scene scene = VirtualVideo.createScene();
        scene.addMedia(mMedia);
        VirtualVideo virtualVideo = new VirtualVideo();
        try {
            virtualVideo.addScene(scene);
            virtualVideo.build(mMediaPlayer);
        } catch (InvalidStateException e) {
            e.printStackTrace();
        }
    }

    private OnClickListener mOnPlayerClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            if (mMediaPlayer.isPlaying()) {
                pauseVideo();
            } else {
                playVideo();
            }
        }
    };

    private VirtualVideoView.VideoViewListener mPlayerListener = new VirtualVideoView.VideoViewListener() {
        private float lastPosition;

        @Override
        public void onPlayerPrepared() {
            SysAlertDialog.cancelLoadingDialog();
            lastPosition = -1;
            int ms = Utils.s2ms(mMediaPlayer.getDuration());
            mPbPreview.setMax(ms);
            mTvVideoDuration.setText(getTime(ms));
            updatePreviewFrameAspect(mMediaPlayer.getVideoWidth(), mMediaPlayer.getVideoHeight());
        }

        @Override
        public boolean onPlayerError(int what, int extra) {
            onToast(R.string.preview_error);
            onBackPressed();
            return false;
        }

        @Override
        public void onPlayerCompletion() {
            onComplete();
        }

        @Override
        public void onGetCurrentPosition(float position) {
            if (position < lastPosition && mIsAutoRepeat) {
                lastPosition = -1;
                mIsAutoRepeat = false;
                pauseVideo();
                onComplete();
            } else {
                lastPosition = position;
                mPbPreview.setProgress(Utils.s2ms(position));
            }
        }
    };


    private void initView() {
        mPflVideoPreview = $(R.id.rlPreview);
        mTvVideoDuration = $(R.id.tvEditorDuration);
        mIvVideoPlayState = $(R.id.ivPlayerState);
        mMediaPlayer = $(R.id.epvPreview);
        mDragSpeed = $(R.id.dragViewSpeed);
        tvCurSpeed = $(R.id.tvCurSpeed);
        mCbApplyToAll = $(R.id.cbSpeedApplyToAll);
        if (bExportVideo) {
            mCbApplyToAll.setVisibility(View.GONE);
        }
        setText(R.id.tvBottomTitle, R.string.preview_speed);

        $(R.id.ivSure).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onSure();

            }
        });

        $(R.id.ivCancel).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                onBackPressed();
            }
        });

        mPflVideoPreview.setClickable(true);
        mLastPlayPostion = -1;

        mMediaPlayer = $(R.id.epvPreview);
        mMediaPlayer.setClearFirst(true);
        mMediaPlayer.setAutoRepeat(mIsAutoRepeat);
        mMediaPlayer.setOnClickListener(mOnPlayerClickListener);
        mMediaPlayer.setOnPlaybackListener(mPlayerListener);
        mPbPreview = $(R.id.pbPreview);
        mPbPreview.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            boolean mLastPlaying;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mMediaPlayer.seekTo(Utils.ms2s(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if ((mLastPlaying = mMediaPlayer.isPlaying())) {
                    mMediaPlayer.pause();
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mLastPlaying) {
                    mMediaPlayer.start();
                }
            }
        });
    }

    /**
     * 确认
     */
    private void onSure() {
        pauseVideo();

        Scene scene = new Scene();
        VideoOb temp = (VideoOb) mMedia.getTag();
        if (null != temp) {
            float ft = mMedia.getSpeed() / mOldSpeed;
            temp.nStart = temp.nStart / ft;
            temp.nEnd = temp.nEnd / ft;
            mMedia.setTag(temp);
        }
        //修正特效有效时间线
        EffectManager.fixEffect(mMedia, mEffectInfos);
        scene.addMedia(mMedia);
        if (bExportVideo) {
            onExport(scene);
        } else {
            Intent data = new Intent();
            data.putExtra(IntentConstants.INTENT_EXTRA_SCENE, scene);
            data.putExtra(IntentConstants.INTENT_ALL_APPLY, mCbApplyToAll.isChecked());
            setResult(RESULT_OK, data);
            onBackPressed();
        }
    }

    /**
     * 直接导出
     */
    private void onExport(final Scene scene) {
        ExportHandler exportHandler = new ExportHandler(this, new ExportHandler.IExport() {
            @Override
            public void addData(VirtualVideo virtualVideo) {
                virtualVideo.addScene(scene);
            }
        });
        exportHandler.onExport(mMediaPlayer.getWidth() / (float) mMediaPlayer.getHeight(), true);
    }

    private void onComplete() {
        mIvVideoPlayState.setImageResource(R.drawable.btn_play);
        mIvVideoPlayState.setVisibility(View.VISIBLE);
        mMediaPlayer.seekTo(0);
        mPbPreview.setProgress(0);
        mMediaPlayer.setAutoRepeat(mIsAutoRepeat);
    }

    /**
     * 更新预览视频播放器比例
     */
    protected void updatePreviewFrameAspect(int nVideoWidth, int nVideoHeight) {

        mPflVideoPreview.setAspectRatio((float) nVideoWidth / nVideoHeight);

    }

    private float mOldSpeed = 1.0f;

    @Override
    public void clickView(View v) {
        super.clickView(v);
        int id = v.getId();
        if (id == R.id.rlPreview) {
            if (mMediaPlayer.isPlaying()) {
                pauseVideo();
            } else {
                playVideo();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (null != mMediaPlayer) {
            mMediaPlayer.pause();
            mLastPlayPostion = Utils.s2ms(mMediaPlayer.getCurrentPosition());
            pauseVideo();
        }
    }

    @Override
    protected void onDestroy() {
        if (null != mMediaPlayer) {
            mMediaPlayer.stop();
            mMediaPlayer.reset();
        }
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMediaPlayer.start();
        // 从后台切换到前台，并且已经成功打开正常的视频，进行继续播放
        if (mLastPlayPostion == 0) {
            mMediaPlayer.seekTo(0);
        }
        if (mLastPlayPostion > 0) {
            if (null != mMediaPlayer) {
                mMediaPlayer.seekTo(mLastPlayPostion);
                mLastPlayPostion = -1;
                playVideo();
            }
        }
    }

    private void playVideo() {
        mMediaPlayer.start();
        mIvVideoPlayState.setImageResource(R.drawable.btn_pause);
        ViewUtils.fadeOut(this, mIvVideoPlayState);
    }

    private void pauseVideo() {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        }
        mIvVideoPlayState.clearAnimation();
        mIvVideoPlayState.setImageResource(R.drawable.btn_play);
        mIvVideoPlayState.setVisibility(View.VISIBLE);
    }
}
