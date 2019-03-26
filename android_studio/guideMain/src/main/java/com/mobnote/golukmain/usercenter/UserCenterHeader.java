package com.mobnote.golukmain.usercenter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.UserPersonalInfoActivity;
import com.mobnote.golukmain.usercenter.bean.HomeData;
import com.mobnote.golukmain.usercenter.bean.HomeUser;
import com.mobnote.user.UserUtils;
import com.mobnote.util.GlideUtils;
import com.mobnote.util.GolukUtils;
import com.mobnote.util.glideblur.RSBlur;

import java.io.File;
import java.util.concurrent.TimeUnit;

import cn.com.tiros.debug.GolukDebugUtils;

public class UserCenterHeader implements OnClickListener {

	private Context mContext;
	private ImageView mImageHeadBg, mImageHead, mLogoImage;
	private TextView mTextName, mTextAttention, mTextFans, mTextContent;
	private LinearLayout mWonderfulLayout, mRecommendLayout, mHeadlinesLayout;
	private TextView mWonderfulText, mRecommednText, mHeadlinesText;
	private Button mAttentionBtn;
	private RelativeLayout mLayoutCloudTip;
	private ImageView mIcCloudClose;
	/** 取消关注 **/
	private static final String TYPE_ATTENTION_CANCLE = "0";
	/** 关注 **/
	private static final String TYPE_ATTENTION = "1";

	private HomeData mData;
	private NewUserCenterActivity mUserCenterActivity = null;

	public UserCenterHeader(Context context) {
		super();
		this.mContext = context;
		mUserCenterActivity = (NewUserCenterActivity) mContext;
	}

	public void setHeaderData(HomeData data) {
		this.mData = data;

	}

	public void saveHeadData(HomeData data){
		if(data!=null && data.user!=null) {
			GolukApplication.getInstance().setMyinfo(data.user.nickname, data.user.avatar, data.user.introduction, data.user.customavatar);
		}
	}

	View view;
	public View createHeader() {
		view = LayoutInflater.from(mContext).inflate(R.layout.activity_usercenter_header, null);
		mImageHeadBg = (ImageView) view.findViewById(R.id.image_user_big);
		mImageHead = (ImageView) view.findViewById(R.id.iv_usercenter_header_head);
		mAttentionBtn = (Button) view.findViewById(R.id.btn_usercenter_header_attention);
		mLogoImage = (ImageView) view.findViewById(R.id.iv_vip_logo);
		mTextName = (TextView) view.findViewById(R.id.tv_usercenter_header_name);
		mTextAttention = (TextView) view.findViewById(R.id.tv_usercenter_header_attention_count);
		mTextFans = (TextView) view.findViewById(R.id.tv_usercenter_header_fans_count);
		mTextContent = (TextView) view.findViewById(R.id.tv_usercenter_header_content);
		mWonderfulLayout = (LinearLayout) view.findViewById(R.id.layout_usercenter_header_category_wonderful);
		mRecommendLayout = (LinearLayout) view.findViewById(R.id.layout_usercenter_header_category_recommend);
		mHeadlinesLayout = (LinearLayout) view.findViewById(R.id.layout_usercenter_header_category_headlines);
		mWonderfulText = (TextView) view.findViewById(R.id.tv_usercenter_header_wonderful_count);
		mRecommednText = (TextView) view.findViewById(R.id.tv_usercenter_header_recommendcount);
		mHeadlinesText = (TextView) view.findViewById(R.id.tv_usercenter_header_headlines_count);
		mLayoutCloudTip = (RelativeLayout) view.findViewById(R.id.layout_cloud_tip);
		mIcCloudClose = (ImageView) view.findViewById(R.id.ic_cloud_close);

		mAttentionBtn.setOnClickListener(this);
		mTextAttention.setOnClickListener(this);
		mTextFans.setOnClickListener(this);
		mWonderfulLayout.setOnClickListener(this);
		mRecommendLayout.setOnClickListener(this);
		mHeadlinesLayout.setOnClickListener(this);
		mImageHead.setOnClickListener(this);
		mIcCloudClose.setOnClickListener(this);

		return view;
	}

	public void getHeaderData() {
		if (null != mData) {
			HomeUser user = mData.user;
			if (null != user && !"".equals(user.customavatar)) {
				// 使用网络地址
				GlideUtils.loadNetHead(mContext, mImageHead, user.customavatar, R.drawable.usercenter_head_default);

				final String headUrl = user.customavatar;
				blurHeadIcon(headUrl);

			} else {
				UserUtils.focusHead(mContext, user.avatar, mImageHead);
			}

			mTextName.setText(user.nickname);
			mTextAttention.setText(mContext.getString(R.string.str_usercenter_header_attention_text) + " "
					+ GolukUtils.getFormatNumber(user.following));
			mTextFans.setText(mContext.getString(R.string.str_usercenter_header_fans_text) + " "
					+ GolukUtils.getFormatNumber(user.fans));
			mWonderfulText.setText(GolukUtils.getFormatNumber(mData.selectcount));
			mRecommednText.setText(GolukUtils.getFormatNumber(mData.recommendcount));
			mHeadlinesText.setText(GolukUtils.getFormatNumber(mData.headlinecount));
			if (0 == mData.selectcount) {
				mWonderfulLayout.setEnabled(false);
			} else {
				mWonderfulLayout.setEnabled(true);
			}
			if (0 == mData.recommendcount) {
				mRecommendLayout.setEnabled(false);
			} else {
				mRecommendLayout.setEnabled(true);
			}
			if (0 == mData.headlinecount) {
				mHeadlinesLayout.setEnabled(false);
			} else {
				mHeadlinesLayout.setEnabled(true);
			}
			// 认证信息
			if (null != user.certification) {
				String isorgcertificated = user.certification.isorgcertificated;// 蓝V标识
				String orgcertification = user.certification.orgcertification;// 蓝V内容
				String isusercertificated = user.certification.isusercertificated;// 黄V标识
				String usercertification = user.certification.usercertification;// 黄V内容
				String isstar = user.certification.isstar;// 达人标识
				mLogoImage.setVisibility(View.VISIBLE);
				if ("1".equals(isorgcertificated)) {
					mLogoImage.setImageResource(R.drawable.authentication_bluev_icon);
					if (null == orgcertification || "".equals(orgcertification)) {
						mTextContent.setText(mContext.getResources().getString(R.string.str_let_sharevideo));
					} else {
						mTextContent.setText(orgcertification);
					}
				} else if ("1".equals(isusercertificated)) {
					mLogoImage.setImageResource(R.drawable.authentication_yellowv_icon);
					if (null == usercertification || "".equals(usercertification)) {
						mTextContent.setText(mContext.getResources().getString(R.string.str_let_sharevideo));
					} else {
						mTextContent.setText(usercertification);
					}
				} else if ("1".equals(isstar)) {
					mLogoImage.setImageResource(R.drawable.authentication_star_icon);
					// TODO 达人接口中没有认证信息
					if (null == user.introduction || "".equals(user.introduction)) {
						mTextContent.setText(mContext.getResources().getString(R.string.str_let_sharevideo));
					} else {
						mTextContent.setText(user.introduction);
					}
				} else {
					mLogoImage.setVisibility(View.GONE);
					if (null == user.introduction || "".equals(user.introduction)) {
						mTextContent.setText(mContext.getResources().getString(R.string.str_let_sharevideo));
					} else {
						mTextContent.setText(user.introduction);
					}
				}
			} else {
				mLogoImage.setVisibility(View.GONE);
				mTextContent.setText(user.introduction);
			}

			if(mUserCenterActivity.testUser()) {
				//显示修改资料按钮
				mAttentionBtn.setBackgroundResource(R.drawable.bg_edit);
				Drawable drawable1= mContext.getResources().getDrawable(R.drawable.usercenter_edit);
				drawable1.setBounds(0, 0, drawable1.getMinimumWidth(), drawable1.getMinimumHeight());
				mAttentionBtn.setCompoundDrawables(drawable1,null,null,null);
				mAttentionBtn.setText(mContext.getString(R.string.str_usercenter_header_edit_text));
				mAttentionBtn.setTextColor(Color.parseColor("#ffffff"));
			} else {
				changeAttentionState(mData.user.link);
			}

		}

	}

	private void blurHeadIcon(final String headUrl) {
		new Thread() {
			@Override
			public void run() {
				try {
					File result = Glide.with(mContext)
							.load(headUrl)
							.downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
							.get(10, TimeUnit.SECONDS);
					Bitmap bitmapHead = BitmapFactory.decodeFile(result.getAbsolutePath());
					bitmapHead = RSBlur.blur(mContext, bitmapHead, 1);
					mUiHandler.obtainMessage(MSG_TYPE_HEAD_ICON, bitmapHead).sendToTarget();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

	private static final int MSG_TYPE_HEAD_ICON = 1;
	private Handler mUiHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if(msg.what == MSG_TYPE_HEAD_ICON) {
				Bitmap bitmapHead = (Bitmap) msg.obj;
				mImageHeadBg.setImageBitmap(bitmapHead);
			}
		}
	};

	@Override
	public void onClick(View view) {
		int id = view.getId();
		if (id == R.id.tv_usercenter_header_attention_count) {
			if (!UserUtils.isNetDeviceAvailable(mContext)) {
				GolukUtils.showToast(mContext, mContext.getString(R.string.str_network_unavailable));
				return;
			}
			if (null != mData && null != mData.user && null != mData.user.uid) {
				GolukUtils.startFollowingListActivity(mContext, mData.user.uid);
			} else {
				GolukUtils.showToast(mContext, mContext.getString(R.string.str_network_unavailable));
			}
		} else if (id == R.id.tv_usercenter_header_fans_count) {
			if (!UserUtils.isNetDeviceAvailable(mContext)) {
				GolukUtils.showToast(mContext, mContext.getString(R.string.str_network_unavailable));
				return;
			}
			if (null != mData && null != mData.user && null != mData.user.uid) {
				GolukUtils.startFanListActivity(mContext, mData.user.uid);
			} else {
				GolukUtils.showToast(mContext, mContext.getString(R.string.str_network_unavailable));
			}
		} else if (id == R.id.btn_usercenter_header_attention) {
			if (!UserUtils.isNetDeviceAvailable(mContext)) {
				GolukUtils.showToast(mContext, mContext.getString(R.string.str_network_unavailable));
				return;
			}
			if (mUserCenterActivity.testUser()) {
				// 跳到个人中心编辑页面
				Intent it = new Intent(mContext, UserPersonalInfoActivity.class);
				mContext.startActivity(it);
			} else {
				if (!GolukApplication.getInstance().isUserLoginSucess) {
					GolukUtils.showToast(mContext, mContext.getString(R.string.str_usercenter_login_hint_text));
					return;
				}
				if (null == mData || null == mData.user) {
					GolukUtils.showToast(mContext, mContext.getString(R.string.str_network_unavailable));
					return;
				}
				switch (mData.user.link) {
				case 0:
					// 未关注——去关注
					if (null != mContext && mContext instanceof NewUserCenterActivity) {
						mUserCenterActivity.attentionRequest(TYPE_ATTENTION);
					}
					break;
				case 1:
					// 已关注——去取消关注
					if (null != mContext && mContext instanceof NewUserCenterActivity) {
						mUserCenterActivity.attentionRequest(TYPE_ATTENTION_CANCLE);
					}
					break;
				case 2:
					// 互相关注——去取消关注
					if (null != mContext && mContext instanceof NewUserCenterActivity) {
						mUserCenterActivity.attentionRequest(TYPE_ATTENTION_CANCLE);
					}
					break;
				case 3:
					// 别人关注了我——去关注按钮
					if (null != mContext && mContext instanceof NewUserCenterActivity) {
						mUserCenterActivity.attentionRequest(TYPE_ATTENTION);
					}
					break;
				default:
					GolukUtils.showToast(mContext, "no match link");
					break;
				}
			}
		} else if (id == R.id.layout_usercenter_header_category_wonderful) {
			intentToCategory(UserVideoCategoryActivity.COLLECTION_WONDERFUL_VIDEO);
		} else if (id == R.id.layout_usercenter_header_category_recommend) {
			intentToCategory(UserVideoCategoryActivity.COLLECTION_RECOMMEND_VIDEO);
		} else if (id == R.id.layout_usercenter_header_category_headlines) {
			intentToCategory(UserVideoCategoryActivity.COLLECTION_HEADLINES_VIDEO);
		} else if(id == R.id.iv_usercenter_header_head) {
			showLargeHead();
		} else if (id == R.id.ic_cloud_close) {
			mLayoutCloudTip.setVisibility(View.GONE);
			mUserCenterActivity.refresh();
		}
	}

	private void showLargeHead() {
		if (null == mData || null == mData.user || null == mData.user.customavatar || null == mData.user.avatar
				|| null == mData.user.rawavatar) {
			return;
		}
		String largeHead = "";
		Intent itLargeHead = new Intent(mContext, UserLargeHeadActivity.class);
		if (!"".equals(mData.user.customavatar) && !"".equals(mData.user.rawavatar)) {
			// 使用网络地址
			largeHead = mData.user.rawavatar;
		} else {
			largeHead = mData.user.avatar;
		}
		GolukDebugUtils.e("","UserLargeHeadActivity---------largeHead: "+largeHead);
		itLargeHead.putExtra("headurl", largeHead);
		mContext.startActivity(itLargeHead);
	}

	private void intentToCategory(String type) {
		if (!UserUtils.isNetDeviceAvailable(mContext)) {
			GolukUtils.showToast(mContext, mContext.getString(R.string.str_network_unavailable));
			return;
		}
		if (null != mData && null != mData.user && null != mData.user.uid) {
			Intent it = new Intent(mContext, UserVideoCategoryActivity.class);
			it.putExtra("type", type);
			it.putExtra("uid", mData.user.uid);
			mContext.startActivity(it);
		}
	}

	/**
	 * 修改关注按钮状态
	 * @param link
	 */
	public void changeAttentionState(int link) {
		if (!GolukApplication.getInstance().isUserLoginSucess) {
			// 未关注——显示关注按钮
			mAttentionBtn.setBackgroundResource(R.drawable.bg_add);
			Drawable drawable2 = mContext.getResources().getDrawable(R.drawable.usercenter_to_attention);
			drawable2.setBounds(0, 0, drawable2.getMinimumWidth(), drawable2.getMinimumHeight());
			mAttentionBtn.setCompoundDrawables(drawable2, null, null, null);
			mAttentionBtn.setText(mContext.getString(R.string.str_usercenter_header_attention_text));
			mAttentionBtn.setTextColor(Color.parseColor("#0984ff"));
			return;
		}
		switch (link) {
		case 0:
			// 未关注——显示关注按钮
			mAttentionBtn.setBackgroundResource(R.drawable.bg_add);
			Drawable drawable2 = mContext.getResources().getDrawable(R.drawable.usercenter_to_attention);
			drawable2.setBounds(0, 0, drawable2.getMinimumWidth(), drawable2.getMinimumHeight());
			mAttentionBtn.setCompoundDrawables(drawable2, null, null, null);
			mAttentionBtn.setText(mContext.getString(R.string.str_usercenter_header_attention_text));
			mAttentionBtn.setTextColor(Color.parseColor("#0984ff"));
			break;
		case 1:
			// 已关注——显示已关注按钮
			mAttentionBtn.setBackgroundResource(R.drawable.bg_fans);
			Drawable drawable3 = mContext.getResources().getDrawable(R.drawable.usercenter_attention);
			drawable3.setBounds(0, 0, drawable3.getMinimumWidth(), drawable3.getMinimumHeight());
			mAttentionBtn.setCompoundDrawables(drawable3, null, null, null);
			mAttentionBtn.setText(mContext.getString(R.string.str_usercenter_header_attention_already_text));
			mAttentionBtn.setTextColor(Color.parseColor("#ffffff"));
			break;
		case 2:
			// 互相关注——显示互相关注按钮
			mAttentionBtn.setBackgroundResource(R.drawable.bg_follow);
			Drawable drawable4 = mContext.getResources().getDrawable(R.drawable.usercenter_attention_each_other);
			drawable4.setBounds(0, 0, drawable4.getMinimumWidth(), drawable4.getMinimumHeight());
			mAttentionBtn.setCompoundDrawables(drawable4, null, null, null);
			mAttentionBtn.setText(mContext.getString(R.string.str_usercenter_header_attention_each_other_text));
			mAttentionBtn.setTextColor(Color.parseColor("#ffffff"));
			break;
		case 3:
			// 别人关注了我——显示去关注按钮
			mAttentionBtn.setBackgroundResource(R.drawable.bg_add);
			Drawable drawable5 = mContext.getResources().getDrawable(R.drawable.usercenter_to_attention);
			drawable5.setBounds(0, 0, drawable5.getMinimumWidth(), drawable5.getMinimumHeight());
			mAttentionBtn.setCompoundDrawables(drawable5, null, null, null);
			mAttentionBtn.setText(mContext.getString(R.string.str_usercenter_header_attention_text));
			mAttentionBtn.setTextColor(Color.parseColor("#0984ff"));
			break;

		default:
			break;
		}
	}

}
