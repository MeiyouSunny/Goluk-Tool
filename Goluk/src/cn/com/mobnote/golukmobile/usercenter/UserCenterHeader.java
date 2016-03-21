package cn.com.mobnote.golukmobile.usercenter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.usercenter.bean.HomeData;
import cn.com.mobnote.golukmobile.usercenter.bean.HomeUser;
import cn.com.mobnote.user.UserUtils;
import cn.com.mobnote.util.GlideUtils;
import cn.com.mobnote.util.GolukUtils;

public class UserCenterHeader implements OnClickListener {

	private Context mContext;
	private ImageView mImageHead, mAttentionBtn, mLogoImage;
	private TextView mTextName, mTextAttention, mTextFans, mTextContent;
	private LinearLayout mWonderfulLayout, mRecommendLayout, mHeadlinesLayout;
	private TextView mWonderfulText, mRecommednText, mHeadlinesText;

	private HomeData mData;
	private TestGridViewActivity mTestGridViewActivity = null;

	public UserCenterHeader(Context context) {
		super();
		this.mContext = context;
		mTestGridViewActivity = (TestGridViewActivity) mContext;
	}

	public void setHeaderData(HomeData data) {
		this.mData = data;
	}

	public View createHeader() {
		View view = LayoutInflater.from(mContext).inflate(R.layout.activity_usercenter_header, null);
		mImageHead = (ImageView) view.findViewById(R.id.iv_usercenter_header_head);
		mAttentionBtn = (ImageView) view.findViewById(R.id.btn_usercenter_header_attention);
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

		mAttentionBtn.setOnClickListener(this);
		mTextAttention.setOnClickListener(this);
		mTextFans.setOnClickListener(this);
		mWonderfulLayout.setOnClickListener(this);
		mRecommendLayout.setOnClickListener(this);
		mHeadlinesLayout.setOnClickListener(this);
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
			mTextAttention.setText("关注 " + user.following);
			mTextFans.setText("粉丝 " + user.fans);
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
					mTextContent.setText(user.introduction);
				} else {
					mLogoImage.setVisibility(View.GONE);
					mTextContent.setText(user.introduction);
				}
			} else {
				mLogoImage.setVisibility(View.GONE);
				mTextContent.setText(user.introduction);
			}
			
			if(mTestGridViewActivity.testUser()) {
				//显示修改资料按钮
				test = 4;
			} else {
				switch (mData.user.link) {
				case 0:
					// 未关注——显示关注按钮
					test = 0;
					break;
				case 1:
					// 已关注——显示取消关注按钮
					test = 1;
					break;
				case 2:
					// 互相关注——显示取消关注按钮
					test = 2;
					break;
				case 3:
					//别人关注了我——显示关注按钮
					test = 3;
					break;

				default:
					break;
				}
				
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
			String show = "";
			if(test == 0) {
				show = "关注成功";
			} else if(test == 1) {
				show = "取消关注成功";
			} else if(test == 2) {
				show = "取消关注成功";
			} else if(test == 3) {
				show = "关注成功";
			} else {
				show = "编辑资料";
			}
			GolukUtils.showToast(mContext, show);
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
}
