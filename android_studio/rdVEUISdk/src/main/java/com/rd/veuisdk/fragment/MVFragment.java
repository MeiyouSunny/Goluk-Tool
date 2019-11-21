package com.rd.veuisdk.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rd.downfile.utils.DownLoadUtils;
import com.rd.downfile.utils.IDownFileListener;
import com.rd.http.MD5;
import com.rd.lib.utils.CoreUtils;
import com.rd.lib.utils.LogUtil;
import com.rd.vecore.RdVECore;
import com.rd.vecore.models.MVInfo;
import com.rd.veuisdk.IVideoEditorHandler;
import com.rd.veuisdk.R;
import com.rd.veuisdk.TempVideoParams;
import com.rd.veuisdk.database.MVData;
import com.rd.veuisdk.model.MVWebInfo;
import com.rd.veuisdk.mvp.model.ICallBack;
import com.rd.veuisdk.mvp.model.MVFragmentModel;
import com.rd.veuisdk.ui.HorizontalListViewFresco;
import com.rd.veuisdk.ui.HorizontalListViewFresco.OnListViewItemSelectListener;
import com.rd.veuisdk.utils.IParamData;
import com.rd.veuisdk.utils.IParamHandler;
import com.rd.veuisdk.utils.PathUtils;
import com.rd.veuisdk.utils.SysAlertDialog;
import com.rd.veuisdk.utils.Utils;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * @author JIAN
 * @date 2017-3-23 下午4:35:09
 */
public class MVFragment extends BaseFragment {

    private String mMvUrl;
    private boolean mIsFirstCreate = true;// 防止videoEditActivity oncreate 首选MV
    private Context mContext;
    private IVideoEditorHandler mHlrVideoEditor;
    private IParamData mIMenu;

    public MVFragment() {
        super();
    }

    private final String WEB_MV_URL = "http://d.56show.com/filemanage/public/filemanage/file/appData";
    private boolean bUseNewMV = false;

    /**
     * @param useMV    新的mv网络接口
     * @param _url
     * @param _isfirst
     */
    @SuppressLint("ValidFragment")
    public MVFragment(boolean useMV, String _url, boolean _isfirst) {
        bUseNewMV = useMV;
        //启用mv功能未设置mvUrl
        if (TextUtils.isEmpty(_url)) {
            mMvUrl = WEB_MV_URL;
            bUseNewMV = false;
        } else {
            mMvUrl = _url.trim();
        }
        mIsFirstCreate = _isfirst;
    }

    private MVFragmentModel mModel;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        mHlrVideoEditor = (IVideoEditorHandler) context;
        mIMenu = ((IParamHandler) context).getParamData();

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPageName = getString(R.string.mv);
        MVData.getInstance().initilize(getContext());
        TAG = "MVFragment";
        mModel = new MVFragmentModel(getContext(), new ICallBack<MVWebInfo>() {
            @Override
            public void onSuccess(List<MVWebInfo> list) {
                mlist = list;
                mHanlder.obtainMessage(MSG_WEB_PREPARED).sendToTarget();
            }

            @Override
            public void onFailed() {
                mHanlder.obtainMessage(MSG_WEB_PREPARED).sendToTarget();
            }
        });
    }


    private HorizontalListViewFresco mListView;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        initHandler();
        initListener();
        mRoot = inflater.inflate(R.layout.fragment_video_short_mv, container, false);
        mListView = $(R.id.lvListView);
        mListView.setListItemSelectListener(mOnMvSelectListener);
        mListView.setRepeatSelection(false);
        mListView.setCheckFastRepeat(true);

        mRoot.findViewById(R.id.ivCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBack();
            }
        });

        mRoot.findViewById(R.id.ivSure).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHlrVideoEditor.onSure();
            }
        });
        ((TextView) $(R.id.tvBottomTitle)).setText(R.string.mv);
        if (null != mHanlder) {
            mHanlder.obtainMessage(MSG_NONE_PREPARED).sendToTarget();
        }
        mModel.getWebMV(mMvUrl, bUseNewMV);
        return mRoot;

    }

    public void onBack() {
        if (defaultIndex != -1) {
            mListView.selectListItem(defaultIndex);
        }
        mHlrVideoEditor.onBack();
    }


    @Override
    public void onDestroyView() {
        if (null != mModel) {
            mModel.recycle();
        }
        super.onDestroyView();

        if (null != mDownloading) {
            DownLoadUtils.forceCancelAll();
            mDownloading.clear();
        }
        if (null != mHanlder) {
            mHanlder.removeMessages(MSG_NONE_PREPARED);
            mHanlder.removeMessages(MSG_WEB_DOWNLOADING);
            mHanlder.removeMessages(MSG_ASSET_EXPORT_START);
            mHanlder.removeMessages(MSG_WEB_PREPARED);
            mHanlder.removeMessages(MSG_WEB_DOWN_START);
            mHanlder.removeMessages(MSG_WEB_DOWN_END);
            mHanlder.removeMessages(MSG_WEB_DOWN_FAILED);
            mHanlder = null;
        }
        if (null != mListView) {
            mListView.recycle();
            mListView = null;
        }
        mOnMvSelectListener = null;

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != mModel) {
            mModel.recycle();
        }
        MVData.getInstance().close();
        mHanlder = null;
        mIsFirstCreate = true;

    }


    private int lastItemId = 0;

    private void onSelectedImp(int nItemId, boolean user) {
        boolean bReload = true;
        TempVideoParams.getInstance().setThemeId(nItemId);
        if (nItemId >= 1) {
            int index = nItemId - 1;
            if (index >= 0 && index < mlist.size()) {
                MVWebInfo info = mlist.get(index);
                if (mIMenu.getMVId() != info.getId()) {
                    if (info.getId() != MVWebInfo.DEFAULT_MV_NO_REGISTED) {//已注册此MV,直接使用
                        TempVideoParams.getInstance().setThemeHeader(info.getHeadDuration());
                        TempVideoParams.getInstance().setThemeLast(info.getLastDuration());
                        mIMenu.setMVId(info.getId());
                        mHlrVideoEditor.getEditorVideo().setMV(info.getId());
                        lastItemId = nItemId;
                        mListView.onItemChecked(nItemId);
                    } else {  //此MV未注册，下载该MV
                        bReload = false;
                        if (CoreUtils.checkNetworkInfo(mContext) == CoreUtils.UNCONNECTED) {
                            mListView.selectListItem(lastItemId, true);
                            mListView.resetItem(nItemId);
                            onToast(R.string.please_open_wifi);
                        } else {
                            // 下载
                            downMV(nItemId, info);
                            mListView.onItemChecked(nItemId);
                        }
                    }
                }
            }
        } else {
            TempVideoParams.getInstance().setThemeHeader(0);
            TempVideoParams.getInstance().setThemeLast(0);
            mIMenu.setMVId(RdVECore.DEFAULT_MV_ID);
            mHlrVideoEditor.getEditorVideo().setMV(RdVECore.DEFAULT_MV_ID);
            mListView.onItemChecked(nItemId);
        }
        if (mIsFirstCreate) {
            mIsFirstCreate = false;// 第一次创建fragment
        } else {
            if (user) {
                mHlrVideoEditor.removeMvMusic(false);
                if (nItemId != 0) {
                    //切换mv时m,  清空配乐
                    TempVideoParams.getInstance().recycleMusicObject();
                }
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

    private ArrayList<Long> mDownloading = null;


    /**
     * mv的sd 完整路径
     *
     * @param info
     * @return
     */
    private String getMVFilePath(MVWebInfo info) {
        return PathUtils.getRdMVPath() + "/" + MD5.getMD5(info.getUrl()) + ".zip";
    }

    private void downMV(int itemId, final MVWebInfo info) {
        if (null == mDownloading) {
            mDownloading = new ArrayList<Long>();
        }
        if (!mDownloading.contains((long) itemId)) {

            /**
             * 支持指定下载文件的存放位置
             */
            final DownLoadUtils download = new DownLoadUtils(getContext(), itemId,
                    info.getUrl(), getMVFilePath(info));
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
                    LogUtil.i(TAG, "Finished : " + localPath);

                    //注册当前下载的MV，返回当前MV的Id、片头、片尾
                    if (isRunning && null != mHanlder) {

                        try {
                            MVInfo temp = RdVECore.registerMV(localPath);
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
                    Log.e(TAG, "Canceled: " + mid);
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


    private List<MVWebInfo> mlist = new ArrayList<>();


    private final int MSG_ASSET_EXPORT_START = 51;
    private final int MSG_WEB_PREPARED = 53;
    private final int MSG_NONE_PREPARED = 59; //优先加载的本地图标
    private final int MSG_WEB_DOWNLOADING = 54;
    private final int MSG_WEB_DOWN_START = 55;
    private final int MSG_WEB_DOWN_END = 56;
    private final int MSG_WEB_DOWN_FAILED = -58;
    private Handler mHanlder = null;
    private int defaultIndex = -1;

    private void initHandler() {
        mHanlder = new Handler() {
            int nItemId = 0;

            @Override
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case MSG_ASSET_EXPORT_START:
                        SysAlertDialog.showLoadingDialog(getContext(), R.string.prepareMV);
                        break;

                    case MSG_NONE_PREPARED: {
                        if (null != mListView) {
                            mListView.removeAllListItem();
                            nItemId = 0;
                            mListView.addListItem(nItemId, R.drawable.none_filter_n, getString(R.string.none));
                            $(R.id.tvLoading).setVisibility(View.GONE);
                        }
                        nItemId++;
                        break;

                    }
                    case MSG_WEB_PREPARED: {
                        SysAlertDialog.cancelLoadingDialog();
                        int len = mlist.size();
                        if (null != mListView) {
                            int dst = 0;
                            int mvId = mIMenu.getMVId();
                            for (int i = 0; i < len; i++) {
                                MVWebInfo info = mlist.get(i);
                                mListView.addListItem(nItemId, info.getCover(), info.getName());
                                mListView.setDownLayout(nItemId, info.getId() != MVWebInfo.DEFAULT_MV_NO_REGISTED);
                                if (mvId == info.getId()) {
                                    dst = nItemId;
                                    defaultIndex = dst;
                                }
                                nItemId++;
                            }
                            mListView.selectListItem(dst);
                            $(R.id.tvLoading).setVisibility(View.GONE);
                        }
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
