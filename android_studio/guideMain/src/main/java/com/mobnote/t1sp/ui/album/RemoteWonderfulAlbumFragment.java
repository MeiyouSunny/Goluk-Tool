package com.mobnote.t1sp.ui.album;

import com.mobnote.golukmain.photoalbum.PhotoAlbumConfig;

/**
 * T1SP远程精彩视频列表
 */
public class RemoteWonderfulAlbumFragment extends BaseRemoteAblumFragment {

    @Override
    protected int getVideoType() {
        return PhotoAlbumConfig.PHOTO_BUM_IPC_WND;
    }

}
