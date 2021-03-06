package com.mobnote.t1sp.ui.album;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.emilsjolander.components.stickylistheaders.StickyListHeadersListView;
import com.mobnote.application.GlobalWindow;
import com.mobnote.application.GolukApplication;
import com.mobnote.eventbus.EventDeletePhotoAlbumVid;
import com.mobnote.eventbus.EventDownloadIpcVid;
import com.mobnote.eventbus.EventDownloadVideoFinish;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.carrecorder.entity.DoubleVideoInfo;
import com.mobnote.golukmain.carrecorder.entity.VideoInfo;
import com.mobnote.golukmain.carrecorder.util.SettingUtils;
import com.mobnote.golukmain.carrecorder.view.CustomLoadingDialog;
import com.mobnote.golukmain.fileinfo.GolukVideoInfoDbManager;
import com.mobnote.golukmain.fileinfo.VideoFileInfoBean;
import com.mobnote.golukmain.photoalbum.LocalWonderfulVideoAdapter;
import com.mobnote.golukmain.photoalbum.PhotoAlbumConfig;
import com.mobnote.golukmain.photoalbum.PhotoAlbumPlayer;
import com.mobnote.golukmain.photoalbum.VideoDataManagerUtils;
import com.mobnote.golukmain.promotion.PromotionSelectItem;
import com.mobnote.golukmain.wifibind.WiFiLinkListActivity;
import com.mobnote.t1sp.callback.CommonCallback;
import com.mobnote.t1sp.download.DownloaderT1sp;
import com.mobnote.t1sp.download.ThumbDownloader;
import com.mobnote.t1sp.download2.IpcDownloadListener;
import com.mobnote.t1sp.download2.IpcDownloader;
import com.mobnote.t1sp.download2.IpcDownloaderImpl;
import com.mobnote.t1sp.file.IpcFileDelete;
import com.mobnote.t1sp.file.IpcFileListener;
import com.mobnote.t1sp.util.CollectionUtils;
import com.mobnote.t1sp.util.FileUtil;
import com.mobnote.t2s.files.IpcFileQueryF4;
import com.mobnote.t2s.files.IpcFileQueryListener;
import com.mobnote.t2s.files.IpcQuery;
import com.mobnote.util.GolukUtils;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import likly.dollar.$;

/**
 * T1SP????????????(??????/??????/??????)????????????BaseFragment
 */
public abstract class BaseRemoteAblumFragment extends Fragment implements LocalWonderfulVideoAdapter.IListViewItemClickColumn, ThumbDownloader.ThumbDownloadListener, DownloaderT1sp.IDownloadSuccess, IpcFileQueryListener, IpcFileListener {

    private View mWonderfulVideoView;

    // ???????????????????????????
    private boolean isGetFileListDataing = false;

    private List<VideoInfo> mDataList = null;
    private List<DoubleVideoInfo> mDoubleDataList = null;
    private CustomLoadingDialog mCustomProgressDialog = null;

    private StickyListHeadersListView mStickyListHeadersListView = null;
    private RemoteVideoAdapter mRemoteVideoAdapter = null;
    private ThumbDownloader mThumbDownloader;

    // ????????????????????????
    private boolean addFooter = false;

    private FragmentAlbumT1SP mFragmentAlbum;

    public boolean isShowPlayer = false;

    // ?????????????????????????????????
    private RelativeLayout mBottomLoadingView = null;

    private List<String> mGroupListName = null;

    private TextView empty = null;

    private boolean isInitLoadData;

    private IpcFileDelete mIpcDeleteOption;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
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

            mIpcDeleteOption = new IpcFileDelete(this);

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
        EventBus.getDefault().unregister(this);
        if (mThumbDownloader != null)
            mThumbDownloader.stop();
    }

    private void initView() {
        empty = (TextView) mWonderfulVideoView.findViewById(R.id.empty);
        this.mCustomProgressDialog = new CustomLoadingDialog(getActivity(), null);
        mCustomProgressDialog.setCancel(false);
        mStickyListHeadersListView = (StickyListHeadersListView) mWonderfulVideoView
                .findViewById(R.id.mStickyListHeadersListView);
        mRemoteVideoAdapter = new RemoteVideoAdapter(getActivity(), getVideoType(),
                (FragmentAlbumT1SP) getParentFragment(), mStickyListHeadersListView, this);
        setListener();
        //loadData(true);

        mThumbDownloader = new ThumbDownloader(getActivity(), isWonderfulVideoType());
        mThumbDownloader.setListener(this);
    }

    private boolean isWonderfulVideoType() {
        return getVideoType() == PhotoAlbumConfig.PHOTO_BUM_IPC_WND;
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
            // ????????????
            if (columnIndex == LocalWonderfulVideoAdapter.IListViewItemClickColumn.COLUMN_FIRST) {
                // ?????????????????????,???????????????????????????
                VideoInfo info1 = d.getVideoInfo1();
                gotoVideoPlayPage(getVideoType(), info1.videoUrl, info1.relativePath,
                        info1.filename, info1.videoCreateDate, info1.videoHP, info1.videoSize);
                String filename = d.getVideoInfo1().filename;
                updateNewState(filename);

                videoInfo.getVideoInfo1().isNew = false;
                mRemoteVideoAdapter.notifyDataSetChanged();
            } else {
                // ?????????????????????,???????????????????????????
                VideoInfo info2 = d.getVideoInfo2();
                if (null == info2)
                    return;
                gotoVideoPlayPage(getVideoType(), info2.videoUrl, info2.relativePath,
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
//                        if (mStickyListHeadersListView.getAdapter().getCount() == (firstVisible + visibleCount)) {
//                            final int size = mDataList.size();
//                            if (size > 0 && isHasData) {
//                                if (isGetFileListDataing) {
//                                    return;
//                                }
//                                // ????????????
//                                loadData(true);
//                            }
//                        }
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
//                firstVisible = firstVisibleItem;
//                visibleCount = visibleItemCount;
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
     * ?????????????????????????????????
     *
     * @param path
     */
    private void gotoVideoPlayPage(int from, String path, String relativePath, String filename, String createTime, String videoHP, String size) {
        if (!isShowPlayer) {
            isShowPlayer = true;
            GolukUtils.startPhotoAlbumPlayerActivityT2S(BaseRemoteAblumFragment.this.getContext(), getVideoType(), "ipc", path, relativePath,
                    filename, createTime, videoHP, size, (PromotionSelectItem) getActivity().getIntent().getSerializableExtra(PhotoAlbumPlayer.ACTIVITY_INFO));
        }
    }

    /**
     * ????????????item
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
     * ????????????loading
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
            isInitLoadData = false;
            return;
        }

        if (flag) {
            mStickyListHeadersListView.setVisibility(View.VISIBLE);

            // ??????????????????
            queryFiles();

        } else {
            Drawable drawable = this.getResources().getDrawable(R.drawable.img_no_video);
            getFragmentAlbum().setEditBtnState(false);
            empty.setCompoundDrawablesRelativeWithIntrinsicBounds(null, drawable, null, null);
            empty.setText(getActivity().getResources().getString(R.string.str_album_no_connect));
            empty.setVisibility(View.VISIBLE);
            mStickyListHeadersListView.setVisibility(View.GONE);
        }
    }

    IpcQuery mIpcQuery;

    private void queryFiles() {
        if (mIpcQuery == null)
            mIpcQuery = new IpcFileQueryF4(this, getActivity());
        switch (getVideoType()) {
            case PhotoAlbumConfig.PHOTO_BUM_IPC_WND:
                mIpcQuery.queryCaptureVideoList();
                break;
            case PhotoAlbumConfig.PHOTO_BUM_IPC_URG:
                mIpcQuery.queryUrgentVideoList();
                break;
            case PhotoAlbumConfig.PHOTO_BUM_IPC_LOOP:
                mIpcQuery.queryNormalVideoList();
                break;
            case PhotoAlbumConfig.PHOTO_BUM_IPC_TIMESLAPSE:
                mIpcQuery.queryTimeslapseVideoList();
                break;
        }

    }

//    private FileListCallback mFileCallback = new FileListCallback() {
//        @Override
//        public boolean isNetworkAvailable() {
//            return true;
//        }
//
//        @Override
//        public void onStart() {
//            isGetFileListDataing = true;
//            if (mDataList.isEmpty())
//                showLoading();
//        }
//
//        @Override
//        public void onGetFileList(List<VideoInfo> files) {
//            if (files != null && !files.isEmpty()) {
//                mLastFileIndex += files.size();
//                updateData(files);
//            }
//        }
//
//        @Override
//        protected void onSuccess() {
//        }
//
//        @Override
//        public void onFinish() {
//            isGetFileListDataing = false;
//            closeLoading();
//            checkListState();
//        }
//
//        @Override
//        protected void onServerError(int errorCode, String errorMessage) {
//            empty.setVisibility(View.VISIBLE);
//            mStickyListHeadersListView.setVisibility(View.GONE);
//        }
//    };

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
        removeFooterView();
//        if (fileList.size() < FILE_COUNT_ONE_TIME) {
//            isHasData = false;
//            removeFooterView();
//        } else {
//            isHasData = true;
//        }

        parseDataAndShow();

        addThumbDownload(fileList);
    }

    private void addThumbDownload(List<VideoInfo> fileList) {
        if (CollectionUtils.isEmpty(fileList))
            return;

        List<String> thumbUrls = new ArrayList<>(fileList.size());
        for (VideoInfo videoInfo : fileList) {
//            if (isWonderfulVideoType()) {
//                thumbUrls.add(videoInfo.videoPath);
//            } else {
            if (videoInfo.videoPath.contains("/SD/")) {
                thumbUrls.add(videoInfo.videoPath.replace("/SD/", "/thumb/"));
            }
//            }
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

        checkListState();
    }

    private void checkListState() {
        if (dataListIsEmpty()) {
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
     * ??????????????????
     */
    private boolean dataListIsEmpty() {
        return mDataList == null || mDataList.isEmpty();
    }

    /**
     * ??????loading
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
     * PagerView??????????????????
     */
    public void onShow() {
        // ????????????????????????
        updateEditState(!dataListIsEmpty());
    }

    /**
     * ?????????????????????
     */
    public void downloadVideoFlush(List<String> selectedList) {
        if (CollectionUtils.isEmpty(selectedList))
            return;

        List<VideoInfo> videoInfos = new ArrayList<>(selectedList.size());
        for (String videoPath : selectedList) {
            for (VideoInfo videoInfo : mDataList) {
                if (TextUtils.equals(videoPath, videoInfo.relativePath)) {
                    videoInfos.add(videoInfo);
                }
            }
        }

//        List<Task> downloadTasks = new ArrayList<>();
//        Task task = null;
//        String savePath = "";
//        for (String videoPath : selectedList) {
//            savePath = videoPath.substring(videoPath.lastIndexOf("/") + 1);
//            savePath = getSavePath(savePath);
//            task = new Task(videoPath, savePath);
//            downloadTasks.add(task);
//        }
//
//        DownloaderT1spImpl.getInstance().addDownloadTasks(downloadTasks, this);

        final IpcDownloader ipcDownloader = IpcDownloaderImpl.getInstance();
        ipcDownloader.addDownloadFileList(videoInfos);
        ipcDownloader.setListener(new IpcDownloadListener() {
            @Override
            public void onDownloadCountUpdate(int currentDownload, int total) {
                // ????????????: ?????????????????????/?????????
                Log.e("IpcDownloader", currentDownload + "/" + total);
                final String showTxt = getString(R.string.str_video_transfer_ongoing)
                        + currentDownload + getString(R.string.str_slash) + total;
                if (!GlobalWindow.getInstance().isShow()) {
                    GlobalWindow.getInstance().createVideoUploadWindow(showTxt);
                } else {
                    GlobalWindow.getInstance().updateText(currentDownload, total);
                }
            }

            @Override
            public void onProgressUpdate(String fileName, int progress) {
                // ????????????????????????
                Log.e("IpcDownloader", fileName + ": " + progress + "%");
                GlobalWindow.getInstance().refreshPercent(progress);
            }

            @Override
            public void onSingleFileDownloadResult(String fileName, boolean isSuccess, String msg) {
                // ??????????????????????????????
                Log.e("IpcDownloader", fileName + " Result:" + isSuccess);
            }

            @Override
            public void onDownloadedComplete(int countSuccess, int countfailed, int countTotal) {
                // ????????????????????????
                Log.e("IpcDownloader", "onAllDownloaded");
                if (getContext() != null) {
                    $.toast().text(R.string.download_complete).show();
                    GlobalWindow.getInstance().topWindowSucess(getString(R.string.str_video_transfer_success));
                }
                // ????????????????????????Event
                EventBus.getDefault().post(new EventDownloadVideoFinish());
                GlobalWindow.getInstance().dimissGlobalWindow();
            }

            @Override
            public void onSDNoEnoughError(int countSuccess, int countfailed, int countTotal) {

            }

        });
        // ????????????
        ipcDownloader.start();
    }

    /**
     * ???????????????????????????????????????
     *
     * @param videoName ?????????
     */
    private String getSavePath(String videoName) {
        switch ((getVideoType())) {
            case PhotoAlbumConfig.PHOTO_BUM_IPC_WND:
                return FileUtil.WONDERFUL_VIDEO_PATH + videoName;
            case PhotoAlbumConfig.PHOTO_BUM_IPC_URG:
                return FileUtil.URGENT_VIDEO_PATH + videoName;
            case PhotoAlbumConfig.PHOTO_BUM_IPC_LOOP:
                return FileUtil.LOOP_VIDEO_PATH + videoName;
            case PhotoAlbumConfig.PHOTO_BUM_IPC_TIMESLAPSE:
                return FileUtil.REDUCE_VIDEO_PATH + videoName;
        }
        return "";
    }

    public boolean hasLoadedFirst() {
        return isInitLoadData;
    }

    /**
     * ?????????????????????
     */
    public void onEventMainThread(EventDownloadIpcVid event) {
        if (event != null && event.getType() == getVideoType()) {
            List<String> list = new ArrayList<String>();
            list.add(event.getVidPath());
            downloadVideoFlush(list);
        }
    }

    /**
     * ???PhotoAlbumPlayer???????????????????????????
     */
    public void onEventMainThread(EventDeletePhotoAlbumVid event) {
        if (event != null && event.getType() == getVideoType()) {
            List<String> selectedListData = getFragmentAlbum().getSelectedList();
            selectedListData.add(event.getRelativePath());
            deleteListData(selectedListData);
        }
    }

    /**
     * ???????????????????????????
     */
    public void deleteListData(List<String> selectedList) {
        // ????????????????????????
        if (CollectionUtils.isEmpty(selectedList)) {
            closeLoading();
            GolukUtils.showToast(getActivity(), getResources().getString(R.string.str_photo_delete_ok));
            // ????????????????????????
            getFragmentAlbum().resetTopBar();
            // ????????????
            //checkListState();
            parseDataAndShow();
            return;
        }

        // ????????????????????????????????????,????????????,??????????????????
        boolean isAllowDelete = isAllowedDelete(selectedList);
        if (!isAllowDelete) {
            $.toast().text(R.string.str_photo_downing).show();
            getFragmentAlbum().resetTopBar();
            closeLoading();
            return;
        }

        // ????????????
        // http://192.72.1.1/SD/Share/F/SHARE171113-151216F.MP4 --> /SD/Share/F/SHARE171113-151216F.MP4
//        String filelPath = selectedList.get(0);
//        if (filelPath.contains(Const.HTTP_SCHEMA_ADD_IP)) {
//            filelPath = filelPath.substring(Const.HTTP_SCHEMA_ADD_IP.length());
//        }
//        ApiUtil.apiServiceAit().sendRequest(ParamsBuilder.deleteFileParam(filelPath), mDeleteCallback);

        mIpcDeleteOption.deleteRemoteFiles(selectedList);
        showLoading();
    }

    private CommonCallback mDeleteCallback = new CommonCallback() {
        @Override
        public void onStart() {
            //showLoading();
        }

        @Override
        protected void onSuccess() {
            List<String> selectedList = getFragmentAlbum().getSelectedList();
            deleteFromDataByVideoName(selectedList.get(0));
            selectedList.remove(0);
            // ????????????
            //parseDataAndShow();
            // ???????????????
            deleteListData(selectedList);
        }

        @Override
        protected void onServerError(int errorCode, String errorMessage) {
            closeLoading();
        }

        @Override
        public void onFinish() {
            // closeLoading();
        }
    };

    private boolean isAllowedDelete(List<String> deleteList) {
        List<VideoInfo> videoInfos = IpcDownloaderImpl.getInstance().getDownloadingFiles();
        if (!CollectionUtils.isEmpty(videoInfos)) {
            for (VideoInfo videoInfo : videoInfos) {
                for (String filePath : deleteList) {
                    if (filePath.contains(videoInfo.filename))
                        return false;
                }
            }
        }

        return true;
    }

    /**
     * ???????????????????????????????????????????????????
     */
    private void deleteFromDataByVideoName(String videoPath) {
        if (TextUtils.isEmpty(videoPath))
            return;
        int size = mDataList.size();
        VideoInfo tempInfo = null;
        for (int i = 0; i < size; i++) {
            tempInfo = mDataList.get(i);
            if (tempInfo != null && TextUtils.equals(tempInfo.relativePath, videoPath)) {
                mDataList.remove(i);
                return;
            }
        }
    }

    private void removeFromVideoList(List<String> selectedList) {
        if (CollectionUtils.isEmpty(selectedList))
            return;
        for (String path : selectedList) {
            deleteFromDataByVideoName(path);
        }
    }

    /**
     * ??????????????????: ??????/??????/??????
     */
    protected abstract int getVideoType();

    @Override
    public void onThumbDownload(String thumbUrl) {
        // ???????????????????????????
        if (mRemoteVideoAdapter != null)
            mRemoteVideoAdapter.notifyDataSetChanged();
    }

    @Override
    public void onVideoDownloadSuccess(String videoName, boolean sucess) {
        // ????????????????????????,??????
        VideoInfo videoInfo = getVideoInfoByName(videoName);
        if (videoInfo == null)
            return;

        VideoFileInfoBean videoFileInfo = new VideoFileInfoBean();
        videoFileInfo.filename = videoInfo.filename;
        videoFileInfo.resolution = videoInfo.videoHP;
        videoFileInfo.timestamp = videoInfo.videoCreateDate;
        // ??????DB
        if (!GolukVideoInfoDbManager.getInstance().hasSaved(videoFileInfo.filename))
            GolukVideoInfoDbManager.getInstance().addVideoInfoData(videoFileInfo);
    }

    private VideoInfo getVideoInfoByName(String videoName) {
        if (TextUtils.isEmpty(videoName))
            return null;
        for (VideoInfo videoInfo : mDataList) {
            if (TextUtils.equals(videoInfo.filename, videoName))
                return videoInfo;
        }

        return null;
    }

    @Override
    public void onNormalVideoListQueryed(ArrayList<VideoInfo> fileList) {
        updateData(fileList);
    }

    @Override
    public void onUrgentVideoListQueryed(ArrayList<VideoInfo> fileList) {
        updateData(fileList);
    }

    @Override
    public void onCaptureVideoListQueryed(ArrayList<VideoInfo> fileList) {
        updateData(fileList);
    }

    @Override
    public void onTimeslapseVideoListQueryed(ArrayList<VideoInfo> fileList) {
        updateData(fileList);
    }

    @Override
    public void onGetVideoListIsEmpty() {

    }

    @Override
    public void onQueryVideoListFailed() {

    }

    @Override
    public void onRemoteFileDeleted(boolean success) {
        closeLoading();
        GolukUtils.showToast(getActivity(), getResources().getString(R.string.str_photo_delete_ok));
        removeFromVideoList(getFragmentAlbum().getSelectedList());
        getFragmentAlbum().getSelectedList().clear();
        // ????????????????????????
        getFragmentAlbum().resetTopBar();
        // ????????????
        //checkListState();
        parseDataAndShow();
    }

}