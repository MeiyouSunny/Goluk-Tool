package com.rd.veuisdk;

import android.widget.FrameLayout;

import com.rd.vecore.VirtualVideo;
import com.rd.vecore.VirtualVideoView;
import com.rd.veuisdk.ae.model.AETemplateInfo;
import com.rd.veuisdk.fragment.helper.IFilterHandler;


/**
 * 视频编辑预览抽象接口
 *
 * @author abreal
 */
public interface IVideoEditorHandler extends IFilterHandler, IVideoMusicEditor {
    /**
     * 获取到编辑器
     *
     */
    VirtualVideoView getEditor();

    /**
     * 字幕容器
     */
    FrameLayout getSubEditorParent();

    /**
     * 获取缩略图编辑器
     */
    VirtualVideo getSnapshotEditor();

    /**
     * 取消loading...
     */
    void cancelLoading();

    /**
     * 停止播放
     */
    void stop();

    /**
     * 改变动画模板
     */
    void changeAnimation(int animation);

    /**
     * 改变AE模板
     */
    void setAETemplateInfo(AETemplateInfo aeTemplateInfo);

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

    /**
     * 切换配乐强制清除mv中的音乐 (同理：切换mv，清除配乐)
     */
    void removeMvMusic(boolean remove);

    void onProportionChanged(float aspect);

    void onBackgroundModeChanged(boolean isEnableBg);

    void onBackgroundColorChanged(int color);


}
