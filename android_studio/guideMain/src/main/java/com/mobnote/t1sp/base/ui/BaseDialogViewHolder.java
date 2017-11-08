package com.mobnote.t1sp.base.ui;


import android.content.Context;
import android.view.View;

import butterknife.ButterKnife;
import likly.dialogger.ViewHolder;

public class BaseDialogViewHolder extends ViewHolder {

    private Context mContext;


    public BaseDialogViewHolder(int layoutRes) {
        super(layoutRes);
    }

    public BaseDialogViewHolder(View view) {
        super(view);
    }

    @Override
    public void onViewCreated(View view) {
        super.onViewCreated(view);
        mContext = view.getContext();
        ButterKnife.bind(this, view);
    }

    protected Context getContext() {
        return mContext;
    }
}
