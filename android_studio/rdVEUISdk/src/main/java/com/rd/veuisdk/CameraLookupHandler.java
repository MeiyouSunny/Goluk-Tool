package com.rd.veuisdk;

import android.content.Context;
import android.hardware.Camera;
import android.os.Handler;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.rd.downfile.utils.DownLoadUtils;
import com.rd.downfile.utils.IDownFileListener;
import com.rd.http.MD5;
import com.rd.lib.utils.CoreUtils;
import com.rd.lib.utils.ThreadPoolUtils;
import com.rd.recorder.api.RecorderCore;
import com.rd.veuisdk.adapter.FilterLookupAdapter;
import com.rd.veuisdk.database.FilterData;
import com.rd.veuisdk.fragment.helper.FilterLookupLocalHandler;
import com.rd.veuisdk.listener.OnItemClickListener;
import com.rd.veuisdk.model.WebFilterInfo;
import com.rd.veuisdk.utils.FileUtils;
import com.rd.veuisdk.utils.ModeDataUtils;
import com.rd.veuisdk.utils.PathUtils;
import com.rd.veuisdk.utils.SysAlertDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * 摄像头支持lookup滤镜
 */
class CameraLookupHandler {
    private RecyclerView mRecyclerView;
    private Context mContext;
    private String TAG = "CameraLookupHandler";
    private String mUrl;
    private int mLastPageIndex = 0;
    private CameraEffectHandler.IFilterCheck mIFilterCheck;

    private CameraLookupHandler() {

    }

    public CameraLookupHandler(Context context, String url, CameraEffectHandler.IFilterCheck iFilterCheck) {
        mlist = new ArrayList<>();
        mContext = context;
        mIFilterCheck = iFilterCheck;
        FilterData.getInstance().initilize(context);
        mUrl = url;
        mFilterAdapter = new FilterLookupAdapter(context);
        mFilterAdapter.setOnItemClickListener(new OnItemClickListener<Object>() {
            @Override
            public void onItemClick(int position, Object item) {
                seekBar.setEnabled(position > 0);
                onSelectedImp(position);
            }
        });

        initHandler();
    }

    /**
     * 进入调节强度
     */
    private void onStrengthShow() {
        seekBar = ((SeekBar) mStrengthLayout.findViewById(R.id.sbarStrength));
        int value = (int) (100 * RecorderCore.getFilterValue());
        seekBar.setProgress(value);
        seekBar.setEnabled(mLastPageIndex > 0);
        tvFilterValue = mStrengthLayout.findViewById(R.id.tvFilterValue);
        tvFilterValue.setText(value + "%");
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                if (fromUser) {
                    tvFilterValue.setText(progress + "%");
                    RecorderCore.setFilterValue(progress / 100.0f);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private FilterLookupAdapter mFilterAdapter;
    private LinearLayout mStrengthLayout;
    private TextView tvFilterValue;
    private boolean enableRepeat = false;
    private SeekBar seekBar;

    public void initView(RecyclerView recyclerView, LinearLayout strengthLayout) {
        mRecyclerView = recyclerView;
        mStrengthLayout = strengthLayout;
        mStrengthLayout.setVisibility(View.VISIBLE);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext(), LinearLayoutManager.HORIZONTAL, false));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        enableRepeat = true;
        mFilterAdapter.setEnableRepeatClick(enableRepeat);
        mRecyclerView.setAdapter(mFilterAdapter);
        onStrengthShow();
    }

    private boolean isVer = true;

    /**
     * @param checkItem
     */
    public void init(boolean isVer, int checkItem) {
        this.isVer = isVer;
        mLastPageIndex = checkItem;
        getWebFilter();
    }

    /**
     * @param nItemId
     */
    public void selectListItem(int nItemId) {
        if (null != mFilterAdapter) {
            mFilterAdapter.onItemChecked(nItemId);
        }
    }

    public int getCurrentItemId() {
        return (null != mFilterAdapter) ? mFilterAdapter.getCurrentId() : 0;
    }

    /**
     * @param isVer
     */
    public void notifyDataSetChanged(boolean isVer) {
        if (null != mFilterAdapter) {
            mFilterAdapter.setOrientation(isVer);
            mFilterAdapter.notifyDataSetChanged();
        }
    }

    private Context getContext() {
        return mContext;
    }

    public void onResume() {
        isRunning = true;
    }

    private boolean isRunning = false;

    private int lastItemId = 0;


    public int size() {
        return mFilterAdapter.getItemCount();
    }

    /**
     * 已下载的lookup文件
     *
     * @param index
     * @return
     */
    public String get(int index) {
        if (index >= 1) {
            //mlist 不包含 "无"
            WebFilterInfo info = mFilterAdapter.getItem(index);
            if (null != info) {
                String file = info.getLocalPath();
                if (!TextUtils.isEmpty(file) && FileUtils.isExist(file)) {
                    return file;
                }
            }
        }

        return Camera.Parameters.EFFECT_NONE;

    }

    /**
     * 切换滤镜效果
     *
     * @param index
     */
    private void switchFliter(int index) {
        //lookup滤镜
        if (index > 0) {
            if (null != mIFilterCheck) {
                mIFilterCheck.onSelected(index, true);
            }
        } else {
            //第0个 无滤镜效果
            if (null != mIFilterCheck) {
                mIFilterCheck.onSelected(index, true);
            }
        }
    }

    private void onSelectedImp(int nItemId) {
        mLastPageIndex = nItemId;
        if (nItemId >= 1) {
            WebFilterInfo info = mFilterAdapter.getItem(nItemId);
            if (lastItemId != nItemId) {
                if (!TextUtils.isEmpty(info.getLocalPath()) && FileUtils.isExist(info.getLocalPath())) {//已下载的lookup滤镜,直接使用
                    switchFliter(nItemId);
                    lastItemId = nItemId;
                } else {  // 下载该filter
                    if (CoreUtils.checkNetworkInfo(mContext) == CoreUtils.UNCONNECTED) {
                        mFilterAdapter.onItemChecked(lastItemId);
                        onToast(getContext().getString(R.string.please_open_wifi));
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
        }


    }


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
     * 下载
     *
     * @param itemId
     * @param info
     */
    private void down(int itemId, final WebFilterInfo info) {
        if (null == mDownloading) {
            mDownloading = new ArrayList<Long>();
        }
        if (mDownloading.size() >= 1) {

            Log.e(TAG, "down:  thread is downloading");

        } else {

            if (!mDownloading.contains((long) itemId)) {
                /**
                 * 支持指定下载文件的存放位置
                 */
                final DownLoadUtils download = new DownLoadUtils(getContext(), itemId,
                        info.getUrl(), getFilterFilePath(info));
                download.setConfig(0, 50, 100);
                download.DownFile(new IDownFileListener() {

                    @Override
                    public void onProgress(long mid, int progress) {
                        if (isRunning && null != mHanlder) {
                            mHanlder.obtainMessage(MSG_WEB_DOWNLOADING, (int) mid,
                                    progress).sendToTarget();
                        }
                    }

                    @Override
                    public void Finished(long mid, String localPath) {
                        if (isRunning && null != mHanlder) {
                            info.setLocalPath(localPath);
                            FilterData.getInstance().replace(info);
                            mHanlder.obtainMessage(MSG_WEB_DOWN_END, (int) mid, 0)
                                    .sendToTarget();
                            mDownloading.remove((long) mid);
                        }
                    }

                    @Override
                    public void Canceled(long mid) {
                        Log.e(TAG, "Canceled: " + mid);
                        if (isRunning && null != mHanlder) {
                            mHanlder.obtainMessage(MSG_WEB_DOWN_FAILED, (int) mid,
                                    0).sendToTarget();
                        }
                        if (null != mDownloading) {
                            mDownloading.remove((long) mid);
                        }
                    }
                });

                if (isRunning && null != mHanlder) {
                    mDownloading.add((long) itemId);
                    mHanlder.obtainMessage(MSG_WEB_DOWN_START, itemId, 0)
                            .sendToTarget();
                }

            } else {
                Log.e(TAG, "download " + info.getUrl() + "  is mDownloading");
            }
        }
    }

    //防止频繁获取网络数据（一次加载成功即可加载本地离线数据）
    private boolean mLoadWebDataSuccessed = false;

    private void getWebFilter() {
        mCacheDir = mContext.getCacheDir();
        ThreadPoolUtils.execute(new Runnable() {

            @Override
            public void run() {
                if (TextUtils.isEmpty(mUrl)) {
//                    Log.e(TAG, "lookup  config.getUrl()  is null");
                    mlist.clear();
                    //本地lookup滤镜 (10个)
                    FilterLookupLocalHandler lookupLocalHandler = new FilterLookupLocalHandler(mContext);
                    mlist.addAll(lookupLocalHandler.getArrayList());
                    if (isRunning && null != mHanlder) {
                        mHanlder.obtainMessage(MSG_WEB_PREPARED).sendToTarget();
                    }
                } else {
                    if (mlist.size() == 0) {
                        //防止频繁请求网络
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

        File f = new File(mCacheDir, MD5.getMD5(mUrl + ModeDataUtils.TYPE_FILTER));
//        Log.e(TAG, "getFilterImp: "+mLoadWebDataSuccessed );
        if (!mLoadWebDataSuccessed && CoreUtils.checkNetworkInfo(mContext) != CoreUtils.UNCONNECTED) {
            String str = ModeDataUtils.getModeData(mUrl, ModeDataUtils.TYPE_FILTER);
            if (!TextUtils.isEmpty(str)) {// 加载网络数据
                onParseJson2(str);
                try {
                    String data = URLEncoder.encode(str, "UTF-8");
                    FileUtils.writeText2File(data,
                            f.getAbsolutePath());
                    mLoadWebDataSuccessed = true;
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();

                }
            }
        }
        if (!mLoadWebDataSuccessed && null != f && f.exists()) {// 加载离线数据
            String offline = FileUtils.readTxtFile(f
                    .getAbsolutePath());
            try {
                offline = URLDecoder.decode(offline, "UTF-8");
                if (!TextUtils.isEmpty(offline)) {
                    onParseJson2(offline);
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();

            }

        }
    }

    private File mCacheDir;
    private ArrayList<Long> mDownloading = null;

    private void onToast(String msg) {
        SysAlertDialog
                .showAutoHideDialog(mContext, "", msg, Toast.LENGTH_SHORT);
    }

    private ArrayList<WebFilterInfo> mlist;

    private void onParseJson2(String data) {
        if (null != mlist) {
            mlist.add(new WebFilterInfo("", R.drawable.camera_effect_0 + "", getContext().getString(R.string.none), "", 0));
            try {
                JSONObject jobj = new JSONObject(data);
                if (null != jobj && jobj.optInt("code", -1) == 0) {
                    JSONArray jarr = jobj.optJSONArray("data");
                    if (null != jarr) {
                        int len = jarr.length();
                        JSONObject jt;
                        for (int i = 0; i < len; i++) {
                            jt = jarr.getJSONObject(i);
                            String url = jt.getString("file");
                            String name = jt.getString("name");
                            String cover = jt.getString("cover");
                            long updateTime = jt.getLong("updatetime");
                            WebFilterInfo local = FilterData.getInstance().quweryOne(
                                    url);
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
                                if (null != mlist) {
                                    mlist.add(local);
                                }
                                //删除数据库记录
                                FilterData.getInstance().delete(local.getUrl());
                            } else {
                                if (null != local) {
                                    local.setUpdatetime(updateTime);
                                    String localPath = local.getLocalPath();
                                    if (!TextUtils.isEmpty(localPath) && FileUtils.isExist(localPath)) {
                                        //已下载，且有效
                                        local.setCover(cover);
                                        if (null != mlist) {
                                            mlist.add(local);
                                        }
                                    } else {
                                        //未下载
                                        if (null != mlist) {
                                            mlist.add(new WebFilterInfo(0, url, cover,
                                                    name, "", updateTime));
                                        }
                                    }
                                } else {
                                    //未下载
                                    WebFilterInfo info = new WebFilterInfo(0, url, cover,
                                            name, "", updateTime);
                                    if (null != mlist) {
                                        mlist.add(info);
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void recycle() {
        isRunning = false;
        mlist.clear();
    }

    private final int MSG_WEB_PREPARED = 53;
    private final int MSG_WEB_DOWNLOADING = 54;
    private final int MSG_WEB_DOWN_START = 55;
    private final int MSG_WEB_DOWN_END = 56;
    private final int MSG_WEB_DOWN_FAILED = -58;
    private Handler mHanlder = null;

    private void initHandler() {
        mHanlder = new Handler() {
            @Override
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case MSG_WEB_PREPARED: {

                        mFilterAdapter.addAll(isVer, mlist, mLastPageIndex);
                    }

                    break;
                    case MSG_WEB_DOWNLOADING: {
                        int id = msg.arg1;
                        if (null != mFilterAdapter) {
                            mFilterAdapter.setdownProgress(id, msg.arg2);
                        }

                    }
                    break;
                    case MSG_WEB_DOWN_START: {
                        int id = msg.arg1;
                        if (null != mFilterAdapter) {
                            mFilterAdapter.setdownStart(id);
                        }
                    }
                    break;
                    case MSG_WEB_DOWN_END: {
                        int id = msg.arg1;
                        if (null != mFilterAdapter) {
                            mFilterAdapter.setdownEnd(id);
                            mFilterAdapter.onItemChecked(id);
                            onSelectedImp(id);
                        }

                    }
                    break;
                    case MSG_WEB_DOWN_FAILED: {
                        int id = msg.arg1;
                        if (null != mFilterAdapter) {
                            mFilterAdapter.setdownFailed(id);
                        }
                    }

                    break;
                    default:
                        break;
                }
            }
        };

    }


}
