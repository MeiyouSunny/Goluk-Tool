package com.mobnote.golukmain.photoalbum;

import android.annotation.SuppressLint;
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

public class WonderfulFragment extends Fragment implements IPCManagerFn, LocalWonderfulVideoAdapter.IListViewItemClickColumn {

    private View mWonderfulVideoView;

    /**
     * ???????????????????????????
     */
    private boolean isGetFileListDataing = false;

    private List<VideoInfo> mDataList = null;
    private List<DoubleVideoInfo> mDoubleDataList = null;
    private CustomLoadingDialog mCustomProgressDialog = null;

    private StickyListHeadersListView mStickyListHeadersListView = null;
    private CloudWonderfulVideoAdapter mCloudWonderfulVideoAdapter = null;

    /**
     * ?????????????????????????????????
     */
    private int firstVisible;
    /**
     * ??????????????????item??????
     */
    private int visibleCount;

    /**
     * ?????????????????????????????????
     */
    private boolean isHasData = true;

    /**
     * ??????????????????
     */
    private final int pageCount = 40;

    /**
     * ????????????????????????
     */
    private int lastTime = 0;

    /**
     * ????????????????????????
     */
    private boolean addFooter = false;

    private FragmentAlbum mFragmentAlbum;

    public boolean isShowPlayer = false;

    /**
     * ?????????????????????????????????
     */
    private RelativeLayout mBottomLoadingView = null;

    private int timeend = 2147483647;

    private List<String> mGroupListName = null;

    private TextView empty = null;

    private boolean isListener = false;

    /**
     * ??????????????????
     */
    List<Boolean> exist = new ArrayList<Boolean>();

    /**
     *
     */
    private boolean mIntent2WifiConn;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GolukDebugUtils.e("", "crash zh start App ------ WonderfulFragment-----onCreate------------:");
        EventBus.getDefault().register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        GolukDebugUtils.e("", "crash zh start App ------ WonderfulFragment-----onCreateView------------:");
        if (mWonderfulVideoView == null) {
            if (mWonderfulVideoView == null) {
                mWonderfulVideoView = inflater.inflate(R.layout.wonderful_listview, (ViewGroup) getActivity()
                        .findViewById(R.id.viewpager), false);
            }

            mBottomLoadingView = (RelativeLayout) LayoutInflater.from(getActivity()).inflate(
                    R.layout.video_square_below_loading, null);

            mFragmentAlbum = getFragmentAlbum();
            mDataList = new ArrayList<>();
            mDoubleDataList = new ArrayList<>();
            mGroupListName = new ArrayList<>();

            mWonderfulVideoView = inflater.inflate(R.layout.wonderful_listview, null, false);
            initView();
        }

        ViewGroup parent = (ViewGroup) mWonderfulVideoView.getParent();
        if (parent != null) {
            parent.removeView(mWonderfulVideoView);
        }

        return mWonderfulVideoView;
    }

    public FragmentAlbum getFragmentAlbum() {
        if (mFragmentAlbum == null) {
            GolukDebugUtils.e("", "crash zh start App ------ WonderfulFragment-----getFragmentAlbum------------: null");
            mFragmentAlbum = (FragmentAlbum) getParentFragment();
        }
        GolukDebugUtils.e("", "crash zh start App ------ WonderfulFragment-----getFragmentAlbum------------: not null");
        return mFragmentAlbum;

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    /**
     * ??????????????????event
     *
     * @param event
     */
    public void onEventMainThread(EventDeletePhotoAlbumVid event) {
        if (event != null && event.getType() == PhotoAlbumConfig.PHOTO_BUM_IPC_WND) {

            List<String> list = new ArrayList<String>();
            list.add(event.getVidPath());
            deleteListData(list);
        }
    }

    public void onEventMainThread(EventSingleConnSuccess event) {
        if (mIntent2WifiConn) {
            loadData(true);
        }
    }

    public void onEventMainThread(EventIpcConnState event) {
        if (null == event) {
            return;
        }
        if (getFragmentAlbum().mCurrentType == PhotoAlbumConfig.PHOTO_BUM_IPC_WND && isListener == true) {
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
     * ?????????????????????????????????
     *
     * @param event
     */
    public void onEventMainThread(EventDownloadIpcVid event) {
        if (event != null && event.getType() == PhotoAlbumConfig.PHOTO_BUM_IPC_WND) {

            List<String> list = new ArrayList<String>();
            list.add(event.getVidPath());
            downloadVideoFlush(list);
        }
    }

    public void downloadVideoFlush(List<String> selectedListData) {

        boolean hasPermission = FloatWindowPermissionUtil.judgePermission(getContext());
        if (!hasPermission) {
            Toast.makeText(getContext(), getContext().getString(R.string.str_system_window_not_allowed), Toast.LENGTH_SHORT).show();
        }

        exist.clear();
        for (String filename : selectedListData) {
            // ???????????????????????????
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

            // ??????????????????
            String mp4 = FileUtils.libToJavaPath(PhotoAlbumConfig.LOCAL_WND_VIDEO_PATH + filename);

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
        empty = (TextView) mWonderfulVideoView.findViewById(R.id.empty);
        this.mCustomProgressDialog = new CustomLoadingDialog(getActivity(), null);
        mStickyListHeadersListView = (StickyListHeadersListView) mWonderfulVideoView
                .findViewById(R.id.mStickyListHeadersListView);
        mCloudWonderfulVideoAdapter = new CloudWonderfulVideoAdapter(getActivity(),
                (FragmentAlbum) getParentFragment(), mStickyListHeadersListView, this);
        setListener();
        if (GolukApplication.getInstance().isIpcLoginSuccess) {
            loadData(true);
        } else {
            loadData(false);
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
            // ????????????
            if (columnIndex == LocalWonderfulVideoAdapter.IListViewItemClickColumn.COLUMN_FIRST) {
                // ?????????????????????,???????????????????????????
                VideoInfo info1 = d.getVideoInfo1();
                gotoVideoPlayPage(PhotoAlbumConfig.PHOTO_BUM_IPC_WND, info1.videoPath,
                        info1.filename, info1.videoCreateDate, info1.videoHP, info1.videoSize);
                String filename = d.getVideoInfo1().filename;
                updateNewState(filename);

                videoInfo.getVideoInfo1().isNew = false;
                mCloudWonderfulVideoAdapter.notifyDataSetChanged();
            } else {
                // ?????????????????????,???????????????????????????
                VideoInfo info2 = d.getVideoInfo2();
                if (null == info2)
                    return;
                gotoVideoPlayPage(PhotoAlbumConfig.PHOTO_BUM_IPC_WND, info2.videoPath,
                        info2.filename, info2.videoCreateDate, info2.videoHP, info2.videoSize);
                String filename = info2.filename;
                updateNewState(filename);

                videoInfo.getVideoInfo2().isNew = false;
                mCloudWonderfulVideoAdapter.notifyDataSetChanged();
            }
        }
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
                        if (mStickyListHeadersListView.getAdapter() == null)
                            break;

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
                                        .queryFileListInfo(IPCManagerFn.TYPE_SHORTCUT, pageCount, 0, lastTime, "1");
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
     * ?????????????????????????????????
     *
     * @param path
     */
    private void gotoVideoPlayPage(int from, String path, String filename, String createTime, String videoHP, String size) {
        if (!isShowPlayer) {
            isShowPlayer = true;
            ZhugeUtils.eventAlbumPlayer(getActivity(),
                    getString(R.string.str_zhuge_video_player_wonderful),
                    getString(R.string.str_zhuge_video_player_wonderful));
            GolukUtils.startPhotoAlbumPlayerActivity(WonderfulFragment.this.getContext(), PhotoAlbumConfig.PHOTO_BUM_IPC_WND, "ipc", path,
                    filename, createTime, videoHP, size, (PromotionSelectItem) getActivity().getIntent().getSerializableExtra(PhotoAlbumPlayer.ACTIVITY_INFO));

            XLog.tag(LogConst.TAG_ALUMB).i("Click remote wonderful video, info:\n" +
                    "FileName:%s, Path:%s, HP:%s, Size:%s", filename, path, videoHP, size);
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
                        selectedListData.size()));
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
        mIntent2WifiConn = false;
        GolukDebugUtils.e("", "crash zh start App ------ WonderfulFragment-----onResume------------:");
        isShowPlayer = false;
        if (null != GolukApplication.getInstance().getIPCControlManager()) {
            GolukApplication.getInstance().getIPCControlManager()
                    .addIPCManagerListener("filemanager" + IPCManagerFn.TYPE_SHORTCUT, this);
            isListener = true;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (null != GolukApplication.getInstance().getIPCControlManager()) {
            GolukApplication.getInstance().getIPCControlManager()
                    .removeIPCManagerListener("filemanager" + IPCManagerFn.TYPE_SHORTCUT);
            isListener = false;
            if (isGetFileListDataing) {
                this.removeFooterView();
                isGetFileListDataing = false;
            }
        }
    }

    @SuppressLint("NewApi")
    public void loadData(boolean flag) {
        GolukDebugUtils.e("", "crash zh start App ------ WonderfulFragment-----loadData------------:");
        if (isGetFileListDataing) {
            return;
        }

        if(null == empty || null == mStickyListHeadersListView) {
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
                    .queryFileListInfo(IPCManagerFn.TYPE_SHORTCUT, pageCount, 0, timeend, "1");
            GolukDebugUtils.e("", "YYYYYY=====queryFileListInfo====isSucess=" + isSucess);
            if (!isSucess) {
                isGetFileListDataing = false;
                if (null != mCustomProgressDialog && mCustomProgressDialog.isShowing()) {
                    mCustomProgressDialog.close();
                }
            }
        } else {
            Drawable drawable = this.getResources().getDrawable(R.drawable.img_no_video);
            getFragmentAlbum().setEditBtnState(false);
            empty.setCompoundDrawablesRelativeWithIntrinsicBounds(null, drawable, null, null);
            empty.setText(getActivity().getResources().getString(R.string.str_album_no_connect));
            empty.setVisibility(View.VISIBLE);
            mStickyListHeadersListView.setVisibility(View.GONE);
        }

    }

    private void updateEditState(boolean isHasData) {
        getFragmentAlbum().setEditBtnState(isHasData);
        /*
         * GolukDebugUtils.e("",
		 * "Album------WondowvideoListView------updateEditState" + isHasData);
		 * if (null == mCloudVideoListView) { return; }
		 * mCloudVideoListView.updateEdit(4, isHasData);
		 */
    }

    @SuppressLint("NewApi")
    private void checkListState() {
        if (mDataList.size() <= 0) {
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

    @Override
    public void IPCManage_CallBack(int event, int msg, int param1, Object param2) {
        switch (event) {
            case ENetTransEvent_IPC_VDCP_CommandResp:
                if (IPC_VDCP_Msg_Query == msg && getFragmentAlbum() != null
                        && getFragmentAlbum().mCurrentType == PhotoAlbumConfig.PHOTO_BUM_IPC_WND) {
                    if (mCustomProgressDialog != null && mCustomProgressDialog.isShowing()) {
                        mCustomProgressDialog.close();
                    }
                    isGetFileListDataing = false;
                    GolukDebugUtils.e("xuhw", "YYYYYY=======??????????????????===@@@======param1=" + param1 + "=====param2=" + param2);
                    XLog.tag(LogConst.TAG_ALUMB).i("Query remote wonderful video list: param1%s,\nData%s", param1, (String) param2);
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
                            if (type != IPCManagerFn.TYPE_SHORTCUT) {
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
                        GolukDebugUtils.e("xuhw", "YYYYYY=======??????????????????====fail==@@@======param1=" + param1);
                        // ??????????????????
                        this.removeFooterView();
                        GolukUtils.showToast(getActivity(), getActivity().getString(R.string.str_inquiry_fail));
                    }
                    checkListState();
                }
                break;

            // IPC??????????????????
            case ENetTransEvent_IPC_VDTP_Resp:
                // ??????????????????
                if (IPC_VDTP_Msg_File == msg) {
                    // ??????????????????

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
                                    if (type != IPCManagerFn.TYPE_SHORTCUT) {
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
                        // ?????????????????????
                    } else if (1 == param1) {
                        // param1?????????????????????
                    } else {
                        // ??????????????????
                    }
                }
                break;

            default:
                break;
        }

    }
}