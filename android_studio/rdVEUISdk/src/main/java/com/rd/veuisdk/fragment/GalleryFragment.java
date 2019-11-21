package com.rd.veuisdk.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;

import com.rd.veuisdk.R;
import com.rd.veuisdk.base.BaseFragment;
import com.rd.veuisdk.model.ImageItem;

import java.util.ArrayList;

/**
 * 画中画-图库
 */
public class GalleryFragment extends BaseFragment {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TAG = "GalleryFragment";
    }

    public static GalleryFragment newInstance() {

        Bundle args = new Bundle();

        GalleryFragment fragment = new GalleryFragment();
        fragment.setArguments(args);
        return fragment;
    }


    public interface ICallBack {
        void onItem(ImageItem item);
    }


    private ViewPager mViewPager;
    private MPageAdapter mAdapter;


    public void setGallerySizeListener(IGallerySizeListener gallerySizeListener) {
        mGallerySizeListener = gallerySizeListener;
    }

    private IGallerySizeListener mGallerySizeListener;

    @Override
    public void initView(View view) {
        $(R.id.ivChangeSize).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mGallerySizeListener) {
                    mGallerySizeListener.onGallerySizeClicked();
                }
            }
        });
        mViewPager = $(R.id.mediaViewPager);
        mVideoFragment = VideoFragment.newInstance();
        mPhotoFragment = PhotoFragment.newInstance();
        mAdapter = new MPageAdapter(getChildFragmentManager());
        mViewPager.setAdapter(mAdapter);

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mVideoFragment.setCallBack(new ICallBack() {
            @Override
            public void onItem(ImageItem item) {
                //退出全屏
                if (null != mGalleryCallBack) {
                    mGalleryCallBack.onVideo(item);
                }
            }
        });
        mPhotoFragment.setCallBack(new ICallBack() {
            @Override
            public void onItem(ImageItem item) {
                if (null != mGalleryCallBack) {
                    mGalleryCallBack.onPhoto(item);
                }
            }
        });

        mVPageListener = new VPageListener();
        mViewPager.addOnPageChangeListener(mVPageListener);
        mViewPager.setCurrentItem(isCheckVideo ? 0 : 1);
    }


    void setCheckVideo(boolean checkVideo) {
        isCheckVideo = checkVideo;
    }

    private boolean isCheckVideo = true;

    private VPageListener mVPageListener;

    private class VPageListener implements ViewPager.OnPageChangeListener {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            if (null != mGalleryCallBack) {
                mGalleryCallBack.onRGCheck(position == 0);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    }

    ;

    void onVideoClick() {
        if (null != mViewPager) {
            mViewPager.removeOnPageChangeListener(mVPageListener);
            mViewPager.setCurrentItem(0, true);
            mViewPager.addOnPageChangeListener(mVPageListener);
        }

    }

    void onPhotoClick() {
        if (null != mViewPager) {
            mViewPager.removeOnPageChangeListener(mVPageListener);
            mViewPager.setCurrentItem(1, true);
            mViewPager.addOnPageChangeListener(mVPageListener);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mViewPager.removeOnPageChangeListener(mVPageListener);
        mVPageListener = null;
        mRoot = null;
        mViewPager = null;
        mAdapter.fragments.clear();
        mAdapter = null;
        mVideoFragment = null;
        mPhotoFragment = null;
    }

    private IGalleryCallBack mGalleryCallBack;

    /**
     * @param mGalleryCallBack
     */
    public void setCallBack(IGalleryCallBack mGalleryCallBack) {
        this.mGalleryCallBack = mGalleryCallBack;
    }


    public interface IGalleryCallBack {

        void onVideo(ImageItem item);

        void onPhoto(ImageItem item);

        void onRGCheck(boolean isVideo);
    }


    @Override
    public int getLayoutId() {
        return R.layout.fragment_gallery_layout;
    }

    private PhotoFragment mPhotoFragment;
    private VideoFragment mVideoFragment;

    private class MPageAdapter extends FragmentPagerAdapter {
        private ArrayList<Fragment> fragments = new ArrayList<Fragment>();

        public MPageAdapter(FragmentManager fm) {
            super(fm);
            fragments.add(mVideoFragment);
            fragments.add(mPhotoFragment);
        }

        public int getCount() {
            return fragments.size();
        }

        public Fragment getItem(int paramInt) {
            return fragments.get(paramInt);
        }

    }

    public interface IGallerySizeListener {

        void onGallerySizeClicked();
    }
}
