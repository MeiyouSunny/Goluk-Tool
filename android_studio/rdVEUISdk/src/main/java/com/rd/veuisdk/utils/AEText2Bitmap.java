package com.rd.veuisdk.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.TextUtils;

import com.rd.lib.utils.PaintUtils;
import com.rd.vecore.graphics.BitmapEx;
import com.rd.veuisdk.ae.model.AETextLayerInfo;

import java.util.ArrayList;

/**
 * AEText转bitmap
 */
public class AEText2Bitmap {
    String TAG = "AEText2Bitmap";

    /**
     * 对于刚从json解析的字体（没有换行符)，主动处理换行
     *
     * @param paint
     * @param str
     * @param textMaxWidth
     * @param textMaxHeight
     * @return
     */
    private ArrayList<String> getText(Paint paint, String str, int textMaxWidth, int textMaxHeight) {
        String text = str.replace(" ", "").trim();
        ArrayList<String> list = new ArrayList<>();
        int len = text.length();
        if (len > 0) {
            int index = 0;
            int textIndexMax = len - 1;
            while (index < textIndexMax) {
                for (int i = index; i < len; i++) {
                    String tmp = str.substring(index, i);
                    int tmpWidth = PaintUtils.getWidth(paint, tmp);
                    if (tmpWidth > textMaxWidth) {
                        list.add(str.substring(index, i - 1));
                        index = i - 1;
                        break;
                    } else if (i == textIndexMax) {
                        //最后一个字已经扫描完毕  (跳出while 循环)
                        list.add(str.substring(index, len));
                        index = textIndexMax;
                        break;
                    }
                }
            }
        }
        return list;

    }


    /**
     * 文字转bitmapEx
     */
    public Bitmap fixAEText(AETextLayerInfo layerInfo, String text, String ttf) {
        int width = layerInfo.getWidth();
        int height = layerInfo.getHeight();
        boolean isVer = layerInfo.isVertical();
        ArrayList<String> textList = new ArrayList<>();
        if (isVer) {
            //纵向1列显示
            int len = text.length();
            for (int i = 0; i < len; i++) {
                textList.add(text.substring(i, i + 1));
            }
        } else {
            if (text.contains("\n") || layerInfo.getFontSize() == 0) {
                //横向多行显示
                String[] arr = text.split("\n");
                int len = arr.length;
                for (int i = 0; i < len; i++) {
                    if (!TextUtils.isEmpty(arr[i])) {
                        textList.add(arr[i]);
                    }
                }
            } else {
                //根据文字多少和字体大小，自动计算换行
                int textWidthMax = width - layerInfo.getPadding().left - layerInfo.getPadding().right;
                int textHeightMax = height - layerInfo.getPadding().top - layerInfo.getPadding().bottom;
                Paint paint = getPaint(layerInfo);
                paint.setTextSize(layerInfo.getFontSize());
                textList.addAll(getText(paint, text, textWidthMax, textHeightMax));

            }


        }
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas cv = new Canvas(bitmap);
//        cv.drawColor(Color.WHITE);
        cv.drawColor(Color.TRANSPARENT);

        if (textList.size() > 0) {
            //绘制文字
            drawText(cv, layerInfo, textList, ttf);
        }
        return bitmap;
    }

    /**
     *
     */
    public BitmapEx fixAETextEx(AETextLayerInfo layerInfo, String text) {
        Bitmap src = fixAEText(layerInfo, text, null);
        BitmapEx dst = BitmapEx.createBitmap(src);
        src.recycle();
        return dst;
    }

    /**
     * 字体
     */
    private void initFont(Paint paint, String ttf, AETextLayerInfo layerInfo) {
        String tmpTTF = ttf;
        if (TextUtils.isEmpty(ttf) || !ttf.startsWith("/")) {
            tmpTTF = layerInfo.getTtfPath();
        }
        if (!TextUtils.isEmpty(tmpTTF) && tmpTTF.startsWith("/") && FileUtils.isExist(tmpTTF)) {
            try {
                Typeface typeface = Typeface.createFromFile(tmpTTF);
                paint.setTypeface(typeface);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 文字画笔
     */
    private Paint initTextPaint(AETextLayerInfo layerInfo, ArrayList<String> textList, String ttf) {
        Paint paint = getPaint(layerInfo);
        initFont(paint, ttf, layerInfo);
        paint.setColor(layerInfo.getTextColor());
        if (layerInfo.getFontSize() > 0) {
            //fontSize有效
            paint.setTextSize(layerInfo.getFontSize());
        } else {
            //单行最长的文字字符串
            String textTarget = getTarget(textList);
            fixTextSize(textTarget, new Rect(0, 0, layerInfo.getWidth(), layerInfo.getHeight()), layerInfo.getPadding(), paint, 1);
        }
        return paint;

    }

    /**
     * 绘制文字
     *
     * @param cv        目标源
     * @param layerInfo 样式
     * @param textList  文字集合
     */
    private void drawText(Canvas cv, AETextLayerInfo layerInfo, ArrayList<String> textList, String ttf) {
        //边框画笔
        Paint strokePaint = initStrokePaint(layerInfo, ttf);

        //文字画笔
        Paint paint = initTextPaint(layerInfo, textList, ttf);

        if (null != strokePaint) {
            strokePaint.setTextSize(paint.getTextSize());
        }

        Rect textPadding = layerInfo.getPadding();
        int width = layerInfo.getWidth();
        int height = layerInfo.getHeight();

        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        int[] tHeight = PaintUtils.getHeight(paint);
        int len = textList.size();
        int cHeight = (height - textPadding.top - textPadding.bottom);
        int itemHeight = cHeight / len;
        paint.setAlpha((int) (Math.max(0, Math.min(1, layerInfo.getAlpha())) * 255));


        for (int i = 0; i < len; i++) {
            Rect bounds = new Rect();
            String text = textList.get(i);
            paint.getTextBounds(text, 0, text.length(), bounds);
            int textWidth = PaintUtils.getWidth(paint, text);

            float tY = textPadding.top + itemHeight * i + (itemHeight / 2 + (tHeight[0] / 2 - fontMetrics.bottom));

            if (layerInfo.getAlignment().equals("left")) {
                //左对齐
                cv.drawText(text, textPadding.left, tY, paint);
                if (strokePaint != null) {
                    cv.drawText(text, textPadding.left, tY, strokePaint);
                }
            } else if (layerInfo.getAlignment().equals("right")) {
                //右对齐
                cv.drawText(text, (width - textPadding.right - textWidth), tY, paint);
                if (strokePaint != null) {
                    cv.drawText(text, (width - textPadding.right - textWidth), tY, strokePaint);
                }
            } else {
                //左右居中
                float centerX = textPadding.left + ((width - textPadding.left - textPadding.right - textWidth) / 2);

//                Log.e(TAG, i + " drawText: " + bounds + "   " + bounds.width() + "*" + bounds.height() + "  " + textWidth + "  " +
//                        "" + " width:" + width + "*" + height + " " + layerInfo.getAlignment() + " text:" + text + " textPadding：" + textPadding + "" +
//                        " center:" + centerX + "<>" + tY + " paint:" + paint.getTextSize());

                cv.drawText(text, centerX, tY, paint);
                if (strokePaint != null) {
                    cv.drawText(text, centerX, tY, strokePaint);
                }
            }
        }

    }

    /**
     * 初始化边框画笔
     */
    private Paint initStrokePaint(AETextLayerInfo layerInfo, String ttf) {
        Paint strokePaint = null;
        if (layerInfo.getStrokeWidth() > 0) {
            strokePaint = new Paint();
            strokePaint.setAntiAlias(true);
            strokePaint.setStyle(Paint.Style.STROKE);
            strokePaint.setColor(layerInfo.getStrokeColor());
            strokePaint.setAlpha((int) (Math.max(0, Math.min(1, layerInfo.getStroleAlpha())) * 255));
            initFont(strokePaint, ttf, layerInfo);
        }
        return strokePaint;
    }

    private String getTarget(ArrayList<String> textList) {
        String target = null;
        int textWidth = 0;
        int len = textList.size();
        for (int i = 0; i < len; i++) {
            int count = getWordCount(textList.get(i));
            if (count >= textWidth) {
                textWidth = count;
                target = textList.get(i);
            }
        }
        return target;
    }


    /**
     * 由于Java是基于Unicode编码的，因此，一个汉字的长度为1，而不是2。
     * 但有时需要以字节单位获得字符串的长度。例如，“123abc长城”按字节长度计算是10，而按Unicode计算长度是8。
     * 为了获得10，需要从头扫描根据字符的Ascii来获得具体的长度。如果是标准的字符，Ascii的范围是0至255，如果是汉字或其他全角字符，Ascii会大于255。
     * 因此，可以编写如下的方法来获得以字节为单位的字符串长度
     *
     * @param s
     * @return
     */
    private int getWordCount(String s) {
        int length = 0;
        for (int i = 0; i < s.length(); i++) {
            int ascii = Character.codePointAt(s, i);
            if (ascii >= 0 && ascii <= 255)
                length++;
            else
                length += 2;
        }
        return length;

    }

    private Paint getPaint(AETextLayerInfo layerInfo) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStrokeWidth(layerInfo.getStrokeWidth());
        paint.setShadowLayer(layerInfo.getShadowRadius(), layerInfo.getShadowDx(),
                layerInfo.getShadowDy(), layerInfo.getShadowColor());
        paint.setFakeBoldText(layerInfo.isBold());
        if (layerInfo.isItalic()) {
            //文字倾斜默认为0，官方推荐的 - 0.25f 是斜体
            paint.setTextSkewX(-0.25f);

        }
        return paint;
    }

    /**
     * 根据画布大小，间距、行数和文本内容，设置确定合适的文字大小
     */
    private void fixTextSize(String text, Rect rect, Rect textPadding, Paint paint, int line) {

        int maxWidth = rect.width() - (textPadding.left + textPadding.right);
        int maxHeight = rect.height() - (textPadding.bottom + textPadding.top);
        //
        float textSize = Math.min(Math.min(maxWidth, maxHeight), 200);
        paint.setTextSize(textSize);
        int width = 0, height = 0;
        Rect rect1 = new Rect();
        paint.getTextBounds(text, 0, text.length(), rect1);

        while ((width = PaintUtils.getWidth(paint, text)) >= maxWidth || (height = rect1.height()) >= (maxHeight / line)) {
            paint.getTextBounds(text, 0, text.length(), rect1);
            if (textSize < 3) {
                break;
            }
            textSize -= 3;
            paint.setTextSize(textSize);
        }

    }

}
