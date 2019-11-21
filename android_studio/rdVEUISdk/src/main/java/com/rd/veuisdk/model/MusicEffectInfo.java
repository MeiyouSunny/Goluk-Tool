package com.rd.veuisdk.model;

/**
 * 音效
 *
 * @author JIAN
 * @create 2019/1/14
 * @Describe
 */
public class MusicEffectInfo {

    public MusicEffectInfo(String text, int resId, int typeId) {
        this.text = text;
        this.resId = resId;
        this.typeId = typeId;
    }

    private String text; //标题
    private int resId; //封面

    public int getTypeId() {
        return typeId;
    }

    private int typeId;// core type id

    public String getText() {
        return text;
    }

    public int getResId() {
        return resId;
    }


}
