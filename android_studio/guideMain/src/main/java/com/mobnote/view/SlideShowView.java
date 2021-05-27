package com.mobnote.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.UserOpenUrlActivity;
import com.mobnote.golukmain.newest.BannerSlideBody;
import com.mobnote.util.GolukConfig;
import com.mobnote.util.ZhugeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

/**
 * ViewPager实现的轮播图广告自定义视图，如京东首页的广告轮播图效果；
 * 既支持自动轮播页面也支持手势滑动切换页面
 */

public class SlideShowView extends FrameLayout implements View.OnClickListener{

    private static final String TAG = SlideShowView.class.getSimpleName();

    //轮播图图片数量
    private final static int IMAGE_COUNT = 10;
    //自动轮播的时间间隔
    private final static int TIME_INTERVAL = 5;
    //自动轮播启用开关
    private final static boolean isAutoPlay = true;

    //放轮播图片的ImageView 的list
    private List<ImageView> imageViewsList;
    //放圆点的View的list
    private List<View> dotViewsList;
    private List<BannerSlideBody> mBannerDataList = new ArrayList<BannerSlideBody>();

    private ViewPager viewPager;
    //当前轮播页
    private int currentItem = 0;
    //定时任务
    private ScheduledExecutorService scheduledExecutorService;

    private Context mContext;

    private boolean isInited = false;

    private OnImageClickedListener mListener;
    private final static String PURE_PIC = "0";
    private final static String VIDEO_DETAIL = "1";
    private final static String SPECIAL_LIST = "2";
    private final static String LIVE_VIDEO = "3";
    private final static String ACTIVITY_TOGETHER = "4";
    private final static String H5_PAGE = "5";
    private final static String SPECIAL_SOLO = "6";
    private final static String TAG_PAGE = "10";

    private class ImageViewTag {
        public ImageViewTag(int position, BannerSlideBody data) {
            this.position = position;
            this.data = data;
        }

        public int position;
        public BannerSlideBody data;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        stopPlay();
        super.onDetachedFromWindow();
    }

    @Override
    public void onClick(View v) {
        final ImageViewTag tag = (ImageViewTag)v.getTag(R.id.tag_slideshow_item);
        if(null != mListener) {
            this.mListener.onImageClicked(tag.position, tag.data);
        }

        if(null == tag.data) {
            return;
        }

        String picture = tag.data.getPicture();
        if(null == picture || picture.trim().equals("")) {
            return;
        }

        if(picture.equals("fake")) {
            Log.d(TAG, "This is default pic, do nothing");
            return;
        }

        String type = tag.data.getType();
        if(null == type || type.trim().equals("")) {
            return;
        }

        Intent intent = null;

        if (null != type) {
            //轮播图点击
            ZhugeUtils.eventSlideView(mContext, tag.data.getIndex());
            if (PURE_PIC.equals(type)) {
                // do nothing
                Log.d(TAG, "pure picture clicked");
            } else if(VIDEO_DETAIL.equals(type)) {
            } else if(SPECIAL_LIST.equals(type)) {
            } else if(LIVE_VIDEO.equals(type)) {
                //TODO: This should proceed in future
//                intent = new Intent(mContext, LiveActivity.class);
//                intent.putExtra(LiveActivity.KEY_IS_LIVE, false);
//                intent.putExtra(LiveActivity.KEY_GROUPID, "");
//                intent.putExtra(LiveActivity.KEY_PLAY_URL, "");
//                intent.putExtra(LiveActivity.KEY_JOIN_GROUP, "");
//                intent.putExtra(LiveActivity.KEY_USERINFO, user);
//                mContext.startActivity(intent);
            } else if(ACTIVITY_TOGETHER.equals(type)) {
            } else if(H5_PAGE.equals(type)) {
                // launch h5 page
                String accessId = tag.data.getAccess();
                if(null == accessId || accessId.trim().equals("")) {
                    return;
                } else {
                    String url = tag.data.getAccess();
                    intent = new Intent(mContext, UserOpenUrlActivity.class);
                    intent.putExtra(GolukConfig.WEB_TYPE, GolukConfig.NEED_SHARE);
                    intent.putExtra(GolukConfig.H5_URL, url);
                    intent.putExtra(GolukConfig.URL_OPEN_PATH, "image_banner");
                    if(null != tag.data && !tag.data.getTitle().equals("")) {
                        intent.putExtra(GolukConfig.NEED_H5_TITLE, tag.data.getTitle());
                    }
                    mContext.startActivity(intent);
                }
            } else if(SPECIAL_SOLO.equals(type)) {
            } else if(TAG_PAGE.equals(type)) {
            }
        }
    }

    public interface OnImageClickedListener {
        public void onImageClicked(int position, BannerSlideBody data);
    }

    public void setOnImageClickedListener(OnImageClickedListener listener) {
        this.mListener = listener;
    }

    //Handler
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            viewPager.setCurrentItem(currentItem);
        }

    };

    public SlideShowView(Context context) {
        this(context, null);
    }

    public SlideShowView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlideShowView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mContext = context;

        initData();
        if(isAutoPlay) {
            startPlay();
        }
    }

    /**
     * 开始轮播图切换
     */
    private synchronized void startPlay() {
        if(scheduledExecutorService == null || scheduledExecutorService.isShutdown()) {
            scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
            scheduledExecutorService.scheduleAtFixedRate(new SlideShowTask(), 4, 4, TimeUnit.SECONDS);
        }
    }

    /**
     * 停止轮播图切换
     */
    private synchronized void stopPlay() {
        if(null != scheduledExecutorService) {
            scheduledExecutorService.shutdown();
        }
    }

    /**
     * 初始化相关Data
     */
    private void initData() {
        imageViewsList = new ArrayList<ImageView>();
        dotViewsList = new ArrayList<View>();
    }

    /**
     * 初始化Views等UI
     */
    private synchronized void initUI(Context context) {
        if (mBannerDataList == null || mBannerDataList.size() == 0)
            return;

        if(!isInited) {
            LayoutInflater.from(context).inflate(R.layout.layout_slideshow, this, true);
            isInited = true;
        }
        LinearLayout dotLayout = (LinearLayout) findViewById(R.id.dotLayout);
        dotLayout.removeAllViews();
        dotViewsList.removeAll(dotViewsList);
        imageViewsList.removeAll(imageViewsList);

        // 热点个数与图片特殊相等
        for (int i = 0; i < mBannerDataList.size(); i++) {
            ImageView view = new ImageView(context);
            view.setTag(R.id.tag_slideshow_item, new ImageViewTag(i, /*imageUrls.get(i))*/mBannerDataList.get(i)));
            if (i == 0)//给一个默认图
                view.setBackgroundResource(R.drawable.album_default_img);
            view.setScaleType(ScaleType.CENTER_CROP);
            imageViewsList.add(view);
            view.setOnClickListener(this);

            ImageView dotView = new ImageView(context);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            params.leftMargin = 4;
            params.rightMargin = 4;
            dotLayout.addView(dotView, params);
            if(i == 0) {
                dotView.setBackgroundResource(R.drawable.dot_focus);
            } else {
                dotView.setBackgroundResource(R.drawable.dot_blur);
            }
            dotViewsList.add(dotView);
            if(mBannerDataList.size() == 1) {
                dotLayout.setVisibility(View.GONE);
            } else {
                dotLayout.setVisibility(View.VISIBLE);
            }
        }

        viewPager = (ViewPager) findViewById(R.id.viewPager);
        viewPager.setFocusable(true);

        viewPager.setAdapter(new MyPagerAdapter());
        viewPager.setOnPageChangeListener(new MyPageChangeListener());
    }

    /**
     * 填充ViewPager的页面适配器
     */
    private class MyPagerAdapter extends PagerAdapter {

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            //((ViewPag.er)container).removeView((View)object);
            //container.removeView(imageViewsList.get(position));
            if(null != object && null != container) {
                if(object instanceof View && container instanceof ViewPager) {
                    ((ViewPager)container).removeView((View)object);
                }
            }
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            ImageView imageView = imageViewsList.get(position);

            if("fake".equals(((ImageViewTag)imageView.getTag(R.id.tag_slideshow_item)).data.getPicture())) {
                imageView.setImageResource(R.drawable.tacitly_pic);
            } else {
                Glide.with(mContext)
                .load(((ImageViewTag)imageView.getTag(R.id.tag_slideshow_item)).data.getPicture())
                .placeholder(R.drawable.tacitly_pic)
                .error(R.drawable.tacitly_pic)
                .fallback(R.drawable.tacitly_pic)
                .into(imageView);
            }

            container.addView(imageViewsList.get(position));
            return imageViewsList.get(position);
        }

        @Override
        public int getCount() {
            return imageViewsList.size();
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public void restoreState(Parcelable arg0, ClassLoader arg1) {

        }

        @Override
        public Parcelable saveState() {
            return null;
        }

        @Override
        public void startUpdate(View arg0) {

        }

        @Override
        public void finishUpdate(View arg0) {

        }

    }

    /**
     * ViewPager的监听器
     * 当ViewPager中页面的状态发生改变时调用
     */
    private class MyPageChangeListener implements ViewPager.OnPageChangeListener {
        boolean isAutoPlay = false;

        @Override
        public void onPageScrollStateChanged(int arg0) {
            switch (arg0) {
                case 1:// 手势滑动，空闲中
                    stopPlay();
                    isAutoPlay = false;
                    break;
                case 2:// 界面切换中
                    isAutoPlay = true;
                    break;
                case 0:// 滑动结束，即切换完毕或者加载完毕
                    // 当前为最后一张，此时从右向左滑，则切换到第一张
                    startPlay();
                    if (viewPager.getCurrentItem() == viewPager.getAdapter().getCount() - 1 && !isAutoPlay) {
                        viewPager.setCurrentItem(0, false);
                    }
                    // 当前为第一张，此时从左向右滑，则切换到最后一张
                    else if (viewPager.getCurrentItem() == 0 && !isAutoPlay) {
                        viewPager.setCurrentItem(viewPager.getAdapter().getCount() - 1, false);
                    }
                    break;
            }
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageSelected(int pos) {

            currentItem = pos;
            //ILogger.d(TAG, pos + "");
            for (int i = 0; i < dotViewsList.size(); i++) {
                if (i == pos) {
                    ((View) dotViewsList.get(pos)).setBackgroundResource(R.drawable.dot_focus);
                } else {
                    ((View) dotViewsList.get(i)).setBackgroundResource(R.drawable.dot_blur);
                }
            }
        }

    }

    /**
     * 执行轮播图切换任务
     */
    private class SlideShowTask implements Runnable {
        @Override
        public void run() {
            synchronized (viewPager) {
                currentItem = (currentItem + 1) % imageViewsList.size();
                handler.obtainMessage().sendToTarget();
            }
        }

    }

    /**
     * 销毁ImageView资源，回收内存
     */
    private void destoryBitmaps() {
        for (int i = 0; i < IMAGE_COUNT; i++) {
            ImageView imageView = imageViewsList.get(i);
            Drawable drawable = imageView.getDrawable();
            if (drawable != null) {
                //解除drawable对view的引用
                drawable.setCallback(null);
            }
        }
    }

    public void setImageDataList(List<BannerSlideBody> dataList) {
        mBannerDataList = dataList;
        initUI(mContext);
    }

//    public void addImageUrl(String url) {
//        if(!imageUrls.contains(url)) {
//            imageUrls.add(url);
//            initUI(context);
//        }
//    }

    public void clearImages() {
        if(null == mBannerDataList) {
            mBannerDataList.clear();
            initUI(mContext);
        }
    }

    public void onDestroy() {
        stopPlay();
    }
}
