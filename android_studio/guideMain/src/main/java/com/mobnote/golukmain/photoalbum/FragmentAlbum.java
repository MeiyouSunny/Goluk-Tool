package com.mobnote.golukmain.photoalbum;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.carrecorder.CarRecorderActivity;
import com.mobnote.golukmain.carrecorder.view.CustomDialog;
import com.mobnote.golukmain.carrecorder.view.CustomDialog.OnLeftClickListener;
import com.mobnote.golukmain.carrecorder.view.CustomDialog.OnRightClickListener;
import com.mobnote.golukmain.promotion.PromotionSelectItem;
import com.mobnote.golukmain.wifibind.WiFiLinkListActivity;
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
    public static final String SELECT_MODE = "select_item";

    private CustomViewPager mViewPager;
    private LocalFragment mLocalFragment;
    private TimeslapseFragment mTimeslapseFragment;
    private WonderfulFragment mWonderfulFragment;
    private LoopFragment mLoopFragment;
    private UrgentFragment mUrgentFragment;

    private LinearLayout mLinearLayoutTab;

    // 四个tab
    private TextView mTabLocal, mTabTimeslapse, mTabWonderful, mTabUrgent, mTabLoop;
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
    private ImageView mBackBtn;

    private List<String> selectedListData = null;

    /**
     * Fragment的父节点只会是{@link com.mobnote.golukmain.MainActivity} 和  {@link com.mobnote.golukmain.photoalbum.PhotoAlbumActivity}
     * 如果为true 表示父页面为MainActivity，否则相反
     */
    public boolean parentViewIsMainActivity = true;
    public boolean selectMode = false;
    public boolean fromCloud = false;

    public PromotionSelectItem mPromotionSelectItem;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            parentViewIsMainActivity = bundle.getBoolean(PARENT_VIEW, true);
            selectMode = bundle.getBoolean(SELECT_MODE, false);
            fromCloud = bundle.getBoolean("from", false);
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

        if (isSupportTimeslapse())
            mTimeslapseFragment = new TimeslapseFragment();

        mWonderfulFragment = new WonderfulFragment(); // WonderfulFragment.newInstance(IPCManagerFn.TYPE_SHORTCUT,
        // IPCManagerFn.TYPE_SHORTCUT);
        mLoopFragment = new LoopFragment();// newInstance(IPCManagerFn.TYPE_CIRCULATE,
        // IPCManagerFn.TYPE_CIRCULATE);
        mUrgentFragment = new UrgentFragment(); // newInstance(IPCManagerFn.TYPE_URGENT,
        // IPCManagerFn.TYPE_URGENT);
        selectedListData = new ArrayList<>();

        fragmentList = new ArrayList<>();
        if (parentViewIsMainActivity) {
            fragmentList.add(mLocalFragment);
            mCurrentType = PhotoAlbumConfig.PHOTO_BUM_LOCAL;
        } else {
            fragmentList.add(mWonderfulFragment);
            fragmentList.add(mUrgentFragment);
            if (isSupportTimeslapse())
                fragmentList.add(mTimeslapseFragment);
            fragmentList.add(mLoopFragment);
            mCurrentType = PhotoAlbumConfig.PHOTO_BUM_IPC_WND;
        }

        mViewPager.setCurrentItem(0);
        initView();
        mViewPager.setAdapter(new MyViewPagerAdapter(getChildFragmentManager()));
        mViewPager.setOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                GolukDebugUtils.e("", "crash zh start App ------ FragmentAlbum-----onPageSelected------------:");
                if (mCurrentType == PhotoAlbumConfig.PHOTO_BUM_IPC_TIMESLAPSE) {
                    mTimeslapseFragment.removeFooterView();
                } else if (mCurrentType == PhotoAlbumConfig.PHOTO_BUM_IPC_WND) {
                    mWonderfulFragment.removeFooterView();
                } else if (mCurrentType == PhotoAlbumConfig.PHOTO_BUM_IPC_URG) {
                    mUrgentFragment.removeFooterView();
                } else if (mCurrentType == PhotoAlbumConfig.PHOTO_BUM_IPC_LOOP) {
                    mLoopFragment.removeFooterView();
                }

                if (parentViewIsMainActivity) {
                    mCurrentType = PhotoAlbumConfig.PHOTO_BUM_LOCAL;
                } else {
                    if (position == 0) {
                        mCurrentType = PhotoAlbumConfig.PHOTO_BUM_IPC_WND;
                    } else if (position == 1) {
                        mCurrentType = PhotoAlbumConfig.PHOTO_BUM_IPC_URG;
                    } else if (position == 2 && isSupportTimeslapse()) {
                        mCurrentType = PhotoAlbumConfig.PHOTO_BUM_IPC_TIMESLAPSE;
                    } else if (((position == 2) && !isSupportTimeslapse())
                            || ((position == 3) && isSupportTimeslapse())) {
                        mCurrentType = PhotoAlbumConfig.PHOTO_BUM_IPC_LOOP;
                    }
                }
//                mCurrentType = position;
//                setItemLineState(position);
                setItemLineState(mCurrentType);
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
        mTabTimeslapse = (TextView) mAlbumRootView.findViewById(R.id.tab_timeslapse);
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
        mBackBtn = (ImageView) mAlbumRootView.findViewById(R.id.back_btn);
        mCBAll = (CheckBox) mAlbumRootView.findViewById(R.id.cb_select_all);
        if (parentViewIsMainActivity) {
            mBackBtn.setVisibility(View.VISIBLE);
            if (selectMode) {
                mTabLocal.setText(R.string.str_ae_add_video_title);
            }
            if (!selectMode && !fromCloud) {
                mBackBtn.setImageResource(R.drawable.remote_album_sd);
                mBackBtn.setBackgroundResource(0);
            }
            mTabLocal.setVisibility(View.VISIBLE);
            mTabTimeslapse.setVisibility(View.GONE);
            mTabWonderful.setVisibility(View.GONE);
            mTabUrgent.setVisibility(View.GONE);
            mTabLoop.setVisibility(View.GONE);
        } else {
            mBackBtn.setVisibility(View.VISIBLE);
            mTabLocal.setVisibility(View.GONE);
            mTabTimeslapse.setVisibility(View.VISIBLE);
            mTabWonderful.setVisibility(View.VISIBLE);
            mTabUrgent.setVisibility(View.VISIBLE);
            mTabLoop.setVisibility(View.VISIBLE);
        }

        mCancelBtn.setOnClickListener(this);
        mDownLoadBtn.setOnClickListener(this);
        mBackBtn.setOnClickListener(this);
        mAlbumRootView.findViewById(R.id.mDeleteBtn).setOnClickListener(this);
        mTabLocal.setOnClickListener(this);
        mTabTimeslapse.setOnClickListener(this);
        mTabWonderful.setOnClickListener(this);
        mTabUrgent.setOnClickListener(this);
        mTabLoop.setOnClickListener(this);
        mEditBtn.setOnClickListener(this);
        mLLAll.setOnClickListener(this);
        mCBAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                boolean all = mCBAll.getText().equals(getString(R.string.select_all));
                adaptCbAllText(!all);
                mLocalFragment.allSelect(all);
            }
        });

        if (!isSupportTimeslapse())
            mTabTimeslapse.setVisibility(View.GONE);
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

                } else if (mCurrentType == PhotoAlbumConfig.PHOTO_BUM_IPC_TIMESLAPSE) {
                    if (!mTimeslapseFragment.isShowPlayer) {
                        mTimeslapseFragment.loadData(true);
                    }
                } else if (mCurrentType == PhotoAlbumConfig.PHOTO_BUM_IPC_URG) {
                    if (!mUrgentFragment.isShowPlayer) {
                        mUrgentFragment.loadData(true);
                    }
                } else if (mCurrentType == PhotoAlbumConfig.PHOTO_BUM_IPC_LOOP) {
                    if (!mLoopFragment.isShowPlayer) {
                        mLoopFragment.loadData(true);
                    }
                } else if (mCurrentType == PhotoAlbumConfig.PHOTO_BUM_LOCAL) {
                    mLocalFragment.loadData(true);
                }
            }
        }

    }

    /**
     * 设置tab页的下划线显示和隐藏
     *
     * @param position 位置index
     */
    public void setItemLineState(int currentType) {
        mTabLocal.setTextColor(this.getResources().getColor(R.color.photoalbum_text_color_def));
        mTabTimeslapse.setTextColor(this.getResources().getColor(R.color.photoalbum_text_color_def));
        mTabWonderful.setTextColor(this.getResources().getColor(R.color.photoalbum_text_color_def));
        mTabUrgent.setTextColor(this.getResources().getColor(R.color.photoalbum_text_color_def));
        mTabLoop.setTextColor(this.getResources().getColor(R.color.photoalbum_text_color_def));
        if (currentType == PhotoAlbumConfig.PHOTO_BUM_LOCAL) {
            mLocalFragment.loadData(true);
            mTabLocal.setTextColor(this.getResources().getColor(R.color.photoalbum_text_color));
        } else if (currentType == PhotoAlbumConfig.PHOTO_BUM_IPC_TIMESLAPSE) {
            mTimeslapseFragment.loadData(GolukApplication.getInstance().isIpcLoginSuccess);
            mTabTimeslapse.setTextColor(this.getResources().getColor(R.color.photoalbum_text_color));
        } else if (currentType == PhotoAlbumConfig.PHOTO_BUM_IPC_WND) {
            mWonderfulFragment.loadData(GolukApplication.getInstance().isIpcLoginSuccess);
            mTabWonderful.setTextColor(this.getResources().getColor(R.color.photoalbum_text_color));
        } else if (currentType == PhotoAlbumConfig.PHOTO_BUM_IPC_URG) {
            mUrgentFragment.loadData(GolukApplication.getInstance().isIpcLoginSuccess);
            mTabUrgent.setTextColor(this.getResources().getColor(R.color.photoalbum_text_color));
        } else if (currentType == PhotoAlbumConfig.PHOTO_BUM_IPC_LOOP) {
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
                } else if (mCurrentType == PhotoAlbumConfig.PHOTO_BUM_IPC_TIMESLAPSE) {
                    mTimeslapseFragment.downloadVideoFlush(selectedListData);
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
        mBackBtn.setVisibility(View.VISIBLE);
        mTitleName.setVisibility(View.GONE);
        mEditLayout.setVisibility(View.GONE);
        selectedListData.clear();
        adaptCbAllText(true);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.tab_local) {
            mViewPager.setCurrentItem(0);
            mCurrentType = PhotoAlbumConfig.PHOTO_BUM_LOCAL;
        } else if (id == R.id.tab_wonderful) {
            mViewPager.setCurrentItem(0);
            mCurrentType = PhotoAlbumConfig.PHOTO_BUM_IPC_WND;
        } else if (id == R.id.tab_urgent) {
            mViewPager.setCurrentItem(1);
            mCurrentType = PhotoAlbumConfig.PHOTO_BUM_IPC_URG;
        } else if (id == R.id.tab_timeslapse) {
            mViewPager.setCurrentItem(2);
            mCurrentType = PhotoAlbumConfig.PHOTO_BUM_IPC_TIMESLAPSE;
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
        } else if (id == R.id.ll_select_all) {
            if (selectedListData.size() <= 0) {
                return;
            }
            mCBAll.setChecked(!mCBAll.isChecked());
        } else if (id == R.id.cancel_btn) {
            resetEditState();
            setEditBtnState(true);
            GolukUtils.setTabHostVisibility(true, getActivity());
        } else if (id == R.id.back_btn) {
            if (parentViewIsMainActivity && !selectMode && !fromCloud) {
                final PopupMenu mPopMenu = new PopupMenu(getContext(), mBackBtn);
                mPopMenu.getMenuInflater().inflate(R.menu.menu_album_change, mPopMenu.getMenu());
                mPopMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        mPopMenu.dismiss();
                        if (item.getItemId() == R.id.action_sd) {
                            if (GolukApplication.getInstance().getIpcIsLogin()) {
                                Intent photoalbum = new Intent(FragmentAlbum.this.getActivity(), PhotoAlbumActivity.class);
                                photoalbum.putExtra("from", "cloud");
                                startActivity(photoalbum);
                            } else {
                                Intent intent = new Intent(getContext(), WiFiLinkListActivity.class);
                                intent.putExtra(WiFiLinkListActivity.ACTION_GO_To_ALBUM, true);
                                startActivity(intent);
                            }
                        }
                        return false;
                    }
                });
                mPopMenu.show();
            } else {
                PhotoAlbumActivity activity = (PhotoAlbumActivity) getActivity();
                activity.onBackPressed();
            }
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

    public void checkDowningExit() {
        if (GolukApplication.getInstance().getDownLoadList() == null
                || GolukApplication.getInstance().getDownLoadList().size() == 0
                || !GolukApplication.getInstance().isDownloading()) {
            getActivity().finish();
        } else {
            preExit();
        }
    }

    private void preExit() {
        final AlertDialog dialog = new AlertDialog.Builder(getContext()).create();
        dialog.setTitle(getString(R.string.str_global_dialog_title));
        dialog.setMessage(getString(R.string.msg_of_exit_when_download));
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.str_button_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialog.dismiss();
                GolukApplication.getInstance().stopDownloadList();
                getActivity().finish();
            }
        });
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.dialog_str_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialog.dismiss();
            }
        });
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }

    private void deleteDataFlush() {

        //相册页面-批量删除视频
        ZhugeUtils.eventAlbumBatchDelete(getActivity(), mCurrentType);

        if (mCurrentType != PhotoAlbumConfig.PHOTO_BUM_LOCAL) {
            if (mCurrentType == PhotoAlbumConfig.PHOTO_BUM_IPC_WND) {
                mWonderfulFragment.deleteListData(selectedListData);
            } else if (mCurrentType == PhotoAlbumConfig.PHOTO_BUM_IPC_TIMESLAPSE) {
                mTimeslapseFragment.deleteListData(selectedListData);
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
            mBackBtn.setVisibility(View.GONE);
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
            } else if (mCurrentType == PhotoAlbumConfig.PHOTO_BUM_IPC_TIMESLAPSE) {
                mDownLoadBtn.setVisibility(View.VISIBLE);
                mLLAll.setVisibility(View.GONE);
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

    public void adaptCbAllText(boolean all) {
        mCBAll.setText(all ? R.string.select_all : R.string.un_select_all);
    }

    public void updateTitleName(String titlename) {
        mTitleName.setText(titlename);
    }

    public void setEditBtnState(boolean isShow) {
        if (null == mEditBtn) {
            return;
        }
        if (isShow && !selectMode) {
            mEditBtn.setVisibility(View.VISIBLE);
        } else {
            mEditBtn.setVisibility(View.GONE);
        }
    }

    private boolean isSupportTimeslapse() {
        return GolukApplication.getInstance().getIPCControlManager().isSupportTimeslapse();
    }

}
