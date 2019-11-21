package com.rd.veuisdk.model.bean;

import android.support.annotation.Keep;

import java.util.List;

/**
 * 素材管理接口  appdata/ 返回的数据
 */
@Keep
public class AppData {

    private int code;
    private String msg;
    private List<DataBean> data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public List<DataBean> getData() {
        return data;
    }

    public void setData(List<DataBean> data) {
        this.data = data;
    }


}
