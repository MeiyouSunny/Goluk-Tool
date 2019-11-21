package com.rd.veuisdk.ui.exoplayer;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.rd.exoplayer2.C;
import com.rd.exoplayer2.DefaultLoadControl;
import com.rd.exoplayer2.ExoPlaybackException;
import com.rd.exoplayer2.ExoPlayer;
import com.rd.exoplayer2.ExoPlayerFactory;
import com.rd.exoplayer2.LoadControl;
import com.rd.exoplayer2.SimpleExoPlayer;
import com.rd.exoplayer2.Timeline;
import com.rd.exoplayer2.extractor.DefaultExtractorsFactory;
import com.rd.exoplayer2.extractor.ExtractorsFactory;
import com.rd.exoplayer2.metadata.Metadata;
import com.rd.exoplayer2.metadata.id3.ApicFrame;
import com.rd.exoplayer2.source.ExtractorMediaSource;
import com.rd.exoplayer2.source.MediaSource;
import com.rd.exoplayer2.source.TrackGroupArray;
import com.rd.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.rd.exoplayer2.trackselection.DefaultTrackSelector;
import com.rd.exoplayer2.trackselection.TrackSelection;
import com.rd.exoplayer2.trackselection.TrackSelectionArray;
import com.rd.exoplayer2.trackselection.TrackSelector;
import com.rd.exoplayer2.upstream.BandwidthMeter;
import com.rd.exoplayer2.upstream.DataSource;
import com.rd.exoplayer2.upstream.DefaultBandwidthMeter;
import com.rd.exoplayer2.upstream.DefaultDataSourceFactory;
import com.rd.exoplayer2.util.Assertions;
import com.rd.exoplayer2.util.Util;
import com.rd.veuisdk.R;
import com.rd.veuisdk.ui.exoplayer.AspectRatioFrameLayout.ResizeMode;

/**
 * @author JIAN
 * @create 2018/11/23
 * @Describe
 */
@TargetApi(16)
public class RdExoPlayerView extends FrameLayout {

    private static final int SURFACE_TYPE_NONE = 0;
    private static final int SURFACE_TYPE_SURFACE_VIEW = 1;
    private static final int SURFACE_TYPE_TEXTURE_VIEW = 2;

    private final AspectRatioFrameLayout contentFrame;
    private final View surfaceView;
    private final RdPlaybackControlView controller;
    private final ComponentListener componentListener;

    private SimpleExoPlayer player;
    private boolean useController;
    private boolean useArtwork;
    private Bitmap defaultArtwork;
    private int controllerShowTimeoutMs;

    public RdExoPlayerView(Context context) {
        this(context, null);
    }

    public RdExoPlayerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RdExoPlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        int playerLayoutId = R.layout.rd_exo_simple_player_view;

        boolean useController = true;
        int surfaceType = SURFACE_TYPE_SURFACE_VIEW;
        int resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT;
        int controllerShowTimeoutMs = RdPlaybackControlView.DEFAULT_SHOW_TIMEOUT_MS;


        LayoutInflater.from(context).inflate(playerLayoutId, this);
        componentListener = new ComponentListener();
        setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);

        // Content frame.
        contentFrame = (AspectRatioFrameLayout) findViewById(R.id.rd_exo_content_frame);
        if (contentFrame != null) {
            setResizeModeRaw(contentFrame, resizeMode);
        }


        // Create a surface view and insert it into the content frame, if there is one.
        if (contentFrame != null && surfaceType != SURFACE_TYPE_NONE) {
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            surfaceView = surfaceType == SURFACE_TYPE_TEXTURE_VIEW ? new TextureView(context)
                    : new SurfaceView(context);
            surfaceView.setLayoutParams(params);
            contentFrame.addView(surfaceView, 0);
        } else {
            surfaceView = null;
        }


        // Playback control view.
//        View controllerPlaceholder = findViewById(R.id.rd_exo_simple_playback_control_layout);
//        if (controllerPlaceholder != null) {
        // Note: rewindMs and fastForwardMs are passed via attrs, so we don't need to make explicit
        // calls to set them.
        this.controller = new RdPlaybackControlView(context, attrs);
//            controller.setLayoutParams(controllerPlaceholder.getLayoutParams());
//            ViewGroup parent = ((ViewGroup) controllerPlaceholder.getParent());
//            int controllerIndex = parent.indexOfChild(controllerPlaceholder);
//            parent.removeView(controllerPlaceholder);
//            parent.addView(controller, controllerIndex);
        addView(controller);
//        } else {
//            this.controller = null;
//        }
        this.controllerShowTimeoutMs = controller != null ? controllerShowTimeoutMs : 0;
        this.useController = useController && controller != null;
        hideController();
    }

    /**
     * Returns the player currently set on this view, or null if no player is set.
     */
    public SimpleExoPlayer getPlayer() {
        return player;
    }


    private void setPlayer(SimpleExoPlayer player) {
        if (this.player == player) {
            return;
        }
        if (this.player != null) {
            this.player.setVideoListener(null);
            this.player.removeListener(componentListener);
            this.player.setVideoSurface(null);
        }
        this.player = player;
        if (useController) {
            controller.setPlayer(player);
        }

        if (player != null) {
            if (surfaceView instanceof TextureView) {
                player.setVideoTextureView((TextureView) surfaceView);
            } else if (surfaceView instanceof SurfaceView) {
                player.setVideoSurfaceView((SurfaceView) surfaceView);
            }
            player.setVideoListener(componentListener);
            player.addListener(componentListener);
            maybeShowController(false);
            updateForCurrentTrackSelections();
        } else {
            hideController();
            hideArtwork();
        }
    }

    /**
     * Sets the resize mode.
     *
     * @param resizeMode The resize mode.
     */
    public void setResizeMode(@ResizeMode int resizeMode) {
        Assertions.checkState(contentFrame != null);
        contentFrame.setResizeMode(resizeMode);
    }

    /**
     * Returns whether artwork is displayed if present in the media.
     */
    public boolean getUseArtwork() {
        return useArtwork;
    }



    /**
     * Returns the default artwork to display.
     */
    public Bitmap getDefaultArtwork() {
        return defaultArtwork;
    }

    /**
     * Sets the default artwork to display if {@code useArtwork} is {@code true} and no artwork is
     * present in the media.
     *
     * @param defaultArtwork the default artwork to display.
     */
    public void setDefaultArtwork(Bitmap defaultArtwork) {
        if (this.defaultArtwork != defaultArtwork) {
            this.defaultArtwork = defaultArtwork;
            updateForCurrentTrackSelections();
        }
    }

    /**
     * Returns whether the playback controls are enabled.
     */
    public boolean getUseController() {
        return useController;
    }

    /**
     * Sets whether playback controls are enabled. If set to {@code false} the playback controls are
     * never visible and are disconnected from the player.
     *
     * @param useController Whether playback controls should be enabled.
     */
    public void setUseController(boolean useController) {
        Assertions.checkState(!useController || controller != null);
        if (this.useController == useController) {
            return;
        }
        this.useController = useController;
        if (useController) {
            controller.setPlayer(player);
        } else if (controller != null) {
            controller.hide();
            controller.setPlayer(null);
        }
    }

    /**
     * Called to process media key events. Any {@link KeyEvent} can be passed but only media key
     * events will be handled. Does nothing if playback controls are disabled.
     *
     * @param event A key event.
     * @return Whether the key event was handled.
     */
    public boolean dispatchMediaKeyEvent(KeyEvent event) {
        return useController && controller.dispatchMediaKeyEvent(event);
    }

    /**
     * Shows the playback controls. Does nothing if playback controls are disabled.
     */
    public void showController() {
        if (useController) {
            maybeShowController(true);
        }
    }

    /**
     * Hides the playback controls. Does nothing if playback controls are disabled.
     */
    public void hideController() {
        if (controller != null) {
            controller.hide();
        }
    }

    /**
     * Returns the playback controls timeout. The playback controls are automatically hidden after
     * this duration of time has elapsed without user input and with playback or buffering in
     * progress.
     *
     * @return The timeout in milliseconds. A non-positive value will cause the controller to remain
     * visible indefinitely.
     */
    public int getControllerShowTimeoutMs() {
        return controllerShowTimeoutMs;
    }

    /**
     * Sets the playback controls timeout. The playback controls are automatically hidden after this
     * duration of time has elapsed without user input and with playback or buffering in progress.
     *
     * @param controllerShowTimeoutMs The timeout in milliseconds. A non-positive value will cause
     *                                the controller to remain visible indefinitely.
     */
    public void setControllerShowTimeoutMs(int controllerShowTimeoutMs) {
        Assertions.checkState(controller != null);
        this.controllerShowTimeoutMs = controllerShowTimeoutMs;
    }


    public void setControllerVisibilityListener(RdPlaybackControlView.VisibilityListener listener) {
        Assertions.checkState(controller != null);
        controller.setVisibilityListener(listener);
    }


    public void setSeekDispatcher(RdPlaybackControlView.SeekDispatcher seekDispatcher) {
        Assertions.checkState(controller != null);
        controller.setSeekDispatcher(seekDispatcher);
    }

    /**
     * Sets the rewind increment in milliseconds.
     *
     * @param rewindMs The rewind increment in milliseconds.
     */
    public void setRewindIncrementMs(int rewindMs) {
        Assertions.checkState(controller != null);
        controller.setRewindIncrementMs(rewindMs);
    }

    /**
     * Sets the fast forward increment in milliseconds.
     *
     * @param fastForwardMs The fast forward increment in milliseconds.
     */
    public void setFastForwardIncrementMs(int fastForwardMs) {
        Assertions.checkState(controller != null);
        controller.setFastForwardIncrementMs(fastForwardMs);
    }

    /**
     * Gets the view onto which video is rendered. This is either a {@link SurfaceView} (default)
     * or a {@link TextureView} if the {@code use_texture_view} view attribute has been set to true.
     *
     * @return Either a {@link SurfaceView} or a {@link TextureView}.
     */
    public View getVideoSurfaceView() {
        return surfaceView;
    }



    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (!useController || player == null || ev.getActionMasked() != MotionEvent.ACTION_DOWN) {
            return false;
        }
        if (controller.isVisible()) {
            controller.hide();
        } else {
            maybeShowController(true);
        }
        return true;
    }

    @Override
    public boolean onTrackballEvent(MotionEvent ev) {
        if (!useController || player == null) {
            return false;
        }
        maybeShowController(true);
        return true;
    }

    private void maybeShowController(boolean isForced) {
        if (!useController || player == null) {
            return;
        }
        int playbackState = player.getPlaybackState();
        boolean showIndefinitely = playbackState == ExoPlayer.STATE_IDLE
                || playbackState == ExoPlayer.STATE_ENDED || !player.getPlayWhenReady();
        boolean wasShowingIndefinitely = controller.isVisible() && controller.getShowTimeoutMs() <= 0;
        controller.setShowTimeoutMs(showIndefinitely ? 0 : controllerShowTimeoutMs);
        if (isForced || showIndefinitely || wasShowingIndefinitely) {
            controller.show();
        }
    }

    private void updateForCurrentTrackSelections() {
        if (player == null) {
            return;
        }
        TrackSelectionArray selections = player.getCurrentTrackSelections();
        for (int i = 0; i < selections.length; i++) {
            if (player.getRendererType(i) == C.TRACK_TYPE_VIDEO && selections.get(i) != null) {
                // Video enabled so artwork must be hidden. If the shutter is closed, it will be opened in
                // onRenderedFirstFrame().
                hideArtwork();
                return;
            }
        }

        // Display artwork if enabled and available, else hide it.
        if (useArtwork) {
            for (int i = 0; i < selections.length; i++) {
                TrackSelection selection = selections.get(i);
                if (selection != null) {
                    for (int j = 0; j < selection.length(); j++) {
                        Metadata metadata = selection.getFormat(j).metadata;
                        if (metadata != null && setArtworkFromMetadata(metadata)) {
                            return;
                        }
                    }
                }
            }
            if (setArtworkFromBitmap(defaultArtwork)) {
                return;
            }
        }
        // Artwork disabled or unavailable.
        hideArtwork();
    }

    private boolean setArtworkFromMetadata(Metadata metadata) {
        for (int i = 0; i < metadata.length(); i++) {
            Metadata.Entry metadataEntry = metadata.get(i);
            if (metadataEntry instanceof ApicFrame) {
                byte[] bitmapData = ((ApicFrame) metadataEntry).pictureData;
                Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapData, 0, bitmapData.length);
                return setArtworkFromBitmap(bitmap);
            }
        }
        return false;
    }

    private boolean setArtworkFromBitmap(Bitmap bitmap) {
        if (bitmap != null) {
            int bitmapWidth = bitmap.getWidth();
            int bitmapHeight = bitmap.getHeight();
            if (bitmapWidth > 0 && bitmapHeight > 0) {
                if (contentFrame != null) {
                    contentFrame.setAspectRatio((float) bitmapWidth / bitmapHeight);
                }

                return true;
            }
        }
        return false;
    }

    private void hideArtwork() {

    }

    @SuppressWarnings("ResourceType")
    private static void setResizeModeRaw(AspectRatioFrameLayout aspectRatioFrame, int resizeMode) {
        aspectRatioFrame.setResizeMode(resizeMode);
    }

    private final class ComponentListener implements SimpleExoPlayer.VideoListener,
            ExoPlayer.EventListener {


        // SimpleExoPlayer.VideoListener implementation

        @Override
        public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees,
                                       float pixelWidthHeightRatio) {
            if (contentFrame != null) {
                float aspectRatio = height == 0 ? 1 : (width * pixelWidthHeightRatio) / height;
                contentFrame.setAspectRatio(aspectRatio);
            }
        }

        @Override
        public void onRenderedFirstFrame() {

        }

        @Override
        public void onTracksChanged(TrackGroupArray tracks, TrackSelectionArray selections) {
            updateForCurrentTrackSelections();
        }

        // ExoPlayer.EventListener implementation

        @Override
        public void onLoadingChanged(boolean isLoading) {
            // Do nothing.
        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            maybeShowController(false);
        }

        @Override
        public void onPlayerError(ExoPlaybackException e) {
            // Do nothing.
        }

        @Override
        public void onPositionDiscontinuity() {
            // Do nothing.
        }

        @Override
        public void onTimelineChanged(Timeline timeline, Object manifest) {
            // Do nothing.

        }

    }

    private long resumePosition = 0;
    private int resumeWindow = C.INDEX_UNSET;
    private String TAG = "RdExoPlayerView";

    /***
     * 创建实例播放实例，并不开始缓冲
     **/
    private void createFullPlayer() {
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory =
                new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector =
                new DefaultTrackSelector(videoTrackSelectionFactory);
        LoadControl loadControl = new DefaultLoadControl();
        setPlayer(ExoPlayerFactory.newSimpleInstance(getContext(), trackSelector, loadControl));

    }

    /***
     * 创建实例播放实例，开始缓冲
     */
    void playerNoAlertDialog() {
        if (player == null) {
            createFullPlayer();
        }


        boolean haveResumePosition = resumeWindow != C.INDEX_UNSET;
        if (handPause) {
            player.setPlayWhenReady(false);
        } else {
            player.setPlayWhenReady(true);
        }
        if (haveResumePosition) {
            player.seekTo(resumeWindow, resumePosition);
        }
        player.removeListener(componentListener);
        player.addListener(componentListener);
        player.prepare(mMediaSource, !haveResumePosition, false);
        isPause = false;
    }


    /**
     * 切回前台
     */
    public void onResume() {
        boolean is = (null == player);
        if (is) {
            startVideo();
        }

    }

    /***
     * 是否播放中
     * @return boolean boolean
     */
    public boolean isPlaying() {
        if (player == null)
            return false;
        int playbackState = player.getPlaybackState();
        return playbackState != ExoPlayer.STATE_IDLE && playbackState != ExoPlayer.STATE_ENDED
                && player.getPlayWhenReady();
    }

    /***
     * 播放视频
     **/
    private void startVideo() {
        boolean iss = isPause || isPlaying();
        if (iss) {
            playerNoAlertDialog();
        }
    }

    /****
     * 重置进度
     */
    private void updateResumePosition() {
        if (player != null) {
            resumeWindow = player.getCurrentWindowIndex();
            resumePosition = Math.max(0, player.getCurrentPosition());
        }
    }

    private boolean isPause = true;
    private boolean handPause = false;

    /**
     * 切到后台
     */
    public void onPause() {
        isPause = true;
        if (player != null) {
            handPause = !player.getPlayWhenReady();
            releasePlayers();
        }

    }

    /***
     * 释放资源
     */
    public void releasePlayers() {
        updateResumePosition();
        if (player != null) {
            player.removeListener(componentListener);
            player.stop();
            player.release();
            player = null;
        }
    }

    private MediaSource mMediaSource;

    /**
     * 准备数据并播放
     *
     * @param url
     */
    public void setUrl(String url) {
        DefaultBandwidthMeter bandwidthMeter2 = new DefaultBandwidthMeter();
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(getContext(),
                Util.getUserAgent(getContext(), getContext().getPackageName()), bandwidthMeter2);
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
        mMediaSource = new ExtractorMediaSource(Uri.parse(url),
                dataSourceFactory, extractorsFactory, null, null);
    }

    /**
     * 播放视频
     */
    public void startPlayer() {
        isPause = true;
        handPause = false;
        startVideo();
    }

}

