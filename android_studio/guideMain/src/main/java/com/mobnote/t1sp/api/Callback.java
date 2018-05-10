package com.mobnote.t1sp.api;

import likly.reverse.ResponseException;

public abstract class Callback<T> implements likly.reverse.Callback<T> {

    public static final int SUCCESS_CODE = 0;
    public static final String SUCCESS_MSG = "OK";
    public static final String SPLIT_FLAG = "\n";

    @Override
    public void onStart() {
    }

    @Override
    public void onCancel() {
    }

    @Override
    public void onFinish() {
    }

    @Override
    public void onResponse(T response) {
    }

    @Override
    public void onError(Throwable throwable) {
        if (throwable instanceof ResponseException) {
            ResponseException exception = (ResponseException) throwable;
            onServerError(exception.code(), exception.message());
        } else {
            onServerError(-1, throwable.getMessage());
        }
    }

    /**
     * 执行成功返回
     */
    protected abstract void onSuccess();

    /**
     * 执行失败返回
     *
     * @param errorCode    错误码
     * @param errorMessage 错误描述
     */
    protected abstract void onServerError(int errorCode, String errorMessage);
}
