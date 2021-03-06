package com.mobnote.t1sp.callback;

import android.text.TextUtils;

import com.mobnote.t1sp.api.Callback;
import com.mobnote.t1sp.util.Const;

import cn.com.tiros.debug.GolukDebugUtils;

/**
 * 通用请求回调接口
 */
public abstract class CommonCallback extends Callback<String> {

    @Override
    public void onResponse(String response) {
        super.onResponse(response);

        GolukDebugUtils.e(Const.LOG_TAG, "Response: " + response);

        parseResponse(response);
    }

    /**
     * 解析返回数据
     */
    private void parseResponse(String response) {
        if (TextUtils.isEmpty(response))
            return;

        if (response.contains(SPLIT_FLAG)) {
            final String[] datas = response.split(SPLIT_FLAG);
            if (datas != null && datas.length >= 2) {
                final int code = Integer.parseInt(datas[0]);
                final String message = datas[1];
                if (code == SUCCESS_CODE && TextUtils.equals(SUCCESS_MSG, message)) {
                    onSuccess();
                } else {
                    onServerError(code, message);
                }
            }
        }
    }

    @Override
    public boolean isNetworkAvailable() {
        return true;
    }
}
