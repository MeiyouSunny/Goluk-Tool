package com.mobnote.golukmain.fan;

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
import cn.com.mobnote.module.page.IPageNotifyFn;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.BaseActivity;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.carrecorder.view.CustomDialog;
import com.mobnote.golukmain.carrecorder.view.CustomLoadingDialog;
import com.mobnote.golukmain.carrecorder.view.CustomDialog.OnRightClickListener;
import com.mobnote.golukmain.follow.FollowRequest;
import com.mobnote.golukmain.follow.bean.FollowRetBean;
import com.mobnote.golukmain.following.FollowingConfig;
import com.mobnote.golukmain.following.bean.FollowingRetBean;
import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.userbase.bean.SimpleUserItemBean;
import com.mobnote.util.GolukUtils;

/**
 * 关注的用户列表
 * @author leege100
 *
 */
public class FanListActivity extends BaseActivity implements IRequestResultListener, OnClickListener,OnItemClickListener{
	
//	private final static String TAG = "ActivityFollowingList";
	private final static String REFRESH_NORMAL = "0";
	private final static String REFRESH_PULL_DOWN = "1";
	private final static String REFRESH_PULL_UP = "2";
	
	private ImageButton mFanlistBackIb;
	private TextView mFanlistTitleTv;
	private PullToRefreshListView mFanlistPtrList;
	
	private List<SimpleUserItemBean> mFollowingList;
	private FanListAdapter mFansListAdapter;
	
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
		setContentView(R.layout.activity_fan_list);
		
		mLinkuid = getIntent().getStringExtra("linkuid");
		
		initView();
		
		setupView();
		
		setup();
		
	}
	

	private void setup() {
		// TODO Auto-generated method stub
		
		mFollowingList = new ArrayList<SimpleUserItemBean>();
		mFansListAdapter = new FanListAdapter(this,mFollowingList);
		mFanlistPtrList.setAdapter(mFansListAdapter);
		
		mFanlistPtrList.setMode(PullToRefreshBase.Mode.DISABLED);
		mFanlistPtrList.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
			@Override
			public void onPullDownToRefresh(PullToRefreshBase<ListView> pullToRefreshBase) {
				// show latest refresh time
				pullToRefreshBase.getLoadingLayoutProxy(true, false).setLastUpdatedLabel(
						FanListActivity.this.getString(R.string.updating) +
						GolukUtils.getCurrentFormatTime(FanListActivity.this));
				sendFollowingListRequest(REFRESH_PULL_DOWN);
				
			}

			@Override
			public void onPullUpToRefresh(PullToRefreshBase<ListView> pullToRefreshBase) {
				pullToRefreshBase.getLoadingLayoutProxy(false, true).setPullLabel(
						FanListActivity.this.getResources().getString(
						R.string.goluk_pull_to_refresh_footer_pull_label));
				sendFollowingListRequest(REFRESH_PULL_UP);
				
			}
		});

		sendFollowingListRequest(REFRESH_NORMAL);
		
	}
	
	protected void follow(final String linkuid,final String type){
		if (GolukApplication.getInstance().isUserLoginSucess) {

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
		}else{
			GolukUtils.startLoginActivity(this);
		}
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
		
		FansListRequest request = new FansListRequest(IPageNotifyFn.PageType_Fans, this);
		
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
		mFanlistBackIb.setOnClickListener(this);
		mFanlistTitleTv.setText(R.string.str_fans);
		
		mEmptyRl.setOnClickListener(this);
		
	}

	private void initView() {
		// TODO Auto-generated method stub
		
		mFanlistBackIb = (ImageButton) findViewById(R.id.ib_fanlist_back);
		mFanlistTitleTv = (TextView) findViewById(R.id.tv_fanlist_title);
		mFanlistPtrList = (PullToRefreshListView) findViewById(R.id.ptrlist_fanlist);
		
		mEmptyRl = (RelativeLayout) findViewById(R.id.ry_fanlist_refresh);
		mEmptyTv = (TextView) findViewById(R.id.tv_fanlist_empty);
		
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
		int id = v.getId();
		if (id == R.id.ib_fanlist_back) {
			FanListActivity.this.finish();
		} else if (id == R.id.ry_fanlist_refresh) {
			sendFollowingListRequest(REFRESH_NORMAL);
		}
		
	}
	
	private void setEmptyView(String emptyInfo) {
		if(REFRESH_NORMAL.equals(mCurMotion)) {
			mFanlistPtrList.setAdapter(null);
			mFanlistPtrList.setEmptyView(mEmptyRl);
			mFanlistPtrList.setMode(PullToRefreshBase.Mode.DISABLED);
			mEmptyTv.setText(emptyInfo);
		}
	}
	protected final static String FOLLOWD_EMPTY = "FOLLOWED_EMPTY";

	@Override
	public void onLoadComplete(int requestType, Object result) {
		
		if(mLoadingDialog!=null&&mLoadingDialog.isShowing()){
			mLoadingDialog.close();
		}

		if(requestType == IPageNotifyFn.PageType_Fans) {
			
			mFanlistPtrList.onRefreshComplete();
			FollowingRetBean bean = (FollowingRetBean)result;
			
			if(null == bean) {
				Toast.makeText(FanListActivity.this, getString(R.string.network_error), Toast.LENGTH_SHORT).show();
				if(REFRESH_NORMAL.equals(mCurMotion) || REFRESH_PULL_DOWN.equals(mCurMotion)) {
					setEmptyView(getString(R.string.msg_system_connect_error));
				}
				return;
			}

			if(null == bean.data) {
				setEmptyView(getString(R.string.no_fans_tips));
				return;
			}

			mFanlistPtrList.setMode(PullToRefreshBase.Mode.BOTH);

			List<SimpleUserItemBean> followingBeanList = bean.data.userlist;
			
			if(null == followingBeanList || followingBeanList.size() == 0) {
				
				if(REFRESH_PULL_UP.equals(mCurMotion)) {
					
					Toast.makeText(FanListActivity.this, getString(
							R.string.str_pull_refresh_listview_bottom_reach), Toast.LENGTH_SHORT).show();
					mCurMotion = REFRESH_NORMAL;
					return;
				}else if(REFRESH_NORMAL.equals(mCurMotion) || REFRESH_PULL_DOWN.equals(mCurMotion)){
					mCurMotion = REFRESH_NORMAL;
					setEmptyView(getString(R.string.no_fans_tips));
					return;
				}	
			}else{
				if(REFRESH_PULL_UP.equals(mCurMotion)) {
					mFollowingList.addAll(followingBeanList);
					mFansListAdapter.notifyDataSetChanged();
				} else if(REFRESH_NORMAL.equals(mCurMotion) || REFRESH_PULL_DOWN.equals(mCurMotion)) {
					mFollowingList.clear();
					mFollowingList.addAll(followingBeanList);
					//mFansListAdapter.setData(mFollowingList);
					mFanlistPtrList.setAdapter(mFansListAdapter);
				} else {
				}
			}
			mCurMotion = REFRESH_NORMAL;
		}else if(requestType == IPageNotifyFn.PageType_Follow) {//关注

			if(null!=mLoadingDialog&&mLoadingDialog.isShowing()){
				mLoadingDialog.close();
			}
			FollowRetBean bean = (FollowRetBean)result;
			if(null != bean) {

				if(bean.code != 0) {
                    if(10001 ==bean.code || 10002 == bean.code){
                        GolukUtils.startLoginActivity(FanListActivity.this);
                    }else if(bean.code == 12011){
                        Toast.makeText(FanListActivity.this, getString(R.string.follow_operation_limit_total), Toast.LENGTH_SHORT).show();
                    }else if(bean.code == 12016){
                        Toast.makeText(FanListActivity.this, getString(R.string.follow_operation_limit_day), Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(FanListActivity.this, bean.msg, Toast.LENGTH_SHORT).show();
                    }
					return;
				}
				if(bean.data == null){
					return;
				}

				// User link uid to find the changed recommend user item status
				int i = findLinkUserItem(bean.data.linkuid);
				if(i >=0 && i < mFollowingList.size()) {
					SimpleUserItemBean tempBean = mFollowingList.get(i);
					tempBean.link = bean.data.link;

					if(bean.data.link == FollowingConfig.LINK_TYPE_FOLLOW_EACHOTHER || bean.data.link == FollowingConfig.LINK_TYPE_FOLLOW_ONLY){
						tempBean.fans = tempBean.fans + 1;
					}else if(bean.data.link == FollowingConfig.LINK_TYPE_FAN_ONLY || bean.data.link == FollowingConfig.LINK_TYPE_UNLINK){
						tempBean.fans = tempBean.fans - 1;
						if(tempBean.fans <0){
							tempBean.fans = 0;
						}
					}
					mFollowingList.set(i, tempBean);
					mFansListAdapter.notifyDataSetChanged();

					if(mFollowingList==null||mFollowingList.size()<=0){
						setEmptyView(getString(R.string.no_fans_tips));
					}

					if(bean.data.link == FollowingConfig.LINK_TYPE_FAN_ONLY
							||bean.data.link == FollowingConfig.LINK_TYPE_UNLINK){
						Toast.makeText(FanListActivity.this,
								getResources().getString(R.string.str_usercenter_attention_cancle_ok),Toast.LENGTH_SHORT).show();
					}else if(bean.data.link == FollowingConfig.LINK_TYPE_FOLLOW_EACHOTHER){
						Toast.makeText(FanListActivity.this,
								getResources().getString(R.string.str_usercenter_attention_ok),Toast.LENGTH_SHORT).show();
					}
				}
			} else {
				// Toast for operation failed
				Toast.makeText(FanListActivity.this, getString(R.string.network_error), Toast.LENGTH_SHORT).show();

			}

		} 
	}
	
	private int findLinkUserItem(String linkuid) {
		if(null == mFollowingList || mFollowingList.size() == 0 || TextUtils.isEmpty(linkuid)) {
			return -1;
		}

		int size = mFollowingList.size();
		for(int i = 0; i < size; i++) {
			SimpleUserItemBean bean = mFollowingList.get(i);
			if(null != bean &&bean.uid.equals(linkuid)) {
				return i;
			}
		}

		return -1;
	}
}
