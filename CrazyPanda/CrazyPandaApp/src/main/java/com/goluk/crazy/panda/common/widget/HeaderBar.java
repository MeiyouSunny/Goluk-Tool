package com.goluk.crazy.panda.common.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.goluk.crazy.panda.R;

/**
 * Created by leege100 on 2016/8/19.
 */
public class HeaderBar extends RelativeLayout{

    public TextView leftTv;
    public TextView rightTv;
    public TextView centerTv;
    public ImageView centerIv;

    String leftText;
    String centerText;
    String rightText;

    public LinearLayout centerLayout;

    Drawable centerDrawable;

    public HeaderBar(Context context) {
        super(context);
    }

    public HeaderBar(Context context, AttributeSet attrs) {
        super(context, attrs);

        View view = View.inflate(context, R.layout.header_bar, this);
        leftTv = (TextView) view.findViewById(R.id.tv_headerbar_left);
        rightTv = (TextView) view.findViewById(R.id.tv_headerbar_right);
        centerTv = (TextView) view.findViewById(R.id.tv_headerbar_center);
        centerIv = (ImageView) view.findViewById(R.id.iv_center);
        centerLayout = (LinearLayout) view.findViewById(R.id.ll_headerbar_center);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.headerbar);
        leftText = a.getString(R.styleable.headerbar_leftText);
        rightText = a.getString(R.styleable.headerbar_rightText);
        centerText = a.getString(R.styleable.headerbar_centerText);
        centerDrawable = a.getDrawable(R.styleable.headerbar_centerResDrawable);
        a.recycle();

        leftTv.setText(leftText, TextView.BufferType.NORMAL);
        rightTv.setText(rightText, TextView.BufferType.NORMAL);
        centerTv.setText(centerText, TextView.BufferType.NORMAL);
        if (centerDrawable != null) {
            centerIv.setVisibility(View.VISIBLE);
            centerIv.setImageDrawable(centerDrawable);
        } else {
            centerIv.setVisibility(View.GONE);
        }
    }

    public void setOnLeftClickListener(View.OnClickListener clickListener) {
        leftTv.setOnClickListener(clickListener);
    }

    public void setOnRightClickListener(View.OnClickListener clickListener) {
        rightTv.setOnClickListener(clickListener);
    }

    public void setOnCenterClickListener(View.OnClickListener clickListener) {
        centerLayout.setOnClickListener(clickListener);
    }

    public String getLeftText() {
        return leftText;
    }

    public void setLeftText(String leftText) {
        this.leftText = leftText;
        leftTv.setText(leftText);
    }

    public String getCenterText() {
        return centerText;
    }

    public void setCenterText(String centerText) {
        this.centerText = centerText;
        centerTv.setText(centerText);
    }

    public String getRightText() {
        return rightText;
    }

    public void setRightText(String rightText) {
        this.rightText = rightText;
        rightTv.setText(rightText);
    }

    public Drawable getCenterDrawable() {
        return centerDrawable;
    }

    public void setCenterDrawable(Drawable centerDrawable) {
        this.centerDrawable = centerDrawable;
    }
//    public HeaderBar(Context context, AttributeSet attrs, int defStyleAttr) {
//        super(context, attrs, defStyleAttr);
//    }

//    public HeaderBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
//        super(context, attrs, defStyleAttr, defStyleRes);
//    }
}
