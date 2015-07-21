package cn.com.mobnote.golukmobile;

import java.net.URLEncoder;

import org.json.JSONObject;

import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomLoadingDialog;
import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.mobnote.user.UserUtils;
import cn.com.mobnote.util.GolukUtils;
import cn.com.tiros.debug.GolukDebugUtils;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * 个人资料
 * 
 * @author mobnote
 *
 */
public class UserPersonalInfoActivity extends BaseActivity implements OnClickListener, OnTouchListener {

	/** application **/
	private GolukApplication mApplication = null;
	/** context **/
	private Context mContext = null;
	/** title **/
	private ImageButton backBtn = null;

	private Button rightBtn = null;
	private TextView mTextCenter = null;
	/** 头像 **/
	private ImageView mImageHead = null;
	private TextView mTextName = null;
	/** 个性签名 **/
	private TextView mTextSign = null;
	/** xinxi **/
	private String head = null;
	private String name = null;
	private String sign = null;
	private String sex = null;
	/****/
	private View mViewSolid1, mViewSolid2;
	private ImageView mImageArrow1, mImageArrow2, mImageArrow3;
	/** 右 **/
	public static boolean clickBtn = false;
	/** 头像 **/
	private RelativeLayout mHeadLayout = null;
	/** 昵称 **/
	private RelativeLayout mNameLayout = null;
	/** 个性签名 **/
	private RelativeLayout mSignLayout = null;

	// 保存数据的loading
	private CustomLoadingDialog mCustomProgressDialog = null;
	/** 从下级页面传来的修改后的用户信息 **/
	private String head2 = null;
	private String name2 = null;
	private String sign2 = null;
	private String sex2 = null;
	/** 请求接口格式化数据 **/
	private String newName = "";
	private String newSign = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.user_personal_info);

		mContext = this;
		// 获得GolukApplication对象
		mApplication = (GolukApplication) getApplication();
		intiView();
		initData();
		// 设置title
		mTextCenter.setText("个人资料");

	}

	@Override
	protected void onResume() {
		super.onResume();
		mApplication.setContext(mContext, "UserPersonalInfo");

	}

	public void intiView() {
		// title
		backBtn = (ImageButton) findViewById(R.id.back_btn);
		rightBtn = (Button) findViewById(R.id.user_title_right);
		mTextCenter = (TextView) findViewById(R.id.user_title_text);
		// 头像
		mImageHead = (ImageView) findViewById(R.id.user_personal_info_head);
		mTextName = (TextView) findViewById(R.id.user_personal_info_name);
		// 个性签名
		mTextSign = (TextView) findViewById(R.id.user_personal_info_sign);
		//
		mViewSolid1 = findViewById(R.id.user_personal_solid_1);
		mViewSolid2 = findViewById(R.id.user_personal_solid_2);
		mImageArrow1 = (ImageView) findViewById(R.id.user_personal_arrow1);
		mImageArrow2 = (ImageView) findViewById(R.id.user_personal_arrow2);
		mImageArrow3 = (ImageView) findViewById(R.id.user_personal_arrow3);

		mHeadLayout = (RelativeLayout) findViewById(R.id.user_personal_info_head_layout);
		mNameLayout = (RelativeLayout) findViewById(R.id.user_personal_info_name_layout);
		mSignLayout = (RelativeLayout) findViewById(R.id.user_personal_info_sign_layout);

		if (null == mCustomProgressDialog) {
			mCustomProgressDialog = new CustomLoadingDialog(mContext, "保存中，请稍候……");
		}

		// 监听
		backBtn.setOnClickListener(this);
		rightBtn.setOnClickListener(this);
		rightBtn.setOnTouchListener(this);
		mHeadLayout.setOnClickListener(this);
		mNameLayout.setOnClickListener(this);
		mSignLayout.setOnClickListener(this);
	}

	// 点击效果
	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouch(View view, MotionEvent event) {
		int action = event.getAction();
		switch (view.getId()) {
		case R.id.user_title_right:
			switch (action) {
			case MotionEvent.ACTION_DOWN:
				rightBtn.setTextColor(Color.rgb(0, 197, 176));
				break;
			case MotionEvent.ACTION_UP:
				rightBtn.setTextColor(Color.WHITE);
				break;
			default:
				break;
			}
			break;
		default:
			break;
		}
		return false;
	}

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.back_btn:
			finish();
			break;
		// 编辑
		case R.id.user_title_right:
			if (!clickBtn) {
				mViewSolid1.setVisibility(View.VISIBLE);
				mViewSolid2.setVisibility(View.VISIBLE);
				mImageArrow1.setVisibility(View.VISIBLE);
				mImageArrow2.setVisibility(View.VISIBLE);
				mImageArrow3.setVisibility(View.VISIBLE);
				translateAnim(mImageHead);
				rightBtn.setText("完成");
			} else {
				saveInfo();
			}
			break;
		// 头像
		case R.id.user_personal_info_head_layout:
			if (clickBtn) {
				Intent itHead = new Intent(UserPersonalInfoActivity.this, UserPersonalHeadActivity.class);
				Bundle bundle = new Bundle();
				if(head2 == null)
					head2 = head;
				bundle.putString("intentHeadText", head2);
				itHead.putExtras(bundle);
				startActivityForResult(itHead, 3);
			}
			break;
		// 昵称
		case R.id.user_personal_info_name_layout:
			if (clickBtn) {
				Intent itName = new Intent(UserPersonalInfoActivity.this, UserPersonalNameActivity.class);
				Bundle bundle = new Bundle();
				bundle.putString("intentNameText", mTextName.getText().toString());
				itName.putExtras(bundle);
				startActivityForResult(itName, 1);
			}
			break;
		// 个性签名
		case R.id.user_personal_info_sign_layout:
			if (clickBtn) {
				Intent itSign = new Intent(UserPersonalInfoActivity.this, UserPersonalSignActivity.class);
				Bundle bundle = new Bundle();
				bundle.putString("intentSignText", mTextSign.getText().toString());
				itSign.putExtras(bundle);
				startActivityForResult(itSign, 2);
			}
			break;
		default:
			break;
		}
	}

	/**
	 * 初始化用户信息
	 */
	public void initData() {
		String info = mApplication.mGoluk.GolukLogicCommGet(GolukModule.Goluk_Module_HttpPage, 0, "");
		try {
			JSONObject json = new JSONObject(info);

			GolukDebugUtils.i("lily", "====json====" + json);
			head = json.getString("head");
			name = json.getString("nickname");
			sign = json.getString("desc");
			sex = json.getString("sex");

			mTextName.setText(name);
			UserUtils.focusHead(head, mImageHead);
			mTextSign.setText(sign);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 分别获取从修改昵称、修改个性签名和修改头像页面的数据
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (resultCode) {
		// 修改昵称
		case 1:
			Bundle bundle = data.getExtras();
			name2 = bundle.getString("itName");
			mTextName.setText(name2);
			GolukDebugUtils.i("lily", "--------onActivityResult-------name----" + mTextName.getText().toString());
			break;
		// 修改个性签名
		case 2:
			Bundle bundle2 = data.getExtras();
			sign2 = bundle2.getString("itSign");
			mTextSign.setText(sign2);
			break;
		// 修改头像
		case 3:
			Bundle bundle3 = data.getExtras();
			head2 = bundle3.getString("intentSevenHead");
			UserUtils.focusHead(head2, mImageHead);
			if (head2.equals("1") || head2.equals("2") || head2.equals("3")) {
				sex2 = "1";
			} else if (head2.equals("4") || head2.equals("5") || head2.equals("6")) {
				sex2 = "2";
			} else if (head2.equals("7")) {
				sex2 = "0";
			}
			break;
		default:
			break;
		}
	}

	/**
	 * 修改用户信息
	 */
	public void saveInfo() {
		if (head2 == null) {
			head2 = head;
		}
		if (null == name2) {
			name2 = name;
		}
		if (null == sign2) {
			sign2 = sign;
		}
		if (null == sex2) {
			sex2 = sex;
		}
		try {
			newName = URLEncoder.encode(name2, "utf-8");
			newSign = URLEncoder.encode(sign2, "utf-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (name2.equals(name) && head2.equals(head) && sign2.equals(sign)) {
			// this.finish();
			GolukDebugUtils.i("lily", "数据没有修改");
			mViewSolid1.setVisibility(View.GONE);
			mViewSolid2.setVisibility(View.GONE);
			mImageArrow1.setVisibility(View.GONE);
			mImageArrow2.setVisibility(View.GONE);
			mImageArrow3.setVisibility(View.GONE);
			translateAnim(mImageHead);
			rightBtn.setText("编辑");
		} else {
			// {NickName：“昵称”，UserHead:”1”，UserSex:”1”,Desc:""}
			String isSave = "{\"NickName\":\"" + newName + "\",\"UserHead\":\"" + head2 + "\",\"UserSex\":\"" + sex2
					+ "\",\"Desc\":\"" + newSign + "\"}";
			GolukDebugUtils.i("lily", "-----name-----" + newName + "-----head2----" + head2 + "-----sex2-----" + sex2
					+ "-----newsign---" + newSign);
			boolean b = mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage,
					IPageNotifyFn.PageType_ModifyUserInfo, isSave);
			if (b) {
				// 保存中
				mCustomProgressDialog.show();
				backBtn.setEnabled(false);
				rightBtn.setEnabled(false);
				mHeadLayout.setEnabled(false);
				mNameLayout.setEnabled(false);
				mSignLayout.setEnabled(false);
			}
		}
	}

	/**
	 * 修改用户信息回调
	 */

	public void saveInfoCallBack(int success, Object obj) {
		GolukDebugUtils.e("", "修改用户信息回调---saveInfoCallBack---" + success + "---" + obj);
		if (1 == success) {
			try {
				String data = (String) obj;
				JSONObject json = new JSONObject(data);
				int code = Integer.valueOf(json.getString("code"));

				closeProgressDialog();
				switch (code) {
				case 200:
					GolukUtils.showToast(mContext, "数据修改成功");
					mViewSolid1.setVisibility(View.GONE);
					mViewSolid2.setVisibility(View.GONE);
					mImageArrow1.setVisibility(View.GONE);
					mImageArrow2.setVisibility(View.GONE);
					mImageArrow3.setVisibility(View.GONE);
					translateAnim(mImageHead);
					rightBtn.setText("编辑");
					// this.finish();
					break;
				case 405:
					GolukUtils.showToast(mContext, "该用户未注册");
					closeProgressDialog();
					break;

				case 500:
					GolukUtils.showToast(mContext, "服务器异常");
					closeProgressDialog();
					break;

				default:
					break;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			// success不等于1
			GolukUtils.showToast(mContext, "数据修改失败,请重试");
			closeProgressDialog();
		}
	}

	/**
	 * 头像动画
	 * 
	 * @param mImageView
	 */
	public void translateAnim(ImageView mImageView) {
		WindowManager wm = this.getWindowManager();
		int width = wm.getDefaultDisplay().getWidth() / 3;
		TranslateAnimation animation = null;
		if (clickBtn) {
			animation = new TranslateAnimation(-width, 0, 0, 0);
			animation.setDuration(500);
			clickBtn = false;
		}else{
			animation = new TranslateAnimation(0, -width, 0, 0);
			animation.setDuration(500);
			clickBtn = true;
		}
		animation.setFillAfter(true);
		mImageView.startAnimation(animation);
	}

	/**
	 * 关闭加载中对话框
	 * 
	 * @date 2015年4月15日
	 */
	private void closeProgressDialog() {
		if (null != mCustomProgressDialog) {
			mCustomProgressDialog.close();
			backBtn.setEnabled(true);
			rightBtn.setEnabled(true);
			mHeadLayout.setEnabled(true);
			mNameLayout.setEnabled(true);
			mSignLayout.setEnabled(true);
		}
	}
}
