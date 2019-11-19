package com.mobnote.golukmain.photoalbum;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import android.widget.Toast;

import com.elvishew.xlog.XLog;
import com.emilsjolander.components.stickylistheaders.StickyListHeadersListView;
import com.mobnote.application.FloatWindowPermissionUtil;
import com.mobnote.application.GolukApplication;
import com.mobnote.eventbus.EventConfig;
import com.mobnote.eventbus.EventDeletePhotoAlbumVid;
import com.mobnote.eventbus.EventDownloadIpcVid;
import com.mobnote.eventbus.EventIpcConnState;
import com.mobnote.eventbus.EventSingleConnSuccess;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.carrecorder.IpcDataParser;
import com.mobnote.golukmain.carrecorder.entity.DoubleVideoInfo;
import com.mobnote.golukmain.carrecorder.entity.VideoInfo;
import com.mobnote.golukmain.carrecorder.util.SettingUtils;
import com.mobnote.golukmain.carrecorder.view.CustomLoadingDialog;
import com.mobnote.golukmain.promotion.PromotionSelectItem;
import com.mobnote.golukmain.wifibind.WiFiLinkListActivity;
import com.mobnote.t1sp.util.FileUtil;
import com.mobnote.log.app.LogConst;
import com.mobnote.util.GolukUtils;
import com.mobnote.util.ZhugeUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.com.mobnote.module.ipcmanager.IPCManagerFn;
import cn.com.tiros.api.FileUtils;
import cn.com.tiros.debug.GolukDebugUtils;
import de.greenrobot.event.EventBus;

public class LoopFragment extends Fragment implements IPCManagerFn, LocalWonderfulVideoAdapter.IListViewItemClickColumn {
    private View mLoopVideoView;

    /**
     * 列表数据加载中标识
     */
    private boolean isGetFileListDataing = false;

    private List<VideoInfo> mDataList = null;
    private List<DoubleVideoInfo> mDoubleDataList = null;

    private CustomLoadingDialog mCustomProgressDialog = null;

    private StickyListHeadersListView mStickyListHeadersListView = null;
    private CloudWonderfulVideoAdapter mCloudWonderfulVideoAdapter = null;

    /**
     * 保存列表一个显示项索引
     */
    private int firstVisible;
    /**
     * 保存列表显示item个数
     */
    private int visibleCount;

    /**
     * 判断服务端是否还有数据
     */
    private boolean isHasData = true;

    /**
     * 数据分页个数
     */
    private final int pageCount = 40;

    /**
     * 列表最后的时间戳
     */
    private int lastTime = 0;

    /**
     * 列表添加页脚标识
     */
    private boolean addFooter = false;

    private FragmentAlbum mFragmentAlbum;

    public boolean isShowPlayer = false;

    /**
     * 添加列表底部加载中布局
     */
    private RelativeLayout mBottomLoadingView = null;

    private int timeend = 2147483647;

    private List<String> mGroupListName = null;

    private TextView empty = null;

    private boolean isListener = false;

    /**
     * 防止重复下载
     */
    List<Boolean> exist = new ArrayList<Boolean>();

    private boolean mIntent2WifiConn;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (mLoopVideoView == null) {
            mFragmentAlbum = (FragmentAlbum) getParentFragment();
            this.mDataList = new ArrayList<VideoInfo>();
            this.mDoubleDataList = new ArrayList<DoubleVideoInfo>();
            this.mGroupListName = new ArrayList<String>();
            this.mLoopVideoView = inflater.inflate(R.layout.wonderful_listview, null, false);
            initView();
        }
        ViewGroup parent = (ViewGroup) mLoopVideoView.getParent();
        if (parent != null) {
            parent.removeView(mLoopVideoView);
        }
        return mLoopVideoView;
    }

    @Override
    public void onResume() {
        super.onResume();
        isShowPlayer = false;
        mIntent2WifiConn = false;
        if (null != GolukApplication.getInstance().getIPCControlManager()) {
            GolukApplication.getInstance().getIPCControlManager()
                    .addIPCManagerListener("filemanager" + IPCManagerFn.TYPE_CIRCULATE, this);
            isListener = true;
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        if (null != GolukApplication.getInstance().getIPCControlManager()) {
            GolukApplication.getInstance().getIPCControlManager()
                    .removeIPCManagerListener("filemanager" + IPCManagerFn.TYPE_CIRCULATE);
            isListener = false;
            if (isGetFileListDataing) {
                this.removeFooterView();
                isGetFileListDataing = false;
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    public void onEventMainThread(EventDeletePhotoAlbumVid event) {
        if (event != null && event.getType() == PhotoAlbumConfig.PHOTO_BUM_IPC_LOOP) {

            List<String> list = new ArrayList<String>();
            list.add(FileUtil.getFileNameFromPath(event.getVidPath()));
            deleteListData(list);
        }
    }


    /**
     * 从设备上下载视频到本地
     *
     * @param event
     */
    public void onEventMainThread(EventDownloadIpcVid event) {
        if (event != null && event.getType() == PhotoAlbumConfig.PHOTO_BUM_IPC_LOOP) {

            List<String> list = new ArrayList<String>();
            list.add(FileUtil.getFileNameFromPath(event.getVidPath()));
            downloadVideoFlush(list);
        }
    }

    public void onEventMainThread(EventSingleConnSuccess event) {
        if (mIntent2WifiConn) {
            loadData(true);
        }
    }

    public void downloadVideoFlush(List<String> selectedListData) {

        boolean hasPermission = FloatWindowPermissionUtil.judgePermission(getContext());
        if (!hasPermission) {
            Toast.makeText(getContext(), getContext().getString(R.string.str_system_window_not_allowed), Toast.LENGTH_SHORT).show();
        }

        exist.clear();
        for (String filename : selectedListData) {
            // 下载视频对应的图片
            String imgFileName = filename.replace(".mp4", ".jpg");
            String filePath = GolukApplication.getInstance().getCarrecorderCachePath() + File.separator + "image";
            File imgfile = new File(filePath + File.separator + imgFileName);
            if (!imgfile.exists()) {
                GolukApplication
                        .getInstance()
                        .getIPCControlManager()
                        .downloadFile(imgFileName, "download", FileUtils.javaToLibPath(filePath),
                                PhotoAlbumUtils.findtime(filename, mDataList));
            }

            // 下载视频文件
            String mp4 = FileUtils.libToJavaPath(PhotoAlbumConfig.LOCAL_LOOP_VIDEO_PATH + filename);

            File file = new File(mp4);
            if (!file.exists()) {
                List<String> downloadlist = GolukApplication.getInstance().getDownLoadList();
                if (!downloadlist.contains(filename)) {
                    exist.add(false);
                    Log.i("download", "download:" + mp4);
                    boolean a = GolukApplication.getInstance().getIPCControlManager().querySingleFile(filename);
                    GolukDebugUtils.e("xuhw", "YYYYYY===a=" + a + "==querySingleFile======filename=" + filename);
                }
            } else {
                exist.add(true);
            }

        }

        boolean isshow = false;
        for (boolean flag : exist) {
            if (!flag) {
                isshow = false;
                break;
            } else {
                isshow = true;
            }
        }

        if (isshow) {
            GolukUtils.showToast(getActivity(), getActivity().getString(R.string.str_synchronous_video_loaded));
        }
    }

    public void deleteListData(List<String> deleteData) {
        for (String path : deleteData) {
            for (VideoInfo info : mDataList) {
                if (info.filename.equals(path)) {
                    GolukApplication.getInstance().getIPCControlManager().deleteFile(path);
                    mDataList.remove(info);
                    String filename = path.replace(".mp4", ".jpg");
                    SettingUtils.getInstance().putBoolean("Cloud_" + filename, true);
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
        mCloudWonderfulVideoAdapter.setData(mGroupListName, mDoubleDataList);
        checkListState();
    }

    private void initView() {
        empty = (TextView) mLoopVideoView.findViewById(R.id.empty);
        this.mCustomProgressDialog = new CustomLoadingDialog(getActivity(), null);
        mStickyListHeadersListView = (StickyListHeadersListView) mLoopVideoView
                .findViewById(R.id.mStickyListHeadersListView);
        mCloudWonderfulVideoAdapter = new CloudWonderfulVideoAdapter(getActivity(),
                (FragmentAlbum) getParentFragment(), mStickyListHeadersListView, this);
        setListener();
    }

    private void setListener() {
        mStickyListHeadersListView.setOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView arg0, int scrollState) {
                switch (scrollState) {
                    case AbsListView.OnScrollListener.SCROLL_STATE_FLING:
                        // mCloudWonderfulVideoAdapter.lock();
                        break;
                    case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
                        // mCloudWonderfulVideoAdapter.unlock();
                        GolukDebugUtils.e("", "YYYYYY=====SCROLL_STATE_IDLE====11111111111=");
                        if (mStickyListHeadersListView.getAdapter().getCount() == (firstVisible + visibleCount)) {
                            GolukDebugUtils.e("", "YYYYYY=====SCROLL_STATE_IDLE====22222222=");
                            final int size = mDataList.size();
                            if (size > 0 && isHasData) {
                                GolukDebugUtils.e("", "YYYYYY=====SCROLL_STATE_IDLE====33333=isGetFileListDataing="
                                        + isGetFileListDataing + "====mDataList.size()=" + mDataList.size());
                                if (isGetFileListDataing) {
                                    return;
                                }
                                GolukDebugUtils.e("", "YYYYYY=====SCROLL_STATE_IDLE====44444=");
                                isGetFileListDataing = true;
                                boolean isSucess = GolukApplication.getInstance().getIPCControlManager()
                                        .queryFileListInfo(IPCManagerFn.TYPE_CIRCULATE, pageCount, 0, lastTime, "1");
                                GolukDebugUtils.e("", "YYYYYY=====queryFileListInfo====isSucess=" + isSucess);
                                if (!isSucess) {
                                    isGetFileListDataing = false;
                                } else {
                                    addFooterView();
                                }

                            }
                        }
                        break;
                    case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                        // mCloudWonderfulVideoAdapter.lock();
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
                    ZhugeUtils.eventAlbumClickToConnectIPC(getActivity());
                    mIntent2WifiConn = true;
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
                    getString(R.string.str_zhuge_video_player_recycle),
                    getString(R.string.str_zhuge_video_player_recycle));
            GolukUtils.startPhotoAlbumPlayerActivity(LoopFragment.this.getContext(), PhotoAlbumConfig.PHOTO_BUM_IPC_LOOP, "ipc", path,
                    filename, createTime, videoHP, size, (PromotionSelectItem) getActivity().getIntent().getSerializableExtra(PhotoAlbumPlayer.ACTIVITY_INFO));

            XLog.tag(LogConst.TAG_ALUMB).i("Click remote loop video, info:\n" +
                    "FileName:%s, Path:%s, HP:%s, Size:%s", filename, path, videoHP, size);
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
                mFragmentAlbum.updateTitleName(getActivity().getString(R.string.local_video_title_text));
                mFragmentAlbum.updateDeleteState(false);
            } else {
                mFragmentAlbum.updateDeleteState(true);
                mFragmentAlbum.updateTitleName(getActivity().getString(R.string.str_photo_select,
                        selectedListData.size()));
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
            mBottomLoadingView = (RelativeLayout) LayoutInflater.from(getActivity()).inflate(
                    R.layout.video_square_below_loading, null);
            mStickyListHeadersListView.addFooterView(mBottomLoadingView);
        }
    }

    @SuppressLint("NewApi")
    public void loadData(boolean flag) {
        if (isGetFileListDataing || mFragmentAlbum == null || empty == null) {
            return;
        }

        if (null == empty || null == mStickyListHeadersListView) {
            return;
        }

        if (flag) {
            if (null != mCustomProgressDialog && !mCustomProgressDialog.isShowing()) {
                mCustomProgressDialog.show();
            }
            empty.setVisibility(View.GONE);
            mStickyListHeadersListView.setVisibility(View.VISIBLE);
            isGetFileListDataing = true;
            mDataList.clear();
            boolean isSucess = GolukApplication.getInstance().getIPCControlManager()
                    .queryFileListInfo(IPCManagerFn.TYPE_CIRCULATE, pageCount, 0, timeend, "1");
            GolukDebugUtils.e("", "YYYYYY=====queryFileListInfo====isSucess=" + isSucess);
            if (!isSucess) {
                isGetFileListDataing = false;
                if (null != mCustomProgressDialog && mCustomProgressDialog.isShowing()) {
                    mCustomProgressDialog.close();
                }
            }
        } else {
            mFragmentAlbum.setEditBtnState(false);
            Drawable drawable = this.getResources().getDrawable(R.drawable.img_no_video);
            empty.setCompoundDrawablesRelativeWithIntrinsicBounds(null, drawable, null, null);
            empty.setText(getActivity().getResources().getString(R.string.str_album_no_connect));
            empty.setVisibility(View.VISIBLE);
            mStickyListHeadersListView.setVisibility(View.GONE);
        }

    }

    private void updateEditState(boolean isHasData) {
        mFragmentAlbum.setEditBtnState(isHasData);
    }

    @SuppressLint("NewApi")
    private void checkListState() {
        if (mDataList.size() <= 0) {
            Drawable drawable = this.getResources().getDrawable(R.drawable.album_img_novideo);
            empty.setCompoundDrawablesRelativeWithIntrinsicBounds(null, drawable, null, null);
            empty.setText(getActivity().getResources().getString(R.string.photoalbum_no_video_text));
            empty.setVisibility(View.VISIBLE);
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

    private void updateData(ArrayList<VideoInfo> fileList) {
        addFooterView();
        if (mDataList.size() == 0) {
            mStickyListHeadersListView.setAdapter(mCloudWonderfulVideoAdapter);
        }
        mDataList.addAll(fileList);
        if (fileList.size() < pageCount) {
            isHasData = false;
            removeFooterView();
        } else {
            isHasData = true;
        }
        mDoubleDataList.clear();
        mDoubleDataList = VideoDataManagerUtils.videoInfo2Double(mDataList);
        mGroupListName = VideoDataManagerUtils.getGroupName(mDataList);
        mCloudWonderfulVideoAdapter.setData(mGroupListName, mDoubleDataList);
    }

    public void onEventMainThread(EventIpcConnState event) {
        if (null == event) {
            return;
        }
        if (mFragmentAlbum != null && mFragmentAlbum.mCurrentType == PhotoAlbumConfig.PHOTO_BUM_IPC_LOOP && isListener == true) {
            switch (event.getmOpCode()) {

                case EventConfig.IPC_DISCONNECT:
                    //showConnectionDialog();
                    break;
                case EventConfig.IPC_CONNECT:
                    loadData(true);
                    break;
                default:
                    break;
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

    @Override
    public void IPCManage_CallBack(int event, int msg, int param1, Object param2) {
        switch (event) {
            case ENetTransEvent_IPC_VDCP_CommandResp:
                if (IPC_VDCP_Msg_Query == msg && mFragmentAlbum != null
                        && mFragmentAlbum.mCurrentType == PhotoAlbumConfig.PHOTO_BUM_IPC_LOOP) {
                    if (mCustomProgressDialog != null && mCustomProgressDialog.isShowing()) {
                        mCustomProgressDialog.close();
                    }
                    isGetFileListDataing = false;
                    GolukDebugUtils.e("xuhw", "YYYYYY=======获取文件列表===@@@======param1=" + param1 + "=====param2=" + param2);
                    if (RESULE_SUCESS == param1) {
                        if (TextUtils.isEmpty((String) param2)) {
                            return;
                        }

                        String tag = IpcDataParser.getIpcQueryListReqTag((String) param2);
                        if (!tag.equals(PhotoAlbumConfig.VIDEO_LIST_TAG_PHOTO)) {
                            return;
                        }

                        ArrayList<VideoInfo> fileList = IpcDataParser.parseVideoListData((String) param2);
                        if (null != fileList && fileList.size() > 0) {
                            int type = IpcDataParser.parseVideoFileType(fileList.get(0).filename);
                            if (type != IPCManagerFn.TYPE_CIRCULATE) {
                                return;
                            }

                            VideoInfo vfi = null;
                            if (fileList.size() > 0) {
                                vfi = fileList.get(fileList.size() - 1);
                                lastTime = (int) vfi.time - 1;
                            }

                            updateData(fileList);
                        } else {
                            isHasData = false;
                            removeFooterView();
                        }
                    } else {
                        GolukDebugUtils.e("xuhw", "YYYYYY=======获取文件列表====fail==@@@======param1=" + param1);
                        // 命令发送失败
                        this.removeFooterView();
                        GolukUtils.showToast(getActivity(), getActivity().getString(R.string.str_inquiry_fail));
                    }
                    checkListState();
                }
                break;
            // IPC下载结果应答
            case ENetTransEvent_IPC_VDTP_Resp:
                // 文件传输消息
                if (IPC_VDTP_Msg_File == msg) {
                    // 文件下载成功
                    if (RESULE_SUCESS == param1) {
                        if (TextUtils.isEmpty((String) (param2))) {
                            return;
                        }
                        try {
                            JSONObject json = new JSONObject((String) param2);
                            if (null != json) {
                                String filename = json.optString("filename");
                                String tag = json.optString("tag");

                                if (tag.contains("IPC_IMAGE")) {
                                    int type = IpcDataParser.parseVideoFileType(filename);
                                    if (type != IPCManagerFn.TYPE_CIRCULATE) {
                                        return;
                                    }

                                    if (null != mCloudWonderfulVideoAdapter) {
                                        mCloudWonderfulVideoAdapter.updateImage(filename);
                                    }

                                } else {
                                    GolukDebugUtils.e("xuhw", "TTT======no filelist  file======filename=" + filename);
                                }

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        // 文件下载中进度
                    } else if (1 == param1) {
                        // param1为文件下载进度
                    } else {
                        // 其他下载失败
                    }
                }
                break;

            default:
                break;
        }

    }

    @Override
    public void onItemClicked(View parent, DoubleVideoInfo videoInfo, int columnIndex) {
        RelativeLayout mTMLayout1 = (RelativeLayout) parent.findViewById(R.id.mTMLayout1);
        RelativeLayout mTMLayout2 = (RelativeLayout) parent.findViewById(R.id.mTMLayout2);
        String tag1 = (String) mTMLayout1.getTag();
        String tag2 = (String) mTMLayout2.getTag();
        if (mFragmentAlbum.getEditState()) {
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
                gotoVideoPlayPage(PhotoAlbumConfig.PHOTO_BUM_IPC_LOOP, info1.videoPath,
                        info1.filename, info1.videoCreateDate, info1.videoHP, info1.videoSize);
                String filename = d.getVideoInfo1().filename;
                updateNewState(filename);

                videoInfo.getVideoInfo1().isNew = false;
                mCloudWonderfulVideoAdapter.notifyDataSetChanged();
            } else {
                // 点击列表右边项,跳转到视频播放页面
                VideoInfo info2 = d.getVideoInfo2();
                if (null == info2)
                    return;
                gotoVideoPlayPage(PhotoAlbumConfig.PHOTO_BUM_IPC_LOOP, info2.videoPath,
                        info2.filename, info2.videoCreateDate, info2.videoHP, info2.videoSize);
                String filename = info2.filename;
                updateNewState(filename);

                videoInfo.getVideoInfo2().isNew = false;
                mCloudWonderfulVideoAdapter.notifyDataSetChanged();
            }
        }
    }
}