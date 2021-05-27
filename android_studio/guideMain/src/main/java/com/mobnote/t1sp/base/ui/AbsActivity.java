package com.mobnote.t1sp.base.ui;

import android.os.Bundle;

import androidx.annotation.Nullable;
import likly.mvp.BaseActivity;
import likly.mvp.Presenter;

public class AbsActivity<P extends Presenter> extends BaseActivity<P> {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onViewCreated() {

    }

}
