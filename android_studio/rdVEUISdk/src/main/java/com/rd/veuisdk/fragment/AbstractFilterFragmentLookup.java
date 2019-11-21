package com.rd.veuisdk.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rd.downfile.utils.DownLoadUtils;
import com.rd.downfile.utils.IDownFileListener;
import com.rd.http.MD5;
import com.rd.lib.utils.CoreUtils;
import com.rd.lib.utils.ThreadPoolUtils;
import com.rd.veuisdk.R;
import com.rd.veuisdk.database.FilterData;
import com.rd.veuisdk.model.WebFilterInfo;
import com.rd.veuisdk.utils.FileUtils;
import com.rd.veuisdk.utils.ModeDataUtils;
import com.rd.veuisdk.utils.PathUtils;
import com.rd.veuisdk.utils.SysAlertDialog;

import java.util.ArrayList;
import java.util.List;

/**
 * 网络滤镜 (布局支持自定义)
 */
public abstract class AbstractFilterFragmentLookup extends FilterFragmentLookupBase {

    private String mUrl;


    protected static final String PARAM_URL = "param_url";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPageName = getString(R.string.filter);
        mUrl = getArguments().getString(PARAM_URL);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        initHandler();
        mHanlder.removeMessages(MSG_ASSET_EXPORT_START);
        mHanlder.obtainMessage(MSG_ASSET_EXPORT_START).sendToTarget();
        getWebFilter();
        return mRoot;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (null != mDownloading) {
            DownLoadUtils.forceCancelAll();
            mDownloading.clear();
        }
        if (null != mHanlder) {
            mHanlder.removeMessages(MSG_ASSET_EXPORT_START);
            mHanlder.removeMessages(MSG_WEB_PREPARED);
            mHanlder = null;
        }

        if (null != mAdapter) {
            mAdapter.setOnItemClickListener(null);
            mAdapter = null;
        }
        mRoot = null;

    }

    @Override
    public void recycle() {
        super.recycle();
        if (null != mlist) {
            mlist.clear();
        }
    }

    private int lastItemId = 0;


    @Override
    public void onSelectedImp(int nItemId) {
        mLastPageIndex = nItemId;
        if (nItemId >= 1) {
            WebFilterInfo info = mAdapter.getItem(nItemId);
            if (lastItemId != nItemId) {
                if (FileUtils.isExist(info.getLocalPath())) {//已下载的lookup滤镜,直接使用
                    switchFliter(nItemId);
                    lastItemId = nItemId;
                    mAdapter.onItemChecked(nItemId);
                } else {  //此滤镜未注册，下载该filter
                    if (CoreUtils.checkNetworkInfo(mContext) == CoreUtils.UNCONNECTED) {
                        mAdapter.onItemChecked(lastItemId);
                        onToast(getString(R.string.please_open_wifi));
                    } else {
                        // 下载
                        lastItemId = 0;
                        down(nItemId, info);
                    }
                }
            }


        } else {
            lastItemId = nItemId;
            switchFliter(nItemId);
            mAdapter.onItemChecked(lastItemId);
        }

        if (!mIFilterHandler.isPlaying()) {
            mIFilterHandler.start();
        }

    }


    private SparseArray<DownLoadUtils> mDownloading = null;


    /**
     * filter的sd 完整路径
     *
     * @param info
     * @return
     */
    private String getFilterFilePath(WebFilterInfo info) {
        return PathUtils.getRdFilterPath() + "/" + MD5.getMD5(info.getUrl());
    }

    /**
     * 下载滤镜
     */
    private void down(int itemId, final WebFilterInfo info) {
        if (null == mDownloading) {
            mDownloading = new SparseArray<>();
        }
        if (mDownloading.size() <= 0 && null == mDownloading.get(itemId)) {
            /**
             * 支持指定下载文件的存放位置
             */
            final DownLoadUtils download = new DownLoadUtils(getContext(), itemId,
                    info.getUrl(), getFilterFilePath(info));
            download.DownFile(new IDownFileListener() {

                @Override
                public void onProgress(long mid, int progress) {
                    if (isRunning && null != mHanlder) {
                        if (null != mAdapter)
                            mAdapter.setdownProgress((int) mid,
                                    progress);
                    }
                }

                @Override
                public void Finished(long mid, String localPath) {
                    if (null != mDownloading) {
                        mDownloading.remove((int) mid);
                    }
                    if (isRunning && null != mHanlder) {
                        info.setLocalPath(localPath);
                        FilterData.getInstance().replace(info);
                        if (null != mAdapter) {
                            int id = (int) mid;
                            mAdapter.setdownEnd(id);
                            onSelectedImp(id);
                        }

                    }
                }

                @Override
                public void Canceled(long mid) {
                    Log.e(TAG, "Canceled: xxx" + mid);
                    if (null != mDownloading) {
                        mDownloading.remove((int) mid);
                    }
                    if (isRunning && null != mHanlder) {
                        if (null != mAdapter)
                            mAdapter.setdownFailed((int) mid);
                    }
                }
            });

            if (isRunning && null != mHanlder) {
                mDownloading.put(itemId, download);
                if (null != mAdapter)
                    mAdapter.setdownStart(itemId);
            }
        } else {
            mAdapter.notifyDataSetChanged();
            Log.e(TAG, "download " + info.getUrl() + "  is mDownloading");
        }
    }

    private void getWebFilter() {
        ThreadPoolUtils.execute(new Runnable() {
            @Override
            public void run() {
                if (TextUtils.isEmpty(mUrl)) {
                    Log.e(TAG, "lookup  config.getUrl()  is null");
                    if (null != mHanlder) {
                        mHanlder.obtainMessage(MSG_WEB_PREPARED).sendToTarget();
                    }
                } else {
                    if (mlist.size() == 0) {
                        //防止频繁从网络上获取（慢）
                        getFilterImp();
                    }
                    if (isRunning && null != mHanlder) {
                        mHanlder.obtainMessage(MSG_WEB_PREPARED).sendToTarget();
                    }
                }
            }
        });
    }


    /**
     * 网络filter
     */
    private void getFilterImp() {
        mlist.add(new WebFilterInfo("", R.drawable.camera_effect_0 + "", mContext.getString(R.string.none), "", 0));
        List data = ModeDataUtils.init(getContext(), mUrl, ModeDataUtils.TYPE_FILTER);
        if (null != data) {
            onParseJson2((ArrayList<WebFilterInfo>) data);
        }

    }


    private ArrayList<WebFilterInfo> mlist = new ArrayList<WebFilterInfo>();

    private void onParseJson2(List<WebFilterInfo> data) {
        int len = data.size();
        for (int i = 0; i < len; i++) {
            WebFilterInfo item = data.get(i);
            String url = item.getUrl();
            String name = item.getName();
            String cover = item.getCover();
            long updateTime = item.getUpdatetime();
            WebFilterInfo local = FilterData.getInstance().quweryOne(url);
            boolean result = false;
            if (null != local) {
                //判断服务器是否已经更新 （如果有更新删除旧的文件或文件夹）
                if (updateTime != local.getUpdatetime()) {
                    //服务端已经更新，需要重新下载
                    FileUtils.deleteAll(local.getLocalPath());
                    result = true;
                }
            }
            if (result) {
                //旧的版本，服务端已经更新，需要重新下载
                local.setCover(cover);
                local.setName(name);
                local.setLocalPath("");
                local.setUpdatetime(updateTime);
                mlist.add(local);
                //删除数据库记录
                FilterData.getInstance().delete(local.getUrl());
            } else {
                if (null != local) {
                    local.setUpdatetime(updateTime);
                    String localPath = local.getLocalPath();
                    if (!TextUtils.isEmpty(localPath) && FileUtils.isExist(localPath)) {
                        //已下载，且有效
                        local.setCover(cover);
                        mlist.add(local);
                    } else {
                        //未下载
                        mlist.add(new WebFilterInfo(0, url, cover, name, "", updateTime));
                    }
                } else {
                    //未下载
                    WebFilterInfo info = new WebFilterInfo(0, url, cover,
                            name, "", updateTime);
                    mlist.add(info);

                }
            }
        }
    }

    private final int MSG_ASSET_EXPORT_START = 51;
    private final int MSG_WEB_PREPARED = 53;
    private Handler mHanlder = null;

    private void initHandler() {
        mHanlder = new Handler() {
            @Override
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case MSG_ASSET_EXPORT_START:
                        SysAlertDialog.showLoadingDialog(mContext, R.string.isloading);
                        break;

                    case MSG_WEB_PREPARED: {
                        SysAlertDialog.cancelLoadingDialog();
                        $(R.id.strengthLayout).setVisibility(View.VISIBLE);
                        mAdapter.addAll(true, mlist, mLastPageIndex);
                    }
                    break;

                    default:
                        break;
                }
            }
        };
    }


}
