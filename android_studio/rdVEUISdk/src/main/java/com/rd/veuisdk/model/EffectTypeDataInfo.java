package com.rd.veuisdk.model;

import com.rd.veuisdk.model.bean.TypeBean;

import java.util.List;

/**
 * 当个特效分类下绑定的数据
 */
public class EffectTypeDataInfo<E extends IApiInfo> {


    public EffectTypeDataInfo(TypeBean type) {
        mType = type;
    }

    public TypeBean getType() {
        return mType;
    }

    private TypeBean mType;


    public List<E> getList() {
        return mList;
    }


    public void setList(List<E> list) {
        mList = list;
    }

    private List<E> mList;


}
