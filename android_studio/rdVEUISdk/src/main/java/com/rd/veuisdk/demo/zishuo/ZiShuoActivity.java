package com.rd.veuisdk.demo.zishuo;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.fragment.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.rd.lib.ui.PreviewFrameLayout;
import com.rd.lib.utils.CoreUtils;
import com.rd.vecore.Music;
import com.rd.vecore.VirtualVideo;
import com.rd.vecore.VirtualVideoView;
import com.rd.vecore.exception.InvalidArgumentException;
import com.rd.vecore.exception.InvalidStateException;
import com.rd.vecore.models.AspectRatioFitMode;
import com.rd.vecore.models.CanvasObject;
import com.rd.vecore.models.CustomDrawObject;
import com.rd.vecore.models.MediaObject;
import com.rd.vecore.models.MediaType;
import com.rd.vecore.models.MusicFilterType;
import com.rd.vecore.models.Scene;
import com.rd.vecore.models.VideoConfig;
import com.rd.vecore.models.VisualFilterConfig;
import com.rd.vecore.models.caption.CaptionLiteObject;
import com.rd.vecore.utils.Log;
import com.rd.veuisdk.BaseActivity;
import com.rd.veuisdk.ExportHandler;
import com.rd.veuisdk.IVideoEditorHandler;
import com.rd.veuisdk.R;
import com.rd.veuisdk.SdkEntry;
import com.rd.veuisdk.TempVideoParams;
import com.rd.veuisdk.ae.model.AETemplateInfo;
import com.rd.veuisdk.database.StickerData;
import com.rd.veuisdk.database.TTFData;
import com.rd.veuisdk.demo.zishuo.drawtext.CustomHandler;
import com.rd.veuisdk.fragment.BackgroundZishuoFragment;
import com.rd.veuisdk.fragment.BaseFragment;
import com.rd.veuisdk.fragment.MusicEffectFragment;
import com.rd.veuisdk.fragment.MusicFragmentEx;
import com.rd.veuisdk.fragment.StickerFragment;
import com.rd.veuisdk.manager.ExportConfiguration;
import com.rd.veuisdk.manager.UIConfiguration;
import com.rd.veuisdk.model.StickerInfo;
import com.rd.veuisdk.net.SubUtils;
import com.rd.veuisdk.ui.RdSeekBar;
import com.rd.veuisdk.utils.AppConfiguration;
import com.rd.veuisdk.utils.DateTimeUtils;
import com.rd.veuisdk.utils.IParamData;
import com.rd.veuisdk.utils.IParamDataImp;
import com.rd.veuisdk.utils.IParamHandler;
import com.rd.veuisdk.utils.PathUtils;
import com.rd.veuisdk.utils.SysAlertDialog;
import com.rd.veuisdk.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ZiShuoActivity extends BaseActivity implements View.OnClickListener,
        IVideoEditorHandler, IParamHandler, MusicEffectFragment.IMusicEffectCallBack {

    /*
     * ????????????code:??????????????????
     */
    private final int REQUEST_CODE_READ_EXTERNAL_STORAGE_PERMISSIONS = 1;
    /*
     * ???????????????????????? (????????????????????????)????????????
     */
    private PreviewFrameLayout mPflVideoPreview;
    private VirtualVideoView mVirtualVideoView;
    private VirtualVideo mVirtualVideo;
    /**
     * ??????
     */
    private PreviewFrameLayout mPfContainer;
    private FrameLayout mFlSticker;
    /**
     * ????????????
     */
    private TextView mTvTitle;
    private RelativeLayout mRlTopmenu;
    private Switch mStMoreAnim;
    /**
     * UIConfig???ExportConfig
     */
    private UIConfiguration mUIConfig;
    private ExportConfiguration mExportConfig = null;
    private IParamDataImp mParamDataImp = new IParamDataImp();
    /*
     * ??????????????????????????????????????????????????????????????????????????????
     */
    private LinearLayout mLlPlayFullScreen;
    private RdSeekBar mSbPlayControl;
    private ImageView mIvVideoPlayState;
    private TextView mTvTotalTime;
    private TextView mTvCurTime;
    private ImageView mIvFullScreen;
    /**
     * ??????????????????????????????????????????
     */
    private StickerFragment mStickerFragment;
    private MusicEffectFragment mMusicEffectFragment;
    private MusicFragmentEx mMusicFragmentEx;
    private BackgroundZishuoFragment mBackgroundFragment;
    private TextFragment mTextFragment;
    /*
     * ???????????????????????????
     */
    private boolean mIsFullScreen = false;
    /**
     * fragment
     */
    private BaseFragment mFragCurrent;//??????
    private ZishuoFragment mZishuoFragment;
    private Scene mScene;
    private ZishuoFragment.IMenuListener mListener;
    //????????????
    private View mContent;
    private float mPreviewAsp = 0;
    private String bgVideoPath;
    private String bgColor = "#000000";
    private boolean mIsChooseColor = false;
    //?????????
    private LinearLayout mLlAlpha;
    private TextView mTvAlphaValue;
    private RdSeekBar mSbAlpha;
    //????????????????????????  ??????
    private CustomHandler mTemplate;
    //???????????????
    private int mAlphaBg = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zi_shuo);
        //?????????????????????????????????
        AppConfiguration.fixAspectRatio(this);
        // ??????api 23????????????
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int hasReadPermission = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);

            List<String> permissions = new ArrayList<String>();
            if (hasReadPermission != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            if (!permissions.isEmpty()) {
                requestPermissions(permissions.toArray(new String[permissions.size()]), REQUEST_CODE_READ_EXTERNAL_STORAGE_PERMISSIONS);
            } else {
                init();
            }
        } else {
            init();
        }
    }

    private void init() {
        SysAlertDialog.cancelLoadingDialog();
        //???????????????
        initView();
        mTvTitle.setText(getString(R.string.zishuo));
        //?????????????????????
        mUIConfig = SdkEntry.getSdkService().getUIConfig();
        mExportConfig = SdkEntry.getSdkService().getExportConfig();
        //??????????????????
        TTFData.getInstance().initilize(this);
        StickerData.getInstance().initilize(this);//????????????????????????
        //??????
        mVirtualVideo = new VirtualVideo();
        mVirtualVideo.setIsZishuo(true);
        mZishuoFragment = ZishuoFragment.newInstance();
        mListener = new ZishuoFragment.IMenuListener() {
            @Override
            public void onStyle() {

            }

            @Override
            public void onWord() {
                if (mTextFragment == null) {
                    mTextFragment = new TextFragment();
                }
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.ll_text_fragment, mTextFragment);
                ft.commit();
                seekTo(0);
                pause();
                findViewById(R.id.ll_text_fragment).setVisibility(View.VISIBLE);
                mFragCurrent = mTextFragment;
            }

            @Override
            public void onSticker() {
                hideTitlebar();
                // ??????
                mVirtualVideoView.setAutoRepeat(false); // ???????????????????????????
                stop();
                if (mStickerFragment == null) {
                    mStickerFragment = StickerFragment.newInstance(mUIConfig.soundTypeUrl, mUIConfig.stickerUrl);
                }
                mStickerFragment.setHandler(mFlSticker);
                changeToFragment(mStickerFragment, false);
                pause();
                seekTo(0);
            }

            @Override
            public void onSoundEffect() {
                hideTitlebar();
                if (null == mMusicEffectFragment) {
                    mMusicEffectFragment = MusicEffectFragment.newInstance();
                }
                changeToFragment(mMusicEffectFragment, false);
                pause();
                seekTo(0);
            }

            @Override
            public void onMusic() {
                hideTitlebar();
                if (null == mMusicFragmentEx) {
                    mMusicFragmentEx = MusicFragmentEx.newInstance();
                }
                //??????
                mMusicFragmentEx.checkEnableVolume();
                //??????????????????
                String musicUrl = mUIConfig.newMusicUrl;
                boolean useNewMusicUrl = true;
                if (TextUtils.isEmpty(musicUrl)) {
                    useNewMusicUrl = false;
                    musicUrl = mUIConfig.musicUrl;
                }
                //?????????
                if (!TextUtils.isEmpty(mUIConfig.soundTypeUrl) && !TextUtils.isEmpty(mUIConfig.soundUrl)) {
                    //??????????????????
                    mMusicFragmentEx.init(useNewMusicUrl, mExportConfig.trailerDuration, musicUrl, mUIConfig.voiceLayoutTpye,
                            mMusicListener, mUIConfig.soundTypeUrl, mUIConfig.soundUrl, true, mUIConfig.enableLocalMusic, mUIConfig.isHideDubbing(),
                            mUIConfig.mCloudAuthorizationInfo);
                } else {
                    //????????????????????? 190625
                    String cloudUrl = mUIConfig.newCloudMusicUrl;
                    boolean bNewCloud = true;
                    if (TextUtils.isEmpty(cloudUrl)) {
                        bNewCloud = false;
                        cloudUrl = mUIConfig.cloudMusicUrl;
                    }
                    mMusicFragmentEx.init(useNewMusicUrl, mExportConfig.trailerDuration, musicUrl, mUIConfig.voiceLayoutTpye,
                            mMusicListener, "", cloudUrl, bNewCloud, mUIConfig.enableLocalMusic, mUIConfig.isHideDubbing(), mUIConfig.mCloudAuthorizationInfo);
                }
                changeToFragment(mMusicFragmentEx, false);
                pause();
                seekTo(0);
            }

            @Override
            public void onBackground() {
                hideTitlebar();
                if (mBackgroundFragment == null) {
                    mBackgroundFragment = BackgroundZishuoFragment.newInstance();
                }
                mBackgroundFragment.setListener(new BackgroundZishuoFragment.OnBackgroundListener() {

                    @Override
                    public void onBackground(String path, String color) {
                        bgVideoPath = path;
                        bgColor = color;
                        build(false);
                        seekTo(1000);
                    }

                    @Override
                    public void onAlpha(boolean b) {
                        //?????????
                        if (b) {
                            mLlAlpha.setVisibility(View.GONE);
                        } else {
                            mSbAlpha.setProgress(mAlphaBg);
                            mLlAlpha.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void resetAlpha(int alpha) {
                        mAlphaBg = alpha;
                    }
                }, bgVideoPath, bgColor, mAlphaBg);
                mBackgroundFragment.setOtherFragmentHeight(findViewById(R.id.rl_zishuo_fragment).getHeight());
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.mixContainer, mBackgroundFragment);
                ft.commitAllowingStateLoss();
                findViewById(R.id.galleryFragmentParent).setVisibility(View.VISIBLE);
                findViewById(R.id.edit_video_layout).setVisibility(View.GONE);
                mFragCurrent = mBackgroundFragment;
            }

            @Override
            public void onTemplate(CustomHandler handler, String path) {
                if (!TextUtils.isEmpty(path)) {
                    bgVideoPath = path;
                    bgColor = null;
                }
                mTemplate = handler;
                build(false);
                seekTo(0);
                start();
            }

        };
        mZishuoFragment.setMenuListener(mListener);
        changeToFragment(mZishuoFragment, false);
    }

    private void initView() {
        mContent = findViewById(android.R.id.content);
        mPflVideoPreview = findViewById(R.id.rl_player_container);
        mVirtualVideoView = findViewById(R.id.epv_preview);
        mLlPlayFullScreen = findViewById(R.id.rl_player_full_screen);
        mSbPlayControl = findViewById(R.id.sb_schedule);
        mIvVideoPlayState = findViewById(R.id.iv_player);
        mTvTotalTime = findViewById(R.id.tv_total_time);
        mTvCurTime = findViewById(R.id.tv_cur_time);
        mIvFullScreen = findViewById(R.id.iv_full_screen);
        mRlTopmenu = findViewById(R.id.titlebar_layout);
        mStMoreAnim = findViewById(R.id.sw_more_anim);
        mTvTitle = findViewById(R.id.tvTitle);
        mPfContainer = findViewById(R.id.pf_preview);
        mFlSticker = findViewById(R.id.fl_sticker);
        mLlAlpha = findViewById(R.id.ll_alpha);
        mTvAlphaValue = findViewById(R.id.tv_value);
        mSbAlpha = findViewById(R.id.sb_alpha);

        //????????????
        findViewById(R.id.btnLeft).setOnClickListener(this);
        findViewById(R.id.btnRight).setOnClickListener(this);
        mIvVideoPlayState.setOnClickListener(this);
        mIvFullScreen.setOnClickListener(this);

        //???????????????
        mVirtualVideoView.setOnPlaybackListener(new VirtualVideoView.VideoViewListener() {

            @Override
            public void onPlayerPrepared() {
                SysAlertDialog.cancelLoadingDialog();
                int ms = Utils.s2ms(mVirtualVideo.getDuration());
                TempVideoParams.getInstance().setEditingVideoDuration(ms);
                mSbPlayControl.setMax(ms);
                mTvTotalTime.setText(getFormatTime(ms));

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
                seekTo(0);
            }

            @Override
            public void onGetCurrentPosition(float position) {
                onSeekTo(Utils.s2ms(position));
                notifyCurrentPosition(Utils.s2ms(position));
            }

            @Override
            public boolean onPlayerError(int what, int extra) {
                Log.e(TAG, "onPlayerError: " + what + "..." + extra);
                if (extra == -14) {
                    build(false);
                    seekTo(0);
                    start();
                }
                return false;
            }

        });
        mVirtualVideoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int checkId = mZishuoFragment.getCheckedId();
                if (checkId != R.id.rb_sticker) {
                    if (mIsFullScreen) {
                        if (mLlPlayFullScreen.getVisibility() == View.VISIBLE) {
                            mLlPlayFullScreen.setVisibility(View.GONE);
                            mHandler.removeMessages(FULL_SCREEN);
                        } else {
                            mLlPlayFullScreen.setVisibility(View.VISIBLE);
                            mHandler.sendEmptyMessageDelayed(FULL_SCREEN, 3 * 1000);
                        }
                    } else {
                        playOrPause();
                    }
                }
            }
        });
        //??????
        mSbPlayControl.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                if (fromUser) {
                    seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                pause();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                start();
            }
        });

        //????????????
        mStMoreAnim.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                TempZishuoParams.getInstance().setMore(isChecked);
                build(false);
                seekTo(0);
                start();
            }
        });

        //?????????
        mSbAlpha.setMax(100);
        mSbAlpha.setProgress(100);
        mSbAlpha.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mTvAlphaValue.setText(progress + "%");
                mAlphaBg = progress;
                if (mIsChooseColor && fromUser) {
                    mIsChooseColor = false;
                    build(false);
                    seekTo(1000);
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

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.iv_player) {
            //????????????
            playOrPause();
        } else if (i == R.id.iv_full_screen) {
            //??????
            if (mIsFullScreen) {
                fullScreen(false);
            } else {
                fullScreen(true);
            }
        } else if (i == R.id.btnLeft) {
            //????????????
            onShowAlert();
        } else if (i == R.id.btnRight) {
            //??????
            stop();
            ExportHandler exportHandler = new ExportHandler(this, new ExportHandler.IExport() {
                @Override
                public void addData(VirtualVideo virtualVideo) {
                    export(virtualVideo);
                }
            });
            VideoConfig videoConfig = exportHandler.getExportConfig(mVirtualVideoView.getVideoWidth() / (mVirtualVideoView.getVideoHeight() + 0.0f));
            videoConfig.setBackgroundColor(Color.BLACK);
            videoConfig.setVideoSize(mVirtualVideoView.getVideoWidth(), mVirtualVideoView.getVideoHeight());
            exportHandler.onExport(true, videoConfig);
        }
    }

    /**
     * buld
     *
     * @param anlyMusic
     */
    private void build(boolean anlyMusic) {
        SysAlertDialog.cancelLoadingDialog();
        if (mVirtualVideoView.isPlaying()) {
            pause();
        }
        if (anlyMusic) {
            //????????????
            addMusic(mVirtualVideo);
            mVirtualVideo.updateMusic(mVirtualVideoView);
        } else {
            //????????????
            mVirtualVideoView.reset();
            mVirtualVideo.reset();
            addMusic(mVirtualVideo);
            reload(mVirtualVideo);
            try {
                mVirtualVideo.build(mVirtualVideoView);
                mIsChooseColor = true;
            } catch (InvalidStateException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * ????????????
     */
    private void addMusic(VirtualVideo virtualVideo) {
        virtualVideo.clearMusic();
        try {
            //??????
            Music music = TempVideoParams.getInstance().getMusic();
            if (music != null) {
                virtualVideo.addMusic(music);
            }
            //??????
            ArrayList<Music> audition = TempZishuoParams.getInstance().getMusicList();
            for (Music music1 : audition) {
                virtualVideo.addMusic(music1);
            }
            virtualVideo.removeMVMusic(mParamDataImp.isRemoveMVMusic());
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        }
        //????????????
        virtualVideo.setMusicFilter(MusicFilterType.valueOf(mParamDataImp.getSoundEffectId()));
    }

    /**
     * ??????
     *
     * @param virtualVideo
     */
    private void export(VirtualVideo virtualVideo) {
        addMusic(virtualVideo);
        reload(virtualVideo);
    }

    /**
     * ??????????????????
     *
     * @param virtualVideo
     */
    private boolean reload(VirtualVideo virtualVideo) {
        try {
            //????????????
            final float finalMaxTrim = TempZishuoParams.getInstance().getDuration();
            //??????
            MediaObject mediaObject = null;
            if (!TextUtils.isEmpty(bgColor)) {
                //??????????????????
                mediaObject = new MediaObject(createColor(bgColor));
                mediaObject.setClearImageDefaultAnimation(true);
                mediaObject.setIntrinsicDuration(finalMaxTrim);
            } else if (!TextUtils.isEmpty(bgVideoPath)) {
                mediaObject = new MediaObject(bgVideoPath);
                mediaObject.setClearImageDefaultAnimation(true);
            }
            mScene = VirtualVideo.createScene();
            if (mediaObject != null) {
                int num = (int) (finalMaxTrim / mediaObject.getDuration());
                //??????????????????
                for (int i = 0; i < num; i++) {
                    mediaObject = new MediaObject(mediaObject);
                    mediaObject.setAlpha(mAlphaBg / 100f);
                    mScene.addMedia(mediaObject);
                }
                float time = finalMaxTrim % mediaObject.getDuration();
                if (time > 0) {
                    mediaObject = new MediaObject(mediaObject);
                    mediaObject.setAlpha(mAlphaBg / 100f);
                    mScene.addMedia(mediaObject.setTimeRange(0, time));
                }

            }
            virtualVideo.addScene(mScene);
            mPreviewAsp = (mVirtualVideoView.getVideoWidth() + 0.0f) / mVirtualVideoView.getVideoHeight();
            mPfContainer.setAspectRatio(mPreviewAsp);
            mPflVideoPreview.setAspectRatio(mPreviewAsp);
            //??????
            if (mTemplate != null) {
                mTemplate.reset();
            }
            //??????
            virtualVideo.addCustomDraw(new CustomDrawObject(finalMaxTrim) {
                @Override
                public CustomDrawObject clone() {
                    return null;
                }

                @Override
                public void draw(CanvasObject canvas, float progress) {
                    float ps = progress * finalMaxTrim;
                    if (mTemplate != null) {
                        mTemplate.onDraw(canvas, ps);
                    }
                }
            });
            //??????
            if (mZishuoFragment.getCheckedId() != R.id.rb_sticker) {
                ArrayList<StickerInfo> lstspecials = TempVideoParams.getInstance().getRSpEffects();
                for (StickerInfo stickerInfo : lstspecials) {
                    ArrayList<CaptionLiteObject> titem = stickerInfo.getList();
                    for (CaptionLiteObject object : titem) {
                        virtualVideo.addSubtitle(object);
                    }
                }
            }
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * ??????????????????
     *
     * @param color
     */
    private String createColor(String color) {
        File localPath = new File(PathUtils.getRdTempPath(), color + ".png");
        if (!localPath.exists()) {
            Bitmap bitmap = Bitmap.createBitmap(720, (int) (720 * (CustomHandler.fH / CustomHandler.fW + 0.0f)), Bitmap.Config.ARGB_8888);
            bitmap.eraseColor(Color.parseColor(color));//????????????
            try {
                com.rd.lib.utils.BitmapUtils.saveBitmapToFile(bitmap, true, 100, localPath.getAbsolutePath());
                bitmap.recycle();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return localPath.getAbsolutePath();
    }

    /**
     * ??????fragment
     *
     * @param fragment        ???????????????fragment
     * @param enableAnimation ????????????????????????
     */
    private void changeToFragment(final BaseFragment fragment, boolean enableAnimation) {
        if (mFragCurrent == fragment) {
            // ???????????????fragment??????????????????
            // ???????????????????????????????????????
            if (fragment instanceof ZishuoFragment) {
                mVirtualVideoView.setAutoRepeat(true);
            }
            return;
        }
        try {
            if (!enableAnimation) {
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.fl_fragment_container, fragment);
                ft.commit();
                mFragCurrent = fragment;
            } else {
                Animation aniSlideOut = AnimationUtils.loadAnimation(this, R.anim.editor_preview_slide_out);
                findViewById(R.id.rl_zishuo_fragment).startAnimation(aniSlideOut);
                aniSlideOut.setAnimationListener(new Animation.AnimationListener() {

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
                        Animation aniSlideIn = AnimationUtils.loadAnimation(getBaseContext(), R.anim.editor_preview_slide_in);
                        findViewById(R.id.rl_zishuo_fragment).startAnimation(aniSlideIn);
                        mFragCurrent = fragment;
                    }
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * @param progress
     */
    private void onSeekTo(int progress) {
        mTvCurTime.setText(getFormatTime(progress));
        mSbPlayControl.setProgress(progress);
    }

    @Override
    public void onBack() {
        int i = mZishuoFragment.getCheckedId();
        if (i == R.id.rb_sticker) {
            SysAlertDialog.showLoadingDialog(this, getString(R.string.isloading), false, null);
            //??????????????????
            mHandler.sendEmptyMessage(STICKER);
        } else if (i == R.id.rb_word) {
            mHandler.sendEmptyMessage(TEXT);
        } else if (i == R.id.rb_background) {
            mHandler.sendEmptyMessage(BACKGROUND);
        } else {
            backToMenu();
        }
    }

    @Override
    public void onSure() {
        int i = mZishuoFragment.getCheckedId();
        if (i == R.id.rb_sticker) {
            SysAlertDialog.showLoadingDialog(this, getString(R.string.isloading), false, null);
            mHandler.sendEmptyMessage(STICKER);
        } else if (i == R.id.rb_word) {
            mHandler.sendEmptyMessage(TEXT);
        } else if (i == R.id.rb_background) {
            mHandler.sendEmptyMessage(BACKGROUND);
        } else {
            backToMenu();
        }
    }

    /**
     * ???????????????
     */
    private void backToMenu() {
        if (mZishuoFragment == null) {
            mZishuoFragment = ZishuoFragment.newInstance();
            mZishuoFragment.setMenuListener(mListener);
        }
        mZishuoFragment.resetMenu();
        changeToFragment(mZishuoFragment, false);
        //??????UI
        mRlTopmenu.setVisibility(View.VISIBLE);
        mLlPlayFullScreen.setVisibility(View.VISIBLE);
    }

    /**
     * ??????titlebar
     */
    private void hideTitlebar() {
        mRlTopmenu.setVisibility(View.INVISIBLE);
        mLlPlayFullScreen.setVisibility(View.INVISIBLE);
    }

    /**
     * ??????
     *
     * @param isFull true?????? false????????????
     */
    private void fullScreen(boolean isFull) {
        if (isFull) {
            mRlTopmenu.setVisibility(View.GONE);
            if (CoreUtils.hasIceCreamSandwich()) {
                // ??????????????????????????????
                mContent.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            }
            findViewById(R.id.tmp).setVisibility(View.GONE);
            findViewById(R.id.rl_zishuo_fragment).setVisibility(View.GONE);
            mIsFullScreen = true;
            mHandler.sendEmptyMessageAtTime(FULL_SCREEN, 3 * 1000);
            if (mVirtualVideoView.getVideoWidth() > mVirtualVideoView.getVideoHeight()) {
                if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                }
            } else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mLlPlayFullScreen.getLayoutParams();
            lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            lp.bottomMargin = 0;
            mIvFullScreen.setBackgroundResource(R.drawable.edit_intercept_revert);
        } else {
            mRlTopmenu.setVisibility(View.VISIBLE);
            if (CoreUtils.hasIceCreamSandwich()) {
                mContent.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            }
            findViewById(R.id.tmp).setVisibility(View.VISIBLE);
            findViewById(R.id.rl_zishuo_fragment).setVisibility(View.VISIBLE);
            mIsFullScreen = false;

            if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mLlPlayFullScreen.getLayoutParams();
            lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0);
            lp.bottomMargin = 0;
            mIvFullScreen.setBackgroundResource(R.drawable.edit_intercept_fullscreen);
        }
    }

    /*
     * ?????????????????????
     */
    private MusicFragmentEx.IMusicListener mMusicListener = new MusicFragmentEx.IMusicListener() {

        @Override
        public void onVoiceChanged(boolean isChecked) {
            if (isChecked) {
                mParamDataImp.setMediaMute(false);
            } else {
                mParamDataImp.setMediaMute(true);
            }
            List<MediaObject> list = mScene.getAllMedia();
            if (null != list && list.size() > 0) {
                for (int j = 0; j < list.size(); j++) {
                    if (list.get(j).getMediaType() == MediaType.MEDIA_VIDEO_TYPE) {
                        list.get(j).setAudioMute(mParamDataImp.isMediaMute());
                    }
                }
            }
            reload(false);
            start();
        }

        @Override
        public void onVoiceClick(View v) {

        }

    };

    /**
     * ????????????
     *
     * @param msec ??????
     * @return
     */
    private String getFormatTime(int msec) {
        return DateTimeUtils.stringForMillisecondTime(msec, true, true);
    }

    /**
     * ????????????
     */
    private void playOrPause() {
        if (mVirtualVideoView.isPlaying()) {
            pause();
        } else {
            start();
        }
    }

    @Override
    public void reload(boolean onlyMusic) {
        build(onlyMusic);
    }

    @Override
    public boolean isPlaying() {
        return mVirtualVideoView.isPlaying();
    }

    /**
     * ??????
     */
    public void start() {
        mVirtualVideoView.start();
        mIvVideoPlayState.setImageResource(R.drawable.btn_edit_pause);
    }

    @Override
    public VirtualVideo getEditorVideo() {
        return mVirtualVideo == null ? null : mVirtualVideo;
    }

    /**
     * ????????????
     */
    public void pause() {
        mVirtualVideoView.pause();
        mIvVideoPlayState.setImageResource(R.drawable.btn_edit_play);
    }

    /**
     * ??????
     */
    public void stop() {
        mVirtualVideoView.stop();
        seekTo(0);
        mIvVideoPlayState.setImageResource(R.drawable.btn_edit_play);
    }

    /**
     * ??????
     *
     * @param msec ??????
     */
    public void seekTo(int msec) {
        onSeekTo(msec);
        mVirtualVideoView.seekTo(Utils.ms2s(msec));
    }

    @Override
    public int getDuration() {
        return mVirtualVideoView == null ? 1 : Utils.s2ms(mVirtualVideoView.getDuration());
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
    public VirtualVideoView getEditor() {
        if (mVirtualVideoView != null) {
            return mVirtualVideoView;
        } else {
            return null;
        }
    }

    @Override
    public FrameLayout getSubEditorParent() {
        return mFlSticker;
    }

    private VirtualVideo mSnapshotEditor;

    @Override
    public VirtualVideo getSnapshotEditor() {
        //???????????????????????????????????????
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

    /*
     * ??????EditorPreivewPositionListener?????????
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
    public void removeMvMusic(boolean remove) {
        VirtualVideo video;
        mParamDataImp.setRemoveMVMusic(remove);
        if (null != (video = getEditorVideo())) {
            video.removeMVMusic(remove);
        }
    }

    @Override
    public void onBackPressed() {
        int checkedId = mZishuoFragment.getCheckedId();
        if (checkedId == R.id.rb_sound_effect) {
            mMusicEffectFragment.onBackPressed();
            return;
        } else if (checkedId == R.id.rb_sticker) {
            mStickerFragment.onBackPressed();
            return;
        } else if (checkedId == R.id.rb_music) {
            onBack();
            return;
        } else if (checkedId == R.id.rb_background) {
            mBackgroundFragment.onBackPressed();
            return;
        } else if (checkedId == R.id.rb_word) {
            mTextFragment.onBackPressed();
            return;
        } else if (mIsFullScreen) {
            fullScreen(false);
            mLlPlayFullScreen.setVisibility(View.VISIBLE);
            return;
        }
        TempVideoParams.getInstance().setAspectRatio(
                TempVideoParams.mEditingVideoAspectRatio);
        onShowAlert();
    }

    //????????????????????????
    private int STICKER = 20;
    private final int FULL_SCREEN = 100;
    private final int TEXT = 101;
    private final int BACKGROUND = 102;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == STICKER) {
                backToMenu();
                build(false);
                seekTo(0);
                start();
            } else if (msg.what == FULL_SCREEN) {
                if (mIsFullScreen) {
                    mLlPlayFullScreen.setVisibility(View.GONE);
                } else {
                    mLlPlayFullScreen.setVisibility(View.VISIBLE);
                }
            } else if (msg.what == TEXT) {
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.remove(mTextFragment);
                ft.commit();
                findViewById(R.id.ll_text_fragment).setVisibility(View.GONE);
                mZishuoFragment.resetMenu();
                build(false);
                seekTo(0);
                start();
            } else if (msg.what == BACKGROUND) {
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.remove(mBackgroundFragment);
                ft.commitAllowingStateLoss();
                findViewById(R.id.galleryFragmentParent).setVisibility(View.GONE);
                findViewById(R.id.edit_video_layout).setVisibility(View.VISIBLE);
                mLlAlpha.setVisibility(View.GONE);
                build(false);
                seekTo(0);
                start();
                backToMenu();
            }
            return false;
        }
    });

    /**
     * ????????????????????????
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
    public void onBackgroundModeChanged(boolean isEnableBg) {
        for (MediaObject mediaObject : mScene.getAllMedia()) {
            if (isEnableBg) {
                mediaObject.setAspectRatioFitMode(AspectRatioFitMode.KEEP_ASPECTRATIO);
            } else {
                mediaObject.setAspectRatioFitMode(AspectRatioFitMode.KEEP_ASPECTRATIO_EXPANDING);
            }
        }
        reload(false);
    }

    private boolean isBulr = true;

    @Override
    public void onBackgroundColorChanged(int color) {
        boolean needReload = false;
        for (MediaObject mediaObject : mScene.getAllMedia()) {
            if (color == VisualFilterConfig.FILTER_ID_GAUSSIAN_BLUR) {
                if (!isBulr) {
                    needReload = true;
                }
                isBulr = true;
                mediaObject.setBackgroundFilterType(color, 0.1f);
                mediaObject.setBackgroundVisiable(true);
            } else {
                if (isBulr) {
                    needReload = true;
                }
                isBulr = false;
                mediaObject.setBackgroundVisiable(false);
            }
        }
        mVirtualVideoView.setBackgroundColor(color);
        if (needReload) {
            reload(false);
        }
        start();
    }

    public static final int REQUSET_MUSICEX = 1000;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUSET_MUSICEX) {
            if (null != mMusicFragmentEx) {
                mVirtualVideo.reset();
                if (resultCode == Activity.RESULT_OK) {
                    mVirtualVideoView.reset();
                }
                mMusicFragmentEx.onActivityResult(requestCode, resultCode, data);
            }
        } else if (requestCode == BackgroundZishuoFragment.REQUESTCODE_FOR_ADD_MEDIA) {
            if (null != mBackgroundFragment) {
                mBackgroundFragment.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_READ_EXTERNAL_STORAGE_PERMISSIONS: {
                for (int i = 0; i < permissions.length; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        init();
                    } else {
                        SysAlertDialog.showAutoHideDialog(this, null, getString(R.string.un_allow_video_photo), Toast.LENGTH_SHORT);
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
    public boolean isMediaMute() {
        return mParamDataImp.isMediaMute();
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

    @Override
    public IParamData getParamData() {
        return mParamDataImp;
    }

    @Override
    public void changeMusicFilter() {
        if (null != mVirtualVideoView) {
            mVirtualVideo.setMusicFilter(MusicFilterType.valueOf(mParamDataImp.getSoundEffectId()));
            if (!isPlaying()) {
                start();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SysAlertDialog.cancelLoadingDialog();
        SubUtils.getInstance().recycle();
        TTFData.getInstance().close();
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
        mTextFragment = null;
        mZishuoFragment = null;
        mStickerFragment = null;
        mMusicEffectFragment = null;
        mMusicFragmentEx = null;
        mBackgroundFragment = null;
        TempVideoParams.getInstance().recycle();
        mParamDataImp = null;
        mStickerFragment = null;
        TempZishuoParams.getInstance().recycle();
        mHandler.removeCallbacksAndMessages(null);
    }

    /**
     * AE?????????
     */
    @Override
    public void changeAnimation(int animation) {

    }

    @Override
    public void setAETemplateInfo(AETemplateInfo aeTemplateInfo) {

    }

    @Override
    public void onProportionChanged(float aspect) {

    }
}
