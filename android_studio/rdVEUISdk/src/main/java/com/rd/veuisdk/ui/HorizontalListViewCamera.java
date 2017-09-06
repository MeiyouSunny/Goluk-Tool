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
 * 滤镜列表View
 *
 * @author abreal
 */
public class HorizontalListViewCamera extends HorizontalScrollView implements
        Rotatable {
    private boolean mHasJB2 = true;// 4.3
    private int mTextColorN, mTextColorP;

    public HorizontalListViewCamera(Context context, AttributeSet attrs) {
        super(context, attrs);

        mLayoutInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mSaCameraFilter = new SparseArray<ListViewItem>();
        mHasJB2 = CoreUtils.hasJELLY_BEAN_MR2();
        setOrientation(true);
        Resources res = context.getResources();
        mTextColorN = res.getColor(R.color.record_menu_txtcolor_n);
        mTextColorP = res.getColor(R.color.main_orange);
    }

    /**
     * 录制界面横竖切换
     *
     * @param isVer
     */
    public void setOrientation(boolean isVer) {
        if (mHasJB2) {
            if (isVer) {
                mListViewItemLayoutId = R.layout.record_filter_list_item;
            } else {
                mListViewItemLayoutId = R.layout.record_filter_list_item_land;
            }
        } else {
            //4.2 以下，滤镜图标为正方形
            if (isVer) {
                mListViewItemLayoutId = R.layout.camera_filter_list_item;
            } else {
                mListViewItemLayoutId = R.layout.camera_filter_list_item_land;
            }
        }
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
        private ImageView selectedFrame;
        private TextView tvItemCaption;
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


            if (mHasJB2) {
                ItemImageView = (RotateImageView) listViewItem
                        .findViewById(R.id.ivItemImage);
                tvItemCaption = (TextView) listViewItem
                        .findViewById(R.id.tvItemCaption);
            } else {

                ItemImageView = (RotateImageView) listViewItem
                        .findViewById(R.id.ivItemImage);
                ItemImageView.setFitCenterSizeMode(false);
                selectedFrame = (ImageView) listViewItem
                        .findViewById(R.id.ivSelectedFrame);

            }
            tvItemCaption = (TextView) listViewItem
                    .findViewById(R.id.tvItemCaption);
            listViewItem.setClickable(true);
            listViewItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onListItemClick(ListViewItem.this);
                }
            });

            if (!mHasJB2) {
                selectedFrame
                        .setImageResource(R.drawable.normal_item_selected);
            }
        }

        public ListViewItem(int nItemId, String localPic, ImageResizer resizer) {
            onImp(nItemId);
            resizer.loadImage(localPic, ItemImageView);
        }

        @Override
        public void setOrientation(int orientation) {

            if (selectedFrame instanceof Rotatable) {
                ((Rotatable) selectedFrame).setOrientation(orientation);
            }
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
         * 设置项目caption
         *
         * @param strItemCaption
         */
        public void setItemCaption(String strItemCaption) {
            if (null != tvItemCaption) {
                tvItemCaption.setText(strItemCaption);
            }
        }

        public String getItemCaption() {
            if (null != tvItemCaption) {
                return tvItemCaption.getText().toString();
            } else {
                return "";
            }
        }

        /**
         * 选择或取消选择该项
         */
        public void enabledSelect(boolean selected) {
            if (mHasJB2) {
                ((ExtCircleImageView) ItemImageView).setChecked(selected);
                tvItemCaption.setTextColor(selected ? mTextColorP : mTextColorN);
            } else {
                selectedFrame.setVisibility(selected ? View.VISIBLE
                        : View.INVISIBLE);
            }
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
        addListItem(nItemId, nItemResId, "");
    }

    /**
     * 添加项目(支持标题)
     *
     * @param nItemId
     * @param nItemResId
     * @param strCaption
     */
    public void addListItem(int nItemId, int nItemResId, String strCaption) {
        ListViewItem item = mSaCameraFilter.get(nItemId);
        if (null == item) {
            item = new ListViewItem(nItemId, nItemResId);
            mSaCameraFilter.put(nItemId, item);
            item.setItemCaption(strCaption);
        } else {
            item.setItemResource(nItemResId);
            item.setItemCaption(strCaption);
        }
    }

    public void addListItem(int nItemId, String picLocal, String strCaption,
                            ImageResizer fetcher) {
        ListViewItem item = mSaCameraFilter.get(nItemId);
        if (null == item) {
            item = new ListViewItem(nItemId, picLocal, fetcher);
            mSaCameraFilter.put(nItemId, item);
            item.setItemCaption(strCaption);
        } else {
            item.setItemResource(picLocal, fetcher);
            item.setItemCaption(strCaption);
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
     * 获取当前项Captain
     *
     * @return
     */
    public String getCurrentItemCaption() {
        ListViewItem lvi = mSaCameraFilter.get(mCurrentItemId, null);
        if (null != lvi) {
            return lvi.getItemCaption();
        } else {
            return "";
        }
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


    public void setdownStart(int nItemId) {
        ListViewItem item = mSaCameraFilter.get(nItemId);
        if (null != item) {
            if (null != item.ItemImageView && mHasJB2) {
                ((ExtCircleImageView) item.ItemImageView).setProgress(0);
            }
        }
    }

    public void setDownProgress(int nItemId, int progress) {
        ListViewItem item = mSaCameraFilter.get(nItemId);
        if (null != item.ItemImageView && mHasJB2) {
            ((ExtCircleImageView) item.ItemImageView).setProgress(progress);
        }
    }

    public void setDownEnd(int nItemId) {

        ListViewItem item = mSaCameraFilter.get(nItemId);
        if (null != item) {
            if (null != item.ItemImageView && mHasJB2) {
                ((ExtCircleImageView) item.ItemImageView).setProgress(100);
            }
        }
    }

    public void setDownFailed(int nItemId) {
        ListViewItem item = mSaCameraFilter.get(nItemId);
        if (null != item) {
            if (null != item.ItemImageView && mHasJB2) {
                ((ExtCircleImageView) item.ItemImageView).setProgress(0);
            }
        }
    }
}
