package com.rd.veuisdk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.rd.lib.ui.PreviewFrameLayout;
import com.rd.vecore.VirtualVideo;
import com.rd.vecore.VirtualVideoView;
import com.rd.vecore.exception.InvalidStateException;
import com.rd.vecore.models.EffectInfo;
import com.rd.vecore.models.MediaObject;
import com.rd.vecore.models.MediaType;
import com.rd.vecore.models.Scene;
import com.rd.veuisdk.fragment.EffectFragment;
import com.rd.veuisdk.manager.UIConfiguration;
import com.rd.veuisdk.ui.RdSeekBar;
import com.rd.veuisdk.utils.DateTimeUtils;
import com.rd.veuisdk.utils.IntentConstants;
import com.rd.veuisdk.utils.SysAlertDialog;
import com.rd.veuisdk.utils.Utils;

import java.util.ArrayList;

/**
 * 片段编辑->特效
 */
public class MediaEffectActivity extends BaseActivity implements IPlayer, EffectFragment.IEffectHandler {

    /**
     * 特效
     */
    static void onMediaEffect(Context context, MediaObject mediaObject, int requestCode) {
        Intent intent = new Intent(context, MediaEffectActivity.class);
        intent.putExtra(IntentConstants.INTENT_MEDIA_OBJECT, mediaObject);
        ((Activity) context).startActivityForResult(intent, requestCode);
    }

    private PreviewFrameLayout mPflVideoPreview;
    private TextView mTvVideoDuration;
    private VirtualVideoView mMediaPlayer;
    private RdSeekBar mPbPreview;
    private int mLastPlayPostion;
    private MediaObject mMedia;
    private boolean mIsAutoRepeat = true;
    private VirtualVideo mVirtualVideo;
    private EffectFragment mEffectFragment;
    private int mDuration = 0;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_filter);
        findViewById(R.id.bottomLayout).setVisibility(View.GONE);
        TAG = "MediaEffectActivity";
        mStrActivityPageName = getString(R.string.effect);
        mVirtualVideo = new VirtualVideo();
        mMedia = getIntent().getParcelableExtra(IntentConstants.INTENT_MEDIA_OBJECT);
        mDuration = Utils.s2ms(mMedia.getDuration());
        ArrayList<EffectInfo> tmp = mMedia.getEffectInfos();
        mEffectInfos = new ArrayList<>();
        if (null != tmp) {
            //当前媒体之前编辑的特效
            mEffectInfos.addAll(tmp);
        }
        initView();
        reload();
        UIConfiguration configuration = SdkEntry.getSdkService().getUIConfig();
        String typeUrl = configuration.mResTypeUrl;
        String url = configuration.getEffectUrl();
        mEffectFragment = EffectFragment.newInstance(typeUrl, url);
        changeFragment(mEffectFragment);
    }

    private void changeFragment(Fragment fragment) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fl_fragment_container, fragment);
        ft.commit();
    }

    /**
     * 加载媒体资源
     */
    private void reload() {
        mPbPreview.setHighLights(null);
        mMediaPlayer.reset();
        mVirtualVideo.reset();
        try {
            Scene scene = VirtualVideo.createScene();
            scene.addMedia(mMedia);
            mVirtualVideo.addScene(scene);
            mVirtualVideo.build(mMediaPlayer);
        } catch (InvalidStateException e) {
            e.printStackTrace();
        }
    }

    private VirtualVideoView.VideoViewListener mPlayerListener = new VirtualVideoView.VideoViewListener() {
        private float lastPosition;

        @Override
        public void onPlayerPrepared() {
            SysAlertDialog.cancelLoadingDialog();
            lastPosition = -1;
            mDuration = Utils.s2ms(mMediaPlayer.getDuration());
            mPbPreview.setMax(mDuration);
            mTvVideoDuration.setText(DateTimeUtils.stringForMillisecondTime(
                    mDuration, true, true));
            updatePreviewFrameAspect(mMediaPlayer.getVideoWidth(), mMediaPlayer.getVideoHeight());
            if (null != mEffectFragment) {
                mEffectFragment.initThumbnail();
            }

        }

        @Override
        public boolean onPlayerError(int what, int extra) {
            onToast(getString(R.string.preview_error));
            onBackPressed();
            return false;
        }

        @Override
        public void onPlayerCompletion() {
            onComplete();
        }

        @Override
        public void onGetCurrentPosition(float position) {
            if (null != mEffectFragment) {
                mEffectFragment.setPosition(position);
            }
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
        mPflVideoPreview = (PreviewFrameLayout) findViewById(R.id.rlPreview);
        mTvVideoDuration = (TextView) findViewById(R.id.tvEditorDuration);
        findViewById(R.id.ivPlayerState).setVisibility(View.GONE);
        mMediaPlayer = (VirtualVideoView) findViewById(R.id.epvPreview);

        mPflVideoPreview.setClickable(true);
        mLastPlayPostion = -1;

        mMediaPlayer = (VirtualVideoView) findViewById(R.id.epvPreview);
        mMediaPlayer.setClearFirst(true);
        mMediaPlayer.setAutoRepeat(mIsAutoRepeat);
        mMediaPlayer.setOnPlaybackListener(mPlayerListener);
        mPbPreview = (RdSeekBar) findViewById(R.id.pbPreview);
        mPbPreview.setVisibility(View.GONE);
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

    private void onComplete() {
        if (null != mEffectFragment) {
            mEffectFragment.onComplete();
        }
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

    @Override
    protected void onResume() {
        super.onResume();
        // 从后台切换到前台，并且已经成功打开正常的视频，进行继续播放
        if (mLastPlayPostion == 0) {
            mMediaPlayer.seekTo(0);
        }
        if (mLastPlayPostion > 0) {
            if (null != mMediaPlayer) {
                mMediaPlayer.seekTo(mLastPlayPostion);
                mLastPlayPostion = -1;
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
            mMediaPlayer.cleanUp();
            mMediaPlayer = null;
        }
        if (null != mVirtualVideo) {
            mVirtualVideo.release();
            mVirtualVideo = null;
        }
        if (null != mSnapshotEditor) {
            mSnapshotEditor.release();
            mSnapshotEditor = null;
        }
        super.onDestroy();
    }


    private void playVideo() {
        mMediaPlayer.start();
    }

    private void pauseVideo() {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        }
    }


    @Override
    public void start() {
        if (mMediaPlayer == null) {
            return;
        }
        playVideo();
    }

    @Override
    public void pause() {

        pauseVideo();
    }

    @Override
    public void seekTo(int msec) {
        mMediaPlayer.seekTo(Utils.ms2s(msec));
    }

    @Override
    public int getDuration() {
        return mDuration;
    }

    @Override
    public int getCurrentPosition() {
        return Utils.s2ms(mMediaPlayer.getCurrentPosition());
    }

    @Override
    public boolean isPlaying() {
        return mMediaPlayer != null && mMediaPlayer.isPlaying();
    }

    private ArrayList<EffectInfo> mEffectInfos;

    @Override
    public VirtualVideoView getPlayer() {
        return mMediaPlayer;
    }

    @Override
    public ArrayList<EffectInfo> getEffectInfos() {
        if (null == mEffectInfos) {
            mEffectInfos = new ArrayList<>();
        }
        return mEffectInfos;
    }

    private VirtualVideo mSnapshotEditor;

    @Override
    public VirtualVideo getSnapVideo() {
        if (null == mSnapshotEditor) {
            mSnapshotEditor = new VirtualVideo();
            Scene scene = VirtualVideo.createScene();
            scene.addMedia(mMedia.clone());
            mSnapshotEditor.addScene(scene);
        }
        return mSnapshotEditor;
    }

    /**
     * 实时更新特效（主要是滤镜特效）
     */
    @Override
    public void updateEffects(ArrayList<EffectInfo> list) {
        boolean lastIsPlaying = isPlaying();
        if (lastIsPlaying) {
            pause();
        }
        mEffectInfos = list;
        mMedia.setEffectInfos(mEffectInfos);
        //实时更新当前媒体的滤镜特效
        mVirtualVideo.updateEffects(mMediaPlayer, mMedia);
        if (lastIsPlaying) {
            playVideo();
        }


    }

    @Override
    public void updateEffectsReload(ArrayList<EffectInfo> list, int seekto) {
        if (isPlaying()) {
            pause();
        }
        mEffectInfos = list;
        //带有时间特效的必须build才能生效
        mMedia.setEffectInfos(mEffectInfos);
        reload();
        seekTo(seekto);

    }

    @Override
    public void onEffectBackToMain() {
        onBackPressed();
    }

    @Override
    public void onEffectSure(ArrayList<EffectInfo> list) {
        mEffectInfos = list;
        mMedia.setEffectInfos(list);
        Intent data = new Intent();
        data.putExtra(IntentConstants.INTENT_MEDIA_OBJECT, mMedia);
        setResult(RESULT_OK, data);
        finish();
    }

    /**
     * 只有一个视频时才能操作时间特效
     *
     * @return
     */
    @Override
    public MediaObject getReverseMediaObjcet() {
        if (mMedia.getMediaType() == MediaType.MEDIA_VIDEO_TYPE) {
            return mMedia;
        }
        return null;
    }

    @Override
    public boolean enableMultiEffect() {
        return false;
    }
}
