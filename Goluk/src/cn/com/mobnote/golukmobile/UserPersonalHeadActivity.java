package cn.com.mobnote.golukmobile;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class UserPersonalHeadActivity extends Activity implements OnClickListener,OnTouchListener{

	//title
	private Button btnBack,btnRight;
	private TextView mTextTitle;
	//body
	private ImageView mImageBoyOne, mImageBoyTwo, mImageBoyThree;
	private ImageView mImageGirlOne, mImageGirlTwo, mImageGirlThree;
	private ImageView mImageDefault;
	//判断点击的是哪个头像
	private String imageIndex = "";
	//头像的提示
	private ImageView mImageHint1,mImageHint2,mImageHint3,mImageHint4,mImageHint5,mImageHint6,mImageHint7;
	
		@Override
		protected void onCreate(Bundle savedInstanceState) {
			// TODO Auto-generated method stub
			super.onCreate(savedInstanceState);
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			setContentView(R.layout.user_personal_edit_head);
			
			initView();
			mTextTitle.setText("编辑头像");
		}
		public void initView(){
			//title
			btnBack = (Button) findViewById(R.id.back_btn);
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
			 * 从编辑界面传来的head编号
			 */
			Intent it = getIntent();
			if(null!= it.getStringExtra("intentHeadText")){
				String headText = it.getStringExtra("intentHeadText");
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
			// TODO Auto-generated method stub
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
			Intent itHead = new Intent(UserPersonalHeadActivity.this,UserPersonalEditActivity.class);
			Bundle bundle = new Bundle();
			Log.i("head", "====imageIndex===="+imageIndex+"+++======");
			bundle.putString("intentSevenHead", imageIndex);
			itHead.putExtras(bundle);
			this.setResult(3, itHead);
			this.finish();
			break;

			default:
				break;
			}
		}
		@SuppressLint("ClickableViewAccessibility")
		@Override
		public boolean onTouch(View view, MotionEvent event) {
			// TODO Auto-generated method stub
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
}
