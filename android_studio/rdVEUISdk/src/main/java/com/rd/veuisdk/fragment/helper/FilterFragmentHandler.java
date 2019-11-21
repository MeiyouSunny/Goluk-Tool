package com.rd.veuisdk.fragment.helper;

import android.content.Context;

import com.rd.veuisdk.R;
import com.rd.veuisdk.SdkEntry;
import com.rd.veuisdk.fragment.FilterFragment;
import com.rd.veuisdk.utils.ReplaceableUtils;

import java.util.ArrayList;

/**
 * acv 滤镜
 *
 * @author JIAN
 * @create 2018/12/5
 * @Describe
 */
public class FilterFragmentHandler {


    private ArrayList<ArrayList<FilterFragment.FliterItem>> mArrFliterBucket = new ArrayList<>();
    private Context mContext;
    // 是否使用jlk滤镜 单行；false  acv 滤镜分组
    private boolean isJLKStyle;


    public FilterFragmentHandler(Context context, boolean isJLKStyle) {
        mContext = context;
        this.isJLKStyle = isJLKStyle;
        initFliter();
    }


    /**
     * 当前组的滤镜列表
     *
     * @param groupIndex
     * @return
     */
    public ArrayList<FilterFragment.FliterItem> getFilterList(int groupIndex) {
        return mArrFliterBucket.get(groupIndex);
    }

    private String getString(int id) {
        return mContext.getString(id);
    }

    /**
     * @param id
     */
    public void resetFliterItem(int id) {

        for (ArrayList<FilterFragment.FliterItem> arr : mArrFliterBucket) {
            for (FilterFragment.FliterItem item : arr) {
                item.setSelected(item.getId() == id);
            }
        }
    }

    private void initFliter() {
        int id = 5;

        if (!isJLKStyle) {
            // 滤镜方式1 (分组)
            /**
             * 午茶
             */
            ArrayList<FilterFragment.FliterItem> arrFliter1 = new ArrayList<FilterFragment.FliterItem>();
            arrFliter1.add(new FilterFragment.FliterItem(R.drawable.camera_effect_0,
                    getString(R.string.filter_1_0), 0));
            arrFliter1.add(new FilterFragment.FliterItem(R.drawable.camera_effect_5,
                    getString(R.string.filter_1_5), id++));
            arrFliter1.add(new FilterFragment.FliterItem(R.drawable.camera_effect_6,
                    getString(R.string.filter_1_6), id++));
            arrFliter1.add(new FilterFragment.FliterItem(R.drawable.camera_effect_7,
                    getString(R.string.filter_1_7), id++));
            arrFliter1.add(new FilterFragment.FliterItem(R.drawable.camera_effect_8,
                    getString(R.string.filter_1_8), id++));
            arrFliter1.add(new FilterFragment.FliterItem(R.drawable.camera_effect_9,
                    getString(R.string.filter_1_9), id++));
            arrFliter1.add(new FilterFragment.FliterItem(R.drawable.camera_effect_10,
                    getString(R.string.filter_1_10), id++));
            mArrFliterBucket.add(arrFliter1);
            if (!SdkEntry.isLite(mContext)) {
                //扩展滤镜
                ReplaceableUtils.initFilterAcv(mContext, mArrFliterBucket, id);

            }

        } else {
            id = 40;
            // 滤镜方式二 （jlk滤镜）
            ArrayList<FilterFragment.FliterItem> arrFliterjlk = new ArrayList<FilterFragment.FliterItem>();
            arrFliterjlk.add(new FilterFragment.FliterItem(R.drawable.sp_filter_01,
                    getString(R.string.sp_filter_1), 0));
            arrFliterjlk.add(new FilterFragment.FliterItem(R.drawable.sp_filter_02,
                    getString(R.string.sp_filter_2), id++));
            arrFliterjlk.add(new FilterFragment.FliterItem(R.drawable.sp_filter_03,
                    getString(R.string.sp_filter_3), id++));
            arrFliterjlk.add(new FilterFragment.FliterItem(R.drawable.sp_filter_04,
                    getString(R.string.sp_filter_4), id++));
            arrFliterjlk.add(new FilterFragment.FliterItem(R.drawable.sp_filter_05,
                    getString(R.string.sp_filter_5), id++));
            arrFliterjlk.add(new FilterFragment.FliterItem(R.drawable.sp_filter_06,
                    getString(R.string.sp_filter_6), id++));
            arrFliterjlk.add(new FilterFragment.FliterItem(R.drawable.sp_filter_07,
                    getString(R.string.sp_filter_7), id++));
            arrFliterjlk.add(new FilterFragment.FliterItem(R.drawable.sp_filter_08,
                    getString(R.string.sp_filter_8), id++));
            arrFliterjlk.add(new FilterFragment.FliterItem(R.drawable.sp_filter_09,
                    getString(R.string.sp_filter_9), id++));
            mArrFliterBucket.add(arrFliterjlk);

        }

    }
}
