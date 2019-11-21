package com.rd.veuisdk;

import com.rd.vecore.models.Transition;

import java.util.ArrayList;

/**
 * 片段编辑接口
 *
 */
public interface IEditPreviewHandler  {

    /**
     *  转场时长变化
     * @param duration
     * @param isApplyToAll
     */
    void onTransitionDurationChanged(float duration,boolean isApplyToAll);

    /**
     *   转场切换
     * @param listTransition 转场列表
     * @param isApplyToAll 是否应用到所有
     */
    void onTransitionChanged(ArrayList<Transition> listTransition,boolean isApplyToAll);
    /**
     *  返回
     */
    void onBack();

    /**
     *  确认
     */
    void onSure();
}
