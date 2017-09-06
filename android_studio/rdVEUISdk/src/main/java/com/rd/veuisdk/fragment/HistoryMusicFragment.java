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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.rd.lib.utils.ThreadPoolUtils;
import com.rd.veuisdk.R;
import com.rd.veuisdk.adapter.MyMusicAdapter;
import com.rd.veuisdk.adapter.MyMusicAdapter.TreeNode;
import com.rd.veuisdk.database.HistoryMusicData;
import com.rd.veuisdk.database.SDMusicData;
import com.rd.veuisdk.database.WebMusicData;
import com.rd.veuisdk.hb.views.PinnedSectionListView;
import com.rd.veuisdk.model.AudioMusicInfo;
import com.rd.veuisdk.model.IMusic;
import com.rd.veuisdk.model.MusicItems;
import com.rd.veuisdk.model.MyMusicInfo;
import com.rd.veuisdk.model.WebMusicInfo;
import com.rd.veuisdk.utils.ExtScanMediaDialog;
import com.rd.veuisdk.utils.SysAlertDialog;

import java.io.File;
import java.util.ArrayList;

/**
 * 我的配乐
 *
 * @author JIAN
 */
public class HistoryMusicFragment extends BaseV4Fragment {

    private MySdReceiver mReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDMusicData.getInstance().initilize(getActivity());
        mPageName = getString(R.string.mymusic);
        mAllKXMusicItems = new MusicItems();
        Context context = getActivity();
        mAllKXMusicItems.loadAssetsMusic(context);
        mAllLocalMusicItems = new MusicItems(context);
        mReceiver = new MySdReceiver();
        getActivity().registerReceiver(mReceiver, new IntentFilter(ACTION_SHOW));
    }

    private View mDeleteItem;
    private CheckBox mCbSelectAll;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (null == mRoot) {
            mRoot = inflater.inflate(R.layout.rdveuisdk_historymusic_layout, null);
        }
        init(getActivity());
        return mRoot;
    }

    private void init(Context context) {

        mDeleteItem = findViewById(R.id.delete_history);
        mDeleteItem.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                deleteMusic();
            }
        });

        mCbSelectAll = (CheckBox) findViewById(R.id.cbSelectAll);
        mCbSelectAll
                .setOnCheckedChangeListener(new OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(CompoundButton buttonView,
                                                 boolean isChecked) {
                        selectAll(isChecked);
                    }
                });

        mListView = (PinnedSectionListView) findViewById(R.id.expandable_mymusic);

        context.registerReceiver(mUpdateReceiver, new IntentFilter(
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

                } else {

                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {

            }
        });

        reLoad = true;

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

    public void selectAll(boolean isChecked) {
        mMusicAdapter.setSelectAll(isChecked);
    }

    private PinnedSectionListView mListView;
    private MyMusicAdapter mMusicAdapter;
    /**
     * 快秀音乐
     */
    private MusicItems mAllKXMusicItems;
    /**
     * 本地音乐
     */
    private MusicItems mAllLocalMusicItems;

    private OnItemClickListener itemlistener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            mMusicAdapter.onItemClick(view, position, false);

        }
    };

    public void deleteMusic() {
        ArrayList<IMusic> temp = HistoryMusicData.getInstance().queryAll();
        ArrayList<TreeNode> arrTreeNode = mMusicAdapter.getData();
        for (int n = 1; n < arrTreeNode.size(); n++) {
            TreeNode tn = arrTreeNode.get(n);
            if (tn.selected) {
                long id = temp.get(n - 1).getId();
                HistoryMusicData.getInstance().deleteItem(id);
            }
        }
        reLoad = true;
        onReLoad();
    }

    private void getMusic() {
        mHandler.sendEmptyMessage(CLEAR); // 清空adapter
        list.clear();
        min = -1;
        max = -1;
        ThreadPoolUtils.executeEx(new Runnable() {
            public void run() {
                mSectionPosition = 3;
                mListPosition = 0;

                TreeNode t3 = new TreeNode();
                t3.childs = null;
                t3.type = TreeNode.SECTION;
                t3.text = getString(R.string.history_recently);
                t3.tag = 3;
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
    static final String ACTION_DOWNLOAD = "action_download";
    /**
     * 重新加载数据
     */
    private boolean reLoad = false;
    private final String TAG = "mymusicfragment";

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
        // Log.d(TAG, reLoad + "...onReLoad......");
        if (reLoad) {
            reLoad = false;
            getMusic();
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
        mMusicAdapter.clear();
        mMusicAdapter.onDestroy();
        super.onDestroy();
        getActivity().unregisterReceiver(mUpdateReceiver);
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
    private BroadcastReceiver mUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getBooleanExtra(
                    ExtScanMediaDialog.INTENT_SIGHTSEEING_DATA, false)) {
                reLoad = true;
                onReLoad();
            }
        }
    };

    /**
     * 异步加载本地音乐
     */
    private void scanLoadMusic(final int scanType,
                               final ArrayList<String> scanDirectory) {
        ArrayList<IMusic> temp = HistoryMusicData.getInstance().queryAll();

        int len = temp.size();
        MyMusicInfo minfoInfo;
        TreeNode mNode;
        IMusic imusic;

        WebMusicInfo mWebMusicInfo;
        for (int i = 0; i < len; i++) {
            imusic = temp.get(i);
            minfoInfo = new MyMusicInfo();
            mWebMusicInfo = new WebMusicInfo();
            mWebMusicInfo.setLocalPath(imusic.getPath());
            mWebMusicInfo.setMusicName(imusic.getName());
            mWebMusicInfo.setDuration(imusic.getDuration());
            minfoInfo.setmInfo(mWebMusicInfo);

            mNode = new TreeNode();
            mNode.childs = minfoInfo;
            mNode.tag = 3;
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

    public static final String ACTION_SHOW = "action_show",
            BCANSHOW = "bcanshow";

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
