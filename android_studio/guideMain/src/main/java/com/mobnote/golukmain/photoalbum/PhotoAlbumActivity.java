package com.mobnote.golukmain.photoalbum;

import com.mobnote.golukmain.BaseActivity;
import com.mobnote.golukmain.R;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

public class PhotoAlbumActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_album_activity);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Bundle bundle = new Bundle();
        bundle.putBoolean(FragmentAlbum.PARENT_VIEW, false);
        FragmentAlbum fa = new FragmentAlbum();
        fa.setArguments(bundle);
        fragmentTransaction.add(R.id.photo_album_fragment, fa);
        fragmentTransaction.commitAllowingStateLoss();
    }
}
