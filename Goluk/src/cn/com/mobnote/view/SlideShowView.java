package cn.com.mobnote.view;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.bumptech.glide.Glide;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import cn.com.mobnote.golukmobile.R;

/**
 * ViewPager实现的轮播图广告自定义视图，如京东首页的广告轮播图效果；
 * 既支持自动轮播页面也支持手势滑动切换页面
 */

public class SlideShowView extends FrameLayout implements View.OnClickListener{

    private static final String TAG = SlideShowView.class.getSimpleName();

    //轮播图图片数量
    private final static int IMAGE_COUNT = 5;
    //自动轮播的时间间隔
    private final static int TIME_INTERVAL = 5;
    //自动轮播启用开关
    private final static boolean isAutoPlay = true;

    //自定义轮播图的资源
    private List<String> imageUrls = new ArrayList<String>();
    //放轮播图片的ImageView 的list
    private List<ImageView> imageViewsList;
    //放圆点的View的list
    private List<View> dotViewsList;

    private ViewPager viewPager;
    //当前轮播页
    private int currentItem = 0;
    //定时任务
    private ScheduledExecutorService scheduledExecutorService;

    private Context context;

    private boolean isInited = false;

    private OnImageClickedListener mListener;

    private class ImageViewTag {
        public ImageViewTag(int position, String url) {
            this.position = position;
            this.url = url;
        }

        public int position;
        public String url;
    }

    @Override
    public void onClick(View v) {
        final ImageViewTag tag = (ImageViewTag)v.getTag(R.id.tag_slideshow_item);
        if(null != mListener) {
            this.mListener.onImageClicked(tag.position, tag.url);
        }
    }

    public interface OnImageClickedListener {
        public void onImageClicked(int position, String url);
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
        this.context = context;

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
        scheduledExecutorService.shutdown();
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
        if (imageUrls == null || imageUrls.size() == 0)
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
        for (int i = 0; i < imageUrls.size(); i++) {
            ImageView view = new ImageView(context);
            view.setTag(R.id.tag_slideshow_item, new ImageViewTag(i, imageUrls.get(i)));
            if (i == 0)//给一个默认图
                view.setBackgroundResource(R.drawable.tacitly_pic);
            view.setScaleType(ScaleType.FIT_XY);
            imageViewsList.add(view);
            view.setOnClickListener(this);

            ImageView dotView = new ImageView(context);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            params.leftMargin = 4;
            params.rightMargin = 4;
            dotLayout.addView(dotView, params);
            dotViewsList.add(dotView);
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
            container.removeView(imageViewsList.get(position));
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            ImageView imageView = imageViewsList.get(position);

            Glide.with(context)
            .load(((ImageViewTag)imageView.getTag(R.id.tag_slideshow_item)).url)
            .placeholder(R.drawable.tacitly_pic)
            .error(R.drawable.tacitly_pic)
            .fallback(R.drawable.tacitly_pic)
            .into(imageView);

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
    private class MyPageChangeListener implements OnPageChangeListener {
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

    public void setImageUrlList(List<String> urlList) {
        imageUrls = urlList;
        initUI(context);
    }

    public void addImageUrl(String url) {
        if(!imageUrls.contains(url)) {
            imageUrls.add(url);
            initUI(context);
        }
    }

    public void clearImages() {
        imageUrls.clear();
        initUI(context);
    }

    public void onDestroy() {
        stopPlay();
    }
}
