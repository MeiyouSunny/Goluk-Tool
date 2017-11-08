package com.mobnote.t1sp.base.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import likly.view.repeat.ViewHolder;

public abstract class BaseViewHolder<T> extends ViewHolder<T> {

    @Override
    public View onCreateView(ViewGroup viewGroup) {
        return LayoutInflater.from(viewGroup.getContext()).inflate(getViewHolderLayout(), viewGroup, false);
    }

    protected abstract int getViewHolderLayout();

    @Override
    protected void onViewCreated(View view) {
        super.onViewCreated(view);
        ButterKnife.bind(this, view);
    }
}
