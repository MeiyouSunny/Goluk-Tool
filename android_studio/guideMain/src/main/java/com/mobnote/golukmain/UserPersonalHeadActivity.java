package com.mobnote.golukmain;

import org.json.JSONException;
import org.json.JSONObject;

import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.carrecorder.view.CustomLoadingDialog;
import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.userlogin.UpHeadData;
import com.mobnote.golukmain.userlogin.UpHeadResult;
import com.mobnote.golukmain.userlogin.UpdUserDescBeanRequest;
import com.mobnote.golukmain.userlogin.UpdUserHeadBeanRequest;
import com.mobnote.user.UserUtils;
import com.mobnote.util.GolukUtils;

import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.page.IPageNotifyFn;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class UserPersonalHeadActivity extends BaseActivity implements OnClickListener, OnTouchListener,IRequestResultListener {

	// title
	private ImageButton btnBack;
	private Button btnRight;
	private TextView mTextTitle;
	// body
	private ImageView mImageBoyOne, mImageBoyTwo, mImageBoyThree;
	private ImageView mImageGirlOne, mImageGirlTwo, mImageGirlThree;
	private ImageView mImageDefault;

	private CustomLoadingDialog mCustomProgressDialog = null;
	// 判断点击的是哪个头像
	private String imageIndex = "";
	// 头像的提示
	private ImageView mImageHint1, mImageHint2, mImageHint3, mImageHint4, mImageHint5, mImageHint6, mImageHint7;
	
	private UpdUserHeadBeanRequest updUserHeadBeanRequest = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.user_personal_edit_head);
		
		initView();
		mTextTitle.setText(this.getResources().getString(R.string.str_choose_head));
	}

	public void initView() {
		// title
		btnBack = (ImageButton) findViewById(R.id.back_btn);
		mTextTitle = (TextView) findViewById(R.id.user_title_text);
		btnRight = (Button) findViewById(R.id.user_title_right);
		// body
		mImageBoyOne = (ImageView) findViewById(R.id.user_personal_boy_one);
		mImageBoyTwo = (ImageView) findViewById(R.id.user_personal_boy_two);
		mImageBoyThree = (ImageView) findViewById(R.id.user_personal_boy_three);
		mImageGirlOne = (ImageView) findViewById(R.id.user_personal_girl_one);
		mImageGirlTwo = (ImageView) findViewById(R.id.user_personal_girl_two);
		mImageGirlThree = (ImageView) findViewById(R.id.user_personal_girl_three);
		mImageDefault = (ImageView) findViewById(R.id.user_personal_default);
		// 头像的提示信息
		mImageHint1 = (ImageView) findViewById(R.id.user_personal_hint_image1);
		mImageHint2 = (ImageView) findViewById(R.id.user_personal_hint_image2);
		mImageHint3 = (ImageView) findViewById(R.id.user_personal_hint_image3);
		mImageHint4 = (ImageView) findViewById(R.id.user_personal_hint_image4);
		mImageHint5 = (ImageView) findViewById(R.id.user_personal_hint_image5);
		mImageHint6 = (ImageView) findViewById(R.id.user_personal_hint_image6);
		mImageHint7 = (ImageView) findViewById(R.id.user_personal_hint_image7);

		/**
		 * 从编辑界面(UserPersonalEditActivity)传来的head编号
		 */
		Intent it = getIntent();
		if (null != it.getStringExtra("intentHeadText")) {
			String headText = it.getStringExtra("intentHeadText");
			String customavatar = it.getStringExtra("customavatar");
			if (customavatar == null || "".equals(customavatar)) {
				if (headText.equals("1")) {
					mImageHint1.setVisibility(View.VISIBLE);
					imageIndex = "1";
				} else if (headText.equals("2")) {
					mImageHint2.setVisibility(View.VISIBLE);
					imageIndex = "2";
				} else if (headText.equals("3")) {
					mImageHint3.setVisibility(View.VISIBLE);
					imageIndex = "3";
				} else if (headText.equals("4")) {
					mImageHint4.setVisibility(View.VISIBLE);
					imageIndex = "4";
				} else if (headText.equals("5")) {
					mImageHint5.setVisibility(View.VISIBLE);
					imageIndex = "5";
				} else if (headText.equals("6")) {
					mImageHint6.setVisibility(View.VISIBLE);
					imageIndex = "6";
				} else if (headText.equals("7")) {
					mImageHint7.setVisibility(View.VISIBLE);
					imageIndex = "7";
				}
			}

		}
		/**
		 * 监听
		 */
		// title
		btnBack.setOnClickListener(this);
		btnRight.setOnClickListener(this);
		//btnRight.setOnTouchListener(this);
		// body
		mImageBoyOne.setOnClickListener(this);
		mImageBoyTwo.setOnClickListener(this);
		mImageBoyThree.setOnClickListener(this);
		mImageGirlOne.setOnClickListener(this);
		mImageGirlTwo.setOnClickListener(this);
		mImageGirlThree.setOnClickListener(this);
		mImageDefault.setOnClickListener(this);

	}

	@Override
	public void onClick(View arg0) {
		int id = arg0.getId();
		if (id == R.id.back_btn) {
			finish();
		} else if (id == R.id.user_personal_boy_one) {
			imageIndex = "1";
			mImageHint1.setVisibility(View.VISIBLE);
			mImageHint2.setVisibility(View.GONE);
			mImageHint3.setVisibility(View.GONE);
			mImageHint4.setVisibility(View.GONE);
			mImageHint5.setVisibility(View.GONE);
			mImageHint6.setVisibility(View.GONE);
			mImageHint7.setVisibility(View.GONE);
		} else if (id == R.id.user_personal_boy_two) {
			imageIndex = "2";
			mImageHint1.setVisibility(View.GONE);
			mImageHint2.setVisibility(View.VISIBLE);
			mImageHint3.setVisibility(View.GONE);
			mImageHint4.setVisibility(View.GONE);
			mImageHint5.setVisibility(View.GONE);
			mImageHint6.setVisibility(View.GONE);
			mImageHint7.setVisibility(View.GONE);
		} else if (id == R.id.user_personal_boy_three) {
			imageIndex = "3";
			mImageHint1.setVisibility(View.GONE);
			mImageHint2.setVisibility(View.GONE);
			mImageHint3.setVisibility(View.VISIBLE);
			mImageHint4.setVisibility(View.GONE);
			mImageHint5.setVisibility(View.GONE);
			mImageHint6.setVisibility(View.GONE);
			mImageHint7.setVisibility(View.GONE);
		} else if (id == R.id.user_personal_girl_one) {
			imageIndex = "4";
			mImageHint1.setVisibility(View.GONE);
			mImageHint2.setVisibility(View.GONE);
			mImageHint3.setVisibility(View.GONE);
			mImageHint4.setVisibility(View.VISIBLE);
			mImageHint5.setVisibility(View.GONE);
			mImageHint6.setVisibility(View.GONE);
			mImageHint7.setVisibility(View.GONE);
		} else if (id == R.id.user_personal_girl_two) {
			imageIndex = "5";
			mImageHint1.setVisibility(View.GONE);
			mImageHint2.setVisibility(View.GONE);
			mImageHint3.setVisibility(View.GONE);
			mImageHint4.setVisibility(View.GONE);
			mImageHint5.setVisibility(View.VISIBLE);
			mImageHint6.setVisibility(View.GONE);
			mImageHint7.setVisibility(View.GONE);
		} else if (id == R.id.user_personal_girl_three) {
			imageIndex = "6";
			mImageHint1.setVisibility(View.GONE);
			mImageHint2.setVisibility(View.GONE);
			mImageHint3.setVisibility(View.GONE);
			mImageHint4.setVisibility(View.GONE);
			mImageHint5.setVisibility(View.GONE);
			mImageHint6.setVisibility(View.VISIBLE);
			mImageHint7.setVisibility(View.GONE);
		} else if (id == R.id.user_personal_default) {
			imageIndex = "7";
			mImageHint1.setVisibility(View.GONE);
			mImageHint2.setVisibility(View.GONE);
			mImageHint3.setVisibility(View.GONE);
			mImageHint4.setVisibility(View.GONE);
			mImageHint5.setVisibility(View.GONE);
			mImageHint6.setVisibility(View.GONE);
			mImageHint7.setVisibility(View.VISIBLE);
		} else if (id == R.id.user_title_right) {
			if (!UserUtils.isNetDeviceAvailable(this)) {
				GolukUtils.showToast(this, this.getResources().getString(R.string.user_net_unavailable));
			}else{
				click_save();
			}
		}
	}

	private void click_save() {
		if (null == imageIndex || "".equals(imageIndex)) {
			// 用户没有选择头像
			GolukUtils.showToast(this, this.getResources().getString(R.string.str_choose_one_head));
			return;
		}
		JSONObject requestStr = new JSONObject();
		try {
			requestStr.put("PicPath", "");
			requestStr.put("channel", "1");
			requestStr.put("head", imageIndex);
			requestStr.put("PicMD5", "");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		updUserHeadBeanRequest = new UpdUserHeadBeanRequest(IPageNotifyFn.PageType_ModifyHeadPic, this);
		updUserHeadBeanRequest.get(GolukApplication.getInstance().getMyInfo().uid, GolukApplication.getInstance().getMyInfo().phone, "1", "", imageIndex);
//		boolean flog = mBaseApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage, PageType_ModifyHeadPic,
//				requestStr.toString());
//		if (!flog) {
//			// 失败
//			closeLoadDialog();
//			GolukUtils.showToast(this, this.getResources().getString(R.string.str_save_fail));
//			return;
//		}
		showLoadingDialog();
	}

	private void showLoadingDialog() {
		if (mCustomProgressDialog == null) {
			mCustomProgressDialog = new CustomLoadingDialog(this, this.getResources().getString(
					R.string.str_save_head_ongoing));
			mCustomProgressDialog.show();
		} else {
			mCustomProgressDialog.show();
		}
	}

	private void closeLoadDialog() {
		if (null != mCustomProgressDialog) {
			if (mCustomProgressDialog.isShowing()) {
				mCustomProgressDialog.close();
			}
		}
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouch(View view, MotionEvent event) {
		int action = event.getAction();
		int id = view.getId();
		if (id == R.id.user_title_right) {
			switch (action) {
			case MotionEvent.ACTION_DOWN:
				btnRight.setTextColor(Color.rgb(0, 197, 176));
				break;
			case MotionEvent.ACTION_UP:
				btnRight.setTextColor(Color.WHITE);
				break;
			default:
				break;
			}
		} else {
		}
		return false;
	}

	@Override
	protected void onResume() {
		mBaseApp.setContext(this, "UserPersonalHeadActivity");
		super.onResume();
	}

//	@Override
//	public void pageNotifyCallBack(int type, int success, Object param1, Object param2) {
//		if (type == PageType_ModifyHeadPic) {
//			closeLoadDialog();
//			if (success == 1) {
//				try {
//					JSONObject result = new JSONObject(param2.toString());
//					Boolean suc = result.getBoolean("success");
//
//					if (suc) {
//						JSONObject data = result.getJSONObject("data");
//						String rst = data.getString("result");
//						// 图片上传成功
//						if ("0".equals(rst)) {
//							
//							String head = data.getString("head");
//							GolukApplication.getInstance().setMyinfo("", head, "",null);
//							GolukUtils.showToast(UserPersonalHeadActivity.this, this.getResources().getString(R.string.str_save_success));
//
//							Intent itHead = new Intent(UserPersonalHeadActivity.this, UserPersonalInfoActivity.class);
//							Bundle bundle = new Bundle();
//							bundle.putString("intentSevenHead", head);
//							itHead.putExtras(bundle);
//							this.setResult(RESULT_OK, itHead);
//							this.finish();
//						} else {
//							GolukUtils.showToast(UserPersonalHeadActivity.this,
//									this.getResources().getString(R.string.str_save_network_error));
//						}
//
//					} else {
//						GolukUtils.showToast(UserPersonalHeadActivity.this,
//								this.getResources().getString(R.string.str_save_network_error));
//					}
//				} catch (JSONException e) {
//					GolukUtils.showToast(UserPersonalHeadActivity.this,
//							this.getResources().getString(R.string.str_save_network_error));
//					e.printStackTrace();
//				}
//			} else {
//				GolukUtils.showToast(UserPersonalHeadActivity.this,
//						this.getResources().getString(R.string.str_save_network_error));
//			}
//		}
//	}

	@Override
	public void onLoadComplete(int requestType, Object result) {
		if(IPageNotifyFn.PageType_ModifyHeadPic == requestType){
			closeLoadDialog();
			UpHeadResult headResult = (UpHeadResult) result;

			if (headResult != null && headResult.success) {
				UpHeadData data = headResult.data;
				String rst = data.result;
				// 图片上传成功
				if ("0".equals(rst)) {
					
					GolukApplication.getInstance().setMyinfo("", imageIndex, "","");
					GolukUtils.showToast(UserPersonalHeadActivity.this, this.getResources().getString(R.string.str_save_success));

					Intent itHead = new Intent(UserPersonalHeadActivity.this, UserPersonalInfoActivity.class);
					Bundle bundle = new Bundle();
					bundle.putString("intentSevenHead", imageIndex);
					itHead.putExtras(bundle);
					this.setResult(RESULT_OK, itHead);
					this.finish();
				} else {
					GolukUtils.showToast(UserPersonalHeadActivity.this,
							this.getResources().getString(R.string.str_save_network_error));
				}

			} else {
				GolukUtils.showToast(UserPersonalHeadActivity.this,
						this.getResources().getString(R.string.str_save_network_error));
			}
		}
		
	}
}
