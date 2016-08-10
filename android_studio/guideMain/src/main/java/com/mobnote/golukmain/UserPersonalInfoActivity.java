package com.mobnote.golukmain;

import org.json.JSONObject;

import com.mobnote.application.GolukApplication;
import com.mobnote.eventbus.EventConfig;
import com.mobnote.eventbus.EventRefreshUserInfo;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.live.ILive;
import com.mobnote.golukmain.live.UserInfo;
import com.mobnote.util.GlideUtils;
import com.mobnote.util.GolukUtils;
import com.mobnote.util.SettingImageView;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.mobnote.logic.GolukModule;
import cn.com.tiros.debug.GolukDebugUtils;
import de.greenrobot.event.EventBus;

/**
 * 个人资料
 * 
 * @author mobnote
 * 
 */
public class UserPersonalInfoActivity extends BaseActivity implements OnClickListener {

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
	private String customavatar = null;

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
	private static final int REQUEST_CODE_PHOTO = 5000;
	private static final int REQUEST_CODE_CAMERA = 6000;
	private static final int REQUEST_CODE_CLIP = 7000;

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
		mImageHead = (ImageView) findViewById(R.id.user_personal_info_head);
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
		int id = arg0.getId();
		if (id == R.id.back_btn) {
			exit();
		} else if (id == R.id.user_personal_info_head_layout) {
			settingHeadOptions();
		} else if (id == R.id.user_personal_info_name_layout) {
			Intent itName = new Intent(UserPersonalInfoActivity.this, UserPersonalNameActivity.class);
			itName.putExtra("intentNameText", name);
			startActivityForResult(itName, REQUEST_CODE_NIKCNAME);
		} else if (id == R.id.user_personal_info_sign_layout) {
			Intent itSign = new Intent(UserPersonalInfoActivity.this, UserPersonalSignActivity.class);
			itSign.putExtra("intentSignText", sign);
			startActivityForResult(itSign, REQUEST_CODE_SIGN);
		} else {
		}
	}

	/**
	 * 打开头像设置菜单选择
	 */
	public void settingHeadOptions() {

		final AlertDialog ad = new AlertDialog.Builder(mContext, R.style.CustomDialog).create();
		Window window = ad.getWindow();
		window.setGravity(Gravity.BOTTOM);
		ad.show();
		ad.getWindow().setContentView(R.layout.user_center_setting_head);

		ad.getWindow().findViewById(R.id.camera).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ad.dismiss();
				boolean isSucess = siv.getCamera();
				if (!isSucess) {
					GolukUtils.showToast(UserPersonalInfoActivity.this,
							mContext.getResources().getString(R.string.str_start_camera_fail));
				}
			}
		});

		ad.getWindow().findViewById(R.id.photo).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ad.dismiss();
				boolean isS = siv.getPhoto();
				if (!isS) {
					GolukUtils.showToast(UserPersonalInfoActivity.this,
							mContext.getResources().getString(R.string.str_start_album_fail));
				}
			}
		});

		ad.getWindow().findViewById(R.id.system).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ad.dismiss();
				Intent itHead = new Intent(UserPersonalInfoActivity.this, UserPersonalHeadActivity.class);
				Bundle bundle = new Bundle();

				bundle.putString("intentHeadText", head);
				bundle.putString("customavatar", customavatar);
				itHead.putExtras(bundle);
				startActivityForResult(itHead, REQUEST_CODE_SYSTEMHEAD);
			}
		});

		ad.getWindow().findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
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
		UserInfo info = mApplication.getMyInfo();
		try {
			if(info != null){
				head = info.head;
				name = info.nickname;
				sign = info.desc;
				sex = info.sex;
				customavatar = info.customavatar;
				if (customavatar != null && !"".equals(customavatar)) {
					GlideUtils.loadNetHead(this, mImageHead, customavatar, R.drawable.editor_head_feault7);
				} else {
					showHead(mImageHead, head);
				}

				mTextName.setText(name);

				mTextSign.setText(sign);
			}
			

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
			Intent intent = new Intent(this, ImageClipActivity.class);
			intent.putExtra("imageuri", imageUri.toString());
			this.startActivityForResult(intent, 7000);
			// iv_head.setImageURI(imageUri);
			break;
		case 6000:
			if (requestCode == siv.CANCELED_CODE) {
				siv.deleteUri();
			}
			Intent it = new Intent(this, ImageClipActivity.class);
			if(siv.mCameraUri == null){
				if (data != null)
				{
					Bundle bundle = data.getExtras();
					if (bundle != null) {
						Bitmap photo = (Bitmap) bundle.get("data"); // get bitmap
						if (photo == null){
						}
						it.putExtra("imagebitmap",photo);
					}
				}
			} else {
				it.putExtra("imageuri", siv.mCameraUri.toString());
			}

			this.startActivityForResult(it, 7000);
			// iv_head.setImageURI(mCameraUri);
			break;
		case 7000:
			Bundle b = data.getExtras();
			String imagepach = b.getString("imagepath");
			customavatar = imagepach;
			GlideUtils.loadNetHead(this, mImageHead, customavatar, R.drawable.editor_head_feault7);
			siv.deleteUri();
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
			customavatar = "";
			showHead(mImageHead, head);

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

	private void showHead(ImageView view, String headportrait) {
		try {
			GlideUtils.loadLocalHead(this, view, ILive.mBigHeadImg[Integer.parseInt(headportrait)]);
		} catch (Exception e) {
			GlideUtils.loadLocalHead(this, view, R.drawable.editor_head_feault7);
		}
	}

	private void exit() {
//		if (null != UserCenterActivity.handler) {
//			UserCenterActivity.handler.sendEmptyMessage(UserCenterActivity.refristUserInfo);
//		}
		this.finish();
	}

}
