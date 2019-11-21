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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.rd.lib.utils.ThreadPoolUtils;
import com.rd.veuisdk.R;
import com.rd.veuisdk.adapter.MyMusicAdapter;
import com.rd.veuisdk.adapter.MyMusicAdapter.TreeNode;
import com.rd.veuisdk.database.SDMusicData;
import com.rd.veuisdk.database.WebMusicData;
import com.rd.veuisdk.model.MusicItem;
import com.rd.veuisdk.model.MusicItems;
import com.rd.veuisdk.model.MyMusicInfo;
import com.rd.veuisdk.model.WebMusicInfo;
import com.rd.veuisdk.utils.CheckSDSize;
import com.rd.veuisdk.utils.ExtScanMediaDialog;
import com.rd.veuisdk.utils.ExtScanMediaDialog.onScanMusicClickInterface;
import com.rd.veuisdk.utils.SysAlertDialog;

import java.io.File;
import java.util.ArrayList;

/**
 * 本地音乐
 */
public class LocalMusicFragment extends BaseV4Fragment {

    private MySdReceiver mReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TAG = "LocalMusicFragment";
        mLocalMusic = getString(R.string.local_music);
        SDMusicData.getInstance().initilize(getActivity());
        Context context = getActivity();
        mAllLocalMusicItems = new MusicItems(context);
        mReceiver = new MySdReceiver();
        getActivity().registerReceiver(mReceiver, new IntentFilter(ACTION_SHOW));
    }

    private View mScanSd;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.rdveuisdk_localmusic_layout, container, false);
        init(getActivity());
        return mRoot;
    }

    private void init(Context context) {
        mScanSd = $(R.id.scan_sd);
        mScanSd.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                showScanType();
            }
        });
        mListView = $(R.id.expandable_mymusic);
        context.registerReceiver(updateReceiver, new IntentFilter(
                ExtScanMediaDialog.INTENT_SIGHTSEEING_UPATE));
        mListView.setOnItemLongClickListener(mOnLongListener);
        mListView.setOnItemClickListener(itemlistener);

        mMusicAdapter = new MyMusicAdapter(context);
        mListView.setAdapter(mMusicAdapter);
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


    private ListView mListView;
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
                if (null != mMusicAdapter) {
                    mMusicAdapter.addTreeNode(t3);
                }
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

        ;
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

    @Override
    public void onPause() {
        if (null != mMusicAdapter)
            mMusicAdapter.onPause();
        super.onPause();

    }


    @Override
    public void onStop() {
        mMusicAdapter.onStop();
        super.onStop();

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //强制取消扫描
        if (null != mAllLocalMusicItems) {
            mAllLocalMusicItems.setIsCancel(true);
        }
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
            if (null != mMusicAdapter) {
                mMusicAdapter.addTreeNode(mNode);
            }
            list.add(mNode);

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
                } else {
                }

            }
        }

    }

}
