package com.mobnote.t1sp.ui.album;

import java.util.List;

/**
 * Adapter回调Fragment接口
 */
public interface AlbumCloudAdapterListener {

    /**
     * 获取已选中的视频文件
     */
    List<String> getSelectedList();

    /**
     * 是否处于编辑模式
     */
    boolean getEditState();

}
