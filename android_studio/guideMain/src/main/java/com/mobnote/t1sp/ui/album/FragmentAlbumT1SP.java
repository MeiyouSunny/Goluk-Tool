package com.mobnote.t1sp.ui.album;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
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
import com.mobnote.golukmain.photoalbum.CustomViewPager;
import com.mobnote.golukmain.photoalbum.PhotoAlbumConfig;
import com.mobnote.t1sp.util.CollectionUtils;
import com.mobnote.util.GolukUtils;
import com.mobnote.util.ZhugeUtils;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

public class FragmentAlbumT1SP extends Fragment implements OnClickListener, AlbumCloudAdapterListener {

    /**
     * 活动分享
     */
    public static final String ACTIVITY_INFO = "activityinfo";
    private static final String TAG = "FragmentAlbumT1SP";
    public static final String SELECT_MODE = "select_item";

    private CustomViewPager mViewPager;
    private RemoteTimelapseAlbumFragment mTimelapseFragment;
    private RemoteWonderfulAlbumFragment mWonderfulFragment;
    private RemoteLoopAlbumFragment mLoopFragment;
    private RemoteEventAlbumFragment mUrgentFragment;

    private LinearLayout mLinearLayoutTab;

    // 四个tab
    private TextView mTabTimelapse, mTabWonderful, mTabUrgent, mTabLoop;
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

    public boolean selectMode = false;
    public boolean fromCloud = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            selectMode = bundle.getBoolean(SELECT_MODE, false);
            fromCloud = bundle.getBoolean("from", false);
        }
        mAlbumRootView = inflater.inflate(R.layout.photo_album_t1sp, container, false);
        editState = false;
        mViewPager = (CustomViewPager) mAlbumRootView.findViewById(R.id.viewpager);
        mViewPager.setOffscreenPageLimit(3);
        mTimelapseFragment = new RemoteTimelapseAlbumFragment();
        mWonderfulFragment = new RemoteWonderfulAlbumFragment();
        mLoopFragment = new RemoteLoopAlbumFragment();
        mUrgentFragment = new RemoteEventAlbumFragment();
        selectedListData = new ArrayList<>();

        fragmentList = new ArrayList<>();
        fragmentList.add(mWonderfulFragment);
        fragmentList.add(mUrgentFragment);
        fragmentList.add(mTimelapseFragment);
        fragmentList.add(mLoopFragment);
        mCurrentType = PhotoAlbumConfig.PHOTO_BUM_IPC_WND;

        mViewPager.setCurrentItem(0);
        initView();
        mViewPager.setAdapter(new MyViewPagerAdapter(getChildFragmentManager()));
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                // 移除FooterView
                ((BaseRemoteAblumFragment) fragmentList.get(mCurrentType - 1)).removeFooterView();
                mCurrentType = position + 1;
                // 刷新Tab显示
                setItemLineState(mCurrentType);
                // 更新当前Fragment编辑按钮显示
                ((BaseRemoteAblumFragment) fragmentList.get(mCurrentType - 1)).onShow();
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

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //loadData();
    }

    public void initView() {
        mTabTimelapse = (TextView) mAlbumRootView.findViewById(R.id.tab_timelapse);
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

        mBackBtn.setVisibility(View.VISIBLE);

        mCancelBtn.setOnClickListener(this);
        mDownLoadBtn.setOnClickListener(this);
        mBackBtn.setOnClickListener(this);
        mAlbumRootView.findViewById(R.id.mDeleteBtn).setOnClickListener(this);
        mTabTimelapse.setOnClickListener(this);
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
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        GolukApplication.getInstance().setContext(getActivity(), "ipcfilemanager");
    }

    public void loadData() {
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
        } else if (mCurrentType == PhotoAlbumConfig.PHOTO_BUM_IPC_TIMESLAPSE) {
            if (!mTimelapseFragment.isShowPlayer) {
                mTimelapseFragment.loadData(true);
            }
        }
    }

    /**
     * 设置tab页的下划线显示和隐藏
     *
     * @param currentType 位置index
     */
    public void setItemLineState(int currentType) {
        mTabTimelapse.setTextColor(this.getResources().getColor(R.color.photoalbum_text_color_def));
        mTabWonderful.setTextColor(this.getResources().getColor(R.color.photoalbum_text_color_def));
        mTabUrgent.setTextColor(this.getResources().getColor(R.color.photoalbum_text_color_def));
        mTabLoop.setTextColor(this.getResources().getColor(R.color.photoalbum_text_color_def));

        if (currentType == PhotoAlbumConfig.PHOTO_BUM_IPC_WND) {
            mTabWonderful.setTextColor(this.getResources().getColor(R.color.photoalbum_text_color));
            if (!mWonderfulFragment.hasLoadedFirst())
                mWonderfulFragment.loadData(true);
        } else if (currentType == PhotoAlbumConfig.PHOTO_BUM_IPC_URG) {
            mTabUrgent.setTextColor(this.getResources().getColor(R.color.photoalbum_text_color));
            if (!mUrgentFragment.hasLoadedFirst())
                mUrgentFragment.loadData(true);
        } else if (currentType == PhotoAlbumConfig.PHOTO_BUM_IPC_LOOP) {
            mTabLoop.setTextColor(this.getResources().getColor(R.color.photoalbum_text_color));
            if (!mLoopFragment.hasLoadedFirst())
                mLoopFragment.loadData(true);
        } else if (currentType == PhotoAlbumConfig.PHOTO_BUM_IPC_TIMESLAPSE) {
            mTabTimelapse.setTextColor(this.getResources().getColor(R.color.photoalbum_text_color));
            if (!mTimelapseFragment.hasLoadedFirst())
                mTimelapseFragment.loadData(true);
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
                } else if (mCurrentType == PhotoAlbumConfig.PHOTO_BUM_IPC_TIMESLAPSE) {
                    mTimelapseFragment.downloadVideoFlush(selectedListData);
                }

            }
        } else {
            GolukUtils.showToast(getActivity(), getResources().getString(R.string.str_photo_check_ipc_state));
        }

        resetTopBar();
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
        adaptCbAllText(true);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.tab_wonderful) {
            mViewPager.setCurrentItem(0);
            mCurrentType = PhotoAlbumConfig.PHOTO_BUM_IPC_WND;
        } else if (id == R.id.tab_urgent) {
            mViewPager.setCurrentItem(1);
            mCurrentType = PhotoAlbumConfig.PHOTO_BUM_IPC_URG;
        } else if (id == R.id.tab_timelapse) {
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
            PhotoAlbumT1SPActivity activity = (PhotoAlbumT1SPActivity) getActivity();
            activity.onBackPressed();
        } else if (id == R.id.mDeleteBtn) {
            if (CollectionUtils.isEmpty(selectedListData))
                return;

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
//            mCustomDialog.setMessage(getString(R.string.str_photo_deletepromote, selectedListData.size()),
            mCustomDialog.setMessage(getString(R.string.str_photo_deletepromote),
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

        if (mCurrentType == PhotoAlbumConfig.PHOTO_BUM_IPC_WND) {
            mWonderfulFragment.deleteListData(selectedListData);
        } else if (mCurrentType == PhotoAlbumConfig.PHOTO_BUM_IPC_URG) {
            mUrgentFragment.deleteListData(selectedListData);
        } else if (mCurrentType == PhotoAlbumConfig.PHOTO_BUM_IPC_LOOP) {
            mLoopFragment.deleteListData(selectedListData);
        } else if (mCurrentType == PhotoAlbumConfig.PHOTO_BUM_IPC_TIMESLAPSE) {
            mTimelapseFragment.deleteListData(selectedListData);
        }
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

            mDownLoadBtn.setVisibility(View.VISIBLE);
            mLLAll.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean getEditState() {
        return editState;
    }

    @Override
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

    /**
     * 恢复顶部按钮状态(隐藏编辑,显示Tab)
     */
    public void resetTopBar() {
        resetEditState();
        GolukUtils.setTabHostVisibility(true, getActivity());
        setEditBtnState(true);
    }

}
