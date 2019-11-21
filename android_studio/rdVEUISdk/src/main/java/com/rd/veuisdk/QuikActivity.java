package com.rd.veuisdk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.rd.lib.ui.ExtButton;
import com.rd.lib.ui.PreviewFrameLayout;
import com.rd.lib.ui.RotateRelativeLayout;
import com.rd.lib.utils.CoreUtils;
import com.rd.lib.utils.FileUtils;
import com.rd.lib.utils.ThreadPoolUtils;
import com.rd.vecore.Music;
import com.rd.vecore.VirtualVideo;
import com.rd.vecore.VirtualVideoView;
import com.rd.vecore.exception.InvalidArgumentException;
import com.rd.vecore.exception.InvalidStateException;
import com.rd.vecore.models.AEFragmentInfo;
import com.rd.vecore.models.BlendEffectObject;
import com.rd.vecore.models.MediaObject;
import com.rd.vecore.models.MediaType;
import com.rd.vecore.models.Scene;
import com.rd.vecore.models.VisualFilterConfig;
import com.rd.vecore.models.caption.CaptionObject;
import com.rd.vecore.utils.AEFragmentUtils;
import com.rd.vecore.utils.Log;
import com.rd.vecore.utils.MiscUtils;
import com.rd.veuisdk.ae.AETemplateUtils;
import com.rd.veuisdk.ae.model.AETemplateInfo;
import com.rd.veuisdk.ae.model.AETextLayerInfo;
import com.rd.veuisdk.database.FilterData;
import com.rd.veuisdk.database.SubData;
import com.rd.veuisdk.database.TTFData;
import com.rd.veuisdk.fragment.BaseFragment;
import com.rd.veuisdk.fragment.FilterFragment;
import com.rd.veuisdk.fragment.FilterFragmentLookup;
import com.rd.veuisdk.fragment.FilterFragmentLookupBase;
import com.rd.veuisdk.fragment.FilterFragmentLookupLocal;
import com.rd.veuisdk.fragment.QuikFragment;
import com.rd.veuisdk.fragment.QuikSettingFragment;
import com.rd.veuisdk.fragment.SubtitleFragment;
import com.rd.veuisdk.listener.IFixPreviewListener;
import com.rd.veuisdk.manager.ExportConfiguration;
import com.rd.veuisdk.manager.UIConfiguration;
import com.rd.veuisdk.model.AETextMediaInfo;
import com.rd.veuisdk.net.TTFUtils;
import com.rd.veuisdk.quik.Jolly;
import com.rd.veuisdk.quik.QuikHandler;
import com.rd.veuisdk.ui.RdSeekBar;
import com.rd.veuisdk.utils.BitmapUtils;
import com.rd.veuisdk.utils.CommonStyleUtils;
import com.rd.veuisdk.utils.DateTimeUtils;
import com.rd.veuisdk.utils.FileLog;
import com.rd.veuisdk.utils.IntentConstants;
import com.rd.veuisdk.utils.PathUtils;
import com.rd.veuisdk.utils.SysAlertDialog;
import com.rd.veuisdk.utils.Utils;
import com.rd.veuisdk.utils.ViewUtils;

import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 仿Quik功能
 * 添加片头   第0个场景添加AEFrgement即可
 * 例如：
 * Scene scene = VirtualVideo.createScene();
 * scene.addAEFragment(aeFragmentInfo, 0, maxTrim);
 * virtualVideo.addScene(scene);
 */
public class QuikActivity extends BaseActivity implements IVideoEditorQuikHandler {
    private QuikHandler.EffectInfo mEffectInfo;
    private String TAG = "QuikActivity";
    private final int mBGColor = Color.WHITE;
    private PreviewFrameLayout mPreviewFrame;
    private ExtButton mBtnNext, mBtnLeft;
    private TextView mTvTitle;
    private VirtualVideoView player;
    private ImageView mIvVideoPlayState;
    private RdSeekBar mRdSeekBar;
    private TextView currentTv;
    private TextView totalTv;
    private RotateRelativeLayout mProgressLayout;
    private FrameLayout mLinearWords;
    private List<MediaObject> mArrImage = new ArrayList<>();
    private float mCurProportion;
    private QuikHandler quikHandler;
    private RadioGroup mEditorMenuGroups;
    private UIConfiguration mUIConfig;

    //字幕资源
    private View mTextLayerBtn;

    private VirtualVideo mVirtualVideo;
    private String snapPath = null;
    private boolean isExport = false;
    /**
     * Action需要第0个媒体补充
     */
    private Runnable mSnapRunnable = new Runnable() {
        @Override
        public void run() {
            MediaObject shadow = mArrImage.get(0);
            if (shadow.getMediaType() == MediaType.MEDIA_VIDEO_TYPE) {
                //视频截取第1帧图
                Rect rect = new Rect();
                MiscUtils.fixZoomTarget(shadow.getWidth(), shadow.getHeight(), rect, 300);
                Bitmap bmp = Bitmap.createBitmap(rect.width(), rect.height(), Bitmap.Config.ARGB_8888);
                VirtualVideo snap = new VirtualVideo();
                Scene scene = VirtualVideo.createScene();
                scene.addMedia(shadow);
                snap.addScene(scene);
                if (snap.getSnapshot(QuikActivity.this, Math.min(1f, shadow.getIntrinsicDuration() / 10), bmp)) {
                    snapPath = PathUtils.getTempFileNameForSdcard("Temp", "snap_mp4" + ".png");
                    BitmapUtils.saveBitmapToFile(bmp, snapPath, true);
                }
                if (null != bmp) {
                    bmp.recycle();
                }
                snap.release();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUserTextList.clear();
        mUIConfig = SdkEntry.getSdkService().getUIConfig();
        mExportConfig = SdkEntry.getSdkService().getExportConfig();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_quik_layout);
        mTextLayerBtn = findViewById(R.id.editTextLayerBtn);
        mTextLayerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onTextLayerClick();
            }
        });
        List<Scene> listScene = getIntent().getParcelableArrayListExtra(IntentConstants.INTENT_EXTRA_SCENE);
        quikHandler = new QuikHandler();
        ThreadPoolUtils.executeEx(new Runnable() {
            @Override
            public void run() {
                quikHandler.init(QuikActivity.this);
                mHandler.post(mRunnable);
            }
        });
        SysAlertDialog.showLoadingDialog(QuikActivity.this, getString(R.string.isloading));
        IntentFilter inFilter = new IntentFilter();
        inFilter.addAction(SdkEntry.MSG_EXPORT);
        registerReceiver(mReceiver, inFilter);

        for (Scene scene : listScene) {
            mArrImage.add(scene.getAllMedia().get(0));
        }
        snapPath = null;
        ThreadPoolUtils.executeEx(mSnapRunnable);

        FilterData.getInstance().initilize(this);
        SubData.getInstance().initilize(this);//字幕初始化数据库
        TTFData.getInstance().initilize(this);//字体初始化数据库
        initView();
        mTvTitle.setText(R.string.temp);
        mCurProportion = QuikHandler.ASP_169;
        mPreviewFrame.setAspectRatio(mCurProportion);
        player.setPreviewAspectRatio(mCurProportion);
        player.setAutoRepeat(true);
        mVirtualVideo = new VirtualVideo();

        player.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (!bInterceptRepeat) {
                    //防止重复点击
                    bInterceptRepeat = true;
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            bInterceptRepeat = false;
                        }
                    }, 500);

                    if (isPlayingORecording) {
                        pause();
                    } else {
                        start();
                    }
                }
            }
        });
        initPlayerListener(player);
    }

    private void onTextLayerClick() {
        pause();
        if (null != mAETemplateInfo) {
            List<AETextLayerInfo> tmp = mAETemplateInfo.getAETextLayerInfos();
            if (null != tmp && tmp.size() >= 1) {
                //编辑ae模板文字
                AEFragmentInfo info = mAETemplateInfo.getAEFragmentInfo();

                List<AEFragmentInfo.LayerInfo> layerInfos = info.getLayers();

                int len = layerInfos.size();
                //例如:Boxed
                ArrayList<AETextMediaInfo> aeQuikTextMediaInfos = new ArrayList<>();

                for (int i = 0; i < len; i++) {
                    AETextMediaInfo item = mEffectInfo.getAETextMediaList().get(i);
                    AETextLayerInfo aeTextLayerInfo = item.getAETextLayerInfo();
                    if (null != aeTextLayerInfo) {
                        aeQuikTextMediaInfos.add(item);
                    }
                }
                if ((aeQuikTextMediaInfos.size() > 1)) {
                    //例如Boxed 一个输入框，支持多个可替换文字的layer
                    ArrayList<String> txtList = new ArrayList<>();
                    if (mUserTextList.size() > 1) {
                        //多行文字
                        txtList.addAll(mUserTextList);
                    } else if (mUserTextList.size() == 1) {
                        //当行文字（在非boxed模板中编辑的文字（可能出现一个layer对应多行文本），到了boxed时，得拆分成一行对应一个layer）
                        String[] arr = mUserTextList.get(0).split("\n");
                        if (null != arr) {
                            int count = arr.length;
                            for (int i = 0; i < count; i++) {
                                txtList.add(arr[i]);
                            }
                        }
                    }
                    AETextActivity.onAEText(this, aeQuikTextMediaInfos, txtList, REQUESTCODE_FOR_AETEXT);
                } else {
                    if (aeQuikTextMediaInfos.size() == 1) {
                        AETextMediaInfo textMediaInfo = aeQuikTextMediaInfos.get(0);
                        //没有文本就编辑默认文本
                        String text = null;
                        if (mUserTextList.size() > 0) {
                            text = userTextListToString();
                        }
                        if (TextUtils.isEmpty(text)) {
                            text = textMediaInfo.getAETextLayerInfo().getTextContent();
                        }
                        AETextActivity.onAEText(this, textMediaInfo.getAETextLayerInfo(), text, textMediaInfo.getTtfIndex(), textMediaInfo.getTtf(), REQUESTCODE_FOR_AETEXT);
                    } else {
                        //未知情况
                    }
                }


            }

        }

    }

    private final int REQUESTCODE_FOR_AETEXT = 265;


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        lastProgress = -1;
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUESTCODE_FOR_AETEXT) {
                mUserTextList.clear();
                AETextActivity.AEText aeText = AETextActivity.getAEText(data);
                if (null != aeText) {
                    mUserTextList.add(aeText.getText());
                    List<AETextMediaInfo> list = mEffectInfo.getAETextMediaList();
                    int len = 0;
                    if (null != list && (len = list.size()) > 0) {
                        for (int i = 0; i < len; i++) {
                            AETextMediaInfo mAETextMediaInfo = list.get(i);
                            AETextLayerInfo aeTextLayerInfo = mAETextMediaInfo.getAETextLayerInfo();

                            if (null != aeTextLayerInfo) {
                                mAETextMediaInfo.setTtf(aeText.getTtf(), aeText.getTtfIndex());
                                String file = AETextActivity.fixAEText(aeTextLayerInfo, aeText.getText(), mAETextMediaInfo.getTtf());
                                try {
                                    mAETextMediaInfo.setTextMediaObj(new MediaObject(file));
                                } catch (InvalidArgumentException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        mEffectInfo.setAETextMediaList(list);
                    }
                } else {
                    //boxed
                    List<AETextActivity.AEText> aeTextList = AETextActivity.getAETextList(data);
                    if (null != aeTextList) {
                        List<AETextMediaInfo> list = mEffectInfo.getAETextMediaList();
                        int len = 0;
                        if (null != list && (len = list.size()) > 0) {
                            for (int i = 0; i < len; i++) {
                                AETextMediaInfo mAETextMediaInfo = list.get(i);
                                AETextLayerInfo aeTextLayerInfo = mAETextMediaInfo.getAETextLayerInfo();
                                aeText = aeTextList.get(i);
                                if (aeText == null) {
                                    //写字板的内容不足， 不再显示没有内容的layer
                                    if (null != aeTextLayerInfo) {
                                        mAETextMediaInfo.setTtf("", 0);
                                        mAETextMediaInfo.setTextMediaObj(null);
                                    }
                                } else {
                                    mUserTextList.add(aeText.getText());
                                    if (null != aeTextLayerInfo) {
                                        mAETextMediaInfo.setTtf(aeText.getTtf(), aeText.getTtfIndex());
                                        String file = AETextActivity.fixAEText(aeTextLayerInfo, aeText.getText(), mAETextMediaInfo.getTtf());
                                        try {
                                            mAETextMediaInfo.setTextMediaObj(new MediaObject(file));
                                        } catch (InvalidArgumentException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                }
                            }
                            mEffectInfo.setAETextMediaList(list);
                        }
                    }
                }
                updateQuikItem(mEffectInfo);
                build(true);
            }
        } else {
            build(true);
        }
    }

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            onQuikClick(false);
        }
    };


    /**
     * 字幕、特效导出按钮
     */
    private void onResultWord() {
        stop();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //字幕对象直接包含再WordInfo.getCaptionObject()中
        mHandler.sendEmptyMessage(RESULT_STYLE);

    }

    private final int RESULT_STYLE = 55;


    @Override
    public VirtualVideoView getEditor() {
        return player;
    }

    @Override
    public VirtualVideo getEditorVideo() {
        return mVirtualVideo;
    }

    @Override
    public FrameLayout getSubEditorParent() {
        return mLinearWords;
    }

    private VirtualVideo mSnapshotEditor;
    private QuikHandler.EffectInfo mEffectSnap;
    private float mProportionSnap;

    @Override
    public VirtualVideo getSnapshotEditor() {
        //截图与预览的虚拟视频需分开
        if (mEffectSnap == mEffectInfo && mProportionSnap == mCurProportion) {
            //效果和比例均没有变化，不需要再次创建缩略图对象，否则需要创建

        } else {
            mProportionSnap = mCurProportion;
            mEffectSnap = mEffectInfo;
            //创建新的缩略图对象
            if (null != mSnapshotEditor) {
                mSnapshotEditor.release();
            }
            mSnapshotEditor = new VirtualVideo();
            reload(mSnapshotEditor);
            mSnapshotEditor.setPreviewAspectRatio(mProportionSnap);
        }
        return mSnapshotEditor;
    }

    @Override
    public void reload(boolean bOnlyAudio) {
        build(true);
    }

    @Override
    public void cancelLoading() {
        SysAlertDialog.cancelLoadingDialog();
    }

    @Override
    public void start() {
        isPlayingORecording = true;
        player.start();
        mIvVideoPlayState.setImageResource(R.drawable.btn_pause);
        ViewUtils.fadeOut(QuikActivity.this, mIvVideoPlayState);
        mTextLayerBtn.setVisibility(View.GONE);
    }

    /**
     * 暂停播放
     */
    @Override
    public void pause() {
        player.pause();
        int checkedId = mEditorMenuGroups.getCheckedRadioButtonId();
        if (checkedId == R.id.rb_word) {
            if (null != mSubtitleFragment) {
                mSubtitleFragment.setImage(R.drawable.edit_music_play);
            }
            mTextLayerBtn.setVisibility(View.GONE);
        } else {
            mIvVideoPlayState.clearAnimation();
            mIvVideoPlayState.setImageResource(R.drawable.btn_play);
            mIvVideoPlayState.setVisibility(View.VISIBLE);
            isPlayingORecording = false;
            if (null != mEffectInfo && mEffectInfo.mQuikTemplate != null) {
                mTextLayerBtn.setVisibility(checkedId != R.id.rb_filter ? View.VISIBLE : View.GONE);
            }
        }


    }

    @Override
    public void seekTo(int msec) {
        player.seekTo(Utils.ms2s(msec));
        onSeekTo(msec);
    }

    @Override
    public void stop() {
        player.stop();
        onSeekTo(0);
        int rbId = mEditorMenuGroups.getCheckedRadioButtonId();
        if (rbId == R.id.rb_word) {
            if (null != mSubtitleFragment) {
                mSubtitleFragment.setImage(R.drawable.edit_music_play);
            }
        } else {
            mIvVideoPlayState.clearAnimation();
            mIvVideoPlayState.setImageResource(R.drawable.btn_play);
            mIvVideoPlayState.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean isPlaying() {
        return player.isPlaying();
    }

    @Override
    public int getDuration() {
        return Utils.s2ms(player.getDuration());
    }

    @Override
    public int getCurrentPosition() {
        return Utils.s2ms(player.getCurrentPosition());
    }


    @Override
    public void changeAnimation(int animation) {

    }

    @Override
    public void setAETemplateInfo(AETemplateInfo aeTemplateInfo) {

    }

    @Override
    public void changeFilterType(int index, int nFilterType) {
        if (player != null) {
            if (!player.isPlaying()) {
                start();
            }
            mCurrentFilterType = nFilterType;
            mVirtualVideo.changeFilter(nFilterType);
        }
    }

    private int mCurrentFilterType = 0;


    private VisualFilterConfig lookupConfig;
    private int lookupIndex = 0;

    @Override
    public void changeFilterLookup(VisualFilterConfig lookup, int index) {
        lookupIndex = index;
        if (player != null) {
            if (!player.isPlaying()) {
                start();
            }
            try {
                mVirtualVideo.changeFilter(lookup);
            } catch (InvalidArgumentException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int getCurrentLookupIndex() {
        return lookupIndex;
    }

    /*
     * 盛放EditorPreivewPositionListener的列表
     */
    private SparseArray<EditorPreivewPositionListener> mSaEditorPostionListener = new SparseArray<EditorPreivewPositionListener>();

    @Override
    public void registerEditorPostionListener(
            EditorPreivewPositionListener listener) {
        mSaEditorPostionListener.append(listener.hashCode(), listener);
    }

    @Override
    public void unregisterEditorProgressListener(
            EditorPreivewPositionListener listener) {
        mSaEditorPostionListener.remove(listener.hashCode());
    }

    private void notifyCurrentPosition(int positionMs) {
        int du = Utils.s2ms(player.getDuration());
        for (int nTmp = 0; nTmp < mSaEditorPostionListener.size(); nTmp++) {
            mSaEditorPostionListener.valueAt(nTmp).onEditorGetPosition(positionMs, du);
        }
    }

    @Override
    public boolean isMediaMute() {
        return false;
    }

    @Override
    public void removeMvMusic(boolean remove) {

    }

    @Override
    public void onProportionChanged(float aspect) {

    }

    @Override
    public void onBackgroundModeChanged(boolean isEnableBg) {

    }

    @Override
    public void onBackgroundColorChanged(int color) {

    }

    @Override
    public void onBack() {
        int checkId = mEditorMenuGroups.getCheckedRadioButtonId();
        if (checkId == R.id.rb_word) {
            onResultWord();
        } else {
            onSure();
        }
    }

    @Override
    public void onSure() {
        // 当前选择功能项id
        int checkId = mEditorMenuGroups.getCheckedRadioButtonId();
        if (checkId == R.id.rb_word) {
            onResultWord();
            return;
        } else if (checkId == R.id.rb_filter) {
            if (null != mFilterFragmentLookup) {
                lookupConfig = mFilterFragmentLookup.getLookup();
            }
            if (null != mFilterFragment) {
                mCurrentFilterType = mFilterFragment.getFilterId();
            }
        }
        returnToMenuLastSelection();
    }


    private void initView() {
        mLinearWords = (FrameLayout) findViewById(R.id.linear_words);
        mEditorMenuGroups = (RadioGroup) findViewById(R.id.edit_groups);
        findViewById(R.id.rb_quik).setOnClickListener(onVerVideoMenuListener);
        findViewById(R.id.rb_word).setOnClickListener(onVerVideoMenuListener);
        findViewById(R.id.rb_filter).setOnClickListener(onVerVideoMenuListener);
        findViewById(R.id.rb_setting).setOnClickListener(onVerVideoMenuListener);


        mProgressLayout = (RotateRelativeLayout) findViewById(R.id.rlPlayerBottomMenu);
        mRdSeekBar = (RdSeekBar) findViewById(R.id.sbEditor);
        currentTv = (TextView) findViewById(R.id.tvCurTime);
        totalTv = (TextView) findViewById(R.id.tvTotalTime);
        mPreviewFrame = (PreviewFrameLayout) findViewById(R.id.previewFrame);
        mBtnNext = (ExtButton) findViewById(R.id.btnRight);
        mBtnNext.setTextColor(getResources().getColor(R.color.main_orange));
        mBtnLeft = (ExtButton) findViewById(R.id.btnLeft);
        mTvTitle = (TextView) findViewById(R.id.tvTitle);
        player = (VirtualVideoView) findViewById(R.id.player);
        mIvVideoPlayState = (ImageView) findViewById(R.id.ivPlayerState);
        mBtnNext.setVisibility(View.VISIBLE);
        mBtnNext.setText(R.string.export);

        mBtnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onRightButtonClick();
            }
        });

        mBtnLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        mRdSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                if (fromUser) {
                    float p = Utils.ms2s(progress);
                    player.seekTo(p);
                    currentTv.setText(getFormatTime(progress));
                }
            }

            private boolean isPlaying = false;

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

                if ((isPlaying = player.isPlaying())) {
                    isPlaying = true;
                    player.pause();
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (isPlaying) {
                    player.start();
                }
            }
        });

    }

    private QuikFragment mQuikFragment;

    /**
     * 更新数据
     *
     * @param effectInfo
     */
    private void updateQuikItem(QuikHandler.EffectInfo effectInfo) {
        quikHandler.updateItem(effectInfo);
        if (null != mQuikFragment) {
            mQuikFragment.updateItem();
        }
    }

    /**
     * @param enableAnimation
     */
    private void onQuikClick(boolean enableAnimation) {
        mTvTitle.setText(R.string.temp);
        setViewVisibility(R.id.titlebar_layout, true);
        resetTitlebar();
        mEditorMenuGroups.check(R.id.rb_quik);
        if (null == mQuikFragment) {
            mQuikFragment = QuikFragment.newInstance();
        }
        mProgressLayout.setVisibility(View.VISIBLE);
        changeToFragment(mQuikFragment, enableAnimation, true);

    }


    private SubtitleFragment mSubtitleFragment;
    private FilterFragmentLookupBase mFilterFragmentLookup;
    private FilterFragment mFilterFragment;
    private View.OnClickListener onVerVideoMenuListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            int checkedId = v.getId();
            if (checkedId == R.id.rb_quik) {
                onQuikClick(true);
                reload(false);
            } else if (checkedId == R.id.rb_word) {
                setViewVisibility(R.id.titlebar_layout, false);
                mTvTitle.setText(R.string.subtitle);
                player.setAutoRepeat(false); // 字幕不需要自动重播
                stop();
                mIvVideoPlayState.setVisibility(View.GONE);
                mProgressLayout.setVisibility(View.GONE);
                if (null == mSubtitleFragment) {
                    mSubtitleFragment = SubtitleFragment.newInstance(mUIConfig.subUrl, mUIConfig.fontUrl);
                }
                mSubtitleFragment.setHideAI();
                mSubtitleFragment.setExtractAudio(new SubtitleFragment.IExtractAudio() {
                    @Override
                    public List<Scene> getAudioSceneList() {
                        //被拆分成很多多，不需要保留原音
                        return null;
                    }
                });
                mSubtitleFragment.setExMode(true);
                changeToFragment(mSubtitleFragment.setFragmentContainer(findViewById(R.id.rlEditorMenuAndSubLayout)), true, false);
            } else if (checkedId == R.id.rb_filter) {
                setViewVisibility(R.id.titlebar_layout, false);
                mTvTitle.setText(R.string.filter);
                if (!TextUtils.isEmpty(mUIConfig.filterUrl)) {
                    //网络lookup滤镜
                    if (null == mFilterFragmentLookup) {
                        mFilterFragmentLookup = FilterFragmentLookup.newInstance(mUIConfig.filterUrl);
                    }
                    changeToFragment(mFilterFragmentLookup, false);
                } else if (mUIConfig.filterLayoutTpye == UIConfiguration.FILTER_LAYOUT_3) {
                    //本地lookup
                    if (null == mFilterFragmentLookup) {
                        mFilterFragmentLookup = FilterFragmentLookupLocal.newInstance();
                    }
                    changeToFragment(mFilterFragmentLookup, false);
                } else {
                    if (mFilterFragment == null) {
                        mFilterFragment = FilterFragment.newInstance();
                    }
                    if (mUIConfig.filterLayoutTpye == UIConfiguration.FILTER_LAYOUT_2) {
                        //jlk滤镜(acv 单行)
                        mFilterFragment.setJLKStyle(true);
                        changeToFragment(mFilterFragment, true);
                    } else {
                        // 分组滤镜 （acv）
                        changeToFragment(mFilterFragment, false);
                    }
                }
                if (!isPlaying()) {
                    start();
                }
                seekTo(0);
            } else {
                setViewVisibility(R.id.titlebar_layout, true);
                mTvTitle.setText(R.string.setting);
                if (null == mQuikSettingFragment) {
                    mQuikSettingFragment = QuikSettingFragment.newInstance();
                }
                changeToFragment(mQuikSettingFragment, true, true);
            }

        }
    };
    private QuikSettingFragment mQuikSettingFragment;

    /**
     * 标题栏还原到默认状态
     */
    private void resetTitlebar() {

        mBtnLeft.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.edit_back_button, 0, 0, 0);
        mBtnNext.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        mBtnNext.setText(R.string.export);

    }


    //管控所有的模板，（只对应一份文本）
    private ArrayList<String> mUserTextList = new ArrayList<>();

    /**
     * 多行文字转单行字符串
     *
     * @return
     */
    private String userTextListToString() {
        StringBuffer sb = new StringBuffer();
        int len = mUserTextList.size();
        for (int i = 0; i < len; i++) {
            sb.append(mUserTextList.get(i));
            if (i != (len - 1)) {
                sb.append("\n");
            }
        }
        return sb.toString();

    }

    @Override
    public void onQuik(QuikHandler.EffectInfo effectInfo) {
        onQuik(effectInfo, false);
    }

    /**
     * @param effectInfo
     * @param keepText   是否保留Text
     */
    private void onQuik(QuikHandler.EffectInfo effectInfo, boolean keepText) {
//        android.util.Log.e(TAG, "onQuik: " + effectInfo + "    " + keepText);
        pause();
        mTextLayerBtn.setVisibility(View.GONE);
        SysAlertDialog.showLoadingDialog(this, R.string.isloading);
        mEffectInfo = effectInfo;
        mAETemplateInfo = null;
        if (null != mEffectInfo) {
            if (null == mEffectInfo.getAETextMediaList() || keepText) {
                //第一次读取此模板信息
                AETemplateInfo tmpAE = readAE(mEffectInfo);
                if (null != tmpAE) {
                    AEFragmentInfo info = tmpAE.getAEFragmentInfo();
                    if (null != info) {
                        List<AETextMediaInfo> aeQuikTextMediaInfos = new ArrayList<>();
                        int len = info.getLayers().size();
//                        android.util.Log.e(TAG, "onQuik:>>>>>>>>>>： " + len);
                        for (int i = 0; i < len; i++) {
                            AEFragmentInfo.LayerInfo layerInfo = info.getLayers().get(i);
                            String key = layerInfo.getName();
                            AETextMediaInfo aeQuikTextMediaInfo = new AETextMediaInfo();
                            AETextLayerInfo tmp = tmpAE.getTargetAETextLayer(key);
                            aeQuikTextMediaInfo.setAETextLayerInfo(tmp, layerInfo);
                            if (null != tmp) {
                                aeQuikTextMediaInfo.setText(tmp.getTextContent());
                            } else {
                                aeQuikTextMediaInfo.setText(null);
                            }
                            aeQuikTextMediaInfos.add(aeQuikTextMediaInfo);
                        }
                        mEffectInfo.setAETextMediaList(aeQuikTextMediaInfos);
                    }
                }
            }


            List<AETextMediaInfo> textMediaInfos = mEffectInfo.getAETextMediaList();
            if (null != textMediaInfos) {
                int len = textMediaInfos.size();
                int j = 0;
//                android.util.Log.e(TAG, "onQuik: " + Arrays.toString(mUserTextList.toArray()));
                for (int i = 0; i < len; i++) {
                    AETextMediaInfo info = textMediaInfos.get(i);
                    //有文字版
                    AETextLayerInfo aeTextLayerInfo = info.getAETextLayerInfo();
                    if (null != aeTextLayerInfo) {
                        String text = null;
                        if (mEffectInfo.mQuikTemplate == QuikHandler.QuikTemplate.Boxed) {
                            if (mUserTextList.size() > 1) {
                                //多行文字
                                if (i < mUserTextList.size()) {
                                    text = mUserTextList.get(i);
                                }
                            } else if (mUserTextList.size() == 1) {
                                //当行文字（在非boxed模板中编辑的文字（可能出现一个layer对应多行文本），到了boxed时，得拆分成一行对应一个layer）
                                String[] arr = mUserTextList.get(0).split("\n");
                                if (null != arr) {
                                    if (j < arr.length) {
                                        text = arr[j];
                                    }
                                }
                                j++;
                            } else {
                                text = info.getText();
                            }
                        } else {
                            text = userTextListToString();
                        }
                        if (!TextUtils.isEmpty(text) && !TextUtils.isEmpty(info.getText()) && !info.getText().equals(text)) {
                            //已经输入了文本内容，切换模板时，需更加新的内容和容器，生成新的图片
                            info.setTextMediaObj(null);
                        } else {
                            //没有输入文本，填充默认的文字
                            text = info.getText();
                        }
//                        android.util.Log.e(TAG, "onQuik:>>> " + i + "/" + len + " >" + info.getTextMediaObj() + ">>" + text + " text:" + info.getText());

                        if (info.getTextMediaObj() == null) {
                            if (!TextUtils.isEmpty(text)) {
                                //文本转图片
                                String file = AETextActivity.fixAEText(aeTextLayerInfo, text, aeTextLayerInfo.getTtfPath(), info.getLayerInfo());
//                                android.util.Log.e(TAG, "onQuik: file:>" + file);
                                try {
                                    info.setTextMediaObj(new MediaObject(file));
                                } catch (InvalidArgumentException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                info.setTextMediaObj(null);
                            }
                        }
                    } else {
                        info.setTextMediaObj(null);
                    }
                    textMediaInfos.set(i, info);

                }
            }
            updateQuikItem(mEffectInfo);
        }
        build(false);
    }

    private int wordLayoutWidth, wordLayoutHeight;

    @Override
    public void onProportion(float asp) {
        pause();
        if (mCurProportion != asp) {
            mCurProportion = asp;
            SysAlertDialog.showLoadingDialog(this, R.string.isloading);
            mPreviewFrame.setAspectRatio(mCurProportion);
            fixCaptionSize(new IFixPreviewListener() {
                @Override
                public void onComplete() {
                    if (null != mEffectInfo && mEffectInfo.mQuikTemplate != QuikHandler.QuikTemplate.NONE) {
                        //清空之前绑定的ae片段，重新更加显示比例获取新的AE片段
                        onQuik(mEffectInfo, true);
                    } else {
                        //没有绑定ae片段
                        build(true);
                    }
                }
            });
        }
    }

    @Override
    public float getProportion() {
        return mCurProportion;
    }

    @Override
    public QuikHandler getQuikHandler() {
        return quikHandler;
    }

    /**
     * 修正字幕文件大小
     */
    private void fixCaptionSize(final IFixPreviewListener fixPreviewListener) {
        List<Scene> list = new ArrayList<>();
        VirtualVideo.Size size = new VirtualVideo.Size(0, CoreUtils.getMetrics().widthPixels);
        list.add(createScene(null));
        VirtualVideo.getMediaObjectOutSize(list, mCurProportion, size);
        int newVideoWidth = size.width;
        int newVideoHeight = size.height;
        //是否需要重新调整字幕
        boolean bNeedChange = (newVideoWidth != wordLayoutWidth || newVideoHeight != wordLayoutHeight);
        if (bNeedChange) {
            Utils.onFixPreviewDataSource(0, newVideoWidth, newVideoHeight, null, new IFixPreviewListener() {
                @Override
                public void onComplete() {
                    if (null != fixPreviewListener) {
                        fixPreviewListener.onComplete();
                    }
                }
            }, newVideoWidth, newVideoHeight, mVirtualVideo, player);
        } else {
            if (null != fixPreviewListener) {
                fixPreviewListener.onComplete();
            }
        }
    }

    /**
     * @param assetName
     * @return
     */
    private AEFragmentInfo createAssetAE(String assetName) {
        InputStream fileInputStream = null;
        AEFragmentInfo info = null;
        try {
            fileInputStream = getAssets().open(assetName);
            info = AEFragmentUtils.loadSync(fileInputStream);
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (InvalidArgumentException e1) {
            e1.printStackTrace();
        } finally {
            if (null != fileInputStream) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return info;
    }

    /**
     * 准备AE模板
     *
     * @param virtualVideo
     * @param mMediaAnimDuration (媒体动画运行时长  单位：秒   ，Jolly循环时需要)
     */
    private void initAEList(VirtualVideo virtualVideo, float mMediaAnimDuration) {
        AEFragmentInfo[] list;
        mAETemplateInfo = null;
        list = null;
//        android.util.Log.e(TAG, "initAEList: " + mEffectInfo.mQuikTemplate + "  mMediaAnimDuration:" + mMediaAnimDuration);
        if (mEffectInfo.mQuikTemplate.equals(QuikHandler.QuikTemplate.Jolly)) {
            //片头
            mAETemplateInfo = readAE(mEffectInfo);
            //循环部分
            String[] arr = Jolly.init(mCurProportion);
            if (null != arr) {
                int len = arr.length;
                list = new AEFragmentInfo[len];
                for (int i = 0; i < len; i++) {
                    list[i] = createAssetAE(arr[i]);
                }
            }
        } else {
            mAETemplateInfo = readAE(mEffectInfo);
        }
        if (mEffectInfo.mQuikTemplate.equals(QuikHandler.QuikTemplate.Jolly)) {
            float videoDuration = mMediaAnimDuration;
            AEFragmentInfo piantou = mAETemplateInfo.getAEFragmentInfo();
            float piantouDu = piantou.getDuration();
            //片头
            virtualVideo.addAEFragment(piantou);

            //后面的效果循环
            float xh = videoDuration - piantouDu;
//            android.util.Log.e(TAG, "initAEList: " + videoDuration + "  piantou:" + piantouDu);

            //可循环部分的duration
            float tdu = 0;
            int len = list.length;
            for (int i = 0; i < len; i++) {
                tdu += list[i].getDuration();
            }
            //循环次数
            len = (int) Math.ceil(xh / tdu);
            float lineStart = piantouDu;
            for (int i = 0; i < len; i++) {
                float tmpLineStart = lineStart;
                for (int j = 0; j < list.length; j++) {
                    AEFragmentInfo tmp = list[j];
                    float line = tmpLineStart + (i * tdu);
//                    android.util.Log.e(TAG, "onEnd: " + j + "/" + list.length + "    frame:" + i + "/" + len + " >> " + line + "<>" + (line + tmp.getDuration()) + " videoDuration：" + videoDuration + "   paintouDu:" + piantouDu);
                    if (line <= videoDuration) {
                        if (line + tmp.getDuration() > videoDuration) {
                            //截取最后一段ae（截取有效的内容）
                            float du = videoDuration - line;
                            virtualVideo.addAEFragment(tmp, line, du);
//                            android.util.Log.e(TAG, "onEnd: last :" + line + "<>" + du);
                            break;
                        } else {
                            virtualVideo.addAEFragment(tmp, line);
                            tmpLineStart += tmp.getDuration();
                        }
                    }
                }
            }
        } else {
            //处理非片头类型的AE片段
            if (null != mAETemplateInfo && null != mAETemplateInfo.getAEFragmentInfo()) {
                mAETemplateInfo.setMapMediaObjects(null);
                List<String> path = new ArrayList<>();
                if (null != mAETemplateInfo.getAEFragmentInfo().getLayers()) {
                    int len = mAETemplateInfo.getAEFragmentInfo().getLayers().size();
                    int tmpReplaceablePicIndex = 0;
                    for (int i = 0; i < len; i++) {
                        MediaObject mediaObject = mEffectInfo.getAETextMediaList().get(i).getTextMediaObj();
                        if (null != mediaObject) {
                            path.add(mediaObject.getMediaPath());
                        } else {
                            int tmp = Math.min(mArrImage.size() - 1, tmpReplaceablePicIndex);
                            path.add(mArrImage.get(tmp).getMediaPath());
                            tmpReplaceablePicIndex++;
                        }
                    }
                }
                mAETemplateInfo.setListPath(path);
                //不能加载场景中的媒体，需指定媒体
                mAETemplateInfo.setAEFragmentInfo(false, mAETemplateInfo.getAEFragmentInfo());
                virtualVideo.addAEFragment(mAETemplateInfo.getAEFragmentInfo());
            }
        }


    }

    /**
     * 读取ae模板
     *
     * @param mEffectInfo
     * @return
     */
    private AETemplateInfo readAE(QuikHandler.EffectInfo mEffectInfo) {
        String dir = quikHandler.initAEDir(mEffectInfo, mCurProportion);
        AETemplateInfo mAETemplateInfo = null;
        if (!TextUtils.isEmpty(dir)) {
            try {
                mAETemplateInfo = AETemplateUtils.getConfig2(dir);
                if (null != mAETemplateInfo) {
                    AEFragmentInfo aeFragmentInfo = AEFragmentUtils.loadSync(mAETemplateInfo.getDataPath());
                    if (null != aeFragmentInfo) {
                        HashMap<String, MediaObject> map = new HashMap<>();
                        List<String> list = new ArrayList<>();
                        if (null != mEffectInfo && null != mEffectInfo.getAETextMediaList()) {
                            int len = mEffectInfo.getAETextMediaList().size();
                            for (int i = 0; i < len; i++) {
                                AETextMediaInfo item = mEffectInfo.getAETextMediaList().get(i);
                                MediaObject mediaObject = item.getTextMediaObj();
                                map.put(item.getLayerInfo().getName(), mediaObject);
                                if (null != mediaObject) {
                                    list.add(mediaObject.getMediaPath());
                                } else {
                                    list.add(null);
                                }
                            }
                        }
                        if (mEffectInfo.mQuikTemplate.equals(QuikHandler.QuikTemplate.Jolly)) {
                            mAETemplateInfo.setListPath(list);
                            mAETemplateInfo.setMapMediaObjects(null);
                            mAETemplateInfo.setAEFragmentInfo(false, aeFragmentInfo);
                        } else {
                            mAETemplateInfo.setListPath(null);
                            mAETemplateInfo.setMapMediaObjects(map);
                            mAETemplateInfo.setAEFragmentInfo(false, aeFragmentInfo);
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (InvalidArgumentException e) {
                e.printStackTrace();
            }

        }
        return mAETemplateInfo;
    }

    private AETemplateInfo mAETemplateInfo;

    /**
     * 准备重新加载
     *
     * @param needDialog
     */
    private void build(boolean needDialog) {
        if (needDialog) {
            SysAlertDialog.showLoadingDialog(this, R.string.isloading);
        }
        if (player.isPlaying()) {
            pause();
        }
        player.reset();
        player.setPreviewAspectRatio(mCurProportion);
        mVirtualVideo.reset();

        //重新加载
        reload(mVirtualVideo);
        addObject(mVirtualVideo);
        try {
            mVirtualVideo.build(player);
            onSeekTo(0);
            start();
        } catch (InvalidStateException e) {
            e.printStackTrace();
        }


    }


    private Handler mHandler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case RESULT_STYLE: {
                    mIvVideoPlayState.setVisibility(View.GONE);
                    onQuikClick(true);
                    reload(false);
                    start();
                    getWindow().clearFlags(
                            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                }
                break;
                default: {
                }
                break;
            }
        }
    };


    private boolean isPlayingORecording = false;
    private boolean bInterceptRepeat = false;


    private String getFormatTime(int msec) {
        return DateTimeUtils.stringForMillisecondTime(msec, true, true);
    }

    protected void updatePreviewFrameAspect() {
        CommonStyleUtils.init(mLinearWords.getWidth(), mLinearWords.getHeight());
    }


    /**
     * 注册播放器回调
     *
     * @param player
     */
    private void initPlayerListener(final VirtualVideoView player) {

        player.setOnPlaybackListener(new VirtualVideoView.VideoViewListener() {
            @Override
            public void onPlayerPrepared() {
                SysAlertDialog.cancelLoadingDialog();
                wordLayoutWidth = player.getVideoWidth();
                wordLayoutHeight = player.getVideoHeight();
                float dura = player.getDuration();
//                android.util.Log.e(TAG, "onPlayerPrepared: " + dura + "    " + wordLayoutWidth + "*" + wordLayoutHeight);
                int ms = Utils.s2ms(dura);
                TempVideoParams.getInstance().setEditingVideoDuration(ms);
                mRdSeekBar.setMax(ms);
                totalTv.setText(getFormatTime(ms));
                onSeekTo(0);
                int checkId = mEditorMenuGroups.getCheckedRadioButtonId();
                mProgressLayout.setVisibility(checkId != R.id.rb_word ? View.VISIBLE : View.GONE);
                player.setBackgroundColor(Color.BLACK);

                int len = mSaEditorPostionListener.size();
                for (int nTmp = 0; nTmp < len; nTmp++) {
                    mSaEditorPostionListener.valueAt(nTmp).onEditorPrepred();
                }
                updatePreviewFrameAspect();

                if (mEffectInfo != null && mEffectInfo.mQuikTemplate != QuikHandler.QuikTemplate.NONE) {
                    if (mEffectInfo.mQuikTemplate.equals(QuikHandler.QuikTemplate.Boxed)) {
                        player.setBackgroundColor(mBGColor);
                    } else if (mEffectInfo.mQuikTemplate.equals(QuikHandler.QuikTemplate.Flick)) {
                        player.setBackgroundColor(mBGColor);
                    }
                }
            }

            @Override
            public boolean onPlayerError(int what, int extra) {
                Log.e(TAG, "onPlayerError: " + what + "..." + extra);
                FileLog.writeLog("onPlayerError:" + " " + what + " ，，" + extra);
                if (extra == -14) {
                    build(false);
                }
                return false;
            }

            @Override
            public void onPlayerCompletion() {
                Log.i(TAG, "onPlayerCompletion:  播放完毕-->" + player.getDuration());
                for (int nTmp = 0; nTmp < mSaEditorPostionListener.size(); nTmp++) {
                    mSaEditorPostionListener.valueAt(nTmp)
                            .onEditorPreviewComplete();
                }
            }

            @Override
            public void onGetCurrentPosition(float position) {
                int positionMs = Utils.s2ms(position);
                notifyCurrentPosition(positionMs);
                onSeekTo(positionMs);
            }
        });


        player.setOnInfoListener(new VirtualVideo.OnInfoListener() {
            @Override
            public boolean onInfo(int what, int extra, Object obj) {
                Log.i(TAG, "onInfo: " + what + "..." + extra + "..." + obj);
                return true;
            }
        });


    }

    /**
     * @param progress
     */
    private void onSeekTo(int progress) {
        currentTv.setText(getFormatTime(progress));
        mRdSeekBar.setProgress(progress);
    }


    @Override
    protected void onDestroy() {
        SysAlertDialog.cancelLoadingDialog();
        TTFUtils.recycle();
        SubData.getInstance().close();
        TTFData.getInstance().close();
        TempVideoParams.getInstance().setThemeId(0);
        FilterData.getInstance().close();
        unregisterReceiver(mReceiver);
        if (null != player) {
            player.cleanUp();
            player = null;
        }
        if (mSnapshotEditor != null) {
            mSnapshotEditor.release();
            mSnapshotEditor = null;
        }
        //清理
        TempVideoParams.getInstance().recycle();
        if (null != mFilterFragmentLookup) {
            mFilterFragmentLookup.recycle();
            mFilterFragmentLookup = null;
        }
        super.onDestroy();
        mSubtitleFragment = null;
        mQuikSettingFragment = null;

    }


    private float lastProgress = -1f;

    @Override
    protected void onStart() {
        super.onStart();
//        android.util.Log.e(TAG, "onStart: " + isPlayingORecording);
        isPlayingORecording = false;

    }

    @Override
    protected void onResume() {
        super.onResume();
//        android.util.Log.e(TAG, "onResume: " + lastProgress);
        if (!isExport && lastProgress != -1) {
            player.seekTo(lastProgress);
            onSeekTo(Utils.s2ms(lastProgress));
            start();
            lastProgress = -1f;
        }


    }

    @Override
    protected void onPause() {
        super.onPause();
        lastProgress = -1;
        if (null != player) {
            if (player.isPlaying()) {
                //暂停
                player.pause();
            }
            //记录播放器位置
            lastProgress = player.getCurrentPosition();
        }
    }

    public void onToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onBackPressed() {

        int cId = mEditorMenuGroups.getCheckedRadioButtonId();
        if (cId == R.id.rb_filter) {
            //滤镜
            onQuikClick(true);
            reload(false);
            return;
        }

        if (mFragCurrent instanceof SubtitleFragment) {
            //字幕
            mSubtitleFragment.onBackPressed();
        } else if (mFragCurrent instanceof QuikSettingFragment) {
            //比例
            if (mFragCurrent.onBackPressed() != 0) {
                onQuikClick(true);
            }
        } else {
            String strMessage = getString(R.string.quit_edit);
            SysAlertDialog.showAlertDialog(this, "", strMessage,
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
                            if (null != player) {
                                player.stop();
                            }
                            finish();
                        }
                    });
        }


    }

    /**
     * 处理片头的ae模板
     *
     * @param virtualVideo
     */
    private void initPiantou(VirtualVideo virtualVideo) {
        String dir = quikHandler.initAEDir(mEffectInfo, mCurProportion);
        if (!TextUtils.isEmpty(dir) && mEffectInfo.isPiantou()) {
            try {
                mAETemplateInfo = AETemplateUtils.getConfig2(dir);
                if (null != mAETemplateInfo) {
                    AEFragmentInfo aeFragmentInfo = AEFragmentUtils.loadSync(mAETemplateInfo.getDataPath());
                    if (null != aeFragmentInfo) {

                        HashMap<String, MediaObject> data = new HashMap<>();
                        float maxTrim = aeFragmentInfo.getDuration();//依据每个layer的时间线，,只保留有效的layer
                        List<AETextMediaInfo> list = mEffectInfo.getAETextMediaList();
                        if (null != list) {
                            int len = list.size();
                            for (int i = 0; i < len; i++) {
                                AETextMediaInfo item = list.get(i);
                                MediaObject mediaObject = item.getTextMediaObj();
                                List<AEFragmentInfo.TimeLine> timeLineList = item.getLayerInfo().getTimeLine();
                                if (null != mediaObject) {
                                    data.put(item.getLayerInfo().getName(), mediaObject);
                                } else {
                                    if (mEffectInfo.mQuikTemplate == QuikHandler.QuikTemplate.Action) {
                                        //只有Action 需要媒体来补充
                                        int tmp = Math.min(mArrImage.size() - 1, i);
                                        MediaObject shadow = mArrImage.get(tmp);
                                        if (shadow.getMediaType() == MediaType.MEDIA_VIDEO_TYPE) {
                                            //视频截取第1帧图
                                            if (!TextUtils.isEmpty(snapPath)) {
                                                data.put(item.getLayerInfo().getName(), new MediaObject(snapPath));
                                            }
                                        } else {
                                            data.put(item.getLayerInfo().getName(), mArrImage.get(tmp));
                                        }
                                        maxTrim = 0;//完整显示，不截取
                                    } else {
                                        //没有媒体时，后面的layer内容，不再显示，例如：Boxed 2/9
                                        data.put(item.getLayerInfo().getName(), null);
                                        maxTrim = getMaxDuration(maxTrim, timeLineList);
                                    }
                                }
                            }
                        }
//                        android.util.Log.e(TAG, "initPiantou: " + data.size() + ">>" + maxTrim);

                        mAETemplateInfo.setMapMediaObjects(data);
                        mAETemplateInfo.setAEFragmentInfo(false, aeFragmentInfo);
                        Scene scene = VirtualVideo.createScene();
                        scene.addAEFragment(aeFragmentInfo, 0, maxTrim);
                        virtualVideo.addScene(scene);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (InvalidArgumentException e) {
                e.printStackTrace();
            }


        }


    }

    /**
     * @param maxTrim
     * @param timeLineList
     * @return
     */
    private float getMaxDuration(float maxTrim, List<AEFragmentInfo.TimeLine> timeLineList) {
        int count = timeLineList.size();
        for (int n = 0; n < count; n++) {
            AEFragmentInfo.TimeLine line = timeLineList.get(n);
            maxTrim = Math.min(maxTrim, line.getStartTime());
        }
        return maxTrim;
    }

    private Scene mCurrentScene = null;

    /**
     * 加载视频资源
     *
     * @param virtualVideo
     */
    private boolean reload(VirtualVideo virtualVideo) {
//        android.util.Log.e(TAG, "reload: " + mEffectInfo);
        if (null != mEffectInfo) {
            //先加载Ae模板，需要注意是否有引用 Scene中的媒体
            if (mEffectInfo.isPiantou()) {
                //先加片头（片头绑定在第0个Scene上），后加媒体
                initPiantou(virtualVideo);
                mCurrentScene = createScene(virtualVideo);
                virtualVideo.addScene(mCurrentScene);
            } else {
                mCurrentScene = createScene(virtualVideo);
                virtualVideo.addScene(mCurrentScene);
                initAEList(virtualVideo, mCurrentScene.getDuration());
            }
        } else {
            mCurrentScene = createScene(virtualVideo);
            virtualVideo.addScene(mCurrentScene);
        }
        return true;
    }


    /**
     * 添加媒体和设置媒体动画
     *
     * @param virtualVideo
     * @return
     */
    private Scene createScene(VirtualVideo virtualVideo) {
        Scene scene = VirtualVideo.createScene();
        int len = mArrImage.size();
        for (int i = 0; i < len; i++) {
            MediaObject mediaObject = mArrImage.get(i).clone();
            scene.addMedia(mediaObject);
        }
        if (mEffectInfo != null && mEffectInfo.mQuikTemplate != QuikHandler.QuikTemplate.NONE) {
            if (null != virtualVideo) {
                if (mEffectInfo.mQuikTemplate.equals(QuikHandler.QuikTemplate.Light)) {
                    if (!TextUtils.isEmpty(mEffectInfo.blendPath)) {
                        BlendEffectObject blendEffectObject
                                = new BlendEffectObject(mEffectInfo.blendPath, BlendEffectObject.EffectObjectType.SCREEN);
                        blendEffectObject.setFilterType("video");
                        blendEffectObject.setRepeat(true);
                        virtualVideo.addMVEffect(blendEffectObject);
                    }
                }
            }
            //设置媒体动画
            quikHandler.setQuikTemplate(scene, virtualVideo, mEffectInfo.mQuikTemplate, mCurProportion, this);
        }
        return scene;
    }


    /**
     * 配乐、字幕
     *
     * @param virtualVideo
     */
    private void addObject(VirtualVideo virtualVideo) {

        //字幕
        List<CaptionObject> mListCaptions = TempVideoParams.getInstance().getCaptionObjects();
        if (null != mListCaptions) {
            int len = mListCaptions.size();
            for (int i = 0; i < len; i++) {
                CaptionObject st = mListCaptions.get(i);
                virtualVideo.addCaption(st);
            }
        }

        //配乐
        if (mEffectInfo != null) {
            if (FileUtils.isExist(mEffectInfo.musicPath)) {
                try {
                    Music music = VirtualVideo.createMusic(mEffectInfo.musicPath);
                    music.setMixFactor(100);
                    music.setTimelineRange(0, 0);
                    virtualVideo.addMusic(music);
                } catch (InvalidArgumentException e) {
                    e.printStackTrace();
                }
            }
        }

        //滤镜
        if (null != lookupConfig) {
            try {
                virtualVideo.changeFilter(lookupConfig);
            } catch (InvalidArgumentException e) {
                e.printStackTrace();
            }
        } else {
            virtualVideo.changeFilter(mCurrentFilterType);
        }

    }


    private ExportConfiguration mExportConfig;

    /**
     * 响应确定与导出
     */
    private void onRightButtonClick() {
        if (mExportConfig.useCustomExportGuide) {
            SdkEntryHandler.getInstance().onExportClick(QuikActivity.this);
        } else {
            onExport();
        }
    }

    private boolean withWatermark = true;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (TextUtils.equals(action, SdkEntry.MSG_EXPORT)) {
                withWatermark = intent.getBooleanExtra(SdkEntry.EXPORT_WITH_WATERMARK, true);
                onExport();
            }
        }
    };

    /**
     * 返回到功能菜单最后选择项
     */

    private void returnToMenuLastSelection() {
        onQuikClick(true);
    }


    /**
     * 点击下一步->导出
     */
    private void onExport() {
        isExport = true;
        if (isPlaying()) {
            pause();
        }
        ExportHandler exportHandler = new ExportHandler(this, new ExportHandler.IExport() {
            @Override
            public void addData(VirtualVideo virtualVideo) {
                reload(virtualVideo);
                addObject(virtualVideo);
            }
        });
        int bgColor = Color.BLACK;
        if (mEffectInfo != null
                && (mEffectInfo.mQuikTemplate == QuikHandler.QuikTemplate.Boxed
                || mEffectInfo.mQuikTemplate == QuikHandler.QuikTemplate.Flick)) {
            bgColor = mBGColor;
        }
        exportHandler.onExport(mCurProportion, withWatermark, bgColor);

    }


    private BaseFragment mFragCurrent;

    private void setFragmentCurrent(BaseFragment fragment) {
        this.mFragCurrent = fragment;
    }


    /**
     * 切换fragment
     */
    private void changeToFragment(BaseFragment fragment,
                                  boolean bEditorGroupsVisible) {
        changeToFragment(fragment, true, bEditorGroupsVisible);
    }

    /**
     * 切换fragment
     *
     * @param fragment             需要切换的fragment
     * @param enableAnimation      确定是否使用动画
     * @param bEditorGroupsVisible 编辑功能组设置是否显示
     */
    private void changeToFragment(final BaseFragment fragment,
                                  boolean enableAnimation, final boolean bEditorGroupsVisible) {
        if (mFragCurrent == fragment) {
            // 未实际切换fragment时，直接返回
            setFragmentCurrent(fragment); // 重新设置，刷新自动重播状态
            return;
        }
        try {
            if (!enableAnimation) {
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.fl_fragment_container, fragment);
                ft.commit();
                setFragmentCurrent(fragment);
                setViewVisibility(R.id.llEditorGroups, bEditorGroupsVisible);
            } else {
                Animation aniSlideOut = AnimationUtils.loadAnimation(this,
                        R.anim.editor_preview_slide_out);
                aniSlideOut
                        .setAnimationListener(new Animation.AnimationListener() {

                            @Override
                            public void onAnimationStart(Animation animation) {
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {
                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {


                                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                                ft.replace(R.id.fl_fragment_container, fragment);
                                ft.commit();
                                Animation aniSlideIn = AnimationUtils
                                        .loadAnimation(getBaseContext(),
                                                R.anim.editor_preview_slide_in);
                                findViewById(R.id.rlEditorMenuAndSubLayout)
                                        .startAnimation(aniSlideIn);
                                setFragmentCurrent(fragment);
                                setViewVisibility(R.id.llEditorGroups,
                                        bEditorGroupsVisible);
                            }
                        });
                findViewById(R.id.rlEditorMenuAndSubLayout).startAnimation(
                        aniSlideOut);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
