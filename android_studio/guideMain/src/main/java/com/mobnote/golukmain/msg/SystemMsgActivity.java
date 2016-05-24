package com.mobnote.golukmain.msg;

import java.util.ArrayList;
import java.util.List;

import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.BaseActivity;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.carrecorder.view.CustomLoadingDialog;
import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.msg.bean.MessageBean;
import com.mobnote.golukmain.msg.bean.MessageMsgsBean;
import com.mobnote.golukmain.msg.bean.SystemMsgBenRequest;
import com.mobnote.golukmain.videosuqare.RTPullListView;
import com.mobnote.golukmain.videosuqare.RTPullListView.OnRefreshListener;
import com.mobnote.manager.MessageManager;
import com.mobnote.util.GolukUtils;

import cn.com.mobnote.module.page.IPageNotifyFn;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
public class SystemMsgActivity  extends BaseActivity implements OnClickListener,IRequestResultListener {
	
	private ImageButton mBackBtn;
	private RTPullListView mRTPullListView;
	private SystemMsgAdapter mSystemMsgAdpter;
	private int mFristItemPosition;//当前页的第一个item坐标
	private int mPageItemCount;//当前页显示的item个数
	
	
	private CustomLoadingDialog mCustomProgressDialog = null;
	
	/**返回的数据集合**/
	private List<MessageMsgsBean> mData = new ArrayList<MessageMsgsBean>();
	
	public Handler mHandler;
	private String mUid;
	
	private int mLoadType = 0; // 0:下拉刷新   1:上拉加载
	/**是否是首次加载**/
	private boolean mIsFrist = false;
	
	/** 是否还有下页数据 **/
	private boolean mIsHaveData = false;
	
	/**没有数据时listView显示的图片**/
	private RelativeLayout mEmpty = null;
	private ImageView mEmptyImg = null;
	private TextView  mEmptyTxt = null;
	/**时间戳**/
	private String mTimestamp = "";
	private final static String TYPES_SYSTEM = "[200]";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.msg_system_list);
		mUid = GolukApplication.getInstance().mCurrentUId;
		initView();//初始化view
		initListener();//初始化监听
		initData();//初始化数据
	}
	
	private void initView(){
		mBackBtn = (ImageButton) findViewById(R.id.back_btn);
		mEmpty = (RelativeLayout) findViewById(R.id.empty);
		mEmptyImg = (ImageView) findViewById(R.id.empty_img);
		mEmptyTxt = (TextView) findViewById(R.id.empty_txt);
		mRTPullListView = (RTPullListView) findViewById(R.id.msg_system_list);
	}
	
	private void httpPost(String uid,String operation, String timestamp){
		SystemMsgBenRequest systemMsgRequest = new SystemMsgBenRequest(IPageNotifyFn.PageType_SystemMsgMain, this);
		systemMsgRequest.get (uid, TYPES_SYSTEM, operation, timestamp);
	}
	
	private void initListener(){
		mBackBtn.setOnClickListener(this);
		mEmpty.setOnClickListener(this);
		mEmptyImg.setOnClickListener(this);
		//下拉刷新
		mRTPullListView.setonRefreshListener(new OnRefreshListener() {
			
			@Override
			public void onRefresh() {
				if(GolukUtils.isNetworkConnected(SystemMsgActivity.this) == false){
					mRTPullListView.onRefreshComplete(GolukUtils.getCurrentFormatTime(SystemMsgActivity.this));
					GolukUtils.showToast(SystemMsgActivity.this, SystemMsgActivity.this.getResources().getString(R.string.user_net_unavailable));
					return;
				}
				mLoadType = 0;
				mIsFrist = false;
				httpPost(mUid, "0", "");//首次进入请求数据
			}
		});
		
		//上拉加载更多
		mRTPullListView.setOnRTScrollListener(new RTPullListView.OnRTScrollListener() {
			
			@Override
			public void onScrollStateChanged(AbsListView arg0, int scrollState) {
				if(scrollState == OnScrollListener.SCROLL_STATE_IDLE){//滚动结束
					if((mFristItemPosition + mPageItemCount) == mRTPullListView.getAdapter().getCount()){
						mIsFrist = false;
						if(mIsHaveData){
							if(GolukUtils.isNetworkConnected(SystemMsgActivity.this) == false){
								mRTPullListView.removeFooterView(1);
								GolukUtils.showToast(SystemMsgActivity.this, SystemMsgActivity.this.getResources().getString(R.string.user_net_unavailable));
								return;
							}
							mRTPullListView.addFooterView(1);
							httpPost(mUid, "2", mTimestamp);//上拉请求更多数据
							mLoadType = 1;
						}
					}
				}
			}
			
			@Override
			public void onScroll(AbsListView arg0, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				mFristItemPosition = firstVisibleItem;
				mPageItemCount = visibleItemCount;
			}
		});
		
	}
	
	private void initData(){
		mSystemMsgAdpter = new SystemMsgAdapter(this,mUid);
		mRTPullListView.setSelector(new ColorDrawable(Color.TRANSPARENT));
		mRTPullListView.setAdapter(mSystemMsgAdpter);
		mRTPullListView.firstFreshState();
		mLoadType = 0;
		mIsFrist = true;
		httpPost(mUid, "0", "");//首次进入请求数据
		
	}
	
	
	@Override
	public void onClick(View view) {
		int id = view.getId();
		if (id == R.id.back_btn) {
			this.finish();
		} else if (id == R.id.empty_img
				|| id == R.id.empty) {
			if(GolukUtils.isNetworkConnected(this)){
				showProgressDialog();
				httpPost(mUid, "0", "");//首次进入请求数据
			}else{
				GolukUtils.showToast(this, this.getResources().getString(R.string.user_net_unavailable));
			}
		} else {
		}
	}
	
	@Override
	protected void onResume() {
		
		super.onResume();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		closeProgressDialog();
	}
	

	public void updateViewData(boolean succ, int count) {
		mRTPullListView.onRefreshComplete(GolukUtils.getCurrentFormatTime(this));
		if (succ) {
			mEmpty.setVisibility(View.GONE);
			mRTPullListView.setVisibility(View.VISIBLE);
			mSystemMsgAdpter.notifyDataSetChanged();
			if (count > 0) {
				
				this.mRTPullListView.setSelection(count);
			}
		}
	}
	
	@Override
	public void onLoadComplete(int requestType, Object result) {
		if(requestType == IPageNotifyFn.PageType_SystemMsgMain){
			MessageBean mb = (MessageBean) result;
			closeProgressDialog();
			if(mb != null && mb.data != null){
				if ("10001".equals(mb.data.result) || "10002".equals(mb.data.result)){
					GolukUtils.startLoginActivity(SystemMsgActivity.this);
					return;
				}
			}
			mRTPullListView.removeFooterView(1);
			mRTPullListView.removeFooterView(2);
			if(mb != null){
				if(mb.success){//返回数据正常
					MessageManager.getMessageManager().setSystemMessageCount(0);
					List<MessageMsgsBean> list = mb.data.messages;
					
					if(list != null && list.size() >= 20){
						mIsHaveData = true;
						mTimestamp = list.get(list.size() - 1).content.time;
					}else{
						mIsHaveData = false;
					}
					if(mLoadType == 0){//下拉刷新
						if(list != null && list.size() > 0){
							mData.clear();
							mData = list;
							mSystemMsgAdpter.setData(mData);
							updateViewData(true, 0);
							
							if(list.size() < 20){
								mRTPullListView.addFooterView(2);
							}else{
								mRTPullListView.addFooterView(1);
							}
						}else{//数据为空
							mEmptyImg.setVisibility(View.GONE);
							mEmptyTxt.setText(this.getResources().getString(R.string.msg_system_no_message));
							this.fristLoadDataError();
						}
					}else{//上拉加载更多
						if(list != null && list.size() >0){
							int count = mData.size();
							mData.addAll(list);
							mSystemMsgAdpter.setData(mData);
							updateViewData(true, count);
							if(list.size() <20){
								mRTPullListView.addFooterView(2);
							}else{
								mRTPullListView.addFooterView(1);
							}
						}else{//数据为空
							updateViewData(true, mData.size());
							mRTPullListView.addFooterView(2);
						}
					}
					
				}else{//数据返回异常
					mEmptyImg.setVisibility(View.VISIBLE);
					mEmptyImg.setImageResource(R.drawable.mine_qitadifang);
					mEmptyTxt.setText(this.getResources().getString(R.string.msg_system_connect_error));
					this.fristLoadDataError();
				}
			}else{//数据返回异常
				mEmptyImg.setVisibility(View.VISIBLE);
				mEmptyImg.setImageResource(R.drawable.mine_qitadifang);
				mEmptyTxt.setText(this.getResources().getString(R.string.msg_system_connect_error));
				this.fristLoadDataError();
			}
		}
	}
	
	public void  fristLoadDataError(){
		mIsHaveData = false;
		if(mIsFrist){//首次加载失败
			updateViewData(false, 0);
			mRTPullListView.setVisibility(View.GONE);
			mEmpty.setVisibility(View.VISIBLE);
		}else{
			if(mLoadType == 0){
				updateViewData(false, 0);
			}
			GolukUtils.showToast(this, this.getResources().getString(R.string.str_network_unusual));
		}
	}
	
	public void showProgressDialog() {
		if (null == mCustomProgressDialog) {
			mCustomProgressDialog = new CustomLoadingDialog(this, null);
		}
		if (!mCustomProgressDialog.isShowing()) {
			mCustomProgressDialog.show();
		}

	}

	public void closeProgressDialog() {
		if (null != mCustomProgressDialog) {
			mCustomProgressDialog.close();
		}
	}
	
}
