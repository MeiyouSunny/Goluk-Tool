package com.mobnote.golukmain.photoalbum;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.emilsjolander.components.stickylistheaders.StickyListHeadersListView;
import com.mobnote.application.GolukApplication;
import com.mobnote.eventbus.EventDeletePhotoAlbumVid;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.carrecorder.entity.DoubleVideoInfo;
import com.mobnote.golukmain.carrecorder.entity.VideoInfo;
import com.mobnote.golukmain.carrecorder.util.SettingUtils;
import com.mobnote.golukmain.carrecorder.util.SoundUtils;
import com.mobnote.golukmain.carrecorder.view.CustomLoadingDialog;
import com.mobnote.golukmain.fileinfo.GolukVideoInfoDbManager;
import com.mobnote.golukmain.promotion.PromotionSelectItem;
import com.mobnote.util.GolukUtils;
import com.mobnote.util.ZhugeUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.com.mobnote.module.ipcmanager.IPCManagerFn;
import cn.com.tiros.debug.GolukDebugUtils;
import de.greenrobot.event.EventBus;

public class LocalFragment extends Fragment {
    private View mLocalVideoView;

    private CustomLoadingDialog mCustomProgressDialog = null;

    private StickyListHeadersListView mStickyListHeadersListView = null;

    private LocalWonderfulVideoAdapter mWonderfulVideoAdapter = null;

    private List<VideoInfo> mDataList = null;
    private List<DoubleVideoInfo> mDoubleDataList = null;

    /**
     * 保存屏幕点击横坐标点
     */
    private float screenX = 0;
    private int screenWidth = 0;

    private float density = 1;

    private FragmentAlbum mFragmentAlbum;

    private TextView empty = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mFragmentAlbum = (FragmentAlbum) getParentFragment();

        if (mLocalVideoView == null) {
            mLocalVideoView = inflater.inflate(R.layout.wonderful_listview,
                    (ViewGroup) getActivity().findViewById(R.id.viewpager), false);
            density = SoundUtils.getInstance().getDisplayMetrics().density;

            this.mDataList = new ArrayList<VideoInfo>();
            this.mDoubleDataList = new ArrayList<DoubleVideoInfo>();
            this.screenWidth = SoundUtils.getInstance().getDisplayMetrics().widthPixels;
            initView();
            loadData(true);
        }

        ViewGroup parent = (ViewGroup) mLocalVideoView.getParent();
        if (parent != null) {
            parent.removeView(mLocalVideoView);
        }

        return mLocalVideoView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    public void onEventMainThread(EventDeletePhotoAlbumVid event) {
        if (event != null && event.getType() == PhotoAlbumConfig.PHOTO_BUM_LOCAL) {

            List<String> list = new ArrayList<String>();
            list.add(event.getVidPath());
            deleteListData(list);
        }
    }

    public void deleteListData(List<String> deleteData) {
        final String filePath = GolukApplication.getInstance().getCarrecorderCachePath() + File.separator + "image";
        for (String path : deleteData) {
            for (VideoInfo info : mDataList) {
                if (info.videoPath.equals(path)) {
                    // 删除视频文件
                    mDataList.remove(info);
                    File mp4file = new File(path);
                    if (mp4file.exists()) {
                        mp4file.delete();
                    }
                    String filename = path.substring(path.lastIndexOf("/") + 1);
                    // 删除数据库中的数据
                    GolukVideoInfoDbManager.getInstance().delVideoInfo(filename);
                    // 删除视频对应的图片
                    filename = filename.replace(".mp4", ".jpg");
                    File imgfile = new File(filePath + File.separator + filename);
                    if (imgfile.exists()) {
                        imgfile.delete();
                    }
                    SettingUtils.getInstance().putBoolean(filename, true);
                    break;
                }
            }
        }

        List<String> mGroupListName = new ArrayList<String>();
        for (VideoInfo info : mDataList) {
            String time = info.videoCreateDate;
            String tabTime = time.substring(0, 10);
            if (!mGroupListName.contains(tabTime)) {
                mGroupListName.add(tabTime);
            }
        }

        mDoubleDataList.clear();
        mDoubleDataList = VideoDataManagerUtils.videoInfo2Double(mDataList);
        mWonderfulVideoAdapter.setData(mGroupListName, mDoubleDataList);
        checkListState();
    }

    private void initView() {
        empty = (TextView) mLocalVideoView.findViewById(R.id.empty);
        mCustomProgressDialog = new CustomLoadingDialog(getActivity(), null);
        mStickyListHeadersListView = (StickyListHeadersListView) mLocalVideoView
                .findViewById(R.id.mStickyListHeadersListView);

        mWonderfulVideoAdapter = new LocalWonderfulVideoAdapter(getActivity(), mFragmentAlbum,
                mStickyListHeadersListView, IPCManagerFn.TYPE_CIRCULATE, "local");
        setListener();
    }

    private void setListener() {
        // 屏蔽某些机型的下拉悬停操作
        // mStickyListHeadersListView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        mStickyListHeadersListView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                screenX = arg1.getX();
                return false;
            }
        });

        mStickyListHeadersListView.setOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView arg0, int scrollState) {
                switch (scrollState) {
                    case AbsListView.OnScrollListener.SCROLL_STATE_FLING:
                        // mWonderfulVideoAdapter.lock();
                        break;
                    case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
                        // mWonderfulVideoAdapter.unlock();
                        break;
                    case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                        // mWonderfulVideoAdapter.lock();
                        break;

                    default:
                        break;
                }

            }

            @Override
            public void onScroll(AbsListView arg0, int firstVisibleItem, int visibleItemCount, int arg3) {

            }
        });

        mStickyListHeadersListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                if (screenX < (30 * density)) {
                    return;
                }
                if (arg2 < mDoubleDataList.size()) {
                    RelativeLayout mTMLayout1 = (RelativeLayout) arg1.findViewById(R.id.mTMLayout1);
                    RelativeLayout mTMLayout2 = (RelativeLayout) arg1.findViewById(R.id.mTMLayout2);
                    String tag1 = (String) mTMLayout1.getTag();
                    String tag2 = (String) mTMLayout2.getTag();
                    if (mFragmentAlbum.getEditState()) {
                        if ((screenX > 0) && (screenX < (screenWidth / 2))) {
                            selectedVideoItem(tag1, mTMLayout1);
                        } else {
                            selectedVideoItem(tag2, mTMLayout2);
                        }
                    } else {
                        DoubleVideoInfo d = mDoubleDataList.get(arg2);
                        // 点击播放
                        if ((screenX > 0) && (screenX < (screenWidth / 2))) {
                            // 点击列表左边项,跳转到视频播放页面

                            VideoInfo info1 = d.getVideoInfo1();
                            gotoVideoPlayPage(getVideoType(info1.filename), info1.videoPath, info1.filename, info1.videoCreateDate,
                                    info1.videoHP, info1.videoSize);
                            String filename = d.getVideoInfo1().filename;
                            updateNewState(filename);

                            mDoubleDataList.get(arg2).getVideoInfo1().isNew = false;
                            mWonderfulVideoAdapter.notifyDataSetChanged();
                        } else {
                            // 点击列表右边项,跳转到视频播放页面

                            VideoInfo info2 = d.getVideoInfo2();
                            if (null == info2)
                                return;
                            // --------------------------------------------------以此标记
                            // type 零时给 1 等主题逻辑调试通了 再去更具文件名称取类型
                            gotoVideoPlayPage(getVideoType(info2.filename), info2.videoPath, info2.filename, info2.videoCreateDate, info2.videoHP, info2.videoSize);
                            String filename = info2.filename;
                            updateNewState(filename);

                            mDoubleDataList.get(arg2).getVideoInfo2().isNew = false;
                            mWonderfulVideoAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }
        });

    }

    public void loadData(boolean flag) {
        if (flag) {
            if (null != mCustomProgressDialog && !mCustomProgressDialog.isShowing()) {
                mCustomProgressDialog.show();
            }
        }
        LocalDataLoadAsyncTask task = new LocalDataLoadAsyncTask(IPCManagerFn.TYPE_SHORTCUT, new DataCallBack() {
            @Override
            public void onSuccess(int type, List<VideoInfo> mLocalListData, List<String> mGroupListName) {
                mDataList.clear();
                mDoubleDataList.clear();
                mDataList.addAll(mLocalListData);
                mDoubleDataList = VideoDataManagerUtils.videoInfo2Double(mLocalListData);
                mWonderfulVideoAdapter.setData(mGroupListName, mDoubleDataList);
                // if(mDoubleDataList == null || mDoubleDataList.size() ==0){
                // View empty =
                // PhotoAlbumUtils.getInstall().getEmptyView(getActivity(),0);
                // ((ViewGroup)mStickyListHeadersListView.getParent()).addView(empty);
                // }

                mStickyListHeadersListView.setAdapter(mWonderfulVideoAdapter);
                try {
                    if (null != mCustomProgressDialog && mCustomProgressDialog.isShowing()) {
                        mCustomProgressDialog.close();
                    }
                } catch (Exception e) {

                }

                checkListState();
            }
        });
        task.execute("");
    }

    @SuppressLint("NewApi")
    private void checkListState() {
        GolukDebugUtils.e("", "Album------WondowvideoListView------checkListState");
        if (mDataList.size() <= 0) {
            empty.setVisibility(View.VISIBLE);
            if (isAdded()) {
                Drawable drawable = this.getResources().getDrawable(R.drawable.album_img_novideo);
                empty.setCompoundDrawablesRelativeWithIntrinsicBounds(null, drawable, null, null);
                empty.setText(getActivity().getResources().getString(R.string.photoalbum_local_no_video_text));
            }
            mStickyListHeadersListView.setVisibility(View.GONE);
            updateEditState(false);
        } else {
            empty.setVisibility(View.GONE);
            mStickyListHeadersListView.setVisibility(View.VISIBLE);
            updateEditState(true);
        }
    }

    private void updateEditState(boolean isHasData) {
        GolukDebugUtils.e("", "Album------WondowvideoListView------updateEditState" + isHasData);
        mFragmentAlbum.setEditBtnState(isHasData);
    }

    private void updateNewState(String filename) {
        SettingUtils.getInstance().putBoolean("Local_" + filename, false);
        for (int i = 0; i < mDataList.size(); i++) {
            VideoInfo info = mDataList.get(i);
            if (info.filename.equals(filename)) {
                mDataList.get(i).isNew = false;
                break;
            }
        }
    }

    /**
     * 选择视频item
     *
     * @param tag1
     * @param mTMLayout1
     */
    private void selectedVideoItem(String tag1, RelativeLayout mTMLayout1) {
        List<String> selectedListData = mFragmentAlbum.getSelectedList();
        if (!TextUtils.isEmpty(tag1)) {
            if (selectedListData.contains(tag1)) {
                selectedListData.remove(tag1);
                mTMLayout1.setVisibility(View.GONE);
            } else {
                selectedListData.add(tag1);
                mTMLayout1.setVisibility(View.VISIBLE);
            }

            if (selectedListData.size() == 0) {
                mFragmentAlbum.updateTitleName(getActivity().getResources()
                        .getString(R.string.local_video_title_text));
                mFragmentAlbum.updateDeleteState(false);
            } else {
                mFragmentAlbum.updateDeleteState(true);
                mFragmentAlbum.updateTitleName(getActivity().getString(R.string.str_photo_select,
                        selectedListData.size() + ""));
            }
        }
    }

    /**
     * 跳转到本地视频播放页面
     *
     * @param path
     */
    private void gotoVideoPlayPage(int type, String path, String filename, String createTime, String videoHP, String size) {
        if (!TextUtils.isEmpty(path)) {
            switch (getVideoType(filename)) {
                case 1:
                    ZhugeUtils.eventAlbumPlayer(getActivity(),
                            getString(R.string.str_zhuge_video_player_local),
                            getString(R.string.str_zhuge_video_player_wonderful));
                    break;
                case 2:
                    ZhugeUtils.eventAlbumPlayer(getActivity(),
                            getString(R.string.str_zhuge_video_player_local),
                            getString(R.string.str_zhuge_video_player_urgent));
                    break;
                case 3:
                    ZhugeUtils.eventAlbumPlayer(getActivity(),
                            getString(R.string.str_zhuge_video_player_local),
                            getString(R.string.str_zhuge_video_player_recycle));
                    break;
                default:
                    break;
            }

            if (!"0".equals(mFragmentAlbum.mPlatform)) {
                if (type != 3) {// 不是循环视频
                    GolukUtils.startPhotoAlbumPlayerActivity(getActivity(), type, "local", path, filename, createTime, videoHP, size,
                            (PromotionSelectItem) getActivity().getIntent().getSerializableExtra(PhotoAlbumPlayer.ACTIVITY_INFO));
                    return;
                }
            }

            GolukUtils.startPhotoAlbumPlayerActivity(LocalFragment.this.getContext(), type, "local", path, filename, createTime, videoHP, size,
                    (PromotionSelectItem) getActivity().getIntent().getSerializableExtra(PhotoAlbumPlayer.ACTIVITY_INFO));
        }
    }

    public int getVideoType(String name) {
        if (name.indexOf("WND") >= 0) {
            return 1;
        } else if (name.indexOf("URG") >= 0) {
            return 2;
        } else {
            return 3;
        }
    }

}