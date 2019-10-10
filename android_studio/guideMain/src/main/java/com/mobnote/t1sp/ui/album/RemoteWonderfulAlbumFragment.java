package com.mobnote.t1sp.ui.album;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.mobnote.golukmain.photoalbum.PhotoAlbumConfig;

/**
 * T1SP远程精彩视频列表
 */
public class RemoteWonderfulAlbumFragment extends BaseRemoteAblumFragment {

    @Override
    protected int getVideoType() {
        return PhotoAlbumConfig.PHOTO_BUM_IPC_WND;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadData(true);
    }

}
