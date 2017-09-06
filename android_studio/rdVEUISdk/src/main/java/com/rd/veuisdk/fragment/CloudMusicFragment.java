package com.rd.veuisdk.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

import com.rd.veuisdk.R;
import com.rd.veuisdk.adapter.MyMusicAdapter;
import com.rd.veuisdk.adapter.MyMusicAdapter.TreeNode;
import com.rd.veuisdk.database.SDMusicData;
import com.rd.veuisdk.database.WebMusicData;
import com.rd.veuisdk.hb.views.PinnedSectionListView;
import com.rd.veuisdk.model.AudioMusicInfo;
import com.rd.veuisdk.model.IMusicApi;
import com.rd.veuisdk.model.MyMusicInfo;
import com.rd.veuisdk.model.WebMusicInfo;
import com.rd.veuisdk.utils.ExtScanMediaDialog;
import com.rd.veuisdk.utils.SysAlertDialog;

import java.io.File;
import java.util.ArrayList;

/**
 * 云音乐->单个分类的fragment
 *
 * @author JIAN
 * @date 2017-5-17 上午10:56:45
 */
public class CloudMusicFragment extends BaseV4Fragment {
    public CloudMusicFragment() {

    }

    private MySdReceiver mReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mIsDownloaded = getString(R.string.downloaded);
        WebMusicData.getInstance().initilize(getActivity());
        SDMusicData.getInstance().initilize(getActivity());
        mPageName = getString(R.string.mymusic);
        mReceiver = new MySdReceiver();
        getActivity().registerReceiver(mReceiver, new IntentFilter(ACTION_SHOW));
    }

    private View mDownloadMusic;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
//		Log.e("oncreateview", this.toString());
        mRoot = inflater.inflate(R.layout.rdveuisdk_mymusic_layout, null);
        init(getActivity());
        return mRoot;
    }

    private void init(Context context) {

        findViewById(R.id.llTitle).setVisibility(View.GONE);
        mDownloadMusic = findViewById(R.id.download);
        mDownloadMusic.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
            }
        });
        mListView = (PinnedSectionListView) mRoot
                .findViewById(R.id.expandable_mymusic);

        context.registerReceiver(updateReceiver, new IntentFilter(
                ExtScanMediaDialog.INTENT_SIGHTSEEING_UPATE));
        mListView.setOnItemLongClickListener(longlistener);
        mListView.setOnItemClickListener(itemlistener);

        mMusicAdapter = new MyMusicAdapter(context);
        mListView.setAdapter(mMusicAdapter);
        mListView.setOnScrollListener(new OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                int mlast = view.getFirstVisiblePosition()
                        + view.getChildCount();
                if (mlast - 1 == mMusicAdapter.getCheckId()
                        || mlast - 2 == mMusicAdapter.getCheckId()) {
                    // if (scan_sd.getVisibility() == View.VISIBLE)
                    // scan_sd.setVisibility(View.GONE);
                } else {
                    // if (scan_sd.getVisibility() != View.VISIBLE)
                    // scan_sd.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {

            }
        });
        downLoadListener = new DownLoadListener();
        mReload = true;
        context.registerReceiver(downLoadListener, new IntentFilter(
                ACTION_DOWNLOAD));

    }

    private ArrayList<TreeNode> list = new ArrayList<MyMusicAdapter.TreeNode>();
    private String mlastMusic = "", mIsDownloaded = "";

    /**
     * 之前的mp3路径
     *
     * @param lastmp3
     */
    public void setLastMp3(String lastmp3) {
        mlastMusic = lastmp3;
    }

    private PinnedSectionListView mListView;
    private MyMusicAdapter mMusicAdapter;

    /**
     * 本地音乐
     */
    private DownLoadListener downLoadListener;

    private OnItemClickListener itemlistener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            mMusicAdapter.onItemClick(view, position, false);
        }
    };

    private IMusicApi iapi;

    public void setIMusic(IMusicApi music) {
        iapi = music;
        mReload = true;
    }

    private void getMusic() {
        mReload = false;
        mHandler.sendEmptyMessage(CLEAR); // 清空adapter
        list.clear();
        min = -1;
        max = -1;

        sectionPosition = 0;
        listPosition = 0;
        if (null != iapi) {
            ArrayList<WebMusicInfo> webs = iapi.getWebs();
            if (null != webs) {
                TreeNode mNode;
                for (int i = 0; i < webs.size(); i++) {
                    mNode = new TreeNode();
                    MyMusicInfo minfoInfo = new MyMusicInfo();
                    minfoInfo.setmInfo(webs.get(i));
                    mNode.childs = minfoInfo;
                    mNode.type = TreeNode.ITEM;
                    mNode.tag = 1;
                    mNode.sectionPosition = sectionPosition;
                    mNode.listPosition = listPosition++;
                    mMusicAdapter.onSectionAdded(mNode, sectionPosition);
                    list.add(mNode);
                }
            }
        }
        mHandler.sendEmptyMessage(NOTIFI);

    }

    /**
     * 执行删除已下载音乐
     *
     * @param wmi
     */
    private void onDeleteMusic(WebMusicInfo wmi) {
        mMusicAdapter.onPause();
        String path = wmi.getLocalPath();
        try {
            new File(path).delete();
        } catch (Exception e) {
            e.printStackTrace();
        }

        WebMusicData.getInstance().deleteItem(wmi.getId());
        ArrayList<TreeNode> temps = mMusicAdapter.getData();

        list.clear();

        int len = temps.size();
        mMusicAdapter.clear();

        // 判断是否是最后一个已下载 ，屏蔽已下载栏目

        boolean islast = (temps.get(2).type == TreeNode.SECTION);
        int index = 0;
        int j = (islast ? 2 : 0);

        int nsectionPosition = 0;
        for (int i = j; i < len; i++) {
            TreeNode mNode = temps.get(i);

            if (mNode.type == TreeNode.ITEM
                    && mNode.childs.getmInfo().getLocalPath().equals(path)) { // 删除的选项
            } else {
                mNode.listPosition = index;
                mMusicAdapter.onSectionAdded(mNode, nsectionPosition);
                list.add(mNode);
                index++;
                if (mNode.type == TreeNode.SECTION) {
                    nsectionPosition++;
                }
            }

        }

        mHandler.sendEmptyMessage(NOTIFI);
    }

    private OnItemLongClickListener longlistener = new OnItemLongClickListener() {

        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view,
                                       final int position, long id) {
            if (min != -1 && max != -1 && position >= min && position <= max) {
                showDialog(position);
            }
            return true;
        }

    };
    static final String ACTION_DOWNLOAD = "action_download_music";
    /**
     * 重新加载数据
     */
    private boolean mReload = false;
    private final String TAG = "mymusicfragment";

    private class DownLoadListener extends BroadcastReceiver {

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            mReload = true;
            // Log.d(TAG, mReload + "onReceive_DownLoadListener");
        }

    }


    @Override
    public void onStart() {
        super.onStart();
//		Log.e("onStart", this.toString());
        mMusicAdapter.onStart();
        mMusicAdapter.onStartReload();
    }

    private int sectionPosition = 0, listPosition = 0, min = -1, max = -1;

    private final int CLEAR = 1, NOTIFI = 2;
    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case CLEAR:
                    mMusicAdapter.clear();
                    break;
                case NOTIFI:
                    // Log.d(TAG, "notihadnler..." + list.size() + "...");
                    mMusicAdapter.setCanAutoPlay(false);
                    if (!TextUtils.isEmpty(mlastMusic)
                            && !new File(mlastMusic).exists()) {
                        mlastMusic = "";
                    }
                    mMusicAdapter.replace(list, mlastMusic);
                    mMusicAdapter.getView(mListView);

                    break;

                default:
                    break;
            }
        }

        ;
    };

    @Override
    public void onResume() {
        super.onResume();
        onReLoad();

    }

    public void onReLoad() {
        // Log.d(TAG, mReload + "...onReLoad......");
        if (mReload) {
            mReload = false;
            getMusic();
        }
    }

    @Override
    public void onPause() {
        if (null != mMusicAdapter) {
            mMusicAdapter.onPause();
        }
        super.onPause();

    }

    public void pausePlay() {
        if (null != mMusicAdapter) {
            mMusicAdapter.onPause();
        }
    }

    @Override
    public void onStop() {
//		Log.e("onstop", this.toString());
        mMusicAdapter.onStop();
        super.onStop();

    }

    @Override
    public void onDestroy() {
//		Log.e("onDestroy", this.toString());
        mMusicAdapter.onDestroy();
        super.onDestroy();
        getActivity().unregisterReceiver(downLoadListener);
        getActivity().unregisterReceiver(updateReceiver);
        getActivity().unregisterReceiver(mReceiver);
        mReceiver = null;

    }

    ;

    public void setCanAutoPlay(boolean canAutoPlay) {
        if (null != mMusicAdapter)
            mMusicAdapter.setCanAutoPlay(canAutoPlay);
    }

    public AudioMusicInfo getCheckMusicInfo() {
        if (null != mMusicAdapter) {
            return mMusicAdapter.getCheckedMusic();
        }
        return null;
    }

    /**
     * 接收浏览刷新音乐文件的广播
     */
    private BroadcastReceiver updateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getBooleanExtra(
                    ExtScanMediaDialog.INTENT_SIGHTSEEING_DATA, false)) {
                mReload = true;
                onReLoad();
            }
        }
    };

    private void showDialog(final int position) {

        TreeNode tdownloaded = mMusicAdapter.getItem(position);
        if (null != tdownloaded) {

            final WebMusicInfo wmi = tdownloaded.childs.getmInfo();
            if (null == wmi) {
                return;
            }

            SysAlertDialog.createAlertDialog(getActivity(), null,
                    getString(R.string.sure_delete),
                    getString(R.string.cancel),
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }, getString(R.string.sure),
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            onDeleteMusic(wmi);

                        }
                    }, false, null).show();
        }

    }

    public static final String ACTION_SHOW = "action_showscansd",
            BCANSHOW = "bcanshowvalue";

    private class MySdReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (TextUtils.equals(ACTION_SHOW, action)) {
                if (intent.getBooleanExtra(BCANSHOW, true)) {
                    // if (null != scan_sd)
                    // scan_sd.setVisibility(View.VISIBLE);
                } else {
                    // if (null != scan_sd)
                    // scan_sd.setVisibility(View.GONE);
                }

            }
        }

    }

}
