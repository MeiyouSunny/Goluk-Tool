package com.rd.veuisdk.fragment.helper;

import android.content.Context;

import com.rd.lib.utils.CoreUtils;
import com.rd.veuisdk.R;
import com.rd.veuisdk.model.WebFilterInfo;
import com.rd.veuisdk.utils.PathUtils;

import java.util.ArrayList;

/**
 * 本地lookup滤镜
 */
public class FilterLookupLocalHandler {

    private Context mContext;

    public FilterLookupLocalHandler(Context context) {
        mArrayList = new ArrayList<>();
        init(context);
    }


    private void init(Context context) {
        mContext = context;
        mArrayList.clear();
        int i = 0;
        initFitler(i++, R.drawable.camare_filter_0, R.string.none);
        initFitler(i++, R.drawable.lookup1, R.string.lookup1);
        initFitler(i++, R.drawable.lookup2, R.string.lookup2);
        initFitler(i++, R.drawable.lookup3, R.string.lookup3);
        initFitler(i++, R.drawable.lookup4, R.string.lookup4);
        initFitler(i++, R.drawable.lookup5, R.string.lookup5);
        initFitler(i++, R.drawable.lookup6, R.string.lookup6);
        initFitler(i++, R.drawable.lookup7, R.string.lookup7);
        initFitler(i++, R.drawable.lookup8, R.string.lookup8);
        initFitler(i++, R.drawable.lookup9, R.string.lookup9);
        initFitler(i++, R.drawable.lookup10, R.string.lookup10);
    }


    /**
     * @param fileIndex
     * @param drawableId
     * @param textResId
     */
    private void initFitler(int fileIndex, int drawableId, int textResId) {
        String dst = null;
        if (fileIndex > 0) {
            String fileName = "lookup" + fileIndex;
            dst = PathUtils.getAssetFileNameForSdcard(fileName, "png");
            CoreUtils.assetRes2File(mContext.getAssets(), "filter/lookup/" + fileName + ".png", dst);
        }
        WebFilterInfo info = new WebFilterInfo(fileIndex, drawableId, mContext.getString(textResId));
        info.setLocalPath(dst);
        mArrayList.add(info);
    }

    public ArrayList<WebFilterInfo> mArrayList;

    public ArrayList<WebFilterInfo> getArrayList() {
        return mArrayList;
    }

    public void recycle() {
        if (null != mArrayList) {
            mArrayList.clear();
            mArrayList = null;
        }

    }


}
