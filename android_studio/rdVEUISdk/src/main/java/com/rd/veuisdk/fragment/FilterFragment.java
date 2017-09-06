package com.rd.veuisdk.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.rd.lib.utils.CoreUtils;
import com.rd.veuisdk.IVideoEditorHandler;
import com.rd.veuisdk.R;
import com.rd.veuisdk.VideoEditActivity;
import com.rd.veuisdk.model.HorizontalListItem;
import com.rd.veuisdk.model.HorizontalListItem.OnFilterClickListener;
import com.rd.veuisdk.model.ImageCacheUtils;
import com.rd.veuisdk.ui.ExtCircleImageView;
import com.rd.veuisdk.utils.ThumbNailUtils;
import com.rd.xpk.editor.modal.ImageObject;

import java.util.ArrayList;

/**
 * 滤镜界面
 *
 * @author jeck
 */
@SuppressLint("ValidFragment")
public class FilterFragment extends BaseFragment implements
        OnFilterClickListener {

    public static int checkFilterId = 0;

    private int mBucketId = 0;

    /**
     * 上下文
     */
    private Context mContext;

    private IVideoEditorHandler mHlrVideoEditor;

    private RadioGroup mRgFliter;

    private GridView mGvFliter;

    private FliterAdapter mFliterAdapter;

    private ArrayList<ArrayList<FliterItem>> mArrFliterBucket = new ArrayList<ArrayList<FliterItem>>();

    private boolean mHideBucket;

    private boolean mIsLoad = false;

    public FilterFragment() {
    }

    public FilterFragment(boolean mHideBucket) {
        this.mHideBucket = mHideBucket;
        mIsLoad = true;
    }

    private boolean isFilterStyle2;

    /**
     * 直接使用jlk的filter资源
     *
     * @param mHideBucket
     * @param isFilterStyle2
     */
    public FilterFragment(boolean mHideBucket, boolean isFilterStyle2) {
        this.mHideBucket = mHideBucket;
        mIsLoad = true;
        this.isFilterStyle2 = isFilterStyle2;

    }
    

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;
        mHlrVideoEditor = (IVideoEditorHandler) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mIsLoad) {
            initFliter();
            mPageName = getString(R.string.filter);
        }
    }

    @SuppressLint("InflateParams")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mRoot = inflater.inflate(R.layout.fragment_video_edit_filter, null);
        if (mIsLoad) {
            initHorizontalListView1(mRoot);
        }
        return mRoot;
    }

    private void initHorizontalListView1(View layout) {
        ImageCacheUtils.getInstance(getActivity()).setTargetSize(
                ThumbNailUtils.THUMB_WIDTH, ThumbNailUtils.THUMB_WIDTH);


        mGvFliter = (GridView) layout.findViewById(R.id.gvFliter);
        if (mHideBucket) {

        } else {
            layout.findViewById(R.id.hsvFilterItem).setPadding(0,
                    CoreUtils.dpToPixel(15), 0, 0);
        }

        mRgFliter = (RadioGroup) layout.findViewById(R.id.rgFliter);

        if (isFilterStyle2) {
            mRgFliter.setVisibility(View.INVISIBLE);
            mRgFliter.setEnabled(false);
            View line = layout.findViewById(R.id.viewMidLine);
            if (null != line) {
                line.setVisibility(View.INVISIBLE);
            }
        } else {
            mRgFliter.setOnCheckedChangeListener(fliterChangedListener);
            mRgFliter.setVisibility(View.VISIBLE);
        }

        // if (mBucketId != 0) {
        // mRgFliter.check(mBucketId);
        // }
        mFliterAdapter = new FliterAdapter();
        // if (mHideBucket) {
        // mFliterAdapter.setFliterList(mArrFliterAll);
        // setGridViewParams(mArrFliterAll.size());
        // } else {
        mFliterAdapter.setFliterList(mArrFliterBucket.get(0));
        setGridViewParams(mArrFliterBucket.get(0).size());
        // }

        mGvFliter.setAdapter(mFliterAdapter);
        resetFliterItem(VideoEditActivity.mCurrentFilterType);

    }

    private void setGridViewParams(int size) {
        int length = 65;
        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        float density = dm.density;
        int gridviewWidth = (int) (size * length * density + size * 5 + 10 * density);
        int itemWidth = (int) (length * density);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                gridviewWidth, LinearLayout.LayoutParams.FILL_PARENT);
        mGvFliter.setLayoutParams(params); // 设置GirdView布局参数,横向布局的关键
        mGvFliter.setColumnWidth(itemWidth); // 设置列表项宽
        mGvFliter.setHorizontalSpacing(5); // 设置列表项水平间距
        mGvFliter.setStretchMode(GridView.NO_STRETCH);
        mGvFliter.setNumColumns(size); // 设置列数量=列表集合数
    }

    private class FliterAdapter extends BaseAdapter {
        ArrayList<FliterItem> arrFliter;
        private int tvN, tvEd;

        public FliterAdapter() {
            Resources res = getResources();
            tvN = res.getColor(R.color.transparent_white);
            tvEd = res.getColor(R.color.main_orange);
        }

        // 极路客滤镜被选中为方形 ；其他滤镜为原型
        public void setFliterList(ArrayList<FliterItem> arr) {
            arrFliter = arr;
            notifyDataSetChanged();

        }

        @Override
        public int getCount() {
            return arrFliter.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(
                        R.layout.rdveuisdk_horizontal_fliter_item, null);
            }
            final FliterItem item = arrFliter.get(position);
            ExtCircleImageView iv = (ExtCircleImageView) convertView
                    .findViewById(R.id.ivFliter);
            iv.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    resetFliterItem(item.id);
                    switchFliter(item.id);
                    notifyDataSetChanged();
                }
            });

            TextView tv = (TextView) convertView.findViewById(R.id.tvFliter);
            iv.setChecked(item.selected);
            if (item.selected) {
                tv.setTextColor(tvEd);
            } else {
                tv.setTextColor(tvN);
            }

            iv.setImageResource(item.drawId);
            tv.setText(item.name);
            return convertView;
        }
    }

    public void resetFliterItem(int id) {
        // if (mHideBucket) {
        // for (FliterItem item : mArrFliterAll) {
        // if (item.id == id) {
        // item.selected = true;
        // } else {
        // item.selected = false;
        // }
        // }
        // } else {
        for (ArrayList<FliterItem> arr : mArrFliterBucket) {
            for (FliterItem item : arr) {
                if (item.id == id) {
                    item.selected = true;
                } else {
                    item.selected = false;
                }
            }
        }
        // }
    }

    private class FliterItem {

        FliterItem(int res, String name, int id) {
            drawId = res;
            this.name = name;
            this.id = id;
        }

        int drawId;
        int id;
        String name;
        boolean selected = false;
    }

    private ArrayList<FliterItem> mArrFliterAll;

    private void initFliter() {
        int id = 5;

        if (!isFilterStyle2) {
            // 滤镜方式1

            /**
             * 午茶
             */
            ArrayList<FliterItem> arrFliter1 = new ArrayList<FliterItem>();
            arrFliter1.add(new FliterItem(R.drawable.camera_effect_0,
                    getString(R.string.filter_1_0), 0));
            arrFliter1.add(new FliterItem(R.drawable.camera_effect_5,
                    getString(R.string.filter_1_5), id++));
            arrFliter1.add(new FliterItem(R.drawable.camera_effect_6,
                    getString(R.string.filter_1_6), id++));
            arrFliter1.add(new FliterItem(R.drawable.camera_effect_7,
                    getString(R.string.filter_1_7), id++));
            arrFliter1.add(new FliterItem(R.drawable.camera_effect_8,
                    getString(R.string.filter_1_8), id++));
            arrFliter1.add(new FliterItem(R.drawable.camera_effect_9,
                    getString(R.string.filter_1_9), id++));
            arrFliter1.add(new FliterItem(R.drawable.camera_effect_10,
                    getString(R.string.filter_1_10), id++));
            mArrFliterBucket.add(arrFliter1);

            /**
             * 哥特
             */
            ArrayList<FliterItem> arrFliter2 = new ArrayList<FliterItem>();
            arrFliter2.add(new FliterItem(R.drawable.camera_effect_0,
                    getString(R.string.filter_1_0), 0));
            arrFliter2.add(new FliterItem(R.drawable.camera_effect_11,
                    getString(R.string.filter_1_11), id++));
            arrFliter2.add(new FliterItem(R.drawable.camera_effect_12,
                    getString(R.string.filter_1_12), id++));
            arrFliter2.add(new FliterItem(R.drawable.camera_effect_13,
                    getString(R.string.filter_1_13), id++));
            arrFliter2.add(new FliterItem(R.drawable.camera_effect_14,
                    getString(R.string.filter_1_14), id++));
            mArrFliterBucket.add(arrFliter2);

            /**
             * lemo
             */
            ArrayList<FliterItem> arrFliter3 = new ArrayList<FliterItem>();
            arrFliter3.add(new FliterItem(R.drawable.camera_effect_0,
                    getString(R.string.filter_1_0), 0));
            arrFliter3.add(new FliterItem(R.drawable.camera_effect_15,
                    getString(R.string.filter_1_15), id++));
            arrFliter3.add(new FliterItem(R.drawable.camera_effect_16,
                    getString(R.string.filter_1_16), id++));
            arrFliter3.add(new FliterItem(R.drawable.camera_effect_17,
                    getString(R.string.filter_1_17), id++));
            arrFliter3.add(new FliterItem(R.drawable.camera_effect_18,
                    getString(R.string.filter_1_18), id++));
            arrFliter3.add(new FliterItem(R.drawable.camera_effect_19,
                    getString(R.string.filter_1_19), id++));
            arrFliter3.add(new FliterItem(R.drawable.camera_effect_20,
                    getString(R.string.filter_1_20), id++));
            arrFliter3.add(new FliterItem(R.drawable.camera_effect_21,
                    getString(R.string.filter_1_21), id++));
            mArrFliterBucket.add(arrFliter3);

            /**
             * 冷调
             */
            ArrayList<FliterItem> arrFliter4 = new ArrayList<FliterItem>();
            arrFliter4.add(new FliterItem(R.drawable.camera_effect_0,
                    getString(R.string.filter_1_0), 0));
            arrFliter4.add(new FliterItem(R.drawable.camera_effect_22,
                    getString(R.string.filter_1_22), id++));
            arrFliter4.add(new FliterItem(R.drawable.camera_effect_23,
                    getString(R.string.filter_1_23), id++));
            arrFliter4.add(new FliterItem(R.drawable.camera_effect_24,
                    getString(R.string.filter_1_24), id++));
            arrFliter4.add(new FliterItem(R.drawable.camera_effect_25,
                    getString(R.string.filter_1_25), id++));
            arrFliter4.add(new FliterItem(R.drawable.camera_effect_26,
                    getString(R.string.filter_1_26), id++));
            mArrFliterBucket.add(arrFliter4);

            /**
             * 薄暮
             */
            ArrayList<FliterItem> arrFliter5 = new ArrayList<FliterItem>();
            arrFliter5.add(new FliterItem(R.drawable.camera_effect_0,
                    getString(R.string.filter_1_0), 0));
            arrFliter5.add(new FliterItem(R.drawable.camera_effect_27,
                    getString(R.string.filter_1_27), id++));
            arrFliter5.add(new FliterItem(R.drawable.camera_effect_28,
                    getString(R.string.filter_1_28), id++));
            arrFliter5.add(new FliterItem(R.drawable.camera_effect_29,
                    getString(R.string.filter_1_29), id++));
            arrFliter5.add(new FliterItem(R.drawable.camera_effect_30,
                    getString(R.string.filter_1_30), id++));
            arrFliter5.add(new FliterItem(R.drawable.camera_effect_31,
                    getString(R.string.filter_1_31), id++));
            arrFliter5.add(new FliterItem(R.drawable.camera_effect_32,
                    getString(R.string.filter_1_32), id++));
            mArrFliterBucket.add(arrFliter5);

            /**
             * 夜色
             */
            ArrayList<FliterItem> arrFliter6 = new ArrayList<FliterItem>();
            arrFliter6.add(new FliterItem(R.drawable.camera_effect_0,
                    getString(R.string.filter_1_0), 0));
            arrFliter6.add(new FliterItem(R.drawable.camera_effect_33,
                    getString(R.string.filter_1_33), id++));
            arrFliter6.add(new FliterItem(R.drawable.camera_effect_34,
                    getString(R.string.filter_1_34), id++));
            arrFliter6.add(new FliterItem(R.drawable.camera_effect_35,
                    getString(R.string.filter_1_35), id++));
            arrFliter6.add(new FliterItem(R.drawable.camera_effect_36,
                    getString(R.string.filter_1_36), id++));
            arrFliter6.add(new FliterItem(R.drawable.camera_effect_37,
                    getString(R.string.filter_1_37), id++));
            mArrFliterBucket.add(arrFliter6);

            /**
             * 夜色
             */
            ArrayList<FliterItem> arrFliter7 = new ArrayList<FliterItem>();
            arrFliter7.add(new FliterItem(R.drawable.camera_effect_0,
                    getString(R.string.filter_1_0), 0));
            arrFliter7.add(new FliterItem(R.drawable.camera_effect_38,
                    getString(R.string.filter_1_38), id++));
//			arrFliter7.add(new FliterItem(R.drawable.camera_effect_39,
//					getString(R.string.filter_1_39), id++));
            mArrFliterBucket.add(arrFliter7);

        } else {
            id = 40;
            // 滤镜方式二
            ArrayList<FliterItem> arrFliterjlk = new ArrayList<FliterItem>();
            arrFliterjlk.add(new FliterItem(R.drawable.sp_filter_01,
                    getString(R.string.sp_filter_1), 0));
            arrFliterjlk.add(new FliterItem(R.drawable.sp_filter_02,
                    getString(R.string.sp_filter_2), id++));
            arrFliterjlk.add(new FliterItem(R.drawable.sp_filter_03,
                    getString(R.string.sp_filter_3), id++));
            arrFliterjlk.add(new FliterItem(R.drawable.sp_filter_04,
                    getString(R.string.sp_filter_4), id++));
            arrFliterjlk.add(new FliterItem(R.drawable.sp_filter_05,
                    getString(R.string.sp_filter_5), id++));
            arrFliterjlk.add(new FliterItem(R.drawable.sp_filter_06,
                    getString(R.string.sp_filter_6), id++));
            arrFliterjlk.add(new FliterItem(R.drawable.sp_filter_07,
                    getString(R.string.sp_filter_7), id++));
            arrFliterjlk.add(new FliterItem(R.drawable.sp_filter_08,
                    getString(R.string.sp_filter_8), id++));
            arrFliterjlk.add(new FliterItem(R.drawable.sp_filter_09,
                    getString(R.string.sp_filter_9), id++));
            mArrFliterBucket.add(arrFliterjlk);

        }
        if (mHideBucket) {
            mArrFliterAll = new ArrayList<FliterItem>();
            mArrFliterAll.add(new FliterItem(R.drawable.camera_effect_0,
                    getString(R.string.filter_1_0), 0));
            for (ArrayList<FliterItem> arr : mArrFliterBucket) {
                for (int n = 1; n < arr.size(); n++) {
                    mArrFliterAll.add(arr.get(n));
                }

            }
        }

    }

    private OnCheckedChangeListener fliterChangedListener = new OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            int n = 0;
            if (checkedId == R.id.rbFliterWuCha) {
                n = 0;
            } else if (checkedId == R.id.rbFliterGeTe) {
                n = 1;
            } else if (checkedId == R.id.rbFliterLemo) {
                n = 2;
            } else if (checkedId == R.id.rbFliterLengdiao) {
                n = 3;
            } else if (checkedId == R.id.rbFliterBomo) {
                n = 4;
            } else if (checkedId == R.id.rbFliterYese) {
                n = 5;
            } else if (checkedId == R.id.rbFliterHuaijiu) {
                n = 6;
            }
            mBucketId = checkedId;
            mFliterAdapter.setFliterList(mArrFliterBucket.get(n));
            setGridViewParams(mArrFliterBucket.get(n).size());
        }

    };

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private HorizontalListItem mLastItem;

    /**
     * 切换滤镜效果
     */

    private void switchFliter(int id) {
        if (id == 40) {
            id = ImageObject.FILTER_TYPE_GRAY;
        } else if (id == 41) {
            id = ImageObject.FILTER_TYPE_SEPIA;
        }
        checkFilterId = id;
        mHlrVideoEditor.changeFilterType(checkFilterId);
        if (null != mLastItem) {
            mLastItem.hideSelectedFlag();
        }
        // mLastItem = item;
    }

    @Override
    public void onSwitchFilterClick(int filterType, HorizontalListItem item) {

        if (filterType == 40) {
            filterType = ImageObject.FILTER_TYPE_GRAY;
        } else if (filterType == 41) {
            filterType = ImageObject.FILTER_TYPE_SEPIA;
        }
        checkFilterId = filterType;
        mHlrVideoEditor.changeFilterType(checkFilterId);
        if (null != mLastItem) {
            mLastItem.hideSelectedFlag();
        }
        mLastItem = item;
    }
}
