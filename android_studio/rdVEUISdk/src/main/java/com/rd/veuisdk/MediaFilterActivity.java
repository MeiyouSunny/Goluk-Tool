package com.rd.veuisdk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.rd.lib.ui.PreviewFrameLayout;
import com.rd.vecore.VirtualVideo;
import com.rd.vecore.VirtualVideoView;
import com.rd.vecore.exception.InvalidArgumentException;
import com.rd.vecore.exception.InvalidStateException;
import com.rd.vecore.models.MediaObject;
import com.rd.vecore.models.Scene;
import com.rd.vecore.models.VisualFilterConfig;
import com.rd.veuisdk.ae.model.AETemplateInfo;
import com.rd.veuisdk.fragment.FilterFragment;
import com.rd.veuisdk.fragment.FilterFragmentLookup;
import com.rd.veuisdk.fragment.FilterFragmentLookupBase;
import com.rd.veuisdk.fragment.FilterFragmentLookupLocal;
import com.rd.veuisdk.fragment.helper.IFilterHandler;
import com.rd.veuisdk.manager.UIConfiguration;
import com.rd.veuisdk.model.VideoOb;
import com.rd.veuisdk.ui.RdSeekBar;
import com.rd.veuisdk.utils.DateTimeUtils;
import com.rd.veuisdk.utils.IMediaParamImp;
import com.rd.veuisdk.utils.IntentConstants;
import com.rd.veuisdk.utils.SysAlertDialog;
import com.rd.veuisdk.utils.Utils;
import com.rd.veuisdk.utils.ViewUtils;

/**
 * 片段编辑->滤镜
 */
public class MediaFilterActivity extends BaseActivity implements IFilterHandler, IVideoEditorHandler {

    /**
     * @param context
     * @param scene
     * @param requestCode
     */
    static void onMediaFilter(Context context, Scene scene, int requestCode) {
        Intent intent = new Intent(context, MediaFilterActivity.class);
        intent.putExtra(IntentConstants.INTENT_EXTRA_SCENE, scene);
        ((Activity) context).startActivityForResult(intent, requestCode);
    }

    private PreviewFrameLayout mPflVideoPreview;
    private TextView mTvVideoDuration;
    private ImageView mIvVideoPlayState;
    private VirtualVideoView mMediaPlayer;
    private RdSeekBar mPbPreview;
    private int mLastPlayPostion;
    private Scene mScene;
    private boolean mIsAutoRepeat = true;
    private FilterFragmentLookupBase mLookup;
    private VirtualVideo virtualVideo;
    private IMediaParamImp mMediaParamImp;
    private FilterFragment mFilterFragment;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_filter);
        TAG = "MediaFilterActivity";
        mStrActivityPageName = getString(R.string.filter);
        virtualVideo = new VirtualVideo();
        mScene = getIntent().getParcelableExtra(IntentConstants.INTENT_EXTRA_SCENE);
        Object tmp = mScene.getAllMedia().get(0).getTag();
        if (tmp instanceof VideoOb) {
            IMediaParamImp tmpImp = ((VideoOb) tmp).getMediaParamImp();
            if (null != tmpImp) {
                mMediaParamImp = tmpImp.clone();
            } else {
                mMediaParamImp = new IMediaParamImp();
            }
        } else {
            mMediaParamImp = new IMediaParamImp();
        }

        initView();
        onLoad();
        playVideo();

        UIConfiguration mUIConfig = SdkEntry.getSdkService().getUIConfig();

        if (!TextUtils.isEmpty(mUIConfig.filterUrl)) {
            //网络lookup滤镜
            mLookup = FilterFragmentLookup.newInstance(mUIConfig.filterUrl);
            mLookup.setShowApplyAll(true);
            mLookup.setIMediaParam(mMediaParamImp);
            changeFragment(mLookup);
        } else if (mUIConfig.filterLayoutTpye == UIConfiguration.FILTER_LAYOUT_3) {
            //本地lookup
            mLookup = FilterFragmentLookupLocal.newInstance();
            mLookup.setShowApplyAll(true);
            mLookup.setIMediaParam(mMediaParamImp);
            changeFragment(mLookup);
        } else {
            mFilterFragment = FilterFragment.newInstance();
            if (mUIConfig.filterLayoutTpye == UIConfiguration.FILTER_LAYOUT_2) {
                //jlk滤镜(acv 单行)
                mFilterFragment.setJLKStyle(true);
            } else {
                // 分组滤镜 （acv）
                mFilterFragment.setJLKStyle(false);
            }
            mFilterFragment.setMediaParam(mMediaParamImp);
            changeFragment(mFilterFragment);
        }


    }

    private void changeFragment(Fragment fragment) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fl_fragment_container, fragment);
        ft.commit();
    }

    /**
     * 加载媒体资源
     */
    private void onLoad() {
        mPbPreview.setHighLights(null);
        virtualVideo.reset();
        try {
            virtualVideo.addScene(mScene);
            virtualVideo.build(mMediaPlayer);
        } catch (InvalidStateException e) {
            e.printStackTrace();
        }
    }

    private View.OnClickListener mOnPlayerClickListener = new View.OnClickListener() {

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
            mTvVideoDuration.setText(DateTimeUtils.stringForMillisecondTime(
                    ms, true, true));
            updatePreviewFrameAspect(mMediaPlayer.getVideoWidth(), mMediaPlayer.getVideoHeight());
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
        mIvVideoPlayState = (ImageView) findViewById(R.id.ivPlayerState);
        mMediaPlayer = (VirtualVideoView) findViewById(R.id.epvPreview);

//        PreviewFrameLayout layout = (PreviewFrameLayout) findViewById(R.id.rlPreviewLayout);
//        layout.setAspectRatio(AppConfiguration.EXT_ASPECTRATIO);

//        TextView title = (TextView) findViewById(R.id.tvBottomTitle);
//        title.setText(mStrActivityPageName);
        mPflVideoPreview.setClickable(true);
        mLastPlayPostion = -1;

        mMediaPlayer = (VirtualVideoView) findViewById(R.id.epvPreview);
        mMediaPlayer.setClearFirst(true);
        mMediaPlayer.setAutoRepeat(mIsAutoRepeat);
        mMediaPlayer.setOnClickListener(mOnPlayerClickListener);
        mMediaPlayer.setOnPlaybackListener(mPlayerListener);
        mMediaPlayer.setOnInfoListener(new VirtualVideo.OnInfoListener() {
            @Override
            public boolean onInfo(int what, int extra, Object obj) {

                if (what == VirtualVideo.INFO_WHAT_PLAYBACK_PREPARING) {
                } else if (what == VirtualVideo.INFO_WHAT_GET_VIDEO_HIGHTLIGHTS) {
                    int[] ls = (int[]) obj;
                    mPbPreview.setHighLights(ls);
                }
                return false;
            }
        });
        mPbPreview = (RdSeekBar) findViewById(R.id.pbPreview);
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
        } else if (id == R.id.public_menu_cancel) {
            setResult(RESULT_CANCELED);
            onBackPressed();
        } else if (id == R.id.public_menu_sure) {
            onMenuSure();
        }
    }

    /**
     * 媒体保存滤镜
     */
    private void onMenuSure() {
        boolean applyToAll = false;
        mMediaPlayer.stop();
        if (null != mLookup) {
            //本地、网络lookup
            //应用到全部片段
            applyToAll = mLookup.onSure();
        } else if (null != mFilterFragment) {
            mMediaParamImp.setFilterIndex(mFilterFragment.getMenuIndex());
            mMediaParamImp.setCurrentFilterType(mFilterFragment.getFilterId());
            mMediaParamImp.setLookupConfig(null);
        }
        Intent data = new Intent();

        MediaObject src = mScene.getAllMedia().get(0);

        Object object = src.getTag();
        VideoOb tmp = null;
        if (object instanceof VideoOb) {
            tmp = (VideoOb) object;
        } else {
            tmp = new VideoOb(src);
        }
        tmp.setMediaParamImp(mMediaParamImp);


        MediaObject mMedia = src.clone();
        mMedia.setTag(tmp);
        try {
            mMedia.changeFilterList(Utils.getFilterList(mMediaParamImp));
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        }
        //必须创建一个新的对象
        Scene scene = VirtualVideo.createScene();
        scene.addMedia(mMedia);
        scene.setTransition(mScene.getTransition());
        data.putExtra(IntentConstants.INTENT_EXTRA_SCENE, scene);
        data.putExtra(IntentConstants.INTENT_ALL_APPLY, applyToAll);
        setResult(RESULT_OK, data);
        onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (null != mMediaPlayer) {
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
        if (null != virtualVideo) {
            virtualVideo.release();
        }
        super.onDestroy();
        if (null != mLookup) {
            mLookup.recycle();
            mLookup = null;
        }
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
        return Utils.s2ms(mMediaPlayer.getDuration());
    }

    @Override
    public int getCurrentPosition() {
        return Utils.s2ms(mMediaPlayer.getCurrentPosition());
    }


    @Override
    public boolean isPlaying() {
        return mMediaPlayer != null && mMediaPlayer.isPlaying();
    }


    @Override
    public void changeFilterType(int index, int nFilterType) {
        if (mMediaPlayer != null) {
            if (!mMediaPlayer.isPlaying()) {
                start();
            }
            mMediaParamImp.setCurrentFilterType(nFilterType);
            try {
                mScene.getAllMedia().get(0).changeFilterList(Utils.getFilterList(mMediaParamImp));
            } catch (InvalidArgumentException e) {
                e.printStackTrace();
            }
        }

    }


    @Override
    public void changeFilterLookup(VisualFilterConfig lookup, int index) {
        if (mMediaPlayer != null) {
            if (!mMediaPlayer.isPlaying()) {
                start();
            }
            mMediaParamImp.setLookupConfig(lookup);
            try {
                mScene.getAllMedia().get(0).changeFilterList(Utils.getFilterList(mMediaParamImp));
            } catch (InvalidArgumentException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public int getCurrentLookupIndex() {
        return mMediaParamImp.getFilterIndex();
    }

    @Override
    public VirtualVideoView getEditor() {
        return null;
    }

    @Override
    public VirtualVideo getEditorVideo() {
        return null;
    }

    @Override
    public FrameLayout getSubEditorParent() {
        return null;
    }

    @Override
    public VirtualVideo getSnapshotEditor() {
        return null;
    }


    @Override
    public void reload(boolean bOnlyAudio) {

    }

    @Override
    public void cancelLoading() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void changeAnimation(int animation) {

    }

    @Override
    public void setAETemplateInfo(AETemplateInfo aeTemplateInfo) {

    }

    @Override
    public void registerEditorPostionListener(EditorPreivewPositionListener listener) {

    }

    @Override
    public void unregisterEditorProgressListener(EditorPreivewPositionListener listener) {

    }

    @Override
    public boolean isMediaMute() {
        return false;
    }

    @Override
    public void removeMvMusic(boolean remove) {

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
    public void onBack() {
        setResult(RESULT_CANCELED);
        onBackPressed();
    }

    @Override
    public void onSure() {
        onMenuSure();
    }
}
