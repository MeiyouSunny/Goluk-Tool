package com.rd.veuisdk.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import com.rd.veuisdk.R;
import com.rd.veuisdk.ui.ExtViewPager;

import java.util.ArrayList;

/**
 * 本地音乐
 */
public class LocalFragment extends BaseV4Fragment {

    private View mRoot;
    private ExtViewPager mViewPager;
    private RadioGroup mRgMusicGroup;
    private MyPageAdapter mMyPageAdapter;

    private ArrayList<Fragment> mFragments = new ArrayList<>();
    private LocalMusicFragment mLocalMusicFragment;
    private LocalVideoMusicFragment mLocalVideoMusicFragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.rdveuisdk_local_layout, container, false);
        initView();
        init();
        return mRoot;
    }

    private void initView() {
        mViewPager = mRoot.findViewById(R.id.vpMusicMain);
        mRgMusicGroup = mRoot.findViewById(R.id.rgMusicGroup);

        mRgMusicGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rbLocalMusic) {
                    setchecked(0);
                } else if (checkedId == R.id.rbLocalVideo) {
                    setchecked(1);
                }
            }
        });
    }

    private void init() {
        mLocalMusicFragment = new LocalMusicFragment();
        mLocalVideoMusicFragment = new LocalVideoMusicFragment();
        mFragments.add(mLocalMusicFragment);
        mFragments.add(mLocalVideoMusicFragment);
        mMyPageAdapter = new MyPageAdapter(this.getChildFragmentManager(), mFragments);
        mViewPager.setAdapter(mMyPageAdapter);
        mViewPager.setOffscreenPageLimit(2);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            public void onPageScrollStateChanged(int paramAnonymousInt) {
            }

            public void onPageScrolled(int paramAnonymousInt1,
                                       float paramAnonymousFloat, int paramAnonymousInt2) {

            }

            public void onPageSelected(int arg0) {
                if (arg0 == 0) {
                    mRgMusicGroup.check(R.id.rbLocalMusic);
                } else if (arg0 == 1) {
                    mRgMusicGroup.check(R.id.rbLocalVideo);
                }
            }
        });

//        mRgMusicGroup.check(R.id.rbLocalMusic);
    }

    private void setchecked(int paramInt) {
        if (mViewPager.getCurrentItem() != paramInt) {
            mViewPager.setCurrentItem(paramInt, true);
        }
    }

    private class MyPageAdapter extends FragmentPagerAdapter {

        private ArrayList<Fragment> fragments = new ArrayList<Fragment>();

        public MyPageAdapter(FragmentManager fm, ArrayList<Fragment> list) {
            super(fm);
            this.fragments = list;
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }
    }

}
