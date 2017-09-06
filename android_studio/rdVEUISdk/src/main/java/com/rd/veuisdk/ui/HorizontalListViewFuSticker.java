package com.rd.veuisdk.ui;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rd.cache.ImageResizer;
import com.rd.lib.ui.Rotatable;
import com.rd.lib.ui.RotateImageView;
import com.rd.lib.utils.CoreUtils;
import com.rd.veuisdk.R;

/**
 * faceu挂件列表
 *
 * @author abreal
 */
public class HorizontalListViewFuSticker extends HorizontalScrollView implements
        Rotatable {
    private boolean mHasJB2 = true;// 4.3
    private int mTextColorN, mTextColorP;

    public HorizontalListViewFuSticker(Context context, AttributeSet attrs) {
        super(context, attrs);

        mLayoutInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mSaCameraFilter = new SparseArray<ListViewItem>();
        mHasJB2 = CoreUtils.hasJELLY_BEAN_MR2();
//        if (mHasJB2) {
////            mListViewItemLayoutId = R.layout.camera_filter_list_item_jben;
//            setOrientation(true);
//        } else {
        mListViewItemLayoutId = R.layout.record_fu_sticker_list_item_land;
//        }
        Resources res = context.getResources();
        mTextColorN = res.getColor(R.color.record_menu_txtcolor_n);
        mTextColorP = res.getColor(R.color.main_orange);
    }


    @SuppressWarnings("unused")
    private static final String TAG = "HorizontalListViewCamera";

    /**
     * 滤镜项目选择回调
     *
     * @author abreal
     */
    public interface OnListViewItemSelectListener {
        /**
         * @param view
         * @param nItemId
         * @return false: 确认此次选择 true: 取消此次选择
         */
        boolean onBeforeSelect(View view, int nItemId);

        /**
         * 响应选择某个项目
         *
         * @param view
         * @param nItemId
         * @param user
         */
        void onSelected(View view, int nItemId, boolean user);
    }

    /**
     * 滤镜列表项
     *
     * @author abreal
     */
    private class ListViewItem implements Rotatable {

        private RotateImageView ItemImageView;
        private View down_item_layout;
        private ImageView down_state;
        private CircleProgressBarView down_pbar;
        private View listViewItem;
        private int itemId;


        private int count = 0;


        /**
         * 获取列表项目Id
         *
         * @return
         */
        public int getItemId() {
            return itemId;
        }

        /**
         * 获取在父容器中的左侧位置
         *
         * @return
         */
        public int getLeft() {
            return listViewItem.getLeft();
        }

        /**
         * 获取在父容器中的右侧位置
         *
         * @return
         */
        public int getRight() {
            return listViewItem.getRight();
        }

        /**
         * 获取Item view
         *
         * @return
         */
        public View getItemView() {
            return listViewItem;
        }

        public ListViewItem(int nItemId, int nItemResId) {
            onImp(nItemId);
            ItemImageView.setImageResource(nItemResId);

        }

        private void onImp(int nItemId) {

            listViewItem = mLayoutInflater.inflate(mListViewItemLayoutId,
                    null);
            mLlFiltersContainer.addView(listViewItem);


            itemId = nItemId;
            ItemImageView = (RotateImageView) listViewItem
                    .findViewById(R.id.ivItemImage);
            down_pbar = (CircleProgressBarView) listViewItem
                    .findViewById(R.id.down_pbar);
            down_state = (ImageView) listViewItem
                    .findViewById(R.id.down_state);
            down_item_layout = listViewItem
                    .findViewById(R.id.down_item_layout);

            listViewItem.setClickable(true);
            listViewItem.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    onListItemClick(HorizontalListViewFuSticker.ListViewItem.this);
                }
            });


        }

        public ListViewItem(int nItemId, String localPic, ImageResizer resizer) {
            onImp(nItemId);
            resizer.loadImage(localPic, ItemImageView);
        }

        @Override
        public void setOrientation(int orientation) {

            ItemImageView.setOrientation(orientation);
        }

        /**
         * 设置项目图片资源
         *
         * @param nItemResId
         */
        public void setItemResource(int nItemResId) {
            ItemImageView.setImageResource(nItemResId);
        }

        /**
         * 设置网络图片
         *
         * @param url
         * @param fetcher
         */
        public void setItemResource(String url, ImageResizer fetcher) {
            fetcher.loadImage(url, ItemImageView);
        }


        /**
         * 选择或取消选择该项
         */
        public void enabledSelect(boolean selected) {
            ((ExtSquareImageView) ItemImageView).setChecked(selected);

        }

    }

    private LayoutInflater mLayoutInflater;// 用于加载滤镜Item
    private LinearLayout mLlFiltersContainer; // 滤镜Item父容器
    private SparseArray<ListViewItem> mSaCameraFilter;
    private ListViewItem mLastSelectedItem; // 最近选择的项目
    private OnListViewItemSelectListener mOnSelectListener;
    private int mCurrentItemId = -1;// 当前选择项目
    private boolean mRepeatSelection = false; // 重复选择
    int mListViewItemLayoutId = R.layout.camera_filter_list_item;
    private boolean mEnabledCheckFastRepeat = false;

    /**
     * 获取是否已启用检测快速重复点击
     *
     * @return
     */
    public boolean CheckFastRepeatEanbled() {
        return mEnabledCheckFastRepeat;
    }

    /**
     * 设置是否启用检测快速重复点击
     *
     * @param bEnabled
     */
    public void setCheckFastRepeat(boolean bEnabled) {
        this.mEnabledCheckFastRepeat = bEnabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        ListViewItem lvi;
        for (int nTmp = 0; nTmp < mSaCameraFilter.size(); nTmp++) {
            lvi = mSaCameraFilter.valueAt(nTmp);
            lvi.getItemView().setEnabled(enabled);
        }
        super.setEnabled(enabled);
    }

    /**
     * 设置单个项目资源Id
     *
     * @param nResId
     */
    public void setListViewItemLayoutId(int nResId) {
        mListViewItemLayoutId = nResId;
    }

    /**
     * 可进行重复选择
     *
     * @param value
     */
    public void setRepeatSelection(boolean value) {
        mRepeatSelection = value;
    }

    @Override
    protected void onFinishInflate() {
        mLlFiltersContainer = (LinearLayout) this
                .findViewById(R.id.llfiltersContainer);
    }

    public void setfiltersContainer(int nid) {
        mLlFiltersContainer = (LinearLayout) this.findViewById(nid);
    }

    /**
     * 删除所有滤镜项目
     */
    public void removeAllListItem() {
        mLlFiltersContainer.removeAllViews();
        mSaCameraFilter.clear();
    }

    /**
     * onDestory
     */
    public void recycle() {
        removeAllListItem();
        mLlFiltersContainer = null;
        mSaCameraFilter = null;
        mLayoutInflater = null;
        mLastSelectedItem = null;
        mOnSelectListener = null;

    }

    public void removeItem(int id) {
        ListViewItem item = mSaCameraFilter.get(id, null);
        if (null != item) {
            mLlFiltersContainer.removeViewInLayout(item.listViewItem);
        }
    }

    private boolean useFaceu = false;

    /**
     * 是否加载网络faceu资源
     *
     * @param isWeb
     */
    public void useWebFace(boolean isWeb) {
        useFaceu = isWeb;
    }

    /**
     * 添加项目
     *
     * @param nItemId    滤镜关联在的图片资源Id
     * @param nItemResId 滤镜关联在的图片资源Id
     */
    public void addListItem(int nItemId, int nItemResId) {

        ListViewItem item = mSaCameraFilter.get(nItemId);
        if (null == item) {
            item = new ListViewItem(nItemId, nItemResId);
            mSaCameraFilter.put(nItemId, item);

        } else {
            item.setItemResource(nItemResId);

        }
    }

    public void addListItem(int nItemId, String picLocal,
                            ImageResizer fetcher) {
        ListViewItem item = mSaCameraFilter.get(nItemId);
        if (null == item) {
            item = new ListViewItem(nItemId, picLocal, fetcher);
            mSaCameraFilter.put(nItemId, item);

        } else {
            item.setItemResource(picLocal, fetcher);

        }
    }

    /**
     * 选择列表项
     *
     * @param nItemId
     */
    public void selectListItem(int nItemId) {
        selectListItem(nItemId, false);
    }

    /**
     * 选择列表项(支持是否组件内触发)
     *
     * @param nItemId
     * @param user
     */
    public void selectListItem(int nItemId, boolean user) {
        ListViewItem clickItem = mSaCameraFilter.get(nItemId);
        if (null != clickItem && isEnabled()) {
            boolean bRepeatClick = false; // 重复选定
            if (null != mLastSelectedItem) {
                bRepeatClick = mLastSelectedItem.getItemId() == nItemId;
                mLastSelectedItem.enabledSelect(false);
            }
            clickItem.enabledSelect(true);
            if (null != mOnSelectListener
                    && (!bRepeatClick || (bRepeatClick && mRepeatSelection))) {
                if (!mOnSelectListener.onBeforeSelect(
                        mLastSelectedItem != null ? mLastSelectedItem
                                .getItemView() : null,
                        mLastSelectedItem != null ? mLastSelectedItem
                                .getItemId() : -1)) {
                    mOnSelectListener.onSelected(clickItem.getItemView(),
                            nItemId, user);

                }
            }
            mCurrentItemId = nItemId;
            mLastSelectedItem = clickItem;
            int nCurrentScrollX = this.getScrollX();
            int nScrollLeft = clickItem.getLeft()
                    - mLlFiltersContainer.getPaddingLeft();
            int nScrollRight = clickItem.getRight() - this.getWidth()
                    + mLlFiltersContainer.getPaddingRight();
            if (nScrollLeft < nCurrentScrollX) {
                this.smoothScrollTo(nScrollLeft, this.getScrollY());
            } else if (nScrollRight > nCurrentScrollX) {
                this.smoothScrollTo(nScrollRight, this.getScrollY());
            }
        }
    }

    /**
     * 清除上一个被选中的项
     */
    public void clearLastSelectedItem() {
        mLastSelectedItem = null;
        mCurrentItemId = -1;
    }

    /**
     * 获取当前项Id
     *
     * @return
     */
    public int getCurrentItemId() {
        return mCurrentItemId;
    }


    /**
     * 获取列表项数量
     *
     * @return
     */
    public int getItemsCount() {
        return mSaCameraFilter.size();
    }

    @Override
    public void setOrientation(int orientation) {
        for (int nTmp = 0; nTmp < mSaCameraFilter.size(); nTmp++) {
            mSaCameraFilter.valueAt(nTmp).setOrientation(orientation);
        }
    }

    /**
     * 设置选择某个滤镜项后事件回调
     */
    public void setListItemSelectListener(OnListViewItemSelectListener listener) {
        mOnSelectListener = listener;
    }

    /**
     * 响应滤镜项Click事件
     *
     * @param clickItem
     */
    private void onListItemClick(ListViewItem clickItem) {
        if (isFastRepeatClick() && mEnabledCheckFastRepeat) {
            return;
        }
        selectListItem(clickItem.getItemId(), true);
    }

    private long lastClickTime;

    private boolean isFastRepeatClick() {
        long time = System.currentTimeMillis();
        long timeD = time - lastClickTime;
        if (0 < timeD && timeD < 1500) {
            return true;
        }
        lastClickTime = time;
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // 屏蔽多点
        if (ev.getPointerCount() > 1) {
            return true;
        } else {
            return super.onInterceptTouchEvent(ev);
        }
    }

    /**
     * 判断是否需要下载按钮
     * @param nItemId
     * @param exit
     */
    public void isExit(int nItemId, boolean exit) {
        ListViewItem item = mSaCameraFilter.get(nItemId);
        if (null != item) {
            if (exit) {
                item.down_item_layout.setVisibility(View.GONE);
            } else {
                item.down_item_layout.setVisibility(View.VISIBLE);
                item.down_state.setVisibility(View.VISIBLE);
                item.down_pbar.setVisibility(View.GONE);
                item.down_pbar.setProgress(0);
            }
        }

    }

    public void setdownStart(int nItemId) {
        ListViewItem item = mSaCameraFilter.get(nItemId);
        if (null != item) {
            item.down_item_layout.setVisibility(View.VISIBLE);
            item.down_state.setVisibility(View.GONE);
            item.down_pbar.setVisibility(View.VISIBLE);
            item.down_pbar.setProgress(0);
        }
    }

    public void setDownProgress(int nItemId, int progress) {
        ListViewItem item = mSaCameraFilter.get(nItemId);
        if (null != item) {
            item.down_item_layout.setVisibility(View.VISIBLE);
            item.down_state.setVisibility(View.GONE);
            item.down_pbar.setVisibility(View.VISIBLE);
            item.down_pbar.setProgress(progress);
        }
    }

    public void setDownEnd(int nItemId) {

        ListViewItem item = mSaCameraFilter.get(nItemId);
        if (null != item) {
            item.down_item_layout.setVisibility(View.GONE);
        }
    }

    public void setDownFailed(int nItemId) {
        ListViewItem item = mSaCameraFilter.get(nItemId);
        if (null != item && null != item.down_item_layout) {
            item.down_item_layout.setVisibility(View.VISIBLE);
            item.down_state.setVisibility(View.VISIBLE);
            item.down_pbar.setVisibility(View.GONE);
            item.down_pbar.setProgress(0);
        }
    }

}
