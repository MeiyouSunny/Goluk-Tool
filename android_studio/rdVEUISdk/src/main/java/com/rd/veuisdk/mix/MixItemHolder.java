package com.rd.veuisdk.mix;

import android.widget.ImageView;

/**
 * 每一块画中画中的按钮组件
 * Created by JIAN on 2017/8/30.
 */

public class MixItemHolder {

    public MixItemHolder() {
    }

    public int getMixId() {
        return mixId;
    }

    public void setMixId(int mixId) {
        this.mixId = mixId;
    }

    public ImageView getBtnAdd() {
        return btnAdd;
    }

    public void setBtnAdd(ImageView btnAdd) {
        this.btnAdd = btnAdd;
    }

    public ImageView getBtnGallery() {
        return btnGallery;
    }

    public void setBtnGallery(ImageView btnGallery) {
        this.btnGallery = btnGallery;
    }


    public ImageView getBtnFullScreen() {
        return btnFullScreen;
    }

    public void setBtnFullScreen(ImageView btnFullScreen) {
        this.btnFullScreen = btnFullScreen;
    }

    //通过mixId关联view
    private int mixId;
    private ImageView btnAdd; //编辑和新增公用一个组件
    private ImageView btnGallery; //图库和删除公用一个组件
    private ImageView btnFullScreen;//全屏按钮



    public MixInfo getBindMix() {
        return iBindMix;
    }

    public void setBindMix(MixInfo iBindMix) {
        this.iBindMix = iBindMix;
    }

    private MixInfo iBindMix;


}
