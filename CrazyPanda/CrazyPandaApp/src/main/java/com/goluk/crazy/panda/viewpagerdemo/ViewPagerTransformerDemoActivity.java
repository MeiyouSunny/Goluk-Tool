package com.goluk.crazy.panda.viewpagerdemo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;

import com.goluk.crazy.panda.R;
import com.zhy.magicviewpager.transformer.ScaleInTransformer;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by leege100 on 2016/8/18.
 */
public class ViewPagerTransformerDemoActivity extends AppCompatActivity {

    @BindView(R.id.viewpager)
    ViewPager mViewpager;

    List<Fragment> mFragmentList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewpager_transformer_demo);
        ButterKnife.bind(this);

        mFragmentList = new ArrayList<>();
        mFragmentList.add(new FragmentPager());
        mFragmentList.add(new FragmentPager());
        mFragmentList.add(new FragmentPager());
        mFragmentList.add(new FragmentPager());
        mFragmentList.add(new FragmentPager());
        mFragmentList.add(new FragmentPager());
        mFragmentList.add(new FragmentPager());

        mViewpager.setPageMargin(30);//设置page间间距，自行根据需求设置
        mViewpager.setOffscreenPageLimit(3);//>=3
        mViewpager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()));

        //setPageTransformer 决定动画效果
        mViewpager.setPageTransformer(true, new ScaleInTransformer(0.85f));
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
