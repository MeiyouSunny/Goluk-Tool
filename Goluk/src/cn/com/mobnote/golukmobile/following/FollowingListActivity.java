package cn.com.mobnote.golukmobile.following;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
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
import cn.com.mobnote.golukmobile.carrecorder.view.CustomDialog;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomDialog.OnRightClickListener;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomLoadingDialog;
import cn.com.mobnote.golukmobile.follow.FollowRequest;
import cn.com.mobnote.golukmobile.follow.bean.FollowRetBean;
import cn.com.mobnote.golukmobile.following.bean.FollowingItemBean;
import cn.com.mobnote.golukmobile.following.bean.FollowingRetBean;
import cn.com.mobnote.golukmobile.http.IRequestResultListener;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.mobnote.util.GolukUtils;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

/**
 * 关注的用户列表
 * @author leege100
 *
 */
public class FollowingListActivity extends BaseActivity implements IRequestResultListener, OnClickListener,OnItemClickListener{
	
	private final static String TAG = "ActivityFollowingList";
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
	
	private String mLinkuid;
	
	private RelativeLayout mEmptyRl;
	private TextView mEmptyTv;
	
	private CustomLoadingDialog mLoadingDialog;
	private CustomDialog mCustomDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_followinglist_layout);
		
		mLinkuid = getIntent().getStringExtra("linkuid");
		
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
				sendFollowingListRequest(REFRESH_PULL_DOWN);
				
			}

			@Override
			public void onPullUpToRefresh(PullToRefreshBase<ListView> pullToRefreshBase) {
				pullToRefreshBase.getLoadingLayoutProxy(false, true).setPullLabel(
						FollowingListActivity.this.getResources().getString(
						R.string.goluk_pull_to_refresh_footer_pull_label));
				sendFollowingListRequest(REFRESH_PULL_UP);
				
			}
		});

		sendFollowingListRequest(REFRESH_NORMAL);
		
	}
	
	protected void follow(final String linkuid,final String type){
		
		if("1".equals(type)){
			sendFollowRequest( linkuid,  type);
			return;
		}
		
		if(mCustomDialog==null){
			mCustomDialog = new CustomDialog(this);
		}
		
		mCustomDialog.setMessage(this.getString(R.string.str_confirm_cancel_follow), Gravity.CENTER);
		mCustomDialog.setLeftButton(this.getString(R.string.dialog_str_cancel), null);
		mCustomDialog.setRightButton(this.getString(R.string.str_button_ok), new OnRightClickListener() {

			@Override
			public void onClickListener() {
				// TODO Auto-generated method stub
				mCustomDialog.dismiss();
				sendFollowRequest( linkuid,  type);
			}
			
		});
		mCustomDialog.show();
		
	}
	
	private void sendFollowingListRequest(String op) {
		
		mCurMotion = op;
		
		String index = null;
		String tmpOp = op;
		if(REFRESH_PULL_DOWN.equals(op)) {
			tmpOp = REFRESH_NORMAL;
		}
		
		if(REFRESH_PULL_UP.equals(mCurMotion)) {
			
			if(mFollowingList != null && mFollowingList.size()>0){
				index = mFollowingList.get(mFollowingList.size()-1).index;
			}
		} else if(REFRESH_NORMAL.equals(mCurMotion) || REFRESH_PULL_DOWN.equals(mCurMotion)) {
			if(mFollowingList != null &&  mFollowingList.size()>0){
				index = mFollowingList.get(0).index;
			}
		}
		
		FollowingListRequest request = new FollowingListRequest(IPageNotifyFn.PageType_Following, this);
		
		GolukApplication app = GolukApplication.getInstance();
		
		if(null != app && app.isUserLoginSucess&&!TextUtils.isEmpty(app.mCurrentUId)) {
			request.get(PROTOCOL,mLinkuid,tmpOp,index,"20", app.mCurrentUId);
		}else{
			request.get(PROTOCOL,mLinkuid,tmpOp,index,"20", null);
		}
		
		if(!mLoadingDialog.isShowing() && REFRESH_NORMAL.equals(op)) {
			mLoadingDialog.show();
		}
	}

	private void sendFollowRequest(String linkuid, String type) {
		
		FollowRequest request = new FollowRequest(IPageNotifyFn.PageType_Follow, this);
		GolukApplication app = GolukApplication.getInstance();
		if(null != app && app.isUserLoginSucess) {
			if(!TextUtils.isEmpty(app.mCurrentUId)) {
				if(!mLoadingDialog.isShowing()) {
					mLoadingDialog.show();
				}
				request.get(PROTOCOL, linkuid, type, app.mCurrentUId);
			}
		}
	}

	private void setupView() {
		// TODO Auto-generated method stub
		mFollowinglistBackIb.setOnClickListener(this);
		mFollowinglistTitleTv.setText(R.string.str_follow);
		
		mEmptyRl.setOnClickListener(this);
		
	}

	private void initView() {
		// TODO Auto-generated method stub
		
		mFollowinglistBackIb = (ImageButton) findViewById(R.id.ib_followinglist_back);
		mFollowinglistTitleTv = (TextView) findViewById(R.id.tv_followinglist_title);
		mFollowinglistPtrList = (PullToRefreshListView) findViewById(R.id.ptrlist_followinglist);
		
		mEmptyRl = (RelativeLayout) findViewById(R.id.ry_followinglist_refresh);
		mEmptyTv = (TextView) findViewById(R.id.tv_followinglist_empty);
		
		mLoadingDialog = new CustomLoadingDialog(this, null);
		
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
		if(mLoadingDialog!=null&&mLoadingDialog.isShowing()){
			mLoadingDialog.close();
		}
		
		mLoadingDialog = null;
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
		case R.id.ry_followinglist_refresh:
			sendFollowingListRequest(REFRESH_NORMAL);
			break;
		}
		
	}
	
	private void setEmptyView(String emptyInfo) {
		if(REFRESH_NORMAL.equals(mCurMotion)) {
			mFollowinglistPtrList.setEmptyView(mEmptyRl);
			mFollowinglistPtrList.setMode(PullToRefreshBase.Mode.DISABLED);
			mEmptyTv.setText(emptyInfo);
		}
	}
	protected final static String FOLLOWD_EMPTY = "FOLLOWED_EMPTY";

	@Override
	public void onLoadComplete(int requestType, Object result) {
		
		if(mLoadingDialog!=null&&mLoadingDialog.isShowing()){
			mLoadingDialog.close();
		}

		if(requestType == IPageNotifyFn.PageType_Following) {
			
			mFollowinglistPtrList.onRefreshComplete();
			FollowingRetBean bean = (FollowingRetBean)result;
			
			if(null == bean) {
				Toast.makeText(FollowingListActivity.this, getString(R.string.network_error), Toast.LENGTH_SHORT).show();
				if(REFRESH_NORMAL.equals(mCurMotion) || REFRESH_PULL_DOWN.equals(mCurMotion)) {
					setEmptyView(getString(R.string.msg_system_connect_error));
				}
				return;
			}

			if(null == bean.data) {
				setEmptyView(getString(R.string.no_following_tips));
				return;
			}

			mFollowinglistPtrList.setMode(PullToRefreshBase.Mode.BOTH);

			List<FollowingItemBean> followingBeanList = bean.data.userlist;
			
			if(null == followingBeanList || followingBeanList.size() == 0) {
				
				if(REFRESH_PULL_UP.equals(mCurMotion)) {
					
					Toast.makeText(FollowingListActivity.this, getString(
							R.string.str_pull_refresh_listview_bottom_reach), Toast.LENGTH_SHORT).show();
					mCurMotion = REFRESH_NORMAL;
					return;
				}else if(REFRESH_NORMAL.equals(mCurMotion) || REFRESH_PULL_DOWN.equals(mCurMotion)){
					setEmptyView(getString(R.string.no_following_tips));
					return;
				}	
			}else{
				if(REFRESH_PULL_UP.equals(mCurMotion)) {
					mFollowingList.addAll(followingBeanList);
					mFollowingListAdapter.notifyDataSetChanged();
				} else if(REFRESH_NORMAL.equals(mCurMotion) || REFRESH_PULL_DOWN.equals(mCurMotion)) {
					mFollowingList.clear();
					mFollowingList.addAll(followingBeanList);
					mFollowingListAdapter.setData(mFollowingList);
				} else {
				}
			}
			mCurMotion = REFRESH_NORMAL;
		}else if(requestType == IPageNotifyFn.PageType_Follow) {//关注
			
			FollowRetBean bean = (FollowRetBean)result;
			if(null != bean) {
				
				if(bean.code != 0) {
					Toast.makeText(FollowingListActivity.this, bean.msg, Toast.LENGTH_SHORT).show();
					return;
				}
				// User link uid to find the changed recommend user item status
				int i = findLinkUserItem(bean.data.linkuid);
				if(i >=0 && i < mFollowingList.size()) {
					FollowingItemBean tempBean = mFollowingList.get(i);
					tempBean.link = bean.data.link;
					mFollowingList.set(i, tempBean);
					mFollowingListAdapter.notifyDataSetChanged();
					
					if(mFollowingList==null||mFollowingList.size()<=0){
						setEmptyView(getString(R.string.no_following_tips));
					}
					
					if(bean.data!=null){
						if(bean.data.link == FollowingConfig.LINK_TYPE_FAN_ONLY
								||bean.data.link == FollowingConfig.LINK_TYPE_UNLINK){
							Toast.makeText(FollowingListActivity.this,
									getResources().getString(R.string.str_usercenter_attention_cancle_ok),Toast.LENGTH_SHORT).show();
						}else if(bean.data.link == FollowingConfig.LINK_TYPE_FOLLOW_EACHOTHER){
							Toast.makeText(FollowingListActivity.this,
									getResources().getString(R.string.str_usercenter_attention_ok),Toast.LENGTH_SHORT).show();
						}
					}
				}
			} else {
				// Toast for operation failed
				Toast.makeText(FollowingListActivity.this, getString(R.string.network_error), Toast.LENGTH_SHORT).show();
				
			}
			
			if(null!=mLoadingDialog&&mLoadingDialog.isShowing()){
				mLoadingDialog.close();
			}
			
		} 
	}
	
	private int findLinkUserItem(String linkuid) {
		if(null == mFollowingList || mFollowingList.size() == 0 || TextUtils.isEmpty(linkuid)) {
			return -1;
		}

		int size = mFollowingList.size();
		for(int i = 0; i < size; i++) {
			FollowingItemBean bean = mFollowingList.get(i);
			if(null != bean &&bean.uid.equals(linkuid)) {
			
				return i;
			}
		}

		return -1;
	}
}
