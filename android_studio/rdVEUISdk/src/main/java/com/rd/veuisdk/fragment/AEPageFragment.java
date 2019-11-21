package com.rd.veuisdk.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.rd.veuisdk.AEDetailActivity;
import com.rd.veuisdk.AEListActivity;
import com.rd.veuisdk.R;
import com.rd.veuisdk.adapter.AEModeAdapter;
import com.rd.veuisdk.ae.model.AETemplateInfo;
import com.rd.veuisdk.listener.OnItemClickListener;
import com.rd.veuisdk.model.bean.DataBean;
import com.rd.veuisdk.model.bean.TypeBean;
import com.rd.veuisdk.mvp.model.ICallBack;
import com.rd.veuisdk.mvp.model.ListDataModel;
import com.rd.veuisdk.utils.ModeDataUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 单页AE模板的内容
 *
 * @create 2019/7/4
 */
public class AEPageFragment extends BaseFragment {


    private static final String PARAM_AE_TYPE = "ae_type_bean";
    private static final String PARAM_AE_URL = "param_ae_url";

    public static AEPageFragment newInstance(TypeBean typeBean, String url) {
        Bundle args = new Bundle();
        args.putSerializable(PARAM_AE_TYPE, typeBean);
        args.putSerializable(PARAM_AE_URL, url);
        AEPageFragment fragment = new AEPageFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private RecyclerView recyclerView;
    private ListDataModel mModel;
    private List<AETemplateInfo> mList;
    private AEModeAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        TypeBean mTypeBean = (TypeBean) bundle.getSerializable(PARAM_AE_TYPE);
        String url = bundle.getString(PARAM_AE_URL);
        mList = null;
        mAdapter = null;
        mModel = new ListDataModel(new ICallBack<DataBean>() {
            @Override
            public void onSuccess(List<DataBean> list) {
                mList = getAEImp(list);
                updateUI();
            }

            @Override
            public void onFailed() {
                onToast(R.string.load_http_failed);
            }
        });

        if (null != mTypeBean) {
            //有分类
            mModel.getList(url, ModeDataUtils.TYPE_VIDEO_AE, mTypeBean.getId());
        } else {
            //未分类
            mModel.getList(url, ModeDataUtils.TYPE_VIDEO_AE, "");
        }
    }

    private void updateUI() {
        if (null != mList && null != mAdapter) {
            mAdapter.addAll(mList);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.fragment_base_page_layout, container, false);
        initView();
        updateUI();
        return mRoot;
    }

    @Override
    public void onDestroyView() {
        if (null != mModel) {
            mModel.recycle();
        }
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != mList) {
            mList.clear();
            mList = null;
        }
        mModel = null;
        mAdapter.recycle();
        mAdapter = null;
        mRoot = null;
    }

    private void onSelectedImp(AETemplateInfo info) {
        //资源已下载
        AEDetailActivity.gotoAEDetail(getContext(), info, AEListActivity.REQUEST_FOR_DETAIL_CODE);
    }


    private List<AETemplateInfo> getAEImp(List<DataBean> dataBeanList) {
        ArrayList<AETemplateInfo> resultList = new ArrayList<>();
        int len = dataBeanList.size();
        DataBean dataBean = null;
        for (int i = 0; i < len; i++) {
            dataBean = dataBeanList.get(i);
            AETemplateInfo aeTemplateInfo = new AETemplateInfo();
            aeTemplateInfo.setUrl(dataBean.getFile());
            aeTemplateInfo.setIconPath(dataBean.getCover());
            aeTemplateInfo.setName(dataBean.getName());
            aeTemplateInfo.setUpdatetime(Long.toString(dataBean.getUpdatetime()));
            aeTemplateInfo.setCoverAsp((float) dataBean.getWidth() / dataBean.getHeight(), dataBean.getWidth(), dataBean.getHeight());
            aeTemplateInfo.setVideoUrl(dataBean.getVideo());
            aeTemplateInfo.setMediaNum(dataBean.getPicture_need(), dataBean.getText_need(), dataBean.getVideo_need());
            resultList.add(aeTemplateInfo);
        }
        dataBeanList.clear();
        return resultList;
    }


    private void initView() {
        recyclerView = $(R.id.recyclerView);

        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, LinearLayout.VERTICAL);
        staggeredGridLayoutManager.setAutoMeasureEnabled(true);
        recyclerView.setLayoutManager(staggeredGridLayoutManager);
        //设置添加或删除item时的动画，这里使用默认动画
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        mAdapter = new AEModeAdapter(getContext());
        mAdapter.setOnItemClickListener(new OnItemClickListener<AETemplateInfo>() {
            @Override
            public void onItemClick(int position, AETemplateInfo item) {
                onSelectedImp(item);
            }
        });
        //设置适配器
        recyclerView.setAdapter(mAdapter);
    }

}
