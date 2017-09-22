package com.rd.veuisdk.mix;

import android.graphics.RectF;

import com.rd.veuisdk.R;

import java.util.ArrayList;

/**
 * 单个画中画模块（区域列表）
 * Created by JIAN on 2017/8/25.
 */

public class ModeInfo {


//    public ModeInfo(ArrayList<RectF> list, String title) {
//        this.list = list;
//        this.title = title;
//    }

    public ArrayList<RectF> getList() {
        return list;
    }

    public void setList(ArrayList<RectF> list) {
        this.list = list;
    }


    private ArrayList<RectF> list = new ArrayList<RectF>();

    public ArrayList<RectF> getNoBorderLineList() {
        return noBorderLineList;
    }

    public void setNoBorderLineList(ArrayList<RectF> noBorderLineList) {
        this.noBorderLineList = noBorderLineList;
    }

    private ArrayList<RectF> noBorderLineList=new ArrayList<RectF>();


    public int getResId() {
        return resId;
    }

    public void setResId(int resId) {
        this.resId = resId;
    }

    private  int resId= R.drawable.mix_icon_1;


    public ModeInfo(ArrayList<RectF> list, ArrayList<RectF> noBorderLineList, int resId, String assetbg) {
        this.list = list;
        this.noBorderLineList = noBorderLineList;
        this.resId = resId;
        this.assetbg = assetbg;
    }

    public String getAssetBg() {
        return assetbg;
    }

    public void setAssetBg(String assetbg) {
        this.assetbg = assetbg;
    }

    private String assetbg = "";


}
