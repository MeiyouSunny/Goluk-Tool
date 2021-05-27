package com.rd.veuisdk.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import android.view.View;
import android.widget.TextView;

import com.rd.veuisdk.R;

/**
 * @author JIAN
 * @create 2019/1/22
 * @Describe
 */
abstract class RBaseFragment extends BaseFragment {

    protected TextView tvTitle;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //基础菜单项
        tvTitle = $(R.id.tvTitle);
        tvTitle.setText(mPageName);
        $(R.id.btnLeft).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLeftClick();
            }
        });
        $(R.id.btnRight).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRightClick();
            }
        });
    }

    abstract void onLeftClick();

    abstract void onRightClick();


    /**
     * @param containerViewId
     * @param fragment
     */
    void changeFragment(int containerViewId, Fragment fragment) {
        if (containerViewId != 0 && null != fragment) {
            FragmentTransaction ft = getChildFragmentManager().beginTransaction();
            ft.replace(containerViewId, fragment);
            ft.commit();
        }
    }

    /**
     * @param fragment
     */
    void removeFragment(Fragment fragment) {
        if (null != fragment) {
            FragmentTransaction ft = getChildFragmentManager().beginTransaction();
            ft.remove(fragment);
            ft.commit();
        }
    }


}
