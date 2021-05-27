package com.rd.veuisdk.fragment;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import androidx.core.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.rd.lib.utils.CoreUtils;
import com.rd.veuisdk.R;
import com.rd.veuisdk.adapter.MyMusicAdapter;
import com.rd.veuisdk.adapter.MyMusicAdapter.TreeNode;
import com.rd.veuisdk.database.SDMusicData;
import com.rd.veuisdk.database.WebMusicData;
import com.rd.veuisdk.hb.views.MyRefreshLayout;
import com.rd.veuisdk.model.MyMusicInfo;
import com.rd.veuisdk.model.WebMusicInfo;
import com.rd.veuisdk.mvp.model.CloudFragmentModel;
import com.rd.veuisdk.utils.ModeDataUtils;
import com.rd.veuisdk.utils.SysAlertDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 音效 ->  单个分类的fragment
 */
public class CloudSoundFragment extends BaseV4Fragment {

    private ArrayList<TreeNode> list = new ArrayList<>();
    private String mLastMusic = "";
    private ListView mListView;
    private MyMusicAdapter mMusicAdapter;
    private MyRefreshLayout mRefreshLayout;

    //分类id 地址 当前页 最后一页
    private String id;
    private String mSoundUrl;
    @ModeDataUtils.ResourceType
    private String mMusicType = ModeDataUtils.TYPE_YUN_AUDIO_EFFECT;
    private int current_page;
    private int last_page;
    private ArrayList<WebMusicInfo> webInfos = new ArrayList<>();

    //开始就加载两页音乐
    private boolean isFirst = true;

    public CloudSoundFragment() {
        TAG = "CloudSoundFragment";
    }

    private CloudFragmentModel mCloudFragmentModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WebMusicData.getInstance().initilize(getActivity());
        SDMusicData.getInstance().initilize(getActivity());
        mCloudFragmentModel = new CloudFragmentModel(getActivity(), new CloudFragmentModel.CallBack<WebMusicInfo>() {
            @Override
            public void onSuccess(List<WebMusicInfo> list, int currentPage, int lastPage) {
                current_page = currentPage;
                last_page = lastPage;
                if (null != list) {
                    webInfos.addAll(list);
                }
                mHandler.sendEmptyMessage(UPDATA);
            }


            @Override
            public void onSuccess(List list) {
                mHandler.sendEmptyMessage(UPDATA);
            }

            @Override
            public void onFailed() {
                mHandler.sendEmptyMessage(UPDATA);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.rdveuisdk_mysound_layout, container, false);
        init(getActivity());
        return mRoot;
    }

    private void init(Context context) {
        mListView = $(R.id.expandable_mymusic);
        mListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mMusicAdapter.onItemClick(view, position, false);
            }
        });
        mMusicAdapter = new MyMusicAdapter(context);
        mListView.setAdapter(mMusicAdapter);
        initRefresh();
        startLoad();
    }

    /**
     * 初始化下拉刷新
     */
    private void initRefresh() {
        mRefreshLayout = $(R.id.swipe_sound);
        //加载
        mRefreshLayout.setOnLoadListener(new MyRefreshLayout.OnLoadListener() {
            @Override
            public void onLoad() {
                if (last_page > current_page) {
                    current_page++;
                    getSoundJson();
                } else {
                    mRefreshLayout.setLoading(false);
                    mRefreshLayout.noLoad();
                }
            }
        });
        //下拉
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                webInfos.clear();
                current_page = 1;
                isFirst = true;
                getSoundJson();
            }
        });
    }

    /**
     * 停止下拉刷新和上拉加载UI
     */
    private void stopRefreshUI() {
        // 加载完后调用该方法
        mRefreshLayout.setLoading(false);
        // 更新完后调用该方法结束刷新
        mRefreshLayout.setRefreshing(false);
        if (last_page > current_page) {
            mRefreshLayout.onUpPromat();
        }
    }

    /**
     * 设置url和当前对应的分类id
     *
     * @param id
     * @param url
     */
    public void setSound(String id, String url, @ModeDataUtils.ResourceType String type) {
        this.id = id;
        this.mSoundUrl = url;
        current_page = 1;
        mMusicType = type;
        last_page = 1;
    }

    private void startLoad() {
        current_page = 1;
        last_page = 1;
        getSoundJson();
    }

    /**
     * 获取音效
     */
    private void getSoundJson() {
        if (CoreUtils.checkNetworkInfo(getActivity()) != CoreUtils.UNCONNECTED) {
            mCloudFragmentModel.getWebData(mSoundUrl, mMusicType, id, current_page);
        }
    }

    /**
     * 刷新
     */
    private void getMusic() {
        mHandler.sendEmptyMessage(CLEAR); // 清空adapter
        list.clear();
        sectionPosition = 0;
        listPosition = 0;
        if (webInfos.size() > 0) {
            TreeNode node;
            for (int i = 0; i < webInfos.size(); i++) {
                node = new TreeNode();
                MyMusicInfo info = new MyMusicInfo();
                info.setmInfo(webInfos.get(i));
                node.childs = info;
                node.type = TreeNode.ITEM;
                node.tag = 1;
                node.sectionPosition = sectionPosition;
                node.listPosition = listPosition++;
                if (null != mMusicAdapter) {
                    mMusicAdapter.addTreeNode(node);
                }
                list.add(node);
            }
        }
        mHandler.sendEmptyMessage(NOTIFI);

    }

    private int sectionPosition = 0, listPosition = 0;

    private final int CLEAR = 1, NOTIFI = 2, UPDATA = 3;

    private Handler mHandler = new Handler() {
        @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case CLEAR:
                    if (!getActivity().isDestroyed()) {
                        mMusicAdapter.clear();
                    }
                    break;
                case NOTIFI:
                    if (!getActivity().isDestroyed()) {
                        mMusicAdapter.setCanAutoPlay(false);
                        if (!TextUtils.isEmpty(mLastMusic)
                                && !new File(mLastMusic).exists()) {
                            mLastMusic = "";
                        }
                        mMusicAdapter.replace(list, mLastMusic);
                        mMusicAdapter.setListView(mListView);
                        if (mIsLoad && list.size() > 0) {
                            if (mListener != null) {
                                mListener.onComplete();
                            } else {
                                SysAlertDialog.cancelLoadingDialog();
                            }
                            mIsLoad = false;
                        }
                    }
                    break;
                case UPDATA:
                    if (!getActivity().isDestroyed()) {
                        if (isFirst) {
                            if (last_page > current_page) {
                                current_page++;
                                getSoundJson();
                            } else {
                                getMusic();
                                mRefreshLayout.setLoading(false);
                                mRefreshLayout.noLoad();
                            }
                            isFirst = false;
                        } else {
                            getMusic();
                            stopRefreshUI();
                        }
                    }
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
    public void onStart() {
        super.onStart();
        mMusicAdapter.onStart();
        mMusicAdapter.onStartReload();
        getMusic();
    }

    @Override
    public void onStop() {
        mMusicAdapter.onStop();
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (null != mCloudFragmentModel) {
            mCloudFragmentModel.recycle();
        }
    }

    @Override
    public void onDestroy() {
        if (null != mCloudFragmentModel) {
            mCloudFragmentModel.recycle();
        }
        mHandler.removeMessages(CLEAR);
        mHandler.removeMessages(NOTIFI);
        mHandler.removeMessages(UPDATA);
        mMusicAdapter.onDestroy();
        mMusicAdapter = null;
        super.onDestroy();

    }

    private LoadingListener mListener;
    private boolean mIsLoad = true;

    public void setListener(LoadingListener listener) {
        this.mListener = listener;
    }

    public interface LoadingListener {
        /**
         * 加载音乐完成
         */
        void onComplete();
    }

}
