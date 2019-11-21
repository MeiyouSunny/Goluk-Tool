package com.rd.veuisdk.model.type;

import android.support.annotation.Keep;
import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 特效分组
 *
 * @create 2019/9/12
 */
@Keep
public class EffectType {
    public static final String DONGGAN = "动感";
    public static final String FENPING = "分屏";
    public static final String ZHUANCHANG = "转场";
    public static final String DINGGE = "定格";
    public static final String TIME = "时间";

    @Keep
    @Retention(RetentionPolicy.SOURCE)
    @StringDef({DONGGAN, FENPING, ZHUANCHANG, DINGGE, TIME})
    public @interface Effect {

    }


}
