package com.rd.veuisdk.fragment;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.rd.lib.utils.CoreUtils;
import com.rd.vecore.models.Transition;
import com.rd.vecore.models.TransitionType;
import com.rd.veuisdk.IEditPreviewHandler;
import com.rd.veuisdk.R;
import com.rd.veuisdk.adapter.BaseRVAdapter;
import com.rd.veuisdk.adapter.TransitionAdapter;
import com.rd.veuisdk.database.TransitionData;
import com.rd.veuisdk.listener.OnItemClickListener;
import com.rd.veuisdk.model.EffectTypeDataInfo;
import com.rd.veuisdk.model.TransitionInfo;
import com.rd.veuisdk.model.bean.TypeBean;
import com.rd.veuisdk.mvp.model.TransitionModel;
import com.rd.veuisdk.mvp.persenter.TransitionPersenter;
import com.rd.veuisdk.mvp.view.ITransitionView;
import com.rd.veuisdk.utils.SysAlertDialog;
import com.rd.veuisdk.utils.TransitionManager;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/**
 * 切换转场
 *
 * @author scott
 */
public class TransitionFragment extends BaseFragment implements ITransitionView<EffectTypeDataInfo<TransitionInfo>> {


    @Override
    public void showLoading() {
        SysAlertDialog.showLoadingDialog(mContext, R.string.isloading);
    }

    @Override
    public void downFailed(int itemId, int strId) {
        onToast(strId);
    }

    private List<EffectTypeDataInfo<TransitionInfo>> mList = null;

    @Override
    public void downSuccessed(int itemId, TransitionInfo info) {
        onItemClickImp(itemId);
    }

    @Override
    public void onSuccess(List<EffectTypeDataInfo<TransitionInfo>> list) {
        mList = list;
        initTransitionTypeList();
    }


    private float mTransitionDuration;
    private Transition mTransition;
    private boolean mApplyToAll = false;
    private boolean mIsRandom = false;
    private TransitionAdapter mTransitionAdapter;
    private int mTransitionCount = 1;

    private RecyclerView mRvTransition;
    private CheckBox mCbRandomTransition;
    private TextView mTvTransitionDuration;
    private SeekBar mSbTransitionDuration;
    private boolean cbRandomFormUser = true;
    private RadioGroup mRadioGroup;
    private TransitionPersenter mPresenter;
    private String mUrl;
    private String mTypeUrl;
    private IEditPreviewHandler iEditPreviewHandler;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        iEditPreviewHandler = (IEditPreviewHandler) context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        TAG = "TransitionFragment";
        super.onCreate(savedInstanceState);
        TransitionData.getInstance().initilize(getContext());
        mPageName = getString(R.string.transition);
        if (mTransitionAdapter == null) {
            mTransitionAdapter = new TransitionAdapter(getContext());
        }
        if (mTransition == null) {
            mTransition = new Transition(TransitionType.TRANSITION_NULL);
            mTransition.setDuration(1f);
        }
        if (TextUtils.isEmpty(mTransition.getTitle())) {
            mTransition.setTitle(getString(R.string.none));
        }
        mTransitionDuration = mTransition.getDuration();
        mPresenter = new TransitionPersenter(getContext());
        mPresenter.attachView(this);
        mPresenter.initData(mTypeUrl, mUrl);
    }


    public void setUrl(String typeUrl, String url) {
        mTypeUrl = typeUrl;
        mUrl = url;
    }

    public void setSceneCount(int count) {
        mTransitionCount = count;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.fragment_transition, container, false);
        mRadioGroup = $(R.id.rgEffect);
        initViews();
        mRvTransition.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        mRvTransition.setAdapter(mTransitionAdapter);
        mTransitionAdapter.setOnItemClickListener(new OnItemClickListener<Object>() {
            @Override
            public void onItemClick(int position, Object item) {
                if (position != BaseRVAdapter.UN_CHECK) {
                    if (mCbRandomTransition.isChecked()) {
                        cbRandomFormUser = false;
                        mCbRandomTransition.setChecked(false);
                    }
                    onItemClickImp(position);
                }
            }
        });
        return mRoot;
    }

    /**
     * 获取指定类型的数据
     */
    private EffectTypeDataInfo getChild(String typeId) {
        EffectTypeDataInfo tmp = null;
        int len = mList.size();
        EffectTypeDataInfo info;
        for (int i = 0; i < len; i++) {
            info = mList.get(i);
            if (info.getType().getId().equals(typeId)) {
                tmp = info;
                break;
            }
        }
        return tmp;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (null != mList && mList.size() > 0 && checkedId != 0) {
                    mTransitionAdapter.updateData(getChild(Integer.toString(checkedId)).getList(), BaseRVAdapter.UN_CHECK);
                }
            }
        });
        initTransitionTypeList();
    }

    private int mPadding;

    /**
     * 有数据再初始化分组
     */
    private void initTransitionTypeList() {
        if (null != mRadioGroup) {
            mRadioGroup.removeAllViews();
            if (null != mList && mList.size() > 0) {
                View view = $(R.id.hsvMenu);
                view.measure(0, 0);
                int width = view.getWidth();

                int len = mList.size();
                RadioGroup.LayoutParams params;
                int nMaxCount = 3;
                if (len <= nMaxCount) {
                    params = new RadioGroup.LayoutParams(width / len, ViewGroup.LayoutParams.WRAP_CONTENT);
                    params.weight = 1;
                } else {
                    params = new RadioGroup.LayoutParams((width / nMaxCount) - CoreUtils.dpToPixel(10), ViewGroup.LayoutParams.WRAP_CONTENT);
                }
                mPadding = CoreUtils.dpToPixel(2);
                for (int i = 0; i < len; i++) {
                    EffectTypeDataInfo info = mList.get(i);
                    TypeBean typeData = info.getType();
                    createRadioButton(typeData.getName(), Integer.parseInt(typeData.getId()), params);
                }

                //默认选中第0个
                EffectTypeDataInfo info = mList.get(0);
                mRadioGroup.check(Integer.parseInt(info.getType().getId()));
                setData(info.getList());

                $(R.id.menuFrameLayout).setVisibility(View.GONE);
            }
        }
    }

    private void setData(List<TransitionInfo> list) {
        int index = 0;
        if (null != mTransition) {
            Object object = mTransition.getTag();
            if (null != object) {
                int len = list.size();
                for (int i = 0; i < len; i++) {
                    TransitionInfo transitionInfo = list.get(i);
                    if (object.equals(transitionInfo.getFile()) || object.equals(transitionInfo.getName())) {
                        index = i;
                        break;
                    }
                }
            }
        }
        SysAlertDialog.cancelLoadingDialog();
        if (null != mTransitionAdapter) {
            mTransitionAdapter.updateData(list, index);
            mRvTransition.scrollToPosition(index);
        }
    }


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

    /**
     * 设置当前转场
     *
     * @param transition
     */
    public void setCurTransition(Transition transition) {
        mTransition = transition;
    }

    /**
     * @param position
     */
    private void onItemClickImp(int position) {
        Transition tmp = getTransition(position);
        if (null != tmp) {
            mTransition = tmp;
        } else {
            //正在下载，忽略
            return;
        }
        reload();
    }

    private Transition getTransition(int itemPosition) {
        TransitionInfo info = mTransitionAdapter.getItem(itemPosition);
        if (mPresenter.isWebTansition()) {
            if (mRadioGroup.getCheckedRadioButtonId() == TransitionModel.BASE_TYPE_ID || info.isExistFile()) {
                //内置的基本效果 或  已经下载的
                if (!mIsRandom) {
                    mTransitionAdapter.setChecked(itemPosition);
                }
                return fixTransition(info);
            } else {
                if (CoreUtils.checkNetworkInfo(getContext()) == CoreUtils.UNCONNECTED) {
                    onToast(R.string.please_check_network);
                    return null;
                } else {
                    //未下载，准备执行下载网络文件
                    if (!mIsRandom) {
                        mTransitionAdapter.setChecked(itemPosition);
                    }
                    //执行下载
                    SysAlertDialog.showLoadingDialog(mContext, getString(R.string.isloading));
                    mPresenter.downTransition(getContext(), itemPosition, info);
                    return null;
                }
            }
        } else {
            if (!mIsRandom) {
                mTransitionAdapter.setChecked(itemPosition);
            }
            return fixTransition(info);
        }
    }

    /**
     * @param info
     * @return
     */
    private Transition fixTransition(TransitionInfo info) {
        Transition transition = null;
        if (null == info) { //防止null
            transition = new Transition(TransitionType.TRANSITION_NULL);
            transition.setTitle(getString(R.string.none));
            transition.setTag(transition.getTitle());
            return transition;
        }
        TransitionType type = null;
        String str = info.getName();
        if (str.equals(getString(R.string.none))) {
            type = TransitionType.TRANSITION_NULL;
        } else if (str.equals(getString(R.string.show_style_item_recovery))) {
            type = TransitionType.TRANSITION_OVERLAP;
        } else if (str.equals(getString(R.string.show_style_item_to_up))) {
            type = TransitionType.TRANSITION_TO_UP;
        } else if (str.equals(getString(R.string.show_style_item_to_down))) {
            type = TransitionType.TRANSITION_TO_DOWN;
        } else if (str.equals(getString(R.string.show_style_item_to_left))) {
            type = TransitionType.TRANSITION_TO_LEFT;
        } else if (str.equals(getString(R.string.show_style_item_to_right))) {
            type = TransitionType.TRANSITION_TO_RIGHT;
        } else if (str.equals(getString(R.string.show_style_item_flash_white))) {
            type = TransitionType.TRANSITION_BLINK_WHITE;
        } else if (str.equals(getString(R.string.show_style_item_flash_black))) {
            type = TransitionType.TRANSITION_BLINK_BLACK;
        } else {
            if (TransitionManager.getInstance().isGlsl(info)) {
                //glsl 转场滤镜
                if (info.getCoreFilterId() == Transition.Unknown) {
                    //未注册
                    TransitionManager.getInstance().init(getContext(), info, null);
                    TransitionManager.getInstance().add(info.getFile(), info.getCoreFilterId());
                }
                if (info.getCoreFilterId() != Transition.Unknown) {
                    transition = new Transition(info.getCoreFilterId());
                    transition.setTag(info.getFile());
                } else {
                    //注册失败，强制切到无
                    type = TransitionType.TRANSITION_NULL;
                }
            } else {
                //普通灰度图转场
                type = TransitionType.TRANSITION_GRAY;
            }
        }

        if (null != type) {
            //普通转场
            transition = new Transition(type, info.getLocalPath());
            transition.setTag(info.getName());
        }
        transition.setTitle(info.getName());
        return transition;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mPresenter.detachView();
        mPresenter.recycle();
    }

    @Override
    public void onDestroy() {
        mPresenter.detachView();
        mPresenter.recycle();
        if (null != mTransitionAdapter) {
            mTransitionAdapter.recycle();
        }
        mTransitionAdapter = null;
        super.onDestroy();
        TransitionData.getInstance().close();
        mPresenter = null;
        if (null != mList) {
            mList.clear();
            mList = null;
        }
    }


    /**
     * 仅限本地转场
     *
     * @return
     */
    private Transition getRandomTransition() {

        int nTmp;
        if (!mPresenter.isWebTansition()) {
            //避免出现无转场 即0
            Random random = new Random();
            nTmp = random.nextInt(mTransitionAdapter.getItemCount() - 1) + 1;
        } else {
            nTmp = mTransitionAdapter.getRandomIndex();
        }
        return getTransition(nTmp);
    }

    private void initViews() {
        mRvTransition = $(R.id.gridview_transition);
        mCbRandomTransition = $(R.id.cbRandomTransition);
        mTvTransitionDuration = $(R.id.tvTransitionDuration);
        mSbTransitionDuration = $(R.id.sbTransitionTime);
        if (!TextUtils.isEmpty(mUrl)) {
            SysAlertDialog.showLoadingDialog(getContext(), R.string.isloading);
        }
        mCbRandomTransition.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mIsRandom = isChecked;
                if (!cbRandomFormUser) {
                    cbRandomFormUser = true;
                    return;
                }
                if (isChecked) {
                    mTransitionAdapter.setChecked(-1);
                    reload();
                } else {
                    onItemClickImp(0);
                }
            }
        });

        ((CheckBox) $(R.id.cbTransitionApplyToAll)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mApplyToAll = isChecked;
            }
        });

        $(R.id.btnRight).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                reload();
                if (mCbRandomTransition.isChecked()) {
                    cbRandomFormUser = false;
                    mCbRandomTransition.setChecked(false);
                }
                ((CheckBox) $(R.id.cbTransitionApplyToAll)).setChecked(false);
                iEditPreviewHandler.onSure();
            }
        });

        $(R.id.btnLeft).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mCbRandomTransition.isChecked()) {
                    cbRandomFormUser = false;
                    mCbRandomTransition.setChecked(false);
                }
                ((CheckBox) $(R.id.cbTransitionApplyToAll)).setChecked(false);
                iEditPreviewHandler.onBack();
            }
        });

        mSbTransitionDuration.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                iEditPreviewHandler.onTransitionDurationChanged(mTransitionDuration, mApplyToAll);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    double duration = MIN + (SBAR_DU * (progress / (0.0f + seekBar.getMax())));
                    mTvTransitionDuration.setText(getString(R.string.long_s, mDecimalFormat.format(duration)));
                    mTransitionDuration = (float) duration;
                }
            }
        });
        double duration = mTransitionDuration;
        mTvTransitionDuration.setText(getString(R.string.long_s, mDecimalFormat.format(duration)));
        mSbTransitionDuration.setProgress((int) (mSbTransitionDuration.getMax() * (duration - MIN) / (SBAR_DU)));
    }

    private final float MIN = 0.10f;
    private final float SBAR_DU = 2f - MIN;
    private final DecimalFormat mDecimalFormat = new DecimalFormat("##0.00");

    private void reload() {
        ArrayList<Transition> arrTransitions = new ArrayList<>();
        if (mIsRandom) {
            if (mApplyToAll) {
                for (int nTemp = 0; nTemp < mTransitionCount; nTemp++) {
                    arrTransitions.add(getRandomTransition());
                }
            } else {
                arrTransitions.add(getRandomTransition());
            }
        } else {
            if (null == mTransition) {
                //构造一个无转场的转场对象
                mTransition = fixTransition(mTransitionAdapter.getItem(0));
            }
            if (mApplyToAll) {
                for (int nTemp = 0; nTemp < mTransitionCount; nTemp++) {
                    arrTransitions.add(mTransition);
                }
            } else {
                arrTransitions.add(mTransition);
            }
        }
        for (Transition transition : arrTransitions) {
            transition.setDuration(mTransitionDuration);
        }
        iEditPreviewHandler.onTransitionChanged(arrTransitions, mApplyToAll);
    }

}
