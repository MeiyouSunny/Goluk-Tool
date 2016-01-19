package cn.com.mobnote.golukmobile.player.factory;

import java.io.IOException;
import java.util.Map;

import android.content.Context;
import android.net.Uri;
import android.view.Surface;

public interface GolukPlayer {

	/**
	 * Seeks to a position specified in milliseconds.
	 *
	 * @param positionMs
	 *            The seek position.
	 */
	public void seekTo(long positionMs);

	/**
	 * Stops playback. Use {@code setPlayWhenReady(false)} rather than this
	 * method if the intention is to pause playback.
	 * <p>
	 * Calling this method will cause the playback state to transition to
	 * {@link ExoPlayer#STATE_IDLE}. The player instance can still be used, and
	 * {@link ExoPlayer#release()} must still be called on the player if it's no
	 * longer required.
	 * <p>
	 * Calling this method does not reset the playback position. If this player
	 * instance will be used to play another video from its start, then
	 * {@code seekTo(0)} should be called after stopping the player and before
	 * preparing it for the next video.
	 */
	public void stop();

	/**
	 * Releases the player. This method must be called when the player is no
	 * longer required.
	 * <p>
	 * The player must not be used after calling this method.
	 */
	public void release();

	/**
	 * Gets the duration of the track in milliseconds.
	 *
	 * @return The duration of the track in milliseconds, or
	 *         {@link ExoPlayer#UNKNOWN_TIME} if the duration is not known.
	 */
	public int getDuration();

	/**
	 * Gets the current playback position in milliseconds.
	 *
	 * @return The current playback position in milliseconds.
	 */
	public int getCurrentPosition();

	public void start();

	public void pause();

	public void prepare();

	public void reset();

	public void setSurface(Surface surface);

	public void setDataSource(Context context, Uri uri, Map<String, String> headers) throws IllegalArgumentException,
			SecurityException, IllegalStateException, IOException;

	public boolean isPlaying();

	public void setAudioStreamType(int streamtype);

	public void setScreenOnWhilePlaying(boolean screenOn);

	public int getVideoWidth();

	public int getVideoHeight();
	
	public void setAudioSessionId(int sessionid);
	
	public int getAudioSessionId();
	/**
	 * Interface definition for a callback to be invoked when the media source
	 * is ready for playback.
	 */
	public interface OnPreparedListener {
		/**
		 * Called when the media file is ready for playback.
		 *
		 * @param mp
		 *            the MediaPlayer that is ready for playback
		 */
		void onPrepared(GolukPlayer mp);
	}

	/**
	 * Register a callback to be invoked when the media source is ready for
	 * playback.
	 *
	 * @param listener
	 *            the callback that will be run
	 */
	public void setOnPreparedListener(OnPreparedListener listener);

	/**
	 * Interface definition for a callback to be invoked when playback of a
	 * media source has completed.
	 */
	public interface OnCompletionListener {
		/**
		 * Called when the end of a media source is reached during playback.
		 *
		 * @param mp
		 *            the MediaPlayer that reached the end of the file
		 */
		void onCompletion(GolukPlayer mp);
	}

	/**
	 * Register a callback to be invoked when the end of a media source has been
	 * reached during playback.
	 *
	 * @param listener
	 *            the callback that will be run
	 */
	public void setOnCompletionListener(OnCompletionListener listener);

	public interface OnVideoSizeChangedListener {
		/**
		 * Called to indicate the video size
		 *
		 * The video size (width and height) could be 0 if there was no video,
		 * no display surface was set, or the value was not determined yet.
		 *
		 * @param mp
		 *            the MediaPlayer associated with this callback
		 * @param width
		 *            the width of the video
		 * @param height
		 *            the height of the video
		 */
		public void onVideoSizeChanged(GolukPlayer mp, int width, int height);
	}

	/**
	 * Register a callback to be invoked when the video size is known or
	 * updated.
	 *
	 * @param listener
	 *            the callback that will be run
	 */
	public void setOnVideoSizeChangedListener(OnVideoSizeChangedListener listener);

	public interface OnErrorListener {
		/**
		 * Called to indicate an error.
		 *
		 * @param mp
		 *            the MediaPlayer the error pertains to
		 * @param what
		 *            the type of error that has occurred:
		 *            <ul>
		 *            <li>{@link #MEDIA_ERROR_UNKNOWN}
		 *            <li>{@link #MEDIA_ERROR_SERVER_DIED}
		 *            </ul>
		 * @param extra
		 *            an extra code, specific to the error. Typically
		 *            implementation dependent.
		 *            <ul>
		 *            <li>{@link #MEDIA_ERROR_IO}
		 *            <li>{@link #MEDIA_ERROR_MALFORMED}
		 *            <li>{@link #MEDIA_ERROR_UNSUPPORTED}
		 *            <li>{@link #MEDIA_ERROR_TIMED_OUT}
		 *            <li><code>MEDIA_ERROR_SYSTEM (-2147483648)</code> -
		 *            low-level system error.
		 *            </ul>
		 * @return True if the method handled the error, false if it didn't.
		 *         Returning false, or not having an OnErrorListener at all,
		 *         will cause the OnCompletionListener to be called.
		 */
		boolean onError(GolukPlayer mp, int what, int extra);
	}

	/**
	 * Register a callback to be invoked when an error has happened during an
	 * asynchronous operation.
	 *
	 * @param listener
	 *            the callback that will be run
	 */
	public void setOnErrorListener(OnErrorListener listener);

	public interface OnInfoListener {
		/**
		 * Called to indicate an info or a warning.
		 *
		 * @param mp
		 *            the MediaPlayer the info pertains to.
		 * @param what
		 *            the type of info or warning.
		 *            <ul>
		 *            <li>{@link #MEDIA_INFO_UNKNOWN}
		 *            <li>{@link #MEDIA_INFO_VIDEO_TRACK_LAGGING}
		 *            <li>{@link #MEDIA_INFO_VIDEO_RENDERING_START}
		 *            <li>{@link #MEDIA_INFO_BUFFERING_START}
		 *            <li>{@link #MEDIA_INFO_BUFFERING_END}
		 *            <li><code>MEDIA_INFO_NETWORK_BANDWIDTH (703)</code> -
		 *            bandwidth information is available (as <code>extra</code>
		 *            kbps)
		 *            <li>{@link #MEDIA_INFO_BAD_INTERLEAVING}
		 *            <li>{@link #MEDIA_INFO_NOT_SEEKABLE}
		 *            <li>{@link #MEDIA_INFO_METADATA_UPDATE}
		 *            <li>{@link #MEDIA_INFO_UNSUPPORTED_SUBTITLE}
		 *            <li>{@link #MEDIA_INFO_SUBTITLE_TIMED_OUT}
		 *            </ul>
		 * @param extra
		 *            an extra code, specific to the info. Typically
		 *            implementation dependent.
		 * @return True if the method handled the info, false if it didn't.
		 *         Returning false, or not having an OnErrorListener at all,
		 *         will cause the info to be discarded.
		 */
		boolean onInfo(GolukPlayer mp, int what, int extra);
	}

	/**
	 * Register a callback to be invoked when an info/warning is available.
	 *
	 * @param listener
	 *            the callback that will be run
	 */
	public void setOnInfoListener(OnInfoListener listener);

	/**
	 * Interface definition of a callback to be invoked indicating buffering
	 * status of a media resource being streamed over the network.
	 */
	public interface OnBufferingUpdateListener {
		/**
		 * Called to update status in buffering a media stream received through
		 * progressive HTTP download. The received buffering percentage
		 * indicates how much of the content has been buffered or played. For
		 * example a buffering update of 80 percent when half the content has
		 * already been played indicates that the next 30 percent of the content
		 * to play has been buffered.
		 *
		 * @param mp
		 *            the MediaPlayer the update pertains to
		 * @param percent
		 *            the percentage (0-100) of the content that has been
		 *            buffered or played thus far
		 */
		void onBufferingUpdate(GolukPlayer mp, int percent);
	}

	/**
	 * Register a callback to be invoked when the status of a network stream's
	 * buffer has changed.
	 *
	 * @param listener
	 *            the callback that will be run.
	 */
	public void setOnBufferingUpdateListener(OnBufferingUpdateListener listener);

}
