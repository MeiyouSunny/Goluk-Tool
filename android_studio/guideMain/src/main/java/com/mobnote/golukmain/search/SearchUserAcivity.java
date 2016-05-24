package com.mobnote.golukmain.search;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import cn.com.mobnote.module.page.IPageNotifyFn;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.BaseActivity;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.carrecorder.view.CustomDialog;
import com.mobnote.golukmain.carrecorder.view.CustomDialog.OnRightClickListener;
import com.mobnote.golukmain.carrecorder.view.CustomLoadingDialog;
import com.mobnote.golukmain.follow.FollowRequest;
import com.mobnote.golukmain.follow.bean.FollowRetBean;
import com.mobnote.golukmain.following.FollowingConfig;
import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.recommend.RecommendRequest;
import com.mobnote.golukmain.recommend.bean.RecommendRetBean;
import com.mobnote.golukmain.search.bean.SearchListBean;
import com.mobnote.golukmain.search.bean.SearchRetBean;
import com.mobnote.golukmain.userbase.bean.SimpleUserItemBean;
import com.mobnote.util.GolukUtils;

public class SearchUserAcivity extends BaseActivity implements IRequestResultListener, OnClickListener{

	private TextView mCancelTv;
	private ImageView mSearchDeleteIv;
	private EditText mSearchContentEt;

	private final static String TAG = "ActivitySearchUserList";
	private final static String REFRESH_NORMAL = "0";
	private final static String REFRESH_PULL_DOWN = "1";
	private final static String REFRESH_PULL_UP = "2";

	private PullToRefreshListView mUserlistPtrList;

	private List<SearchListBean> mUserList;
	private SearchListAdapter mUserListAdapter;

	private String mCurMotion = REFRESH_NORMAL;
	private final static String PROTOCOL = "200";

	private RelativeLayout mEmptyRl;
	private TextView mEmptyTv;

	private CustomLoadingDialog mLoadingDialog;
	private CustomDialog mCustomDialog;

	private String searchContent;
	private boolean hasSearched;

	private final int requestOffset = 20;

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
		
		mUserList = new ArrayList<SearchListBean>();
		mUserListAdapter = new SearchListAdapter(this,mUserList);
		mUserlistPtrList.setAdapter(mUserListAdapter);
		
		mUserlistPtrList.setMode(PullToRefreshBase.Mode.DISABLED);
		mUserlistPtrList.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
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
					doSearch();
				}
				return true;
			}
		});
		mSearchContentEt.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub
				if(TextUtils.isEmpty(mSearchContentEt.getText().toString())){
					mSearchDeleteIv.setVisibility(View.GONE);
				}else{
					mSearchDeleteIv.setVisibility(View.VISIBLE);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
			}
		});
		mUserlistPtrList.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
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

		mUserlistPtrList = (PullToRefreshListView) findViewById(R.id.ptrlist_searchlist);

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

	private void doSearch(){
		hasSearched = true;
		searchContent = mSearchContentEt.getText().toString().trim();
		if(TextUtils.isEmpty(searchContent)){
			Toast.makeText(SearchUserAcivity.this,getResources().getString(R.string.str_search_keywards_cannot_be_empty), Toast.LENGTH_SHORT).show();
			mSearchContentEt.setText(searchContent);
		}else if(!searchContent.matches("[a-zA-Z0-9_\u4e00-\u9fa5]*")){
            //非法字符
			Toast.makeText(SearchUserAcivity.this,getResources().getString(R.string.str_search_supported_words), Toast.LENGTH_SHORT).show();
		}else{
			sendSearchUserRequest(REFRESH_NORMAL,searchContent);
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);  
			imm.hideSoftInputFromWindow(mSearchContentEt.getWindowToken(), 0);
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

			if(mUserList != null && mUserList.size()>0){
				index = mUserList.get(mUserList.size()-1).getUserItemBean().index;
			}
		} else if(REFRESH_NORMAL.equals(mCurMotion) || REFRESH_PULL_DOWN.equals(mCurMotion)) {
			if(mUserList != null &&  mUserList.size()>0){
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
		if (GolukApplication.getInstance().isUserLoginSucess && GolukApplication.getInstance().getMyInfo() != null) {

			if (!mLoadingDialog.isShowing()) {
				mLoadingDialog.show();
			}
			request.get(PROTOCOL, linkuid, type, GolukApplication.getInstance().getMyInfo().uid);
		} 
	}

	protected void follow(final String linkuid,final String type){

		if(!GolukUtils.isNetworkConnected(this)) {
			Toast.makeText(this,getString(R.string.network_error),
					Toast.LENGTH_SHORT).show();
			return;
		}

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
		}else {
			GolukUtils.startLoginActivity(this);
		}

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int id = v.getId();
		if (id == R.id.ry_searchlist_refresh) {
			doSearch();
		}
	}

	@Override
	public void onLoadComplete(int requestType, Object result) {
		// TODO Auto-generated method stub
		if(mLoadingDialog!=null&&mLoadingDialog.isShowing()){
			mLoadingDialog.close();
		}

		if(requestType == IPageNotifyFn.PageType_SearchUser) {

			mUserlistPtrList.onRefreshComplete();
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
				mUserlistPtrList.setMode(PullToRefreshBase.Mode.DISABLED);
				return;
			}

			List<SimpleUserItemBean> followingBeanList = bean.data.userlist;

			if(null == followingBeanList || followingBeanList.size() == 0) {

				if(REFRESH_PULL_UP.equals(mCurMotion)) {

					Toast.makeText(SearchUserAcivity.this, getString(
							R.string.str_pull_refresh_listview_bottom_reach), Toast.LENGTH_SHORT).show();
					mCurMotion = REFRESH_NORMAL;

					//mFollowingList.add(new SearchListBean(4, null));
					mUserlistPtrList.setMode(PullToRefreshBase.Mode.DISABLED);
					return;
				}else if(REFRESH_NORMAL.equals(mCurMotion) || REFRESH_PULL_DOWN.equals(mCurMotion)){
					mCurMotion = REFRESH_NORMAL;
					mUserList.clear();
					List<SimpleUserItemBean> recommendBeanList = bean.data.recomlist;
					if(recommendBeanList == null || recommendBeanList.size() == 0){

					}else{
						mUserList.add(new SearchListBean(1, null));
						mUserList.add(new SearchListBean(2, null));
						for(SimpleUserItemBean userBean:recommendBeanList){
							mUserList.add(new SearchListBean(3, userBean));
						}	
					}
					mUserlistPtrList.setAdapter(mUserListAdapter);
					mUserlistPtrList.setMode(PullToRefreshBase.Mode.DISABLED);
					return;
				}
			}else{
				if(REFRESH_PULL_UP.equals(mCurMotion)) {
					for(SimpleUserItemBean userBean:followingBeanList){
						mUserList.add(new SearchListBean(3, userBean));
					}
					if(mUserList.size() < requestOffset){
						mUserList.add(new SearchListBean(4, null));
						mUserlistPtrList.setMode(PullToRefreshBase.Mode.DISABLED);
					}else{
						mUserlistPtrList.setMode(PullToRefreshBase.Mode.PULL_UP_TO_REFRESH);
					}
					mUserListAdapter.notifyDataSetChanged();
				} else if(REFRESH_NORMAL.equals(mCurMotion) || REFRESH_PULL_DOWN.equals(mCurMotion)) {
					mUserList.clear();
					for(SimpleUserItemBean userBean:followingBeanList){
						mUserList.add(new SearchListBean(3, userBean));
					}
					if(mUserList.size() < requestOffset){
						mUserList.add(new SearchListBean(4, null));
						mUserlistPtrList.setMode(PullToRefreshBase.Mode.DISABLED);
					}else{
						mUserlistPtrList.setMode(PullToRefreshBase.Mode.PULL_UP_TO_REFRESH);
					}
					mUserlistPtrList.setAdapter(mUserListAdapter);
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

			mUserlistPtrList.setMode(PullToRefreshBase.Mode.DISABLED);

			List<SimpleUserItemBean> followingBeanList = bean.data.userlist;

			if(null == followingBeanList || followingBeanList.size() == 0) {
				return;
			}else{
				mUserList.clear();
				mUserList.add(new SearchListBean(2,null));
				for(SimpleUserItemBean userBean:followingBeanList){
					mUserList.add(new SearchListBean(3, userBean));
				}
				mUserlistPtrList.setAdapter(mUserListAdapter);
			}
			mCurMotion = REFRESH_NORMAL;

		}else if(requestType == IPageNotifyFn.PageType_Follow) {//关注

			FollowRetBean bean = (FollowRetBean)result;
			if(null != bean) {

				if(bean.code != 0) {
					if(10001 == bean.code|| 10002 == bean.code){
						GolukUtils.startLoginActivity(SearchUserAcivity.this);
					}else if(bean.code == 12011){
						Toast.makeText(SearchUserAcivity.this, getString(R.string.follow_operation_limit_total), Toast.LENGTH_SHORT).show();
					}else if(bean.code == 12016){
						Toast.makeText(SearchUserAcivity.this, getString(R.string.follow_operation_limit_day), Toast.LENGTH_SHORT).show();
					}else{
						Toast.makeText(SearchUserAcivity.this, bean.msg, Toast.LENGTH_SHORT).show();
					}
					return;
				}

				if(bean.data == null){
					return;
				}

				// User link uid to find the changed recommend user item status
				int i = findLinkUserItem(bean.data.linkuid);
				if(i >=0 && i < mUserList.size()) {
					SimpleUserItemBean tempBean = mUserList.get(i).getUserItemBean();
					tempBean.link = bean.data.link;

					if(bean.data.link == FollowingConfig.LINK_TYPE_FOLLOW_EACHOTHER || bean.data.link == FollowingConfig.LINK_TYPE_FOLLOW_ONLY){
						tempBean.fans = tempBean.fans + 1;
					}else if(bean.data.link == FollowingConfig.LINK_TYPE_FAN_ONLY || bean.data.link == FollowingConfig.LINK_TYPE_UNLINK){
						tempBean.fans = tempBean.fans - 1;
						if(tempBean.fans <0){
							tempBean.fans = 0;
						}
					}
					mUserList.set(i, new SearchListBean(3, tempBean));
					mUserListAdapter.notifyDataSetChanged();

					if(mUserList==null||mUserList.size()<=0){
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

	@Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (isShouldHideKeyboard(v, ev)) {
                hideKeyboard(v.getWindowToken());
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 根据EditText所在坐标和用户点击的坐标相对比，来判断是否隐藏键盘，因为当用户点击EditText时则不能隐藏
     *
     * @param v
     * @param event
     * @return
     */
    private boolean isShouldHideKeyboard(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            int[] l = {0, 0};
            v.getLocationInWindow(l);
            int left = l[0],
                top = l[1],
                bottom = top + v.getHeight(),
                right = left + v.getWidth();
            if (event.getX() > left && event.getX() < right
                    && event.getY() > top && event.getY() < bottom) {
                // 点击EditText的事件，忽略它。
                return false;
            } else {
                return true;
            }
        }
        // 如果焦点不是EditText则忽略，这个发生在视图刚绘制完，第一个焦点不在EditText上，和用户用轨迹球选择其他的焦点
        return false;
    }

    /**
     * 获取InputMethodManager，隐藏软键盘
     * @param token
     */
    private void hideKeyboard(IBinder token) {
        if (token != null) {
            InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            im.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

	private int findLinkUserItem(String linkuid) {
		if(null == mUserList || mUserList.size() == 0 || TextUtils.isEmpty(linkuid)) {
			return -1;
		}

		int size = mUserList.size();
		for(int i = 0; i < size; i++) {
			SimpleUserItemBean bean = mUserList.get(i).getUserItemBean();
			if(null != bean &&bean.uid.equals(linkuid)) {
			
				return i;
			}
		}

		return -1;
	}
	private void setEmptyView(String emptyInfo) {
		if(REFRESH_NORMAL.equals(mCurMotion)) {
			mUserlistPtrList.setAdapter(null);
			mUserlistPtrList.setEmptyView(mEmptyRl);
			mUserlistPtrList.setMode(PullToRefreshBase.Mode.DISABLED);
			mEmptyTv.setText(emptyInfo);
		}
	}
}
