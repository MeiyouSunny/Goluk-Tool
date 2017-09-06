package com.rd.veuisdk.ui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.EditText;

import com.rd.veuisdk.R;

/***
 * 文字板， 输入字导出图片
 */
public class ExtEditPic extends EditText {
    private int mBgColor;
    private int mTextColor;
    private int mTextSide;
    private boolean mSetActionDone;
    private String mStrTTF = "";
    private ArrayList<String> mStrList = new ArrayList<String>();

    public ExtEditPic(Context context, AttributeSet attrs) {
        super(context, attrs);
        mBgColor = Color.BLACK;
        mTextColor = getResources().getColor(R.color.white);
    }


    public void add(ArrayList<String> mlist) {
        mStrList.clear();
        mStrList.addAll(mlist);
        invalidate();
    }


    public String getTTF() {
        return mStrTTF;
    }

    public void setTTF(String mStrTTF) {
        this.mStrTTF = mStrTTF;
    }

    public void setTextColor(int color) {
        super.setTextColor(color);
        mTextColor = color;
    }


    public void setBgColor(int bgColor) {
        mBgColor = bgColor;
        super.setBackgroundColor(mBgColor);
    }

    public int getBgColor() {
        return mBgColor;
    }


    public int getTextColor() {
        return mTextColor;
    }

    public void setTextSide(int side) {
        mTextSide = side;
    }

    public int getTextSide() {
        return mTextSide;
    }


    /***
     * 生成图片文件
     * @param path
     * @return
     */
    public int[] save(String path) {
        int[] wh = new int[2];

        setDrawingCacheEnabled(true);
        Bitmap bmp = getDrawingCache();
        File file = new File(path);
        if (file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        wh[0] = bmp.getWidth();
        wh[1] = bmp.getHeight();

        try {
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 80, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        setDrawingCacheEnabled(false);
        bmp.recycle();
        bmp = null;
        return wh;

    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        InputConnection connection = super.onCreateInputConnection(outAttrs);
        if (mSetActionDone) {
            int imeActions = outAttrs.imeOptions & EditorInfo.IME_MASK_ACTION;
            if ((imeActions & EditorInfo.IME_ACTION_DONE) != 0) {
                // clear the existing action
                outAttrs.imeOptions ^= imeActions;
                // set the DONE action
                outAttrs.imeOptions |= EditorInfo.IME_ACTION_DONE;
            }
            if ((outAttrs.imeOptions & EditorInfo.IME_FLAG_NO_ENTER_ACTION) != 0) {
                outAttrs.imeOptions &= ~EditorInfo.IME_FLAG_NO_ENTER_ACTION;
            }
        }
        return connection;
    }


}
