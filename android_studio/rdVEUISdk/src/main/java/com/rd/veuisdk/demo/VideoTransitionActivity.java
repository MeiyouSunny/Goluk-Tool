package com.rd.veuisdk.demo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.rd.lib.ui.PreviewFrameLayout;
import com.rd.vecore.VirtualVideo;
import com.rd.vecore.VirtualVideoView;
import com.rd.vecore.exception.InvalidStateException;
import com.rd.vecore.models.MediaObject;
import com.rd.vecore.models.Scene;
import com.rd.vecore.models.Transition;
import com.rd.vecore.models.TransitionType;
import com.rd.vecore.utils.MiscUtils;
import com.rd.veuisdk.BaseActivity;
import com.rd.veuisdk.IEditPreviewHandler;
import com.rd.veuisdk.IPlayer;
import com.rd.veuisdk.R;
import com.rd.veuisdk.SdkEntry;
import com.rd.veuisdk.demo.fragment.VideoTransitionFragment;
import com.rd.veuisdk.fragment.BaseFragment;
import com.rd.veuisdk.fragment.TransitionFragment;
import com.rd.veuisdk.manager.UIConfiguration;
import com.rd.veuisdk.model.ExtPicInfo;
import com.rd.veuisdk.utils.IntentConstants;
import com.rd.veuisdk.utils.SysAlertDialog;

import java.util.ArrayList;

/**
 * 视频转场
 */
public class VideoTransitionActivity extends BaseActivity implements IEditPreviewHandler, VideoTransitionFragment.IData, IPlayer, VideoTransitionFragment.IVideoTransition {

    private PreviewFrameLayout mPflVideoPreview;
    private TextView mTvVideoCurrentPos;
    private TextView mTvVideoDuration;
    private SeekBar mSbPlayControl;
    private ImageView mIvVideoPlayState;
    private VirtualVideoView mPlayer;
    private int mLastPlayPostion;
    private ArrayList<Scene> mSceneList;
    private TransitionFragment mFragment;
    private UIConfiguration mUIConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_transcoding);
        mSceneList = getIntent().getParcelableArrayListExtra(IntentConstants.INTENT_EXTRA_SCENE);
        if (null == mSceneList || mSceneList.size() <= 1) {
            onToast(R.string.trans_need_two_medias);
            finish();
            return;
        }
        mUIConfig = SdkEntry.getSdkService().getUIConfig();
        initView();
        initPlayer();
        mVirtualVideo = new VirtualVideo();
        build();
    }

    private ArrayList<Transition> mDefaultTransitionList;

    private void initView() {
        $(R.id.titlebar_layout).setVisibility(View.VISIBLE);

        initFragment();

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
        setText(R.id.tvTitle, R.string.transition);
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
        mPflVideoPreview.setOnClickListener(mPlayStateListener);
        $(R.id.ivPlayerState).setOnClickListener(mPlayStateListener);
        mSbPlayControl.setOnSeekBarChangeListener(mOnSeekbarListener);
    }

    private VideoTransitionFragment mVideoTransitionFragment;

    private void initFragment() {
        if (null == mVideoTransitionFragment) {
            mVideoTransitionFragment = VideoTransitionFragment.newInstance();
        }
        changeFragment(mVideoTransitionFragment);

    }

    @Override
    public void onTransition(int index) {
        mAddItemIndex = index;
        if (mFragment == null) {
            mFragment = new TransitionFragment();
            mFragment.setUrl(mUIConfig.mResTypeUrl, mUIConfig.transitionUrl);
        }
        if (mDefaultTransitionList == null) {
            mDefaultTransitionList = new ArrayList<>();
        }
        mDefaultTransitionList.clear();
        for (Scene scene : mSceneList) {
            mDefaultTransitionList.add(scene.getTransition());
        }
        mFragment.setSceneCount(mSceneList.size());
        mFragment.setCurTransition(mSceneList.get(index).getTransition());
        changeFragment(mFragment);
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
        int len = mSceneList.size();
        for (int i = 0; i < len; i++) {
            virtualVideo.addScene(mSceneList.get(i));
        }
    }

    @Override
    public void build() {
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
    public void onBackPressed() {

        if (mCurrentFragment instanceof TransitionFragment) {
            changeFragment(mVideoTransitionFragment);
        } else {
            showCancelEditDialog();
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
        exportHandler.onExport(mPlayer.getVideoWidth() / (float) mPlayer.getVideoHeight(), true);
    }

    private int mAddItemIndex = 0;

    @Override
    public void onTransitionDurationChanged(float duration, boolean isApplyToAll) {
        if (isApplyToAll) {
            for (Scene scene : mSceneList) {
                if (scene.getTransition() != null) {
                    scene.getTransition().setDuration(duration);
                }
            }
        } else {
            Transition transition = mSceneList.get(mAddItemIndex).getTransition();
            if (transition != null) {
                transition.setDuration(duration);
            }
        }
        build();

    }

    @Override
    public void onTransitionChanged(ArrayList<Transition> listTransition, boolean isApplyToAll) {
        if (isApplyToAll) {
            int len = Math.min(listTransition.size(), mSceneList.size());
            for (int nTemp = 0; nTemp < len; nTemp++) {
                mSceneList.get(nTemp).setTransition(listTransition.get(nTemp));
            }
        } else {
            mSceneList.get(mAddItemIndex).setTransition(listTransition.get(0));
        }
        build();
        if (isApplyToAll) {
            seekToPosition(0, true);
        } else {
            seekToPosition(mAddItemIndex, true);
        }
    }

    private final float MIN_TRANSITION_LIMIT = 0.5f;

    private boolean checkMediaDuration(int addIndex) {
        if (addIndex < 0 || addIndex >= (mSceneList.size() - 1)) {
            return false;
        }
        Scene sceneFront = mSceneList.get(addIndex);
        Scene sceneBelow = mSceneList.get(addIndex + 1);
        if (sceneFront.getDuration() < MIN_TRANSITION_LIMIT || sceneBelow.getDuration() < MIN_TRANSITION_LIMIT) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * 选中当前位置的开始时刻
     *
     * @param position
     * @param isAddItem
     */
    private void seekToPosition(int position, boolean isAddItem) {
        float progress = 0;
        if (isAddItem) {
            position += 1;
        }
        for (int n = 0; n < position; n++) {
            Scene scene = mSceneList.get(n);
            Transition transition = scene.getTransition();
            progress += scene.getDuration();
            if (transition != null) {
                if (checkMediaDuration(n + 1)) {
                    if (transition.getType() != TransitionType.TRANSITION_NULL &&
                            transition.getType() != TransitionType.TRANSITION_BLINK_BLACK &&
                            transition.getType() != TransitionType.TRANSITION_BLINK_WHITE) {
                        progress -= transition.getDuration();
                        if (!isAddItem) {
                            if (n == position - 1) {
                                progress += transition.getDuration();
                            }
                        }
                    } else {
                        if (n == position - 1) {
                            if (isAddItem) {
                                progress -= transition.getDuration();
                            } else {
                                progress += transition.getDuration();
                            }
                        }
                    }
                }
            }
        }
        if (isAddItem) {
            progress -= 0.1f;
        } else {
            progress += 0.1f;
        }
        if (progress < 0) {
            progress = 0;
        }
        if (position == 0) {
            //特别处理，第0个media，开始位置强制为0
            progress = 0;
        }
        playBackSeekTo(progress);
    }

    private void playBackSeekTo(float progress) {
        seekTo(MiscUtils.s2ms(progress));
    }

    @Override
    public void onBack() {
        for (int n = 0; n < mSceneList.size(); n++) {
            Scene scene = mSceneList.get(n);
            scene.setTransition(mDefaultTransitionList.get(n));
        }
        onBackPressed();
        build();
    }

    @Override
    public void onSure() {
        changeFragment(mVideoTransitionFragment);
    }

    private BaseFragment mCurrentFragment;

    private void changeFragment(BaseFragment fragment) {
        changeFragment(R.id.fragmentParent, fragment);
        mCurrentFragment = fragment;
        if (mCurrentFragment instanceof TransitionFragment) {
            $(R.id.titlebar_layout).setVisibility(View.GONE);
        } else {
            $(R.id.titlebar_layout).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public ArrayList<Scene> getSceneList() {
        return mSceneList;
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == VideoTransitionFragment.REQUESTCODE_FOR_APPEND) {
                ArrayList<MediaObject> tempMedias = data
                        .getParcelableArrayListExtra(IntentConstants.EXTRA_MEDIA_LIST);
                int isTxtPic = data.getIntExtra(
                        IntentConstants.EXTRA_EXT_ISEXTPIC, 0);
                int len = tempMedias.size();

                mVideoTransitionFragment.onResetUI();
                int mIndex = mVideoTransitionFragment.getCurrentIndex();
                ExtPicInfo extPicInfo = (ExtPicInfo) data.getParcelableExtra(IntentConstants.EXTRA_EXT_PIC_INFO);
                for (int i = 0; i < len; i++) {
                    MediaObject mo = tempMedias.get(i);
                    int addPosition;
                    addPosition = mIndex + i + 1;
                    Scene scene = VirtualVideo.createScene();
                    scene.addMedia(mo);
                    mSceneList.add(addPosition, scene);
                    mVideoTransitionFragment.addItem(mo, addPosition, scene, isTxtPic, extPicInfo);
                }
                build();
                mVideoTransitionFragment.onCheck(mIndex);
            }
        }
    }
}
