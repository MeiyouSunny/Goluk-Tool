package com.rd.veuisdk.model;

import com.rd.veuisdk.model.type.EffectType;

/**
 * 自定义特效
 *
 * @author JIAN
 * @create 2018/12/28
 * @Describe
 */
public class EffectFilterInfo extends IApiInfo {
    private int coreFilterId;//注册返回的id
    private int color;//颜色遮罩

    public String getType() {
        return type;
    }

    //类型 （动感、分屏、转场 、定格、时间）
    @EffectType.Effect
    private String type;

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    private int duration; //单位：秒

    /**
     * @param name
     * @param file
     * @param cover
     * @param updatetime
     */
    public EffectFilterInfo(String name, String file, String cover, long updatetime, String type) {
        super(name, file, cover, updatetime);
        this.type = type;
    }

    public int getCoreFilterId() {
        return coreFilterId;
    }

    public void setCoreFilterId(int coreFilterId) {
        this.coreFilterId = coreFilterId;
    }


    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }


}
