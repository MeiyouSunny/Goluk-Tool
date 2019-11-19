package com.mobnote.t1sp.base.control;

import android.view.View;

import com.mobnote.golukmain.R2;

import butterknife.OnClick;

public class BackViewControl extends BaseViewControl {

    @OnClick(R2.id.back)
    void onBackClick(View view) {
        getActivity().onBackPressed();
    }
}
