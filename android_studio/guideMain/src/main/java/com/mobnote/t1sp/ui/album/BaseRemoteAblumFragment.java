package com.mobnote.t1sp.ui.album;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.emilsjolander.components.stickylistheaders.StickyListHeadersListView;
import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.carrecorder.entity.DoubleVideoInfo;
import com.mobnote.golukmain.carrecorder.entity.VideoInfo;
import com.mobnote.golukmain.carrecorder.util.SettingUtils;
import com.mobnote.golukmain.carrecorder.view.CustomLoadingDialog;
import com.mobnote.golukmain.photoalbum.LocalWonderfulVideoAdapter;
import com.mobnote.golukmain.photoalbum.PhotoAlbumConfig;
import com.mobnote.golukmain.photoalbum.PhotoAlbumPlayer;
import com.mobnote.golukmain.photoalbum.VideoDataManagerUtils;
import com.mobnote.golukmain.promotion.PromotionSelectItem;
import com.mobnote.golukmain.wifibind.WiFiLinkListActivity;
import com.mobnote.t1sp.api.ApiUtil;
import com.mobnote.t1sp.api.ParamsBuilder;
import com.mobnote.t1sp.callback.CommonCallback;
import com.mobnote.t1sp.callback.FileListCallback;
import com.mobnote.t1sp.ui.download.DownloaderT1spImpl;
import com.mobnote.t1sp.ui.download.Task;
import com.mobnote.t1sp.ui.download.ThumbDownloader;
import com.mobnote.t1sp.util.CollectionUtils;
import com.mobnote.t1sp.util.Const;
import com.mobnote.t1sp.util.FileUtil;
import com.mobnote.util.GolukUtils;
import com.mobnote.util.ZhugeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * T1SP远程相册(精彩/紧急/循环)视频列表BaseFragment
 */
public abstract class BaseRemoteAblumFragment extends Fragment implements LocalWonderfulVideoAdapter.IListViewItemClickColumn, ThumbDownloader.ThumbDownloadListener {

    private View mWonderfulVideoView;

    // 列表数据加载中标识
    private boolean isGetFileListDataing = false;

    private List<VideoInfo> mDataList = null;
    private List<DoubleVideoInfo> mDoubleDataList = null;
    private CustomLoadingDialog mCustomProgressDialog = null;

    private StickyListHeadersListView mStickyListHeadersListView = null;
    private RemoteVideoAdapter mRemoteVideoAdapter = null;
    private ThumbDownloader mThumbDownloader;

    // 是否下载了一次数据(刚进入页面时)
    private boolean isInitLoadData;
    // 保存列表一个显示项索引
    private int firstVisible;
    // 保存列表显示item个数
    private int visibleCount;

    // 判断服务端是否还有数据
    private boolean isHasData = true;

    // 列表添加页脚标识
    private boolean addFooter = false;

    private FragmentAlbumT1SP mFragmentAlbum;

    public boolean isShowPlayer = false;

    // 添加列表底部加载中布局
    private RelativeLayout mBottomLoadingView = null;

    private List<String> mGroupListName = null;

    private TextView empty = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mWonderfulVideoView == null) {
            mWonderfulVideoView = inflater.inflate(R.layout.wonderful_listview, (ViewGroup) getActivity()
                    .findViewById(R.id.viewpager), false);

            mBottomLoadingView = (RelativeLayout) LayoutInflater.from(getActivity()).inflate(
                    R.layout.video_square_below_loading, null);

            mFragmentAlbum = getFragmentAlbum();
            mWonderfulVideoView = inflater.inflate(R.layout.wonderful_listview, null, false);

            mDataList = new ArrayList<>();
            mDoubleDataList = new ArrayList<>();
            mGroupListName = new ArrayList<>();

            initView();
        }

        ViewGroup parent = (ViewGroup) mWonderfulVideoView.getParent();
        if (parent != null) {
            parent.removeView(mWonderfulVideoView);
        }

        return mWonderfulVideoView;
    }

    public FragmentAlbumT1SP getFragmentAlbum() {
        if (mFragmentAlbum == null) {
            mFragmentAlbum = (FragmentAlbumT1SP) getParentFragment();
        }
        return mFragmentAlbum;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mThumbDownloader != null)
            mThumbDownloader.stop();
    }

    private void initView() {
        empty = (TextView) mWonderfulVideoView.findViewById(R.id.empty);
        this.mCustomProgressDialog = new CustomLoadingDialog(getActivity(), null);
        mStickyListHeadersListView = (StickyListHeadersListView) mWonderfulVideoView
                .findViewById(R.id.mStickyListHeadersListView);
        mRemoteVideoAdapter = new RemoteVideoAdapter(getActivity(), getVideoType(),
                (FragmentAlbumT1SP) getParentFragment(), mStickyListHeadersListView, this);
        setListener();
        //loadData(true);
        if (getVideoType() != PhotoAlbumConfig.PHOTO_BUM_IPC_WND) {
            mThumbDownloader = new ThumbDownloader(getActivity());
            mThumbDownloader.setListener(this);
        }
    }

    @Override
    public void onItemClicked(View arg1, DoubleVideoInfo videoInfo, int columnIndex) {
        RelativeLayout mTMLayout1 = (RelativeLayout) arg1.findViewById(R.id.mTMLayout1);
        RelativeLayout mTMLayout2 = (RelativeLayout) arg1.findViewById(R.id.mTMLayout2);
        String tag1 = (String) mTMLayout1.getTag();
        String tag2 = (String) mTMLayout2.getTag();
        if (getFragmentAlbum().getEditState()) {
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
                gotoVideoPlayPage(getVideoType(), info1.videoPath,
                        info1.filename, info1.videoCreateDate, info1.videoHP, info1.videoSize);
                String filename = d.getVideoInfo1().filename;
                updateNewState(filename);

                videoInfo.getVideoInfo1().isNew = false;
                mRemoteVideoAdapter.notifyDataSetChanged();
            } else {
                // 点击列表右边项,跳转到视频播放页面
                VideoInfo info2 = d.getVideoInfo2();
                if (null == info2)
                    return;
                gotoVideoPlayPage(getVideoType(), info2.videoPath,
                        info2.filename, info2.videoCreateDate, info2.videoHP, info2.videoSize);
                String filename = info2.filename;
                updateNewState(filename);

                videoInfo.getVideoInfo2().isNew = false;
                mRemoteVideoAdapter.notifyDataSetChanged();
            }
        }
    }

    private void setListener() {
        mStickyListHeadersListView.setOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView arg0, int scrollState) {
                switch (scrollState) {
                    case OnScrollListener.SCROLL_STATE_FLING:
                        // mRemoteVideoAdapter.lock();
                        break;
                    case OnScrollListener.SCROLL_STATE_IDLE:
                        // mRemoteVideoAdapter.unlock();
                        if (mStickyListHeadersListView.getAdapter().getCount() == (firstVisible + visibleCount)) {
                            final int size = mDataList.size();
                            if (size > 0 && isHasData) {
                                if (isGetFileListDataing) {
                                    return;
                                }
                                // 加载更多
                                loadData(true);
                            }
                        }
                        break;
                    case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                        // mRemoteVideoAdapter.lock();
                        break;

                    default:
                        break;
                }

            }

            @Override
            public void onScroll(AbsListView arg0, int firstVisibleItem, int visibleItemCount, int arg3) {
                firstVisible = firstVisibleItem;
                visibleCount = visibleItemCount;
            }
        });

        empty.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (GolukApplication.getInstance().isIpcLoginSuccess == false) {
                    Intent intent = new Intent(getContext(), WiFiLinkListActivity.class);
                    intent.putExtra(WiFiLinkListActivity.ACTION_FROM_REMOTE_ALBUM, true);
                    startActivity(intent);
                }
            }
        });
    }

    private void updateNewState(String filename) {
        SettingUtils.getInstance().putBoolean("Cloud_" + filename, false);
        for (int i = 0; i < mDataList.size(); i++) {
            VideoInfo info = mDataList.get(i);
            if (info.filename.equals(filename)) {
                mDataList.get(i).isNew = false;
                break;
            }
        }
    }

    /**
     * 跳转到本地视频播放页面
     *
     * @param path
     */
    private void gotoVideoPlayPage(int from, String path, String filename, String createTime, String videoHP, String size) {
        if (!isShowPlayer) {
            isShowPlayer = true;
            ZhugeUtils.eventAlbumPlayer(getActivity(),
                    getString(R.string.str_zhuge_video_player_wonderful),
                    getString(R.string.str_zhuge_video_player_wonderful));
            GolukUtils.startPhotoAlbumPlayerActivity(BaseRemoteAblumFragment.this.getContext(), getVideoType(), "ipc", path,
                    filename, createTime, videoHP, size, (PromotionSelectItem) getActivity().getIntent().getSerializableExtra(PhotoAlbumPlayer.ACTIVITY_INFO));
        }
    }

    /**
     * 选择视频item
     *
     * @param tag1
     * @param mTMLayout1
     */
    private void selectedVideoItem(String tag1, RelativeLayout mTMLayout1) {
        List<String> selectedListData = getFragmentAlbum().getSelectedList();
        if (!TextUtils.isEmpty(tag1)) {
            if (selectedListData.contains(tag1)) {
                selectedListData.remove(tag1);
                mTMLayout1.setVisibility(View.GONE);
            } else {
                selectedListData.add(tag1);
                mTMLayout1.setVisibility(View.VISIBLE);
            }

            if (selectedListData.size() == 0) {
                getFragmentAlbum().updateTitleName(getActivity().getString(R.string.local_video_title_text));
                getFragmentAlbum().updateDeleteState(false);
            } else {
                mFragmentAlbum.updateDeleteState(true);
                mFragmentAlbum.updateTitleName(getActivity().getString(R.string.str_photo_select,
                        selectedListData.size() + ""));
            }
        }
    }

    /**
     * 添加加载loading
     *
     * @author jyf
     */
    private void addFooterView() {
        if (!addFooter) {
            addFooter = true;
            mStickyListHeadersListView.addFooterView(mBottomLoadingView);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        isShowPlayer = false;
    }

    public void loadData(boolean flag) {
        isInitLoadData = true;
        if (isGetFileListDataing) {
            return;
        }

        if (null == empty || null == mStickyListHeadersListView) {
            return;
        }

        if (flag) {
            mStickyListHeadersListView.setVisibility(View.VISIBLE);

            // 获取文件列表
            ApiUtil.apiServiceAit().sendRequest(getRequestParam(), mFileCallback);

        } else {
            Drawable drawable = this.getResources().getDrawable(R.drawable.img_no_video);
            getFragmentAlbum().setEditBtnState(false);
            empty.setCompoundDrawablesRelativeWithIntrinsicBounds(null, drawable, null, null);
            empty.setText(getActivity().getResources().getString(R.string.str_album_no_connect));
            empty.setVisibility(View.VISIBLE);
            mStickyListHeadersListView.setVisibility(View.GONE);
        }
    }

    /**
     * 根据视频类型返回对应的请求参数
     */
    private Map<String, String> getRequestParam() {
        String type = "";
        switch (getVideoType()) {
            case PhotoAlbumConfig.PHOTO_BUM_IPC_WND:
                type = ParamsBuilder.FILE_DIR_TYPE_SHARE;
                break;
            case PhotoAlbumConfig.PHOTO_BUM_IPC_URG:
                type = ParamsBuilder.FILE_DIR_TYPE_EVENT;
                break;
            case PhotoAlbumConfig.PHOTO_BUM_IPC_LOOP:
                type = ParamsBuilder.FILE_DIR_TYPE_NORMAL;
                break;
        }

        return ParamsBuilder.getFileListParam(
                type,
                ParamsBuilder.FILE_TYPE_ALL,
                mLastFileIndex + "",
                FILE_COUNT_ONE_TIME + ""
        );
    }

    private int mLastFileIndex = 0;
    private static final int FILE_COUNT_ONE_TIME = 20;
    private FileListCallback mFileCallback = new FileListCallback() {
        @Override
        public void onStart() {
            isGetFileListDataing = true;
            if (mDataList.isEmpty())
                showLoading();
        }

        @Override
        public void onGetFileList(List<VideoInfo> files) {
            if (files != null && !files.isEmpty()) {
                mLastFileIndex += files.size();
                updateData(files);
            }
        }

        @Override
        protected void onSuccess() {
        }

        @Override
        public void onFinish() {
            isGetFileListDataing = false;
            closeLoading();
            checkListState();
        }

        @Override
        protected void onServerError(int errorCode, String errorMessage) {
            empty.setVisibility(View.VISIBLE);
            mStickyListHeadersListView.setVisibility(View.GONE);
        }
    };

    protected void showLoading() {
        if (mCustomProgressDialog != null && !mCustomProgressDialog.isShowing()) {
            mCustomProgressDialog.show();
        }
    }

    protected void closeLoading() {
        if (mCustomProgressDialog != null && mCustomProgressDialog.isShowing()) {
            mCustomProgressDialog.close();
        }
    }

    private void updateEditState(boolean isHasData) {
        getFragmentAlbum().setEditBtnState(isHasData);
    }

    private void updateData(List<VideoInfo> fileList) {
        addFooterView();
        if (mDataList.size() == 0) {
            mStickyListHeadersListView.setAdapter(mRemoteVideoAdapter);
        }
        mDataList.addAll(fileList);
        if (fileList.size() < FILE_COUNT_ONE_TIME) {
            isHasData = false;
            removeFooterView();
        } else {
            isHasData = true;
        }

        parseDataAndShow();

        addThumbDownload(fileList);
    }

    private void addThumbDownload(List<VideoInfo> fileList) {
        if (CollectionUtils.isEmpty(fileList))
            return;

        List<String> thumbUrls = new ArrayList<>(fileList.size());
        for (VideoInfo videoInfo : fileList) {
            if (videoInfo.videoPath.contains("/SD/")) {
                thumbUrls.add(videoInfo.videoPath.replace("/SD/", "/thumb/"));
            }
        }

        if (mThumbDownloader != null)
            mThumbDownloader.addUrls(thumbUrls);
    }

    private void parseDataAndShow() {
        mDoubleDataList.clear();
        mDoubleDataList.clear();
        mDoubleDataList = VideoDataManagerUtils.videoInfo2Double(mDataList);
        mGroupListName = VideoDataManagerUtils.getGroupName(mDataList);
        mRemoteVideoAdapter.setData(mGroupListName, mDoubleDataList);
    }

    private void checkListState() {
        if (mDataList == null || mDataList.isEmpty()) {
            empty.setVisibility(View.VISIBLE);
            Drawable drawable = this.getResources().getDrawable(R.drawable.album_img_novideo);
            empty.setCompoundDrawablesRelativeWithIntrinsicBounds(null, drawable, null, null);
            empty.setText(getActivity().getResources().getString(R.string.photoalbum_no_video_text));
            mStickyListHeadersListView.setVisibility(View.GONE);
            updateEditState(false);
        } else {
            empty.setVisibility(View.GONE);
            mStickyListHeadersListView.setVisibility(View.VISIBLE);
            if (!mFragmentAlbum.getEditState()) {
                updateEditState(true);
            }
        }
    }

    /**
     * 移除loading
     *
     * @author jyf
     */
    public void removeFooterView() {
        if (addFooter) {
            addFooter = false;
            isGetFileListDataing = false;
            mStickyListHeadersListView.removeFooterView(mBottomLoadingView);
        }
    }

    /**
     * 下载选中的视频
     */
    public void downloadVideoFlush(List<String> selectedList) {
        if (CollectionUtils.isEmpty(selectedList))
            return;

        List<Task> downloadTasks = new ArrayList<>();
        Task task = null;
        String savePath = "";
        for (String videoPath : selectedList) {
            savePath = videoPath.substring(videoPath.lastIndexOf("/") + 1);
            savePath = getSavePath(savePath);
            task = new Task(videoPath, savePath);
            downloadTasks.add(task);
        }

        DownloaderT1spImpl.getInstance().addDownloadTasks(downloadTasks);
    }

    /**
     * 根据视频名获取本地保存路径
     *
     * @param videoName 视频名
     */
    private String getSavePath(String videoName) {
        switch ((getVideoType())) {
            case PhotoAlbumConfig.PHOTO_BUM_IPC_WND:
                return FileUtil.WONDERFUL_VIDEO_PATH + videoName;
            case PhotoAlbumConfig.PHOTO_BUM_IPC_URG:
                return FileUtil.URGENT_VIDEO_PATH + videoName;
            case PhotoAlbumConfig.PHOTO_BUM_IPC_LOOP:
                return FileUtil.LOOP_VIDEO_PATH + videoName;
        }
        return "";
    }

    public boolean hasLoadedFirst() {
        return isInitLoadData;
    }

    /**
     * 删除选中的远程文件
     */
    public void deleteListData(List<String> selectedList) {
        // 列表已经删除完成
        if (CollectionUtils.isEmpty(selectedList)) {
            closeLoading();
            GolukUtils.showToast(getActivity(), getResources().getString(R.string.str_photo_delete_ok));
            // 恢复顶部按钮状态
            getFragmentAlbum().resetTopBar();
            return;
        }

        // 删除文件
        // http://192.72.1.1/SD/Share/F/SHARE171113-151216F.MP4 --> /SD/Share/F/SHARE171113-151216F.MP4
        String filelPath = selectedList.get(0);
        if (filelPath.contains(Const.HTTP_SCHEMA_ADD_IP)) {
            filelPath = filelPath.substring(Const.HTTP_SCHEMA_ADD_IP.length());
        }
        ApiUtil.apiServiceAit().sendRequest(ParamsBuilder.deleteFileParam(filelPath), mDeleteCallback);
    }

    private CommonCallback mDeleteCallback = new CommonCallback() {
        @Override
        public void onStart() {
            showLoading();
        }

        @Override
        protected void onSuccess() {
            List<String> selectedList = getFragmentAlbum().getSelectedList();
            deleteFromDataByVideoName(selectedList.get(0));
            selectedList.remove(0);
            // 更新显示
            parseDataAndShow();
            // 删除下一个
            deleteListData(selectedList);
        }

        @Override
        protected void onServerError(int errorCode, String errorMessage) {
            closeLoading();
        }

        @Override
        public void onFinish() {
        }
    };

    /**
     * 根据视频文件从列表中移除对应的数据
     */
    private void deleteFromDataByVideoName(String videoPath) {
        if (TextUtils.isEmpty(videoPath))
            return;
        int size = mDataList.size();
        VideoInfo tempInfo = null;
        for (int i = 0; i < size; i++) {
            tempInfo = mDataList.get(i);
            if (tempInfo != null && tempInfo.videoPath.contains(videoPath)) {
                mDataList.remove(i);
                return;
            }
        }

    }

    /**
     * 返回视频类型: 精彩/紧急/循环
     */
    protected abstract int getVideoType();

    @Override
    public void onThumbDownload(String thumbUrl) {
        // 视频缩略图下载成功
        if (mRemoteVideoAdapter != null)
            mRemoteVideoAdapter.notifyDataSetChanged();
    }

}