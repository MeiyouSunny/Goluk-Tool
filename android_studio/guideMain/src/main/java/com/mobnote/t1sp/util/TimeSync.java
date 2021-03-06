package com.mobnote.t1sp.util;

import com.mobnote.golukmain.carrecorder.util.SettingUtils;
import com.mobnote.util.DateTimeUtils;

import goluk.com.t1s.api.callback.CallbackCmd;
import likly.dollar.$;

public class TimeSync {

    public void syncTime() {
        boolean syncIsOpen = SettingUtils.getInstance().getBoolean("systemtime", true);
        if (!syncIsOpen)
            return;

        long nowMill = System.currentTimeMillis();
        final String nowDate = DateTimeUtils.getTimeDateString(nowMill);
        final String nowTime = DateTimeUtils.getTimeHourString(nowMill);

        goluk.com.t1s.api.ApiUtil.setDate(nowDate, new CallbackCmd() {
            @Override
            public void onSuccess(int i) {
                goluk.com.t1s.api.ApiUtil.setTime(nowTime, new CallbackCmd() {
                    @Override
                    public void onSuccess(int i) {
                        $.toast().text("时间同步成功: " + nowDate + " " + nowTime).show();
                    }

                    @Override
                    public void onFail(int i, int i1) {
                    }
                });
            }

            @Override
            public void onFail(int i, int i1) {
            }
        });
    }

}
