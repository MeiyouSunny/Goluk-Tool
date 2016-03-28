package cn.com.mobnote.golukmobile.following;

import java.util.ArrayList;
import java.util.List;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.BaseActivity;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.follow.FollowRequest;
import cn.com.mobnote.golukmobile.followed.FollowedListRequest;
import cn.com.mobnote.golukmobile.followed.bean.FollowedListBean;
import cn.com.mobnote.golukmobile.followed.bean.FollowedRecomUserBean;
import cn.com.mobnote.golukmobile.followed.bean.FollowedRetBean;
import cn.com.mobnote.golukmobile.following.bean.FollowingItemBean;
import cn.com.mobnote.golukmobile.http.IRequestResultListener;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.mobnote.util.GolukUtils;

public class FollowingListActivity extends BaseActivity implements IRequestResultListener, OnClickListener,OnItemClickListener{
	
	private final static String TAG = "ActivityFollowing";
	private final static String REFRESH_NORMAL = "0";
	private final static String REFRESH_PULL_DOWN = "1";
	private final static String REFRESH_PULL_UP = "2";
	
	private ImageButton mFollowinglistBackIb;
	private TextView mFollowinglistTitleTv;
	private PullToRefreshListView mFollowinglistPtrList;
	
	private List<FollowingItemBean> mFollowingList;
	private FollowingListAdapter mFollowingListAdapter;
	
	private String mCurMotion = REFRESH_NORMAL;
	private final static String PROTOCOL = "200";
	
	private String linkuid;
	private String index;
	
	FollowingListRequest request;
	
	private RelativeLayout mEmptyRL;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_followinglist_layout);
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		initView();
		
		setupView();
		
		setup();
		
	}

	private void setup() {
		// TODO Auto-generated method stub
		mFollowingList = new ArrayList<FollowingItemBean>();
		mFollowingListAdapter = new FollowingListAdapter(this,mFollowingList);
		mFollowinglistPtrList.setAdapter(mFollowingListAdapter);
		
		mFollowinglistPtrList.setMode(PullToRefreshBase.Mode.DISABLED);
		mFollowinglistPtrList.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
			@Override
			public void onPullDownToRefresh(PullToRefreshBase<ListView> pullToRefreshBase) {
				// show latest refresh time
				pullToRefreshBase.getLoadingLayoutProxy(true, false).setLastUpdatedLabel(
						FollowingListActivity.this.getString(R.string.updating) +
						GolukUtils.getCurrentFormatTime(FollowingListActivity.this));
				sendFollowingListRequest(REFRESH_PULL_DOWN, index);
				mCurMotion = REFRESH_PULL_DOWN;
			}

			@Override
			public void onPullUpToRefresh(PullToRefreshBase<ListView> pullToRefreshBase) {
				pullToRefreshBase.getLoadingLayoutProxy(false, true).setPullLabel(
						FollowingListActivity.this.getResources().getString(
						R.string.goluk_pull_to_refresh_footer_pull_label));
				sendFollowingListRequest(REFRESH_PULL_UP, index);
				mCurMotion = REFRESH_PULL_UP;
			}
		});

		sendFollowingListRequest(REFRESH_NORMAL, index);
		
	}
	
	private void sendFollowingListRequest(String op, String index) {
		String tmpOp = op;
		if(REFRESH_PULL_DOWN.equals(op)) {
			tmpOp = REFRESH_NORMAL;
		}
		
		if(null == request){
			request = new FollowingListRequest(IPageNotifyFn.PageType_Following, this);
		}
		
		GolukApplication app = GolukApplication.getInstance();
		if(null != app && app.isUserLoginSucess) {
			if(!TextUtils.isEmpty(app.mCurrentUId)) {
				request.get(PROTOCOL,linkuid,op,index,"20", app.mCurrentUId);
			}
		}
		
//		FollowedListRequest request =
//				new FollowedListRequest(IPageNotifyFn.PageType_FollowedContent, this);
//		GolukApplication app = GolukApplication.getInstance();
//		if(null != app && app.isUserLoginSucess) {
//			if(!TextUtils.isEmpty(app.mCurrentUId)) {
//				request.get(PROTOCOL, app.mCurrentUId, PAGESIZE, tmpOp, timeStamp);
//			}
//		}

//		if(!mLoadingDialog.isShowing() && REFRESH_NORMAL.equals(op)) {
//			mLoadingDialog.show();
//		}
	}

	protected void sendFollowRequest(String linkuid, String type) {
		FollowRequest request =
				new FollowRequest(IPageNotifyFn.PageType_Follow, this);
		GolukApplication app = GolukApplication.getInstance();
		if(null != app && app.isUserLoginSucess) {
			if(!TextUtils.isEmpty(app.mCurrentUId)) {
				request.get(PROTOCOL, linkuid, type, app.mCurrentUId);
			}
		}
	}

	private void setupView() {
		// TODO Auto-generated method stub
		mFollowinglistBackIb.setOnClickListener(this);
		mFollowinglistTitleTv.setText(R.string.str_usercenter_header_attention_text);
		
	}

	private void initView() {
		// TODO Auto-generated method stub
		
		mFollowinglistBackIb = (ImageButton) findViewById(R.id.ib_followinglist_back);
		mFollowinglistTitleTv = (TextView) findViewById(R.id.tv_followinglist_title);
		mFollowinglistPtrList = (PullToRefreshListView) findViewById(R.id.ptrlist_followinglist);
		
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		case R.id.ib_followinglist_back:
			FollowingListActivity.this.finish();
			break;
		}
	}
	
	private void setEmptyView() {
		if(REFRESH_NORMAL.equals(mCurMotion)) {
			mFollowinglistPtrList.setEmptyView(mEmptyRL);
			mFollowinglistPtrList.setMode(PullToRefreshBase.Mode.DISABLED);
		}
	}
	protected final static String FOLLOWD_EMPTY = "FOLLOWED_EMPTY";

	@Override
	public void onLoadComplete(int requestType, Object result) {
		// TODO Auto-generated method stub
//		mFollowinglistPtrList.onRefreshComplete();
//		if(null != mLoadingDialog) {
//			mLoadingDialog.close();
//		}

//		if(requestType == IPageNotifyFn.PageType_FollowedContent) {
//			mFollowinglistPtrList.onRefreshComplete();
//			FollowedRetBean bean = (FollowedRetBean)result;
//			if(null == bean) {
//				Toast.makeText(FollowingListActivity.this, getString(R.string.network_error), Toast.LENGTH_SHORT).show();
//				if(REFRESH_NORMAL.equals(mCurMotion) || REFRESH_PULL_DOWN.equals(mCurMotion)) {
//					setEmptyView();
//				}
//				return;
//			}
//
//			if(!bean.success) {
//				if(!TextUtils.isEmpty(bean.msg)) {
//					Toast.makeText(FollowingListActivity.this, bean.msg, Toast.LENGTH_SHORT).show();
//				}
//				setEmptyView();
//				return;
//			}
//
//			if(null == bean.data) {
//				setEmptyView();
//				return;
//			}
//
//			if("1".equals(bean.data.result)) {
//				Toast.makeText(FollowingListActivity.this, FollowingListActivity.this.getString(
//						R.string.str_server_request_arg_error), Toast.LENGTH_SHORT).show();
//				setEmptyView();
//				return;
//			}
//
//			if("2".equals(bean.data.result)) {
//				Toast.makeText(FollowingListActivity.this, FollowingListActivity.this.getString(
//						R.string.str_server_request_unknown_error), Toast.LENGTH_SHORT).show();
//				setEmptyView();
//				return;
//			}
//
//			mFollowinglistPtrList.setMode(PullToRefreshBase.Mode.BOTH);
//
//			List<FollowedListBean> followedBeanList = bean.data.list;
//
//			if(followedBeanList.size() == 0) {
//				Toast.makeText(FollowingListActivity.this, getString(
//						R.string.str_pull_refresh_listview_bottom_reach), Toast.LENGTH_SHORT).show();
//			}
//
//			FollowedListBean last = bean.data.list.get(followedBeanList.size() - 1);
//			if(null != last) {
//			
//			} else {
//				return;
//			}
//
//			List<FollowingItemBean> gotList = new ArrayList<FollowingItemBean>();
//			// Refill to common list
//			if("0".equals(bean.data.count)) {
//				if(followedBeanList.size() == 1) {
////					mFollowingList.add(new String(FOLLOWD_EMPTY));
//				
//					FollowedListBean followBean = followedBeanList.get(0);
//					if("1".equals(followBean.type)) {
//						List<FollowedRecomUserBean> userBeanList = followBean.recomuser;
//						if(null != userBeanList && userBeanList.size() > 0) {
//							int userCount = userBeanList.size();
//							for(int j = 0; j < userCount; j++) {
////								mFollowingList.add(userBeanList.get(j));
//								gotList.add(userBeanList.get(j));
//							}
//						}
//					}
//				} else {
//					// TODO: no recommend, no followed
//				}
//			} else {
//				int count = followedBeanList.size();
//				for(int i = 0; i < count; i++) {
//					FollowedListBean followBean = followedBeanList.get(i);
//					if("0".equals(followBean.type)) {
////						mFollowingList.add(followBean.followvideo);
//						gotList.add(followBean.followvideo);
//					} else {
//						List<FollowedRecomUserBean> userBeanList = followBean.recomuser;
//						if(null != userBeanList && userBeanList.size() > 0) {
//							int userCount = userBeanList.size();
//							for(int j = 0; j < userCount; j++) {
//								FollowedRecomUserBean tmpBean = userBeanList.get(j);
//								tmpBean.position = j;
////								mFollowingList.add(tmpBean);
//								gotList.add(tmpBean);
//							}
//						}
//					}
//				}
//			}
//
//			if(REFRESH_PULL_UP.equals(mCurMotion)) {
//				mFollowingList.addAll(gotList);
//				mFollowingListAdapter.notifyDataSetChanged();
//			} else if(REFRESH_NORMAL.equals(mCurMotion) || REFRESH_PULL_DOWN.equals(mCurMotion)) {
////				mOfficialMsgList.clear();
////				mOfficialMsgList.addAll(bean.data.messages);
//				mFollowingList.clear();
//				mFollowingList.addAll(gotList);
//				mFollowingListAdapter.setData(mFollowingList);
//			} else {
//			}
//
////			if(mOfficialMsgList.size() < PAGESIZE) {
////				mFollowinglistPtrList.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
////			}
//			mCurMotion = REFRESH_NORMAL;
//		}
	}
	
}
