package com.rd.veuisdk.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;

import com.rd.vecore.models.VisualFilterConfig;
import com.rd.veuisdk.IVideoMusicEditor;
import com.rd.veuisdk.R;
import com.rd.veuisdk.adapter.FilterLookupAdapter;
import com.rd.veuisdk.fragment.helper.IFilterHandler;
import com.rd.veuisdk.listener.OnItemClickListener;
import com.rd.veuisdk.model.WebFilterInfo;
import com.rd.veuisdk.utils.IMediaParam;
import com.rd.veuisdk.utils.IParamHandler;

/**
 * lookup滤镜
 */
public abstract class FilterFragmentLookupBase extends BaseFragment {
    protected IFilterHandler mIFilterHandler;
    protected IVideoMusicEditor mVideoMusicEditor;

    public void setIMediaParam(IMediaParam IMediaParam) {
        mIMediaParam = IMediaParam;
    }

    private IMediaParam mIMediaParam;
    protected int mLastPageIndex = 0;
    protected int lastItemId = 0;
    private int tmpIndex = 0;

    public void setShowApplyAll(boolean bShowApplyAll) {
        this.bShowApplyAll = bShowApplyAll;
    }

    protected boolean bShowApplyAll = false;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        mIFilterHandler = (IFilterHandler) context;
        if (context instanceof IVideoMusicEditor) {
            mVideoMusicEditor = (IVideoMusicEditor) context;
        }
        if (context instanceof IMediaParam) {
            mIMediaParam = (IMediaParam) context;
        } else if (context instanceof IParamHandler) {
            mIMediaParam = ((IParamHandler) context).getParamData();
        }
    }


    /**
     * 保存lookup滤镜
     *
     * @return true 应用到全部片段；false
     */
    public boolean onSure() {
        if (null != mIMediaParam) {
            mIMediaParam.setFilterIndex(tmpIndex);
            mIMediaParam.setLookupConfig(tmpLookup);
            mIMediaParam.setCurrentFilterType(VisualFilterConfig.FILTER_ID_NORMAL);
        }
        return mCheckBox.isChecked();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLastPageIndex = mIFilterHandler.getCurrentLookupIndex();
    }

    private RecyclerView mRecyclerView;
    protected FilterLookupAdapter mAdapter;
    private CheckBox mCheckBox;
    private SeekBar mStrengthBar;
    private TextView tvBottomTitle, tvFilterValue;

    protected abstract int getLayoutId();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (null != mIMediaParam) {
            mLastPageIndex = mIMediaParam.getFilterIndex();
            tmpIndex = mIMediaParam.getFilterIndex();
            tmpLookup = mIMediaParam.getLookupConfig();
        }
        mRoot = inflater.inflate(getLayoutId(), container, false);
        tvBottomTitle = $(R.id.tvBottomTitle);
        mCheckBox = $(R.id.cbApplyToAll);
        mRecyclerView = $(R.id.recyclerViewFilter);
        tvFilterValue = $(R.id.tvFilterValue);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        //设置添加或删除item时的动画，这里使用默认动画
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mAdapter = new FilterLookupAdapter(getContext());
        tvBottomTitle.setText(R.string.filter);
        mAdapter.setEnableRepeatClick(true);
        mAdapter.setOnItemClickListener(new OnItemClickListener<Object>() {
            @Override
            public void onItemClick(int position, Object item) {
                onSelectedImp(position);
                mStrengthBar.setEnabled(position > 0);
            }
        });
        mStrengthBar = $(R.id.sbarStrength);
        mStrengthBar.setEnabled(tmpIndex > 0);
        //设置适配器
        mRecyclerView.setAdapter(mAdapter);
        mCheckBox.setVisibility(bShowApplyAll ? View.VISIBLE : View.GONE);
        $(R.id.ivCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mVideoMusicEditor != null) {
                    mVideoMusicEditor.onBack();
                }
            }
        });
        $(R.id.ivSure).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mVideoMusicEditor != null) {
                    mVideoMusicEditor.onSure();
                }
            }
        });
        return mRoot;

    }


    @Override
    public int onBackPressed() {

        if (isRunning) {
            return 1;
        }
        return super.onBackPressed();
    }

    /**
     * 默认的锐度
     */
    protected float mDefaultValue = Float.NaN;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        float sharpen = Float.NaN;
        if (null != tmpLookup) {
            //滤镜程度就是调节的锐度参数
            sharpen = tmpLookup.getSharpen();
        }
        int value = Float.isNaN(sharpen) ? 100 : (int) (sharpen * 100);
        mStrengthBar.setProgress(value);
        tvFilterValue.setText(value + "%");
        mStrengthBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                if (fromUser) {
                    mDefaultValue = progress / 100.0f;
                    tvFilterValue.setText(progress + "%");
                    if (null != tmpLookup) {
                        tmpLookup.setDefaultValue(mDefaultValue);
                        mIFilterHandler.changeFilterLookup(tmpLookup, tmpIndex);
                    }

                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }


    /**
     * @return
     */
    public VisualFilterConfig getLookup() {
        return tmpLookup;
    }

    protected VisualFilterConfig tmpLookup = null;

    /**
     * 切换滤镜效果
     *
     * @param index
     */
    void switchFliter(int index) {
        tmpIndex = index;
        //lookup滤镜
        if (index > 0) {
            WebFilterInfo info = mAdapter.getItem(index);
            if (info != null) {
                tmpLookup = new VisualFilterConfig(info.getLocalPath());
                //滤镜程度
                tmpLookup.setDefaultValue(mDefaultValue);
                mIFilterHandler.changeFilterLookup(tmpLookup, index);
            } else {
                tmpIndex = 0;
                tmpLookup = new VisualFilterConfig(VisualFilterConfig.FILTER_ID_NORMAL);
                mIFilterHandler.changeFilterLookup(tmpLookup, 0);
            }
        } else {
            //第0个 无滤镜效果
            tmpLookup = new VisualFilterConfig(VisualFilterConfig.FILTER_ID_NORMAL);
            mIFilterHandler.changeFilterLookup(tmpLookup, index);
        }
    }


    public abstract void onSelectedImp(int nItemId);


}
