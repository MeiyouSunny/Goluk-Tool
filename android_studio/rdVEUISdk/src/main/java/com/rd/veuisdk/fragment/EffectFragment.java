package com.rd.veuisdk.fragment;

import android.os.Bundle;

import com.rd.veuisdk.R;


/**
 * 特效
 */
public class EffectFragment extends AbstractEffectFragment {

    /**
     * 分类加载
     *
     * @param typeUrl
     * @param url
     * @return
     */
    public static EffectFragment newInstance(String typeUrl, String url) {
        Bundle args = new Bundle();
        args.putString(PARAM_TYPE_URL, typeUrl);
        args.putString(PARAM_URL, url);
        EffectFragment fragment = new EffectFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_effect;
    }

}
