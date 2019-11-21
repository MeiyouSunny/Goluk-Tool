package com.rd.veuisdk;

import com.rd.vecore.VirtualVideo;

/**
 * @author JIAN
 * @create 2019/6/6
 * @Describe
 */
public interface IVideoMusicEditor extends IPlayer {

    boolean isMediaMute();


    void reload(boolean onlyMusic);


    VirtualVideo getEditorVideo();

    /**
     * 切换配乐强制清除mv中的音乐 (同理：切换mv，清除配乐)
     */
    void removeMvMusic(boolean remove);

    void onBack();

    void onSure();
}
