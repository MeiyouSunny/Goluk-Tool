package cn.com.mobnote.golukmobile.startshare;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Queue;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.text.Html;
import android.text.TextUtils;
import android.util.SparseIntArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.live.GetBaiduAddress;
import cn.com.mobnote.golukmobile.live.GetBaiduAddress.IBaiduGeoCoderFn;
import cn.com.mobnote.golukmobile.newest.IDialogDealFn;
import cn.com.mobnote.golukmobile.promotion.PromotionActivity;
import cn.com.mobnote.golukmobile.promotion.PromotionData;
import cn.com.mobnote.golukmobile.promotion.PromotionSelectItem;
import cn.com.mobnote.map.LngLat;
import cn.com.mobnote.user.UserUtils;
import cn.com.mobnote.util.GolukFileUtils;
import cn.com.mobnote.util.GolukUtils;

import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult.AddressComponent;

public class ShareTypeLayout implements OnClickListener, IBaiduGeoCoderFn, IDialogDealFn {

	private final int TYPE_BG = 0;
	private final int TYPE_SG = 1;
	private final int TYPE_ML = 2;
	private final int TYPE_SSP = 3;

	private int mCurrentType = TYPE_BG;

//	private final int[] hintArray = {R.string.default_comment1, R.string.default_comment2, R.string.default_comment3,
//			R.string.default_comment4 };

	private Context mContext = null;
	private LayoutInflater mLayoutFlater = null;
	private RelativeLayout mRootLayout = null;
	private TextView mTextView = null;

	/** 曝光台 */
	private TextView mBgBtn = null;
	private TextView mSgBtn = null;
	private TextView mMlBtn = null;
	private TextView mSspBtn = null;

	private LinearLayout mShareOpenLayout = null;
	private ImageView mShareOpenImg = null;
	private String resShareOpen = null;
	private String resShareClose = null;
	/** 是否分享到视频广场 */
	private boolean mIsOpenShare = true;

	/** 曝光台,事故大爆炸,美丽风景, 随手拍　背景颜色 */
	private int resTypeSelectColor = 0;
	private int resTypeUnSelectColor = 0;
	private LinearLayout mShareAddressLayout = null;
	private TextView mAddressTv = null;
	private ImageView mAddressImg = null;

	private TextView[] typeViewArray = new TextView[4];
	private SparseIntArray mTypeArray = new SparseIntArray();
	/** 定位状态 0 表示定位中, 1 表示定位成功, 2表示点击定位, 3 表示用户删除了位置 */
	private int mLocationState = 0;

	/** 定位中 */
	public static final int LOCATION_STATE_ING = 0;
	/** 定位成功 */
	public static final int LOCATION_STATE_SUCCESS = 1;
	/** 定位失败 */
	public static final int LOCATION_STATE_FAILED = 2;
	/** 用户禁止使用位置 */
	public static final int LOCATION_STATE_FORBID = 3;
	/** 保存当前定位信息 */
	private String mCurrentAddress = "";
	private StartShareFunctionDialog mStartShareDialog = null;

	/**活动*/
	private TextView mPromotionTextView1;
	private TextView mPromotionTextView2;
	private TextView mMorePromotions;
	private TextView mNewFlagTextView;
	private PopupWindow mPopupWindow;
	private boolean bPopup;
	private PromotionSelectItem mPromotionSelectItem;
	private ArrayList<PromotionData> mPromotionList;
	private ArrayList<PromotionSelectItem> mRecommendActivities;
	private boolean bShowNew = false;
	private String mMd5String;
	private Toast mToast;

	public ShareTypeLayout(Context context, PromotionSelectItem item) {
		mContext = context;
		mLayoutFlater = LayoutInflater.from(mContext);
		mRootLayout = (RelativeLayout) mLayoutFlater.inflate(R.layout.shareselecttype, null);
		mPromotionSelectItem = item;
		bPopup = GolukFileUtils.loadBoolean(GolukFileUtils.SHOW_PROMOTION_POPUP_FLAG, true);
		mRecommendActivities = new ArrayList<PromotionSelectItem>(2);
		loadRes();
		initView();
		initData();

		switchTypeUI(TYPE_BG);
		mLocationState = LOCATION_STATE_ING;
		refreshLocationUI();
		// 请求位置信息
		GetBaiduAddress.getInstance().setCallBackListener(this);
		GetBaiduAddress.getInstance().searchAddress(LngLat.lat, LngLat.lng);
	}

	private void initData() {
		mTypeArray.put(TYPE_BG, 1);
		mTypeArray.put(TYPE_SG, 5);
		mTypeArray.put(TYPE_ML, 3);
		mTypeArray.put(TYPE_SSP, 4);
	}

	private void loadRes() {
		resShareOpen = mContext.getResources().getString(R.string.share_str_open);
		resShareClose = mContext.getResources().getString(R.string.share_str_close);

		resTypeSelectColor = mContext.getResources().getColor(R.color.share_type_shigu_select);
		resTypeUnSelectColor = mContext.getResources().getColor(R.color.share_type_shigu_unselect);
	}

	public ViewGroup getRootLayout() {
		return mRootLayout;
	}

	private void initView() {
		mTextView = (TextView) mRootLayout.findViewById(R.id.share_sayother);
		mTextView.setOnClickListener(this);

		mBgBtn = (TextView) mRootLayout.findViewById(R.id.share_type_bg);
		mSgBtn = (TextView) mRootLayout.findViewById(R.id.share_type_sg);
		mMlBtn = (TextView) mRootLayout.findViewById(R.id.share_type_ml);
		mSspBtn = (TextView) mRootLayout.findViewById(R.id.share_type_ssp);
		mPromotionTextView1 = (TextView) mRootLayout.findViewById(R.id.activity_textview1);
		mPromotionTextView1.setOnClickListener(this);

		mPromotionTextView2 = (TextView) mRootLayout.findViewById(R.id.activity_textview2);
		mPromotionTextView2.setOnClickListener(this);
		
		mMorePromotions = (TextView) mRootLayout.findViewById(R.id.join_activity_textview);
		mMorePromotions.setOnClickListener(this);
		mNewFlagTextView = (TextView) mRootLayout.findViewById(R.id.new_textview);
		mNewFlagTextView.setOnClickListener(this);

		typeViewArray[0] = mBgBtn;
		typeViewArray[1] = mSgBtn;
		typeViewArray[2] = mMlBtn;
		typeViewArray[3] = mSspBtn;

		mShareOpenLayout = (LinearLayout) mRootLayout.findViewById(R.id.share_open_layout);
		mShareOpenImg = (ImageView) mRootLayout.findViewById(R.id.share_open_img);

		mShareAddressLayout = (LinearLayout) mRootLayout.findViewById(R.id.share_address_layout);
		mAddressTv = (TextView) mRootLayout.findViewById(R.id.share_address_txt);
		mAddressImg = (ImageView) mRootLayout.findViewById(R.id.share_address_img);

		mBgBtn.setOnClickListener(this);
		mSgBtn.setOnClickListener(this);
		mMlBtn.setOnClickListener(this);
		mSspBtn.setOnClickListener(this);
		mShareOpenLayout.setOnClickListener(this);
		mShareAddressLayout.setOnClickListener(this);
		switchOpenAndClose(mIsOpenShare);
	}

	public boolean isOpenShare() {
		return mIsOpenShare;
	}

	public int getCurrentSelectType() {
		return mTypeArray.get(mCurrentType);
	}

	// 返回当前的描述文字
	public String getCurrentDesc() {
		String inputStr = mTextView.getText().toString().trim();
		if (null != inputStr && !inputStr.equals("")) {
			return inputStr;
		}
		return mContext.getString(R.string.default_comment);
	}

	private void switchOpenAndClose(boolean isOpen) {
		mIsOpenShare = isOpen;
		if (mIsOpenShare) {
			mShareOpenImg.setBackgroundResource(R.drawable.share_open_icon);
		} else {
			showToast(R.string.str_video_close_hint);
			mShareOpenImg.setBackgroundResource(R.drawable.share_close_icon);
		}
	}

	private void switchTypeUI(final int select) {
		mCurrentType = select;
		final int length = typeViewArray.length;
		for (int i = 0; i < length; i++) {
			if (select == i) {
				typeViewArray[i].setBackgroundResource(R.drawable.share_type_bg);
				typeViewArray[i].setTextColor(resTypeSelectColor);
//				mTextView.setHint(hintArray[i]);
			} else {
				typeViewArray[i].setBackgroundDrawable(null);
				typeViewArray[i].setTextColor(resTypeUnSelectColor);
			}
		}
	}

	public void setEditContent(boolean isCancel, String content) {
		if (isCancel) {
			// mEditText.setText("");
		} else {
			mTextView.setText(content);
		}
	}

	private boolean mIsExit = false;

	public void setExit() {
		mIsExit = true;
		GetBaiduAddress.getInstance().setCallBackListener(null);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.share_type_bg:
			if (mIsExit) {
				return;
			}
			switchTypeUI(TYPE_BG);
			break;
		case R.id.share_type_sg:
			if (mIsExit) {
				return;
			}
			switchTypeUI(TYPE_SG);
			break;
		case R.id.share_type_ml:
			if (mIsExit) {
				return;
			}
			switchTypeUI(TYPE_ML);
			break;
		case R.id.share_type_ssp:
			if (mIsExit) {
				return;
			}
			switchTypeUI(TYPE_SSP);
			break;
		case R.id.share_open_layout:
			if (mIsExit) {
				return;
			}
			if (mPromotionSelectItem != null) {
				showToast(R.string.str_video_open_hint);
				return;
			}
			switchOpenAndClose(!mIsOpenShare);
			break;
		case R.id.share_sayother:
			if (mIsExit) {
				return;
			}
			click_input();
			break;
		case R.id.share_address_layout:
			click_location();
			break;
		case R.id.activity_textview1:
			PromotionSelectItem item = (PromotionSelectItem) mPromotionTextView1.getTag();
			if (item == null) {
				return;
			}
			if (mPromotionSelectItem != null && item.activityid.equalsIgnoreCase(mPromotionSelectItem.activityid)) {
				mPromotionSelectItem = null;
				mPromotionTextView1.setTextColor(Color.parseColor("#bcbdbd"));
				mPromotionTextView2.setTextColor(Color.parseColor("#bcbdbd"));
			} else {
				mPromotionSelectItem = item;
				switchOpenAndClose(true);
				mPromotionTextView1.setTextColor(Color.parseColor("#ffcc00"));
				mPromotionTextView2.setTextColor(Color.parseColor("#bcbdbd"));
			}
			break;
		case R.id.activity_textview2:
			PromotionSelectItem item2 = (PromotionSelectItem) mPromotionTextView2.getTag();
			if (item2 == null) {
				return;
			}
			if (mPromotionSelectItem != null && mPromotionSelectItem.activityid.equalsIgnoreCase(item2.activityid)) {
				mPromotionSelectItem = null;
				mPromotionTextView1.setTextColor(Color.parseColor("#bcbdbd"));
				mPromotionTextView2.setTextColor(Color.parseColor("#bcbdbd"));
			} else {
				mPromotionSelectItem = item2;
				switchOpenAndClose(true);
				mPromotionTextView2.setTextColor(Color.parseColor("#ffcc00"));
				mPromotionTextView1.setTextColor(Color.parseColor("#bcbdbd"));
			}
			break;
		case R.id.join_activity_textview:
			if (!UserUtils.isNetDeviceAvailable(mContext)) {
				GolukUtils.showToast(mContext, mContext.getResources().getString(R.string.user_net_unavailable));
				return;
			}
			if (bShowNew) {
				bShowNew = false;
				GolukFileUtils.saveString(GolukFileUtils.PROMOTION_LIST_STRING, mMd5String);
				mNewFlagTextView.setVisibility(View.INVISIBLE);
			}
			Intent intent = new Intent(mContext, PromotionActivity.class);
			if (mPromotionSelectItem != null) {
				intent.putExtra(PromotionActivity.PROMOTION_SELECTED_ITEM, mPromotionSelectItem.activityid);
			}

			if (mPromotionList != null) {

				intent.putExtra(PromotionActivity.PROMOTION_DATA, mPromotionList);
			}

			((Activity)mContext).startActivityForResult(intent, VideoEditActivity.PROMOTION_ACTIVITY_BACK);
			break;
		default:
			break;
		}
	}

	public String getCurrentLocation() {
		if (LOCATION_STATE_SUCCESS == this.mLocationState) {
			return this.mCurrentAddress;
		}
		return "";
	}

	private void showDealDialog() {
		dissmissDialog();
		mStartShareDialog = new StartShareFunctionDialog(mContext, this);
		mStartShareDialog.show();
	}

	private void dissmissDialog() {
		if (null != mStartShareDialog) {
			mStartShareDialog.dismiss();
			mStartShareDialog = null;
		}
	}

	private void click_location() {
		switch (mLocationState) {
		case LOCATION_STATE_ING:
			// 当前状态是定位中，用户点击，直接再次发起定位
			GetBaiduAddress.getInstance().searchAddress(LngLat.lat, LngLat.lng);
			break;
		case LOCATION_STATE_SUCCESS:
			// 定位成功
			// 需要弹出框让用户确认
			showDealDialog();
			break;
		case LOCATION_STATE_FAILED:
		case LOCATION_STATE_FORBID:
			// 未定位
			mLocationState = LOCATION_STATE_ING;
			refreshLocationUI();
			GetBaiduAddress.getInstance().searchAddress(LngLat.lat, LngLat.lng);
			break;
		}
	}

	private void refreshLocationUI() {
		switch (mLocationState) {
		case LOCATION_STATE_ING:
			// 改图标
			mAddressImg.setBackgroundResource(R.drawable.share_weizhi_failed);
			mAddressTv.setText(R.string.share_str_no_location);
			break;
		case LOCATION_STATE_SUCCESS:
			// 改变图标
			mAddressImg.setBackgroundResource(R.drawable.share_weizhi_success);
			mAddressTv.setText(mCurrentAddress);
			break;
		case LOCATION_STATE_FAILED:
		case LOCATION_STATE_FORBID:
			mAddressImg.setBackgroundResource(R.drawable.share_weizhi_failed);
			mAddressTv.setText(R.string.get_current_location);
			break;
		default:
			break;
		}
	}

	// 显示输入界面
	private void click_input() {
		if (null != mContext && mContext instanceof VideoEditActivity) {
			((VideoEditActivity) mContext).mInputLayout.show(mTextView.getText().toString().trim());
		}
	}

	@Override
	public void CallBack_BaiduGeoCoder(int function, Object obj) {
		if (LOCATION_STATE_FORBID == mLocationState) {
			// 当前是用户禁止定位状态,不显示位置
			return;
		}
		if (function == GetBaiduAddress.FUN_GET_ADDRESS && obj != null) {
			ReverseGeoCodeResult result = (ReverseGeoCodeResult) obj;
			AddressComponent addressDetail = result.getAddressDetail();
			if (null == addressDetail) {
				return;
			}
			// 当前城市
			String city = addressDetail.city;
			// 区，县
			String district = addressDetail.district;
			mCurrentAddress = city + "·" + district;
			mLocationState = LOCATION_STATE_SUCCESS;
			refreshLocationUI();
		} else {
			mLocationState = LOCATION_STATE_FAILED;
			refreshLocationUI();
		}
	}

	@Override
	public void CallBack_Del(int event, Object data) {
		if (1 == event) {
			// 重新定位
			mLocationState = LOCATION_STATE_ING;
			refreshLocationUI();
			GetBaiduAddress.getInstance().searchAddress(LngLat.lat, LngLat.lng);
		} else if (2 == event) {
			// 删除定位
			mLocationState = LOCATION_STATE_FORBID;
			refreshLocationUI();
		}

	}

	public boolean getPopupFlag() {
		return bPopup;
	}

	@SuppressWarnings("deprecation")
	public void showPopUp() {
		View contentView = mLayoutFlater
				.inflate(R.layout.promotion_popup_hint, null);

		contentView.measure(View.MeasureSpec.UNSPECIFIED,
				View.MeasureSpec.UNSPECIFIED);
		int popWidth = contentView.getMeasuredWidth();
		int popHeight = contentView.getMeasuredHeight();
		mPopupWindow = new PopupWindow(contentView, popWidth, popHeight);
		contentView.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				mPopupWindow.dismiss();
				return false;
			}
		});
	
		mPopupWindow.setOutsideTouchable(true);
		mPopupWindow.setBackgroundDrawable(new BitmapDrawable());

		int[] location = new int[2];
		mPromotionTextView1.getLocationOnScreen(location);

		mPopupWindow.showAtLocation(mPromotionTextView1, Gravity.NO_GRAVITY,
				location[0], location[1] - popHeight);
		bPopup = false;
		GolukFileUtils.saveBoolean(GolukFileUtils.SHOW_PROMOTION_POPUP_FLAG, false);
	}
	
	public void refreshPromotionUI() {
		if (mPromotionSelectItem != null) {
			mPromotionTextView1.setTextColor(Color.parseColor("#ffcc00"));
			mPromotionTextView2.setTextColor(Color.parseColor("#bcbdbd"));
			mPromotionTextView1.setText("#" + mPromotionSelectItem.activitytitle + "#");
			mPromotionTextView1.setTag(mPromotionSelectItem);
			/** 移除掉和选中活动相同的活动，把选择的活动放到第一位 */
			if (mRecommendActivities.size() > 0) {
				PromotionSelectItem item = mRecommendActivities.get(0);
				if (!mPromotionSelectItem.activityid.equalsIgnoreCase(item.activityid)) {
					mPromotionTextView2.setText("#" + item.activitytitle + "#");
					mPromotionTextView2.setTag(item);
				} else if (mRecommendActivities.size() > 1) {
					mPromotionTextView2.setText("#" + mRecommendActivities.get(1).activitytitle + "#");
					mPromotionTextView2.setTag(mRecommendActivities.get(1));
				}
			}
		} else {
			mPromotionTextView1.setText("");
			mPromotionTextView2.setText("");
			for (int i = 0; i < mRecommendActivities.size(); i++) {
				if (i == 0) {
					mPromotionTextView1.setText("#" + mRecommendActivities.get(i).activitytitle + "#");
					mPromotionTextView1.setTag(mRecommendActivities.get(i));
				} else {
					mPromotionTextView2.setText("#" + mRecommendActivities.get(i).activitytitle + "#");
					mPromotionTextView2.setTag(mRecommendActivities.get(i));
				}
			}
			mPromotionTextView1.setTextColor(Color.parseColor("#bcbdbd"));
			mPromotionTextView2.setTextColor(Color.parseColor("#bcbdbd"));
		}	
	}

	public void onActivityResult(int resultCode, Intent data) {
		if (resultCode != Activity.RESULT_OK || data == null) {
			return;
		}

		mPromotionSelectItem = (PromotionSelectItem) data.getSerializableExtra(PromotionActivity.PROMOTION_SELECTED_ITEM);
		if (mPromotionSelectItem != null && !TextUtils.isEmpty(mPromotionSelectItem.activitytitle)) {
			switchOpenAndClose(true);
		}
		refreshPromotionUI();
	}
	
	public PromotionSelectItem getPromotionSelectItem() {
		return mPromotionSelectItem;
	}
	
	public void setPromotionList(ArrayList<PromotionData> list, ArrayList<PromotionSelectItem> recommendList) {
		if (list == null) {
			return;
		}
		mPromotionList = list;
		mRecommendActivities = recommendList;
		refreshPromotionUI();
		showNewFlag();
	}

	private void showNewFlag() {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutput out = null;
		String md5 = "";
		try {
		  out = new ObjectOutputStream(bos);   
		  out.writeObject(mPromotionList);
		  byte[] content = bos.toByteArray();
		  md5 = GolukUtils.compute32(content);
		} catch (IOException ex) {

		} finally {
		  try {
		    if (out != null) {
		      out.close();
		    }
		    bos.close();
		  } catch (IOException ex) {
		    // ignore close exception
		  }
		}

		mMd5String = GolukFileUtils.loadString(GolukFileUtils.PROMOTION_LIST_STRING, "");
		if (TextUtils.isEmpty(mMd5String) || !mMd5String.equalsIgnoreCase(md5)) {
			bShowNew = true;
			mNewFlagTextView.setVisibility(View.VISIBLE);
			mMd5String = md5;
		}
	}
	
	private void showToast(int id) {
		if (mToast == null) {
			mToast = Toast.makeText(mContext, "", Toast.LENGTH_SHORT);
			mToast.setGravity(Gravity.CENTER, 0, 0);
		}
		mToast.setText(id);
		mToast.show();
	}
}
