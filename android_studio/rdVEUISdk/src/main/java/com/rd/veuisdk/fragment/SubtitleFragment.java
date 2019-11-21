package com.rd.veuisdk.fragment;

import android.content.DialogInterface;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.rd.lib.ui.ExtButton;
import com.rd.lib.utils.CoreUtils;
import com.rd.lib.utils.InputUtls;
import com.rd.lib.utils.ThreadPoolUtils;
import com.rd.vecore.VirtualVideo;
import com.rd.vecore.VirtualVideoView;
import com.rd.vecore.exception.InvalidArgumentException;
import com.rd.vecore.models.Scene;
import com.rd.vecore.models.caption.CaptionAnimation;
import com.rd.vecore.models.caption.CaptionObject;
import com.rd.vecore.utils.MiscUtils;
import com.rd.veuisdk.IVideoEditorHandler.EditorPreivewPositionListener;
import com.rd.veuisdk.R;
import com.rd.veuisdk.TTFHandler;
import com.rd.veuisdk.TempVideoParams;
import com.rd.veuisdk.adapter.StyleAdapter;
import com.rd.veuisdk.adapter.SubtitleAdapter;
import com.rd.veuisdk.adapter.TTFAdapter;
import com.rd.veuisdk.database.TTFData;
import com.rd.veuisdk.demo.VideoEditAloneActivity;
import com.rd.veuisdk.fragment.helper.CaptionAnimHandler;
import com.rd.veuisdk.fragment.helper.CaptionPositionHandler;
import com.rd.veuisdk.listener.OnItemClickListener;
import com.rd.veuisdk.model.StyleInfo;
import com.rd.veuisdk.model.WordInfo;
import com.rd.veuisdk.model.bean.FindText;
import com.rd.veuisdk.mvp.model.ISSCallBack;
import com.rd.veuisdk.mvp.model.SubtitleFragmentModel;
import com.rd.veuisdk.net.SubUtils;
import com.rd.veuisdk.ui.CaptionDrawRect;
import com.rd.veuisdk.ui.CircleProgressBarView;
import com.rd.veuisdk.ui.ColorDragScrollView;
import com.rd.veuisdk.ui.ColorPicker.IColorListener;
import com.rd.veuisdk.ui.IThumbLineListener;
import com.rd.veuisdk.ui.PopViewUtil;
import com.rd.veuisdk.ui.SubInfo;
import com.rd.veuisdk.utils.AppConfiguration;
import com.rd.veuisdk.utils.CommonStyleUtils;
import com.rd.veuisdk.utils.DateTimeUtils;
import com.rd.veuisdk.utils.SysAlertDialog;
import com.rd.veuisdk.utils.Utils;
import com.rd.veuisdk.videoeditor.widgets.IViewTouchListener;
import com.rd.veuisdk.videoeditor.widgets.ScrollViewListener;

import java.util.ArrayList;
import java.util.List;

/**
 * 字幕
 */
public class SubtitleFragment extends SSBaseFragment<WordInfo, SubtitleAdapter, SubtitleFragmentModel> {

    private final String DEFAULT_STYLE_CODE = "text_sample";
    private final String DEFAULT_STYLE_CODE_VER = "text_vertical";
    private static final String PARAM_SUBURL = "param_subUrl";
    private static final String PARAM_TTFURL = "param_ttfurl";

    public static SubtitleFragment newInstance(String subUrl, String ttfUrl) {
        SubtitleFragment subtitleFragment = new SubtitleFragment();
        Bundle bundle = new Bundle();
        bundle.putString(PARAM_SUBURL, subUrl);
        bundle.putString(PARAM_TTFURL, ttfUrl);
        subtitleFragment.setArguments(bundle);
        return subtitleFragment;
    }

    public SubtitleFragment() {
        super();
    }

    private View mTreeView;
    private VirtualVideo mVirtualVideo;
    private String subUrl = null;
    private String ttfUrl = null;

    private EditText mEtSubtitle;
    private RadioGroup mRgMenu;

    private FrameLayout mLinearWords;
    private VirtualVideoView mPlayer;
    private RecyclerView mGvSubtitleStyle;
    private GridView mGvTTF;
    private View mStyleLayout, mTTFLayout, mColorLayout, mSizeLayout,
            mAnimLayout, mStrokeLayout, mPositionLayout;

    private ImageView mIvClear;
    private boolean mIsUpdate = false;
    private int mLayoutWidth = 1024, mLayoutHeight = 1024;

    private ExtButton mTvSave;
    private int mStateSize = 0;
    private CaptionDrawRect mCaptionDrawRect;

    private CheckBox mCbApplyToAll;
    private SeekBar mSbSubtitleSize;
    private SeekBar mSbColorAlpha, mSbStrokeWidth;
    private CheckBox mSbBlod, mSbItalic, mSbShadow;
    private TextView mTvColorAlphaPercent;
    private TextView mTvSubtitleSize;
    private View mLlWordEditer;
    private ColorDragScrollView mColorScrollView, mStrokeColorView;


    private final int STYLE = 0;
    private final int ANIM = 1;
    private final int COLOR = 2;
    private final int STROKE = 3;
    private final int FONT = 4;
    private final int SIZE = 5;
    private final int POSITION = 6;

    private int mCurLayoutIndex = 0;

    private boolean mStyleApplyToAll, mAnimApplyToAll, mColorApplyToAll, mStrokeApplyToAll,
            mFontApplyToAll, mSizeApplyToAll, mPositionApplyToAll;


    private CaptionAnimHandler mCaptionAnimHandler;
    private CaptionPositionHandler mCaptionPositionHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TAG = "SubtitleFragment";
        Bundle bundle = getArguments();
        this.subUrl = bundle.getString(PARAM_SUBURL);
        this.ttfUrl = bundle.getString(PARAM_TTFURL);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.fragment_subtitle_layout, container, false);
        mModel = new SubtitleFragmentModel(getContext());
        mCurrentInfo = null;
        return mRoot;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
        init();
        checkTitleLayout();
    }

    @Override
    SubtitleAdapter initAdapter() {
        return new SubtitleAdapter();
    }

    /**
     * 选中单个(准备编辑)
     */
    @Override
    void onAdapterItemClick(int position, WordInfo info) {
        //跳到指定位置
        mEditorHandler.seekTo((int) info.getStart() + 1);  // 额外加1ms
        onScrollProgress((int) info.getStart() + 1);

        //选中当前片段(编辑)
        mCurrentInfo = new WordInfo(info);
        try {
            mCurrentInfo.getCaptionObject().setVirtualVideo(mVirtualVideo, mPlayer);
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        }
        //当前编辑项高亮
        mThumbNailLine.showCurrent(mCurrentInfo.getId());
        btnDel.setVisibility(View.VISIBLE);
        btnEdit.setText(R.string.edit);
        btnEdit.setVisibility(View.VISIBLE);
    }

    /**
     * 公共组件设置
     */
    @Override
    void initListener() {
        mScrollView.addScrollListener(mThumbOnScrollListener);
        mScrollView.setViewTouchListener(mViewTouchListener);
        mThumbNailLine.setSubtitleThumbNailListener(mSubtitleListener);
    }

    /**
     * 新增单个
     */
    @Override
    void onBtnAddClick() {
        if (!bUIPrepared) {
            Log.e(TAG, "onAddListener: recovering sub data ...");
            return;
        }
        if (mEditorHandler != null && mEditorHandler.isPlaying()) {
            pauseVideo();
        }
        PopViewUtil.cancelPopWind();
        if (null == mStyleAdapter || mStyleAdapter.getCount() == 0) {
            int re = CoreUtils.checkNetworkInfo(mContext);
            if (re == CoreUtils.UNCONNECTED) {
                onToast(R.string.please_check_network);
            } else {
                SysAlertDialog.showLoadingDialog(mContext,
                        mContext.getString(R.string.isloading));
                getData(true);
            }
        } else {
            /**
             * 新增字幕
             */
            int progress = mScrollView.getProgress();

            int header = TempVideoParams.getInstance().getThemeHeader();
            if (progress < header) {
                onToast(R.string.addsub_video_head_failed);
                return;
            }
            int last = TempVideoParams.getInstance().getThemeLast();
            if (progress > mDuration - last) {
                onToast(R.string.addsub_video_end_failed);
                return;
            }

            if (!mThumbNailLine.canAddSub(progress, mDuration, mSizeParams[0],
                    header, last)) {
                onToast(R.string.addsub_video_between_failed);
                return;
            }
            mIsAddCaption = true;
            mCurrentInfo = new WordInfo();
            try {
                mCurrentInfo.getCaptionObject().setVirtualVideo(mVirtualVideo, mPlayer);
            } catch (InvalidArgumentException e) {
                e.printStackTrace();
            }
            mCurrentInfo.setStart(progress);
            mCurrentInfo.setId(Utils.getWordId());

            int end = progress;

            //默认添加到整条时间线，预览时通过GL实现预览,所以必须设置一个大于当前时刻的结束点，待保存时重新修正结束点
            mCurrentInfo.setEnd(Utils.s2ms(mPlayer.getDuration()));
            mThumbNailLine.addRect(progress, end + 3000, "", mCurrentInfo.getId());

            mEtSubtitle.setText("");
            StyleInfo styleInfo = mStyleAdapter.getItem(0);
            if (styleInfo.isdownloaded) {
                mCurrentInfo.setStyleId(styleInfo.pid);
            }
            onStartSub(styleInfo.isdownloaded, true);
            mHandler.removeMessages(MSG_ONSTYLE_IMP);
            mHandler.sendEmptyMessageDelayed(MSG_ONSTYLE_IMP, 500);
        }
    }

    /**
     * 保存并退出
     */
    @Override
    void onBtnRightClick() {
        pauseVideo();
        if (mAdapter.getChecked() >= 0) {
            //保存参数并退出编辑模式
            resetUI();
        } else {
            onBackToActivity(true);
            TempVideoParams.getInstance().setSubs(mList);
            mEditorHandler.onSure();
        }
    }

    /**
     * 编辑按钮
     */
    @Override
    void onEditClick() {
        onEditWordImp();
    }

    @Override
    void onAutoRecognition() {
        PopViewUtil.cancelPopWind();
        if (CoreUtils.checkNetworkInfo(getContext()) == CoreUtils.UNCONNECTED) {
            onToast(R.string.please_check_network);
            return;
        }
        //step 1:  提取音频文件
        if (null != mExtractAudio && null != mAudioSceneList && mAudioSceneList.size() > 0) {
            SysAlertDialog.showLoadingDialog(getContext(), R.string.auto_recognition_ing);
            mModel.onAI(getContext(), mAudioSceneList, new SubtitleFragmentModel.IAudioAutoRecognitionCallBack() {
                @Override
                public void onResult(List<FindText.TextInfo> list, String error) {
                    if (isRunning) {
                        SysAlertDialog.cancelLoadingDialog();
                        if (null != list && list.size() > 0) {
                            bShowAutoRecognition = false;
                            checkEditUIGone();
                            int len = list.size();
                            for (int i = 0; i < len; i++) {
                                createWord(list.get(i));
                            }
                            checkTitleLayout();
                            ((SubtitleAdapter) mAdapter).addAll(mList, -1);
                        } else {
                            if (!TextUtils.isEmpty(error)) {
                                onToast(error);
                                //隐藏ai功能
                                bShowAutoRecognition = false;
                                checkEditUIGone();
                            } else {
                                onToast(R.string.auto_recognition_failed);
                            }
                        }
                    }
                }
            });
        } else {
            onToast(R.string.auto_recognition_failed);
        }
    }


    /***
     * 是否需要启用AI功能
     */
    @Override
    boolean checkEnableAI() {
        if (bShowAutoRecognition && (null != mAudioSceneList && mAudioSceneList.size() > 0)) {
            return mModel.checkEnableAI(mAudioSceneList);
        } else {
            return false;
        }
    }

    /**
     * 构建默认的字幕样式(防止所有字幕样式未下载)
     */

    private void createWord(FindText.TextInfo textInfo) {
//        Log.e(TAG, "createWord: " + textInfo);
        WordInfo wordInfo = new WordInfo();
        try {
            wordInfo.setId(wordInfo.hashCode());
            wordInfo.getCaptionObject().setVirtualVideo(mVirtualVideo, mPlayer);
            wordInfo.setTimelineRange(MiscUtils.s2ms(textInfo.getStart()), MiscUtils.s2ms(textInfo.getEnd()));
            int styleId = DEFAULT_STYLE_CODE.hashCode();
            wordInfo.setText(textInfo.getText());

            String tmp = textInfo.getText();
            String lineText = addLine(tmp);
            wordInfo.setInputText(tmp, lineText);

            wordInfo.setStyleId(styleId);
            //构建默认的样式 (与网络字幕中的text_sample 的json等效)
            StyleInfo styleInfo = mModel.initDefaultStyle(getContext());
            setCommonStyleImp(styleInfo, wordInfo);
            wordInfo.getCaptionObject().setCenter(new PointF(0.5f, 0.8f));
            wordInfo.setDisf(1.6f);
            wordInfo.getCaptionObject().apply(true);
            mThumbNailLine.addRect((int) wordInfo.getStart(), (int) wordInfo.getEnd(), wordInfo.getText(), wordInfo.getId());
            mList.add(wordInfo);
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        }

    }


    private List<Scene> mAudioSceneList = null;

    public void setExtractAudio(@NonNull IExtractAudio extractAudio) {
        mExtractAudio = extractAudio;
        mAudioSceneList = extractAudio.getAudioSceneList();
    }

    private IExtractAudio mExtractAudio;

    public interface IExtractAudio {

        /**
         * 提取场景中音频文件
         */
        List<Scene> getAudioSceneList();

    }


    /**
     * 删除按钮
     */
    @Override
    void onDeleteClick() {
        if (null != mCurrentInfo) {
            onDelWordItem(mCurrentInfo.getId());
        }
    }


    private void initView() {
        mGvSubtitleStyle = $(R.id.style_sub);
        mGvTTF = $(R.id.gridview_ttf);
        mLlWordEditer = $(R.id.thelocation);
        $(R.id.ivAddSubCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //放弃新增
                onMenuBackClick();
            }
        });
        $(R.id.ivAddSubSure).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAddSubtitle();
                checkTitleLayout();
            }
        });

        mMenuLayout = $(R.id.subtitle_menu_layout);

        mStateSize = mContext.getResources().getDimensionPixelSize(R.dimen.add_sub_play_state_size);
        mStyleLayout = $(R.id.subtitle_style_layout);
        mTTFLayout = $(R.id.subtitle_ttf_layout);
        mColorLayout = $(R.id.subtitle_color_layout);
        mSizeLayout = $(R.id.subtitle_size_layout);
        mAnimLayout = $(R.id.subtitle_anim_layout);
        mCaptionAnimHandler = new CaptionAnimHandler(mAnimLayout);
        mStrokeLayout = $(R.id.subtitle_stroke_layout);
        mPositionLayout = $(R.id.subtitle_position_layout);
        mCaptionPositionHandler = new CaptionPositionHandler();
        mIvClear = $(R.id.ivClear);
        mTvSave = $(R.id.subtitle_save);
        mTvSave.setOnClickListener(onSaveChangeListener);
        mEtSubtitle = $(R.id.subtitle_et);
        mRgMenu = $(R.id.subtitle_menu_group);

        mColorScrollView = $(R.id.scrollColorPicker);
        mStrokeColorView = $(R.id.scrollStrokePicker);
        mColorScrollView.setColorChangedListener(mColorPickListener);
        mStrokeColorView.setColorChangedListener(mStrokeColorPickListener);

        mCbApplyToAll = $(R.id.cbStApplyToAll);
        if (isHideAppToAll) {
            mCbApplyToAll.setVisibility(View.INVISIBLE);
        }
        mSbSubtitleSize = $(R.id.sbSubtitleSize);
        mSbColorAlpha = $(R.id.sbSubtitleColorAlpha);
        mSbStrokeWidth = $(R.id.sbStrokeWdith);

        mTvSubtitleSize = $(R.id.tvSubtitleSize);

        mSbBlod = $(R.id.cbSubtitleBold);
        mSbItalic = $(R.id.cbSubtitleItalic);
        mSbShadow = $(R.id.cbSubtitleShadow);

        mTvColorAlphaPercent = $(R.id.tvColorAlphaPercent);

        mCbApplyToAll.setOnCheckedChangeListener(mOnApplyToAllCheckedListener);
        mSbSubtitleSize.setOnSeekBarChangeListener(mOnSizeChangeListener);

        mSbColorAlpha.setOnSeekBarChangeListener(mOnColorAlphaChangeListener);
        mSbStrokeWidth.setOnSeekBarChangeListener(mOnStrokeWidthChangeListener);

        mIvClear.setOnClickListener(mClearSubtitle);

        mTreeView = getActivity().findViewById(android.R.id.content);
        mDisplay = CoreUtils.getMetrics();
        tvTitle.setText(R.string.subtitle);


    }


    private final int POSITION_DEFAULT = -1;
    private int nPositionId = POSITION_DEFAULT;
    /**
     * 字幕中心点改变
     */
    private CaptionPositionHandler.IPositionChangeListener mPositionChangeListener = new CaptionPositionHandler.IPositionChangeListener() {
        @Override
        public void onChangePosition(int id) {
            if (null != mCurrentInfo) {
                nPositionId = id;
                PointF pointF = mCaptionPositionHandler.getFixCenter(mCurrentInfo.getCaptionObject().getCaptionDisplayRectF(), nPositionId);
                if (null != pointF) {
                    //平移到指定位置
                    mCurrentInfo.getCaptionObject().setCenter(pointF);
                    onResetDrawRect();
                }
            }

        }
    };


    /***
     * 编辑状态下，给控制器设置回调并显示
     */
    private void registerDrawRectListener() {
        mCaptionDrawRect.SetOnAlignClickListener(new CaptionDrawRect.OnUIClickListener() {
            @Override
            public void onDeleteClick() {
                if (null != mCurrentInfo) {
                    onDelWordItem(mCurrentInfo.getId());
                }
                removeInputListener();
                InputUtls.hideKeyboard(mEtSubtitle);
            }

            @Override
            public void onAlignClick(int align) {
                mCurrentInfo.getCaptionObject().setTextAlignment(align);
                onResetDrawRect();
            }
        });
        mCaptionDrawRect.SetOnTouchListener(new CaptionDrawRect.OnTouchListener() {
            @Override
            public void onDrag(PointF prePointF, PointF nowPointF) {
                if (null != nowPointF && null != prePointF) {
                    if (null != mCurrentInfo) {
                        /**
                         * 偏移字幕
                         */
                        mCurrentInfo.getCaptionObject().offSetCenter((nowPointF.x - prePointF.x) / mLayoutWidth, (nowPointF.y - prePointF.y) / mLayoutHeight);
                        onResetDrawRect();
                    }
                }

            }

            @Override
            public void onScaleAndRotate(float offsetScale, float rotation) {
                if (null != mCurrentInfo) {
                    if (Math.abs(rotation) != 0) {
                        mCurrentInfo.getCaptionObject().rotateCaption(mCurrentInfo.getRotateAngle() - rotation);
                        onResetDrawRect();

                    }
                    if (offsetScale != 0) {
                        /**
                         * 缩放字幕，缩放变化量
                         */
                        float scale = mCurrentInfo.getDisf() + (mCurrentInfo.getDisf() * offsetScale);
                        if (scale <= CaptionObject.MIN_SCALE) {
                            scale = CaptionObject.MIN_SCALE;
                        } else if (scale >= CaptionObject.MAX_SCALE) {
                            scale = CaptionObject.MAX_SCALE;
                        }
                        mSbSubtitleSize.setProgress((int) ((scale - CaptionObject.MIN_SCALE) * 100 / (CaptionObject.MAX_SCALE - CaptionObject.MIN_SCALE)));
                        mCurrentInfo.getCaptionObject().offSetScale(offsetScale);
                        onResetDrawRect();
                    }
                }
            }
        });
    }


    /**
     * 退出编辑模式时，解绑回调并影藏控制器
     */
    private void unRegisterDrawRectListener() {
        mCaptionDrawRect.setVisibleUI(false);
        mCaptionDrawRect.SetOnAlignClickListener(null);
        mCaptionDrawRect.SetOnTouchListener(null);

    }

    /**
     * 每次缩放，字体样式，描边，旋转之后都要重新设置控制器的新坐标
     */
    private void onResetDrawRect() {
        if (null != mCurrentInfo) {
            //设置控制器的坐标
            mCaptionDrawRect.SetDrawRect(mCurrentInfo.getCaptionObject().getListPoint());
        }
    }


    private TTFHandler.ITTFHandlerListener mTTFListener = new TTFHandler.ITTFHandlerListener() {

        @Override
        public void onItemClick(String ttf, int position) {
            if (null != mCurrentInfo) {
                if (ttf.equals(mContext.getString(R.string.default_ttf))) {
                    if (null != mCurrentInfo) {
                        mCurrentInfo.setTtfLocalPath(null);
                        onResetDrawRect();
                    }
                    mTTFHandler.ToReset();
                } else {
                    if (null != mCurrentInfo) {
                        mCurrentInfo.setTtfLocalPath(ttf);
                        onResetDrawRect();
                    }
                }

            }
        }
    };


    private IViewTouchListener mViewTouchListener = new IViewTouchListener() {

        @Override
        public void onActionDown() {
            mEditorHandler.pause();
            if (mIsAddCaption) {
                onWordEnd();
            } else {
                int progress = mScrollView.getProgress();
                mEditorHandler.seekTo(progress);
                setProgressText(progress);
            }
        }

        @Override
        public void onActionMove() {
            int progress = mScrollView.getProgress();
            mEditorHandler.seekTo(progress);
            setProgressText(progress);
        }

        @Override
        public void onActionUp() {
            mScrollView.resetForce();
            int progress = mScrollView.getProgress();
            setProgressText(progress);
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        if (null != mCurrentInfo) {
            //设置控制器的坐标
            mCurrentInfo.getCaptionObject().onResume();
        }
    }


    @Override
    public void onDestroyView() {
        mRgMenu.setOnCheckedChangeListener(null);
        mSbBlod.setOnCheckedChangeListener(null);
        mSbItalic.setOnCheckedChangeListener(null);
        mSbShadow.setOnCheckedChangeListener(null);
        mScrollView.removeScrollListener(mThumbOnScrollListener);
        mScrollView.setViewTouchListener(null);
        super.onDestroyView();
        removeInputListener();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        bUIPrepared = false;
        mHandler.removeCallbacks(resetSubDataRunnable);
        if (mThumbNailLine != null) {
            mThumbNailLine.recycle(true);
        }
        mHandler.removeMessages(MSG_LISTVIEW);
        mCaptionAnimHandler = null;
    }

    @Override
    IntentFilter createIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(TTFAdapter.ACTION_TTF);
        intentFilter.addAction(StyleAdapter.ACTION_SUCCESS_CAPTION);
        intentFilter.addAction(StyleAdapter.ACTION_HAS_DOWNLOAD_ING);
        return intentFilter;
    }

    @Override
    void onDownload(boolean isDownloadLoading) {
        mIsDownloading = isDownloadLoading;
        // 防止下载完成设置adapter.setcheck(int p)无法notifi
        mStyleAdapter.notifyDataSetChanged();
    }

    @Override
    void onStyleItemDownloaded(int position) {
        if (-1 != position && mMenuLayout.getVisibility() == View.VISIBLE) {
            if (null != mCurrentInfo) {
                StyleInfo info = mStyleAdapter.getItem(position);
                if (null != info) {
                    mCurrentInfo.setStyleId(info.pid);
                }
            }
            if (null != mCurrentInfo) {
                //再重新出事新的字幕，用新的样式
                initItemSub(false);
                onStyleItem(position, mCurrentInfo);
            } else {
                Log.e(TAG, "onReceive: 当前没有字幕");
            }
        }
    }

    @Override
    void onTTFDownloaded(String path) {
        if (null != mCurrentInfo && mTTFLayout.getVisibility() == View.VISIBLE) {
            mCurrentInfo.setInputTTF(path);
            onResetDrawRect();
        }
    }

    private OnItemClickListener mStyleItemlistener = new OnItemClickListener<StyleInfo>() {
        @Override
        public void onItemClick(int position, StyleInfo item) {
            onStyleItem(position, mCurrentInfo);
        }
    };


    private void onStyleItem(int position, WordInfo winfo) {

        StyleInfo tInfo = mStyleAdapter.getItem(position);
        if (null == tInfo) {
            return;

        }
//        Log.e(TAG, "onStyleItem: " + position + ">>" + winfo.getText() + " tInfo:" + tInfo);
        if (tInfo.isdownloaded) {
            if (null == winfo) {
                Log.e(TAG, "onStyleItem:  winfo is null");
                return;
            }
            winfo.setStyleId(tInfo.pid);
            mStyleAdapter.setCheckItem(position);
            int index = getIndex(winfo.getId());
            if (index >= 0) {
                mCurrentInfo.setRotateAngle(winfo.getRotateAngle());
            } else {
                mCurrentInfo.setRotateAngle(tInfo.rotateAngle);
            }
            setCommonStyleImp(tInfo, mCurrentInfo);
            setTextChangedByInputManager(false);
            if (tInfo.type == 0) {
                //可以写字
                if (tInfo.vertical) {
                    //竖排字幕
                    //禁用换行符
                    mEtSubtitle.setSingleLine(true);
                    String text = winfo.getInputTextHor();
                    if (!TextUtils.isEmpty(text)) {
                        //输入框横排模式显示输入的内容
                        setEditInputText(text);
                        mCurrentInfo.setText(winfo.getInputTextVer());
                    } else {
                        if (tInfo.code.contains(DEFAULT_STYLE_CODE_VER) || TextUtils.isEmpty(tInfo.getHint())) {
                            setEditInputText("");
                        } else {
                            setEditInputText(tInfo.getHint());
                        }
                        mCurrentInfo.setText(addLine(tInfo.getHint()));
                    }
                } else {
                    //横排字幕
                    //允许换行
                    mEtSubtitle.setSingleLine(false);
                    String text = winfo.getInputTextHor();

                    if (!TextUtils.isEmpty(text)) {
                        mCurrentInfo.setText(text);
                        //手动输入的内容（横排版排列）
                        setEditInputText(text);
                    } else {
                        if (tInfo.code.contains(DEFAULT_STYLE_CODE)) {
                            setEditInputText("");
                        } else {
                            setEditInputText(tInfo.getHint());
                        }
                        mCurrentInfo.setText(tInfo.getHint());
                    }
                }
                int color = (mCurrentInfo.getInputTextColor() == Color.WHITE ? tInfo.getTextDefaultColor() : mCurrentInfo.getInputTextColor());
                mCurrentInfo.setTextColor(color);
            } else {
                mCurrentInfo.setText(winfo.getInputText());
            }
            setTextChangedByInputManager(true);
            onResetDrawRect();
            mCaptionDrawRect.setVisibleUI(true);
        } else {
            // 执行下载
            int visiblep = position % Math.max(1, mGvSubtitleStyle.getChildCount());
            View child = mGvSubtitleStyle.getChildAt(visiblep);
            if (null != child) {
                mStyleAdapter.onDown(position, (ImageView) Utils.$(child, R.id.ttf_state), (CircleProgressBarView) Utils.$(child, R.id.ttf_pbar));
            } else {
                Log.e(TAG, "onStyleItem: " + visiblep + ">>>" + mGvSubtitleStyle.getChildCount());
            }

        }

    }

    /**
     * 给输入框设置内容
     *
     * @param text 横排排版下的文本
     */
    private void setEditInputText(String text) {
        mEtSubtitle.setText(text);
        mEtSubtitle.setSelection(text.length());
    }


    private void setShadowColor(int color) {
        mCurrentInfo.setShadowColor(color);
        onResetDrawRect();

    }

    /***
     *应用到全部
     * @return
     */
    private boolean needApplyToAll() {
        return mStyleApplyToAll || mColorApplyToAll || mStrokeApplyToAll || mFontApplyToAll || mSizeApplyToAll || mPositionApplyToAll;
    }

    /***
     * 应用所有属性到全部字幕
     * @param temp  排除当前编辑的字幕
     */
    private void applyToAll(WordInfo temp) {
//        Log.e(TAG, "applyToAll: " + mList.size());
        for (WordInfo info : mList) {
            if (null != info && info.getId() == temp.getId()) {
                continue;
            }
            boolean needBuild = false;
            if (mStyleApplyToAll) {
                needBuild = true;
                int position = mStyleAdapter.getPosition(temp.getStyleId());
                StyleInfo tInfo = mStyleAdapter.getItem(position);
                if (null == tInfo) {
                    return;
                }
                info.setStyleId(tInfo.pid);
                setCommonStyleImp(tInfo, info);

            }

            if (mColorApplyToAll) {
                needBuild = true;
                info.setInputTextColor(temp.getInputTextColor());
                info.setInputTextColorAlpha(temp.getInputTextColorAlpha());
            }
            if (mStrokeApplyToAll) {
                needBuild = true;
                info.setShadowColor(temp.getShadowColor());
                info.setInputTextStrokeAlpha(temp.getInputTextStrokeAlpha());
                info.setInputTextStrokeWidth(temp.getInputTextStrokeWidth());
            }
            if (mFontApplyToAll) {
                needBuild = true;
                info.setBold(temp.isBold());
                info.setItalic(temp.isItalic());
                info.setShadow(temp.isShadow());
                info.setTtfLocalPath(temp.getTtfLocalPath());
            }
            if (mSizeApplyToAll) {
                needBuild = true;
                info.setDisf(temp.getDisf());
            }


            if (mPositionApplyToAll) {
                if (nPositionId != POSITION_DEFAULT) {
                    needBuild = true;
                    //******需要根据对齐方式，结合当前item字幕之前的位置做相对的平移
                    RectF rectF = info.getCaptionObject().getCaptionDisplayRectF();
                    PointF pointF = mCaptionPositionHandler.getFixCenter(rectF, nPositionId);
                    //重置字幕的显示中心点 （字幕边框有出入，只能保证中心点在同一个位置）
                    info.getCaptionObject().setCenter(pointF);
                }
            }
//            Log.e(TAG, "applyToAll: mPositionApplyToAll: " + mPositionApplyToAll + "  mAnimApplyToAll:" + mAnimApplyToAll + ">>needBuild:" + needBuild);

            if (needBuild) {
                try {
                    //构建字幕对象，并更新到虚拟视频
                    //如果要引用动画，此处就不用实时更新虚拟视频(***********因为:应用动画时，必须再次更新到虚拟视频)
                    boolean updateCaption = true;

                    if (mAnimApplyToAll) {
                        updateCaption = false;
                    } else {
                        updateCaption = true;
                        if (mPositionApplyToAll) {
                            updateCaption = false;
                        }
                    }
                    //防止频繁的修改虚拟视频
                    info.getCaptionObject().apply(updateCaption);
                } catch (InvalidArgumentException e) {
                    e.printStackTrace();
                }
            }
            //必须放到apply之后再重新构造动画，因为构造动画需要依赖字幕的位置

            if (mAnimApplyToAll) {
                //**********************移动动画，跟字幕预览时的位置有关，需通过预览时的坐标(apply()会修正字幕的显示位置），计算，字幕的动画的起始位置
                CaptionAnimation animation = mCaptionAnimHandler.getAnimation(info.getCaptionObject().getCaptionDisplayRectF());
                //设置动画
                info.setAnimType(animation, mCaptionAnimHandler.getCheckedId());
                //更新字幕到虚拟视频
                info.getCaptionObject().updateCaption(mVirtualVideo);
            } else {
                if (mPositionApplyToAll) {
                    //***********************重置各个对象原来的动画,因为位置有可能已经变化了,动画需要重新构建

                    int checkId = info.getCheckId();
                    CaptionAnimation animation = mCaptionAnimHandler.getAnimation(info.getCaptionObject().getCaptionDisplayRectF(), checkId);
                    //设置动画
                    info.setAnimType(animation, checkId);
                    //更新字幕到虚拟视频
                    info.getCaptionObject().updateCaption(mVirtualVideo);
                }
            }
        }


        mStyleApplyToAll = false;
        mAnimApplyToAll = false;
        mColorApplyToAll = false;
        mStrokeApplyToAll = false;
        mFontApplyToAll = false;
        mSizeApplyToAll = false;
        mPositionApplyToAll = false;
        nPositionId = POSITION_DEFAULT;
    }


    private IThumbLineListener mSubtitleListener = new IThumbLineListener() {

        private int tempId, tempStart, tempEnd;

        @Override
        public void onTouchUp() {
            int index = getIndex(tempId);
            if (index >= 0) {
                WordInfo info = mList.get(index);
                if (null != info && tempId == info.getId()) {
                    if (mEditorHandler != null) {
                        mEditorHandler.pause();
                    }
                    info.getCaptionObject().setTimelineRange(Utils.ms2s(tempStart), Utils.ms2s(tempEnd));
                    mPlayer.refresh();
                    mList.set(index, info);
                }
            }
            tempId = 0;

        }

        @Override
        public void updateThumb(int id, int start, int end) {
            mIsUpdate = true;
            if (mEditorHandler != null) {
                mEditorHandler.pause();
            }
            tempId = id;
            tempStart = start;
            tempEnd = end;
        }


        @Override
        public void onCheckItem(boolean changed, int id) {
            if (mEditorHandler != null) {
                mEditorHandler.pause();
            }
            int index = getIndex(id);
            if (index >= 0 && index < mList.size() - 1) {
                WordInfo info = mList.get(index);
                mCurrentInfo = new WordInfo(info);
                try {
                    mCurrentInfo.getCaptionObject().setVirtualVideo(mVirtualVideo, mPlayer);
                } catch (InvalidArgumentException e) {
                    e.printStackTrace();
                }
                checkVisible(mCurrentInfo);
                if (changed) {
                    initItemSub(false);
                }
            }
        }
    };

    /**
     * 移除指定的项
     */
    private int removeById(int id) {
        int index = getIndex(id);
        if (index > -1 && index <= (mList.size() - 1)) {
            WordInfo tmp = mList.remove(index);
//            if (null != tmp) {
//                removeCaption(tmp.getCaptionObject());
//            }
        }
        return index;
    }

    /**
     * 颜色回调
     */
    private IColorListener mColorPickListener = new IColorListener() {

        @Override
        public void getColor(int color, int position) {
            if (null != mCurrentInfo) {
                mCurrentInfo.setInputTextColor(color);
                onResetDrawRect();
            }
        }
    };

    /**
     * 边框颜色回调
     */
    private IColorListener mStrokeColorPickListener = new IColorListener() {

        @Override
        public void getColor(int color, int position) {
            setShadowColor(color);
        }
    };


    /**
     * 字体handler
     */
    private TTFHandler mTTFHandler;

    private DisplayMetrics mDisplay;
    private int mDuration = 1000;

    /**
     * 点击取消保存和重新进入时重置List
     */
    private void onListReset(boolean isInit) {
        mList.clear();
        if (isInit) {
            mBackupList.clear();
        }
        ArrayList<WordInfo> tempList = TempVideoParams.getInstance()
                .getSubsDuraionChecked();
        mList.clear();
        int len = tempList.size();
        WordInfo temp;
        for (int i = 0; i < len; i++) {
            temp = tempList.get(i);
            temp.resetChanged();
            mList.add(temp);
            if (isInit) {
                mBackupList.add(temp.clone());
            }
        }

    }

    private CaptionDrawRect.onClickListener mOnCheckedListener = new CaptionDrawRect.onClickListener() {
        @Override
        public void onClick(float x, float y) {
            if (!mCaptionDrawRect.isVisible()) {
                WordInfo temp = getCurSelectedTemp();
                if (null != temp) {
                    //判断当前点击的坐标是否在字幕顶点范围内
                    boolean isChecked = Utils.isContains(temp.getListPointF(), x, y);
                    if (isChecked) {
                        if (null == mCurrentInfo || !mCurrentInfo.getCaptionObject().isEditing() || temp.id != mCurrentInfo.id) {
                            //编辑时，判断，防止频繁编辑同一个
                            onEditWordImp();
                            //确认当前位置是否存在字幕
                            mRoot.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    InputUtls.showInput(mEtSubtitle);
                                }
                            }, 100);
                        }
                    }
                }
            } else {
                InputUtls.showInput(mEtSubtitle);
            }
        }
    };

    /**
     * 进入字幕的入口
     */
    private void init() {
        mCaptionPositionHandler.init(mPositionLayout, mPositionChangeListener);
        mVirtualVideo = mEditorHandler.getEditorVideo();
        mLinearWords = mEditorHandler.getSubEditorParent();
        mPlayer = mEditorHandler.getEditor();
        mDuration = mEditorHandler.getDuration();
        mIsAddCaption = false;
        mLayoutWidth = mLinearWords.getWidth();
        mLayoutHeight = mLinearWords.getHeight();
        mCaptionDrawRect = new CaptionDrawRect(mContext, null);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mCaptionDrawRect.setLayoutParams(lp);
        mCaptionDrawRect.initbmp();
        mCaptionDrawRect.setClickListener(mOnCheckedListener);
        mLinearWords.addView(mCaptionDrawRect);
        mViewHint.setVisibility(View.VISIBLE);
        nPositionId = POSITION_DEFAULT;
        mMenuLayout.setVisibility(View.GONE);
        mAddLayout.setVisibility(View.VISIBLE);

        SubUtils.getInstance().recycle();
        onListReset(true);
        onInitThumbTimeLine();
        setImage(R.drawable.edit_music_play);


        if (null != mEditorHandler) { // 清除另一种方式下的字幕
            mEditorHandler.registerEditorPostionListener(mEditorPreivewPositionListener);
            mEditorHandler.reload(false);
            mEditorHandler.seekTo(0);
        }
        mThumbNailLine.setCantouch(true);
        mThumbNailLine.setMoveItem(isThumbMoveItem);
        mPlayState.setOnClickListener(onStateChangeListener);
        getData(false);
        mTTFHandler = new TTFHandler(mGvTTF, mTTFListener, true, ttfUrl);
        //确认当前位置是否存在字幕
        ((SubtitleAdapter) mAdapter).addAll(mList, -1);
        mCaptionDrawRect.setVisibleUI(false);

    }


    private EditorPreivewPositionListener mEditorPreivewPositionListener = new EditorPreivewPositionListener() {

        @Override
        public void onEditorPreviewComplete() {
            onScrollCompleted();
        }

        @Override
        public void onEditorPrepred() {
            if (null != mEditorHandler) { // 清除另一种方式下的字幕,取消loading...
                mEditorHandler.cancelLoading();
            }
            if (!bUIPrepared) {
                //防止后台切回前台，字幕轴重新初始化
                onInitThumbTimeLine(mEditorHandler.getSnapshotEditor());
            }
        }

        @Override
        public void onEditorGetPosition(int nPosition, int nDuration) {
            onScrollProgress(nPosition);
            if (mIsAddCaption) {
                if (mCurrentInfo.getStart() < nPosition) {
                    if (!mThumbNailLine.canAddSub(nPosition, mDuration, mSizeParams[0], -1, -1)) {
                        onWordEnd(nPosition);
                    }
                }
            }
        }
    };

    private int[] mSizeParams;

    private int mHalfWidth = 0;

    private void onInitThumbTimeLine() {

        mHalfWidth = mDisplay.widthPixels / 2;
        mScrollView.setHalfParentWidth(mHalfWidth - mStateSize);
        mSizeParams = mThumbNailLine.setDuration(mDuration, mScrollView.getHalfParentWidth());
        mScrollView.setLineWidth(mSizeParams[0]);
        mScrollView.setDuration(mDuration);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(mSizeParams[0]
                + 2 * mThumbNailLine.getpadding(), mSizeParams[1]);

        lp.setMargins(mScrollView.getHalfParentWidth() - mThumbNailLine.getpadding(),
                0, mHalfWidth - mThumbNailLine.getpadding(), 0);

        mThumbNailLine.setLayoutParams(lp);

        FrameLayout.LayoutParams lframe = new FrameLayout.LayoutParams(lp.width
                + lp.leftMargin + lp.rightMargin, lp.height);
        lframe.setMargins(0, 0, 0, 0);

        mMediaLinearLayout.setLayoutParams(lframe);
        mViewHint.setVisibility(View.GONE);

    }

    //UI数据恢复成功
    private boolean bUIPrepared = false;

    /**
     * 是否启用增强模式取缩略图
     *
     * @param bExMode
     */
    public void setExMode(boolean bExMode) {
        this.bExMode = bExMode;
    }

    private boolean bExMode = false;//是否增强模式取缩略图 （耗时：）

    /**
     * 初始化缩略图时间轴和恢复字幕数据
     */
    private void onInitThumbTimeLine(VirtualVideo virtualVideo) {
        mThumbNailLine.setVirtualVideo(virtualVideo, bExMode);
        mThumbNailLine.prepare(mScrollView.getHalfParentWidth() + mHalfWidth);
        onScrollProgress(0);
        mHandler.postDelayed(resetSubDataRunnable, 100);
    }

    //恢复数据
    private Runnable resetSubDataRunnable = new Runnable() {

        @Override
        public void run() {
            ArrayList<SubInfo> sublist = new ArrayList<SubInfo>();
            int len = mList.size();
            SubInfo info;
            WordInfo item;
            for (int i = 0; i < len; i++) {
                item = mList.get(i);
                info = new SubInfo(item);
                StyleInfo styleInfo = mStyleAdapter.getItem(mStyleAdapter.getPosition(item.getStyleId()));
                if (null != styleInfo) {
                    item.getCaptionObject().setShowRectF(styleInfo.mShowRectF);
                }
                info.setStr(item.getText());
                sublist.add(info);
            }
            mThumbNailLine.prepareData(sublist);
            if (mCurrentInfo != null) {
                mThumbNailLine.showCurrent(mCurrentInfo.getId());
            }
            mThumbNailLine.setStartThumb(mScrollView.getScrollX());
            if (AppConfiguration.isFirstShowInsertSub()) {
                PopViewUtil.showPopupWindowStyle(mThumbNailLine, true, true, 120,
                        true, mThumbNailLine.getpadding(),
                        new PopViewUtil.CallBack() {

                            @Override
                            public void onClick() {
                                AppConfiguration.setIsFirstInsertSub();
                            }
                        }, R.string.drag_thumb_for_insert_sub, 0.5);
            }

            bUIPrepared = true;
        }
    };

    /**
     * 缩放
     */
    private void setDisf(float zoom) {
        if (null != mCurrentInfo) {
            mCurrentInfo.setDisf(zoom);
            onResetDrawRect();
        }
    }


    private void setProgressText(int progress) {
        mTvProgress.setText(DateTimeUtils.stringForMillisecondTime(progress));
        //判断当前时间是不是在某一个添加的贴纸内
        mThumbNailLine.setDuration(progress);
        ((SubtitleAdapter) mAdapter).setDuration(progress);
    }

    private ScrollViewListener mThumbOnScrollListener = new ScrollViewListener() {

        @Override
        public void onScrollProgress(View view, int scrollX, int scrollY, boolean appScroll) {
            mThumbNailLine.setStartThumb(mScrollView.getScrollX());
            int progress = mScrollView.getProgress();
            if (!appScroll) {
                if (mEditorHandler != null) {
                    mEditorHandler.seekTo(progress);
                }
                setProgressText(progress);
            }

        }

        @Override
        public void onScrollEnd(View view, int scrollX, int scrollY, boolean appScroll) {
            int progress = mScrollView.getProgress();
            mThumbNailLine.setStartThumb(mScrollView.getScrollX());
            if (!appScroll) {
                if (mEditorHandler != null) {
                    mEditorHandler.seekTo(progress);
                }
            }
            setProgressText(progress);

        }

        @Override
        public void onScrollBegin(View view, int scrollX, int scrollY, boolean appScroll) {
            int progress = mScrollView.getProgress();
            mThumbNailLine.setStartThumb(mScrollView.getScrollX());
            if (!appScroll) {
                if (mEditorHandler != null) {
                    mEditorHandler.pause();
                    mEditorHandler.seekTo(progress);
                }
                setProgressText(progress);
            }
        }
    };

    private boolean mIsAddCaption = false;// 新增true，编辑false

    private boolean bShowAutoRecognition = true;

    public void setHideAI() {
        bShowAutoRecognition = false;
    }

    /**
     * 点击确认按钮时，更改UI菜单
     *
     * @param current 正在编辑的字幕，()
     */
    private void checkVisible(WordInfo current) {
        if (!mIsAddCaption) {
            if (null != current) {
                btnDel.setVisibility(View.VISIBLE);
                btnEdit.setText(R.string.edit);
                btnEdit.setVisibility(View.VISIBLE);
                mThumbNailLine.showCurrent(current.getId());
            } else {
                btnDel.setVisibility(View.GONE);
                checkEditUIGone();
                mThumbNailLine.setShowCurrentFalse();
            }
        }
    }

    /**
     * 编辑按钮状态
     */
    private void checkEditUIGone() {
        if (bShowAutoRecognition) {
            //没有点击识别前，允许点击
            btnEdit.setText(R.string.auto_recognition);
            btnEdit.setVisibility(View.VISIBLE);
        } else {
            btnEdit.setText(R.string.edit);
            btnEdit.setVisibility(View.GONE);
        }
    }

    /**
     * 编辑已经存在的字幕
     */
    private void onEditWordImp() {
        mIsAddCaption = false;
        WordInfo tmp = getCurSelectedTemp();
        if (null != tmp) {
            pauseVideo();
            //移除temp字幕在GL中的资源，
            try {
                tmp.getCaptionObject().setVirtualVideo(mVirtualVideo, mPlayer);
            } catch (InvalidArgumentException e) {
                e.printStackTrace();
            }
            tmp.getCaptionObject().removeCaption();
            mPlayer.refresh();
            //编辑时，构造新对象
            mCurrentInfo = new WordInfo(tmp);
            //当前字幕的时间线
            int progress = (int) ((tmp.getEnd() + tmp.getStart()) / 2);
            mEditorHandler.seekTo(progress);
            onScrollProgress(progress);
            try {
                mCurrentInfo.getCaptionObject().setVirtualVideo(mVirtualVideo, mPlayer);
            } catch (InvalidArgumentException e) {
                e.printStackTrace();
            }
            mCaptionDrawRect.setVisibleUI(true);
            StyleInfo styleInfo = SubUtils.getInstance().getStyleInfo(tmp.getStyleId());
            if (null == styleInfo) {
                onStartSub(false, false);
            } else {
                onStartSub(styleInfo.isdownloaded, false);
            }

        }
    }

    private static final int OFFSET_END_POSTION = 0;


    /**
     * 结束点
     */
    private void onWordEnd() {
        onWordEnd(-1);
    }

    private void onWordEnd(long nPos) {

        if (null == mCurrentInfo) {
            return;
        }
        // 保存
        mIsAddCaption = false;
        mThumbNailLine.setIsAdding(mIsAddCaption);
        pauseVideo();

        if (-1 == nPos) {
            //点击完成->确认按钮，修改captionObject中的时间线
            int[] mrect = mThumbNailLine.getCurrent(mCurrentInfo.getId());
            if (null != mrect) {
//                Log.e(TAG, "onWordEnd: " + Arrays.toString(mrect));
                mCurrentInfo.setTimelineRange(mrect[0], mrect[1]);
            }
        } else {
            mCurrentInfo.setEnd(nPos < 0 ? mEditorHandler.getCurrentPosition() : nPos);
        }
        onSaveToList(false);

        int end = (int) mCurrentInfo.getEnd();
        mThumbNailLine.replace(mCurrentInfo.getId(), (int) mCurrentInfo.getStart(), end);
//        if (null != mSprCurView) {
        boolean hasExit = checkExit(mCurrentInfo.getId());
        if (hasExit) {
            mThumbNailLine.replace(mCurrentInfo.getId(), mCurrentInfo.getText());
        } else {
            mThumbNailLine.removeById(mCurrentInfo.getId());
        }
        end = end + OFFSET_END_POSTION;// 向后偏移
        if (end >= mEditorHandler.getDuration()) {
            end = mEditorHandler.getDuration() - 20;
        }
//        mCurrentInfo = null;

    }

    /**
     * 调整播放那个状态
     */
    private View.OnClickListener onStateChangeListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (mIsAddCaption) {
                onWordEnd();
                mThumbNailLine.clearCurrent();
            } else {
                if (mEditorHandler.isPlaying()) {
                    pauseVideo();
                } else {
                    if (Math.abs(mEditorHandler.getCurrentPosition()
                            - mEditorHandler.getDuration()) < 300) {
                        mEditorHandler.seekTo(0);
                    }
                    playVideo();
                }

            }
        }
    };

    private void playVideo() {
        mEditorHandler.start();
        if (null != mCurrentInfo) {

            //退出时保存控制器的位置
            mCurrentInfo.setList(mCaptionDrawRect.getList());
            mCaptionDrawRect.setVisibleUI(false);
            mCurrentInfo.getCaptionObject().quitEditCaptionMode(false);
        }
        setImage(R.drawable.edit_music_pause);
        mThumbNailLine.setHideCurrent();
        ((SubtitleAdapter) mAdapter).addAll(mList, -1);
    }

    private void pauseVideo() {
        mEditorHandler.pause();
        setImage(R.drawable.edit_music_play);

    }


    private void hideInput() {
        InputUtls.hideKeyboard(mEtSubtitle);
    }


    /**
     * 完成按钮
     */
    private View.OnClickListener onSaveChangeListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            onAddSubtitle();
        }
    };

    private void onAddSubtitle() {
        if (mIsDownloading) {
            onToast(R.string.downloading);
        } else {
            mLlWordEditer.setVisibility(View.GONE);
            if (null != mCurrentInfo) {
                int id = mCurrentInfo.getStyleId();
                StyleInfo styleInfo = SubUtils.getInstance().getStyleInfo(id);
                if (null != styleInfo && styleInfo.isdownloaded) {
                    //防止没有绑定可用的字幕效果
                    onSaveBtnItem();
                } else {
                    //参数有误，异常情况，放弃当前字幕
                    Log.e(TAG, "onSaveChangeListener: onSaveChangeListener is error");
                }
            } else {
                //异常
                Log.e(TAG, "onSaveChangeListener: onSaveChangeListener is null");
            }
        }
    }

    /**
     * 检测该id之前是否存在于字幕列表
     *
     * @param id
     * @return
     */
    private boolean checkExit(int id) {
        WordInfo temp;
        boolean hasExit = false;
        for (int i = 0; i < mList.size(); i++) {
            temp = mList.get(i);
            if (temp.getId() == id) {
                hasExit = true;
                break;
            }
        }
        return hasExit;
    }

    /**
     * 从备份列表中找字幕
     *
     * @param id
     * @return
     */
    private boolean checkExitBackup(int id) {
        WordInfo temp;
        boolean hasExit = false;
        for (int i = 0; i < mBackupList.size(); i++) {
            temp = mBackupList.get(i);
            if (temp.getId() == id) {
                hasExit = true;
                break;
            }
        }
        return hasExit;
    }


    /**
     * 点击menu_layout 中得保存(保存文本，颜色，字体等信息)，继续播放
     */
    private void onSaveBtnItem() {
        SysAlertDialog.showLoadingDialog(mContext, R.string.isloading);
        mIsSetWatcher = false;
        mThumbNailLine.replace(mCurrentInfo.getId(), mCurrentInfo.getText());
        //先移除监听再隐藏输入法(移除的时候调用了强制恢复布局)
        removeInputListener();

        InputUtls.hideKeyboard(mEtSubtitle);

        if (needApplyToAll() || mAnimApplyToAll) {
            //应用到全部
            ThreadPoolUtils.executeEx(new Runnable() {

                @Override
                public void run() {
                    applyToAll(mCurrentInfo);
                    mHandler.removeMessages(MSG_APPLY_ALL);
                    mHandler.sendEmptyMessage(MSG_APPLY_ALL);
                }
            });
        } else {
            onSaveBtnItemImp();
        }


    }

    /***
     * 保存单个字幕
     */
    private void onSaveBtnItemImp() {
//        Log.e(TAG, "onSaveBtnItemImp: " + mIsAddCaption);
        if (mIsAddCaption) {
            int start = (int) mCurrentInfo.getStart();
            onWordEnd();
            //播放器内容改变,刷新播放器
            mPlayer.refresh();
            mEditorHandler.seekTo(start);
        } else {
            onSaveToList(false);
            //播放器内容改变,刷新播放器
            mPlayer.refresh();
        }
        onMenuViewOnBackpressed();
        SysAlertDialog.cancelLoadingDialog();
    }


    private void saveInfo(boolean needStart) {
//        Log.e(TAG, "saveInfo: " + needStart + ">>mCurrentInfo:" + mCurrentInfo);
        if (null != mCurrentInfo && mCaptionDrawRect.getList() != null) {

            mCaptionDrawRect.setVisibleUI(false);
            mCurrentInfo.setList(mCaptionDrawRect.getList());
            RectF captionPreviewRectF = mCurrentInfo.getCaptionObject().getCaptionDisplayRectF();
            //保存字幕动画
            mCurrentInfo.setAnimType(mCaptionAnimHandler.getAnimation(captionPreviewRectF), mCaptionAnimHandler.getCheckedId());
            //退出编辑模式
            mCurrentInfo.getCaptionObject().quitEditCaptionMode(true);
            mCurrentInfo.setParent(mLayoutWidth, mLayoutHeight);
            //隐藏UI层的控制按钮
            unRegisterDrawRectListener();


            if (needStart) {
                if (mEditorHandler != null) {
                    mEditorHandler.stop();
                    mEditorHandler.start();
                }
            }

            int re = getIndex(mCurrentInfo.getId());
            int index;
            if (re > -1) {
                mList.set(re, mCurrentInfo); // 重新编辑
                index = re;
            } else {
                mList.add(mCurrentInfo); // 新增
                index = mList.size() - 1;
            }
            ((SubtitleAdapter) mAdapter).addAll(mList, index);
            checkVisible(mCurrentInfo);
        }

        if (null != mTTFHandler) {
            mTTFHandler.ToReset();
        }

    }

    /**
     * 当前字幕在集合的索引
     *
     * @param id 当前字幕的Id
     * @return
     */
    private int getIndex(int id) {
        int index = -1;
        for (int i = 0; i < mList.size(); i++) {
            if (id == mList.get(i).getId()) {
                index = i;
                break;
            }
        }
        return index;
    }

    private boolean mIsSetWatcher = false;

    /**
     * 移除播放器中的字幕
     */
    private void removeCaption(CaptionObject captionObject) {
        if (null != captionObject) {
            captionObject.quitEditCaptionMode(false);
            captionObject.removeCaption();
            mPlayer.refresh();
        }
    }

    /**
     * 执行删除单个word
     */
    private void onDelWordItem(int id) {
//        Log.e(TAG, "onDelWordItem: " + id + " mCurrentInfo:" + mCurrentInfo);
        //******必须先暂停再删除
        pauseVideo();
        if (null != mCurrentInfo) {
            mThumbNailLine.removeById(mCurrentInfo.getId());
            removeById(mCurrentInfo.getId());
            mCurrentInfo.setList(null);
            mCaptionDrawRect.setVisibleUI(false);
            removeCaption(mCurrentInfo.getCaptionObject());
            mCurrentInfo.recycle();
            mCurrentInfo = null;
        } else {
            mThumbNailLine.removeById(id);
            removeById(id);
        }
        unRegisterDrawRectListener();
        if (mMenuLayout.getVisibility() == View.VISIBLE) {
            mMenuLayout.setVisibility(View.GONE);
        }

        if (mAddLayout.getVisibility() != View.VISIBLE) {
            mAddLayout.setVisibility(View.VISIBLE);
        }
        mIsAddCaption = false;

        checkTitleLayout();
        ((SubtitleAdapter) mAdapter).addAll(mList, -1);
        checkVisible(null);

    }

    private void onCheckStyle(WordInfo info) {
        if (mStyleAdapter.getCount() > 0) {
            mStyleAdapter.setCheckItem(mStyleAdapter.getPosition(info
                    .getStyleId()));

        }
    }


    /***
     * 点击新增字幕或编辑字幕样式时
     */
    private void initItemSub(boolean isAdd) {

        initItemWord(mCurrentInfo, isAdd);
        onCheckStyle(mCurrentInfo);


    }

    /**
     * 开始编辑字幕
     *
     * @param isdownload
     * @param isAdd
     */
    private void onStartSub(boolean isdownload, boolean isAdd) {
        controlKeyboardLayout();
        mIsSetWatcher = true;
        if (mEditorHandler != null) {
            mEditorHandler.pause();
        }
        mRgMenu.setOnCheckedChangeListener(onCheckedChangeListener);
        if (null != mCurrentInfo && null != mCaptionAnimHandler) {
            //还原之前编辑的动画索引
            mCaptionAnimHandler.resetCheckAnim(mCurrentInfo.getCheckId());
        }
        mAddLayout.setVisibility(View.GONE);
        mMenuLayout.setVisibility(View.VISIBLE);
        mHandler.removeMessages(MSG_LISTVIEW);
        mHandler.sendEmptyMessage(MSG_LISTVIEW);
        if (isdownload) {
            initItemSub(isAdd);
        } else {
            mHandler.removeMessages(MSG_ONSTYLE_IMP);
            mHandler.sendEmptyMessageDelayed(MSG_ONSTYLE_IMP, 500);
        }
        mEtSubtitle.addTextChangedListener(mTextWatcher);
        ((RadioButton) $(R.id.subtitle_style)).setChecked(true);

    }

    private boolean isChangedByInputManager = true;

    /**
     * 记录是否是输入法输入的文本
     *
     * @param inputByInputManager true 键盘改变的文本；false  et.settext("****")
     */
    public void setTextChangedByInputManager(boolean inputByInputManager) {
        isChangedByInputManager = inputByInputManager;
    }

    /***
     * 把横排排版的文本切换成竖排
     * @param tmp 横排文本 （可以包含有换行符）
     * @return 竖排排版的文本
     */
    private String addLine(String tmp) {
        if (!TextUtils.isEmpty(tmp)) {
            StringBuffer sb = new StringBuffer();
            int len = tmp.replace("\n", "").length();
            for (int i = 0; i < len; i++) {
                sb.append(tmp.substring(i, i + 1));
                if (i != len - 1) {
                    sb.append("\n");
                }
            }
            return sb.toString();
        }
        return null;
    }

    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
//            Log.e(TAG, "mTextWatcher->onTextChanged: " + s + ">>" + mIsSetWatcher + "  isChangedByInputManager:" + isChangedByInputManager);
            if (mIsSetWatcher) {
                if (isChangedByInputManager) {
                    if (null != mCurrentInfo) {
                        String tmp = s.toString();
                        String lineText = addLine(tmp);
                        mCurrentInfo.setInputText(tmp, lineText);
                        StyleInfo styleInfo = SubUtils.getInstance().getStyleInfo(mCurrentInfo.getStyleId());
                        if (null != styleInfo && styleInfo.vertical) {
                            mCurrentInfo.setInputText(lineText);
                        } else {
                            mCurrentInfo.setInputText(tmp);
                        }
                        onResetDrawRect();
                    }
                }

            }

        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };


    /**
     * 单个编辑字幕
     *
     * @param w
     * @param isAdd true 新增；false 编辑
     */
    private void initItemWord(WordInfo w, boolean isAdd) {


        StyleInfo info = SubUtils.getInstance().getStyleInfo(w.getStyleId());
        int color = Color.WHITE;

        String text = "";
        if (null != info) {
            if (info.vertical) {
                //竖着排版
                text = TextUtils.isEmpty(w.getInputTextVer()) ? info.getHint() : w.getInputTextVer();
            } else {
                text = TextUtils.isEmpty(w.getInputTextHor()) ? info.getHint() : w.getInputTextHor();
            }

            if (!TextUtils.isEmpty(w.getInputTextHor())) {
                //都默认设置横排模式下的文字排版
                mEtSubtitle.setText(w.getInputTextHor());
            }
            color = (w.getInputTextColor() == Color.WHITE ? info.getTextDefaultColor() : w.getTextColor());
        }


        setTextChangedByInputManager(false);


        String ttf = TTFData.getInstance().quweryOne(info.tFont);
        if (!TextUtils.isEmpty(w.getInputTTF())) {
            ttf = w.getInputTTF();
        }
//        Log.e(TAG, "initItemWord: "+si.toString() +"  "+w.getDisf());
        //再次编辑时，默认不再设置style样式，
//        WordInfo temp = getCurSelectedTemp();
//        if (null == temp) {
        if (isAdd) {
            w.getCaptionObject().setCaptionType(CaptionObject.CaptionType.sub);
            w.setTextColor(color);
            w.setText(text);
            w.getCaptionObject().setFontByFilePath(ttf);
            //应用样式
            setCommonStyleImp(info, w);
        } else {
            //编辑
        }
//        }
        //如果已经创建了视图，直接就不需要再次创建
        if (!w.getCaptionObject().isEditing()) {
            try {
                w.getCaptionObject().editCaptionMode();
            } catch (InvalidArgumentException e) {
                e.printStackTrace();

            }
        }

        onResetDrawRect();
        registerDrawRectListener();
        setTextChangedByInputManager(true);

    }

    /***
     * 设置样式
     * @param styleInfo
     * @param wordInfo
     */
    private void setCommonStyleImp(StyleInfo styleInfo, WordInfo wordInfo) {
        try {
//            Log.e(TAG, "setCommonStyleImp: " + styleInfo);
            //字幕只有一个背景
            wordInfo.getCaptionObject().setFrameArray(styleInfo.type, styleInfo.mShowRectF, styleInfo.getTextRectF(), styleInfo.frameArray.valueAt(0).pic, styleInfo.lashen,
                    styleInfo.getNinePitch(), styleInfo.onlyone, styleInfo.disf);
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        }
    }

    private SeekBar.OnSeekBarChangeListener mOnColorAlphaChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            mTvColorAlphaPercent.setText(progress + "%");
            if (null != mCurrentInfo) {
                mCurrentInfo.setInputTextColorAlpha(progress / 100f);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    private final float MAX_STROKE_WIDTH = 8.0f;
    private SeekBar.OnSeekBarChangeListener mOnStrokeWidthChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (null != mCurrentInfo) {
                float scale = MAX_STROKE_WIDTH / 100;
                mCurrentInfo.setInputTextStrokeWidth(progress * scale);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    /**
     * 缩放比
     */
    private void onDisfImp(SeekBar seekBar, int progress, boolean fromUser) {
        float scale = 100 / (CaptionObject.MAX_SCALE - CaptionObject.MIN_SCALE);
        float textScale = 100f / (CommonStyleUtils.ZOOM_MAX_TEXT - CommonStyleUtils.ZOOM_MIN_TEXT);
        int textNum = (int) (CommonStyleUtils.ZOOM_MIN_TEXT + progress / textScale);
        mTvSubtitleSize.setText(Integer.toString(textNum));
        setSizeTextPosition(seekBar);
        if (fromUser) {
            setDisf(CaptionObject.MIN_SCALE + progress / scale);
        }
    }

    private SeekBar.OnSeekBarChangeListener mOnSizeChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            onDisfImp(seekBar, progress, fromUser);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    private void setSizeTextPosition(final SeekBar seekBar) {
        seekBar.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int progressWidth = seekBar.getProgressDrawable().getBounds().width();
                int thumbX = seekBar.getThumb().getBounds().left;
                mTvSubtitleSize.setTranslationX(thumbX - progressWidth / 2);
            }
        });
    }

    private CompoundButton.OnCheckedChangeListener mOnApplyToAllCheckedListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (mCurLayoutIndex == STYLE) {
                mStyleApplyToAll = isChecked;
            } else if (mCurLayoutIndex == ANIM) {
                mAnimApplyToAll = isChecked;
            } else if (mCurLayoutIndex == COLOR) {
                mColorApplyToAll = isChecked;
            } else if (mCurLayoutIndex == STROKE) {
                mStrokeApplyToAll = isChecked;
            } else if (mCurLayoutIndex == FONT) {
                mFontApplyToAll = isChecked;
            } else if (mCurLayoutIndex == SIZE) {
                mSizeApplyToAll = isChecked;
            } else if (mCurLayoutIndex == POSITION) {
                mPositionApplyToAll = isChecked;
            }

        }
    };

    private CompoundButton.OnCheckedChangeListener mOnTextStyleChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (null != mCurrentInfo) {
                int id = buttonView.getId();
                if (id == R.id.cbSubtitleBold) {
                    mCurrentInfo.setBold(isChecked);
                } else if (id == R.id.cbSubtitleItalic) {
                    mCurrentInfo.setItalic(isChecked);
                } else if (id == R.id.cbSubtitleShadow) {
                    mCurrentInfo.setShadow(isChecked);
                }
                onResetDrawRect();
            }
        }
    };


    private View.OnClickListener mClearSubtitle = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            mEtSubtitle.setText("");
        }
    };

    private RadioGroup.OnCheckedChangeListener onCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(RadioGroup mRgMenu, int checkedId) {

            onVisibile(checkedId);
        }
    };

    private StyleAdapter mStyleAdapter = null;


    /**
     * 获取字幕样式
     */
    private void getData(final boolean addItem) {
        int count = null == mStyleAdapter ? 0 : mStyleAdapter.getCount();
        if (!mModel.isPrepareing() && count == 0) {
            mModel.load(subUrl, new ISSCallBack<StyleInfo>() {
                @Override
                public void onSuccess(List<StyleInfo> list) {
                    if (null != mHandler && isRunning) {
                        //取消loading状态
                        mHandler.removeMessages(MSG_CANCEL_DIALOG);
                        mHandler.sendEmptyMessage(MSG_CANCEL_DIALOG);
                        mHandler.obtainMessage(MSG_DATA, list).sendToTarget();
                        if (addItem) {
                            // 执行新增一个字幕逻辑
                            mHandler.removeMessages(MSG_INITED_ADD);
                            mHandler.sendEmptyMessageDelayed(MSG_INITED_ADD, 600);
                        }
                    }
                }

                @Override
                public void onFailed() {
                    if (null != mHandler && isRunning) {
                        mHandler.removeMessages(MSG_CANCEL_DIALOG);
                        mHandler.sendEmptyMessage(MSG_CANCEL_DIALOG);
                        mHandler.sendEmptyMessage(MSG_TIMEOUT);
                    }
                }

                @Override
                public void onIconSuccess() {
                    if (null != mHandler && isRunning) {
                        mHandler.removeMessages(MSG_CANCEL_DIALOG);
                        mHandler.sendEmptyMessage(MSG_CANCEL_DIALOG);
                        mHandler.sendEmptyMessage(MSG_ICON);
                    }
                }
            });
        } else {
            Log.e(TAG, "getData: isWebing:" + mModel.isPrepareing() + " count:" + count);
        }
    }


    private final int MSG_ICON = 5665, MSG_TIMEOUT = 5701, MSG_LISTVIEW = 5689, MSG_INITED_ADD = 5690, MSG_DATA = 5691,
            MSG_ONSTYLE_IMP = 5692, MSG_CANCEL_DIALOG = 5693, MSG_APPLY_ALL = 5694;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case MSG_TIMEOUT: {
                    onToast(R.string.timeout);
                }
                break;
                case MSG_APPLY_ALL: {
                    mCbApplyToAll.setChecked(false);
                    //保存单个字幕，如果应用到全部后，继续完善UI
                    onSaveBtnItemImp();
                }
                break;
                case MSG_ICON: {
                    if (null != mStyleAdapter) {
                        mStyleAdapter.updateIcon();
                    }
                }
                break;
                case MSG_LISTVIEW: {
                    mStyleAdapter.setListview(mGvSubtitleStyle);
                }
                break;
                case MSG_INITED_ADD: {
                    onBtnAddClick();
                }
                break;
                case MSG_DATA: {
                    ArrayList<StyleInfo> list = (ArrayList<StyleInfo>) msg.obj;
                    GridLayoutManager manager = new GridLayoutManager(getContext(), 6);
                    mGvSubtitleStyle.setLayoutManager(manager);
                    mStyleAdapter = new StyleAdapter(mContext, true, !TextUtils.isEmpty(subUrl), list);
                    mStyleAdapter.setOnItemClickListener(mStyleItemlistener);
                    mGvSubtitleStyle.setAdapter(mStyleAdapter);
                }
                break;
                case MSG_ONSTYLE_IMP: {
                    onStyleItem(0, mCurrentInfo);
                }
                break;
                case MSG_CANCEL_DIALOG: {
                    SysAlertDialog.cancelLoadingDialog();
                }
                break;
                default:
                    break;
            }
        }
    };

    /**
     * 控制menu部分的布局状态
     *
     * @param checkedId
     */
    private void onVisibile(int checkedId) {
        resetLayout();
        if (checkedId == R.id.subtitle_style) {
            mCurLayoutIndex = STYLE;
            mCbApplyToAll.setText(R.string.st_style_apply_to_all);
            mCbApplyToAll.setChecked(mStyleApplyToAll);
            if (null != mTTFHandler) {
                mTTFHandler.onPasue();
            }
            mStyleLayout.setVisibility(View.VISIBLE);
            getData(false);
        } else if (checkedId == R.id.subtitle_font) {
            mCurLayoutIndex = FONT;
            mSbBlod.setOnCheckedChangeListener(mOnTextStyleChangeListener);
            mSbItalic.setOnCheckedChangeListener(mOnTextStyleChangeListener);
            mSbShadow.setOnCheckedChangeListener(mOnTextStyleChangeListener);
            mCbApplyToAll.setText(R.string.st_font_apply_to_all);
            mCbApplyToAll.setChecked(mFontApplyToAll);
            hideInput();
            mTTFLayout.setVisibility(View.VISIBLE);
            mTTFHandler.refleshData();
            if (mCurrentInfo != null) {
                String strTTFlocal = mCurrentInfo.getTtfLocalPath();
                mSbBlod.setChecked(mCurrentInfo.isBold());
                mSbItalic.setChecked(mCurrentInfo.isItalic());
                mSbShadow.setChecked(mCurrentInfo.isShadow());
                if (!TextUtils.isEmpty(strTTFlocal)) {
                    int ttfNum = mTTFHandler.mAdapter.getCount();
                    for (int i = 0; i < ttfNum; i++) {
                        String localPath = mTTFHandler.mAdapter.getItem(i).local_path;
                        if (!TextUtils.isEmpty(localPath) && localPath.equals(strTTFlocal)) {
                            mTTFHandler.mAdapter.setCheck(i);
                            break;
                        }
                    }
                } else {
                    mTTFHandler.ToReset();// 执行默认字体
                }
            }
        } else if (checkedId == R.id.subtitle_color) {
            mCurLayoutIndex = COLOR;
            mCbApplyToAll.setText(R.string.st_color_apply_to_all);
            mCbApplyToAll.setChecked(mColorApplyToAll);
            hideInput();
            mTTFHandler.onPasue();
            mColorLayout.setVisibility(View.VISIBLE);
            if (null != mCurrentInfo) {
                lastColorAlpha = (int) mCurrentInfo.getInputTextColorAlpha();
                mSbColorAlpha.setProgress((int) (lastColorAlpha * 100));
                lastTextColor = mCurrentInfo.getInputTextColor();
                mColorScrollView.setChecked(lastTextColor);

            }
            $(R.id.ivColorDefault).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != mCurrentInfo) {
                        mCurrentInfo.setInputTextColor(lastTextColor);
                        mCurrentInfo.setInputTextColorAlpha(lastColorAlpha);
                    }
                    mColorScrollView.setChecked(lastTextColor);
                    mSbColorAlpha.setProgress((int) (lastColorAlpha * 100));
                }
            });

        } else if (checkedId == R.id.subtitle_size) {
            mCurLayoutIndex = SIZE;
            mCbApplyToAll.setText(R.string.st_size_apply_to_all);
            mCbApplyToAll.setChecked(mSizeApplyToAll);
            float scale = (CaptionObject.MAX_SCALE - CaptionObject.MIN_SCALE) / 100;
            if (null != mCurrentInfo) {
                mSbSubtitleSize.setProgress((int) ((mCurrentInfo.getDisf() - CaptionObject.MIN_SCALE) / scale));
            }
            hideInput();
            mTTFHandler.onPasue();
            mSizeLayout.setVisibility(View.VISIBLE);
        } else if (checkedId == R.id.subtitle_anim) {
            mCurLayoutIndex = ANIM;
            mCbApplyToAll.setText(R.string.st_anim_apply_to_all);
            mCbApplyToAll.setChecked(mAnimApplyToAll);
            hideInput();
            mTTFHandler.onPasue();
            mAnimLayout.setVisibility(View.VISIBLE);
            if (null != mCurrentInfo) {
                mCaptionAnimHandler.resetCheckAnim(mCurrentInfo.getCheckId());
            }
        } else if (checkedId == R.id.subtitle_stroke) {
            mCurLayoutIndex = STROKE;
            mCbApplyToAll.setText(R.string.st_stroke_apply_to_all);
            mCbApplyToAll.setChecked(mStrokeApplyToAll);
            mStrokeLayout.setVisibility(View.VISIBLE);
            if (mCurrentInfo != null) {
                lastStrokeWidth = mCurrentInfo.getTextStrokeWidth();
                lastShadowColor = mCurrentInfo.getShadowColor();
                mSbStrokeWidth.setProgress((int) (lastStrokeWidth * 100 / MAX_STROKE_WIDTH));
            }

            $(R.id.ivStrokeDefault).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != mCurrentInfo) {
                        mCurrentInfo.setInputTextStrokeWidth(lastStrokeWidth);
                        mCurrentInfo.setShadowColor(lastShadowColor);
                    }
                    mStrokeColorView.setChecked(lastShadowColor);
                    mSbStrokeWidth.setProgress((int) (lastStrokeWidth * 100 / MAX_STROKE_WIDTH));
                }
            });


        } else if (checkedId == R.id.subtitle_position) {
            mCurLayoutIndex = POSITION;
            mCbApplyToAll.setText(R.string.st_position_apply_to_all);
            mCbApplyToAll.setChecked(mPositionApplyToAll);
            mPositionLayout.setVisibility(View.VISIBLE);
        }
    }

    private float lastStrokeWidth = 0f;
    private int lastShadowColor = Color.WHITE;
    private float lastColorAlpha = 0;  //0~1.0f
    private int lastTextColor = Color.WHITE;

    private void resetLayout() {
        mStyleLayout.setVisibility(View.GONE);
        mTTFLayout.setVisibility(View.GONE);
        mColorLayout.setVisibility(View.GONE);
        mSizeLayout.setVisibility(View.GONE);
        mAnimLayout.setVisibility(View.GONE);
        mStrokeLayout.setVisibility(View.GONE);
        mPositionLayout.setVisibility(View.GONE);
    }


    /**
     * 播放控制器图标
     */
    public void setImage(int resId) {
        if (isRunning && null != mPlayState) {
            mPlayState.setImageResource(resId);
        }
    }

    private WordInfo getCurSelectedTemp() {
        WordInfo temp = null;
        if (!mList.isEmpty() && mAdapter.getChecked() >= 0) {
            temp = mList.get(mAdapter.getChecked());
        }
        return temp;
    }


    /**
     * 一级界面返回
     */
    private void onAddBackPressed() {
        if (!CommonStyleUtils.isEquals(mList, TempVideoParams.getInstance()
                .getSubsDuraionChecked()) || mIsUpdate) {
            onShowAlert();
        } else {
            onBackToActivity(false);
            mEditorHandler.onBack(); //通知Activity
        }
    }

    /**
     * UI按钮恢复到默认
     */
    @Override
    void resetUI() {
        mThumbNailLine.setShowCurrentFalse();
        mCurrentInfo = null;
        btnDel.setVisibility(View.GONE);
        btnAdd.setText(R.string.add);
        checkEditUIGone();
        ((SubtitleAdapter) mAdapter).addAll(mList, -1);
    }

    /**
     * 响应第一个返回按钮
     */
    @Override
    protected void onBtnLeftClick() {
        if (mAdapter.getChecked() >= 0) {
            //保存参数并退出编辑模式
            resetUI();
            pauseVideo();
        } else {
            onAddBackPressed();
        }
    }

    /**
     * 二级界面-返回
     */
    @Override
    public void onMenuBackClick() {
        onMenuBackPressed();
        if (null != mStyleAdapter) {
            mStyleAdapter.clearDownloading();
        }
        WordInfo wordInfo = getCurSelectedTemp();
        if (wordInfo != null) {
            wordInfo.getCaptionObject().updateCaption(mVirtualVideo);
            mCurrentInfo = new WordInfo(wordInfo);
            mThumbNailLine.showCurrent(mCurrentInfo.getId());
        }
        mIsAddCaption = false;
        checkTitleLayout();
    }

    /**
     * 从字幕(样式、大小）界面  ->返回到字幕1(添加字幕界面)
     * 二级界面-->一级界面
     */
    private void onMenuBackPressed() {
        if (null != mCurrentInfo) {
            if (mIsAddCaption) {
                //放弃新增
                mThumbNailLine.removeById(mCurrentInfo.getId());
                mCurrentInfo.getCaptionObject().quitEditCaptionMode(false);
                mCurrentInfo.getCaptionObject().removeCaption();
            } else {
                boolean hasExit = checkExit(mCurrentInfo.getId());
                if (hasExit) {
                    mThumbNailLine.replace(mCurrentInfo.getId(), TextUtils.isEmpty(mCurrentInfo.getInputText()) ? mCurrentInfo.getText() : mCurrentInfo.getInputText());
                } else {
                    mThumbNailLine.removeById(mCurrentInfo.getId());
                }
                mCurrentInfo.getCaptionObject().quitEditCaptionMode(false);
            }
        }
        onMenuViewOnBackpressed();
        mViewTouchListener.onActionUp();
        unRegisterDrawRectListener();
        removeInputListener();
    }

    /**
     * 点击上级界面（场景：正在编辑字幕样式，(1点击保存按钮,2点击返回))
     */
    private void onMenuViewOnBackpressed() {
        mHandler.removeMessages(MSG_ONSTYLE_IMP);
        mHandler.removeMessages(MSG_INITED_ADD);
        mHandler.removeMessages(MSG_DATA);
        mEtSubtitle.setText("");
        if (mMenuLayout.getVisibility() == View.VISIBLE) {
            mMenuLayout.setVisibility(View.GONE);
        }
        mTTFHandler.onPasue();
        if (mAddLayout.getVisibility() != View.VISIBLE) {
            mAddLayout.setVisibility(View.VISIBLE);
        }
        InputUtls.hideKeyboard(mEtSubtitle);
    }

    private void onShowAlert() {
        SysAlertDialog.createAlertDialog(mContext,
                mContext.getString(R.string.dialog_tips),
                mContext.getString(R.string.cancel_all_changed),
                mContext.getString(R.string.cancel),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }, mContext.getString(R.string.sure),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        //移除播放器中的字幕
                        if (null != mCurrentInfo) {
                            removeCaption(mCurrentInfo.getCaptionObject());
                            mCurrentInfo = null;
                        }
                        onListReset(false);
                        onBackToActivity(false);
                        //直接finish退出
                        if (mExitListener != null) {
                            mExitListener.exit(1);
                        }
                        mEditorHandler.onBack();
                    }
                }, false, null).show();
    }

    /**
     * 返回
     *
     * @param save
     */
    private void onBackToActivity(boolean save) {
        if (mExitListener != null) {
            return;
        }
        mIsUpdate = false;
        if (mIsAddCaption) {
            onWordEnd();
        }
        if (save) {
            if (mCurrentInfo != null) {
                CaptionObject co = mCurrentInfo.getCaptionObject();
                if (co.getParentSize().x <= 1) {  // 并未编辑过字幕
                    mCurrentInfo = null;
                }
            }
            onSaveToList(true);
            if (null != mCurrentInfo) {
                mCurrentInfo.getCaptionObject().quitEditCaptionMode(true);
            }
        } else {
            if (null != mCurrentInfo) {
                mCurrentInfo.getCaptionObject().quitEditCaptionMode(false);
            }
            for (int n = 0; n < mList.size(); n++) {
                mList.get(n).set(mBackupList.get(n));
            }
        }

        mHandler.removeMessages(MSG_ICON);
        mHandler.removeMessages(MSG_LISTVIEW);
        mHandler.removeMessages(MSG_INITED_ADD);
        mAddLayout.setVisibility(View.GONE);
        if (null != mThumbNailLine) {
            mThumbNailLine.recycle();
        }
        onScrollTo(0);
        setProgressText(0);
        if (null != mStyleAdapter) {
            mStyleAdapter.onDestory();
        }
        if (null != mTTFHandler) {
            mTTFHandler.onDestory();
            mTTFHandler = null;
        }
        mEditorHandler.unregisterEditorProgressListener(mEditorPreivewPositionListener);
        mThumbNailLine.clearAll();
        bUIPrepared = false;
        mCaptionDrawRect.recycle();

    }

    /**
     * 保存当前编辑字幕到集合 （完成按钮，播放按钮控制）
     */
    private void onSaveToList(boolean clearCurrent) {
        unRegisterDrawRectListener();
        saveInfo(clearCurrent);
        if (clearCurrent)
            mThumbNailLine.clearCurrent();
    }

    /**
     * 播放中的进度
     *
     * @param progress (单位ms)
     */
    private void onScrollProgress(int progress) {
        onScrollTo(getScrollX(progress));
        setProgressText(progress);
    }

    private int getScrollX(long progress) {
        return (int) (progress * (mThumbNailLine.getThumbWidth() / mDuration));
    }

    /**
     * 播放完成
     */
    private void onScrollCompleted() {
        onScrollTo((int) mThumbNailLine.getThumbWidth());
        setProgressText(mDuration);
        mPlayState.setImageResource(R.drawable.edit_music_play);
    }

    /**
     * 设置播放进度
     */
    private void onScrollTo(int mScrollX) {
        mScrollView.appScrollTo(mScrollX, true);
    }


    private boolean mIsDownloading = false;

    /**
     * fragment的容器对应的父容器
     */
    public SubtitleFragment setFragmentContainer(View fragmentContainer) {
        mFragmentContainer = fragmentContainer;
        return this;
    }

    /**
     * 装载fragment的容器
     */
    private View mFragmentContainer;


    //注册输入法监听，动态调整bu布局setY
    private com.rd.veuisdk.listener.OnGlobalLayoutListener mGlobalLayoutListener;

    private void controlKeyboardLayout() {
        removeInputListener();
        if (null != mTreeView && null != mFragmentContainer && null != mLlWordEditer) {
            mGlobalLayoutListener = new com.rd.veuisdk.listener.OnGlobalLayoutListener(mTreeView, mFragmentContainer, mLlWordEditer);
            mTreeView.getViewTreeObserver().addOnGlobalLayoutListener(mGlobalLayoutListener);
        }
    }

    private void removeInputListener() {
        if (null != mTreeView) {
            if (null != mGlobalLayoutListener) {
                //先移除监听再隐藏输入法(移除的时候调用了强制恢复布局)
                mTreeView.getViewTreeObserver().removeOnGlobalLayoutListener(mGlobalLayoutListener);
                mGlobalLayoutListener.resetUI();
                mGlobalLayoutListener = null;
            }
        }
    }

    private boolean isHideAppToAll = false;

    public void setHideApplyToAll(boolean hide) {
        this.isHideAppToAll = hide;
    }

    private boolean isThumbMoveItem = true;

    public void setThumbMoveItem(boolean move) {
        this.isThumbMoveItem = move;
    }

    private VideoEditAloneActivity.ExitListener mExitListener;

    public void setExitListener(VideoEditAloneActivity.ExitListener exitListener) {
        this.mExitListener = exitListener;
    }

    //隐藏编辑框
    public void setHideEdit() {
        if (mThumbNailLine != null) {
            mThumbNailLine.setHideCurrent();
            ((SubtitleAdapter) mAdapter).addAll(mList, -1);
        }
    }

}
