package com.rd.veuisdk.demo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.rd.lib.ui.PreviewFrameLayout;
import com.rd.vecore.VirtualVideo;
import com.rd.vecore.VirtualVideoView;
import com.rd.vecore.exception.InvalidStateException;
import com.rd.vecore.models.MusicFilterType;
import com.rd.vecore.models.Scene;
import com.rd.vecore.utils.MiscUtils;
import com.rd.veuisdk.BaseActivity;
import com.rd.veuisdk.IVideoMusicEditor;
import com.rd.veuisdk.R;
import com.rd.veuisdk.fragment.MusicEffectFragment;
import com.rd.veuisdk.utils.IParamData;
import com.rd.veuisdk.utils.IParamDataImp;
import com.rd.veuisdk.utils.IParamHandler;
import com.rd.veuisdk.utils.IntentConstants;
import com.rd.veuisdk.utils.SysAlertDialog;


/**
 * 小功能-变声
 */
public class VideoSoundEffectActivity extends BaseActivity implements MusicEffectFragment.IMusicEffectCallBack, IParamHandler, IVideoMusicEditor {


    private PreviewFrameLayout mPflVideoPreview;
    private TextView mTvVideoCurrentPos;
    private TextView mTvVideoDuration;
    private SeekBar mSbPlayControl;
    private ImageView mIvVideoPlayState;
    private VirtualVideoView mPlayer;
    private int mLastPlayPostion;
    private Scene mScene;
    private MusicEffectFragment mFragment;

    /**
     * sdk-视频变声
     *
     * @param needExport 是否直接导出视频
     */
    public static void videoSoundEffect(Context context, Scene scene, boolean needExport, int requestCode) {
        Intent intent = new Intent(context, VideoSoundEffectActivity.class);
        intent.putExtra(IntentConstants.INTENT_EXTRA_SCENE, scene);
        intent.putExtra(IntentConstants.INTENT_NEED_EXPORT, needExport);
        ((Activity) context).startActivityForResult(intent, requestCode);
    }

    private IParamDataImp mParamDataImp = new IParamDataImp();

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
        mFragment = MusicEffectFragment.newInstance();
        changeFragment(R.id.fragmentParent, mFragment);
        mVirtualVideo = new VirtualVideo();
        build();
    }

    private void initView() {
        $(R.id.titlebar_layout).setVisibility(View.GONE);
        mPflVideoPreview = $(R.id.rlPreview);
        mTvVideoCurrentPos = $(R.id.tvEditorCurrentPos);
        mTvVideoDuration = $(R.id.tvEditorDuration);
        mSbPlayControl = $(R.id.sbEditor);
        mIvVideoPlayState = $(R.id.ivPlayerState);
        mPlayer = $(R.id.epvPreview);
        mPflVideoPreview.setClickable(true);
        $(R.id.ivPlayerState).setOnClickListener(mPlayStateListener);
        mPflVideoPreview.setOnClickListener(mPlayStateListener);
        mSbPlayControl.setOnSeekBarChangeListener(mOnSeekbarListener);
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
        virtualVideo.addScene(mScene);
        virtualVideo.setMusicFilter(MusicFilterType.valueOf(mParamDataImp.getSoundEffectId()));
    }

    private void build() {
        SysAlertDialog.showLoadingDialog(this, R.string.loading);
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

    @Override
    public void changeMusicFilter() {
        if (null != mPlayer) {
            mVirtualVideo.setMusicFilter(MusicFilterType.valueOf(mParamDataImp.getSoundEffectId()));
            if (!mPlayer.isPlaying()) {
                playVideo();
            }
        }
    }

    @Override
    public IParamData getParamData() {
        return mParamDataImp;
    }

    @Override
    public boolean isMediaMute() {
        return false;
    }

    @Override
    public void reload(boolean onlyMusic) {

    }

    @Override
    public boolean isPlaying() {
        return mPlayer.isPlaying();
    }

    @Override
    public void start() {
        playVideo();
    }

    @Override
    public void pause() {
        pauseVideo();
    }

    @Override
    public void seekTo(int msec) {
        onSeekTo(msec);
    }

    @Override
    public int getDuration() {
        return MiscUtils.s2ms(mPlayer.getDuration());
    }

    @Override
    public int getCurrentPosition() {
        return MiscUtils.s2ms(mPlayer.getCurrentPosition());
    }

    @Override
    public VirtualVideo getEditorVideo() {
        return mVirtualVideo;
    }

    @Override
    public void removeMvMusic(boolean remove) {

    }

    @Override
    public void onBack() {
        this.finish();
    }

    @Override
    public void onBackPressed() {
        mFragment.onBackPressed();
    }

    @Override
    public void onSure() {
        onExport();
    }
}
