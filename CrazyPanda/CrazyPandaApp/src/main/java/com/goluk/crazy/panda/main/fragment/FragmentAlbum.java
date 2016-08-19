package com.goluk.crazy.panda.main.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.goluk.crazy.panda.R;
import com.goluk.crazy.panda.main.adapter.AlbumListAdapter;
import com.goluk.crazy.panda.test.TestData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by DELL-PC on 2016/8/18.
 */
public class FragmentAlbum extends Fragment {
    private static final String TAG = "FragmentAlbum";
    private RecyclerView mImageListRV;
    private AlbumListAdapter mAdapter;
    private List mImageList;

    public static class AlbumItemText {
        public String nDate;
        public String nLocation;
    }

    public class AlbumItemImage {
        public int nImagePath;
        public String nDesciption;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mImageList = new ArrayList();

        AlbumItemText itemText = new AlbumItemText();
        itemText.nDate = "Today";
        itemText.nLocation = "Here";
        mImageList.add(itemText);

        for(int i = 0; i < TestData.album_image_str.length; i++) {
            String path = TestData.album_image_str[i];
            int drawable = TestData.album_image_drawable[i];

            if(!path.contains("yestoday")) {
                AlbumItemImage item = new AlbumItemImage();
                item.nDesciption = path;
                item.nImagePath = drawable;
                mImageList.add(item);
            } else {
                break;
            }
        }

        AlbumItemText itemText1 = new AlbumItemText();
        itemText.nDate = "yestoday";
        itemText.nLocation = "There";
        mImageList.add(itemText1);

        for(int i = 0; i < TestData.album_image_str.length; i++) {
            String path = TestData.album_image_str[i];
            int drawable = TestData.album_image_drawable[i];

            if(path.contains("yestoday")) {
                AlbumItemImage item = new AlbumItemImage();
                item.nDesciption = path;
                item.nImagePath = drawable;
                mImageList.add(item);
            } else {
                continue;
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_album, null);

        mImageListRV = (RecyclerView)rootView.findViewById(R.id.rv_fragment_album_list);
        mImageListRV.setLayoutManager(new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL));
        mAdapter = new AlbumListAdapter(getActivity(), mImageList);
        mImageListRV.setAdapter(mAdapter);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}

