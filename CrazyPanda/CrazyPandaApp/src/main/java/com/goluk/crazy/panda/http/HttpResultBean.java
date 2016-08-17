package com.goluk.crazy.panda.http;

/**
 * Created by leege100 on 2016/8/16.
 */
public class HttpResultBean<T> {

    private int code;
    private T data;
    private String msg;
    private int xieyi;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getXieyi() {
        return xieyi;
    }

    public void setXieyi(int xieyi) {
        this.xieyi = xieyi;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("code=" + code + " msg=" + msg + " xieyi=" + xieyi);
        if (null != data) {
            sb.append(" data:" + data.toString());
        }
        return sb.toString();
    }
}
