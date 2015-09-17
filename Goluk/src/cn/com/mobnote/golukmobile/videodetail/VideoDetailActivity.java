package cn.com.mobnote.golukmobile.videodetail;

import java.util.ArrayList;

import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.BaseActivity;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.comment.CommentBean;
import cn.com.mobnote.golukmobile.comment.ICommentFn;
import cn.com.mobnote.golukmobile.videosuqare.RTPullListView;
import cn.com.mobnote.golukmobile.videosuqare.RTPullListView.OnRTScrollListener;
import cn.com.mobnote.golukmobile.videosuqare.RTPullListView.OnRefreshListener;
import cn.com.mobnote.golukmobile.videosuqare.VideoSquareManager;
import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.videosquare.VideoSuqareManagerFn;
import cn.com.mobnote.util.GolukUtils;
import cn.com.mobnote.util.JsonUtil;
import cn.com.tiros.debug.GolukDebugUtils;

public class VideoDetailActivity extends BaseActivity implements OnClickListener, OnRefreshListener,
		OnRTScrollListener, VideoSuqareManagerFn, ICommentFn {

	/** application */
	public GolukApplication mApp = null;

	/** 布局 **/
	private ImageButton mImageBack = null;
	private TextView mTextTitle = null;
	private ImageView mImageRight = null;
	private TextView mTextSend = null;
	private EditText mEditInput = null;
	private RTPullListView mRTPullListView = null;
	private ImageView mImageNoData = null;
	private TextView mTextNoInput = null;

	/** 评论 **/
	private ArrayList<CommentBean> commentDataList = null;
	/** 详情 **/
	private VideoJson mVideoJson = null;

	/** 监听管理类 **/
	private VideoSquareManager mVideoSquareManager = null;
	/****/
	private VideoDetailAdapter mAdapter = null;
	/** 专题id **/
	private String ztId = null;
	/** 保存列表一个显示项索引 */
	private int detailFirstVisible;
	/** 保存列表显示item个数 */
	private int detailVisibleCount;

	/** 操作 (0:首次进入；1:下拉；2:上拉) */
	private int mCurrentOperator = 0;
	/** 上拉刷新时，在ListView底部显示的布局 */
	private RelativeLayout loading = null;
	/** 最近更新时间 */
	private String historyDate = "";
	/** 是否还有分页 */
	private boolean mIsHaveData = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		mApp = (GolukApplication) getApplication();
		setContentView(R.layout.comment);

		initView();
		historyDate = GolukUtils.getCurrentFormatTime();
		setListener();
		initListener();

		getDetailData();

	}

	@Override
	protected void onResume() {
		super.onResume();
		mApp.setContext(this, "detailcomment");
		mAdapter.setOnResume();
	}

	private void initView() {
		mImageBack = (ImageButton) findViewById(R.id.comment_back);
		mTextTitle = (TextView) findViewById(R.id.comment_title);
		mImageRight = (ImageView) findViewById(R.id.comment_title_right);
		mTextSend = (TextView) findViewById(R.id.comment_send);
		mEditInput = (EditText) findViewById(R.id.comment_input);
		mRTPullListView = (RTPullListView) findViewById(R.id.commentRTPullListView);
		mImageNoData = (ImageView) findViewById(R.id.comment_nodata);
		mTextNoInput = (TextView) findViewById(R.id.comment_noinput);

		mAdapter = new VideoDetailAdapter(this);
		mRTPullListView.setAdapter(mAdapter);

	}

	private void initListener() {
		mImageBack.setOnClickListener(this);
		mImageRight.setOnClickListener(this);
		mTextSend.setOnClickListener(this);

		mRTPullListView.setonRefreshListener(this);
		mRTPullListView.setOnRTScrollListener(this);
	}

	/**
	 * 获取网络视频详情数据
	 */
	public void getDetailData() {
		// 设置这个参数第一次进来会由下拉状态变为松开刷新的状态
		mRTPullListView.firstFreshState();

		String title = getIntent().getStringExtra("title");
		if (null == title || "".equals(title)) {
			mTextTitle.setText("视频详情");
		} else {
			mTextTitle.setText(title);
		}

		Intent it = getIntent();
		if (null != it.getStringExtra("ztid")) {
			ztId = it.getStringExtra("ztid").toString();
			boolean b = GolukApplication.getInstance().getVideoSquareManager().getVideoDetailData(ztId);
			GolukDebugUtils.e("", "----VideoDetailActivity-----b====: " + b);
		}
	}

	/**
	 * 获取评论列表数据
	 */
	public void getCommentList(int operation, String timestamp) {
		final String requestStr = JsonUtil.getCommentRequestStr(mVideoJson.data.avideo.video.videoid, "1", operation,
				timestamp, PAGE_SIZE);
		GolukDebugUtils.e("", "================VideoDetailActivity：requestStr=="+requestStr);
		boolean isSucess = mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_Square,
				VideoSuqareManagerFn.VSquare_Req_List_Comment, requestStr);
		GolukDebugUtils.e("", "================VideoDetailActivity：isSucess=="+isSucess);
		if (!isSucess) {
			// TODO 失败
		}
	}

	// 注册监听
	private void setListener() {
		// 注册监听
		VideoSquareManager mVideoSquareManager = GolukApplication.getInstance().getVideoSquareManager();
		if (null != mVideoSquareManager) {
			if (mVideoSquareManager.checkVideoSquareManagerListener("detailcomment")) {
				mVideoSquareManager.removeVideoSquareManagerListener("detailcomment");
			}
			mVideoSquareManager.addVideoSquareManagerListener("detailcomment", this);
		}
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.comment_back:
			exit();
			break;

		default:
			break;
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView arg0, int scrollState) {
		if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
			int count = mRTPullListView.getAdapter().getCount();
			int visibleCount = detailFirstVisible + detailVisibleCount;

			GolukDebugUtils.e("", "----VideoDetailActivity-----onScrollStateChanged-----222: " + count + "  vicount:"
					+ visibleCount+"  mIsHaveData==="+mIsHaveData);

			if (count == visibleCount && mIsHaveData) {
				startPush();
			}
		}
	}

	@Override
	public void onScroll(AbsListView arg0, int firstVisibleItem, int visibleItemCount, int arg3) {
		detailFirstVisible = firstVisibleItem;
		detailVisibleCount = visibleItemCount;
	}

	/*
	 * 
	 * 首次进入，数据回调处理 之调用视频详情的借口
	 */
	private void firstEnterCallBack(int count, VideoJson videoJsonData, ArrayList<CommentBean> dataList) {
		// 首次进入
		this.mAdapter.setData(videoJsonData, dataList);
		mRTPullListView.onRefreshComplete(getLastRefreshTime());
		if (count >= PAGE_SIZE) {
			mIsHaveData = true;
			addFoot();
		} else {
			mIsHaveData = false;
			this.removeFoot();
		}
	}

	// 下拉刷新，数据回调处理
	private void pullCallBack(int count, VideoJson videoJsonData, ArrayList<CommentBean> dataList) {
		this.mAdapter.setData(videoJsonData, dataList);
		mRTPullListView.onRefreshComplete(getLastRefreshTime());
		if (count >= PAGE_SIZE) {
			mIsHaveData = true;
			addFoot();
		} else {
			mIsHaveData = false;
			this.removeFoot();
		}

	}

	// 上拉刷新，数据回调处理
	private void pushCallBack(int count, VideoJson videoJsonData, ArrayList<CommentBean> dataList) {
		if (count >= PAGE_SIZE) {
			mIsHaveData = true;
		} else {
			mIsHaveData = false;
			this.removeFoot();
		}
		this.mAdapter.appendData(dataList);
	}

	@Override
	public void onRefresh() {
		startPull();
	}

	// 开始下拉刷新
	private void startPull() {
		mCurrentOperator = OPERATOR_DOWN;
		getDetailData();
	}

	// 开始上拉刷新
	private void startPush() {
		mCurrentOperator = OPERATOR_UP;
		GolukDebugUtils.e("", "================VideoDetailActivity：mCurrentOperator=="+mCurrentOperator+"  down=="+OPERATOR_DOWN);
		getCommentList(OPERATOR_DOWN, mAdapter.getLastDataTime());
	}

	@Override
	public void VideoSuqare_CallBack(int event, int msg, int param1, Object param2) {
		if (event == VSquare_Req_Get_VideoDetail) {
			callBack_videoDetail(msg, param1, param2);
		} else if (event == VSquare_Req_List_Comment) {
			callBack_commentList(msg, param1, param2);
		}
	}

	private void callBack_videoDetail(int msg, int param1, Object param2) {
		if (RESULE_SUCESS == msg) {
			String jsonStr = (String) param2;
			GolukDebugUtils.e("newadapter", "================VideoDetailActivity：jsonStr==" + jsonStr);
			mVideoJson = VideoDetailParser.parseDataFromJson(jsonStr);

//			mAdapter.setData(mVideoJson, commentDataList);

			updateRefreshTime();
			if (null != mVideoJson.data.avideo.video.comment.comcount
					&& !"".equals(mVideoJson.data.avideo.video.comment.comcount)) {
				int commentCount = Integer.parseInt(mVideoJson.data.avideo.video.comment.comcount);
				if (OPERATOR_FIRST == mCurrentOperator) {
					// 首次进入
					firstEnterCallBack(commentCount, mVideoJson, commentDataList);
				} else if (OPERATOR_DOWN == mCurrentOperator) {
					// 下拉刷新
					pullCallBack(commentCount, mVideoJson, commentDataList);
				}
			}
		}
	}

	private void callBack_commentList(int msg, int param1, Object param2) {
		if (1 != msg) {
			// 请求失败
			callBackFailed();
			GolukUtils.showToast(this, "当前网络不可用，请检查网络");
			return;
		}
		try {
			JSONObject rootObj = new JSONObject((String) param2);
			boolean isSucess = rootObj.getBoolean("success");
			if (!isSucess) {
				// 请求失败
				callBackFailed();
				return;
			}
			JSONObject dataObj = rootObj.getJSONObject("data");
			String result = dataObj.getString("result");
			int count = Integer.parseInt(dataObj.getString("count"));
			if (count <= 0) {
				// 　没数据
				noDataDeal();
				return;
			}
			// 有数据
			commentDataList = JsonUtil.parseCommentData(dataObj.getJSONArray("comments"));
			if (null == commentDataList || commentDataList.size() <= 0) {
				// 无数据
				noDataDeal();
				return;
			}
			GolukDebugUtils.e("", "----CommentActivity----msg:" + msg + "  param1:" + param1 + "  param2:" + param2);

			updateRefreshTime();
			noData(false);

			if (OPERATOR_FIRST == mCurrentOperator) {
				// 首次进入
				firstEnterCallBack(count, mVideoJson, commentDataList);
			} else if (OPERATOR_UP == mCurrentOperator) {
				// 上拉刷新
				GolukDebugUtils.e("newadapter", "================VideoDetailActivity：commentDataList=="+commentDataList.size());
				pushCallBack(count, mVideoJson, commentDataList);
			}

		} catch (Exception e) {
			callBackFailed();
		}
	}

	private void updateRefreshTime() {
		historyDate = GolukUtils.getCurrentFormatTime();
	}

	private String getLastRefreshTime() {
		return historyDate;
	}

	/**
	 * 添加上拉底部的loading View
	 * 
	 * @author jyf
	 * @date 2015年8月12日
	 */
	private void addFoot() {
		if (mRTPullListView.getFooterViewsCount() > 0) {
			return;
		}
		loading = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.video_square_below_loading, null);
		mRTPullListView.addFooterView(loading);
	}

	private void removeFoot() {
		if (loading != null) {
			if (mRTPullListView != null) {
				mRTPullListView.removeFooterView(loading);
			}
		}
	}

	private void exit() {
		if (null != mVideoSquareManager) {
			mVideoSquareManager.removeVideoSquareManagerListener("detailcomment");
		}
		mAdapter.cancleTimer();
		if(null != mAdapter.headHolder.mVideoView){
			mAdapter.headHolder.mVideoView.stopPlayback();
			mAdapter.headHolder.mVideoView = null;
		}
		this.finish();
	}

	private void callBackFailed() {
		if (mCurrentOperator == OPERATOR_FIRST || mCurrentOperator == OPERATOR_DOWN) {
			mRTPullListView.onRefreshComplete(getLastRefreshTime());
		} else if (mCurrentOperator == OPERATOR_UP) {
			// 上拉刷新
			removeFoot();
		}

		noData(mAdapter.getCount() <= 0);
	}
	
	private void noDataDeal() {
		mIsHaveData = false;
		if (mCurrentOperator == OPERATOR_UP) {// 上拉刷新
			removeFoot();
		} else if (mCurrentOperator == OPERATOR_FIRST || OPERATOR_DOWN == mCurrentOperator) {// 下拉刷新
			mRTPullListView.onRefreshComplete(getLastRefreshTime());
		}

		noData(mAdapter.getCount() <= 0);
	}
	// 是否显示无数据提示
	private void noData(boolean isno) {
		if (isno) {
			mRTPullListView.setVisibility(View.GONE);
//			mNoData.setVisibility(View.VISIBLE);
		} else {
			mRTPullListView.setVisibility(View.VISIBLE);
//			mNoData.setVisibility(View.GONE);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		mAdapter.setOnPause();
	}

}
