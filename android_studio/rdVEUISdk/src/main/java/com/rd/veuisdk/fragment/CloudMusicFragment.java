package com.rd.veuisdk.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.rd.veuisdk.R;
import com.rd.veuisdk.adapter.MyMusicAdapter;
import com.rd.veuisdk.adapter.MyMusicAdapter.TreeNode;
import com.rd.veuisdk.database.SDMusicData;
import com.rd.veuisdk.database.WebMusicData;
import com.rd.veuisdk.model.IMusicApi;
import com.rd.veuisdk.model.MyMusicInfo;
import com.rd.veuisdk.model.WebMusicInfo;
import com.rd.veuisdk.utils.SysAlertDialog;

import java.io.File;
import java.util.ArrayList;

/**
 * 云音乐->单个分类的fragment
 *
 */
public class CloudMusicFragment extends BaseV4Fragment {
    public CloudMusicFragment() {
        TAG = "CloudMusicFragment";
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WebMusicData.getInstance().initilize(getActivity());
        SDMusicData.getInstance().initilize(getActivity());
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.rdveuisdk_mymusic_layout, container, false);
        init(getActivity());
        return mRoot;
    }

    private void init(Context context) {
        mListView = $(R.id.expandable_mymusic);
        mListView.setOnItemClickListener(itemlistener);
        mMusicAdapter = new MyMusicAdapter(context);
        mListView.setAdapter(mMusicAdapter);
    }

    private ArrayList<TreeNode> list = new ArrayList<MyMusicAdapter.TreeNode>();
    private String mLastMusic = "";


    private ListView mListView;
    private MyMusicAdapter mMusicAdapter;

    /**
     * 本地音乐
     */

    private OnItemClickListener itemlistener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            mMusicAdapter.onItemClick(view, position, false);
        }
    };

    private IMusicApi mApi;

    public void setIMusic(IMusicApi music) {
        mApi = music;
    }

    private void getMusic() {
        mHandler.sendEmptyMessage(CLEAR); // 清空adapter
        list.clear();
        sectionPosition = 0;
        listPosition = 0;
        if (null != mApi) {
            ArrayList<WebMusicInfo> webs = mApi.getWebs();
            if (null != webs) {
                TreeNode node;
                for (int i = 0; i < webs.size(); i++) {
                    node = new TreeNode();
                    MyMusicInfo info = new MyMusicInfo();
                    info.setmInfo(webs.get(i));
                    node.childs = info;
                    node.type = TreeNode.ITEM;
                    node.tag = 1;
                    node.sectionPosition = sectionPosition;
                    node.listPosition = listPosition++;
                    if(null!=mMusicAdapter) {
                        mMusicAdapter.addTreeNode(node);
                    }
                    list.add(node);
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

    private int sectionPosition = 0, listPosition = 0;

    private final int CLEAR = 1, NOTIFI = 2;
    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case CLEAR:
                    mMusicAdapter.clear();
                    break;
                case NOTIFI:
                    mMusicAdapter.setCanAutoPlay(false);
                    if (!TextUtils.isEmpty(mLastMusic)  && !new File(mLastMusic).exists()) {
                        mLastMusic = "";
                    }
                    mMusicAdapter.replace(list, mLastMusic);
                    mMusicAdapter.setListView(mListView);
                    SysAlertDialog.cancelLoadingDialog();
                    break;

                default:
                    break;
            }
        }
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
