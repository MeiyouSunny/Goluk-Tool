package com.rd.veuisdk.demo;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.rd.lib.ui.PreviewFrameLayout;
import com.rd.vecore.VirtualVideo;
import com.rd.vecore.VirtualVideoView;
import com.rd.vecore.exception.InvalidArgumentException;
import com.rd.vecore.exception.InvalidStateException;
import com.rd.vecore.models.DewatermarkObject;
import com.rd.vecore.models.EffectInfo;
import com.rd.vecore.models.MediaObject;
import com.rd.vecore.models.Scene;
import com.rd.vecore.models.VisualFilterConfig;
import com.rd.vecore.models.caption.CaptionLiteObject;
import com.rd.vecore.models.caption.CaptionObject;
import com.rd.veuisdk.BaseActivity;
import com.rd.veuisdk.IVideoEditorHandler;
import com.rd.veuisdk.R;
import com.rd.veuisdk.SdkEntry;
import com.rd.veuisdk.TempVideoParams;
import com.rd.veuisdk.ae.model.AETemplateInfo;
import com.rd.veuisdk.database.EffectData;
import com.rd.veuisdk.database.FilterData;
import com.rd.veuisdk.database.StickerData;
import com.rd.veuisdk.database.SubData;
import com.rd.veuisdk.database.TTFData;
import com.rd.veuisdk.fragment.CollageFragment;
import com.rd.veuisdk.fragment.EffectFragment;
import com.rd.veuisdk.fragment.FilterFragment;
import com.rd.veuisdk.fragment.FilterFragmentLookup;
import com.rd.veuisdk.fragment.FilterFragmentLookupLocal;
import com.rd.veuisdk.fragment.GraffitiFragment;
import com.rd.veuisdk.fragment.OSDFragment;
import com.rd.veuisdk.fragment.StickerFragment;
import com.rd.veuisdk.fragment.SubtitleFragment;
import com.rd.veuisdk.fragment.helper.IFragmentHandler;
import com.rd.veuisdk.listener.ICollageListener;
import com.rd.veuisdk.manager.UIConfiguration;
import com.rd.veuisdk.model.CollageInfo;
import com.rd.veuisdk.model.GraffitiInfo;
import com.rd.veuisdk.model.StickerInfo;
import com.rd.veuisdk.net.SubUtils;
import com.rd.veuisdk.net.TTFUtils;
import com.rd.veuisdk.ui.PaintView;
import com.rd.veuisdk.utils.AppConfiguration;
import com.rd.veuisdk.utils.CollageManager;
import com.rd.veuisdk.utils.CommonStyleUtils;
import com.rd.veuisdk.utils.ExportHandler;
import com.rd.veuisdk.utils.IParamData;
import com.rd.veuisdk.utils.IParamDataImp;
import com.rd.veuisdk.utils.IParamHandler;
import com.rd.veuisdk.utils.IntentConstants;
import com.rd.veuisdk.utils.SysAlertDialog;
import com.rd.veuisdk.utils.Utils;
import com.rd.veuisdk.utils.VideoEditCollageHandler;

import java.util.ArrayList;
import java.util.List;

public class VideoEditAloneActivity extends BaseActivity implements IVideoEditorHandler,
        IParamHandler, EffectFragment.IEffectHandler {

    private static final String TAG = "VideoEditAloneActivity";
    /*
     * 请求权限code:读取外置存储
     */
    private final int REQUEST_CODE_READ_EXTERNAL_STORAGE_PERMISSIONS = 1;
    private PreviewFrameLayout mPlayerContainer, mPreviewContainer;
    /**
     * VirtualVideoView播放器对象
     */
    private VirtualVideoView mVirtualVideoView;
    private VirtualVideo mVirtualVideo;
    /**
     * 字幕、贴纸 容器
     */
    private FrameLayout mWordsLayout;
    /**
     * UICconfig
     */
    private UIConfiguration mUIConfig;
    /**
     * 字幕fragment
     */
    private SubtitleFragment mSubtitleFragment;
    /**
     * 贴纸
     */
    private StickerFragment mStickerFragment;
    /*
     * 滤镜类对象
     */
    private FilterFragment mFilterFragment;
    private FilterFragmentLookupLocal mLookupLocal;
    private FilterFragmentLookup mLookup;
    /**
     * 特效
     */
    private EffectFragment mEffectFragment;
    /**
     * 马赛克 去水印
     */
    private OSDFragment mOSDFragment;
    /**
     * 画中画
     */
    private CollageFragment mCollageFragment;
    /**
     * 涂鸦
     */
    private GraffitiFragment mGraffitiFragment;
    /**
     * 场景
     */
    private Scene mScene;
    /**
     * 类型  字幕、贴纸
     */
    private String mType = null;
    /**
     * 预览比例
     */
    private VirtualVideo.Size mNewSize;
    //预览比例
    private float mPreviewAsp = 0;
    //是否第一次打开
    private boolean isFirst = true;
    /**
     * 画中画
     */
    private ICollageListener mICollageListener;
    private ViewGroup mGalleryFragmentContainer;
    private IParamDataImp mParamDataImp = new IParamDataImp();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_edit_alone);
        init();
    }

    private void init() {
        SysAlertDialog.showLoadingDialog(VideoEditAloneActivity.this, R.string.isloading);
        //修正播放器容器显示比例
        AppConfiguration.fixAspectRatio(this);
        // 添加api 23权限控制
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int hasReadPermission = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);

            List<String> permissions = new ArrayList<String>();
            if (hasReadPermission != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            if (!permissions.isEmpty()) {
                requestPermissions(
                        permissions.toArray(new String[permissions.size()]),
                        REQUEST_CODE_READ_EXTERNAL_STORAGE_PERMISSIONS);
            } else {
                onInitialized();
            }
        } else {
            onInitialized();
        }
    }

    private void onInitialized() {
        //获取场景
        mScene = getIntent().getParcelableExtra(IntentConstants.INTENT_EXTRA_SCENE);
        if (mScene == null) {
            //没有媒体
            onToast(R.string.no_media);
            finish();
            return;
        }
        //初始化布局
        initView();
        //获取布局
        mUIConfig = SdkEntry.getSdkService().getUIConfig();
        //获取类型
        mType = getIntent().getStringExtra(TYPE);
        //加载
        reload(false);
        //计算预览比例
        mNewSize = new VirtualVideo.Size(0, 0);
        fixPreviewSize();
        mPlayerContainer.setAspectRatio(mPreviewAsp);
        mPreviewContainer.setAspectRatio(mPreviewAsp);
        mVirtualVideoView.setPreviewAspectRatio(mPreviewAsp);
        mPreviewContainer.post(new Runnable() {

            @Override
            public void run() {
                CommonStyleUtils.init(mPreviewContainer.getWidth(),
                        mPreviewContainer.getHeight());
            }
        });
        //设置时间
        int duration = Utils.s2ms(mScene.getDuration());
        TempVideoParams.getInstance().checkParams(duration);
        TempVideoParams.getInstance().setEditingVideoDuration(duration);
    }

    private void initType(String type) {
        if (TYPE_SUBTITLE.equals(type)) {
            //字幕
            SubData.getInstance().initilize(this);//字幕初始化数据库
            TTFData.getInstance().initilize(this);//字体初始化数据库
            onSubtitle();
        } else if (TYPE_STICKER.equals(type)) {
            //贴纸
            StickerData.getInstance().initilize(this);//贴纸初始化数据库
            onSticker();
        } else if (TYPE_FILTER.equals(type)) {
            //滤镜
            EffectData.getInstance().initilize(this);
            FilterData.getInstance().initilize(this);
            onFilter();
        } else if (TYPE_EFFECT.equals(type)) {
            //特效
            onEffect();
        } else if (TYPE_OSD.equals(type)) {
            //去水印
            onOSD();
        } else if (TYPE_COLLAGE.equals(type)) {
            //画中画
            onCollage();
        } else if (TYPE_GRAFFITI.equals(type)) {
            //涂鸦
            onGraffiti();
        } else {
            finish();
        }
    }

    /**
     * 字幕
     */
    private void onSubtitle() {
        mVirtualVideoView.setAutoRepeat(false); // 字幕不需要自动重播
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mSubtitleFragment = SubtitleFragment.newInstance(mUIConfig.subUrl, mUIConfig.fontUrl);
        mSubtitleFragment.setExtractAudio(new SubtitleFragment.IExtractAudio() {
            @Override
            public List<Scene> getAudioSceneList() {
                List<Scene> list = new ArrayList<>();
                list.add(mScene.clone());
                return list;
            }
        });
        mSubtitleFragment.setFragmentContainer($(R.id.rlEditorMenuAndSubLayout));
        mSubtitleFragment.setHideApplyToAll(true);//隐藏应用到全部
        mSubtitleFragment.setExitListener(mExitListener);
        changeFragment(R.id.fl_fragment_container, mSubtitleFragment);
    }

    /**
     * 贴纸
     */
    private void onSticker() {
        // 贴纸
        mVirtualVideoView.setAutoRepeat(false); // 贴纸不需要自动重播
        stop();
        mStickerFragment = StickerFragment.newInstance(mUIConfig.soundTypeUrl, mUIConfig.stickerUrl);
        mStickerFragment.setHandler(mWordsLayout);
        mStickerFragment.setExitListener(mExitListener);
        changeFragment(R.id.fl_fragment_container, mStickerFragment);
    }

    /**
     * 滤镜
     */
    private void onFilter() {
        if (!TextUtils.isEmpty(mUIConfig.filterUrl)) {
            //网络lookup滤镜
            if (null == mLookup) {
                mLookup = FilterFragmentLookup.newInstance(mUIConfig.filterUrl);
            }
            changeFragment(R.id.fl_fragment_container, mLookup);
        } else if (mUIConfig.filterLayoutTpye == UIConfiguration.FILTER_LAYOUT_3) {
            //本地lookup
            if (null == mLookupLocal) {
                mLookupLocal = FilterFragmentLookupLocal.newInstance();
            }
            changeFragment(R.id.fl_fragment_container, mLookupLocal);
        } else {
            if (mFilterFragment == null) {
                mFilterFragment = FilterFragment.newInstance();
            }
            if (mUIConfig.filterLayoutTpye == UIConfiguration.FILTER_LAYOUT_2) {
                //jlk滤镜(acv 单行)
                mFilterFragment.setJLKStyle(true);
                changeFragment(R.id.fl_fragment_container, mFilterFragment);
            } else {
                // 分组滤镜 （acv）
                changeFragment(R.id.fl_fragment_container, mFilterFragment);
            }
        }
        seekTo(0);
        if (!isPlaying()) {
            start();
        }
    }

    /**
     * 特效
     */
    private void onEffect() {
        // 特效
        mVirtualVideoView.setAutoRepeat(false);
        stop();
        String typeUrl = mUIConfig.mResTypeUrl;
        String url = mUIConfig.getEffectUrl();
        mEffectFragment = EffectFragment.newInstance(typeUrl, url);
        changeFragment(R.id.fl_fragment_container, mEffectFragment);
    }

    /**
     * 去水印
     */
    private void onOSD() {
        //马赛克
        mVirtualVideoView.setAutoRepeat(false); // 字幕不需要自动重播
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        stop();
        if (null == mOSDFragment) {
            mOSDFragment = OSDFragment.newInstance();
        }
        mOSDFragment.setHandler(iMosaicHandler);
        mOSDFragment.setExitListener(mExitListener);
        changeFragment(R.id.fl_fragment_container, mOSDFragment);
    }

    /**
     * 画中画
     */
    private void onCollage() {
        //切换到画中画
        View view = findViewById(R.id.edit_video_layout);
        mICollageListener = new VideoEditCollageHandler(view, mGalleryFragmentContainer, getSupportFragmentManager());
        seekTo(0);
        mCollageFragment = CollageFragment.newInstance();
        mCollageFragment.setOtherFragmentHeight(view.getHeight());
        mCollageFragment.setLinearWords(mWordsLayout);
        mCollageFragment.setCallBack(mCollageCallBack);
        mCollageFragment.setExitListener(mExitListener);
        mICollageListener.onCollage(mCollageFragment);
    }

    /**
     * 显示涂鸦
     */
    private void onGraffiti() {
        if (null == mGraffitiFragment) {
            mGraffitiFragment = GraffitiFragment.newInstance();
        }
        mGraffitiFragment.setPaintView((PaintView) $(R.id.paintView));
        mGraffitiFragment.setListener(new GraffitiFragment.IGraffitiListener() {

            @Override
            public void onUpdate(CaptionLiteObject liteObject) {
                mVirtualVideo.updateSubtitleObject(liteObject);
                mVirtualVideoView.refresh();
            }

            @Override
            public void onDelete(CaptionLiteObject liteObject) {
                mVirtualVideo.deleteSubtitleObject(liteObject);
                mVirtualVideoView.refresh();
            }

        });
        mGraffitiFragment.setExitListener(mExitListener);
        pause();
        seekTo(0);
        changeFragment(R.id.fl_fragment_container, mGraffitiFragment);
    }

    private void initView() {
        mVirtualVideo = new VirtualVideo();
        mPlayerContainer = findViewById(R.id.rlPlayerContainer);
        mPreviewContainer = findViewById(R.id.rlPreview);
        mVirtualVideoView = findViewById(R.id.epvPreview);
        mWordsLayout = findViewById(R.id.linear_words);
        mGalleryFragmentContainer = (ViewGroup) findViewById(R.id.galleryFragmentParent);

        //播放器
        mVirtualVideoView.setOnPlaybackListener(new VirtualVideoView.VideoViewListener() {

            @Override
            public void onPlayerPrepared() {
                if (isFirst) {
                    isFirst = false;
                    initType(mType);
                }
                SysAlertDialog.cancelLoadingDialog();
                for (int nTmp = 0; nTmp < mSaEditorPostionListener.size(); nTmp++) {
                    mSaEditorPostionListener.valueAt(nTmp).onEditorPrepred();
                }
                notifyCurrentPosition(Utils.s2ms(mVirtualVideoView.getCurrentPosition()));
            }

            @Override
            public void onPlayerCompletion() {
                for (int nTmp = 0; nTmp < mSaEditorPostionListener.size(); nTmp++) {
                    mSaEditorPostionListener.valueAt(nTmp).onEditorPreviewComplete();
                }
                notifyCurrentPosition(0);
                onCompleted();
                if (null != mEffectFragment) {
                    mEffectFragment.onComplete();
                }
            }

            @Override
            public void onGetCurrentPosition(float position) {
                notifyCurrentPosition(Utils.s2ms(position));
                if (null != mEffectFragment) {
                    mEffectFragment.setPosition(position);
                }
            }

            @Override
            public boolean onPlayerError(int what, int extra) {
                Log.e(TAG, "Player error:" + what + "," + extra);
                SysAlertDialog.cancelLoadingDialog();
                SysAlertDialog.showAlertDialog(VideoEditAloneActivity.this,
                        "",
                        getString(R.string.preview_error),
                        getString(R.string.sure), null, null, null);
                return false;
            }

        });
        mVirtualVideoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playOrPause();
            }
        });
    }

    /**
     * 切换fragment
     *
     * @param containerViewId
     * @param fragment
     */
    protected void changeFragment(int containerViewId, Fragment fragment) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(containerViewId, fragment);
        ft.commitAllowingStateLoss();
    }


    /**
     * 计算预览比例
     */
    private void fixPreviewSize() {
        mNewSize.set(mVirtualVideoView.getPreviewMaxWH(), 0);
        ArrayList<Scene> list = new ArrayList<>();
        list.add(mScene);
        VirtualVideo.getMediaObjectOutSize(list, 0,
                mNewSize);
        mPreviewAsp = mNewSize.width / (mNewSize.height + 0.0f);
    }

    /**
     * 播放完成
     */
    private void onCompleted() {
        seekTo(0);
    }

    @Override
    public VirtualVideoView getEditor() {
        if (mVirtualVideoView != null) {
            return mVirtualVideoView;
        } else {
            return null;
        }
    }

    @Override
    public FrameLayout getSubEditorParent() {
        return mWordsLayout;
    }

    private VirtualVideo mSnapshotEditor;

    @Override
    public VirtualVideo getSnapshotEditor() {
        //截图与预览的虚拟视频需分开
        if (mSnapshotEditor == null) {
            mSnapshotEditor = new VirtualVideo();
            mSnapshotEditor.addScene(mScene);
        }
        return mSnapshotEditor;
    }

    @Override
    public void cancelLoading() {
        SysAlertDialog.cancelLoadingDialog();
    }

    @Override
    public void changeAnimation(int animation) {

    }

    @Override
    public void setAETemplateInfo(AETemplateInfo aeTemplateInfo) {

    }

    /*
     * 盛放EditorPreivewPositionListener的列表
     */
    private SparseArray<EditorPreivewPositionListener> mSaEditorPostionListener = new SparseArray<EditorPreivewPositionListener>();

    @Override
    public void registerEditorPostionListener(EditorPreivewPositionListener listener) {
        mSaEditorPostionListener.append(listener.hashCode(), listener);
    }

    private void notifyCurrentPosition(int positionMs) {
        for (int nTmp = 0; nTmp < mSaEditorPostionListener.size(); nTmp++) {
            mSaEditorPostionListener.valueAt(nTmp).onEditorGetPosition(
                    positionMs, Utils.s2ms(mVirtualVideoView.getDuration()));
        }
    }

    @Override
    public void unregisterEditorProgressListener(EditorPreivewPositionListener listener) {
        mSaEditorPostionListener.remove(listener.hashCode());
    }

    @Override
    public boolean isMediaMute() {
        return false;
    }

    @Override
    public void reload(boolean onlyMusic) {
        SysAlertDialog.showLoadingDialog(this, R.string.isloading);
        mVirtualVideoView.reset();
        mVirtualVideo.reset();
        mVirtualVideo.addScene(mScene);
        if (TYPE_COLLAGE.equals(mType)) {
            //画中画
            ExportHandler.loadMix(mVirtualVideo, mCollageInfos, Utils.s2ms(mVirtualVideo.getDuration()));
        } else if (TYPE_EFFECT.equals(mType)) {
            //特效
            ExportHandler.updateEffects(mVirtualVideo, mEffectInfos);
        }
        try {
            mVirtualVideo.build(mVirtualVideoView);
        } catch (InvalidStateException e) {
            e.printStackTrace();
        }
    }

    @Override
    public VirtualVideo getEditorVideo() {
        if (mVirtualVideo != null) {
            return mVirtualVideo;
        } else {
            return null;
        }
    }

    @Override
    public void removeMvMusic(boolean remove) {

    }

    @Override
    public void onBackPressed() {
        if (TYPE_SUBTITLE.equals(mType)) {
            //字幕
            if (null != mSubtitleFragment) {
                mSubtitleFragment.onBackPressed();
            }
        } else if (TYPE_STICKER.equals(mType)) {
            //贴纸
            if (null != mStickerFragment) {
                mStickerFragment.onBackPressed();
            }
        } else if (TYPE_EFFECT.equals(mType)) {
            pause();
            if (null != mEffectFragment) {
                mEffectFragment.onBackPressed();
                onShowAlert();
            }
        } else if (TYPE_OSD.equals(mType)) {
            if (null != mOSDFragment) {
                int re = mOSDFragment.onBackPressed();
                if (re == 1) {
                    iMosaicHandler.onBackPressed();
                }
            }
        } else if (TYPE_COLLAGE.equals(mType)) {
            if (null != mCollageFragment) {
                mCollageFragment.onLeftClick();
            }
        } else if (TYPE_GRAFFITI.equals(mType)) {
            if (null != mGraffitiFragment) {
                mGraffitiFragment.onBackPressed();
            }
        } else {
            onShowAlert();
        }
    }

    @Override
    public void onBack() {
        if (mExit == 0) {
            onShowAlert();
        } else {
            finish();
        }
    }

    @Override
    public void onSure() {
        if (TYPE_STICKER.equals(mType) && mStickerFragment != null) {
            SysAlertDialog.showLoadingDialog(this, getString(R.string.isloading), false, null);
            mHandler.sendEmptyMessage(EXPORT);
        } else if (TYPE_FILTER.equals(mType)) {
            if (null != mLookupLocal) {
                mLookupLocal.onSure();
            } else if (null != mLookup) {
                mLookup.onSure();
            } else if (null != mFilterFragment) {
                mParamDataImp.setFilterIndex(mFilterFragment.getMenuIndex());
                mParamDataImp.setCurrentFilterType(mFilterFragment.getFilterId());
                mParamDataImp.setLookupConfig(null);
            }
            mHandler.sendEmptyMessage(EXPORT);
        } else {
            //导出
            mHandler.sendEmptyMessage(EXPORT);
        }
    }

    //导出视频
    private void export() {
        stop();
        com.rd.veuisdk.ExportHandler exportHandler = new com.rd.veuisdk.ExportHandler(VideoEditAloneActivity.this, new com.rd.veuisdk.ExportHandler.ExportCallBack() {

            @Override
            public void onCancel() {
                if (TYPE_COLLAGE.equals(mType)) {
                    reload(false);
                }
            }

            @Override
            public void addData(VirtualVideo virtualVideo) {
                virtualVideo.addScene(mScene.clone());
                if (TYPE_SUBTITLE.equals(mType)) {
                    //字幕
                    ArrayList<CaptionObject> subtitleList = TempVideoParams.getInstance().getCaptionObjects();
                    for (CaptionObject object : subtitleList) {
                        virtualVideo.addCaption(object);
                    }
                } else if (TYPE_STICKER.equals(mType)) {
                    //贴纸
                    ArrayList<StickerInfo> lstspecials = TempVideoParams.getInstance().getRSpEffects();
                    for (StickerInfo stickerInfo : lstspecials) {
                        ArrayList<CaptionLiteObject> titem = stickerInfo.getList();
                        for (CaptionLiteObject object : titem) {
                            virtualVideo.addSubtitle(object);
                        }
                    }
                } else if (TYPE_FILTER.equals(mType)) {
                    //滤镜
                    if (null != mParamDataImp.getLookupConfig()) {
                        try {
                            virtualVideo.changeFilter(mParamDataImp.getLookupConfig());
                        } catch (InvalidArgumentException e) {
                            e.printStackTrace();
                        }
                    } else {
                        virtualVideo.changeFilter(mParamDataImp.getCurrentFilterType());
                    }
                } else if (TYPE_EFFECT.equals(mType)) {
                    //特效
                    if (mEffectInfos != null && mEffectInfos.size() > 0) {
                        ExportHandler.updateEffects(virtualVideo, mEffectInfos);
                    }
                } else if (TYPE_OSD.equals(mType)) {
                    //马赛克|去水印
                    List<DewatermarkObject> mMarkObjectList = TempVideoParams.getInstance().getMarkList();
                    if (null != mMarkObjectList) {
                        //马赛克编辑时，直接操作的是DewatermarkObject
                        int len = mMarkObjectList.size();
                        for (int i = 0; i < len; i++) {
                            virtualVideo.addDewatermark(mMarkObjectList.get(i));
                        }
                    }
                } else if (TYPE_COLLAGE.equals(mType)) {
                    //画中画
                    CollageManager.loadMix(virtualVideo, mCollageInfos, Utils.s2ms(mScene.getDuration()));
                } else if (TYPE_GRAFFITI.equals(mType)) {
                    //涂鸦
                    if (null != mParamDataImp.getGraffitiList()) {
                        //涂鸦
                        for (GraffitiInfo graffitiInfo : mParamDataImp.getGraffitiList()) {
                            virtualVideo.addSubtitle(graffitiInfo.getLiteObject());
                        }
                    }
                }
            }
        });
        exportHandler.onExport(mVirtualVideoView.getVideoWidth() / (mVirtualVideoView.getVideoHeight() + .0f), true);
    }


    private int EXPORT = 20;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == EXPORT) {
                SysAlertDialog.cancelLoadingDialog();
                export();
            }
        }
    };

    /**
     * 去水印
     */
    private IFragmentHandler iMosaicHandler = new IFragmentHandler() {

        @Override
        public void onBackPressed() {
        }
    };

    /**
     * 画中画
     */
    private ArrayList<CollageInfo> mCollageInfos = new ArrayList<>();

    private CollageFragment.CallBack mCollageCallBack = new CollageFragment.CallBack() {
        @Override
        public void onLeftClick() {
            mCollageInfos.clear();
            mCollageInfos.addAll(TempVideoParams.getInstance().getCollageDurationChecked());
            //判断直接finsh还是弹框
            onBack();
        }

        @Override
        public void onRightClick(List<CollageInfo> mixList) {
            mCollageInfos.clear();
            if (null != mixList && mixList.size() > 0) {
                mCollageInfos.addAll(mixList);
            }
            TempVideoParams.getInstance().setCollageList(mixList);
            //导出
            mHandler.sendEmptyMessage(EXPORT);
        }
    };

    /**
     * 提示是否放弃保存
     */
    private void onShowAlert() {
        SysAlertDialog.createAlertDialog(this,
                getString(R.string.dialog_tips),
                getString(R.string.cancel_all_changed),
                getString(R.string.cancel),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }, getString(R.string.sure),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                }, false, null).show();
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
    public void changeFilterType(int index, int nFilterType) {
        if (mVirtualVideoView != null) {
            if (!mVirtualVideoView.isPlaying()) {
                start();
            }
            mVirtualVideo.changeFilter(nFilterType);
        }
    }

    @Override
    public void changeFilterLookup(VisualFilterConfig lookup, int index) {
        if (mVirtualVideoView != null) {
            if (!mVirtualVideoView.isPlaying()) {
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
        return mParamDataImp.getFilterIndex();
    }

    private void playOrPause() {
        if (isPlaying()) {
            pause();
        } else {
            start();
        }
    }

    @Override
    public boolean isPlaying() {
        return mVirtualVideoView != null && mVirtualVideoView.isPlaying();
    }

    @Override
    public void start() {
        mVirtualVideoView.start();
        //隐藏编辑框
        if (mOSDFragment != null) {
            mOSDFragment.setHideEdit();
        } else if (mGraffitiFragment != null) {
            mGraffitiFragment.setHideEdit();
        } else if (mCollageFragment != null) {
            mCollageFragment.setHideEdit();
        } else if (mSubtitleFragment != null) {
            mSubtitleFragment.setHideEdit();
        } else if (mStickerFragment != null) {
            mStickerFragment.setHideEdit();
        }
    }

    @Override
    public void pause() {
        if (mVirtualVideoView.isPlaying()) {
            mVirtualVideoView.pause();
        }
    }

    @Override
    public void stop() {
        mVirtualVideoView.stop();
    }

    @Override
    public void seekTo(int msec) {
        mVirtualVideoView.seekTo(Utils.ms2s(msec));
    }

    @Override
    public int getDuration() {
        if (null == mVirtualVideoView) {
            return 1;
        }
        return Utils.s2ms(mVirtualVideoView.getDuration());
    }

    @Override
    public int getCurrentPosition() {
        if (null != mVirtualVideoView) {
            return Utils.s2ms(mVirtualVideoView.getCurrentPosition());
        } else {
            return 0;
        }
    }

    @Override
    public IParamData getParamData() {
        return mParamDataImp;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SysAlertDialog.cancelLoadingDialog();
        SubUtils.getInstance().recycle();
        TTFUtils.recycle();
        TTFData.getInstance().close();
        SubData.getInstance().close();
        FilterData.getInstance().close();
        EffectData.getInstance().close();
        CollageManager.recycle();
        if (null != mVirtualVideoView) {
            mVirtualVideoView.cleanUp();
            mVirtualVideoView = null;
        }
        if (null != mVirtualVideo) {
            mVirtualVideo.release();
            mVirtualVideo = null;
        }
        if (null != mSaEditorPostionListener) {
            mSaEditorPostionListener.clear();
            mSaEditorPostionListener = null;
        }
        if (null != mEffectInfos) {
            mEffectInfos.clear();
            mEffectInfos = null;
        }
        if (null != mCollageInfos) {
            mCollageInfos.clear();
            mCollageInfos = null;
        }
        mParamDataImp = null;
        mSubtitleFragment = null;
        mStickerFragment = null;
        mFilterFragment = null;
        mEffectFragment = null;
        mOSDFragment = null;
        mCollageFragment = null;
        mICollageListener = null;
        mGraffitiFragment = null;
        TempVideoParams.getInstance().recycle();
        mHandler.removeCallbacksAndMessages(null);
    }

    /**
     * 跳转
     */
    private static final String TYPE = "type";
    public static final String TYPE_SUBTITLE = "Subtitle";//字幕
    public static final String TYPE_STICKER = "Sticker";//贴纸
    public static final String TYPE_FILTER = "Filter";//滤镜
    public static final String TYPE_EFFECT = "Effect";//特效
    public static final String TYPE_OSD = "OSD";//去水印
    public static final String TYPE_COLLAGE = "Collage";//画中画
    public static final String TYPE_GRAFFITI = "Graffiti";//涂鸦

    public static void newInstance(Context context, Scene scene, String type, int requestCode) {
        Intent intent = new Intent(context, VideoEditAloneActivity.class);
        intent.putExtra(IntentConstants.INTENT_EXTRA_SCENE, scene);
        intent.putExtra(TYPE, type);
        ((Activity) context).startActivityForResult(intent, requestCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_READ_EXTERNAL_STORAGE_PERMISSIONS: {
                for (int i = 0; i < permissions.length; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        onInitialized();
                    } else {
                        SysAlertDialog.showAutoHideDialog(this, null,
                                getString(R.string.un_allow_video_photo),
                                Toast.LENGTH_SHORT);
                        finish();
                    }
                }
            }
            break;
            default: {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CollageFragment.REQUESTCODE_FOR_ADD_MEDIA) {
            if (TYPE_COLLAGE.equals(mType) && null != mCollageFragment) {
                mCollageFragment.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    /**
     * 特效
     */
    private ArrayList<EffectInfo> mEffectInfos;

    @Override
    public VirtualVideoView getPlayer() {
        return mVirtualVideoView;
    }

    @Override
    public ArrayList<EffectInfo> getEffectInfos() {
        if (null == mEffectInfos) {
            mEffectInfos = new ArrayList<>();
        }
        return mEffectInfos;
    }

    @Override
    public void updateEffects(ArrayList<EffectInfo> list) {
        boolean isPlaying = mVirtualVideoView.isPlaying();
        if (isPlaying) {
            mVirtualVideoView.pause();
        }
        mEffectInfos = list;

        //清理播放器中已加载的全部特效 （实时预览必须先删除再新增）
        mVirtualVideo.clearEffects(mVirtualVideoView);
        //再次往虚拟视频添加特效
        ExportHandler.updateEffects(mVirtualVideo, mEffectInfos);
        //更新播放器
        mVirtualVideo.updateEffects(mVirtualVideoView);
        if (isPlaying) {
            mVirtualVideoView.start();
        }
    }

    @Override
    public void updateEffectsReload(ArrayList<EffectInfo> list, int seekto) {
        boolean isPlaying = mVirtualVideoView.isPlaying();
        if (isPlaying) {
            mVirtualVideoView.pause();
        }
        mEffectInfos = list;
        reload(false);
        seekTo(seekto);
        if (isPlaying) {
            mVirtualVideoView.start();
        }
    }

    @Override
    public VirtualVideo getSnapVideo() {
        return getSnapshotEditor();
    }

    @Override
    public void onEffectBackToMain() {
    }

    @Override
    public void onEffectSure(ArrayList<EffectInfo> list) {
        mEffectInfos = list;
    }

    @Override
    public MediaObject getReverseMediaObjcet() {
        return mScene.getAllMedia().get(0);
    }

    @Override
    public boolean enableMultiEffect() {
        return true;
    }

    /**
     * 如果为 0 弹出提示框 否则直接finsh
     */
    private int mExit = 0;

    public interface ExitListener {

        /**
         * 退出模式 0 弹窗 1 直接finish
         *
         * @param t
         */
        void exit(int t);

    }

    private ExitListener mExitListener = new ExitListener() {
        @Override
        public void exit(int t) {
            mExit = t;
        }
    };

}
