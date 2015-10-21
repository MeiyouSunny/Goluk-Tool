package cn.com.mobnote.golukmobile;

import org.json.JSONObject;

import u.aly.da;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.photoalbum.PhotoAlbumActivity;
import cn.com.mobnote.golukmobile.usercenter.UCUserInfo;
import cn.com.mobnote.golukmobile.usercenter.UserCenterActivity;
import cn.com.mobnote.golukmobile.videosuqare.VideoSquareManager;
import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.videosquare.VideoSuqareManagerFn;
import cn.com.mobnote.user.UserUtils;
import android.os.Handler;
import android.os.Message;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.mobnote.user.UserInterface;
import cn.com.mobnote.util.GolukUtils;
import cn.com.tiros.debug.GolukDebugUtils;

/**
 * <pre>
 * 1.类命名首字母大写
 * 2.公共函数驼峰式命名
 * 3.属性函数驼峰式命名
 * 4.变量/参数驼峰式命名
 * 5.操作符之间必须加空格
 * 6.注释都在行首写.(枚举除外)
 * 7.编辑器必须显示空白处
 * 8.所有代码必须使用TAB键缩进
 * 9.函数使用块注释,代码逻辑使用行注释
 * 10.文件头部必须写功能说明
 * 11.后续人员开发保证代码格式一致
 * </pre>
 * 
 * @ 功能描述:Goluk首页更多页面
 * 
 * @author 陈宣宇
 * 
 */

@SuppressLint({ "HandlerLeak", "Instantiatable" })
public class IndexMoreActivity implements OnClickListener, UserInterface, VideoSuqareManagerFn {
	/** application */
	// private GolukApplication mApp = null;
	/** 上下文 */
	private Context mContext = null;

	/** 个人中心 **/
	private RelativeLayout mUserCenterItem = null;
	/** 未登录不显示用户id **/
	private RelativeLayout mUserCenterId = null;
	/** 我的相册 **/
	private RelativeLayout mVideoItem = null;
	/** 摄像头管理 **/
	private RelativeLayout mCameraItem = null;
	/** 通用设置 **/
	private RelativeLayout mSetItem = null;
	/** 极路客小技巧 **/
	private RelativeLayout mSkillItem = null;
	/** 安装指导 **/
	private RelativeLayout mInstallItem = null;
	/** 版本信息 **/
	private RelativeLayout mQuestionItem = null;
	/** 购买极路客 **/
	private RelativeLayout mShoppingItem = null;

	/** 个人中心的头像、性别、昵称 */
	private ImageView mImageHead;
	private TextView mTextName, mTextId;
	private LinearLayout mVideoLayout;
	private TextView mTextShare, mTextPraise;
	/** 分享视频 赞我的人 **/
	private LinearLayout mShareLayout, mPraiseLayout;

	/** 自动登录中的loading提示框 **/
	private Builder mBuilder = null;
	private SharedPreferences mPreferences = null;
	private boolean isFirstLogin;
	private boolean isHasInfo = false;
	private Editor mEditor = null;
	RelativeLayout mRootLayout = null;
	private MainActivity ma;

	/** 用户信息 **/
	private String userHead, userName, userId, userDesc, userUId, userSex;
	private int shareCount, praiseCount;

	public IndexMoreActivity(RelativeLayout rootlayout, Context context) {
		mRootLayout = rootlayout;
		mContext = context;
		ma = (MainActivity) mContext;
		setListener();

		mPreferences = mContext.getSharedPreferences("firstLogin", mContext.MODE_PRIVATE);
		isFirstLogin = mPreferences.getBoolean("FirstLogin", true);

		ma.mApp.mUser.setUserInterface(this);
		init();
	}

	public void showView() {
		mPreferences = mContext.getSharedPreferences("firstLogin", mContext.MODE_PRIVATE);
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
		mUserCenterId = (RelativeLayout) mRootLayout.findViewById(R.id.user_center_id_layout);
		mVideoItem = (RelativeLayout) mRootLayout.findViewById(R.id.video_item);
		mCameraItem = (RelativeLayout) mRootLayout.findViewById(R.id.camera_item);
		mSetItem = (RelativeLayout) mRootLayout.findViewById(R.id.set_item);
		mSkillItem = (RelativeLayout) mRootLayout.findViewById(R.id.skill_item);
		mInstallItem = (RelativeLayout) mRootLayout.findViewById(R.id.install_item);
		mQuestionItem = (RelativeLayout) mRootLayout.findViewById(R.id.question_item);
		mShoppingItem = (RelativeLayout) mRootLayout.findViewById(R.id.shopping_item);

		// 头像、昵称、id
		mImageHead = (ImageView) mRootLayout.findViewById(R.id.user_center_head);
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
			isHasInfo = false;
			mVideoLayout.setVisibility(View.GONE);
			mImageHead.setImageResource(R.drawable.editor_head_feault7);
			mTextName.setText("点击登录");
			mTextId.setTextColor(Color.rgb(128, 138, 135));
			mTextId.setText("登录查看个人主页");
		}
	}

	protected void onResume() {
		mPreferences = mContext.getSharedPreferences("firstLogin", mContext.MODE_PRIVATE);
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
			intentToUserCenter(0);
			break;
		case R.id.user_praise:
			intentToUserCenter(1);
			break;
		// 点击跳转到我的主页
		case R.id.user_center_item:
			// 自动登录中，成功，失败，超时、密码错误
			GolukDebugUtils.i("lily", "-----autoLoginStatus-----" + ma.mApp.autoLoginStatus
					+ "------isUserLoginSuccess------" + ma.mApp.isUserLoginSucess);
			if (isHasInfo && (ma.mApp.loginoutStatus == false || ma.mApp.registStatus == 2)) {
				if (ma.mApp.autoLoginStatus == 1 || ma.mApp.autoLoginStatus == 4) {
					mBuilder = new AlertDialog.Builder(mContext);
					dialog = mBuilder.setMessage("正在为您登录，请稍候……").setCancelable(false)
							.setOnKeyListener(new OnKeyListener() {
								@Override
								public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
									if (keyCode == KeyEvent.KEYCODE_BACK) {
										return true;
									}
									return false;
								}
							}).create();
					dialog.show();
				} else if (ma.mApp.autoLoginStatus == 2 || ma.mApp.isUserLoginSucess) {
					GolukDebugUtils.i("lily", "--------更多页面------");

					intentToUserCenter(0);
				}
			} else {
				GolukDebugUtils.i("lily", "-------用户登出成功,跳转登录页------" + ma.mApp.autoLoginStatus);
				Intent itNo = new Intent(mContext, UserLoginActivity.class);
				// 登录页回调判断
				itNo.putExtra("isInfo", "indexmore");

				mPreferences = mContext.getSharedPreferences("toRepwd", Context.MODE_PRIVATE);
				mEditor = mPreferences.edit();
				mEditor.putString("toRepwd", "more");
				mEditor.commit();

				mContext.startActivity(itNo);
				isHasInfo = true;
			}
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
		}
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
		user.customavatar = "";
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
		String info = ma.mApp.mGoluk.GolukLogicCommGet(GolukModule.Goluk_Module_HttpPage, 0, "");
		GolukDebugUtils.i("lily", "---IndexMore--------" + info);
		try {
			JSONObject json = new JSONObject(info);
			userHead = json.getString("head");
			userName = json.getString("nickname");
			userId = json.getString("key");
			userDesc = json.getString("desc");
			shareCount = json.getInt("sharevideonumber");
			praiseCount = json.getInt("praisemenumber");
			userUId = json.getString("uid");
			userSex = json.getString("sex");

			mTextName.setText(userName);
			GolukDebugUtils.i("lily", userHead);
//			UserUtils.focusHead(userHead, mImageHead);
			if ("".equals(userDesc) || null == userDesc) {
				mTextId.setText("大家一起来分享视频吧");
			} else {
				mTextId.setText(userDesc);
			}
			mTextId.setTextColor(Color.rgb(0, 0, 0));
			mTextShare.setText(GolukUtils.getFormatNumber(shareCount + ""));
			mTextPraise.setText(GolukUtils.getFormatNumber(praiseCount + ""));

			// 获取用户信息
			boolean b = GolukApplication.getInstance().getVideoSquareManager().getUserInfo(userUId);
			GolukDebugUtils.e("", "=======IndexMoreActivity====b：" + b);

		} catch (Exception e) {
			e.printStackTrace();
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
		GolukDebugUtils.i("lily", "----IndexMoreActivity---自动登录个人中心变化---statusChange()-----mApp.autoLoginStatus-----"
				+ ma.mApp.autoLoginStatus);
		if (ma.mApp.autoLoginStatus == 2) {
			dismissDialog();
			GolukDebugUtils.i("lily", "-------IndexMoreActivity-----自动登录个人中心变化--------当autoLoginStatus==2时----");
			personalChanged();
		} else if (ma.mApp.autoLoginStatus == 3 || ma.mApp.autoLoginStatus == 4 || ma.mApp.isUserLoginSucess == false) {
			dismissDialog();
			personalChanged();
			mVideoLayout.setVisibility(View.GONE);
			mTextName.setText("点击登录");
			mTextId.setTextColor(Color.rgb(128, 138, 135));
			mTextId.setText("登录查看个人主页");
		} else if (ma.mApp.autoLoginStatus == 5) {
			mVideoLayout.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * 个人中心状态的变化
	 */
	public void personalChanged() {
		GolukDebugUtils.i("lily", "======registStatus====" + ma.mApp.registStatus);
		if (ma.mApp.loginStatus == 1 || ma.mApp.autoLoginStatus == 1 || ma.mApp.autoLoginStatus == 2) {// 登录成功、自动登录中、自动登录成功
			mVideoLayout.setVisibility(View.VISIBLE);
			mImageHead.setImageResource(R.drawable.my_head_moren7);
			initData();
			isHasInfo = true;
		} else {// 没有用户信息
			mVideoLayout.setVisibility(View.GONE);
			mTextName.setText("点击登录");
			mTextId.setTextColor(Color.rgb(128, 138, 135));
			mTextId.setText("登录查看个人主页");
			mImageHead.setImageResource(R.drawable.my_head_moren7);
			isHasInfo = false;
		}
	}

}
