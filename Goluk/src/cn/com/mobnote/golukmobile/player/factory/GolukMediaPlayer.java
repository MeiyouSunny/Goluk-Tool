package cn.com.mobnote.golukmobile.player.factory;

import java.io.IOException;
import java.util.Map;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.view.Surface;

public class GolukMediaPlayer implements GolukPlayer, MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener,
		MediaPlayer.OnErrorListener, MediaPlayer.OnVideoSizeChangedListener, MediaPlayer.OnInfoListener,
		MediaPlayer.OnBufferingUpdateListener {

	private MediaPlayer mMediaPlayer = null;
	private OnPreparedListener mOnPreparedListener = null;
	private OnCompletionListener mOnCompletionListener = null;
	private OnVideoSizeChangedListener mOnVideoSizeChangedListener = null;
	private OnErrorListener mOnErrorListener = null;
	private OnInfoListener mOnInfoListener = null;
	private OnBufferingUpdateListener mOnBufferingUpdateListener = null;

	public GolukMediaPlayer() {
		mMediaPlayer = new MediaPlayer();
		mMediaPlayer.setOnPreparedListener(this);
		mMediaPlayer.setOnCompletionListener(this);
		mMediaPlayer.setOnVideoSizeChangedListener(this);
		mMediaPlayer.setOnErrorListener(this);
		mMediaPlayer.setOnInfoListener(this);
		mMediaPlayer.setOnBufferingUpdateListener(this);
	}

	@Override
	public void seekTo(long positionMs) {
		// TODO Auto-generated method stub
		mMediaPlayer.seekTo((int) positionMs);
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		mMediaPlayer.stop();
	}

	@Override
	public void release() {
		// TODO Auto-generated method stub
		mMediaPlayer.release();
	}

	@Override
	public int getDuration() {
		// TODO Auto-generated method stub
		return mMediaPlayer.getDuration();
	}

	@Override
	public int getCurrentPosition() {
		// TODO Auto-generated method stub
		return mMediaPlayer.getCurrentPosition();
	}

	@Override
	public void start() {
		// TODO Auto-generated method stub
		mMediaPlayer.start();
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		mMediaPlayer.pause();
	}

	@Override
	public void prepare() {
		// TODO Auto-generated method stub
		mMediaPlayer.prepareAsync();
	}

	@Override
	public void setDataSource(Context context, Uri uri, Map<String, String> headers) throws IllegalArgumentException,
			SecurityException, IllegalStateException, IOException {
		// TODO Auto-generated method stub
		mMediaPlayer.setDataSource(context, uri, headers);
	}

	@Override
	public void setSurface(Surface surface) {
		// TODO Auto-generated method stub
		mMediaPlayer.setSurface(surface);
	}

	@Override
	public void setAudioStreamType(int streamtype) {
		// TODO Auto-generated method stub
		mMediaPlayer.setAudioStreamType(streamtype);
	}

	@Override
	public void setScreenOnWhilePlaying(boolean screenOn) {
		// TODO Auto-generated method stub
		mMediaPlayer.setScreenOnWhilePlaying(screenOn);
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
		mOnBufferingUpdateListener = listener;
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		// TODO Auto-generated method stub
		if (mOnPreparedListener != null) {
			mOnPreparedListener.onPrepared(this);
		}
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		// TODO Auto-generated method stub
		if (mOnCompletionListener != null) {
			mOnCompletionListener.onCompletion(this);
		}
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		// TODO Auto-generated method stub
		if (mOnErrorListener != null) {
			return mOnErrorListener.onError(this, what, extra);
		} else {
			return false;
		}
	}

	@Override
	public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
		// TODO Auto-generated method stub
		if (mOnVideoSizeChangedListener != null) {
			mOnVideoSizeChangedListener.onVideoSizeChanged(this, width, height);
		}
	}

	@Override
	public boolean onInfo(MediaPlayer mp, int what, int extra) {
		// TODO Auto-generated method stub
		if (mOnInfoListener != null) {
			return mOnInfoListener.onInfo(this, what, extra);
		} else {
			return false;
		}
	}

	@Override
	public void onBufferingUpdate(MediaPlayer mp, int percent) {
		// TODO Auto-generated method stub
		if (mOnBufferingUpdateListener != null) {
			mOnBufferingUpdateListener.onBufferingUpdate(this, percent);
		}
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		mMediaPlayer.reset();
	}

	@Override
	public boolean isPlaying() {
		// TODO Auto-generated method stub
		return mMediaPlayer.isPlaying();
	}

	@Override
	public int getVideoWidth() {
		// TODO Auto-generated method stub
		return mMediaPlayer.getVideoWidth();
	}

	@Override
	public int getVideoHeight() {
		// TODO Auto-generated method stub
		return mMediaPlayer.getVideoHeight();
	}

	@Override
	public void setAudioSessionId(int sessionid) {
		// TODO Auto-generated method stub
		mMediaPlayer.setAudioSessionId(sessionid);
	}

	@Override
	public int getAudioSessionId() {
		// TODO Auto-generated method stub
		return mMediaPlayer.getAudioSessionId();
	}

}
