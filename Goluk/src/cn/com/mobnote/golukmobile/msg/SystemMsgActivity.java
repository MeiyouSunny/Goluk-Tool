package cn.com.mobnote.golukmobile.msg;

import java.util.ArrayList;
import java.util.List;

import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.BaseActivity;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomLoadingDialog;
import cn.com.mobnote.golukmobile.http.IRequestResultListener;
import cn.com.mobnote.golukmobile.msg.bean.MessageBean;
import cn.com.mobnote.golukmobile.msg.bean.MessageMsgsBean;
import cn.com.mobnote.golukmobile.msg.bean.SystemMsgBenRequest;
import cn.com.mobnote.golukmobile.videosuqare.RTPullListView;
import cn.com.mobnote.golukmobile.videosuqare.RTPullListView.OnRefreshListener;
import cn.com.mobnote.manager.MessageManager;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.mobnote.util.GolukUtils;
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
						mLoadType = 1;
						if(mIsHaveData){
							mRTPullListView.addFooterView(1);
							httpPost(mUid, "2", mTimestamp);//上拉请求更多数据
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
		switch (view.getId()) {
		case R.id.back_btn:
			this.finish();
			break;
		case R.id.empty_img:
		case R.id.empty:
			if(GolukUtils.isNetworkConnected(this)){
				showProgressDialog();
				httpPost(mUid, "0", "");//首次进入请求数据
			}else{
				GolukUtils.showToast(this, this.getResources().getString(R.string.user_net_unavailable));
			}
			
			break;
		default:
			break;
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
		mRTPullListView.onRefreshComplete(GolukUtils.getCurrentFormatTime());
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
							mEmptyImg.setImageResource(R.drawable.mine_zanwuxitongxiaoxi);
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
					mEmptyImg.setImageResource(R.drawable.mine_qitadifang);
					this.fristLoadDataError();
				}
			}else{//数据返回异常
				mEmptyImg.setImageResource(R.drawable.mine_qitadifang);
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
