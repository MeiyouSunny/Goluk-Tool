package com.mobnote.golukmain.cluster;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mobnote.golukmain.BaseActivity;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.cluster.bean.RankingAdapter;
import com.mobnote.golukmain.cluster.bean.RankingListBean;
import com.mobnote.golukmain.cluster.bean.RankingListVideo;
import com.mobnote.golukmain.cluster.bean.VideoListBean;
import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.msg.SystemMsgAdapter;
import com.mobnote.golukmain.thirdshare.SharePlatformUtil;
import com.mobnote.golukmain.videosuqare.RTPullListView;
import com.mobnote.golukmain.videosuqare.VideoSquareInfo;
import com.mobnote.util.GolukUtils;

import java.util.List;

import cn.com.mobnote.module.page.IPageNotifyFn;

public class RankingActivity extends BaseActivity implements IRequestResultListener, View.OnClickListener{

    private RTPullListView mRTPullListView = null;

    private RankingAdapter mRrankingAdapter;

    /** 保存列表一个显示项索引 */
    private int mWonderfulFirstVisible;
    /** 保存列表显示item个数 */
    private int mWonderfulVisibleCount;

    public  List<RankingListVideo> mRandkingList = null;

    /** 0:首次和下拉   1：上拉加载更多 **/
    private int mRequestType = 0;

    /** 是否还有下页数据 **/
    private boolean mIsHaveData = false;

    /** 返回按钮 */
    private ImageButton mBackbtn;

    private String mActivityid;

    /**没有数据时listView显示的图片**/
    private RelativeLayout mEmpty = null;
    private ImageView mEmptyImg = null;
    private TextView  mEmptyTxt = null;

    /**时间戳**/
    private String mTimestamp = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ranking_list);
        Intent intent = this.getIntent();
        mActivityid = intent.getStringExtra("activityid");
        initView();
        initListener();
        initData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getRankingData("0","");//首次进入请求数据
    }

    private void initListener(){
        mBackbtn.setOnClickListener(this);

        mRTPullListView.setonRefreshListener(new RTPullListView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // 下拉刷新个人中心所有数据
                mRequestType = 0;
                getRankingData("0",mTimestamp);// 请求数据
            }
        });

        mRTPullListView.setOnRTScrollListener(new RTPullListView.OnRTScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView arg0, int scrollState) {
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {

                    if (mRTPullListView.getAdapter().getCount() == (mWonderfulFirstVisible + mWonderfulVisibleCount)) {// 推荐
                            if (mRandkingList != null && mRandkingList.size() > 0 && mIsHaveData) {// 加载更多视频数据

                                if(GolukUtils.isNetworkConnected(RankingActivity.this) == false){
                                    mRTPullListView.removeFooterView(1);
                                    GolukUtils.showToast(RankingActivity.this, RankingActivity.this.getResources().getString(R.string.user_net_unavailable));
                                    return;
                                }
                                mRTPullListView.addFooterView(1);
                                mRequestType = 1;
                                getRankingData( "2", mTimestamp);
                            }
                    }
                }
            }

            @Override
            public void onScroll(AbsListView arg0, int firstVisibleItem, int visibleItemCount, int arg3) {
                mWonderfulFirstVisible = firstVisibleItem;
                mWonderfulVisibleCount = visibleItemCount;
            }
        });

    }

    private void initView() {
        mRTPullListView = (RTPullListView) findViewById(R.id.ranking_list);
        mBackbtn = (ImageButton) findViewById(R.id.back_btn);
        mEmpty = (RelativeLayout) findViewById(R.id.empty);
        mEmptyImg = (ImageView) findViewById(R.id.empty_img);
        mEmptyTxt = (TextView) findViewById(R.id.empty_txt);

    }


    private void initData(){
        mRrankingAdapter = new RankingAdapter(this);
        mRTPullListView.setSelector(new ColorDrawable(Color.TRANSPARENT));
        mRTPullListView.setAdapter(mRrankingAdapter);
        mRTPullListView.firstFreshState();
//        getRankingData("0","");//首次进入请求数据
    }

    private void getRankingData(String operation,String timestamp){
        RankingBeanRequest rbr = new RankingBeanRequest(IPageNotifyFn.PageType_RankingList,this);
        rbr.get(mActivityid,operation,timestamp,"20");
    }


    @Override
    public void onLoadComplete(int requestType, Object result) {
        if (requestType == IPageNotifyFn.PageType_RankingList){
            mRTPullListView.removeFooterView(1);
            mRTPullListView.removeFooterView(2);
            RankingListBean rlb = (RankingListBean) result;
            if(rlb!= null && rlb.success){
                if (mRequestType == 0){//下拉和首次
                    if(rlb.data.videolist != null  && rlb.data.videolist.size() > 0){
                        if(mRandkingList !=null && mRandkingList.size() > 0){
                            mRandkingList.clear();
                        }
                        mRandkingList = rlb.data.videolist;
                        mRrankingAdapter.setData(mRandkingList);
                        updateViewData(true, 0);
                        mTimestamp = mRandkingList.get(mRandkingList.size() - 1).video.addtime;

                        if(rlb.data.videolist.size() < 20){
                            mIsHaveData = false;
                            mRTPullListView.addFooterView(2);
                        }else{
                            mIsHaveData = true;
                            mRTPullListView.addFooterView(1);
                        }
                    }else{//没有数据
                        mIsHaveData = false;
                        if (mRandkingList != null && mRandkingList.size() > 0){
                            //说明之前有数据，不作任何处理
                            return;
                        }else{
                            mEmptyImg.setVisibility(View.GONE);
                            mEmptyTxt.setText(this.getResources().getString(R.string.msg_system_no_message));
                            updateViewData(false, 0);
                            mRTPullListView.setVisibility(View.GONE);
                            mEmpty.setVisibility(View.VISIBLE);
                        }
                    }

                }else{//上拉加载更多
                    if(rlb.data.videolist != null && rlb.data.videolist.size() >0){
                        int count = rlb.data.videolist.size();
                        mRandkingList.addAll(rlb.data.videolist);
                        mTimestamp = rlb.data.videolist.get(rlb.data.videolist.size() - 1).video.addtime;
                        mRrankingAdapter.setData(mRandkingList);
                        updateViewData(true, count);
                        if(rlb.data.videolist.size() <20){
                            mIsHaveData = false;
                            mRTPullListView.addFooterView(2);
                        }else{
                            mIsHaveData = true;
                            mRTPullListView.addFooterView(1);
                        }
                    }else{//数据为空
                        updateViewData(true, mRandkingList.size());
                        mRTPullListView.addFooterView(2);
                    }
                }
            }else{
                updateViewData(false,0);
                GolukUtils.showToast(this, this.getResources().getString(R.string.network_error));
            }
        }
    }

    public void updateViewData(boolean succ, int count) {
        mRTPullListView.onRefreshComplete(GolukUtils.getCurrentFormatTime(this));
        if (succ) {
            mEmpty.setVisibility(View.GONE);
            mRTPullListView.setVisibility(View.VISIBLE);
            mRrankingAdapter.notifyDataSetChanged();
            if (count > 0) {

                this.mRTPullListView.setSelection(count);
            }
        }
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.back_btn){
            this.finish();;
        }
    }
}
