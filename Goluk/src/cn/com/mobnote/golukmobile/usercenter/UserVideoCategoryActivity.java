package cn.com.mobnote.golukmobile.usercenter;

import java.util.List;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshGridView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.BaseActivity;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomLoadingDialog;
import cn.com.mobnote.golukmobile.http.IRequestResultListener;
import cn.com.mobnote.golukmobile.usercenter.bean.VideoJson;
import cn.com.mobnote.golukmobile.usercenter.bean.VideoList;
import cn.com.mobnote.golukmobile.videodetail.VideoDetailActivity;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.mobnote.user.UserUtils;
import cn.com.mobnote.util.GolukUtils;

public class UserVideoCategoryActivity extends BaseActivity implements OnClickListener, IRequestResultListener,
		OnItemClickListener {

	private ImageButton mBackBtn = null;
	private TextView mTitleText, mNoDataText;
	private PullToRefreshGridView mGridView = null;
	private RelativeLayout mToRefreshLayout = null;
	private UserVideoCategoryAdapter mAdapter = null;
	/** 首次进入 */
	private static final String OPERATOR_FIRST = "0";
	/** 下拉 */
	private static final String OPERATOR_DOWN = "1";
	/** 上拉 */
	private static final String OPERATOR_UP = "2";
	/** 当前状态 **/
	private String mCurrentState = "";
	private String mOtheruid = "";
	private String mType = "";
	private String mCurrentUid = "";
	/** 所有视频 **/
	public static final String COLLECTION_ALL_VIDEO = "0";
	/** 精选视频 **/
	public static final String COLLECTION_WONDERFUL_VIDEO = "1";
	/** 推荐视频 **/
	public static final String COLLECTION_RECOMMEND_VIDEO = "2";
	/** 头条视频 **/
	public static final String COLLECTION_HEADLINES_VIDEO = "3";
	/** 视频列表数据 **/
	private List<VideoList> mVideoList = null;
	/** 视频列表第一个index **/
	private String mFirstIndex = "";
	/** 视频列表最后一个index **/
	private String mLastIndex = "";
	/** 加载中 **/
	private CustomLoadingDialog mLoadinDialog = null;
	/**是否是第一次请求数据**/
	private boolean mIsFirst = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_videocategory_layout);

		mCurrentUid = GolukApplication.getInstance().mCurrentUId;

		Intent it = getIntent();
		mOtheruid = it.getStringExtra("uid");
		mType = it.getStringExtra("type");

		initView();

	}

	private void initView() {
		mBackBtn = (ImageButton) findViewById(R.id.ib_videocategory_back);
		mTitleText = (TextView) findViewById(R.id.tv_videocategory_title);
		mGridView = (PullToRefreshGridView) findViewById(R.id.gv_videocategory);
		mNoDataText = (TextView) findViewById(R.id.tv_videocategory_nodata);
		mToRefreshLayout = (RelativeLayout) findViewById(R.id.ry_videocategory_refresh);

		mBackBtn.setOnClickListener(this);
		mToRefreshLayout.setOnClickListener(this);
		mGridView.setOnItemClickListener(this);

		mGridView.getRefreshableView().setNumColumns(2);
		mGridView.getRefreshableView().setVerticalSpacing(12);// 设置内部item边距
		mGridView.getRefreshableView().setHorizontalSpacing(12);
		mGridView.getRefreshableView().setClipToPadding(true);
		mGridView.getRefreshableView().setPadding(12, 12, 12, 12);// 设置外部item边距
		mGridView.setMode(PullToRefreshBase.Mode.BOTH);

		mAdapter = new UserVideoCategoryAdapter(this);
		mGridView.setAdapter(mAdapter);

		mGridView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<GridView>() {

			@Override
			public void onPullDownToRefresh(PullToRefreshBase<GridView> pullToRefreshBase) {
				// 下拉刷新
				mIsFirst = false;
				pullToRefreshBase.getLoadingLayoutProxy(true, false).setLastUpdatedLabel(
						getResources().getString(R.string.updating)
								+ GolukUtils.getCurrentFormatTime(UserVideoCategoryActivity.this));
				httpRequestData(OPERATOR_DOWN, mOtheruid, mCurrentUid, "");
			}

			@Override
			public void onPullUpToRefresh(PullToRefreshBase<GridView> pullToRefreshBase) {
				// 上拉加载
				mIsFirst = false;
				pullToRefreshBase.getLoadingLayoutProxy(false, true).setLastUpdatedLabel(
						getResources().getString(R.string.goluk_pull_to_refresh_footer_pull_label));
				httpRequestData(OPERATOR_UP, mOtheruid, mCurrentUid, mLastIndex);
			}
		});

		httpRequestData(OPERATOR_FIRST, mOtheruid, mCurrentUid, "");
		mIsFirst = true;

	}

	private void httpRequestData(String operation, String otheruid, String currentuid, String index) {
		if (operation.equals(OPERATOR_FIRST)) {
			showLoadinDialog();
		}
		if (operation.equals(OPERATOR_DOWN)) {
			operation = OPERATOR_FIRST;
		}
		UserVideoListRequest request = new UserVideoListRequest(IPageNotifyFn.PageType_HomeVideoList, this);
		if (COLLECTION_RECOMMEND_VIDEO.equals(mType)) {
			mTitleText.setText(this.getString(R.string.str_user_video_type_recommend_text));
			mNoDataText.setText(this.getString(R.string.str_usercenter_recommend_video));
			request.get("200", otheruid, COLLECTION_RECOMMEND_VIDEO, operation, currentuid, index);
		} else if (COLLECTION_HEADLINES_VIDEO.equals(mType)) {
			mTitleText.setText(this.getString(R.string.str_user_video_type_headline_text));
			mNoDataText.setText(this.getString(R.string.str_usercenter_headlines_video));
			request.get("200", otheruid, COLLECTION_HEADLINES_VIDEO, operation, currentuid, index);
		} else {
			mTitleText.setText(this.getString(R.string.str_user_video_type_wonderful_text));
			mNoDataText.setText(this.getString(R.string.str_usercenter_nowonderful_video));
			request.get("200", otheruid, COLLECTION_WONDERFUL_VIDEO, operation, currentuid, index);
		}
		mCurrentState = operation;
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.ib_videocategory_back:
			finish();
			break;
		case R.id.ry_videocategory_refresh:
			httpRequestData(OPERATOR_FIRST, mOtheruid, mCurrentUid, "");
			break;

		default:
			break;
		}
	}

	@Override
	public void onLoadComplete(int requestType, Object result) {
		closeLoadingDialog();
		mGridView.onRefreshComplete();
		if (requestType == IPageNotifyFn.PageType_HomeVideoList) {
			VideoJson video = (VideoJson) result;
			if (null != video && 0 == video.code && null != video.data && null != video.data.videolist) {
				mVideoList = video.data.videolist;
				int size = mVideoList.size();
				if ((0 == video.data.videocount || size <= 0) && !mCurrentState.equals(OPERATOR_UP)) {
					mNoDataText.setVisibility(View.VISIBLE);
					mGridView.setVisibility(View.GONE);
					mToRefreshLayout.setVisibility(View.GONE);
					return;
				}
				mGridView.setVisibility(View.VISIBLE);
				mNoDataText.setVisibility(View.GONE);
				mToRefreshLayout.setVisibility(View.GONE);
				mGridView.setMode(PullToRefreshBase.Mode.BOTH);
				if (mCurrentState.equals(OPERATOR_FIRST)) {
					mFirstIndex = mVideoList.get(0).index;
					mLastIndex = mVideoList.get(video.data.videocount - 1).index;
					this.mAdapter.setDataInfo(mVideoList);
				} else {
					if (0 == mVideoList.size()) {
						GolukUtils.showToast(this, this.getString(R.string.str_pull_refresh_listview_bottom_reach));
						return;
					}
					this.mAdapter.appendData(mVideoList);
				}
			} else {
				unusual();
			}
		}
	}

	private void unusual() {
		GolukUtils.showToast(this, this.getResources().getString(R.string.str_network_unavailable));
		if (mIsFirst) {
			mNoDataText.setVisibility(View.GONE);
			mGridView.setVisibility(View.GONE);
			mToRefreshLayout.setVisibility(View.VISIBLE);
			mGridView.setMode(PullToRefreshBase.Mode.PULL_DOWN_TO_REFRESH);
		}
	}
	
	private void showLoadinDialog() {
		if (null == mLoadinDialog) {
			mLoadinDialog = new CustomLoadingDialog(this, null);
		}
		mLoadinDialog.show();
	}

	private void closeLoadingDialog() {
		if (null != mLoadinDialog) {
			mLoadinDialog.close();
			mLoadinDialog = null;
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		closeLoadingDialog();
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {
		if (!UserUtils.isNetDeviceAvailable(this)) {
			GolukUtils.showToast(this, this.getString(R.string.str_network_unavailable));
			return;
		}
		if (null != mAdapter) {
			VideoList video = (VideoList) mAdapter.getItem(position);
			if (null != video) {
				Intent itVideoDetail = new Intent(this, VideoDetailActivity.class);
				itVideoDetail.putExtra(VideoDetailActivity.VIDEO_ID, video.videoid);
				startActivity(itVideoDetail);
			}

		}
	}

}
