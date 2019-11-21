package com.rd.veuisdk;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;

import com.rd.lib.utils.ThreadPoolUtils;
import com.rd.vecore.RdVECoreHelper;

import java.io.IOException;
import java.io.InputStream;

/**
 * 图片变形demo
 */
public class ImageTransActivity extends BaseActivity {
    private Handler mHandler;
    //将要被转换处理的原图截取的矩形区
    private Rect mRect = new Rect();
    //背景图的四个点
    private Point mPtLeftTop = new Point(194, 36);
    private Point mPtLeftBottom = new Point(180, 440);
    private Point mPtRightTop = new Point(641, 205);
    private Point mPtRightBottom = new Point(648, 493);
    //背景图
    private Bitmap mBackBmp;
    //将要被转换处理的原图
    private Bitmap mSrcBmp;
    //通过接口内部重新赋值的RECT，用于合并新的bitmap
    private Rect mOutPutRect = new Rect();
    private ImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_trans);
        InputStream is = null;
        try {
            is = getAssets().open("back_bitmap.jpg");
            mBackBmp = BitmapFactory.decodeStream(is);
            is.reset();
            is = getAssets().open("plane1.jpg");
            mSrcBmp = BitmapFactory.decodeStream(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mImageView = (ImageView) findViewById(R.id.iv_show_image);
        mImageView.setImageBitmap(mBackBmp);
        findViewById(R.id.sample_text).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ThreadPoolUtils.executeEx(new Runnable() {
                    public void run() {
                        Bitmap bmp = mSrcBmp;
                        mRect.left = 0;
                        mRect.top = 0;
                        mRect.right = bmp.getWidth();
                        mRect.bottom = bmp.getHeight();

                        Bitmap stitchBmp = RdVECoreHelper.imageTransform(
                                bmp, mRect, mPtLeftTop, mPtLeftBottom, mPtRightTop, mPtRightBottom, mOutPutRect);
                        mHandler.obtainMessage(MSG_MERGE, stitchBmp).sendToTarget();
                    }
                });
            }
        });

        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case MSG_MERGE:
                        Bitmap dst = (Bitmap) msg.obj;
                        if (dst != null) {
                            Bitmap tmp = mergeBitmap(mBackBmp, dst, mOutPutRect);
                            mImageView.setImageBitmap(tmp);
                            if (null != dst) {
                                dst.recycle();
                            }
                        } else {
                            String errInfo = "libRdVECore.so maybe failed！";
                            onToast(errInfo);
                        }
                        break;
                    default:
                        break;
                }
            }
        };
    }

    private final int MSG_MERGE = 500;

    private Bitmap mergeBitmap(Bitmap backBitmap, Bitmap frontBitmap, Rect outPutRect) {
        if (backBitmap == null || backBitmap.isRecycled()
                || frontBitmap == null || frontBitmap.isRecycled()) {
            return null;
        }
        Bitmap bitmap = Bitmap.createBitmap(backBitmap.getWidth(), backBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Rect baseRect = new Rect(0, 0, backBitmap.getWidth(), backBitmap.getHeight());
        canvas.drawBitmap(backBitmap, null, baseRect, null);
        canvas.drawBitmap(frontBitmap, null, outPutRect, null);
        return bitmap;
    }
}
