package cn.com.mobnote.golukmobile;

import org.json.JSONObject;

import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.photoalbum.PhotoAlbumActivity;
import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.user.UserUtils;
import android.os.Bundle;
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
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.mobnote.application.SysApplication;
import cn.com.mobnote.user.UserInterface;
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

@SuppressLint("HandlerLeak")
public class IndexMoreActivity implements OnClickListener, UserInterface {
	/** application */
	//private GolukApplication mApp = null;
	/** 上下文 */
	private Context mContext = null;
	/** 返回按钮 */
	//private ImageButton mLayoutBack = null;

	/** 本地视频item */
	private RelativeLayout mLocalVideoItem = null;
	/** 设置item */
	private RelativeLayout mSetupItem = null;

	/** 个人中心页面handler用来接收消息,更新UI */
	public static Handler mUserCenterHandler = null;

	/** 个人中心点击进入我的主页 */
	private RelativeLayout mLayoutHome;

	/** 个人中心的头像、性别、昵称 */
	private ImageView mImageHead, mImageSex;
	private TextView mTextName;

	/** 自动登录中的loading提示框 **/
	private Builder mBuilder = null;
	private SharedPreferences mPreferences = null;
	private boolean isFirstLogin;
	/** 头部有无信息替换 **/
	private LinearLayout mLayoutHasInfo = null;
	private LinearLayout mLayoutNoInfo = null;
	private boolean isHasInfo = false;
	private Editor mEditor = null;
	RelativeLayout mRootLayout = null;
	private MainActivity ma;
	
	public IndexMoreActivity(RelativeLayout rootlayout, Context context) {
		mRootLayout = rootlayout;
		mContext = context;
		ma = (MainActivity) mContext;
		init();
	}

	/**
	 * 页面初始化
	 */
	private void init() {
		// 获取页面元素
		//mLayoutBack = (ImageButton) mRootLayout.findViewById(R.id.back_btn);

		mLocalVideoItem = (RelativeLayout) mRootLayout.findViewById(R.id.local_video_item);

		mSetupItem = (RelativeLayout) mRootLayout.findViewById(R.id.setup_item);
		// 进入我的主页
		mLayoutHome = (RelativeLayout) mRootLayout.findViewById(R.id.head_layout);
		// 头像、性别、昵称
		mImageHead = (ImageView) mRootLayout.findViewById(R.id.photo_img);
		mImageSex = (ImageView) mRootLayout.findViewById(R.id.user_sex_image);
		mTextName = (TextView) mRootLayout.findViewById(R.id.user_name_text);
		// 头部有无信息的布局替换
		mLayoutHasInfo = (LinearLayout) mRootLayout.findViewById(R.id.index_more_hasinfo);
		mLayoutNoInfo = (LinearLayout) mRootLayout.findViewById(R.id.index_more_noinfo);
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
			mLayoutHasInfo.setVisibility(View.GONE);
			mLayoutNoInfo.setVisibility(View.VISIBLE);
			mImageHead.setImageResource(R.drawable.more_head_no_log_in);
		}

		// 注册事件
		//mLayoutBack.setOnClickListener(this);
		mLocalVideoItem.setOnClickListener(this);
		mSetupItem.setOnClickListener(this);

		mLayoutHome.setOnClickListener(this);

		// 更新UI handler
		mUserCenterHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
			}
		};
	}


	protected void onResume() {
		mPreferences = mContext.getSharedPreferences("firstLogin",mContext. MODE_PRIVATE);
		isFirstLogin = mPreferences.getBoolean("FirstLogin", true);

		//SysApplication.getInstance().addActivity(this);
		// 获得GolukApplication对象
		//mApp = (GolukApplication) getApplication();
		//mApp.setContext(mContext, "IndexMore");

		ma.mApp.mUser.setUserInterface(this);

		// 页面初始化
		init();

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
			//this.finish();
			break;
		case R.id.local_video_item:
			ma.mApp.mUser.setUserInterface(null);
//			intent = new Intent(mContext, LocalVideoListActivity.class);
			intent = new Intent(mContext, PhotoAlbumActivity.class);
			intent.putExtra("from", "local");
			mContext.startActivity(intent);
			break;
		case R.id.setup_item:
			ma.mApp.mUser.setUserInterface(null);
			// 跳转到设置页面
			GolukDebugUtils.i("lily", "onclick---setup--item");
			intent = new Intent(mContext, UserSetupActivity.class);
			mContext.startActivity(intent);
			break;
		// 点击跳转到我的主页
		case R.id.head_layout:
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
					intent = new Intent(mContext, UserPersonalInfoActivity.class);
					mContext.startActivity(intent);
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
		}
	}

	private void dismissDialog() {
		if (null != dialog) {
			dialog.dismiss();
			dialog = null;
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
			String head = json.getString("head");
			String name = json.getString("nickname");
			String sex = json.getString("sex");

			mTextName.setText(name);
			GolukDebugUtils.i("lily", head);
			UserUtils.focusHead(head, mImageHead);
			if (sex.equals("1")) {
				mImageSex.setImageResource(R.drawable.more_man);
			} else if (sex.equals("2")) {
				mImageSex.setImageResource(R.drawable.more_girl);
			} else if (sex.equals("0")) {
				mImageSex.setImageResource(R.drawable.more_no_log_in_icon);
			}

		} catch (Exception e) {
			e.printStackTrace();
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
			mLayoutHasInfo.setVisibility(View.GONE);
			mLayoutNoInfo.setVisibility(View.VISIBLE);
		} else if (ma.mApp.autoLoginStatus == 5) {
			mLayoutHasInfo.setVisibility(View.VISIBLE);
			mLayoutNoInfo.setVisibility(View.GONE);
		}
	}

	/**
	 * 自动登录失败后个人中心状态的变化
	 */
	public void personalChanged() {
		GolukDebugUtils.i("lily", "======registStatus====" + ma.mApp.registStatus);
		if (ma.mApp.loginStatus == 1 || ma.mApp.autoLoginStatus == 1 || ma.mApp.autoLoginStatus == 2) {// 登录成功、自动登录中、自动登录成功
			mLayoutHasInfo.setVisibility(View.VISIBLE);
			mLayoutNoInfo.setVisibility(View.GONE);
			mImageHead.setImageResource(R.drawable.individual_center_head_moren);
			initData();
			isHasInfo = true;
		} else {// 没有用户信息
			mLayoutHasInfo.setVisibility(View.GONE);
			mLayoutNoInfo.setVisibility(View.VISIBLE);
			mImageHead.setImageResource(R.drawable.more_head_no_log_in);
			isHasInfo = false;
		}
	}

}
