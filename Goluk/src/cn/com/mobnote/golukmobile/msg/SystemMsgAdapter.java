package cn.com.mobnote.golukmobile.msg;

import java.util.List;

import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.UserOpenUrlActivity;
import cn.com.mobnote.golukmobile.cluster.ClusterActivity;
import cn.com.mobnote.golukmobile.http.HttpManager;
import cn.com.mobnote.golukmobile.live.UserInfo;
import cn.com.mobnote.golukmobile.msg.bean.MessageMsgsBean;
import cn.com.mobnote.golukmobile.profit.MyProfitActivity;
import cn.com.mobnote.golukmobile.special.SpecialListActivity;
import cn.com.mobnote.golukmobile.usercenter.UCUserInfo;
import cn.com.mobnote.golukmobile.usercenter.UserCenterActivity;
import cn.com.mobnote.golukmobile.videodetail.VideoDetailActivity;
import cn.com.mobnote.golukmobile.videosuqare.VideoSquareInfo;
import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.serveraddress.IGetServerAddressType;
import cn.com.mobnote.util.GlideUtils;
import cn.com.mobnote.util.GolukUtils;
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

@SuppressLint("CutPasteId")
public class SystemMsgAdapter extends BaseAdapter {

	public Context mContext;

	public List<MessageMsgsBean> mMsgList;

	private final static int sMessageTypeTxt = 0;// 普通文字类型
	private final static int sMessageTypeImg = 1;// 带图片的消息类型
	private final static int sMessageTypeError = 2;// 数据异常

	/** 所有的系统消息 **/
	private final static int msgTypeSystem = 200;
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

	/** 失败 **/
	private final static String resultFial = "0";

	/** 审核 **/
	private final static String WITHDRAWTYPESH = "0";
	/** 汇款 **/
	private final static String WITHDRAWTYPEHK = "1";

	/** 成功 **/
	private final static String resultSuccess = "1";
	/**专题**/
	private  static String selectZt = "1";
	/**tag聚合**/
	private  static String selectJh = "2";
	/**单视频**/
	private  static String selectDsp = "3";
	
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
				|| msgTypeSelect == json.type) {
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
					txt = mContext.getResources().getString(R.string.msg_system_certificate_success);
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
			}

			txtHolder.msgTime.setText(GolukUtils.getCommentShowFormatTime(time));
			txtHolder.msgTxt.setText(txt);
			txtHolder.msgLayout.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					if (GolukUtils.isNetworkConnected(mContext)) {
						if (mmbTxt.type == msgTypeCertificate) {// 认证消息跳转到个人中心
							UCUserInfo user = new UCUserInfo();
							user.uid = mmbTxt.receiver.uid;
							Intent i = new Intent(mContext, UserCenterActivity.class);
							i.putExtra("userinfo", user);
							i.putExtra("type", 0);
							mContext.startActivity(i);
						} else if (mmbTxt.type == msgTypeSelect) {// 跳转到专题页
							if (sSpecialType.equals(mmbTxt.content.type)) {
								String specialid = mmbTxt.content.access;
								Intent i = null;
								
								if(selectZt.equals(mmbTxt.content.select.type)){//专题
									i = new Intent(mContext, SpecialListActivity.class);
									i.putExtra("ztid", specialid);
									i.putExtra("title", "");
								}else if(selectJh.equals(mmbTxt.content.select.type)){//聚合
									i = new Intent(mContext, ClusterActivity.class);
									i.putExtra("activityid", specialid);
									i.putExtra("cluster_key_title", "");
								}else{//单视频
									i = new Intent(mContext, VideoDetailActivity.class);
									i.putExtra("videoid", specialid);
								}
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
							intent.putExtra("url", url);
							intent.putExtra("slide_h5_title", mContext.getString(R.string.str_activity_rule));
							mContext.startActivity(intent);
						}

					}else{
						GolukUtils.showToast(mContext, mContext.getResources().getString(R.string.user_net_unavailable));
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

			if (mmbImg.type == msgTypeRewardsystem || mmbImg.type == msgTypeRewardmanual) {// 系统获奖或者人工获奖
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
							// intent.putExtra("uid", mUid);
							mContext.startActivity(intent);
						}else{
							GolukUtils.showToast(mContext, mContext.getResources().getString(R.string.user_net_unavailable));
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
						System.out.println("woqunimeide" + mmbImg.content.access);
						Intent intent = new Intent(mContext, VideoDetailActivity.class);
						intent.putExtra("videoid", mmbImg.content.access);
						mContext.startActivity(intent);
					}else{
						GolukUtils.showToast(mContext, mContext.getResources().getString(R.string.user_net_unavailable));
					}
					
				}
			});

			GlideUtils.loadImage(mContext, imageHolder.msgImage, imgPath, 0);
			imageHolder.msgTime.setText(GolukUtils.getCommentShowFormatTime(imgTime));
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
