package com.rd.veuisdk.fragment;

import android.os.Bundle;
import android.text.TextUtils;

import com.rd.veuisdk.R;

/**
 * 网络滤镜
 */
public class FilterFragmentLookup extends AbstractFilterFragmentLookup {


    /**
     * @param _url
     * @return
     */
    public static FilterFragmentLookup newInstance(String _url) {
        FilterFragmentLookup lookup = new FilterFragmentLookup();
        Bundle bundle = new Bundle();
        String url = TextUtils.isEmpty(_url) ? "" : _url.trim();
        bundle.putString(PARAM_URL, url);
        lookup.setArguments(bundle);
        return lookup;
    }


    @Override
    protected int getLayoutId() {
        return R.layout.fragment_lookup_base_layout;
    }
}
