package com.rd.veuisdk.mvp.model;

import android.content.Context;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.rd.lib.utils.ThreadPoolUtils;
import com.rd.veuisdk.model.EffectFilterInfo;
import com.rd.veuisdk.model.EffectTypeDataInfo;
import com.rd.veuisdk.model.IApiInfo;
import com.rd.veuisdk.model.bean.AppData;
import com.rd.veuisdk.model.bean.DataBean;
import com.rd.veuisdk.model.bean.TypeBean;
import com.rd.veuisdk.model.bean.TypeData;
import com.rd.veuisdk.utils.ModeDataUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 特效数据
 *
 * @create 2019/6/19
 */
public class EffectFragmentModel<E extends IApiInfo> extends BaseModel {

    public EffectFragmentModel(Context context, ICallBack callBack) {
        super(callBack);
    }

    public void loadData(final String typeUrl, final String url) {
        ThreadPoolUtils.execute(new ThreadPoolUtils.ThreadPoolRunnable() {
            List<EffectTypeDataInfo> list = new ArrayList<>();

            @Override

            public void onBackground() {
                String data = ModeDataUtils.getTypeData(typeUrl, ModeDataUtils.TYPE_SPECIAL_EFFECT);
                if (!TextUtils.isEmpty(data)) {
                    TypeData typeData = JSON.parseObject(data, TypeData.class);
                    int len = typeData.getData().size();
                    for (int i = 0; i < len; i++) {
                        getItemData(typeData.getData().get(i));
                    }
                }
            }

            /**
             * 按照分类id获取
             * @param typeBean 单个分类
             */
            private void getItemData(TypeBean typeBean) {
                if (null != typeBean) {
                    AppData appData = ModeDataUtils.getEffectAppData(url, ModeDataUtils.TYPE_SPECIAL_EFFECT, typeBean.getId());
                    if (null != appData) {
                        EffectTypeDataInfo info = new EffectTypeDataInfo<EffectFilterInfo>(typeBean);
                        info.setList(getChild(appData.getData(), typeBean));
                        if (info.getList() != null && info.getList().size() > 0) {
                            list.add(info);
                        }
                    }
                }
            }

            @Override
            public void onEnd() {
                super.onEnd();
                if (list.size() > 0) {
                    onSuccess(list);
                } else {
                    onFailed();
                }
            }
        });

    }

    private List<EffectFilterInfo> getChild(List<DataBean> list, TypeBean bean) {
        List<EffectFilterInfo> tmp = new ArrayList<>();
        int len = list.size();
        boolean isZhuanChange = Integer.toString(71).equals(bean.getId());
        for (int i = 0; i < len; i++) {
            DataBean dataBean = list.get(i);
            String name = dataBean.getName();
            String file = dataBean.getFile();
            String cover = dataBean.getCover();
            long updatetime = dataBean.getUpdatetime();
            EffectFilterInfo filterInfo = new EffectFilterInfo(name, file, cover, updatetime, bean.getName());
            if (isZhuanChange) {
                filterInfo.setDuration(1);
            }
            tmp.add(filterInfo);
        }
        return tmp;

    }


    /**
     * 获取指定类型的数据
     */
    public EffectTypeDataInfo getChild(List<EffectTypeDataInfo<E>> list, int typeId) {
        EffectTypeDataInfo tmp = null;
        int len = list.size();
        EffectTypeDataInfo info;
        String tmpId = Integer.toString(typeId);
        for (int i = 0; i < len; i++) {
            info = list.get(i);
            if (info.getType().getId().equals(tmpId)) {
                tmp = info;
                break;
            }
        }
        return tmp;
    }

    /**
     * 从列表查询指定的特效
     */
    public EffectFilterInfo getDBItem(List<EffectFilterInfo> list, String url) {
        EffectFilterInfo dst = null;
        if (null != list) {
            int len = list.size();
            for (int i = 0; i < len; i++) {
                EffectFilterInfo info = list.get(i);
                if (info.getFile().equals(url)) {
                    dst = info;
                    break;
                }
            }
        }
        return dst;
    }


}
