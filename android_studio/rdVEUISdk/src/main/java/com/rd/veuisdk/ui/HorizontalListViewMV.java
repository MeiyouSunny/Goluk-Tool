package com.rd.veuisdk.ui;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rd.cache.ImageResizer;
import com.rd.lib.ui.Rotatable;
import com.rd.veuisdk.R;

/**
 * 滤镜、MV、配乐列表View
 *
 * @author JIAN
 */
public class HorizontalListViewMV extends HorizontalScrollView implements
        Rotatable {

    private int mColorNormal, mColorSelected;

    public HorizontalListViewMV(Context context, AttributeSet attrs) {
        super(context, attrs);

        mLayoutInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mSaCameraFilter = new SparseArray<ListViewItem>();

        mListViewItemLayoutId = R.layout.filter_list_item;
        Resources res = context.getResources();

        mColorNormal = res.getColor(R.color.borderline_color);
        mColorSelected = res.getColor(R.color.main_orange);
//        mColorDownloaded = res.getColor(R.color.white);
    }

    @SuppressWarnings("unused")
    private static final String TAG = "HorizontalListViewMV";

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

        private ExtCircleImageView mItemImageView;
        private TextView mTvItemCaption;
        private View mListViewItem;
        private int mItemId;

        private int mCount = 0;


        /**
         * 获取列表项目Id
         *
         * @return
         */
        public int getItemId() {
            return mItemId;
        }

        /**
         * 获取在父容器中的左侧位置
         *
         * @return
         */
        public int getLeft() {
            return mListViewItem.getLeft();
        }

        /**
         * 获取在父容器中的右侧位置
         *
         * @return
         */
        public int getRight() {
            return mListViewItem.getRight();
        }

        /**
         * 获取Item view
         *
         * @return
         */
        public View getItemView() {
            return mListViewItem;
        }

        public ListViewItem(int nItemId, int nItemResId) {
            onImp(nItemId);
            mItemImageView.setImageResource(nItemResId);
        }

        private void onImp(int nItemId) {

            mListViewItem = mLayoutInflater.inflate(mListViewItemLayoutId,
                    null);
            mLlFiltersContainer.addView(mListViewItem);

            mItemId = nItemId;
            mItemImageView = (ExtCircleImageView) mListViewItem
                    .findViewById(R.id.ivItemImage);
            mItemImageView.setBorderWidth((int) getResources().getDimension(R.dimen.circlebuttonborderwidth));
            mTvItemCaption = (TextView) mListViewItem
                    .findViewById(R.id.tvItemCaption);
            mListViewItem.setClickable(true);
            mListViewItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onListItemClick(ListViewItem.this);
                }
            });
            if (mItemId == 0) {
                mItemImageView.setImageResource(R.drawable.none_filter_p);
            } else {

            }
        }

        public ListViewItem(int nItemId, String localPic, ImageResizer resizer) {
            onImp(nItemId);
            resizer.loadImage(localPic, mItemImageView);
        }

        @Override
        public void setOrientation(int orientation) {
            // if (m_selectedFrame instanceof Rotatable) {
            // ((Rotatable) m_selectedFrame).setOrientation(orientation);
            // }
            mItemImageView.setOrientation(orientation);
        }

        /**
         * 设置项目图片资源
         *
         * @param nItemResId
         */
        public void setItemResource(int nItemResId) {
            mItemImageView.setImageResource(nItemResId);
        }

        /**
         * 设置网络图片
         *
         * @param url
         * @param fetcher
         */
        public void setItemResource(String url, ImageResizer fetcher) {
            fetcher.loadImage(url, mItemImageView);
        }

        /**
         * 设置项目caption
         *
         * @param strItemCaption
         */
        public void setItemCaption(String strItemCaption) {
            if (null != mTvItemCaption) {
                mTvItemCaption.setText(strItemCaption);
            }
        }

        public String getItemCaption() {
            if (null != mTvItemCaption) {
                return mTvItemCaption.getText().toString();
            } else {
                return "";
            }
        }

        /**
         * 选择或取消选择该项
         */
        public void enabledSelect(boolean selected) {


            if (isMusic && mItemId == 0) {// 配乐2->原音开关
                mItemImageView.setChecked(false);
            } else {
                mItemImageView.setChecked(selected);
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
    private int mListViewItemLayoutId = R.layout.filter_list_item;
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
            mLlFiltersContainer.removeViewInLayout(item.mListViewItem);
        }
    }

    /**
     * 滤镜关联在的图片资源Id
     *
     * @param nItemId
     * @param nItemResId
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
     * 单项恢复到默认状态
     *
     * @param nItemId
     */
    public void resetItem(int nItemId) {
        ListViewItem clickItem = mSaCameraFilter.get(nItemId);
        if (null != clickItem) {
            clickItem.enabledSelect(false);
            clickItem.mTvItemCaption.setTextColor(mColorNormal);


        }

    }

    /**
     * 选择列表项(支持是否组件内触发)
     *
     * @param nItemId
     * @param user
     */
    public void selectListItem(int nItemId, boolean user) {
        ListViewItem clickItem = mSaCameraFilter.get(nItemId);
        boolean isjlkMusicVoice = false;// 当前选中项是否为:配乐2->原音开关
        if (null != clickItem && isEnabled()) {
            boolean bRepeatClick = false; // 重复选定
            if (null != mLastSelectedItem) {
                bRepeatClick = mLastSelectedItem.getItemId() == nItemId;
                isjlkMusicVoice = (nItemId == 0 && isMusic);
                if (isjlkMusicVoice) {

                } else {
                    mLastSelectedItem.enabledSelect(false);
                    mLastSelectedItem.mTvItemCaption.setTextColor(mColorNormal);
                }
            }
            clickItem.enabledSelect(true);
            if (null != mOnSelectListener
                    && (!bRepeatClick || (bRepeatClick && mRepeatSelection))) {
                if (!isjlkMusicVoice) {
                    mLastSelectedItem = clickItem;
                    mLastSelectedItem.mTvItemCaption.setTextColor(mColorSelected);
                }
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
     * 设置当前选中项的描述
     *
     * @param text
     */
    public void setCurrentCaption(String text) {
        setCaption(mCurrentItemId, text);
    }

    /**
     * 设置指定项的描述
     *
     * @param nItemId
     * @param text
     */
    public void setCaption(int nItemId, String text) {
        ListViewItem lvi = mSaCameraFilter.get(nItemId, null);
        if (null != lvi) {
            lvi.mTvItemCaption.setText(text);
        }
    }

    private boolean isLand = false;

    public void setIsLand(boolean mland) {
        isLand = mland;
    }

    /**
     * 更改单个Item的src图标
     *
     * @param nItemId
     * @param src
     * @param txtId
     */
    public void setItemSrc(int nItemId, int src, int txtId) {

        ListViewItem lvi = mSaCameraFilter.get(nItemId, null);
        if (null != lvi) {
            if (isLand) {
                lvi.mItemImageView.setChecked(true);
            }
            lvi.mItemImageView.setImageResource(src);
            lvi.mTvItemCaption.setText(txtId);

        }
    }

    public void setItemChecked(int nItemId) {

        ListViewItem lvi = mSaCameraFilter.get(nItemId, null);
        if (null != lvi) {
            if (isLand) {
                lvi.mItemImageView.setChecked(true);
            }

        }
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
        if (mEnabledCheckFastRepeat && isFastRepeatClick()) {
            return;
        }
        selectListItem(clickItem.getItemId(), true);
    }

    private long lastClickTime;

    private boolean isFastRepeatClick() {
        long time = System.currentTimeMillis();
        long timeD = time - lastClickTime;
        if (0 < timeD && timeD < 800) {
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
     * 设置是否需要下载
     *
     * @param nItemId
     * @param isExist
     */
    public void setDownLayout(int nItemId, boolean isExist) {
        ListViewItem item = mSaCameraFilter.get(nItemId);
        if (null != item) {
            if (!isExist) {
                item.mItemImageView.setProgress(0);
            }
            item.mTvItemCaption.setTextColor(mColorNormal);

        }
    }

    /***
     * 设置为选中状态
     * @param nItemId
     */
    public void onItemChecked(int nItemId) {
        ListViewItem item = mSaCameraFilter.get(nItemId);
        if (null != item) {
            item.mTvItemCaption.setTextColor(mColorSelected);

        }
    }


    public void setdownStart(int nItemId) {
        ListViewItem item = mSaCameraFilter.get(nItemId);
        if (null != item) {
            if (null != item.mItemImageView) {
                item.mItemImageView.setProgress(1);
            }
        }
    }

    public void setdownProgress(int nItemId, int progress) {
        ListViewItem item = mSaCameraFilter.get(nItemId);
        if (null != item && null != item.mItemImageView) {
            item.mItemImageView.setProgress(progress);
        }
    }

    public void setdownEnd(int nItemId) {

        ListViewItem item = mSaCameraFilter.get(nItemId);
        if (null != item) {
            if (null != item && null != item.mItemImageView) {
                item.mItemImageView.setProgress(100);
            }
        }
    }

    public void setdownFailed(int nItemId) {
        ListViewItem item = mSaCameraFilter.get(nItemId);
        if (null != item) {
            if (null != item.mItemImageView) {
                item.mItemImageView.setProgress(0);
            }
        }
    }

    /***
     * 下载失败
     * @param nItemId
     */
    public void setdownFailedUI(int nItemId) {
        ListViewItem item = mSaCameraFilter.get(nItemId);
        if (null != item) {
            resetItem(item.getItemId());
        }
    }

    private boolean isMusic = false;

    public void setIsMusic() {
        isMusic = true;
    }
}
