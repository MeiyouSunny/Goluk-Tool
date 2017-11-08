package com.mobnote.t1sp.base.ui;

import com.mobnote.t1sp.base.control.BackViewControl;
import com.mobnote.t1sp.base.control.BindViewControl;
import com.mobnote.t1sp.base.control.TitleViewControl;

import likly.mvp.BaseFragment;
import likly.mvp.Presenter;

@BindViewControl({
        BackViewControl.class,
        TitleViewControl.class
})
public class BackTitleFragment<P extends Presenter> extends BaseFragment<P> implements ITitleView {

    @Override
    public int initTitle() {
        return -1;
    }
}
