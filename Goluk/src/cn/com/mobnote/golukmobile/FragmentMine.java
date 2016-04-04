package cn.com.mobnote.golukmobile;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.eventbus.EventConfig;
import cn.com.mobnote.eventbus.EventMessageUpdate;
import cn.com.mobnote.golukmobile.http.IRequestResultListener;
import cn.com.mobnote.golukmobile.live.ILive;
import cn.com.mobnote.golukmobile.live.UserInfo;
import cn.com.mobnote.golukmobile.msg.MessageBadger;
import cn.com.mobnote.golukmobile.msg.MessageCenterActivity;
import cn.com.mobnote.golukmobile.photoalbum.PhotoAlbumActivity;
import cn.com.mobnote.golukmobile.praised.MyPraisedActivity;
import cn.com.mobnote.golukmobile.profit.MyProfitActivity;
import cn.com.mobnote.golukmobile.userinfohome.UserInfohomeRequest;
import cn.com.mobnote.golukmobile.userinfohome.bean.UserinfohomeRetBean;
import cn.com.mobnote.golukmobile.videosuqare.VideoSquareManager;
import cn.com.mobnote.manager.MessageManager;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.mobnote.module.videosquare.VideoSuqareManagerFn;
import cn.com.mobnote.user.UserInterface;
import cn.com.mobnote.util.GlideUtils;
import cn.com.mobnote.util.GolukUtils;
import cn.com.tiros.debug.GolukDebugUtils;
import de.greenrobot.event.EventBus;

/**
 * 
 * @ 功能描述:Goluk首页更多页面
 * 
 * @author 陈宣宇
 * 
 */

@SuppressLint({ "HandlerLeak", "Instantiatable" })
public class FragmentMine extends Fragment implements OnClickListener, UserInterface, VideoSuqareManagerFn ,IRequestResultListener{
	
	/** 个人中心 **/
	private RelativeLayout mUserCenterItem = null;
	/** 未登录不显示用户id **/
	// private RelativeLayout mUserCenterId = null;
	/** 我的相册 **/
	private TextView mVideoItem = null;
	/** 摄像头管理 **/
	private TextView mCameraItem = null;
	/** 通用设置 **/
	private TextView mSetItem = null;
	/** 极路客小技巧 **/
	private TextView mSkillItem = null;
	/** 安装指导 **/
	private TextView mInstallItem = null;
	/** 版本信息 **/
	private TextView mQuestionItem = null;
	// /** 购买极路客 **/
	// private TextView mShoppingItem = null;
	/** 我的收益 **/
	private TextView mProfitItem = null;
	/** 消息中心 */
	private RelativeLayout mMsgCenterItem = null;
	private TextView mMessageTip;

	private TextView mPraisedListItem = null;

	/** 个人中心的头像、性别、昵称 */
	private ImageView mImageHead, mImageAuthentication;
	private TextView mTextName, mTextId;
	private LinearLayout mVideoLayout;
	private TextView mTextShare, mTextFans, mTextFollow;
	/** 分享视频 赞我的人 **/
	private LinearLayout mShareLayout, mFansLayout, mFollowLayout;

	/** 自动登录中的loading提示框 **/
	private SharedPreferences mPreferences = null;
	private Editor mEditor = null;
	// LinearLayout mRootLayout = null;
	private MainActivity ma;

	/** 用户信息 **/
	private String userHead, userName, userDesc, userUId, userSex, customavatar, userPhone;
	private int newFansCout;

	/** 个人中心 **/
	private static final int TYPE_USER = 1;
	/** 分享视频／赞我的人 **/
	private static final int TYPE_SHARE_PRAISE = 2;
	/** 我的收益 **/
	private static final int TYPE_PROFIT = 3;
	/** 我的关注 **/
	private static final int TYPE_FOLLOWING = 4;
	private static final String TAG = "FragmentMine";

	LinearLayout mMineRootView = null;

	private UserinfohomeRetBean mUserinfohomeRetBean;

	private ImageView mNewFansIv;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View rootView = inflater.inflate(R.layout.index_more, null);
		mMineRootView = (LinearLayout) rootView;

		EventBus.getDefault().register(this);

		ma = (MainActivity) getActivity();

		setListener();

		initView();

		setupView();

		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onResume() {
		super.onResume();
		resetLoginState();
		int msgCount = MessageManager.getMessageManager().getMessageTotalCount();
		setMessageTipCount(msgCount);
		sendGetUserHomeRequest();
	}

	@Override
	public void onDestroyView() {
		// TODO Auto-generated method stub
		super.onDestroyView();
		EventBus.getDefault().unregister(this);
	}
	

	@Override
	public void onHiddenChanged(boolean hidden) {
		// TODO Auto-generated method stub
		super.onHiddenChanged(hidden);
		
		if(!hidden){
			sendGetUserHomeRequest();
		}
		
	}

	public void onEventMainThread(EventMessageUpdate event) {
		if (null == event) {
			return;
		}

		switch (event.getOpCode()) {
		case EventConfig.MESSAGE_UPDATE:

			int msgCount = MessageManager.getMessageManager().getMessageTotalCount();
			setMessageTipCount(msgCount);
			MessageBadger.sendBadgeNumber(msgCount, getActivity());
			break;
		default:
			break;
		}
	}

	private void setMessageTipCount(int total) {

		// ImageView mainMsgTip = (ImageView)
		// findViewById(R.id.iv_main_message_tip);
		// if (total > 0) {
		// mainMsgTip.setVisibility(View.VISIBLE);
		// } else {
		// mainMsgTip.setVisibility(View.GONE);
		// }
		//
		// // Also set user page message count tip
		// if (null == indexMoreActivity || indexMoreActivity.mRootLayout ==
		// null) {
		// GolukDebugUtils.d(TAG, "index more has been finished");
		// return;
		// }
		//
		// TextView userMsgCounterTV = (TextView)
		// indexMoreActivity.mRootLayout.findViewById(R.id.tv_my_message_tip);
		String strTotal = null;
		if (total > 99) {
			strTotal = "99+";
			mMessageTip.setVisibility(View.VISIBLE);
		} else if (total <= 0) {
			strTotal = "0";
			mMessageTip.setVisibility(View.GONE);
		} else {
			mMessageTip.setVisibility(View.VISIBLE);
			strTotal = String.valueOf(total);
		}

		mMessageTip.setText(strTotal);
	}

	public void setupView() {

		ma.mApp.mUser.setUserInterface(this);
	}

	/**
	 * 页面初始化
	 */
	private void initView() {
		// 获取页面元素

		// 个人中心 我的相册 摄像头管理 通用设置 极路客小技巧 安装指导 版本信息 购买极路客
		mUserCenterItem = (RelativeLayout) mMineRootView.findViewById(R.id.user_center_item);
		// mUserCenterId = (RelativeLayout)
		// mRootLayout.findViewById(R.id.user_center_id_layout);
		mVideoItem = (TextView) mMineRootView.findViewById(R.id.video_item);
		mCameraItem = (TextView) mMineRootView.findViewById(R.id.camera_item);
		mSetItem = (TextView) mMineRootView.findViewById(R.id.set_item);
		mSkillItem = (TextView) mMineRootView.findViewById(R.id.skill_item);
		mInstallItem = (TextView) mMineRootView.findViewById(R.id.install_item);
		mQuestionItem = (TextView) mMineRootView.findViewById(R.id.question_item);
		// mShoppingItem = (TextView)
		// mRootLayout.findViewById(R.id.shopping_item);
		mProfitItem = (TextView) mMineRootView.findViewById(R.id.profit_item);
		mMsgCenterItem = (RelativeLayout) mMineRootView.findViewById(R.id.rl_my_message);
		mMessageTip = (TextView) mMineRootView.findViewById(R.id.tv_my_message_tip);
		mPraisedListItem = (TextView) mMineRootView.findViewById(R.id.tv_praise_item);

		// 头像、昵称、id
		mImageHead = (ImageView) mMineRootView.findViewById(R.id.user_center_head);
		mImageAuthentication = (ImageView) mMineRootView.findViewById(R.id.im_user_center_head_authentication);
		mTextName = (TextView) mMineRootView.findViewById(R.id.user_center_name_text);
		mTextId = (TextView) mMineRootView.findViewById(R.id.user_center_id_text);
		mVideoLayout = (LinearLayout) mMineRootView.findViewById(R.id.user_center_video_layout);
		mTextShare = (TextView) mMineRootView.findViewById(R.id.user_share_count);
		mTextFans = (TextView) mMineRootView.findViewById(R.id.user_fans_count);
		mTextFollow = (TextView) mMineRootView.findViewById(R.id.user_follow_count);
		mShareLayout = (LinearLayout) mMineRootView.findViewById(R.id.user_share);
		mFansLayout = (LinearLayout) mMineRootView.findViewById(R.id.user_fans);
		mFollowLayout = (LinearLayout) mMineRootView.findViewById(R.id.user_follow);
		mNewFansIv = (ImageView) mMineRootView.findViewById(R.id.iv_new_fans);

		// 注册事件
		// 个人中心 我的相册 摄像头管理 通用设置 极路客小技巧 安装指导 版本信息 购买极路客
		mUserCenterItem.setOnClickListener(this);
		mVideoItem.setOnClickListener(this);
		mCameraItem.setOnClickListener(this);
		mSetItem.setOnClickListener(this);
		mSkillItem.setOnClickListener(this);
		mInstallItem.setOnClickListener(this);
		mQuestionItem.setOnClickListener(this);
		// mShoppingItem.setOnClickListener(this);
		mShareLayout.setOnClickListener(this);
		mFansLayout.setOnClickListener(this);
		mFollowLayout.setOnClickListener(this);
		mProfitItem.setOnClickListener(this);
		mMsgCenterItem.setOnClickListener(this);
		mPraisedListItem.setOnClickListener(this);
	}

	// 获取登录状态及用户信息
	private void resetLoginState() {

		mPreferences = getActivity().getSharedPreferences("firstLogin", Context.MODE_PRIVATE);
		ma.mApp.mUser.setUserInterface(this);

		GolukDebugUtils.i("lily", "--------" + ma.mApp.autoLoginStatus + ma.mApp.isUserLoginSucess
				+ "=====mApp.registStatus ====" + ma.mApp.registStatus);
		if (ma.mApp.isUserLoginSucess == true || ma.mApp.registStatus == 2) {// 登录过
			GolukDebugUtils.i("lily", "---------------" + ma.mApp.autoLoginStatus + "------loginStatus------"
					+ ma.mApp.loginStatus);
			// 更多页面
			personalChanged();
		} else {
			// 未登录
			mVideoLayout.setVisibility(View.GONE);
			mImageAuthentication.setVisibility(View.GONE);
			this.showHead(mImageHead, "7");
			mTextName.setText(getActivity().getResources().getString(R.string.str_click_to_login));
			mTextId.setTextColor(Color.rgb(128, 138, 135));
			mTextId.setText(getActivity().getResources().getString(R.string.str_login_tosee_usercenter));
		}
	}

	AlertDialog dialog = null;

	@Override
	public void onClick(View v) {
		int id = v.getId();
		Intent intent = null;
		switch (id) {
		case R.id.back_btn:
			ma.mApp.mUser.setUserInterface(null);
			// 返回
			break;
		case R.id.user_share:
			if(isLoginInfoValid()){
				GolukUtils.startUserCenterActivity(getActivity(), userUId, userName, userHead, customavatar, userSex, userDesc);
			}else{
				clickToLogin(TYPE_SHARE_PRAISE);
			}
			break;	
		case R.id.user_fans:
			if (isLoginInfoValid()) {
				GolukUtils.startFanListActivity(getActivity(), ma.mApp.getMyInfo().uid);
			}else{
				clickToLogin(TYPE_FOLLOWING);
			}
			break;
		case R.id.user_follow:
			if (isLoginInfoValid()) {
				GolukUtils.startFollowingListActivity(getActivity(), ma.mApp.getMyInfo().uid);
			}else{
				clickToLogin(TYPE_FOLLOWING);
			}
			break;
		// 点击跳转到我的主页
		case R.id.user_center_item:
			if (isLoginInfoValid()) {
				GolukUtils.startUserCenterActivity(getActivity(), userUId, userName, userHead, customavatar, userSex, userDesc);
			}else{
				clickToLogin(TYPE_USER);
			}
			break;
		// 我的相册
		case R.id.video_item:
			ma.mApp.mUser.setUserInterface(null);
			intent = new Intent(getActivity(), PhotoAlbumActivity.class);
			intent.putExtra("from", "local");
			getActivity().startActivity(intent);
			break;
		// 摄像头管理
		case R.id.camera_item:
			Intent itCamera = new Intent(getActivity(), UnbindActivity.class);
			getActivity().startActivity(itCamera);
			break;
		// 通用设置
		case R.id.set_item:
			Intent itSet = new Intent(getActivity(), UserSetupActivity.class);
			getActivity().startActivity(itSet);
			break;
		// 极路客小技巧
		case R.id.skill_item:
			Intent itSkill = new Intent(getActivity(), UserOpenUrlActivity.class);
			itSkill.putExtra(UserOpenUrlActivity.FROM_TAG, "skill");
			getActivity().startActivity(itSkill);
			break;
		// 安装指导
		case R.id.install_item:
			Intent itInstall = new Intent(getActivity(), UserOpenUrlActivity.class);
			itInstall.putExtra(UserOpenUrlActivity.FROM_TAG, "install");
			getActivity().startActivity(itInstall);
			break;
		// 版本信息
		case R.id.question_item:
			Intent itQuestion = new Intent(getActivity(), UserVersionActivity.class);
			getActivity().startActivity(itQuestion);
			break;
		// 购买极路客
		// case R.id.shopping_item:
		// Intent itShopping = new Intent(mContext, UserOpenUrlActivity.class);
		// itShopping.putExtra(UserOpenUrlActivity.FROM_TAG, "shopping");
		// mContext.startActivity(itShopping);
		// break;
		// 我的收益
		case R.id.profit_item:
			if (isLoginInfoValid()) {
				Intent itProfit = new Intent(getActivity(), MyProfitActivity.class);
				getActivity().startActivity(itProfit);
			}else{
				clickToLogin(TYPE_PROFIT);
			}
			break;
		case R.id.rl_my_message:
			Intent msgIntent = new Intent(getActivity(), MessageCenterActivity.class);
			getActivity().startActivity(msgIntent);
			break;
		case R.id.tv_praise_item:
			if (!GolukUtils.isNetworkConnected(getActivity())) {
				Toast.makeText(getActivity(), getActivity().getString(R.string.network_error), Toast.LENGTH_SHORT)
						.show();
				return;
			}
			GolukApplication app = (GolukApplication) (getActivity()).getApplication();
			if (!app.isUserLoginSucess) {
				// GolukUtils.showToast(this,
				// this.getResources().getString(R.string.str_please_login));
				Intent loginIntent = new Intent(getActivity(), UserLoginActivity.class);
				getActivity().startActivity(loginIntent);
				return;
			}
			Intent praiseIntent = new Intent(getActivity(), MyPraisedActivity.class);
			getActivity().startActivity(praiseIntent);
			break;
		default:
			GolukDebugUtils.d(TAG, "unknown view clicked");
			break;
		}
	}

	// /**
	// *
	// * @param type 点击个人中心、分享视频赞我的人、我的收益
	// * @param shareOrPraise ０分享视频　　１赞我的人
	// */
	// private void clickAuto(int type,int shareOrPraise) {
	// if (ma.mApp.loginStatus == 1 || ma.mApp.registStatus == 2 ||
	// ma.mApp.autoLoginStatus == 1 || ma.mApp.autoLoginStatus == 2) {// 登录过
	// if (ma.mApp.autoLoginStatus == 1 || ma.mApp.autoLoginStatus == 4)
	// {//自动登录中或自动登录
	// mBuilder = new AlertDialog.Builder(getActivity());
	// dialog =
	// mBuilder.setMessage(getActivity().getResources().getString(R.string.user_personal_autoloading_progress)).create();
	// dialog.show();
	// } else if (ma.mApp.autoLoginStatus == 2 || ma.mApp.isUserLoginSucess) {
	// if(type == TYPE_USER) {
	// intentToUserCenter(shareOrPraise);
	// } else if(type == TYPE_SHARE_PRAISE) {
	// intentToUserCenter(shareOrPraise);
	// } else if(type == TYPE_PROFIT) {
	// Intent itProfit = new Intent(getActivity(),MyProfitActivity.class);
	// // itProfit.putExtra("uid", userUId);
	// // itProfit.putExtra("phone", userPhone);
	// getActivity().startActivity(itProfit);
	// }
	// } else {
	// clickToLogin();
	// }
	// } else {
	// clickToLogin();
	// }
	// }

	/**
	 * 登录状态是否有效s
	 * 
	 * @return
	 */
	private boolean isLoginInfoValid() {
		if (ma.mApp.loginStatus == 1 || ma.mApp.registStatus == 2 || ma.mApp.autoLoginStatus == 2) {// 登录过
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 跳转登录页
	 * 
	 * @param intentType
	 */
	private void clickToLogin(int intentType) {
		mPreferences = getActivity().getSharedPreferences("toRepwd", Context.MODE_PRIVATE);
		mEditor = mPreferences.edit();
		Intent itNo = new Intent(getActivity(), UserLoginActivity.class);
		if (intentType == TYPE_USER) {
			itNo.putExtra("isInfo", "indexmore");
			mEditor.putString("toRepwd", "more");
		} else if (intentType == TYPE_PROFIT) {
			// 登录页回调判断
			itNo.putExtra("isInfo", "profit");
			mEditor.putString("toRepwd", "toProfit");
		}
		mEditor.commit();

		getActivity().startActivity(itNo);
	}

	private void dismissDialog() {
		if (null != dialog) {
			dialog.dismiss();
			dialog = null;
		}
	}

	// 注册监听
	private void setListener() {
		// 注册监听
		VideoSquareManager mVideoSquareManager = GolukApplication.getInstance().getVideoSquareManager();
		if (null != mVideoSquareManager) {
			if (mVideoSquareManager.checkVideoSquareManagerListener("indexmore")) {
				mVideoSquareManager.removeVideoSquareManagerListener("indexmore");
			}
			mVideoSquareManager.addVideoSquareManagerListener("indexmore", this);
		}
	}
	
	/**
	 * 获取用户个人信息
	 */
	private void sendGetUserHomeRequest(){
		
		UserInfohomeRequest request = new UserInfohomeRequest(IPageNotifyFn.PageType_UserinfoHome, this);
		
		if((ma.mApp.isUserLoginSucess == true || ma.mApp.registStatus == 2) && !TextUtils.isEmpty(userUId) ){
			request.get("100", userUId, userUId);
		}
	}

	/**
	 * 个人资料信息
	 */
	public void initData() {
		if (null == ma || null == ma.mApp) {
			return;
		}

		UserInfo userInfo = ma.mApp.getMyInfo();
		if (null != userInfo) {
			userHead = userInfo.head;
			userName = userInfo.nickName;
			userDesc = userInfo.desc;
			newFansCout = userInfo.newfansnumber;
			userUId = userInfo.uid;
			userSex = userInfo.sex;
			customavatar = userInfo.customavatar;
			userPhone = userInfo.phone;

			if (customavatar != null && !"".equals(customavatar)) {
				mImageHead.setImageURI(Uri.parse(customavatar));
				GlideUtils.loadNetHead(getActivity(), mImageHead, customavatar, R.drawable.editor_head_feault7);
			} else {
				showHead(mImageHead, userHead);
			}
			if (null != userInfo.mUserLabel) {
				mImageAuthentication.setVisibility(View.VISIBLE);
				if ("1".equals(userInfo.mUserLabel.approvelabel)) {
					mImageAuthentication.setImageResource(R.drawable.authentication_bluev_icon);
				} else if ("1".equals(userInfo.mUserLabel.headplusv)) {
					mImageAuthentication.setImageResource(R.drawable.authentication_yellowv_icon);
				} else if ("1".equals(userInfo.mUserLabel.tarento)) {
					mImageAuthentication.setImageResource(R.drawable.authentication_star_icon);
				} else {
					mImageAuthentication.setVisibility(View.GONE);
				}
			} else {
				mImageAuthentication.setVisibility(View.GONE);
			}

			mTextName.setText(userName);
			GolukDebugUtils.i("lily", userHead);

			if ("".equals(userDesc) || null == userDesc) {
				mTextId.setText(getActivity().getResources().getString(R.string.str_let_sharevideo));
			} else {
				mTextId.setText(userDesc);
			}
			mTextId.setTextColor(Color.rgb(0, 0, 0));
//			mTextShare.setText(GolukUtils.getFormatNumber(0));
//			mTextFans.setText(GolukUtils.getFormatNumber(0));
//			mTextFollow.setText(GolukUtils.getFormatNumber(0));
			if (newFansCout > 0) {
				Drawable redPoint = getActivity().getResources().getDrawable(R.drawable.home_red_point_little);
				redPoint.setBounds(0, 0, redPoint.getMinimumWidth(), redPoint.getMinimumHeight());
				mTextFans.setCompoundDrawables(null, null, redPoint, null);
			} else {
				mTextFans.setCompoundDrawables(null, null, null, null);
			}
			// 获取用户信息
			boolean b = GolukApplication.getInstance().getVideoSquareManager().getUserInfo(userUId);
			GolukDebugUtils.e("", "=======IndexMoreActivity====b：" + b);
		}
	}

	private void showHead(ImageView view, String headportrait) {
		try {
			GlideUtils.loadLocalHead(getActivity(), view, ILive.mBigHeadImg[Integer.parseInt(headportrait)]);
		} catch (Exception e) {
			GlideUtils.loadLocalHead(getActivity(), view, R.drawable.editor_head_feault7);
		}
	}

	@Override
	public void VideoSuqare_CallBack(int event, int msg, int param1, Object param2) {
		if (event == VSquare_Req_MainPage_UserInfor) {
			if (RESULE_SUCESS == msg) {
				try {
					String jsonStr = (String) param2;
					GolukDebugUtils.e("", "=======VideoSuqare_CallBack====jsonStr：" + jsonStr);
					JSONObject dataObj = new JSONObject(jsonStr);
					JSONObject data = dataObj.optJSONObject("data");
					String praisemenumber = data.optString("praisemenumber");
					String sharevideonumber = data.optString("sharevideonumber");
					GolukDebugUtils.e("", "=======VideoSuqare_CallBack====praisemenumber：" + praisemenumber);
					if ("".equals(praisemenumber)) {
						praisemenumber = "0";
					}
					if ("".equals(sharevideonumber)) {
						sharevideonumber = "0";
					}
					//mTextFans.setText(GolukUtils.getFormatNumber(praisemenumber));
					//mTextShare.setText(GolukUtils.getFormatNumber(sharevideonumber));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

	}

	/**
	 * 自动登录状态变化 对话框消失
	 */
	@Override
	public void statusChange() {
		if (ma.mApp.autoLoginStatus == 2) {
			dismissDialog();
			personalChanged();
		} else if (ma.mApp.autoLoginStatus == 3 || ma.mApp.autoLoginStatus == 4 || ma.mApp.isUserLoginSucess == false) {
			dismissDialog();
			personalChanged();
			mVideoLayout.setVisibility(View.GONE);
			mImageAuthentication.setVisibility(View.GONE);
			mTextName.setText(getActivity().getResources().getString(R.string.str_click_to_login));
			mTextId.setTextColor(Color.rgb(128, 138, 135));
			mTextId.setText(getActivity().getResources().getString(R.string.str_login_tosee_usercenter));
		} else if (ma.mApp.autoLoginStatus == 5) {
			mVideoLayout.setVisibility(View.VISIBLE);
			mImageAuthentication.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * 个人中心状态的变化
	 */
	public void personalChanged() {
		GolukDebugUtils.i("lily", "======registStatus====" + ma.mApp.registStatus);
		if (ma.mApp.autoLoginStatus == 3 || ma.mApp.autoLoginStatus == 4) {
			mVideoLayout.setVisibility(View.GONE);
			mImageAuthentication.setVisibility(View.GONE);
			mTextName.setText(getActivity().getResources().getString(R.string.str_click_to_login));
			mTextId.setTextColor(Color.rgb(128, 138, 135));
			mTextId.setText(getActivity().getResources().getString(R.string.str_login_tosee_usercenter));
			showHead(mImageHead, "7");
			return;
		}
		if (ma.mApp.loginStatus == 1 || ma.mApp.autoLoginStatus == 1 || ma.mApp.autoLoginStatus == 2) {// 登录成功、自动登录中、自动登录成功
			mVideoLayout.setVisibility(View.VISIBLE);
			mImageAuthentication.setVisibility(View.VISIBLE);
			showHead(mImageHead, "7");
			initData();
		} else {// 没有用户信息
			mVideoLayout.setVisibility(View.GONE);
			mImageAuthentication.setVisibility(View.GONE);
			mTextName.setText(getActivity().getResources().getString(R.string.str_click_to_login));
			mTextId.setTextColor(Color.rgb(128, 138, 135));
			mTextId.setText(getActivity().getResources().getString(R.string.str_login_tosee_usercenter));
			showHead(mImageHead, "7");
		}
	}

	@Override
	public void onLoadComplete(int requestType, Object result) {
		// TODO Auto-generated method stub
		if (requestType == IPageNotifyFn.PageType_UserinfoHome) {
			mUserinfohomeRetBean = (UserinfohomeRetBean) result;
			if (null != mUserinfohomeRetBean && null != mUserinfohomeRetBean.data ) {
				if((ma.mApp.isUserLoginSucess == true || ma.mApp.registStatus == 2) && !TextUtils.isEmpty(userUId) ){
					mTextShare.setText(GolukUtils.getFormatNumber(mUserinfohomeRetBean.data.sharevideonumber));
					mTextFans.setText(GolukUtils.getFormatNumber(mUserinfohomeRetBean.data.fansnumber));
					mTextFollow.setText(GolukUtils.getFormatNumber(mUserinfohomeRetBean.data.followingnumber));

					int newFansNumber = Integer.valueOf(mUserinfohomeRetBean.data.newfansnumber);
					if(newFansNumber>0){
						mNewFansIv.setVisibility(View.VISIBLE);
					}else{
						mNewFansIv.setVisibility(View.GONE);
					}
				}
				
			}
		}
	}

}
