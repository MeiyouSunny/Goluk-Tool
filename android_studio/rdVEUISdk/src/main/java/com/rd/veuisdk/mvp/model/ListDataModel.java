package com.rd.veuisdk.mvp.model;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.rd.lib.utils.ThreadPoolUtils;
import com.rd.veuisdk.model.bean.AppData;
import com.rd.veuisdk.utils.ModeDataUtils;

import java.util.List;

/**
 * 素材管理-列表数据（单个分类下的数据）
 *
 * @create 2019/7/4
 */
public class ListDataModel extends BaseModel {

    public ListDataModel(@NonNull ICallBack callBack) {
        super(callBack);
    }

    /**
     * 获取单个分类下的数据
     *
     * @param type   分类
     * @param typeId 单个分类的id
     */
    public void getList(final String url, @ModeDataUtils.ResourceType final String type, final String typeId) {
        ThreadPoolUtils.execute(new ThreadPoolUtils.ThreadPoolRunnable() {
            List list = null;
            @Override
            public void onBackground() {
                String data = ModeDataUtils.getModeData(url, type, typeId);
                if (!TextUtils.isEmpty(data)) {
                    AppData aeData = JSON.parseObject(data, AppData.class);
//                    AEData<DataBean> aeData = JSON.parseObject(data, new TypeReference<AEData<DataBean>>() {
//                    });
                    if (null != aeData && aeData.getData() != null) {
                        list = aeData.getData();
                    }
                }
            }

            @Override
            public void onEnd() {
                super.onEnd();
                if (null != list) {
                    onSuccess(list);
                } else {
                    onFailed();
                }
            }
        });
    }
}
