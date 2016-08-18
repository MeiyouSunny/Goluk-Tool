package com.goluk.crazy.panda.main.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.goluk.crazy.panda.R;
import com.zhy.magicviewpager.transformer.ScaleInTransformer;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by DELL-PC on 2016/8/18.
 */
public class FragmentSquare extends Fragment {
    private static final String TAG = "FragmentSquare";
    @BindView(R.id.viewpager_square)
    ViewPager mSquareViewpager;

    List<Fragment> mFragmentList;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_square, null);
        ButterKnife.bind(this, rootView);
        initData();
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

    private void initData() {
        mFragmentList = new ArrayList<>();
        mFragmentList.add(new FragmentSquarePager());
        mFragmentList.add(new FragmentSquarePager());
        mFragmentList.add(new FragmentSquarePager());
        mFragmentList.add(new FragmentSquarePager());
        mFragmentList.add(new FragmentSquarePager());
        mFragmentList.add(new FragmentSquarePager());
        mFragmentList.add(new FragmentSquarePager());

        mSquareViewpager.setPageMargin(86);//设置page间间距，自行根据需求设置
        mSquareViewpager.setOffscreenPageLimit(3);//>=3
        mSquareViewpager.setAdapter(new FragmentPagerAdapter(getChildFragmentManager()));

        //setPageTransformer 决定动画效果
        mSquareViewpager.setPageTransformer(true, new ScaleInTransformer(0.75f));
        mSquareViewpager.setCurrentItem(2,true);
    }

    public class FragmentPagerAdapter extends FragmentStatePagerAdapter {
        public FragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        /**
         * 每次更新完成ViewPager的内容后，调用该接口，此处复写主要是为了让导航按钮上层的覆盖层能够动态的移动
         */
        @Override
        public void finishUpdate(ViewGroup container) {
            super.finishUpdate(container);//这句话要放在最前面，否则会报错
            //获取当前的视图是位于ViewGroup的第几个位置，用来更新对应的覆盖层所在的位置
        }
    }
}

