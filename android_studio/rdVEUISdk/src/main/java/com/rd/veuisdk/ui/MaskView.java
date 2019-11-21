package com.rd.veuisdk.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.rd.lib.utils.BitmapUtils;

import java.io.IOException;
import java.io.InputStream;

/***
 * bitmap 遮罩 （不显示透明部分的）
 */
public class MaskView extends View {
    private Bitmap result;
    private Bitmap original = null, mask = null;

    public void setResource(Bitmap original, Bitmap mask) {
        this.original = original;
        this.mask = mask;
    }

    public MaskView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        //获取图片的bitmap
//        try {
//            InputStream inputStream = getContext().getAssets().open("bg1.png");
//            original = BitmapFactory.decodeStream(inputStream);
//            inputStream.close();
//            //获取遮罩的bitmap
////            inputStream = getContext().getAssets().open("a.png");
//            inputStream = getContext().getAssets().open("mask4.png");
////            inputStream = getContext().getAssets().open("3-1.png");
////            inputStream = getContext().getAssets().open("4_0_2.png");
////            mask = BitmapFactory.decodeStream(inputStream);
//            mask = BitmapUtils.rorateBmp(BitmapFactory.decodeStream(inputStream), 180);
//            inputStream.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }


//        //根据透明度创建一个新的bitmap，需要注意的是这里的第三个参数的值Bitmap.Config.ARGB_8888表示支持32位图片，也就是支持透明通道。
//        result = Bitmap.createBitmap(original.getWidth() * 2, original.getHeight() * 2, Bitmap.Config.ARGB_8888);
//        //将遮罩层的图片放到画布中
//        Canvas mCanvas = new Canvas(result);
//        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
//        //设置图层混合模式
//        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT));
//        //一次绘制图层
////        mCanvas.drawBitmap(original, 0, 0, null);
////        mCanvas.drawBitmap(mask, 50,50, paint);
//
//        mCanvas.drawBitmap(mask, original.getWidth() / 2, original.getHeight() / 2, paint);
//        paint.setXfermode(null);

//        //计算mask的绘制比例
//        Matrix mMatrix = new Matrix();
//        //这里有个小坑，别忘了getWidth和getHeight的值转为float，不然算出来的也是整数。
//        mMatrix.setScale((float)original.getWidth() / (float)mask.getWidth(), (float)original.getHeight() / (float)mask.getHeight());
//        mCanvas.drawBitmap(mask, mMatrix, paint);
//        paint.setXfermode(null);


//        original.recycle();
//        mask.recycle();

    }

    @Override
    protected void onDraw(Canvas canvas) {
//        canvas.drawColor(Color.RED);

        if (null != mask && null != original) {


            //一次绘制图层
//        canvas.drawBitmap(original, 0, 0, null);

            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

            int layerID = canvas.saveLayer(0, 0, getWidth(), getHeight(), paint, Canvas.ALL_SAVE_FLAG);


            //一次绘制图层
            canvas.drawBitmap(original, 0, 0, paint);

            //设置图层混合模式
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
//        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT));


            canvas.drawBitmap(mask, new Rect(0, 0, mask.getWidth(), mask.getHeight()), new Rect(50, 50, original.getWidth() + 50, original.getHeight() - 50), paint);

            canvas.restoreToCount(layerID);
        }
    }
}
