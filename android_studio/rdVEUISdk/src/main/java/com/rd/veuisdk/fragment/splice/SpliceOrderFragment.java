package com.rd.veuisdk.fragment.splice;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.rd.lib.ui.PreviewFrameLayout;
import com.rd.vecore.models.MediaObject;
import com.rd.vecore.models.MediaType;
import com.rd.veuisdk.R;
import com.rd.veuisdk.adapter.BaseRVAdapter;
import com.rd.veuisdk.fragment.BaseFragment;
import com.rd.veuisdk.model.SpliceGridMediaInfo;
import com.rd.veuisdk.model.VideoOb;
import com.rd.veuisdk.ui.MultipleBitmapFrameView;
import com.rd.veuisdk.ui.SimpleDraweeViewUtils;
import com.rd.veuisdk.utils.ISpliceHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 拼接-播放顺序
 */
public class SpliceOrderFragment extends BaseFragment {


    public static SpliceOrderFragment newInstance() {
        Bundle args = new Bundle();

        SpliceOrderFragment fragment = new SpliceOrderFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private RadioButton mRbSimultaneously, mRbOrder; //同时、顺序
    private ISpliceHandler mSpliceHandler;
    private RecyclerView mRecyclerView;
    private OrderAdapter mAdapter;

    private MultipleBitmapFrameView mMultipleBitmapFrameView;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mSpliceHandler = (ISpliceHandler) getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.fragment_splice_order_layout, container, false);
        mRecyclerView = $(R.id.recyclerView);
        mMultipleBitmapFrameView = $(R.id.mMultipleBmpFrameView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        //设置添加或删除item时的动画，这里使用默认动画
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        ArrayList<Bitmap> list = new ArrayList<>();
        List<SpliceGridMediaInfo> tmp = mSpliceHandler.getMediaList();
        int len = tmp.size();
        for (int i = 0; i < len; i++) {
            list.add(tmp.get(i).getThumbBmp());
        }
        mMultipleBitmapFrameView.setList(list);
        mAdapter = new OrderAdapter(tmp, -1, mItemHelper);
        mRecyclerView.setAdapter(mAdapter);
        return mRoot;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRbSimultaneously = $(R.id.rbSpliceOrder1);
        mRbOrder = $(R.id.rbSpliceOrder2);
        boolean isAllPic = true;
        int len = mSpliceHandler.getMediaList().size();
        for (int i = 0; i < len; i++) {
            MediaObject mediaObject = mSpliceHandler.getMediaList().get(i).getMediaObject();
            if (null != mediaObject && mediaObject.getMediaType() == MediaType.MEDIA_VIDEO_TYPE) {
                isAllPic = false;
                break;
            }
        }
        if (isAllPic) {
            //全部是图片时，仅能同时播放 -顺序播放无意义
            $(R.id.rbSpliceOrder2).setVisibility(View.GONE);
        }
        RadioGroup rg = $(R.id.rgSpliceOrder);
        if (mSpliceHandler.isOrderPlay()) {
            rg.check(R.id.rbSpliceOrder2);
            onOrderCheck();
        } else {
            rg.check(R.id.rbSpliceOrder1);
            onSimultaneouslyCheck();
        }

        mRbSimultaneously.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSpliceHandler.onSpliceOrder(false);
                onSimultaneouslyCheck();
            }
        });
        mRbOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSpliceHandler.onSpliceOrder(true);
                onOrderCheck();
            }
        });

        mItemHelper.attachToRecyclerView(mRecyclerView);


    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (null != mMultipleBitmapFrameView) {
            mMultipleBitmapFrameView.recycle();
        }
    }

    /**
     * 顺序播放
     */
    private void onOrderCheck() {
        $(R.id.orderMode).setVisibility(View.VISIBLE);
        mMultipleBitmapFrameView.setVisibility(View.GONE);
    }

    /**
     * 同时播放
     */
    private void onSimultaneouslyCheck() {
        mMultipleBitmapFrameView.setVisibility(View.VISIBLE);
        $(R.id.orderMode).setVisibility(View.GONE);
    }

    private ItemTouchHelper mItemHelper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            if (recyclerView.getLayoutManager() instanceof GridLayoutManager) {
                final int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN |
                        ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
                final int swipeFlags = 0;
                return makeMovementFlags(dragFlags, swipeFlags);
            } else {
                final int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN |
                        ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
                final int swipeFlags = 0;
                return makeMovementFlags(dragFlags, swipeFlags);
            }
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            //得到当拖拽的viewHolder的Position
            int fromPosition = viewHolder.getAdapterPosition();
            //拿到当前拖拽到的item的viewHolder
            int toPosition = target.getAdapterPosition();
            List<SpliceGridMediaInfo> list = mSpliceHandler.getMediaList();
            if (fromPosition < toPosition) {
                for (int i = fromPosition; i < toPosition; i++) {
                    Collections.swap(list, i, i + 1);
                }
            } else {
                for (int i = fromPosition; i > toPosition; i--) {
                    Collections.swap(list, i, i - 1);
                }
            }
            mAdapter.notifyItemMoved(fromPosition, toPosition);
            return true;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

//            Log.e(TAG, "onSwiped: " + this);
        }

        @Override
        public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
            super.onSelectedChanged(viewHolder, actionState);
            if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
//                viewHolder.itemView.setBackgroundColor(Color.LTGRAY);
            }
        }

        @Override
        public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            super.clearView(recyclerView, viewHolder);
            viewHolder.itemView.setBackgroundColor(0);
            mAdapter.notifyDataSetChanged();
        }

        //重写拖拽不可用
        @Override
        public boolean isLongPressDragEnabled() {
            return false;
        }
    });


    class OrderAdapter extends BaseRVAdapter<OrderAdapter.ViewHolder> {
        private List<SpliceGridMediaInfo> mList;
        private LayoutInflater mLayoutInflater;
        private ItemTouchHelper mItemHelper;


        public OrderAdapter(List<SpliceGridMediaInfo> list, int checked, ItemTouchHelper itemHelper) {
            mList = list;
            lastCheck = checked;
            mItemHelper = itemHelper;
        }


        @Override
        public int getItemViewType(int position) {
            return 0;
        }

        @Override
        public OrderAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (null == mLayoutInflater) {
                mLayoutInflater = LayoutInflater.from(parent.getContext());
            }
            View view = mLayoutInflater.inflate(R.layout.item_media_checked_layout, parent, false);


            OrderAdapter.ViewClickListener viewClickListener = new OrderAdapter.ViewClickListener();
            view.setOnClickListener(viewClickListener);
            view.setTag(viewClickListener);
            return new OrderAdapter.ViewHolder(view);
        }


        @Override
        public void onBindViewHolder(final OrderAdapter.ViewHolder holder, final int position) {
            OrderAdapter.ViewClickListener viewClickListener = (OrderAdapter.ViewClickListener) holder.itemView.getTag();
            viewClickListener.setPosition(position);

            SpliceGridMediaInfo spliceGridMediaInfo = getItem(position);
            SimpleDraweeViewUtils.setCover(holder.mImageView, spliceGridMediaInfo.getThumbPath());
            holder.mPreviewFrameLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mItemHelper.startDrag(holder);
                    return true;
                }
            });

            holder.mPreviewFrameLayout.setAspectRatio(1f);

            holder.tvNum.setText(Integer.toString(position + 1));

            final MediaObject mediaObject = spliceGridMediaInfo.getMediaObject();
            boolean bHideMediaDuration = false;
            boolean bHideMediaType = false;
            if (bHideMediaDuration && bHideMediaType) {
                holder.buttomLayout.setVisibility(View.GONE);
            } else {
                holder.buttomLayout.setVisibility(View.VISIBLE);
                //隐藏媒体时间
//                if (bHideMediaDuration||mediaObject.getMediaType()==MediaType.MEDIA_IMAGE_TYPE) {
                holder.tvDuration.setVisibility(View.GONE);
//                } else {
//                    holder.tvDuration.setVisibility(View.VISIBLE);
//                    holder.tvDuration.setInputText(DateTimeUtils.stringForTime(mediaObject.getDuration()));
//                }

                //隐藏媒体类型
                if (bHideMediaType) {
                    holder.ivType.setVisibility(View.GONE);
                } else {
                    holder.ivType.setVisibility(View.VISIBLE);
                    if (mediaObject.getMediaType() == MediaType.MEDIA_VIDEO_TYPE) {
                        holder.ivType.setImageResource(R.drawable.edit_item_video);
                    } else {
                        Object tmp = mediaObject.getTag();
                        VideoOb videoOb;
                        if (null != tmp && tmp instanceof VideoOb && (videoOb = (VideoOb) tmp) != null && videoOb.isExtPic == 1) {
                            holder.ivType.setImageResource(R.drawable.edit_item_text);
                        } else {
                            holder.ivType.setImageResource(R.drawable.edit_item_image);
                        }
                    }
                }
            }

        }


        @Override
        public int getItemCount() {
            return mList.size();
        }

        private SpliceGridMediaInfo getItem(int position) {
            return mList.get(position);
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            View delete;
            SimpleDraweeView mImageView;
            PreviewFrameLayout mPreviewFrameLayout;
            TextView tvDuration, tvNum;
            ImageView ivType;
            View buttomLayout;

            ViewHolder(View itemView) {
                super(itemView);
                buttomLayout = itemView.findViewById(R.id.buttomLayout);
                ivType = (ImageView) itemView.findViewById(R.id.ivItemType);
                mPreviewFrameLayout = (PreviewFrameLayout) itemView.findViewById(R.id.previewFrame);
                delete = itemView.findViewById(R.id.part_delete);
                delete.setVisibility(View.GONE);
                tvDuration = (TextView) itemView.findViewById(R.id.item_duration);
                tvNum = (TextView) itemView.findViewById(R.id.tv_media_num);
                mImageView = (SimpleDraweeView) itemView.findViewById(R.id.cover);
            }
        }

        class ViewClickListener extends BaseRVAdapter.BaseItemClickListener {

            @Override
            public void onClick(View v) {
                if (lastCheck != position) {
                    lastCheck = position;
                    notifyDataSetChanged();
                    if (null != mOnItemClickListener) {
                        mOnItemClickListener.onItemClick(position, getItem(position));
                    }
                }
            }
        }
    }

}