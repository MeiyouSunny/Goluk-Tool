package com.mobnote.t1sp.base.control;

import android.widget.TextView;

import com.mobnote.golukmain.R2;
import com.mobnote.t1sp.base.ui.ITitleView;

import butterknife.BindView;

public class TitleViewControl extends BaseViewControl {

    @BindView(R2.id.title)
    TextView mTitle;

    @Override
    public void onBindView(Object view) {
        super.onBindView(view);

        BindTitle bindTitle = (BindTitle) getHolder().getAnnotation(BindTitle.class);
        if (bindTitle != null) {

            if (view instanceof ITitleView) {
                int title = ((ITitleView) view).initTitle();
                if (title != -1) {
                    mTitle.setText(title);
                }
            }
            mTitle.setText(bindTitle.value());
        }

    }
}
