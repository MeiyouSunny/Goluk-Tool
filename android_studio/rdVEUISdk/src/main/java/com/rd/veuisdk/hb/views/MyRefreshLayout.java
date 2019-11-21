package com.rd.veuisdk.hb.views;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.rd.veuisdk.R;

public class MyRefreshLayout extends SwipeRefreshLayout implements AbsListView.OnScrollListener {

    /**
     * 滑动到最下面时的上拉操作
     * 触发移动事件的最短距离
     */
    private int mTouchSlop;

    /**
     * listview实例
     */
    private ListView mListView;

    /**
     * 上拉监听器, 到了最底部的上拉加载操作
     */
    private OnLoadListener mOnLoadListener;

    /**
     * ListView的加载中footer
     */
    private View mListViewFooter;

    /**
     * 文字提示和旋转等待框
     */
    private TextView mPrompt;
    private ProgressBar mProbar;

    /**
     * 按下时的y坐标
     */
    private int mYDown;

    /**
     * 抬起时的y坐标,mYDown一起用于滑动到底部时判断是上拉还是下拉
     */
    private int mLastY;

    /**
     * 是否在加载中 ( 上拉加载更多 )
     */
    private boolean isLoading = false;


    /**
     * @param context
     */
    public MyRefreshLayout(Context context) {
        this(context, null);
    }

    public MyRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        bNoScroll = false;
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

        mListViewFooter = LayoutInflater.from(context).inflate(R.layout.listview_footer, null,
                false);
        mPrompt = mListViewFooter.findViewById(R.id.pull_to_refresh_loadmore_text);
        mProbar = mListViewFooter.findViewById(R.id.pull_to_refresh_load_progress);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        // 初始化ListView对象
        if (mListView == null) {
            getListView();
        }
    }

    /**
     * 获取ListView对象
     */
    private void getListView() {
        int childs = getChildCount();
        if (childs > 0) {
            View childView = getChildAt(0);
            if (childView instanceof ListView) {
                mListView = (ListView) childView;
                mListView.addFooterView(mListViewFooter);
                // 设置滚动监听器给ListView, 使得滚动的情况下也可以自动加载
                mListView.setOnScrollListener(this);
            }
        }
    }


    private static boolean bNoScroll = false;

    /**
     * 是否屏蔽滑动事件
     *
     * @param noScroll true 屏蔽touch相关的事件，false 保留touch事件
     */
    public static void setNoScroll(boolean noScroll) {
        bNoScroll = noScroll;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (bNoScroll)
            return false;
        else {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // 按下
                    mYDown = (int) ev.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    // 移动
                    mLastY = (int) ev.getRawY();
                    break;
                case MotionEvent.ACTION_UP:
                    // 抬起
                    if (canLoad()) {
                        loadData();
                    }
                    break;
                default:
                    break;
            }
            return super.onInterceptTouchEvent(ev);
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent arg0) {
        if (bNoScroll)
            return false;
        else
            return super.onTouchEvent(arg0);
    }


    /**
     * 是否可以加载更多, 条件是到了最底部, listview不在加载中, 且为上拉操作.
     *
     * @return
     */
    private boolean canLoad() {
        return isBottom() && !isLoading && isPullUp();
    }

    /**
     * 判断是否到了最底部
     */
    private boolean isBottom() {
        if (mListView != null && mListView.getAdapter() != null) {
            return mListView.getLastVisiblePosition() == (mListView.getAdapter().getCount() - 1);
        }
        return false;
    }

    /**
     * 是否是上拉操作
     *
     * @return
     */
    private boolean isPullUp() {
        return (mYDown - mLastY) > mTouchSlop;
    }

    /**
     * 如果到了最底部,而且是上拉操作.那么执行onLoad方法
     */
    private void loadData() {
        if (mOnLoadListener != null) {
            // 设置状态
            setLoading(true);
            //
            mOnLoadListener.onLoad();
        }
    }

    /**
     * @param loading
     */
    public void setLoading(boolean loading) {
        isLoading = loading;
        if (isLoading) {
            mPrompt.setText(R.string.loading);
            mPrompt.setVisibility(VISIBLE);
            mProbar.setVisibility(VISIBLE);
        } else {
            mPrompt.setVisibility(GONE);
            mProbar.setVisibility(GONE);
            mYDown = 0;
            mLastY = 0;
        }
    }

    /**
     * 已经加载完成 不能加载了
     */
    public void noLoad() {
        mPrompt.setText(R.string.already_bottom);
        mPrompt.setVisibility(VISIBLE);
        mProbar.setVisibility(GONE);
    }

    /**
     * 提示上拉加载更多
     */
    public void onUpPromat() {
        mPrompt.setText(R.string.up_load_more);
        mPrompt.setVisibility(VISIBLE);
        mProbar.setVisibility(GONE);
    }

    /**
     * @param loadListener
     */
    public void setOnLoadListener(OnLoadListener loadListener) {
        mOnLoadListener = loadListener;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                         int totalItemCount) {
        // 滚动时到了最底部也可以加载更多
        if (canLoad()) {
            loadData();
        }

//        View firstView = mListView.getChildAt(firstVisibleItem);
//
//        // 当firstVisibleItem是第0位。如果firstView==null说明列表为空，需要刷新;或者top==0说明已经到达列表顶部, 也需要刷新
//        if (firstVisibleItem == 0 && (firstView == null || firstView.getTop() == 0)) {
//            this.setEnabled(true);
//        } else {
//            this.setEnabled(false);
//        }
//        if (null != mOnScrollListener) {
//            mOnScrollListener.onScroll(absListView, firstVisibleItem,
//                    visibleItemCount, totalItemCount);
//        }
//
//        ---------------------
//                作者：王府洞庭
//        来源：CSDN
//        原文：https://blog.csdn.net/chudongting/article/details/60746624
//        版权声明：本文为博主原创文章，转载请附上博文链接！
    }

    /**
     * 加载更多
     *
     * @author mrsimple
     */
    public interface OnLoadListener {
        void onLoad();
    }

}
