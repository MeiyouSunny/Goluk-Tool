package com.mobnote.golukmain.followed;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.tiros.debug.GolukDebugUtils;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.mobnote.application.GolukApplication;
import com.mobnote.eventbus.EventConfig;
import com.mobnote.eventbus.EventPraiseStatusChanged;
import com.mobnote.eventbus.EventUserLoginRet;
import com.mobnote.golukmain.MainActivity;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.UserLoginActivity;
import com.mobnote.golukmain.carrecorder.view.CustomLoadingDialog;
import com.mobnote.golukmain.follow.FollowAllRequest;
import com.mobnote.golukmain.follow.FollowRequest;
import com.mobnote.golukmain.follow.bean.FollowAllRetBean;
import com.mobnote.golukmain.follow.bean.FollowRetBean;
import com.mobnote.golukmain.followed.bean.FollowedListBean;
import com.mobnote.golukmain.followed.bean.FollowedRecomUserBean;
import com.mobnote.golukmain.followed.bean.FollowedRetBean;
import com.mobnote.golukmain.followed.bean.FollowedVideoObjectBean;
import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.internation.login.InternationUserLoginActivity;
import com.mobnote.golukmain.praise.PraiseCancelRequest;
import com.mobnote.golukmain.praise.PraiseRequest;
import com.mobnote.golukmain.praise.bean.PraiseCancelResultBean;
import com.mobnote.golukmain.praise.bean.PraiseCancelResultDataBean;
import com.mobnote.golukmain.praise.bean.PraiseResultBean;
import com.mobnote.golukmain.praise.bean.PraiseResultDataBean;
import com.mobnote.golukmain.search.SearchUserAcivity;
import com.mobnote.golukmain.thirdshare.ProxyThirdShare;
import com.mobnote.golukmain.thirdshare.SharePlatformUtil;
import com.mobnote.golukmain.thirdshare.ThirdShareBean;
import com.mobnote.golukmain.videoshare.ShareVideoShortUrlRequest;
import com.mobnote.golukmain.videoshare.bean.VideoShareRetBean;
import com.mobnote.util.GolukUtils;

import de.greenrobot.event.EventBus;

public class FragmentFollowed extends Fragment implements IRequestResultListener {
	private final static String TAG = "FragmentFollow";
	private final static String REFRESH_NORMAL = "0";
	private final static String REFRESH_PULL_DOWN = "1";
	private final static String REFRESH_PULL_UP = "2";

	private PullToRefreshListView mListView;
	private FollowedListAdapter mAdapter;
	private List<Object> mFollowedList;
	private RelativeLayout mEmptyRL;
	private final static String PAGESIZE = "10";
	private String mTimeStamp = "";
	private String mCurMotion = REFRESH_NORMAL;
	private TextView mRetryClickIV;
	private CustomLoadingDialog mLoadingDialog;
	private final static String PROTOCOL = "200";
	private SharePlatformUtil mSharePlatform = null;
	private int mCurrentIndex;
	protected final static String FOLLOWD_EMPTY = "FOLLOWED_EMPTY";
	private GolukApplication mApp;
	private Button mLoginButton;
	private View mLoginRL;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		GolukDebugUtils.d(TAG, "onCreate");
		EventBus.getDefault().register(this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreateView(inflater, container, savedInstanceState);
		GolukDebugUtils.d(TAG, "onCreateView");
		View rootView = inflater.inflate(R.layout.fragment_followed_content_layout, null);
		mEmptyRL = (RelativeLayout)rootView.findViewById(R.id.rl_follow_fragment_exception_refresh);
		mRetryClickIV = (TextView)rootView.findViewById(R.id.iv_follow_fragment_exception_refresh);
		mListView = (PullToRefreshListView)rootView.findViewById(R.id.plv_follow_fragment);
		mLoginButton = (Button)rootView.findViewById(R.id.btn_fragment_followed_content_to_login);
		mLoginRL = rootView.findViewById(R.id.rl_follow_fragment_no_login);

		mLoginButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = null;
				if(GolukApplication.getInstance().isInteral() == false){
					intent = new Intent(FragmentFollowed.this.getActivity(), InternationUserLoginActivity.class);
				}else{
					intent = new Intent(FragmentFollowed.this.getActivity(), UserLoginActivity.class);
				}
				
				FragmentFollowed.this.getActivity().startActivity(intent);
				return;
			}
		});

		mRetryClickIV.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sendFollowedContentRequest(REFRESH_NORMAL, mTimeStamp);
			}
		});

		mAdapter = new FollowedListAdapter(this);
		mListView.setAdapter(mAdapter);
		mLoadingDialog = new CustomLoadingDialog(getActivity(), null);
		mListView.setMode(PullToRefreshBase.Mode.DISABLED);
		mListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
			@Override
			public void onPullDownToRefresh(PullToRefreshBase<ListView> pullToRefreshBase) {
				// show latest refresh time
				pullToRefreshBase.getLoadingLayoutProxy(true, false).setLastUpdatedLabel(
						getActivity().getString(R.string.updating) +
						GolukUtils.getCurrentFormatTime(getActivity()));
				sendFollowedContentRequest(REFRESH_PULL_DOWN, mTimeStamp);
				mCurMotion = REFRESH_PULL_DOWN;
			}

			@Override
			public void onPullUpToRefresh(PullToRefreshBase<ListView> pullToRefreshBase) {
				pullToRefreshBase.getLoadingLayoutProxy(false, true).setPullLabel(
						getActivity().getResources().getString(
						R.string.goluk_pull_to_refresh_footer_pull_label));
				sendFollowedContentRequest(REFRESH_PULL_UP, mTimeStamp);
				mCurMotion = REFRESH_PULL_UP;
			}
		});

		mFollowedList = new ArrayList<Object>();
		if(getActivity() instanceof MainActivity) {
			mSharePlatform = ((MainActivity)getActivity()).getSharePlatform();
		}
		mApp = GolukApplication.getInstance();
		if(null != mApp && mApp.isUserLoginSucess) {
			mLoginRL.setVisibility(View.GONE);
//			mEmptyRL.setVisibility(View.GONE);
			mListView.setVisibility(View.VISIBLE);
			sendFollowedContentRequest(REFRESH_NORMAL, mTimeStamp);
		} else {
			mLoginRL.setVisibility(View.VISIBLE);
//			mEmptyRL.setVisibility(View.GONE);
			mListView.setVisibility(View.GONE);
		}
		
		return rootView;
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		// TODO Auto-generated method stub
		super.onHiddenChanged(hidden);
		GolukDebugUtils.d(TAG, "onHiddenChanged");
		if(!hidden){
			if(null != mApp && mApp.isUserLoginSucess) {
				mLoginRL.setVisibility(View.GONE);
				mListView.setVisibility(View.VISIBLE);
			} else {
				mLoginRL.setVisibility(View.VISIBLE);
				mListView.setVisibility(View.GONE);
			}
		}
	}

	private void sendFollowedContentRequest(String op, String timeStamp) {
		String tmpOp = op;
		if(REFRESH_PULL_DOWN.equals(op)) {
			tmpOp = REFRESH_NORMAL;
		}
		FollowedListRequest request =
				new FollowedListRequest(IPageNotifyFn.PageType_FollowedContent, this);
		if(null != mApp && mApp.isUserLoginSucess) {
			if(!TextUtils.isEmpty(mApp.mCurrentUId)) {
				request.get(PROTOCOL, mApp.mCurrentUId, PAGESIZE, tmpOp, timeStamp);
			}
		}

		if(!mLoadingDialog.isShowing() && REFRESH_NORMAL.equals(op)) {
			mLoadingDialog.show();
		}
	}

	protected void sendFollowRequest(String linkuid, String type) {
		FollowRequest request =
				new FollowRequest(IPageNotifyFn.PageType_Follow, this);
		if(null != mApp && mApp.isUserLoginSucess) {
			if(!TextUtils.isEmpty(mApp.mCurrentUId)) {
				request.get(PROTOCOL, linkuid, type, mApp.mCurrentUId);
			}
		}
	}

	protected void sendFollowAllRequest(String linkuid) {
		FollowAllRequest request =
				new FollowAllRequest(IPageNotifyFn.PageType_FollowAll, this);
		if(null != mApp && mApp.isUserLoginSucess) {
			if(!TextUtils.isEmpty(mApp.mCurrentUId)) {
				request.get(PROTOCOL, linkuid, mApp.mCurrentUId);
			}
		}
	}

	protected void sendGetShareVideoUrlRequest(String videoId, String type) {
		ShareVideoShortUrlRequest request = new ShareVideoShortUrlRequest(
				IPageNotifyFn.PageType_GetShareURL, this);
		if(null != mApp && mApp.isUserLoginSucess) {
			if(!TextUtils.isEmpty(mApp.mCurrentUId)) {
				request.get(videoId, type);
			}
		}
	}

	public void onEventMainThread(EventPraiseStatusChanged event) {
		if(null == event) {
			return;
		}

		switch(event.getOpCode()) {
		case EventConfig.PRAISE_STATUS_CHANGE:
			changePraiseStatus(event.isStatus(), event.getVideoId());
			break;
		default:
			break;
		}
	}

	private void changePraiseStatus(boolean status, String videoId) {
		int index = findFollowedVideoItem(videoId);
		if(-1 == index) {
			return;
		}

		FollowedVideoObjectBean bean = (FollowedVideoObjectBean)mFollowedList.get(index);

		int number = Integer.parseInt(bean.video.praisenumber);
		if (status) {
			number++;
		} else {
			number--;
		}

		bean.video.praisenumber = "" + number;
		bean.video.ispraise = status ? "1" : "0";
		mAdapter.notifyDataSetChanged();
	}

	public void onEventMainThread(EventUserLoginRet event) {
		if(null == event) {
			return;
		}

		if(event.getRet()) {
			mLoginRL.setVisibility(View.GONE);
			sendFollowedContentRequest(REFRESH_NORMAL, mTimeStamp);
			mListView.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		GolukDebugUtils.d(TAG, "onActivityCreated");
	}

	@Override
	public void onDestroyView() {
		GolukDebugUtils.d(TAG, "onDestroyView");
		super.onDestroyView();
	}

	@Override
	public void onPause() {
		GolukDebugUtils.d(TAG, "onPause");
		super.onPause();
	}

	@Override
	public void onResume() {
		GolukDebugUtils.d(TAG, "onResume");
		super.onResume();
	}

	@Override
	public void onStop() {
		GolukDebugUtils.d(TAG, "onStop");
		super.onStop();
	}

	@Override
	public void onAttach(Context context) {
		// TODO Auto-generated method stub
		GolukDebugUtils.d(TAG, "onAttach, context=" + context);
		super.onAttach(context);
	}

	@Override
	public void onDetach() {
		// TODO Auto-generated method stub
		GolukDebugUtils.d(TAG, "onDetach");
		super.onDetach();
	}

	@Override
	public void onDestroy() {
		GolukDebugUtils.d(TAG, "onDestroy");
		if(mLoadingDialog != null) {
			mLoadingDialog.close();
			mLoadingDialog = null;
		}
		EventBus.getDefault().unregister(this);
		super.onDestroy();
	}

	private void setEmptyView() {
		if(REFRESH_NORMAL.equals(mCurMotion)) {
			mListView.setEmptyView(mEmptyRL);
			mListView.setMode(PullToRefreshBase.Mode.DISABLED);
		}
	}

	@Override
	public void onLoadComplete(int requestType, Object result) {
		// TODO Auto-generated method stub
		mListView.onRefreshComplete();
		if(null != mLoadingDialog) {
			mLoadingDialog.close();
		}

		if(requestType == IPageNotifyFn.PageType_FollowedContent) {
			FollowedRetBean bean = (FollowedRetBean)result;
			if(null == bean) {
				Toast.makeText(getActivity(), getString(R.string.network_error), Toast.LENGTH_SHORT).show();
				if(REFRESH_NORMAL.equals(mCurMotion) || REFRESH_PULL_DOWN.equals(mCurMotion)) {
					setEmptyView();
				}
				return;
			}

			if(!bean.success) {
				if(!TextUtils.isEmpty(bean.msg)) {
					Toast.makeText(getActivity(), bean.msg, Toast.LENGTH_SHORT).show();
				}
				setEmptyView();
				return;
			}

			if(null == bean.data) {
				setEmptyView();
				return;
			}

			if("1".equals(bean.data.result)) {
				Toast.makeText(getActivity(), getActivity().getString(
						R.string.str_server_request_arg_error), Toast.LENGTH_SHORT).show();
				setEmptyView();
				return;
			}

			if("2".equals(bean.data.result)) {
				Toast.makeText(getActivity(), getActivity().getString(
						R.string.str_server_request_unknown_error), Toast.LENGTH_SHORT).show();
				setEmptyView();
				return;
			}

			mListView.setMode(PullToRefreshBase.Mode.BOTH);

			List<FollowedListBean> followedBeanList = bean.data.list;
			if(null == followedBeanList || followedBeanList.size() == 0) {
				if(REFRESH_NORMAL.equals(mCurMotion) || REFRESH_PULL_DOWN.equals(mCurMotion)) {
					Toast.makeText(getActivity(), getString(
							R.string.str_follow_no_content), Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(getActivity(), getString(
						R.string.str_pull_refresh_listview_bottom_reach), Toast.LENGTH_SHORT).show();
				}
				return;
			}

			FollowedListBean last = followedBeanList.get(followedBeanList.size() - 1);
			if(null != last) {
				if(null != last.followvideo && last.followvideo.video != null) {
					mTimeStamp = last.followvideo.video.sharingtime;
				}
			} else {
				return;
			}

			List<Object> gotList = new ArrayList<Object>();
			// Refill to common list
			if(0 == bean.data.count) {
				if(followedBeanList.size() == 1) {
					gotList.add(new String(FOLLOWD_EMPTY));
					FollowedListBean followBean = followedBeanList.get(0);
					if("1".equals(followBean.type)) {
						List<FollowedRecomUserBean> userBeanList = followBean.recomuser;
						if(null != userBeanList && userBeanList.size() > 0) {
							int userCount = userBeanList.size();
							for(int j = 0; j < userCount; j++) {
								FollowedRecomUserBean tmpBean = userBeanList.get(j);
								tmpBean.position = j;
								tmpBean.showAllFollow = true;
								gotList.add(tmpBean);
							}
						}
					}
				} else {
					// TODO: no recommend, no followed
				}
				mListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
			} else {
				int count = followedBeanList.size();
				for(int i = 0; i < count; i++) {
					FollowedListBean followBean = followedBeanList.get(i);
					if(null == followBean) {
						continue;
					}
					if("0".equals(followBean.type)) {
						gotList.add(followBean.followvideo);
					} else {
						List<FollowedRecomUserBean> userBeanList = followBean.recomuser;
						if(null != userBeanList && userBeanList.size() > 0) {
							int userCount = userBeanList.size();
							for(int j = 0; j < userCount; j++) {
								FollowedRecomUserBean tmpBean = userBeanList.get(j);
								tmpBean.position = j;
								tmpBean.showAllFollow = false;
								gotList.add(tmpBean);
							}
						}
					}
				}
			}

			if(REFRESH_PULL_UP.equals(mCurMotion)) {
				mFollowedList.addAll(gotList);
				mAdapter.notifyDataSetChanged();
			} else if(REFRESH_NORMAL.equals(mCurMotion) || REFRESH_PULL_DOWN.equals(mCurMotion)) {
				mFollowedList.clear();
				mFollowedList.addAll(gotList);
				mAdapter.setData(mFollowedList);
			} else {
			}

//			if(mOfficialMsgList.size() < PAGESIZE) {
//				mListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
//			}
			mCurMotion = REFRESH_NORMAL;
		} else if(requestType == IPageNotifyFn.PageType_Follow) {
			FollowRetBean bean = (FollowRetBean)result;
			if(null != bean) {
				if(bean.code != 0) {
					Toast.makeText(getActivity(), bean.msg, Toast.LENGTH_SHORT).show();
					return;
				}
				// User link uid to find the changed recommend user item status
				int i = findLinkUserItem(bean.data.linkuid);
				if(i >=0 && i < mFollowedList.size()) {
					FollowedRecomUserBean userBean = (FollowedRecomUserBean)mFollowedList.get(i);
					userBean.link = bean.data.link;
					mAdapter.notifyDataSetChanged();
				}
			} else {
				// Toast for operation failed
				Toast.makeText(getActivity(), getString(R.string.network_error), Toast.LENGTH_SHORT).show();
			}
		} else if(requestType == IPageNotifyFn.PageType_FollowAll) {
			FollowAllRetBean bean = (FollowAllRetBean)result;
			if(null != bean) {
				if(bean.code == 0) {
					sendFollowedContentRequest(REFRESH_NORMAL, "");
				} else {
					Toast.makeText(getActivity(), bean.msg, Toast.LENGTH_SHORT).show();
				}
			} else {
				Toast.makeText(getActivity(), getString(R.string.network_error), Toast.LENGTH_SHORT).show();
			}
		} else if(requestType == IPageNotifyFn.PageType_GetShareURL) {
			VideoShareRetBean bean = (VideoShareRetBean)result;
			if(null == bean) {
				Toast.makeText(getActivity(), getString(R.string.network_error), Toast.LENGTH_SHORT).show();
				return;
			}

			if(!bean.success) {
				Toast.makeText(getActivity(), bean.msg, Toast.LENGTH_SHORT).show();
				return;
			}

			if(null == mSharePlatform) {
				return;
			}
			String shareurl = bean.data.shorturl;
			String coverurl = bean.data.coverurl;
			String describe = bean.data.describe;

			String realDesc = getResources().getString(R.string.str_share_board_real_desc);
			if (TextUtils.isEmpty(describe)) {
				describe = getResources().getString(R.string.str_share_describe);
			}
			String ttl = getResources().getString(R.string.str_goluk_wonderful_video);
			Object obj = mFollowedList.get(mCurrentIndex);
			if(obj instanceof FollowedVideoObjectBean) {
				FollowedVideoObjectBean videoBean = (FollowedVideoObjectBean)obj;
				String videoId = null != videoBean ? videoBean.video.videoid
						: "";
				String username = null != videoBean ? videoBean.user.nickname
						: "";
				describe = username + this.getString(R.string.str_colon) + describe;
				
				
				ThirdShareBean shareBean = new ThirdShareBean();
				shareBean.surl = shareurl;
				shareBean.curl = coverurl;
				shareBean.db = describe;
				shareBean.tl = ttl;
				shareBean.bitmap = null;
				shareBean.realDesc = realDesc;
				shareBean.videoId = videoId;
				
				ProxyThirdShare shareBoard = new ProxyThirdShare(this.getActivity(), mSharePlatform, shareBean);
				shareBoard.showAtLocation(getActivity().getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
			}
		} else if(requestType == IPageNotifyFn.PageType_Praise) {
			// assume the success
			PraiseResultBean prBean = (PraiseResultBean)result;
			if(null == prBean || !prBean.success) {
				GolukUtils.showToast(getActivity(), getString(R.string.user_net_unavailable));
				return;
			}

			PraiseResultDataBean ret = prBean.data;
			if(null != ret && !TextUtils.isEmpty(ret.result)) {
				if("0".equals(ret.result)) {
					Object obj = mFollowedList.get(mCurrentIndex);
					if(obj instanceof FollowedVideoObjectBean) {
						FollowedVideoObjectBean praisedBean = (FollowedVideoObjectBean)obj;
						praisedBean.video.ispraise = "1";
						try {
							int number = Integer.valueOf(praisedBean.video.praisenumber);
							praisedBean.video.praisenumber = String.valueOf(++number);
							mAdapter.notifyDataSetChanged();
						} catch(NumberFormatException e) {
							e.printStackTrace();
							praisedBean.video.praisenumber = "0";
						}
					}
;				} else if("7".equals(ret.result)) {
					GolukUtils.showToast(getActivity(), getString(R.string.str_no_duplicated_praise));
				} else {
					GolukUtils.showToast(getActivity(), getString(R.string.str_praise_failed));
				}
			}
		} else if(requestType == IPageNotifyFn.PageType_PraiseCancel) {
			// assume the success
			PraiseCancelResultBean praiseCancelResultBean = (PraiseCancelResultBean) result;
			if (praiseCancelResultBean == null
					|| !praiseCancelResultBean.success) {
				GolukUtils.showToast(getActivity(),
						this.getString(R.string.user_net_unavailable));
				return;
			}

			PraiseCancelResultDataBean cancelRet = praiseCancelResultBean.data;
			if (null != cancelRet && !TextUtils.isEmpty(cancelRet.result)) {
				if ("0".equals(cancelRet.result)) {
					Object obj = mFollowedList.get(mCurrentIndex);
					if(obj instanceof FollowedVideoObjectBean) {
						FollowedVideoObjectBean cancelBean = (FollowedVideoObjectBean)obj;
						cancelBean.video.ispraise = "0";
						try {
							int number = Integer.valueOf(cancelBean.video.praisenumber);
							number--;
							if(number < 0) {
								number = 0;
							}
							cancelBean.video.praisenumber = String.valueOf(number);
							mAdapter.notifyDataSetChanged();
						} catch(NumberFormatException e) {
							e.printStackTrace();
							cancelBean.video.praisenumber = "0";
						}
					}
				} else {
					GolukUtils.showToast(getActivity(),
							getString(R.string.str_cancel_praise_failed));
				}
			}
		}
	}

	public boolean sendPraiseRequest(String videoId, String type) {
		PraiseRequest request = new PraiseRequest(IPageNotifyFn.PageType_Praise, this);
		return request.get("1", videoId, type);
	}

	public boolean sendCancelPraiseRequest(String videoId) {
		PraiseCancelRequest request = new PraiseCancelRequest(IPageNotifyFn.PageType_PraiseCancel, this);
		return request.get("1", videoId);
	}

	protected void storeCurrentIndex(int index) {
		mCurrentIndex = index;
	}

	private int findLinkUserItem(String linkuid) {
		if(null == mFollowedList || mFollowedList.size() == 0 || TextUtils.isEmpty(linkuid)) {
			return -1;
		}

		int size = mFollowedList.size();
		for(int i = 0; i < size; i++) {
			Object obj = mFollowedList.get(i);
			if(null != obj && obj instanceof FollowedRecomUserBean) {
				FollowedRecomUserBean bean = (FollowedRecomUserBean)obj;
				if(!TextUtils.isEmpty(bean.uid)) {
					if(bean.uid.equals(linkuid)) {
						return i;
					}
				}
			}
		}

		return -1;
	}

	private int findFollowedVideoItem(String videoId) {
		if(null == mFollowedList || mFollowedList.size() == 0 || TextUtils.isEmpty(videoId)) {
			return -1;
		}

		int size = mFollowedList.size();
		for(int i = 0; i < size; i++) {
			Object obj = mFollowedList.get(i);
			if(null != obj && obj instanceof FollowedVideoObjectBean) {
				FollowedVideoObjectBean bean = (FollowedVideoObjectBean)obj;
				if(!TextUtils.isEmpty(bean.video.videoid)) {
					if(bean.video.videoid.equals(videoId)) {
						return i;
					}
				}
			}
		}

		return -1;
	}
}
