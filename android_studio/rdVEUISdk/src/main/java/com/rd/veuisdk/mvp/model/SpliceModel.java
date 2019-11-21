package com.rd.veuisdk.mvp.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.MediaMetadataRetriever;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.rd.lib.utils.BitmapUtils;
import com.rd.lib.utils.CoreUtils;
import com.rd.vecore.VirtualVideo;
import com.rd.vecore.models.MediaObject;
import com.rd.vecore.models.MediaType;
import com.rd.vecore.utils.MiscUtils;
import com.rd.veuisdk.model.GridInfo;
import com.rd.veuisdk.model.SpliceGridMediaInfo;
import com.rd.veuisdk.model.SpliceModeInfo;
import com.rd.veuisdk.utils.PathUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static com.rd.vecore.models.AspectRatioFitMode.KEEP_ASPECTRATIO_EXPANDING;

/**
 * 拼图-数据
 */
public class SpliceModel {
    private String TAG = "SpliceModel";

    /**
     * 获取指定类型的模板
     *
     * @param childCount 单个模板的成员数
     */
    public List<SpliceModeInfo> getSpliceList(Context context, int childCount) {
        List<SpliceModeInfo> list = new ArrayList<>();
        int len = 7;
        if (childCount == 7) {
            len = 6;
        } else if (childCount == 9) {
            len = 5;
        }
        String dir = "mix/mix_" + childCount + "/";
        for (int i = 1; i < len; i++) {
            String content = CoreUtils.read(context.getAssets(), dir + childCount + "-" + i + ".json");
            String base = dir + childCount + "-" + i;
            list.add(new SpliceModeInfo(base + ".png", base + "-1.png", initGrid(content, dir)));
        }
        return list;
    }

    /**
     * 单个小画框-小区域
     */
    private List<GridInfo> initGrid(String content, String dir) {
        List<GridInfo> gridInfos = new ArrayList<>();
        JSONArray jsonArray = null;
        try {
            jsonArray = new JSONArray(content);
            int len = jsonArray.length();
            for (int i = 0; i < len; i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                RectF rectF = new RectF((float) jsonObject.getDouble("l"), (float) jsonObject.getDouble("t"), (float) jsonObject.getDouble("r"), (float) jsonObject.getDouble("b"));
                String mask = jsonObject.optString("mask");
                if (!TextUtils.isEmpty(mask)) {
                    //播放器黑白图
                    mask = dir + mask;
                }
                String transPath = jsonObject.optString("maskShadow");
                if (!TextUtils.isEmpty(transPath)) {
                    //UI透明遮罩
                    transPath = dir + transPath;
                }
                GridInfo tmp = new GridInfo(rectF, mask, transPath);
                {
                    //异形顶点
                    JSONArray jarr = jsonObject.optJSONArray("pointF");
                    if (null != jarr) {
                        List<PointF> list = new ArrayList<>();
                        for (int n = 0; n < jarr.length(); n++) {
                            JSONObject jTmp = jarr.getJSONObject(n);
                            list.add(new PointF((float) jTmp.getDouble("x"), (float) jTmp.getDouble("y")));
                        }
                        tmp.setPointFList(list);
                    }
                }
                gridInfos.add(tmp);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return gridInfos;
    }


    /**
     * 根据当前画框的默认位置和边框宽度 ，计算出 目标显示位置   ( 部分有交集的区域，只保留一个border的宽度 ，防止出现2* borderWidth)
     *
     * @param pW          父容器 宽
     * @param pH          父容器 高
     * @param rectF       默认显示位置  相对于父容器中 0~1.0f
     * @param borderWidth 真实的边框像素   X轴-左边距
     * @param isAlien     是否是异形画框 （异形画框已经有交叉,不需要再/2.0f）
     * @return 在父容器中的相对显示位置 0~1.0f
     */
    public RectF getScaledRectF(int pW, int pH, RectF rectF, float borderWidth, boolean isAlien) {

        if (borderWidth == 0) { //没有缩放
            return new RectF(rectF);
        }
        float itemWidth = rectF.width() * pW;
        float tmpScaleX = 1 - (borderWidth * 2 / itemWidth);

        //X轴的偏移像素
        float py = borderWidth;

        float itemHeight = rectF.height() * pH;
        float scaleY = 1 - (py * 2 / itemHeight);
        RectF tmp = MiscUtils.zoomRectF(rectF, tmpScaleX, scaleY);

        if (!isAlien) {
            //标准矩形画框
            //有交叉部分的边框宽度取一半
            if (rectF.left > 0) {
                tmp.left = (rectF.left + tmp.left) / 2;
            }
            if (rectF.top > 0) {
                tmp.top = (rectF.top + tmp.top) / 2;
            }
            if (rectF.right < 1) {
                tmp.right = (rectF.right + tmp.right) / 2;
            }
            if (rectF.bottom < 1) {
                tmp.bottom = (rectF.bottom + tmp.bottom) / 2;
            }
        }
//        Log.e(TAG, "getScaledRectF: " + tmpScaleX + " " + scaleY + "  tmp:" + tmp + ">>" + rectF + " dY0:" + (rectF.top - tmp.top) + " dy1:" + (rectF.bottom - tmp.bottom));

        return tmp;
    }


    /**
     * 获取指定时间点的缩略图
     *
     * @param nTime 单位：秒
     */
    private Bitmap getThumbVideo(MediaObject tmp, int bmpW, int bmpH, float nTime) {
        MediaMetadataRetriever media = new MediaMetadataRetriever();
        media.setDataSource(tmp.getMediaPath());
        Bitmap bmp = media.getFrameAtTime((long) (nTime * 1000000));
        if (null == bmp) { //系统方式获取失败
            //sdk核心取图
            bmp = Bitmap.createBitmap(bmpW, bmpH, Bitmap.Config.ARGB_8888);
            VirtualVideo.getSnapShot(tmp.getMediaPath(), nTime, bmp);
        } else {
            Bitmap tmpBmp = BitmapUtils.getZoomBitmap(bmp, bmpW, bmpH);
            if (tmpBmp != bmp) {
                bmp.recycle();
                bmp = tmpBmp;
            }
        }
        return bmp;

    }

    /**
     * 构建所需资源  (生成缩略图，更新资源)
     */
    public void initItemMedia(@Nullable Context context, @NonNull MediaObject tmp, @NonNull SpliceGridMediaInfo info) {
        int vW = tmp.getWidth();
        int vH = tmp.getHeight();
        int bmpW = Math.max(240, Math.min(720, vW));
        int bmpH = (int) (bmpW / (vW / (vH + 0.0f)));
        Bitmap bmp = null;
        if (tmp.getMediaType() == MediaType.MEDIA_VIDEO_TYPE) {
            bmp = getThumbVideo(tmp, bmpW, bmpH, tmp.getDuration() - 0.5f);
            if (null != bmp) { //系统方式获取失败
                bmpW = bmp.getWidth();
                bmpH = bmp.getHeight();
            }
        } else {
            //图片
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(tmp.getMediaPath(), options);
            int inSampleSize = MiscUtils.getInSampleSize(options.outWidth, options.outHeight, bmpW, bmpH);
            options = new BitmapFactory.Options();
            options.inJustDecodeBounds = false;
            options.inSampleSize = inSampleSize;
            bmp = BitmapFactory.decodeFile(tmp.getMediaPath(), options);
            if (null != bmp) {
                bmpW = bmp.getWidth();
                bmpH = bmp.getHeight();
            }
        }
        //封面地址
        String path = PathUtils.getTempFileNameForSdcard(PathUtils.TEMP + "_" + tmp.hashCode(), "png");
        try {
            BitmapUtils.saveBitmapToFile(bmp, true, 100, path);
        } catch (Exception e) {
            e.printStackTrace();
            path = null;
        }
        tmp.setAspectRatioFitMode(KEEP_ASPECTRATIO_EXPANDING);
        info.updateMedia(tmp, bmp, path);
        info.setSize(new Rect(vW, vH, bmpW, bmpH));
    }


    /***
     *从旋转之后的文件中计算出要保留的区域
     *
     * @param srcW  原始文件的宽
     * @param srcH   原始文件的高
     * @param srcClipRectF    相对于旋转后的文件的裁剪区域 0~1.0f
     * @param showRectF  显示位置 0~1.0f
     * @param angle  旋转角度
     *
     * @return 相对于旋转后的文件的 裁剪区域 真实的像素   (旋转后的文件的左上角坐标 （0，0）)
     */
    public RectF fixClipRectF(int srcW, int srcH, RectF srcClipRectF, RectF showRectF, int angle) {
        Rect clipRect;
        if (angle == 90 || angle == 270) {  //竖屏变横屏 || 横屏变竖屏
            clipRect = new Rect((int) (srcClipRectF.left * srcH), (int) (srcClipRectF.top * srcW), (int) (srcClipRectF.right * srcH), (int) (srcClipRectF.bottom * srcW));
            //修正宽高为2的整数倍(防止出现拉丝现象)
            MiscUtils.fixClipAlignValue(clipRect, showRectF.width() / showRectF.height(), srcH, srcW);
        } else {
            //默认||上下颠倒
            clipRect = new Rect((int) (srcClipRectF.left * srcW), (int) (srcClipRectF.top * srcH), (int) (srcClipRectF.right * srcW), (int) (srcClipRectF.bottom * srcH));
            //防止裁剪区域越界
            MiscUtils.fixClipAlignValue(clipRect, showRectF.width() / showRectF.height(), srcW, srcH);
        }
        return new RectF(clipRect);
    }


    /***
     * 获取异形模板的UI遮罩图
     */
    public Bitmap getTransBmp(@NonNull Context context, String assetPath) {
        if (TextUtils.isEmpty(assetPath)) {
            return null;
        }
        InputStream inputStream = null;
        Bitmap bmp = null;
        try {
            inputStream = context.getAssets().open(assetPath);
            bmp = BitmapFactory.decodeStream(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != inputStream) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return bmp;
    }
}
