package com.mobnote.golukmain.photoalbum;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.emilsjolander.components.stickylistheaders.StickyListHeadersListView;
import com.mobnote.application.GolukApplication;
import com.mobnote.eventbus.EventDeletePhotoAlbumVid;
import com.mobnote.eventbus.EventDownloadVideoFinish;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.carrecorder.entity.DoubleVideoInfo;
import com.mobnote.golukmain.carrecorder.entity.VideoInfo;
import com.mobnote.golukmain.carrecorder.util.SettingUtils;
import com.mobnote.golukmain.carrecorder.view.CustomLoadingDialog;
import com.mobnote.golukmain.fileinfo.GolukVideoInfoDbManager;
import com.mobnote.golukmain.promotion.PromotionSelectItem;
import com.mobnote.util.GolukUtils;
import com.mobnote.util.ZhugeUtils;
import com.rd.veuisdk.SdkEntry;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.com.mobnote.module.ipcmanager.IPCManagerFn;
import cn.com.tiros.debug.GolukDebugUtils;
import de.greenrobot.event.EventBus;

public class LocalFragment extends Fragment implements LocalWonderfulVideoAdapter.IListViewItemClickColumn {
    private View mLocalVideoView;

    private CustomLoadingDialog mCustomProgressDialog = null;

    private StickyListHeadersListView mStickyListHeadersListView = null;

    private LocalWonderfulVideoAdapter mWonderfulVideoAdapter = null;

    private List<VideoInfo> mDataList = null;
    private List<DoubleVideoInfo> mDoubleDataList = null;

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
        mLocalVideoView = inflater.inflate(R.layout.wonderful_listview, container, false);

        this.mDataList = new ArrayList<>();
        this.mDoubleDataList = new ArrayList<>();
        initView();
        loadData(true);
        return mLocalVideoView;
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
                        if (!mp4file.delete()) {
                            GolukDebugUtils.e(LocalFragment.this.getClass().getSimpleName(), "Delete failed  Path is :" + mp4file.getAbsolutePath());
                        }
                    }
                    String filename = path.substring(path.lastIndexOf("/") + 1);
                    // 删除数据库中的数据
                    GolukVideoInfoDbManager.getInstance().delVideoInfo(filename);
                    // 删除视频对应的图片
                    filename = filename.replace(".mp4", ".jpg");
                    File imgfile = new File(filePath + File.separator + filename);
                    if (imgfile.exists()) {
                        if (!imgfile.delete()) {
                            GolukDebugUtils.e(LocalFragment.this.getClass().getSimpleName(), "Delete failed  Path is :" + imgfile.getAbsolutePath());
                        }
                    }
                    SettingUtils.getInstance().putBoolean(filename, true);
                    break;
                }
            }
        }

        List<String> mGroupListName = new ArrayList<>();
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
        loadData(false);
    }

    private void initView() {
        empty = (TextView) mLocalVideoView.findViewById(R.id.empty);
        mCustomProgressDialog = new CustomLoadingDialog(getActivity(), null);
        mStickyListHeadersListView = (StickyListHeadersListView) mLocalVideoView
                .findViewById(R.id.mStickyListHeadersListView);

        mWonderfulVideoAdapter = new LocalWonderfulVideoAdapter(getActivity(), mFragmentAlbum, IPCManagerFn.TYPE_CIRCULATE, "local", this);
    }

    public void loadData(boolean flag) {
        if (flag) {
            if (null != mCustomProgressDialog && !mCustomProgressDialog.isShowing() && isResumed()) {
                mCustomProgressDialog.show();
            }
        }
        LocalDataLoadAsyncTask task = new LocalDataLoadAsyncTask(IPCManagerFn.TYPE_SHORTCUT, new DataCallBack() {
            @Override
            public void onSuccess(int type, List<VideoInfo> mLocalListData, List<String> mGroupListName) {
                // Bugly #4656
                if (mDataList != null && !mDataList.isEmpty())
                    mDataList.clear();
                if (mDoubleDataList != null && !mDoubleDataList.isEmpty())
                    mDoubleDataList.clear();
                if (mDataList == null)
                    mDataList = new ArrayList<>();

                mDataList.addAll(mLocalListData);
                mDoubleDataList = VideoDataManagerUtils.videoInfo2Double(mLocalListData);
                mWonderfulVideoAdapter.setData(mGroupListName, mDoubleDataList);
                mStickyListHeadersListView.setAdapter(mWonderfulVideoAdapter);
                try {
                    if (null != mCustomProgressDialog && mCustomProgressDialog.isShowing()) {
                        mCustomProgressDialog.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
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
            updateView(selectedListData);
        }
    }

    private void updateView(List<String> selectedListData) {
        if (selectedListData.size() == 0) {
            mFragmentAlbum.updateTitleName(getActivity().getResources()
                    .getString(R.string.local_video_title_text));
            mFragmentAlbum.updateDeleteState(false);
        } else {
            mFragmentAlbum.updateDeleteState(true);
            mFragmentAlbum.updateTitleName(getActivity().getString(R.string.str_photo_select,
                    selectedListData.size() + ""));
        }
        mFragmentAlbum.adaptCbAllText(selectedListData.size() != mDataList.size());
    }

    public void allSelect(boolean selected) {
        List<String> selectedListData = mFragmentAlbum.getSelectedList();
        selectedListData.clear();
        if (selected) {
            for (DoubleVideoInfo doubleVideoInfo : mDoubleDataList) {
                if (doubleVideoInfo.getVideoInfo1() != null) {
                    selectedListData.add(doubleVideoInfo.getVideoInfo1().videoPath);
                }
                if (doubleVideoInfo.getVideoInfo2() != null) {
                    selectedListData.add(doubleVideoInfo.getVideoInfo2().videoPath);
                }
            }
        }
        updateView(selectedListData);
        mWonderfulVideoAdapter.notifyDataSetChanged();
    }

    /**
     * 跳转到本地视频播放页面
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

            if (!mFragmentAlbum.parentViewIsMainActivity) {
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
        if (name.contains("WND")) {
            return 1;
        } else if (name.contains("URG")) {
            return 2;
        } else {
            return 3;
        }
    }

    @Override
    public void onItemClicked(View view, DoubleVideoInfo videoInfo, int columnIndex) {
        RelativeLayout mTMLayout1 = (RelativeLayout) view.findViewById(R.id.mTMLayout1);
        RelativeLayout mTMLayout2 = (RelativeLayout) view.findViewById(R.id.mTMLayout2);
        String tag1 = (String) mTMLayout1.getTag();
        String tag2 = (String) mTMLayout2.getTag();
        if (mFragmentAlbum.selectMode) {
            ArrayList<String> tempList = new ArrayList<>();
            if (columnIndex == LocalWonderfulVideoAdapter.IListViewItemClickColumn.COLUMN_FIRST) {
                tempList.add(tag1);
            } else {
                tempList.add(tag2);
            }
            SdkEntry.onCustomizeAlbum(getContext(), tempList);
            getActivity().finish();
        } else if (mFragmentAlbum.getEditState()) {
            if (columnIndex == LocalWonderfulVideoAdapter.IListViewItemClickColumn.COLUMN_FIRST) {
                selectedVideoItem(tag1, mTMLayout1);
            } else {
                selectedVideoItem(tag2, mTMLayout2);
            }
        } else {
            DoubleVideoInfo d = videoInfo;
            // 点击播放
            if (columnIndex == LocalWonderfulVideoAdapter.IListViewItemClickColumn.COLUMN_FIRST) {
                // 点击列表左边项,跳转到视频播放页面

                VideoInfo info1 = d.getVideoInfo1();
                gotoVideoPlayPage(getVideoType(info1.filename), info1.videoPath, info1.filename, info1.videoCreateDate,
                        info1.videoHP, info1.videoSize);
                String filename = d.getVideoInfo1().filename;
                updateNewState(filename);

                d.getVideoInfo1().isNew = false;
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

                d.getVideoInfo2().isNew = false;
                mWonderfulVideoAdapter.notifyDataSetChanged();
            }
        }
    }

    public void onEventMainThread(EventDeletePhotoAlbumVid event) {
        if (event != null && event.getType() == PhotoAlbumConfig.PHOTO_BUM_LOCAL) {

            List<String> list = new ArrayList<>();
            list.add(event.getVidPath());
            deleteListData(list);
        }
    }

    public void onEventMainThread(EventDownloadVideoFinish event) {
        if (event != null) {
            loadData(true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData(false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

}