package cn.com.mobnote.golukmobile.usercenter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.UserPersonalInfoActivity;
import cn.com.mobnote.golukmobile.usercenter.bean.HomeData;
import cn.com.mobnote.golukmobile.usercenter.bean.HomeUser;
import cn.com.mobnote.user.UserUtils;
import cn.com.mobnote.util.GlideUtils;
import cn.com.mobnote.util.GolukUtils;
import cn.com.tiros.debug.GolukDebugUtils;

public class UserCenterHeader implements OnClickListener {

	private Context mContext;
	private ImageView mImageHead, mLogoImage;
	private TextView mTextName, mTextAttention, mTextFans, mTextContent, mTextNoData;
	private LinearLayout mWonderfulLayout, mRecommendLayout, mHeadlinesLayout;
	private TextView mWonderfulText, mRecommednText, mHeadlinesText;
	private Button mAttentionBtn;
	private LinearLayout mAttentionFansLayout, mBackgroundLayout;
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

	public View createHeader() {
		View view = LayoutInflater.from(mContext).inflate(R.layout.activity_usercenter_header, null);
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
		mBackgroundLayout = (LinearLayout) view.findViewById(R.id.layout_usercenter_header);
		mTextNoData = (TextView) view.findViewById(R.id.tv_usercenter_header_no_introduce);
		mAttentionFansLayout = (LinearLayout) view.findViewById(R.id.ly_usercenter_header_attention_fans);

		mAttentionBtn.setOnClickListener(this);
		mTextAttention.setOnClickListener(this);
		mTextFans.setOnClickListener(this);
		mWonderfulLayout.setOnClickListener(this);
		mRecommendLayout.setOnClickListener(this);
		mHeadlinesLayout.setOnClickListener(this);
		
//		Bitmap image = ((BitmapDrawable)imageView.getDrawable()).getBitmap(); 
		
		return view;
	}

	int test = 0;
	public void getHeaderData() {
		if (null != mData) {
			HomeUser user = mData.user;
			if (null != user && !"".equals(user.customavatar)) {
				// 使用网络地址
				GlideUtils.loadNetHead(mContext, mImageHead, user.customavatar, R.drawable.my_head_moren7);
			} else {
				UserUtils.focusHead(mContext, user.avatar, mImageHead);
			}
			mTextName.setText(user.nickname);
			mTextAttention.setText(mContext.getString(R.string.str_usercenter_header_attention_text) + " "
					+ user.following);
			mTextFans.setText(mContext.getString(R.string.str_usercenter_header_fans_text) + " " + user.fans);
			mWonderfulText.setText(mData.selectcount + "");
			mRecommednText.setText(mData.recommendcount + "");
			mHeadlinesText.setText(mData.headlinecount + "");
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
					mTextContent.setText(orgcertification);
				} else if ("1".equals(isusercertificated)) {
					mLogoImage.setImageResource(R.drawable.authentication_yellowv_icon);
					mTextContent.setText(usercertification);
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
				test = 4;
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

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.tv_usercenter_header_attention_count:
			GolukUtils.showToast(mContext, "intent to attention");
			break;
		case R.id.tv_usercenter_header_fans_count:
			GolukUtils.showToast(mContext, "intent to fans");
			break;
		case R.id.btn_usercenter_header_attention:
			if (mUserCenterActivity.testUser()) {
				// 跳到个人中心编辑页面
				Intent it = new Intent(mContext, UserPersonalInfoActivity.class);
				mContext.startActivity(it);
			} else {
				GolukDebugUtils.e("", "----------usercenterheader-------onclick----link: " + mData.user.link);
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
					GolukUtils.showToast(mContext, "没有符合的link");
					break;
				}
			}
			break;
		case R.id.layout_usercenter_header_category_wonderful:
			intentToCategory(UserVideoCategoryActivity.COLLECTION_WONDERFUL_VIDEO);
			break;
		case R.id.layout_usercenter_header_category_recommend:
			intentToCategory(UserVideoCategoryActivity.COLLECTION_RECOMMEND_VIDEO);
			break;
		case R.id.layout_usercenter_header_category_headlines:
			intentToCategory(UserVideoCategoryActivity.COLLECTION_HEADLINES_VIDEO);
			break;

		default:
			break;
		}
	}

	private void intentToCategory(String type) {
		Intent it = new Intent(mContext, UserVideoCategoryActivity.class);
		it.putExtra("type", type);
		it.putExtra("uid", mData.user.uid);
		mContext.startActivity(it);
	}
	
	private Bitmap getTransparentBitmap(Bitmap sourceImg, int number) {
		int[] argb = new int[sourceImg.getWidth() * sourceImg.getHeight()];

		sourceImg.getPixels(argb, 0, sourceImg.getWidth(), 0, 0, sourceImg.getWidth(), sourceImg.getHeight());// 获得图片的ARGB值

		number = number * 255 / 100;

		for (int i = 0; i < argb.length; i++) {
			argb[i] = (number << 24) | (argb[i] & 0x00FFFFFF);
		}
		sourceImg = Bitmap.createBitmap(argb, sourceImg.getWidth(), sourceImg.getHeight(), Config.ARGB_8888);

		return sourceImg;
	}
	
	public void usercenterNoData(boolean hasData) {
		if (!hasData) {
			mTextName.setVisibility(View.GONE);
			mTextContent.setVisibility(View.GONE);
			mAttentionBtn.setVisibility(View.GONE);
			mTextNoData.setVisibility(View.VISIBLE);
			mTextAttention.setText(mContext.getString(R.string.str_usercenter_header_attention_text) + " 0");
			mTextFans.setText(mContext.getString(R.string.str_usercenter_header_fans_text) + " 0");
			mAttentionFansLayout.setPadding(0, 23, 0, 0);
		} else {
			mTextName.setVisibility(View.VISIBLE);
			mTextContent.setVisibility(View.VISIBLE);
			mAttentionBtn.setVisibility(View.VISIBLE);
			mTextNoData.setVisibility(View.GONE);
			mAttentionFansLayout.setPadding(0, 0, 0, 0);
		}
	}

	/**
	 * 修改关注按钮状态
	 * @param link
	 */
	public void changeAttentionState(int link) {
		switch (link) {
		case 0:
			// 未关注——显示关注按钮
			test = 0;
			mAttentionBtn.setBackgroundResource(R.drawable.bg_add);
			Drawable drawable2 = mContext.getResources().getDrawable(R.drawable.usercenter_to_attention);
			drawable2.setBounds(0, 0, drawable2.getMinimumWidth(), drawable2.getMinimumHeight());
			mAttentionBtn.setCompoundDrawables(drawable2, null, null, null);
			mAttentionBtn.setText(mContext.getString(R.string.str_usercenter_header_attention_text));
			mAttentionBtn.setTextColor(Color.parseColor("#0984ff"));
			break;
		case 1:
			// 已关注——显示已关注按钮
			test = 1;
			mAttentionBtn.setBackgroundResource(R.drawable.bg_fans);
			Drawable drawable3 = mContext.getResources().getDrawable(R.drawable.usercenter_attention);
			drawable3.setBounds(0, 0, drawable3.getMinimumWidth(), drawable3.getMinimumHeight());
			mAttentionBtn.setCompoundDrawables(drawable3, null, null, null);
			mAttentionBtn.setText(mContext.getString(R.string.str_usercenter_header_attention_already_text));
			mAttentionBtn.setTextColor(Color.parseColor("#ffffff"));
			break;
		case 2:
			// 互相关注——显示互相关注按钮
			test = 2;
			mAttentionBtn.setBackgroundResource(R.drawable.bg_follow);
			Drawable drawable4 = mContext.getResources().getDrawable(R.drawable.usercenter_attention_each_other);
			drawable4.setBounds(0, 0, drawable4.getMinimumWidth(), drawable4.getMinimumHeight());
			mAttentionBtn.setCompoundDrawables(drawable4, null, null, null);
			mAttentionBtn.setText(mContext.getString(R.string.str_usercenter_header_attention_each_other_text));
			mAttentionBtn.setTextColor(Color.parseColor("#ffffff"));
			break;
		case 3:
			// 别人关注了我——显示去关注按钮
			test = 3;
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
