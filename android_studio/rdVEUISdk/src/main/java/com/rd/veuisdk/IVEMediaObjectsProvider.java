package com.rd.veuisdk;

import com.rd.vecore.Music;
import com.rd.vecore.models.MediaObject;

import java.util.List;

/**
 * 编辑预览媒体对象提供者
 * 
 * @author abreal
 * 
 */
public interface IVEMediaObjectsProvider {
    /**
     * 获取到媒体对象列表
     *
     * @return
     */
    List<Music> getMusicObjects();

    /**
     * 获取到媒体对象列表
     *
     * @return
     */
    List<MediaObject> getMediaObjects();
}
