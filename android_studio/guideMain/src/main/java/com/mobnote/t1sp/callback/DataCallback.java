package com.mobnote.t1sp.callback;

import android.text.TextUtils;

/**
 * 需要解析返回数据的Callback
 */
public abstract class DataCallback extends CommonCallback {

    @Override
    public void onResponse(String response) {
        super.onResponse(response);

        parseResponse(response);
    }

    /**
     * 解析返回的请求数据
     */
    private void parseResponse(String response) {
        if (TextUtils.isEmpty(response))
            return;

        if (response.contains(SPLIT_FLAG)) {
            String[] datas = response.split(SPLIT_FLAG);
            if (datas != null && datas.length >= 2) {
                final int code = Integer.parseInt(datas[0]);
                final String message = datas[1];
                if (code == SUCCESS_CODE && TextUtils.equals(SUCCESS_MSG, message)) {
                    if (datas.length >= 3) {
                        final String data = datas[2];
                        parseData(datas);
                    }
                } else {
                    onServerError(code, message);
                }
            }
        }
    }

    @Override
    protected void onSuccess() {
        // Do nothing
    }

    /**
     * 解析返回数据
     *
     * @param datas
     */
    protected abstract void parseData(String[] datas);

}
