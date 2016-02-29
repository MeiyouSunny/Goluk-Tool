package cn.com.mobnote.golukmobile;

import org.json.JSONObject;

import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.live.ILive;
import cn.com.mobnote.golukmobile.live.UserInfo;
import cn.com.mobnote.golukmobile.msg.MessageCenterActivity;
import cn.com.mobnote.golukmobile.photoalbum.PhotoAlbumActivity;
import cn.com.mobnote.golukmobile.praised.MyPraisedActivity;
import cn.com.mobnote.golukmobile.profit.MyProfitActivity;
import cn.com.mobnote.golukmobile.usercenter.UCUserInfo;
import cn.com.mobnote.golukmobile.usercenter.UserCenterActivity;
import cn.com.mobnote.golukmobile.videosuqare.VideoSquareManager;
import cn.com.mobnote.module.videosquare.VideoSuqareManagerFn;
import android.net.Uri;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.mobnote.user.UserInterface;
import cn.com.mobnote.util.GlideUtils;
import cn.com.mobnote.util.GolukUtils;
import cn.com.tiros.debug.GolukDebugUtils;

/**
 * 
 * @ 功能描述:Goluk首页更多页面
 * 
 * @author 陈宣宇
 * 
 */

@SuppressLint({ "HandlerLeak", "Instantiatable" })
public class IndexMoreActivity implements OnClickListener, UserInterface, VideoSuqareManagerFn {
	/** 上下文 */
	private Context mContext = null;

	/** 个人中心 **/
	private RelativeLayout mUserCenterItem = null;
	/** 未登录不显示用户id **/
//	private RelativeLayout mUserCenterId = null;
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
	/** 购买极路客 **/
	private TextView mShoppingItem = null;
	/**我的收益**/
	private TextView mProfitItem = null;
	private RelativeLayout mMsgCenterItem = null;
	private TextView mPraisedListItem = null;

	/** 个人中心的头像、性别、昵称 */
	private ImageView mImageHead, mImageAuthentication;
	private TextView mTextName, mTextId;
	private LinearLayout mVideoLayout;
	private TextView mTextShare, mTextPraise;
	/** 分享视频 赞我的人 **/
	private LinearLayout mShareLayout, mPraiseLayout;

	/** 自动登录中的loading提示框 **/
	private Builder mBuilder = null;
	private SharedPreferences mPreferences = null;
	private boolean isFirstLogin;
	private Editor mEditor = null;
	RelativeLayout mRootLayout = null;
	private MainActivity ma;

	/** 用户信息 **/
	private String userHead, userName, userDesc, userUId, userSex,customavatar, userPhone;
	private int shareCount, praiseCount;
	
	/**个人中心**/
	private static final int TYPE_USER = 1;
	/**分享视频／赞我的人**/
	private static final int TYPE_SHARE_PRAISE = 2;
	/**我的收益**/
	private static final int TYPE_PROFIT = 3;
	private static final String TAG = "IndexMoreActivity";

	public IndexMoreActivity(RelativeLayout rootlayout, Context context) {
		mRootLayout = rootlayout;
		mContext = context;
		ma = (MainActivity) mContext;
		setListener();

		mPreferences = mContext.getSharedPreferences("firstLogin", Context.MODE_PRIVATE);
		isFirstLogin = mPreferences.getBoolean("FirstLogin", true);

		ma.mApp.mUser.setUserInterface(this);
		init();
	}

	public void showView() {
		mPreferences = mContext.getSharedPreferences("firstLogin", Context.MODE_PRIVATE);
		isFirstLogin = mPreferences.getBoolean("FirstLogin", true);

		ma.mApp.mUser.setUserInterface(this);
		getDataState();
	}

	/**
	 * 页面初始化
	 */
	private void init() {
		// 获取页面元素

		// 个人中心 我的相册 摄像头管理 通用设置 极路客小技巧 安装指导 版本信息 购买极路客
		mUserCenterItem = (RelativeLayout) mRootLayout.findViewById(R.id.user_center_item);
//		mUserCenterId = (RelativeLayout) mRootLayout.findViewById(R.id.user_center_id_layout);
		mVideoItem = (TextView) mRootLayout.findViewById(R.id.video_item);
		mCameraItem = (TextView) mRootLayout.findViewById(R.id.camera_item);
		mSetItem = (TextView) mRootLayout.findViewById(R.id.set_item);
		mSkillItem = (TextView) mRootLayout.findViewById(R.id.skill_item);
		mInstallItem = (TextView) mRootLayout.findViewById(R.id.install_item);
		mQuestionItem = (TextView) mRootLayout.findViewById(R.id.question_item);
		mShoppingItem = (TextView) mRootLayout.findViewById(R.id.shopping_item);
		mProfitItem = (TextView) mRootLayout.findViewById(R.id.profit_item);
		mMsgCenterItem = (RelativeLayout) mRootLayout.findViewById(R.id.rl_my_message);
		mPraisedListItem = (TextView)mRootLayout.findViewById(R.id.tv_praise_item);

		// 头像、昵称、id
		mImageHead = (ImageView) mRootLayout.findViewById(R.id.user_center_head);
		mImageAuthentication = (ImageView) mRootLayout.findViewById(R.id.im_user_center_head_authentication);
		mTextName = (TextView) mRootLayout.findViewById(R.id.user_center_name_text);
		mTextId = (TextView) mRootLayout.findViewById(R.id.user_center_id_text);
		mVideoLayout = (LinearLayout) mRootLayout.findViewById(R.id.user_center_video_layout);
		mTextShare = (TextView) mRootLayout.findViewById(R.id.user_share_count);
		mTextPraise = (TextView) mRootLayout.findViewById(R.id.user_praise_count);
		mShareLayout = (LinearLayout) mRootLayout.findViewById(R.id.user_share);
		mPraiseLayout = (LinearLayout) mRootLayout.findViewById(R.id.user_praise);

		// 注册事件
		// 个人中心 我的相册 摄像头管理 通用设置 极路客小技巧 安装指导 版本信息 购买极路客
		mUserCenterItem.setOnClickListener(this);
		mVideoItem.setOnClickListener(this);
		mCameraItem.setOnClickListener(this);
		mSetItem.setOnClickListener(this);
		mSkillItem.setOnClickListener(this);
		mInstallItem.setOnClickListener(this);
		mQuestionItem.setOnClickListener(this);
		mShoppingItem.setOnClickListener(this);
		mShareLayout.setOnClickListener(this);
		mPraiseLayout.setOnClickListener(this);
		mProfitItem.setOnClickListener(this);
		mMsgCenterItem.setOnClickListener(this);
		mPraisedListItem.setOnClickListener(this);
	}

	// 获取登录状态及用户信息
	private void getDataState() {
		GolukDebugUtils.i("lily", "--------" + ma.mApp.autoLoginStatus + ma.mApp.isUserLoginSucess
				+ "=====mApp.registStatus ====" + ma.mApp.registStatus);
		if (!isFirstLogin || ma.mApp.isUserLoginSucess == true || ma.mApp.registStatus == 2) {// 登录过
			GolukDebugUtils.i("lily", "---------------" + ma.mApp.autoLoginStatus + "------loginStatus------"
					+ ma.mApp.loginStatus);
			// 更多页面
			personalChanged();
		} else {
			// 未登录
			mVideoLayout.setVisibility(View.GONE);
			mImageAuthentication.setVisibility(View.GONE);
			this.showHead(mImageHead, "7");
			mTextName.setText(mContext.getResources().getString(R.string.str_click_to_login));
			mTextId.setTextColor(Color.rgb(128, 138, 135));
			mTextId.setText(mContext.getResources().getString(R.string.str_login_tosee_usercenter));
		}
	}

	protected void onResume() {
		mPreferences = mContext.getSharedPreferences("firstLogin", Context.MODE_PRIVATE);
		isFirstLogin = mPreferences.getBoolean("FirstLogin", true);

		// 获得GolukApplication对象
		// mApp = (GolukApplication) getApplication();
		// mApp.setContext(mContext, "IndexMore");

		ma.mApp.mUser.setUserInterface(this);

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
			clickAuto(TYPE_SHARE_PRAISE, 0);
			break;
		case R.id.user_praise:
			clickAuto(TYPE_SHARE_PRAISE, 1);
			break;
		// 点击跳转到我的主页
		case R.id.user_center_item:
			clickAuto(TYPE_USER, 0);
			break;
		// 我的相册
		case R.id.video_item:
			ma.mApp.mUser.setUserInterface(null);
			intent = new Intent(mContext, PhotoAlbumActivity.class);
			intent.putExtra("from", "local");
			mContext.startActivity(intent);
			break;
		// 摄像头管理
		case R.id.camera_item:
			Intent itCamera = new Intent(mContext, UnbindActivity.class);
			mContext.startActivity(itCamera);
			break;
		// 通用设置
		case R.id.set_item:
			Intent itSet = new Intent(mContext, UserSetupActivity.class);
			mContext.startActivity(itSet);
			break;
		// 极路客小技巧
		case R.id.skill_item:
			Intent itSkill = new Intent(mContext, UserOpenUrlActivity.class);
			itSkill.putExtra(UserOpenUrlActivity.FROM_TAG, "skill");
			mContext.startActivity(itSkill);
			break;
		// 安装指导
		case R.id.install_item:
			Intent itInstall = new Intent(mContext, UserOpenUrlActivity.class);
			itInstall.putExtra(UserOpenUrlActivity.FROM_TAG, "install");
			mContext.startActivity(itInstall);
			break;
		// 版本信息
		case R.id.question_item:
			Intent itQuestion = new Intent(mContext, UserVersionActivity.class);
			mContext.startActivity(itQuestion);
			break;
		// 购买极路客
		case R.id.shopping_item:
			Intent itShopping = new Intent(mContext, UserOpenUrlActivity.class);
			itShopping.putExtra(UserOpenUrlActivity.FROM_TAG, "shopping");
			mContext.startActivity(itShopping);
			break;
		//我的收益
		case R.id.profit_item:
			clickAuto(TYPE_PROFIT, 0);
			break;
		case R.id.rl_my_message:
			Intent msgIntent = new Intent(mContext, MessageCenterActivity.class);
			mContext.startActivity(msgIntent);
			break;
		case R.id.tv_praise_item:
			Intent praiseIntent = new Intent(mContext, MyPraisedActivity.class);
			mContext.startActivity(praiseIntent);
			break;
		default:
			GolukDebugUtils.d(TAG, "unknown view clicked");
			break;
		}
	}
	
	/**
	 * 
	 * @param type	点击个人中心、分享视频赞我的人、我的收益
	 * @param shareOrPraise	０分享视频　　１赞我的人
	 */
	private void clickAuto(int type,int shareOrPraise) {
		if (!isFirstLogin && (ma.mApp.loginStatus == 1 || ma.mApp.registStatus == 2 
				|| ma.mApp.autoLoginStatus == 1 || ma.mApp.autoLoginStatus == 2)) {// 登录过
			if (ma.mApp.autoLoginStatus == 1 || ma.mApp.autoLoginStatus == 4) {
				mBuilder = new AlertDialog.Builder(mContext);
				dialog = mBuilder.setMessage(mContext.getResources().getString(R.string.user_personal_autoloading_progress)).create();
				dialog.show();
			} else if (ma.mApp.autoLoginStatus == 2 || ma.mApp.isUserLoginSucess) {
				if(type == TYPE_USER) {
					intentToUserCenter(shareOrPraise);
				} else if(type == TYPE_SHARE_PRAISE) {
					intentToUserCenter(shareOrPraise);
				} else if(type == TYPE_PROFIT) {
					Intent itProfit = new Intent(mContext,MyProfitActivity.class);
//					itProfit.putExtra("uid", userUId);
//					itProfit.putExtra("phone", userPhone);
					mContext.startActivity(itProfit);
				}
			} else {
				clickToLogin(type);
			}
		} else {
			clickToLogin(type);
		}
	}
	
	/**
	 * 跳转登录页
	 * @param intentType
	 */
	private void clickToLogin(int intentType) {
		mPreferences = mContext.getSharedPreferences("toRepwd", Context.MODE_PRIVATE);
		mEditor = mPreferences.edit();
		Intent itNo = new Intent(mContext, UserLoginActivity.class);
		if(intentType == TYPE_USER) {
			itNo.putExtra("isInfo", "indexmore");
			mEditor.putString("toRepwd", "more");
		} else if(intentType == TYPE_PROFIT) {
			// 登录页回调判断
			itNo.putExtra("isInfo", "profit");
			mEditor.putString("toRepwd", "toProfit");
		}
		mEditor.commit();

		mContext.startActivity(itNo);
	}
	
	/**
	 * 点击个人中心跳转到个人主页
	 */
	private void intentToUserCenter(int type) {
		UCUserInfo user = new UCUserInfo();
		user.uid = userUId;
		user.nickname = userName;
		user.headportrait = userHead;
		user.introduce = userDesc;
		user.sex = userSex;
		user.customavatar = customavatar;
		user.praisemenumber = praiseCount + "";
		user.sharevideonumber = shareCount + "";

		Intent intent = new Intent(mContext, UserCenterActivity.class);
		intent.putExtra("userinfo", user);
		intent.putExtra("type", type);
		mContext.startActivity(intent);
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
	 * 个人资料信息
	 */
	public void initData() {
		if(null == ma || null == ma.mApp) {
			return;
		}

		UserInfo userInfo = ma.mApp.getMyInfo();
		if(null != userInfo) {
			userHead = userInfo.head;
			userName = userInfo.nickName;
			userDesc = userInfo.desc;
			shareCount = userInfo.sharevideonumber;
			praiseCount = userInfo.praisemenumber;
			userUId = userInfo.uid;
			userSex = userInfo.sex;
			customavatar = userInfo.customavatar;
			userPhone = userInfo.phone;
			
			if(customavatar != null && !"".equals(customavatar)){
				mImageHead.setImageURI(Uri.parse(customavatar));
				GlideUtils.loadNetHead(mContext, mImageHead, customavatar, R.drawable.editor_head_feault7);
			}else{
				showHead(mImageHead,userHead);
			}
			if(null != userInfo.mUserLabel) {
				mImageAuthentication.setVisibility(View.VISIBLE);
				if("1".equals(userInfo.mUserLabel.approvelabel)) {
					mImageAuthentication.setImageResource(R.drawable.authentication_bluev_icon);
				} else if( "1".equals(userInfo.mUserLabel.headplusv)) {
					mImageAuthentication.setImageResource(R.drawable.authentication_yellowv_icon);
				} else if("1".equals(userInfo.mUserLabel.tarento)) {
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
				mTextId.setText(mContext.getResources().getString(R.string.str_let_sharevideo));
			} else {
				mTextId.setText(userDesc);
			}
			mTextId.setTextColor(Color.rgb(0, 0, 0));
			mTextShare.setText(GolukUtils.getFormatNumber(shareCount + ""));
			mTextPraise.setText(GolukUtils.getFormatNumber(praiseCount + ""));

			// 获取用户信息
			boolean b = GolukApplication.getInstance().getVideoSquareManager().getUserInfo(userUId);
			GolukDebugUtils.e("", "=======IndexMoreActivity====b：" + b);
		}
	}
	
	private void showHead(ImageView view, String headportrait) {
		try {
			GlideUtils.loadLocalHead(mContext, view, ILive.mBigHeadImg[Integer.parseInt(headportrait)]);
		} catch (Exception e) {
			GlideUtils.loadLocalHead(mContext, view, R.drawable.editor_head_feault7);
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
					if("".equals(praisemenumber)) {
						praisemenumber = "0";
					}
					if("".equals(sharevideonumber)) {
						sharevideonumber = "0";
					}
					mTextPraise.setText(GolukUtils.getFormatNumber(praisemenumber));
					mTextShare.setText(GolukUtils.getFormatNumber(sharevideonumber));
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
			mTextName.setText(mContext.getResources().getString(R.string.str_click_to_login));
			mTextId.setTextColor(Color.rgb(128, 138, 135));
			mTextId.setText(mContext.getResources().getString(R.string.str_login_tosee_usercenter));
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
		if(ma.mApp.autoLoginStatus == 3 || ma.mApp.autoLoginStatus == 4) {
			mVideoLayout.setVisibility(View.GONE);
			mImageAuthentication.setVisibility(View.GONE);
			mTextName.setText(mContext.getResources().getString(R.string.str_click_to_login));
			mTextId.setTextColor(Color.rgb(128, 138, 135));
			mTextId.setText(mContext.getResources().getString(R.string.str_login_tosee_usercenter));
			showHead(mImageHead, "7");
			return ;
		}
		if (ma.mApp.loginStatus == 1 || ma.mApp.autoLoginStatus == 1 || ma.mApp.autoLoginStatus == 2) {// 登录成功、自动登录中、自动登录成功
			mVideoLayout.setVisibility(View.VISIBLE);
			mImageAuthentication.setVisibility(View.VISIBLE);
			showHead(mImageHead, "7");
			initData();
		} else {// 没有用户信息
			mVideoLayout.setVisibility(View.GONE);
			mImageAuthentication.setVisibility(View.GONE);
			mTextName.setText(mContext.getResources().getString(R.string.str_click_to_login));
			mTextId.setTextColor(Color.rgb(128, 138, 135));
			mTextId.setText(mContext.getResources().getString(R.string.str_login_tosee_usercenter));
			showHead(mImageHead, "7");
		}
	}

}
