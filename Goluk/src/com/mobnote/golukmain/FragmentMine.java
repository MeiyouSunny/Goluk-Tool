package com.mobnote.golukmain;

import org.json.JSONObject;

import com.mobnote.application.GolukApplication;
import com.mobnote.eventbus.EventConfig;
import com.mobnote.eventbus.EventMessageUpdate;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.internation.login.InternationUserLoginActivity;
import com.mobnote.golukmain.live.ILive;
import com.mobnote.golukmain.live.UserInfo;
import com.mobnote.golukmain.msg.MessageBadger;
import com.mobnote.golukmain.msg.MessageCenterActivity;
import com.mobnote.golukmain.photoalbum.PhotoAlbumActivity;
import com.mobnote.golukmain.praised.MyPraisedActivity;
import com.mobnote.golukmain.profit.MyProfitActivity;
import com.mobnote.golukmain.userinfohome.UserInfohomeRequest;
import com.mobnote.golukmain.userinfohome.bean.UserLabelBean;
import com.mobnote.golukmain.userinfohome.bean.UserinfohomeRetBean;
import com.mobnote.golukmain.videosuqare.VideoSquareManager;
import com.mobnote.manager.MessageManager;
import com.mobnote.user.UserInterface;
import com.mobnote.util.GlideUtils;
import com.mobnote.util.GolukUtils;

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
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.mobnote.module.videosquare.VideoSuqareManagerFn;
import cn.com.tiros.debug.GolukDebugUtils;
import de.greenrobot.event.EventBus;

/**
 * 
 * @ ????????????:Goluk??????????????????
 * 
 * @author ?????????
 * 
 */

@SuppressLint({ "HandlerLeak", "Instantiatable" })
public class FragmentMine extends Fragment implements OnClickListener,
		UserInterface, VideoSuqareManagerFn, IRequestResultListener {

	/** ???????????? **/
	private RelativeLayout mUserCenterItem = null;
	/** ????????????????????????id **/
	// private RelativeLayout mUserCenterId = null;
	/** ???????????? **/
	private TextView mVideoItem = null;
	/** ??????????????? **/
	private TextView mCameraItem = null;
	/** ???????????? **/
	private TextView mSetItem = null;
	/** ?????????????????? **/
	private TextView mSkillItem = null;
	/** ???????????? **/
	private TextView mInstallItem = null;
	/** ???????????? **/
	private TextView mQuestionItem = null;
	// /** ??????????????? **/
	// private TextView mShoppingItem = null;
	/** ???????????? **/
	private TextView mProfitItem = null;
	/** ???????????? */
	private RelativeLayout mMsgCenterItem = null;
	private TextView mMessageTip;

	private TextView mPraisedListItem = null;

	/** ??????????????????????????????????????? */
	private ImageView mImageHead, mImageAuthentication;
	private TextView mTextName, mTextIntroduction;
	private LinearLayout mVideoLayout;
	private TextView mTextShare, mTextFans, mTextFollow;
	/** ???????????? ???????????? **/
	private LinearLayout mShareLayout, mFansLayout, mFollowLayout;

	/** ??????????????????loading????????? **/
	private SharedPreferences mPreferences = null;
	private Editor mEditor = null;
	// LinearLayout mRootLayout = null;
	private MainActivity ma;

	/** ???????????? **/
	private String userHead, userName, userDesc, userUId, userSex,
			customavatar, userPhone;
	private int newFansCout;

	/** ???????????? **/
	private static final int TYPE_USER = 1;
	/** ??????????????????????????? **/
	private static final int TYPE_SHARE_PRAISE = 2;
	/** ???????????? **/
	private static final int TYPE_PROFIT = 3;
	/** ???????????? **/
	private static final int TYPE_FOLLOWING = 4;
	private static final String TAG = "FragmentMine";

	LinearLayout mMineRootView = null;
	private UserinfohomeRetBean mUserinfohomeRetBean;
	private ImageView mNewFansIv;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		GolukDebugUtils.d(TAG, "onCreate");
		EventBus.getDefault().register(this);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		GolukDebugUtils.d(TAG, "onDestroy");
		EventBus.getDefault().unregister(this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreateView(inflater, container, savedInstanceState);
		GolukDebugUtils.d(TAG, "onCreateView");
		View rootView = inflater.inflate(R.layout.index_more, null);
		mMineRootView = (LinearLayout) rootView;
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
		GolukDebugUtils.d(TAG, "onActivityCreated");
	}

	@Override
	public void onResume() {
		super.onResume();
		GolukDebugUtils.d(TAG, "onResume");
		resetLoginState();
		int msgCount = MessageManager.getMessageManager()
				.getMessageTotalCount();
		setMessageTipCount(msgCount);
		sendGetUserHomeRequest();
	}

	@Override
	public void onDestroyView() {
		// TODO Auto-generated method stub
		super.onDestroyView();
		GolukDebugUtils.d(TAG, "onDestroyView");
		mMineRootView = null;
	}

	@Override
	public void onAttach(Context context) {
		// TODO Auto-generated method stub
		super.onAttach(context);
		GolukDebugUtils.d(TAG, "onAttach, context=" + context);
	}

	@Override
	public void onDetach() {
		// TODO Auto-generated method stub
		super.onDetach();
		GolukDebugUtils.d(TAG, "onDetach");
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		GolukDebugUtils.d(TAG, "onPause");
	}

	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		GolukDebugUtils.d(TAG, "onStart");
	}

	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		GolukDebugUtils.d(TAG, "onStop");
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		// TODO Auto-generated method stub
		super.onHiddenChanged(hidden);
		GolukDebugUtils.d(TAG, "onHiddenChanged");
		if (!hidden) {
			sendGetUserHomeRequest();
		}
	}

	public void onEventMainThread(EventMessageUpdate event) {
		if (null == event) {
			return;
		}

		switch (event.getOpCode()) {
		case EventConfig.MESSAGE_UPDATE:

			int msgCount = MessageManager.getMessageManager()
					.getMessageTotalCount();
			setMessageTipCount(msgCount);
			MessageBadger.sendBadgeNumber(msgCount, getActivity());
			break;
		default:
			break;
		}
	}

	private void setMessageTipCount(int total) {

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
	 * ???????????????
	 */
	private void initView() {

		// ???????????? ???????????? ??????????????? ???????????? ?????????????????? ???????????? ???????????? ???????????????
		mUserCenterItem = (RelativeLayout) mMineRootView
				.findViewById(R.id.user_center_item);
		mVideoItem = (TextView) mMineRootView.findViewById(R.id.video_item);
		mCameraItem = (TextView) mMineRootView.findViewById(R.id.camera_item);
		mSetItem = (TextView) mMineRootView.findViewById(R.id.set_item);
		mSkillItem = (TextView) mMineRootView.findViewById(R.id.skill_item);
		mInstallItem = (TextView) mMineRootView.findViewById(R.id.install_item);
		mQuestionItem = (TextView) mMineRootView
				.findViewById(R.id.question_item);
		mProfitItem = (TextView) mMineRootView.findViewById(R.id.profit_item);
		mMsgCenterItem = (RelativeLayout) mMineRootView
				.findViewById(R.id.rl_my_message);
		mMessageTip = (TextView) mMineRootView
				.findViewById(R.id.tv_my_message_tip);
		mPraisedListItem = (TextView) mMineRootView
				.findViewById(R.id.tv_praise_item);

		// ??????????????????id
		mImageHead = (ImageView) mMineRootView
				.findViewById(R.id.user_center_head);
		mImageAuthentication = (ImageView) mMineRootView
				.findViewById(R.id.im_user_center_head_authentication);
		mTextName = (TextView) mMineRootView
				.findViewById(R.id.user_center_name_text);
		mTextIntroduction = (TextView) mMineRootView
				.findViewById(R.id.user_center_introduction_text);
		mVideoLayout = (LinearLayout) mMineRootView
				.findViewById(R.id.user_center_video_layout);
		mTextShare = (TextView) mMineRootView
				.findViewById(R.id.user_share_count);
		mTextFans = (TextView) mMineRootView.findViewById(R.id.user_fans_count);
		mTextFollow = (TextView) mMineRootView
				.findViewById(R.id.user_follow_count);
		mShareLayout = (LinearLayout) mMineRootView
				.findViewById(R.id.user_share);
		mFansLayout = (LinearLayout) mMineRootView.findViewById(R.id.user_fans);
		mFollowLayout = (LinearLayout) mMineRootView
				.findViewById(R.id.user_follow);
		mNewFansIv = (ImageView) mMineRootView.findViewById(R.id.iv_new_fans);

		// ????????????
		// ???????????? ???????????? ??????????????? ???????????? ?????????????????? ???????????? ???????????? ???????????????
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

	// ?????????????????????????????????
	private void resetLoginState() {

		mPreferences = getActivity().getSharedPreferences("firstLogin",
				Context.MODE_PRIVATE);
		ma.mApp.mUser.setUserInterface(this);

		GolukDebugUtils.i("lily", "--------" + ma.mApp.autoLoginStatus
				+ ma.mApp.isUserLoginSucess + "=====mApp.registStatus ===="
				+ ma.mApp.registStatus);
		if (ma.mApp.isUserLoginSucess == true || ma.mApp.registStatus == 2) {// ?????????
			GolukDebugUtils.i("lily", "---------------"
					+ ma.mApp.autoLoginStatus + "------loginStatus------"
					+ ma.mApp.loginStatus);
			// ????????????
			personalChanged();
		} else {
			// ?????????
			mVideoLayout.setVisibility(View.GONE);
			mImageAuthentication.setVisibility(View.GONE);
			GlideUtils.loadLocalHead(getActivity(), mImageHead,
					R.drawable.usercenter_head_default);
			mTextName.setText(getActivity().getResources().getString(
					R.string.str_click_to_login));
			mTextIntroduction.setTextColor(Color.rgb(128, 138, 135));
			mTextIntroduction.setText(getActivity().getResources().getString(
					R.string.str_login_tosee_usercenter));
		}
	}

	AlertDialog dialog = null;

	@Override
	public void onClick(View v) {
		int id = v.getId();
		Intent intent = null;
		if (id == R.id.back_btn) {
			ma.mApp.mUser.setUserInterface(null);
		} else if (id == R.id.user_share) {
			if(isLoginInfoValid()){
				GolukUtils.startUserCenterActivity(getActivity(), userUId, userName, userHead, customavatar, userSex, userDesc);
			}else{
				clickToLogin(TYPE_SHARE_PRAISE);
			}
		} else if (id == R.id.user_fans) {
			if (isLoginInfoValid()) {
				GolukUtils.startFanListActivity(getActivity(),
						ma.mApp.getMyInfo().uid);
			} else {
				clickToLogin(TYPE_FOLLOWING);
			}
		} else if (id == R.id.user_follow) {
			if (isLoginInfoValid()) {
				GolukUtils.startFollowingListActivity(getActivity(),
						ma.mApp.getMyInfo().uid);
			} else {
				clickToLogin(TYPE_FOLLOWING);
			}
		} else if (id == R.id.user_center_item) {
			if (isLoginInfoValid()) {
				GolukUtils.startUserCenterActivity(getActivity(), userUId,
						userName, userHead, customavatar, userSex, userDesc);
			} else {
				clickToLogin(TYPE_USER);
			}
		} else if (id == R.id.video_item) {
			ma.mApp.mUser.setUserInterface(null);
			intent = new Intent(getActivity(), PhotoAlbumActivity.class);
			intent.putExtra("from", "local");
			getActivity().startActivity(intent);
		} else if (id == R.id.camera_item) {
			Intent itCamera = new Intent(getActivity(), UnbindActivity.class);
			getActivity().startActivity(itCamera);
		} else if (id == R.id.set_item) {
			Intent itSet = new Intent(getActivity(), UserSetupActivity.class);
			getActivity().startActivity(itSet);

		} else if (id == R.id.skill_item) {
			Intent itSkill = new Intent(getActivity(), UserOpenUrlActivity.class);
			itSkill.putExtra(UserOpenUrlActivity.FROM_TAG, "skill");
			getActivity().startActivity(itSkill);
		} else if (id == R.id.install_item) {
			Intent itInstall = new Intent(getActivity(), UserOpenUrlActivity.class);
			itInstall.putExtra(UserOpenUrlActivity.FROM_TAG, "install");
			getActivity().startActivity(itInstall);
		} else if (id == R.id.question_item) {
			Intent itQuestion = new Intent(getActivity(), UserVersionActivity.class);
			getActivity().startActivity(itQuestion);
		} else if (id == R.id.profit_item) {
			if (isLoginInfoValid()) {
				Intent itProfit = new Intent(getActivity(),
						MyProfitActivity.class);
				getActivity().startActivity(itProfit);
			} else {
				clickToLogin(TYPE_PROFIT);
			}
		} else if (id == R.id.rl_my_message) {
			Intent msgIntent = new Intent(getActivity(), MessageCenterActivity.class);
			getActivity().startActivity(msgIntent);
		} else if (id == R.id.tv_praise_item) {
			if (!GolukUtils.isNetworkConnected(getActivity())) {
				Toast.makeText(getActivity(),
						getActivity().getString(R.string.network_error),
						Toast.LENGTH_SHORT).show();
				return;
			}
			GolukApplication app = (GolukApplication) (getActivity())
					.getApplication();
			if (!app.isUserLoginSucess) {
				// GolukUtils.showToast(this,
				// this.getResources().getString(R.string.str_please_login));
				Intent loginIntent = null;
				if(GolukApplication.getInstance().isInteral() == false){
					loginIntent = new Intent(getActivity(), InternationUserLoginActivity.class);
				}else{
					loginIntent = new Intent(getActivity(), UserLoginActivity.class);
				}
				return;
			}
			Intent praiseIntent = new Intent(getActivity(),
					MyPraisedActivity.class);
			getActivity().startActivity(praiseIntent);
		} else {
			GolukDebugUtils.d(TAG, "unknown view clicked");
		}
	}

	/**
	 * ????????????????????????s
	 * 
	 * @return
	 */
	private boolean isLoginInfoValid() {
		if (ma.mApp.loginStatus == 1 || ma.mApp.registStatus == 2
				|| ma.mApp.autoLoginStatus == 2) {// ?????????
			return true;
		} else {
			return false;
		}
	}

	/**
	 * ???????????????
	 * 
	 * @param intentType
	 */
	private void clickToLogin(int intentType) {
		mPreferences = getActivity().getSharedPreferences("toRepwd",
				Context.MODE_PRIVATE);
		mEditor = mPreferences.edit();
		Intent itNo = null;
		if(GolukApplication.getInstance().isInteral() == false){
			itNo = new Intent(getActivity(), InternationUserLoginActivity.class);
		}else{
			 itNo = new Intent(getActivity(), UserLoginActivity.class);
		}
		
		if (intentType == TYPE_USER) {
			itNo.putExtra("isInfo", "indexmore");
			mEditor.putString("toRepwd", "more");
		} else if (intentType == TYPE_PROFIT) {
			// ?????????????????????
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

	// ????????????
	private void setListener() {
		// ????????????
		VideoSquareManager mVideoSquareManager = GolukApplication.getInstance()
				.getVideoSquareManager();
		if (null != mVideoSquareManager) {
			if (mVideoSquareManager
					.checkVideoSquareManagerListener("indexmore")) {
				mVideoSquareManager
						.removeVideoSquareManagerListener("indexmore");
			}
			mVideoSquareManager
					.addVideoSquareManagerListener("indexmore", this);
		}
	}

	/**
	 * ????????????????????????
	 */
	private void sendGetUserHomeRequest() {

		UserInfohomeRequest request = new UserInfohomeRequest(
				IPageNotifyFn.PageType_UserinfoHome, this);

		if ((ma.mApp.isUserLoginSucess == true || ma.mApp.registStatus == 2)
				&& !TextUtils.isEmpty(userUId)) {
			request.get("100", userUId, userUId);
		}
	}

	/**
	 * ??????????????????
	 */
	public void initData() {
		if (null == ma || null == ma.mApp) {
			return;
		}

		UserInfo userInfo = ma.mApp.getMyInfo();
		if (null != userInfo) {
			userHead = userInfo.head;
			userName = userInfo.nickname;
			userDesc = userInfo.desc;
			newFansCout = userInfo.newfansnumber;
			userUId = userInfo.uid;
			userSex = userInfo.sex;
			customavatar = userInfo.customavatar;
			userPhone = userInfo.phone;

			if (customavatar != null && !"".equals(customavatar)) {
				mImageHead.setImageURI(Uri.parse(customavatar));
				GlideUtils.loadNetHead(getActivity(), mImageHead, customavatar,
						R.drawable.editor_head_feault7);
			} else {
				showHead(mImageHead, userHead);
			}
			if (null != userInfo.mUserLabel) {
				mImageAuthentication.setVisibility(View.VISIBLE);
				if ("1".equals(userInfo.mUserLabel.approvelabel)) {
					mImageAuthentication
							.setImageResource(R.drawable.authentication_bluev_icon);
				} else if ("1".equals(userInfo.mUserLabel.headplusv)) {
					mImageAuthentication
							.setImageResource(R.drawable.authentication_yellowv_icon);
				} else if ("1".equals(userInfo.mUserLabel.tarento)) {
					mImageAuthentication
							.setImageResource(R.drawable.authentication_star_icon);
				} else {
					mImageAuthentication.setVisibility(View.GONE);
				}
			} else {
				mImageAuthentication.setVisibility(View.GONE);
			}

			mTextName.setText(userName);
			GolukDebugUtils.i("lily", userHead);

			if ("".equals(userDesc) || null == userDesc) {
				mTextIntroduction.setText(getActivity().getResources()
						.getString(R.string.str_let_sharevideo));
			} else {
				mTextIntroduction.setText(userDesc);
			}
			mTextIntroduction.setTextColor(Color.parseColor("#808080"));
			// mTextShare.setText(GolukUtils.getFormatNumber(0));
			// mTextFans.setText(GolukUtils.getFormatNumber(0));
			// mTextFollow.setText(GolukUtils.getFormatNumber(0));
			if (newFansCout > 0) {
				Drawable redPoint = getActivity().getResources().getDrawable(
						R.drawable.home_red_point_little);
				redPoint.setBounds(0, 0, redPoint.getMinimumWidth(),
						redPoint.getMinimumHeight());
				mTextFans.setCompoundDrawables(null, null, redPoint, null);
			} else {
				mTextFans.setCompoundDrawables(null, null, null, null);
			}
			// ??????????????????
			boolean b = GolukApplication.getInstance().getVideoSquareManager()
					.getUserInfo(userUId);
			GolukDebugUtils.e("", "=======IndexMoreActivity====b???" + b);
		}
	}

	private void showHead(ImageView view, String headportrait) {
		try {
			GlideUtils.loadLocalHead(getActivity(), view,
					ILive.mBigHeadImg[Integer.parseInt(headportrait)]);
		} catch (Exception e) {
			GlideUtils.loadLocalHead(getActivity(), view,
					R.drawable.usercenter_head_default);
		}
	}

	@Override
	public void VideoSuqare_CallBack(int event, int msg, int param1,
			Object param2) {
		if (event == VSquare_Req_MainPage_UserInfor) {
			if (RESULE_SUCESS == msg) {
				try {
					String jsonStr = (String) param2;
					GolukDebugUtils
							.e("", "=======VideoSuqare_CallBack====jsonStr???"
									+ jsonStr);
					JSONObject dataObj = new JSONObject(jsonStr);
					JSONObject data = dataObj.optJSONObject("data");
					String praisemenumber = data.optString("praisemenumber");
					String sharevideonumber = data
							.optString("sharevideonumber");
					GolukDebugUtils.e("",
							"=======VideoSuqare_CallBack====praisemenumber???"
									+ praisemenumber);
					if ("".equals(praisemenumber)) {
						praisemenumber = "0";
					}
					if ("".equals(sharevideonumber)) {
						sharevideonumber = "0";
					}
					// mTextFans.setText(GolukUtils.getFormatNumber(praisemenumber));
					// mTextShare.setText(GolukUtils.getFormatNumber(sharevideonumber));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

	}

	/**
	 * ???????????????????????? ???????????????
	 */
	@Override
	public void statusChange() {
		if (ma.mApp.autoLoginStatus == 2) {
			dismissDialog();
			personalChanged();
		} else if (ma.mApp.autoLoginStatus == 3 || ma.mApp.autoLoginStatus == 4
				|| ma.mApp.isUserLoginSucess == false) {
			dismissDialog();
			personalChanged();
			mVideoLayout.setVisibility(View.GONE);
			mImageAuthentication.setVisibility(View.GONE);
			mTextName.setText(getActivity().getResources().getString(
					R.string.str_click_to_login));
			mTextIntroduction.setTextColor(Color.rgb(128, 138, 135));
			mTextIntroduction.setText(getActivity().getResources().getString(
					R.string.str_login_tosee_usercenter));
		} else if (ma.mApp.autoLoginStatus == 5) {
			mVideoLayout.setVisibility(View.VISIBLE);
			mImageAuthentication.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * ???????????????????????????
	 */
	public void personalChanged() {
		GolukDebugUtils.i("lily", "======registStatus===="
				+ ma.mApp.registStatus);
		if (ma.mApp.autoLoginStatus == 3 || ma.mApp.autoLoginStatus == 4) {
			mVideoLayout.setVisibility(View.GONE);
			mImageAuthentication.setVisibility(View.GONE);
			mTextName.setText(getActivity().getResources().getString(
					R.string.str_click_to_login));
			mTextIntroduction.setTextColor(Color.rgb(128, 138, 135));
			mTextIntroduction.setText(getActivity().getResources().getString(
					R.string.str_login_tosee_usercenter));
			showHead(mImageHead, "7");
			return;
		}
		if (ma.mApp.loginStatus == 1 || ma.mApp.autoLoginStatus == 1
				|| ma.mApp.autoLoginStatus == 2) {// ???????????????????????????????????????????????????
			mVideoLayout.setVisibility(View.VISIBLE);
			mImageAuthentication.setVisibility(View.VISIBLE);
			showHead(mImageHead, "7");
			initData();
		} else {// ??????????????????
			mVideoLayout.setVisibility(View.GONE);
			mImageAuthentication.setVisibility(View.GONE);
			mTextName.setText(getActivity().getResources().getString(
					R.string.str_click_to_login));
			mTextIntroduction.setTextColor(Color.rgb(128, 138, 135));
			mTextIntroduction.setText(getActivity().getResources().getString(
					R.string.str_login_tosee_usercenter));
			showHead(mImageHead, "7");
		}
	}

	@Override
	public void onLoadComplete(int requestType, Object result) {
		// TODO Auto-generated method stub
		if (requestType == IPageNotifyFn.PageType_UserinfoHome) {
			mUserinfohomeRetBean = (UserinfohomeRetBean) result;
			if (null != mUserinfohomeRetBean
					&& null != mUserinfohomeRetBean.data) {
				if ((ma.mApp.isUserLoginSucess == true || ma.mApp.registStatus == 2)
						&& !TextUtils.isEmpty(userUId)) {
					mTextShare
							.setText(GolukUtils
									.getFormatedNumber(mUserinfohomeRetBean.data.sharevideonumber));
					mTextFans
							.setText(GolukUtils
									.getFormatedNumber(mUserinfohomeRetBean.data.fansnumber));
					mTextFollow
							.setText(GolukUtils
									.getFormatedNumber(mUserinfohomeRetBean.data.followingnumber));

					int newFansNumber = 0;
					if (!TextUtils
							.isEmpty(mUserinfohomeRetBean.data.newfansnumber)) {
						newFansNumber = Integer
								.valueOf(mUserinfohomeRetBean.data.newfansnumber);
					}

					if (newFansNumber > 0) {
						mNewFansIv.setVisibility(View.VISIBLE);
					} else {
						mNewFansIv.setVisibility(View.GONE);
					}

					if (mUserinfohomeRetBean.data.user != null
							&& mUserinfohomeRetBean.data.user.label != null) {
						UserLabelBean lable = mUserinfohomeRetBean.data.user.label;

						mImageAuthentication.setVisibility(View.VISIBLE);
						if ("1".equals(lable.approvelabel)) {
							mImageAuthentication
									.setImageResource(R.drawable.authentication_bluev_icon);
						} else if ("1".equals(lable.headplusv)) {
							mImageAuthentication
									.setImageResource(R.drawable.authentication_yellowv_icon);
						} else if ("1".equals(lable.tarento)) {
							mImageAuthentication
									.setImageResource(R.drawable.authentication_star_icon);
						} else {
							mImageAuthentication.setVisibility(View.GONE);
						}
					} else {
						mImageAuthentication.setVisibility(View.GONE);
					}
				}
			}
		}
	}

}
