package com.mobnote.t1sp.base.ui;

import android.app.Activity;
import android.support.v4.app.Fragment;

import com.mobnote.t1sp.base.control.ViewControlBinder;

import butterknife.ButterKnife;
import likly.mvp.MVP;
import likly.mvp.View;

public class BaseOnViewBindListener implements MVP.OnViewBindListener {
    @Override
    public void onViewBind(View view) {
        ViewControlBinder.bind(view);
        if (view instanceof Activity) {
            ButterKnife.bind((Activity) view);
        } else if (view instanceof Fragment) {
            ButterKnife.bind(view, ((Fragment) view).getView());
        }
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onStart() {

    }

    @Override
    public void onResume() {

    }

    @Override
    public void onPause() {

    }

    @Override
    public void onStop() {

    }

    @Override
    public void onDestroy() {

    }
}
