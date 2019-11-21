package com.rd.veuisdk.model.bean;

import android.support.annotation.Keep;

import java.io.Serializable;

/**
 * 单个分类
 * @create 2019/7/4
 */
@Keep
public class TypeBean implements Serializable {
    /**
     * id : 69
     * name : 动感
     * appkey : 6ecb39f1c12f1a35
     * type : specialeffects
     * updatetime : 1559632289
     */

    private String id;
    private String name;
    private String appkey;
    private String type;
    private String updatetime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUpdatetime() {
        return updatetime;
    }

    public void setUpdatetime(String updatetime) {
        this.updatetime = updatetime;
    }
}
