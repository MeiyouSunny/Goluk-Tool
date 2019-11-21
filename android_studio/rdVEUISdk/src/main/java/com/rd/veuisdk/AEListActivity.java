package com.rd.veuisdk;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.rd.lib.ui.ExtButton;
import com.rd.veuisdk.adapter.MyPagerAdapter;
import com.rd.veuisdk.fragment.AEPageFragment;
import com.rd.veuisdk.manager.UIConfiguration;
import com.rd.veuisdk.model.bean.TypeBean;
import com.rd.veuisdk.mvp.model.ICallBack;
import com.rd.veuisdk.mvp.model.TypeDataModel;
import com.rd.veuisdk.utils.ModeDataUtils;
import com.rd.veuisdk.utils.SysAlertDialog;

import java.util.ArrayList;
import java.util.List;

/**
 * AE模板列表
 */
public class AEListActivity extends BaseActivity {
    public static final int REQUEST_FOR_DETAIL_CODE = 700;
    private TypeDataModel mModel;
    private TabLayout tablayout;
    private ViewPager viewPager;
    private UIConfiguration uiConfiguration;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        TAG = "AEListActivity";
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ae_list_layout);
        $(R.id.titlebar_layout).setBackgroundColor(getResources().getColor(R.color.transparent));
        initView();
        SysAlertDialog.showLoadingDialog(AEListActivity.this, R.string.isloading);
        uiConfiguration = SdkEntry.getSdkService().getUIConfig();

        final String aeUrl = TextUtils.isEmpty(uiConfiguration.mAEUrl) ? ModeDataUtils.RD_APP_DATA : uiConfiguration.mAEUrl;
        if (!TextUtils.isEmpty(uiConfiguration.mResTypeUrl) || (TextUtils.isEmpty(uiConfiguration.mResTypeUrl) && TextUtils.isEmpty(uiConfiguration.mAEUrl))) {
            //未设置有效的分类和资源地址(优先走默认的Rd分类）
            String typeUrl = TextUtils.isEmpty(uiConfiguration.mResTypeUrl) ? ModeDataUtils.RD_TYPE_URL : uiConfiguration.mResTypeUrl;
            $(R.id.pageLayout).setVisibility(View.VISIBLE);
            $(R.id.fragmentParent).setVisibility(View.GONE);
            mModel = new TypeDataModel(new ICallBack<TypeBean>() {
                @Override
                public void onSuccess(List<TypeBean> list) {
                    initPager(list, aeUrl);
                }

                @Override
                public void onFailed() {
                    SysAlertDialog.cancelLoadingDialog();
                }
            });
            mModel.getTypeList(typeUrl, ModeDataUtils.TYPE_VIDEO_AE);
        } else {
            //兼容未分类 （如果数据量太大，建议使用分类）
            $(R.id.pageLayout).setVisibility(View.GONE);
            $(R.id.fragmentParent).setVisibility(View.VISIBLE);
            changeFragment(R.id.fragmentParent, AEPageFragment.newInstance(null, aeUrl));
            SysAlertDialog.cancelLoadingDialog();
        }
    }


    /**
     * 得到分类数据
     *
     * @param list
     */
    private void initPager(List<TypeBean> list, String url) {
        int len = list.size();
        String[] titles = new String[len];
        ArrayList<Fragment> fragments = new ArrayList<>();
        for (int i = 0; i < len; i++) {
            titles[i] = list.get(i).getName();
            fragments.add(AEPageFragment.newInstance(list.get(i), url));
        }

        MyPagerAdapter adapter = new MyPagerAdapter(getSupportFragmentManager(), titles, fragments);
        //tab的标题来自于adapter
        tablayout.setTabsFromPagerAdapter(adapter);
        tablayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tablayout));
        //tabLayout 和 viewpager关联
        tablayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                //tab选中时的回调
                viewPager.setCurrentItem(tab.getPosition(), true);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        viewPager.setAdapter(adapter);

        SysAlertDialog.cancelLoadingDialog();
    }


    private void initView() {
        $(R.id.btnRight).setVisibility(View.GONE);
        ExtButton btnLeft = $(R.id.btnLeft);
        btnLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        TextView tvTitle = $(R.id.tvTitle);
        tvTitle.setText(R.string.ae_list);

        tablayout = $(R.id.tabLayout);
        viewPager = $(R.id.viewPager);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            String path = data.getStringExtra(SdkEntry.EDIT_RESULT);
            Intent intent = new Intent();
            intent.putExtra(SdkEntry.EDIT_RESULT, path);
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        if (null != mModel) {
            mModel.recycle();
        }
        super.onDestroy();
    }
}
