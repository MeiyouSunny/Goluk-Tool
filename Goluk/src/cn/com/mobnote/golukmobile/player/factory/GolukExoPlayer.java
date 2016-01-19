package cn.com.mobnote.golukmobile.player.factory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.media.MediaCodec.CryptoException;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Surface;

import com.google.android.exoplayer.ExoPlaybackException;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.ExoPlayer.Listener;
import com.google.android.exoplayer.MediaCodecTrackRenderer.DecoderInitializationException;
import com.google.android.exoplayer.DummyTrackRenderer;
import com.google.android.exoplayer.MediaCodecVideoTrackRenderer;
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.TrackRenderer;
import com.google.android.exoplayer.audio.AudioTrack.InitializationException;
import com.google.android.exoplayer.audio.AudioTrack.WriteException;
import com.google.android.exoplayer.text.Cue;
import com.google.android.exoplayer.text.TextRenderer;
import com.google.android.exoplayer.upstream.BandwidthMeter;

public class GolukExoPlayer implements GolukPlayer, MediaCodecVideoTrackRenderer.EventListener,
		MediaCodecAudioTrackRenderer.EventListener, TextRenderer, Listener {

	/**
	 * Builds renderers for the player.
	 */
	public interface RendererBuilder {
		/**
		 * Builds renderers for playback.
		 *
		 * @param player
		 *            The player for which renderers are being built.
		 *            {@link DemoPlayer#onRenderers} should be invoked once the
		 *            renderers have been built. If building fails,
		 *            {@link DemoPlayer#onRenderersError} should be invoked.
		 */
		void buildRenderers(GolukExoPlayer player);

		/**
		 * Cancels the current build operation, if there is one. Else does
		 * nothing.
		 * <p>
		 * A canceled build operation must not invoke
		 * {@link DemoPlayer#onRenderers} or {@link DemoPlayer#onRenderersError}
		 * on the player, which may have been released.
		 */
		void cancel();
	}

	private ExoPlayer mPlayer;
	// Constants pulled into this class for convenience.
	public static final int STATE_IDLE = ExoPlayer.STATE_IDLE;
	public static final int STATE_PREPARING = ExoPlayer.STATE_PREPARING;
	public static final int STATE_BUFFERING = ExoPlayer.STATE_BUFFERING;
	public static final int STATE_READY = ExoPlayer.STATE_READY;
	public static final int STATE_ENDED = ExoPlayer.STATE_ENDED;
	public static final int TRACK_DISABLED = ExoPlayer.TRACK_DISABLED;
	public static final int TRACK_DEFAULT = ExoPlayer.TRACK_DEFAULT;
	public static final int RENDERER_COUNT = 4;
	public static final int TYPE_VIDEO = 0;
	public static final int TYPE_AUDIO = 1;
	public static final int TYPE_TEXT = 2;
	public static final int TYPE_METADATA = 3;

	private static final int RENDERER_BUILDING_STATE_IDLE = 1;
	private static final int RENDERER_BUILDING_STATE_BUILDING = 2;
	private static final int RENDERER_BUILDING_STATE_BUILT = 3;

	private int mRendererBuildingState;
	private int mLastReportedPlaybackState;
	private boolean mLastReportedPlayWhenReady;
	private Surface mSurface;
	private TrackRenderer mVideoRenderer;
	private RendererBuilder mRendererBuilder;
	private final Handler mHandler;

	private OnPreparedListener mOnPreparedListener = null;
	private OnCompletionListener mOnCompletionListener = null;
	private OnVideoSizeChangedListener mOnVideoSizeChangedListener = null;
	private OnErrorListener mOnErrorListener = null;
	private OnInfoListener mOnInfoListener = null;
	private OnBufferingUpdateListener mOnBufferingUpdateListener = null;
    private int mVideoWidth;
    private int mVideoHeight;

	public GolukExoPlayer() {
		mPlayer = ExoPlayer.Factory.newInstance(RENDERER_COUNT, 1000, 5000);
		mPlayer.addListener(this);
		mRendererBuildingState = RENDERER_BUILDING_STATE_IDLE;
		mLastReportedPlaybackState = STATE_IDLE;
		mHandler = new Handler();
	}

	@Override
	public void seekTo(long positionMs) {
		// TODO Auto-generated method stub
		long seekPosition = mPlayer.getDuration() == ExoPlayer.UNKNOWN_TIME ? 0 : Math.min(Math.max(0, positionMs),
				getDuration());
		mPlayer.seekTo(seekPosition);
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		mPlayer.stop();
	}

	@Override
	public void release() {
		// TODO Auto-generated method stub
		if (mPlayer != null) {
			mRendererBuilder.cancel();
			mRendererBuildingState = RENDERER_BUILDING_STATE_IDLE;
			mSurface = null;
			mPlayer.removeListener(this);
			mPlayer.release();
			mPlayer = null;
		}
	}

	@Override
	public int getDuration() {
		// TODO Auto-generated method stub
		return (int) mPlayer.getDuration();
	}

	@Override
	public int getCurrentPosition() {
		// TODO Auto-generated method stub
		return (int) mPlayer.getCurrentPosition();
	}

	@Override
	public void start() {
		// TODO Auto-generated method stub
		mPlayer.setPlayWhenReady(true);
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		mPlayer.setPlayWhenReady(false);
	}

	@Override
	public void prepare() {
		// TODO Auto-generated method stub
		if (mRendererBuildingState == RENDERER_BUILDING_STATE_BUILT) {
			mPlayer.stop();
		}
		mRendererBuilder.cancel();
		mVideoRenderer = null;
		mRendererBuildingState = RENDERER_BUILDING_STATE_BUILDING;
		maybeReportPlayerState();
		mRendererBuilder.buildRenderers(this);
	}

	public void setSurface(Surface surface) {
		mSurface = surface;
		pushSurface(false);
	}

	public Surface getSurface() {
		return mSurface;
	}

	public void blockingClearSurface() {
		mSurface = null;
		pushSurface(true);
	}

	private void maybeReportPlayerState() {
		boolean playWhenReady = mPlayer.getPlayWhenReady();
		int playbackState = getPlaybackState();
		if (mLastReportedPlayWhenReady != playWhenReady || mLastReportedPlaybackState != playbackState) {
			onStateChanged(playWhenReady, playbackState);
			mLastReportedPlayWhenReady = playWhenReady;
			mLastReportedPlaybackState = playbackState;
		}
	}

	private void pushSurface(boolean blockForSurfacePush) {
		if (mVideoRenderer == null) {
			return;
		}

		if (blockForSurfacePush) {
			mPlayer.blockingSendMessage(mVideoRenderer, MediaCodecVideoTrackRenderer.MSG_SET_SURFACE, mSurface);
		} else {
			mPlayer.sendMessage(mVideoRenderer, MediaCodecVideoTrackRenderer.MSG_SET_SURFACE, mSurface);
		}
	}

	public int getPlaybackState() {
		if (mRendererBuildingState == RENDERER_BUILDING_STATE_BUILDING) {
			return STATE_PREPARING;
		}
		int playerState = mPlayer.getPlaybackState();
		if (mRendererBuildingState == RENDERER_BUILDING_STATE_BUILT && playerState == STATE_IDLE) {
			// This is an edge case where the renderers are built, but are still
			// being passed to the
			// player's playback thread.
			return STATE_PREPARING;
		}
		return playerState;
	}

	@Override
	public void setDataSource(Context context, Uri uri, Map<String, String> headers) throws IllegalArgumentException,
			SecurityException, IllegalStateException, IOException {
		// TODO Auto-generated method stub
		mRendererBuilder = new ExtractorRendererBuilder(context, "Goluk Play", uri);
	}

	@Override
	public void setAudioStreamType(int streamtype) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setScreenOnWhilePlaying(boolean screenOn) {
		// TODO Auto-generated method stub

	}

	/* package */Looper getPlaybackLooper() {
		return mPlayer.getPlaybackLooper();
	}

	/* package */Handler getMainHandler() {
		return mHandler;
	}

	@Override
	public void onCryptoError(CryptoException arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDecoderInitializationError(DecoderInitializationException arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDecoderInitialized(String arg0, long arg1, long arg2) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDrawnToSurface(Surface arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDroppedFrames(int arg0, long arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
		// TODO Auto-generated method stub
        mVideoWidth = width;
        mVideoHeight = height;
		if (mOnVideoSizeChangedListener != null) {
			mOnVideoSizeChangedListener.onVideoSizeChanged(this, width, height);
		}
	}

	@Override
	public void onAudioTrackInitializationError(InitializationException arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onAudioTrackUnderrun(int arg0, long arg1, long arg2) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onAudioTrackWriteError(WriteException arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onCues(List<Cue> arg0) {
		// TODO Auto-generated method stub

	}

	/**
	 * Invoked with the results from a {@link RendererBuilder}.
	 *
	 * @param renderers
	 *            Renderers indexed by {@link DemoPlayer} TYPE_* constants. An
	 *            individual element may be null if there do not exist tracks of
	 *            the corresponding type.
	 * @param bandwidthMeter
	 *            Provides an estimate of the currently available bandwidth. May
	 *            be null.
	 */
	/* package */void onRenderers(TrackRenderer[] renderers, BandwidthMeter bandwidthMeter) {
		for (int i = 0; i < RENDERER_COUNT; i++) {
			if (renderers[i] == null) {
				// Convert a null renderer to a dummy renderer.
				renderers[i] = new DummyTrackRenderer();
			}
		}
		// Complete preparation.
		mVideoRenderer = renderers[TYPE_VIDEO];
		pushSurface(false);
		mPlayer.prepare(renderers);
		mRendererBuildingState = RENDERER_BUILDING_STATE_BUILT;
	}

	@Override
	public void setOnPreparedListener(OnPreparedListener listener) {
		// TODO Auto-generated method stub
		mOnPreparedListener = listener;
	}

	@Override
	public void setOnCompletionListener(OnCompletionListener listener) {
		// TODO Auto-generated method stub
		mOnCompletionListener = listener;
	}

	@Override
	public void setOnVideoSizeChangedListener(OnVideoSizeChangedListener listener) {
		// TODO Auto-generated method stub
		mOnVideoSizeChangedListener = listener;
	}

	@Override
	public void setOnErrorListener(OnErrorListener listener) {
		// TODO Auto-generated method stub
		mOnErrorListener = listener;
	}

	@Override
	public void setOnInfoListener(OnInfoListener listener) {
		// TODO Auto-generated method stub
		mOnInfoListener = listener;
	}

	@Override
	public void setOnBufferingUpdateListener(OnBufferingUpdateListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPlayWhenReadyCommitted() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPlayerError(ExoPlaybackException arg0) {
		// TODO Auto-generated method stub
	    mRendererBuildingState = RENDERER_BUILDING_STATE_IDLE;
	    if(mOnErrorListener != null) {
	    	mOnErrorListener.onError(this, MediaPlayer.MEDIA_ERROR_UNKNOWN, MediaPlayer.MEDIA_ERROR_UNKNOWN);
	    }
	}

	@Override
	public void onPlayerStateChanged(boolean arg0, int arg1) {
		// TODO Auto-generated method stub
		maybeReportPlayerState();
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean isPlaying() {
		// TODO Auto-generated method stub
		if (mPlayer == null)
			return false;
		int state = mPlayer.getPlaybackState();
		switch (state) {
		case ExoPlayer.STATE_BUFFERING:
		case ExoPlayer.STATE_READY:
			return mPlayer.getPlayWhenReady();
		case ExoPlayer.STATE_IDLE:
		case ExoPlayer.STATE_PREPARING:
		case ExoPlayer.STATE_ENDED:
		default:
			return false;
		}
	}

	private boolean mIsPrepareing = false;
	private boolean mIsBuffering = false;

	private void onStateChanged(boolean playWhenReady, int playbackState) {
		switch (playbackState) {
		case ExoPlayer.STATE_IDLE:
			// if (mOnCompletionListener != null) {
			// mOnCompletionListener.onCompletion(this);
			// }
			break;
		case ExoPlayer.STATE_PREPARING:
			mIsPrepareing = true;
			break;
		case ExoPlayer.STATE_BUFFERING:
			if (mOnInfoListener != null) {
				mOnInfoListener.onInfo(this, MediaPlayer.MEDIA_INFO_BUFFERING_START, mPlayer.getBufferedPercentage());
			}
			mIsBuffering = true;
			break;
		case ExoPlayer.STATE_READY:
			if (mIsPrepareing) {
				if (mOnPreparedListener != null) {
					mOnPreparedListener.onPrepared(this);
				}
				mIsPrepareing = false;
			}
			if (mIsBuffering) {
				if (mOnInfoListener != null) {
					mOnInfoListener.onInfo(this, MediaPlayer.MEDIA_INFO_BUFFERING_END, mPlayer.getBufferedPercentage());
				}
				mIsBuffering = false;
			}
			break;
		case ExoPlayer.STATE_ENDED:
			if (mOnCompletionListener != null) {
				mOnCompletionListener.onCompletion(this);
			}
			break;
		default:
			break;
		}
	}

	@Override
	public int getVideoWidth() {
		// TODO Auto-generated method stub
		return mVideoWidth;
	}

	@Override
	public int getVideoHeight() {
		// TODO Auto-generated method stub
		return mVideoHeight;
	}

	@Override
	public void setAudioSessionId(int sessionid) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getAudioSessionId() {
		// TODO Auto-generated method stub
		return 0;
	}

}
