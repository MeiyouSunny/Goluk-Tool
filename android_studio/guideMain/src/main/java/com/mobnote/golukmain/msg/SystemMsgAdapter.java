package com.mobnote.golukmain.msg;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.UserOpenUrlActivity;
import com.mobnote.golukmain.http.HttpManager;
import com.mobnote.golukmain.msg.bean.MessageMsgsBean;
import com.mobnote.golukmain.profit.MyProfitActivity;
import com.mobnote.golukmain.special.SpecialListActivity;
import com.mobnote.golukmain.videodetail.VideoDetailActivity;
import com.mobnote.util.GlideUtils;
import com.mobnote.util.GolukConfig;
import com.mobnote.util.GolukUtils;
import com.mobnote.util.ZhugeUtils;

import java.util.List;

import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.serveraddress.IGetServerAddressType;

@SuppressLint("CutPasteId")
public class SystemMsgAdapter extends BaseAdapter {

	public Context mContext;

	public List<MessageMsgsBean> mMsgList;

	private final static int sMessageTypeTxt = 0;// 普通文字类型
	private final static int sMessageTypeImg = 1;// 带图片的消息类型
	/** 系统获奖 **/
	private final static int msgTypeRewardsystem = 201;
	/** 人工获奖 **/
	private final static int msgTypeRewardmanual = 202;
	/** 推荐 **/
	private final static int msgTypeRecommend = 203;
	/** 认证 **/
	private final static int msgTypeCertificate = 204;
	/** 提现 **/
	private final static int msgTypeWithdraw = 205;
	/** 投票 **/
	private final static int msgTypePoll = 206;
	/** 精选 **/
	private final static int msgTypeSelect = 207;
    /** G币奖励 **/
    private final static int msgTypeGReward = 208;
	/** 失败 **/
	private final static String resultFial = "0";

	/** 审核 **/
	private final static String WITHDRAWTYPESH = "0";
	/** 单视频 **/
	private static String selectDsp = "6";

	/** mUid当前用户的uid **/
	private String mUid = "";
	/** 精选数据类型 **/
	private static String sSpecialType = "2";

	public SystemMsgAdapter(Context context, String uid) {
		mContext = context;
		mUid = uid;
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public int getCount() {
		if (mMsgList != null && mMsgList.size() > 0) {
			return mMsgList.size();
		} else {
			return 0;
		}

	}

	public void setData(List<MessageMsgsBean> data) {
		this.mMsgList = data;
	}

	@Override
	public Object getItem(int position) {
		return 0;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public int getItemViewType(int position) {
		MessageMsgsBean json = mMsgList.get(position);
		if (msgTypeCertificate == json.type || msgTypeWithdraw == json.type || msgTypePoll == json.type
				|| msgTypeSelect == json.type || msgTypeGReward == json.type) {
			return 0;
		} else {
			return 1;
		}

	}

	@Override
	public View getView(int position, View convertView, ViewGroup vg) {
		int type = getItemViewType(position);
		switch (type) {
		case sMessageTypeTxt:
			final MessageMsgsBean mmbTxt = mMsgList.get(position);

			TxtHolder txtHolder = null;
			if (convertView == null) {
				convertView = LayoutInflater.from(mContext).inflate(R.layout.msg_system_txt, null);
				txtHolder = new TxtHolder();
				txtHolder.msgTime = (TextView) convertView.findViewById(R.id.msg_time);
				txtHolder.msgTxt = (TextView) convertView.findViewById(R.id.msg_txt);
				txtHolder.msgLayout = (RelativeLayout) convertView.findViewById(R.id.all_layout);
				convertView.setTag(txtHolder);
			} else {
				txtHolder = (TxtHolder) convertView.getTag();
			}

			String time = "";
			String txt = "";

			time = mmbTxt.content.time;
			if (mmbTxt.type == msgTypeCertificate) {// 认证
				if (resultFial.equals(mmbTxt.content.certificate.result)) {// 失败
					txt = mContext.getResources().getString(R.string.msg_system_certificate_fail);
				} else {
					if("0".equals(mmbTxt.content.certificate.type)){
						txt = mContext.getResources().getString(R.string.msg_system_certificate_success_sys);
					}else{
						txt = mContext.getResources().getString(R.string.msg_system_certificate_success);
					}
				}

			} else if (mmbTxt.type == msgTypeWithdraw) {// 提现
				if (WITHDRAWTYPESH.equals(mmbTxt.content.withdraw.type)) {
					if (resultFial.equals(mmbTxt.content.withdraw.result)) {
						txt = mContext.getResources().getString(R.string.msg_system_withdraw_check_fail);
					} else {
						txt = mContext.getResources().getString(R.string.msg_system_withdraw_check_success);
					}
				} else {
					if (resultFial.equals(mmbTxt.content.withdraw.result)) {
						txt = mContext.getResources().getString(R.string.msg_system_withdraw_fail);
					} else {
						txt = mContext.getResources().getString(R.string.msg_system_withdraw_success);
					}
				}

			} else if (mmbTxt.type == msgTypePoll) {// 投票
				txt = mContext.getResources().getString(R.string.msg_system_poll_txt);
			} else if (mmbTxt.type == msgTypeSelect) {// 精选
				txt = mContext.getResources().getString(R.string.msg_system_select_txt);
			} else if (mmbTxt.type == msgTypeGReward) {// G币奖励

				txt = mContext.getString(R.string.str_sys_msg_gaward_text)
						+ mmbTxt.content.gaward.reason + mmbTxt.content.gaward.name;
            }

			txtHolder.msgTime.setText(GolukUtils.getCommentShowFormatTime(mContext, mmbTxt.content.ts));
			txtHolder.msgTxt.setText(txt);
			txtHolder.msgLayout.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					if (GolukUtils.isNetworkConnected(mContext)) {
						if (mmbTxt.type == msgTypeCertificate) {// 认证消息跳转到个人中心
                            GolukUtils.startUserCenterActivity(mContext,mmbTxt.receiver.uid);
						} else if (mmbTxt.type == msgTypeSelect) {// 跳转到专题页
							if (sSpecialType.equals(mmbTxt.content.type)) {//专题
								String specialid = mmbTxt.content.access;
								Intent i = new Intent(mContext, SpecialListActivity.class);
								i.putExtra("ztid", specialid);
								i.putExtra("title", "");
								mContext.startActivity(i);
							}else if (selectDsp.equals(mmbTxt.content.type)) {//单视频
								//视频详情页访问
								ZhugeUtils.eventVideoDetail(mContext, mContext.getString(R.string.str_zhuge_msg_center));

								String specialid = mmbTxt.content.access;
								Intent i = new Intent(mContext, VideoDetailActivity.class);
								i.putExtra(VideoDetailActivity.TYPE, "Wonderful");
								i.putExtra("ztid", specialid);
								mContext.startActivity(i);
							}
						} else if (mmbTxt.type == msgTypeWithdraw) {// 收益详情页
							if ("7".equals(mmbTxt.content.type)) {
								String withdraw_url = getRtmpAddress() + "?type=10&serialno=" + mmbTxt.content.access;
								Intent i = new Intent(mContext, UserOpenUrlActivity.class);
								i.putExtra("withdraw_url", withdraw_url);
								i.putExtra(UserOpenUrlActivity.FROM_TAG, "withdrawals");
								mContext.startActivity(i);
							}
						} else if (mmbTxt.type == msgTypePoll) {// 投票页
							String url = HttpManager.getInstance().getWebH5Host()
									+ "/videoshare/tag/castvote.html?voteid=" + mmbTxt.content.access;
							Intent intent = new Intent(mContext, UserOpenUrlActivity.class);
							intent.putExtra(GolukConfig.H5_URL, url);
							intent.putExtra(GolukConfig.NEED_H5_TITLE, mContext.getString(R.string.str_activity_rule));
							mContext.startActivity(intent);
						} else if(mmbTxt.type == msgTypeGReward){
							//中奖消息点击不跳转
                        }

					} else {
						GolukUtils.showToast(mContext, mContext.getResources().getString(R.string.user_net_unavailable));
						return;
					}
				}
			});
			break;
		case sMessageTypeImg:
			final MessageMsgsBean mmbImg = mMsgList.get(position);
			ImageHolder imageHolder = null;
			if (convertView == null) {

				imageHolder = new ImageHolder();
				convertView = LayoutInflater.from(mContext).inflate(R.layout.msg_system_image, null);
				imageHolder.msgTime = (TextView) convertView.findViewById(R.id.msg_time);
				imageHolder.msgTxt = (TextView) convertView.findViewById(R.id.msg_txt);
				imageHolder.msgReasonTxt = (TextView) convertView.findViewById(R.id.msg_reason_txt);
				imageHolder.msgImage = (ImageView) convertView.findViewById(R.id.msg_image);
				imageHolder.msgMyincome = (TextView) convertView.findViewById(R.id.msg_my_income_btn);
				convertView.setTag(imageHolder);
			} else {
				imageHolder = (ImageHolder) convertView.getTag();
			}

			String imgTime = mmbImg.content.time;
			String imgReason = "";
			String imgPath = "";
			String imgTxt = "";

			if (mmbImg.type == msgTypeRewardsystem || mmbImg.type == msgTypeRewardmanual ) {// 系统获奖或者人工获奖

                imgReason = mmbImg.content.reward.reason;

				imgTxt = mContext.getResources().getString(R.string.msg_system_reward_txt_began)
						+ mmbImg.content.reward.count
						+ mContext.getResources().getString(R.string.msg_system_reward_txt_end);
				imgPath = mmbImg.content.picture;
				imageHolder.msgMyincome.setVisibility(View.VISIBLE);
				imageHolder.msgMyincome.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View view) {
						if (GolukUtils.isNetworkConnected(mContext)) {
							Intent intent = new Intent(mContext, MyProfitActivity.class);
							mContext.startActivity(intent);
						} else {
							GolukUtils.showToast(mContext,
									mContext.getResources().getString(R.string.user_net_unavailable));
						}

					}
				});
			} else if (mmbImg.type == msgTypeRecommend) {// 推荐
				imgPath = mmbImg.content.picture;
				imgTxt = mContext.getResources().getString(R.string.msg_system_recommend_title);
				imgReason = mmbImg.content.recommend.reason;
				imageHolder.msgMyincome.setVisibility(View.GONE);
			}
			imageHolder.msgImage.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {

					if (GolukUtils.isNetworkConnected(mContext)) {
						//视频详情页访问
						ZhugeUtils.eventVideoDetail(mContext, mContext.getString(R.string.str_zhuge_msg_center));

						System.out.println("woqunimeide" + mmbImg.content.access);
						Intent intent = new Intent(mContext, VideoDetailActivity.class);
						intent.putExtra("videoid", mmbImg.content.access);
						mContext.startActivity(intent);
					} else {
						GolukUtils
								.showToast(mContext, mContext.getResources().getString(R.string.user_net_unavailable));
					}

				}
			});

			GlideUtils.loadImage(mContext, imageHolder.msgImage, imgPath, 0);
			imageHolder.msgTime.setText(GolukUtils.getCommentShowFormatTime(mContext, mmbImg.content.ts));
			imageHolder.msgTxt.setText(imgTxt);
			imageHolder.msgReasonTxt.setText(mContext.getResources().getString(R.string.msg_system_reason_began)
					+ imgReason);
			break;
		default:
			break;
		}
		return convertView;
	}

	public static class TxtHolder {
		TextView msgTxt;
		TextView msgTime;
		RelativeLayout msgLayout;
	}

	public static class ImageHolder {
		ImageView msgImage;
		TextView msgTxt;
		TextView msgReasonTxt;
		TextView msgTime;
		TextView msgMyincome;
	}

	public static class NoVideoDataViewHolder {
		TextView tips;
		ImageView tipsimage;
		boolean bMeasureHeight;
	}

	private String getRtmpAddress() {
		String rtmpUrl = GolukApplication.getInstance().mGoluk.GolukLogicCommGet(
				GolukModule.Goluk_Module_GetServerAddress, IGetServerAddressType.GetServerAddress_HttpServer,
				"UrlRedirect");
		return rtmpUrl;
	}

}
