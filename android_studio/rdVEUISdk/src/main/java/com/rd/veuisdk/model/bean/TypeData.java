package com.rd.veuisdk.model.bean;

import android.support.annotation.Keep;

import java.util.List;

/**
 * 分类
 *
 * @create 2019/6/19
 */
@Keep
public class TypeData {


    /**
     * code : 0
     * msg : ok
     * data : [{"id":"69","name":"动感","appkey":"6ecb39f1c12f1a35","type":"specialeffects","updatetime":"1559632289"},{"id":"70","name":"分屏","appkey":"6ecb39f1c12f1a35","type":"specialeffects","updatetime":"1559632289"},{"id":"71","name":"转场","appkey":"6ecb39f1c12f1a35","type":"specialeffects","updatetime":"1559632289"}]
     */

    private int code;
    private String msg;
    private List<TypeBean> data;

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

    public List<TypeBean> getData() {
        return data;
    }

    public void setData(List<TypeBean> data) {
        this.data = data;
    }


}
