package com.rd.veuisdk.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.os.Handler;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.rd.http.MD5;
import com.rd.vecore.VirtualVideo;
import com.rd.vecore.models.MediaObject;
import com.rd.vecore.models.MediaType;
import com.rd.vecore.models.Scene;
import com.rd.vecore.utils.MiscUtils;
import com.rd.veuisdk.R;
import com.rd.veuisdk.ui.DraggableGridView.RearrangeListAdapater;
import com.rd.veuisdk.ui.ExtListItemView;
import com.rd.veuisdk.utils.DateTimeUtils;
import com.rd.veuisdk.utils.ThumbNailUtils;
import com.rd.veuisdk.utils.Utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DragMediaAdapter extends VideoSelectorAdapter implements RearrangeListAdapater {
    private final int SCALE_TEXTSIZE = 8;
    private Context mContext;
    private DragItemListener mDragItemListener = null;
    private boolean mHasSort = false;

    public DragMediaAdapter(Context context, LayoutInflater inflater) {
        super(inflater);
        mContext = context;
    }

    public void updateThumb() {
        onDestroy();
        mCacheArray = new SparseArray<>();
        notifyDataSetChanged();
    }

    /*
     * 重新排序
     */
    @Override
    public void onRearrange(int oldIndex, int newIndex) {
        int count = getCount();
        if (count > 0) {
            if (oldIndex < newIndex) {
                Scene current = mArrItems.get(oldIndex);
                for (int i = oldIndex; i < newIndex; i++) {
                    int j = i + 1;
                    if (j < count) {
                        mArrItems.set(i, mArrItems.get(j));
                    }
                }
                if (newIndex < count) {
                    mArrItems.set(newIndex, current);
                }
            }

            if (newIndex < oldIndex) {// 从后往前
                Scene current = mArrItems.get(oldIndex);
                for (int i = oldIndex; i > newIndex; i--) {
                    int j = i - 1;
                    if (j >= newIndex) {
                        mArrItems.set(i, mArrItems.get(j));
                    }
                }

                if (newIndex < count) {
                    mArrItems.set(newIndex, current);
                }
            }
        }

    }

    /**
     * 按住准备移动当前项的位置
     */
    @Override
    public void onTouched(int index) {
        selectedIndex = index;
        notifyDataSetChanged();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View itemView;
        if (convertView == null) {
            itemView = mInflater.inflate(R.layout.item_edit_priview, null);
        } else {
            itemView = convertView;
        }
        if (mArrItems == null || getCount() == 0 || position >= mArrItems.size()) {
            return itemView;
        }


        ExtListItemView extListItemView = Utils.$(itemView, R.id.ivItemExt);
        TextView tvItemNum = Utils.$(itemView, R.id.tvItemNum1);
        ImageView ivType = Utils.$(itemView, R.id.ivItemType);
        TextView tvDuration = Utils.$(itemView, R.id.tvDuration);
        RelativeLayout rb = Utils.$(itemView, R.id.rlRemove);
        final Scene item = mArrItems.get(position);
        if (item != null) {
            if (!setResource(extListItemView, getKey(item))) {
                getThreadPool().execute(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            getThumb(item, position);
                            //防止频繁取图，造成锁死
                            Thread.sleep(200);
                        } catch (Exception e) {
                        }

                    }
                });
            }
            extListItemView.setSelected(selectedIndex == position);

            rb.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    mDragItemListener.onRemove(position);
                }
            });

            tvItemNum.setText(Integer.toString(position + 1));
            tvDuration.setText(DateTimeUtils.stringForMillisecondTime(MiscUtils.s2ms(item.getDuration()), true, true));

            if (selectedIndex == position) {
                tvDuration.setGravity(Gravity.CENTER_HORIZONTAL);
                rb.setVisibility(View.VISIBLE);

            } else {
                rb.setVisibility(View.INVISIBLE);
                tvDuration.setGravity(Gravity.LEFT);
            }

            if (item.getAllMedia().size() == 1) {
                MediaObject mediaObject = item.getAllMedia().get(0);
                if (mediaObject.getMediaType() == MediaType.MEDIA_VIDEO_TYPE) {
                    ivType.setImageResource(R.drawable.edit_item_video);
                } else {
                    if (mDragItemListener != null) {
                        if (mDragItemListener.isExt(position)) {
                            ivType.setImageResource(R.drawable.edit_item_text);
                        } else {
                            ivType.setImageResource(R.drawable.edit_item_image);
                        }
                    }
                }
            }
            if (position >= 99) {
                tvItemNum.setTextSize(SCALE_TEXTSIZE);
            }
        } else {
            extListItemView.setBackgroundResource(R.drawable.edit_add_video_button);
            tvItemNum.setVisibility(View.INVISIBLE);
            rb.setVisibility(View.INVISIBLE);
        }
        return itemView;
    }

    private SparseArray<Bitmap> mCacheArray = new SparseArray<>();

    private boolean setResource(ExtListItemView itemView, int key) {
        Bitmap bmp = getBmp(key);
        if (null != bmp) {
            itemView.setbitmap(bmp);
            return true;
        }
        return false;
    }

    private Bitmap getBmp(int key) {
        Bitmap bmp = mCacheArray.get(key);
        if (null != bmp && !bmp.isRecycled()) {
            return bmp;
        }
        return null;
    }


    private float getKind(Scene scene) {
        return Math.min(0.2f, scene.getDuration());
    }

    private int getKey(Scene scene) {
        MediaObject mediaObject = scene.getAllMedia().get(0);
        return MD5.getMD5(mediaObject.hashCode() + "flip:" + mediaObject.getFlipType() + "angle: " + mediaObject.getAngle()
                + " trim:" + mediaObject.getTrimStart() + "<>" + mediaObject.getTrimEnd()
                + getKind(scene) + "scene:" + scene.getAllMedia().size() + " _" + scene.hashCode()).hashCode();
    }

    private int selectedIndex = -1;

    public void setCheckId(int position) {
        selectedIndex = position;
        notifyDataSetChanged();
    }


    private void getThumb(Scene scene, int nId) {
        int key = getKey(scene);
        if (null != mCacheArray.get(key) && !mHasSort) {
            mhandler.sendEmptyMessage(ITEM_THUMB_OK);
        } else {
            Bitmap bmp = Bitmap.createBitmap(
                    ThumbNailUtils.THUMB_HEIGHT,
                    ThumbNailUtils.THUMB_HEIGHT, Config.ARGB_8888);
            VirtualVideo virtualVideo = new VirtualVideo();
            virtualVideo.addScene(scene);
            if (virtualVideo.getSnapshot(mContext, getKind(scene), bmp, scene.getAllMedia().size() > 1)) {
                virtualVideo.release();
                if (null != mCacheArray) {
                    mCacheArray.put(key, bmp);
                    //防止频繁更新
                    mhandler.sendEmptyMessage(ITEM_THUMB_OK);
                } else {
                    bmp.recycle();
                }
            } else {
                virtualVideo.release();
                bmp.recycle();
            }

        }
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
                    mImageThreadPool = Executors.newFixedThreadPool(2);
                }
            }
        }

        return mImageThreadPool;

    }

    private final int ITEM_THUMB_OK = 6;
    private Handler mhandler = new Handler() {

        public void handleMessage(android.os.Message msg) {

            switch (msg.what) {
                case ITEM_THUMB_OK:
                    notifyDataSetChanged();
                    break;
                default:
                    break;
            }

        }

        ;
    };

    public void sortActivity(boolean sort) {
        mHasSort = sort;
    }

    /**
     * recycle data
     */
    public void onDestroy() {
        if (mCacheArray != null && mCacheArray.size() > 0) {
            for (int i = 0; i < mCacheArray.size(); i++) {
                Bitmap bmp = mCacheArray.valueAt(i);
                if (null != bmp && !bmp.isRecycled()) {
                    bmp.recycle();
                }
            }
            mCacheArray.clear();
        }
        mhandler.removeMessages(ITEM_THUMB_OK);
    }

    /**
     * 清除缓存，重新load缩略图
     *
     * @param scene
     */
    public void onClear(Scene scene) {
        if (mCacheArray != null && mCacheArray.size() > 0 && null != scene) {
            int key = getKey(scene);
            Bitmap bmp = mCacheArray.get(key);
            if (null != bmp) {
                mCacheArray.remove(key);
                if (!bmp.isRecycled()) {
                    bmp.recycle();
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return mArrItems.size();
    }

    public void setDragItemListener(DragItemListener listener) {
        mDragItemListener = listener;
    }

    /**
     * 和编辑1的接口
     */
    public interface DragItemListener {
        /**
         * 删除视频
         *
         * @param position
         */
        void onRemove(int position);

        /**
         * 判断是否为文字版
         *
         * @param position
         */
        boolean isExt(int position);
    }

}
