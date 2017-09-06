package com.rd.veuisdk.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.rd.veuisdk.R;

public class ProportionDialog extends AlertDialog implements
        android.view.View.OnClickListener {

    private LinearLayout mLlSquare, mLlLandscape, mLlPortrait, mLlAuto;
    private ImageView mIvSquare, mIvLandscape, mIvPortrait, mIvAuto;
    public static final int ORIENTATION_SQUARE = 1;
    public static final int ORIENTATION_LANDSCAPE = 2;
    public static final int ORIENTATION_PORTRAIT = 3;
    public static final int ORIENTATION_AUTO = 0;
    public int mIndex = -1;

    public ProportionDialog(Context context) {
        super(context);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.more_orientation);
        mLlSquare = (LinearLayout) findViewById(R.id.llOrientationSquare);
        mLlAuto = (LinearLayout) findViewById(R.id.llOrientationAuto);
        mLlLandscape = (LinearLayout) findViewById(R.id.llOrientationLandscape);
        mLlPortrait = (LinearLayout) findViewById(R.id.llOrientationPortrait);

        mIvSquare = (ImageView) findViewById(R.id.ivOrientationSquare);
        mIvLandscape = (ImageView) findViewById(R.id.ivOrientationLandscape);
        mIvPortrait = (ImageView) findViewById(R.id.ivOrientationPortrait);
        mIvAuto = (ImageView) findViewById(R.id.ivOrientationAuto);

        mLlSquare.setOnClickListener(this);
        mLlAuto.setOnClickListener(this);
        mLlPortrait.setOnClickListener(this);
        mLlLandscape.setOnClickListener(this);

        mLlAuto.setBackgroundResource(R.drawable.orientation_bg);
        mIvAuto.setImageResource(R.drawable.edit_more_orientation_auto_p);

        Window win = getWindow();
        WindowManager.LayoutParams lp = win.getAttributes();

        win.setBackgroundDrawable(new ColorDrawable());
        win.setGravity(Gravity.TOP);
        lp.x = 0;
        lp.y = 200;

        win.setAttributes(lp);

    }

    @SuppressLint("NewApi")
    @Override
    public void onClick(View v) {
        resetStatus();
        int id = v.getId();
        if (id == R.id.llOrientationSquare) {
            mIndex = ORIENTATION_SQUARE;
        } else if (id == R.id.llOrientationLandscape) {
            mIndex = ORIENTATION_LANDSCAPE;
        } else if (id == R.id.llOrientationPortrait) {
            mIndex = ORIENTATION_PORTRAIT;
        } else if (id == R.id.llOrientationAuto) {
            mIndex = ORIENTATION_AUTO;
        } else {

        }
        dismiss();

    }

    public void resetStatus() {
        mLlSquare.setBackground(null);
        mLlAuto.setBackground(null);
        mLlPortrait.setBackground(null);
        mLlLandscape.setBackground(null);
        mIvSquare.setImageResource(R.drawable.edit_more_orientation_1_1);
        mIvAuto.setImageResource(R.drawable.edit_more_orientation_auto);
        mIvLandscape
                .setImageResource(R.drawable.edit_more_orientation_landscape);
        mIvPortrait.setImageResource(R.drawable.edit_more_orientation_portrait);

        if (mIndex == ORIENTATION_SQUARE) {
            mLlSquare.setBackgroundResource(R.drawable.orientation_bg);
            mIvSquare.setImageResource(R.drawable.edit_more_orientation_1_1_p);
        } else if (mIndex == ORIENTATION_AUTO) {
            mLlAuto.setBackgroundResource(R.drawable.orientation_bg);
            mIvAuto.setImageResource(R.drawable.edit_more_orientation_auto_p);
        } else if (mIndex == ORIENTATION_LANDSCAPE) {
            mLlLandscape.setBackgroundResource(R.drawable.orientation_bg);
            mIvLandscape
                    .setImageResource(R.drawable.edit_more_orientation_landscape_p);
        } else if (mIndex == ORIENTATION_PORTRAIT) {
            mLlPortrait.setBackgroundResource(R.drawable.orientation_bg);
            mIvPortrait
                    .setImageResource(R.drawable.edit_more_orientation_portrait_p);
        } else {

        }
        mIndex = -1;
    }

}
