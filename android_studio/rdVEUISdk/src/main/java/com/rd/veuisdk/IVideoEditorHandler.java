package com.rd.veuisdk;

import android.graphics.RectF;

import com.rd.vecore.VirtualVideo;
import com.rd.vecore.VirtualVideoView;
import com.rd.vecore.models.MediaObject;
import com.rd.vecore.models.Scene;

import java.util.List;

//import com.rd.xpk.editor.modal.ImageObject;

/**
 * 视频编辑预览抽象接口
 *
 * @author abreal
 */
public interface IVideoEditorHandler {
    /**
     * 获取到编辑器
     *
     * @return
     */
    VirtualVideoView getEditor();

    /**
     * 获取到编辑器
     *
     * @return
     */
    VirtualVideo getEditorVideo();


    /**
     * 获取缩略图编辑器
     */
    VirtualVideo getSnapshotEditor();

    /**
     * 获取准备剪辑的图片或视频媒体列表
     *
     * @return
     */
    List<MediaObject> getEditingMediaObjectsWithTransition();

    /**
     * 编辑预览重新加载
     *
     * @param bFastPreview     是否为快速预览
     * @param lstEditingScenes 不为空时代表预览加载指定场景列表
     */
    void reload(boolean bFastPreview, List<Scene> lstEditingScenes);

    /**
     * 编辑预览重新加载
     *
     * @param bOnlyAudio 只重新加载音频
     */
    void reload(boolean bOnlyAudio);

    /**
     * 取消loading...
     */
    void cancelLoading();

    /**
     * 开始播放
     */
    void start();

    /**
     * 暂停预览
     */
    void pause();

    /**
     * 跳转到指定时间点
     *
     * @param msec 单位：毫秒
     */
    void seekTo(int msec);

    /**
     * 停止播放
     */
    void stop();

    /**
     * 是否播放中...
     *
     * @return
     */
    boolean isPlaying();

    /**
     * @return 获取播放持续时间(ms)
     */
    int getDuration();

    /**
     * @return 获取当前播放器的时间点 单位：毫秒
     */
    int getCurrentPosition();

    /**
     * 想要点击多段配乐
     */
    void onMenuChanged();

    /**
     * 改变滤镜
     *
     * @param nFilterType 滤镜类型:<br>
     */
    void changeFilterType(int nFilterType);

    /**
     * 获取当前滤镜
     *
     * @return 滤镜类型:<br>
     */
    int getCurrentFilterType();

    /**
     * 注册获取编辑器预览进度
     *
     * @param listener
     */
    void registerEditorPostionListener(EditorPreivewPositionListener listener);

    /**
     * 取消注册获取编辑器预览进度
     *
     * @param listener
     */
    void unregisterEditorProgressListener(EditorPreivewPositionListener listener);

    /**
     * 获取编辑器预览进度
     *
     * @author abreal
     */
    interface EditorPreivewPositionListener {
        /**
         * 响应播放器就绪
         */
        void onEditorPrepred();

        /**
         * 响应获取到编辑器预览进度
         *
         * @param nPosition 当前位置(ms)
         * @param nDuration 播放器持续时间(ms)
         */
        void onEditorGetPosition(int nPosition, int nDuration);

        /**
         * 响应播放器预览结束
         */
        void onEditorPreviewComplete();
    }

    interface IEditorThemeTitleHandler {
        /**
         * 设置主题标题是否显示，以及显示区域
         *
         * @param bShowing
         * @param rectTitleBack
         */
        void showTitleBack(boolean bShowing, RectF rectTitleBack);
    }

    /**
     * 视频资源是否静音
     *
     * @return
     */
    boolean isMediaMute();

    /**
     * 切换配乐强制清除mv中的音乐 (同理：切换mv，清除配乐)
     */
    void removeMvMusic(boolean remove);
}
