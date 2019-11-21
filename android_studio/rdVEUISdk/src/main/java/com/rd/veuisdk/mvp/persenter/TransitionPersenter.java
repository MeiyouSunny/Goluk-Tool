package com.rd.veuisdk.mvp.persenter;


import android.content.Context;

import com.rd.veuisdk.R;
import com.rd.veuisdk.database.TransitionData;
import com.rd.veuisdk.model.TransitionInfo;
import com.rd.veuisdk.mvp.model.ITransitionModel;
import com.rd.veuisdk.mvp.model.TransitionModel;
import com.rd.veuisdk.mvp.view.ITransitionView;

import java.util.List;


/**
 * @author JIAN
 * @create 2019/2/22
 * @Describe
 */
public class TransitionPersenter<E> extends BasePresenter<ITransitionView> {
    private ITransitionModel model;

    public TransitionPersenter(Context context) {
        model = new TransitionModel(context);
    }

    public void initData(String typeUrl, final String url) {
        //View是否绑定 如果没有绑定，就不执行网络请求
        if (!isViewAttached()) {
            return;
        }
        mViewRef.get().showLoading();
        model.initData(typeUrl, url, new ITransitionModel.ICallBack<E>() {
            @Override
            public void onSuccess(List<E> list) {
                if (!isViewAttached()) {
                    return;
                }
                mViewRef.get().onSuccess(list);
            }
        });

    }


    public boolean isWebTansition() {
        return model.isWebTansition();
    }


    public void downTransition(Context context, int itemId, final TransitionInfo info) {
        if (!isViewAttached()) {
            return;
        }
        model.downTransition(context, itemId, info, new ITransitionModel.IDownCallBack() {
            @Override
            public void downFailed(int itemId, int strId) {
                if (!isViewAttached()) {
                    return;
                }
                mViewRef.get().downFailed(itemId, R.string.download_failed);
            }

            @Override
            public void downSuccessed(int itemId, TransitionInfo info) {
                if (!isViewAttached()) {
                    return;
                }
                TransitionData.getInstance().replace(info);
                mViewRef.get().downSuccessed(itemId, info);
            }
        });
    }

    public void recycle() {
        if (!isViewAttached()) {
            return;
        }
        model.onCancel();
    }


}
