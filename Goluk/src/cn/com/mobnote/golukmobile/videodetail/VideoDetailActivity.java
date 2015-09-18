package cn.com.mobnote.golukmobile.videodetail;

import java.io.File;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.Gravity;
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
import cn.com.mobnote.golukmobile.carrecorder.util.ImageManager;
import cn.com.mobnote.golukmobile.carrecorder.util.MD5Utils;
import cn.com.mobnote.golukmobile.comment.CommentBean;
import cn.com.mobnote.golukmobile.comment.ICommentFn;
import cn.com.mobnote.golukmobile.thirdshare.CustomShareBoard;
import cn.com.mobnote.golukmobile.thirdshare.SharePlatformUtil;
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
	/**视频id**/
	public static final String VIDEO_ID = "videoid";
	private VideoDetailAdapter mAdapter = null;
	/** 专题id **/
//	private String ztId = null;
	/****/
	
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
	private SharePlatformUtil sharePlatform;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		mApp = (GolukApplication) getApplication();
		setContentView(R.layout.comment);

		initView();
		
		sharePlatform = new SharePlatformUtil(this);
		sharePlatform.configPlatforms();// 设置分享平台的参数
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
		
		mImageRight.setImageResource(R.drawable.mine_icon_more);

		mAdapter = new VideoDetailAdapter(this);
		mRTPullListView.setAdapter(mAdapter);

	}

	private void initListener() {
		mImageBack.setOnClickListener(this);
		mImageRight.setOnClickListener(this);
		mTextSend.setOnClickListener(this);
		mImageRight.setOnClickListener(this);

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
//		if (null != it.getStringExtra("ztid")) {
//			ztId = it.getStringExtra("ztid").toString();
//			GolukDebugUtils.e("", "================ztId=="+ztId);
////			boolean b = GolukApplication.getInstance().getVideoSquareManager().getVideoDetailData(ztId);
//			boolean b = GolukApplication.getInstance().getVideoSquareManager().getVideoDetailListData(ztId);
//			GolukDebugUtils.e("", "----VideoDetailActivity-----b====: " + b);
//		}
		if (null != it.getStringExtra(VIDEO_ID)) {
			String videoId = it.getStringExtra(VIDEO_ID).toString();
			GolukDebugUtils.e("", "================videoid=="+videoId);
			boolean b = GolukApplication.getInstance().getVideoSquareManager().getVideoDetailListData(videoId);
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
		case R.id.comment_title_right:
			new DetailDialog(this, mVideoJson.data.avideo.video.videoid).show();
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
		GolukDebugUtils.e("", "=====VideoSuqare_CallBack===========VideoDetailActivity：event==" + event);
		if (event == VSquare_Req_Get_VideoDetail_ComentList) {
			callBack_videoDetail(msg, param1, param2);
		} else if (event == VSquare_Req_List_Comment) {
			callBack_commentList(msg, param1, param2);
		} else if (event == VSquare_Req_VOP_GetShareURL_Video){
			callBack_share(msg, param1, param2);
		} else if (event == VSquare_Req_VOP_Praise){
			callBack_praise(msg, param1, param2);
		}
	}

	private void callBack_videoDetail(int msg, int param1, Object param2) {
		if (RESULE_SUCESS == msg) {
			String jsonStr = (String) param2;
			GolukDebugUtils.e("newadapter", "================VideoDetailActivity：jsonStr==" + jsonStr);
			try {
				JSONObject jsonObject = new JSONObject(jsonStr);
				JSONObject commentList = jsonObject.optJSONObject("CommentList");
				// 详情
				String detailStr = jsonObject.optString("VideoDetail");
				mVideoJson = VideoDetailParser.parseDataFromJson(detailStr);
				GolukDebugUtils.e("newadapter", "=========VideoDetailActivity：commentList==" + mVideoJson.data.avideo.video.describe);
				// 评论
				JSONObject root = commentList.optJSONObject("data");
				JSONArray commentArray = root.optJSONArray("comments");
				int count = Integer.parseInt(root.getString("count"));
				GolukDebugUtils.e("newadapter", "==========VideoDetailActivity：commentArray==" + commentArray);
//				if (count <= 0) {
//					// 　没数据
//					noDataDeal();
//				}else{
//					// 有数据
//					commentDataList = JsonUtil.parseCommentData(commentArray);
//				}

				updateRefreshTime();
				if (OPERATOR_FIRST == mCurrentOperator) {
					// 首次进入
					firstEnterCallBack(count, mVideoJson, commentDataList);
				} else if (OPERATOR_DOWN == mCurrentOperator) {
					// 下拉刷新
					pullCallBack(count, mVideoJson, commentDataList);
				}
			}catch(Exception e){
				e.printStackTrace();
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
	
	private void callBack_share(int msg, int param1, Object param2) {
		if (RESULE_SUCESS == msg) {
			try {
				JSONObject result = new JSONObject((String) param2);
				if (result.getBoolean("success")) {
					JSONObject data = result.getJSONObject("data");
					GolukDebugUtils.i("detail", "------VideoSuqare_CallBack--------data-----" + data);
					String shareurl = data.getString("shorturl");
					String coverurl = data.getString("coverurl");
					String describe = data.optString("describe");
					String realDesc = "极路客精彩视频(使用#极路客Goluk#拍摄)";

					String allDescribe = "";
					if (TextUtils.isEmpty(describe)) {
						allDescribe = mVideoJson.data.avideo.user.nickname+"："+mVideoJson.data.avideo.video.describe;
					}else{
						allDescribe = mVideoJson.data.avideo.user.nickname+"："+describe;
					}
					String ttl = "极路客精彩视频";
					// 缩略图
					Bitmap bitmap = getThumbBitmap(mVideoJson.data.avideo.video.picture);
					if (this != null && !this.isFinishing()) {
						mAdapter.closeLoadingDialog();
						CustomShareBoard shareBoard = new CustomShareBoard(this, sharePlatform, shareurl, coverurl,
								allDescribe, ttl, bitmap, realDesc, mVideoJson.data.avideo.video.videoid);
						shareBoard.showAtLocation(this.getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
					}
				} else {
					GolukUtils.showToast(this, "网络异常，请检查网络");
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} else {
			mAdapter.closeLoadingDialog();
			GolukUtils.showToast(this, "网络异常，请检查网络");
		}
	}
	
	private void callBack_praise(int msg, int param1, Object param2) {
		GolukDebugUtils.e("lily", "222VideoSuqare_CallBack=@@@@Get_VideoDetail=="+ "=msg=" + msg+ "=param1=" + param1 + "=param2=" + param2);
		if (msg == RESULE_SUCESS) {
			// {"data":{"result":"3"},"msg":"视频不存在","success":false}
			try {
				String jsonStr = (String) param2;
				JSONObject jsonObject = new JSONObject(jsonStr);
				JSONObject dataObject = jsonObject.optJSONObject("data");
				String result = dataObject.optString("result");
				if ("0".equals(result)) {
					// 成功
				} else {
					// 错误
					GolukUtils.showToast(this, "网络异常，请稍后重试");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			GolukUtils.showToast(this, "网络异常，请稍后重试");
		}
	}
	
	public Bitmap getThumbBitmap(String netUrl) {
		String name = MD5Utils.hashKeyForDisk(netUrl) + ".0";
		String path = Environment.getExternalStorageDirectory() + File.separator + "goluk/image_cache";
		File file = new File(path + File.separator + name);
		Bitmap t_bitmap = null;
		if (file.exists()) {
			t_bitmap = ImageManager.getBitmapFromCache(file.getAbsolutePath(), 50, 50);
		}
		return t_bitmap;
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
