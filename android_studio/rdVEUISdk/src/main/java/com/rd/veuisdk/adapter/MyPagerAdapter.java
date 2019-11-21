package com.rd.veuisdk.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;

/**
 * veiwPage+fragment适配器
 */
public class MyPagerAdapter extends FragmentPagerAdapter {

    private String[] titles;
    private ArrayList<Fragment> fragments;

    public MyPagerAdapter(FragmentManager fm, String[] titles, ArrayList<Fragment> fragments) {
        super(fm);
        this.titles = titles;
        this.fragments = fragments;
    }

    //返回标题
    @Override
    public CharSequence getPageTitle(int position) {
        return titles[position];
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return titles.length;
    }
}