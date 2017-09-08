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
import android.widget.Toast;

import com.rd.lib.utils.ThreadPoolUtils;
import com.rd.net.JSONObjectEx;
import com.rd.veuisdk.R;
import com.rd.veuisdk.adapter.MyMusicAdapter;
import com.rd.veuisdk.adapter.MyMusicAdapter.TreeNode;
import com.rd.veuisdk.database.SDMusicData;
import com.rd.veuisdk.database.WebMusicData;
import com.rd.veuisdk.hb.views.PinnedSectionListView;
import com.rd.veuisdk.model.AudioMusicInfo;
import com.rd.veuisdk.model.MusicItem;
import com.rd.veuisdk.model.MusicItems;
import com.rd.veuisdk.model.MyMusicInfo;
import com.rd.veuisdk.model.WebMusicInfo;
import com.rd.veuisdk.utils.CheckSDSize;
import com.rd.veuisdk.utils.ExtScanMediaDialog;
import com.rd.veuisdk.utils.ExtScanMediaDialog.onScanMusicClickInterface;
import com.rd.veuisdk.utils.MusicUtils;
import com.rd.veuisdk.utils.SysAlertDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

/**
 * 我的配乐
 *
 * @author JIAN
 */
public class LocalMusicFragment extends BaseV4Fragment {

    private MySdReceiver mReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLocalMusic = getString(R.string.local_music);
        SDMusicData.getInstance().initilize(getActivity());
        mPageName = getString(R.string.mymusic);
        Context context = getActivity();
        mAllLocalMusicItems = new MusicItems(context);
        mReceiver = new MySdReceiver();
        getActivity().registerReceiver(mReceiver, new IntentFilter(ACTION_SHOW));
    }

    private View mScanSd;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (null == mRoot) {
            mRoot = inflater.inflate(R.layout.rdveuisdk_localmusic_layout, null);
        }
        init(getActivity());
        return mRoot;
    }

    private void init(Context context) {
        mScanSd = findViewById(R.id.scan_sd);
        mScanSd.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                showScanType();
            }
        });

        mListView = (PinnedSectionListView) mRoot
                .findViewById(R.id.expandable_mymusic);

        context.registerReceiver(updateReceiver, new IntentFilter(
                ExtScanMediaDialog.INTENT_SIGHTSEEING_UPATE));
        mListView.setOnItemLongClickListener(mOnLongListener);
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
                    // if (mScanSd.getVisibility() == View.VISIBLE)
                    // mScanSd.setVisibility(View.GONE);
                } else {
                    // if (mScanSd.getVisibility() != View.VISIBLE)
                    // mScanSd.setVisibility(View.VISIBLE);
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

    /**
     * 显示扫描样式
     */
    public void showScanType() {
        if (!CheckSDSize.ExistSDCard()) {
            SysAlertDialog.showAutoHideDialog(getActivity(), "",
                    getString(R.string.sd_umount), Toast.LENGTH_LONG);
            return;
        }
        SysAlertDialog.showListviewAlertMenu(getActivity(), "", getResources()
                        .getStringArray(R.array.scan_sd_menu),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            ExtScanMediaDialog musicDialog = new ExtScanMediaDialog(
                                    getActivity());
                            musicDialog
                                    .setonScanMusicClickInterface(new onScanMusicClickInterface() {

                                        @Override
                                        public void cancel() {
                                            mAllLocalMusicItems
                                                    .setIsCancel(true);
                                        }

                                        @Override
                                        public void accomplish() {
                                            // 让界面回到本地音乐
                                            mReload = true;
                                            onReLoad();

                                        }
                                    });
                            // 注册显示音乐文件的回调接口
                            mAllLocalMusicItems
                                    .setOnShowScanFileInterface(musicDialog);
                            musicDialog.show();
                            ThreadPoolUtils.executeEx(new Runnable() {
                                public void run() {
                                    // 快速扫描本地文件夹
                                    scanLoadMusic(2, null);
                                }
                            });
                        } else {
                            startActivity(new Intent(
                                    getActivity(),
                                    com.rd.veuisdk.SightseeingFileActivity.class));
                        }
                    }
                });
    }

    private ArrayList<TreeNode> list = new ArrayList<MyMusicAdapter.TreeNode>();
    private String mlastMusic = "";

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
    private MusicItems mAllLocalMusicItems;
    private DownLoadListener downLoadListener;

    private OnItemClickListener itemlistener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            mMusicAdapter.onItemClick(view, position, false);

        }
    };

    private String mLocalMusic = null;

    private void getMusic() {
        mHandler.sendEmptyMessage(CLEAR); // 清空adapter
        list.clear();
        min = -1;
        max = -1;
        ThreadPoolUtils.executeEx(new Runnable() {
            public void run() {
                mSectionPosition = 0;
                mListPosition = 0;

                mSectionPosition++;

                mSectionPosition++;
                TreeNode t3 = new TreeNode();
                t3.childs = null;
                t3.type = TreeNode.SECTION;
                t3.text = mLocalMusic;
                t3.tag = 2;
                t3.sectionPosition = mSectionPosition;
                t3.listPosition = mListPosition++;
                mMusicAdapter.onSectionAdded(t3, mSectionPosition);
                list.add(t3);
                scanLoadMusic(1, null);
                mHandler.sendEmptyMessage(NOTIFI);
            }
        });
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

    private OnItemLongClickListener mOnLongListener = new OnItemLongClickListener() {

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
        mMusicAdapter.onStart();
    }

    private int mSectionPosition = 0, mListPosition = 0, min = -1, max = -1;

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

    /**
     * 获取下载的音乐的信息
     */
    private void getDownloadInfo() {
        ArrayList<WebMusicInfo> download = WebMusicData.getInstance()
                .queryAll();
        int msize = download.size();

        // Log.e("downloadinfo....", msize + "...msize");

        if (msize > 0) {
            TreeNode t1 = new TreeNode();
            t1.type = TreeNode.SECTION;
            t1.childs = null;
            t1.text = getString(R.string.downloaded);
            t1.tag = 0;
            t1.sectionPosition = mSectionPosition;
            t1.listPosition = mListPosition++;
            mMusicAdapter.onSectionAdded(t1, mSectionPosition);
            list.add(t1);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < msize; i++) {
                sb.append(download.get(i).getId() + ",");
            }
            String mids = sb.substring(0, sb.lastIndexOf(",")).toString()
                    .trim();
            String content = MusicUtils.musicdowncount(mids);

            if (!TextUtils.isEmpty(content)) {
                try {
                    JSONObjectEx jobjEx = new JSONObjectEx(content);
                    if (1 == jobjEx.getInt("result")) {
                        JSONArray array = jobjEx.getJSONArray("data");
                        int len = array.length();
                        min = mListPosition;
                        max = -1;
                        for (int i = 0; i < len; i++) {
                            JSONObject object = array.getJSONObject(i);
                            MyMusicInfo itemInfo = new MyMusicInfo();
                            WebMusicInfo ninfo = download.get(i);
                            ninfo.setArtName(object.optString("sid",
                                    ninfo.getArtName()));
                            itemInfo.setmInfo(ninfo);
                            itemInfo.setDowntimes(object.getString("downcount"));
                            TreeNode mNode = new TreeNode();
                            mNode.type = TreeNode.ITEM;
                            mNode.childs = itemInfo;
                            mNode.tag = 0;
                            mNode.sectionPosition = mSectionPosition;
                            mNode.listPosition = mListPosition++;
                            mMusicAdapter.onSectionAdded(mNode, mSectionPosition);
                            list.add(mNode);
                        }
                        max = mListPosition - 1;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } else {
                WebMusicInfo ninfo;
                MyMusicInfo itemInfo;
                TreeNode mNode;
                for (int i = 0; i < msize; i++) {
                    itemInfo = new MyMusicInfo();
                    ninfo = download.get(i);
                    itemInfo.setmInfo(ninfo);
                    mNode = new TreeNode();
                    mNode.type = TreeNode.ITEM;
                    mNode.childs = itemInfo;
                    mNode.tag = 0;
                    mNode.sectionPosition = mSectionPosition;
                    mNode.listPosition = mListPosition++;
                    mMusicAdapter.onSectionAdded(mNode, mSectionPosition);
                    list.add(mNode);
                }
            }
        }
    }

    @Override
    public void onPause() {
        if (null != mMusicAdapter)
            mMusicAdapter.onPause();
        super.onPause();

    }

    public void pausePlay() {
        if (null != mMusicAdapter) {
            mMusicAdapter.onPause();
        }
    }

    @Override
    public void onStop() {
        mMusicAdapter.onStop();
        super.onStop();

    }

    @Override
    public void onDestroy() {
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

    /**
     * 异步加载本地音乐
     */
    private void scanLoadMusic(final int scanType,
                               final ArrayList<String> scanDirectory) {
        mAllLocalMusicItems.loadMusicItems(scanType, scanDirectory);
        MyMusicInfo minfoInfo;
        MusicItem mi;
        File file;
        TreeNode mNode;
        int len = mAllLocalMusicItems.size();
        WebMusicInfo mWebMusicInfo;
        for (int i = 0; i < len; i++) {
            mi = mAllLocalMusicItems.get(i);
            file = new File(mi.getPath());
            if (!file.exists()) {
                file.delete();
                mAllLocalMusicItems.remove(i);
                continue;
            }

            minfoInfo = new MyMusicInfo();
            mWebMusicInfo = new WebMusicInfo();
            mWebMusicInfo.setLocalPath(mi.getPath());
            mWebMusicInfo.setMusicName(mi.getTitle());
            mWebMusicInfo.setArtName(mi.getArt());
            mWebMusicInfo.setDuration(mi.getDuration());
            minfoInfo.setmInfo(mWebMusicInfo);

            mNode = new TreeNode();
            mNode.childs = minfoInfo;
            mNode.tag = 2;
            mNode.type = TreeNode.ITEM;
            mNode.sectionPosition = mSectionPosition;
            mNode.listPosition = mListPosition++;
            mMusicAdapter.onSectionAdded(mNode, mSectionPosition);
            list.add(mNode);

            // Log.d(TAG, "scanLoadMusic-" +
            // minfoInfo.getmInfo().getLocalPath());

        }

    }

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

    public static final String ACTION_SHOW = "action_show_scansd",
            BCANSHOW = "bcanshow_scansd_value";

    private class MySdReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (TextUtils.equals(ACTION_SHOW, action)) {
                if (intent.getBooleanExtra(BCANSHOW, true)) {
                    // if (null != mScanSd)
                    // mScanSd.setVisibility(View.VISIBLE);
                } else {
                    // if (null != mScanSd)
                    // mScanSd.setVisibility(View.GONE);
                }

            }
        }

    }

}
