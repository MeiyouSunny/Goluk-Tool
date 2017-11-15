package com.mobnote.t1sp.ui.setting;

import android.content.Context;
import android.support.annotation.ArrayRes;

import com.mobnote.t1sp.bean.SettingValue;

import java.util.ArrayList;

import likly.mvp.Presenter;

public interface DeviceSettingsPresenter extends Presenter<DeviceSettingsModel, DeviceSettingsView> {

    /**
     * 进入/退出设置模式
     */
    void enterOrExitSettingMode(boolean isEnter);

    /**
     * 获取所有信息
     */
    void getAllInfo();

    //////

    /**
     * 设置开机声音信息
     */
    void setPowerSound(boolean onOff);

    /**
     * 设置抓拍声音信息
     */
    void setCaptureSound(boolean onOff);

    /**
     * 设置自动旋转信息
     */
    void setAutoRotate(boolean onOff);

    /**
     * 设置视频水印信息
     */
    void setRecStamp(boolean onOff);

    /**
     * 设置停车安防信息
     */
    void setParkGuard(boolean onOff);

    /**
     * 设置移动侦测信息
     */
    void setMTD(boolean onOff);

    /**
     * 设置录像
     */
    void setSoundRecord(boolean onOff);

//                case REQUEST_CODE_VIDEO_RES:
//
//            break;
//            case REQUEST_CODE_SNAP_TIME:
//
//            break;
//            case REQUEST_CODE_GSENSOR:
//
//            break;
//            case REQUEST_CODE_PARKING_GUARD:
//
//            break;
//            case REQUEST_CODE_MTD:
//
//            break;
//            case REQUEST_CODE_POWER_OFF_DELAY:
//
//            break;

    /**
     * 设置选项类型参数设置项
     *
     * @param type  类型
     * @param value 值
     */
    void setSelectionSettingValue(int type, String value);

    /**
     * 恢复出厂设置
     */
    void resetFactory();

    //////

    /**
     * 生成对应的设置选项列表
     *
     * @param context      Context
     * @param labels       描述Array
     * @param values       值Array
     * @param currentValue 当前值
     * @return ArrayList<SettingValue>
     */
    ArrayList<SettingValue> generateSettingValues(Context context, @ArrayRes int labels, @ArrayRes int values, String currentValue);

    String getSettingLabelByValue(Context context, @ArrayRes int labels, @ArrayRes int values, String value);

}
