package com.rd.veuisdk.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.rd.downfile.utils.DownLoadUtils;
import com.rd.downfile.utils.IDownListener;
import com.rd.http.MD5;
import com.rd.http.NameValuePair;
import com.rd.lib.utils.CoreUtils;
import com.rd.lib.utils.LogUtil;
import com.rd.lib.utils.ThreadPoolUtils;
import com.rd.net.RdHttpClient;
import com.rd.vecore.Music;
import com.rd.vecore.VirtualVideo;
import com.rd.veuisdk.IVideoMusicEditor;
import com.rd.veuisdk.MoreMusicActivity;
import com.rd.veuisdk.R;
import com.rd.veuisdk.TempVideoParams;
import com.rd.veuisdk.VideoEditActivity;
import com.rd.veuisdk.database.HistoryMusicCloud;
import com.rd.veuisdk.manager.UIConfiguration;
import com.rd.veuisdk.model.AudioMusicInfo;
import com.rd.veuisdk.model.CloudAuthorizationInfo;
import com.rd.veuisdk.model.IApiInfo;
import com.rd.veuisdk.ui.HorizontalListViewFresco;
import com.rd.veuisdk.ui.HorizontalListViewFresco.OnListViewItemSelectListener;
import com.rd.veuisdk.utils.FileUtils;
import com.rd.veuisdk.utils.IParamData;
import com.rd.veuisdk.utils.IParamHandler;
import com.rd.veuisdk.utils.ModeDataUtils;
import com.rd.veuisdk.utils.PathUtils;
import com.rd.veuisdk.utils.SysAlertDialog;
import com.rd.veuisdk.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FilenameFilter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * ??????
 */
public class MusicFragmentEx extends BaseFragment {
    public static final int MENU_NONE = 1;
    private static final int MENU_ORIGIN = 0, MENU_LOCAL = 2, MENU_YUN = 3;
    /**
     * Music????????????
     */
    private String mBGMUrl;
    /**
     * ??????videoEditActivity oncreate
     */
    private boolean mIsFirstCreate = true;

    /**
     * ???????????????????????????
     */
    private IVideoMusicEditor mHlrVideoEditor;
    /**
     * ????????????
     */
    private int mMenuIndex = 0;
    /**
     * ????????????
     */
    private float mTrailerDuration;
    /**
     * ???????????????
     */
    private String mCloudMusicUrl = null;
    private String mMusicTypeUrl = null;
    private CloudAuthorizationInfo mCloudAuthorizationInfo = null;
    private boolean isNewCloudApi = false;
    /**
     * ??????????????????
     */
    private SeekBar mFactor;
    /**
     * ????????????
     */
    private LinearLayout mLlVoiceVolume;
    private SeekBar mSbVoiceVolume;
    private int mOldVolume;
    private boolean mIsVolumeVisibility = false;

    public static MusicFragmentEx newInstance() {

        Bundle args = new Bundle();

        MusicFragmentEx fragment = new MusicFragmentEx();
        fragment.setArguments(args);
        return fragment;
    }

    public MusicFragmentEx() {
        super();
    }

    private int mVoiceLayout = UIConfiguration.VOICE_LAYOUT_1;
    /**
     * ????????????
     */
    private IMusicListener mMusicListener;
    private boolean enableLocalMusic = true;
    private boolean isHideDubbing = false;
    private boolean isNewBGMApi = false;
    private Music mInitalMusic;  // ????????????
    private int mInitalItemId;  // ????????????
    private String mInitalMusicName;
    private boolean mInitIsMute;
    private int mInitMusicFactor;

    /**
     * @param isNewBGM          ?????????????????????????????????
     * @param bgmUrl            ????????????url
     * @param cloudMusicTypeUrl ??????????????????url
     * @param cloudMusicUrl     ???????????????
     * @param isNewCloud        ???????????????????????????
     * @param enableLocalMusic  ????????????????????????
     * @param isHideDubbing     ??????????????????
     * @param info              ???????????????????????????
     */
    public void init(boolean isNewBGM, float trailerDuration, String bgmUrl, int voiceLayout, IMusicListener listener,
                     String cloudMusicTypeUrl, String cloudMusicUrl, boolean isNewCloud, boolean enableLocalMusic, boolean isHideDubbing, CloudAuthorizationInfo info) {
        isNewBGMApi = isNewBGM;
        mTrailerDuration = trailerDuration;
        mMusicListener = listener;
        mVoiceLayout = voiceLayout;
        this.enableLocalMusic = enableLocalMusic;
        this.isHideDubbing = isHideDubbing;
        mBGMUrl = TextUtils.isEmpty(bgmUrl) ? "" : bgmUrl.trim();
        this.isNewCloudApi = isNewCloud;
        mCloudMusicUrl = TextUtils.isEmpty(cloudMusicUrl) ? "" : cloudMusicUrl.trim();
        mMusicTypeUrl = TextUtils.isEmpty(cloudMusicTypeUrl) ? "" : cloudMusicTypeUrl.trim();
        mCloudAuthorizationInfo = info;
    }


    private IParamData mParamData;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        mHlrVideoEditor = (IVideoMusicEditor) context;
        mParamData = ((IParamHandler) context).getParamData();
        mMenuIndex = mParamData.getMusicIndex();
        mInitalMusicName = mParamData.getMusicName();
        lastItemId = mMenuIndex;
        mInitalItemId = mMenuIndex;
        mInitIsMute = mHlrVideoEditor.isMediaMute();
        mInitMusicFactor = mParamData.getMusicFactor();
    }


    private HorizontalListViewFresco mListView;

    /**
     * ?????????????????????music
     *
     * @param onlyMusic
     */
    private void onlyReloadMusic(boolean onlyMusic) {
        mHlrVideoEditor.reload(onlyMusic);
        mHlrVideoEditor.seekTo(0);
        mHlrVideoEditor.start();
    }

    private TextView tvFactorType;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.music_fragment, container, false);
        mFactor = $(R.id.sbFactor);
        tvFactorType = $(R.id.tvFactorType);
        tvFactorType.setText(R.string.music);
        mInitalMusic = TempVideoParams.getInstance().getMusic();
        if (mInitalMusic == null) {
            lastItemId = MENU_NONE;
            mInitalItemId = MENU_NONE;
        }
        musicName = mParamData.getMusicName();
        mFactor.setProgress(mParamData.getMusicFactor());
        mFactor.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            Music audio;

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (null != audio) {
                    mParamData.setMusicFactor(seekBar.getProgress());
                    audio.setMixFactor(mParamData.getMusicFactor());
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                audio = TempVideoParams.getInstance().getMusic();
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (null != audio && fromUser) {
                    mParamData.setMusicFactor(progress);
                    audio.setMixFactor(mParamData.getMusicFactor());
                }
            }
        });

        //??????
        mLlVoiceVolume = $(R.id.ll_voice_volume);
        if (mIsVolumeVisibility) {
            mLlVoiceVolume.setVisibility(View.VISIBLE);
        }
        mSbVoiceVolume = $(R.id.sb_voice);
        mSbVoiceVolume.setMax(100);
        mOldVolume = mParamData.getFactor();
        mSbVoiceVolume.setProgress(mParamData.getFactor());
        mSbVoiceVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                if (fromUser) {
                    mParamData.setFactor(progress);
                    mHlrVideoEditor.getEditorVideo().setOriginalMixFactor(mParamData.getFactor());
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        View btnVoice = $(R.id.btnVoice2);
        if (!isHideDubbing && mVoiceLayout == UIConfiguration.VOICE_LAYOUT_2) {
            btnVoice.setVisibility(View.VISIBLE);
            btnVoice.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (null != mMusicListener) {
                        mMusicListener.onVoiceClick(v);
                    }
                }
            });
        } else {
            btnVoice.setVisibility(View.GONE);
        }
        $(R.id.ivCancel).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                backToInit();
                if (mIsVolumeVisibility) {
                    mParamData.setFactor(mOldVolume);
                    mHlrVideoEditor.getEditorVideo().setOriginalMixFactor(mParamData.getFactor());
                }
                mHlrVideoEditor.onBack();
            }
        });
        $(R.id.ivSure).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mHlrVideoEditor.onSure();
            }
        });
        ((TextView) $(R.id.tvBottomTitle)).setText(R.string.music);
        initHandler();
        initListener();
        mListView = $(R.id.lvListView);
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
        if (requestCode == VideoEditActivity.REQUSET_MUSICEX) {
            onCheckMediaMute();
            if (resultCode == Activity.RESULT_OK) {
                AudioMusicInfo audioMusic = data
                        .getParcelableExtra(MoreMusicActivity.MUSIC_INFO);
                //??????
                mParamData.setMusicFactor(mFactor.getProgress());
                musicName = audioMusic.getName();
                if (null != mListView) {
                    mListView.setCurrentCaption(musicName);
                    if (mMenuIndex == MENU_LOCAL) {
                        mListView.setCaption(MENU_YUN, getString(R.string.music_yun));
                    } else if (mMenuIndex == MENU_YUN) {
                        mListView.setCaption(MENU_LOCAL, getString(R.string.local));
                    }
                }
                mParamData.setMusicIndex(mMenuIndex, musicName);
                lastItemId = mMenuIndex;
//                HistoryMusicCloud.getInstance().replaceMusic(audioMusic.getPath(), musicName, audioMusic.getDuration());
                Music ao = VirtualVideo.createMusic(audioMusic.getPath());
                ao.setTimeRange(Utils.ms2s(audioMusic.getStart()), Utils.ms2s(audioMusic.getEnd()));
                ao.setMixFactor(mParamData.getMusicFactor());
                onMusicChecked(ao, false);
            } else {
                if (lastItemId != MENU_ORIGIN) {// ???????????????
                    if (null != mListView && lastItemId != mMenuIndex) {
                        mListView.selectListItem(lastItemId, false);
                    }
                    lastItemId = mMenuIndex;
                    mHlrVideoEditor.reload(false);
                    mHlrVideoEditor.seekTo(0);
                    mHlrVideoEditor.start();
                }
            }
        }

    }


    /**
     * ????????????????????????
     */
    private void backToInit() {
        mFactor.setProgress(mInitMusicFactor);
        if (null == mInitalMusic) {// ??????mv ????????????
            lastItemId = MENU_NONE;
            //???????????? ??????????????????
            TempVideoParams.getInstance().setMusicObject(mInitalMusic);
            onlyReloadMusic(true);
        } else {
            onMusicChecked(mInitalMusic, true);
        }
        mParamData.setMusicIndex(mInitalItemId, mInitalMusicName);
        mListView.selectListItem(mInitalItemId, mInitalItemId > MENU_YUN);
        if (mInitIsMute != mHlrVideoEditor.isMediaMute()) {
            onSelectedImp(MENU_ORIGIN, true);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (null != mDownloading) {
            DownLoadUtils.forceCancelAll();
            mDownloading.clear();
        }
        mHanlder = null;
        listener = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mIsFirstCreate = true;
    }


    public interface IMusicListener {
        /**
         * ?????????/???
         *
         * @param isChecked
         */
        void onVoiceChanged(boolean isChecked);

        /**
         * ????????????
         */
        void onVoiceClick(View v);

    }

    private OnListViewItemSelectListener listener = null;

    private void initListener() {
        listener = new OnListViewItemSelectListener() {

            @Override
            public void onSelected(View view, int nItemId, boolean user) {
                onSelectedImp(nItemId, user);
            }

            @Override
            public boolean onBeforeSelect(View view, int nItemId) {
                return false;
            }
        };
    }

    /**
     * ??????????????????????????????
     */
    private void onMusicLocal() {
        HistoryMusicCloud.getInstance().initilize(mContext);
        MoreMusicActivity.onLocalMusic(getActivity(), VideoEditActivity.REQUSET_MUSICEX);
    }

    /**
     * ???????????????????????????
     */
    private void onMusicYUN() {
        HistoryMusicCloud.getInstance().initilize(mContext);
        MoreMusicActivity.onYunMusic(getActivity(), isNewCloudApi, mMusicTypeUrl, mCloudMusicUrl, mCloudAuthorizationInfo);
    }

    /**
     * ??????????????????
     */
    private void onMusicChecked(Music ao, boolean onlyMusic) {
        mHlrVideoEditor.removeMvMusic(true);
        try {
            if (mTrailerDuration > 0) {
                ao.setFadeInOut(mTrailerDuration, mTrailerDuration);
            } else {
                ao.setFadeInOut(Utils.ms2s(800), Utils.ms2s(800));
            }
            TempVideoParams.getInstance().setMusicObject(ao);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("onclick", "audio not exists");
        }

        onlyReloadMusic(onlyMusic);
    }

    private boolean isDoing = false;

    /**
     * ????????????????????????
     */
    private void onSelectedImp(int nItemId, boolean user) {
        mMenuIndex = nItemId;
        mFactor.setEnabled(true);
        if (nItemId == MENU_ORIGIN) {
            if (user) {
                if (!isDoing) {
                    isDoing = true;
                    boolean isOriginMute = mHlrVideoEditor.isMediaMute();
                    mListView.setItemSrc(nItemId, isOriginMute ? R.drawable.video_origin_n : R.drawable.video_origin_p, isOriginMute ? R.string.video_voice_n : R.string.video_voice_p);
                    if (null != mMusicListener) {
                        mMusicListener.onVoiceChanged(isOriginMute);
                    }
                    //??????????????????
                    mHanlder.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            isDoing = false;
                        }
                    }, 500);
                }
            }
        } else if (nItemId == MENU_NONE) {
            mParamData.setMusicIndex(nItemId, "");
            if (user) {
                try {
                    mListView.setCaption(MENU_LOCAL, getString(R.string.local));
                    mListView.setCaption(MENU_YUN, getString(R.string.music_yun));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mFactor.setProgress(50);
                mFactor.setEnabled(false);
                TempVideoParams.getInstance().recycleMusicObject();
                onlyReloadMusic(true);
                mListView.onItemChecked(nItemId);
                if (mIsFirstCreate) {
                    mIsFirstCreate = false;// ???????????????fragment
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
                onCheckMediaMute();
                IApiInfo info = mlist.get(nItemId - (MENU_YUN + 1));
                if (info.existsMusic()) {
                    try {
                        mListView.setCaption(MENU_LOCAL, getString(R.string.local));
                        mListView.setCaption(MENU_YUN, getString(R.string.music_yun));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    mParamData.setMusicIndex(nItemId, info.getName());
                    Music ao = VirtualVideo.createMusic(info.getLocalPath());
                    ao.setEnableRepeat(true);
                    ao.setMixFactor(mParamData.getMusicFactor());
                    onMusicChecked(ao, true);
                    lastItemId = nItemId;
                } else {
                    if (CoreUtils.checkNetworkInfo(mContext) == CoreUtils.UNCONNECTED) {
                        int temp = nItemId;
                        mListView.resetItem(temp);
                        mListView.selectListItem(lastItemId, true);
                        onToast(R.string.please_open_wifi);
                    } else { // ??????
                        downMusic(nItemId, info);
                    }
                }
            }
        }
    }

    /**
     * ????????????????????????seekbar????????????
     */
    private void onCheckMediaMute() {
        //????????????????????????
        if (null != mFactor) {
            mFactor.setEnabled(true);
        }
    }

    private ArrayList<Long> mDownloading = null;

    /**
     * ????????????
     */
    private void downMusic(int itemId, final IApiInfo info) {
        if (null == mDownloading) {
            mDownloading = new ArrayList<Long>();
        }
        if (!mDownloading.contains((long) itemId)) {

            String local = info.getLocalPath();
            final DownLoadUtils download = new DownLoadUtils(getContext(), itemId,
                    info.getUrl(), local);
            download.setConfig(0, 50, 100);
            download.DownFile(new IDownListener() {

                @Override
                public void onFailed(long mid, int code) {
                    Log.e(TAG, "onFailed: " + mid + ">>" + code);
                    if (isRunning) {
                        if (null != mHanlder) {
                            mHanlder.obtainMessage(MSG_WEB_DOWN_FAILED, (int) mid,
                                    0).sendToTarget();

                        }
                    }
                    if (null != mDownloading) {
                        mDownloading.remove((long) mid);
                    }
                }

                @Override
                public void onProgress(long mid, int progress) {
                    if (null != mHanlder) {
                        mHanlder.obtainMessage(MSG_WEB_DOWNLOADING, (int) mid,
                                progress).sendToTarget();
                    }
                }

                @Override
                public void Finished(long mid, String localPath) {
                    LogUtil.i(TAG, "Finished : " + mid + " >" + localPath);
                    if (isRunning) {
                        if (null != mHanlder) {
                            mHanlder.obtainMessage(MSG_WEB_DOWN_END, (int) mid, 0)
                                    .sendToTarget();
                        }

                    }
                    if (null != mDownloading) {
                        mDownloading.remove((long) mid);
                    }

                }

                @Override
                public void Canceled(long mid) {
                    Log.e(TAG, "Canceled: " + mid);
                    if (isRunning) {
                        if (null != mHanlder) {
                            mHanlder.obtainMessage(MSG_WEB_DOWN_FAILED, (int) mid,
                                    0).sendToTarget();
                        }
                    }
                    if (null != mDownloading) {
                        mDownloading.remove((long) mid);
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
     * ???????????????JSON
     */
    private void getWebMusic() {

        if (null != mHanlder) {
            mHanlder.obtainMessage(MSG_LOCAL_MENU).sendToTarget();
        }
        ThreadPoolUtils.execute(new Runnable() {

            @Override
            public void run() {
                if (isNewBGMApi) {
                    //????????????????????????????????????
                    getNewBgMusic();
                } else {
                    //????????????????????????
                    getBgMusic();
                }
                if (null != mHanlder) {
                    mHanlder.obtainMessage(MSG_WEB_PREPARED).sendToTarget();
                }
            }
        });
    }

    /**
     * ????????????
     */
    private void getNewBgMusic() {
        if (TextUtils.isEmpty(mBGMUrl)) {
            Log.i(TAG, "mBGMUrl is null " + isNewBGMApi);
        } else {
            File cacheDir = mContext.getCacheDir();
            File f = new File(cacheDir, MD5.getMD5("rd_new_bgMusic.url"));
            if (!bLoadWebDataSuccessed) {
                int netState = CoreUtils.checkNetworkInfo(mContext);
                if (netState == CoreUtils.UNCONNECTED) {
                    String offline = null;
                    if (null != f && f.exists() && !TextUtils.isEmpty(offline = FileUtils.readTxtFile(f
                            .getAbsolutePath()))) {// ??????????????????
                        try {
                            offline = URLDecoder.decode(offline, "UTF-8");
                            if (!TextUtils.isEmpty(offline)) {
                                onParseJson2(offline);
                            }
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    } else {
                        onToast(R.string.please_open_wifi);
                    }
                } else {
                    String str = ModeDataUtils.getModeData(mBGMUrl, ModeDataUtils.TYPE_MUSIC);
                    if (!TextUtils.isEmpty(str)) {// ??????????????????
                        onParseJson2(str);
                        try {
                            String data = URLEncoder.encode(str, "UTF-8");
                            FileUtils.writeText2File(data,
                                    f.getAbsolutePath());
                            bLoadWebDataSuccessed = true;
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

    }

    /**
     * ????????????????????????????????????
     */
    @Deprecated
    private void getBgMusic() {
        if (TextUtils.isEmpty(mBGMUrl)) {
            Log.i(TAG, "mBGMUrl is null");
        } else {
            File cacheDir = mContext.getCacheDir();
            File f = new File(cacheDir, MD5.getMD5("music_data.json"));
            if (!bLoadWebDataSuccessed) {
                int netState = CoreUtils.checkNetworkInfo(mContext);
                if (netState == CoreUtils.UNCONNECTED) {
                    String offline = null;
                    if (null != f && f.exists() && !TextUtils.isEmpty(offline = FileUtils.readTxtFile(f
                            .getAbsolutePath()))) {// ??????????????????
                        try {
                            offline = URLDecoder.decode(offline, "UTF-8");
                            if (!TextUtils.isEmpty(offline)) {
                                onParseJson(offline);
                            }
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    } else {
                        onToast(R.string.please_open_wifi);
                    }
                } else {
                    String str = RdHttpClient.PostJson(mBGMUrl,
                            new NameValuePair("type", "android"));
                    if (!TextUtils.isEmpty(str)) {// ??????????????????
                        onParseJson(str);
                        try {
                            String data = URLEncoder.encode(str, "UTF-8");
                            FileUtils.writeText2File(data,
                                    f.getAbsolutePath());
                            bLoadWebDataSuccessed = true;
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

    }


    /**
     * ????????????
     */
    private ArrayList<IApiInfo> mlist = new ArrayList<IApiInfo>();

    /**
     * ???????????????MUSIC???json????????????????????????mlist
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
                    for (int i = 0; i < len; i++) {
                        jt = jarr.getJSONObject(i);
                        if (null != jt) {
                            String url = jt.getString("url");
                            String name = jt.getString("name");
                            String localPath = (PathUtils.getRdMusic() + "/"
                                    + MD5.getMD5(url) + ".mp3").trim();
                            mlist.add(new IApiInfo(name, url, jt.getString("icon"), localPath, 0));

                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    /**
     * ???????????????MUSIC???json????????????????????????mlist
     */
    private void onParseJson2(String data) {
        try {
            JSONObject jobj = new JSONObject(data);
            if (null != jobj && jobj.optInt("code", -1) == 0) {
                JSONArray jarr = jobj.optJSONArray("data");
                if (null != jarr) {
                    int len = jarr.length();
                    JSONObject jt;
                    mlist.clear();
                    //?????????????????????????????????
                    String[] fs = new File(PathUtils.getRdMusic()).list(new FilenameFilter() {
                        @Override
                        public boolean accept(File file, String s) {
                            if (s.endsWith(".mp3")) {
                                return true;
                            }
                            return false;
                        }
                    });
                    for (int i = 0; i < len; i++) {
                        jt = jarr.getJSONObject(i);
                        if (null != jt) {
                            String url = jt.getString("file");
                            String name = jt.getString("name");
                            long updatetime = jt.optLong("updatetime");

                            String md5 = MD5.getMD5(url) + "_";
                            //????????????????????????????????????
                            deleteLastConstainFile(md5, fs, updatetime);
                            String localPath = (PathUtils.getRdMusic() + "/"
                                    + md5 + updatetime + ".mp3").trim();
                            mlist.add(new IApiInfo(name, url, jt.getString("cover"), localPath, updatetime));


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
     * ?????????????????????
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
                            boolean isMute = mHlrVideoEditor.isMediaMute();
                            if (isMute) {
                                mListView.addListItem(nItemId, R.drawable.video_origin_p,
                                        getString(R.string.video_voice_p));
                            } else {
                                mListView.addListItem(nItemId, R.drawable.video_origin_n,
                                        getString(R.string.video_voice_n));
                            }
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
                                IApiInfo info = mlist.get(i);
                                mListView.addListItem(nItemId, info.getCover(),
                                        info.getName());
                                mListView.setDownLayout(nItemId, info.existsMusic());
                                nItemId++;
                            }

                            Music audio = TempVideoParams.getInstance().getMusic();
                            if (null == audio) {// ??????mv ????????????
                                lastItemId = MENU_NONE;
                            }
                            mListView.selectListItem(lastItemId, lastItemId > MENU_YUN);
                            if (lastItemId == MENU_LOCAL || lastItemId == MENU_YUN) {
                                if (!TextUtils.isEmpty(musicName)) {
                                    //????????????????????????????????????????????????
                                    mListView.setCurrentCaption(musicName);
                                }
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
                            if (mParamData.getMusicIndex() == id) {
                                mListView.resetItem(id);
                            } else {
                                mListView.selectListItem(id, true);
                            }
                        }

                    }
                    break;
                    case MSG_WEB_DOWN_FAILED: {
                        int id = msg.arg1;
                        if (null != mListView) {
                            if (isRunning) {
                                onToast(R.string.please_open_wifi);
                            }
                            mListView.setdownFailedUI(id);
                            mListView.selectListItem(lastItemId, true);
                        }
                    }
                    break;
                    default:
                        break;
                }
            }
        };
    }


    /**
     * ???????????????????????????????????????????????????????????????
     *
     * @param lastMusic  ????????????????????????????????????
     * @param fs
     * @param updateTime ?????????????????????????????????
     * @return ?????????????????????
     */
    private boolean deleteLastConstainFile(String lastMusic, String[] fs, long updateTime) {
        boolean result = false;
        if (null != fs) {
            int len = fs.length;
            for (int i = 0; i < len; i++) {
                String path = fs[i];
                if (path.contains(lastMusic)) {
                    if (!path.contains(Long.toString(updateTime))) {
                        //??????????????????????????????????????????
                        result = true;
                        FileUtils.deleteAll(path);
                    }
                    break;
                }
            }

        }
        return result;

    }

    /**
     * ????????????
     */
    public void checkEnableVolume() {
        mIsVolumeVisibility = true;
    }

}
