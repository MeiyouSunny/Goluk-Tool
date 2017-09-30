package com.rd.veuisdk.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap.CompressFormat;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.rd.cache.HttpImageFetcher;
import com.rd.cache.ImageCache.ImageCacheParams;
import com.rd.cache.ImageResizer;
import com.rd.downfile.utils.DownLoadUtils;
import com.rd.downfile.utils.IDownFileListener;
import com.rd.http.MD5;
import com.rd.http.NameValuePair;
import com.rd.lib.utils.CoreUtils;
import com.rd.lib.utils.ThreadPoolUtils;
import com.rd.net.RdHttpClient;
import com.rd.vecore.RdVECore;
import com.rd.vecore.models.MVInfo;
import com.rd.veuisdk.IVideoEditorHandler;
import com.rd.veuisdk.R;
import com.rd.veuisdk.TempVideoParams;
import com.rd.veuisdk.database.MVData;
import com.rd.veuisdk.model.MVWebInfo;
import com.rd.veuisdk.ui.HorizontalListViewMV;
import com.rd.veuisdk.ui.HorizontalListViewMV.OnListViewItemSelectListener;
import com.rd.veuisdk.utils.FileUtils;
import com.rd.veuisdk.utils.SysAlertDialog;
import com.rd.veuisdk.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;


/**
 * @author JIAN
 * @date 2017-3-23 下午4:35:09
 */
public class MVFragment extends BaseFragment {

    private String mMvUrl;
    private boolean mIsFirstCreate = true;// 防止videoEditActivity oncreate 首选MV

    public MVFragment() {
        super();
    }

    private static final String WEB_MV_URL = "http://dianbook.17rd.com/api/shortvideo/getmvprop2";

    @SuppressLint("ValidFragment")
    public MVFragment(String _url, boolean _isfirst) {
        //启用mv功能未设置mvUrl
        mMvUrl = TextUtils.isEmpty(_url) ? WEB_MV_URL : _url.trim();
        mIsFirstCreate = _isfirst;
    }

    private Context mContext;

    private IVideoEditorHandler mHlrVideoEditor;

    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;
        mHlrVideoEditor = (IVideoEditorHandler) activity;
    }

    private ImageResizer mFetcher;
    private int mThemeType = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPageName = getString(R.string.mv);
        // Log.e("onCreate", this.toString() + "---" + mThemeType);


        ImageCacheParams cacheParams = new ImageCacheParams(mContext,
                null);
        cacheParams.compressFormat = CompressFormat.PNG;
        cacheParams.setMemCacheSizePercent(0.05f);
        mFetcher = new HttpImageFetcher(mContext, 150, 150);
        mFetcher.addImageCache(mContext, cacheParams);

        mThemeType = TempVideoParams.getInstance().getThemeId();

    }

    private HorizontalListViewMV mListView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Log.e("onCreateView", this.toString());
        initHandler();
        initListener();
        mRoot = inflater.inflate(R.layout.fragment_video_short_mv, null);
        mListView = (HorizontalListViewMV) findViewById(R.id.lvListView);
        mListView.setListItemSelectListener(mOnMvSelectListener);
        mListView.setRepeatSelection(false);
        mListView.setCheckFastRepeat(true);
        getWebMV();
        return mRoot;

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Log.e("ondestory", this.toString());
        mIsFirstCreate = true;
        mFetcher.cleanUpCache();
        mFetcher = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (null != mDownloading) {
            DownLoadUtils.forceCancelAll();
            mDownloading.clear();
        }
        if (null != mListView) {
            mListView.recycle();
            mListView = null;
        }
        mHanlder = null;
        mOnMvSelectListener = null;

    }

    private int lastMVId = 0;//记录当前被应用的MVId

    public int getCurrentMVId() {
        return lastMVId;
    }

    private int lastItemId = 0;

    private void onSelectedImp(int nItemId, boolean user) {
        boolean bReload = true;
        mThemeType = nItemId;
        TempVideoParams.getInstance().setThemeId(mThemeType);
        if (nItemId >= 1) {
            MVWebInfo info = mlist.get(nItemId - 1);
            if (lastMVId != info.getId()) {
                if (info.getId() != MVWebInfo.DEFAULT_MV_NO_REGISTED) {//已注册此MV,直接使用
                    lastMVId = info.getId();
                    TempVideoParams.getInstance().setThemeHeader(info.getHeadDuration());
                    TempVideoParams.getInstance().setThemeLast(info.getLastDuration());
                    mHlrVideoEditor.getEditorVideo().setMV(lastMVId);
                    lastItemId = nItemId;
                    mListView.onItemChecked(nItemId);
                } else {  //此MV未注册，下载该MV
                    bReload = false;
                    if (CoreUtils.checkNetworkInfo(mContext) == CoreUtils.UNCONNECTED) {
                        mListView.selectListItem(lastItemId, true);
                        mListView.resetItem(nItemId);
                        onToast(getString(R.string.please_open_wifi));
                    } else {
                        // 下载
                        lastMVId = 0;
                        downMV(nItemId, info);
                        mListView.onItemChecked(nItemId);
                    }
                }
            }


        } else {
            TempVideoParams.getInstance().setThemeHeader(0);
            TempVideoParams.getInstance().setThemeLast(0);
            lastMVId = 0;
            mHlrVideoEditor.getEditorVideo().setMV(RdVECore.DEFAULT_MV_ID);
            mListView.onItemChecked(nItemId);
        }


        if (mIsFirstCreate) {
            mIsFirstCreate = false;// 第一次创建fragment
        } else {
            if (user) {
                mHlrVideoEditor.removeMvMusic(false);
                TempVideoParams.getInstance().recycleMusicObject();// 清空配乐
            }
            if (bReload) {
                mHlrVideoEditor.reload(false);
            } else {
                mHlrVideoEditor.seekTo(0);
            }
            if (!mHlrVideoEditor.isPlaying()) {
                mHlrVideoEditor.start();
            }
        }

    }

    private OnListViewItemSelectListener mOnMvSelectListener = null;

    private void initListener() {
        mOnMvSelectListener = new OnListViewItemSelectListener() {

            @Override
            public void onSelected(View view, int nItemId, boolean user) {
                // Log.e("onSelected", nItemId + "--" + isFirst);
                onSelectedImp(nItemId, user);

            }

            @Override
            public boolean onBeforeSelect(View view, int nItemId) {
                return false;
            }
        };
    }

    private File mCacheDir;
    private ArrayList<Long> mDownloading = null;

    private void downMV(int itemId, final MVWebInfo info) {
        if (null == mDownloading) {
            mDownloading = new ArrayList<Long>();
        }
        if (!mDownloading.contains((long) itemId)) {
            final DownLoadUtils download = new DownLoadUtils(itemId,
                    info.getUrl(), "zip");
            download.setConfig(0, 50, 100);
            download.setMethod(true);
            download.DownFile(new IDownFileListener() {

                @Override
                public void onProgress(long mid, int progress) {
                    // Log.e("onProgress" + Thread.currentThread().toString(),
                    // mid
                    // + "---" + progress);
                    if (isRunning && null != mHanlder) {
                        mHanlder.obtainMessage(MSG_WEB_DOWNLOADING, (int) mid,
                                progress).sendToTarget();
                    }
                }

                @Override
                public void Finished(long mid, String localPath) {
//                    Log.e("Finished" + Thread.currentThread().toString(), mid
//                            + "---" + localPath);
                    //注册当前下载的MV，返回当前MV的Id、片头、片尾
                    if (isRunning && null != mHanlder) {
                        MVInfo temp = null;
                        try {
                            temp = RdVECore.registerMV(localPath);
                            if (null != temp) {
                                info.setId(temp.getId());
                                info.setHeadDuration(Utils.s2ms(temp.getHeadDuration()));
                                info.setLastDuration(Utils.s2ms(temp.getLastDuration()));
                            }
                            info.setLocalPath(localPath);
                            MVData.getInstance().replace(info);
                            mHanlder.obtainMessage(MSG_WEB_DOWN_END, (int) mid, 0)
                                    .sendToTarget();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        mDownloading.remove((long) mid);
                    }
                }

                @Override
                public void Canceled(long mid) {
                    // Log.e("Canceled" + Thread.currentThread().toString(), mid
                    // + "---");

                    if (isRunning && null != mHanlder) {
                        mHanlder.obtainMessage(MSG_WEB_DOWN_FAILED, (int) mid,
                                0).sendToTarget();
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

    //防止频繁获取网络数据（一次加载成功即可加载本地离线数据）
    private boolean mLoadWebDataSuccessed = false;

    private void getWebMV() {
        mCacheDir = mContext.getCacheDir();
        if (null != mHanlder) {
            mHanlder.obtainMessage(MSG_NONE_PREPARED).sendToTarget();
        }
        ThreadPoolUtils.execute(new Runnable() {

            @Override
            public void run() {

                if (TextUtils.isEmpty(mMvUrl)) {
                    Log.e(TAG, "mv  config.getUrl()  is null");
                    if (null != mHanlder) {
                        mHanlder.obtainMessage(MSG_WEB_PREPARED).sendToTarget();
                    }
                } else {
                    File f = new File(mCacheDir, MD5.getMD5("mv_data.json"));
                    if (!mLoadWebDataSuccessed && CoreUtils.checkNetworkInfo(mContext) != CoreUtils.UNCONNECTED) {
                        String str = RdHttpClient.PostJson(mMvUrl,
                                new NameValuePair("type", "android"));
                        if (!TextUtils.isEmpty(str)) {// 加载网络数据
                            onParseJson(str);
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
                        Log.e(TAG, "offline" + offline);
                        try {
                            offline = URLDecoder.decode(offline, "UTF-8");
                            if (!TextUtils.isEmpty(offline)) {
                                onParseJson(offline);
                            }
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();

                        }

                    }
                    if (isRunning && null != mHanlder) {
                        mHanlder.obtainMessage(MSG_WEB_PREPARED).sendToTarget();
                    }
                }
            }
        });
    }

    private void onToast(String msg) {
        SysAlertDialog
                .showAutoHideDialog(mContext, "", msg, Toast.LENGTH_SHORT);
    }

    private ArrayList<MVWebInfo> mlist = new ArrayList<MVWebInfo>();
    ;

    private void onParseJson(String data) {
        try {
            JSONObject jobj = new JSONObject(data);
            JSONObject jtemp = jobj.optJSONObject("result");
            if (null != jtemp) {
                JSONArray jarr = jtemp.optJSONArray("mvlist");
                if (null == jarr) {
                    jarr = jtemp.optJSONArray("data");
                }
                if (null != jarr) {
                    int len = jarr.length();
                    JSONObject jt;
                    mlist.clear();
                    for (int i = 0; i < len; i++) {
                        jt = jarr.getJSONObject(i);
                        if (null != jt) {
                            String url = jt.getString("url");
                            String name = jt.getString("name");
                            String localPath = "";
                            MVWebInfo local = MVData.getInstance().quweryOne(
                                    url);

                            if (null != local) {
                                localPath = local.getLocalPath();
                                if (!TextUtils.isEmpty(localPath)) {
                                    MVInfo mv = null;
                                    try {
                                        mv = RdVECore.registerMV(localPath);
                                        if (null != mv) {
                                            local.setHeadDuration(Utils.s2ms(mv.getHeadDuration()));
                                            local.setLastDuration(Utils.s2ms(mv.getLastDuration()));
                                            local.setId(mv.getId());
                                            local.setImg(jt.getString("img"));
                                            mlist.add(local);
                                        } else {
                                            mlist.add(new MVWebInfo(MVWebInfo.DEFAULT_MV_NO_REGISTED, url, jt.getString("img"),
                                                    name, localPath));
                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                        mlist.add(new MVWebInfo(MVWebInfo.DEFAULT_MV_NO_REGISTED, url, jt.getString("img"),
                                                name, localPath));
                                    }

                                } else {
                                    mlist.add(new MVWebInfo(MVWebInfo.DEFAULT_MV_NO_REGISTED, url, jt.getString("img"),
                                            name, localPath));
                                }
                            } else {
                                mlist.add(new MVWebInfo(MVWebInfo.DEFAULT_MV_NO_REGISTED, url, jt.getString("img"),
                                        name, localPath));
                            }
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private final int MSG_ASSET_EXPORT_START = 51;
    private final int MSG_WEB_PREPARED = 53;
    private final int MSG_NONE_PREPARED = 59; //优先加载的本地图标
    private final int MSG_WEB_DOWNLOADING = 54;
    private final int MSG_WEB_DOWN_START = 55;
    private final int MSG_WEB_DOWN_END = 56;
    private final int MSG_WEB_DOWN_FAILED = -58;
    private Handler mHanlder = null;

    private void initHandler() {
        mHanlder = new Handler() {
            int nItemId = 0;

            @Override
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case MSG_ASSET_EXPORT_START:
                        SysAlertDialog.showLoadingDialog(
                                mContext.getApplicationContext(),
                                R.string.prepareMV);
                        break;

                    case MSG_NONE_PREPARED: {
                        mListView.removeAllListItem();
                        nItemId = 0;
                        mListView.addListItem(nItemId, R.drawable.none_filter_n,
                                getString(R.string.none));
                        nItemId++;
                        break;

                    }
                    case MSG_WEB_PREPARED: {
                        SysAlertDialog.cancelLoadingDialog();
                        int len = mlist.size();
                        for (int i = 0; i < len; i++) {
                            MVWebInfo info = mlist.get(i);
                            mListView.addListItem(nItemId, info.getImg(),
                                    info.getName(), mFetcher);
                            mListView.setDownLayout(nItemId, info.getId() != MVWebInfo.DEFAULT_MV_NO_REGISTED);
                            nItemId++;
                        }

                        mListView.selectListItem(mThemeType);
                    }

                    break;
                    case MSG_WEB_DOWNLOADING: {
                        int id = msg.arg1;
                        if (null != mListView)
                            mListView.setdownProgress(id, msg.arg2);
                    }
                    break;
                    case MSG_WEB_DOWN_START: {
                        int id = msg.arg1;
                        if (null != mListView)
                            mListView.setdownStart(id);
                    }
                    break;
                    case MSG_WEB_DOWN_END: {
                        int id = msg.arg1;
                        if (null != mListView) {
                            mListView.setdownEnd(id);
                            mListView.selectListItem(id);
                            onSelectedImp(id, false);
                        }

                    }
                    break;
                    case MSG_WEB_DOWN_FAILED: {
                        int id = msg.arg1;
                        if (null != mListView)
                            mListView.setdownFailed(id);
                    }

                    break;
                    default:
                        break;
                }
            }
        };
    }


}
