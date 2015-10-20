package cn.com.mobnote.golukmobile;

import java.net.URLEncoder;

import org.json.JSONObject;

import com.facebook.drawee.drawable.ScalingUtils.ScaleType;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.view.SimpleDraweeView;

import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomLoadingDialog;
import cn.com.mobnote.golukmobile.usercenter.UserCenterActivity;
import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.mobnote.user.UserUtils;
import cn.com.mobnote.util.GolukUtils;
import cn.com.mobnote.util.SettingImageView;
import cn.com.tiros.debug.GolukDebugUtils;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * 个人资料
 * 
 * @author mobnote
 * 
 */
public class UserPersonalInfoActivity extends BaseActivity implements
		OnClickListener {

	/** application **/
	private GolukApplication mApplication = null;
	/** context **/
	private Context mContext = null;
	/** title **/
	private ImageButton backBtn = null;

	private Button rightBtn = null;
	private TextView mTextCenter = null;
	/** 头像 **/
	private RelativeLayout mImageHead = null;
	private TextView mTextName = null;
	/** 个性签名 **/
	private TextView mTextSign = null;
	/** xinxi **/
	private String head = null;
	private String name = null;
	private String sign = null;
	private String sex = null;

	/** 头像 **/
	private RelativeLayout mHeadLayout = null;
	/** 昵称 **/
	private RelativeLayout mNameLayout = null;
	/** 个性签名 **/
	private LinearLayout mSignLayout = null;

	/** 请求接口格式化数据 **/
	private String newName = "";
	private String newSign = "";

	public SettingImageView siv = new SettingImageView(UserPersonalInfoActivity.this);

	private static final int REQUEST_CODE_NIKCNAME = 1000;
	private static final int REQUEST_CODE_SIGN = REQUEST_CODE_NIKCNAME + 1;
	private static final int REQUEST_CODE_SYSTEMHEAD = REQUEST_CODE_NIKCNAME + 2;
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
		mTextCenter.setText(R.string.user_personal_edit_info);

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
		mImageHead = (RelativeLayout) findViewById(R.id.user_personal_info_head);
		mTextName = (TextView) findViewById(R.id.user_personal_info_name);
		// 个性签名
		mTextSign = (TextView) findViewById(R.id.user_personal_info_sign);

		mHeadLayout = (RelativeLayout) findViewById(R.id.user_personal_info_head_layout);
		mNameLayout = (RelativeLayout) findViewById(R.id.user_personal_info_name_layout);
		mSignLayout = (LinearLayout) findViewById(R.id.user_personal_info_sign_layout);


		// 监听
		backBtn.setOnClickListener(this);
		rightBtn.setVisibility(View.GONE);
		mHeadLayout.setOnClickListener(this);
		mNameLayout.setOnClickListener(this);
		mSignLayout.setOnClickListener(this);
	}

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.back_btn:
			exit();
			break;

		// 头像
		case R.id.user_personal_info_head_layout:

			settingHeadOptions();
			break;

		// 昵称
		case R.id.user_personal_info_name_layout:
			Intent itName = new Intent(UserPersonalInfoActivity.this, UserPersonalNameActivity.class);
			itName.putExtra("intentNameText", name);
			startActivityForResult(itName, REQUEST_CODE_NIKCNAME);
			break;

		// 个性签名
		case R.id.user_personal_info_sign_layout:
			Intent itSign = new Intent(UserPersonalInfoActivity.this, UserPersonalSignActivity.class);
			itSign.putExtra("intentSignText", sign);
			startActivityForResult(itSign, REQUEST_CODE_SIGN);
			break;
		default:
			break;
		}
	}

	/**
	 * 打开头像设置菜单选择
	 */
	public void settingHeadOptions() {

		final AlertDialog ad = new AlertDialog.Builder(mContext,
				R.style.CustomDialog).create();
		Window window = ad.getWindow();
		window.setGravity(Gravity.BOTTOM);
		ad.show();
		ad.getWindow().setContentView(R.layout.user_center_setting_head);

		ad.getWindow().findViewById(R.id.camera)
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						ad.dismiss();
						siv.getCamera();
					}
				});

		ad.getWindow().findViewById(R.id.photo)
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						ad.dismiss();
						siv.getPhoto();
					}
				});

		ad.getWindow().findViewById(R.id.system)
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						ad.dismiss();
						Intent itHead = new Intent(
								UserPersonalInfoActivity.this,
								UserPersonalHeadActivity.class);
						Bundle bundle = new Bundle();

						bundle.putString("intentHeadText", head);
						itHead.putExtras(bundle);
						startActivityForResult(itHead, REQUEST_CODE_SYSTEMHEAD);
					}
				});

		ad.getWindow().findViewById(R.id.cancel)
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						ad.dismiss();
					}
				});

	}

	/**
	 * 初始化用户信息
	 */
	public void initData() {
		String info = mApplication.mGoluk.GolukLogicCommGet(
				GolukModule.Goluk_Module_HttpPage, 0, "");
		try {
			JSONObject json = new JSONObject(info);

			GolukDebugUtils.i("lily", "====json====" + json);
			head = json.getString("head");
			name = json.getString("nickname");
			sign = json.getString("desc");
			sex = json.getString("sex");

			mTextName.setText(name);
			
			mImageHead.setBackgroundResource(R.drawable.my_head_moren7);
			//UserUtils.focusHead(head, mImageHead);
			mTextSign.setText(sign);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			exit();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * 分别获取从修改昵称、修改个性签名和修改头像页面的数据
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode != RESULT_OK) {
			return;
		}
		switch (requestCode) {
		
		case 5000:
			if (requestCode == siv.CANCELED_CODE) {
				return;
			}
			Uri imageUri = data.getData();
			Intent  intent = new Intent(this,ImageClipActivity.class);
			intent.putExtra("imageuri", imageUri.toString());
			this.startActivityForResult(intent,7000);
			//iv_head.setImageURI(imageUri);
			break;
		case 6000:
			if (requestCode == siv.CANCELED_CODE) {
				siv.deleteUri();
			}
			Intent  it = new Intent(this,ImageClipActivity.class);
			it.putExtra("imageuri", siv.mCameraUri.toString());
			this.startActivityForResult(it,7000);
			//iv_head.setImageURI(mCameraUri);
			break;
		case 7000:
			Bundle b = data.getExtras();
			name = b.getString("imagepath");
			
			int nwidth = (int) (GolukUtils.mDensity * 95);
			loadImage(mImageHead, name, nwidth);
			
			
			System.out.println("xxxxxxxxxxxxxxxxxx");
			break;
		case REQUEST_CODE_NIKCNAME:
			Bundle bundle = data.getExtras();
			name = bundle.getString("itName");
			mTextName.setText(name);
			GolukDebugUtils.i("lily", "--------onActivityResult-------name----" + mTextName.getText().toString());
			break;
		// 修改个性签名
		case REQUEST_CODE_SIGN:
			Bundle bundle2 = data.getExtras();
			sign = bundle2.getString("itSign");
			mTextSign.setText(sign);
			break;
		// 修改头像
		case REQUEST_CODE_SYSTEMHEAD:
			Bundle bundle3 = data.getExtras();
			head = bundle3.getString("intentSevenHead");
			
			
			if(head.equals("1")){
				mImageHead.setBackgroundResource(R.drawable.my_head_boy1);
			}else if(head.equals("2")){
				mImageHead.setBackgroundResource(R.drawable.my_head_boy2);
			}else if(head.equals("3")){
				mImageHead.setBackgroundResource(R.drawable.my_head_boy3);
			}else if(head.equals("4")){
				mImageHead.setBackgroundResource(R.drawable.my_head_girl4);
			}else if(head.equals("5")){
				mImageHead.setBackgroundResource(R.drawable.my_head_girl5);
			}else if(head.equals("6")){
				mImageHead.setBackgroundResource(R.drawable.my_head_girl6);
			}else{
				mImageHead.setBackgroundResource(R.drawable.my_head_moren7);
			}
			
			//UserUtils.focusHead(head, mImageHead);
			if (head.equals("1") || head.equals("2") || head.equals("3")) {
				sex = "1";
			} else if (head.equals("4") || head.equals("5") || head.equals("6")) {
				sex = "2";
			} else if (head.equals("7")) {
				sex = "0";
			}
			break;
		default:
			break;
		}

	}
	
	
	private void loadImage(RelativeLayout layout, String url, int nWidth) {
		layout.removeAllViews();
		SimpleDraweeView view = new SimpleDraweeView(mContext);
		GenericDraweeHierarchyBuilder builder = new GenericDraweeHierarchyBuilder(
				mContext.getResources());
		GenericDraweeHierarchy hierarchy = builder
				.setFadeDuration(300)
				.setPlaceholderImage(
						mContext.getResources().getDrawable(
								R.drawable.my_head_moren7), ScaleType.FIT_XY)
				.setFailureImage(
						mContext.getResources().getDrawable(
								R.drawable.my_head_moren7), ScaleType.FIT_XY)
				.setActualImageScaleType(ScaleType.FIT_XY).build();
		view.setHierarchy(hierarchy);

		view.setImageURI(Uri.parse(url));

		int height = (int) ((float) nWidth / 1.77f);
		RelativeLayout.LayoutParams mPreLoadingParams = new RelativeLayout.LayoutParams(
				nWidth, height);
		mPreLoadingParams.addRule(RelativeLayout.CENTER_VERTICAL,
				RelativeLayout.TRUE);
		layout.addView(view, mPreLoadingParams);
	}


	private void exit() {
		if(null != UserCenterActivity.handler){
			UserCenterActivity.handler.sendEmptyMessage(UserCenterActivity.refristUserInfo);
		}
		this.finish();
	}
	
}
