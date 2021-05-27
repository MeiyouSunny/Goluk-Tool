package com.mobnote.videoedit.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.emilsjolander.components.stickylistheaders.StickyListHeadersListView;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.carrecorder.entity.DoubleVideoInfo;
import com.mobnote.golukmain.carrecorder.entity.VideoInfo;
import com.mobnote.golukmain.carrecorder.util.SoundUtils;
import com.mobnote.golukmain.carrecorder.view.CustomLoadingDialog;
import com.mobnote.golukmain.photoalbum.DataCallBack;
import com.mobnote.golukmain.photoalbum.LocalDataLoadAsyncTask;
import com.mobnote.golukmain.photoalbum.VideoDataManagerUtils;
import com.mobnote.videoedit.adapter.LocalVideoChooseAdapter;

import java.util.ArrayList;
import java.util.List;

import cn.com.mobnote.module.ipcmanager.IPCManagerFn;

public class VideoChooseFragment extends Fragment {
    private View mLocalVideoView;

    private CustomLoadingDialog mCustomProgressDialog = null;

    private StickyListHeadersListView mStickyListHeadersListView = null;

    private LocalVideoChooseAdapter mWonderfulVideoAdapter = null;

    private List<VideoInfo> mDataList = null;
    private List<DoubleVideoInfo> mDoubleDataList = null;

    /** 保存屏幕点击横坐标点 */
    private float screenX = 0;
    private int screenWidth = 0;

    private float density = 1;

    private TextView empty = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mLocalVideoView = inflater.inflate(R.layout.wonderful_listview, null, false);
        density = SoundUtils.getInstance().getDisplayMetrics().density;

        mDataList = new ArrayList<VideoInfo>();
        mDoubleDataList = new ArrayList<DoubleVideoInfo>();
        screenWidth = SoundUtils.getInstance().getDisplayMetrics().widthPixels;
        initView();
        loadData(true);

        return mLocalVideoView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void initView() {
        empty = (TextView) mLocalVideoView.findViewById(R.id.empty);
        mCustomProgressDialog = new CustomLoadingDialog(getActivity(), null);
        mStickyListHeadersListView = (StickyListHeadersListView) mLocalVideoView
                .findViewById(R.id.mStickyListHeadersListView);

        mWonderfulVideoAdapter = new LocalVideoChooseAdapter(getActivity(), IPCManagerFn.TYPE_CIRCULATE, "local");
        setListener();
    }

    private void setListener() {
        mStickyListHeadersListView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                screenX = arg1.getX();
                return false;
            }
        });

        mStickyListHeadersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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

                    DoubleVideoInfo d = mDoubleDataList.get(arg2);
                    // 点击播放
                    if ((screenX > 0) && (screenX < (screenWidth / 2))) {
                        // 点击列表左边项,跳转到视频播放页面
                        VideoInfo info1 = d.getVideoInfo1();
                        gotoVideoEditPage(getVideoType(info1.filename), info1.videoPath, info1.videoCreateDate,
                                info1.videoHP, info1.videoSize);
                        String filename = d.getVideoInfo1().filename;

                        mDoubleDataList.get(arg2).getVideoInfo1().isNew = false;
                        mWonderfulVideoAdapter.notifyDataSetChanged();
                    } else {
                        // 点击列表右边项,跳转到视频播放页面
                        VideoInfo info2 = d.getVideoInfo2();
                        if (null == info2)
                            return;

                        gotoVideoEditPage(getVideoType(info2.filename), info2.videoPath,
                                info2.videoCreateDate, info2.videoHP, info2.videoSize);
                        String filename = info2.filename;

                        mDoubleDataList.get(arg2).getVideoInfo2().isNew = false;
                        mWonderfulVideoAdapter.notifyDataSetChanged();
                    }
                }
            }
        });
    }

    public void loadData(boolean flag) {
        if (flag) {
            if (!mCustomProgressDialog.isShowing()) {
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

                mStickyListHeadersListView.setAdapter(mWonderfulVideoAdapter);
                try {
                    if (mCustomProgressDialog.isShowing()) {
                        mCustomProgressDialog.close();
                    }
                } catch (Exception e) {

                }
            }
        });
        task.execute("Load local videos");
    }

    private void gotoVideoEditPage(int type, String path, String createTime, String videoHP, String size) {
        if (!TextUtils.isEmpty(path)) {
            Bundle bundle = new Bundle();
            bundle.putString("vidPath", path);
            Intent intent = new Intent();
            intent.putExtras(bundle);
            getActivity().setResult(Activity.RESULT_OK, intent);
            getActivity().finish();
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
