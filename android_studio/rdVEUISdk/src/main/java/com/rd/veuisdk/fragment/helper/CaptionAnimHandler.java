package com.rd.veuisdk.fragment.helper;

import android.graphics.PointF;
import android.graphics.RectF;
import android.view.View;
import android.widget.CheckedTextView;

import com.rd.vecore.models.caption.CaptionAnimation;
import com.rd.veuisdk.R;

/**
 * 作者：JIAN on 2017/11/23 14:36
 * 字幕动画
 */
public class CaptionAnimHandler {
    private View parent;
    private CheckedTextView anim_none;
    private CheckedTextView animPushLeft;
    private CheckedTextView animPushRight;
    private CheckedTextView animPushTop;
    private CheckedTextView animPushBottom;
    private CheckedTextView animScaleFade;
    private CheckedTextView animRotateFade;
    private CheckedTextView animFadeInOut;
    private CheckedTextView lastChecked;

    public CaptionAnimHandler(View parent) {
        this.parent = parent;
        anim_none = (CheckedTextView) parent.findViewById(R.id.anim_none);
        animPushLeft = (CheckedTextView) parent.findViewById(R.id.anim_left);
        animPushRight = (CheckedTextView) parent.findViewById(R.id.anim_right);
        animPushTop = (CheckedTextView) parent.findViewById(R.id.anim_top);
        animPushBottom = (CheckedTextView) parent.findViewById(R.id.anim_bottom);
        animScaleFade = (CheckedTextView) parent.findViewById(R.id.anim_scale_fade_in);
        animRotateFade = (CheckedTextView) parent.findViewById(R.id.anim_rotate_fade_in);
        animFadeInOut = (CheckedTextView) parent.findViewById(R.id.anim_fade_in_out);
        anim_none.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCheckedItem(v);
            }
        });
        animPushLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCheckedItem(v);
            }
        });
        animPushRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCheckedItem(v);
            }
        });
        //从上往下
        animPushTop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCheckedItem(v);
            }
        });
        //从下往上
        animPushBottom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCheckedItem(v);
            }
        });
        //缩放
        animScaleFade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCheckedItem(v);
            }
        });
        //滚动
        animRotateFade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCheckedItem(v);
            }
        });


        //淡入淡出
        animFadeInOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCheckedItem(v);
            }
        });
    }


    private void onCheckedItem(View v) {
        if (null != lastChecked) {
            if (lastChecked.getId() != v.getId()) {
                lastChecked.setChecked(false);
            }
        }
        if (lastChecked.getId() != v.getId()) {
            lastChecked = (CheckedTextView) v;
            lastChecked.setChecked(true);
        }

    }


    /**
     * 还原按钮状态
     *
     * @param checkId
     */
    public void resetCheckAnim(int checkId) {

        if (null != lastChecked) {
            lastChecked.setChecked(false);
        }
        if (checkId == R.id.anim_left) {
            lastChecked = animPushLeft;
        } else if (checkId == R.id.anim_right) {
            lastChecked = animPushRight;
        } else if (checkId == R.id.anim_top) {
            lastChecked = animPushTop;
        } else if (checkId == R.id.anim_bottom) {
            lastChecked = animPushBottom;
        } else if (checkId == R.id.anim_scale_fade_in) {
            lastChecked = animScaleFade;
        } else if (checkId == R.id.anim_rotate_fade_in) {
            lastChecked = animRotateFade;
        } else if (checkId == R.id.anim_fade_in_out) {
            lastChecked = animFadeInOut;
        } else {
            lastChecked = anim_none;

        }
        lastChecked.setChecked(true);

    }


    /**
     * 被选中的模式
     *
     * @return
     */
    public int getCheckedId() {
        return null != lastChecked ? lastChecked.getId() : R.id.anim_none;
    }

    /***
     * 获取指定的字幕动画
     * @param captionPreviewRectF  预览时字幕在预览框中的位置0~1.0f
     * @return 字幕动画
     */
    public CaptionAnimation getAnimation(RectF captionPreviewRectF) {
        int checkId = null != lastChecked ? lastChecked.getId() : R.id.anim_none;
        return getAnimation(captionPreviewRectF, checkId);
    }

    /**
     *
     * @param captionPreviewRectF
     * @param checkId
     * @return
     */
    public CaptionAnimation getAnimation(RectF captionPreviewRectF, int checkId) {
        CaptionAnimation animation = null;
        if (checkId == R.id.anim_none) {
            animation = null;
        } else if (checkId == R.id.anim_left) {
            animation = new CaptionAnimation(CaptionAnimation.CaptionAnimationType.MO_ANIMATION_TYPE_MOVE);

            PointF rtl = new PointF(1 + (captionPreviewRectF.width() / 2), captionPreviewRectF.centerY());

            animation.setMove(rtl, 2f, null, 0);


        } else if (checkId == R.id.anim_right) {
            animation = new CaptionAnimation(CaptionAnimation.CaptionAnimationType.MO_ANIMATION_TYPE_MOVE);

            PointF ltr = new PointF(-(captionPreviewRectF.width() / 2), captionPreviewRectF.centerY());

            animation.setMove(ltr, 2f, null, 0);


        } else if (checkId == R.id.anim_top) {


            animation = new CaptionAnimation(CaptionAnimation.CaptionAnimationType.MO_ANIMATION_TYPE_MOVE);

            PointF btt = new PointF(captionPreviewRectF.centerX(), 1 + (captionPreviewRectF.height() / 2));

            animation.setMove(btt, 2f, null, 0);

        } else if (checkId == R.id.anim_bottom) {


            animation = new CaptionAnimation(CaptionAnimation.CaptionAnimationType.MO_ANIMATION_TYPE_MOVE);

            PointF ttb = new PointF(captionPreviewRectF.centerX(), -(captionPreviewRectF.height() / 2));

            animation.setMove(ttb, 2f, null, 0);

        } else if (checkId == R.id.anim_scale_fade_in) {


            animation = new CaptionAnimation(CaptionAnimation.CaptionAnimationType.MO_ANIMATION_TYPE_ZOOM);
            animation.setZoom(0f, 1f, 0f, 1f);
            animation.setFadeInOut(true, 1f, 1f);

        } else if (checkId == R.id.anim_rotate_fade_in) {

            animation = new CaptionAnimation(CaptionAnimation.CaptionAnimationType.MO_ANIMATION_TYPE_EXPAND);
            animation.setExpand(1f, 1f);
            animation.setFadeInOut(true, 1f, 1f);

        } else if (checkId == R.id.anim_fade_in_out) {
            animation = new CaptionAnimation(CaptionAnimation.CaptionAnimationType.MO_ANIMATION_TYPE_FADE);
            animation.setFadeInOut(true, 1f, 1f);
        }
        return animation;
    }


}
