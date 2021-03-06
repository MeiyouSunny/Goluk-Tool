package com.mobnote.t1sp.ui.setting;

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

    /**
     * 获取SD容量信息
     */
    void getSDCardInfo();

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
     * 设置紧急视频声音开关
     */
    void setEmgVideoSound(boolean onOff);

    /**
     * 设置休眠模式开关
     */
    void setSleepMode(boolean onOff);

    /**
     * 设置安防模式开关
     */
    void setPKMode(boolean onOff);

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

}
