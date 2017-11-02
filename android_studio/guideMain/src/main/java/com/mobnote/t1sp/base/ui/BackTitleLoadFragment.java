package com.mobnote.t1sp.base.ui;

import com.mobnote.t1sp.base.control.BackViewControl;
import com.mobnote.t1sp.base.control.BindViewControl;
import com.mobnote.t1sp.base.control.TitleViewControl;

import likly.mvp.Presenter;

@BindViewControl({
        BackViewControl.class,
        TitleViewControl.class
})
public class BackTitleLoadFragment<P extends Presenter> extends LoadFragment<P> implements LoadingView, ITitleView {
    @Override
    public int initTitle() {
        return -1;
    }
}
