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
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

import com.rd.lib.utils.ThreadPoolUtils;
import com.rd.net.JSONObjectEx;
import com.rd.veuisdk.R;
import com.rd.veuisdk.adapter.MyMusicAdapter;
import com.rd.veuisdk.adapter.MyMusicAdapter.TreeNode;
import com.rd.veuisdk.database.SDMusicData;
import com.rd.veuisdk.database.WebMusicData;
import com.rd.veuisdk.model.MusicItem;
import com.rd.veuisdk.model.MusicItems;
import com.rd.veuisdk.model.MyMusicInfo;
import com.rd.veuisdk.model.WebMusicInfo;
import com.rd.veuisdk.utils.ExtScanMediaDialog;
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
public class MyMusicFragment extends BaseV4Fragment {
    private MySdReceiver mReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mInternalMusic = getString(R.string.internal_music);
        mIsDownloaded = getString(R.string.downloaded);
        SDMusicData.getInstance().initilize(getActivity());
        mAllKxMusicItems = new MusicItems();
        Context context = getActivity();
        mAllKxMusicItems.loadAssetsMusic(context);
        mReceiver = new MySdReceiver();
        getActivity().registerReceiver(mReceiver, new IntentFilter(ACTION_SHOW));
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.rdveuisdk_mymusic_layout, container, false);
        init(getActivity());
        return mRoot;
    }

    private void init(Context context) {
        mListView = $(R.id.expandable_mymusic);
        context.registerReceiver(updateReceiver, new IntentFilter(
                ExtScanMediaDialog.INTENT_SIGHTSEEING_UPATE));
        mListView.setOnItemLongClickListener(mOnItemLongClickListener);
        mListView.setOnItemClickListener(mOnItemClickListener);
        mMusicAdapter = new MyMusicAdapter(context);
        mListView.setAdapter(mMusicAdapter);
        mDownloadListener = new DownLoadListener();
        mReload = true;
        context.registerReceiver(mDownloadListener, new IntentFilter(
                ACTION_DOWNLOAD));

    }

    private ArrayList<TreeNode> list = new ArrayList<MyMusicAdapter.TreeNode>();
    private String mlastMusic = "", mIsDownloaded = "";


    private ListView mListView;
    private MyMusicAdapter mMusicAdapter;
    /**
     * 快秀音乐
     */
    private MusicItems mAllKxMusicItems;
    private DownLoadListener mDownloadListener;

    private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            mMusicAdapter.onItemClick(view, position, false);
        }
    };

    private String mInternalMusic = null;

    private void getMusic() {
        mHandler.sendEmptyMessage(CLEAR); // 清空adapter
        list.clear();
        min = -1;
        max = -1;
        ThreadPoolUtils.executeEx(new Runnable() {
            public void run() {
                mSectionPosition = 0;
                mListPosition = 0;
                getDownloadInfo();

                mSectionPosition++;

                TreeNode t2 = new TreeNode();

                t2.childs = null;
                t2.type = TreeNode.SECTION;
                t2.text = mInternalMusic;
                t2.tag = 1;
                t2.sectionPosition = mSectionPosition;
                t2.listPosition = mListPosition++;
                if (null != mMusicAdapter) {
                    mMusicAdapter.addTreeNode(t2);
                }
                list.add(t2);
                WebMusicInfo mWebMusicInfo;
                MyMusicInfo minfoInfo;
                TreeNode mNode;
                for (MusicItem musicExternal : mAllKxMusicItems) {
                    minfoInfo = new MyMusicInfo();

                    mWebMusicInfo = new WebMusicInfo();
                    mWebMusicInfo.setLocalPath(musicExternal.getPath());
                    mWebMusicInfo.setMusicName(musicExternal.getTitle());
                    mWebMusicInfo.setArtName(musicExternal.getAssetsName());
                    mWebMusicInfo.setDuration(musicExternal.getDuration());
                    minfoInfo.setmInfo(mWebMusicInfo);

                    mNode = new TreeNode();
                    mNode.childs = minfoInfo;
                    mNode.type = TreeNode.ITEM;
                    mNode.tag = 1;
                    mNode.sectionPosition = mSectionPosition;
                    mNode.listPosition = mListPosition++;
                    if (null != mMusicAdapter) {
                        mMusicAdapter.addTreeNode(mNode);
                    }
                    list.add(mNode);

                }
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

        for (int i = j; i < len; i++) {
            TreeNode mNode = temps.get(i);

            if (mNode.type == TreeNode.ITEM
                    && mNode.childs.getmInfo().getLocalPath().equals(path)) { // 删除的选项
            } else {
                mNode.listPosition = index;
                if (null != mMusicAdapter) {
                    mMusicAdapter.addTreeNode(mNode);
                }
                list.add(mNode);
                index++;
            }

        }

        mHandler.sendEmptyMessage(NOTIFI);
    }

    private OnItemLongClickListener mOnItemLongClickListener = new OnItemLongClickListener() {

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

    private class DownLoadListener extends BroadcastReceiver {

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            mReload = true;
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
                    mMusicAdapter.setCanAutoPlay(false);
                    if (!TextUtils.isEmpty(mlastMusic)
                            && !new File(mlastMusic).exists()) {
                        mlastMusic = "";
                    }
                    mMusicAdapter.replace(list, mlastMusic);
                    mMusicAdapter.setListView(mListView);

                    break;

                default:
                    break;
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        onReLoad();
    }

    public void onReLoad() {
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

        if (msize > 0) {
            TreeNode t1 = new TreeNode();
            t1.type = TreeNode.SECTION;
            t1.childs = null;
            t1.text = mIsDownloaded;
            t1.tag = 0;
            t1.sectionPosition = mSectionPosition;
            t1.listPosition = mListPosition++;
            mMusicAdapter.addTreeNode(t1);
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
                            mMusicAdapter.addTreeNode(mNode);
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
                    mMusicAdapter.addTreeNode(mNode);
                    list.add(mNode);
                }
            }
        }
    }

    @Override
    public void onPause() {
        if (null != mMusicAdapter) {
            mMusicAdapter.onPause();
        }
        super.onPause();

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
        getActivity().unregisterReceiver(mDownloadListener);
        getActivity().unregisterReceiver(updateReceiver);
        getActivity().unregisterReceiver(mReceiver);
        mReceiver = null;

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
                } else {
                }

            }
        }

    }

}
