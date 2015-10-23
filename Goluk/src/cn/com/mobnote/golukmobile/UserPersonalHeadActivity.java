package cn.com.mobnote.golukmobile;

import java.io.File;

import org.json.JSONException;
import org.json.JSONObject;

import cn.com.mobnote.golukmobile.carrecorder.view.CustomLoadingDialog;
import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.mobnote.util.GolukUtils;
import cn.com.tiros.api.FileUtils;
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

public class UserPersonalHeadActivity extends BaseActivity implements OnClickListener,OnTouchListener,IPageNotifyFn{

	//title
	private ImageButton btnBack;
	private Button btnRight;
	private TextView mTextTitle;
	//body
	private ImageView mImageBoyOne, mImageBoyTwo, mImageBoyThree;
	private ImageView mImageGirlOne, mImageGirlTwo, mImageGirlThree;
	private ImageView mImageDefault;
	
	private CustomLoadingDialog mCustomProgressDialog = null;
	//判断点击的是哪个头像
	private String imageIndex = "";
	//头像的提示
	private ImageView mImageHint1,mImageHint2,mImageHint3,mImageHint4,mImageHint5,mImageHint6,mImageHint7;
	
		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			setContentView(R.layout.user_personal_edit_head);
			
			initView();
			mTextTitle.setText("选择头像");
		}
		
		public void initView(){
			//title
			btnBack = (ImageButton) findViewById(R.id.back_btn);
			mTextTitle = (TextView) findViewById(R.id.user_title_text);
			btnRight = (Button) findViewById(R.id.user_title_right);
			//body
			mImageBoyOne = (ImageView) findViewById(R.id.user_personal_boy_one);
			mImageBoyTwo = (ImageView) findViewById(R.id.user_personal_boy_two);
			mImageBoyThree = (ImageView) findViewById(R.id.user_personal_boy_three);
			mImageGirlOne = (ImageView) findViewById(R.id.user_personal_girl_one);
			mImageGirlTwo = (ImageView) findViewById(R.id.user_personal_girl_two);
			mImageGirlThree = (ImageView) findViewById(R.id.user_personal_girl_three);
			mImageDefault = (ImageView) findViewById(R.id.user_personal_default);
			//头像的提示信息
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
			if(null!= it.getStringExtra("intentHeadText")){
				String headText = it.getStringExtra("intentHeadText");
				String customavatar = it.getStringExtra("customavatar");
				if(customavatar == null || "".equals(customavatar)){
					if(headText.equals("1")){
						mImageHint1.setVisibility(View.VISIBLE);
						imageIndex = "1";
					}else if(headText.equals("2")){
						mImageHint2.setVisibility(View.VISIBLE);
						imageIndex = "2";
					}else if(headText.equals("3")){
						mImageHint3.setVisibility(View.VISIBLE);
						imageIndex = "3";
					}else if(headText.equals("4")){
						mImageHint4.setVisibility(View.VISIBLE);
						imageIndex = "4";
					}else if(headText.equals("5")){
						mImageHint5.setVisibility(View.VISIBLE);
						imageIndex = "5";
					}else if(headText.equals("6")){
						mImageHint6.setVisibility(View.VISIBLE);
						imageIndex = "6";
					}else if(headText.equals("7")){
						mImageHint7.setVisibility(View.VISIBLE);
						imageIndex = "7";
					}
				}
				
			}
			/**
			 * 监听
			 */
			//title
			btnBack.setOnClickListener(this);
			btnRight.setOnClickListener(this);
			btnRight.setOnTouchListener(this);
			//body
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
			switch (arg0.getId()) {
			//返回
			case R.id.back_btn:
				finish();
				break;
			//男
			case R.id.user_personal_boy_one:
				imageIndex = "1";
				mImageHint1.setVisibility(View.VISIBLE);
				mImageHint2.setVisibility(View.GONE);
				mImageHint3.setVisibility(View.GONE);
				mImageHint4.setVisibility(View.GONE);
				mImageHint5.setVisibility(View.GONE);
				mImageHint6.setVisibility(View.GONE);
				mImageHint7.setVisibility(View.GONE);
				break;
			case R.id.user_personal_boy_two:
				imageIndex = "2";
				mImageHint1.setVisibility(View.GONE);
				mImageHint2.setVisibility(View.VISIBLE);
				mImageHint3.setVisibility(View.GONE);
				mImageHint4.setVisibility(View.GONE);
				mImageHint5.setVisibility(View.GONE);
				mImageHint6.setVisibility(View.GONE);
				mImageHint7.setVisibility(View.GONE);
				break;
			case R.id.user_personal_boy_three:
				imageIndex = "3";
				mImageHint1.setVisibility(View.GONE);
				mImageHint2.setVisibility(View.GONE);
				mImageHint3.setVisibility(View.VISIBLE);
				mImageHint4.setVisibility(View.GONE);
				mImageHint5.setVisibility(View.GONE);
				mImageHint6.setVisibility(View.GONE);
				mImageHint7.setVisibility(View.GONE);
				break;
			//女
			case R.id.user_personal_girl_one:
				imageIndex = "4";
				mImageHint1.setVisibility(View.GONE);
				mImageHint2.setVisibility(View.GONE);
				mImageHint3.setVisibility(View.GONE);
				mImageHint4.setVisibility(View.VISIBLE);
				mImageHint5.setVisibility(View.GONE);
				mImageHint6.setVisibility(View.GONE);
				mImageHint7.setVisibility(View.GONE);
				break;
			case R.id.user_personal_girl_two:
				imageIndex = "5";
				mImageHint1.setVisibility(View.GONE);
				mImageHint2.setVisibility(View.GONE);
				mImageHint3.setVisibility(View.GONE);
				mImageHint4.setVisibility(View.GONE);
				mImageHint5.setVisibility(View.VISIBLE);
				mImageHint6.setVisibility(View.GONE);
				mImageHint7.setVisibility(View.GONE);
				break;
			case R.id.user_personal_girl_three:
				imageIndex = "6";
				mImageHint1.setVisibility(View.GONE);
				mImageHint2.setVisibility(View.GONE);
				mImageHint3.setVisibility(View.GONE);
				mImageHint4.setVisibility(View.GONE);
				mImageHint5.setVisibility(View.GONE);
				mImageHint6.setVisibility(View.VISIBLE);
				mImageHint7.setVisibility(View.GONE);
				break;
			//默认
			case R.id.user_personal_default:
				imageIndex = "7";
				mImageHint1.setVisibility(View.GONE);
				mImageHint2.setVisibility(View.GONE);
				mImageHint3.setVisibility(View.GONE);
				mImageHint4.setVisibility(View.GONE);
				mImageHint5.setVisibility(View.GONE);
				mImageHint6.setVisibility(View.GONE);
				mImageHint7.setVisibility(View.VISIBLE);
				break;
		// 右边保存
		case R.id.user_title_right:
			if (mCustomProgressDialog == null) {
				mCustomProgressDialog = new CustomLoadingDialog(this, "正在保存头像,请稍候!");
				mCustomProgressDialog.show();
			} else {
				mCustomProgressDialog.show();
			}
			
			JSONObject requestStr = new JSONObject();
				try {
					requestStr.put("PicPath","");
					requestStr.put("channel", "1");
					requestStr.put("head",imageIndex);
					requestStr.put("PicMD5", "");
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
			
			boolean flog = mBaseApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage, PageType_ModifyHeadPic,
						requestStr.toString());
			System.out.println("flog =" + flog);
			
			break;
			default:
				break;
			}
		}
		@SuppressLint("ClickableViewAccessibility")
		@Override
		public boolean onTouch(View view, MotionEvent event) {
			int action = event.getAction();
			switch (view.getId()) {
			case R.id.user_title_right:
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
				break;
			default:
				break;
			}
			return false;
		}
		
		@Override
		protected void onResume() {
			// TODO Auto-generated method stub
			mBaseApp.setContext(this, "UserPersonalHeadActivity");
			super.onResume();
		}

		@Override
		public void pageNotifyCallBack(int type, int success, Object param1, Object param2) {
			// TODO Auto-generated method stub
			if (type == PageType_ModifyHeadPic) {
				if (mCustomProgressDialog.isShowing()) {
					mCustomProgressDialog.close();
				}
				if (success == 1) {
					try {
						JSONObject result = new JSONObject(param2.toString());
						Boolean suc = result.getBoolean("success");

						if (suc) {
							JSONObject data = result.getJSONObject("data");
							String rst = data.getString("result");
							// 图片上传成功
							if ("0".equals(rst)) {
								

								String head = data.getString("head");
								GolukUtils.showToast(UserPersonalHeadActivity.this, "保存成功");

								
								Intent itHead = new Intent(UserPersonalHeadActivity.this,UserPersonalInfoActivity.class);
								Bundle bundle = new Bundle();
								bundle.putString("intentSevenHead", head);
								itHead.putExtras(bundle);
								this.setResult(RESULT_OK, itHead);
								this.finish();
							}else{
								GolukUtils.showToast(UserPersonalHeadActivity.this, "网络异常,保存失败");
							}

						}else{
							GolukUtils.showToast(UserPersonalHeadActivity.this, "网络异常,保存失败");
						}
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						GolukUtils.showToast(UserPersonalHeadActivity.this, "网络异常,保存失败");
						e.printStackTrace();
					}
				}else{
					GolukUtils.showToast(UserPersonalHeadActivity.this, "网络异常,保存失败");
				}
			}
		}
}
