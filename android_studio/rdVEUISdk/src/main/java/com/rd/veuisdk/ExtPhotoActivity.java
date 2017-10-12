package com.rd.veuisdk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.rd.lib.ui.ExtButton;
import com.rd.lib.ui.PreviewFrameLayout;
import com.rd.lib.utils.CoreUtils;
import com.rd.lib.utils.InputUtls;
import com.rd.vecore.exception.InvalidArgumentException;
import com.rd.vecore.models.MediaObject;
import com.rd.veuisdk.TTFHandler.ITTFHandlerListener;
import com.rd.veuisdk.adapter.TTFAdapter;
import com.rd.veuisdk.model.ExtPicInfo;
import com.rd.veuisdk.ui.ColorPicker.IColorListener;
import com.rd.veuisdk.ui.ExtColorPicker;
import com.rd.veuisdk.ui.ExtEditPic;
import com.rd.veuisdk.utils.AppConfiguration;
import com.rd.veuisdk.utils.IntentConstants;
import com.rd.veuisdk.utils.PathUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class ExtPhotoActivity extends BaseActivity {
    ExtEditPic mEtInput;
    RadioGroup mRgMainType;
    RadioGroup mRgTextSide;
    ExtColorPicker mColorPicker;
    ExtColorPicker mBgPicker;
    GridView mGvTTF;

    private ExtPicInfo mExtPicInfo;
    private TTFHandler mTTFHandler;
    private IntentFilter mIntentFilter;
    private DisplayMetrics mDisplayMetrics;
    private int DEFAULT_BG_COLOR = 23;

    private final int MID_SIDE = 0;
    private final int LEFT_SIDE = 1;
    private final int RIGHT_SIDE = 2;

    private int mTTFPosition;
    private int mBgPosition;
    private int mTextColorPosition;


    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        mStrActivityPageName = getString(R.string.blackboard_text);
        mFrameRect = new Rect();
        setContentView(R.layout.activity_edit_pic);
        mExtPicInfo = getIntent().getParcelableExtra(IntentConstants.EXTRA_EXT_PIC_INFO);
        initView();

        PreviewFrameLayout priLevel1 = (PreviewFrameLayout) findViewById(R.id.rlPreviewLevel1);
        PreviewFrameLayout pri = (PreviewFrameLayout) findViewById(R.id.rlPreview);

        pri.setAspectRatio(EditPreviewActivity.mCurAspect);
        if (EditPreviewActivity.mCurAspect > 1) {
            priLevel1.setAspectRatio(EditPreviewActivity.mCurAspect);
        } else {
            priLevel1.setAspectRatio(AppConfiguration.ASPECTRATIO);
        }


        mEtInput.postDelayed(new Runnable() {

            @Override
            public void run() {
                if (null != mExtPicInfo) {
                    mEtInput.setText(mExtPicInfo.getText());
                    mEtInput.setBgColor(mExtPicInfo.getBgColor());
                    setTTf(mExtPicInfo.getTtf());
                    mEtInput.setTextColor(mExtPicInfo.getTxColor());
                    mEtInput.setTextSide(mExtPicInfo.getTxSide());
                    checkTextSide(mExtPicInfo.getTxSide());
                    mTTFPosition = mExtPicInfo.getTtfposition();
                    mBgPosition = mExtPicInfo.getBgPosition();
                    mTextColorPosition = mExtPicInfo.getTxColorPosition();
                    mTTFHandler.setChecked(mTTFPosition);
                    mColorPicker.setCheckId(mTextColorPosition);
                    mBgPicker.setCheckId(mBgPosition);
                    mEtInput.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (!TextUtils.isEmpty(mExtPicInfo.getText())) {
                                mEtInput.setSelection(mExtPicInfo.getText().length());
                            }
                        }
                    }, 100);
                }
            }
        }, 100);

        mDisplayMetrics = CoreUtils.getMetrics();
        mIntentFilter = new IntentFilter(TTFAdapter.ACTION_TTF);
        registerReceiver(mReceiver, mIntentFilter);

        onInputManager();
    }

    private void checkTextSide(int textSide) {
        if (textSide == LEFT_SIDE) {
            mRgTextSide.check(R.id.rbLeftSide);
        } else if (textSide == RIGHT_SIDE) {
            mRgTextSide.check(R.id.rbRightSide);
        } else if (textSide == MID_SIDE) {
            mRgTextSide.check(R.id.rbMidSide);
        }
    }

    private void setTTf(String mGvTTF) {
        try {
            mEtInput.setTTF(mGvTTF);
            if (!TextUtils.isEmpty(mGvTTF)) {
                mEtInput.setTypeface(Typeface.createFromFile(mGvTTF));
            }
        } catch (Exception e) {
            mEtInput.setTypeface(null);
            mEtInput.setTTF("");
        }
    }

    private Rect mFrameRect = null;

    /**
     * 点击输入法开关
     */
    private void onInputManager() {
        mEtInput.postDelayed(new Runnable() {
            @Override
            public void run() {
                View t = findViewById(R.id.theFrame);
                t.getLocalVisibleRect(mFrameRect);
                int[] lo = new int[2];
                t.getLocationInWindow(lo);
                FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(mDisplayMetrics.widthPixels, mFrameRect.height());


                mColorPicker.setLayoutParams(lp);
                mBgPicker.setLayoutParams(lp);

                mEtInput.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        InputMethodManager inputm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputm.toggleSoftInput(0,
                                InputMethodManager.HIDE_NOT_ALWAYS);
                        mEtInput.requestFocus();
                        mEtInput.setFocusable(true);
                        mEtInput.setFocusableInTouchMode(true);
                    }
                }, 200);
            }
        }, 200);

    }

    public void clickView(View v) {
        int id = v.getId();
        if (id == R.id.public_menu_sure) {
            onSure();
        } else if (id == R.id.public_menu_cancel) {
            onBackPressed();
        }
    }

    private void hideInput() {
        InputUtls.hideKeyboard(mEtInput);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String path = intent.getStringExtra(TTFAdapter.TTF_ITEM);
            mTTFPosition = intent.getIntExtra(TTFAdapter.TTF_ITEM_POSITION, 0);
            setTTf(path);
        }

    };

    private OnCheckedChangeListener mOnMainTypeCheckedListener = new OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            hideInput();
            if (checkedId == R.id.rbTTF) {
                mGvTTF.setVisibility(View.VISIBLE);
                mColorPicker.setVisibility(View.GONE);
                mBgPicker.setVisibility(View.GONE);
            } else if (checkedId == R.id.rbTextColor) {
                mColorPicker.setVisibility(View.VISIBLE);
                mBgPicker.setVisibility(View.GONE);
                mGvTTF.setVisibility(View.GONE);
            } else if (checkedId == R.id.rbBackgroundColor) {
                mBgPicker.setVisibility(View.VISIBLE);
                mColorPicker.setVisibility(View.GONE);
                mGvTTF.setVisibility(View.GONE);
            }

        }
    };

    private OnCheckedChangeListener mOnTextSideListener = new OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            hideInput();
            if (checkedId == R.id.rbLeftSide) {
                mEtInput.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
                mEtInput.setTextSide(LEFT_SIDE);
            } else if (checkedId == R.id.rbMidSide) {
                mEtInput.setGravity(Gravity.CENTER);
                mEtInput.setTextSide(MID_SIDE);
            } else if (checkedId == R.id.rbRightSide) {
                mEtInput.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
                mEtInput.setTextSide(RIGHT_SIDE);
            }
        }
    };

    private IColorListener mTextColorListener = new IColorListener() {

        @Override
        public void getColor(int color, int position) {
            mEtInput.setTextColor(color);
            mTextColorPosition = position;
        }

    };

    private IColorListener mBgPickListener = new IColorListener() {

        @Override
        public void getColor(int color, int position) {
            mEtInput.setBgColor(color);
            mBgPosition = position;
        }
    };

    private OnClickListener mOnLeftListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            onBackPressed();
        }
    };

    private void initView() {
        mEtInput = (ExtEditPic) findViewById(R.id.etEditPic);
        mRgMainType = (RadioGroup) findViewById(R.id.rgMainType);
        mRgTextSide = (RadioGroup) findViewById(R.id.rgTextSide);
        mColorPicker = (ExtColorPicker) findViewById(R.id.txColorPicker);
        mBgPicker = (ExtColorPicker) findViewById(R.id.bgColorPicker);
        mGvTTF = (GridView) findViewById(R.id.gvTTF);

        mBgPicker.setColorListener(mBgPickListener);
        mBgPicker.setCheckId(DEFAULT_BG_COLOR);

        mColorPicker.setColorListener(mTextColorListener);

        ExtButton left = (ExtButton) findViewById(R.id.btnLeft);
        left.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.public_menu_cancel, 0, 0, 0);
        left.setOnClickListener(mOnLeftListener);

        ExtButton right = (ExtButton) findViewById(R.id.btnRight);
        right.setText("");
        right.setCompoundDrawablesWithIntrinsicBounds(0, 0,
                R.drawable.public_menu_sure, 0);
        right.setVisibility(View.VISIBLE);
        right.setOnClickListener(mOnRightListener);

        TextView title = (TextView) findViewById(R.id.tvTitle);
        title.setText(mStrActivityPageName);

        mRgMainType.setOnCheckedChangeListener(mOnMainTypeCheckedListener);
        mRgTextSide.setOnCheckedChangeListener(mOnTextSideListener);
        mEtInput.addTextChangedListener(mTextWatcher);

        mEtInput.setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER
                        && mEtInput.getLineCount() >= 6) {
                    return true;

                }
                return false;
            }

        });
        mTTFHandler = new TTFHandler(mGvTTF, mTTFListener);

    }

    private ITTFHandlerListener mTTFListener = new ITTFHandlerListener() {

        @Override
        public void onItemClick(String mGvTTF, int position) {
            mTTFPosition = position;
            if (mGvTTF.equals(getString(R.string.default_ttf))) {
                mEtInput.setTTF("");
                mEtInput.setTypeface(Typeface.create(Typeface.DEFAULT,
                        Typeface.NORMAL));
            } else {
                mEtInput.setTTF(mGvTTF);
                mEtInput.setTypeface(Typeface.createFromFile(mGvTTF));
            }
        }
    };

    @Override
    protected void onDestroy() {
        unregisterReceiver(mReceiver);
        mTTFHandler.onDestory();
        super.onDestroy();
    }

    private TextWatcher mTextWatcher = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            String text = mEtInput.getText().toString().trim();

            String[] arr = text.split("\n");

            String target = "", temp = "";

            ArrayList<String> tlist = new ArrayList<String>();

            for (int i = 0; i < arr.length; i++) {
                temp = arr[i];
                if (i < arr.length - 1) {
                    tlist.add(temp);
                }
                if (target.length() < temp.length()) {
                    target = temp;
                }
            }

            mEtInput.add(tlist);

            // 计算字体大小

            int txsize = draw(target, new Paint(), mEtInput.getWidth(),
                    mEtInput.getHeight() / 6);

            mEtInput.setTextSize(TypedValue.COMPLEX_UNIT_PX, txsize);

        }
    };

    private int draw(String text, Paint p, int width, int height) {
        int textSize = 100;
        FontMetrics fm;
        p.setAntiAlias(true);
        p.setTextSize(textSize);

        while (true) {
            p.setTextSize(textSize);
            if (p.measureText(text) > width) {
                textSize -= 2;
            } else {
                fm = p.getFontMetrics();
                if ((Math.abs(fm.leading) + Math.abs(fm.ascent) + Math
                        .abs(fm.descent)) > height) {
                    textSize -= 2;
                } else {
                    break;
                }
            }
        }

        return textSize;
    }

    private OnClickListener mOnRightListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            onSure();
        }
    };

    private void onSure() {
        mEtInput.setCursorVisible(false);
        String path = PathUtils.getTempFileNameForSdcard("Temp_bmp_", "png");

        int[] wh = save(path);


        MediaObject media = null;
        try {
            media = new MediaObject(path);
            RectF rectF = new RectF(0, 0, wh[0], wh[1]);
            media.setClipRectF(rectF);
            media.setShowRectF(rectF);
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        }

        Intent intent = new Intent();

        if (null != mExtPicInfo) {
            mExtPicInfo.setText(mEtInput.getText().toString());
            mExtPicInfo.setBgColor(mEtInput.getBgColor());
            mExtPicInfo.setTxColor(mEtInput.getTextColor());
            mExtPicInfo.setTtf(mEtInput.getTTF());
            mExtPicInfo.setTxSide(mEtInput.getTextSide());
            mExtPicInfo.setTtfposition(mTTFPosition);
            mExtPicInfo.setBgPosition(mBgPosition);
            mExtPicInfo.setTxColorPosition(mTextColorPosition);
        } else {
            mExtPicInfo = new ExtPicInfo(mEtInput.getBgColor(), mEtInput.getTextColor(),
                    mEtInput.getText().toString(), mEtInput.getTTF(),
                    mEtInput.getTextSide(), mTTFPosition, mBgPosition,
                    mTextColorPosition);
        }
        ArrayList<MediaObject> allMedia = new ArrayList<MediaObject>();
        allMedia.add(media);
        intent.putParcelableArrayListExtra(IntentConstants.EXTRA_MEDIA_LIST,
                allMedia);
        intent.putExtra(IntentConstants.EXTRA_MEDIA_OBJECTS, media);
        intent.putExtra(IntentConstants.EXTRA_EXT_ISEXTPIC, 1);
        intent.putExtra(IntentConstants.EXTRA_EXT_PIC_INFO, mExtPicInfo);
        setResult(RESULT_OK, intent);
        finish();
    }

    private int[] save(String path) {
        int[] wh = new int[2];
        mEtInput.setDrawingCacheEnabled(true);
        Bitmap mBitmap = mEtInput.getDrawingCache();
        File file = new File(path);
        if (file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        wh[0] = mBitmap.getWidth();
        wh[1] = mBitmap.getHeight();

        try {
            FileOutputStream out = new FileOutputStream(file);
            mBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mEtInput.setDrawingCacheEnabled(false);
        mBitmap.recycle();
        mBitmap = null;
        return wh;

    }
}
