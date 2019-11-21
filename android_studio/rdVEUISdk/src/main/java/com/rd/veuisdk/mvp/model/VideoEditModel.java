package com.rd.veuisdk.mvp.model;

import android.content.Context;
import android.support.annotation.StringRes;

import com.rd.vecore.models.Transition;
import com.rd.vecore.models.TransitionType;
import com.rd.veuisdk.R;
import com.rd.veuisdk.model.TransitionInfo;
import com.rd.veuisdk.utils.TransitionManager;

import java.util.List;
import java.util.Random;

/**
 * VideoEditActivity 数据
 *
 * @author JIAN
 * @create 2019/4/29
 * @Describe
 */
public class VideoEditModel {
    private Context mContext;

    public VideoEditModel(Context context) {
        mContext = context;
    }

    private VideoEditModel() {

    }

    private String getString(@StringRes int resId) {
        return mContext.getString(resId);
    }

    private List<TransitionInfo> mListRandTransiton;

    public void init() {
        if (mListRandTransiton == null || mListRandTransiton.isEmpty()) {
            TransitionModel model = new TransitionModel(mContext);
            mListRandTransiton = model.initData(null, true);
        }
    }

    /**
     * 获取随机转场
     */
    public Transition getRandomTransition() {
        Random random = new Random();
        int index = random.nextInt(mListRandTransiton.size());
        TransitionInfo info = mListRandTransiton.get(index);
        TransitionType type = null;
        String str = info.getName();
        Transition transition = null;
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
                    TransitionManager.getInstance().init(mContext, info, null);
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
}
