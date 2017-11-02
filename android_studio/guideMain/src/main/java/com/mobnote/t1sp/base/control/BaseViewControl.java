package com.mobnote.t1sp.base.control;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;

public class BaseViewControl implements ViewControl {
    private Class holder;
    private Object view;
    private Activity mActivity;

    @Override
    public void onBindView(Object view) {
        this.view = view;
        holder = view.getClass();
        if (view instanceof Activity) {
            mActivity = (Activity) view;
            View rootView = ((ViewGroup) mActivity.findViewById(android.R.id.content)).getChildAt(0);
            ButterKnife.bind(this, rootView);
        } else if (view instanceof Fragment) {
            mActivity = ((Fragment) view).getActivity();
            ButterKnife.bind(this, ((Fragment) view).getView());
        }
    }

    @Override
    public void onViewBind() {

    }

    protected Class getHolder() {
        return holder;
    }

    protected Activity getActivity() {
        return mActivity;
    }
}
