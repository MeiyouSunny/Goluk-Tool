package com.rd.veuisdk.adapter;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;

import com.rd.veuisdk.listener.OnItemClickListener;

/**
 * RecyclerView ->Adapter
 *
 * @param <VH>
 */
public abstract class BaseRVAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    protected String TAG = BaseRVAdapter.class.getName();


    public static final int UN_CHECK = -1;

    protected int lastCheck = UN_CHECK;
    private LayoutInflater mLayoutInflater;


    protected LayoutInflater getLayoutInflater(Context context) {
        if (null == mLayoutInflater) {
            mLayoutInflater = LayoutInflater.from(context);
        }
        return mLayoutInflater;
    }


    /**
     * 被选中的项的下标
     */
    public int getChecked() {
        return lastCheck;
    }


    public void clearChecked(){
        lastCheck=UN_CHECK;
        notifyDataSetChanged();
    }

    /**
     * 是否允许重复点击
     *
     * @param enableRepeatClick
     */
    public void setEnableRepeatClick(boolean enableRepeatClick) {

        this.enableRepeatClick = enableRepeatClick;
    }

    protected boolean enableRepeatClick = false;


    /**
     * 设置单击事件
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }


    protected OnItemClickListener mOnItemClickListener;

    /**
     * 查找容器范围内的组件
     */
    protected <T extends View> T $(View mRoot, int resId) {
        return mRoot.findViewById(resId);
    }

    /**
     * item单击事件
     */
    protected class BaseItemClickListener implements View.OnClickListener {

        protected int position;

        public void setPosition(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {

        }
    }

}
