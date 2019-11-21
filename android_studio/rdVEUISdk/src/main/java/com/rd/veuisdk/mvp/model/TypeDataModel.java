package com.rd.veuisdk.mvp.model;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.rd.lib.utils.ThreadPoolUtils;
import com.rd.veuisdk.model.bean.TypeData;
import com.rd.veuisdk.utils.ModeDataUtils;

/**
 * 素材管理-分类数据
 *
 * @create 2019/7/4
 */
public class TypeDataModel extends BaseModel {

    public TypeDataModel(@NonNull ICallBack callBack) {
        super(callBack);
    }

    /**
     *
     * 获取单个类别下的分类
     * @param url
     * @param type 单个类别 （ae模板、特效）
     */
    public void getTypeList(final String url, final @ModeDataUtils.ResourceType String type) {
        ThreadPoolUtils.execute(new ThreadPoolUtils.ThreadPoolRunnable() {
            TypeData typeData = null;

            @Override
            public void onBackground() {
                String data = ModeDataUtils.getTypeData(url, type);
                if (!TextUtils.isEmpty(data)) {
                    try {
                        typeData = JSON.parseObject(data, TypeData.class);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onEnd() {
                super.onEnd();
                if (null != typeData) {
                    onSuccess(typeData.getData());
                } else {
                    onFailed();
                }
            }
        });
    }
}
