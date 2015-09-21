package cn.com.mobnote.golukmobile.usercenter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.lidroid.xutils.util.LogUtils;

import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.BaseActivity;
import cn.com.mobnote.golukmobile.MainActivity;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.util.SettingUtils;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomLoadingDialog;
import cn.com.mobnote.golukmobile.live.GetBaiduAddress;
import cn.com.mobnote.golukmobile.live.LiveDialogManager;
import cn.com.mobnote.golukmobile.live.UserInfo;
import cn.com.mobnote.golukmobile.newest.ClickPraiseListener.IClickPraiseView;
import cn.com.mobnote.golukmobile.newest.JsonParserUtils;
import cn.com.mobnote.golukmobile.special.ClusterInfo;
import cn.com.mobnote.golukmobile.special.ClusterListActivity;
import cn.com.mobnote.golukmobile.special.SpecialDataManage;
import cn.com.mobnote.golukmobile.special.SpecialInfo;
import cn.com.mobnote.golukmobile.thirdshare.CustomShareBoard;
import cn.com.mobnote.golukmobile.thirdshare.SharePlatformUtil;
import cn.com.mobnote.golukmobile.videosuqare.RTPullListView;
import cn.com.mobnote.golukmobile.videosuqare.VideoSquareInfo;
import cn.com.mobnote.golukmobile.videosuqare.RTPullListView.OnRTScrollListener;
import cn.com.mobnote.golukmobile.videosuqare.RTPullListView.OnRefreshListener;
import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.videosquare.VideoSuqareManagerFn;
import cn.com.mobnote.user.UserUtils;
import cn.com.mobnote.util.GolukUtils;
import cn.com.tiros.debug.GolukDebugUtils;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.AbsListView.OnScrollListener;
import cn.com.mobnote.golukmobile.newest.ClickShareListener.IClickShareView;

/**
 * 
 * @author 曾浩
 * 
 */
public class UserCenterActivity extends BaseActivity implements
		VideoSuqareManagerFn, IClickShareView, IClickPraiseView,
		OnClickListener {

	private RTPullListView mRTPullListView = null;
	private UserCenterAdapter uca = null;
	private SharePlatformUtil sharePlatform = null;

	private String uchistoryDate;

	private CustomLoadingDialog mCustomProgressDialog = null;
	/** 保存列表一个显示项索引 */
	private int wonderfulFirstVisible;
	/** 保存列表显示item个数 */
	private int wonderfulVisibleCount;

	/**
	 * 返回按钮
	 */
	private ImageButton backbtn;

	/**
	 * 分享按钮
	 */
	private Button sharebtn;

	class ShareVideoGroup {
		public List<VideoSquareInfo> videolist = null;
		/** 是否还有分页 */
		public boolean isHaveData = false;
		public boolean addFooter = false;
	}

	class PraiseInfoGroup {
		public List<PraiseInfo> praiselist = null;
		/** 是否还有分页 */
		public boolean isHaveData = true;
	}

	// 当前访问的用户信息
	private UCUserInfo curUser = null;
	// 当前该用户的分享视频信息列表
	private ShareVideoGroup videogroupdata = null;
	// 当前该用户的点赞人员信息列表
	private PraiseInfoGroup praisgroupdata = null;

	private RelativeLayout mBottomLoadingView = null;

	@SuppressLint("SimpleDateFormat")
	private SimpleDateFormat sdf = new SimpleDateFormat("MM月dd日 HH时mm分ss秒");

	private UserCenterDataFormat ucdf = new UserCenterDataFormat();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_center);
		GolukApplication.getInstance().getVideoSquareManager()
				.addVideoSquareManagerListener("UserCenterActivity", this);
		
		mBaseApp.setContext(this, "UserCenterActivity");
		videogroupdata = new ShareVideoGroup();
		videogroupdata.videolist = new ArrayList<VideoSquareInfo>();
		videogroupdata.isHaveData = false;
		praisgroupdata = new PraiseInfoGroup();
		praisgroupdata.praiselist = new ArrayList<PraiseInfo>();
		praisgroupdata.isHaveData = false;
		Intent i = this.getIntent();
		curUser = (UCUserInfo) i.getSerializableExtra("userinfo");
		this.init();
		uca.setDataInfo(curUser, videogroupdata, praisgroupdata);
		uca.notifyDataSetChanged();
		mRTPullListView.firstFreshState();
		httpPost(curUser.uid);
		backbtn = (ImageButton) findViewById(R.id.back_btn);
		sharebtn = (Button) findViewById(R.id.title_share);
		sharebtn.setOnClickListener(this);
		backbtn.setOnClickListener(this);
		mBottomLoadingView = (RelativeLayout) LayoutInflater.from(this)
				.inflate(R.layout.video_square_below_loading, null);
	}

	@Override
	protected void onResume() {
		mBaseApp.setContext(this, "UserCenterActivity");
		super.onResume();
	}

	/**
	 * 初始化数据
	 */
	private void init() {
		if (sharePlatform == null) {
			sharePlatform = new SharePlatformUtil(this);
			uca = new UserCenterAdapter(this, sharePlatform);
			mRTPullListView = (RTPullListView) findViewById(R.id.mRTPullListView);
			mRTPullListView.setSelector(new ColorDrawable(Color.TRANSPARENT));
			mRTPullListView.setAdapter(uca);

			mRTPullListView.setonRefreshListener(new OnRefreshListener() {
				@Override
				public void onRefresh() {
					uchistoryDate = SettingUtils.getInstance().getString(
							"gcHistoryDate", sdf.format(new Date()));
					SettingUtils.getInstance().putString("ucHistoryDate",
							sdf.format(new Date()));
					// 下拉刷新个人中心所有数据
					httpPost("");// 请求数据
				}
			});

			mRTPullListView.setOnRTScrollListener(new OnRTScrollListener() {
				@Override
				public void onScrollStateChanged(AbsListView arg0,
						int scrollState) {
					if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {

						if (mRTPullListView.getAdapter().getCount() == (wonderfulFirstVisible + wonderfulVisibleCount)) {
							if (uca.getCurrentViewType() == UserCenterAdapter.ViewType_ShareVideoList) {// 视频列表
								if (videogroupdata.isHaveData) {// 加载更多视频数据
									if (videogroupdata.videolist.size() > 0) {
										if (!videogroupdata.addFooter) {
											videogroupdata.addFooter = true;
											mRTPullListView
													.addFooterView(mBottomLoadingView);
										}
										httpGetNextVideo(videogroupdata.videolist
												.get(videogroupdata.videolist
														.size() - 1).mVideoEntity.sharingtime);
									}
								}
							} else {// 点赞用户列表

							}
						}
					}
				}

				@Override
				public void onScroll(AbsListView arg0, int firstVisibleItem,
						int visibleItemCount, int arg3) {
					wonderfulFirstVisible = firstVisibleItem;
					wonderfulVisibleCount = visibleItemCount;
				}
			});
		}
	}

	public void updateViewData(boolean succ, int count) {
		if (succ) {
			uca.notifyDataSetChanged();
			if (count > 0) {
				this.mRTPullListView.setSelection(count);
			}
		} else {

		}
		mRTPullListView.onRefreshComplete("获取数据成功");
	}

	/**
	 * 获取网络数据
	 * 
	 * @param flag
	 *            是否显示加载中对话框
	 * @author xuhw
	 * @date 2015年4月15日
	 */
	private void httpPost(String otheruid) {
		boolean result = GolukApplication.getInstance().getVideoSquareManager()
				.getUserCenter(curUser.uid);
	}

	/**
	 * 获取更多视频列表
	 * 
	 */
	private void httpGetNextVideo(String sharingtime) {
		boolean result = GolukApplication.getInstance().getVideoSquareManager()
				.getUserCenterShareVideo(curUser.uid, "2", sharingtime);
	}
	
	/**
	 * 验证当前看的是自己的个人中心 还是别人的个人中心
	 * @return
	 */
	public boolean testUser(){
		String info = mBaseApp.mGoluk.GolukLogicCommGet(GolukModule.Goluk_Module_HttpPage, 0, "");
		GolukDebugUtils.i("lily", "---IndexMore--------" + info);
		try {
			JSONObject json = new JSONObject(info);
			String id = json.getString("uid");
			LogUtils.d("fucking=" + "id="+ curUser.uid + " key=" +id);
			if(id.equals(curUser.uid)){
				return true;
			}else{
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public void VideoSuqare_CallBack(int event, int msg, int param1,
			Object param2) {
		if (event == VSquare_Req_MainPage_Infor) {
			if (RESULE_SUCESS == msg) {
				List<VideoSquareInfo> videos = ucdf
						.getClusterList((String) param2);
				List<PraiseInfo> praise = ucdf.getPraises((String) param2);
				UCUserInfo user = ucdf.getUserInfo((String) param2);
				if (user != null) {
					// 说明有数据
					if (videos != null && videos.size() > 0) {
						if (videos.size() >= 20) {
							videogroupdata.isHaveData = true;
						} else {
							videogroupdata.isHaveData = false;
						}
						videogroupdata.videolist = videos;
					}
					// 说明有数据
					if (praise != null && praise.size() > 0) {
						this.praisgroupdata.praiselist = praise;
					}
					// 说明有数据
					curUser = user;
					uca.setDataInfo(curUser, videogroupdata, praisgroupdata);
					updateViewData(true, 0);
				} else {
					videogroupdata.isHaveData = false;
					GolukUtils
							.showToast(UserCenterActivity.this, "数据异常，请检查服务器");
					updateViewData(false, 0);
				}
			} else {
				videogroupdata.isHaveData = false;
				GolukUtils.showToast(UserCenterActivity.this, "网络异常，请检查网络");
				updateViewData(false, 0);
			}

		} else if (event == VSquare_Req_MainPage_List_ShareVideo) {// 个人主页视频列表结果
			if (RESULE_SUCESS == msg) {
				List<VideoSquareInfo> videos = JsonParserUtils
						.parserNewestItemData((String) param2);
				if (videos != null && videos.size() > 0) {
					int count = videogroupdata.videolist.size();
					videogroupdata.videolist.addAll(videos);
					updateViewData(true, count);
				}
			} else {
				GolukUtils.showToast(UserCenterActivity.this, "网络异常，请检查网络");
			}
			videogroupdata.addFooter = false;
			// 移除下拉
			mRTPullListView.removeFooterView(this.mBottomLoadingView);
		} else if (event == VSquare_Req_VOP_GetShareURL_Video) {
			Context topContext = mBaseApp.getContext();
			if (topContext != this) {
				return;
			}
			closeProgressDialog();
			if (RESULE_SUCESS == msg) {
				try {
					JSONObject result = new JSONObject((String) param2);
					if (result.getBoolean("success")) {
						JSONObject data = result.getJSONObject("data");
						String shareurl = data.getString("shorturl");
						String coverurl = data.getString("coverurl");
						String describe = data.optString("describe");

						String realDesc = "极路客精彩视频(使用#极路客Goluk#拍摄)";

						if (TextUtils.isEmpty(describe)) {
							describe = "#极路客精彩视频#";
						}
						String ttl = "极路客精彩视频";

						if (!this.isFinishing()) {
							String videoId = null != mWillShareVideoSquareInfo ? mWillShareVideoSquareInfo.mVideoEntity.videoid
									: "";
							String username = null != mWillShareVideoSquareInfo ? mWillShareVideoSquareInfo.mUserEntity.nickname
									: "";
							describe = username + "：" + describe;
							CustomShareBoard shareBoard = new CustomShareBoard(
									this, sharePlatform, shareurl, coverurl,
									describe, ttl, null, realDesc, videoId);
							shareBoard.showAtLocation(this.getWindow()
									.getDecorView(), Gravity.BOTTOM, 0, 0);
						}

					} else {
						GolukUtils.showToast(this, "网络异常，请检查网络");
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			} else {
				GolukUtils.showToast(this, "网络异常，请检查网络");
			}
		} else if (event == VSquare_Req_VOP_Praise) {
			if (RESULE_SUCESS == msg) {
				if (null != mVideoSquareInfo) {
					if ("0".equals(mVideoSquareInfo.mVideoEntity.ispraise)) {
						mVideoSquareInfo.mVideoEntity.ispraise = "1";
						updateClickPraiseNumber(true, mVideoSquareInfo);
					}
				}

			} else {
				GolukUtils.showToast(this, "网络异常，请检查网络");
			}
		}else if (event == VSquare_Req_MainPage_Share){
			closeProgressDialog();
			if (RESULE_SUCESS == msg) {
				try {
					JSONObject json = new JSONObject((String) param2);
					JSONObject data = json.getJSONObject("data");
					String result = data.getString("result");
					//如果返回成功
					if("0".equals(result)){
						String shorturl = data.getString("shorturl");
						String describe = data.getString("describe");
						String title = data.getString("title");
						String customavatar = data.getString("customavatar");
						String headportrait = data.getString("headportrait");
						
						String realDesc = "极路客精彩视频(使用#极路客Goluk#拍摄)";
						
						CustomShareBoard shareBoard = new CustomShareBoard(
								this, sharePlatform, shorturl, customavatar,
								describe, title, null, "", "");
						shareBoard.showAtLocation(this.getWindow()
								.getDecorView(), Gravity.BOTTOM, 0, 0);
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	}

	@Override
	public void showProgressDialog() {
		if (null == mCustomProgressDialog) {
			mCustomProgressDialog = new CustomLoadingDialog(this, null);
		}

		if (!mCustomProgressDialog.isShowing()) {
			mCustomProgressDialog.show();
		}

	}

	@Override
	public void closeProgressDialog() {
		// TODO Auto-generated method stub
		if (null != mCustomProgressDialog) {
			mCustomProgressDialog.close();
		}
	}

	private VideoSquareInfo mWillShareVideoSquareInfo;

	@Override
	public void setWillShareInfo(VideoSquareInfo info) {
		mWillShareVideoSquareInfo = info;
	}

	VideoSquareInfo mVideoSquareInfo;

	@Override
	public void updateClickPraiseNumber(boolean flag, VideoSquareInfo info) {
		// TODO Auto-generated method stub
		mVideoSquareInfo = info;
		if (!flag) {
			return;
		}

		for (int i = 0; i < this.videogroupdata.videolist.size(); i++) {
			VideoSquareInfo vs = this.videogroupdata.videolist.get(i);
			if (vs.id.equals(mVideoSquareInfo.id)) {
				int number = Integer
						.parseInt(mVideoSquareInfo.mVideoEntity.praisenumber);
				if ("1".equals(mVideoSquareInfo.mVideoEntity.ispraise)) {
					number++;
				} else {
					number--;
				}

				this.videogroupdata.videolist.get(i).mVideoEntity.praisenumber = ""
						+ number;
				this.videogroupdata.videolist.get(i).mVideoEntity.ispraise = mVideoSquareInfo.mVideoEntity.ispraise;
				mVideoSquareInfo.mVideoEntity.praisenumber = "" + number;
				break;
			}
		}

		this.uca.notifyDataSetChanged();
	}
	

	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		switch (view.getId()) {
		case R.id.back_btn:
			this.finish();
			break;
		case R.id.title_share:
			showProgressDialog();
			boolean result = GolukApplication.getInstance().getVideoSquareManager().getUserCenterShareUrl(curUser.uid);
			if(result == false){
				GolukUtils.showToast(UserCenterActivity.this, "请求异常，请检查网络是否正常");
			}
			break;
		default:
			break;
		}
	}

}
