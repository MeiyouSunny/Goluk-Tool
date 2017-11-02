package com.mobnote.t1sp.base.ui;

import android.content.Context;
import android.view.View;

import butterknife.ButterKnife;

public abstract class BaseWidgetHolder {
    protected final View mView;

    public BaseWidgetHolder(View view) {
        mView = view;
        ButterKnife.bind(this, view);
        onViewCreated();
    }

    protected void onViewCreated() {

    }

    protected Context getContext() {
        return mView.getContext();
    }


}
