package com.rd.veuisdk.fragment;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.rd.lib.utils.CoreUtils;
import com.rd.vecore.VirtualVideo;
import com.rd.vecore.VirtualVideoView;
import com.rd.vecore.models.EffectInfo;
import com.rd.vecore.models.EffectType;
import com.rd.vecore.models.MediaObject;
import com.rd.vecore.utils.MiscUtils;
import com.rd.veuisdk.IPlayer;
import com.rd.veuisdk.IVideoEditorHandler;
import com.rd.veuisdk.R;
import com.rd.veuisdk.adapter.EffectFilterAdapter;
import com.rd.veuisdk.database.EffectData;
import com.rd.veuisdk.model.EffectFilterInfo;
import com.rd.veuisdk.model.EffectTypeDataInfo;
import com.rd.veuisdk.model.FilterEffectItem;
import com.rd.veuisdk.model.TimeEffectItem;
import com.rd.veuisdk.model.bean.TypeBean;
import com.rd.veuisdk.mvp.model.EffectFragmentModel;
import com.rd.veuisdk.mvp.model.ICallBack;
import com.rd.veuisdk.ui.CircleAnimationView;
import com.rd.veuisdk.ui.ExtCircleSimpleDraweeView;
import com.rd.veuisdk.ui.SimpleDraweeViewUtils;
import com.rd.veuisdk.ui.VideoThumbNailView2;
import com.rd.veuisdk.utils.EffectManager;
import com.rd.veuisdk.utils.ModeDataUtils;
import com.rd.veuisdk.utils.Utils;

import java.util.ArrayList;
import java.util.List;


/**
 * 特效(布局支持自定义)
 */
public abstract class AbstractEffectFragment extends BaseFragment {


    private EffectFragmentModel mModel;
    protected static final String PARAM_URL = "param_url";
    protected static final String PARAM_TYPE_URL = "param_type_url";
    private List<EffectTypeDataInfo<EffectFilterInfo>> mTypeDataInfos;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        String url = "", typeUrl = "";
        if (null != bundle) {
            url = bundle.getString(PARAM_URL);
            typeUrl = bundle.getString(PARAM_TYPE_URL);
        }
        mModel = new EffectFragmentModel<EffectFilterInfo>(getContext(), new ICallBack<EffectTypeDataInfo<EffectFilterInfo>>() {

            @Override
            public void onSuccess(List<EffectTypeDataInfo<EffectFilterInfo>> list) {
                mTypeDataInfos = list;
                initData();
                initEffectType();
            }

            @Override
            public void onFailed() {
                updateAdapter(null, -1);
            }
        });

        typeUrl = TextUtils.isEmpty(typeUrl) ? ModeDataUtils.RD_TYPE_URL : typeUrl;
        url = TextUtils.isEmpty(url) ? ModeDataUtils.RD_APP_DATA : url;
        mModel.loadData(typeUrl, url);
    }

    private void initData() {
        List<EffectFilterInfo> dbList = EffectData.getInstance().queryAll();
        int n = mTypeDataInfos.size();
        List<EffectFilterInfo> currentPageList = new ArrayList<>();
        List<EffectFilterInfo> allEffectList = new ArrayList<>();
        for (int j = 0; j < n; j++) {
            int len = mTypeDataInfos.get(j).getList().size();
            for (int i = 0; i < len; i++) {
                EffectFilterInfo filterInfo = mTypeDataInfos.get(j).getList().get(i);
                EffectFilterInfo data = mModel.getDBItem(dbList, filterInfo.getFile());
                if (null != data && filterInfo.getUpdatetime() <= data.getUpdatetime()) {
                    //已下载的资源为最新资源
                    filterInfo.setLocalPath(data.getLocalPath());
                    int registeredId = EffectManager.getInstance().getRegistered(filterInfo.getFile());
                    if (0 != registeredId) {
                        //已经注册过了
                        filterInfo.setCoreFilterId(registeredId);
                        EffectFilterInfo tmp = EffectManager.getInstance().getRegisterFilterInfo(registeredId);
                        if (null != tmp) {
                            filterInfo.setColor(tmp.getColor());
                            filterInfo.setDuration(tmp.getDuration());
                        }
                    } else {
                        //解析已下载的资源信息（注册）
                        if (EffectManager.getInstance().init(getContext(), filterInfo, mEffectHandler.getPlayer(), null)) {
                            EffectManager.getInstance().add(filterInfo.getFile(), filterInfo.getCoreFilterId());
                        }
                    }
                }
                if (j == 0) {
                    currentPageList.add(filterInfo);
                }
                allEffectList.add(filterInfo);
            }

        }
        EffectManager.getInstance().setFilterList(allEffectList);
        updateAdapter(currentPageList, -1);
    }

    private IPlayer mPlayer;
    //滤镜特效
    private ArrayList<FilterEffectItem> mArrFilterEffectItem = new ArrayList<>();
    private ArrayList<EffectInfo> mArrEffectInfo;
    //时间特效
    private TimeEffectItem mTimeEffectItem;
    private float mTimeEffectStartTime, mTimeEffectEndTime;
    //倒序时间跟其他特效分别保存
    private float mReverseStartTime, mReverseEndTime;
    /**
     * 初始特效状态保存，用于返回时恢复初始状态
     */
    private List<EffectInfo> mTempArrEffect = new ArrayList<>();

    private RadioGroup mRadioGroup;
    private LinearLayout mLlTimeEffect;
    private LinearLayout mLlFilterEffect;


    //时间特效按钮
    private CircleAnimationView mRbNull;
    private CircleAnimationView mRbSlow;
    private CircleAnimationView mRbRepeat;
    private LinearLayout mLlNull;
    private LinearLayout mLlSlow;
    private LinearLayout mLlRepeat;
    private VideoThumbNailView2 mViewThumbnail;
    private TextView tvEffectHint;
    private RecyclerView mRecyclerView;
    private EffectFilterAdapter mAdapter;
    /**
     * 调整播放那个状态
     */
    private View.OnClickListener onStateChangeListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (mPlayer.isPlaying()) {
                pauseVideo();
            } else {
                if (Math.abs(mPlayer.getCurrentPosition() - mPlayer.getDuration()) < 300) {
                    mPlayer.seekTo(0);
                }
                playVideo();
            }
        }
    };

    private void playVideo() {
        mPlayState.setImageResource(R.drawable.edit_music_pause);
        mPlayer.start();
        lastIsComplete = false;
    }

    private void pauseVideo() {
        mPlayState.setImageResource(R.drawable.edit_music_play);
        mPlayer.pause();
    }

    private Animation mEffectScale;

    private void initAnim() {
        if (mEffectScale == null) {
            mEffectScale = AnimationUtils.loadAnimation(getContext(), R.anim.filter_effect_scale);
            mEffectScale.setFillAfter(true);
        }
    }

    private void initView2() {
        mRecyclerView = $(R.id.recyclerViewFilter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        //设置添加或删除item时的动画，这里使用默认动画
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mAdapter = new EffectFilterAdapter(getContext());

        mAdapter.setCallBack(mEffectHandler.enableMultiEffect(), new EffectFilterAdapter.ICallBack() {

            @Override
            public void onItemClick(int position) {
                onItemClickImp(position);
            }

            @Override
            public void onTouchBegin(View view, EffectFilterInfo filterInfo) {
                isDown = true;
                FilterEffectItem filterEffectItem = new FilterEffectItem(filterInfo.getCoreFilterId(), 0, 0, filterInfo.getColor());
                mArrFilterEffectItem.add(filterEffectItem);
                float startTime = Utils.ms2s(mPlayer.getCurrentPosition());
                float duration = Utils.ms2s(mPlayer.getDuration());
                if (startTime >= duration) {
                    startTime = 0;
                }
                mViewThumbnail.drawEffectRect(startTime, filterEffectItem);
                EffectInfo info = new EffectInfo(filterInfo.getCoreFilterId());
                info.setTimeRange(startTime, duration);
                mArrEffectInfo.add(info);
                mEffectHandler.updateEffects(mArrEffectInfo);
                playVideo();
                if (view != null) {
                    view.setAnimation(mEffectScale);
                    view.startAnimation(mEffectScale);
                    if (view instanceof ExtCircleSimpleDraweeView) {
                        ((ExtCircleSimpleDraweeView) view).showShadow(filterInfo.getColor());
                    }
                }

            }

            @Override
            public void onTouchEnd(View view) {
                isDown = false;
                float progress = 0;
                if (lastIsComplete) {
                    progress = Utils.ms2s(mPlayer.getDuration());
                }
                pauseVideo();
                if (!lastIsComplete) {
                    progress = Utils.ms2s(mPlayer.getCurrentPosition());
                }
                float startTime = mViewThumbnail.stopDrawEffectRect(progress);
                setPosition(progress);
                int index = mArrEffectInfo.size() - 1;
                if (index >= 0) {
                    mArrEffectInfo.get(index).setTimeRange(startTime, progress);
                    mEffectHandler.updateEffects(mArrEffectInfo);
                }
                view.clearAnimation();
                if (view instanceof ExtCircleSimpleDraweeView) {
                    ((ExtCircleSimpleDraweeView) view).cancelShadow();
                }
            }

            float begin = 0;

            @Override
            public void onTouchBeginEarser() {
                //按住时清除全部特效
                mEffectHandler.updateEffects(null);
                begin = mViewThumbnail.getPosition();
                mViewThumbnail.beginEraser(begin);
                playVideo();
            }

            @Override
            public void onTouchEndEarser() {
                pauseVideo();
                float end = mViewThumbnail.getPosition();
                mViewThumbnail.endEraser(end);
                //截取特效
                if (mArrFilterEffectItem.size() > 0) {
                    ArrayList<FilterEffectItem> tmp = new ArrayList<>();
                    int len = mArrFilterEffectItem.size();
                    for (int i = 0; i < len; i++) {
                        FilterEffectItem tmpItem = mArrFilterEffectItem.get(i);
                        float fstart = tmpItem.getStartTime(), fend = tmpItem.getEndTime();
                        if (begin <= fstart && fend <= end) {
                            //当前橡皮擦时间线范围内的数据全部删掉
                        } else {
                            if (fstart < end && begin < fend) {
                                if (fstart < begin && begin < fend) {
                                    //需要拆分保留前段
                                    FilterEffectItem item = new FilterEffectItem(tmpItem.getFilterId(), fstart, begin, tmpItem.getColor());
                                    mViewThumbnail.drawEffectRect(item);
                                    tmp.add(item);
                                }
                                if (fend > end) {
                                    //保留后段
                                    FilterEffectItem item = new FilterEffectItem(tmpItem.getFilterId(), end, fend, tmpItem.getColor());
                                    mViewThumbnail.drawEffectRect(item);
                                    tmp.add(item);
                                }
                            } else {
                                //与橡皮擦时间线没交集的特效，完全保留
                                tmp.add(tmpItem);
                            }
                        }
                    }

                    mArrFilterEffectItem.clear();
                    mArrFilterEffectItem.addAll(tmp);
                    reloadEffect();
                    mViewThumbnail.updateEffectList(mArrFilterEffectItem);
                    mEffectHandler.updateEffectsReload(mArrEffectInfo, Utils.s2ms(end));
                    mViewThumbnail.setPosition(end);

                }
            }

            @Override
            public void onRevoke() {
                revokeEffect();
            }

            @Override
            public VirtualVideoView getPlayer() {
                return mEffectHandler.getPlayer();
            }

            @Override
            public VirtualVideo getThumbVirtualVideo() {
                return mEffectHandler.getSnapVideo();
            }


        });

        mPlayState = $(R.id.ivPlayerState);
        tvEffectHint = $(R.id.tvEffectHint);
        mViewThumbnail = $(R.id.view_thumbnail);
        mRadioGroup = $(R.id.rgEffect);
        mLlTimeEffect = $(R.id.ll_time_effect);
        mLlFilterEffect = $(R.id.ll_filter_effect);

        //时间特效按钮
        mRbNull = $(R.id.rb_effect_null);
        mRbSlow = $(R.id.rb_effect_slow);
        mRbRepeat = $(R.id.rb_effect_repeat);
        mLlNull = $(R.id.ll_effect_null);
        mLlSlow = $(R.id.ll_effect_slow);
        mLlRepeat = $(R.id.ll_effect_repeat);
    }

    /**
     * 片段编辑，单个特效
     *
     * @param position
     */
    private void onItemClickImp(int position) {
        if (!mEffectHandler.enableMultiEffect()) {
            //是否同时支持多个特效
            for (int i = 0; i < mArrEffectInfo.size(); i++) {
                EffectInfo effectInfo = mArrEffectInfo.get(i);
                if (effectInfo.getFilterId() != EffectInfo.Unknown) {
                    //滤镜特效
                    mArrEffectInfo.remove(effectInfo);
                }
            }
            mArrFilterEffectItem.clear();
        }
        int nStart = 0;
        if (position > 0) {
            //片段编辑，整个时间轴都铺满当前效果
            EffectFilterInfo filterInfo = mAdapter.getItem(position);
            FilterEffectItem filterEffectItem = null;
            if (filterInfo.getDuration() != 0) {
                //转场特效|| 定格特效
                nStart = mPlayer.getCurrentPosition();
                filterEffectItem = new FilterEffectItem(filterInfo.getCoreFilterId(), MiscUtils.ms2s(nStart), Utils.ms2s(nStart) + filterInfo.getDuration(), filterInfo.getColor());
            } else {
                //其他特效  ( 默认（0,0）片段铺满)
                nStart = 0;
                filterEffectItem = new FilterEffectItem(filterInfo.getCoreFilterId(), MiscUtils.ms2s(nStart), Utils.ms2s(mPlayer.getDuration()), filterInfo.getColor());
            }

            mViewThumbnail.drawEffectRect(filterEffectItem);
            mArrFilterEffectItem.add(filterEffectItem);

            EffectInfo info = new EffectInfo(filterInfo.getCoreFilterId());
            info.setTimeRange(filterEffectItem.getStartTime(), filterEffectItem.getEndTime());
            mArrEffectInfo.add(info);
        }

        mViewThumbnail.updateEffectList(mArrFilterEffectItem);
        mEffectHandler.updateEffectsReload(mArrEffectInfo, nStart);
        playVideo();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mPlayer = (IPlayer) context;
        mEffectHandler = (IEffectHandler) context;
        if (context instanceof IVideoEditorHandler) {
            mIVideoHandler = (IVideoEditorHandler) context;
        }
    }

    private ImageView mPlayState;
    private IVideoEditorHandler mIVideoHandler;

    private int mPadding;
    private final int N_RB_TIME_ID = -5125;

    /**
     * 特效分类
     */
    private void initEffectType() {
        if (null != mTypeDataInfos && mTypeDataInfos.size() > 0 && null != mRadioGroup) {
            View view = $(R.id.hsvMenu);
            view.measure(0, 0);
            int width = view.getWidth();
            mRadioGroup.removeAllViews();
            boolean enableTimeFilter = null != mEffectHandler && mEffectHandler.getReverseMediaObjcet() != null;
            int len = mTypeDataInfos.size();
            RadioGroup.LayoutParams params;
            int nMaxCount = 3;
            if (len <= nMaxCount) {
                params = new RadioGroup.LayoutParams(width / (enableTimeFilter ? len + 1 : len), ViewGroup.LayoutParams.WRAP_CONTENT);
                params.weight = 1;
            } else {
                params = new RadioGroup.LayoutParams((width / (enableTimeFilter ? nMaxCount + 1 : nMaxCount)) - CoreUtils.dpToPixel(10), ViewGroup.LayoutParams.WRAP_CONTENT);
            }
            mPadding = CoreUtils.dpToPixel(2);
            for (int i = 0; i < len; i++) {
                EffectTypeDataInfo info = mTypeDataInfos.get(i);
                TypeBean typeData = info.getType();
                createRadioButton(typeData.getName(), Integer.parseInt(typeData.getId()), params);
            }
            if (enableTimeFilter) {
                createRadioButton(getString(R.string.effcet_time), N_RB_TIME_ID, params);
            }
            //默认选中第0个
            mRadioGroup.check(Integer.parseInt(mTypeDataInfos.get(0).getType().getId()));
            $(R.id.menuFrameLayout).setVisibility(View.GONE);
        }


    }

    protected abstract int getLayoutId();

    /**
     * 创建单个分类
     */
    private void createRadioButton(String text, int nId, RadioGroup.LayoutParams lpItem) {
        RadioButton radioButton = new RadioButton(getContext());
        radioButton.setId(nId);
        radioButton.setText(text);
        radioButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        radioButton.setButtonDrawable(new ColorDrawable(Color.TRANSPARENT));
        radioButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        radioButton.setTextColor(getResources().getColorStateList(R.drawable.edit_menu_color));
        radioButton.setGravity(Gravity.CENTER);
        radioButton.setPadding(mPadding, 0, mPadding, 0);
        mRadioGroup.addView(radioButton, lpItem);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mRoot = inflater.inflate(getLayoutId(), container, false);
        bPrepared = false;
        initAnim();
        initView2();
        initEffect();
        initView();
        return mRoot;
    }


    private boolean lastIsComplete = false;

    public void onComplete() {
        lastIsComplete = true;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAdapter.setRunning(true);
        initThumbnail();
        initWebpIcon();
        mPlayState.setOnClickListener(onStateChangeListener);
        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == N_RB_TIME_ID) {
                    showTimeEffect();
                } else {
                    showFilterEffect();
                    if (null != mTypeDataInfos) {
                        EffectTypeDataInfo effectTypeDataInfo = mModel.getChild(mTypeDataInfos, checkedId);
                        mAdapter.addAll(effectTypeDataInfo.getList(), -1);
                    }
                }
            }
        });
        initEffectType();
        $(R.id.btnLeft).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEffectHandler.onEffectBackToMain();
                if (mIVideoHandler != null) {
                    mIVideoHandler.onBack();
                }
            }
        });
        $(R.id.btnRight).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEffectHandler.onEffectSure(mArrEffectInfo);
                if (mIVideoHandler != null) {
                    mIVideoHandler.onSure();
                }
            }
        });


    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mAdapter.setRunning(false);
        mAdapter.recycle();
        mViewThumbnail.recycle();
        mRadioGroup.removeAllViews();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != mTypeDataInfos) {
            mTypeDataInfos.clear();
            mTypeDataInfos = null;
        }
        if (null != mModel) {
            mModel.recycle();
            mModel = null;
        }
    }


    /**
     * 初始特效
     */
    private void initEffect() {
        mReverseStartTime = 0;
        float duration = Utils.ms2s(mPlayer.getDuration());
        mReverseEndTime = duration;

        //默认时间特效在中间，占总时长的1/5;
        float timeEffectDuration = duration / 5;
        mTimeEffectStartTime = (duration - timeEffectDuration) / 2;
        mTimeEffectEndTime = (duration + timeEffectDuration) / 2;

        mArrFilterEffectItem.clear();
        mArrEffectInfo = mEffectHandler.getEffectInfos();
        mTempArrEffect.clear();
        mTempArrEffect.addAll(mArrEffectInfo);
        mTimeEffectItem = new TimeEffectItem(getContext());
        mRecyclerView.setAdapter(mAdapter);


        //时间特效
        for (EffectInfo info : mArrEffectInfo) {
            float startTime = info.getStartTime();
            float endTime = info.getEndTime();
            EffectType type = info.getEffectType();
            if (type == EffectType.REPEAT || type == EffectType.REVERSE || type == EffectType.SLOW) {
                mTimeEffectItem.setType(type);
                if (type == EffectType.REVERSE) {
                    mReverseStartTime = startTime;
                    mReverseEndTime = endTime;
                } else {
                    mTimeEffectStartTime = startTime;
                    mTimeEffectEndTime = endTime;
                }
                mTimeEffectItem.setStartTime(startTime);
                mTimeEffectItem.setEndTime(endTime);
                continue;
            }
        }
    }


    /**
     * 滤镜特效
     */
    private void initFilterEffect() {
        for (EffectInfo info : mArrEffectInfo) {
            float startTime = info.getStartTime();
            float endTime = info.getEndTime();
            if (info.getFilterId() != EffectInfo.Unknown) {
                FilterEffectItem effectItem = null;
                EffectFilterInfo tmp = EffectManager.getInstance().getRegisterFilterInfo(info.getFilterId());
                int color = Color.RED;
                if (null != tmp) {
                    color = tmp.getColor();
                }
                if (startTime == endTime && startTime == 0) {
                    effectItem = new FilterEffectItem(info.getFilterId(), startTime, Utils.ms2s(mPlayer.getDuration()), color);
                } else {
                    effectItem = new FilterEffectItem(info.getFilterId(), startTime, endTime, color);
                }
                mArrFilterEffectItem.add(effectItem);
            }
        }

        mViewThumbnail.setFilterEffectList(mArrFilterEffectItem);
        for (FilterEffectItem effectItem : mArrFilterEffectItem) {
            mViewThumbnail.drawEffectRect(effectItem);
        }

    }


    private void updateAdapter(List<EffectFilterInfo> list, int index) {
        //滤镜特效
        initFilterEffect();
        if (null == list) {
            onToast(R.string.load_http_failed);
        }
        mAdapter.addAll(list, index);

    }

    /**
     * 初始化界面
     */
    private void initView() {
        mViewThumbnail.setOnEffectChangeListener(onEffectChangeListener);
        mLlNull.setOnClickListener(onTimeEffectClickListener);
        mLlSlow.setOnClickListener(onTimeEffectClickListener);
        mLlRepeat.setOnClickListener(onTimeEffectClickListener);
        mViewThumbnail.setTimeEffect(mTimeEffectItem);
        setDefaultTimeEffectType();
        mPlayer.seekTo(0);
        pauseVideo();
    }

    private void initWebpIcon() {
        SimpleDraweeViewUtils.setCover(mRbNull, R.drawable.effect_time_none);
        SimpleDraweeViewUtils.setCover(mRbSlow, R.drawable.effect_time_slow);
        SimpleDraweeViewUtils.setCover(mRbRepeat, R.drawable.effect_time_repeat);
    }

    private boolean bPrepared = false;

    public void initThumbnail() {
        int padding = 0;
        int duMs = mPlayer.getDuration();
        if (null != mViewThumbnail && !bPrepared) {
            bPrepared = true;
            int widthPixels = CoreUtils.getMetrics().widthPixels - getResources().getDimensionPixelSize(R.dimen.add_sub_play_state_size);
            int[] param = mViewThumbnail.setPlayer(mEffectHandler.getSnapVideo(), widthPixels, padding, Utils.ms2s(duMs));

            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) mViewThumbnail.getLayoutParams();
            layoutParams.width = param[0];
            layoutParams.height = param[1];
            mViewThumbnail.setLayoutParams(layoutParams);

            mViewThumbnail.setStartThumb();
            for (FilterEffectItem effectItem : mArrFilterEffectItem) {
                mViewThumbnail.drawEffectRect(effectItem);
            }
            mViewThumbnail.drawTimeEffectRect(mTimeEffectItem.getStartTime(), mTimeEffectItem.getEndTime());
            setPosition(0);
        }
    }

    private VideoThumbNailView2.OnEffectChangeListener onEffectChangeListener = new VideoThumbNailView2.OnEffectChangeListener() {
        @Override
        public void onPositionMove(float position) {
            mPlayer.seekTo(Utils.s2ms(position));
            if (mPlayer.isPlaying()) {
                pauseVideo();
            }
        }

        @Override
        public void onPositionUp() {
            reloadEffect();
            mEffectHandler.updateEffectsReload(mArrEffectInfo, Utils.s2ms(mTimeEffectItem.getStartTime()));
        }
    };

    /**
     * 根据时间特效类型设置对应按钮的状态
     */
    public void setDefaultTimeEffectType() {
        refreshTimeEffectButton();
        if (mTimeEffectItem.getType() == EffectType.SLOW) {
            mRbSlow.setChecked(true);
        } else if (mTimeEffectItem.getType() == EffectType.REPEAT) {
            mRbRepeat.setChecked(true);
        } else {
            mRbNull.setChecked(true);
        }
    }


    @Override
    public int onBackPressed() {
        if (isRunning) {
            mTimeEffectStartTime = 0;
            mTimeEffectEndTime = 0;
            mReverseStartTime = 0;
            mReverseEndTime = 0;
            mArrEffectInfo.clear();
            mArrEffectInfo.addAll(mTempArrEffect);
            mEffectHandler.updateEffects(mArrEffectInfo);
            return 1;
        } else {
            return super.onBackPressed();
        }
    }

    /**
     * 点击时间特效
     */
    public void showTimeEffect() {
        tvEffectHint.setText(R.string.effect_hint_time);
        mLlFilterEffect.setVisibility(View.INVISIBLE);
        mLlTimeEffect.setVisibility(View.VISIBLE);
        mViewThumbnail.setDrawTimeEffect(true);
        mPlayer.seekTo(0);

    }

    /**
     * 点击滤镜特效
     */
    public void showFilterEffect() {
        tvEffectHint.setText(R.string.effect_hint_filter);
        mLlFilterEffect.setVisibility(View.VISIBLE);
        mLlTimeEffect.setVisibility(View.INVISIBLE);
        mViewThumbnail.setDrawTimeEffect(false);
        mPlayer.seekTo(0);

    }


    /**
     * 撤回按钮
     */
    public void revokeEffect() {
        pauseVideo();
        if (mArrFilterEffectItem.size() > 0) {
            FilterEffectItem effectItem = mArrFilterEffectItem.remove(mArrFilterEffectItem.size() - 1);
            reloadEffect();
            if (mArrFilterEffectItem.size() == 0) {
                mPlayer.seekTo(0);
                setPosition(0);
            } else {
                mPlayer.seekTo(Utils.s2ms(effectItem.getStartTime()));
                setPosition(effectItem.getStartTime());
            }
            mEffectHandler.updateEffects(mArrEffectInfo);
            mViewThumbnail.invalidate();
        }
        pauseVideo();
    }

    // 防止多次触摸
    private boolean isDown = false;


    /**
     * 时间特效按钮响应回调
     */
    private View.OnClickListener onTimeEffectClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int checkedId = view.getId();
            refreshTimeEffectButton();
            EffectType type = EffectType.NONE;
            if (checkedId == R.id.ll_effect_slow) {
                type = EffectType.SLOW;
                mRbSlow.setChecked(true);
            } else if (checkedId == R.id.ll_effect_repeat) {
                type = EffectType.REPEAT;
                mRbRepeat.setChecked(true);
            } else {
                mRbNull.setChecked(true);
            }
            if (mTimeEffectItem.getType() == type) {
                mPlayer.seekTo(Utils.s2ms(mTimeEffectItem.getStartTime()));
                return;
            }
            mTimeEffectItem.setType(type);
            if (mTimeEffectItem.getType() == EffectType.REVERSE) {
                mViewThumbnail.drawTimeEffectRect(mReverseStartTime, mReverseEndTime);
            } else if (mTimeEffectItem.getType() == EffectType.SLOW) {
                mViewThumbnail.drawTimeEffectRect(mTimeEffectStartTime, mTimeEffectEndTime);
            }
            reloadEffect();
            //时间特效
            mEffectHandler.updateEffectsReload(mArrEffectInfo, Utils.s2ms(mTimeEffectItem.getStartTime()));
            mViewThumbnail.setPosition(mTimeEffectItem.getStartTime());
        }
    };

    private void reloadEffect() {
        mArrEffectInfo.clear();
        EffectInfo timeEffect = new EffectInfo();
        if (mTimeEffectItem.getType() != EffectType.NONE) {
            float startTime = mTimeEffectItem.getStartTime();
            float endTime = mTimeEffectItem.getEndTime();
            if (mTimeEffectItem.getType() == EffectType.REVERSE) {
                mReverseStartTime = startTime;
                mReverseEndTime = endTime;
            } else {
                mTimeEffectStartTime = startTime;
                mTimeEffectEndTime = endTime;
            }
            timeEffect.setEffectType(mTimeEffectItem.getType());
            timeEffect.setTimeRange(mTimeEffectItem.getStartTime(), mTimeEffectItem.getEndTime());
            mArrEffectInfo.add(timeEffect);
        }
        for (FilterEffectItem effectItem : mArrFilterEffectItem) {
            EffectInfo info = new EffectInfo();
            info.setEffectType(effectItem.getType());
            info.setFilterId(effectItem.getFilterId());
            info.setTimeRange(effectItem.getStartTime(), effectItem.getEndTime());
            mArrEffectInfo.add(info);
        }
    }

    private void refreshTimeEffectButton() {
        mRbNull.setChecked(false);
        mRbSlow.setChecked(false);
        mRbRepeat.setChecked(false);
    }

    public void setPosition(float position) {
        if (mViewThumbnail != null) {
            mViewThumbnail.setPosition(position);
            if (!isDown && position >= Utils.ms2s(mPlayer.getDuration())) {
                mViewThumbnail.setPosition(0);
            }
        }
    }


    private IEffectHandler mEffectHandler;

    public interface IEffectHandler {

        VirtualVideoView getPlayer();

        ArrayList<EffectInfo> getEffectInfos();


        /**
         * 滤镜特效可以实时预览
         *
         * @param list
         */
        void updateEffects(ArrayList<EffectInfo> list);

        /**
         * 时间特效预览 （必须reload）
         *
         * @param list
         * @param seekto
         */
        void updateEffectsReload(ArrayList<EffectInfo> list, int seekto);

        VirtualVideo getSnapVideo();

        void onEffectBackToMain();

        void onEffectSure(ArrayList<EffectInfo> list);

        MediaObject getReverseMediaObjcet();

        /**
         * 是否支持多个滤镜特效 （true ：编辑一 ；false 片段编辑）
         *
         * @return
         */
        boolean enableMultiEffect();


    }
}
