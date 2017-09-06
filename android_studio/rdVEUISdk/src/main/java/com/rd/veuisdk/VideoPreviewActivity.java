package com.rd.veuisdk;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.VideoView;

import com.rd.lib.ui.PreviewFrameLayout;
import com.rd.veuisdk.utils.DateTimeUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 预览视频
 *
 * @author ADMIN
 */
@SuppressLint("HandlerLeak")
public class VideoPreviewActivity extends BaseActivity {
    @BindView(R2.id.rlPreview)
    PreviewFrameLayout mPflVideoPreview;
    @BindView(R2.id.tvEditorCurrentPos)
    TextView mTvVideoCurrentPos;
    @BindView(R2.id.tvEditorDuration)
    TextView mTvVideoDuration;
    @BindView(R2.id.sbEditor)
    SeekBar mSbPlayControl;
    @BindView(R2.id.ivPlayerState)
    ImageView mIvVideoPlayState;
    @BindView(R2.id.vvPriview)
    VideoView mVideoPlayer;

    private int mLastPlayPostion;
    private String mPath;
    public static final String ACTION_PATH = "action_path";

    /**
     * 处理播放进度runnable
     */
    private Runnable mPlayProgressRunnable = new Runnable() {

        @Override
        public void run() {
            int nPosition = mVideoPlayer.getCurrentPosition();
            mVideoPlayer.postDelayed(this, 100);
            mSbPlayControl.setProgress(nPosition);
            mTvVideoCurrentPos.setText(gettime(nPosition));
        }
    };

    @SuppressLint("NewApi")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mStrActivityPageName = getString(R.string.priview_title);
        setContentView(R.layout.activity_video_prieview);
        ButterKnife.bind(this);
        Intent in = getIntent();
        Uri uri = null;
        if (Intent.ACTION_VIEW.equals(in.getAction())) {
            uri = in.getData();
        } else {
            mPath = in.getStringExtra(ACTION_PATH);
        }
        findViewById(R.id.btnLeft).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        TextView title = (TextView) findViewById(R.id.tvTitle);
        title.setText(mStrActivityPageName);


        mPflVideoPreview.setClickable(true);
        mPflVideoPreview.setOnClickListener(mPlayStateListener);

        mSbPlayControl.setOnSeekBarChangeListener(mOnSeekbarListener);
        mSbPlayControl.setMax(100);

        mLastPlayPostion = -1;

        mVideoPlayer.setOnPreparedListener(new OnPreparedListener() {

            @Override
            public void onPrepared(MediaPlayer mp) {
                prepare(mp);
            }
        });
        mVideoPlayer.setOnErrorListener(new OnErrorListener() {

            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                com.rd.veuisdk.utils.Utils.autoToastNomal(
                        VideoPreviewActivity.this, R.string.preview_error);
                onBackPressed();
                return false;
            }
        });
        mVideoPlayer.setOnCompletionListener(new OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
                onComplete();
                mVideoPlayer.removeCallbacks(mPlayProgressRunnable);
            }
        });
        if (!TextUtils.isEmpty(mPath))
            mVideoPlayer.setVideoPath(mPath);
        if (null != uri)
            mVideoPlayer.setVideoURI(uri);

    }

    private void prepare(MediaPlayer mp) {
        // Log.d("onPrepared....", "" + mp.getDuration());
        if (mIvVideoPlayState.getVisibility() != View.VISIBLE) {
            mIvVideoPlayState.setVisibility(View.VISIBLE);
        }
        onSeekTo(0);
        if (mLastPlayPostion == -1) {
            mTvVideoCurrentPos.setText(gettime(0));
            mTvVideoDuration.setText(gettime(mp.getDuration()));
            mSbPlayControl.setMax(mp.getDuration());

            updatePreviewFrameAspect(mp.getVideoWidth(), mp.getVideoHeight());
        }

        playVideo();

    }

    private String gettime(int progress) {
        return DateTimeUtils.stringForMillisecondTime(progress, false, true);
    }

    private OnClickListener mPlayStateListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            clickView(v);
        }
    };

    private void onSeekTo(int seekto) {
        mVideoPlayer.removeCallbacks(mPlayProgressRunnable);
        mVideoPlayer.seekTo(seekto);
    }

    private void onComplete() {
        onSeekTo(0);
        mSbPlayControl.setProgress(0);
        mIvVideoPlayState.setImageResource(R.drawable.btn_play);
        mIvVideoPlayState.setVisibility(View.VISIBLE);
        mTvVideoCurrentPos.setText(DateTimeUtils.stringForMillisecondTime(0,
                false, true));

    }

    /**
     * 更新预览视频播放器比例
     */
    protected void updatePreviewFrameAspect(final int m_nVideoWidth,
                                            final int m_nVideoHeight) {

        if (mPflVideoPreview != null) {
            if (m_nVideoWidth > 0 && m_nVideoHeight > 0) {
                mPflVideoPreview.setAspectRatio((float) m_nVideoWidth
                        / m_nVideoHeight);
            } else {
                mPflVideoPreview.setAspectRatio(4.0f / 3.0);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (null != mVideoPlayer && mVideoPlayer.isPlaying()) {
            pauseVideo();
        }
        super.onBackPressed();
    }

    public void clickView(View v) {

        if (mVideoPlayer.isPlaying()) {
            pauseVideo();
        } else {
            playVideo();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (null != mVideoPlayer) {
            mLastPlayPostion = mVideoPlayer.getCurrentPosition();
            pauseVideo();
        }

    }

    @Override
    protected void onDestroy() {
        if (null != mVideoPlayer) {
            mVideoPlayer.stopPlayback();
        }
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 从后台切换到前台，并且已经成功打开正常的视频，进行继续播放
        if (mLastPlayPostion > 0) {
            if (null != mVideoPlayer) {
                onSeekTo(mLastPlayPostion);
                mLastPlayPostion = -1;
                playVideo();

            }
        }
    }

    private OnSeekBarChangeListener mOnSeekbarListener = new OnSeekBarChangeListener() {
        private boolean IsPlayingOnSeek; // Seek时是否播放中...

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
            if (fromUser) {
                mTvVideoCurrentPos.setText(DateTimeUtils
                        .stringForMillisecondTime(progress, false, true));
                onSeekTo(progress);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            if (mVideoPlayer.isPlaying()) {
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
        mVideoPlayer.start();
        mIvVideoPlayState.setImageResource(R.drawable.btn_pause);
        Animation an = AnimationUtils.loadAnimation(getApplicationContext(),
                android.R.anim.fade_out);
        mIvVideoPlayState.setAnimation(an);
        mIvVideoPlayState.setVisibility(View.INVISIBLE);
        mVideoPlayer.removeCallbacks(mPlayProgressRunnable);
        mVideoPlayer.post(mPlayProgressRunnable);

    }

    private void pauseVideo() {
        if (mVideoPlayer.isPlaying()) {
            mVideoPlayer.pause();
        }
        mIvVideoPlayState.setImageResource(R.drawable.btn_play);
        mIvVideoPlayState.setVisibility(View.VISIBLE);
        mVideoPlayer.removeCallbacks(mPlayProgressRunnable);
    }

}
