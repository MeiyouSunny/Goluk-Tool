package com.rd.veuisdk.utils;

/**
 * @author JIAN
 * @create 2018/12/6
 * @Describe
 */
public interface IMediaFilter {

    IMediaParamImp getFilterConfig();

    void onStartTrackingTouch(int textId, float filterValue);

    void onProgressChanged(float value);

    void onStopTrackingTouch( );



}
