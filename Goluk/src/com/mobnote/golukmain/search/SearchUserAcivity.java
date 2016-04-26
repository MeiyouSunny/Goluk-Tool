package com.mobnote.golukmain.search;

import io.vov.vitamio.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView.OnEditorActionListener;
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
import com.mobnote.golukmain.following.FollowingListActivity;
import com.mobnote.golukmain.following.FollowingListAdapter;
import com.mobnote.golukmain.following.FollowingListRequest;
import com.mobnote.golukmain.following.bean.FollowingItemBean;
import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.recommend.RecommendRequest;
import com.mobnote.golukmain.recommend.bean.RecommendRetBean;
import com.mobnote.golukmain.search.bean.SearchListBean;
import com.mobnote.golukmain.search.bean.SearchRetBean;
import com.mobnote.util.GolukUtils;

public class SearchUserAcivity extends BaseActivity implements IRequestResultListener, OnClickListener,OnItemClickListener{

	private TextView mCancelTv;
	private ImageView mSearchDeleteIv;
	private EditText mSearchContentEt;

	private final static String TAG = "ActivitySearchUserList";
	private final static String REFRESH_NORMAL = "0";
	private final static String REFRESH_PULL_DOWN = "1";
	private final static String REFRESH_PULL_UP = "2";

	private PullToRefreshListView mFollowinglistPtrList;

	private List<SearchListBean> mFollowingList;
	private SearchListAdapter mFollowingListAdapter;

	private String mCurMotion = REFRESH_NORMAL;
	private final static String PROTOCOL = "200";

	private String mLinkuid;

	private RelativeLayout mEmptyRl;
	private TextView mEmptyTv;

	private CustomLoadingDialog mLoadingDialog;
	private CustomDialog mCustomDialog;

	private String searchContent;
	private boolean hasSearched;

	private final int requestOffset = 10;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search_user);

		initView();

		setupView();

		setup();

		sendRecommendRequest();
	}

	private void setup() {
		// TODO Auto-generated method stub
		
		mFollowingList = new ArrayList<SearchListBean>();
		mFollowingListAdapter = new SearchListAdapter(this,mFollowingList);
		mFollowinglistPtrList.setAdapter(mFollowingListAdapter);
		
		mFollowinglistPtrList.setMode(PullToRefreshBase.Mode.DISABLED);
		mFollowinglistPtrList.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
			@Override
			public void onPullDownToRefresh(PullToRefreshBase<ListView> pullToRefreshBase) {
				// show latest refresh time
				pullToRefreshBase.getLoadingLayoutProxy(true, false).setLastUpdatedLabel(
						SearchUserAcivity.this.getString(R.string.updating) +
						GolukUtils.getCurrentFormatTime(SearchUserAcivity.this));

			}

			@Override
			public void onPullUpToRefresh(PullToRefreshBase<ListView> pullToRefreshBase) {
				pullToRefreshBase.getLoadingLayoutProxy(false, true).setPullLabel(
						SearchUserAcivity.this.getResources().getString(
						R.string.goluk_pull_to_refresh_footer_pull_label));
				sendSearchUserRequest(REFRESH_PULL_UP,searchContent);
				
			}
		});

		//sendFollowingListRequest(REFRESH_NORMAL);
		
	}

	private void setupView() {

		mEmptyRl.setOnClickListener(this);
		mSearchDeleteIv.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mSearchContentEt.setText("");;
			}
		});
		mCancelTv.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				SearchUserAcivity.this.finish();
			}});
		mSearchContentEt.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				// TODO Auto-generated method stub
				if (actionId==EditorInfo.IME_ACTION_SEARCH){
					hasSearched = true;
					searchContent = v.getText().toString();
					if(TextUtils.isEmpty(searchContent)){
						Toast.makeText(SearchUserAcivity.this,getResources().getString(R.string.str_search_keywards_cannot_be_empty), Toast.LENGTH_SHORT).show();
					}else{
						sendSearchUserRequest(REFRESH_NORMAL,searchContent);
						InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);  
						imm.hideSoftInputFromWindow(mSearchContentEt.getWindowToken(), 0);
					}
				}
				return true;
			}
		});
		mFollowinglistPtrList.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
			@Override
			public void onPullDownToRefresh(PullToRefreshBase<ListView> pullToRefreshBase) {
				// show latest refresh time
//				pullToRefreshBase.getLoadingLayoutProxy(true, false).setLastUpdatedLabel(
//						SearchUserAcivity.this.getString(R.string.updating) +
//						GolukUtils.getCurrentFormatTime(SearchUserAcivity.this));
//				sendSearchUserRequest(REFRESH_NORMAL,searchContent);
				
			}

			@Override
			public void onPullUpToRefresh(PullToRefreshBase<ListView> pullToRefreshBase) {
				pullToRefreshBase.getLoadingLayoutProxy(false, true).setPullLabel(
						SearchUserAcivity.this.getResources().getString(
						R.string.goluk_pull_to_refresh_footer_pull_label));
				sendSearchUserRequest(REFRESH_PULL_UP,searchContent);
				
			}
		});
	}

	private void initView() {
		mCancelTv = (TextView) findViewById(R.id.tv_search_cancel);
		mSearchDeleteIv = (ImageView) findViewById(R.id.iv_search_delete);
		mSearchContentEt = (EditText) findViewById(R.id.et_search_content);

		mFollowinglistPtrList = (PullToRefreshListView) findViewById(R.id.ptrlist_searchlist);

		mEmptyRl = (RelativeLayout) findViewById(R.id.ry_searchlist_refresh);
		mEmptyTv = (TextView) findViewById(R.id.tv_searchlist_empty);

		mLoadingDialog = new CustomLoadingDialog(this, null);

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

	private void sendRecommendRequest(){
		RecommendRequest recommendRequest = new RecommendRequest(IPageNotifyFn.PageType_RecommendUser, this);
		GolukApplication app = GolukApplication.getInstance();

		if(null != app && app.isUserLoginSucess&&!TextUtils.isEmpty(app.mCurrentUId)) {
			recommendRequest.get(PROTOCOL, app.mCurrentUId);
		}else{
			recommendRequest.get(PROTOCOL, null);
		}

	}

	private void sendSearchUserRequest(String op,String searchContent) {

		mCurMotion = op;

		String index = null;
		String tmpOp = op;
		if(REFRESH_PULL_DOWN.equals(op)) {
			tmpOp = REFRESH_NORMAL;
		}

		if(REFRESH_PULL_UP.equals(mCurMotion)) {

			if(mFollowingList != null && mFollowingList.size()>0){
				index = mFollowingList.get(mFollowingList.size()-1).getUserItemBean().index;
			}
		} else if(REFRESH_NORMAL.equals(mCurMotion) || REFRESH_PULL_DOWN.equals(mCurMotion)) {
			if(mFollowingList != null &&  mFollowingList.size()>0){
				//index = mFollowingList.get(0).getUserItemBean().index;
			}
		}

		SearchRequest request = new SearchRequest(IPageNotifyFn.PageType_SearchUser, this);
		GolukApplication app = GolukApplication.getInstance();

		if(null != app && app.isUserLoginSucess&&!TextUtils.isEmpty(app.mCurrentUId)) {
			request.get(PROTOCOL,searchContent,tmpOp,index,String.valueOf(requestOffset), app.mCurrentUId);
		}else{
			request.get(PROTOCOL,searchContent,tmpOp,index,String.valueOf(requestOffset), null);
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

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int id = v.getId();
	}

	@Override
	public void onLoadComplete(int requestType, Object result) {
		// TODO Auto-generated method stub
		if(mLoadingDialog!=null&&mLoadingDialog.isShowing()){
			mLoadingDialog.close();
		}

		if(requestType == IPageNotifyFn.PageType_SearchUser) {

			mFollowinglistPtrList.onRefreshComplete();
			SearchRetBean bean = (SearchRetBean)result;

			if(null == bean) {
				Toast.makeText(SearchUserAcivity.this, getString(R.string.network_error), Toast.LENGTH_SHORT).show();
				if(REFRESH_NORMAL.equals(mCurMotion) || REFRESH_PULL_DOWN.equals(mCurMotion)) {
					setEmptyView(getString(R.string.msg_system_connect_error));
				}
				return;
			}

			if(null == bean.data) {
				setEmptyView(getString(R.string.no_following_tips));
				mFollowinglistPtrList.setMode(PullToRefreshBase.Mode.DISABLED);
				return;
			}

			List<FollowingItemBean> followingBeanList = bean.data.userlist;

			if(null == followingBeanList || followingBeanList.size() == 0) {

				if(REFRESH_PULL_UP.equals(mCurMotion)) {

					Toast.makeText(SearchUserAcivity.this, getString(
							R.string.str_pull_refresh_listview_bottom_reach), Toast.LENGTH_SHORT).show();
					mCurMotion = REFRESH_NORMAL;

					//mFollowingList.add(new SearchListBean(4, null));
					mFollowinglistPtrList.setMode(PullToRefreshBase.Mode.DISABLED);
					return;
				}else if(REFRESH_NORMAL.equals(mCurMotion) || REFRESH_PULL_DOWN.equals(mCurMotion)){
					mCurMotion = REFRESH_NORMAL;
					mFollowingList.clear();
					mFollowingList.add(new SearchListBean(1, null));
					mFollowingList.add(new SearchListBean(2, null));
					List<FollowingItemBean> recommendBeanList = bean.data.recomlist;
					for(FollowingItemBean userBean:recommendBeanList){
						mFollowingList.add(new SearchListBean(3, userBean));
					}
					mFollowinglistPtrList.setAdapter(mFollowingListAdapter);
					mFollowinglistPtrList.setMode(PullToRefreshBase.Mode.DISABLED);
					return;
				}
			}else{
				if(REFRESH_PULL_UP.equals(mCurMotion)) {
					for(FollowingItemBean userBean:followingBeanList){
						mFollowingList.add(new SearchListBean(3, userBean));
					}
					if(mFollowingList.size() < requestOffset){
						mFollowingList.add(new SearchListBean(4, null));
						mFollowinglistPtrList.setMode(PullToRefreshBase.Mode.DISABLED);
					}else{
						mFollowinglistPtrList.setMode(PullToRefreshBase.Mode.PULL_UP_TO_REFRESH);
					}
					mFollowingListAdapter.notifyDataSetChanged();
				} else if(REFRESH_NORMAL.equals(mCurMotion) || REFRESH_PULL_DOWN.equals(mCurMotion)) {
					mFollowingList.clear();
					for(FollowingItemBean userBean:followingBeanList){
						mFollowingList.add(new SearchListBean(3, userBean));
					}
					if(mFollowingList.size() < requestOffset){
						mFollowingList.add(new SearchListBean(4, null));
						mFollowinglistPtrList.setMode(PullToRefreshBase.Mode.DISABLED);
					}else{
						mFollowinglistPtrList.setMode(PullToRefreshBase.Mode.PULL_UP_TO_REFRESH);
					}
					mFollowinglistPtrList.setAdapter(mFollowingListAdapter);
				} else {
				}
			}
			mCurMotion = REFRESH_NORMAL;
		}else if(requestType == IPageNotifyFn.PageType_RecommendUser){
			if(hasSearched){
				return;
			}
			RecommendRetBean bean = (RecommendRetBean)result;

			if(null == bean||null == bean.data) {
				return;
			}

			mFollowinglistPtrList.setMode(PullToRefreshBase.Mode.DISABLED);

			List<FollowingItemBean> followingBeanList = bean.data.userlist;

			if(null == followingBeanList || followingBeanList.size() == 0) {
				return;
			}else{
				mFollowingList.clear();
				mFollowingList.add(new SearchListBean(2,null));
				for(FollowingItemBean userBean:followingBeanList){
					mFollowingList.add(new SearchListBean(3, userBean));
				}
				mFollowinglistPtrList.setAdapter(mFollowingListAdapter);
			}
			mCurMotion = REFRESH_NORMAL;

		}else if(requestType == IPageNotifyFn.PageType_Follow) {//关注

			FollowRetBean bean = (FollowRetBean)result;
			if(null != bean) {

				if(bean.code != 0) {
					Toast.makeText(SearchUserAcivity.this, bean.msg, Toast.LENGTH_SHORT).show();
					return;
				}

				if(bean.data == null){
					return;
				}

				// User link uid to find the changed recommend user item status
				int i = findLinkUserItem(bean.data.linkuid);
				if(i >=0 && i < mFollowingList.size()) {
					FollowingItemBean tempBean = mFollowingList.get(i).getUserItemBean();
					tempBean.link = bean.data.link;

					if(bean.data.link == FollowingConfig.LINK_TYPE_FOLLOW_EACHOTHER || bean.data.link == FollowingConfig.LINK_TYPE_FOLLOW_ONLY){
						tempBean.fans = tempBean.fans + 1;
					}else if(bean.data.link == FollowingConfig.LINK_TYPE_FAN_ONLY || bean.data.link == FollowingConfig.LINK_TYPE_UNLINK){
						tempBean.fans = tempBean.fans - 1;
						if(tempBean.fans <0){
							tempBean.fans = 0;
						}
					}
					mFollowingList.set(i, new SearchListBean(3, tempBean));
					mFollowingListAdapter.notifyDataSetChanged();
					
					if(mFollowingList==null||mFollowingList.size()<=0){
						setEmptyView(getString(R.string.no_following_tips));
					}

					if(bean.data.link == FollowingConfig.LINK_TYPE_FAN_ONLY
							||bean.data.link == FollowingConfig.LINK_TYPE_UNLINK){
						Toast.makeText(SearchUserAcivity.this,
								getResources().getString(R.string.str_usercenter_attention_cancle_ok),Toast.LENGTH_SHORT).show();
					}else if(bean.data.link == FollowingConfig.LINK_TYPE_FOLLOW_EACHOTHER){
						Toast.makeText(SearchUserAcivity.this,
								getResources().getString(R.string.str_usercenter_attention_ok),Toast.LENGTH_SHORT).show();
					}
				}
			} else {
				// Toast for operation failed
				Toast.makeText(SearchUserAcivity.this, getString(R.string.network_error), Toast.LENGTH_SHORT).show();

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
			FollowingItemBean bean = mFollowingList.get(i).getUserItemBean();
			if(null != bean &&bean.uid.equals(linkuid)) {
			
				return i;
			}
		}

		return -1;
	}
	private void setEmptyView(String emptyInfo) {
		if(REFRESH_NORMAL.equals(mCurMotion)) {
			mFollowinglistPtrList.setAdapter(null);
			mFollowinglistPtrList.setEmptyView(mEmptyRl);
			mFollowinglistPtrList.setMode(PullToRefreshBase.Mode.DISABLED);
			mEmptyTv.setText(emptyInfo);
		}
	}
}
