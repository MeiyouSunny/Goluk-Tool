package com.rd.veuisdk.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.rd.veuisdk.R;
import com.rd.veuisdk.fragment.helper.FilterLookupLocalHandler;

/**
 * 本地lookup滤镜
 *
 * @author JIAN
 * @create 2018/11/30
 * @Describe
 */
public class FilterFragmentLookupLocal extends FilterFragmentLookupBase {


    private FilterLookupLocalHandler mLocalHandler;

    public static FilterFragmentLookupLocal newInstance() {
        return new FilterFragmentLookupLocal();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mLocalHandler = new FilterLookupLocalHandler(context);

    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_lookup_base_layout;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        $(R.id.strengthLayout).setVisibility(View.VISIBLE);
        mAdapter.addAll(true, mLocalHandler.getArrayList(), mLastPageIndex);
        mAdapter.notifyDataSetChanged();
    }


    @Override
    public void onSelectedImp(int nItemId) {
        mLastPageIndex = nItemId;
        if (nItemId >= 1) {
            if (lastItemId != nItemId) {
                switchFliter(nItemId);
                lastItemId = nItemId;
                mAdapter.onItemChecked(nItemId);
            }
        } else {
            lastItemId = nItemId;
            switchFliter(nItemId);
            mAdapter.onItemChecked(lastItemId);
        }
        if (!mIFilterHandler.isPlaying()) {
            mIFilterHandler.start();
        }
    }


}
