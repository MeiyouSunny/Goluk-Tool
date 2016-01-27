package cn.com.mobnote.golukmobile.usercenter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.eventbus.EventConfig;
import cn.com.mobnote.eventbus.EventRefreshUserInfo;
import cn.com.mobnote.golukmobile.BaseActivity;
import cn.com.mobnote.golukmobile.MainActivity;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomLoadingDialog;
import cn.com.mobnote.golukmobile.live.LiveDialogManager;
import cn.com.mobnote.golukmobile.live.LiveDialogManager.ILiveDialogManagerFn;
import cn.com.mobnote.golukmobile.newest.ClickPraiseListener.IClickPraiseView;
import cn.com.mobnote.golukmobile.newest.ClickShareListener.IClickShareView;
import cn.com.mobnote.golukmobile.newest.IDialogDealFn;
import cn.com.mobnote.golukmobile.newest.JsonParserUtils;
import cn.com.mobnote.golukmobile.thirdshare.CustomShareBoard;
import cn.com.mobnote.golukmobile.thirdshare.SharePlatformUtil;
import cn.com.mobnote.golukmobile.usercenter.UserCenterAdapter.IUserCenterInterface;
import cn.com.mobnote.golukmobile.videosuqare.RTPullListView;
import cn.com.mobnote.golukmobile.videosuqare.RTPullListView.OnRTScrollListener;
import cn.com.mobnote.golukmobile.videosuqare.RTPullListView.OnRefreshListener;
import cn.com.mobnote.golukmobile.videosuqare.VideoSquareInfo;
import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.videosquare.VideoSuqareManagerFn;
import cn.com.mobnote.util.GlideUtils;
import cn.com.mobnote.util.GolukUtils;
import cn.com.mobnote.util.JsonUtil;
import cn.com.tiros.debug.GolukDebugUtils;
import de.greenrobot.event.EventBus;

/**
 * 
 * @author 曾浩
 * 
 */

public class UserCenterActivity extends BaseActivity implements VideoSuqareManagerFn, IClickShareView,
		IClickPraiseView, OnClickListener, IDialogDealFn, ILiveDialogManagerFn, IUserCenterInterface {

	private static final String TAG = "UserCenterActivity";

	private RTPullListView mRTPullListView = null;
	private UserCenterAdapter uca = null;
	private SharePlatformUtil sharePlatform = null;

	private CustomLoadingDialog mCustomProgressDialog = null;
	/** 保存列表一个显示项索引 */
	private int wonderfulFirstVisible;
	/** 保存列表显示item个数 */
	private int wonderfulVisibleCount;
	/** 返回首页 */
	private Button titlehome;
	/** 返回按钮 */
	private ImageButton backbtn;
	// public static Handler handler = null;

	/** 所有的数据请求 id */
	public long mAllDataSequenceId = 0;
	/** 我的分享视频请求 */
	private long mMyShareVideoSequenceId = 0;

	class ShareVideoGroup {
		public List<VideoSquareInfo> videolist = null;
		/** 是否还有分页 */
		public boolean isHaveData = false;
		public boolean addFooter = false;
		public boolean loadfailed; // 首次加载失败
		public boolean firstSucc = false;// 首次是否加载成功
	}

	class PraiseInfoGroup {
		public List<PraiseInfo> praiselist = null;
		/** 是否还有分页 */
		public boolean isHaveData = false;
		public boolean loadfailed = false; // 首次加载失败
		public boolean firstSucc = false; // 首次是否加载成功
	}

	// 当前访问的用户信息
	private UCUserInfo curUser = null;
	// 当前该用户的分享视频信息列表
	private ShareVideoGroup videogroupdata = null;
	// 当前该用户的点赞人员信息列表
	private PraiseInfoGroup praisgroupdata = null;

	private RelativeLayout mBottomLoadingView = null;

	private RelativeLayout mVideoTheEndView = null;

	private TextView title = null;

	@SuppressLint("SimpleDateFormat")
	private SimpleDateFormat sdf;

	private UserCenterDataFormat ucdf = new UserCenterDataFormat();

	private int tabtype = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_center);
		GolukApplication.getInstance().getVideoSquareManager().addVideoSquareManagerListener(TAG, this);

		mBaseApp.setContext(this, "UserCenterActivity");
		videogroupdata = new ShareVideoGroup();
		videogroupdata.videolist = new ArrayList<VideoSquareInfo>();
		videogroupdata.isHaveData = false;
		praisgroupdata = new PraiseInfoGroup();
		praisgroupdata.praiselist = new ArrayList<PraiseInfo>();
		praisgroupdata.isHaveData = false;

		sdf = new SimpleDateFormat(this.getString(R.string.str_date_formatter));
		
		Intent i = this.getIntent();
		curUser = (UCUserInfo) i.getSerializableExtra("userinfo");
		tabtype = i.getIntExtra("type", 0);

		this.init();

		/**
		 * 如果是自己的主页 先请求个人主页的缓存数据
		 */
		if (testUser()) {
			// 请求同步数据接口
			String historydata = GolukApplication.getInstance().getVideoSquareManager().getUserCenter();
			// 说明本地有缓存数据
			if (historydata != null && !"".equals(historydata)) {
				this.formatAllData(historydata);
			}
		}
		mRTPullListView.firstFreshState();
		httpPost(curUser.uid);// 请求数据
		uca.setDataInfo(curUser, videogroupdata, praisgroupdata);
		uca.notifyDataSetChanged();
		backbtn = (ImageButton) findViewById(R.id.back_btn);
		title = (TextView) findViewById(R.id.title);
		titlehome = (Button) findViewById(R.id.title_home);
		backbtn.setOnClickListener(this);
		titlehome.setOnClickListener(this);
		if (testUser()) {
			title.setText(this.getString(R.string.user_personal_home_title));
		}

		LiveDialogManager.getManagerInstance().setDialogManageFn(this);

		mBottomLoadingView = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.video_square_below_loading,
				null);

		mVideoTheEndView = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.usercenter_videos_below_loading,
				null);
		EventBus.getDefault().register(this);
	}

	public void onEventMainThread(EventRefreshUserInfo event) {
		if (null == event) {
			return;
		}

		switch (event.getOpCode()) {
		case EventConfig.REFRESH_USER_INFO:
			if (curUser != null) {
				if (testUser()) {
					httpPost(curUser.uid);
				}
			}
			break;
		default:
			break;
		}
	}

	@Override
	protected void onResume() {
		mBaseApp.setContext(this, "UserCenterActivity");
		super.onResume();
		GolukApplication.getInstance().getVideoSquareManager().addVideoSquareManagerListener(TAG, this);
		LiveDialogManager.getManagerInstance().setDialogManageFn(this);
	}

	/**
	 * 个人资料信息
	 */
	public JSONObject getUserData() {
		String info = mBaseApp.mGoluk.GolukLogicCommGet(GolukModule.Goluk_Module_HttpPage, 0, "");
		GolukDebugUtils.i("lily", "---IndexMore--------" + info);
		try {
			JSONObject json = new JSONObject(info);

			return json;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 初始化数据
	 */
	private void init() {
		if (sharePlatform == null) {
			sharePlatform = new SharePlatformUtil(this);
			uca = new UserCenterAdapter(this, sharePlatform, this, tabtype);
			mRTPullListView = (RTPullListView) findViewById(R.id.mRTPullListView);
			mRTPullListView.setSelector(new ColorDrawable(Color.TRANSPARENT));
			mRTPullListView.setAdapter(uca);

			mRTPullListView.setonRefreshListener(new OnRefreshListener() {
				@Override
				public void onRefresh() {
					// 下拉刷新个人中心所有数据
					httpPost(curUser.uid);// 请求数据
				}
			});

			mRTPullListView.setOnRTScrollListener(new OnRTScrollListener() {
				@Override
				public void onScrollStateChanged(AbsListView arg0, int scrollState) {
					if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {

						if (mRTPullListView.getAdapter().getCount() == (wonderfulFirstVisible + wonderfulVisibleCount)) {
							if (uca.getCurrentViewType() == UserCenterAdapter.ViewType_ShareVideoList) {// 视频列表
								if (videogroupdata.isHaveData) {// 加载更多视频数据
									if (videogroupdata.videolist.size() > 0) {
										if (!videogroupdata.addFooter) {
											videogroupdata.addFooter = true;
											mRTPullListView.addFooterView(mBottomLoadingView);
										}
										httpGetNextVideo(videogroupdata.videolist.get(videogroupdata.videolist.size() - 1).mVideoEntity.sharingtime);
									}
								}
							} else {// 点赞用户列表

							}
						}
					}
				}

				@Override
				public void onScroll(AbsListView arg0, int firstVisibleItem, int visibleItemCount, int arg3) {
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
		mRTPullListView.onRefreshComplete(GolukUtils.getCurrentFormatTime(this));
	}

	/**
	 * 获取网络数据
	 * 
	 * @param flag
	 *            是否显示加载中对话框
	 * @author xuhw
	 * @date 2015年4月15日
	 */
	public void httpPost(String otheruid) {
		mAllDataSequenceId = GolukApplication.getInstance().getVideoSquareManager().getUserCenter(otheruid);
	}

	/**
	 * 获取更多视频列表
	 * 
	 */
	private void httpGetNextVideo(String sharingtime) {
		mMyShareVideoSequenceId = GolukApplication.getInstance().getVideoSquareManager()
				.getUserCenterShareVideo(curUser.uid, "2", sharingtime);
	}

	/**
	 * 验证当前看的是自己的个人中心 还是别人的个人中心
	 * 
	 * @return
	 */
	public boolean testUser() {
		if (!isLoginSucess()) {
			return false;
		}
		String info = mBaseApp.mGoluk.GolukLogicCommGet(GolukModule.Goluk_Module_HttpPage, 0, "");
		GolukDebugUtils.i("lily", "---IndexMore--------" + info);
		try {
			JSONObject json = new JSONObject(info);
			String id = json.getString("uid");

			if (id.equals(curUser.uid)) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private boolean isLoginSucess() {
		if (mBaseApp.isUserLoginSucess) {
			// 登录成功
			return true;
		}
		if (mBaseApp.loginoutStatus) {
			// 用户注销, 表示登录失败
			return false;
		}
		if (mBaseApp.loginStatus == 1 || (mBaseApp.autoLoginStatus == 1 || mBaseApp.autoLoginStatus == 2)) {
			return true;
		}

		return false;
	}

	/**
	 * 封装三个请求同时回来的数据
	 */
	private void formatAllData(String data) {
		UCUserInfo user = ucdf.getUserInfo(data);
		if (user != null) {
			List<VideoSquareInfo> videos;
			videos = ucdf.getClusterList(data);
			List<PraiseInfo> praise = ucdf.getPraises(data);

			// 说明有数据
			if (videos != null) {
				mRTPullListView.removeFooterView(mVideoTheEndView);
				if (videos.size() >= 20) {
					videogroupdata.isHaveData = true;
				} else {
					videogroupdata.isHaveData = false;
				}
				videogroupdata.videolist = videos;
				videogroupdata.firstSucc = true;
				videogroupdata.loadfailed = false;
			} else {// 数据异常
				if (videogroupdata.firstSucc == false) {
					videogroupdata.loadfailed = true;
				}
			}
			// 说明有数据
			if (praise != null) {
				this.praisgroupdata.praiselist = praise;
				this.praisgroupdata.firstSucc = true;
				this.praisgroupdata.loadfailed = false;
			} else {// 数据异常
				if (praisgroupdata.firstSucc == false) {
					this.praisgroupdata.loadfailed = true;
				}
			}
			// 说明有数据
			curUser = user;
			uca.setDataInfo(curUser, videogroupdata, praisgroupdata);
			updateViewData(true, 0);

		} else {
			if (videogroupdata.firstSucc == false) {
				videogroupdata.loadfailed = true;
			}
			if (praisgroupdata.firstSucc == false) {
				this.praisgroupdata.loadfailed = true;
			}
			GolukUtils.showToast(UserCenterActivity.this, this.getString(R.string.network_error));

			updateViewData(false, 0);
		}

	}

	@Override
	public void VideoSuqare_CallBack(int event, int msg, int param1, Object param2) {
		if (event == VSquare_Req_MainPage_Infor && param1 == mAllDataSequenceId) {
			closeProgressDialog();
			if (RESULE_SUCESS == msg) {
				this.formatAllData(param2.toString());
			} else {
				GolukUtils.showToast(UserCenterActivity.this, this.getString(R.string.network_error));
				updateViewData(false, 0);
			}

		} else if (event == VSquare_Req_MainPage_List_ShareVideo && param1 == mMyShareVideoSequenceId) {// 个人主页视频列表结果
			if (RESULE_SUCESS == msg) {
				List<VideoSquareInfo> videos = JsonParserUtils.parserNewestItemData((String) param2);
				if (videos != null && videos.size() > 0) {
					int count = videogroupdata.videolist.size();
					videogroupdata.videolist.addAll(videos);
					updateViewData(true, count);
				}

				videogroupdata.addFooter = false;
				// 移除下拉
				mRTPullListView.removeFooterView(this.mBottomLoadingView);

				if (videos.size() < 20) {
					videogroupdata.isHaveData = false;
					mRTPullListView.addFooterView(mVideoTheEndView);
				} else {
					mRTPullListView.removeFooterView(mVideoTheEndView);
					videogroupdata.isHaveData = true;
				}
			} else {
				GolukUtils.showToast(UserCenterActivity.this, this.getString(R.string.network_error));
			}

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

						String realDesc = this.getString(R.string.str_share_board_real_desc);

						if (TextUtils.isEmpty(describe)) {
							describe = this.getString(R.string.str_share_describe);
						}
						String ttl = this.getString(R.string.str_video_edit_share_title);

						if (!this.isFinishing()) {
							String videoId = null != mWillShareVideoSquareInfo ? mWillShareVideoSquareInfo.mVideoEntity.videoid
									: "";
							String username = null != mWillShareVideoSquareInfo ? mWillShareVideoSquareInfo.mUserEntity.nickname
									: "";
							describe = username + this.getString(R.string.str_colon) + describe;
							CustomShareBoard shareBoard = new CustomShareBoard(this, sharePlatform, shareurl, coverurl,
									describe, ttl, null, realDesc, videoId);
							shareBoard.showAtLocation(this.getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
						}

					} else {
						GolukUtils.showToast(this, this.getString(R.string.network_error));
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			} else {
				GolukUtils.showToast(this, this.getString(R.string.network_error));
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
				GolukUtils.showToast(this, this.getString(R.string.network_error));
			}

		} else if (event == VSquare_Req_MainPage_Share) {
			closeProgressDialog();

			if (RESULE_SUCESS == msg) {
				try {
					JSONObject json = new JSONObject((String) param2);
					JSONObject data = json.getJSONObject("data");
					String result = data.getString("result");
					// 如果返回成功
					if ("0".equals(result)) {
						String shorturl = data.getString("shorturl");
						String describe = data.getString("describe");
						String title = data.getString("title");
						String customavatar = data.getString("customavatar");
						String headportrait = data.getString("headportrait");

						String realDesc = this.getString(R.string.str_usercenter_share_realdesc);

						CustomShareBoard shareBoard = new CustomShareBoard(this, sharePlatform, shorturl, customavatar,
								describe, title, null, realDesc, "");
						shareBoard.showAtLocation(this.getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			} else {
				GolukUtils.showToast(this, this.getString(R.string.str_request_error));
			}
		} else if (VSquare_Req_MainPage_DeleteVideo == event) {
			callBack_DelVideo(msg, param1, param2);
		}
	}

	/**
	 * 控制theEnd 的显示和 去掉
	 * 
	 * @param flog
	 */
	public void updateTheEnd(boolean flog) {
		if (flog) {
			mRTPullListView.addFooterView(mVideoTheEndView);
		} else {
			mRTPullListView.removeFooterView(mVideoTheEndView);
		}
	}

	/**
	 * 删除视频回调
	 * 
	 * @author jyf
	 */
	private void callBack_DelVideo(int msg, int param1, Object param2) {
		LiveDialogManager.getManagerInstance().dissmissCommProgressDialog();
		if (RESULE_SUCESS != msg) {
			GolukUtils.showToast(this, this.getString(R.string.str_delete_video_fail));
			return;
		}
		String result = JsonUtil.parseDelVideo(param2);
		if (!"0".equals(result)) {
			GolukUtils.showToast(this, this.getString(R.string.str_delete_video_fail));
			return;
		}
		GolukUtils.showToast(this, this.getString(R.string.str_delete_success));
		uca.dealData(this.mDelVid);
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
		mVideoSquareInfo = info;
		if (!flag) {
			return;
		}

		for (int i = 0; i < this.videogroupdata.videolist.size(); i++) {
			VideoSquareInfo vs = this.videogroupdata.videolist.get(i);
			if (vs.id.equals(mVideoSquareInfo.id)) {
				int number = Integer.parseInt(mVideoSquareInfo.mVideoEntity.praisenumber);
				if ("1".equals(mVideoSquareInfo.mVideoEntity.ispraise)) {
					number++;
				} else {
					number--;
				}

				this.videogroupdata.videolist.get(i).mVideoEntity.praisenumber = "" + number;
				this.videogroupdata.videolist.get(i).mVideoEntity.ispraise = mVideoSquareInfo.mVideoEntity.ispraise;
				mVideoSquareInfo.mVideoEntity.praisenumber = "" + number;
				this.uca.notifyDataSetChanged();
				break;
			}
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (null != sharePlatform) {
			sharePlatform.onActivityResult(requestCode, resultCode, data);
		}
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.back_btn:
			this.finish();
			break;
		case R.id.title_home:
			Intent it = new Intent(this, MainActivity.class);
			startActivity(it);
			break;
		default:
			break;
		}
	}

	/** 保存将要删除的视频id */
	private String mDelVid = "";

	private void showDelDialog(final String vid) {
		mDelVid = vid;

		new AlertDialog.Builder(this).setTitle(this.getString(R.string.str_delete_video_title))
				.setMessage(this.getString(R.string.str_delete_video_message))
				.setPositiveButton(this.getString(R.string.delete_text), new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						delVideo(vid);
					}
				}).setNegativeButton(this.getString(R.string.user_cancle), null).create().show();
	}

	private void delVideo(String vid) {
		boolean isSucess = mBaseApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_Square,
				VSquare_Req_MainPage_DeleteVideo, JsonUtil.getDelRequestJson(vid));
		if (isSucess) {
			LiveDialogManager.getManagerInstance().showCommProgressDialog(UserCenterActivity.this,
					LiveDialogManager.DIALOG_TYPE_DEL_VIDEO, "", this.getString(R.string.str_delete_ongoing_with_omit),
					true);
		} else {
			GolukUtils.showToast(UserCenterActivity.this, this.getString(R.string.str_delete_video_fail));
		}
	}

	@Override
	public void CallBack_Del(int event, Object data) {
		if (OPERATOR_DEL == event) {
			if (null != data) {
				showDelDialog((String) data);
			}
		}
	}

	@Override
	public void dialogManagerCallBack(int dialogType, int function, String data) {
		if (LiveDialogManager.DIALOG_TYPE_DEL_VIDEO == dialogType) {
			LiveDialogManager.getManagerInstance().dissmissCommProgressDialog();
			mBaseApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_Square, VSquare_Req_MainPage_DeleteVideo,
					JsonUtil.getCancelJson());
		}

	}

	// 猛戳我刷新
	@Override
	public void OnRefrushMainPageData() {
		// 下拉刷新个人中心所有数据
		httpPost(curUser.uid);// 请求数据
		mRTPullListView.firstFreshState();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (GolukApplication.getInstance().getVideoSquareManager() != null) {
			GolukApplication.getInstance().getVideoSquareManager().removeVideoSquareManagerListener(TAG);
			GolukApplication.getInstance().getVideoSquareManager()
					.removeVideoSquareManagerListener("videosharehotlist");
		}
		EventBus.getDefault().unregister(this);
		GlideUtils.clearMemory(this);
	}

	@Override
	public int OnGetListViewWidth() {
		return mRTPullListView.getWidth();
	}

	@Override
	public int OnGetListViewHeight() {
		return mRTPullListView.getHeight();
	}

}
