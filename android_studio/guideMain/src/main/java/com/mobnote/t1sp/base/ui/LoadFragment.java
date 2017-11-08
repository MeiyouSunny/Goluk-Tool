package com.mobnote.t1sp.base.ui;

import android.view.Gravity;

import com.mobnote.t1sp.base.dialog.LoadingDialogHolder;

import likly.dialogger.Dialogger;
import likly.mvp.BaseFragment;
import likly.mvp.Presenter;

public class LoadFragment<P extends Presenter> extends BaseFragment<P> implements LoadingView {

    private Dialogger mLoadingDialogger;

    @Override
    public void showLoadingDialog() {
        if (mLoadingDialogger == null) {
            mLoadingDialogger = Dialogger.newDialog(getActivity())
                    .holder(new LoadingDialogHolder())
                    .gravity(Gravity.CENTER)
                    .background(android.R.color.transparent)
                    .cancelable(false);
        }
        mLoadingDialogger.show();
    }

    @Override
    public void hideLoadingDialog() {
        if (mLoadingDialogger != null) {
            mLoadingDialogger.dismiss();
        }
    }

}
