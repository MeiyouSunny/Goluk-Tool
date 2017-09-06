package com.rd.veuisdk;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.rd.vecore.VirtualVideo;
import com.rd.vecore.VirtualVideoView;
import com.rd.vecore.exception.InvalidStateException;
import com.rd.vecore.models.Scene;

/**
 * RdVEUISdk本地播放器View
 */
public class VideoView extends FrameLayout {
    private VirtualVideoView mVideoPreview;
    private PlayerListener listener;

    public VideoView(Context context) {
        super(context, null);
    }

    public VideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mVideoPreview = new VirtualVideoView(context, attrs);
        mVideoPreview.setOnPlaybackListener(epvListener);
        addView(mVideoPreview);
    }

    private void playVideo() {
        mVideoPreview.start();
    }

    private void pauseVideo() {
        mVideoPreview.pause();
    }

    private VirtualVideoView.VideoViewListener epvListener = new VirtualVideoView.VideoViewListener() {

        /**
         * 播放器准备完成回调
         */
        @Override
        public void onPlayerPrepared() {
            if (null != listener) {
                listener.onPlayerPrepare(VideoView.this);
            }
        }

        /**
         * 播放器错误回调
         *
         * @param what
         * @param extra
         * @return
         */
        @Override
        public boolean onPlayerError(int what, int extra) {
            if (null != listener) {
                return listener.onPlayerError(VideoView.this, what, extra);
            } else {
                return false;
            }
        }

        /**
         * 播放完成回调
         */
        @Override
        public void onPlayerCompletion() {
            if (null != listener) {
                listener.onPlayerCompletion(VideoView.this);
            }
        }

        /**
         * 播放进度回调
         *
         * @param position 播放进度(单位秒)
         */
        @Override
        public void onGetCurrentPosition(float position) {
            if (null != listener) {
                listener.onGetCurrentPosition(VideoView.this, position);
            }
        }

    };

    /**
     * 设置需要播放的视频路径
     *
     * @param videoPath 视频路径
     */
    public void setVideoPath(String videoPath) throws InvalidStateException {
        mVideoPreview.reset();
        VirtualVideo virtualVideo = new VirtualVideo();
        Scene scene = new Scene(videoPath);
        virtualVideo.addScene(scene);
        virtualVideo.build(mVideoPreview);
        start();
    }

    /**
     * 开始播放
     */
    public void start() {
        playVideo();
    }

    /**
     * 暂停播放
     */
    public void pause() {
        pauseVideo();
    }

    /**
     * 获取是否播放器
     */
    public boolean isPlaying() {
        return mVideoPreview.isPlaying();
    }

    /**
     * 获取当前播放位置(second)
     */
    public float getCurrentPosition() {
        return mVideoPreview.getCurrentPosition();
    }

    /**
     * 设置播放位置(ms)
     *
     * @param position 播放位置(second)
     */
    public void seekTo(float position) {
        mVideoPreview.seekTo(position);
    }

    /**
     * 获取持续时间(second)
     */
    public float getDuration() {
        return mVideoPreview.getDuration();
    }

    /**
     * 设置播放器listener
     *
     * @param listener
     */
    public void setPlayerListener(PlayerListener listener) {
        this.listener = listener;
    }

    /**
     * 播放器listener
     *
     * @author scott, abreal
     */
    public interface PlayerListener {
        /**
         * 播放器已经就绪
         *
         * @param mediaPlayerControl
         */
        void onPlayerPrepare(VideoView mediaPlayerControl);

        /**
         * 播放器出现错误
         *
         * @param mediaPlayerControl
         * @param what               错误号
         * @param extra              错误扩展信息
         * @return 返回true代表已处理错误
         */
        boolean onPlayerError(VideoView mediaPlayerControl, int what,
                              int extra);

        /**
         * 播放器播放已结束
         *
         * @param mediaPlayerControl
         */
        void onPlayerCompletion(VideoView mediaPlayerControl);

        /**
         * 获取到当前播放位置
         *
         * @param mediaPlayerControl
         * @param position           当前播放位置(秒为单位)
         */
        void onGetCurrentPosition(VideoView mediaPlayerControl,
                                  float position);
    }

}
