package com.rd.veuisdk.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap.CompressFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
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
import com.rd.vecore.Music;
import com.rd.vecore.VirtualVideo;
import com.rd.veuisdk.IVideoEditorHandler;
import com.rd.veuisdk.MoreMusicActivity;
import com.rd.veuisdk.R;
import com.rd.veuisdk.TempVideoParams;
import com.rd.veuisdk.VideoEditActivity;
import com.rd.veuisdk.database.HistoryMusicCloud;
import com.rd.veuisdk.manager.UIConfiguration;
import com.rd.veuisdk.model.AudioMusicInfo;
import com.rd.veuisdk.model.WebInfo;
import com.rd.veuisdk.ui.HorizontalListViewMV;
import com.rd.veuisdk.ui.HorizontalListViewMV.OnListViewItemSelectListener;
import com.rd.veuisdk.ui.SubFunctionUtils;
import com.rd.veuisdk.utils.FileUtils;
import com.rd.veuisdk.utils.PathUtils;
import com.rd.veuisdk.utils.SysAlertDialog;
import com.rd.veuisdk.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * 配乐方式2
 */
public class MusicFragmentEx extends BaseFragment {
    private final int MENU_ORIGIN = 0, MENU_NONE = 1, MENU_LOCAL = 2,
            MENU_YUN = 3;
    private View mBtnVoice;
    /**
     * Music下载地址
     */
    private String mMusicUrl;
    /**
     * 防止videoEditActivity oncreate
     */
    private boolean mIsFirstCreate = true;

    /**
     * 编辑界面控制类对象
     */
    private IVideoEditorHandler mHlrVideoEditor;
    /**
     * 选择标记
     */
    private int mThemeType = 0;
    /**
     * 片尾时长
     */
    private float mTrailerDuration;
    /**
     * 云音乐地址
     */
    private String mCloudMusicUrl = null;
    /**
     * 控制混音等级
     */
    private SeekBar mFactor;

    public MusicFragmentEx() {
        super();
    }

    private int voiceLayout = UIConfiguration.VOICE_LAYOUT_1;
    /**
     * 监听回调
     */
    private IMusicListener mMusicListener;
    private boolean enableLocalMusic = true;

    public void init(float trailerDuration, String _musicUrl,
                     int _voiceLayout, IMusicListener ilistener, String url, boolean enable) {
        mTrailerDuration = trailerDuration;
        mMusicListener = ilistener;
        voiceLayout = _voiceLayout;
        this.enableLocalMusic = enable;
        mMusicUrl = TextUtils.isEmpty(_musicUrl) ? "" : _musicUrl.trim();
        mCloudMusicUrl = TextUtils.isEmpty(url) ? "" : url.trim();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;
        mHlrVideoEditor = (IVideoEditorHandler) activity;
    }

    /**
     * 缓存图片用
     */
    private ImageResizer mFetcher;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPageName = getString(R.string.music2);
        ImageCacheParams cacheParams = new ImageCacheParams(mContext,
                null);
        cacheParams.compressFormat = CompressFormat.PNG;
        cacheParams.setMemCacheSizePercent(0.05f);
        mFetcher = new HttpImageFetcher(mContext, 150, 150);
        mFetcher.addImageCache(mContext, cacheParams);

    }

    private HorizontalListViewMV mListView;

    /**
     * 是否仅重新加载music
     *
     * @param onlyMusic
     */
    private void onlyReloadMusic(boolean onlyMusic) {
        mHlrVideoEditor.reload(onlyMusic);
        mHlrVideoEditor.seekTo(0);
        mHlrVideoEditor.start();
        mHlrVideoEditor.getEditorVideo().setOriginalMixFactor(mFactor.getProgress());
    }

    private boolean loadWeb = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.music_fragment, null);
        mFactor = (SeekBar) findViewById(R.id.musicfactor);

        if (null != mFactor) {
            mFactor.setProgress(100 - factor);
            mFactor.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
                Music audio;

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    if (null != audio) {
                        factor = 100 - mFactor.getProgress();
                        audio.setMixFactor(factor);
                    }
                    mHlrVideoEditor.getEditorVideo().setOriginalMixFactor(mFactor.getProgress());
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                    audio = TempVideoParams.getInstance().getMusic();

                }

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress,
                                              boolean fromUser) {
                    if (null != audio) {
                        factor = 100 - progress;
                        audio.setMixFactor(factor);
                    }
                    mHlrVideoEditor.getEditorVideo().setOriginalMixFactor(progress);
                }
            });
        }

        mBtnVoice = findViewById(R.id.btnVoice2);
        if (SubFunctionUtils.isHideDubbing()) {
            mBtnVoice.setVisibility(View.GONE);
        } else {
            if (voiceLayout == UIConfiguration.VOICE_LAYOUT_2) {
                mBtnVoice.setVisibility(View.VISIBLE);
                mBtnVoice.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if (null != mMusicListener) {
                            mMusicListener.onVoiceClick(v);
                        }
                    }
                });

            } else {
                mBtnVoice.setVisibility(View.GONE);

            }
        }
        initHandler();
        initListener();
        mListView = (HorizontalListViewMV) findViewById(R.id.lvListView);
        mListView.setIsMusic();
        mListView.setListItemSelectListener(listener);
        mListView.setCheckFastRepeat(true);
        mListView.setRepeatSelection(true);
        getWebMusic();
        return mRoot;
    }

    private int lastItemId = MENU_ORIGIN;
    private String musicName = "";

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Log.e("onActivityResult", resultCode + "");
        if (requestCode == VideoEditActivity.REQUSET_MUSICEX) {
            if (resultCode == Activity.RESULT_OK) {
                AudioMusicInfo audioMusic = (AudioMusicInfo) data
                        .getParcelableExtra(MoreMusicActivity.MUSIC_INFO);
                mFactor.setEnabled(true);
                if (mThemeType == MENU_LOCAL) {
                    try {
                        mListView.setCurrentCaption(audioMusic.getName());
                        mListView.setCaption(MENU_YUN,
                                getString(R.string.music_yun));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (mThemeType == MENU_YUN) {
                    try {
                        mListView.setCurrentCaption(audioMusic.getName());
                        mListView.setCaption(MENU_LOCAL,
                                getString(R.string.local));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                lastItemId = mThemeType;
                musicName = audioMusic.getName();
                HistoryMusicCloud.getInstance().replaceMusic(
                        audioMusic.getPath(), audioMusic.getName(),
                        audioMusic.getDuration());
                Music ao = VirtualVideo.createMusic(audioMusic.getPath());
                ao.setTimeRange(Utils.ms2s(audioMusic.getStart()), Utils.ms2s(audioMusic.getEnd()));
                ao.setMixFactor(factor);
                onMusicChecked(ao, false);
            } else {
                if (lastItemId != MENU_ORIGIN) {// 非原音开关
                    if (lastItemId != mThemeType) {
                        mListView.selectListItem(lastItemId, false);
                    }
                    lastItemId = mThemeType;
                    mHlrVideoEditor.reload(false);
                    mHlrVideoEditor.seekTo(0);
                    mHlrVideoEditor.start();
                }
            }

        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mIsFirstCreate = true;
        mFetcher.cleanUpCache();
        mFetcher = null;
        mListView = null;
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
        }
        mHanlder = null;
        listener = null;
        mRoot = null;
    }

    public interface IMusicListener {
        void onVoiceChanged(boolean isChecked);

        void onVoiceClick(View v);

    }

    private OnListViewItemSelectListener listener = null;

    private void initListener() {
        listener = new OnListViewItemSelectListener() {

            @Override
            public void onSelected(View view, int nItemId, boolean user) {
                // Log.e("onSelected", nItemId + "--" + mIsFirstCreate + "/..."
                // + user);
                onSelectedImp(nItemId, user);

            }

            @Override
            public boolean onBeforeSelect(View view, int nItemId) {
                return false;
            }
        };
    }

    /**
     * 选择进入本地音乐界面
     */
    private void onMusicLocal() {
        HistoryMusicCloud.getInstance().initilize(mContext);
        Intent music = new Intent(getActivity(), MoreMusicActivity.class);
        music.putExtra(MoreMusicActivity.PARAM_TYPE,
                MoreMusicActivity.TYPE_MUSIC_LOCAL);
        getActivity().startActivityForResult(music,
                VideoEditActivity.REQUSET_MUSICEX);
        getActivity().overridePendingTransition(R.anim.push_bottom_in,
                R.anim.push_top_out);
    }

    /**
     * 选择进入云音乐界面
     */
    private void onMusicYUN() {
        HistoryMusicCloud.getInstance().initilize(mContext);
        Intent music = new Intent(getActivity(), MoreMusicActivity.class);
        music.putExtra(MoreMusicActivity.PARAM_TYPE,
                MoreMusicActivity.TYPE_MUSIC_YUN);
        music.putExtra(MoreMusicActivity.PARAM_CLOUDMUSIC, mCloudMusicUrl);
        getActivity().startActivityForResult(music,
                VideoEditActivity.REQUSET_MUSICEX);
        getActivity().overridePendingTransition(R.anim.push_bottom_in,
                R.anim.push_top_out);
    }

    /**
     * 选中一首配乐
     */
    private void onMusicChecked(Music ao, boolean onlyMusic) {
        mHlrVideoEditor.removeMvMusic(true);
        try {
            if (mTrailerDuration > 0) {
                ao.setFadeInOut(mTrailerDuration, mTrailerDuration);
            } else {
                ao.setFadeInOut(Utils.ms2s(800),
                        Utils.ms2s(800));
            }
            TempVideoParams.getInstance().setMusicObject(ao);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("onclick", "audio not exists");
        }

        onlyReloadMusic(onlyMusic);
    }

    private int factor = 50;

    /**
     * 音乐列表选中处理
     */
    private void onSelectedImp(int nItemId, boolean user) {
        boolean bReload = true;
//        Log.e("onSelectedImp", mThemeType + "...." + nItemId + "..." +
//                user + "...." + lastItemId);
        mThemeType = nItemId;
        mFactor.setEnabled(true);
        if (nItemId == MENU_ORIGIN) {
            if (user) {
                boolean voiceIsOpen = mHlrVideoEditor.isMediaMute();

                mListView.setItemSrc(nItemId,
                        voiceIsOpen ? R.drawable.video_origin_n
                                : R.drawable.video_origin_p,
                        voiceIsOpen ? R.string.video_voice_n
                                : R.string.video_voice_p);

                if (voiceIsOpen) {
                    factor = 50;
                    if (null != mFactor) {
                        mFactor.setProgress(50);
                        mFactor.setEnabled(true);
                    }
                } else {
                    factor = 100;
                    if (null != mFactor) {
                        mFactor.setProgress(0);
                        mFactor.setEnabled(false);
                    }
                }
                Music audio = TempVideoParams.getInstance().getMusic();
                if (null != audio) {
                    audio.setMixFactor(factor);
                } else {
                    if (null != mFactor) {
                        mFactor.setEnabled(false);
                    }
                }
                if (null != mMusicListener) {
                    mMusicListener.onVoiceChanged(voiceIsOpen);
                }

            }
            // lastItemId = nItemId; 原音开关特别处理，不记录当前位置
        } else if (nItemId == MENU_NONE) {
            if (user) {
                try {
                    mListView.setCaption(MENU_LOCAL, getString(R.string.local));
                    mListView.setCaption(MENU_YUN, getString(R.string.music_yun));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                factor = 50;
                mFactor.setProgress(factor);
                TempVideoParams.getInstance().recycleMusicObject();
                onlyReloadMusic(true);
                mListView.onItemChecked(nItemId);
                if (mIsFirstCreate) {
                    mIsFirstCreate = false;// 第一次创建fragment
                }
            }
            mFactor.setEnabled(false);
            lastItemId = nItemId;
        } else if (nItemId == MENU_LOCAL) {
            if (user) {
                onMusicLocal();
            }
        } else if (nItemId == MENU_YUN) {
            if (user) {
                onMusicYUN();
            }
        } else {
            if (user) {
                WebInfo info = mlist.get(nItemId - (MENU_YUN + 1));
                if (info.existsMusic()) {
                    try {
                        mListView.setCaption(MENU_LOCAL,
                                getString(R.string.local));
                        mListView.setCaption(MENU_YUN,
                                getString(R.string.music_yun));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (null != mMusicListener) {
                        Music ao = VirtualVideo.createMusic(info.getLocalPath());
                        ao.setMixFactor(factor);
                        onMusicChecked(ao, true);
                    }
                    lastItemId = nItemId;
                } else {
                    if (CoreUtils.checkNetworkInfo(mContext
                            .getApplicationContext()) == CoreUtils.UNCONNECTED) {
                        int temp=nItemId;
                        mListView.resetItem(temp);
                        mListView.selectListItem(lastItemId, true);
                        onToast(getString(R.string.please_open_wifi));
                    } else {
                        // 下载
                        downMusic(nItemId, info);
                    }
                }
            }

        }

    }

    private ArrayList<Long> mDownloading = null;

    /**
     * 下载音乐
     */
    private void downMusic(int itemId, final WebInfo info) {
        if (null == mDownloading) {
            mDownloading = new ArrayList<Long>();
        }
        if (!mDownloading.contains((long) itemId)) {
            final DownLoadUtils download = new DownLoadUtils(itemId,
                    info.getUrl(), "mp3");
            download.setConfig(0, 50, 100);
            download.setMethod(true);
            download.DownFile(new IDownFileListener() {

                @Override
                public void onProgress(long mid, int progress) {
                    // Log.e("onProgress" + Thread.currentThread().toString(),
                    // mid
                    // + "---" + progress);
                    if (null != mHanlder) {
                        mHanlder.obtainMessage(MSG_WEB_DOWNLOADING, (int) mid,
                                progress).sendToTarget();
                    }
                }

                @Override
                public void Finished(long mid, String localPath) {
                    if (isRunning) {
                        try {
                            File ftar = new File(localPath);
                            ftar.renameTo(new File(info.getLocalPath()));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (null != mHanlder) {
                            mHanlder.obtainMessage(MSG_WEB_DOWN_END, (int) mid, 0)
                                    .sendToTarget();
                        }
                        if (null != mDownloading) {
                            mDownloading.remove((long) mid);
                        }
                    }

                }

                @Override
                public void Canceled(long mid) {
                    // Log.e("Canceled" + Thread.currentThread().toString(), mid
                    // + "---");
                    if (isRunning) {
                        if (null != mHanlder) {
                            mHanlder.obtainMessage(MSG_WEB_DOWN_FAILED, (int) mid,
                                    0).sendToTarget();
                        }
                        if (null != mDownloading) {
                            mDownloading.remove((long) mid);
                        }

                    }

                }
            });
            mDownloading.add((long) itemId);
            if (isRunning && null != mHanlder) {
                mHanlder.obtainMessage(MSG_WEB_DOWN_START, itemId, 0)
                        .sendToTarget();
            }
        } else {

            Log.e(TAG, "download " + info.getUrl() + "  is downloading");
        }
    }

    private boolean bLoadWebDataSuccessed = false;

    /**
     * 获取云音乐JSON
     */
    private void getWebMusic() {


        if (null != mHanlder) {
            mHanlder.obtainMessage(MSG_LOCAL_MENU).sendToTarget();
        }
        ThreadPoolUtils.execute(new Runnable() {

            @Override
            public void run() {

                if (TextUtils.isEmpty(mMusicUrl)) {
                    Log.i(TAG, "mMusicUrl is null");
                } else {
                    File cacheDir = mContext.getCacheDir();
                    File f = new File(cacheDir, MD5.getMD5("music_data.json"));
                    boolean bNeedLoadLocal = true;//加载网络数据失败就加载本地离线
                    if (!bLoadWebDataSuccessed
                            && CoreUtils.checkNetworkInfo(mContext) != CoreUtils.UNCONNECTED) {
                        String str = RdHttpClient.PostJson(mMusicUrl,
                                new NameValuePair("type", "android"));
                        if (!TextUtils.isEmpty(str)) {// 加载网络数据
                            onParseJson(str);
                            try {
                                String data = URLEncoder.encode(str, "UTF-8");
                                FileUtils.writeText2File(data,
                                        f.getAbsolutePath());
                                bNeedLoadLocal = false;
                                bLoadWebDataSuccessed = true;
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    if (bNeedLoadLocal && null != f && f.exists()) {// 加载离线数据
                        String offline = FileUtils.readTxtFile(f
                                .getAbsolutePath());
                        // Log.e("str", str + "");
                        try {
                            offline = URLDecoder.decode(offline, "UTF-8");
                            if (!TextUtils.isEmpty(offline)) {
                                onParseJson(offline);
                            }
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }

                }
                if (null != mHanlder) {
                    mHanlder.obtainMessage(MSG_WEB_PREPARED).sendToTarget();
                }
            }
        });
    }


    private void onToast(String msg) {
        SysAlertDialog
                .showAutoHideDialog(mContext, "", msg, Toast.LENGTH_SHORT);
    }

    /**
     * 音乐列表
     */
    private ArrayList<WebInfo> mlist = new ArrayList<WebInfo>();

    /**
     * 解析返回的MUSIC的json，把音乐信息写入mlist
     */
    private void onParseJson(String data) {
        try {
            JSONObject jobj = new JSONObject(data);
            JSONObject jtemp = jobj.optJSONObject("result");
            if (null != jtemp) {
                JSONArray jarr = jtemp.optJSONArray("bgmusic");
                if (null != jarr) {
                    int len = jarr.length();
                    JSONObject jt;
                    mlist.clear();
                    loadWeb = false;
                    for (int i = 0; i < len; i++) {
                        jt = jarr.getJSONObject(i);
                        if (null != jt) {
                            String url = jt.getString("url");
                            String name = jt.getString("name");
                            String localPath = (PathUtils.getRdMusic() + "/"
                                    + MD5.getMD5(url) + ".mp3").trim();
                            mlist.add(new WebInfo(url, jt.getString("icon"),
                                    name, localPath));

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
    private final int MSG_WEB_DOWNLOADING = 54;
    private final int MSG_WEB_DOWN_START = 55;
    private final int MSG_WEB_DOWN_END = 56;
    private final int MSG_WEB_DOWN_FAILED = -58;
    private Handler mHanlder = null;

    private final int MSG_LOCAL_MENU = 510;

    /**
     * 初始化消息接收
     */
    private void initHandler() {
        mHanlder = new Handler(Looper.getMainLooper()) {
            int nItemId = 0;

            @Override
            public void handleMessage(android.os.Message msg) {

                switch (msg.what) {
                    case MSG_ASSET_EXPORT_START:
                        SysAlertDialog.showLoadingDialog(
                                mContext.getApplicationContext(),
                                R.string.isloading);
                        break;
                    case MSG_LOCAL_MENU: {
                        if (null != mListView) {
                            mListView.removeAllListItem();
                            nItemId = 0;
                            mListView.addListItem(nItemId, R.drawable.video_origin_n,
                                    getString(R.string.video_voice_n));
                            nItemId++;
                            mListView.addListItem(nItemId, R.drawable.music_none,
                                    getString(R.string.music_none));
                            nItemId++;
                            if (enableLocalMusic) {
                                mListView.addListItem(nItemId, R.drawable.music_local,
                                        getString(R.string.local));
                            }
                            nItemId++;

                            if (!TextUtils.isEmpty(mCloudMusicUrl)) {
                                mListView.addListItem(nItemId, R.drawable.music_yun,
                                        getString(R.string.music_yun));
                            }
                            nItemId++;
                        }
                        break;
                    }
                    case MSG_WEB_PREPARED: {
                        SysAlertDialog.cancelLoadingDialog();
                        if (null != mListView) {
                            int len = mlist.size();
                            for (int i = 0; i < len; i++) {
                                WebInfo info = mlist.get(i);
                                mListView.addListItem(nItemId, info.getImg(),
                                        info.getName(), mFetcher);
                                mListView.setDownLayout(nItemId, info.existsMusic());
                                nItemId++;
                            }

                            Music audio = TempVideoParams.getInstance()
                                    .getMusic();
                            if (null == audio) {// 防止mv 清空配乐
                                lastItemId = MENU_NONE;
                            }
                            mListView.selectListItem(lastItemId, mThemeType > MENU_YUN);
                            if (lastItemId == MENU_LOCAL || lastItemId == MENU_YUN) {
                                mListView.setCurrentCaption(musicName);
                            }
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
                            mListView.selectListItem(id, true);
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
