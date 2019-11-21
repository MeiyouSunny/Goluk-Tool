package com.rd.veuisdk.fragment.helper;

import android.graphics.PointF;
import android.graphics.RectF;
import android.view.View;

import com.rd.veuisdk.R;

/**
 * 作者：JIAN on 2017/11/23 19:13
 * <p>
 * 字幕中心点坐标
 */
public class CaptionPositionHandler {

    private IPositionChangeListener mListener;

    public CaptionPositionHandler() {

    }

    /**
     * @param parent
     * @param listener
     */
    public void init(View parent, IPositionChangeListener listener) {
        this.mListener = listener;
        if (null != parent) {
            View left = parent.findViewById(R.id.ivSubtitleLeft);
            View right = parent.findViewById(R.id.ivSubtitleRight);
            View top = parent.findViewById(R.id.ivSubtitleTop);
            View bottom = parent.findViewById(R.id.ivSubtitleBottom);
            View centerVer = parent.findViewById(R.id.ivSubtitleVertMid);
            View centerHor = parent.findViewById(R.id.ivSubtitleHoriMid);

            left.setOnClickListener(mClickListener);
            right.setOnClickListener(mClickListener);
            top.setOnClickListener(mClickListener);
            bottom.setOnClickListener(mClickListener);
            centerVer.setOnClickListener(mClickListener);
            centerHor.setOnClickListener(mClickListener);
        }
    }

    private View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (null != mListener) {
                mListener.onChangePosition(v.getId());
            }
        }
    };

    /**
     *
     */
    public interface IPositionChangeListener {
        public void onChangePosition(int id);
    }


    /**
     * 获取新的移动
     *
     * @param src 原始区域
     * @param id
     * @return 新的中心点的坐标
     */
    public PointF getFixCenter(RectF src, int id) {

        PointF dst = new PointF();

        if (id == R.id.ivSubtitleLeft) {
            dst.set(src.centerX() - src.left, src.centerY());
        } else if (id == R.id.ivSubtitleRight) {
            dst.set(src.centerX() + (1 - src.right), src.centerY());
        } else if (id == R.id.ivSubtitleTop) {
            dst.set(src.centerX(), src.centerY() - src.top);
        } else if (id == R.id.ivSubtitleBottom) {
            dst.set(src.centerX(), src.centerY() + (1 - src.bottom));
        } else if (id == R.id.ivSubtitleVertMid) {
            dst.set(src.centerX(), 0.5f);
        } else if (id == R.id.ivSubtitleHoriMid) {
            dst.set(0.5f, src.centerY());
        } else {
            dst.set(0.5f, 0.5f);
        }
        return dst;


    }

}
