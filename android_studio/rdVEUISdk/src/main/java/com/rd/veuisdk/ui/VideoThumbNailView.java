package com.rd.veuisdk.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;

import com.rd.vecore.VirtualVideo;
import com.rd.vecore.models.MediaObject;
import com.rd.vecore.models.Scene;
import com.rd.veuisdk.R;
import com.rd.veuisdk.model.PointHighLight;
import com.rd.veuisdk.model.SplitItem;
import com.rd.veuisdk.model.SplitThumbItemInfo;
import com.rd.veuisdk.model.ThumbNailInfo;
import com.rd.veuisdk.utils.ThumbNailUtils;
import com.rd.veuisdk.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 分割视频的组件
 *
 * @author JIAN
 */
public class VideoThumbNailView extends View {
    private final String TAG = "VideoThumbNailView";
    private final int SPLIT_WIDTH = 40;
    private final int THUMBITEM = 10;
    private final int BORDER_SIZE = 2;
    private boolean isDrawing = false;
    private int mHighLight;
    private Paint pLight = new Paint();
    private int[] lights;
    private ArrayList<PointHighLight> points = new ArrayList<PointHighLight>();
    private ArrayList<SplitItem> list = new ArrayList<SplitItem>();


    public VideoThumbNailView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mLinePaint.setColor(getResources().getColor(R.color.white));
        mLinePaint.setStyle(Style.STROKE);
        mLinePaint.setAntiAlias(true);

        pLight.setStyle(Style.FILL);
        pLight.setAntiAlias(true);
        pLight.setColor(getResources().getColor(R.color.trim_point_color));
        mHighLight = getResources().getDimensionPixelSize(R.dimen.point_width);
        initThread(context);
    }


    public VideoThumbNailView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }


    public ArrayList<SplitItem> getSplits() {
        return list;
    }

    public void remove(SplitItem item) {

        list.remove(item);
        int moffset = item.getRect().width() + SPLIT_WIDTH;
        int count = list.size();
        for (int i = 0; i < count; i++) {
            SplitItem temp = list.get(i);
            if (temp.getStart() >= item.getEnd()) {
                temp.getRect().offset(-moffset, 0);
                int len = temp.getList().size();
                for (int j = 0; j < len; j++) {
                    temp.getList().get(j).dst.offset(-moffset, 0);
                }
                temp.setStart((int) (temp.getStart() - item.getDuration()));
                temp.setEnd((int) (temp.getEnd() - item.getDuration()));
            }
        }

        count = item.getList().size();
        for (int i = 0; i < count; i++) { // 清除已删除区域的图片

            SplitThumbItemInfo info = item.getList().get(i);
            if (null != info && (!info.isLeft && !info.isRight)) { // 判断图片是否被两边裁剪
                ThumbNailInfo temp = mMemoryCache.remove(info);
                if (null != temp) {
                    temp.recycle();
                }
            }
        }

        setIsEditing(null);

        PointHighLight p;
        count = points.size();
        for (int i = 0; i < count; i++) { // 删除该区间的点
            p = points.get(i);
            if (item.getStart() <= p.getTime() && p.getTime() < item.getEnd()) { // 判断图片是否被两边裁剪
                points.remove(i);
                count--;
            }
        }
        int len = points.size();
        for (int i = 0; i < len; i++) { // 且把后面的点依次向前移动moffset个像素
            p = points.get(i);

            if (p.getTime() >= item.getEnd()) {
                p.getPoint().offset(-moffset, 0);
                p.setTime((int) (p.getTime() - item.getDuration()));

            }
        }

    }

    private Paint mLinePaint = new Paint();

    //当前被选中的分割块
    private SplitItem mEditingItem;

    /**
     * 正在拖动该条数据，准备删除
     *
     * @param editingItem
     */
    public void setIsEditing(SplitItem editingItem) {
        mEditingItem = editingItem;
        invalidate();
    }

    public SplitItem getIsEditing() {
        return mEditingItem;
    }

    private Rect tempItem = new Rect();


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        isDrawing = true;
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG
                | Paint.FILTER_BITMAP_FLAG));

        boolean hasborder = false;
        if (null != list) {
            hasborder = list.size() >= 1 ? true : false;
        }

        if (hasborder) {
            Rect tempRect;
            int half = (int) (BORDER_SIZE * 0.5);
            for (SplitItem item : list) {
                if (mEditingItem != item) { //
                    // 屏蔽正在删除的条目
                    Rect mRect = item.getRect();
                    tempRect = new Rect(mRect.left + half, mRect.top + half,
                            mRect.right - half, mRect.bottom - half);
                    canvas.drawRect(tempRect, mLinePaint);

                }

            }

            for (SplitItem item : list) {
                if (mEditingItem != item) {
                    ArrayList<SplitThumbItemInfo> list = item.getList();
                    int len = list.size();
                    SplitThumbItemInfo temp;
                    for (int i = 0; i < len; i++) {
                        temp = list.get(i);
                        Bitmap bmp = getBitmapFromMemCache(temp.nTime);
                        if (null != bmp && !bmp.isRecycled()) {
                            int mleft = temp.dst.left;
                            int mtop = temp.dst.top + BORDER_SIZE;
                            int mright = temp.dst.right;
                            int mbottom = temp.dst.bottom - BORDER_SIZE;

                            boolean showall = true;
                            int srcleft = temp.src.left, srcright = temp.src.right;
                            if (temp.isLeft) {
                                showall = false;
                                mleft = temp.dst.left + BORDER_SIZE;
                                srcleft = srcleft + BORDER_SIZE;
                            }
                            if (temp.isRight) {
                                showall = false;
                                mright = mright - BORDER_SIZE;
                                srcright = srcright - BORDER_SIZE;
                            }
                            tempItem.set(mleft, mtop, mright, mbottom);

                            if (showall) {
                                canvas.drawBitmap(bmp, null, tempItem, null);
                            } else {
                                Rect src = new Rect(srcleft, temp.src.top
                                        + BORDER_SIZE, srcright, temp.src.bottom
                                        - BORDER_SIZE);
                                canvas.drawBitmap(bmp, src, tempItem, null);

                            }
                        }
                    }
                }

            }

        } else {

            for (Entry<Integer, ThumbNailInfo> item : mMemoryCache.entrySet()) {
                ThumbNailInfo temp = item.getValue();

                if (temp != null && temp.bmp != null && !temp.bmp.isRecycled()) {
                    canvas.drawBitmap(temp.bmp, null, temp.dst, null);
                }
            }

        }
        if (null != points) {
            int len = points.size();
            Point p;
            PointHighLight tempHigh;
            pLight.setTextSize(40);
            for (int j = 0; j < len; j++) {
                tempHigh = points.get(j);
                p = points.get(j).getPoint();
                if ((null == mEditingItem)
                        || !(mEditingItem.getStart() <= tempHigh.getTime() && tempHigh
                        .getTime() <= mEditingItem.getEnd())) {
                    canvas.drawCircle(p.x, p.y, mHighLight, pLight);
                }
            }
        }
        isDrawing = false;
    }


    public void setHighLights(int[] _lights) {
        lights = _lights;
        points.clear();
        if (null != lights) {

            int len = lights.length;
            for (int i = 0; i < len; i++) {
                int py = thumbH / 2;
                int left = (int) ((lights[i] + 0.0) / mduration * params[0]);
                points.add(new PointHighLight(lights[i], new Point(left, py)));
            }

        }
    }

    /**
     * 长按组件
     *
     * @author ADMIN
     */
    public interface onLongListener {
        /**
         * 开始长按
         *
         * @param x
         * @param y
         */
        public void onLong(int x, int y);

        /**
         * 松开长按
         */
        public void onCancel();

    }

    private VirtualVideo mVirtualVideo;

    private int mduration;
    private MediaObject mediaObject;
    private int lastTime = -1;

    private int[] params = new int[2];

    private int thumbW = ThumbNailUtils.THUMB_WIDTH,
            thumbH = ThumbNailUtils.THUMB_HEIGHT;

    /**
     * @param virtualVideo
     * @param vo
     * @return
     */
    public int[] setVirtualVdieo(VirtualVideo virtualVideo,
                                 Scene vo) {
        mVirtualVideo = virtualVideo;
        int nduration = Utils.s2ms(mVirtualVideo.getDuration());
        if (nduration < 5000) {
            maxCount = 2;
        } else if (nduration < 10000) {
            maxCount = 8;
        } else if (nduration < 60000) {
            maxCount = (nduration / 3000);
        } else {
            maxCount = 25;
        }
        mduration = nduration;
        itemTime = mduration / maxCount;
        mediaObject = vo.getAllMedia().get(0);
        double fwh = mediaObject.getWidth() / (mediaObject.getHeight() + .0);
        thumbW = (int) (thumbH * fwh);
        params[0] = thumbW * maxCount;

        if ((mduration % itemTime) != 0) {
            // 画半块
            lastTime = (int) ((double) itemTime * (maxCount - 0.1));
            params[0] = params[0] + thumbW / 2;
        }
        params[1] = thumbH;
        return params;
    }


    private final Handler mhandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {

            switch (msg.what) {
                case THUMBITEM:
                    if (!isDrawing)
                        invalidate();
                    break;

                default:
                    break;
            }

        }

        ;
    };

    /**
     * 缓存Image的类，当存储Image的大小大于LruCache设定的值，系统自动释放内存
     */
    private HashMap<Integer, ThumbNailInfo> mMemoryCache;

    /**
     * 异步加载图片
     *
     * @param context
     */
    private void initThread(Context context) {
        mMemoryCache = new HashMap<Integer, ThumbNailInfo>();

    }

    /**
     * 下载Image的线程池
     */
    private ExecutorService mImageThreadPool = null;

    /**
     * 获取线程池的方法，因为涉及到并发的问题，我们加上同步锁
     *
     * @return
     */
    private ExecutorService getThreadPool() {
        if (mImageThreadPool == null) {
            synchronized (ExecutorService.class) {
                if (mImageThreadPool == null) {
                    // 为了下载图片更加的流畅，我们用了2个线程来加载图片
                    mImageThreadPool = Executors.newFixedThreadPool(4);
                }
            }
        }

        return mImageThreadPool;

    }

    /**
     * 添加Bitmap到内存缓存
     *
     * @param key
     * @param bitmap
     */
    private void addBitmapToMemoryCache(Integer key, Rect src, Rect dst,
                                        boolean isleft, boolean isright, Bitmap bitmap) {
        ThumbNailInfo info = new ThumbNailInfo(key, src, dst, isleft, isright);
        info.bmp = bitmap;
        mMemoryCache.put(key, info);

    }

    /**
     * 从内存缓存中获取一个Bitmap
     *
     * @param key
     * @return
     */
    public Bitmap getBitmapFromMemCache(Integer key) {
        ThumbNailInfo info = mMemoryCache.get(key);
        return (null != info) ? info.bmp : null;
    }

    private int maxCount = 40;

    private int itemTime = 0;

    /**
     * 开始加载图片
     */
    public void setStartThumb() {
        Rect tdst = new Rect(0, 0, thumbW, thumbH), src = new Rect(0, 0,
                thumbW, thumbH);

        int splitTime = itemTime / 2; // 每张图对应的中间时刻

        downloadImage(splitTime, src, new Rect(tdst), true, false);

        for (int i = 1; i < (maxCount - 1); i++) {
            tdst = new Rect(tdst.right, tdst.top, tdst.right + thumbW,
                    tdst.bottom);
            splitTime += itemTime;
            downloadImage(splitTime, src, tdst, false, false);
        }

        if (lastTime > 0) { // 有最后半张
            splitTime += itemTime;
            tdst = new Rect(tdst.right, tdst.top, tdst.right + thumbW,
                    tdst.bottom);
            downloadImage(splitTime, src, tdst, false, false);

            int half = thumbW / 2;
            src = new Rect(0, 0, half, thumbH);
            tdst = new Rect(params[0] - half, tdst.top, params[0], tdst.bottom);
            downloadImage(lastTime, src, tdst, false, true);
        } else { // 没最后半张

            tdst = new Rect(tdst.right, tdst.top, tdst.right + thumbW,
                    tdst.bottom);
            downloadImage(mduration - itemTime / 2, src, tdst, false, true);

        }

    }

    private void downloadImage(final int nTime, final Rect src, final Rect dst,
                               final boolean isleft, final boolean isright) {

        Bitmap bitmap = getBitmapFromMemCache(nTime);
        if (bitmap != null) {
            mhandler.sendEmptyMessage(THUMBITEM);
        } else {

            if (mMemoryCache.get(nTime) == null) {
                addBitmapToMemoryCache(nTime, src, dst, isleft, isright, bitmap);
                getThreadPool().execute(new Runnable() {

                    @Override
                    public void run() {

                        Bitmap bitmap = Bitmap.createBitmap(thumbW, thumbH,
                                Config.ARGB_8888);
                        if (null != mVirtualVideo && mVirtualVideo.getSnapshot(getContext(),Utils.ms2s(nTime), bitmap)) {
                            // 将Bitmap 加入内存缓存
                            addBitmapToMemoryCache(nTime, src, dst, isleft,
                                    isright, bitmap);
                            mhandler.sendEmptyMessage(THUMBITEM);
                        } else {
                            bitmap.recycle();

                        }
                    }
                });
            }
        }

    }

    /**
     * 释放资源
     */
    public void recycle() {
        if (null != mVirtualVideo) {
            mVirtualVideo.release();
            mVirtualVideo = null;
        }
        list.clear();
        for (Entry<Integer, ThumbNailInfo> item : mMemoryCache.entrySet()) {
            ThumbNailInfo temp = item.getValue();
            if (null != temp) {
                temp.recycle();
            }
        }
        mMemoryCache.clear();
        invalidate();

    }

    public Rect getMaxRect() {

        Rect temp = new Rect(0, 0, 0, 0);

        for (int i = 0; i < list.size(); i++) {
            Rect m = list.get(i).getRect();
            temp = (temp.right < m.right) ? m : temp;
        }
        return temp;

    }

    public int getDuration() {
        int duration = 0;
        for (int i = 0; i < list.size(); i++) {
            SplitItem m = list.get(i);
            duration = duration + (m.getEnd() - m.getStart());
        }
        return duration;

    }


    /**
     * 获取当前进度时刻，距离时间轴left的距离
     *
     * @param progress 当前进度
     * @param temp     当前的片段
     * @return 单位px
     */
    public int getSplitRectLeft(int progress, SplitItem temp) {
        return (int) (temp.getRect().left + ((progress - temp.getStart() + 0.0)
                / temp.getDuration() * temp.getRect().width()));

    }

    public void onSplit(int ntime) {
        int index = getSplitItemIndexByMin(ntime);

        ArrayList<SplitThumbItemInfo> mlistLeft = new ArrayList<SplitThumbItemInfo>(), mlistRight = new ArrayList<SplitThumbItemInfo>();
        if (index != -1) {
            SplitItem temp = list.get(index);
            ArrayList<SplitThumbItemInfo> mthumb = temp.getList();
            int nX = getSplitRectLeft(ntime, temp);
            int len = mthumb.size();
            SplitThumbItemInfo info;
            Rect src, dst;
            for (int i = 0; i < len; i++) {

                info = mthumb.get(i);
                if (info.dst.left <= nX) {
                    if (nX <= info.dst.right) {
                        // 保留左边部分
                        src = new Rect(info.src.left, 0, info.src.left
                                + (nX - info.dst.left), info.dst.bottom);

                        dst = new Rect(info.dst.left, 0, nX, info.dst.bottom);

                        mlistLeft.add(new SplitThumbItemInfo(info.nTime, src,
                                dst, info.isLeft, true));

                        // 保留右边部分
                        src = new Rect(src.right, 0, info.src.right,
                                info.src.bottom);

                        dst = new Rect(nX + SPLIT_WIDTH, 0, info.dst.right
                                + SPLIT_WIDTH, info.dst.bottom);

                        mlistRight.add(new SplitThumbItemInfo(info.nTime, src,
                                dst, true, info.isRight));

                    } else {
                        mlistLeft.add(new SplitThumbItemInfo(info.nTime,
                                info.src, info.dst, info.isLeft, info.isRight));
                    }
                } else {
                    info.dst.offset(SPLIT_WIDTH, 0);
                    mlistRight.add(new SplitThumbItemInfo(info.nTime, info.src,
                            info.dst, info.isLeft, info.isRight));

                }

            }

            ArrayList<SplitItem> old = new ArrayList<SplitItem>();
            old.addAll(list);
            list.clear();
            for (int i = 0; i < index; i++) {
                list.add(old.get(i));
            }
            SplitItem left = new SplitItem();

            left.setStart(temp.getStart());
            left.setEnd(ntime);
            left.setTlstart(temp.getTlstart());
            left.setTlend((int) (temp.getTlstart() + (left.getDuration() * mediaObject
                    .getSpeed())));
            left.setList(mlistLeft);

            left.setRect(new Rect(temp.getRect().left, temp.getRect().top, nX,
                    temp.getRect().bottom));
            list.add(left);

            SplitItem right = new SplitItem();
            right.setList(mlistRight);
            right.setStart(ntime);
            right.setEnd((int) temp.getEnd());

            right.setTlstart(left.getTlend());
            right.setTlend(temp.getTlend());

            right.setRect(new Rect(nX + SPLIT_WIDTH, temp.getRect().top, temp
                    .getRect().right + SPLIT_WIDTH, temp.getRect().bottom));
            list.add(right);
            int end = old.size();
            for (int i = (index + 1); i < end; i++) { // 大于该时刻的时间片段全部向右偏移
                SplitItem temp2 = old.get(i);
                if (temp2.getTlstart() >= right.getTlend()) {
                    for (int j = 0; j < temp2.getList().size(); j++) { // 区域全部向右偏移
                        temp2.getList().get(j).dst.offset(SPLIT_WIDTH, 0);
                    }
                    temp2.getRect().offset(SPLIT_WIDTH, 0);

                }
                list.add(temp2);

            }
            old.clear();

            len = points.size();
            PointHighLight p;
            for (int i = 0; i < len; i++) {
                p = points.get(i);
                if (p.getTime() > ntime) {
                    p.getPoint().offset(SPLIT_WIDTH, 0);
                }
            }

        } else {
            int nX = (int) (ntime * ((getWidth() + 0.0) / mduration));
            SplitItem left = new SplitItem();
            left.setStart(0);
            left.setEnd(ntime);

            left.setTlstart(Utils.s2ms(mediaObject.getTrimStart()));
            left.setTlend((int) (left.getTlstart() + left.getDuration()
                    * mediaObject.getSpeed()));

            int mright = nX + SPLIT_WIDTH;
            Rect src, dst;
            for (Entry<Integer, ThumbNailInfo> item : mMemoryCache.entrySet()) {
                ThumbNailInfo info = item.getValue();
                if (info.dst.left <= nX) {
                    if (nX <= info.dst.right) { // 拆分当前

                        int offWidth = nX - info.dst.left;

                        if (offWidth > thumbW) {
                            offWidth = thumbW;
                        }
                        // 保留左边部分
                        src = new Rect(0, 0, offWidth, info.src.bottom);

                        dst = new Rect(info.dst.left, 0, nX, info.dst.bottom);
                        mlistLeft.add(new SplitThumbItemInfo(info.nTime, src,
                                dst, info.isLeft, true));

                        // 保留右边部分
                        src = new Rect(offWidth, 0, info.src.right,
                                info.src.bottom);
                        dst = new Rect(nX + SPLIT_WIDTH, 0, info.dst.right
                                + SPLIT_WIDTH, info.dst.bottom);
                        mlistRight.add(new SplitThumbItemInfo(info.nTime, src,
                                dst, true, info.isRight));

                        int mr = dst.right;
                        if (mr >= mright) {
                            mright = mr;
                        }

                    } else {
                        mlistLeft.add(new SplitThumbItemInfo(info.nTime,
                                info.src, info.dst, info.isLeft, false));
                    }
                } else {
                    info.dst.offset(SPLIT_WIDTH, 0);
                    mlistRight.add(new SplitThumbItemInfo(info.nTime, info.src,
                            info.dst, false, info.isRight));
                    int mr = info.dst.right;
                    if (mr >= mright) {
                        mright = mr;
                    }
                }
            }

            left.setList(mlistLeft);
            left.setRect(new Rect(0, 0, nX, thumbH));
            list.add(left);

            SplitItem right = new SplitItem();
            right.setStart(ntime);
            right.setEnd(mduration);
            right.setTlstart(left.getTlend());
            right.setTlend((int) (mediaObject.getTrimEnd() * 1000));
            right.setRect(new Rect(nX + SPLIT_WIDTH, 0, mright, thumbH));
            right.setList(mlistRight);
            list.add(right);

            int len = points.size();
            PointHighLight p;
            for (int i = 0; i < len; i++) {
                p = points.get(i);
                if (p.getTime() > ntime) {
                    p.getPoint().offset(SPLIT_WIDTH, 0);
                }
            }

        }

        invalidate();
    }

    public int getSplitThumbWidth() {
        return SPLIT_WIDTH;
    }

    /**
     * 获取当前时刻对应的splitItem
     *
     * @param progress 当前时刻
     * @return
     */
    public SplitItem getSplitItemByMin(int progress) {
        int index = getSplitItemIndexByMin(progress);
        if (index >= 0 && index < list.size()) {
            return list.get(index);
        }
        return null;

    }

    public int getSplitItemIndexByMin(int progress) {
        int index = -1;
        int len = list.size();
        for (int i = 0; i < len; i++) {
            SplitItem tempItem = list.get(i);
            if (tempItem.getStart() < progress && progress <= tempItem.getEnd()) {
                index = i;
                break;
            }
        }
        return index;
    }

    /**
     * 通过时间轴滚动的进度获取当前的splitItem
     *
     * @param scrollX 单位px
     * @return
     */
    public SplitItem getSplitItemByScrollX(int scrollX) {
        SplitItem item = null;
        int len = list.size();
        SplitItem tempItem;
        for (int i = 0; i < len; i++) {
            tempItem = list.get(i);

            if ((tempItem.getRect().left) <= scrollX
                    && scrollX <= (tempItem.getRect().right + 5)) {
                item = tempItem;
                break;
            }

        }

        return item;

    }

}
