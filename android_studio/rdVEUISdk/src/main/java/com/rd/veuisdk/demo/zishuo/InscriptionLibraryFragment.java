package com.rd.veuisdk.demo.zishuo;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.rd.lib.utils.CoreUtils;
import com.rd.veuisdk.R;
import com.rd.veuisdk.fragment.BaseFragment;
import com.rd.veuisdk.ui.ExtViewPager;
import com.rd.veuisdk.utils.SysAlertDialog;

import java.util.ArrayList;

/**
 * 题词库
 */
public class InscriptionLibraryFragment extends BaseFragment implements View.OnClickListener {

    private RadioGroup mRadioGroup;
    private ExtViewPager mViewPager;
    private HorizontalScrollView mHsvScroll;
    private Resources mResources;
    private Handler mHandler;
    private InscriptPageAdapter mPageAdapter;
    private ArrayList<InscriptionInfo> mApiList = new ArrayList<>();
    //详情
    private LinearLayout mLlFragment;
    private InscriptionDetailFragment mDetailFragment;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.fragment_inscription_library, container, false);
        initView();
        init();
        return mRoot;
    }

    private void initView() {
        mRadioGroup = $(R.id.rg_group);
        mViewPager = $(R.id.vp_inscription);
        mHsvScroll = $(R.id.hsv_menu);
        mLlFragment = $(R.id.ll_fragment);

        $(R.id.btnLeft).setOnClickListener(this);
        $(R.id.btn_custom).setOnClickListener(this);

        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            int mMax = 4;
            int tn = Color.parseColor("#ffffff");
            int tp = Color.parseColor("#27262C");
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int pIndex = 0;
                if (mApiList.size() > 0) {
                    for (int i = 0; i < mApiList.size(); i++) {
                        if (mApiList.get(i).getMenu().hashCode() == checkedId) {
                            pIndex = i;
                            break;
                        }
                    }
                    if (pIndex > mMax) {
                        // 单个item 左右(padding+margin)
                        mHsvScroll
                                .setScrollX((mRadioGroup.getChildAt(0).getWidth() + mPadding
                                        * mMax)
                                        * (pIndex - mMax));
                    } else {
                        mHsvScroll.setScrollX(0);
                    }
                    for (int j = 0; j < mRadioGroup.getChildCount(); j++) {

                        RadioButton rb = (RadioButton) mRadioGroup.getChildAt(j);
                        if (null != rb) {
                            rb.setTextColor((j == pIndex) ? tp : tn);
                        }
                    }
                    if (mViewPager.getCurrentItem() != pIndex) {
                        mViewPager.setCurrentItem(pIndex, true);
                    }
                }
            }
        });

    }

    private void init() {
        mResources = getContext().getResources();
        initHandler();
        getApiList();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btnLeft) {
            //返回
            if (mListener != null) {
                mListener.cancel();
            }
        } else if (id == R.id.btn_custom) {
            //自定义题词
            if (mListener != null) {
                mListener.onCustom();
            }
        }
    }

    private int mPadding = 5;

    /**
     * 创建单个 题词库分类
     */
    private void createRadioButton(String text, RadioGroup.LayoutParams lpItem) {
        RadioButton radioButton = new RadioButton(getContext());
        radioButton.setId(text.hashCode());
        radioButton.setText(text);
        radioButton.setButtonDrawable(new ColorDrawable(Color.TRANSPARENT));
        radioButton.setBackgroundResource(R.drawable.more_music_menu_bg);
        radioButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        radioButton.setTextColor(mResources.getColor(R.drawable.radio_music_txcolor));
        radioButton.setGravity(Gravity.CENTER);
        radioButton.setPadding(mPadding, 0, mPadding, 0);
        mRadioGroup.addView(radioButton, lpItem);
    }

    /**
     * 获取分类
     */
    private void getApiList() {
        InscriptionInfo info = new InscriptionInfo();
        info.setMenu("热门");
        info.setType("1");
        mApiList.add(info);

        info = new InscriptionInfo();
        info.setMenu("励志能量");
        info.setType("2");
        mApiList.add(info);

        info = new InscriptionInfo();
        info.setMenu("情感语录");
        info.setType("3");
        mApiList.add(info);

        info = new InscriptionInfo();
        info.setMenu("搞笑段子");
        info.setType("4");
        mApiList.add(info);

        mHandler.sendEmptyMessage(MSG_API);
    }

    private final int MSG_API = 500;

    private void initHandler() {
        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.what == MSG_API) {
                    int len = mApiList.size();
                    if (len > 0) {
                        int itemHeightpx = CoreUtils.dpToPixel(24);
                        RadioGroup.LayoutParams lpItem;
                        if (len <= 5) {
                            setRadioGroupFill();
                            lpItem = new RadioGroup.LayoutParams(0, itemHeightpx);
                            lpItem.weight = 1;
                        } else {
                            lpItem = new RadioGroup.LayoutParams(RadioGroup.LayoutParams.WRAP_CONTENT,
                                    itemHeightpx);
                        }
                        int dp2px = CoreUtils.dpToPixel(6);
                        lpItem.leftMargin = dp2px;
                        lpItem.rightMargin = dp2px;
                        mPadding = dp2px;
                        for (int i = 0; i < len; i++) {
                            createRadioButton(mApiList.get(i).getMenu(), lpItem);
                        }
                        initViewPager();
                    } else {
                        SysAlertDialog.cancelLoadingDialog();
                        onToast(getString(R.string.load_http_failed));
                    }
                }
                return false;
            }
        });
    }

    /**
     * 让容器天充满整个横屏(match 无效,必须设置widthpix)
     */
    private void setRadioGroupFill() {
        int w = CoreUtils.getMetrics().widthPixels;
        mRadioGroup.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
                        w, CoreUtils.dpToPixel(50)));
    }

    private void initViewPager() {
        if (mPageAdapter == null) {
            mPageAdapter = new InscriptPageAdapter(this.getChildFragmentManager());
        }
        mViewPager.setAdapter(mPageAdapter);
        int len = mApiList.size();
        if (len > 0) {
            mViewPager.setOffscreenPageLimit(len);
            mRadioGroup.check(mApiList.get(0).getMenu().hashCode());
        }
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            public void onPageScrollStateChanged(int paramAnonymousInt) {
            }

            public void onPageScrolled(int paramAnonymousInt1,
                                       float paramAnonymousFloat, int paramAnonymousInt2) {

            }

            public void onPageSelected(int arg0) {

                if (mApiList.size() > 0) {
                    int id = mApiList.get(arg0).getMenu().hashCode();
                    mRadioGroup.check(id);
                }
            }
        });
    }

    @Override
    public int onBackPressed() {
        if (mLlFragment.getVisibility() == View.VISIBLE) {
            mLlFragment.setVisibility(View.GONE);
        } else {
            if (mListener != null) {
                mListener.cancel();
            }
        }
        return super.onBackPressed();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
        mApiList.clear();
    }

    private InscriptionFragment mInscriptionFragment;

    private class InscriptPageAdapter extends FragmentPagerAdapter {

        private ArrayList<Fragment> fragments = new ArrayList<Fragment>();

        public InscriptPageAdapter(FragmentManager fm) {
            super(fm);
            for (InscriptionInfo info : mApiList) {
                mInscriptionFragment = InscriptionFragment.newInstance(info.getType());
                mInscriptionFragment.setListener(new InscriptionFragment.OnInscriptionClickListener() {

                    @Override
                    public void sure() {
                        if (mListener != null) {
                            mListener.sure();
                        }
                    }

                    @Override
                    public void onDetail(String title, ArrayList<String> content) {
                        //查看详情
                        if (mDetailFragment == null) {
                            mDetailFragment = InscriptionDetailFragment.newInstance(title, content);
                            mDetailFragment.setListener(new InscriptionListener() {
                                @Override
                                public void cancel() {
                                    mLlFragment.setVisibility(View.GONE);
                                }

                                @Override
                                public void sure() {
                                    if (mListener != null) {
                                        mListener.sure();
                                    }
                                }

                                @Override
                                public void onCustom() {

                                }
                            });
                            FragmentTransaction ft = getChildFragmentManager().beginTransaction();
                            ft.replace(R.id.ll_fragment, mDetailFragment);
                            ft.commitAllowingStateLoss();
                        } else {
                            mDetailFragment.setContent(title, content);
                        }
                        mLlFragment.setVisibility(View.VISIBLE);
                    }

                });
                fragments.add(mInscriptionFragment);
            }
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public Fragment getItem(int paramInt) {
            return fragments.get(paramInt);
        }
    }

    public class InscriptionInfo {
        private String menu;//菜单名
        private String type;//分类

        public String getMenu() {
            return menu;
        }

        public void setMenu(String menu) {
            this.menu = menu;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }

    private InscriptionListener mListener;

    public void setListener(InscriptionListener listener) {
        mListener = listener;
    }

    public interface InscriptionListener {

        /**
         * 返回
         */
        void cancel();

        /**
         * 点击选择使用
         */
        void sure();

        /**
         * 自定义题词
         */
        void onCustom();

    }

}
