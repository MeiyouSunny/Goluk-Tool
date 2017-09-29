package com.rd.veuisdk.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.rd.veuisdk.R;
import com.rd.veuisdk.adapter.MyMusicAdapter;
import com.rd.veuisdk.adapter.MyMusicAdapter.TreeNode;
import com.rd.veuisdk.database.SDMusicData;
import com.rd.veuisdk.database.WebMusicData;
import com.rd.veuisdk.hb.views.PinnedSectionListView;
import com.rd.veuisdk.model.IMusicApi;
import com.rd.veuisdk.model.MyMusicInfo;
import com.rd.veuisdk.model.WebMusicInfo;

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
        TAG = "CloudMusicFragment";
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIsDownloaded = getString(R.string.downloaded);
        WebMusicData.getInstance().initilize(getActivity());
        SDMusicData.getInstance().initilize(getActivity());
        mPageName = getString(R.string.mymusic);
    }

    private View mDownloadMusic;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.rdveuisdk_mymusic_layout, null);
        init(getActivity());
        return mRoot;
    }

    private void init(Context context) {

        findViewById(R.id.llTitle).setVisibility(View.GONE);
        mDownloadMusic = findViewById(R.id.download);
        mDownloadMusic.setVisibility(View.GONE);
        mListView = (PinnedSectionListView) mRoot
                .findViewById(R.id.expandable_mymusic);

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

    }

    private ArrayList<TreeNode> list = new ArrayList<MyMusicAdapter.TreeNode>();
    private String mlastMusic = "", mIsDownloaded = "";


    private PinnedSectionListView mListView;
    private MyMusicAdapter mMusicAdapter;

    /**
     * 本地音乐
     */

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
    }

    private void getMusic() {
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


    @Override
    public void onStart() {
        super.onStart();
        mMusicAdapter.onStart();
        mMusicAdapter.onStartReload();
        getMusic();
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
        if (null != mMusicAdapter) {
            mMusicAdapter.onResume();
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

    }


}
