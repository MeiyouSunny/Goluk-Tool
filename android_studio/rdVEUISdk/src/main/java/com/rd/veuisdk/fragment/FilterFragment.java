package com.rd.veuisdk.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.rd.lib.utils.CoreUtils;
import com.rd.veuisdk.R;
import com.rd.veuisdk.SdkEntry;
import com.rd.veuisdk.adapter.BaseRVAdapter;
import com.rd.veuisdk.fragment.helper.FilterFragmentHandler;
import com.rd.veuisdk.fragment.helper.IFilterHandler;
import com.rd.veuisdk.ui.ExtCircleSimpleDraweeView;
import com.rd.veuisdk.utils.IMediaParam;
import com.rd.veuisdk.utils.IParamHandler;
import com.rd.veuisdk.utils.Utils;
import com.rd.xpk.editor.modal.ImageObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 滤镜界面  (支持分组(哥特、冷漠);   单行显示 (asset/filter/jlk   ) )
 */
@SuppressLint("ValidFragment")
public class FilterFragment extends BaseFragment {

    public void setMediaParam(IMediaParam mediaParam) {
        mMediaParam = mediaParam;
        menuIndex = mMediaParam.getFilterIndex();
        nFilterId = mMediaParam.getCurrentFilterType();
    }

    private IMediaParam mMediaParam;
    private IFilterHandler mHlrVideoEditor;
    private RadioGroup mRgFliter;

    public FilterFragment() {
    }

    public static FilterFragment newInstance() {
        return new FilterFragment();
    }

    /**
     * @param isJlkStyle 是否使用jlk滤镜 单行；false  acv 滤镜分组
     */
    public void setJLKStyle(boolean isJlkStyle) {
        isJLK = isJlkStyle;
    }

    private boolean isJLK;
    private int menuIndex = 0;
    private int nFilterId = 0;
    private FilterFragmentHandler mFragmentHandler;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        mFragmentHandler = new FilterFragmentHandler(context, isJLK);
        mHlrVideoEditor = (IFilterHandler) context;
        if (context instanceof IMediaParam) {
            mMediaParam = (IMediaParam) context;
        } else if (context instanceof IParamHandler) {
            mMediaParam = ((IParamHandler) context).getParamData();
            menuIndex = mMediaParam.getFilterIndex();
            nFilterId = mMediaParam.getCurrentFilterType();
        }

    }


    public int getMenuIndex() {
        return menuIndex;
    }

    public int getFilterId() {
        return nFilterId;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPageName = getString(R.string.filter);
    }

    private RecyclerView mRecyclerView;
    private FilterAdapter mLookupAdapter;

    @SuppressLint("InflateParams")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.fragment_video_edit_filter, container, false);
        mRecyclerView = $(R.id.recyclerViewFilter);
        if (!isJLK) {
            mRecyclerView.setPadding(0, CoreUtils.dpToPixel(15), 0, 0);
        }
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        //设置添加或删除item时的动画，这里使用默认动画
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mLookupAdapter = new FilterAdapter(getContext());
        //设置适配器
        mRecyclerView.setAdapter(mLookupAdapter);
        initView();
        return mRoot;
    }

    private HorizontalScrollView mHorizontalScrollView;

    private void initView() {
        mHorizontalScrollView = $(R.id.hsvFilterType);
        mRgFliter = $(R.id.rgFliter);
        mFragmentHandler.resetFliterItem(nFilterId);
        if (isJLK) {
            mHorizontalScrollView.setVisibility(View.GONE);
            mRgFliter.setEnabled(false);
            setViewVisibility(R.id.viewMidLine, false);
            mLookupAdapter.addAll(mFragmentHandler.getFilterList(0));
        } else {
            if (SdkEntry.isLite(getContext())) {
                //精简版只保留“午茶”的滤镜
                mHorizontalScrollView.setVisibility(View.GONE);
                mRgFliter.setEnabled(false);
                setViewVisibility(R.id.viewMidLine, false);
            } else {
                mHorizontalScrollView.setVisibility(View.VISIBLE);
            }
            resetMenuGroupImp(menuIndex);
            mLookupAdapter.addAll(mFragmentHandler.getFilterList(menuIndex));
            mRgFliter.setOnCheckedChangeListener(fliterChangedListener);
        }

    }


    private void resetMenuGroupImp(int menuIndex) {
        if (isJLK) {

        } else {
            if (menuIndex == 1) {
                mRgFliter.check(R.id.rbFliterGeTe);
            } else if (menuIndex == 2) {
                mRgFliter.check(R.id.rbFliterLemo);
            } else if (menuIndex == 3) {
                mRgFliter.check(R.id.rbFliterLengdiao);
            } else if (menuIndex == 4) {
                mRgFliter.check(R.id.rbFliterBomo);
            } else if (menuIndex == 5) {
                mRgFliter.check(R.id.rbFliterYese);
            } else if (menuIndex == 6) {
                mRgFliter.check(R.id.rbFliterHuaijiu);
            } else {
                mRgFliter.check(R.id.rbFliterWuCha);
            }
        }
    }

    private int getCurrentRadioIndex() {
        int checkId = mRgFliter.getCheckedRadioButtonId();
        if (checkId == R.id.rbFliterWuCha) {
            return 0;
        } else if (checkId == R.id.rbFliterGeTe) {
            return 1;
        } else if (checkId == R.id.rbFliterLemo) {
            return 2;
        } else if (checkId == R.id.rbFliterLengdiao) {
            return 3;
        } else if (checkId == R.id.rbFliterBomo) {
            return 4;
        } else if (checkId == R.id.rbFliterYese) {
            return 5;
        } else if (checkId == R.id.rbFliterHuaijiu) {
            return 6;
        } else {
            return 0;
        }

    }

    public static class FliterItem {

        public FliterItem(int res, String name, int id) {
            drawId = res;
            this.name = name;
            this.id = id;
        }

        int drawId;


        public int getId() {
            return id;
        }

        int id;
        String name;


        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        boolean selected = false;
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
            mLookupAdapter.addAll(mFragmentHandler.getFilterList(n));
        }

    };


    /**
     * 切换滤镜效果
     *
     * @param groupIndex
     * @param id
     */
    private void switchFliter(int groupIndex, int id) {
        if (id == 40) {
            id = ImageObject.FILTER_TYPE_GRAY;
        } else if (id == 41) {
            id = ImageObject.FILTER_TYPE_SEPIA;
        }
        menuIndex = groupIndex;
        nFilterId = id;
        mHlrVideoEditor.changeFilterType(groupIndex, id);
    }


    class FilterAdapter extends BaseRVAdapter<FilterAdapter.ViewHolder> {
        private List<FliterItem> list;
        private String TAG = "FilterAdapter";
        private int mColorNormal, mColorSelected;

        public FilterAdapter(Context context) {
            Resources res = context.getResources();
            mColorNormal = res.getColor(R.color.borderline_color);
            mColorSelected = res.getColor(R.color.main_orange);
            list = new ArrayList<>();
        }


        public void addAll(List<FliterItem> tmp) {
            list.clear();
            if (null != tmp && tmp.size() > 0) {
                list.addAll(tmp);
            }
            download_progress = 100;
            notifyDataSetChanged();

        }


        @Override
        public int getItemViewType(int position) {
            return 0;
        }

        @Override
        public FilterAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getLayoutInflater(parent.getContext()).inflate(R.layout.fresco_list_item, parent, false);
            ViewClickListener viewClickListener = new ViewClickListener();
            view.setOnClickListener(viewClickListener);
            view.setTag(viewClickListener);
            return new FilterAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(FilterAdapter.ViewHolder holder, int position) {
            FilterAdapter.ViewClickListener viewClickListener = (FilterAdapter.ViewClickListener) holder.itemView.getTag();
            viewClickListener.setPosition(position);
            FliterItem info = list.get(position);
            if (info.selected) {
                //被选中
                holder.mImageView.setProgress(download_progress);
                holder.mImageView.setChecked(true);
                holder.mText.setTextColor(mColorSelected);
            } else {
                //未选中
                holder.mImageView.setProgress(0);
                holder.mImageView.setChecked(false);
                holder.mText.setTextColor(mColorNormal);
            }

            holder.mImageView.setImageResource(info.drawId);
            holder.mText.setText(info.name);
        }


        private int download_progress = 100;


        @Override
        public int getItemCount() {
            return list.size();
        }

        public FliterItem getItem(int position) {
            if (0 <= position && position <= (getItemCount() - 1)) {
                return list.get(position);
            }
            return null;

        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView mText;
            ExtCircleSimpleDraweeView mImageView;

            ViewHolder(View itemView) {
                super(itemView);
                mText = Utils.$(itemView, R.id.tvItemCaption);
                mImageView = Utils.$(itemView, R.id.ivItemImage);
            }
        }

        class ViewClickListener extends BaseRVAdapter.BaseItemClickListener {
            @Override
            public void onClick(View v) {
                int filterId = getItem(position).id;
                mFragmentHandler.resetFliterItem(filterId);
                if (!isJLK) {
                    switchFliter(getCurrentRadioIndex(), filterId);
                } else {
                    switchFliter(position, filterId);
                }
                notifyDataSetChanged();
            }
        }
    }

}
