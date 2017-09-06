package com.rd.veuisdk.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.os.Handler;
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
import com.rd.vecore.exception.InvalidStateException;
import com.rd.vecore.models.MediaObject;
import com.rd.vecore.models.MediaType;
import com.rd.vecore.models.Scene;
import com.rd.veuisdk.R;
import com.rd.veuisdk.ui.DraggableGridView.RearrangeListAdapater;
import com.rd.veuisdk.ui.ExtListItemView;
import com.rd.veuisdk.utils.DateTimeUtils;
import com.rd.veuisdk.utils.ThumbNailUtils;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DragMediaAdapter extends VideoSelectorAdapter implements RearrangeListAdapater {
    private final String TAG = "dragAdapter";

    private final int SCALE_TEXTSIZE = 8;
    private Context mContext;
    private DragItemListener mDragItemListener = null;
    private boolean mHasSort = false;

    public DragMediaAdapter(Context context, LayoutInflater inflater) {
        super(inflater);
        mContext = context;
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
        if (mArrItems == null || getCount() == 0) {
            return itemView;
        }

        final Scene item = mArrItems.get(position);
        final ExtListItemView itemview = (ExtListItemView) itemView.findViewById(R.id.ivItemExt);
        TextView tvItemNum = (TextView) itemView.findViewById(R.id.tvItemNum1);
        ImageView ivType = (ImageView) itemView.findViewById(R.id.ivItemType);
        TextView tvDuration = (TextView) itemView.findViewById(R.id.tvDuration);

        RelativeLayout rb = (RelativeLayout) itemView.findViewById(R.id.rlRemove);
        if (item != null) {
            if (!setResource(itemview, getKey(item))) {
                getThreadPool().execute(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            getThumb(item, position, itemview);
                            Thread.sleep(200);
                        } catch (Exception e) {
                        }

                    }
                });
            }
            itemview.setSelected(selectedIndex == position);

            rb.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    mDragItemListener.onRemove(position);
                }
            });

            tvItemNum.setText("" + (position + 1));
            tvDuration.setText(DateTimeUtils.stringForMillisecondTime(
                    (long) (item.getDuration() * 1000), true, true));

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
            itemview.setBackgroundResource(R.drawable.edit_add_video_button);
            tvItemNum.setVisibility(View.INVISIBLE);

            rb.setVisibility(View.INVISIBLE);
        }
        return itemView;
    }

    private HashMap<String, Bitmap> maps = new HashMap<String, Bitmap>();

    private boolean setResource(ExtListItemView itemview, String key) {
        Bitmap bmp = getBmp(key);
        if (null != bmp) {
            itemview.setbitmap(bmp);
            return true;
        }
        return false;
    }

    private Bitmap getBmp(String key) {
        Bitmap bmp = maps.get(key);
        if (null != bmp && !bmp.isRecycled()) {
            return bmp;
        }
        return null;
    }

    public Bitmap getThumbItem(int index) {
        return maps.get(getKey(mArrItems.get(index)));
    }

    private float getKind(Scene tempScene) {
        com.rd.vecore.models.MediaObject mediaObject = tempScene.getAllMedia().get(0);
        return Math.min(1, mediaObject.getDuration());
    }

    private String getKey(Scene tempScene) {
        MediaObject mediaObject = tempScene.getAllMedia().get(0);
        return MD5.getMD5(mediaObject.getMediaPath() + mediaObject.getTrimStart() + "..."
                + mediaObject.getTrimEnd())
                + getKind(tempScene);
    }

    private int selectedIndex = -1;

    public void setCheckId(int position) {
        selectedIndex = position;
        notifyDataSetChanged();
    }

    private void getThumb(Scene tempScene, final int nId, ExtListItemView itemview) {

        float kind = getKind(tempScene);
        String key = getKey(tempScene);

        if (maps.containsKey(key) && !mHasSort) {
            mhandler.sendMessage(mhandler.obtainMessage(ITEM_THUMB_OK, nId, 0));
        } else {
            Bitmap bmp = Bitmap.createBitmap(
                    ThumbNailUtils.THUMB_HEIGHT,
                    ThumbNailUtils.THUMB_HEIGHT, Config.ARGB_8888);
            VirtualVideo virtualVideo = new VirtualVideo();
            virtualVideo.addScene(tempScene);
            try {
                virtualVideo.build(mContext);
            } catch (InvalidStateException e) {
                e.printStackTrace();
            }
            if (virtualVideo.getSnapshot(kind, bmp)) {
                if (null != maps) {
                    maps.put(key, bmp);
                    mhandler.sendMessage(mhandler.obtainMessage(
                            ITEM_THUMB_OK, nId, 0, itemview));
                } else {
                    bmp.recycle();
                    bmp = null;
                }
            } else {
                bmp.recycle();
                bmp = null;
            }
            virtualVideo.release();
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

    private final int ITEM_THUMB_OK = 6, ALL_OVER = 8;
    private Handler mhandler = new Handler() {

        public void handleMessage(android.os.Message msg) {

            switch (msg.what) {
                case ITEM_THUMB_OK:
                    ExtListItemView itemview = ((ExtListItemView) msg.obj);
                    if (null != itemview) {
                        Scene scene = getItem(msg.arg1);
                        if (scene != null) {
                            if (maps != null) {
                                itemview.setbitmap(maps.get(getKey(scene)));
                                itemview.setSelected(selectedIndex == msg.arg1);
                            }
                        }
                    }
                    break;
                case ALL_OVER:
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

        if (maps != null && maps.size() > 0) {
            Set<Entry<String, Bitmap>> all = maps.entrySet();
            Bitmap bmp;
            for (Entry<String, Bitmap> item : all) {
                bmp = item.getValue();
                if (null != bmp && !bmp.isRecycled()) {
                    bmp.recycle();
                }
                bmp = null;
            }
            maps.clear();
            maps = null;
            all.clear();
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
