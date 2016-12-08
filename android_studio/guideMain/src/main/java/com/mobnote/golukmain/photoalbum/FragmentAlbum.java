package com.mobnote.golukmain.photoalbum;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.carrecorder.view.CustomDialog;
import com.mobnote.golukmain.carrecorder.view.CustomDialog.OnLeftClickListener;
import com.mobnote.golukmain.carrecorder.view.CustomDialog.OnRightClickListener;
import com.mobnote.golukmain.promotion.PromotionSelectItem;
import com.mobnote.util.GolukUtils;
import com.mobnote.util.ZhugeUtils;

import java.util.ArrayList;
import java.util.List;

import cn.com.tiros.debug.GolukDebugUtils;

public class FragmentAlbum extends Fragment implements OnClickListener {

    /**
     * 活动分享
     */
    public static final String ACTIVITY_INFO = "activityinfo";
    private static final String TAG = "FragmentAlbum";
    public static final String PARENT_VIEW = "MainActivity";

    private CustomViewPager mViewPager;
    private LocalFragment mLocalFragment;
    private WonderfulFragment mWonderfulFragment;
    private LoopFragment mLoopFragment;
    private UrgentFragment mUrgentFragment;

    private LinearLayout mLinearLayoutTab;

    // 四个tab
    private TextView mTabLocal;
    private TextView mTabWonderful;
    private TextView mTabUrgent;
    private TextView mTabLoop;
    private CheckBox mCBAll;
    private Button mCancelBtn;

    private ImageView mEditBtn;
    private RelativeLayout mEditLayout = null;
    private TextView mTitleName = null;

    private ImageView mDownLoadIcon = null;
    private ImageView mDeleteIcon = null;

    /**
     * 0:本地 1:远程精彩 2：远程紧急 3：远程循环
     **/
    public int mCurrentType = 0;

    // 页面列表
    private ArrayList<Fragment> fragmentList;

    private View mAlbumRootView;

    private boolean editState = false;

    private LinearLayout mDownLoadBtn = null;
    private LinearLayout mLLAll;

    private List<String> selectedListData = null;

    /**
     * Fragment的父节点只会是{@link com.mobnote.golukmain.MainActivity} 和  {@link com.mobnote.golukmain.photoalbum.PhotoAlbumActivity}
     * 如果为true 表示父页面为MainActivity，否则相反
     */
    public boolean parentViewIsMainActivity = true;

    public PromotionSelectItem mPromotionSelectItem;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            parentViewIsMainActivity = bundle.getBoolean(PARENT_VIEW,true);
        }

        if (savedInstanceState == null) {
            mPromotionSelectItem = (PromotionSelectItem) getActivity().getIntent().getSerializableExtra(ACTIVITY_INFO);
        } else {
            mPromotionSelectItem = (PromotionSelectItem) savedInstanceState.getSerializable(ACTIVITY_INFO);
        }

        mAlbumRootView = inflater.inflate(R.layout.photo_album, container, false);
        editState = false;
        mViewPager = (CustomViewPager) mAlbumRootView.findViewById(R.id.viewpager);
        mViewPager.setOffscreenPageLimit(1);
        mLocalFragment = new LocalFragment();
        mWonderfulFragment = new WonderfulFragment(); // WonderfulFragment.newInstance(IPCManagerFn.TYPE_SHORTCUT,
        // IPCManagerFn.TYPE_SHORTCUT);
        mLoopFragment = new LoopFragment();// newInstance(IPCManagerFn.TYPE_CIRCULATE,
        // IPCManagerFn.TYPE_CIRCULATE);
        mUrgentFragment = new UrgentFragment(); // newInstance(IPCManagerFn.TYPE_URGENT,
        // IPCManagerFn.TYPE_URGENT);
        selectedListData = new ArrayList<>();

        fragmentList = new ArrayList<>();
        fragmentList.add(mLocalFragment);
        fragmentList.add(mWonderfulFragment);
        fragmentList.add(mUrgentFragment);
        fragmentList.add(mLoopFragment);
        mViewPager.setCurrentItem(0);
        initView();
        mViewPager.setAdapter(new MyViewPagerAdapter(getChildFragmentManager()));
        mViewPager.setOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                GolukDebugUtils.e("", "crash zh start App ------ FragmentAlbum-----onPageSelected------------:");
                if (mCurrentType == PhotoAlbumConfig.PHOTO_BUM_IPC_WND) {
                    mWonderfulFragment.removeFooterView();
                } else if (mCurrentType == PhotoAlbumConfig.PHOTO_BUM_IPC_URG) {
                    mUrgentFragment.removeFooterView();
                } else if (mCurrentType == PhotoAlbumConfig.PHOTO_BUM_IPC_LOOP) {
                    mLoopFragment.removeFooterView();
                }
                mCurrentType = position;
                setItemLineState(position);
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });
        return mAlbumRootView;
    }

    public void initView() {
        mTabLocal = (TextView) mAlbumRootView.findViewById(R.id.tab_local);
        mTabWonderful = (TextView) mAlbumRootView.findViewById(R.id.tab_wonderful);
        mTabUrgent = (TextView) mAlbumRootView.findViewById(R.id.tab_urgent);
        mTabLoop = (TextView) mAlbumRootView.findViewById(R.id.tab_loop);
        mEditBtn = (ImageView) mAlbumRootView.findViewById(R.id.edit_btn);
        mEditLayout = (RelativeLayout) mAlbumRootView.findViewById(R.id.mEditLayout);
        mTitleName = (TextView) mAlbumRootView.findViewById(R.id.video_title_text);
        mLinearLayoutTab = (LinearLayout) mAlbumRootView.findViewById(R.id.tab_type);
        mDownLoadBtn = (LinearLayout) mAlbumRootView.findViewById(R.id.mDownLoadBtn);
        mLLAll = (LinearLayout) mAlbumRootView.findViewById(R.id.ll_select_all);
        mDownLoadIcon = (ImageView) mAlbumRootView.findViewById(R.id.mDownLoadIcon);
        mDeleteIcon = (ImageView) mAlbumRootView.findViewById(R.id.mDeleteIcon);
        mCancelBtn = (Button) mAlbumRootView.findViewById(R.id.cancel_btn);
        ImageView mBackBtn = (ImageView) mAlbumRootView.findViewById(R.id.back_btn);
        mCBAll = (CheckBox) mAlbumRootView.findViewById(R.id.cb_select_all);
        if (parentViewIsMainActivity) {
            mBackBtn.setVisibility(View.GONE);
        } else {
            mBackBtn.setVisibility(View.VISIBLE);
        }

        mCancelBtn.setOnClickListener(this);
        mDownLoadBtn.setOnClickListener(this);
        mBackBtn.setOnClickListener(this);
        mAlbumRootView.findViewById(R.id.mDeleteBtn).setOnClickListener(this);
        mTabLocal.setOnClickListener(this);
        mTabWonderful.setOnClickListener(this);
        mTabUrgent.setOnClickListener(this);
        mTabLoop.setOnClickListener(this);
        mEditBtn.setOnClickListener(this);
        mLLAll.setOnClickListener(this);
        mCBAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mCBAll.setText(isChecked?R.string.un_select_all:R.string.select_all);
                mLocalFragment.allSelect(isChecked);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        GolukDebugUtils.e(TAG, "FragmentAlbum-----onResume------------:");
        GolukApplication.getInstance().setContext(getActivity(), "ipcfilemanager");

        if (parentViewIsMainActivity && !getEditState()) {
            if (GolukApplication.getInstance().isIpcLoginSuccess) {
                if (mCurrentType == PhotoAlbumConfig.PHOTO_BUM_IPC_WND) {
                    if (!mWonderfulFragment.isShowPlayer) {
                        mWonderfulFragment.loadData(true);
                    }

                } else if (mCurrentType == PhotoAlbumConfig.PHOTO_BUM_IPC_URG) {
                    if (!mUrgentFragment.isShowPlayer) {
                        mUrgentFragment.loadData(true);
                    }
                } else if (mCurrentType == PhotoAlbumConfig.PHOTO_BUM_IPC_LOOP) {
                    if (!mLoopFragment.isShowPlayer) {
                        mLoopFragment.loadData(true);
                    }

                }
            }
        }

    }

    /**
     * 设置tab页的下划线显示和隐藏
     *
     * @param position 位置index
     */
    public void setItemLineState(int position) {
        mTabLocal.setTextColor(this.getResources().getColor(R.color.photoalbum_text_color_def));
        mTabWonderful.setTextColor(this.getResources().getColor(R.color.photoalbum_text_color_def));
        mTabUrgent.setTextColor(this.getResources().getColor(R.color.photoalbum_text_color_def));
        mTabLoop.setTextColor(this.getResources().getColor(R.color.photoalbum_text_color_def));
        if (position == 0) {
            mLocalFragment.loadData(true);
            mTabLocal.setTextColor(this.getResources().getColor(R.color.photoalbum_text_color));
        } else if (position == 1) {
            mWonderfulFragment.loadData(GolukApplication.getInstance().isIpcLoginSuccess);
            mTabWonderful.setTextColor(this.getResources().getColor(R.color.photoalbum_text_color));
        } else if (position == 2) {
            mUrgentFragment.loadData(GolukApplication.getInstance().isIpcLoginSuccess);
            mTabUrgent.setTextColor(this.getResources().getColor(R.color.photoalbum_text_color));
        } else if (position == 3) {
            mLoopFragment.loadData(GolukApplication.getInstance().isIpcLoginSuccess);
            mTabLoop.setTextColor(this.getResources().getColor(R.color.photoalbum_text_color));
        }
    }

    public class MyViewPagerAdapter extends FragmentPagerAdapter {
        public MyViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int arg0) {
            return fragmentList.get(arg0);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

    }

    private void downloadVideoFlush() {
        if (GolukApplication.getInstance().getIpcIsLogin()) {
            if (mCurrentType != PhotoAlbumConfig.PHOTO_BUM_LOCAL) {
                //相册页面-批量下载到本地
                ZhugeUtils.eventAlbumBatchDownload(getActivity(), mCurrentType);
                if (mCurrentType == PhotoAlbumConfig.PHOTO_BUM_IPC_WND) {
                    mWonderfulFragment.downloadVideoFlush(selectedListData);
                } else if (mCurrentType == PhotoAlbumConfig.PHOTO_BUM_IPC_URG) {
                    mUrgentFragment.downloadVideoFlush(selectedListData);
                } else if (mCurrentType == PhotoAlbumConfig.PHOTO_BUM_IPC_LOOP) {
                    mLoopFragment.downloadVideoFlush(selectedListData);
                }

            }
        } else {
            GolukUtils.showToast(getActivity(), getResources().getString(R.string.str_photo_check_ipc_state));
        }
        resetEditState();
        setEditBtnState(true);
        GolukUtils.setTabHostVisibility(true, getActivity());
    }

    private void resetEditState() {
        mViewPager.setCanScroll(true);
        // mEditBtn.setVisibility(View.VISIBLE);
        mCancelBtn.setVisibility(View.GONE);
        mLinearLayoutTab.setVisibility(View.VISIBLE);
        mDownLoadIcon.setBackgroundResource(R.drawable.photo_download_icon);
        mDeleteIcon.setBackgroundResource(R.drawable.select_video_del_icon);
        editState = false;
        mTitleName.setVisibility(View.GONE);
        mEditLayout.setVisibility(View.GONE);
        selectedListData.clear();

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.tab_local) {
            mViewPager.setCurrentItem(0);
            mCurrentType = PhotoAlbumConfig.PHOTO_BUM_LOCAL;
        } else if (id == R.id.tab_wonderful) {
            mViewPager.setCurrentItem(1);
            mCurrentType = PhotoAlbumConfig.PHOTO_BUM_IPC_WND;
        } else if (id == R.id.tab_urgent) {
            mViewPager.setCurrentItem(2);
            mCurrentType = PhotoAlbumConfig.PHOTO_BUM_IPC_URG;
        } else if (id == R.id.tab_loop) {
            mViewPager.setCurrentItem(3);
            mCurrentType = PhotoAlbumConfig.PHOTO_BUM_IPC_LOOP;
        } else if (id == R.id.edit_btn) {
            mViewPager.setCanScroll(false);
            updateEditState();
        } else if (id == R.id.mDownLoadBtn) {
            if (selectedListData.size() <= 0) {
                return;
            }
            downloadVideoFlush();
        }else if(id == R.id.ll_select_all){
            if (selectedListData.size() <= 0) {
                return;
            }
            mCBAll.setChecked(!mCBAll.isChecked());
        }else if (id == R.id.cancel_btn) {
            resetEditState();
            setEditBtnState(true);
            GolukUtils.setTabHostVisibility(true, getActivity());
        } else if (id == R.id.back_btn) {
            getActivity().finish();
        } else if (id == R.id.mDeleteBtn) {
            if (selectedListData.size() <= 0) {
                return;
            }
            if (mCurrentType != PhotoAlbumConfig.PHOTO_BUM_LOCAL) {
                if (!isAllowedDelete()) {
                    GolukUtils.showToast(getActivity(), getResources().getString(R.string.str_photo_downing));
                    return;
                }

                if (!GolukApplication.getInstance().getIpcIsLogin()) {
                    resetEditState();
                    GolukUtils.showToast(getActivity(), getResources().getString(R.string.str_photo_check_ipc_state));
                    return;
                }
            }
            CustomDialog mCustomDialog = new CustomDialog(getActivity());
            mCustomDialog.setMessage(this.getString(R.string.str_photo_deletepromote, selectedListData.size()),
                    Gravity.CENTER);
            mCustomDialog.setLeftButton(getResources().getString(R.string.str_phote_delete_ok),
                    new OnLeftClickListener() {
                        @Override
                        public void onClickListener() {
                            editState = false;
                            deleteDataFlush();
                        }
                    });
            mCustomDialog.setRightButton(getResources().getString(R.string.dialog_str_cancel),
                    new OnRightClickListener() {
                        @Override
                        public void onClickListener() {
                            setEditBtnState(true);
                            GolukUtils.setTabHostVisibility(true, getActivity());
                            resetEditState();
                        }
                    });
            mCustomDialog.show();
        }
    }

    private void deleteDataFlush() {

        //相册页面-批量删除视频
        ZhugeUtils.eventAlbumBatchDelete(getActivity(), mCurrentType);

        if (mCurrentType != PhotoAlbumConfig.PHOTO_BUM_LOCAL) {
            if (mCurrentType == PhotoAlbumConfig.PHOTO_BUM_IPC_WND) {
                mWonderfulFragment.deleteListData(selectedListData);
            } else if (mCurrentType == PhotoAlbumConfig.PHOTO_BUM_IPC_URG) {
                mUrgentFragment.deleteListData(selectedListData);
            } else if (mCurrentType == PhotoAlbumConfig.PHOTO_BUM_IPC_LOOP) {
                mLoopFragment.deleteListData(selectedListData);
            }
        } else {
            mLocalFragment.deleteListData(selectedListData);
        }

        resetEditState();
        GolukUtils.setTabHostVisibility(true, getActivity());
        GolukUtils.showToast(this.getActivity(), getResources().getString(R.string.str_photo_delete_ok));
    }

    private boolean isAllowedDelete() {
        boolean downloading = true;
        List<String> dlist = GolukApplication.getInstance().getDownLoadList();
        for (String name : selectedListData) {
            if (dlist.contains(name)) {
                downloading = false;
                break;
            }
        }

        return downloading;
    }

    private void updateEditState() {
        if (!editState) {
            editState = true;

            // mEditBtn.setText(this.getResources().getString(R.string.short_input_cancel));
            mTitleName.setVisibility(View.VISIBLE);
            mCancelBtn.setVisibility(View.VISIBLE);
            mEditBtn.setVisibility(View.GONE);
            GolukUtils.setTabHostVisibility(false, getActivity());
            mTitleName.setText(this.getResources().getString(R.string.local_video_title_text));

            mLinearLayoutTab.setVisibility(View.GONE);
            mEditLayout.setVisibility(View.VISIBLE);

            if (mCurrentType == PhotoAlbumConfig.PHOTO_BUM_LOCAL) {
                mDownLoadBtn.setVisibility(View.GONE);
                mLLAll.setVisibility(View.VISIBLE);
            } else if (mCurrentType == PhotoAlbumConfig.PHOTO_BUM_IPC_WND) {
                mDownLoadBtn.setVisibility(View.VISIBLE);
                mLLAll.setVisibility(View.GONE);
            } else if (mCurrentType == PhotoAlbumConfig.PHOTO_BUM_IPC_URG) {
                mDownLoadBtn.setVisibility(View.VISIBLE);
                mLLAll.setVisibility(View.GONE);
            } else if (mCurrentType == PhotoAlbumConfig.PHOTO_BUM_IPC_LOOP) {
                mDownLoadBtn.setVisibility(View.VISIBLE);
                mLLAll.setVisibility(View.GONE);
            }
        }
    }

    public boolean getEditState() {
        return editState;
    }

    public List<String> getSelectedList() {
        return selectedListData;
    }

    public void updateDeleteState(boolean light) {
        if (light) {
            mDeleteIcon.setBackgroundResource(R.drawable.select_video_del_press_icon);
            mDownLoadIcon.setBackgroundResource(R.drawable.photo_download_icon_press);
        } else {
            mDeleteIcon.setBackgroundResource(R.drawable.select_video_del_icon);
            mDownLoadIcon.setBackgroundResource(R.drawable.photo_download_icon);
        }
    }

    public void updateTitleName(String titlename) {
        mTitleName.setText(titlename);
    }

    public void setEditBtnState(boolean isShow) {
        if (null == mEditBtn) {
            return;
        }
        if (isShow) {
            mEditBtn.setVisibility(View.VISIBLE);
        } else {
            mEditBtn.setVisibility(View.GONE);
        }
    }
}
