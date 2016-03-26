package cn.com.mobnote.golukmobile.followed;

import java.util.ArrayList;
import java.util.List;

import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomLoadingDialog;
import cn.com.mobnote.golukmobile.follow.FollowRequest;
import cn.com.mobnote.golukmobile.follow.bean.FollowRetBean;
import cn.com.mobnote.golukmobile.followed.bean.FollowedListBean;
import cn.com.mobnote.golukmobile.followed.bean.FollowedRecomUserBean;
import cn.com.mobnote.golukmobile.followed.bean.FollowedRetBean;
import cn.com.mobnote.golukmobile.http.IRequestResultListener;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.mobnote.util.GolukUtils;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class FragmentFollowed extends Fragment implements IRequestResultListener {
	private final static String TAG = "FragmentFollow";
	private final static String REFRESH_NORMAL = "0";
	private final static String REFRESH_PULL_DOWN = "1";
	private final static String REFRESH_PULL_UP = "2";

	private PullToRefreshListView mListView;
	private FollowedListAdapter mAdapter;
//	private List<FollowListBean> mOrgFollowedList;
	private List<Object> mFollowedList;
	private RelativeLayout mEmptyRL;
	private final static String PAGESIZE = "10";
	private String mTimeStamp = "";
	private String mCurMotion = REFRESH_NORMAL;
	private TextView mRetryClickIV;
	private CustomLoadingDialog mLoadingDialog;
	private final static String PROTOCOL = "200";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View rootView = inflater.inflate(R.layout.fragment_follow_layout, null);
		mEmptyRL = (RelativeLayout)rootView.findViewById(R.id.rl_follow_fragment_exception_refresh);
		mRetryClickIV = (TextView)rootView.findViewById(R.id.iv_follow_fragment_exception_refresh);
		mListView = (PullToRefreshListView)rootView.findViewById(R.id.plv_follow_fragment);
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

		sendFollowedContentRequest(REFRESH_NORMAL, mTimeStamp);
		mFollowedList = new ArrayList<Object>();
		return rootView;
	}

	private void sendFollowedContentRequest(String op, String timeStamp) {
		String tmpOp = op;
		if(REFRESH_PULL_DOWN.equals(op)) {
			tmpOp = REFRESH_NORMAL;
		}
		FollowedListRequest request =
				new FollowedListRequest(IPageNotifyFn.PageType_FollowedContent, this);
		GolukApplication app = GolukApplication.getInstance();
		if(null != app && app.isUserLoginSucess) {
			if(!TextUtils.isEmpty(app.mCurrentUId)) {
				request.get(PROTOCOL, app.mCurrentUId, PAGESIZE, tmpOp, timeStamp);
			}
		}

		if(!mLoadingDialog.isShowing() && REFRESH_NORMAL.equals(op)) {
			mLoadingDialog.show();
		}
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

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onDestroy() {
		if(mLoadingDialog != null) {
			mLoadingDialog.close();
			mLoadingDialog = null;
		}
		super.onDestroy();
	}

	private void setEmptyView() {
		if(REFRESH_NORMAL.equals(mCurMotion)) {
			mListView.setEmptyView(mEmptyRL);
			mListView.setMode(PullToRefreshBase.Mode.DISABLED);
		}
	}
	protected final static String FOLLOWD_EMPTY = "FOLLOWED_EMPTY";

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

			if(followedBeanList.size() == 0) {
				Toast.makeText(getActivity(), getString(
						R.string.str_pull_refresh_listview_bottom_reach), Toast.LENGTH_SHORT).show();
			}

			FollowedListBean last = bean.data.list.get(followedBeanList.size() - 1);
			if(null != last) {
				mTimeStamp = last.followvideo.video.sharingtime;
			} else {
				return;
			}

			List<Object> gotList = new ArrayList<Object>();
			// Refill to common list
			if("0".equals(bean.data.count)) {
				if(followedBeanList.size() == 1) {
//					mFollowedList.add(new String(FOLLOWD_EMPTY));
					gotList.add(new String(FOLLOWD_EMPTY));
					FollowedListBean followBean = followedBeanList.get(0);
					if("1".equals(followBean.type)) {
						List<FollowedRecomUserBean> userBeanList = followBean.recomuser;
						if(null != userBeanList && userBeanList.size() > 0) {
							int userCount = userBeanList.size();
							for(int j = 0; j < userCount; j++) {
//								mFollowedList.add(userBeanList.get(j));
								gotList.add(userBeanList.get(j));
							}
						}
					}
				} else {
					// TODO: no recommend, no followed
				}
			} else {
				int count = followedBeanList.size();
				for(int i = 0; i < count; i++) {
					FollowedListBean followBean = followedBeanList.get(i);
					if("0".equals(followBean.type)) {
//						mFollowedList.add(followBean.followvideo);
						gotList.add(followBean.followvideo);
					} else {
						List<FollowedRecomUserBean> userBeanList = followBean.recomuser;
						if(null != userBeanList && userBeanList.size() > 0) {
							int userCount = userBeanList.size();
							for(int j = 0; j < userCount; j++) {
								FollowedRecomUserBean tmpBean = userBeanList.get(j);
								tmpBean.position = j;
//								mFollowedList.add(tmpBean);
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
//				mOfficialMsgList.clear();
//				mOfficialMsgList.addAll(bean.data.messages);
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
				// User link uid to find the changed recommend user item status
				int i = findLinkUserItem(bean.data.linkuid);
				if(i >=0 && i < mFollowedList.size()) {
					FollowedRecomUserBean userBean = (FollowedRecomUserBean)mFollowedList.get(i);
					userBean.link = bean.data.link;
					mAdapter.notifyDataSetChanged();
				}
			} else {
				// Toast for operation failed
				Toast.makeText(getActivity(), "操作失败", Toast.LENGTH_SHORT).show();
			}
		}
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
}
