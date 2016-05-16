package com.mobnote.golukmain.startshare;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.mobnote.golukmain.BaseActivity;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.live.GetBaiduAddress;
import com.mobnote.golukmain.newest.IDialogDealFn;
import com.mobnote.golukmain.promotion.PromotionActivity;
import com.mobnote.golukmain.promotion.PromotionSelectItem;
import com.mobnote.golukmain.thirdshare.SharePlatformAdapter;
import com.mobnote.map.LngLat;
import com.mobnote.user.UserUtils;
import com.mobnote.util.GolukFileUtils;
import com.mobnote.util.GolukUtils;

import cn.com.mobnote.eventbus.EventShortLocationFinish;
import de.greenrobot.event.EventBus;

/**
 * Created by wangli on 2016/5/10.
 */
public class VideoShareActivity extends BaseActivity implements View.OnClickListener , IDialogDealFn {

    public static final int PROMOTION_ACTIVITY_BACK = 110;
    private final int SHARE_PLATFORM_COLUMN_NUMBERS = 3;
    private RecyclerView mRcShareList;
    private SharePlatformAdapter mSharePlatformAdapter;
    private LinearLayout mLocationLayout;
    private TextView mLocationTv;
    private ImageView mLocationIv;
    private String mLocationAddress;

    /** 定位状态 0 表示定位中, 1 表示定位成功, 2表示点击定位, 3 表示用户删除了位置 */
    private int mLocationState = 0;
    /** 定位中 */
    public static final int LOCATION_STATE_ING = 0;
    /** 定位成功 */
    public static final int LOCATION_STATE_SUCCESS = 1;
    /** 定位失败 */
    public static final int LOCATION_STATE_FAILED = 2;
    /** 用户禁止使用位置 */
    public static final int LOCATION_STATE_FORBID = 3;
    private StartShareFunctionDialog mStartShareDialog = null;

    private PromotionSelectItem mPromotionSelectItem;
    private TextView mJoinActivityTV;
    PopupWindow mPopupWindow;
    boolean isPopup;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_share);
        EventBus.getDefault().register(this);
        initView();
        setupView();
    }

    public void onEventMainThread(EventShortLocationFinish event) {
        if (null == event) {
            return;
        }

        if(event.getShortAddress() != null && mLocationState != LOCATION_STATE_FORBID){
            mLocationAddress = event.getShortAddress();
            mLocationState = LOCATION_STATE_SUCCESS;
            refreshLocationUI();
        }
    }

    private void setupView() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this,SHARE_PLATFORM_COLUMN_NUMBERS);
        mRcShareList.setLayoutManager(gridLayoutManager);
        mSharePlatformAdapter = new SharePlatformAdapter(this);
        mRcShareList.setAdapter(mSharePlatformAdapter);
        mRcShareList.addItemDecoration(new SpacesItemDecoration());
        mLocationLayout.setOnClickListener(this);
        mJoinActivityTV.setOnClickListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void initView(){
        mLocationIv = (ImageView) findViewById(R.id.iv_share_location);
        mLocationTv = (TextView) findViewById(R.id.tv_share_location);
        mLocationLayout = (LinearLayout) findViewById(R.id.ll_share_location);
        mRcShareList = (RecyclerView) findViewById(R.id.rv_share_list);
        mJoinActivityTV = (TextView) findViewById(R.id.tv_share_joniActivity);
    }

    @Override
    public void onClick(View view) {
        int vId = view.getId();
        if(vId == R.id.ll_share_location){
            click_location();
        }else if(vId == R.id.tv_share_joniActivity){
            if (!UserUtils.isNetDeviceAvailable(VideoShareActivity.this)) {
                GolukUtils.showToast(VideoShareActivity.this, VideoShareActivity.this.getResources().getString(R.string.user_net_unavailable));
                return;
            }

            Intent intent = new Intent(VideoShareActivity.this, PromotionActivity.class);
            VideoShareActivity.this.startActivityForResult(intent,PROMOTION_ACTIVITY_BACK);
        }
    }

    private void click_location() {
        switch (mLocationState) {
            case LOCATION_STATE_ING:
                // 当前状态是定位中，用户点击，直接再次发起定位
                GetBaiduAddress.getInstance().searchAddress(LngLat.lat, LngLat.lng);
                break;
            case LOCATION_STATE_SUCCESS:
                // 定位成功
                // 需要弹出框让用户确认
                showDealDialog();
                break;
            case LOCATION_STATE_FAILED:
            case LOCATION_STATE_FORBID:
                // 未定位
                mLocationState = LOCATION_STATE_ING;
                refreshLocationUI();
                GetBaiduAddress.getInstance().searchAddress(LngLat.lat, LngLat.lng);
                break;
        }
    }

    private void refreshLocationUI() {
        switch (mLocationState) {
            case LOCATION_STATE_ING:
                // 改图标
                mLocationIv.setImageResource(R.drawable.share_weizhi_failed);
                mLocationTv.setText(R.string.share_str_no_location);
                break;
            case LOCATION_STATE_SUCCESS:
                // 改变图标
                mLocationIv.setImageResource(R.drawable.share_weizhi_success);
                mLocationTv.setText(mLocationAddress);
                break;
            case LOCATION_STATE_FAILED:
            case LOCATION_STATE_FORBID:
                mLocationIv.setImageResource(R.drawable.share_weizhi_failed);
                mLocationTv.setText(R.string.get_current_location);
                break;
            default:
                break;
        }
    }
    private void showDealDialog() {
        if (null != mStartShareDialog) {
            mStartShareDialog.dismiss();
            mStartShareDialog = null;
        }
        mStartShareDialog = new StartShareFunctionDialog(VideoShareActivity.this, this);
        mStartShareDialog.show();
    }

    public void showPopup() {
        View contentView = this.getLayoutInflater().inflate(R.layout.promotion_popup_hint, null);

        contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int popWidth = contentView.getMeasuredWidth();
        int popHeight = contentView.getMeasuredHeight();
        mPopupWindow = new PopupWindow(contentView, popWidth, popHeight);
        contentView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // TODO Auto-generated method stub
                mPopupWindow.dismiss();
                return false;
            }
        });

        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setBackgroundDrawable(new BitmapDrawable());

        int[] location = new int[2];
        mLocationTv.getLocationOnScreen(location);

        mPopupWindow.showAtLocation(mLocationTv, Gravity.NO_GRAVITY, location[0], location[1] - popHeight);
        isPopup = false;
        GolukFileUtils.saveBoolean(GolukFileUtils.SHOW_PROMOTION_POPUP_FLAG, false);
    }

    @Override
    public void CallBack_Del(int event, Object data) {
        if (1 == event) {
            // 重新定位
            mLocationState = LOCATION_STATE_ING;
            refreshLocationUI();
            GetBaiduAddress.getInstance().searchAddress(LngLat.lat, LngLat.lng);
        } else if (2 == event) {
            // 删除定位
            mLocationState = LOCATION_STATE_FORBID;
            refreshLocationUI();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PROMOTION_ACTIVITY_BACK) {
            mPromotionSelectItem = (PromotionSelectItem) data
                    .getSerializableExtra(PromotionActivity.PROMOTION_SELECTED_ITEM);
            if(mPromotionSelectItem != null){
                mJoinActivityTV.setText(mPromotionSelectItem.activitytitle);
            }
            return;
        }
    }

    public class SpacesItemDecoration extends RecyclerView.ItemDecoration {

        public SpacesItemDecoration() {}

        /**
         * 是否是最后一行
         * @param position
         * @return
         */
        private boolean isTheLastRow(int position){
            int count = mSharePlatformAdapter.getItemCount();
            int rowNum ;
            if(count % SHARE_PLATFORM_COLUMN_NUMBERS == 0){
                rowNum = count / SHARE_PLATFORM_COLUMN_NUMBERS;
            }else{
                rowNum = count / SHARE_PLATFORM_COLUMN_NUMBERS + 1;
            }

            if((position + 1) % SHARE_PLATFORM_COLUMN_NUMBERS == 0 && (position + 1 ) / SHARE_PLATFORM_COLUMN_NUMBERS == rowNum){
                return true;
            }else if((position + 1) % SHARE_PLATFORM_COLUMN_NUMBERS != 0 && (position + 1 ) / SHARE_PLATFORM_COLUMN_NUMBERS == (rowNum -1)){
                return true;
            }
            return false;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);
            int spanIndex = ((GridLayoutManager.LayoutParams) view.getLayoutParams()).getSpanIndex();
            int position = parent.getChildAdapterPosition(view);

            if (spanIndex == 0) {
                outRect.left = 0;
                outRect.right = 0;

            } else {//if you just have 2 span . Or you can use (staggeredGridLayoutManager.getSpanCount()-1) as last span
                outRect.left = 2;
                outRect.right = 0;
            }
            if(isTheLastRow(position)){
                outRect.bottom = 0;
            }else{
                outRect.bottom = 2;
            }

        }

        @Override
        public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
            c.drawColor(Color.parseColor("#404246"));
            c.save();
            super.onDraw(c, parent, state);
        }
    }
}
