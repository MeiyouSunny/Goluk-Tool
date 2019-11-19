package com.mobnote.t1sp.base.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mobnote.golukmain.R2;
import com.mobnote.t1sp.base.control.BackViewControl;
import com.mobnote.t1sp.base.control.BindViewControl;
import com.mobnote.t1sp.base.control.TitleViewControl;
import com.mobnote.t1sp.base.dialog.LoadingDialogHolder;

import butterknife.BindView;
import likly.dialogger.Dialogger;
import likly.mvp.Presenter;

public class BackTitleActivity<P extends Presenter> extends AbsActivity<P> implements LoadingView, ITitleView {
    private Dialogger mLoadingDialogger;

    @BindView(R2.id.title)
    TextView mTitle;
    @BindView(R2.id.back)
    ImageView mBack;

    public void setTitle(@StringRes int title) {
        mTitle.setText(title);
    }

    @Override
    public void showLoadingDialog() {
        if (mLoadingDialogger == null) {
            mLoadingDialogger = Dialogger.newDialog(getContext())
                    .holder(new LoadingDialogHolder())
                    .gravity(Gravity.CENTER)
                    .background(android.R.color.transparent)
                    .cancelable(true);
        }
        mLoadingDialogger.show();
    }

    @Override
    public void hideLoadingDialog() {
        if (mLoadingDialogger != null) {
            mLoadingDialogger.dismiss();
        }
    }

    @Override
    public int initTitle() {
        return -1;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

}
