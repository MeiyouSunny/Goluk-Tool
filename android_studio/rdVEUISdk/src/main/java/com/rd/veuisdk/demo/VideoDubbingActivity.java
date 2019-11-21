package com.rd.veuisdk.demo;

import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.rd.lib.ui.PreviewFrameLayout;
import com.rd.vecore.Music;
import com.rd.vecore.VirtualVideo;
import com.rd.vecore.VirtualVideoView;
import com.rd.vecore.exception.InvalidArgumentException;
import com.rd.vecore.exception.InvalidStateException;
import com.rd.vecore.models.Scene;
import com.rd.vecore.models.VisualFilterConfig;
import com.rd.vecore.utils.MiscUtils;
import com.rd.veuisdk.BaseActivity;
import com.rd.veuisdk.IVideoEditorHandler;
import com.rd.veuisdk.R;
import com.rd.veuisdk.TempVideoParams;
import com.rd.veuisdk.ae.model.AETemplateInfo;
import com.rd.veuisdk.fragment.AudioFragment;
import com.rd.veuisdk.utils.IntentConstants;
import com.rd.veuisdk.utils.SysAlertDialog;

/**
 * 视频配音
 */
public class VideoDubbingActivity extends BaseActivity implements IVideoEditorHandler {

    private PreviewFrameLayout mPflVideoPreview;
    private VirtualVideoView mPlayer;
    private int mLastPlayPostion;
    private Scene mScene;
    private AudioFragment mAudioFragment;
    /*
     * 盛放EditorPreivewPositionListener的列表
     */
    private SparseArray<EditorPreivewPositionListener> mSaEditorPostionListener = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_transcoding);
        mSaEditorPostionListener = new SparseArray<EditorPreivewPositionListener>();
        mScene = getIntent().getParcelableExtra(IntentConstants.INTENT_EXTRA_SCENE);
        if (null == mScene) {
            onToast(R.string.select_media_hint);
            finish();
            return;
        }
        isFirst = true;
        initView();
        initPlayer();
        mVirtualVideo = new VirtualVideo();
        build();
    }

    private void initView() {
        mPflVideoPreview = $(R.id.rlPreview);
        $(R.id.player_bar_layout).setVisibility(View.GONE);
        $(R.id.ivPlayerState).setVisibility(View.GONE);
        mPlayer = $(R.id.epvPreview);
        $(R.id.titlebar_layout).setVisibility(View.GONE);
        mPflVideoPreview.setClickable(true);
        mPflVideoPreview.setOnClickListener(mPlayStateListener);

    }

    private VirtualVideo mVirtualVideo;

    private boolean isFirst = true;

    private void initPlayer() {
        mPlayer.setOnPlaybackListener(new VirtualVideoView.VideoViewListener() {
            @Override
            public void onPlayerPrepared() {
                SysAlertDialog.cancelLoadingDialog();
                int len = mSaEditorPostionListener.size();
                for (int nTmp = 0; nTmp < len; nTmp++) {
                    mSaEditorPostionListener.valueAt(nTmp).onEditorPrepred();
                }
                if (isFirst) { //需要依赖媒体的时长等信息，待播放器准备就绪后再切换到配音界面*********************
                    isFirst = false;
                    mAudioFragment = AudioFragment.newInstance();
                    mAudioFragment.setSeekBar((LinearLayout) $(R.id.llAudioFactor));
                    mAudioFragment.setShowFactor(true);
                    changeFragment(R.id.fragmentParent, mAudioFragment);
                }
                notifyCurrentPosition(getCurrentPosition());
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
                notifyPreviewComplete();
            }

            @Override
            public void onGetCurrentPosition(float position) {
                notifyCurrentPosition(MiscUtils.s2ms(position));
            }
        });


    }


    private void notifyCurrentPosition(int positionMs) {
        int duration = getDuration();
        for (int nTmp = 0; nTmp < mSaEditorPostionListener.size(); nTmp++) {
            mSaEditorPostionListener.valueAt(nTmp).onEditorGetPosition(positionMs, duration);
        }
    }


    private void notifyPreviewComplete() {
        for (int nTmp = 0; nTmp < mSaEditorPostionListener.size(); nTmp++) {
            mSaEditorPostionListener.valueAt(nTmp).onEditorPreviewComplete();
        }
    }

    /**
     * 加载媒体数据到虚拟视频
     */
    private void reload(VirtualVideo virtualVideo, boolean onlyMusic) {
        if (onlyMusic) {
            if (isPlaying()) {
                mPlayer.pause();
            }
            virtualVideo.clearMusic();
            addMusic(virtualVideo);
            virtualVideo.updateMusic(mPlayer);
        } else {
            virtualVideo.addScene(mScene);
            addMusic(virtualVideo);
        }
    }

    private void addMusic(VirtualVideo virtualVideo) {
        if (null != mAudioFragment) {
            // 正在配音界面，处理试听
            for (Music mo : mAudioFragment.getMusicObjects()) {
                try {
                    virtualVideo.addMusic(mo);
                } catch (InvalidArgumentException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void build() {
        SysAlertDialog.showLoadingDialog(this, R.string.loading);
        pauseVideo();
        mVirtualVideo.reset();
        reload(mVirtualVideo, false);
        try {
            mVirtualVideo.build(mPlayer);
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
    }

    private void onComplete() {
        onSeekTo(0);
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
        TempVideoParams.getInstance().recycle();
        super.onDestroy();

        if (null != mSaEditorPostionListener) {
            mSaEditorPostionListener.clear();
            mSaEditorPostionListener = null;
        }
    }


    private void playVideo() {
        mPlayer.start();

    }

    private void pauseVideo() {
        if (mPlayer.isPlaying()) {
            mPlayer.pause();
        }
    }


    /**
     * 导出
     */
    private void onExport() {
        stop();
        com.rd.veuisdk.ExportHandler exportHandler = new com.rd.veuisdk.ExportHandler(this, new com.rd.veuisdk.ExportHandler.IExport() {
            @Override
            public void addData(VirtualVideo virtualVideo) {
                reload(virtualVideo, false);
            }
        });
        exportHandler.onExport(
                mPlayer.getVideoWidth() / (float) mPlayer.getVideoHeight(), true);
    }

    @Override
    public VirtualVideoView getEditor() {
        return mPlayer;
    }

    @Override
    public FrameLayout getSubEditorParent() {
        return null;
    }

    private VirtualVideo mSnapshotEditor;

    private void getSnapshotEditorImp() {
        mSnapshotEditor = new VirtualVideo();
        mSnapshotEditor.addScene(mScene);
    }


    @Override
    public VirtualVideo getSnapshotEditor() {
        //截图与预览的虚拟视频需分开
        if (mSnapshotEditor == null) {
            getSnapshotEditorImp();
        }
        return mSnapshotEditor;
    }

    @Override
    public void cancelLoading() {

    }

    @Override
    public void stop() {
        mPlayer.stop();
    }

    @Override
    public void changeAnimation(int animation) {

    }

    @Override
    public void setAETemplateInfo(AETemplateInfo aeTemplateInfo) {

    }

    @Override
    public void registerEditorPostionListener(EditorPreivewPositionListener listener) {
        if (null != listener && null != mSaEditorPostionListener) {
            mSaEditorPostionListener.append(listener.hashCode(), listener);
        }
    }

    @Override
    public void unregisterEditorProgressListener(EditorPreivewPositionListener listener) {
        if (null != listener && null != mSaEditorPostionListener) {
            mSaEditorPostionListener.remove(listener.hashCode());
        }
    }

    @Override
    public boolean isMediaMute() {
        return false;
    }

    @Override
    public void reload(boolean onlyMusic) {
        if (onlyMusic) {
            reload(mVirtualVideo, onlyMusic);
        } else {
            build();
        }
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
        onBackPressed();
    }

    @Override
    public void onSure() {
        onExport();
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
        return MiscUtils.s2ms(Math.max(1, mPlayer.getDuration()));
    }

    @Override
    public int getCurrentPosition() {
        return MiscUtils.s2ms(mPlayer.getCurrentPosition());
    }
}
