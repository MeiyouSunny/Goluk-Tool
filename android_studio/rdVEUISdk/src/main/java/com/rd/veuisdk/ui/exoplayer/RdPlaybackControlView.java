package com.rd.veuisdk.ui.exoplayer;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.rd.exoplayer2.C;
import com.rd.exoplayer2.ExoPlaybackException;
import com.rd.exoplayer2.ExoPlayer;
import com.rd.exoplayer2.Timeline;
import com.rd.exoplayer2.source.TrackGroupArray;
import com.rd.exoplayer2.trackselection.TrackSelectionArray;
import com.rd.exoplayer2.util.Util;
import com.rd.veuisdk.R;

import java.util.Formatter;
import java.util.Locale;

/**
 * 自定义Exoplayer播放器控制部分的UI
 *
 * @author JIAN
 * @create 2018/11/28
 * @Describe
 */
public class RdPlaybackControlView extends FrameLayout {

    /**
     * Listener to be notified about changes of the visibility of the UI control.
     */
    public interface VisibilityListener {

        /**
         * Called when the visibility changes.
         *
         * @param visibility The new visibility. Either {@link View#VISIBLE} or {@link View#GONE}.
         */
        void onVisibilityChange(int visibility);

    }

    /**
     * Dispatches seek operations to the player.
     */
    public interface SeekDispatcher {


        boolean dispatchSeek(ExoPlayer player, int windowIndex, long positionMs);

    }


    public static final SeekDispatcher DEFAULT_SEEK_DISPATCHER = new SeekDispatcher() {

        @Override
        public boolean dispatchSeek(ExoPlayer player, int windowIndex, long positionMs) {
            player.seekTo(windowIndex, positionMs);
            return true;
        }

    };

    public static final int DEFAULT_FAST_FORWARD_MS = 15000;
    public static final int DEFAULT_REWIND_MS = 5000;
    public static final int DEFAULT_SHOW_TIMEOUT_MS = 1000;

    private static final int PROGRESS_BAR_MAX = 1000;
    private static final long MAX_POSITION_FOR_SEEK_TO_PREVIOUS = 3000;

    private final ComponentListener componentListener;
    private final View playButton;
    private final View pauseButton;
    private final TextView durationView;
    private final TextView positionView;
    private final SeekBar progressBar;
    private final StringBuilder formatBuilder;
    private final Formatter formatter;
    private final Timeline.Window currentWindow;

    private ExoPlayer player;
    private SeekDispatcher seekDispatcher;
    private VisibilityListener visibilityListener;

    private boolean isAttachedToWindow;
    private boolean dragging;
    private int rewindMs;
    private int fastForwardMs;
    private int showTimeoutMs;
    private long hideAtMs;

    private final Runnable updateProgressAction = new Runnable() {
        @Override
        public void run() {
            updateProgress();
        }
    };

    private final Runnable hideAction = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    public RdPlaybackControlView(Context context) {
        this(context, null);
    }

    public RdPlaybackControlView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RdPlaybackControlView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        int controllerLayoutId = R.layout.rd_exo_simple_playback_control_layout;
        rewindMs = DEFAULT_REWIND_MS;
        fastForwardMs = DEFAULT_FAST_FORWARD_MS;
        showTimeoutMs = DEFAULT_SHOW_TIMEOUT_MS;

        currentWindow = new Timeline.Window();
        formatBuilder = new StringBuilder();
        formatter = new Formatter(formatBuilder, Locale.getDefault());
        componentListener = new ComponentListener();
        seekDispatcher = DEFAULT_SEEK_DISPATCHER;

        LayoutInflater.from(context).inflate(controllerLayoutId, this);
        setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);

        durationView = (TextView) findViewById(R.id.rd_exo_duration);
        positionView = (TextView) findViewById(R.id.rd_exo_position);
        progressBar = (SeekBar) findViewById(R.id.rd_exo_progress);
        if (progressBar != null) {
            progressBar.setOnSeekBarChangeListener(componentListener);
            progressBar.setMax(PROGRESS_BAR_MAX);
        }
        playButton = findViewById(R.id.rd_exo_play);
        if (playButton != null) {
            playButton.setOnClickListener(componentListener);
        }
        pauseButton = findViewById(R.id.rd_exo_pause);
        if (pauseButton != null) {
            pauseButton.setOnClickListener(componentListener);
        }

    }

    /**
     * Returns the player currently being controlled by this view, or null if no player is set.
     */
    public ExoPlayer getPlayer() {
        return player;
    }

    /**
     * Sets the {@link ExoPlayer} to control.
     *
     * @param player the {@code ExoPlayer} to control.
     */
    public void setPlayer(ExoPlayer player) {
        if (this.player == player) {
            return;
        }
        if (this.player != null) {
            this.player.removeListener(componentListener);
        }
        this.player = player;
        if (player != null) {
            player.addListener(componentListener);
        }
        updateAll();
    }


    public void setVisibilityListener(VisibilityListener listener) {
        this.visibilityListener = listener;
    }


    public void setSeekDispatcher(SeekDispatcher seekDispatcher) {
        this.seekDispatcher = seekDispatcher == null ? DEFAULT_SEEK_DISPATCHER : seekDispatcher;
    }

    /**
     * Sets the rewind increment in milliseconds.
     *
     * @param rewindMs The rewind increment in milliseconds. A non-positive value will cause the
     *                 rewind button to be disabled.
     */
    public void setRewindIncrementMs(int rewindMs) {
        this.rewindMs = rewindMs;
        updateNavigation();
    }

    /**
     * Sets the fast forward increment in milliseconds.
     *
     * @param fastForwardMs The fast forward increment in milliseconds. A non-positive value will
     *                      cause the fast forward button to be disabled.
     */
    public void setFastForwardIncrementMs(int fastForwardMs) {
        this.fastForwardMs = fastForwardMs;
        updateNavigation();
    }

    /**
     * Returns the playback controls timeout. The playback controls are automatically hidden after
     * this duration of time has elapsed without user input.
     *
     * @return The duration in milliseconds. A non-positive value indicates that the controls will
     * remain visible indefinitely.
     */
    public int getShowTimeoutMs() {
        return showTimeoutMs;
    }

    /**
     * Sets the playback controls timeout. The playback controls are automatically hidden after this
     * duration of time has elapsed without user input.
     *
     * @param showTimeoutMs The duration in milliseconds. A non-positive value will cause the controls
     *                      to remain visible indefinitely.
     */
    public void setShowTimeoutMs(int showTimeoutMs) {
        this.showTimeoutMs = showTimeoutMs;
    }

    /**
     * Shows the playback controls. If {@link #getShowTimeoutMs()} is positive then the controls will
     * be automatically hidden after this duration of time has elapsed without user input.
     */
    public void show() {
        if (!isVisible()) {
            setVisibility(VISIBLE);
            if (visibilityListener != null) {
                visibilityListener.onVisibilityChange(getVisibility());
            }
            updateAll();
            requestPlayPauseFocus();
        }
        // Call hideAfterTimeout even if already visible to reset the timeout.
        hideAfterTimeout();
    }

    /**
     * Hides the controller.
     */
    public void hide() {
        if (isVisible()) {
            setVisibility(GONE);
            if (visibilityListener != null) {
                visibilityListener.onVisibilityChange(getVisibility());
            }
            removeCallbacks(updateProgressAction);
            removeCallbacks(hideAction);
            hideAtMs = C.TIME_UNSET;
        }
    }

    /**
     * Returns whether the controller is currently visible.
     */
    public boolean isVisible() {
        return getVisibility() == VISIBLE;
    }

    private void hideAfterTimeout() {
        removeCallbacks(hideAction);
        if (showTimeoutMs > 0) {
            hideAtMs = SystemClock.uptimeMillis() + showTimeoutMs;
            if (isAttachedToWindow) {
                postDelayed(hideAction, showTimeoutMs);
            }
        } else {
            hideAtMs = C.TIME_UNSET;
        }
    }

    private void updateAll() {
        updatePlayPauseButton();
        updateNavigation();
        updateProgress();
    }

    private void updatePlayPauseButton() {
        if (!isVisible() || !isAttachedToWindow) {
            return;
        }
        boolean requestPlayPauseFocus = false;
        boolean playing = player != null && player.getPlayWhenReady();
        if (playButton != null) {
            requestPlayPauseFocus |= playing && playButton.isFocused();
            playButton.setVisibility(playing ? View.GONE : View.VISIBLE);
        }
        if (pauseButton != null) {
            requestPlayPauseFocus |= !playing && pauseButton.isFocused();
            pauseButton.setVisibility(!playing ? View.GONE : View.VISIBLE);
        }
        if (requestPlayPauseFocus) {
            requestPlayPauseFocus();
        }
    }

    private void updateNavigation() {
        if (!isVisible() || !isAttachedToWindow) {
            return;
        }
        Timeline currentTimeline = player != null ? player.getCurrentTimeline() : null;
        boolean haveNonEmptyTimeline = currentTimeline != null && !currentTimeline.isEmpty();
        boolean isSeekable = false;
        boolean enablePrevious = false;
        boolean enableNext = false;
        if (haveNonEmptyTimeline) {
            int currentWindowIndex = player.getCurrentWindowIndex();
            currentTimeline.getWindow(currentWindowIndex, currentWindow);
            isSeekable = currentWindow.isSeekable;
            enablePrevious = currentWindowIndex > 0 || isSeekable || !currentWindow.isDynamic;
            enableNext = (currentWindowIndex < currentTimeline.getWindowCount() - 1)
                    || currentWindow.isDynamic;
        }

        if (progressBar != null) {
            progressBar.setEnabled(isSeekable);
        }
    }

    private void updateProgress() {
        if (!isVisible() || !isAttachedToWindow) {
            return;
        }
        long duration = player == null ? 0 : player.getDuration();
        long position = player == null ? 0 : player.getCurrentPosition();
        if (durationView != null) {
            durationView.setText(stringForTime(duration));
        }

        if (positionView != null && !dragging) {
            positionView.setText(stringForTime(position));
        }

        if (progressBar != null) {
            if (!dragging) {
                progressBar.setProgress(progressBarValue(position));
            }
            long bufferedPosition = player == null ? 0 : player.getBufferedPosition();
            progressBar.setSecondaryProgress(progressBarValue(bufferedPosition));
            // Remove scheduled updates.
        }
        removeCallbacks(updateProgressAction);
        // Schedule an update if necessary.
        int playbackState = player == null ? ExoPlayer.STATE_IDLE : player.getPlaybackState();
        if (playbackState != ExoPlayer.STATE_IDLE && playbackState != ExoPlayer.STATE_ENDED) {
            long delayMs;
            if (player.getPlayWhenReady() && playbackState == ExoPlayer.STATE_READY) {
                delayMs = 1000 - (position % 1000);
                if (delayMs < 200) {
                    delayMs += 1000;
                }
            } else {
                delayMs = 1000;
            }
            postDelayed(updateProgressAction, delayMs);
        }
    }

    private void requestPlayPauseFocus() {
        boolean playing = player != null && player.getPlayWhenReady();
        if (!playing && playButton != null) {
            playButton.requestFocus();
        } else if (playing && pauseButton != null) {
            pauseButton.requestFocus();
        }
    }

    private void setButtonEnabled(boolean enabled, View view) {
        if (view == null) {
            return;
        }
        view.setEnabled(enabled);
        if (Util.SDK_INT >= 11) {
            setViewAlphaV11(view, enabled ? 1f : 0.3f);
            view.setVisibility(VISIBLE);
        } else {
            view.setVisibility(enabled ? VISIBLE : INVISIBLE);
        }
    }

    @TargetApi(11)
    private void setViewAlphaV11(View view, float alpha) {
        view.setAlpha(alpha);
    }

    private String stringForTime(long timeMs) {
        if (timeMs == C.TIME_UNSET) {
            timeMs = 0;
        }
        long totalSeconds = (timeMs + 500) / 1000;
        long seconds = totalSeconds % 60;
        long minutes = (totalSeconds / 60) % 60;
        long hours = totalSeconds / 3600;
        formatBuilder.setLength(0);
        return hours > 0 ? formatter.format("%d:%02d:%02d", hours, minutes, seconds).toString()
                : formatter.format("%02d:%02d", minutes, seconds).toString();
    }

    private int progressBarValue(long position) {
        long duration = player == null ? C.TIME_UNSET : player.getDuration();
        return duration == C.TIME_UNSET || duration == 0 ? 0
                : (int) ((position * PROGRESS_BAR_MAX) / duration);
    }

    private long positionValue(int progress) {
        long duration = player == null ? C.TIME_UNSET : player.getDuration();
        return duration == C.TIME_UNSET ? 0 : ((duration * progress) / PROGRESS_BAR_MAX);
    }

    private void previous() {
        Timeline currentTimeline = player.getCurrentTimeline();
        if (currentTimeline.isEmpty()) {
            return;
        }
        int currentWindowIndex = player.getCurrentWindowIndex();
        currentTimeline.getWindow(currentWindowIndex, currentWindow);
        if (currentWindowIndex > 0 && (player.getCurrentPosition() <= MAX_POSITION_FOR_SEEK_TO_PREVIOUS
                || (currentWindow.isDynamic && !currentWindow.isSeekable))) {
            seekTo(currentWindowIndex - 1, C.TIME_UNSET);
        } else {
            seekTo(0);
        }
    }

    private void next() {
        Timeline currentTimeline = player.getCurrentTimeline();
        if (currentTimeline.isEmpty()) {
            return;
        }
        int currentWindowIndex = player.getCurrentWindowIndex();
        if (currentWindowIndex < currentTimeline.getWindowCount() - 1) {
            seekTo(currentWindowIndex + 1, C.TIME_UNSET);
        } else if (currentTimeline.getWindow(currentWindowIndex, currentWindow, false).isDynamic) {
            seekTo(currentWindowIndex, C.TIME_UNSET);
        }
    }

    private void rewind() {
        if (rewindMs <= 0) {
            return;
        }
        seekTo(Math.max(player.getCurrentPosition() - rewindMs, 0));
    }

    private void fastForward() {
        if (fastForwardMs <= 0) {
            return;
        }
        seekTo(Math.min(player.getCurrentPosition() + fastForwardMs, player.getDuration()));
    }

    private void seekTo(long positionMs) {
        seekTo(player.getCurrentWindowIndex(), positionMs);
    }

    private void seekTo(int windowIndex, long positionMs) {
        boolean dispatched = seekDispatcher.dispatchSeek(player, windowIndex, positionMs);
        if (!dispatched) {
            // The seek wasn't dispatched. If the progress bar was dragged by the user to perform the
            // seek then it'll now be in the wrong position. Trigger a progress update to snap it back.
            updateProgress();
        }
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        isAttachedToWindow = true;
        if (hideAtMs != C.TIME_UNSET) {
            long delayMs = hideAtMs - SystemClock.uptimeMillis();
            if (delayMs <= 0) {
                hide();
            } else {
                postDelayed(hideAction, delayMs);
            }
        }
        updateAll();
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        isAttachedToWindow = false;
        removeCallbacks(updateProgressAction);
        removeCallbacks(hideAction);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        boolean handled = dispatchMediaKeyEvent(event) || super.dispatchKeyEvent(event);
        if (handled) {
            show();
        }
        return handled;
    }

    /**
     * Called to process media key events. Any {@link KeyEvent} can be passed but only media key
     * events will be handled.
     *
     * @param event A key event.
     * @return Whether the key event was handled.
     */
    public boolean dispatchMediaKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        if (player == null || !isHandledMediaKey(keyCode)) {
            return false;
        }
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
                    fastForward();
                    break;
                case KeyEvent.KEYCODE_MEDIA_REWIND:
                    rewind();
                    break;
                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                    player.setPlayWhenReady(!player.getPlayWhenReady());
                    break;
                case KeyEvent.KEYCODE_MEDIA_PLAY:
                    player.setPlayWhenReady(true);
                    break;
                case KeyEvent.KEYCODE_MEDIA_PAUSE:
                    player.setPlayWhenReady(false);
                    break;
                case KeyEvent.KEYCODE_MEDIA_NEXT:
                    next();
                    break;
                case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                    previous();
                    break;
                default:
                    break;
            }
        }
        show();
        return true;
    }

    private static boolean isHandledMediaKey(int keyCode) {
        return keyCode == KeyEvent.KEYCODE_MEDIA_FAST_FORWARD
                || keyCode == KeyEvent.KEYCODE_MEDIA_REWIND
                || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
                || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY
                || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE
                || keyCode == KeyEvent.KEYCODE_MEDIA_NEXT
                || keyCode == KeyEvent.KEYCODE_MEDIA_PREVIOUS;
    }

    private final class ComponentListener implements ExoPlayer.EventListener,
            SeekBar.OnSeekBarChangeListener, View.OnClickListener {

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            removeCallbacks(hideAction);
            dragging = true;
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                long position = positionValue(progress);
                if (positionView != null) {
                    positionView.setText(stringForTime(position));
                }
                if (player != null && !dragging) {
                    seekTo(position);
                }
            }
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            dragging = false;
            if (player != null) {
                seekTo(positionValue(seekBar.getProgress()));
            }
            hideAfterTimeout();
        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            updatePlayPauseButton();
            updateProgress();
            if (playbackState == ExoPlayer.STATE_ENDED) {
                //播放完毕
                pause();
                seekTo(0);
            }


        }

        @Override
        public void onPositionDiscontinuity() {
            updateNavigation();
            updateProgress();
        }

        @Override
        public void onTimelineChanged(Timeline timeline, Object manifest) {
            updateNavigation();
            updateProgress();
        }

        @Override
        public void onLoadingChanged(boolean isLoading) {
            // Do nothing.
        }

        @Override
        public void onTracksChanged(TrackGroupArray tracks, TrackSelectionArray selections) {
            // Do nothing.
        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {
            // Do nothing.
        }

        @Override
        public void onClick(View view) {
            if (player != null) {
                if (playButton == view) {
                    player.setPlayWhenReady(true);
                } else if (pauseButton == view) {
                    player.setPlayWhenReady(false);
                }
            }
            hideAfterTimeout();
        }

    }

    //    public void onResumeStart() {
//        if (null != componentListener && null != playButton) {
//            componentListener.onClick(playButton);
//        }
//    }

    /**
     * 切后台响应暂停
     */
    public void pause() {
        if (null != componentListener && null != pauseButton) {
            componentListener.onClick(pauseButton);
        }

    }
}

