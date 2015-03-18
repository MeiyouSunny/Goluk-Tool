package cn.com.mobnote.map;

import cn.com.mobnote.golukmobile.R;
import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class BubbleView extends LinearLayout {

	private TextView mRefreshTime = null;
	private TextView mLibraryType = null;
	private TextView mDistance = null;
	private TextView mCompany = null;
	private TextView mAddress = null;
	private TextView mTheme = null;

	private Context mContext = null;

	private View mView = null;

	public BubbleView(Context context, PoiDetailInfo info) {
		super(context);
		mContext = context;
		initView();
		//setPoiDetail(info);
	}

	public BubbleView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		initView();
	}

	public BubbleView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		initView();
	}

	/**
	 * 设置poi详情信息
	 * 
	 * @param info
	 */
	private void setPoiDetail(PoiDetailInfo info) {
//		if (info != null) {
//			if(!TextUtils.isEmpty(info.getRefreshTime())) {
//				mRefreshTime.setVisibility(View.VISIBLE);
//				mRefreshTime.setText(info.getRefreshTime());
//			} else {
//				mRefreshTime.setVisibility(View.INVISIBLE);
//			}
//			if (!TextUtils.isEmpty(info.getLibraryType())) {
//				StringBuffer str = new StringBuffer();
//				str.append(info.getLibraryType());
//				if (!TextUtils.isEmpty(info.getOpportunityStage())) {
//					str.append("(" + info.getOpportunityStage() + ")");
//				}
//				mLibraryType.setText(str.toString());
//			}
//			if (!TextUtils.isEmpty(info.getCustomerName())) {
//				mCompany.setText(info.getCustomerName());
//			} else {
//				mCompany.setText(R.string.no_customer_name);
//			}
//			if (!TextUtils.isEmpty(info.getOpportunityAddress())) {
//				mAddress.setText(info.getOpportunityAddress());
//			} else {
//				mAddress.setText(R.string.no_opportunity_address);
//			}
//			if (!TextUtils.isEmpty(info.getOpportunityName())) {
//				mTheme.setText(info.getOpportunityName());
//			} else {
//				mTheme.setText(R.string.no_opportunity_name);
//			}
//			float dis = info.getDistance();
//			if (dis >= 1) {
//				float distance = Math.round(dis * 100) / 100;
//				mDistance.setText(distance + "km");
//			} else {
//				int distance = (int) (dis * 1000);
//				mDistance.setText(distance + "m");
//			}
//		}
	}

	private void initView() {
		mView = LayoutInflater.from(mContext).inflate(R.layout.bubble, null);
//		mRefreshTime = (TextView) mView.findViewById(R.id.poi_bubble_refresh_time);
//		mLibraryType = (TextView) mView.findViewById(R.id.poi_bubble_library_type);
//		mDistance = (TextView) mView.findViewById(R.id.poi_bubble_distance);
//		mCompany = (TextView) mView.findViewById(R.id.poi_bubble_company);
//		mAddress = (TextView) mView.findViewById(R.id.poi_bubble_address);
//		mTheme = (TextView) mView.findViewById(R.id.poi_bubble_theme);
		int width = 150;//(int) (CRMAppUtil.getDisplayMetrics(mCtx).widthPixels * 0.95);
		LayoutParams params = new LayoutParams(width,90);// LayoutParams.WRAP_CONTENT
		this.addView(mView, params);
		
//		this.addView(mView);
	}

	public void setBackgroundResource(int resid) {
		mView.setBackgroundResource(resid);
	}
}

