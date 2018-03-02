package com.mobnote.golukmain.newest;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.widget.AbsListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.mobnote.application.GolukApplication;
import com.mobnote.eventbus.EventLoginSuccess;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.carrecorder.util.SettingUtils;
import com.mobnote.golukmain.carrecorder.view.CustomLoadingDialog;
import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.videosuqare.RTPullListView;
import com.mobnote.golukmain.videosuqare.RTPullListView.OnRTScrollListener;
import com.mobnote.golukmain.videosuqare.RTPullListView.OnRefreshListener;
import com.mobnote.golukmain.videosuqare.VideoSquareManager;
import com.mobnote.util.GolukUtils;
import com.mobnote.util.SharedPrefUtil;
import com.mobnote.util.ZhugeUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.mobnote.module.videosquare.VideoSuqareManagerFn;
import cn.com.tiros.debug.GolukDebugUtils;
import de.greenrobot.event.EventBus;

@SuppressLint("InflateParams")
public class WonderfulSelectedListView implements VideoSuqareManagerFn {
	private RelativeLayout mRootLayout = null;
	private Context mContext = null;
	private RTPullListView mRTPullListView = null;
	public List<JXListItemDataInfo> mDataList = null;
	private CustomLoadingDialog mCustomProgressDialog = null;
	private WonderfulSelectedAdapter mWonderfulSelectedAdapter = null;
	private String historyDate;
	@SuppressLint("SimpleDateFormat")
	private SimpleDateFormat sdf = null;
	/** 列表添加页脚标识 */
	private boolean addFooter = false;
	/** 添加列表底部加载中布局 */
	private RelativeLayout mBottomLoadingView = null;
	private int pageCount = 4;
	private String mJxid = "0";
	private boolean isGetFileListDataing = false;
	/** 保存列表一个显示项索引 */
	private int firstVisible;
	/** 保存列表显示item个数 */
	private int visibleCount;
	private RelativeLayout shareBg = null;
	private static final String TAG = "WonderfulSelectedListView";

	private long requestId = 0;
	
	private TextView mRefresh = null;
	
	private AlphaAnimation mHideAnimation = null;

	/** 上拉加载次数 **/
	private int mPushCount = 0;

	public WonderfulSelectedListView(Context context) {
		mContext = context;
		EventBus.getDefault().register(this);
		sdf = new SimpleDateFormat(mContext.getString(R.string.str_date_formatter));
		mDataList = new ArrayList<JXListItemDataInfo>();
		mRTPullListView = new RTPullListView(mContext);
		mRTPullListView.setDividerHeight(0);
		mRTPullListView.setDivider(new ColorDrawable(Color.TRANSPARENT));
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
				RelativeLayout.LayoutParams.MATCH_PARENT);
		mRTPullListView.setLayoutParams(lp);
		mRootLayout = new RelativeLayout(mContext);
		shareBg = (RelativeLayout) View.inflate(context, R.layout.video_square_bj, null);
		if(mRefresh == null){
			mRefresh = this.createTextView(mContext);
			RelativeLayout.LayoutParams rl = (RelativeLayout.LayoutParams)mRefresh.getLayoutParams();
		}
		initListener();
		historyDate = SettingUtils.getInstance().getString("hotHistoryDate", "");
		if ("".equals(historyDate)) {
			historyDate = sdf.format(new Date());
		}
		SettingUtils.getInstance().putString("hotHistoryDate", sdf.format(new Date()));

		VideoSquareManager mVideoSquareManager = GolukApplication.getInstance().getVideoSquareManager();
		if (null != mVideoSquareManager) {
			mVideoSquareManager.addVideoSquareManagerListener("wonderfulSelectedList", this);
		}

		RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		rlp.addRule(RelativeLayout.CENTER_IN_PARENT);
		mRootLayout.addView(shareBg, rlp);
		mRootLayout.addView(mRTPullListView);
		mRootLayout.addView(mRefresh);
		if (null == mWonderfulSelectedAdapter) {
			mWonderfulSelectedAdapter = new WonderfulSelectedAdapter(mContext);
		}
		mRTPullListView.setAdapter(mWonderfulSelectedAdapter);
		initHistoryData();
		setViewListBg(false);
		httpPost(true, "0", "");
//		loadBannerData();
		loadHistoryBanner();

		shareBg.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				GolukDebugUtils.e("", "request-----------------------no DAta");
				setViewListBg(false);
				httpPost(true, "0", "");

			}
		});
	}
	
	public TextView createTextView(Context context){
		TextView tv = new TextView(context);
		RelativeLayout.LayoutParams para = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,81);
		tv.setBackgroundColor(context.getResources().getColor(R.color.color_square_refresh_msg_bg));
		tv.setTextColor(context.getResources().getColor(R.color.color_square_refresh_msg));
		tv.setTextSize(12);
		tv.setLayoutParams(para);
		tv.setGravity(Gravity.CENTER);
		tv.setVisibility(View.GONE);
		return tv;
	}

	public void loadBannerData(String cityCode) {
		BannerListRequest request = new BannerListRequest(IPageNotifyFn.PageType_BannerGet, mBannerRequestListener);
		request.get(cityCode);
	}

	private IRequestResultListener mBannerRequestListener = new IRequestResultListener() {
		@Override
		public void onLoadComplete(int requestType, Object result) {
			BannerModel model = (BannerModel)result;

			do {
				if(null == model) {
					GolukDebugUtils.d(TAG, "Can't get banner");
					break;
				}

				if(!model.isSuccess() || null == model.getData()) {
					GolukDebugUtils.d(TAG, model.getMsg());
					Toast.makeText(mContext, model.getMsg(), Toast.LENGTH_SHORT).show();
					break;
				}
				mWonderfulSelectedAdapter.setBannerData(model.getData());

				// Save banner data
				String bannerJson = com.alibaba.fastjson.JSON.toJSONString(model);
				SharedPrefUtil.saveBannerListString(bannerJson);
			} while(false);

			return;
		}
	};

	private void loadHistoryBanner() {
		String json = SharedPrefUtil.getBannerListString();
		GolukDebugUtils.d(TAG, "banner string=" + json);
        if (TextUtils.isEmpty(json))
            return;

        BannerModel model = (BannerModel) com.alibaba.fastjson.JSON.parseObject(json, BannerModel.class);
        if (null != model) {
            mWonderfulSelectedAdapter.setBannerData(model.getData());
        }
    }

	private void initHistoryData() {
		String data = GolukApplication.getInstance().getVideoSquareManager().getJXList();
		if (!TextUtils.isEmpty(data)) {
			initLayout(JsonParserUtils.parserJXData(data));
		}
	}

	private void httpPost(boolean flag, String jxid, String pagesize) {
		if (GolukUtils.isCurrWifiGolukT(mContext)) {
			return;
		}
		if (isGetFileListDataing) {
			return;
		}

		this.mJxid = jxid;
		if (flag) {
			mRTPullListView.firstFreshState();
		}

		if (null != GolukApplication.getInstance().getVideoSquareManager()) {
			isGetFileListDataing = true;
			GolukDebugUtils.e("", "TTTTTT=====11111=====jxid=" + jxid);
			requestId = GolukApplication.getInstance().getVideoSquareManager().getJXListData(jxid, pagesize);
			GolukDebugUtils.e("", "TTTTTT=====11111=====requestId=" + requestId);
			if (requestId <= 0) {
				closeProgressDialog();
			}
		} else {
			closeProgressDialog();
		}
	}

	private void closeProgressDialog() {
		if (null != mCustomProgressDialog) {
			mCustomProgressDialog.close();
		}
	}

	private void initLayout(List<JXListItemDataInfo> list) {
		mDataList.addAll(list);

		if (!addFooter) {
			addFooter = true;
			mBottomLoadingView = (RelativeLayout) LayoutInflater.from(mContext).inflate(
					R.layout.video_square_below_loading, null);
			mRTPullListView.addFooterView(mBottomLoadingView);
		}

		if (pageCount < 4) {
			if (addFooter) {
				addFooter = false;
				mRTPullListView.removeFooterView(mBottomLoadingView);
			}
		}
		mWonderfulSelectedAdapter.setData(mDataList);
	}

	private void initListener() {
		mRTPullListView.setonRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				ZhugeUtils.eventWonderfulPull(mContext);
				historyDate = SettingUtils.getInstance().getString("hotHistoryDate", sdf.format(new Date()));
				SettingUtils.getInstance().putString("hotHistoryDate", sdf.format(new Date()));
				httpPost(false, "0", "");
				String cityCode = SharedPrefUtil.getCityIDString();

				if(null == cityCode || cityCode.trim().equals("")) {
					SharedPrefUtil.setCityIDString("-1");
					loadBannerData("-1");
				} else {
					loadBannerData(cityCode);
				}
			}
		});

		mRTPullListView.setOnRTScrollListener(new OnRTScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView arg0, int scrollState) {
				switch (scrollState) {
				case AbsListView.OnScrollListener.SCROLL_STATE_FLING:
					break;
				case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
					if (mRTPullListView.getAdapter().getCount() == (firstVisible + visibleCount)) {
						if (mDataList.size() > 0) {
							if (isGetFileListDataing) {
								return;
							}

							if (!addFooter) {
								addFooter = true;
								mBottomLoadingView = (RelativeLayout) LayoutInflater.from(mContext).inflate(
										R.layout.video_square_below_loading, null);
								mRTPullListView.addFooterView(mBottomLoadingView);
							}

							ZhugeUtils.eventWonderfulPush(mContext, (++mPushCount));
							httpPost(false, mDataList.get(mDataList.size() - 1).jxid, "");
						}
					}
					break;
				case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
					break;

				default:
					break;
				}
			}

			@Override
			public void onScroll(AbsListView arg0, int firstVisibleItem, int visibleItemCount, int arg3) {
				firstVisible = firstVisibleItem;
				visibleCount = visibleItemCount;
				if (null == mDataList && mDataList.size() <= 0) {
					return;
				}
			}

		});
	}

	

	public View getView() {
		return mRootLayout;
	}

	@Override
	public void VideoSuqare_CallBack(int event, int msg, int param1, Object param2) {
		GolukDebugUtils.e("", "TTTTTT=====2222=====param1=" + param1);
		if (event == VSquare_Req_List_HandPick && requestId == param1) {
			GolukDebugUtils.e("", "TTTTTT=====2222=====param2=" + param2);
			isGetFileListDataing = false;
			closeProgressDialog();
			mRTPullListView.onRefreshComplete(historyDate);

			if (RESULE_SUCESS == msg) {
				List<JXListItemDataInfo> list = JsonParserUtils.parserJXData((String) param2);
				pageCount = JsonParserUtils.parserJXCount((String) param2);
				if ("0".equals(mJxid)) {
					mDataList.clear();
				}

				if(null == list || list.size() <= 0) {
					Toast.makeText(mContext, mContext.getString(
							R.string.str_pull_refresh_listview_bottom_reach), Toast.LENGTH_SHORT).show();
				}
				initLayout(list);
				JSONObject json = JsonParserUtils.getRefreshCount((String) param2);
				if(json != null){
					//今日首次
					try {
						int refreshcount = json.getInt("refreshcount");
						if(refreshcount > 0){
							if(json.getInt("isfirst") == 1){
								startAnimation(mContext.getResources().getString(R.string.str_frist_refresh_begen_txt)+" "+ refreshcount+" "+mContext.getResources().getString(R.string.str_frist_refresh_end_txt));
							}else{
								startAnimation(mContext.getResources().getString(R.string.str_more_refresh_begen_txt)+" "+refreshcount+" "+mContext.getResources().getString(R.string.str_frist_refresh_end_txt));
							}
						}
						
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
				
			} else {
				if (!"0".equals(mJxid)) {
					if (addFooter) {
						addFooter = false;
						mRTPullListView.removeFooterView(mBottomLoadingView);
					}
				}

				GolukUtils.showToast(mContext, mContext.getString(R.string.network_error));
			}

			if (mDataList.size() > 0) {
				setViewListBg(false);
			} else {
				setViewListBg(true);
			}
		}

	}

	public void setViewListBg(boolean flog) {
		if (flog) {
			shareBg.setVisibility(View.VISIBLE);
			mRTPullListView.setVisibility(View.GONE);
		} else {
			shareBg.setVisibility(View.GONE);
			mRTPullListView.setVisibility(View.VISIBLE);
		}
	}
	
	public void startAnimation(String txt){
		
		if( null == mRefresh ){

	        return;

	    }
		mRefresh.setVisibility(View.VISIBLE);
		mRefresh.setText(txt);

	    if( null != mHideAnimation ){

	        mHideAnimation.cancel( );

	    }

	    mHideAnimation = new AlphaAnimation(1.0f, 0.0f);

	    mHideAnimation.setDuration(5000);

	    mHideAnimation.setFillAfter( true );

	    mRefresh.startAnimation( mHideAnimation );
	}

	public void onDestroy() {
		EventBus.getDefault().unregister(this);
	}



	public void onEventMainThread(EventLoginSuccess event) {
		shareBg.performClick();
	}
}
